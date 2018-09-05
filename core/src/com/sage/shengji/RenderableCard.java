package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import static com.sage.shengji.TableScreen.CARD_HEIGHT;
import static com.sage.shengji.TableScreen.CARD_WIDTH;

class RenderableCard extends Card {
    private Sprite cardSprite;
    private float scale = 1f; // Overall scale of the card with respect to the world
    private float borderWidth = 0.009f;

    private Vector2 position;

    private static float faceScale = 0.95f; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle


    RenderableCard(Rank rank, Suit suit) {
        super(rank, suit);
        imageSetup();
    }


    RenderableCard(Rank rank, Suit suit, Vector2 position) {
        super(rank, suit);
        this.position = position;
        imageSetup();
    }

    RenderableCard(Rank rank, Suit suit, Vector2 position, float scale) {
        super(rank, suit);
        this.position = position;
        this.scale = scale;
        imageSetup();
    }

    RenderableCard(Card c) {
        super(c);
        imageSetup();
    }

    RenderableCard(Card c, Vector2 position) {
        super(c);
        this.position = position;
        imageSetup();
    }

    RenderableCard(Card c, Vector2 position, float scale) {
        super(c);
        this.position = position;
        this.scale = scale;
        imageSetup();
    }

    static RenderableCard getRandomCardAtPos(Vector2 position) {
        return new RenderableCard(Card.getRandomCard(), position);
    }

    static RenderableCard getRandomCardAtPosWithScale(Vector2 position, float scale) {
        return new RenderableCard(Card.getRandomCard(), position, scale);
    }

    static RenderableCard getRandomCard() {
        return new RenderableCard(Card.getRandomCard());
    }

    void setScale(float scale) {
        this.scale = scale;
    }

    float getScale() {
        return scale;
    }

    void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getPosition() {
        return position;
    }

    private void imageSetup() {
        String cardImagePath = "playing_cards/";

        if(!suit.isJoker()) {
            cardImagePath += rank.toStringName() + "_of_" + suit.toString() + ".png";
        } else {
            cardImagePath += suit.toString() + ".png";
        }

        Texture cardTexture = new Texture(Gdx.files.internal(cardImagePath));
        cardSprite = new Sprite(cardTexture);
    }

    void render(SpriteBatch batch, ShapeRenderer renderer) {
        renderBackground(batch, renderer);
        renderFace(batch);
    }

    private void renderFace(SpriteBatch batch) {
        cardSprite.setSize(CARD_WIDTH * scale * faceScale, CARD_HEIGHT * scale * faceScale);
        cardSprite.setPosition(position.x + (0.5f * scale * (CARD_WIDTH - (CARD_WIDTH * faceScale))), position.y + (0.5f * scale * (CARD_HEIGHT - (CARD_HEIGHT * faceScale))));
        cardSprite.draw(batch);
    }

    private void renderBackground(SpriteBatch batch, ShapeRenderer renderer) {
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
}
