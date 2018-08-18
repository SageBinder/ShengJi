package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class StartScreen extends InputAdapter implements Screen {
    private ShengJiGame game;

    private ExtendViewport viewport;

    private SpriteBatch batch;
    private ShapeRenderer renderer;
    private BitmapFont font;

    private Vector2 createButtonPos, joinButtonPos, optionsButtonPos;
    private float buttonHeight, buttonWidth;

    StartScreen(ShengJiGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        viewport = new ExtendViewport(Constants.START_WORLD_SIZE, Constants.START_WORLD_SIZE);
        batch = new SpriteBatch();
        renderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.setUseIntegerPositions(false);

        updateButtons();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Constants.BACKGROUND_COLOR.r, Constants.BACKGROUND_COLOR.g, Constants.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        renderer.setProjectionMatrix(viewport.getCamera().combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Constants.START_WORLD_BUTTON_COLOR);
        renderer.rect(createButtonPos.x, createButtonPos.y, buttonWidth, buttonHeight);
        renderer.rect(joinButtonPos.x, joinButtonPos.y, buttonWidth, buttonHeight);
        renderer.rect(optionsButtonPos.x, optionsButtonPos.y, buttonWidth, buttonHeight);
        renderer.end();

        var createButtonLayout = new GlyphLayout(font, "Create game", Color.BLACK, 0, Align.center, false);
        float createButtonLayoutHeight = createButtonLayout.height;

        var joinButtonLayout = new GlyphLayout(font, "Join game", Color.BLACK, 0, Align.center, false);
        float joinButtonLayoutHeight = joinButtonLayout.height;

        var optionsButtonLayout = new GlyphLayout(font, "Options", Color.BLACK, 0, Align.center, false);
        float optionsButtonLayoutHeight = optionsButtonLayout.height;

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.draw(batch,
                createButtonLayout,
                createButtonPos.x + (buttonWidth / 2),
                (createButtonPos.y + (buttonHeight / 2)) + (createButtonLayoutHeight / 2));
        font.draw(batch,
                joinButtonLayout,
                joinButtonPos.x + (buttonWidth / 2),
                (joinButtonPos.y + (buttonHeight / 2)) + (joinButtonLayoutHeight / 2));
        font.draw(batch,
                optionsButtonLayout,
                optionsButtonPos.x + (buttonWidth / 2),
                (optionsButtonPos.y + (buttonHeight / 2)) + (optionsButtonLayoutHeight / 2));
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateButtons();
        font.getData().setScale(Constants.START_FONT_REFERENCE_SCREEN_SIZE);
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

    private void updateButtons() {
        createButtonPos = new Vector2(Constants.CREATE_GAME_BUTTON_POS.x * (viewport.getWorldWidth() / Constants.START_WORLD_SIZE),
                Constants.CREATE_GAME_BUTTON_POS.y * (viewport.getWorldHeight() / Constants.START_WORLD_SIZE));

        joinButtonPos = new Vector2(Constants.JOIN_GAME_BUTTON_POS.x * (viewport.getWorldWidth() / Constants.START_WORLD_SIZE),
                Constants.JOIN_GAME_BUTTON_POS.y * (viewport.getWorldHeight() / Constants.START_WORLD_SIZE));

        optionsButtonPos = new Vector2(Constants.OPTIONS_BUTTON_POS.x * (viewport.getWorldWidth() / Constants.START_WORLD_SIZE),
                Constants.OPTIONS_BUTTON_POS.y * (viewport.getWorldHeight() / Constants.START_WORLD_SIZE));

        buttonHeight = Constants.START_WORLD_BUTTON_HEIGHT * (viewport.getWorldHeight() / Constants.START_WORLD_SIZE);
        buttonWidth = Constants.START_WORLD_BUTTON_WIDTH * (viewport.getWorldWidth() / Constants.START_WORLD_SIZE);
    }
}
