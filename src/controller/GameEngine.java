package controller;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import model.Model;
import model.Model.GameState;
import model.Participant.Direction;

public class GameEngine {

	public static final int MILLIES_PER_FRAME = 1000 / 40;

	public static final double INITIAL_MAMMOTH_SPEED = .004;
	public static final double MAMMOTH_SPEEDUP_PER_LEVEL = .0015;

	/** The model that runs the game */
	public Model model;

	/**
	 * An array of objects that should be updated for every frame and at the end of rounds.
	 */
	private GameUpdateHandler[] updateArray;

	/** The game clock */
	public Timer timer;

	/** The directional inputs that player 0 is currently pressing */
	ArrayList<Direction> P0Inputs;

	/** The directional inputs that player 1 is currently pressing */
	ArrayList<Direction> P1Inputs;

	/** The directional inputs that player 2 is currently pressing */
	ArrayList<Direction> P2Inputs;

	/** The current level */
	int level;

	/**
	 * Creates a GameEngine that will start the game at the given level, and updates the given gameView at every frame.
	 */
	public GameEngine(GameUpdateHandler[] updateArray, int startingLevel) {
		this.updateArray = updateArray;

		// -1 because it is incremented each time a level starts, even the first time:
		level = startingLevel - 1;

		P0Inputs = new ArrayList<Direction>();
		P1Inputs = new ArrayList<Direction>();
		P2Inputs = new ArrayList<Direction>();
	}

	/**
	 * Shuts down this GameEngine immediately. It is assumed that the caller will update the gui as well.
	 */
	public void close() {
		if (timer != null)
			timer.cancel();
	}

	/**
	 * Starts a new round of the game. This method should not be called while a game is in progress.
	 */
	public void startRound() {
		level++;
		model = new Model(INITIAL_MAMMOTH_SPEED + level * MAMMOTH_SPEEDUP_PER_LEVEL);

		for (GameUpdateHandler f : updateArray) {
			f.startRound(level);
		}

		timer = new Timer();
		timer.scheduleAtFixedRate(new newFrameHandler(), 0, MILLIES_PER_FRAME);
	}

	/**
	 * Has the identified caveman throw its spear if he is holding it.
	 */
	public void tryThrowSpear(int cavemanNumber) {
		model.tryThrowSpear(cavemanNumber);
	}

	/**
	 * Has the identified caveman begin trying to move in the given direction.
	 */
	public void beginMovement(int cavemanNumber, Direction direction) {
		ArrayList<Direction> currentInputs = getPlayerInputArray(cavemanNumber);
		if (!currentInputs.contains(direction)) {
			currentInputs.add(0, direction);
			model.directCaveman(cavemanNumber, direction, true);
		}
	}

	/**
	 * Has the identified caveman stop trying to move in the given direction.
	 */
	public void endMovement(int cavemanNumber, Direction direction) {
		ArrayList<Direction> currentInputs = getPlayerInputArray(cavemanNumber);
		if (currentInputs.remove(direction)) {
			if (currentInputs.isEmpty()) {
				model.directCaveman(cavemanNumber, direction, false);
			} else {
				model.directCaveman(cavemanNumber, currentInputs.get(0), true);
			}
		}
	}

	/**
	 * Returns a list of directions that the given caveman is trying to move
	 */
	private ArrayList<Direction> getPlayerInputArray(int cavemanNumber) {
		switch (cavemanNumber) {
		case 0:
			return P0Inputs;
		case 1:
			return P1Inputs;
		default:
			return P2Inputs;
		}
	}

	class newFrameHandler extends TimerTask {
		// This is the game clock; run() is called for every new frame.
		@Override
		public void run() {
			GameState state = model.calculateNextFrame();

			for (GameUpdateHandler f : updateArray)
				f.newFrame(model.getParticipantList());

			// end game if needed
			if (state == GameState.win) {
				timer.cancel();
				for (GameUpdateHandler f : updateArray)
					f.roundWin(GameEngine.this);
			} else if (state == GameState.loss) {
				timer.cancel();
				for (GameUpdateHandler f : updateArray)
					f.roundLoss();
			}
		}
	}

}
