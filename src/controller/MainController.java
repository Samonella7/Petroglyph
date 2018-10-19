package controller;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.io.IOException;

import model.Participant.Direction;
import view.GameView;
import view.PetroglyphWindow;

/**
 * The mother of the entire Petroglyph application. This class facilitates
 * communication between the gui, the game engine, and the networking code while
 * the user starts and ends games. While the game is in progress, however, those
 * branches of the application can communicate directly with each other.
 * 
 * @author Sam Thayer
 */
public class MainController implements KeyEventDispatcher {
	public static void main(String[] args) {
		new MainController();
	}

	/**
	 * An array of 5 Virtual Key Codes that will be used as the controls for player
	 * 0. In order, they represent: "Move up," "Move left," "Move down," "Move
	 * right," and "Throw spear."
	 */
	public static int[] P0Keys = { KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT,
			KeyEvent.VK_SHIFT };
	/**
	 * An array of 5 Virtual Key Codes that will be used as the controls for player
	 * 1. In order, they represent: "Move up," "Move left," "Move down," "Move
	 * right," and "Throw spear."
	 */
	public static int[] P1Keys = { KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_E };
	/**
	 * An array of 5 Virtual Key Codes that will be used as the controls for player
	 * 2. In order, they represent: "Move up," "Move left," "Move down," "Move
	 * right," and "Throw spear."
	 */
	public static int[] P2Keys = { KeyEvent.VK_U, KeyEvent.VK_H, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_I };

	/** A reference the GameEngine, if a game is active. null otherwise. */
	private GameEngine gameEngine;
	/**
	 * A reference to the Server, if this instance of the game is acting as one.
	 * null otherwise.
	 */
	private Server server;
	/**
	 * A reference to the Client, if this instance of the game is acting as one.
	 * null otherwise.
	 */
	private Client client;

	/** A reference to the gui */
	private PetroglyphWindow window;

	/** Tells whether there is an active game or not */
	private boolean gameIsActive;
	/**
	 * Tells how many of the cavemen are being controlled by this instance of
	 * Petroglyph
	 */
	private int localPlayerCount;

	/**
	 * Starts a new instance of Petroglyph
	 */
	public MainController() {
		window = new PetroglyphWindow(this, this);
	}

	/**
	 * Starts a local game
	 */
	public void startLocalGame(GameView view, int startingLevel) {
		gameIsActive = true;
		localPlayerCount = 3;
		gameEngine = new GameEngine(view, startingLevel);
		gameEngine.startRound();
	}

