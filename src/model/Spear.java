package model;

import java.awt.Color;

public class Spear extends Participant {

	public static final double SPEAR_WIDTH = .007;
	public static final double SPEAR_LENGTH = .06;
	
	public static final double SPEAR_SPEED = .012;

	public enum SpearState {
		held,
		grounded,
		active
	}
	
	protected SpearState state;
	
	protected Spear(double x, double y, Color color) {
		this.x = x;
		this.y = y;
		this.direction = Direction.up;
		this.color = color;
		this.state = SpearState.held;
	}
	
	@Override
	public Hitbox getHitbox() {
		if (direction == Direction.up || direction == Direction.down) {
			return new Hitbox(x, y, SPEAR_WIDTH, SPEAR_LENGTH);			
		} else {
			return new Hitbox(x, y, SPEAR_LENGTH, SPEAR_WIDTH);
		}
	}

	@Override
	public void move() {
		if (state == SpearState.active)
			super.move(SPEAR_SPEED);
		// if this spear is held, the caveman will move it whenever it moves
	}

}
