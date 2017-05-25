package org.joltshpere.main.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import org.joltshpere.testing.main.JoltSphereTesting;

public class TestingLauncher {
	
	public static void main (String[] arg) {
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
	
		config.width = JoltSphereTesting.WIDTH;
		config.height = JoltSphereTesting.HEIGHT;
		
		config.foregroundFPS = JoltSphereTesting.FPS;
		config.backgroundFPS = 60;
		
		new LwjglApplication(new JoltSphereTesting(), config);
	
	}
	
}