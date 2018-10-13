package model;

import java.awt.Color;

public abstract class Participant {

	public enum Direction {
		up, down, left, right
	}

	protected double x;
	protected double y;
	protected Color color;
	protected Direction direction;

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Color getColor() {
		return color;
	}

	public abstract Hitbox getHitbox();

	public abstract void move();

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
			if (hby.bottomRightY > 1)
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
			if (hbx.bottomRightX > 1)
				x = 1 - hbx.width;
			break;
		}
	}

	public final boolean collidedWith(Participant other) {
		Hitbox thisHb = this.getHitbox();
		Hitbox otherHb = other.getHitbox();

		Hitbox leftMost = thisHb.topLeftX < otherHb.topLeftX ? thisHb : otherHb;
		Hitbox rightMost = leftMost == thisHb ? otherHb : thisHb;
		boolean overlappingX = leftMost.bottomRightX > rightMost.topLeftX;

		Hitbox topMost = thisHb.topLeftY < otherHb.topLeftY ? thisHb : otherHb;
		Hitbox bottomMost = topMost == thisHb ? otherHb : thisHb;
		boolean overlappingY = topMost.bottomRightY > bottomMost.topLeftY;

		return overlappingX && overlappingY;
	}

}
