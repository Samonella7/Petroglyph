package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.SimpleParticipant;

/**
 * A window for playing Petroglyph
 * 
 * @author Sam Thayer
 */
public class GameView extends JPanel {
	private static final long serialVersionUID = 6118153292544416756L;

	/** A panel to display the game area */
	private GamePanel gamePanel;

	/** A Text box to display the level number in */
	private JTextField levelField;

	/** The display for the Mammoth's HP brr */
	private HPBar hpbar;

	/**
	 * Creates a GameViewx
	 * 
	 * @param participants
	 *            An array of participants to display
	 * @param inputHandler
	 *            An object to pass key-press events to
	 */
	public GameView() {
		BorderLayout contentLayout = new BorderLayout();
		setLayout(contentLayout);

		gamePanel = new GamePanel();
		add(gamePanel, BorderLayout.CENTER);

		JPanel sidePanel = new JPanel();
		BoxLayout sideLayout = new BoxLayout(sidePanel, BoxLayout.Y_AXIS);
		sidePanel.setLayout(sideLayout);
		add(sidePanel, BorderLayout.EAST);

		JPanel titlePanel = new JPanel();
		JLabel titleLabel = new JLabel("PETROGLYPH");
		titleLabel.setFont(new Font(null, Font.BOLD, 20));
		titlePanel.add(titleLabel);
		sidePanel.add(titlePanel);

		JPanel levelPanel = new JPanel();

		JLabel levelLabel = new JLabel("Level:");
		levelLabel.setFont(new Font(null, Font.ITALIC, 20));
		levelPanel.add(levelLabel);

		levelField = new JTextField();
		levelField.setEditable(false);
		levelField.setText("1");
		levelField.setBackground(Color.black);
		levelField.setForeground(Color.white);
		levelField.setFont(new Font("", Font.BOLD, 18));
		levelField.setPreferredSize(new Dimension(60, 30));
		levelPanel.add(levelField);

		sidePanel.add(levelPanel);

		JPanel hpbarPanel = new JPanel();
		hpbarPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
		hpbarPanel.setLayout(new BoxLayout(hpbarPanel, BoxLayout.Y_AXIS));
		hpbar = new HPBar();
		hpbar.setPreferredSize(new Dimension(100, 300));
		hpbarPanel.add(hpbar);
		sidePanel.add(hpbarPanel);

		JPanel hpLabelPanel = new JPanel();
		JLabel hpLabel = new JLabel("Mammoth HP");
		hpLabel.setFont(new Font(null, Font.ITALIC, 20));
		hpLabelPanel.add(hpLabel);
		sidePanel.add(hpLabelPanel);

		sidePanel.add(Box.createVerticalGlue());
		sidePanel.add(Box.createVerticalGlue());
	}

	/**
	 * Updates the display, taking into account any changes to the GameWindow's
	 * participants
	 */
	public void update(SimpleParticipant[] participants) {
		gamePanel.update(participants);

		hpbar.hpPercent = participants[0].getHP();
		hpbar.repaint();
	}

	/**
	 * A class to display the Mammoth's HP bar
	 */
	class HPBar extends JPanel {
		private static final long serialVersionUID = -331939071445648092L;

		public double hpPercent;

		public HPBar() {
			hpPercent = 1;
		}

		@Override
		public void paint(Graphics g) {
			int xSize = getWidth();
			int ySize = getHeight();

			g.setColor(Color.red);
			g.fillRect(0, 0, xSize, ySize);

			g.setColor(Color.black);
			g.fillRect(0, 0, xSize, (int) ((1 - hpPercent) * ySize));
		}
	}
}
