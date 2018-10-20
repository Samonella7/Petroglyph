package controller;

import java.io.IOException;
import java.nio.channels.WritePendingException;
import java.util.ArrayList;

import controller.NetworkingLibrary.NetworkConnectionHandler;
import controller.NetworkingLibrary.NetworkUpdateHandler;
import model.Participant;
import model.SimpleParticipant;
import model.Participant.Direction;
import view.PetroglyphWindow;
import controller.NetworkingLibrary.NetworkConnection;
import controller.NetworkingLibrary.NetworkListener;

/**
 * A class to run a server version of Petroglyph. This class handles listening
 * for client connections, keeping the gui up-to-date during the connection
 * process, sending game updates to clients, and passing clients' user input to
 * the MainController. <br>
 * <br>
 * Here is the definition of Petroglyph's network protocol: <br>
 * <ol>
 * <li>When the client first connects<br>
 * No messages are sent either way until the server is ready to launch the
 * game.</li>
 * <li>Unexpected disconnects<br>
 * If at any time any connection is broken, the game is considered unplayable
 * and all other connections, if any, are immediately closed. The same is true
 * if any malformed messages are received by either the client or the server;
 * the receiving end should immediately close the connection.
 * <li>Message format<br>
 * All messages, either server-to-client or client-to-server, follow a simple
 * format. Messages start with a key that identifies the message's type. All
 * keys have the same length, which is defined by a static constant in the
 * Server class. The key is followed by zero or more bytes, as defined by the
 * message type. Finally, all messages are terminated by the same character,
 * which is defined by a static constant in the Server class.</li>
 * <li>When the game launches<br>
 * The server will send a START_NEW_ROUND message (see below). From this point
 * until the game finishes, both the server and client may send any number of
 * the following messages in any order.</li>
 * </ol>
 * <h2>Server to client messages</h2>
 * <ul>
 * <li>NEW_FRAME<br>
 * This is the largest type of message. The message key is followed by numerous
 * String representations of {@link SimpleParticipant}s, which represent the
 * complete state of the game during the new frame. These Strings are separated
 * by a separator character that is defined by a static constant in the Server
 * class.</li>
 * <li>ROUND_WIN<br>
 * This message indicates that the players won a round. It will eventually be
 * followed by a START_NEW_ROUND message, without any other messages coming from
 * the server in between them. This message contains no extra data: the message
 * key is immediately followed by the message terminator.
 * <ul>
 * <li>START_NEW_ROUND<br>
 * This type of message is only sent at the very begining of the game and after
 * ROUND_WIN messages. The message's key is followed only by an integer value
 * representable by java's standard int type. This value indicates the level of
 * the upcoming round.</li>
 * </ul>
 * </li>
 * <li>ROUND_LOSS<br>
 * This message indicates that the players lost a round, thereby loosing the
 * entire game. Since the game is over, the server will promptly close all its
 * connections after sending this message, and clients will promptly close their
 * connections after receiving it. This message has no extra data: the message
 * key is immediately followed by the message terminator.</li>
 * </ul>
 * <h2>Client to server messages</h2>
 * <li>THROW_SPEAR<br>
 * This message indicates that the sending client's player wants to throw their
 * spear. This message has no extra data: the message key is immediately
 * followed by the message terminator.</li>
 * <li>BEGIN_MOVEMENT_KEY<br>
 * This message indicates that the sending client's player wants to move their
 * caveman. The message key is followed only by a String representation of a
 * {@link Participant.Direction}, which is the direction the player wants to
 * move in.</li>
 * <ul>
 * <li>END_MOVEMENT_KEY<br>
 * This message indicates that the sending client's player wants their caveman
 * to stop moving in a certain direction. The message key is followed only by a
 * String representation of a {@link Participant.Direction}, which is the
 * direction the player wants to stop moving in.</li>
 * </ul>
 * 
 * @author Sam Thayer
 */
public class Server implements NetworkConnectionHandler, NetworkUpdateHandler, GameUpdateHandler {
	/** The object used to listen for new Clients */
	private NetworkListener listener;
	/** A list of connections to Clients */
	private ArrayList<NetworkConnection> allClients;
	/** The number of clients that are needed for this game to start */
	private int remotePlayersNeeded;

	/**
	 * A reference to the main application window, used for communicating with the
	 * user during connection
	 */
	private PetroglyphWindow window;
	/** A reference to the game's MainController */
	private MainController controller;

