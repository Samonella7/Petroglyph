package controller;

import controller.NetworkingLibrary.NetworkConnectionHandler;
import controller.NetworkingLibrary.NetworkUpdateHandler;
import model.SimpleParticipant;
import model.Participant.Direction;
import view.PetroglyphWindow;

import java.util.ArrayList;

import controller.NetworkingLibrary.NetworkConnection;

/**
 * A class to run a client side version of Petroglyph. This class handles logic
 * for connecting, sending user inputs to the server, and updating the view to
 * stay up-to-date with the server. <br>
 * See the {@link Server} class's documentation for details on the network
 * protocol.
 * 
 * @author Sam Thayer
 */
public class Client implements NetworkConnectionHandler, NetworkUpdateHandler {
	/** The connection to the server */
	private NetworkConnection connection;

	/** A reference to the top-level gui window, used during connection */
	private PetroglyphWindow window;

	/** A reference to the game display panel, used while the game is running */
	private GameUpdateHandler view;

	/** A reference to the game's MainController */
	private MainController controller;

	/** Whether the game is running or not */
	private boolean gameIsActive;

	/**
	 * Makes a Client that will immediately try to connect with a server at
	 * remoteIP.
	 */
	public Client(PetroglyphWindow window, MainController controller, String remoteIP) {
		this.window = window;
		this.controller = controller;
		this.gameIsActive = false;
		NetworkingLibrary.connectToServer(this, remoteIP, Server.MESSAGE_TERMINATOR);
	}

	/**
	 * Closes the connection to the server.
	 */
	public void close() {
		NetworkingLibrary.closeConnection(connection);
	}

	@Override
	public void connectionUpdate(NetworkConnection connection, boolean success, String message) {
		if (!success) {
			// If the server disconnected
			controller.lostConnection();
			return;
		}

		if (!gameIsActive) {
			// If this is the first message received, then the game is just starting up
			gameIsActive = true;
			controller.startGameAsClient();
			view = window.readyToLaunchAsClient();
		}

		// Even if this is the first message, it still has valid data:
		newMessage(message);
		// Ignore the return value. As per the Petroglyph protocol, malformed messages
		// are ignored.
		NetworkingLibrary.getData(connection, this);
	}

	/**
	 * Tries to parse the given message. If it is a valid message, takes appropriate
	 * action and returns true. Otherwise, returns false.
	 */
	private boolean newMessage(String message) {
		try {
			String key = message.substring(0, Server.MESSAGE_KEY_LENGTH);
			message = message.substring(Server.MESSAGE_KEY_LENGTH);

			if (key.equals(Server.START_ROUND_KEY)) {
				int roundNum = Integer.parseInt(message);
				view.startRound(roundNum);
			}

			else if (key.equals(Server.NEW_FRAME_KEY)) {
				String[] rawParticipants = message.split(Character.toString(Server.PARTICIPANT_SEPERATOR));
				ArrayList<SimpleParticipant> parsedParticipants = new ArrayList<SimpleParticipant>();

				for (String rp : rawParticipants) {
					SimpleParticipant pp = new SimpleParticipant(rp);
					parsedParticipants.add(pp);
				}

				view.newFrame(parsedParticipants.toArray(new SimpleParticipant[parsedParticipants.size()]));
			}

			else if (key.equals(Server.ROUND_WIN_KEY)) {
				view.roundWin(null);
			}

			else if (key.equals(Server.ROUND_LOSS_KEY)) {
				view.roundLoss();
				close();
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
	public void initialConnectionUpdate(NetworkConnection connection, boolean success) {
		// This is called by the NetworkingLibrary shortly after the client is created
		if (success) {
			// If we connected, get ready for the game to start
			this.connection = connection;
			NetworkingLibrary.getData(connection, this);
			window.connectedAsClient();
		} else {
			// If connection failed, let the user know
			window.failedToConnectAsClient();
		}
	}

	/**
	 * Has the caveman throw its spear if he is holding it.
	 */
	public void tryThrowSpear() {
		// Caveman can't do anything unless the game has started
		if (gameIsActive)
			NetworkingLibrary.send(connection, Server.THROW_SPEAR_KEY);
	}

	/**
	 * Has the caveman begin trying to move in the given direction.
	 */
	public void beginMovement(Direction direction) {
		// Caveman can't do anything unless the game has started
		if (gameIsActive)
			NetworkingLibrary.send(connection, Server.BEGIN_MOVEMENT_KEY + direction.toString());
	}

	/**
	 * Has the caveman stop trying to move in the given direction.
	 */
	public void endMovement(Direction direction) {
		// Caveman can't do anything unless the game has started
		if (gameIsActive)
			NetworkingLibrary.send(connection, Server.END_MOVEMENT_KEY + direction.toString());
	}

}
