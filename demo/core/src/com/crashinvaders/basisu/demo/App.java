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
import com.crashinvaders.basisu.gdx.BasisuTextureData;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

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
}
