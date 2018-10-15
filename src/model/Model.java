package model;

import java.awt.Color;

import model.Participant.Direction;
import model.Spear.SpearState;

/**
 * A class to represent the state of a game of Petroglyph.
 * 
 * All dimensions are measured in percents, so x=.5 is halfway accross the
 * board. All speeds are measured in percent per frame.
 * 
 * @author Sam Thayer
 */
public class Model {
	/** An array of references to the cavemen */
	private Caveman[] cavemen;
	/** An array of references to the cavemen's spears */
	private Spear[] spears;
	/** A reference to the mammoth */
	private Mammoth mammoth;
	
	/** A state that the game can be in */
	public enum GameState {
		running,
		win,
		loss
	}

	/**
	 * Returns a representation of all the game's participants.
	 * getParticipantList()[0] is a SimpleParticipant representing the Mammoth. Next
	 * are representations for the cavemen, and finally for the spears.
	 */
	public SimpleParticipant[] getParticipantList() {
		SimpleParticipant[] list = new SimpleParticipant[cavemen.length + spears.length + 1];

		list[0] = new SimpleParticipant(mammoth);

		int i = 1;
		for (; i < 1 + cavemen.length; i++) {
			list[i] = new SimpleParticipant(cavemen[i - 1]);
		}

		for (; i < list.length; i++) {
			list[i] = new SimpleParticipant(spears[i - 1 - cavemen.length]);
		}

		return list;
	}

	/**
	 * Creates a Model
	 * 
	 * @param mammothSpeedScaler
	 *            The maximum speed that the mammoth should be able to move at
	 */
	public Model(double mammothSpeedScaler) {
		cavemen = new Caveman[3];
		spears = new Spear[3];

		cavemen[0] = new Caveman(.1, .1, Color.red);
		spears[0] = cavemen[0].getSpear();

		cavemen[1] = new Caveman(.9 - Caveman.CAVEMAN_WIDTH, .1, Color.cyan);
		spears[1] = cavemen[1].getSpear();

		cavemen[2] = new Caveman(.5 - Caveman.CAVEMAN_WIDTH / 2, .8, Color.yellow);
		spears[2] = cavemen[2].getSpear();

		mammoth = new Mammoth(mammothSpeedScaler, cavemen);
	}

	/**
	 * Updates the model for the next frame of the game
	 */
	public GameState calculateNextFrame() {
		for (Caveman c : cavemen) {
			c.move();
		}
		for (Spear s : spears) {
			s.move();
		}

		mammoth.move();

		// Caveman can pick up their spear
		for (int i = 0; i < cavemen.length; i++) {
			if (spears[i].state == SpearState.grounded && cavemen[i].collidedWith(spears[i])) {
				spears[i].state = SpearState.held;
			}
		}
		
		// Cavemen can revive each other 
		for (int reviver = 0; reviver < cavemen.length; reviver++) {
			if (!cavemen[reviver].isConscious()) {
				continue;
			}
			for (int revivee = 0; revivee < cavemen.length; revivee++) {
				if (revivee == reviver) {
					continue;
				}
				if (!cavemen[revivee].isConscious() && cavemen[revivee].collidedWith(cavemen[reviver])) {
					cavemen[revivee].setConscious(true);
				}
			}
		}

		// The Mammoth knocks Cavemen unconscious
		for (int i = 0; i < cavemen.length; i++) {
			if (cavemen[i].collidedWith(mammoth)) {
				cavemen[i].setConscious(false);
			}
		}
		
		// Spears damage the mammoth
		for (int i = 0; i < spears.length; i++) {
			if (spears[i].state == SpearState.active && mammoth.collidedWith(spears[i])) {
				mammoth.takeDamage(i);
			}
		}
		
		// You win if the mammoth's hp reaches 0
		if (mammoth.getHP() == 0) {
			return GameState.win;
		}
		
		// You lose of all Cavemen are unconscious
		int consciousCount = 0;
		for (int i = 0; i < cavemen.length; i++) {
			if (cavemen[i].isConscious()) {
				consciousCount++;
			}
		}
		if (consciousCount == 0) {
			return GameState.loss;
		}
		
		// otherwise, the game is still in progress
		return GameState.running;
	}

	/**
	 * Controls a Caveman's movement
	 * 
	 * @param cavemanNumber
	 *            An identifier for a Caveman: 0, 1, or 2
	 * @param direction
	 *            The direction for the Caveman to face
	 * @param moving
	 *            True if the Caveman is moving, false otherwise
	 */
	public void directCaveman(int cavemanNumber, Direction direction, boolean moving) {
		cavemen[cavemanNumber].setDirection(direction);
		cavemen[cavemanNumber].setMoving(moving);
	}

	/**
	 * Makes a Caveman try to throw his spear. Does nothing if the Caveman is not
	 * holding his spear.
	 * 
	 * @param cavemanNumber
	 *            An identifier for a Caveman: 0, 1, or 2
	 */
	public void tryThrowSpear(int cavemanNumber) {
		cavemen[cavemanNumber].tryThrowSpear();
	}
}
