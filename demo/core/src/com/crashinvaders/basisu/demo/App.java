package com.crashinvaders.basisu.demo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.crashinvaders.basisu.gdx.BasisuData;
import com.crashinvaders.basisu.gdx.BasisuGdxGl;
import com.crashinvaders.basisu.gdx.BasisuNativeLibLoader;
import com.crashinvaders.basisu.gdx.BasisuTextureData;
import com.crashinvaders.basisu.wrapper.BasisuFileInfo;
import com.crashinvaders.basisu.wrapper.BasisuImageInfo;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuWrapper;

import java.nio.ByteBuffer;

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

        testBasisuClasses();

        BasisuTextureData basisuData0 = new BasisuTextureData(Gdx.files.internal("kodim3.basis"));
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.RGBA32);
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.ETC1_RGB);
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.BC3_RGBA);
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.BC7_RGBA);
        texture0 = new Texture(basisuData0);

        BasisuTextureData basisuData1 = new BasisuTextureData(Gdx.files.internal("level_temple0.basis"));
        basisuData0.setTextureFormatSelector(BasisuTranscoderTextureFormat.RGBA32);
        texture1 = new Texture(basisuData1);

        // UI actors.
        {
            Table rootTable = new Table();
            rootTable.setFillParent(true);
            rootTable.center();

            Image image0 = new Image(new TextureRegionDrawable(texture0), Scaling.fit);
            image0.setScaling(Scaling.fit);
            rootTable.add(image0).grow();
            Image image1 = new Image(new TextureRegionDrawable(texture1), Scaling.fit);
            Container containerImage1 = new Container<>(image1);
            containerImage1.setTransform(true);
            containerImage1.addAction(Actions.delay(0.25f, Actions.forever(Actions.sequence(
                            Actions.run(() -> containerImage1.setOrigin(Align.center)),
                            Actions.rotateBy(360f, 1f, Interpolation.pow3),
                            Actions.delay(2f)
            ))));
            rootTable.add(containerImage1).grow();

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

    private static void testBasisuClasses() {
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
            ByteBuffer basisuData = BasisuData.readFileIntoBuffer(Gdx.files.internal("kodim3.basis"));
            boolean valid = BasisuWrapper.validateHeader(basisuData);
            Gdx.app.log("App", "Data is " + (valid ? "valid" : "invalid"));

            ByteBuffer rgba = BasisuWrapper.transcode(basisuData, 0, 0, BasisuTranscoderTextureFormat.RGBA32);
            Gdx.app.log("App", "Transcoded size: " + rgba.capacity());
            Gdx.app.log("App", "Transcoded checksum: " + modRtuCrc(rgba));

            BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisuData, 0);
            Gdx.app.log("App", "===== IMAGE INFO =====");
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
            imageInfo.close();

            BasisuFileInfo fileInfo = BasisuWrapper.getFileInfo(basisuData);
            Gdx.app.log("App", "===== FILE INFO =====");
            Gdx.app.log("App", "getVersion() " + fileInfo.getVersion());
            Gdx.app.log("App", "getTotalHeaderSize() " + fileInfo.getTotalHeaderSize());
            Gdx.app.log("App", "getTotalSelectors() " + fileInfo.getTotalSelectors());
            Gdx.app.log("App", "getSelectorCodebookSize() " + fileInfo.getSelectorCodebookSize());
            Gdx.app.log("App", "getTotalEndpoints() " + fileInfo.getTotalEndpoints());
            Gdx.app.log("App", "getEndpointCodebookSize() " + fileInfo.getEndpointCodebookSize());
            Gdx.app.log("App", "getTablesSize() " + fileInfo.getTablesSize());
            Gdx.app.log("App", "getSlicesSize() " + fileInfo.getSlicesSize());
            Gdx.app.log("App", "getUsPerFrame() " + fileInfo.getUsPerFrame());
            Gdx.app.log("App", "getTotalImages() " + fileInfo.getTotalImages());
            Gdx.app.log("App", "getUserdata0() " + fileInfo.getUserdata0());
            Gdx.app.log("App", "getUserdata1() " + fileInfo.getUserdata1());
            Gdx.app.log("App", "isFlippedY() " + fileInfo.isFlippedY());
            Gdx.app.log("App", "isEtc1s() " + fileInfo.isEtc1s());
            Gdx.app.log("App", "hasAlphaSlices() " + fileInfo.hasAlphaSlices());
            Gdx.app.log("App", "getImageMipmapLevels().length " + fileInfo.getImageMipmapLevels().length);
            Gdx.app.log("App", "getTextureType() " + fileInfo.getTextureType());
            Gdx.app.log("App", "getTextureFormat() " + fileInfo.getTextureFormat());
            fileInfo.close();
        }
    }

    // Compute the MODBUS RTU CRC
    // https://stackoverflow.com/a/38901367/3802890
    private static int modRtuCrc(ByteBuffer buf) {
        int crc = 0xFFFF;
        int len = buf.capacity();

        for (int pos = 0; pos < len; pos++) {
            crc ^= (int) buf.get(pos) & 0xFF;   // XOR byte into least sig. byte of crc

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
