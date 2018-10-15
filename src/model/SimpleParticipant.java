package model;

import java.awt.Color;

import model.Participant.Direction;

/**
 * A simplified representation for Participants. This class can represent any
 * type of participant, and stores its state (location, color, etc). It doesn't
 * have extra information required for game logic (SpearState, movement speed,
 * etc).
 * 
 * This class is suitable for passing information to the view, and can be
 * translated to and from Strings so it is suitable for passing across the
 * network as well.
 * 
 * @author Sam Thayer
 */
public class SimpleParticipant {

	/**
	 * A type of Participant
	 */
	public enum ParticipantType {
		mammoth, caveman, spear
	}

	private Hitbox hitbox;
	private double x;
	private double y;
	private Direction direction;
	private ParticipantType type;
	private Color color;

	private double hp;
	private boolean conscious;

	public SimpleParticipant(Participant original) {
		this.hitbox = original.getHitbox();
		this.x = original.getX();
		this.y = original.getY();
		this.direction = original.getDirection();
		this.color = original.getColor();

		if (original instanceof Mammoth) {
			type = ParticipantType.mammoth;
			hp = ((Mammoth) original).getHP();
		} else if (original instanceof Caveman) {
			type = ParticipantType.caveman;
			conscious = ((Caveman) original).isConscious();
		} else if (original instanceof Spear) {
			type = ParticipantType.spear;
		}
	}

	public Hitbox getHitbox() {
		return hitbox;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Direction getDirection() {
		return direction;
	}

	public ParticipantType getType() {
		return type;
	}

	public Color getColor() {
		return color;
	}

	public double getHP() {
		if (type != ParticipantType.mammoth)
			throw new IllegalStateException("Non-mammoth participant does not have a value for HP");
		else
			return hp;
	}

	public boolean isConscious() {
		if (type != ParticipantType.caveman)
			throw new IllegalStateException("Non-caveman participant does not have a value for conscious");
		else
			return conscious;
	}

}
