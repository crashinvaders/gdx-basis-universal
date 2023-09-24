package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import org.junit.*;

public class BasisuGdxUtilsTest {

    private static TestAppListener appListener;
    private static HeadlessApplication application;

    private static class TestAppListener extends ApplicationAdapter {

    }

    @BeforeClass
    public static void init() {
        appListener = new TestAppListener();
        application = new HeadlessApplication(appListener);
    }

    @AfterClass
    public static void destroy() {
        Gdx.app.exit();
        appListener = null;
        application = null;
    }

    /**
     * Ensures that all the declared BasisuTranscoderTextureFormat values are mapped to
     * their OpenGL internal texture format representation codes.
     */
    @Test
    public void testToGlTextureFormat() {
        for (BasisuTranscoderTextureFormat value : BasisuTranscoderTextureFormat.values()) {
            BasisuGdxUtils.toGlTextureFormat(value);
        }
    }

    /**
     * Texture data load test.
     * For the sake of simplicity, we lock the transcoding to ETC2_RGBA.
     */
    @Test
    public void testTextureDataPrepare() {
        BasisuTextureData.defaultFormatSelector = (data, fileInfo, imageInfo) ->
                BasisuTranscoderTextureFormat.ETC2_RGBA;

        FileHandle textureFile = Gdx.files.classpath("kodim3.basis");
        BasisuTextureData textureData = new BasisuTextureData(textureFile, 0);
        textureData.prepare();
    }
}
