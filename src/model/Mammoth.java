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

	/** The color of a mammoth */
	public static final Color MAMMOTH_COLOR = new Color(66, 33, 00);

	/**
	 * The length of the mammoths head.
	 * 
	 * This is a section of the mammoth's length, so the body_length == length -
	 * head_length
	 */
	public static final double MAMMOTH_HEAD_LENGTH = MAMMOTH_LENGTH - MAMMOTH_WIDTH;

	/**
	 * If the mammoth is moving at this percent of its max speed, it turns to face
	 * that direction
	 */
	public static final double MAMMOTH_TURN_PERCENT = .05;

	/**
	 * The number of frames for which the Mammoth will stay angry at a caveman that
	 * hit it
	 */
	public static final int AGGRO_FRAMES = 300;

	/**
	 * A Scalar that determines how much the Mammoth will target aggro-ed cavemen
	 */
	public static final double AGGRO_SCALAR = 2.2;

	/** The amount of damage that the mammoth takes per spear per frame */
	public static final double SPEAR_DPF = .008;

	/**
	 * A value that influences the mammoth's movement. Making this value larger
	 * makes the mammoth more likely to move at its maxSpeed.
	 */
	private static final double MAMMOTH_SCORE_MODIFIER = 1.8;

	/** The maximum speed that this mammoth can move at */
	private double maxSpeed;

	/** An array of 3 cavemen for the mammoth to target */
	private Caveman[] cavemen;

	/**
	 * An array to identify cavemen that this Mammoth is angry at.
	 * 
	 * aggros[0] is the number of remaining frames for which the Mammoth will target
	 * caveman 0.
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
	 * Makes this mammoth take damage for 1 frame of 1 collided spear, and allows
	 * the movement algorithm to prioritize the caveman that hit it.
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
		double speed = maxSpeed * improvement * MAMMOTH_SCORE_MODIFIER;
		if (speed > maxSpeed) {
			speed = maxSpeed;
		}
		vector.scaleInto(speed);

		x += vector.x;
		y += vector.y;

		// Also, turn to face that direction if the mammoth is moving very quickly
		if (speed > maxSpeed * MAMMOTH_TURN_PERCENT) {
			turn(targetLoc);
		}

		// Now that movement is over, reduce the frame counts in aggros
		for (int i = 0; i < aggros.length; i++) {
			if (aggros[i] > 0)
				aggros[i]--;
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
		double w0 = Math.pow(1.8 - c0Loc.minus(mamLoc).norm(), 6);
		double w1 = Math.pow(1.8 - c1Loc.minus(mamLoc).norm(), 6);
		double w2 = Math.pow(1.8 - c2Loc.minus(mamLoc).norm(), 6);

		// Weights are also effected by the aggro
		if (aggros[0] > 0) {
			w0 *= AGGRO_SCALAR;
		}
		if (aggros[1] > 0) {
			w1 *= AGGRO_SCALAR;
		}
		if (aggros[2] > 0) {
			w2 *= AGGRO_SCALAR;
		}

		// But unconcious cavemen have no weight
		if (!cavemen[0].isConscious()) {
			w0 = 0;
		}
		if (!cavemen[1].isConscious()) {
			w1 = 0;
		}
		if (!cavemen[2].isConscious()) {
			w2 = 0;
		}

		c0Loc.scaleBy(w0);
		c1Loc.scaleBy(w1);
		c2Loc.scaleBy(w2);

		// A quick check to avoid silly exceptions:
		if (0 == w0 + w1 + w2)
			return new Vector(.5, .5);

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
	 * Note that the maximum possible score is lower if some cavemen are
	 * unconscious.
	 */
	private double scoreOf(Vector location) {
		double totalScore = 0;
		double totalPossible = 0;
		for (int i = 0; i < cavemen.length; i++) {
			if (!cavemen[i].isConscious()) {
				totalPossible += 1;
				continue;
			}

			Vector cLoc = new Vector(cavemen[i].getCenterX(), cavemen[i].getCenterY());
			Vector difference = cLoc.minus(location);
			double partialScore = 1.0 - difference.norm();

			if (aggros[i] > 0) {
				totalScore += partialScore * AGGRO_SCALAR;
				totalPossible += AGGRO_SCALAR;
			} else {
				totalScore += partialScore;
				totalPossible += 1;
			}
		}

		double completeScore = totalScore / totalPossible;

		return Math.pow(completeScore, 2);
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
