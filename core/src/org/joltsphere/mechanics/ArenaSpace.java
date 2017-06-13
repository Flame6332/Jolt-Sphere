package org.joltsphere.mechanics;

import com.badlogic.gdx.Gdx;
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
	
	public void input(int player, int up, int down, int left, int right, int modifier) {
		if (Gdx.input.isKeyPressed(modifier)) {
			if (Gdx.input.isKeyPressed(up)) players.get(player).magnify();
		}
		else {
			if (Gdx.input.isKeyJustPressed(up)) players.get(player).jump();
			else if (Gdx.input.isKeyPressed(down)) players.get(player).smash(); // added not smashing to notify when finger released
				else players.get(player).notSmashing(); //place at top for smash density reset 
			if (Gdx.input.isKeyPressed(left)) players.get(player).moveLeft(1);
			if (Gdx.input.isKeyPressed(right)) players.get(player).moveRight(1);
			if (Gdx.input.isKeyPressed(up)) players.get(player).jumpHold();
		}
	}
	
	
	
}
