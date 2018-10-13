package model;

import java.awt.Color;

public class Spear extends Participant {

	public static final double SPEAR_WIDTH = .007;
	public static final double SPEAR_LENGTH = .06;
	
	public static final double SPEAR_SPEED = .01;
	
	public static final int SPEAR_AIR_TIME = 15;

	public enum SpearState {
		held,
		grounded,
		active
	}
	
	protected SpearState state;
	protected int remainingAirtime;
	
	protected Spear(double x, double y, Color color) {
		this.x = x;
		this.y = y;
		this.direction = Direction.up;
		this.color = color;
		this.state = SpearState.held;
		this.remainingAirtime = 0;
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
		if (state != SpearState.active)
			return;
		// if this spear is held, the caveman will move it whenever it moves
		
		super.move(SPEAR_SPEED);
		remainingAirtime--;
		if (remainingAirtime == 0) {
			state = SpearState.grounded;
		}
	}

	public void tryLaunch() {
		if (state == SpearState.held) {
			state = SpearState.active;
			remainingAirtime = SPEAR_AIR_TIME;
		}
	}

}
