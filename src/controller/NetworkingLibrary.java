
package controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

/**
 * This is a library of static methods for network connections.
 * 
 * The idea is that any program in need of a Tcp Server and Client can use these
 * methods and never need to know what a TcpListener or Socket is; they should
 * never use a method or object in System.Net.Sockets
 * 
 * Instead, they only need to know how to use these relatively simple methods.
 * There are just a few things to understand:
 * 
 * 1) The objects SocketState and Listener state should be thought of as your
 * connections; when you get one from one of these methods, save a reference so
 * you can call more methods with it.
 * 
 * 2) How to implement SocketCallback()s: There are a few things that every
 * method used as a SocketCallback should do: First, check whether
 * connectionIsValid (the boolean parameter). If it is not, understand that this
 * connection was closed and you should not try to send or receive any more
 * data. You don't need to call CloseConnection either, just stop trying to use
 * this connection. Second, use DataBuffer. All incoming messages will be stuck
 * onto the end of it; they only way they come off is if you remove them. You
 * should have a well defined message protocol, so it's up to you to parse
 * messages and remove them from DataBuffer. The ExampleChatServer and
 * ExampleChatClient both show how to do these things.
 * 
 * @Author Sam Thayer
 */
public class NetworkingLibrary {

	/** The port for communications to use */
	public static final int DEFAULT_PORT = 11000;

	/** The space available for short-term message storing */
	private static final int BUFFER_SIZE = 1024;

	/**
	 * An object that can be used to process updates to SocketConnections
	 */
	public interface NetworkUpdateHandler {
		/**
		 * Handles updates to a previously established connection.
		 * 
		 * @param connection
		 *            A {@link NetworkConnection} associated to this callback
		 * @param success
		 *            Whether or not this connection has ended.
		 * @param message
		 *            The message sent from the connection. message does not contain the
		 *            "messageTerminator" that was used to create this connection. If
		 *            connectionIsValdid is false, message is null.
		 */
		public void connectionUpdate(NetworkConnection connection, boolean success, String message);
	}

	/**
	 * An object that can be used to process new clients
	 */
	public interface NetworkConnectionHandler {
		/**
		 * Handles updates related to making a new connection.
		 * 
		 * @param connection
		 *            A {@link NetworkConnection} representing the new connection
		 * @param success
		 *            True if a client connected successfully. If this value is false,
		 *            then connection is null.
		 */
		public void initialConnectionUpdate(NetworkConnection connection, boolean success);
	}

	/*
	 * This instance exists only so we can make NetworkListener and
	 * NetworkConnection be nested classes, so that they can have private variables
	 * that are still accessible from the NetworkLibrary functions.
	 * 
	 * In short, java is silly.
	 */
	/** The only instance of the NetworkingLibrary object */
	private static NetworkingLibrary instance = new NetworkingLibrary();

	/** Creates a NetworkingLibrary object */
	private NetworkingLibrary() {
		// There is nothing to do, this is just so instance can work as explained
	}

	/**
	 * Returns this machine's IP address, or null if an error occurs.
	 */
	public static String getIP() {
		InetAddress adr;
		try {
			adr = InetAddress.getLocalHost();
			return adr.getHostAddress();
		} catch (UnknownHostException e) {
			return null;
		}
	}

