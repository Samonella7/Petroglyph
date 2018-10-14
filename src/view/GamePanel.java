package view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import model.Caveman;
import model.Hitbox;
import model.Mammoth;
import model.Participant;
import model.Spear;
import model.Participant.Direction;

/**
 * A panel for displaying the Petroglyph gameboard
 * 
 * @author Sam Thayer
 */
class GamePanel extends JPanel {
	private static final long serialVersionUID = 2L;

	private static final Color BACKGROUND_COLOR = new Color(33, 88, 00);

	// A set of constants used for drawing the mammoth
	// The units are percents of the whole panel, just like participant hitboxes
	private static final double MAMMOTH_HEAD_LENGTH = Mammoth.MAMMOTH_HEAD_LENGTH;
	private static final double MAMMOTH_HEAD_WIDTH = .5 * Mammoth.MAMMOTH_WIDTH;
	private static final double MAMMOTH_HORN_OFFSET = .06 * Mammoth.MAMMOTH_LENGTH;
	private static final double MAMMOTH_HORN_WIDTH = .08 * Mammoth.MAMMOTH_LENGTH;

	/** The width (in pixels) of this panel last time it was painted */
	private int panelWidth;
	/** The height (in pixels) of this panel last time it was painted */
	private int panelHeight;

	/** The width (in pixels) of a spear tip, scaled according to panelWidth */
	private int spearTipWidth;
	/** The height (in pixels) of a spear tip, scaled according to panelHeight */
	private int spearTipHeight;

	/** An array of the participants that should be drawn */
	private Participant[] participants;

	/**
	 * Creates a GamePanel that will draw all participants in the given array
	 */
	public GamePanel(Participant[] participants) {
		this.participants = participants;
	}

	/**
	 * Recalculates field-variables that depend on the size of this panel
	 */
	private void recalculateConstants() {
		panelWidth = getWidth();
		panelHeight = getHeight();

		spearTipWidth = panelWidth / 60;
		spearTipHeight = panelHeight / 60;
	}

	/**
	 * Redraws this panel, taking into account changes (if any) in the location of
	 * the participants that this GamePanel draws
	 */
	@Override
	public void paint(Graphics g) {
		recalculateConstants();

		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, panelWidth, panelHeight);

