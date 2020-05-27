package model;

import java.awt.Color;

import model.Participant.Direction;

/**
 * A simplified representation for {@link Participant}s. This class can represent any type of participant, and stores
 * its state (location, color, etc). It doesn't have extra information required for game logic (SpearState, movement
 * speed, etc).
 * 
 * This class is suitable for passing information to the view, and can be translated to and from Strings so it is
 * suitable for passing across the network as well.
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

	/** This Participant's Hitbox */
	private Hitbox hitbox;
	/** The direction this participant is facing */
	private Direction direction;
	/** The type of Participant that this SimpleParticipant represents */
	private ParticipantType type;
	/** The Color of this participant */
	private Color color;

	/**
	 * The amount of hp this participant has. This value is meaningless unless type==ParticipantType.mammoth
	 */
	private double hp;
	/**
	 * Whether this participant is conscious or not. This value is meaningless unless type==ParticipantType.caveman
	 */
	private boolean conscious;

	/** Makes a new SimpleParticipant representative of the given Participant */
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

	/**
	 * Makes a SimpleParticipant out of the given String representation of one. This constructor should only be used with
	 * Strings that were made by SimpleParticipant.toString().
	 * 
	 * @throws IllegalArgumentException
	 *             If parsing fails.
	 */
	public SimpleParticipant(String string) {
		try {
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
		} catch (Exception e) {
			// If anything goes wrong at all, the string was malformed
			throw new IllegalArgumentException("The given String was not a valid SimpleParticipant String");
		}
	}

	/** Returns this participant's hitbox */
	public Hitbox getHitbox() {
		return hitbox;
	}

	/** Returns the direction that this participant is facing */
	public Direction getDirection() {
		return direction;
	}

	/** Returns the type of Participant that this SimpleParticipant represents */
	public ParticipantType getType() {
		return type;
	}

	/** Returns the Color of this participant */
	public Color getColor() {
		return color;
	}

	/**
	 * Returns the amount of HP that this mammoth has remaining.
	 * 
	 * @throws IllegalStateException
	 *             If this SimpleParticipant does not represent a Mammoth
	 */
	public double getHP() {
		if (type != ParticipantType.mammoth)
			throw new IllegalStateException("Non-mammoth participant does not have a value for HP.");
		else
			return hp;
	}

	/**
	 * Returns true if this caveman is conscious, false otherwise.
	 * 
	 * @throws IllegalStateException
	 *             If this SimpleParticipant does not represent a Caveman
	 */
	public boolean isConscious() {
		if (type != ParticipantType.caveman)
			throw new IllegalStateException("Non-caveman participant does not have a value for conscious.");
		else
			return conscious;
	}

	/**
	 * Returns a precise String representation of this SimpleParticipant that could be used to create a new one identical to
	 * this.
	 */
	@Override
	public String toString() {
		String baseString = "{" + hitbox.toString() + "," + direction.toString() + "," + type.toString() + "," + color.getRed() + "," + color.getGreen() + ","
				+ color.getBlue();

		if (type == ParticipantType.mammoth) {
			baseString += "," + hp;
		} else if (type == ParticipantType.caveman) {
			baseString += "," + conscious;
		}

		return baseString + "}";
	}

}
