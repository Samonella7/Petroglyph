package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.Hitbox;
import model.Participant;

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

		public PetroglyphCanvas() {
		}

		@Override
		public void paint(Graphics g) {
			int xSize = getWidth();
			int ySize = getHeight();

			g.setColor(BACKGROUND_COLOR);
			g.fillRect(0, 0, xSize, ySize);

			for (Participant p : participants) {
				g.setColor(p.getColor());
				Hitbox hb = p.getHitbox();
				int x1 = (int) (xSize * hb.topLeftX);
				int y1 = (int) (ySize * hb.topLeftY);
				int x2 = (int) (xSize * hb.bottomRightX);
				int y2 = (int) (ySize * hb.bottomRightY);
				g.fillRect(x1, y1, x2 - x1, y2 - y1);
			}
		}
	}
}
