package model;

import java.awt.Color;

/**
 * A class to represent the Mammoth.
 * 
 * A Mammoth is controlled by the computer, and tries to catch the Cavemen. It
 * can move in any direction.
 * 
 * @author Sam Thayer
 */
public class Mammoth extends Participant {

	/** The width of a mammoth */
	public static final double MAMMOTH_WIDTH = .14;

	/** The length of a mammoth */
	public static final double MAMMOTH_LENGTH = .185;

	/**
	 * The length of the mammoths head.
	 * 
	 * This is a section of the mammoth's length, so the body_length == length -
	 * head_length
	 */
	public static final double MAMMOTH_HEAD_LENGTH = MAMMOTH_LENGTH - MAMMOTH_WIDTH;

	/** The maximum speed that this mammoth can move at */
	private double maxSpeed;

	/** An array of 3 cavemen for the mammoth to target */
	private Caveman[] cavemen;

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
		this.color = new Color(66, 33, 00);
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
	 * Turns the mammoth to face the given location. Note that this can change x,y:
	 * the center of the mammoths body will stay anchored, but the head will swing
	 * around it, so the top-left corner of the overall mammoth could change
	 */
	private void turn(Vector targetLoc) {
		// first, figure out what Direction the location is in
		Vector mammothLoc = new Vector(getCenterX(), getCenterY());
		Vector vector = targetLoc.minus(mammothLoc);

		Direction newDir;
		if (Math.abs(vector.x) > Math.abs(vector.y)) {
			newDir = vector.x > 0 ? Direction.right : Direction.left;
		} else {
			newDir = vector.y > 0 ? Direction.down : Direction.up;
		}

		setDirection(newDir);
	}

	/**
	 * Turns the mammoth to face the given direction. Note that this can change x,y:
	 * the center of the mammoths body will stay anchored, but the head will swing
	 * around it, so the top-left corner of the overall mammoth could change
	 */
	@Override
	public void setDirection(Direction newDir) {
		// first, down/right as needed so that the new x,y is where
		// the corner of the body used to be
		if (direction == Direction.left) {
			x += MAMMOTH_HEAD_LENGTH;
		} else if (direction == Direction.up) {
			y += MAMMOTH_HEAD_LENGTH;
		}

		direction = newDir;

		// next shift up/left as needed so the corner of the body
		// is where it was before this method was called
		if (newDir == Direction.left) {
			x -= MAMMOTH_HEAD_LENGTH;
		} else if (newDir == Direction.up) {
			y -= MAMMOTH_HEAD_LENGTH;
		}
	}

	@Override
	/**
	 * Moves the mammoth
	 */
	public void move() {
		Vector currentLoc = new Vector(getCenterX(), getCenterY());
		Vector targetLoc = chooseTarget();

		// See how much better the target location is than the current one
		double currentScore = scoreOf(currentLoc);
		double targetScore = scoreOf(targetLoc);

		// I don't think this will ever happen:
		if (targetScore < currentScore)
			return;

		double improvement = targetScore - currentScore;

		// speed of movement depends on how much better the new location would be
		Vector vector = targetLoc.minus(currentLoc);
		vector.scaleInto(maxSpeed * improvement);

		x += vector.x;
		y += vector.y;

		// Also, turn to face that direction if the mammoth is moving very quickly
		if (improvement > .08) {
			turn(targetLoc);
		}
	}

	/**
	 * Returns a vector that is the location that the mammoth should move towards
	 */
	private Vector chooseTarget() {
		// Locations of the mammoth and each caveman
		Vector mamLoc = new Vector(getCenterX(), getCenterY());
		Vector c0Loc = new Vector(cavemen[0].getCenterX(), cavemen[0].getCenterY());
		Vector c1Loc = new Vector(cavemen[1].getCenterX(), cavemen[1].getCenterY());
		Vector c2Loc = new Vector(cavemen[2].getCenterX(), cavemen[2].getCenterY());

		// weighted average of the cavemen's locations, where each point's weight is
		// inversely correlated to the distance from the mammoth
		double w0 = Math.pow(1.8 - c0Loc.minus(mamLoc).norm(), 5);
		double w1 = Math.pow(1.8 - c1Loc.minus(mamLoc).norm(), 5);
		double w2 = Math.pow(1.8 - c2Loc.minus(mamLoc).norm(), 5);

		c0Loc.scaleBy(w0);
		c1Loc.scaleBy(w1);
		c2Loc.scaleBy(w2);

		Vector target = c0Loc.plus(c1Loc).plus(c2Loc);
		target.scaleBy(1 / (w0 + w1 + w2));
		return target;
	}

	/**
	 * Assigns a score between 0 and 1 to the given location.
	 * 
	 * This score is higher if the location is closer to more cavemen, and lower if
	 * it is far from them.
	 * 
	 * Specifically, the score is 1 if all three cavemen are at that exact location,
	 * and 0 if all cavemen are more than a third of the map away
	 */
	private double scoreOf(Vector location) {
		double totalScore = 0;
		for (Caveman c : cavemen) {
			Vector cLoc = new Vector(c.getCenterX(), c.getCenterY());
			Vector difference = cLoc.minus(location);
			totalScore += 1.0 / 3.0 - difference.norm();
		}
		return totalScore;
	}

	/**
	 * A class to help with the vector math that the Mammoth does when deciding how
	 * to move
	 */
	class Vector {
		/**
		 * Creates a Vector with the given dimensions
		 */
		public Vector(double x, double y) {
			this.x = x;
			this.y = y;
		}

		/**
		 * Creates a vector identical to the given one
		 */
		public Vector(Vector other) {
			this.x = other.x;
			this.y = other.y;
		}

		/**
		 * This Vector's x dimension
		 */
		public double x;

		/**
		 * This Vector's y dimension
		 */
		public double y;

		/**
		 * Returns the norm (length) of this vector
		 */
		public double norm() {
			return Math.sqrt(x * x + y * y);
		}

		/**
		 * Scales this vector so that its norm is newNorm
		 */
		public void scaleInto(double newNorm) {
			double oldNorm = norm();
			x = x / oldNorm * newNorm;
			y = y / oldNorm * newNorm;
		}

		/**
		 * Scales this vector by multiplying its values by the given scalar
		 */
		public void scaleBy(double scalar) {
			x = x * scalar;
			y = y * scalar;
		}

		/**
		 * Returns a new vector that is the sum of this one and the given one
		 */
		public Vector plus(Vector other) {
			return new Vector(x + other.x, y + other.y);
		}

		/**
		 * Returns a new vector that is the difference between this one and the given
		 * one
		 */
		public Vector minus(Vector other) {
			return new Vector(x - other.x, y - other.y);
		}
	}

}
