package com.crashinvaders.basisu.demo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.crashinvaders.basisu.gdx.BasisuTextureData;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

public class App implements ApplicationListener {

    private final PlatformLauncher launcher;

    private ExtendViewport viewport;
    private SpriteBatch batch;

    private Texture texture0;
    private Texture texture1;

    public App(PlatformLauncher launcher) {
        this.launcher = launcher;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        viewport = new ExtendViewport(800f, 480f);
        batch = new SpriteBatch();

        BasisuTextureData basisuData0 = new BasisuTextureData(Gdx.files.internal("kodim3.basis"));
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.RGBA32);
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.ETC1_RGB);
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.BC3_RGBA);
//        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.BC7_RGBA);
        texture0 = new Texture(basisuData0);

        BasisuTextureData basisuData1 = new BasisuTextureData(Gdx.files.internal("cosmocat_promo.basis"));
        basisuData0.setFormatSelector(BasisuTranscoderTextureFormat.RGBA32);
        texture1 = new Texture(basisuData1);
    }

    @Override
    public void dispose() {
        texture0.dispose();
        texture1.dispose();
        batch.dispose();
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
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.2f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(texture0, 0f, 0f, viewport.getMinWorldWidth() * 0.5f, viewport.getMinWorldHeight() * 0.5f);
        batch.draw(texture1, viewport.getMinWorldWidth(), 0f, viewport.getMinWorldWidth() * 0.5f, viewport.getMinWorldHeight() * 0.5f);
        batch.end();
    }
}
