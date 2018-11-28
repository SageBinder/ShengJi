package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ListIterator;
import java.util.Random;

class PlaygroundScreen extends InputAdapter implements Screen {
    private ScreenManager game;

	private SpriteBatch batch;
	private ExtendViewport viewport;

	private RenderableHand hand;
	private RenderableCardList placedCards = new RenderableCardList();

	private int currentCardNum = 0;
	private float currentCardSinInput = 0; // <- for moving card across screen
    private RenderableCard currentCard;

	private Random random = new Random(69);

	PlaygroundScreen(ScreenManager game) {
	    this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hand = new RenderableHand();

        for(int i = 0; i < 5; i++) {
            hand.add(new RenderableCard(i));
        }

        currentCard = new RenderableCard(currentCardNum);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
	    // SPRITE STRETCHING PROBLEM FIXED! I left this code in this commit just for demonstration.
	    // Code for testing the sprite stretching problem thing. It moves currentCard across screen slowly. Very lazily written lol
	    float speed = 1 / 100f;
	    currentCardSinInput += delta;
	    currentCardSinInput %= (int)1f / speed;
	    float sin = (MathUtils.sin(currentCardSinInput * MathUtils.PI2 * speed) / 2f) + 0.5f;
	    currentCard.setX((viewport.getWorldWidth() / 10f) + (sin * viewport.getWorldWidth() * 0.7f));

        Gdx.gl.glClearColor(ScreenManager.BACKGROUND_COLOR.r, ScreenManager.BACKGROUND_COLOR.g, ScreenManager.BACKGROUND_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        currentCard.render(batch, viewport);
        hand.render(batch, viewport);
        placedCards.render(batch, viewport);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
	    viewport.update(width, height, true);

	    currentCard.setPosition(viewport.getWorldWidth() / 20f, viewport.getWorldHeight() / 2f)
                .setHeight(viewport.getWorldHeight() / 5f);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 clickCoordinates = viewport.unproject(new Vector2(screenX, screenY));

        for(ListIterator<RenderableCard> i = placedCards.reverseListIterator(); i.hasPrevious();) {
            RenderableCard c = i.previous();
            if(c.displayRectContainsPoint(clickCoordinates)) {
                if(button == Input.Buttons.LEFT && c.isFaceUp()) {
                    if(c.getFaceBackgroundColor().a == 0) {
                        c.setFaceBackgroundColor(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1));
                        return true;
                    }

                    c.setFaceBackgroundColor(c.getFaceBackgroundColor().sub(0.1f, 0.1f, 0.1f, 0.1f));
                    return true;
                } else if(button == Input.Buttons.RIGHT) {
                    c.flip();
                    return true;
                } else if(button == Input.Buttons.MIDDLE) {
                    placedCards.remove(c);
                    return true;
                }

                return false;
            }
        }

        for(ListIterator<RenderableCard> i = hand.reverseListIterator(); i.hasPrevious();) {
            final RenderableCard c = i.previous();
            if(c.displayRectContainsPoint(clickCoordinates) || (!c.isSelected() && c.baseRectContainsPoint(clickCoordinates))) {
                if(button == Input.Buttons.LEFT && c.isFaceUp()) {
                    c.toggleSelected();
                    return true;
                } else if(button == Input.Buttons.RIGHT) {
                    c.flip();
                    return true;
                } else if(button == Input.Buttons.MIDDLE) {
                    hand.remove(c);
                    return true;
                }

                return false;
            }
        }

        placedCards.add(new RenderableCard(currentCardNum)
                        .setPosition(clickCoordinates)
                        .setHeight(viewport.getWorldHeight() / 4f)
                        .setFaceUp(button == Input.Buttons.LEFT));

        return true;
    }

    @Override
    public boolean scrolled(int amount) {
	    currentCardNum -= amount;
	    currentCardNum %= 54;
	    if(currentCardNum < 0) {
	        currentCardNum = 54 + currentCardNum;
        }
        currentCard = new RenderableCard(currentCardNum)
                .setPosition(currentCard.getPosition())
                .setHeight(currentCard.getHeight());

        return false;
    }

    @Override
    public boolean keyDown(int keyCode) {
	    switch(keyCode) {
            case Input.Keys.H:
                hand.add(new RenderableCard(currentCardNum));
                return true;
            case Input.Keys.C:
                if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
                        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                    hand.clear();
                } else {
                    hand.removeIf(AbstractRenderableCard::isSelected);
                }
                return true;
            case Input.Keys.D:
                hand.setDebug(!hand.inDebugMode());
                return true;
            case Input.Keys.BACKSPACE:
                placedCards.clear();
                return true;

            default:
                return false;
        }
    }

    private RenderableCard lastHighlightedCard;
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
	    Vector2 clickCoordinates = viewport.unproject(new Vector2(screenX, screenY));

	    for(ListIterator<RenderableCard> i = hand.reverseListIterator(); i.hasPrevious();) {
            final RenderableCard c = i.previous();

            if(c.isSelected()) {
                if(c.displayRectContainsPoint(clickCoordinates)) {
                    break;
                } else {
                    continue;
                }
            }

            if(c.displayRectContainsPoint(clickCoordinates) || c.baseRectContainsPoint(clickCoordinates)) {
                if(lastHighlightedCard != null && !lastHighlightedCard.isSelected() && lastHighlightedCard != c) {
                    lastHighlightedCard.resetDisplayRect();
                    lastHighlightedCard.setFaceBackgroundColor(new Color(AbstractRenderableCard.defaultFaceUnselectedBackgroundColor));
                }

                c.setDisplayY(c.getY() + (c.getHeight() * 0.4f));
                c.setFaceBackgroundColor(new Color(1.0f, 1.0f, 0.5f, 1.0f));
                lastHighlightedCard = c;

                return false;
            }
        }

        if(lastHighlightedCard != null && !lastHighlightedCard.isSelected()) {
            lastHighlightedCard.resetDisplayRect();
            lastHighlightedCard.setFaceBackgroundColor(new Color(AbstractRenderableCard.defaultFaceUnselectedBackgroundColor));
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
		batch.dispose();
		RenderableCard.dispose();
	}
}
