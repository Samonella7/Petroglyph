package controller;

import java.util.HashSet;
import java.util.Scanner;

import controller.NetworkingLibrary.NetworkConnectionHandler;
import controller.NetworkingLibrary.NetworkUpdateHandler;
import controller.NetworkingLibrary.NetworkConnection;
import controller.NetworkingLibrary.NetworkListener;

public class Server implements NetworkConnectionHandler, NetworkUpdateHandler {
	public static void main(String[] args) {
		boolean loop = true;
		try (Scanner in = new Scanner(System.in)) {
			while (loop) {
				Server s = new Server();
				System.out.println("Server launched at " + NetworkingLibrary.getIP() + ". Press enter to terminate");
				in.nextLine();

				s.close();

				System.out.println("To start a new server, enter 'yes'\nTo terminate program, enter anything else");
				if (!in.nextLine().equals("yes"))
					loop = false;
			}
		}
	}

	private NetworkListener listener;
	private HashSet<NetworkConnection> allClients;

	public Server() {
		allClients = new HashSet<NetworkConnection>();

		listener = NetworkingLibrary.openServer(this, '\n');
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
			System.out.println("Accepted new client");
			NetworkingLibrary.getData(connection, this);
		} else {
			System.out.println("A client failed to connect, or the server listener failed");
		}
		NetworkingLibrary.resumeAcceptingClients(listener);
	}

	@Override
	public void connectionUpdate(NetworkConnection connection, boolean success, String message) {
		if (!success) {
			allClients.remove(connection);
			System.out.println("A client disconnected");
		} else {
			for (NetworkConnection s : allClients) {
				NetworkingLibrary.send(s, message);
			}
			NetworkingLibrary.getData(connection, this);
		}
	}
}
