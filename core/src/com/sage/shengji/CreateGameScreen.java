package com.sage.shengji;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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

    private float textProportion = 1f / 7f;
    private float viewportScale = 5f;

    private ScreenManager game;
    private Stage stage;
    private Table table;
    private Viewport viewport;

    CreateGameScreen(ScreenManager game) {
        this.game = game;

        float viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        float viewportWidth = Gdx.graphics.getWidth() * viewportScale;

        int textSize = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);

        stage = new Stage();
        viewport = new ExtendViewport(viewportWidth, viewportHeight);
        stage.setViewport(viewport);

        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = textSize;
        parameter.hinting = FreeTypeFontGenerator.Hinting.Medium;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        TextField.TextFieldStyle textFieldStyle = skin.get(TextField.TextFieldStyle.class);
        textFieldStyle.font = font;

        Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);
        labelStyle.font = font;

        TextButton.TextButtonStyle textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = font;

        TextField portField = new TextField("", textFieldStyle);
        portField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        portField.setMessageText("Enter port");
        portField.setDisabled(false);

        TextField numPlayersField = new TextField("", textFieldStyle);
        numPlayersField.setMessageText("# of players");
        numPlayersField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        numPlayersField.setDisabled(false);

        TextField nameField = new TextField("", textFieldStyle);
        nameField.setMessageText("Enter name");
        nameField.setDisabled(false);

        Label errorLabel = new Label("", labelStyle);
        errorLabel.setColor(new Color(1f, 0.2f, 0.2f, 1));

        TextButton createGameButton = new TextButton("Start game", textButtonStyle);
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

                game.startGameServer(port, numPlayers);
                game.joinGame(port, "127.0.0.1", name);
            }
        });


        Label IPLabel = new Label("Determining your IP...", labelStyle);
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
//        table.setDebug(true);
        table.add(IPLabel).align(Align.center).colspan(2);
        ipGetterThread.start();

        table.row().fillX().padTop(viewportHeight / 35f);
//        HorizontalGroup portGroup = new HorizontalGroup();
//        Label portLabel = new Label("Port: ", labelStyle);
//        portLabel.setFillParent(true);
//        portGroup.addActor(portLabel);
//        portField.setFillParent(true);
//        portGroup.addActor(portField);
        table.add(new Label("Port: ", labelStyle));
        table.add(portField).prefWidth(viewportWidth / 5);

        table.row().padTop(viewportHeight / 120f).fillX();
        table.add(new Label("Number of players: ", labelStyle));
        table.add(numPlayersField);

        table.row().padTop(viewportHeight / 120f).fillX();
        table.add(new Label("Your name: ", labelStyle));
        table.add(nameField);

        table.row().padTop(viewportHeight / 120f).fillX();
        table.add(errorLabel).colspan(2);

        table.row().padTop(viewportHeight / 20f).fillX();
        table.add(createGameButton).colspan(2);

        table.top().padTop(viewportHeight / 5f);

        table.setFillParent(true);

        table.setWidth(viewportWidth / 2);

        stage.addActor(table);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(this);

        Gdx.input.setInputProcessor(inputMultiplexer);
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
