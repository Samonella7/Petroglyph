package model;

public class Hitbox {
	public double topLeftX;
	public double topLeftY;
	
	public double width;
	public double length;
	
	public double bottomRightX;
	public double bottomRightY;
	
	public Hitbox(double x, double y, double width, double length) {
		this.topLeftX = x;
		this.topLeftY = y;
		this.bottomRightX = x + width;
		this.bottomRightY = y + length;
		this.width = width;
		this.length = length;
	}
}
