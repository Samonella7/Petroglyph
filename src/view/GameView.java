package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.Caveman;
import model.Participant;
import model.Spear;
import model.Participant.Direction;

public class GameView extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Color BACKGROUND_COLOR = new Color(33, 88, 00);

	private PetroglyphCanvas canvas;
	private Participant[] participants;

	public GameView(Participant[] participants, KeyEventDispatcher inputHandler) {
		this.participants = participants;

		setSize(800, 800);

		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(inputHandler);

		setTitle("Petroglyph");
		JPanel content = new JPanel();
		BorderLayout contentLayout = new BorderLayout();
		content.setLayout(contentLayout);
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.add(content);

		canvas = new PetroglyphCanvas();
		content.add(canvas);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public void update() {
		canvas.repaint();
	}

	class PetroglyphCanvas extends JPanel {
		private static final long serialVersionUID = 2L;

		private int panelWidth;
		private int panelHeight;

		private int spearTipWidth;
		private int spearTipHeight;

		private void recalculateConstants() {
			panelWidth = getWidth();
			panelHeight = getHeight();

			spearTipWidth = panelWidth / 70;
			spearTipHeight = panelHeight / 70;
		}

		@Override
		public void paint(Graphics g) {
			recalculateConstants();

			g.setColor(BACKGROUND_COLOR);
			g.fillRect(0, 0, panelWidth, panelHeight);

			for (Participant p : participants) {
				PixelBox dimensions = new PixelBox(p.getHitbox(), panelWidth, panelHeight);

				if (p instanceof Caveman) {
					paintCaveman(g, (Caveman) p, dimensions);
				} else {
					paintSpear(g, (Spear) p, dimensions);
				}
			}
		}

		public void paintCaveman(Graphics g, Caveman c, PixelBox hb) {
			g.setColor(c.getColor());
			g.fillRect(hb.topLeftX, hb.topLeftY, hb.width, hb.length);
		}

		public void paintSpear(Graphics g, Spear s, PixelBox hb) {
			g.setColor(s.getColor());

			// The distance from the back corner of the spear tip to the shaft
			// declared here instead of in the if to satisfy the silly compiler
			int offset;

			// these represent the points of the triangle's tip
			int[] xPoints = new int[3];
			int[] yPoints = new int[3];

			// Calculating them is non-trivial
			if (s.getDirection() == Direction.up || s.getDirection() == Direction.down) {
				offset = (spearTipWidth - hb.width) / 2;
				xPoints[0] = hb.topLeftX - offset;
				xPoints[1] = hb.bottomRightX + offset;
				xPoints[2] = hb.topLeftX + (hb.width / 2);
			} else {
				offset = (spearTipHeight - hb.length) / 2;
				yPoints[0] = hb.topLeftY - offset;
				yPoints[1] = hb.bottomRightY + offset;
				yPoints[2] = hb.topLeftY + (hb.length / 2);
			}

			// Each case draws the shaft and calculates the points that couldn't be done above
			switch (s.getDirection()) {
			case up:
				g.fillRect(hb.topLeftX, hb.topLeftY + spearTipHeight, hb.width, hb.length - spearTipHeight);

				yPoints[0] = hb.topLeftY + spearTipHeight;
				yPoints[1] = hb.topLeftY + spearTipHeight;
				yPoints[2] = hb.topLeftY;
				break;
			case down:
				g.fillRect(hb.topLeftX, hb.topLeftY, hb.width, hb.length - spearTipHeight);

				yPoints[0] = hb.bottomRightY - spearTipHeight;
				yPoints[1] = hb.bottomRightY - spearTipHeight;
				yPoints[2] = hb.bottomRightY;
				break;
			case left:
				g.fillRect(hb.topLeftX + spearTipWidth, hb.topLeftY, hb.width - spearTipWidth, hb.length);
				
				xPoints[0] = hb.topLeftX + spearTipWidth; 
				xPoints[1] = hb.topLeftX + spearTipWidth;
				xPoints[2] = hb.topLeftX;
				break;
			case right:
				g.fillRect(hb.topLeftX, hb.topLeftY, hb.width - spearTipWidth, hb.length);
				
				xPoints[0] = hb.bottomRightX - spearTipWidth; 
				xPoints[1] = hb.bottomRightX - spearTipWidth;
				xPoints[2] = hb.bottomRightX;
				break;
			}

			// Finally, draw the tip
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}
}
