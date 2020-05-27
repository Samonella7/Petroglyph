package model;

/**
 * A class to represent a Participant's hitbox
 * 
 * @author Sam Thayer
 */
public class Hitbox {
	public double leftX;
	public double topY;

	public double width;
	public double length;

	public double rightX;
	public double bottomY;

	/**
	 * Makes a Hitbox with the given dimensions
	 */
	public Hitbox(double x, double y, double width, double length) {
		this.leftX = x;
		this.topY = y;
		this.rightX = x + width;
		this.bottomY = y + length;
		this.width = width;
		this.length = length;
	}

	/**
	 * Makes a new Hitbox identical to the given one
	 */
	public Hitbox(Hitbox other) {
		this.leftX = other.leftX;
		this.topY = other.topY;
		this.rightX = other.rightX;
		this.bottomY = other.bottomY;
		this.width = other.width;
		this.length = other.length;
	}

	/**
	 * Returns a String representation of this Hitbox
	 */
	@Override
	public String toString() {
		return "{" + leftX + "," + topY + "," + width + "," + length + "}";
	}
}
