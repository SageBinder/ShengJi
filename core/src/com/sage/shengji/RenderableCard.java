package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import static com.sage.shengji.TableScreen.*;

class RenderableCard extends Card {
    private Sprite faceSprite;
    private Sprite backSprite;
    private float scale = 1f; // Overall scale of the card with respect to the world
    private float borderWidth = 0.009f;
    
    
    private boolean faceUp = true;

    private Vector2 position = new Vector2(0, 0);

    private Rectangle cardRect = new Rectangle();

    private Rectangle verticalRect = new Rectangle(), // VerticalRect borders top and bottom, horizontalRect borders left and right
            horizontalRect = new Rectangle();
    
    private float scaledCornerRadius = 0.009f;
    private Vector2 bottomLeftCircleCenter = new Vector2(),
            bottomRightCircleCenter = new Vector2(),
            topLeftCircleCenter = new Vector2(),
            topRightCircleCenter = new Vector2();
    
    
    private static float unscaledCornerRadius = 0.075f;
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

    private void updateBounds() {
        scaledCornerRadius = unscaledCornerRadius * scale;

        cardRect.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        cardRect.setPosition(position);

        verticalRect.setPosition(position.x + scaledCornerRadius, position.y);
        verticalRect.setSize((CARD_WIDTH * scale) - (2 * scaledCornerRadius), CARD_HEIGHT * scale);

        horizontalRect.setPosition(position.x, position.y + scaledCornerRadius);
        horizontalRect.setSize(CARD_WIDTH * scale, (CARD_HEIGHT * scale) - (2 * scaledCornerRadius));

        bottomLeftCircleCenter.set(position.x + scaledCornerRadius, position.y + scaledCornerRadius);
        bottomRightCircleCenter.set(position.x + (CARD_WIDTH * scale) - scaledCornerRadius, position.y + scaledCornerRadius);
        topLeftCircleCenter.set(position.x + scaledCornerRadius, position.y + (CARD_HEIGHT * scale) - scaledCornerRadius);
        topRightCircleCenter.set(position.x + (CARD_WIDTH * scale) - scaledCornerRadius, position.y + (CARD_HEIGHT * scale) - scaledCornerRadius);
    }

    RenderableCard setScale(float scale) {
        this.scale = scale;
        updateBounds();
        return this;
    }

    RenderableCard setPosition(Vector2 position) {
        this.position = position;
        updateBounds();
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
        faceImageSetup();
        backImageSetup();
    }

    private void backImageSetup() {
        Pixmap pixmap = new Pixmap(Gdx.files.internal("playing_cards/back.png"));
        pixmap.setColor(Color.CHARTREUSE);

        int radiusInPixels = (int)(((unscaledCornerRadius * scale) / (CARD_WIDTH * scale)) * CARD_WIDTH_IN_PIXELS);

        pixmap.fillCircle(radiusInPixels, radiusInPixels, radiusInPixels);
        pixmap.fillCircle(CARD_WIDTH_IN_PIXELS - radiusInPixels, radiusInPixels, radiusInPixels);
        pixmap.fillCircle(radiusInPixels, CARD_HEIGHT_IN_PIXELS - radiusInPixels, radiusInPixels);
        pixmap.fillCircle(CARD_WIDTH_IN_PIXELS - radiusInPixels, CARD_HEIGHT_IN_PIXELS - radiusInPixels, radiusInPixels);

        pixmap.drawRectangle(radiusInPixels, 0, CARD_WIDTH_IN_PIXELS - (2 * radiusInPixels), CARD_HEIGHT_IN_PIXELS);
        pixmap.drawRectangle(0, radiusInPixels, CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS - (2 * radiusInPixels));

        backSprite = new Sprite(new Texture(pixmap));
    }

    private void faceImageSetup() {
        String cardImagePath = "playing_cards/";

        if(!suit().isJoker()) {
            cardImagePath += rank().toStringName() + "_of_" + suit().toString() + ".png";
        } else {
            cardImagePath += suit().toString() + ".png";
        }

        Texture cardTexture = new Texture(Gdx.files.internal(cardImagePath));
        faceSprite = new Sprite(cardTexture);
    }

