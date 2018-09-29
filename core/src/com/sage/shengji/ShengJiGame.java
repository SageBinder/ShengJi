package com.sage.shengji;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.sage.server.ShengJiServer;

// TODO: Rendering part of player code
public class ShengJiGame extends Game {
    static final Color BACKGROUND_COLOR = new Color(0, 0.2f, 0.11f, 1);

    private ShengJiClient client;
    private ShengJiServer server;

    @Override
    public void create() {
        showPlaygroundScreen();
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

    void showLobbyScreen() {
        setScreen(new LobbyScreen(this));
    }

    void startGame(int port, String serverIP, String name) {
        GameState gameState = new GameState();
        this.client = new ShengJiClient(port, serverIP, name, this, gameState);
        client.start();

        setScreen(new GameScreen(this, gameState));
    }

    void openServer(int port, int numPlayers) {
        this.server = new ShengJiServer(port, numPlayers);
        server.start();
    }
}
