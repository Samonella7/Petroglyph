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
	private Direction direction;
	private ParticipantType type;
	private Color color;

	private double hp;
	private boolean conscious;

	public SimpleParticipant(Participant original) {
		this.hitbox = original.getHitbox();
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

	public SimpleParticipant(String string) {
		// No safety, if a bad string gets sent in here we'll just crash as gracelessly
		// as possible

		String[] input = string.split("[\\{\\}\\,]+");
		
		// input[0] is empty

		double x = Double.parseDouble(input[1]);
		double y = Double.parseDouble(input[2]);
		double w = Double.parseDouble(input[3]);
		double l = Double.parseDouble(input[4]);
		this.hitbox = new Hitbox(x, y, w, l);

		this.direction = Direction.valueOf(input[5]);
		this.type = ParticipantType.valueOf(input[6]);
		
		int r = Integer.parseInt(input[7]);
		int g = Integer.parseInt(input[8]);
		int b = Integer.parseInt(input[9]);
		this.color = new Color(r, g, b);
		
		if (this.type == ParticipantType.mammoth) {
			this.hp = Double.parseDouble(input[10]);
		} else if (this.type == ParticipantType.caveman) {
			this.conscious = Boolean.parseBoolean(input[10]);
		}
	}

	public Hitbox getHitbox() {
		return hitbox;
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

	@Override
	public String toString() {
		String baseString = "{" + hitbox.toString() + "," + direction.toString() + "," + type.toString() + ","
				+ color.getRed() + "," + color.getGreen() + "," + color.getBlue();

		if (type == ParticipantType.mammoth) {
			baseString += "," + hp;
		} else if (type == ParticipantType.caveman) {
			baseString += "," + conscious;
		}

		return baseString + "}";
	}

}
