package org.joltsphere.mechanics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;

public class StreamBeamTowedPlayer extends StreamBeamPlayer {

	@Override
	public float density() {return 0.3f;}; // overrides the density variable in super class
	
	public StreamBeamTowedPlayer(World world, int x, int y, Color color) {
		super(world, x, y, color);
	}

}
