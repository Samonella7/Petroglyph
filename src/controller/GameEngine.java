package controller;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import model.Model;
import model.Participant.Direction;
import view.GameView;

public class GameEngine extends TimerTask implements KeyEventDispatcher {

	public static final int MILLIES_PER_FRAME = 1000 / 40;

	/** The model that runs the game */
	public Model model;

	/**
	 * A gui to display the game. May be null, in which case this GameEngine is
	 * acting purely as a server
	 */
	public GameView view;

	/** The game clock */
	public Timer timer;

	/** The direcional inputs that player 0 is currently pressing */
	ArrayList<Direction> P0Inputs;

	/** The direcional inputs that player 1 is currently pressing */
	ArrayList<Direction> P1Inputs;

	/** The direcional inputs that player 2 is currently pressing */
	ArrayList<Direction> P2Inputs;

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

	public GameEngine(GameView gameView, int localPlayerCount) {
		model = new Model(.11);
		view = gameView;

		P0Inputs = new ArrayList<Direction>();
		if (localPlayerCount > 1)
			P1Inputs = new ArrayList<Direction>();
		if (localPlayerCount > 2)
			P2Inputs = new ArrayList<Direction>();

		timer = new Timer();
		timer.scheduleAtFixedRate(this, 0, MILLIES_PER_FRAME);
	}

	@Override
	public void run() {
		model.calculateNextFrame();

		if (view != null)
			view.update(model.getParticipantList());
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent rawInput) {
		if (rawInput.getID() == KeyEvent.KEY_PRESSED)
			handleKeyPress(rawInput.getKeyCode());
		else if (rawInput.getID() == KeyEvent.KEY_RELEASED)
			handleKeyRelease(rawInput.getKeyCode());

		return false;
	}

	private void handleKeyPress(int keyCode) {
		// There are two needed pieces of information: which player is the command for,
		// and which command is it
		int inputCode = lookForKeyMatch(keyCode, P0Keys);
		int playerNum = 0;
		ArrayList<Direction> playerInputArray = P0Inputs;

		if (inputCode == -1 && P1Inputs != null) {
			inputCode = lookForKeyMatch(keyCode, P1Keys);
			playerNum = 1;
			playerInputArray = P1Inputs;
		}
		if (inputCode == -1 && P2Inputs != null) {
			inputCode = lookForKeyMatch(keyCode, P2Keys);
			playerNum = 2;
			playerInputArray = P2Inputs;
		}

		// Now that information is gathered, so pass it on to the model:

		if (inputCode == -1)
			return;

		if (inputCode == 4) {
			model.tryThrowSpear(playerNum);
			return;
		}

		Direction direction;

		switch (inputCode) {
		case 0:
			direction = Direction.up;
			break;
		case 1:
			direction = Direction.left;
			break;
		case 2:
			direction = Direction.down;
			break;
		default:
			direction = Direction.right;
			break;
		}

		if (!playerInputArray.contains(direction)) {
			playerInputArray.add(0, direction);
			model.directCaveman(playerNum, direction, true);
		}
	}

	private void handleKeyRelease(int keyCode) {
		int inputCode = lookForKeyMatch(keyCode, P0Keys);
		int playerNum = 0;
		ArrayList<Direction> playerInputArray = P0Inputs;

		if (inputCode == -1 && P1Inputs != null) {
			inputCode = lookForKeyMatch(keyCode, P1Keys);
			playerNum = 1;
			playerInputArray = P1Inputs;
		}
		if (inputCode == -1 && P2Inputs != null) {
			inputCode = lookForKeyMatch(keyCode, P2Keys);
			playerNum = 2;
			playerInputArray = P2Inputs;
		}

		if (inputCode == -1 || inputCode == 4) {
			// ignore release of the 'throw spear' button
			return;
		}

		Direction direction;

		switch (inputCode) {
		case 0:
			direction = Direction.up;
			break;
		case 1:
			direction = Direction.left;
			break;
		case 2:
			direction = Direction.down;
			break;
		default:
			direction = Direction.right;
			break;
		}

		if (playerInputArray.remove(direction)) {
			if (playerInputArray.isEmpty()) {
				model.directCaveman(playerNum, direction, false);
			} else {
				model.directCaveman(playerNum, playerInputArray.get(0), true);
			}
		}
	}

	private int lookForKeyMatch(int keyCode, int[] keyBindings) {
		for (int i = 0; i < keyBindings.length; i++) {
			if (keyBindings[i] == keyCode) {
				return i;
			}
		}
		return -1;
	}

}
