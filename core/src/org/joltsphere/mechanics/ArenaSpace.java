package org.joltsphere.mechanics;

import com.badlogic.gdx.utils.Array;

public class ArenaSpace {

	public Array<ArenaPlayer> players;
	
	public int width, height;
	
	public ArenaSpace(int width, int height) {
		
		players = new Array<ArenaPlayer>();
		
		this.width = width;
		this.height = height; 
		
	}
	 
	public void update(float dv, Array<Integer> contacts) {
		
		for (int i = 1; i <= players.size; i++) {
			players.get(i - 1).update(contacts.get(i - 1), dv, width, height);
		
			if (players.get(i - 1).wasKnockedOut) {
				players.get(i - 1).wasKnockedOut = false; // resets event
				for (int j = 1; j <= players.size; j++) {
					if (j != i) players.get(j - 1).otherPlayerKnockedOut();
				}
			}
			
		}
		
	}
	
	
}
