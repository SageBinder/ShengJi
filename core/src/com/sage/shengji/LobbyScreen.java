package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LobbyScreen extends InputAdapter implements Screen {
    private ScreenManager game;
    private GameState gameState;
    private ShengJiClient client;

    private Viewport viewport;

    private int maxNameChars = 38;

    private float textProportion = 1f / 7f,
            viewportScale = 5f;

    private Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

    InputMultiplexer multiplexer;

    private Stage stage;
    private Table table;

    private Table playersListTable;
    private Label.LabelStyle playerLabelStyle = skin.get(Label.LabelStyle.class);

    private TextButton startGameButton;
    private TextButton.TextButtonStyle startGameButtonStyle = skin.get(TextButton.TextButtonStyle.class);

    LobbyScreen(ScreenManager game, GameState gameState, ShengJiClient client) {
        this.game = game;
        this.gameState = gameState;
        this.client = client;

        float viewportWidth = Gdx.graphics.getWidth() * viewportScale,
                viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);

        var fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var playerLabelFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        playerLabelFontParameter.size = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);
        playerLabelFontParameter.hinting = FreeTypeFontGenerator.Hinting.Medium;
        playerLabelFontParameter.minFilter = Texture.TextureFilter.Linear;
        playerLabelFontParameter.magFilter = Texture.TextureFilter.Linear;

        playerLabelStyle.font = fontGenerator.generateFont(playerLabelFontParameter);
        startGameButtonStyle.font = fontGenerator.generateFont(playerLabelFontParameter);

        fontGenerator.dispose();

        startGameButton = new TextButton("Start game!", startGameButtonStyle);
        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                client.sendInt(ClientCodes.START_ROUND);
            }
        });
        startGameButton.setVisible(false);
        startGameButton.setDisabled(true);



        stage = new Stage();
        stage.setViewport(viewport);

        table = new Table();
        table.setFillParent(true);

        playersListTable = new Table();

//        table.setDebug(true);
        table.row();
        table.add(playersListTable).align(Align.center).maxWidth(viewportWidth / 2f);

        table.row().padTop(viewportHeight / 10f);
        table.add(startGameButton);

        stage.addActor(table);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
//        game.showGameScreen(gameState); // Default to showing gameScreen for now just to test gameScreen
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ScreenManager.BACKGROUND_COLOR.r, ScreenManager.BACKGROUND_COLOR.g, ScreenManager.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(gameState.update(client)) {
            float groupSpacing = viewport.getWorldWidth() / 24f;

            playersListTable.clearChildren();
            playersListTable.setFillParent(false);
            playersListTable.setWidth(viewport.getWorldWidth() / 20f);
            playersListTable.setDebug(false);

            playersListTable.defaults();

            playersListTable.row().padBottom(viewport.getWorldHeight() / 20f);
            playersListTable.add(new Label("P#", playerLabelStyle)).padRight(groupSpacing);
            playersListTable.add(new Label("NAME", playerLabelStyle));
            playersListTable.add(new Label("CALL RANK", playerLabelStyle)).padLeft(groupSpacing);

            gameState.players.forEach(p -> {
                var playerNumLabel = new Label("P" + p.getPlayerNum(), playerLabelStyle);
                var playerNameLabel = new Label(p.getName().substring(0, Math.min(p.getName().length(), maxNameChars)), playerLabelStyle);
                var callRankLabel = new Label(Integer.toString(p.getCallRank().rankNum), playerLabelStyle);

                if(p.getName().length() > maxNameChars) {
                    playerNameLabel.setText(playerNameLabel.getText() + "...");
                }

                playersListTable.row();
                playersListTable.add(playerNumLabel).padRight(groupSpacing);
                playersListTable.add(playerNameLabel);
                playersListTable.add(callRankLabel).padLeft(groupSpacing);

                if(p.isHost()) {
                    Color hostColor = new Color(1f, 1f, 0f, 1f);
                    playerNumLabel.setColor(hostColor);
                    playerNameLabel.setColor(hostColor);
                    callRankLabel.setColor(hostColor);
                }
                if(p.getPlayerNum() == gameState.thisPlayer.getPlayerNum()) {
                    playerNumLabel.getText().insert(0, "->");
                }
            });

            playersListTable.invalidate();

            if(gameState.thisPlayer.isHost()) {
                startGameButton.setVisible(true);
                startGameButton.setDisabled(false);
            } else {
                startGameButton.setVisible(false);
                startGameButton.setDisabled(true);
            }
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

        playersListTable.invalidate();
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

    }
}
