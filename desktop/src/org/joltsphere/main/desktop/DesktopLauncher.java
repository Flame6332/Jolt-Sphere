package org.joltsphere.main.desktop;

import org.joltsphere.main.JoltSphereMain;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {

    static LwjglApplicationConfiguration config;

    public static void main (String[] arg) {
        config = new LwjglApplicationConfiguration();

        config.x = 0;
        config.y = 0;

        config.width = JoltSphereMain.Companion.getWIDTH();
        config.height = JoltSphereMain.Companion.getHEIGHT();

        new LwjglApplication(new JoltSphereMain() {
            @Override
            protected void setFPSLimit(int value) {
                config.foregroundFPS = value;
                config.backgroundFPS = value;
            }
        }, config);

    }

}

