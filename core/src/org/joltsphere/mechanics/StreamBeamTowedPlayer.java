package org.joltsphere.mechanics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;

public class StreamBeamTowedPlayer extends StreamBeamPlayer {

	@Override
	public float density() {return 0.3f;}; // overrides the density variable in super class
	
	public StreamBeamTowedPlayer(World world, int x, int y, Color color) {
		super(world, x, y, color);
		
		body.setLinearDamping(100000);
	}
	
	public void reverseMovement(int up, int left, int right) {
		int x = 15;
		if (Gdx.input.isKeyPressed(up)) body.applyForceToCenter(0, -10, true);
		if (Gdx.input.isKeyPressed(left)) body.applyForceToCenter(x, 0, true);
		if (Gdx.input.isKeyPressed(right)) body.applyForceToCenter(-x, 0, true);
	}
}
