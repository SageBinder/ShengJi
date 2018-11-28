package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

class StartScreen extends InputAdapter implements Screen {
    private ScreenManager game;
    private Viewport viewport;
    private float textProportion = 1f / 7f;
    private float viewportScale = 5f;

    private Stage stage;

    private Table table;
    private TextButton createButton;
    private TextButton joinButton;
    private TextButton optionsButton;

    private FreeTypeFontGenerator generator;

    StartScreen(ScreenManager game) {
        this.game = game;

        int textSize = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);

        float viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        float viewportWidth = Gdx.graphics.getWidth() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var buttonFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        buttonFontParameter.size = textSize;
        buttonFontParameter.incremental = true;

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        var textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = generator.generateFont(buttonFontParameter);

        createButton = new TextButton("Create game", textButtonStyle);
        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showCreateGameScreen();
            }
        });

        joinButton = new TextButton("Join game", textButtonStyle);
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showJoinGameScreen();
            }
        });

        optionsButton = new TextButton("Options", textButtonStyle);
        optionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showOptionsScreen();
            }
        });

        table = new Table().top();
        table.setFillParent(true);

        table.row().padTop(viewportHeight * 0.1f);
        table.add(createButton).prefWidth(viewportWidth * 0.3f).prefHeight(viewportHeight * 0.1f);

        table.row().padTop(viewportHeight * 0.25f);
        table.add(joinButton).prefWidth(viewportWidth * 0.3f).prefHeight(viewportHeight * 0.1f);

        table.row().padTop(viewportHeight * 0.25f);
        table.add(optionsButton).prefWidth(viewportWidth * 0.3f).prefHeight(viewportHeight * 0.1f);

        stage = new Stage(viewport);
        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ScreenManager.BACKGROUND_COLOR.r, ScreenManager.BACKGROUND_COLOR.g, ScreenManager.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        table.invalidate();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        generator.dispose();
    }
}
