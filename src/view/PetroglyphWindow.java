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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import controller.MainController;
import controller.NetworkingLibrary;

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

	/**
	 * The IP address of the machine this game is running on. If null, there was an
	 * error while trying to obtain it.
	 */
	private String localIP;

	// A host of gui elements. Sorry, I'm not going throw and commenting all of
	// them. The names are self-explanatory anyway.
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

	private JPanel statusPanel;
	private JLabel statusLabel;

	private JButton launchButton;
	private JButton cancelButton;

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
		ipArea.add(ipBox);
		ipArea.setVisible(false);
		lobbyPanel.add(ipArea, BorderLayout.NORTH);
		localIP = NetworkingLibrary.getIP();

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
		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
		// cancelButton's actionListener will be set when it is made visible
		cancelButton.setVisible(false);
		launchPanel.add(cancelButton);
		lobbyPanel.add(launchPanel);

		statusPanel = new JPanel();
		statusLabel = new JLabel();
		statusPanel.add(statusLabel);
		lobbyPanel.add(statusPanel);
		statusPanel.setVisible(false);

		// Why do I have to call these in the constructor? silly java
		revalidate();
		repaint();
	}

	/**
	 * Hides the lobby display and opens the game display
	 */
	protected void enterGameView() {
		lobbyPanel.setVisible(false);
		gameView.setVisible(true);
	}

	/**
	 * Hides the game display and opens the lobby display
	 */
	protected void enterLobbyView() {
		lobbyPanel.setVisible(true);
		gameView.setVisible(false);
	}

	/**
	 * Prevents the user from switching between tabs of the lobby
	 */
	private void lockLobbySection() {
		localGameButton.setEnabled(false);
		hostGameButton.setEnabled(false);
		connectButton.setEnabled(false);
		oneLocalPlayerButton.setEnabled(false);
		twoLocalPlayersButton.setEnabled(false);
	}

	/**
	 * Allows the user to switch between tabs of the lobby (to be used after a call
	 * to lockLobbySection())
	 */
	private void unlockLobbySection() {
		localGameButton.setEnabled(true);
		hostGameButton.setEnabled(true);
		connectButton.setEnabled(true);
		oneLocalPlayerButton.setEnabled(true);
		twoLocalPlayersButton.setEnabled(true);
	}

	/**
	 * Configures the lobby for a wait for a network connection. This mainly means
	 * locking down features that were used to configure the connection.
	 */
	private void beginWaitingForConnection() {
		statusPanel.setVisible(true);
		statusLabel.setText("Waiting for a connection...");
		launchButton.setText("Launch");
		launchButton.setEnabled(false);
		cancelButton.setVisible(true);
		lockLobbySection();

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopWaitingForConnection();
				controller.cancelConnection();
			}
		});
	}

	/**
	 * Notifies the user that a client has connected to their server. This should
	 * only be called while the program is waiting for clients to connect.
	 */
	public void newConnectionAsServer(int playersStillNeeded) {
		statusLabel.setText("Another player connected. Wating for " + playersStillNeeded + " more...");
	}

	/**
	 * Notifies the user that they successfully connected to a server. This should
	 * only be called when the program was connecting to a server.
	 */
	public void connectedAsClient() {
		statusLabel.setText("Connected. Waiting for host to launch game...");
	}

	/**
	 * Notifies the user that they were unable to connect to a server, and opens
	 * options for them to try again or do something else. This should only be
	 * called when the program was connecting to a server.
	 */
	public void failedToConnectAsClient() {
		stopWaitingForConnection();
		JOptionPane.showMessageDialog(null, "Failed to connect. Are you sure that there is a server running at "
				+ ipBox.getText() + "? Are you sure that this game has internet access?");
	}

	/**
	 * Unfreezes features related to configuring network connections. Specifically,
	 * undoes the effects of beginWaitingForConnection()
	 * 
	 * @param launchButtonText
	 */
	private void stopWaitingForConnection() {
		unlockLobbySection();
		statusPanel.setVisible(false);
		launchButton.setEnabled(true);
		cancelButton.setVisible(false);

		if (hostGameButton.isSelected())
			launchButton.setText("Open Server");
		else
			launchButton.setText("Connect");

		ActionListener[] oldListeners = cancelButton.getActionListeners();
		for (ActionListener a : oldListeners)
			cancelButton.removeActionListener(a);

		// This part only does something if readyToLaunchAsServer had been called,
		// but doesn't hurt otherwise
		oldListeners = launchButton.getActionListeners();
		for (ActionListener a : oldListeners)
			launchButton.removeActionListener(a);
		launchButton.addActionListener(this);
	}

	/**
	 * Allows the user to launch the game. Should only be called when the game was
	 * waiting for clients.
	 */
	public void readyToLaunchAsServer() {
		launchButton.setEnabled(true);
		statusLabel.setText("Ready to play");

		ActionListener oldListener = launchButton.getActionListeners()[0];
		launchButton.removeActionListener(oldListener);

		launchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				launchButton.removeActionListener(this);
				launchButton.addActionListener(oldListener);
				stopWaitingForConnection();
				enterGameView();
				controller.startGameAsServer(gameView, Integer.parseInt(startingLevelBox.getText()));
			}
		});
	}

	/**
	 * Sets up the view for playing as a client
	 */
	public GameView readyToLaunchAsClient() {
		launchButton.setEnabled(true);
		statusLabel.setText("Ready to play");
		stopWaitingForConnection();
		enterGameView();
		return gameView;
	}

	/**
	 * Notifies the user that a connection error occurred, and closes gui elements
	 * for any active operation. This is a general purpose method that can be called
	 * any time while the user is connected to another instance of Petroglyph.
	 */
	public void lostConnection() {
		JOptionPane.showMessageDialog(null,
				"A connection error occured. Either you lost internet access, or one of the other players disconnected.");
		if (lobbyPanel.isVisible()) {
			stopWaitingForConnection();
		}

		else {
			enterLobbyView();
		}
	}

	// This is the actionListener for most buttons
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
			if (localIP == null) {
				ipBox.setText("error");
			} else {
				ipBox.setText(localIP);
			}
			launchButton.setText("Open Server");
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
		} else if (e.getSource() == twoLocalPlayersButton) {
			oneLocalPlayerButton.setSelected(false);
		}

		else if (e.getSource() == launchButton) {
			launchButtonPressed();
		}
	}

	/**
	 * A helper method to be called when the launch button is pressed
	 */
	private void launchButtonPressed() {
		// behavior depends on which tab the user was in

		if (localGameButton.isSelected()) {
			enterGameView();
			controller.startLocalGame(gameView, Integer.parseInt(startingLevelBox.getText()));
		}

		else if (hostGameButton.isSelected()) {
			int localPlayerCount = oneLocalPlayerButton.isSelected() ? 1 : 2;

			if (controller.startServer(localPlayerCount)) {
				beginWaitingForConnection();
			} else {
				JOptionPane.showMessageDialog(null,
						"A connection error occured. Are you sure that this game has access to the internet?");
			}
		}

		else if (connectButton.isSelected()) {
			beginWaitingForConnection();
			controller.startClient(ipBox.getText());
		}
	}

}
