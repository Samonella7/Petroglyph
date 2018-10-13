package view;

import model.Hitbox;

public class PixelBox {
	public int topLeftX;
	public int topLeftY;

	public int width;
	public int length;

	public int bottomRightX;
	public int bottomRightY;

	public PixelBox(Hitbox hb, int maxX, int maxY) {
		this.topLeftX = (int) (hb.topLeftX * maxX);
		this.topLeftY = (int) (hb.topLeftY * maxY);
		this.bottomRightX = (int) (hb.bottomRightX * maxX);
		this.bottomRightY = (int) (hb.bottomRightY * maxY);
		this.width = bottomRightX - topLeftX;
		this.length = bottomRightY - topLeftY;
	}
}
