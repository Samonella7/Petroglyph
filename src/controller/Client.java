package controller;

import controller.NetworkingLibrary.NetworkConnectionHandler;
import controller.NetworkingLibrary.NetworkUpdateHandler;
import view.PetroglyphWindow;
import controller.NetworkingLibrary.NetworkConnection;

public class Client implements NetworkConnectionHandler, NetworkUpdateHandler {
	private NetworkConnection connection;
	private PetroglyphWindow window;
	private MainController controller;

	public Client(PetroglyphWindow window, MainController controller, String remoteIP) {
		this.window = window;
		this.controller = controller;
		NetworkingLibrary.connectToServer(this, remoteIP, '\n');
	}

	public void close() {
		NetworkingLibrary.closeConnection(connection);
	}
	
	@Override
	public void connectionUpdate(NetworkConnection connection, boolean success, String message) {
		if (!success) {
			controller.lostConnection();
		}
	}

	@Override
	public void initialConnectionUpdate(NetworkConnection connection, boolean success) {
		if (success) {
			this.connection = connection;
			NetworkingLibrary.getData(connection, this);
			window.connectedAsClient();
		} else {
			window.failedToConnectAsClient();
		}
	}



}
