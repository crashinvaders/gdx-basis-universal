package com.crashinvaders.basisu.demo.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.crashinvaders.basisu.demo.App;
import com.crashinvaders.basisu.demo.PlatformLauncher;

public class AndroidLauncher extends AndroidApplication implements PlatformLauncher {

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useAccelerometer = false;
        cfg.useCompass = false;
        cfg.useImmersiveMode = true;
        cfg.useWakelock = true;

        app = new App(this);

        initialize(app, cfg);
    }
}
