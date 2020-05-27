package model;

import java.awt.Color;

/**
 * A class to represent objects in the game. All game objects extend Participant.
 * 
 * All dimensions are measured in percents, so x=.5 is halfway accross the board. All speeds are measured in percent per
 * frame.
 * 
 * @author Sam Thayer
 */
public abstract class Participant {
	/**
	 * A direction that a Participant can face.
	 */
	public enum Direction {
		up, down, left, right
	}

	/** The x coordinate of the top-left corner of this participant's hitbox */
	protected double x;
	/** The y coordinate of the top-left corner of this participant's hitbox */
	protected double y;

	/** The color of this participant */
	protected Color color;

	/** The Direction that this participant is facing */
	protected Direction direction;

	/**
	 * Returns the x coordinate of the top-left corner of this participant's hitbox
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the y coordinate of the top-left corner of this participant's hitbox
	 */
	public double getY() {
		return y;
	}

	/** Returns the x coordinate of the center of this participant's hitbox */
	public double getCenterX() {
		Hitbox hb = getHitbox();
		return (hb.leftX + hb.rightX) / 2;
	}

	/** Returns the y coordinate of the center of this participant's hitbox */
	public double getCenterY() {
		Hitbox hb = getHitbox();
		return (hb.topY + hb.bottomY) / 2;
	}

	/** Returns a vector indicating the center of this participant's hitbox */
	public Vector getCenterLocation() {
		return new Vector(getCenterX(), getCenterY());
	}

	/** Returns the Direction that this participant is facing */
	public Direction getDirection() {
		return direction;
	}

	/** Makes this participant face the given direction */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/** Returns the color of this Participant */
	public Color getColor() {
		return color;
	}

	/** Returns the hitbox of this Participant */
	public abstract Hitbox getHitbox();

	/** Calculates and moves to the correct position for the next frame */
	public abstract void move();

	/**
	 * Moves this Participant in it's current direction by the given amount, but will not move it off the edge of the game
	 * area.
	 */
	protected void move(double speed) {
		switch (direction) {
		case up:
			y -= speed;
			if (y < 0)
				y = 0;
			break;
		case down:
			y += speed;
			Hitbox hby = getHitbox();
			if (hby.bottomY > 1)
				y = 1 - hby.length;
			break;
		case left:
			x -= speed;
			if (x < 0)
				x = 0;
			break;
		case right:
			x += speed;
			Hitbox hbx = getHitbox();
			if (hbx.rightX > 1)
				x = 1 - hbx.width;
			break;
		}
	}

	/**
	 * Returns true if this Participant's hitbox is overlapping with that of the given Participant, and false otherwise.
	 */
	public final boolean collidedWith(Participant other) {
		Hitbox thisHb = this.getHitbox();
		Hitbox otherHb = other.getHitbox();

		Hitbox leftMost = thisHb.leftX < otherHb.leftX ? thisHb : otherHb;
		Hitbox rightMost = leftMost == thisHb ? otherHb : thisHb;
		boolean overlappingX = leftMost.rightX > rightMost.leftX;

		Hitbox topMost = thisHb.topY < otherHb.topY ? thisHb : otherHb;
		Hitbox bottomMost = topMost == thisHb ? otherHb : thisHb;
		boolean overlappingY = topMost.bottomY > bottomMost.topY;

		return overlappingX && overlappingY;
	}

	/**
	 * A class to help with the vector math that the Mammoth does when deciding how to move
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
		 * Scales to match the given norm, and returns this Vector
		 */
		public Vector scaleInto(double newNorm) {
			double oldNorm = norm();
			x = x / oldNorm * newNorm;
			y = y / oldNorm * newNorm;
			return this;
		}

		/**
		 * Scales this vector by multiplying its values by the given scalar, and returns this vector
		 */
		public Vector scaleBy(double scalar) {
			x = x * scalar;
			y = y * scalar;
			return this;
		}

		/**
		 * Returns a new vector that is the sum of this one and the given one
		 */
		public Vector plus(Vector other) {
			return new Vector(x + other.x, y + other.y);
		}

		/**
		 * Returns a new vector that is the difference between this one and the given one
		 */
		public Vector minus(Vector other) {
			return new Vector(x - other.x, y - other.y);
		}
	}
}
