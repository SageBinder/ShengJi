package com.sage.shengji;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

class CreateGameScreen extends InputAdapter implements Screen {
    private static int MAX_PLAYERS = 10;
    private static int MAX_NAME_LENGTH = 24;

    private ShengJiGame game;
    private Stage stage;
    private Table table;
    private Viewport viewport;

    CreateGameScreen(ShengJiGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage();
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.setViewport(viewport);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        TextField portField = new TextField("", skin);
        portField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        portField.setMessageText("Enter port");
        portField.setDisabled(false);

        TextField numPlayersField = new TextField("", skin);
        numPlayersField.setMessageText("# of players");
        numPlayersField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        numPlayersField.setDisabled(false);

        TextField nameField = new TextField("", skin);
        nameField.setMessageText("Enter name");
        nameField.setDisabled(false);

        Label errorLabel = new Label("", skin);
        errorLabel.setColor(new Color(1f, 0.2f, 0.2f, 1));

        TextButton createGameButton = new TextButton("Start game", skin);
        createGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int port, numPlayers;
                String name = nameField.getText();

                try {
                    port = Integer.parseInt(portField.getText());
                    if(port > 65535) {
                        errorLabel.setText("Error: Port number too large");
                        return;
                    } else if(port < 1) {
                        errorLabel.setText("Error: Port number cannot be negative or zero");
                        return;
                    }
                } catch(NumberFormatException e) {
                    errorLabel.setText("Error: Invalid port");
                    return;
                }

                try {
                    numPlayers = Integer.parseInt(numPlayersField.getText());
                    if(numPlayers > MAX_PLAYERS) {
                        errorLabel.setText("Error: Too many players");
                        return;
                    } else if(numPlayers < 2) {
                        errorLabel.setText("Error: The number of players must be greater than 1");
                        return;
                    }
                } catch(NumberFormatException e) {
                    errorLabel.setText("Error: Invalid number of players");
                    return;
                }

                if(name.length() > MAX_NAME_LENGTH) {
                    errorLabel.setText("Error: Name too long");
                    return;
                } else if(name.length() == 0) {
                    errorLabel.setText("Error: No name");
                    return;
                }

                game.openServer(port, numPlayers);
                game.startGame(port, "127.0.0.1", name);

                game.showLobbyScreen();
            }
        });


        Label IPLabel = new Label("Determining your IP...", skin);
        IPLabel.setAlignment(Align.center);
        Thread ipGetterThread = new Thread(()->{
            try {
                String thisMachineIP = new BufferedReader(new InputStreamReader(new URL("https://api.ipify.org").openStream())).readLine();
                IPLabel.setText("Your IP: " + thisMachineIP);
            } catch(IOException e) {
                IPLabel.setText("Error: could not determine your IP");
            }
        });

        table = new Table();
        table.setFillParent(true);
//        table.setDebug(true);

        table.add(IPLabel).fillX().padBottom(30).align(Align.center).colspan(2);
        ipGetterThread.start();

        table.row().fill();
        table.add(new Label("Enter port: ", skin));
        table.add(portField);

        table.row().padTop(10).fill();
        table.add(new Label("Enter number of players: ", skin));
        table.add(numPlayersField);

        table.row().padTop(10).fill();
        table.add(new Label("Enter name: ", skin));
        table.add(nameField);

        table.row().padTop(10).fill();
        table.add(errorLabel).colspan(2);

        table.row().pad(70, 20, 20, 20).fill();
        table.add(createGameButton).colspan(2);

        table.top().padTop(60);

        stage.addActor(table);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(this);

        Gdx.input.setInputProcessor(inputMultiplexer);
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
        table.setFillParent(true);
        table.invalidate();
    }

    @Override
    public boolean keyDown(int keyCode) {
        if(keyCode == Input.Keys.ESCAPE) {
            game.showStartScreen();
            return true;
        }
        return false;
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
