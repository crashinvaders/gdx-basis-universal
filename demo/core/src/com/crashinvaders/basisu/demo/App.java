package com.crashinvaders.basisu.demo;

import com.badlogic.gdx.*;

public class App extends Game {

    private final PlatformLauncher launcher;
    private final AppParams appParams;
    private final InputMultiplexer inputMultiplexer;

    public App(PlatformLauncher launcher, AppParams appParams) {
        this.launcher = launcher;
        this.appParams = appParams;

        this.inputMultiplexer = new InputMultiplexer();
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        Gdx.input.setInputProcessor(inputMultiplexer);

        Screen initialScreen = createInitialScreen(appParams.screen);
        setScreen(initialScreen);
    }

    @Override
    public void dispose() {
        super.dispose();

        Gdx.input.setInputProcessor(null);

        if (screen != null) {
            screen.dispose();
        }
    }

    @Override
    public void setScreen(Screen screen) {
        if (this.screen != null) {
            this.screen.hide();
            this.screen.dispose();
        }
        this.screen = screen;
        if (this.screen != null) {
            this.screen.show();
            this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    public AppParams getAppParams() {
        return appParams;
    }

    public InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }

    private Screen createInitialScreen(AppParams.Screen screenValue) {
        if (screenValue == null)
            return new MipMapScreen(this);

        switch (screenValue) {
            case MIPMAPS:
                return new MipMapScreen(this);
            case TEXTURE_GALLERY:
                return new TextureGalleryScreen(this);
            default:
                Gdx.app.error("App", "Unexpected AppParams.Screen value: " + screenValue + ". Falling back to gallery screen.");
                return new MipMapScreen(this);
        }
    }
}
