package com.crashinvaders.basisu.gdx;

import com.crashinvaders.basisu.gdx.gwt.BasisuGwt;

/**
 * GWT counterpart of {@code com.crashinvaders.basisu.gdx.BasisuNativeLibLoader}. Setup happens in
 * {@link BasisuGwt#initialize}, not here. Every basisu-gdx entry point still calls
 * {@code loadIfNeeded()} first though, which makes it a convenient early, actionable check that the
 * app actually called that setup before trying (and failing much less clearly) to use the native
 * module.
 */
public class BasisuNativeLibLoader {

    public static synchronized void loadIfNeeded() {
        if (!BasisuGwt.isInitialized()) {
            throw new IllegalStateException(
                    "Basis Universal was not initialized for GWT. " +
                    "Call BasisuGwt.initialize(() -> super.onModuleLoad()) as the first line of " +
                    "your GwtApplication's onModuleLoad() override.");
        }
    }
}
