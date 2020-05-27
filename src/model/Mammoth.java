package model;

import java.awt.Color;

/**
 * A class to represent the Mammoth.
 * 
 * A Mammoth is controlled by the computer, and tries to catch the Cavemen by following a somewhat complex algorithm. It
 * can move in any direction.
 * 
 * @author Sam Thayer
 */
public class Mammoth extends Participant {

	/** The width of a mammoth */
	public static final double MAMMOTH_WIDTH = .14;

	/** The length of a mammoth */
	public static final double MAMMOTH_LENGTH = .185;

	/** The color of a mammoth */
	public static final Color MAMMOTH_COLOR = new Color(66, 33, 00);

	/**
	 * The length of the mammoths head.
	 * 
	 * This is a section of the mammoth's length, so the body_length == length - head_length
	 */
	public static final double MAMMOTH_HEAD_LENGTH = MAMMOTH_LENGTH - MAMMOTH_WIDTH;

	/** The percent of max speed at which the mammoth turns to face its direction */
	public static final double MAMMOTH_TURN_SCALAR = .3;

	/** The number of frames for which the Mammoth will stay aggroed */
	public static final int AGGRO_FRAMES = 300;

	/** How much more the Mammoth cares about aggroed cavemen */
	public static final double AGGRO_SCALAR = 1.6;

	/** Determines how quickly the mammoth starts prioritizing cavemen as they move closer */
	public static final double PROXIMITY_TARGETING_DISTRIBUTION = 1.7;

	/** Percentage defining how much the mammoth's movement is influenced by proximity targeting */
	public static final double PROXIMITY_TARGETING_WEIGHT = .8;

	/** The amount of damage that the mammoth takes per spear per frame */
	public static final double SPEAR_DPF = .008;

	/** The maximum speed that this mammoth can move at */
	private double maxSpeed;

	/** An array of 3 cavemen for the mammoth to target */
	private Caveman[] cavemen;

	/**
	 * An array to identify cavemen that this Mammoth is angry at.
	 * 
	 * aggros[0] is the number of remaining frames for which the Mammoth will target caveman 0.
	 */
	private int[] aggros;

	/** The percent of this mammoth's health that remains */
	private double hp;

	/**
	 * Creates a mammoth in the center of the map
	 * 
	 * @param maxSpeed
	 *            The maximum speed that this mammoth can move at
	 * @param cavemen
	 *            An array of 3 cavemen for the mammoth to target
	 */
	public Mammoth(double maxSpeed, Caveman[] cavemen) {
		this.maxSpeed = maxSpeed;
		this.cavemen = cavemen;
		this.x = .5 - MAMMOTH_WIDTH / 2;
		this.y = .5 - MAMMOTH_LENGTH / 2;
		this.direction = Direction.up;
		this.color = MAMMOTH_COLOR;
		this.hp = 1;
		aggros = new int[cavemen.length];
	}

	/**
	 * Makes this mammoth take damage for 1 frame of 1 collided spear, and allows the movement algorithm to prioritize the
	 * caveman that hit it.
	 */
	public void takeDamage(int caveman) {
		hp -= SPEAR_DPF;
		if (hp < 0)
			hp = 0;
		aggros[caveman] = AGGRO_FRAMES;
	}

	/**
	 * Returns a percentage representing how much of this Mammoth's health remains
	 */
	public double getHP() {
		return hp;
	}

	/**
	 * Returns this mammoth's hitbox
	 */
	@Override
	public Hitbox getHitbox() {
		if (direction == Direction.up || direction == Direction.down) {
			return new Hitbox(x, y, MAMMOTH_WIDTH, MAMMOTH_LENGTH);
		} else {
			return new Hitbox(x, y, MAMMOTH_LENGTH, MAMMOTH_WIDTH);
		}
	}

	/**
	 * Returns the X coordinate of the center of this mammoth's body
	 */
	@Override
	public double getCenterX() {
		if (direction == Direction.left) {
			return x + MAMMOTH_HEAD_LENGTH + (MAMMOTH_LENGTH - MAMMOTH_HEAD_LENGTH) / 2;
		} else if (direction == Direction.right) {
			return x + (MAMMOTH_LENGTH - MAMMOTH_HEAD_LENGTH) / 2;
		} else {
			return x + MAMMOTH_WIDTH / 2;
		}
	}

