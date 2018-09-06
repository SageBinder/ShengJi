package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import static com.sage.shengji.TableScreen.CARD_HEIGHT;
import static com.sage.shengji.TableScreen.CARD_WIDTH;

class RenderableCard extends Card {
    private Sprite faceSprite;
    private Sprite backSprite;
    private float scale = 1f; // Overall scale of the card with respect to the world
    private float borderWidth = 0.009f;

    private boolean faceUp = true;

    private Vector2 position = new Vector2(0, 0);

    private static float faceScale = 0.95f; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle


    RenderableCard(Rank rank, Suit suit) {
        super(rank, suit);
        imageSetup();
    }

    RenderableCard(Card c) {
        super(c);
        imageSetup();
    }

    RenderableCard(int cardNum) {
        super(cardNum);
        imageSetup();
    }

    RenderableCard() {
        super();
        imageSetup();
    }

    RenderableCard setScale(float scale) {
        this.scale = scale;
        return this;
    }

    RenderableCard setPosition(Vector2 position) {
        this.position = position;
        return this;
    }

    RenderableCard setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        return this;
    }

    float getScale() {
        return scale;
    }

    Vector2 getPosition() {
        return position;
    }

    boolean getFaceUp() {
        return faceUp;
    }

    private void imageSetup() {
        String cardImagePath = "playing_cards/";

        if(!suit().isJoker()) {
            cardImagePath += rank().toStringName() + "_of_" + suit().toString() + ".png";
        } else {
            cardImagePath += suit().toString() + ".png";
        }

        System.out.println(rank().toStringName() + ", " + suit().toString());
        System.out.println(rank().toString() + ", " + suit().toString());

        Texture cardTexture = new Texture(Gdx.files.internal(cardImagePath));
        faceSprite = new Sprite(cardTexture);

        backSprite = new Sprite(new Texture(Gdx.files.internal("playing_cards/back.png")));
    }

    void render(SpriteBatch batch, ShapeRenderer renderer) {
        if(faceUp) {
            renderFaceBackground(batch, renderer);
            renderFace(batch);
        } else {
            renderBack(batch);
        }
    }

    private void renderFace(SpriteBatch batch) {
        faceSprite.setSize(CARD_WIDTH * scale * faceScale, CARD_HEIGHT * scale * faceScale);
        faceSprite.setPosition(position.x + (0.5f * scale * (CARD_WIDTH - (CARD_WIDTH * faceScale))), position.y + (0.5f * scale * (CARD_HEIGHT - (CARD_HEIGHT * faceScale))));
        faceSprite.draw(batch);
    }

    private void renderFaceBackground(SpriteBatch batch, ShapeRenderer renderer) {
        batch.end();

        float radius = 0.075f * scale;
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        // Values for each rectangle's x and y coordinates
        float rect1_x, rect1_y, rect2_x, rect2_y;
        rect1_x = position.x + radius;
        rect1_y = position.y;
        rect2_x = position.x;
        rect2_y = position.y + radius;

        // Values for each rectangle's height and width
        float rect1_height, rect1_width, rect2_height, rect2_width;
        rect1_height = CARD_HEIGHT * scale;
        rect1_width = (CARD_WIDTH * scale) - (2 * radius);
        rect2_height = (CARD_HEIGHT * scale) - (2 * radius);
        rect2_width = CARD_WIDTH * scale;

        // Drawing outer rectangle borders
        renderer.setColor(0, 0, 0, 0);

        renderer.rectLine(rect1_x, rect1_y, rect1_x + rect1_width, rect1_y, borderWidth * 2);
        renderer.rectLine(rect1_x, rect1_y + rect1_height, rect1_x + rect1_width, rect1_y + rect1_height, borderWidth * 2);

        renderer.rectLine(rect2_x, rect2_y , rect2_x, rect2_y + rect2_height, borderWidth * 2);
        renderer.rectLine(rect2_x + rect2_width, rect2_y, rect2_x + rect2_width, rect2_y + rect2_height, borderWidth * 2);

        // Draw a slightly larger corner circle with black color for the border
        renderer.circle(position.x + radius, position.y + radius, radius + borderWidth, 30);
        renderer.circle(position.x + (CARD_WIDTH * scale) - radius, position.y + radius, radius + borderWidth, 30);
        renderer.circle(position.x + radius, position.y + (CARD_HEIGHT * scale) - radius, radius + borderWidth, 30);
        renderer.circle(position.x + (CARD_WIDTH * scale) - radius, position.y + (CARD_HEIGHT * scale) - radius, radius + borderWidth, 30);

        // Draw corner circles for rounded corners
        renderer.setColor(new Color(255, 255, 255, 1));

        renderer.circle(position.x + radius, position.y + radius, radius, 30);
        renderer.circle(position.x + (CARD_WIDTH * scale) - radius, position.y + radius, radius, 30);
        renderer.circle(position.x + radius, position.y + (CARD_HEIGHT * scale) - radius, radius, 30);
        renderer.circle(position.x + (CARD_WIDTH * scale) - radius, position.y + (CARD_HEIGHT * scale) - radius, radius, 30);

        // Draw each rectangle
        renderer.rect(rect1_x, rect1_y, rect1_width, rect1_height); // Rect 1 borders up and down
        renderer.rect(rect2_x, rect2_y, rect2_width, rect2_height); // Rect 2 borders left and right

        renderer.end();

        batch.begin();
    }

    private void renderBack(SpriteBatch batch) {
        backSprite.setSize(CARD_WIDTH * scale * faceScale, CARD_HEIGHT * scale * faceScale);
        backSprite.setPosition(position.x + (0.5f * scale * (CARD_WIDTH - (CARD_WIDTH * faceScale))), position.y + (0.5f * scale * (CARD_HEIGHT - (CARD_HEIGHT * faceScale))));
        backSprite.draw(batch);
    }
}
