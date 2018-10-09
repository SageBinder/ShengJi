package com.sage.shengji;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.sage.server.ShengJiServer;

// TODO: Rendering part of player code
public class ShengJiGame extends Game {
    static final Color BACKGROUND_COLOR = new Color(0, 0.2f, 0.11f, 1);
    static final float TABLE_WORLD_SIZE = 100f;

    private ShengJiClient client;
    private ShengJiServer server;

    private GameState gameState;

    @Override
    public void create() {
        startGameServer(1000, 1);
        joinGame(1000, "127.0.0.1", "Sponge");
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
        this.client = new ShengJiClient(port, serverIP, name, this, gameState);
        client.start();

        showLobbyScreen(gameState);
    }

    void startGameServer(int port, int numPlayers) {
        this.server = new ShengJiServer(port, numPlayers);
        server.start();
    }
}
