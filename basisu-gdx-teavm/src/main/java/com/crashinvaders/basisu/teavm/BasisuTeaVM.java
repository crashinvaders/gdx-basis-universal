package com.crashinvaders.basisu.teavm;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLScriptElement;

/**
 * TeaVM-specific setup for the Basis Universal library.
 */
public class BasisuTeaVM {

    private static volatile boolean initialized = false;

    private BasisuTeaVM() {}

    /**
     * Loads the Basis Universal Emscripten/Wasm glue script and waits for it to be fully ready
     * (the wasm module instantiated, not just the script tag executed) before running {@code onReady}.
     * <p/>
     * Wrap your {@code new WebApplication(...)} call with {@code onReady} in your
     * {@code WebLauncher}'s {@code main()} - everything else can stay where it is:
     * <pre>{@code
     * WebApplicationConfiguration config = new WebApplicationConfiguration("canvas");
     * // ... your own config setup ...
     * BasisuTeaVM.initialize(config, () -> new WebApplication(new MyApp(), config));
     * }</pre>
     * Skipping this call is a hard error: {@code BasisuNativeLibLoader.loadIfNeeded()} - invoked by
     * every basisu-gdx entry point - throws with instructions pointing back here.
     */
    public static void initialize(WebApplicationConfiguration config, Runnable onReady) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        if (onReady == null) {
            throw new IllegalArgumentException("onReady must not be null.");
        }
        if (initialized) {
            throw new IllegalStateException("BasisuTeaVM.initialize(...) was already called.");
        }
        if (Gdx.app != null) {
            throw new IllegalStateException(
                    "BasisuTeaVM.initialize(...) was called too late - Gdx.app is already set, " +
                    "meaning a WebApplication was already constructed. Construct it only inside " +
                    "the onReady callback passed to this method.");
        }
        initialized = true;

        String scriptUrl = config.baseUrlProvider.getBaseUrl() + "scripts/gdx-basis-universal.js";
        loadScript(scriptUrl, onReady);
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void loadScript(String url, Runnable onReady) {
        HTMLDocument document = Window.current().getDocument();
        HTMLScriptElement script = (HTMLScriptElement) document.createElement("script");
        script.addEventListener("load", evt -> awaitModuleReady(onReady));
        script.addEventListener("error", evt -> {
            throw new RuntimeException("Failed to load the Basis Universal glue script: " + url);
        });
        script.setSrc(url);
        document.getBody().appendChild(script);
    }

    private static void awaitModuleReady(Runnable onReady) {
        if (isModuleLoaded()) {
            onReady.run();
        } else {
            Window.current().addEventListener("basisuModuleLoaded", evt -> onReady.run());
        }
    }

    /** Set by web-lib-setup.js once {@code createBasisuGdxModule()}'s promise resolves. */
    @JSBody(script = "return !!window.basisuModule;")
    private static native boolean isModuleLoaded();
}
