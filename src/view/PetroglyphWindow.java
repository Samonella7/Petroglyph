package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import controller.MainController;

/**
 * The main window for the program. Starts as a lobby for connecting with
 * friends, and replaces everything with a GameView once the game starts.
 * 
 * @author Sam Thayer
 */
public class PetroglyphWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = -6023228881396349805L;

	private static final int BUTTON_WIDTH = 120;
	private static final int BUTTON_HEIGHT = 25;

	/** A reference to the MainController */
	private MainController controller;

	/** The GameView, when there is one */
	private GameView gameView;

	/** A JPanel for displaying the lobby */
	private JPanel lobbyPanel;

	private JRadioButton localGameButton;
	private JRadioButton hostGameButton;
	private JRadioButton connectButton;

	private JTextField ipBox;

	private JButton launchButton;

	/**
	 * Creates a GameView
	 * 
	 * @param participants
	 *            An array of participants to display
	 * @param inputHandler
	 *            An object to pass key-press events to
	 */
	public PetroglyphWindow(MainController controller, KeyEventDispatcher inputHandler) {
		this.controller = controller;

		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(inputHandler);

		setSize(900, 800);
		setTitle("Petroglyph");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

		JPanel content = new JPanel();
		BorderLayout contentLayout = new BorderLayout();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content.setLayout(contentLayout);
		this.add(content);

		lobbyPanel = new JPanel();
		content.add(lobbyPanel, BorderLayout.NORTH);
		BoxLayout lobbyLayout = new BoxLayout(lobbyPanel, BoxLayout.Y_AXIS);
		lobbyPanel.setLayout(lobbyLayout);

		JPanel titlePanel = new JPanel();
		JLabel titleLabel = new JLabel("PETROGLYPH");
		titleLabel.setFont(new Font(null, Font.BOLD, 32));
		titlePanel.add(titleLabel);
		lobbyPanel.add(titlePanel);

		JPanel buttonsArea = new JPanel();
		lobbyPanel.add(buttonsArea);

		localGameButton = new JRadioButton("Local Game");
		localGameButton.setSelected(true);
		localGameButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
		localGameButton.addActionListener(this);
		buttonsArea.add(localGameButton);

		hostGameButton = new JRadioButton("Host Game");
		hostGameButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
		hostGameButton.addActionListener(this);
		buttonsArea.add(hostGameButton);

		connectButton = new JRadioButton("Connect");
		connectButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
		connectButton.addActionListener(this);
		buttonsArea.add(connectButton);

		gameView = new GameView();
		gameView.setVisible(false);
		content.add(gameView, BorderLayout.CENTER);

		JPanel ipArea = new JPanel();
		ipArea.add(new JLabel("IP: "));
		ipBox = new JTextField("Nope");
		ipBox.setPreferredSize(new Dimension(200, 20));
		ipBox.setEnabled(false);
		ipArea.add(ipBox);
		lobbyPanel.add(ipArea, BorderLayout.NORTH);

		JPanel launchPanel = new JPanel();
		launchButton = new JButton("Launch");
		launchButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
		launchButton.addActionListener(this);
		launchPanel.add(launchButton);
		lobbyPanel.add(launchPanel);

		// There is no reason that I should have to call these at the end of the
		// constructor.
		// Thank you swing.
		revalidate();
		repaint();
	}

	private void enterGameView() {
		lobbyPanel.setVisible(false);
		gameView.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == localGameButton) {
			hostGameButton.setSelected(false);
			connectButton.setSelected(false);
			ipBox.setEnabled(false);
			ipBox.setEditable(false);
			ipBox.setText("Nope");
			launchButton.setText("Launch");
		}

		else if (e.getSource() == hostGameButton) {
			localGameButton.setSelected(false);
			connectButton.setSelected(false);
			ipBox.setEnabled(true);
			ipBox.setEditable(false);
			ipBox.setText("TODO");
			launchButton.setText("Launch");
		}

		else if (e.getSource() == connectButton) {
			localGameButton.setSelected(false);
			hostGameButton.setSelected(false);
			ipBox.setEnabled(true);
			ipBox.setEditable(true);
			ipBox.setText("");
			launchButton.setText("Connect");
		}

		else if (e.getSource() == launchButton) {
			if (localGameButton.isSelected()) {
				enterGameView();
				controller.startLocalGame(gameView);
			} else if (hostGameButton.isSelected()) {
				// TODO
			} else if (connectButton.isSelected()) {
				// TODO
			}
		}
	}

}
