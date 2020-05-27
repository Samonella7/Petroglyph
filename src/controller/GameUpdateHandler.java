package controller;

import model.SimpleParticipant;

/**
 * A type of class that can handle game-related updates. These methods can communicate all the information needed to
 * display a game of Petroglyph from start to finish.
 * 
 * @author Sam Thayer
 */
public interface GameUpdateHandler {
	/**
	 * Indicates that it is time for a new frame of the game.
	 * 
	 * @param participants
	 *            An array of all the game's participants, including their locations and states at the time of the new
	 *            frame.
	 */
	public void newFrame(SimpleParticipant[] participants);

	/**
	 * Indicates that a new round of the game is starting
	 * 
	 * @param level
	 *            The new round's difficulty level.
	 */
	public void startRound(int level);

	/**
	 * Indicates that the players won a round of the game.
	 * 
	 * @param engine
	 *            An object to notify when the user is ready for the next round. This may be null, in which case a different
	 *            GameUpdateHandler will decide when to start the next round.
	 */
	public void roundWin(GameEngine engine);

	/**
	 * Indicates that the players lost a round of the game. This also means that they lost the entire game.
	 */
	public void roundLoss();
}
