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

	public void startLocalGame(GameView view, int startingLevel) {
		server = new GameEngine(view, 3, startingLevel);
		server.startRound();
	}
	
	public void gameLoss() {
		server = null;
	}
	
	public void roundWin() {
		server.startRound();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent rawInput) {
		if (server != null) {
			return server.dispatchKeyEvent(rawInput);
		}
		return false;
	}
}
