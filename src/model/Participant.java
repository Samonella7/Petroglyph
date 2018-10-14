package model;

import java.awt.Color;

/**
 * A class to represent objects in the game. All game objects extend
 * Participant.
 * 
 * All dimensions are measured in percents, so x=.5 is halfway accross the
 * board. All speeds are measured in percent per frame.
 * 
 * @author Sam Thayer
 */
public abstract class Participant {
	/**
	 * A direction that a Participant can face
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
	 * Moves this Participant in it's current direction by the given amount, but
	 * will not move it off the edge of the game area.
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
	 * Returns true if this Participant's hitbox is overlapping with that of the
	 * given Participant, and false otherwise.
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

}