	// Constants used in the network protocol
	public static final char MESSAGE_TERMINATOR = '\n';
	public static final char PARTICIPANT_SEPERATOR = '\t';

	public static final String NEW_FRAME_KEY = "0";
	public static final String START_ROUND_KEY = "1";
	public static final String ROUND_WIN_KEY = "2";
	public static final String ROUND_LOSS_KEY = "3";

	public static final String THROW_SPEAR_KEY = "0";
	public static final String BEGIN_MOVEMENT_KEY = "1";
	public static final String END_MOVEMENT_KEY = "2";

	public static final int MESSAGE_KEY_LENGTH = 1;

	/**
	 * Tries to create a server that will immediately start listening for clients.
	 * 
	 * @throws IOException
	 *             If the server can't access the network. If this is thrown, the
	 *             created Server object should not be used.
	 */
	public Server(PetroglyphWindow window, MainController controller, int localPlayerCount) throws IOException {
		this.window = window;
		this.controller = controller;
		allClients = new ArrayList<NetworkConnection>();
		remotePlayersNeeded = 3 - localPlayerCount;
		listener = NetworkingLibrary.openServer(this, MESSAGE_TERMINATOR);

		if (listener == null) {
			throw new IOException();
		}
	}

	/**
	 * Closes this Server by disconnecting all its clients.
	 */
	public void close() {
		NetworkingLibrary.closeListener(listener);
		for (NetworkConnection s : allClients) {
			NetworkingLibrary.closeConnection(s);
		}
	}

	@Override
	public void initialConnectionUpdate(NetworkConnection connection, boolean success) {
		if (success) {
			allClients.add(connection);
			window.newConnectionAsServer(remotePlayersNeeded - allClients.size());
			NetworkingLibrary.getData(connection, this);
		}

		if (allClients.size() == remotePlayersNeeded) {
			window.readyToLaunchAsServer();
		} else {
			NetworkingLibrary.resumeAcceptingClients(listener);
		}
	}

	@Override
	public void connectionUpdate(NetworkConnection connection, boolean success, String message) {
		if (!success) {
			allClients.remove(connection);
			close();
			controller.lostConnection();
			return;
		}

		if (newMessage(connection, message)) {
			NetworkingLibrary.getData(connection, this);
		} else {
			close();
			controller.lostConnection();
		}

	}

	/**
	 * A helper method to parse incoming messages
	 * 
	 * @param connection
	 *            The connection that this message came from
	 * @param message
	 *            The incoming message
	 * @return
	 */
	private boolean newMessage(NetworkConnection connection, String message) {
		try {
			String key = message.substring(0, MESSAGE_KEY_LENGTH);
			message = message.substring(MESSAGE_KEY_LENGTH);

			int playerNum = 2;
			if (allClients.size() == 2) {
				// If there are two clients connected (the only possibility other than 1) then
				// figure out which client this message came from
				playerNum = allClients.get(0) == connection ? 1 : 2;
			}

			if (key.equals(THROW_SPEAR_KEY)) {
				controller.tryThrowSpear(playerNum);
			}

			else if (key.equals(BEGIN_MOVEMENT_KEY)) {
				controller.beginMovement(playerNum, Direction.valueOf(message));
			}

			else if (key.equals(END_MOVEMENT_KEY)) {
				controller.endMovement(playerNum, Direction.valueOf(message));
			}

			else {
				// if it didn't have a known key, it was an invalid message
				return false;
			}
		} catch (Exception e) {
			// If anything else goes wrong, it was an invalid message
			return false;
		}

		// Otherwise it was fine
		return true;
	}

	@Override
	public void newFrame(SimpleParticipant[] participants) {
		StringBuilder message = new StringBuilder();
		for (SimpleParticipant p : participants) {
			message.append(p.toString() + PARTICIPANT_SEPERATOR);
		}
		message.deleteCharAt(message.length() - 1);

		for (NetworkConnection c : allClients) {
			NetworkingLibrary.send(c, NEW_FRAME_KEY + message.toString());
		}
	}

	@Override
	public void startRound(int level) {
		for (NetworkConnection c : allClients)
			NetworkingLibrary.send(c, START_ROUND_KEY + level);
	}

	@Override
	public void roundWin(GameEngine engine) {
		for (NetworkConnection c : allClients) {
			NetworkingLibrary.send(c, ROUND_WIN_KEY);
		}
	}

	@Override
	public void roundLoss() {
		for (NetworkConnection c : allClients) {
			NetworkingLibrary.send(c, ROUND_LOSS_KEY);
		}
		close();
	}
}
