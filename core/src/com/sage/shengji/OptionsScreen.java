package com.sage.shengji;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

class OptionsScreen extends InputAdapter implements Screen {
    private ScreenManager game;
    private Viewport viewport;
    private float textProportion = 1f / 7f;
    private float viewportScale = 5f;

    private Stage stage;

    private Table table;

    private FreeTypeFontGenerator generator;

    OptionsScreen(ScreenManager game) {
        this.game = game;

        int textSize = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);

        float viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        float viewportWidth = Gdx.graphics.getWidth() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = textSize;
        fontParameter.incremental = true;

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        var labelStyle = skin.get(Label.LabelStyle.class);
        labelStyle.font = generator.generateFont(fontParameter);

        table = new Table().center();
        table.setFillParent(true);

        table.row().prefSize(viewportWidth * 0.33f, viewportHeight * 0.1f).fillX();
        table.add(new Label("No options yet :(\nPress esc to go back", labelStyle));

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
