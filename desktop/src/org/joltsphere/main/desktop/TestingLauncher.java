package org.joltsphere.main.desktop;

import org.joltsphere.testing.main.JoltSphereTesting;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

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