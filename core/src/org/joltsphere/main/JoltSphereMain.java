package org.joltsphere.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class JoltSphereMain extends ApplicationAdapter {
	
	public static short WIDTH = 1000;
	public static short HEIGHT = 1000;
	
	@Override
	public void create () {
		
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
	}
	
	@Override
	public void dispose () {
		
	}
}