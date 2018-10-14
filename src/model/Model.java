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
	public void calculateNextFrame() {
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

		for (int i = 0; i < spears.length; i++) {
			if (spears[i].state == SpearState.active && mammoth.collidedWith(spears[i])) {
				mammoth.takeDamage(i);
			}
		}
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
		spears[cavemanNumber].tryLaunch();
	}
}
