package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;

class TableScreen extends InputAdapter implements Screen {
    static final float GAME_WORLD_SIZE = 5f;

    private ShengJiGame game;

	private SpriteBatch batch;
	private ShapeRenderer renderer;
	private ExtendViewport viewport;
	private OrthographicCamera camera;

	private RenderableHand hand;
	private ArrayList<RenderableCard> cards = new ArrayList<>();

	TableScreen(ShengJiGame game) {
	    this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        renderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(GAME_WORLD_SIZE, GAME_WORLD_SIZE, camera);
        hand  = new RenderableHand(viewport);

        for(int i = 0; i < 52; i++) {
            hand.add(new RenderableCard());
        }

        camera.position.set(camera.viewportWidth/2,camera.viewportHeight/2,0);
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ShengJiGame.BACKGROUND_COLOR.r, ShengJiGame.BACKGROUND_COLOR.g, ShengJiGame.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        renderer.setProjectionMatrix(camera.combined);

        batch.begin();
        hand.render(batch, renderer);
        for(RenderableCard c : cards) {
            c.render(batch, renderer);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
	    viewport.update(width, height, true);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 clickCoordinates = new Vector2(viewport.unproject(new Vector2(screenX, screenY)));
        cards.add(new RenderableCard().setPosition(clickCoordinates).setScale(1f).setFaceUp((button == Input.Buttons.LEFT)));
        return true;
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
		batch.dispose();
	}
}
