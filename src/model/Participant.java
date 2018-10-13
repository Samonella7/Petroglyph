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
			break;
		case left:
			x -= speed;
			if (x < 0)
				x = 0;
			break;
		case right:
			x += speed;
			break;
		}
	}

	public final boolean collidedWith(Participant other) {
		// TODO
		return true;
	}

}
