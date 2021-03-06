package com.sage.shengji;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.sage.Rank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class LobbyScreen extends InputAdapter implements Screen {
    private ScreenManager game;
    private GameState gameState;
    private ShengJiClient client;
    private Viewport viewport;

    private int maxNameChars = 38;

    private float textProportion = 1f / 7f,
            viewportScale = 5f;

    private Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

    private InputMultiplexer multiplexer;

    private Stage stage;
    private Table table;

    private Label gameIPLabel;
    private Label.LabelStyle gameIPLabelStyle = skin.get(Label.LabelStyle.class);

    private Table playersListTable;
    private Label.LabelStyle playerLabelStyle = skin.get(Label.LabelStyle.class);

    private TextButton startGameButton;
    private TextButton.TextButtonStyle startGameButtonStyle = skin.get(TextButton.TextButtonStyle.class);

    private Label messageLabel;
    private Label.LabelStyle messageLabelStyle = skin.get(Label.LabelStyle.class);

    private FreeTypeFontGenerator fontGenerator;

    LobbyScreen(ScreenManager game, GameState gameState, ShengJiClient client) {
        this.game = game;
        this.gameState = gameState;
        this.client = client;

        float viewportWidth = Gdx.graphics.getWidth() * viewportScale,
                viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var playerLabelFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        playerLabelFontParameter.size = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);
        playerLabelFontParameter.hinting = FreeTypeFontGenerator.Hinting.Medium;
        playerLabelFontParameter.minFilter = Texture.TextureFilter.Linear;
        playerLabelFontParameter.magFilter = Texture.TextureFilter.Linear;
        playerLabelFontParameter.incremental = true;

        BitmapFont font = fontGenerator.generateFont(playerLabelFontParameter);
        font.getData().markupEnabled = true;
        gameIPLabelStyle.font = font;
        playerLabelStyle.font = font;
        startGameButtonStyle.font = font;
        messageLabelStyle.font = font;

        if(client.serverIP.equals("127.0.0.1")) {
            gameIPLabel = new Label("Fetching game IP...", gameIPLabelStyle);
            new Thread(() -> {
                try {
                    String thisMachineIP =
                            new BufferedReader(
                                    new InputStreamReader(
                                            new URL("https://api.ipify.org").openStream())).readLine();
                    gameIPLabel.setText("Connected to [CYAN]" + thisMachineIP + "[]:[ORANGE]" + client.port);
                } catch(IOException e) {
                    gameIPLabel.setText("Error: could not determine game IP");
                }
            }).start();
        } else {
            gameIPLabel = new Label("Connected to [CYAN]" + client.serverIP + "[]:[ORANGE]" + client.port, gameIPLabelStyle);
        }

        playersListTable = new Table();

        startGameButton = new TextButton("Start game!", startGameButtonStyle);
        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                client.sendInt(ClientCodes.START_ROUND);
            }
        });
        startGameButton.setVisible(false);
        startGameButton.setDisabled(true);

        messageLabel = new Label("", messageLabelStyle);

        table = new Table();
        table.setFillParent(true);

        table.row();
        table.add(gameIPLabel).padBottom(viewportHeight * 0.1f);

        table.row();
        table.add(playersListTable).align(Align.center).maxWidth(viewportWidth / 2f);

        table.row().padTop(viewportHeight * 0.1f);
        table.add(startGameButton);

        table.row().padTop(viewportHeight * 0.05f);
        table.add(messageLabel).padBottom(viewportHeight * 0.01f);

        stage = new Stage(viewport);
        stage.addActor(table);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        updateUIFromGameState();
    }

    @Override
    public void show() {
        if(gameState.roundStarted) {
            game.showGameScreen(gameState);
        }

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ScreenManager.BACKGROUND_COLOR.r, ScreenManager.BACKGROUND_COLOR.g, ScreenManager.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        messageLabel.setText(gameState.message);
        if(gameState.update(client)) {
            updateUIFromGameState();
        }

        stage.act(delta);
        stage.draw();
    }

    private void updateUIFromGameState() {
        float groupSpacing = viewport.getWorldWidth() / 12f;

        var pNumHeaderLabel = new Label("P#", playerLabelStyle);
        var pNameHeaderLabel = new Label("NAME", playerLabelStyle);
        var pCallRankHeaderLabel = new Label("CALL RANK", playerLabelStyle);
        pNameHeaderLabel.setAlignment(Align.center);

        playersListTable.clearChildren();
        playersListTable.setFillParent(false);
        playersListTable.setWidth(viewport.getWorldWidth() / 20f);
        playersListTable.defaults();

        playersListTable.row().padBottom(viewport.getWorldHeight() / 20f);
        playersListTable.add(pNumHeaderLabel).padRight(groupSpacing);
        playersListTable.add(pNameHeaderLabel);
        playersListTable.add(pCallRankHeaderLabel).padLeft(groupSpacing);

        gameState.players.forEach(p -> {
            var playerNumLabel = new Label("P" + p.getPlayerNum(), playerLabelStyle);
            var playerNameLabel = new Label(p.getName(maxNameChars), playerLabelStyle);
            var callRankLabel = new Label(p.getCallRank().toString(), playerLabelStyle);

            var increaseCallRankButton = new TextButton("+", startGameButtonStyle);
            var decreaseCallRankButton = new TextButton("-", startGameButtonStyle);
            increaseCallRankButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Rank nextRank = Rank.nextRank(p.getCallRank().rankNum);
                    if(nextRank == Rank.JOKER) {
                        nextRank = Rank.TWO;
                    }
                    p.setCallRank(nextRank.rankNum);
                    client.sendString(ClientCodes.WAIT_FOR_NEW_CALLING_RANK
                            + "\n" + p.getPlayerNum()
                            + "\n" + nextRank.rankNum);
                    updateUIFromGameState();
                }
            });
            decreaseCallRankButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Rank previousRank = Rank.previousRank(p.getCallRank().rankNum);
                    if(previousRank == Rank.JOKER) {
                        previousRank = Rank.ACE;
                    }
                    p.setCallRank(previousRank.rankNum);
                    client.sendString(ClientCodes.WAIT_FOR_NEW_CALLING_RANK
                            + "\n" + p.getPlayerNum()
                            + "\n" + previousRank.rankNum);
                    updateUIFromGameState();
                }
            });

            if(p.getName().length() > maxNameChars) {
                playerNameLabel.setText(playerNameLabel.getText() + "...");
            }

            playersListTable.row().padBottom(viewport.getWorldHeight() * 0.01f);
            playersListTable.add(playerNumLabel).padRight(groupSpacing);
            playersListTable.add(playerNameLabel);
            playersListTable.add(callRankLabel).padLeft(groupSpacing);
            if(gameState.thisPlayer != null && gameState.thisPlayer.isHost()) {
                playersListTable.add(decreaseCallRankButton)
                        .padLeft(viewport.getWorldWidth() * 0.05f)
                        .minWidth(viewport.getWorldWidth() * 0.05f);
                playersListTable.add(increaseCallRankButton)
                        .padLeft(viewport.getWorldWidth() * 0.05f)
                        .minWidth(viewport.getWorldWidth() * 0.05f);
            }

            if(p.isHost()) {
                Color hostColor = new Color(1f, 1f, 0f, 1f);
                playerNumLabel.setColor(hostColor);
                playerNameLabel.setColor(hostColor);
                callRankLabel.setColor(hostColor);
            }
            if(gameState.thisPlayer != null && p.getPlayerNum() == gameState.thisPlayer.getPlayerNum()) {
                playerNumLabel.getText().insert(0, "->");
            }
        });

        playersListTable.invalidate();

        if(gameState.thisPlayer != null && gameState.thisPlayer.isHost()) {
            startGameButton.setVisible(true);
            startGameButton.setDisabled(false);
        } else {
            startGameButton.setVisible(false);
            startGameButton.setDisabled(true);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

        playersListTable.invalidate();
        table.invalidate();
        updateUIFromGameState();
    }

    @Override
    public boolean keyDown(int keyCode) {
        if(keyCode == Input.Keys.ESCAPE) {
            client.quit();
            game.showStartScreen();
        }
        return true;
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
        fontGenerator.dispose();
    }
}
