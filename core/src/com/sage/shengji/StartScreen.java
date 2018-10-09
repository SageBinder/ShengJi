package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

class StartScreen extends InputAdapter implements Screen {
    private static final float WORLD_SIZE = 100f;
    private static final float BUTTON_WIDTH = WORLD_SIZE * 0.3f;
    private static final float BUTTON_HEIGHT = WORLD_SIZE * 0.1f;

    private static final Vector2 CREATE_GAME_BUTTON_POS = new Vector2((WORLD_SIZE - BUTTON_WIDTH) * 0.5f,
            (WORLD_SIZE * (5f / 6f)) - (BUTTON_HEIGHT * 0.5f));
    private static final Vector2 JOIN_GAME_BUTTON_POS = new Vector2((WORLD_SIZE - BUTTON_WIDTH) * 0.5f,
            (WORLD_SIZE * (3f / 6f)) - (BUTTON_HEIGHT * 0.5f));
    private static final Vector2 OPTIONS_BUTTON_POS = new Vector2((WORLD_SIZE - BUTTON_WIDTH) * 0.5f,
            (WORLD_SIZE * (1f / 6f)) - (BUTTON_HEIGHT * 0.5f));
    
    private ShengJiGame game;

    private Viewport viewport;

    private Stage stage;
    private TextButton createButton, joinButton, optionsButton;

    StartScreen(ShengJiGame game) {
        this.game = game;

        stage = new Stage();
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.setViewport(viewport);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        createButton = new TextButton("Create game", skin);
        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showCreateGameScreen();
            }
        });
        joinButton = new TextButton("Join game", skin);
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showJoinGameScreen();
            }
        });
        optionsButton = new TextButton("Options", skin);
        optionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showOptionsScreen();
            }
        });

        stage.addActor(createButton);
        stage.addActor(joinButton);
        stage.addActor(optionsButton);

        updateButtons();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ShengJiGame.BACKGROUND_COLOR.r, ShengJiGame.BACKGROUND_COLOR.g, ShengJiGame.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateButtons();
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

    }

    private void updateButtons() {
        float heightScale = viewport.getWorldHeight() / WORLD_SIZE;
        float widthScale = viewport.getWorldWidth() / WORLD_SIZE;

        Vector2 createButtonPos = new Vector2(CREATE_GAME_BUTTON_POS.x * widthScale,
                CREATE_GAME_BUTTON_POS.y * heightScale);

        Vector2 joinButtonPos = new Vector2(JOIN_GAME_BUTTON_POS.x * widthScale,
                JOIN_GAME_BUTTON_POS.y * heightScale);

        Vector2 optionsButtonPos = new Vector2(OPTIONS_BUTTON_POS.x * widthScale,
                OPTIONS_BUTTON_POS.y * heightScale);

        float buttonHeight = BUTTON_HEIGHT * heightScale;
        float buttonWidth = BUTTON_WIDTH * widthScale;

        createButton.setPosition(createButtonPos.x, createButtonPos.y);
        createButton.setSize(buttonWidth, buttonHeight);

        joinButton.setPosition(joinButtonPos.x, joinButtonPos.y);
        joinButton.setSize(buttonWidth, buttonHeight);

        optionsButton.setPosition(optionsButtonPos.x, optionsButtonPos.y);
        optionsButton.setSize(buttonWidth, buttonHeight);
    }
}
