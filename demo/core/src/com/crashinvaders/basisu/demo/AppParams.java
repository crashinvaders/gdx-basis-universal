package com.crashinvaders.basisu.demo;

import com.badlogic.gdx.utils.Null;

public class AppParams {
    @Null
    public Screen screen = null; // Null means "default" screen.

    public enum Screen {
        MIPMAPS,
        TEXTURE_GALLERY,
    }
}
