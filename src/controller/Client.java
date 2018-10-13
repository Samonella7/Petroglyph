package controller;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import model.Model;
import model.Participant.Direction;
import view.GameView;

public class Client extends TimerTask implements KeyEventDispatcher {
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Client c = new Client();
	}
	
	public static final int MILLIES_PER_FRAME = 1000/40;
	
	Model model;
	GameView view;
	Timer timer;
	
	ArrayList<Direction> directionalInputs;
	
	public Client () {
		model = new Model();
		view = new GameView(model.getParticipantList(), this);
		
		directionalInputs = new ArrayList<Direction>();

		timer = new Timer();
		timer.scheduleAtFixedRate(this, 0, MILLIES_PER_FRAME);
	}

	@Override
	public void run() {
		model.calculateNextFrame();
		view.update();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent rawInput) {
		if (rawInput.getID() == KeyEvent.KEY_PRESSED)
			handleKeyPress(rawInput.getKeyCode());
		else if (rawInput.getID() == KeyEvent.KEY_RELEASED)
			handleKeyRelease(rawInput.getKeyCode());

		return false;
	}
	
	private void handleKeyPress (int keyCode) {
		if (keyCode == KeyEvent.VK_SHIFT) {
			// TODO throw spear
			return;
		}
		
		Direction direction;
		
		switch (keyCode) {
		case KeyEvent.VK_LEFT:
			direction = Direction.left;
			break;
		case KeyEvent.VK_RIGHT:
			direction = Direction.right;
			break;
		case KeyEvent.VK_UP:
			direction = Direction.up;
			break;
		case KeyEvent.VK_DOWN:
			direction = Direction.down;
			break;
		default:
			return;
		}
		
		if (!directionalInputs.contains(direction)) {
			directionalInputs.add(0, direction);
			model.directCaveman(0, direction, true);
		}
	}
	
	private void handleKeyRelease (int keyCode) {
		Direction direction;
		
		switch (keyCode) {
		case KeyEvent.VK_LEFT:
			direction = Direction.left;
			break;
		case KeyEvent.VK_RIGHT:
			direction = Direction.right;
			break;
		case KeyEvent.VK_UP:
			direction = Direction.up;
			break;
		case KeyEvent.VK_DOWN:
			direction = Direction.down;
			break;
		default:
			return;
		}
		
		if (directionalInputs.remove(direction)) {
			if (directionalInputs.isEmpty()) {
				model.directCaveman(0, direction, false);
			} else {
				model.directCaveman(0, directionalInputs.get(0), true);
			}
		}
	}

}
