package controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import controller.NetworkingLibrary.NetworkConnectionHandler;
import controller.NetworkingLibrary.NetworkUpdateHandler;
import controller.NetworkingLibrary.NetworkConnection;

public class Client extends JFrame implements NetworkConnectionHandler, NetworkUpdateHandler, ActionListener {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		new Client();
	}

	private NetworkConnection connection;

	private JTextField ipField;
	private JButton connectButton;
	private JTextField messageField;
	private JButton sendButton;
	private JTextPane log;

	public Client() {
		// NetworkingLibrary.InitiateConnection(this, "localhost");
		setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(500, 500);

		JPanel content = new JPanel();
		this.add(content);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		JPanel ipPanel = new JPanel();
		ipPanel.add(new JLabel("IP:"));
		ipField = new JTextField("localhost");
		ipField.setPreferredSize(new Dimension(200, 25));
		ipPanel.add(ipField);
		connectButton = new JButton("Connect");
		connectButton.addActionListener(this);
		ipPanel.add(connectButton);
		content.add(ipPanel);

		JPanel inputPanel = new JPanel();
		messageField = new JTextField();
		messageField.setPreferredSize(new Dimension(200, 25));
		inputPanel.add(messageField);
		sendButton = new JButton("Send");
		sendButton.addActionListener(this);
		sendButton.setEnabled(false);
		inputPanel.add(sendButton);
		content.add(inputPanel);

		log = new JTextPane();
		log.setPreferredSize(new Dimension(300, 300));
		log.setEditable(false);
		content.add(log);

		revalidate();
		repaint();
	}

	@Override
	public void connectionUpdate(NetworkConnection connection, boolean connectionIsValid, String message) {
		if (!connectionIsValid) {
			log.setText(log.getText() + "Lost connection to server");
			this.connection = null;
			connectButton.setEnabled(true);
			sendButton.setEnabled(false);
			return;
		}

		log.setText(log.getText() + message + "\n");
		NetworkingLibrary.getData(connection, this);
	}

	@Override
	public void initialConnectionUpdate(NetworkConnection connection, boolean success) {
		if (success) {
			sendButton.setEnabled(true);
			log.setText("");
			this.connection = connection;
			NetworkingLibrary.getData(connection, this);
		} else {
			log.setText("Failed to connect");
			connectButton.setEnabled(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == connectButton) {
			NetworkingLibrary.connectToServer(this, ipField.getText(), '\n');
			connectButton.setEnabled(false);
		}

		else if (e.getSource() == sendButton) {
			String rawText = messageField.getText();
			messageField.setText("");
			NetworkingLibrary.send(connection, rawText.replaceAll("\n", ""));
		}
	}
}
