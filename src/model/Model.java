package model;

import java.awt.Color;

import model.Participant.Direction;
import model.Spear.SpearState;

public class Model {
	private Caveman[] cavemen;
	private Spear[] spears;
	
	private Participant[] allParticipants;
	
	public Participant[] getParticipantList() {
		return allParticipants;
	}
	
	public Model () {
		allParticipants = new Participant[6];
		cavemen = new Caveman[3];
		spears = new Spear[3];
		
		cavemen[0] = new Caveman(.48, .1, Color.red);
		spears[0] = cavemen[0].getSpear();
		allParticipants[0] = cavemen[0];
		allParticipants[3] = spears[0];
		
		cavemen[1] = new Caveman(.1, .9, Color.blue);
		spears[1] = cavemen[1].getSpear();
		allParticipants[1] = cavemen[1];
		allParticipants[4] = spears[1];
		
		cavemen[2] = new Caveman(.9, .9, Color.yellow);
		spears[2] = cavemen[2].getSpear();
		allParticipants[2] = cavemen[2];
		allParticipants[5] = spears[2];
		
	}
	
	public void calculateNextFrame () {
		for (Caveman c : cavemen) {
			c.move();
		}
		for (Spear s : spears) {
			s.move();
		}
		
		for (int i = 0; i < cavemen.length; i++) {
			if (spears[i].state == SpearState.grounded && cavemen[i].collidedWith(spears[i])) {
				spears[i].state = SpearState.held;
			}
		}
	}
	
	public void directCaveman (int cavemanNumber, Direction direction, boolean moving) {
		cavemen[cavemanNumber].setDirection(direction);
		cavemen[cavemanNumber].setMoving(moving);
	}
	
	public void tryThrowSpear (int cavemanNumber) {
		spears[cavemanNumber].tryLaunch();
	}
}
