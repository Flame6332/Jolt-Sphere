package org.joltsphere.main.desktop;

import org.joltsphere.main.JoltSphereMain;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {

    private static LwjglApplicationConfiguration config;

    public static void main(String[] args) {

        config = new LwjglApplicationConfiguration();

        config.x = 0;
        config.y = 0;

        config.width = 3200;//JoltSphereMain.Companion.getWIDTH();
        config.height = 1800;//JoltSphereMain.Companion.getHEIGHT();

        new LwjglApplication(new JoltSphereMain() /*{
            @Override protected void setFPSLimit(int value) {
                config.foregroundFPS = value;
                config.backgroundFPS = value;
            }
        }*/, config);

    }

}