    void render(SpriteBatch batch, ShapeRenderer renderer) {
        if(faceUp) {
            renderFace(batch, renderer);
        } else {
            renderBack(batch, renderer);
        }
    }

    private void renderFace(SpriteBatch batch, ShapeRenderer renderer) {
        renderFaceBackground(batch, renderer);

        faceSprite.setSize(CARD_WIDTH * scale * faceScale, CARD_HEIGHT * scale * faceScale);
        faceSprite.setPosition(position.x + (0.5f * scale * (CARD_WIDTH - (CARD_WIDTH * faceScale))), position.y + (0.5f * scale * (CARD_HEIGHT - (CARD_HEIGHT * faceScale))));
        faceSprite.draw(batch);
    }

    private void renderFaceBackground(SpriteBatch batch, ShapeRenderer renderer) {
        batch.end();
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderBorder(renderer);

        // Draw corner circles for rounded corners
        renderer.setColor(new Color(255, 255, 255, 1));

        renderer.circle(bottomLeftCircleCenter.x, bottomLeftCircleCenter.y, scaledCornerRadius, 30);
        renderer.circle(bottomRightCircleCenter.x, bottomRightCircleCenter.y, scaledCornerRadius, 30);
        renderer.circle(topLeftCircleCenter.x, topLeftCircleCenter.y, scaledCornerRadius, 30);
        renderer.circle(topRightCircleCenter.x, topRightCircleCenter.y, scaledCornerRadius, 30);

        // Draw each rectangle
        renderer.rect(verticalRect.x, verticalRect.y, verticalRect.width, verticalRect.height);
        renderer.rect(horizontalRect.x, horizontalRect.y, horizontalRect.width, horizontalRect.height);

        renderer.end();
        batch.begin();
    }

    private void renderBorder(ShapeRenderer renderer) {
        // Drawing outer rectangle borders
        renderer.setColor(0, 0, 0, 0);

        // Vertical rectangle top border
        renderer.rectLine(verticalRect.x,                // x1
                verticalRect.y,                          // y1
                verticalRect.x + verticalRect.width, // x2
                verticalRect.y,                          // y2
                borderWidth * 2);                 // width

        // Vertical rectangle bottom border
        renderer.rectLine(verticalRect.x,                 // x1
                verticalRect.y + verticalRect.height, // y1
                verticalRect.x + verticalRect.width,  // x2
                verticalRect.y + verticalRect.height, // y2
                borderWidth * 2);                  // width

        // Horizontal rectangle left border
        renderer.rectLine(horizontalRect.x,                   // x1
                horizontalRect.y,                             // y1
                horizontalRect.x,                             // x2
                horizontalRect.y + horizontalRect.height, // y2
                borderWidth * 2);                      // width

        // Horizontal rectangle right border
        renderer.rectLine(horizontalRect.x + horizontalRect.width, // x1
                horizontalRect.y,                                      // y1
                horizontalRect.x + horizontalRect.width,           // x2
                horizontalRect.y + horizontalRect.height,          // y2
                borderWidth * 2);                               // width

        // Draw a slightly larger corner circle with black color for the border
        renderer.circle(bottomLeftCircleCenter.x, bottomLeftCircleCenter.y, scaledCornerRadius + borderWidth, 30);
        renderer.circle(bottomRightCircleCenter.x, bottomRightCircleCenter.y, scaledCornerRadius + borderWidth, 30);
        renderer.circle(topLeftCircleCenter.x, topLeftCircleCenter.y, scaledCornerRadius + borderWidth, 30);
        renderer.circle(topRightCircleCenter.x, topRightCircleCenter.y, scaledCornerRadius + borderWidth, 30);
    }

    private void renderBack(SpriteBatch batch, ShapeRenderer renderer) {
        batch.end();
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderBorder(renderer);

        renderer.end();
        batch.begin();

        backSprite.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        backSprite.setPosition(position.x, position.y);

        backSprite.draw(batch);
    }
}
