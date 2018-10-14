package view;

import java.awt.BorderLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.Participant;

/**
 * A window for playing Petroglyph
 * 
 * @author Sam Thayer
 */
public class GameView extends JFrame {
	private static final long serialVersionUID = 1L;

	/** A panel to display the game area */
	private GamePanel gamePanel;

	/**
	 * Creates a GameViewx
	 * 
	 * @param participants
	 *            An array of participants to display
	 * @param inputHandler
	 *            An object to pass key-press events to
	 */
	public GameView(Participant[] participants, KeyEventDispatcher inputHandler) {
		setSize(800, 800);

		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(inputHandler);

		setTitle("Petroglyph");
		JPanel content = new JPanel();
		BorderLayout contentLayout = new BorderLayout();
		content.setLayout(contentLayout);
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.add(content);

		gamePanel = new GamePanel(participants);
		content.add(gamePanel);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * Updates the display, taking into account any changes to the GameWindow's
	 * participants
	 */
	public void update() {
		gamePanel.repaint();
	}

}
