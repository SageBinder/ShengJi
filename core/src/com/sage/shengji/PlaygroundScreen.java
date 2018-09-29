package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.Random;

class PlaygroundScreen extends InputAdapter implements Screen {
    static final float TABLE_WORLD_SIZE = 100f;

    private ShengJiGame game;

	private SpriteBatch batch;
	private ExtendViewport viewport;
	private OrthographicCamera camera;

	private RenderableHand hand;
	private ArrayList<RenderableCard> placedCards = new ArrayList<>();

	private int currentCardNum = 0;
    private RenderableCard currentCard;

	private Random random = new Random(69);

	PlaygroundScreen(ShengJiGame game) {
	    this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(TABLE_WORLD_SIZE, TABLE_WORLD_SIZE, camera);
        hand  = new RenderableHand();

        for(int i = 0; i < 22; i++) {
            hand.add(new RenderableCard());
        }

        currentCard = new RenderableCard(currentCardNum).setPosition(5f, TABLE_WORLD_SIZE / 2).setScale(0.8f);

        camera.position.set(camera.viewportWidth/2,camera.viewportHeight/2,0);
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ShengJiGame.BACKGROUND_COLOR.r, ShengJiGame.BACKGROUND_COLOR.g, ShengJiGame.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        currentCard.render(batch);
        hand.render(batch, viewport);
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
                    c.setFaceBackgroundColor(c.getFaceBackgroundColor().sub(0.1f, 0.1f, 0.1f, 0.1f));
                    if(c.getFaceBackgroundColor().a == 0) {
                        c.setFaceBackgroundColor(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1));
                    }
                    return true;
                } else if(button == Input.Buttons.RIGHT) {
                    c.flip();
                    return true;
                } else if(button == Input.Buttons.MIDDLE) {
                    placedCards.remove(i);
                    return true;
                }

                return false;
            }
        }

        RenderableCard c;
        if((c = hand.getClickedCard(clickCoordinates)) != null) {
            if(button == Input.Buttons.LEFT && c.isFaceUp()) {
                c.toggleSelected();
                return true;
            } else if(button == Input.Buttons.RIGHT) {
                c.flip();
                return true;
            }

            return false;
        }

        placedCards.add(new RenderableCard(currentCardNum)
                        .setPosition(clickCoordinates)
                        .setScale(1f).setFaceUp(button == Input.Buttons.LEFT));
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
	    currentCardNum -= amount;
	    currentCardNum %= 54;
	    if(currentCardNum < 0) {
	        currentCardNum = 54 + currentCardNum;
        }
        currentCard = new RenderableCard(currentCardNum).setPosition(5f, TABLE_WORLD_SIZE / 2).setScale(0.8f);

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
		batch.dispose();
		RenderableCard.dispose();
	}
}
