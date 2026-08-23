package com.crashinvaders.basisu.demo.teavm;

import com.badlogic.gdx.ApplicationListener;
import com.crashinvaders.basisu.demo.App;
import com.crashinvaders.basisu.demo.AppParams;
import com.crashinvaders.basisu.demo.PlatformLauncher;
import com.crashinvaders.basisu.teavm.BasisuTeaVM;
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

        AppParams params = new AppParams();

        String screenParam = getUrlParameter("screen");
        if (screenParam != null) {
            try {
                params.screen = AppParams.Screen.valueOf(screenParam.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Unexpected \"screen\" URL parameter value: " + screenParam);
            }
        }

        // dispose() can NPE if preload finishes before the first render() builds its Stage - harmless to skip.
        ApplicationListener preloadListener = new WebPreloadApplicationListener() {
            @Override
            public void dispose() {
            }
        };

        // Required - BasisuNativeLibLoader throws if this is skipped. WebApplication is constructed
        // only once the Basis Universal glue script is actually ready.
        BasisuTeaVM.initialize(config, () ->
                new WebApplication(new App(new WebLauncher(), params), preloadListener, config));
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
