package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LobbyScreen implements Screen {
    private ShengJiGame game;

    private Viewport viewport;
    private Stage stage;
    private Table table;
    private List<String> playersList;

    LobbyScreen(ShengJiGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage();
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.setViewport(viewport);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        playersList = new List<>(skin);



        table = new Table();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ShengJiGame.BACKGROUND_COLOR.r, ShengJiGame.BACKGROUND_COLOR.g, ShengJiGame.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void resize(int width, int height) {

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