		for (Participant p : participants) {
			if (p instanceof Caveman) {
				paintCaveman(g, (Caveman) p);
			} else if (p instanceof Spear) {
				paintSpear(g, (Spear) p);
			} else {
				paintMammoth(g, (Mammoth) p);
			}
		}
	}

	/**
	 * Draws the given caveman onto the given graphics object
	 */
	private void paintCaveman(Graphics g, Caveman c) {
		PixelBox box = new PixelBox(c.getHitbox(), panelWidth, panelHeight);
		g.setColor(c.getColor());
		g.fillRect(box.leftX, box.topY, box.width, box.length);
	}

	/**
	 * Draws the given spear onto the given graphics object
	 */
	private void paintSpear(Graphics g, Spear s) {
		PixelBox box = new PixelBox(s.getHitbox(), panelWidth, panelHeight);
		g.setColor(s.getColor());

		// The distance from the back corner of the spear tip to the shaft
		// declared here instead of in the if to satisfy the silly compiler
		int offset;

		// these represent the points of the triangle's tip
		int[] xPoints = new int[3];
		int[] yPoints = new int[3];

		// Calculating them is non-trivial
		if (s.getDirection() == Direction.up || s.getDirection() == Direction.down) {
			offset = (spearTipWidth - box.width) / 2;
			xPoints[0] = box.leftX - offset;
			xPoints[1] = box.rightX + offset;
			xPoints[2] = box.leftX + (box.width / 2);
		} else {
			offset = (spearTipHeight - box.length) / 2;
			yPoints[0] = box.topY - offset;
			yPoints[1] = box.bottomY + offset;
			yPoints[2] = box.topY + (box.length / 2);
		}

		// Each case draws the shaft and calculates the points that couldn't be done
		// above
		switch (s.getDirection()) {
		case up:
			g.fillRect(box.leftX, box.topY + spearTipHeight, box.width, box.length - spearTipHeight);

			yPoints[0] = box.topY + spearTipHeight;
			yPoints[1] = box.topY + spearTipHeight;
			yPoints[2] = box.topY;
			break;
		case down:
			g.fillRect(box.leftX, box.topY, box.width, box.length - spearTipHeight);

			yPoints[0] = box.bottomY - spearTipHeight;
			yPoints[1] = box.bottomY - spearTipHeight;
			yPoints[2] = box.bottomY;
			break;
		case left:
			g.fillRect(box.leftX + spearTipWidth, box.topY, box.width - spearTipWidth, box.length);

			xPoints[0] = box.leftX + spearTipWidth;
			xPoints[1] = box.leftX + spearTipWidth;
			xPoints[2] = box.leftX;
			break;
		case right:
			g.fillRect(box.leftX, box.topY, box.width - spearTipWidth, box.length);

			xPoints[0] = box.rightX - spearTipWidth;
			xPoints[1] = box.rightX - spearTipWidth;
			xPoints[2] = box.rightX;
			break;
		}

		// Finally, draw the tip
		g.fillPolygon(xPoints, yPoints, 3);
	}

	/**
	 * Draws the given mammoth onto the given graphics object
	 */
	private void paintMammoth(Graphics g, Mammoth m) {
		g.setColor(m.getColor());

		Hitbox oHb = m.getHitbox();

		// this is a mess, I know. Efficiently drawing boxes isn't what I'm here for.

		Hitbox bodyHb;
		Hitbox headHb;

		// shaft is one rectangle that goes straight through the head and makes the
		// first part of each horn
		Hitbox shaftHb;

		// these are the smaller parts of each horn, perpendicular to the shaft
		Hitbox leftHb;
		Hitbox rightHb;

		switch (m.getDirection()) {
		case up:
			bodyHb = new Hitbox(oHb.leftX, oHb.topY + MAMMOTH_HEAD_LENGTH, oHb.width, oHb.length - MAMMOTH_HEAD_LENGTH);

			headHb = new Hitbox(oHb.leftX + (oHb.width - MAMMOTH_HEAD_WIDTH) / 2, oHb.topY, MAMMOTH_HEAD_WIDTH,
					MAMMOTH_HEAD_LENGTH);

			shaftHb = new Hitbox(oHb.leftX, oHb.topY + MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET - MAMMOTH_HORN_WIDTH,
					oHb.width, MAMMOTH_HORN_WIDTH);

			leftHb = new Hitbox(oHb.leftX, oHb.topY, MAMMOTH_HORN_WIDTH, MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET);

			rightHb = new Hitbox(oHb.rightX - MAMMOTH_HORN_WIDTH, oHb.topY, MAMMOTH_HORN_WIDTH,
					MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET);
			break;
		case down:
			bodyHb = new Hitbox(oHb.leftX, oHb.topY, oHb.width, oHb.length - MAMMOTH_HEAD_LENGTH);

			headHb = new Hitbox(oHb.leftX + (oHb.width - MAMMOTH_HEAD_WIDTH) / 2, oHb.bottomY - MAMMOTH_HEAD_LENGTH,
					MAMMOTH_HEAD_WIDTH, MAMMOTH_HEAD_LENGTH);

			shaftHb = new Hitbox(oHb.leftX, oHb.bottomY - MAMMOTH_HEAD_LENGTH + MAMMOTH_HORN_OFFSET, oHb.width,
					MAMMOTH_HORN_WIDTH);

			leftHb = new Hitbox(oHb.leftX, oHb.bottomY - MAMMOTH_HEAD_LENGTH + MAMMOTH_HORN_OFFSET, MAMMOTH_HORN_WIDTH,
					MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET);

			rightHb = new Hitbox(oHb.rightX - MAMMOTH_HORN_WIDTH,
					oHb.bottomY - MAMMOTH_HEAD_LENGTH + MAMMOTH_HORN_OFFSET, MAMMOTH_HORN_WIDTH,
					MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET);
			break;
		case left:
			bodyHb = new Hitbox(oHb.leftX + MAMMOTH_HEAD_LENGTH, oHb.topY, oHb.width - MAMMOTH_HEAD_LENGTH, oHb.length);

			headHb = new Hitbox(oHb.leftX, oHb.topY + (oHb.length - MAMMOTH_HEAD_WIDTH) / 2, MAMMOTH_HEAD_LENGTH,
					MAMMOTH_HEAD_WIDTH);

			shaftHb = new Hitbox(oHb.leftX + MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET - MAMMOTH_HORN_WIDTH, oHb.topY,
					MAMMOTH_HORN_WIDTH, oHb.length);

			leftHb = new Hitbox(oHb.leftX, oHb.bottomY - MAMMOTH_HORN_WIDTH, MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET,
					MAMMOTH_HORN_WIDTH);

			rightHb = new Hitbox(oHb.leftX, oHb.topY, MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET, MAMMOTH_HORN_WIDTH);
			break;
		default: // (this is facing right)
			bodyHb = new Hitbox(oHb.leftX, oHb.topY, oHb.width - MAMMOTH_HEAD_LENGTH, oHb.length);

			headHb = new Hitbox(oHb.rightX - MAMMOTH_HEAD_LENGTH, oHb.topY + (oHb.length - MAMMOTH_HEAD_WIDTH) / 2,
					MAMMOTH_HEAD_LENGTH, MAMMOTH_HEAD_WIDTH);

			shaftHb = new Hitbox(oHb.rightX - MAMMOTH_HEAD_LENGTH + MAMMOTH_HORN_OFFSET, oHb.topY, MAMMOTH_HORN_WIDTH,
					oHb.length);

			leftHb = new Hitbox(oHb.rightX - MAMMOTH_HEAD_LENGTH + MAMMOTH_HORN_OFFSET, oHb.topY,
					MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET, MAMMOTH_HORN_WIDTH);

			rightHb = new Hitbox(oHb.rightX - MAMMOTH_HEAD_LENGTH + MAMMOTH_HORN_OFFSET,
					oHb.bottomY - MAMMOTH_HORN_WIDTH, MAMMOTH_HEAD_LENGTH - MAMMOTH_HORN_OFFSET, MAMMOTH_HORN_WIDTH);
			break;
		}

		// Now just convert the hitboxes to pixels and draw them
		// god if this code wasn't just art I would spend a lot of time making it better

		PixelBox draw = new PixelBox(bodyHb, panelWidth, panelHeight);
		g.fillRect(draw.leftX, draw.topY, draw.width, draw.length);

		draw = new PixelBox(headHb, panelWidth, panelHeight);
		g.fillRect(draw.leftX, draw.topY, draw.width, draw.length);

		draw = new PixelBox(shaftHb, panelWidth, panelHeight);
		g.fillRect(draw.leftX, draw.topY, draw.width, draw.length);

		draw = new PixelBox(leftHb, panelWidth, panelHeight);
		g.fillRect(draw.leftX, draw.topY, draw.width, draw.length);

		draw = new PixelBox(rightHb, panelWidth, panelHeight);
		g.fillRect(draw.leftX, draw.topY, draw.width, draw.length);
	}

	/**
	 * A class to represent a rectangle and its dimensions.
	 * 
	 * Units are measured in pixels.
	 * 
	 * @author Sam Thayer
	 */
	public class PixelBox {
		public int leftX;
		public int topY;

		public int width;
		public int length;

		public int rightX;
		public int bottomY;

		/**
		 * Converts the given Hitbox to a PixelBox. The Hitbox's units are percents of
		 * the gameboard, so the new PixelBox's is basically a scaled version, using the
		 * given dimensions of the GamePanel.
		 */
		public PixelBox(Hitbox hb, int maxX, int maxY) {
			this.leftX = (int) (hb.leftX * maxX);
			this.topY = (int) (hb.topY * maxY);
			this.rightX = (int) (hb.rightX * maxX);
			this.bottomY = (int) (hb.bottomY * maxY);
			this.width = rightX - leftX;
			this.length = bottomY - topY;
		}
	}
}
