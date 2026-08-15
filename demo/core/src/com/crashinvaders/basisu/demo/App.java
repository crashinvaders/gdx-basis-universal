package com.crashinvaders.basisu.demo;

import com.badlogic.gdx.*;

public class App extends Game {

    private final PlatformLauncher launcher;
    private final InputMultiplexer inputMultiplexer;

    public App(PlatformLauncher launcher) {
        this.launcher = launcher;
        this.inputMultiplexer = new InputMultiplexer();
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        Gdx.input.setInputProcessor(inputMultiplexer);

//        setScreen(new MipMapScreen(this));
        setScreen(new TextureGalleryScreen(this));
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

    public InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }
}
