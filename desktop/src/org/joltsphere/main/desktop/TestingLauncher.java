package org.joltsphere.main.desktop;

import org.joltsphere.main.JoltSphereMain;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class TestingLauncher {
	
	public static void main (String[] arg) {
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
	
		config.width = JoltSphereMain.WIDTH;
		config.height = JoltSphereMain.HEIGHT;
		
		config.foregroundFPS = JoltSphereMain.FPS;
		config.backgroundFPS = 60;
		
		new LwjglApplication(new JoltSphereMain(), config);
	
	}
	
}