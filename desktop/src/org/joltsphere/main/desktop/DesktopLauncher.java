package org.joltsphere.main.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.joltsphere.main.JoltSphereMain;

public class DesktopLauncher {

    static Lwjgl3ApplicationConfiguration config;

    public static void main(String[] arg) {
        config = new Lwjgl3ApplicationConfiguration();

        config.setWindowedMode(JoltSphereMain.Companion.getWIDTH(), JoltSphereMain.Companion.getHEIGHT());

        new Lwjgl3Application(new JoltSphereMain(), config);
        /*new Lwjgl3Application(new JoltSphereMain() {
            @Override
            protected void setFPSLimit(int value) {
                config.foregroundFPS = value;
                config.backgroundFPS = value;
            }
        }, config);
*/
    }

}