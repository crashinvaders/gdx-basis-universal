package com.crashinvaders.basisu.demo.ios;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.backends.iosrobovm.IOSAudio;
import com.crashinvaders.basisu.demo.App;
import com.crashinvaders.basisu.demo.PlatformLauncher;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

public class IOSLauncher extends IOSApplication.Delegate implements PlatformLauncher {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();

        return new IOSApplication(new App(this), config) {
            @Override
            protected IOSAudio createAudio(IOSApplicationConfiguration config) {
                // This helps to run the demo on a simulator inside
                // a virtualized macOS instance (with no audio support).
                return new MockIosAudio();
            }
        };
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }

}