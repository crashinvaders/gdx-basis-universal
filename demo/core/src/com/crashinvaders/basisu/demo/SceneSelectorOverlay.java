package com.crashinvaders.basisu.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * A self-contained overlay meant to be hosted (in composition, not via {@link App#setScreen}) by every demo screen.
 * Shows a small "Screens" button that opens a modal popup for jumping to any of the other demo screens.
 * <p/>
 * Host screens must pass all {@link Screen} lifecycle calls through to this instance.
 */
public class SceneSelectorOverlay implements Screen {

    private static final ScreenEntry[] AVAILABLE_SCREENS = {
            new ScreenEntry("Texture Gallery", TextureGalleryScreen::new),
            new ScreenEntry("Mipmaps", MipMapScreen::new),
    };

    private final App app;
    private final int buttonAlign;

    private Stage stage;
    private Skin skin;

    public SceneSelectorOverlay(App app) {
        this(app, Align.topLeft);
    }

    /**
     * @param buttonAlign alignment of the "Screens" button within the screen, see {@link Align}
     */
    public SceneSelectorOverlay(App app, int buttonAlign) {
        this.app = app;
        this.buttonAlign = buttonAlign;
    }

    @Override
    public void show() {
        skin = buildMinimalSkin();
        stage = new Stage(new ExtendViewport(800f, 480f));

        TextButton screensButton = new TextButton("Demo Scenes", skin);
        screensButton.pad(16f);
        screensButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showScreenPickerDialog();
            }
        });

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.align(buttonAlign);
        rootTable.add(screensButton).pad(8f);
        stage.addActor(rootTable);

        // Inserted at the front so this overlay's modal popup always gets first crack at input,
        // regardless of what order the host screen registers its own stage in.
        app.getInputMultiplexer().addProcessor(0, stage);
    }

    private void showScreenPickerDialog() {
        // Purely visual: conveys that input is being taken over by the modal dialog.
        // (The dialog already blocks input everywhere on its own via Window's modal hit-test.)
        final Image scrim = new Image(skin.newDrawable("white", new Color(0f, 0f, 0f, 0.6f)));
        scrim.setFillParent(true);
        stage.addActor(scrim);

        final Dialog dialog = new Dialog("Demo Scenes", skin);
        dialog.setModal(true);
        dialog.setMovable(false);

        // Window reserves title-bar space from its own padTop (see Window's class javadoc), not from
        // the title table's preferred size, so this is what actually makes room for the header.
        dialog.padTop(44f);
        dialog.getTitleTable().pad(10f, 16f, 10f, 16f);
        dialog.getTitleTable().setBackground(skin.newDrawable("white", new Color(0.05f, 0.05f, 0.05f, 1f)));
        dialog.getTitleLabel().setAlignment(Align.left);

        for (final ScreenEntry entry : AVAILABLE_SCREENS) {
            TextButton button = new TextButton(entry.name, skin);
            button.getLabel().setAlignment(Align.left);
            button.padLeft(16f);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    dialog.hide(null);
                    scrim.remove();
                    // Defer the switch: this callback runs from inside the current screen's own
                    // render() (via this overlay's stage.draw()), and App#setScreen() disposes the
                    // current screen immediately, which would pull the rug from under this very frame.
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            app.setScreen(entry.factory.create(app));
                        }
                    });
                }
            });
            dialog.getContentTable().add(button).width(280f).height(40f).row();
        }

        TextButton closeButton = new TextButton("Close", skin);
        closeButton.pad(8f);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide(null);
                scrim.remove();
            }
        });
        dialog.getButtonTable().right();
        dialog.getButtonTable().add(closeButton).width(90f);

        // The no-action Dialog#show(Stage) overload does the centering itself, but only after running
        // its default fade-in action; since we skip that action here, center it explicitly instead.
        dialog.show(stage, null);
        dialog.setPosition(Math.round((stage.getWidth() - dialog.getWidth()) / 2f), Math.round((stage.getHeight() - dialog.getHeight()) / 2f));
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

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = skin.newDrawable("white", new Color(0.25f, 0.25f, 0.25f, 0.9f));
        buttonStyle.down = skin.newDrawable("white", new Color(0.45f, 0.45f, 0.45f, 0.9f));
        buttonStyle.over = skin.newDrawable("white", new Color(0.35f, 0.35f, 0.35f, 0.9f));
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.background = skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.95f));
        windowStyle.titleFont = font;
        windowStyle.titleFontColor = Color.WHITE;
        skin.add("default", windowStyle);

        return skin;
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        app.getInputMultiplexer().removeProcessor(stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    private interface ScreenFactory {
        Screen create(App app);
    }

    private static final class ScreenEntry {
        final String name;
        final ScreenFactory factory;

        ScreenEntry(String name, ScreenFactory factory) {
            this.name = name;
            this.factory = factory;
        }
    }
}
