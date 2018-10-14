package model;

import java.awt.Color;

import model.Spear.SpearState;

/**
 * A class to represent a Caveman.
 * 
 * Cavemen are controlled by players. They can move only in
 * Participant.Directions, not in diagonals. Each Caveman is associated with a
 * Spear, which it can throw and then pick up again any number of times. A
 * caveman can be knocked out by touching the Mammoth, and can be picked up
 * again by contact with another (conscious) Caveman.
 * 
 * All dimensions are measured in percents, so x=.5 is halfway accross the
 * board. All speeds are measured in percent per frame.
 * 
 * @author Sam Thayer
 */
public class Caveman extends Participant {
	// Constants detailing Cavemen's properties
	public static final double CAVEMAN_WIDTH = .035;
	public static final double CAVEMAN_LENGTH = .035;
	public static final double CAVEMAN_SPEED = .009;

	/** A reference to this caveman's spear */
	private Spear spear;

	/** True if this caveman is currently moving, false otherwise */
	private boolean moving;

	/** Returns a reference to this caveman's spear */
	public Spear getSpear() {
		return spear;
	}

	/** Sets whether this caveman is moving or not */
	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	/**
	 * Creates a caveman (and its associated spear) at the given location, and of
	 * the given color.
	 */
	public Caveman(double x, double y, Color color) {
		this.x = x;
		this.y = y;
		this.direction = Direction.up;
		this.color = color;
		this.moving = false;

		this.spear = new Spear(x, y, color);
	}

	/** Returns true if this Caveman is holding his spear, and false otherwise. */
	public boolean hasSpear() {
		return spear.state == SpearState.held;
	}

	/**
	 * Returns this Caveman's hitbox.
	 */
	@Override
	public Hitbox getHitbox() {
		if (direction == Direction.up || direction == Direction.down) {
			return new Hitbox(x, y, CAVEMAN_WIDTH, CAVEMAN_LENGTH);
		} else {
			return new Hitbox(x, y, CAVEMAN_LENGTH, CAVEMAN_WIDTH);
		}
	}

	/**
	 * Updates this caveman's location for the next frame. If this Caveman is hold
	 * its spear, the update's the spear's location as well.
	 */
	@Override
	public void move() {
		if (moving) {
			super.move(CAVEMAN_SPEED);
		}

		if (hasSpear()) {
			spear.direction = direction;
			switch (direction) {
			case up:
				spear.x = x + CAVEMAN_WIDTH;
				spear.y = y - (Spear.SPEAR_LENGTH - CAVEMAN_LENGTH) * 3.0 / 4.0;
				break;
			case down:
				spear.x = x - Spear.SPEAR_WIDTH;
				spear.y = y - (Spear.SPEAR_LENGTH - CAVEMAN_LENGTH) * 1.0 / 4.0;
				break;
			case left:
				spear.x = x - (Spear.SPEAR_LENGTH - CAVEMAN_LENGTH) * 3.0 / 4.0;
				spear.y = y - Spear.SPEAR_WIDTH;
				break;
			case right:
				spear.x = x - (Spear.SPEAR_LENGTH - CAVEMAN_LENGTH) * 1.0 / 4.0;
				spear.y = y + CAVEMAN_WIDTH;
				break;
			}
		}
	}

}
