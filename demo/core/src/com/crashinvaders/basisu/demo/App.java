package com.crashinvaders.basisu.demo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.files.FileHandle;
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
import com.crashinvaders.basisu.gdx.*;
import com.crashinvaders.basisu.wrapper.BasisuFileInfo;
import com.crashinvaders.basisu.wrapper.BasisuImageInfo;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuWrapper;

import java.nio.ByteBuffer;

public class App implements ApplicationListener {
    private static final String TAG = App.class.getSimpleName();

    private final PlatformLauncher launcher;

    private ExtendViewport viewport;
    private SpriteBatch batch;

    private Texture texture0;
    private Stage stage;

    private AssetManager assetManager;

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

        assetManager = new AssetManager();
        assetManager.setLoader(Texture.class, ".basis", new BasisuTextureLoader(assetManager.getFileHandleResolver()));
        assetManager.load("level_temple0.basis", Texture.class);
        assetManager.finishLoading();

        BasisuTextureData basisuData0 = new BasisuTextureData(Gdx.files.internal("kodim3.basis"));
        texture0 = new Texture(basisuData0);

        // UI actors.
        {
            Table rootTable = new Table();
            rootTable.setFillParent(true);
            rootTable.center();

            Image image0 = new Image(new TextureRegionDrawable(texture0), Scaling.fit);
            image0.setScaling(Scaling.fit);
            rootTable.add(image0).grow();
            Image image1 = new Image(new TextureRegionDrawable(assetManager.get("level_temple0.basis", Texture.class)), Scaling.fit);
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
        assetManager.dispose();
        texture0.dispose();
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
            Gdx.app.log(TAG, sb.toString());
        }

        {
            BasisuNativeLibLoader.loadIfNeeded();
            FileHandle file = Gdx.files.internal("kodim3.basis");
            Gdx.app.log(TAG, "Reading Basis file: " + file.name());
            ByteBuffer basisuData = BasisuData.readFileIntoBuffer(file);
            boolean valid = BasisuWrapper.validateHeader(basisuData);
            Gdx.app.log(TAG, "Data is " + (valid ? "valid" : "invalid"));

            ByteBuffer rgba = BasisuWrapper.transcode(basisuData, 0, 0, BasisuTranscoderTextureFormat.RGBA32);
            Gdx.app.log(TAG, "Transcoded size: " + rgba.capacity());
            Gdx.app.log(TAG, "Transcoded checksum: " + modRtuCrc(rgba));

            BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisuData, 0);
            Gdx.app.log(TAG, "===== IMAGE INFO =====");
            Gdx.app.log(TAG, "getImageIndex() " + imageInfo.getImageIndex());
            Gdx.app.log(TAG, "getTotalLevels() " + imageInfo.getTotalLevels());
            Gdx.app.log(TAG, "getOrigWidth() " + imageInfo.getOrigWidth());
            Gdx.app.log(TAG, "getOrigHeight() " + imageInfo.getOrigHeight());
            Gdx.app.log(TAG, "getWidth() " + imageInfo.getWidth());
            Gdx.app.log(TAG, "getHeight() " + imageInfo.getHeight());
            Gdx.app.log(TAG, "getNumBlocksX() " + imageInfo.getNumBlocksX());
            Gdx.app.log(TAG, "getNumBlocksY() " + imageInfo.getNumBlocksY());
            Gdx.app.log(TAG, "getTotalBlocks() " + imageInfo.getTotalBlocks());
            Gdx.app.log(TAG, "getFirstSliceIndex() " + imageInfo.getFirstSliceIndex());
            Gdx.app.log(TAG, "hasAlphaFlag() " + imageInfo.hasAlphaFlag());
            Gdx.app.log(TAG, "hasIframeFlag() " + imageInfo.hasIframeFlag());
            imageInfo.close();

            BasisuFileInfo fileInfo = BasisuWrapper.getFileInfo(basisuData);
            Gdx.app.log(TAG, "===== FILE INFO =====");
            Gdx.app.log(TAG, "getVersion() " + fileInfo.getVersion());
            Gdx.app.log(TAG, "getTotalHeaderSize() " + fileInfo.getTotalHeaderSize());
            Gdx.app.log(TAG, "getTotalSelectors() " + fileInfo.getTotalSelectors());
            Gdx.app.log(TAG, "getSelectorCodebookSize() " + fileInfo.getSelectorCodebookSize());
            Gdx.app.log(TAG, "getTotalEndpoints() " + fileInfo.getTotalEndpoints());
            Gdx.app.log(TAG, "getEndpointCodebookSize() " + fileInfo.getEndpointCodebookSize());
            Gdx.app.log(TAG, "getTablesSize() " + fileInfo.getTablesSize());
            Gdx.app.log(TAG, "getSlicesSize() " + fileInfo.getSlicesSize());
            Gdx.app.log(TAG, "getUsPerFrame() " + fileInfo.getUsPerFrame());
            Gdx.app.log(TAG, "getTotalImages() " + fileInfo.getTotalImages());
            Gdx.app.log(TAG, "getUserdata0() " + fileInfo.getUserdata0());
            Gdx.app.log(TAG, "getUserdata1() " + fileInfo.getUserdata1());
            Gdx.app.log(TAG, "isFlippedY() " + fileInfo.isFlippedY());
            Gdx.app.log(TAG, "isEtc1s() " + fileInfo.isEtc1s());
            Gdx.app.log(TAG, "hasAlphaSlices() " + fileInfo.hasAlphaSlices());
            Gdx.app.log(TAG, "getImageMipmapLevels().length " + fileInfo.getImageMipmapLevels().length);
            Gdx.app.log(TAG, "getTextureType() " + fileInfo.getTextureType());
            Gdx.app.log(TAG, "getTextureFormat() " + fileInfo.getTextureFormat());
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
