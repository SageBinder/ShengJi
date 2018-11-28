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
    static final int MAX_PLAYERS = 10;
    static final int MAX_NAME_LENGTH = 17;

    private ScreenManager game;
    private Viewport viewport;
    private float textProportion = 1f / 7f;
    private float viewportScale = 5f;

    private Stage stage;

    private Table table;
    private Label IPLabel;
    private TextField portField;
    private TextField numPlayersField;
    private TextField nameField;
    private Label errorLabel;
    private TextButton createGameButton;

    private FreeTypeFontGenerator generator;

    CreateGameScreen(ScreenManager game) {
        this.game = game;

        int textSize = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);

        float viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        float viewportWidth = Gdx.graphics.getWidth() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = textSize;
        parameter.hinting = FreeTypeFontGenerator.Hinting.Medium;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.incremental = true;

        BitmapFont font = generator.generateFont(parameter);
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        var textFieldStyle = skin.get(TextField.TextFieldStyle.class);
        textFieldStyle.font = font;

        var labelStyle = skin.get(Label.LabelStyle.class);
        labelStyle.font = font;
        labelStyle.font.getData().markupEnabled = true;

        var textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = font;

        IPLabel = new Label("Determining your IP...", labelStyle);
        IPLabel.setAlignment(Align.center);
        new Thread(() -> {
            try {
                String thisMachineIP =
                        new BufferedReader(
                                new InputStreamReader(
                                        new URL("https://api.ipify.org").openStream())).readLine();
                IPLabel.setText("Your IP: [CYAN]" + thisMachineIP);
            } catch(IOException e) {
                IPLabel.setText("[YELLOW]Error: could not determine your IP");
            }
        }).start();

        nameField = new TextField("", textFieldStyle);
        nameField.setMessageText("Enter name");
        nameField.setDisabled(false);

        portField = new TextField("", textFieldStyle);
        portField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        portField.setMessageText("Enter port");
        portField.setDisabled(false);

        numPlayersField = new TextField("", textFieldStyle);
        numPlayersField.setMessageText("# of players");
        numPlayersField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        numPlayersField.setDisabled(false);

        errorLabel = new Label("", labelStyle);
        errorLabel.setColor(new Color(1f, 0.2f, 0.2f, 1));

        createGameButton = new TextButton("Start game", textButtonStyle);
        createGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int port, numPlayers;
                String name = nameField.getText();

                if(name.length() > MAX_NAME_LENGTH) {
                    errorLabel.setText("Error: your name cannot be more than "
                            + MAX_NAME_LENGTH + " letters");
                    return;
                } else if(name.length() == 0) {
                    errorLabel.setText("Error: no name");
                    return;
                }

                try {
                    port = Integer.parseInt(portField.getText());
                    if(port < 1 || port > 65535) {
                        errorLabel.setText("Error: port number must be between 1 and 65535");
                        return;
                    } else if(port == 1023) { // 1023 is a reserved port
                        errorLabel.setText("Error: port 1023 is reserved");
                        return;
                    }
                } catch(NumberFormatException e) {
                    errorLabel.setText("Error: invalid port");
                    return;
                }

                try {
                    numPlayers = Integer.parseInt(numPlayersField.getText());
                    if(numPlayers > MAX_PLAYERS) {
                        errorLabel.setText("Error: cannot have more than " + MAX_PLAYERS + " players");
                        return;
                    } else if(numPlayers < 2) {
                        errorLabel.setText("Error: the max number of players must be greater than 1");
                        return;
                    }
                } catch(NumberFormatException e) {
                    errorLabel.setText("Error: invalid number of players");
                    return;
                }

                game.startGameServer(port, numPlayers);
                game.joinGame("127.0.0.1", port, name);
            }
        });

        table = new Table().top().padTop(viewportHeight * 0.2f);
        table.setFillParent(true);

        table.row().padBottom(viewportHeight / 30f);
        table.add(new Label("Create game", labelStyle)).align(Align.center).colspan(2);

        table.row();
        table.add(IPLabel).align(Align.center).colspan(2);

        table.row().padTop(viewportHeight / 35f).fillX();
        table.add(new Label("Enter name: ", labelStyle));
        table.add(nameField).prefWidth(viewportWidth * 0.3f);

        table.row().fillX().padTop(viewportHeight / 120f);
        table.add(new Label("Enter port: ", labelStyle));
        table.add(portField);

        table.row().padTop(viewportHeight / 120f).fillX();
        table.add(new Label("Enter max players: ", labelStyle));
        table.add(numPlayersField);

        table.row().padTop(viewportHeight / 120f).fillX();
        table.add(errorLabel).colspan(2);

        table.row().padTop(viewportHeight * 0.05f).fillX();
        table.add(createGameButton).colspan(2);

        stage = new Stage(viewport);
        stage.addActor(table);

        var inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
        inputMultiplexer.addProcessor(stage);
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
        generator.dispose();
    }
}
