package com.crashinvaders.basisu.demo.teavm;

import com.badlogic.gdx.ApplicationListener;
import com.crashinvaders.basisu.demo.App;
import com.crashinvaders.basisu.demo.AppParams;
import com.crashinvaders.basisu.demo.PlatformLauncher;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.web.WebPreloadApplicationListener;
import org.teavm.jso.browser.Location;

public class WebLauncher implements PlatformLauncher {

    public static void main(String[] args) {
        WebApplicationConfiguration config = new WebApplicationConfiguration("canvas");
        config.width = 0;
        config.height = 0;
        config.showDownloadLogs = true;
        // The Basis Universal Emscripten/Wasm glue (shared with the GWT backend) is a plain
        // classic script that attaches itself to the global "basisuModule" once loaded.
        // Shipped alongside the other TeaVM assets - see gdxTeaVM.assets in build.gradle.
        // loadScript() resolves relative to the "scripts/" folder, so climb back up to the
        // webapp root and into "assets/" from there.
        config.preloadListener = assetLoader -> assetLoader.loadScript("../assets/gdx-basis-universal.js");

        AppParams params = new AppParams();

        String screenParam = getUrlParameter("screen");
        if (screenParam != null) {
            try {
                params.screen = AppParams.Screen.valueOf(screenParam.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Unexpected \"screen\" URL parameter value: " + screenParam);
            }
        }

        // Keep the default WebPreloadApplicationListener - it's what actually fetches every
        // declared asset (gdxTeaVM.assets, i.e. everything under demo/assets plus the basisu js/wasm)
        // into memory before signaling the app ready; skipping it entirely makes Gdx.files.internal()
        // fail with "File not found" once the real App starts. Its own dispose() can occasionally NPE
        // (disposes a Stage that's still being built asynchronously when preload finishes very fast),
        // so override it away - it's a one-time app-boot transition, leaking one small splash
        // texture/stage until page unload is harmless.
        ApplicationListener preloadListener = new WebPreloadApplicationListener() {
            @Override
            public void dispose() {
            }
        };
        new WebApplication(new App(new WebLauncher(), params), preloadListener, config);
    }

    /** Location#getSearch() returns the raw "?key=value&..." query string - no URLSearchParams in teavm-jso-apis. */
    private static String getUrlParameter(String name) {
        String search = Location.current().getSearch();
        if (search == null || search.isEmpty()) return null;
        for (String pair : search.substring(1).split("&")) {
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            if (key.equals(name)) {
                return eq < 0 ? "" : pair.substring(eq + 1);
            }
        }
        return null;
    }
}
