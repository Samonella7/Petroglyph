package model;

public class Hitbox {
	public double topLeftX;
	public double topLeftY;
	
	public double bottomRightX;
	public double bottomRightY;
	
	public Hitbox(double x, double y, double width, double length) {
		topLeftX = x;
		topLeftY = y;
		bottomRightX = x + width;
		bottomRightY = y + length;
	}
}
