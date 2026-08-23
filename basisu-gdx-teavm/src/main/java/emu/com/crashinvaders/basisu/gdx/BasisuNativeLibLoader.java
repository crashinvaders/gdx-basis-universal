package emu.com.crashinvaders.basisu.gdx;

import com.crashinvaders.basisu.teavm.BasisuTeaVM;

/**
 * TeaVM counterpart of {@code com.crashinvaders.basisu.gdx.BasisuNativeLibLoader}. Setup happens in
 * {@link BasisuTeaVM#initialize}, not here. Every basisu-gdx entry point still calls
 * {@code loadIfNeeded()} first though, which makes it a convenient early, actionable check that the
 * app actually called that setup before trying (and failing much less clearly) to use the native
 * module.
 */
public class BasisuNativeLibLoader {

    public static synchronized void loadIfNeeded() {
        if (!BasisuTeaVM.isInitialized()) {
            throw new IllegalStateException(
                    "Basis Universal was not initialized for TeaVM. " +
                    "Call BasisuTeaVM.initialize(config) in your WebLauncher's main(), " +
                    "right after building the WebApplicationConfiguration and before " +
                    "constructing WebApplication.");
        }
    }
}
