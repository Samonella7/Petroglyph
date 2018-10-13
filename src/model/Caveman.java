package model;

import java.awt.Color;

import model.Spear.SpearState;

public class Caveman extends Participant {

	public static final double CAVEMAN_WIDTH = .03;
	public static final double CAVEMAN_LENGTH = .03;
	public static final double CAVEMAN_SPEED = .008;

	private Spear spear;
	private boolean moving;

	public Spear getSpear() {
		return spear;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	public Caveman(double x, double y, Color color) {
		this.x = x;
		this.y = y;
		this.direction = Direction.up;
		this.color = color;
		this.moving = false;

		this.spear = new Spear(x, y, color);
	}

	public boolean hasSpear() {
		return spear.state == SpearState.held;
	}

	@Override
	public Hitbox getHitbox() {
		if (direction == Direction.up || direction == Direction.down) {
			return new Hitbox(x, y, CAVEMAN_WIDTH, CAVEMAN_LENGTH);
		} else {
			return new Hitbox(x, y, CAVEMAN_LENGTH, CAVEMAN_WIDTH);
		}
	}

	@Override
	public void move() {
		if (moving) {
			super.move(CAVEMAN_SPEED);
		}

		// Drag the spear along if it is held
		if (hasSpear()) {
			spear.direction = direction;
			switch (direction) {
			case up:
				spear.x = x + CAVEMAN_WIDTH;
				spear.y = y - (Spear.SPEAR_LENGTH - CAVEMAN_LENGTH) * 3.0/4.0;
				break;
			case down:
				spear.x = x - Spear.SPEAR_WIDTH;
				spear.y = y - (Spear.SPEAR_LENGTH - CAVEMAN_LENGTH) * 1.0/4.0;
				break;
			case left:
				spear.x = x - (Spear.SPEAR_LENGTH - CAVEMAN_LENGTH) * 3.0/4.0;
				spear.y = y - Spear.SPEAR_WIDTH;
				break;
			case right:
				spear.x = x - (Spear.SPEAR_LENGTH - CAVEMAN_LENGTH) * 1.0/4.0;
				spear.y = y + CAVEMAN_WIDTH;
				break;
			}
		}
	}

}
