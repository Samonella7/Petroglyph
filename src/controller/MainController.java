package controller;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import view.GameView;
import view.PetroglyphWindow;

public class MainController implements KeyEventDispatcher {
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		MainController c = new MainController();
	}

	private GameEngine server;
	private PetroglyphWindow window;

	public MainController() {
		window = new PetroglyphWindow(this, this);
	}

	public void startLocalGame(GameView view) {
		server = new GameEngine(view, 3);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent rawInput) {
		if (server != null) {
			return server.dispatchKeyEvent(rawInput);
		}
		return false;
	}
}
