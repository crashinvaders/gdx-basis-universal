package com.crashinvaders.basisu.teavm;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.web.WebAssetPreloadListener;

/**
 * TeaVM-specific setup for the Basis Universal library.
 */
public class BasisuTeaVM {

    private static volatile boolean initialized = false;

    private BasisuTeaVM() {}

    /**
     * Prepares everything Basis Universal needs on the TeaVM/web platform - currently, wiring the
     * Emscripten/Wasm glue script into the TeaVM preload phase. Composes with any
     * {@link WebApplicationConfiguration#preloadListener} already assigned, rather than overwriting it.
     * <p/>
     * Call this once from your {@code WebLauncher}'s {@code main()}, right after building the
     * {@code WebApplicationConfiguration} and before constructing {@code WebApplication}.
     * <p/>
     * Skipping this call is a hard error: {@code BasisuNativeLibLoader.loadIfNeeded()} - invoked by
     * every basisu-gdx entry point - throws with instructions pointing back here.
     */
    public static void initialize(WebApplicationConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        if (initialized) {
            throw new IllegalStateException("BasisuTeaVM.initialize(config) was already called.");
        }
        if (Gdx.app != null) {
            throw new IllegalStateException(
                    "BasisuTeaVM.initialize(config) was called too late - Gdx.app is already set, " +
                    "meaning a WebApplication was already constructed (its preload phase, which this " +
                    "call needs to hook into, has already run). Call it right after building the " +
                    "WebApplicationConfiguration in your WebLauncher's main(), before `new WebApplication(...)`.");
        }

        installPreloadScript(config);

        initialized = true;
    }

    private static void installPreloadScript(WebApplicationConfiguration config) {
        WebAssetPreloadListener existing = config.preloadListener;
        config.preloadListener = assetLoader -> {
            if (existing != null) {
                existing.onPreload(assetLoader);
            }
            assetLoader.loadScript("gdx-basis-universal.js");
        };
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
