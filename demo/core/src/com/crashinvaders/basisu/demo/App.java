package com.crashinvaders.basisu.demo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.crashinvaders.basisu.gdx.*;
import com.crashinvaders.basisu.wrapper.BasisuFileInfo;
import com.crashinvaders.basisu.wrapper.BasisuImageInfo;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuWrapper;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class App implements ApplicationListener {

    private final PlatformLauncher launcher;

    private ExtendViewport viewport;
    private SpriteBatch batch;

    private Texture texture0;
    private Texture texture1;
    private Stage stage;

    public App(PlatformLauncher launcher) {
        this.launcher = launcher;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        viewport = new ExtendViewport(800f, 480f);
        batch = new SpriteBatch();
        stage = new Stage(viewport, batch);

        {
            StringBuilder sb = new StringBuilder("Supported texture formats: [");
            int[] supportedTextureFormats = BasisuGdxGl.getSupportedTextureFormats();
            for (int i = 0; i < supportedTextureFormats.length; i++) {
                sb.append(Integer.toHexString(supportedTextureFormats[i]));
                if (i < supportedTextureFormats.length-1) {
                    sb.append(", ");
                }
            }sb.append("]");
            Gdx.app.log("App", sb.toString());
        }

        {
            BasisuNativeLibLoader.loadIfNeeded();
            byte[] basisuData = Gdx.files.internal("kodim3.basis").readBytes();
            boolean valid = BasisuWrapper.validateHeader(basisuData);
            Gdx.app.log("App", "Data is " + (valid ? "valid" : "invalid"));

            byte[] rgba = BasisuWrapper.transcode(basisuData, 0, 0, BasisuTranscoderTextureFormat.RGBA32);
            Gdx.app.log("App", "Transcoded size: " + rgba.length);
            Gdx.app.log("App", "Transcoded checksum: " + modRtuCrc(rgba));

            BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisuData, 0);
//            Gdx.app.log("App", "Image size: " + imageInfo.getWidth() + "x" + imageInfo.getHeight());
            Gdx.app.log("App", "getImageIndex() " + imageInfo.getImageIndex());
            Gdx.app.log("App", "getTotalLevels() " + imageInfo.getTotalLevels());
            Gdx.app.log("App", "getOrigWidth() " + imageInfo.getOrigWidth());
            Gdx.app.log("App", "getOrigHeight() " + imageInfo.getOrigHeight());
            Gdx.app.log("App", "getWidth() " + imageInfo.getWidth());
            Gdx.app.log("App", "getHeight() " + imageInfo.getHeight());
            Gdx.app.log("App", "getNumBlocksX() " + imageInfo.getNumBlocksX());
            Gdx.app.log("App", "getNumBlocksY() " + imageInfo.getNumBlocksY());
            Gdx.app.log("App", "getTotalBlocks() " + imageInfo.getTotalBlocks());
            Gdx.app.log("App", "getFirstSliceIndex() " + imageInfo.getFirstSliceIndex());
            Gdx.app.log("App", "hasAlphaFlag() " + imageInfo.hasAlphaFlag());
            Gdx.app.log("App", "hasIframeFlag() " + imageInfo.hasIframeFlag());
        }

        BasisuTextureData basisuData0 = new BasisuTextureData(Gdx.files.internal("kodim3.basis"));
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.RGBA32);
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.ETC1_RGB);
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.BC3_RGBA);
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.BC7_RGBA);
        texture0 = new Texture(basisuData0);

        BasisuTextureData basisuData1 = new BasisuTextureData(Gdx.files.internal("cosmocat_promo.basis"));
        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.RGBA32);
        texture1 = new Texture(basisuData1);

        // UI actors.
        {
            Table rootTable = new Table();
            rootTable.setFillParent(true);
            rootTable.center();

            rootTable.add(new Image(new TextureRegionDrawable(texture0), Scaling.fit)).growX();
            rootTable.add(new Image(new TextureRegionDrawable(texture1), Scaling.fit)).growX();

            stage.addActor(rootTable);
        }
    }

    @Override
    public void dispose() {
        texture0.dispose();
        texture1.dispose();
        batch.dispose();
        stage.dispose();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.4f, 0.0f, 0.6f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    // Compute the MODBUS RTU CRC
    // https://stackoverflow.com/a/38901367/3802890
    private static int modRtuCrc(byte[] buf) {
        int crc = 0xFFFF;
        int len = buf.length;

        for (int pos = 0; pos < len; pos++) {
            crc ^= (int) buf[pos] & 0xFF;   // XOR byte into least sig. byte of crc

            for (int i = 8; i != 0; i--) {    // Loop over each bit
                if ((crc & 0x0001) != 0) {      // If the LSB is set
                    crc >>= 1;                    // Shift right and XOR 0xA001
                    crc ^= 0xA001;
                } else                            // Else LSB is not set
                    crc >>= 1;                    // Just shift right
            }
        }
        // Note, this number has low and high bytes swapped, so use it accordingly (or swap bytes)
        return crc;
    }
}
