package controller;

import java.io.IOException;
import java.util.ArrayList;

import controller.NetworkingLibrary.NetworkConnectionHandler;
import controller.NetworkingLibrary.NetworkUpdateHandler;
import view.PetroglyphWindow;
import controller.NetworkingLibrary.NetworkConnection;
import controller.NetworkingLibrary.NetworkListener;

public class Server implements NetworkConnectionHandler, NetworkUpdateHandler {
	private NetworkListener listener;
	private ArrayList<NetworkConnection> allClients;
	private int remotePlayersNeeded;

	private PetroglyphWindow window;
	private MainController controller;

	/**
	 * @throws IOException
	 */
	public Server(PetroglyphWindow window, MainController controller, int localPlayerCount) throws IOException {
		this.window = window;
		this.controller = controller;
		allClients = new ArrayList<NetworkConnection>();
		remotePlayersNeeded = 3 - localPlayerCount;
		listener = NetworkingLibrary.openServer(this, '\n');

		if (listener == null) {
			throw new IOException();
		}
	}

	public void close() {
		NetworkingLibrary.closeListener(listener);
		for (NetworkConnection s : allClients) {
			NetworkingLibrary.closeConnection(s);
		}
	}

	@Override
	public void initialConnectionUpdate(NetworkConnection connection, boolean success) {
		if (success) {
			allClients.add(connection);
			window.newConnectionAsServer(remotePlayersNeeded - allClients.size());
			NetworkingLibrary.getData(connection, this);
		}

		if (allClients.size() == remotePlayersNeeded) {
			// TODO
		} else {
			NetworkingLibrary.resumeAcceptingClients(listener);
		}
	}

	@Override
	public void connectionUpdate(NetworkConnection connection, boolean success, String message) {
		if (!success) {
			allClients.remove(connection);
			controller.lostConnection();
		}
	}
}
