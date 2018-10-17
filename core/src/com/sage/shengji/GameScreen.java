package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import static com.sage.server.ServerCodes.INVALID_KITTY;
import static com.sage.server.ServerCodes.SEND_KITTY;

public class GameScreen extends InputAdapter implements Screen {
    private ScreenManager game;
    private GameState gameState;
    private ShengJiClient client;

    private ExtendViewport viewport;
    private OrthographicCamera camera;

    private SpriteBatch batch = new SpriteBatch();

    private Stage gameStage;
    private TextButton sendButton;

    private BitmapFont messageFont;

    GameScreen(ScreenManager game, GameState gameState, ShengJiClient client) {
        this.game = game;
        this.gameState = gameState;
        this.client = client;
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(ScreenManager.TABLE_WORLD_SIZE, ScreenManager.TABLE_WORLD_SIZE, camera);

        gameState.setViewport(viewport);

        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 15;

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        var textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = generator.generateFont(parameter);

        messageFont = generator.generateFont(parameter);

        generator.dispose();

        gameStage = new Stage(viewport, batch);
        sendButton = new TextButton("", textButtonStyle);

        gameStage.addActor(sendButton);

        var multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(gameState);
        multiplexer.addProcessor(gameStage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
        gameState.setViewport(viewport);
    }

    @Override
    public void render(float delta) {
        if(gameState.update(client)) {
            sendButton.setText(gameState.buttonText);
            sendButton.clearListeners();
            sendButton.addListener(gameState.buttonAction);
        }

        Gdx.gl.glClearColor(ScreenManager.BACKGROUND_COLOR.r, ScreenManager.BACKGROUND_COLOR.g, ScreenManager.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameStage.act(delta);
        gameStage.draw();

        batch.begin();

        messageFont.draw(batch, gameState.message, viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);

        batch.setProjectionMatrix(viewport.getCamera().combined);

        if(gameState.lastServerPrompt == INVALID_KITTY || gameState.lastServerPrompt == SEND_KITTY) {
            gameState.kitty.render(batch);
        }

        gameState.hand.render(batch, viewport);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
}