	/**
	 * Attempts to start a server that will eventually host a game.
	 * 
	 * @param localPlayerCount
	 *            The number of cavemen that will be controlled by this instance of
	 *            the game. Should be 1 or 2.
	 * @return True if the server started up successfully, false otherwise.
	 */
	public boolean startServer(int localPlayerCount) {
		this.localPlayerCount = localPlayerCount;
		try {
			this.server = new Server(window, this, localPlayerCount);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Attempts to connect to a server at the given host.
	 */
	public void startClient(String remoteIP) {
		this.localPlayerCount = 1;
		this.client = new Client(window, this, remoteIP);
		// We can't immediately return true/false to indicate success, because the
		// asynchronous network code hasn't finished yet.
		// From here on, the Client will make sure things are kept up to date.
	}

	/**
	 * Cancels any connections that are in progress, whether this instance of the
	 * game is acting as a Server, Client, or neither (in which case this method
	 * does nothing).
	 */
	public void cancelConnection() {
		if (server != null) {
			server.close();
			server = null;
		} else if (client != null) {
			client.close();
			client = null;
		}
	}

	/**
	 * Closes all in-progress games and/or connections.
	 */
	public void lostConnection() {
		if (gameIsActive) {
			gameEngine.close();
			gameEngine = null;
		}
		if (client != null) {
			// One connection was lost. Since this is a client, the only connection that
			// could have been lost is the one to the server, so there's no need to close
			// that connection.
			client = null;
		} else if (server != null) {
			// If this is a server, however, there could be another client connected, or
			// maybe even a Listener that is trying to accept more connections. However, for
			// this simple game, it's best to just quit everything if anything goes wrong on
			// the network:
			server.close();
			server = null;
		}
		// Also, have the gui take any necessary actions:
		window.lostConnection();
	}

	/**
	 * Resets this instance of the game to account for a game ending.
	 */
	public void gameOver() {
		// Remember that the server/game engine/view communicate directly while the game
		// is running.
		// This method is how they signal that that process is over.
		gameIsActive = false;
		gameEngine = null;
	}

	// Key presses/releases are sent directly here from the gui
	@Override
	public boolean dispatchKeyEvent(KeyEvent rawInput) {
		if (!gameIsActive)
			return false;

		if (rawInput.getID() == KeyEvent.KEY_PRESSED)
			handleKeyPress(rawInput.getKeyCode());
		else if (rawInput.getID() == KeyEvent.KEY_RELEASED)
			handleKeyRelease(rawInput.getKeyCode());

		return false;
	}

	/**
	 * Helper method that processes key presses
	 */
	private void handleKeyPress(int keyCode) {
		// There are two needed pieces of information: which player is the command for,
		// and which command is it
		int inputCode = lookForKeyMatch(keyCode, P0Keys);
		int playerNum = 0;

		// Only check for extra cavemen's input if this instance of the game is
		// controlling that many
		// (and of course if we haven't already identified the key)
		if (inputCode == -1 && localPlayerCount > 1) {
			inputCode = lookForKeyMatch(keyCode, P1Keys);
			playerNum = 1;
		}
		if (inputCode == -1 && localPlayerCount > 2) {
			inputCode = lookForKeyMatch(keyCode, P2Keys);
			playerNum = 2;
		}

		// if we never found a match, ie if the key isn't a valid instruction
		if (inputCode == -1)
			return;

		// Now that information is gathered, so pass it on to the GameEngine:

		if (inputCode == 4) {
			gameEngine.tryThrowSpear(playerNum);
			return;
		}

		Direction direction = directionFromInputCode(inputCode);
		gameEngine.beginMovement(playerNum, direction);
	}

	/**
	 * Helper method that processes key releases
	 */
	private void handleKeyRelease(int keyCode) {
		int inputCode = lookForKeyMatch(keyCode, P0Keys);
		int playerNum = 0;

		if (inputCode == -1 && localPlayerCount > 1) {
			inputCode = lookForKeyMatch(keyCode, P1Keys);
			playerNum = 1;
		}
		if (inputCode == -1 && localPlayerCount > 2) {
			inputCode = lookForKeyMatch(keyCode, P2Keys);
			playerNum = 2;
		}

		if (inputCode == -1 || inputCode == 4) {
			// ignore release of the 'throw spear' button
			return;
		}

		Direction direction = directionFromInputCode(inputCode);
		gameEngine.endMovement(playerNum, direction);
	}

	/**
	 * If keycode is a valid input for a specific caveman (ie if keycode is
	 * contained in keyBindings) then returns the "input code" for the key. This is
	 * just keyCode's index in keyBindings, but its value has meaning:
	 * 
	 * <ul>
	 * <li>0-3: Directional inputs, that can be decoded by directionFromInputCode
	 * <li>4: Input for the caveman to throw its spear
	 * </ul>
	 * 
	 * Returns -1 if keyCode is not a valid input.
	 */
	private int lookForKeyMatch(int keyCode, int[] keyBindings) {
		for (int i = 0; i < keyBindings.length; i++) {
			if (keyBindings[i] == keyCode) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Assumes that inputCode is a directional input (ie it is on the interval
	 * [0,3]) and returns the Direction associated with it.
	 */
	private Direction directionFromInputCode(int inputCode) {
		switch (inputCode) {
		case 0:
			return Direction.up;
		case 1:
			return Direction.left;
		case 2:
			return Direction.down;
		default:
			return Direction.right;
		}
	}
}
