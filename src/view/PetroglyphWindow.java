package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

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
	
	private static final int TEXT_AREA_HEIGHT = 20;
	
	protected static final int WINDOW_BORDER = 10;

	/** A reference to the MainController */
	private MainController controller;

	/** The GameView, when there is one */
	private GameView gameView;

	/** A JPanel for displaying the lobby */
	private JPanel lobbyPanel;

	private JRadioButton localGameButton;
	private JRadioButton hostGameButton;
	private JRadioButton connectButton;

	private JPanel ipArea;
	private JTextField ipBox;

	private JPanel localPlayersArea;
	private JRadioButton oneLocalPlayerButton;
	private JRadioButton twoLocalPlayersButton;

	private JPanel startingLevelArea;
	private JFormattedTextField startingLevelBox;
	
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
		content.setBorder(BorderFactory.createEmptyBorder(WINDOW_BORDER, WINDOW_BORDER, WINDOW_BORDER, WINDOW_BORDER));
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

		gameView = new GameView(controller, this);
		gameView.setVisible(false);
		content.add(gameView, BorderLayout.CENTER);

		ipArea = new JPanel();
		ipArea.add(new JLabel("IP: "));
		ipBox = new JTextField("");
		ipBox.setPreferredSize(new Dimension(200, TEXT_AREA_HEIGHT));
		ipBox.setEnabled(false);
		ipArea.add(ipBox);
		ipArea.setVisible(false);
		lobbyPanel.add(ipArea, BorderLayout.NORTH);
		
		localPlayersArea = new JPanel();
		localPlayersArea.add(new JLabel("Number of local players:"));
		oneLocalPlayerButton = new JRadioButton("One");
		oneLocalPlayerButton.setSelected(true);
		oneLocalPlayerButton.addActionListener(this);
		localPlayersArea.add(oneLocalPlayerButton);
		twoLocalPlayersButton = new JRadioButton("Two");
		twoLocalPlayersButton.addActionListener(this);
		localPlayersArea.add(twoLocalPlayersButton);
		localPlayersArea.setVisible(false);
		lobbyPanel.add(localPlayersArea);
		
		startingLevelArea = new JPanel();
		startingLevelArea.add(new JLabel("Start at level:"));
		NumberFormatter numberFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		numberFormatter.setValueClass(Integer.class);
		numberFormatter.setAllowsInvalid(false);
		numberFormatter.setMinimum(1);
		startingLevelBox = new JFormattedTextField(numberFormatter);
		startingLevelBox.setText("1");
		startingLevelBox.setPreferredSize(new Dimension(BUTTON_WIDTH, TEXT_AREA_HEIGHT));
		startingLevelArea.add(startingLevelBox);
		lobbyPanel.add(startingLevelArea);
		
		JPanel launchPanel = new JPanel();
		launchButton = new JButton("Launch");
		launchButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
		launchButton.addActionListener(this);
		launchPanel.add(launchButton);
		lobbyPanel.add(launchPanel);

		// There is no reason that I should have to call these at the end of the
		// constructor.
		revalidate();
		repaint();
	}

	protected void enterGameView() {
		lobbyPanel.setVisible(false);
		gameView.setVisible(true);
	}
	
	protected void enterLobbyView() {
		lobbyPanel.setVisible(true);
		gameView.setVisible(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == localGameButton) {
			hostGameButton.setSelected(false);
			connectButton.setSelected(false);
			ipArea.setVisible(false);
			launchButton.setText("Launch");
			localPlayersArea.setVisible(false);
			startingLevelArea.setVisible(true);
		}

		else if (e.getSource() == hostGameButton) {
			localGameButton.setSelected(false);
			connectButton.setSelected(false);
			ipArea.setVisible(true);
			ipBox.setEditable(false);
			ipBox.setText("TODO");
			launchButton.setText("Launch");
			localPlayersArea.setVisible(true);
			startingLevelArea.setVisible(true);
		}

		else if (e.getSource() == connectButton) {
			localGameButton.setSelected(false);
			hostGameButton.setSelected(false);
			ipArea.setVisible(true);
			ipBox.setEditable(true);
			ipBox.setText("");
			launchButton.setText("Connect");
			localPlayersArea.setVisible(false);
			startingLevelArea.setVisible(false);
		}
		
		else if (e.getSource() == oneLocalPlayerButton) {
			twoLocalPlayersButton.setSelected(false);
		}
		else if (e.getSource() == twoLocalPlayersButton) {
			oneLocalPlayerButton.setSelected(false);
		}

		else if (e.getSource() == launchButton) {
			if (localGameButton.isSelected()) {
				enterGameView();
				controller.startLocalGame(gameView, Integer.parseInt(startingLevelBox.getText()));
			} else if (hostGameButton.isSelected()) {
				// TODO
			} else if (connectButton.isSelected()) {
				// TODO
			}
		}
	}

}
