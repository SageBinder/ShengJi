package com.sage.shengji;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

class JoinGameScreen extends InputAdapter implements Screen  {
    private ScreenManager game;
    private Viewport viewport;
    private float textProportion = 1f / 7f;
    private float viewportScale = 5f;

    private Stage stage;

    private Table table;
    private TextField nameField;
    private TextField ipField;
    private TextField portField;
    private Label errorLabel;
    private TextButton joinButton;

    private FreeTypeFontGenerator generator;

    JoinGameScreen(ScreenManager game) {
        this.game = game;

        int textSize = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);

        float viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        float viewportWidth = Gdx.graphics.getWidth() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var textFieldParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        textFieldParameter.size = textSize;
        textFieldParameter.incremental = true;

        BitmapFont font = generator.generateFont(textFieldParameter);
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        font.getData().markupEnabled = true;

        var textFieldStyle = skin.get(TextField.TextFieldStyle.class);
        textFieldStyle.font = font;

        var textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = font;

        var labelStyle = skin.get(Label.LabelStyle.class);
        labelStyle.font = font;

        nameField = new TextField("", textFieldStyle);
        nameField.setMessageText("Enter name");
        nameField.setDisabled(false);

        ipField = new TextField("", textFieldStyle);
        ipField.setMessageText("Enter IP");
        ipField.setTextFieldFilter((textField, c) -> Character.isDigit(c) || c == '.');
        ipField.setDisabled(false);

        portField = new TextField("", textFieldStyle);
        portField.setMessageText("Enter port");
        portField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        portField.setDisabled(false);

        errorLabel = new Label("", labelStyle);
        errorLabel.setColor(new Color(1f, 0.2f, 0.2f, 1));

        joinButton = new TextButton("Join game", textButtonStyle);
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String name = nameField.getText();
                String ip = ipField.getText();
                int port;
                try {
                    port = Integer.parseInt(portField.getText());
                } catch(NumberFormatException e) {
                    errorLabel.setText("Error: port number must be between 1 and 65535");
                    return;
                }

                if(name.length() > CreateGameScreen.MAX_NAME_LENGTH) {
                    errorLabel.setText("Error: your name cannot be more than "
                            + CreateGameScreen.MAX_NAME_LENGTH + " letters");
                    return;
                } else if(name.length() == 0) {
                    errorLabel.setText("Error: no name");
                    return;
                }

                if(!ip.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
                    errorLabel.setText("Error: invalid IP");
                    return;
                }

                // 65535 is the max port number
                if(port < 0 || port > 65535) {
                    errorLabel.setText("Error: port number must be between 1 and 65535");
                    return;
                }

                try {
                    game.joinGame(ipField.getText(), port, nameField.getText());
                } catch(GdxRuntimeException e) {
                    errorLabel.setText("Error: couldn't connect to host");
                }
                // game.joinGame() will switch screen to LobbyScreen if it successfully connects to the host
            }
        });

        table = new Table().top().padTop(viewportHeight * 0.2f);
        table.defaults().padBottom(viewportHeight / 120f);
        table.setFillParent(true);

        table.row().padBottom(viewportHeight * 0.1f);
        table.add(new Label("Join game", labelStyle)).align(Align.center).colspan(2);

        table.row().fillX();
        table.add(new Label("Enter name: ", labelStyle)).prefWidth(viewportWidth * 0.33f);
        table.add(nameField).prefWidth(viewportWidth * 0.3f);

        table.row().fillX();
        table.add(new Label("Enter IP: ", labelStyle));
        table.add(ipField);

        table.row().fillX();
        table.add(new Label("Enter port:", labelStyle));
        table.add(portField);

        table.row().fillX();
        table.add(errorLabel).colspan(2);

        table.row().padTop(viewportHeight * 0.05f).fillX();
        table.add(joinButton).colspan(2);

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
        generator.dispose();
    }
}
