package org.joltsphere.main.desktop;

import org.joltsphere.main.JoltSphereMain;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	
	public static void main (String[] arg) {
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.x = 0;
		config.y = 0;
		
		config.width = JoltSphereMain.WIDTH;
		config.height = JoltSphereMain.HEIGHT;

		new LwjglApplication(new JoltSphereMain(), config);
	
	}
	
}

