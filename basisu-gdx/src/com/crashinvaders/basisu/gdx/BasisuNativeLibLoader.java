package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class BasisuNativeLibLoader {

    private static boolean nativeLibLoaded = false;

    /**
     * Ensures that the basisu-wrapper native library is loaded and initialized.
     */
    public static synchronized void loadIfNeeded() {
        if (nativeLibLoaded) return;

        // No need to load for GWT (native lib is loaded by the "basisu-gdx-gwt" module).
        if (Gdx.app.getType() == Application.ApplicationType.WebGL) {
            nativeLibLoaded = true;
            return;
        }

        new SharedLibraryLoader().load("gdx-basis-universal");
        nativeLibLoaded = true;
    }
}
