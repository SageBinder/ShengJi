package com.sage.shengji;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sage.server.ShengJiServer;

public class ScreenManager extends Game {
    static final Color BACKGROUND_COLOR = new Color(0, 0.2f, 0.11f, 1);

    private ShengJiClient client;
    private ShengJiServer server;

    private GameState gameState;

    @Override
    public void create() {
        Gdx.graphics.setTitle("ShengJi");

//        showCreateGameScreen();

//        showPlaygroundScreen();

        try {
            startGameServer(25565, 10);
            joinGame(25565, "127.0.0.1", "i");
        } catch(GdxRuntimeException e) {
            e.printStackTrace();
            joinGame(25565, "127.0.0.1", "i");
        }
    }

    void showStartScreen() {
        setScreen(new StartScreen(this));
    }

    void showPlaygroundScreen() {
        setScreen(new PlaygroundScreen(this));
    }

    void showCreateGameScreen() {
        setScreen(new CreateGameScreen(this));
    }

    void showJoinGameScreen() {
        setScreen(new JoinGameScreen(this));
    }

    void showOptionsScreen() {
        setScreen(new JoinGameScreen(this));
    }

    void showLobbyScreen(GameState gameState) {
        setScreen(new LobbyScreen(this, gameState, client));
    }

    void showGameScreen(GameState gameState) {
        setScreen(new GameScreen(this, gameState, client));
    }

    void joinGame(int port, String serverIP, String name) {
        gameState = new GameState(this);
        this.client = new ShengJiClient(port, serverIP, name, this);
        client.start();

        showLobbyScreen(gameState);
    }

    void startGameServer(int port, int numPlayers) {
        this.server = new ShengJiServer(port, numPlayers);
        server.start();
    }

    @Override
    public void dispose() {
        if(client != null) {
            client.quit();
        }
    }
}
