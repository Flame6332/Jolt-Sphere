package org.joltsphere.mechanics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class ArenaSpace {

	public Array<ArenaPlayer> players;
	
	public int width, height;
	
	public ArenaSpace(int width, int height) {
		
		players = new Array<ArenaPlayer>();
		
		this.width = width;
		this.height = height; 
		
	}
	 
	public void update(float dt, Array<Integer> contacts) {
		
		for (int i = 1; i <= players.size; i++) {
			players.get(i - 1).update(contacts.get(i - 1), dt, width, height);
		
			if (players.get(i - 1).wasKnockedOut) {
				players.get(i - 1).wasKnockedOut = false; // resets event
				for (int j = 1; j <= players.size; j++) {
					if (j != i) players.get(j - 1).otherPlayerKnockedOut();
				}
			}
			
		}
		
	}
	
	public void shapeRender(ShapeRenderer shapeRender) {
		for (int i = 0; i < players.size; i++) {
			players.get(i).shapeRender(shapeRender);
		}
		
		for (int i = 1; i <= players.first().paint.size; i++) { // all paint trails the same
			for (int j = 0; j < players.size; j++) {
				players.get(j).renderPaint(shapeRender, i);
			}		
		}
		
		shapeRender.rect(-10000 - width/2f, height * 1.5f, 20000, 200, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
		shapeRender.rect(-10000 - width/2f, height * -0.5f - 200, 20000, 200, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
		
	}
	
	public void input(int player, int up, int down, int left, int right, int modifier) {
		/*if (Gdx.input.isKeyPressed(modifier)) {
			if (Gdx.input.isKeyPressed(up)) playerMagnified(player);
		}*/
		if (Gdx.input.isKeyPressed(modifier)) playerMagnified(player);
		else {
			if (Gdx.input.isKeyJustPressed(up)) players.get(player).jump();
			else if (Gdx.input.isKeyPressed(down)) players.get(player).smash(); // added not smashing to notify when finger released
				else players.get(player).notSmashing(); //place at top for smash density reset 
			if (Gdx.input.isKeyPressed(left)) players.get(player).moveLeft(1);
			if (Gdx.input.isKeyPressed(right)) players.get(player).moveRight(1);
			if (Gdx.input.isKeyPressed(up)) players.get(player).jumpHold();
		}
		if (!Gdx.input.isKeyPressed(modifier)) players.get(player).notMagnifying();
	}
	
	public void playerMagnified(int player) {
		if (players.get(player).canMagnify()) {	
			Vector2 magnifyingPlayer = new Vector2();
			Array<Vector2> otherPlayers = new Array<Vector2>();
			for (int i = 0; i < players.size; i++) {
				if (i == player) {
					magnifyingPlayer = players.get(i).body.getPosition();
				}
				else {
					otherPlayers.add(new Vector2(players.get(i).body.getPosition()));
				}
 			}
			players.get(player).magnify(otherPlayers.first());
			for (int i = 0; i < players.size; i++) {
				if (i != player) {
					players.get(i).otherPlayerMagnified(magnifyingPlayer);
				}
 			}
			
		}
	}
	
}
