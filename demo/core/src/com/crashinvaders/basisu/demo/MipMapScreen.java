package com.crashinvaders.basisu.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.crashinvaders.basisu.gdx.Ktx2Data;
import com.crashinvaders.basisu.gdx.Ktx2TextureData;

public class MipMapScreen implements Screen {
    private static final String TEXTURE_FILE = "subarking512.uastc.mipmap.ktx2";
    private static final float CUBE_SIZE = 1f;
    private static final float CORNER_TILT_DEG = 45f;
    private static final float SPIN_SPEED_DEG_PER_SEC = 30f;
    private static final float DRAG_ROTATE_DEG_PER_PIXEL = 0.5f;

    private final App app;
    private final SceneSelectorOverlay sceneSelectorOverlay;

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private Model cubeModel;
    private ModelInstance cubeInstance;
    private Texture cubeTexture;
    private int mipLevelCount;

    private float spinDeg;

    private Stage stage;
    private Skin skin;
    private Label mipLevelLabel;
    /** -1 means "let the GPU pick automatically", otherwise the forced mip level. */
    private int forcedMipLevel = -1;
    private boolean rotationEnabled = true;
    private boolean linearFilteringEnabled = false;
    private boolean manualDragging = false;

    /**
     * Handles manually spinning the cube by dragging with the left mouse button.
     * Registered as the lowest-priority input processor (added last), so it only ever
     * sees a touch that no UI actor (this screen's own stage, or the scene selector overlay) consumed first.
     */
    private final InputAdapter cubeDragInputProcessor = new InputAdapter() {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT) return false;
            manualDragging = true;
            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (!manualDragging) return false;
            spinDeg = (spinDeg + Gdx.input.getDeltaX() * DRAG_ROTATE_DEG_PER_PIXEL) % 360f;
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT) return false;
            manualDragging = false;
            return true;
        }
    };

    public MipMapScreen(App app) {
        this.app = app;
        this.sceneSelectorOverlay = new SceneSelectorOverlay(app);
    }

    @Override
    public void show() {
        camera = new PerspectiveCamera(35f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 0f, 3f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.35f, 0.35f, 0.35f, 1f));
        environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, -1f, -0.8f, -0.2f));

        Ktx2Data probeData = new Ktx2Data(Gdx.files.internal(TEXTURE_FILE));
        mipLevelCount = probeData.getTotalMipmapLevels();
        probeData.dispose();

        Ktx2TextureData textureData = new Ktx2TextureData(Gdx.files.internal(TEXTURE_FILE));
        cubeTexture = new Texture(textureData);
        applyTextureFilter();
        applyMipLevelClamp();

        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material(TextureAttribute.createDiffuse(cubeTexture));
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        cubeModel = modelBuilder.createBox(CUBE_SIZE, CUBE_SIZE, CUBE_SIZE, material, attributes);
        cubeInstance = new ModelInstance(cubeModel);

        setUpUi();

        app.getInputMultiplexer().addProcessor(stage);
        app.getInputMultiplexer().addProcessor(cubeDragInputProcessor);

        sceneSelectorOverlay.show();
    }

    private void setUpUi() {
        skin = buildMinimalSkin();

        stage = new Stage(new ScreenViewport());

        mipLevelLabel = new Label("", skin);
        updateMipLevelLabel();

        Slider mipLevelSlider = new Slider(-1, mipLevelCount - 1, 1, false, skin);
        mipLevelSlider.setValue(forcedMipLevel);
        mipLevelSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                forcedMipLevel = Math.round(((Slider) actor).getValue());
                applyMipLevelClamp();
                updateMipLevelLabel();
            }
        });

        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.bottom().pad(24f);
        uiTable.add(mipLevelLabel).padBottom(8f).row();
        uiTable.add(mipLevelSlider).width(320f);

        stage.addActor(uiTable);

        final TextButton rotateButton = new TextButton("Rotation", skin);
        rotateButton.pad(16f);
        rotateButton.setChecked(rotationEnabled);
        rotateButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rotationEnabled = rotateButton.isChecked();
            }
        });

        final TextButton linearFilteringButton = new TextButton("Linear Filtering", skin);
        linearFilteringButton.pad(16f);
        linearFilteringButton.setChecked(linearFilteringEnabled);
        linearFilteringButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                linearFilteringEnabled = linearFilteringButton.isChecked();
                applyTextureFilter();
            }
        });

        Table bottomLeftTable = new Table();
        bottomLeftTable.defaults().fillX();
        bottomLeftTable.setFillParent(true);
        bottomLeftTable.bottom().left().pad(8f);
        bottomLeftTable.add(rotateButton).left().row();
        bottomLeftTable.add(linearFilteringButton).left().padTop(8f);

        stage.addActor(bottomLeftTable);
    }

    private Skin buildMinimalSkin() {
        Skin skin = new Skin();

        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);
        skin.add("default", new Label.LabelStyle(font, Color.WHITE));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = skin.newDrawable("white", Color.DARK_GRAY);
        sliderStyle.background.setMinHeight(6f);
        sliderStyle.knob = skin.newDrawable("white", Color.WHITE);
        sliderStyle.knob.setMinWidth(16f);
        sliderStyle.knob.setMinHeight(16f);
        skin.add("default-horizontal", sliderStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = skin.newDrawable("white", new Color(0.25f, 0.25f, 0.25f, 0.9f));
        buttonStyle.over = skin.newDrawable("white", new Color(0.35f, 0.35f, 0.35f, 0.9f));
        buttonStyle.checked = skin.newDrawable("white", new Color(0.2f, 0.6f, 0.35f, 0.95f));
        buttonStyle.checkedOver = skin.newDrawable("white", new Color(0.25f, 0.7f, 0.42f, 0.95f));
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        return skin;
    }

    private void applyTextureFilter() {
        if (linearFilteringEnabled) {
            cubeTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        } else {
            // Nearest (no linear blending within/between levels) so the actual per-level texel quality stays visible.
            cubeTexture.setFilter(Texture.TextureFilter.MipMapNearestNearest, Texture.TextureFilter.Nearest);
        }
    }

    private void applyMipLevelClamp() {
        cubeTexture.bind();
        if (forcedMipLevel < 0) {
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL30.GL_TEXTURE_BASE_LEVEL, 0);
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAX_LEVEL, mipLevelCount - 1);
        } else {
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL30.GL_TEXTURE_BASE_LEVEL, forcedMipLevel);
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAX_LEVEL, forcedMipLevel);
        }
    }

    private void updateMipLevelLabel() {
        if (forcedMipLevel < 0) {
            mipLevelLabel.setText("Mip level: Auto (" + mipLevelCount + " levels)");
        } else {
            mipLevelLabel.setText("Mip level: " + forcedMipLevel + " / " + (mipLevelCount - 1));
        }
    }

    @Override
    public void pause() {
        sceneSelectorOverlay.pause();
    }

    @Override
    public void resume() {
        sceneSelectorOverlay.resume();
    }

    @Override
    public void hide() {
        app.getInputMultiplexer().removeProcessor(stage);
        app.getInputMultiplexer().removeProcessor(cubeDragInputProcessor);

        sceneSelectorOverlay.hide();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        cubeModel.dispose();
        cubeTexture.dispose();
        stage.dispose();
        skin.dispose();

        sceneSelectorOverlay.dispose();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        stage.getViewport().update(width, height, true);

        sceneSelectorOverlay.resize(width, height);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Matrix4#rotate() post-multiplies, so the call order is reversed from application order:
        // the first call here ends up as the outermost (world-space) rotation, applied last to each vertex.
        // Spinning around world Y first keeps the corner tilt as a fixed local pose underneath the spin.
        // Manual dragging (see cubeDragInputProcessor) always takes precedence over the automatic spin.
        if (rotationEnabled && !manualDragging) {
            spinDeg = (spinDeg + delta * SPIN_SPEED_DEG_PER_SEC) % 360f;
        }
        cubeInstance.transform
                .idt()
                .rotate(Vector3.Y, spinDeg)
                .rotate(Vector3.X, CORNER_TILT_DEG)
                .rotate(Vector3.Z, CORNER_TILT_DEG);

        modelBatch.begin(camera);
        modelBatch.render(cubeInstance, environment);
        modelBatch.end();

        stage.act(delta);
        stage.draw();

        sceneSelectorOverlay.render(delta);
    }
}