	/**
	 * Returns the Y coordinate of the center of this mammoth's body
	 */
	@Override
	public double getCenterY() {
		if (direction == Direction.up) {
			return y + MAMMOTH_HEAD_LENGTH + (MAMMOTH_LENGTH - MAMMOTH_HEAD_LENGTH) / 2;
		} else if (direction == Direction.down) {
			return y + (MAMMOTH_LENGTH - MAMMOTH_HEAD_LENGTH) / 2;
		} else {
			return y + MAMMOTH_WIDTH / 2;
		}
	}

	/**
	 * Turns the mammoth to face the given direction. Note that this can change x,y: the center of the mammoths body will
	 * stay anchored, but the head will swing around it, so the top-left corner of the overall mammoth could change
	 */
	private void setDirection(Vector direction) {
		Direction newDir;
		if (Math.abs(direction.x) > Math.abs(direction.y)) {
			newDir = direction.x > 0 ? Direction.right : Direction.left;
		} else {
			newDir = direction.y > 0 ? Direction.down : Direction.up;
		}

		setDirection(newDir);
	}

	/**
	 * Turns the mammoth to face the given direction. Note that this can change x,y: the center of the mammoths body will
	 * stay anchored, but the head will swing around it, so the top-left corner of the overall mammoth could change
	 */
	@Override
	public void setDirection(Direction newDirection) {
		// first, down/right as needed so that the new x,y is where
		// the corner of the body used to be
		if (direction == Direction.left) {
			x += MAMMOTH_HEAD_LENGTH;
		} else if (direction == Direction.up) {
			y += MAMMOTH_HEAD_LENGTH;
		}

		direction = newDirection;

		// next shift up/left as needed so the corner of the body
		// is where it was before this method was called
		if (newDirection == Direction.left) {
			x -= MAMMOTH_HEAD_LENGTH;
		} else if (newDirection == Direction.up) {
			y -= MAMMOTH_HEAD_LENGTH;
		}
	}

	@Override
	/**
	 * Moves the mammoth
	 */
	public void move() {
		Vector vec0 = calculateInfluence(0);
		Vector vec1 = calculateInfluence(1);
		Vector vec2 = calculateInfluence(2);

		Vector move = vec0.plus(vec1).plus(vec2);
		move.scaleBy(maxSpeed);

		// Actually move:
		x += move.x;
		y += move.y;

		// Also, turn to face that direction if the mammoth is moving very quickly
		if (move.norm() > maxSpeed * MAMMOTH_TURN_SCALAR) {
			setDirection(move);
		}

		// Now that movement is over, reduce the frame counts in aggros
		for (int i = 0; i < aggros.length; i++) {
			if (aggros[i] > 0)
				aggros[i]--;
		}
	}

	/**
	 * Returns a vector pointed toward the specified caveman. It is scaled (norm 0-1) according to how much the mammoth
	 * wants to attack it.
	 */
	private Vector calculateInfluence(int cavemanNumber) {
		Caveman caveman = cavemen[cavemanNumber];

		if (!caveman.isConscious()) {
			return new Vector(0, 0);
		}

		Vector direction = cavemen[cavemanNumber].getCenterLocation().minus(getCenterLocation());
		double distance = direction.norm();

		// reminder: all coordinates are percents of map width/height
		// max distance is diagonal corners, scale so this is 1
		distance /= Math.sqrt(2);

		double proximityWeight = (1 - distance);
		proximityWeight = Math.pow(proximityWeight, PROXIMITY_TARGETING_DISTRIBUTION) * PROXIMITY_TARGETING_WEIGHT;
		double constantWeight = 1 - PROXIMITY_TARGETING_WEIGHT;

		double totalWeight = proximityWeight + constantWeight;

		// finally, non-aggroed cavemen are less important
		if (aggros[cavemanNumber] == 0) {
			totalWeight /= AGGRO_SCALAR;
		}

		return direction.scaleInto(totalWeight);
	}

}
