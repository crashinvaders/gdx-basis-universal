package com.crashinvaders.basisu.demo.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.crashinvaders.basisu.demo.App;
import com.crashinvaders.basisu.demo.PlatformLauncher;

public class DesktopLauncher implements PlatformLauncher {

    private final Lwjgl3Application app;

    public static void main(String[] args) {
        new DesktopLauncher();
    }

    public DesktopLauncher() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("gdx-basis-universal");
        config.setResizable(true);
        config.setWindowedMode(800, 480);

        this.app = new Lwjgl3Application(new App(this), config);
    }
}