	/**
	 * Starts a server that will wait for clients to connect. The method an object
	 * representing the connecting, unless there is an IO problem in which case it
	 * returns null.
	 * 
	 * When a client connects, the given handler will be given an object
	 * representing the connection. To resume listening for clients, use
	 * 
	 * If there is an error while listening, the given handler will be notified.
	 * 
	 * Whenever the program is finished using the {@link NetworkListener}, it should
	 * call closeListener with it, regardless of whether the listener is active or
	 * not.
	 * 
	 * @param handler
	 *            A functor to notify when a client connects.
	 * @param messageTerminator
	 *            A character that can be used to designate the end of messages.
	 *            This character should not be used in ANY message passed to
	 *            NetworkLibrary.send
	 */
	public static NetworkListener openServer(NetworkConnectionHandler handler, char messageTerminator) {
		try {
			AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open();
			NetworkListener listenerState = instance.new NetworkListener(listener, handler, messageTerminator);
			listener.bind(new InetSocketAddress(DEFAULT_PORT));
			// It's called 'resume' because that's how it will be used by outside classes
			resumeAcceptingClients(listenerState);

			return listenerState;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Makes the given {@link NetworkListener} resume accepting clients.
	 * 
	 * When a client connects, the {@link NetworkListener}'s handler will be given
	 * an object representing the connection. To resume listening for clients, use
	 * 
	 * If there is an error while listening, the {@link NetworkListener}'s handler
	 * will be notified.
	 */
	public static void resumeAcceptingClients(NetworkListener listenerState) {
		listenerState.listener.accept(listenerState,
				new CompletionHandler<AsynchronousSocketChannel, NetworkListener>() {
					@Override
					public void completed(AsynchronousSocketChannel result, NetworkListener listenerState) {
						if (!listenerState.isValid) {
							return;
						}

						NetworkConnection socketState = instance.new NetworkConnection(result,
								listenerState.messageTerminator);
						listenerState.callMe.initialConnectionUpdate(socketState, true);
					}

					@Override
					public void failed(Throwable exc, NetworkListener listenerState) {
						if (listenerState.isValid) {
							listenerState.callMe.initialConnectionUpdate(null, false);
						}
					}
				});
	}

	/**
	 * Closes the given listener. After calling this, you should consider the given
	 * NetworkListener to be useless.
	 */
	public static void closeListener(NetworkListener listener) {
		try {
			listener.isValid = false;
			listener.listener.close();
		} catch (IOException e) {
			// This might be a bad choice, but let's just ignore the exception. If it won't
			// let me close the listener there's not much I can do.
		}
	}

	/**
	 * Creates a connection for communicating with a server given by a hostname or
	 * ip address.
	 * 
	 * @param handler
	 *            A functor to be used when a connection is made
	 * @param hostName
	 *            The name of the server to connect to
	 * @param messageTerminator
	 *            A character that can be used to designate the end of messages.
	 *            This character should not be used in ANY message passed to
	 *            NetworkLibrary.send
	 */
	public static void connectToServer(NetworkConnectionHandler handler, String hostName, char messageTerminator) {
		try {
			AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
			InetSocketAddress hostAddress = new InetSocketAddress(hostName, DEFAULT_PORT);

			if (hostAddress.isUnresolved()) {
				handler.initialConnectionUpdate(null, false);
				return;
			}

			NetworkConnection connectionState = instance.new NetworkConnection(client, messageTerminator);
			connectionState.connectionCallback = handler;
			client.connect(hostAddress, connectionState, new CompletionHandler<Void, NetworkConnection>() {
				@Override
				public void completed(Void result, NetworkConnection connectionState) {
					connectionState.connectionCallback.initialConnectionUpdate(connectionState, true);
				}

				@Override
				public void failed(Throwable exc, NetworkConnection attachment) {
					connectionState.connectionCallback.initialConnectionUpdate(null, false);
				}

			});
		} catch (IOException e) {
			handler.initialConnectionUpdate(null, false);
			return;
		}
	}

	/**
	 * Tells the given socket to listen for data from its connection. When data is
	 * received, callback will be called.
	 */
	public static void getData(NetworkConnection connection, NetworkUpdateHandler callback) {
		connection.messageCallback = callback;
		connection.socket.read(connection.tempBuffer, connection, new CompletionHandler<Integer, NetworkConnection>() {
			@Override
			public void completed(Integer result, NetworkConnection connectionState) {
				// If, in between the user's call to getData and this callback being triggered,
				// the user called closeConnection, ignore this message
				if (!connectionState.isValid) {
					return;
				}

				// If the connection is closing:
				if (result == -1) {
					connectionState.isValid = false;
					connectionState.messageCallback.connectionUpdate(connectionState, false, null);
					return;
				}
				// Otherwise, get the message out of tempBuffer and into a better spot:
				connectionState.tempBuffer.flip();
				connectionState.largeBuffer
						.append(StandardCharsets.UTF_8.decode(connectionState.tempBuffer).toString());
				connectionState.tempBuffer.clear();

				// Now search through the large buffer to see if there is a complete message (or
				// even more than one) and if so, notify the user
				String data = connectionState.largeBuffer.toString();
				while (data.contains(Character.toString(connectionState.messageTerminator))) {
					int indexOfEndOfMessageChar = data.indexOf(connectionState.messageTerminator);
					String oneCompleteMessage = data.substring(0, indexOfEndOfMessageChar);
					data = data.substring(indexOfEndOfMessageChar + 1, data.length());
					connectionState.messageCallback.connectionUpdate(connectionState, true, oneCompleteMessage);
				}
				connectionState.largeBuffer = new StringBuilder(data);
			}

			@Override
			public void failed(Throwable exc, NetworkConnection connectionState) {
				if (!connectionState.isValid) {
					return;
				}
				connectionState.messageCallback.connectionUpdate(connectionState, false, null);
			}
		});
	}

	/** Sends the given data across the given connection */
	public static void send(NetworkConnection connection, String data) {
		ByteBuffer message = StandardCharsets.UTF_8.encode(data + connection.messageTerminator);

		connection.socket.write(message, connection, new CompletionHandler<Integer, NetworkConnection>() {
			// Don't do anything in response. For the kind of simple program that this
			// library is designed for:
			// a) the program doesn't care for confirmation that messages were sent
			// b) if the connection fails, it is sufficient to find out next time they call
			// getData
			@Override
			public void completed(Integer result, NetworkConnection connectionState) {
			}

			@Override
			public void failed(Throwable exc, NetworkConnection connectionState) {
			}
		});
	}

	/**
	 * Closes a {@link NetworkConnection}. After calling this method, the given
	 * {@link NetworkConnection} should be considered useless.
	 */
	public static void closeConnection(NetworkConnection connection) {
		try {
			connection.isValid = false;
			connection.socket.shutdownInput();
			connection.socket.shutdownOutput();
			connection.socket.close();
		} catch (IOException e) {
			// This might be a bad choice, but let's just ignore the exception. If it won't
			// let me close the socket there's not much I can do.
		}
	}

	/**
	 * An object that represents a connection, either to a server or to a client.
	 */
	public class NetworkConnection {
		/**
		 * The socket that this is a state for
		 */
		private AsynchronousSocketChannel socket;

		/** A functor to be used whenever new data arrives */
		private NetworkUpdateHandler messageCallback;

		/** A functor to be used when the connection is initiated */
		private NetworkConnectionHandler connectionCallback;

		/**
		 * The small buffer which the socket will save data to. From here, data should
		 * be promptly moved to "data," a StringBuilder
		 */
		private ByteBuffer tempBuffer;

		/**
		 * A large buffer to dump data from messageBuffer into. All incoming data will
		 * end up here before the program sees it.
		 */
		private StringBuilder largeBuffer;

		/**
		 * True unless the user has asked to close this connection, meaning any future
		 * messages should be ignored.
		 */
		private boolean isValid;

		/**
		 * A character used to denote the end of messages
		 */
		private char messageTerminator;

		/**
		 * Makes a NetworkConnection for the given socket
		 */
		private NetworkConnection(AsynchronousSocketChannel socket, char messageTerminator) {
			this.socket = socket;
			this.tempBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			this.largeBuffer = new StringBuilder();
			this.isValid = true;
			this.messageTerminator = messageTerminator;
		}
	}

	/**
	 * An object that represents a server that listens for clients.
	 */
	public class NetworkListener {
		/** The server socket that this is a state for */
		private AsynchronousServerSocketChannel listener;

		/**
		 * The object to be notified whenever a client attempts connecting to the
		 * TCPListener
		 */
		private NetworkConnectionHandler callMe;

		/**
		 * True unless the user has asked to close this listener, meaning any future
		 * messages should be ignored.
		 */
		private boolean isValid;

		/**
		 * A character used to denote the end of messages
		 */
		private char messageTerminator;

		/**
		 * Creates a state that contains information relevant to a TCPListener
		 */
		private NetworkListener(AsynchronousServerSocketChannel listener, NetworkConnectionHandler callback,
				char messageTerminator) {
			this.listener = listener;
			this.callMe = callback;
			this.isValid = true;
			this.messageTerminator = messageTerminator;
		}

	}
}
