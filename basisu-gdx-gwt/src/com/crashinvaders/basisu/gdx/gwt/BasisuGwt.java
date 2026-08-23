package com.crashinvaders.basisu.gdx.gwt;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;

/**
 * GWT-specific setup for the Basis Universal library.
 */
public class BasisuGwt {

    private static boolean initialized = false;

    private BasisuGwt() {}

    /**
     * Loads the Basis Universal Emscripten/Wasm glue script and waits for it to be fully ready
     * (the wasm module instantiated, not just the script tag executed) before running {@code onReady}.
     * <p/>
     * Call this as the first line of your {@code GwtApplication}'s {@code onModuleLoad()} override,
     * wrapping the rest of it:
     * <pre>{@code
     * @Override
     * public void onModuleLoad() {
     *     BasisuGwt.initialize(() -> super.onModuleLoad());
     * }
     * }</pre>
     * Skipping this call is a hard error: {@code BasisuNativeLibLoader.loadIfNeeded()} - invoked by
     * every basisu-gdx entry point - throws with instructions pointing back here.
     */
    public static void initialize(Runnable onReady) {
        if (onReady == null) {
            throw new IllegalArgumentException("onReady must not be null.");
        }
        if (initialized) {
            throw new IllegalStateException("BasisuGwt.initialize(onReady) was already called.");
        }
        initialized = true;

        ScriptInjector.fromUrl(GWT.getModuleBaseURL() + "gdx-basis-universal.js")
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(Void result) {
                        awaitModuleReady(onReady);
                    }

                    @Override
                    public void onFailure(Exception reason) {
                        throw new RuntimeException("Failed to load the Basis Universal glue script.", reason);
                    }
                })
                .inject();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void awaitModuleReady(Runnable onReady) {
        if (isModuleLoaded()) {
            onReady.run();
        } else {
            addModuleLoadedListener(onReady);
        }
    }

    /** Set by web-lib-setup.js once {@code createBasisuGdxModule()}'s promise resolves. */
    private static native boolean isModuleLoaded() /*-{
        return !!$wnd.basisuModule;
    }-*/;

    private static native void addModuleLoadedListener(Runnable listener) /*-{
        $wnd.addEventListener("basisuModuleLoaded", function () {
            listener.@java.lang.Runnable::run()();
        }, { once: true });
    }-*/;
}
