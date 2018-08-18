package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;

import static com.sage.shengji.Constants.GAME_WORLD_SIZE;

public class GameScreen extends InputAdapter implements Screen {
    private ShengJiGame game;

	private SpriteBatch batch;
	private ShapeRenderer renderer;
	private ExtendViewport viewport;
	private OrthographicCamera camera;

	private RenderableHand hand;
	private ArrayList<RenderableCard> cards = new ArrayList<>();

	GameScreen(ShengJiGame game) {
	    this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        renderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(GAME_WORLD_SIZE, GAME_WORLD_SIZE, camera);
        hand  = new RenderableHand(viewport);

//		for(Suit suit : Suit.values()) {
//		    for(Rank rank : Rank.values()) {
//		        if((suit == Suit.JOKER && !(rank == Rank.SMALL || rank == Rank.BIG)) || ((suit != Suit.JOKER) && (rank == Rank.SMALL || rank == Rank.BIG))) {
//		            continue;
//                }
//		        hand.add(suit, rank);
//            }
//        }

//        for(Suit suit : Suit.values()) {
//            if(suit.isJoker()) {
//                continue;
//            }
//            hand.add(Rank.KING, suit);
//            hand.add(Rank.ACE, suit);
//        }

        for(int i = 0; i < 52; i++) {
            hand.add(RenderableCard.getRandomCard());
        }

        camera.position.set(camera.viewportWidth/2,camera.viewportHeight/2,0);
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Constants.BACKGROUND_COLOR.r, Constants.BACKGROUND_COLOR.g, Constants.BACKGROUND_COLOR.b, 1);
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

	@Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
	    Vector2 clickCoordinates = new Vector2(viewport.unproject(new Vector2(screenX, screenY)));
	    cards.add(RenderableCard.getRandomCardAtPosWithScale(new Vector2(clickCoordinates.x, clickCoordinates.y), 0.25f));
        return true;
    }
}
