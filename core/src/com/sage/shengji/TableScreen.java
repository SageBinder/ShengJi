package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.Random;

class TableScreen extends InputAdapter implements Screen {
    static final float TABLE_WORLD_SIZE = 100f;

    private ShengJiGame game;

	private SpriteBatch batch;
	private ShapeRenderer renderer;
	private ExtendViewport viewport;
	private OrthographicCamera camera;

	private RenderableHand hand;
	private ArrayList<RenderableCard> placedCards = new ArrayList<>();

	private int currentCardNum = 0;

	private Random random = new Random(69);

	TableScreen(ShengJiGame game) {
	    this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        renderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(TABLE_WORLD_SIZE, TABLE_WORLD_SIZE, camera);
        hand  = new RenderableHand(viewport);

        for(int i = 0; i < 2; i++) {
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

        hand.render(batch);
        for(RenderableCard c : placedCards) {
            c.render(batch);
        }
    }

    @Override
    public void resize(int width, int height) {
	    viewport.update(width, height, true);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 clickCoordinates = new Vector2(viewport.unproject(new Vector2(screenX, screenY)));

        for(int i = placedCards.size() - 1; i >= 0; i--) {
            RenderableCard c = placedCards.get(i);
            if(c.containsPoint(clickCoordinates)) {
                if(button == Input.Buttons.LEFT && c.isFaceUp()) {
                    c.setFaceColor(c.getFaceColor().sub(0.1f, 0.1f, 0.1f, 0.1f));
                    if(c.getFaceColor().a == 0) {
                        c.setFaceColor(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1));
                    }
                    return true;
                } else if(button == Input.Buttons.RIGHT) {
                    c.flip();
                    return true;
                }

                return false;
            }
        }

        RenderableCard c;
        if((c = hand.click(clickCoordinates)) != null) {
            if(button == Input.Buttons.LEFT && c.isFaceUp()) {
                c.setFaceColor(c.getFaceColor().sub(0.1f, 0.1f, 0.1f, 0.1f));
                if(c.getFaceColor().a == 0) {
                    c.setFaceColor(new Color(1, 1, 1, 1));
                }
                return true;
            } else if(button == Input.Buttons.RIGHT) {
                c.flip();
                return true;
            }

            return false;
        }

        placedCards.add(new RenderableCard((++currentCardNum % 54))
                        .setPosition(clickCoordinates)
                        .setScale(1f).setFaceUp(button == Input.Buttons.LEFT)
                        .setFaceColor(new Color(0, 0.5f, 1, 1)));
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
		RenderableCard.dispose();
	}
}
