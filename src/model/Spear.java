package model;

import java.awt.Color;

/**
 * A class to represent a Spear.
 * 
 * A Spear can be thrown by its associated Caveman to damage the Mammoth. When thrown, it moves in a straight line in
 * the direction the caveman was facing when he threw it.
 * 
 * All dimensions are measured in percents, so x=.5 is halfway accross the board. All speeds are measured in percent per
 * frame.
 * 
 * @author Sam Thayer
 */
public class Spear extends Participant {
	// Constants detailing Spears' properties
	public static final double SPEAR_WIDTH = .0085;
	public static final double SPEAR_LENGTH = .075;
	public static final double SPEAR_SPEED = .012;
	public static final int SPEAR_AIR_TIME = 16;

	/** A state that a Spear can be in, either 'held,' 'grounded,' or 'active.' */
	public enum SpearState {
		held, grounded, active
	}

	/** This spear's current state */
	protected SpearState state;

	/**
	 * The Number of frames that before this spear becomes inactive. This value is irrelevant if this Spear is not active.
	 */
	protected int remainingAirtime;

	/**
	 * Creates a Spear of the given Color at the given location
	 */
	protected Spear(double x, double y, Color color) {
		this.x = x;
		this.y = y;
		this.direction = Direction.up;
		this.color = color;
		this.state = SpearState.held;
		this.remainingAirtime = 0;
	}

	/**
	 * Returns this spear's hitbox
	 */
	@Override
	public Hitbox getHitbox() {
		if (direction == Direction.up || direction == Direction.down) {
			return new Hitbox(x, y, SPEAR_WIDTH, SPEAR_LENGTH);
		} else {
			return new Hitbox(x, y, SPEAR_LENGTH, SPEAR_WIDTH);
		}
	}

	/**
	 * Moves this spear if it is active, does nothing otherwise.
	 */
	@Override
	public void move() {
		if (state != SpearState.active)
			return;
		// if this spear is held, the caveman will move it whenever it moves

		super.move(SPEAR_SPEED);
		remainingAirtime--;
		if (remainingAirtime == 0) {
			state = SpearState.grounded;
		}
	}

}
