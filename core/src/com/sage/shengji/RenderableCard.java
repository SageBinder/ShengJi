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
    @SuppressWarnings("WeakerAccess")
    static final int CARD_HEIGHT_IN_PIXELS = 323;
    @SuppressWarnings("WeakerAccess")
    static final int CARD_WIDTH_IN_PIXELS = 222;
    @SuppressWarnings("WeakerAccess")
    static final float CARD_WIDTH = GAME_WORLD_SIZE / 5f;
    @SuppressWarnings("WeakerAccess")
    static final float CARD_HEIGHT = ((float)CARD_HEIGHT_IN_PIXELS / (float)CARD_WIDTH_IN_PIXELS) * CARD_WIDTH;

    private static float unscaledCornerRadius = 0.075f;
    @SuppressWarnings("FieldCanBeLocal")
    private static float designScale = 0.95f; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle
    @SuppressWarnings("FieldCanBeLocal")
    private static float faceBorderWidth = 0.009f;
    private static float backBorderWidth = 0.009f;

    private Color faceBorderColor = new Color(0, 0, 0, 0);
    private Color backBorderColor = new Color(0.5f, 0.5f, 0.5f, 0.5f);

    private float scale = 1f; // Overall scale of the card with respect to the world
    private float scaledCornerRadius = 0.009f;

    // cardRect represents overall rectangle before rounding corners
    private Rectangle cardRect = new Rectangle();

    // verticalRect and horizontalRect are for the rounded corners effect
    private Rectangle verticalRect = new Rectangle(), // VerticalRect borders top and bottom, horizontalRect borders left and right
            horizontalRect = new Rectangle();

    private Vector2 bottomLeftCircleCenter = new Vector2(),
            bottomRightCircleCenter = new Vector2(),
            topLeftCircleCenter = new Vector2(),
            topRightCircleCenter = new Vector2();

    private boolean faceUp = true;

    private Sprite faceSprite;
    private Sprite backSprite;

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

    private void imageSetup() {
        faceImageSetup();
        backImageSetup();
    }

    private void backImageSetup() {
        Pixmap rectPixmap = new Pixmap(Gdx.files.internal("playing_cards/back.png"));
        Pixmap roundedRectPixmap = new Pixmap(rectPixmap.getWidth(), rectPixmap.getHeight(), Pixmap.Format.RGBA8888);

        int cornerRadiusInPixels = (int)(((unscaledCornerRadius) / (CARD_WIDTH)) * CARD_WIDTH_IN_PIXELS);

        // These loops create the rounded rectangle pixmap by adding transparent pixels at the corners
        for(int x = 0; x < rectPixmap.getWidth(); x++) {
            nextIter:
            for(int y = 0; y < rectPixmap.getHeight(); y++) {
                // These two innermost loops check conditions for adding a transparent pixel for each of the four corners
                for(int i = 0; i < 2; i++) {
                    for(int j = 0; j < 2; j++) {
                        // Top left corner: i == 0, j == 0
                        // Bottom left corner: i == 1, j == 0
                        // Top right corner: i == 0, j == 1
                        // Bottom right corner: i == 1, j == 1
                        int circleCenter_y = (CARD_HEIGHT_IN_PIXELS * i) - (cornerRadiusInPixels * (-1 + (i * 2)));
                        int circleCenter_x = (CARD_WIDTH_IN_PIXELS * j) - (cornerRadiusInPixels * (-1 + (j * 2)));

                        if(((i == 0 && y <= circleCenter_y) || (i == 1 && y >= circleCenter_y))
                                && ((j == 0 && x <= circleCenter_x) || (j == 1 && x >= circleCenter_x))
                                && Math.sqrt(Math.pow(x - circleCenter_x, 2) + Math.pow(y - circleCenter_y, 2)) >= cornerRadiusInPixels) {
                            roundedRectPixmap.drawPixel(x, y, 0);

                            // Since it was determined that pixel (x, y) should be transparent,
                            // the rest of the conditions shouldn't be checked, so exit the two innermost loops.
                            continue nextIter;
                        }
                    }
                }

                // If all four condition checks failed, pixel (x, y) shouldn't be transparent,
                // so add the pixel at (x, y) from the back image pixmap to the new pixmap
                roundedRectPixmap.drawPixel(x, y, rectPixmap.getPixel(x, y));
            }
        }

        backSprite = new Sprite(new Texture(roundedRectPixmap));
        rectPixmap.dispose();
        roundedRectPixmap.dispose();
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

    private void updateShapes(Vector2 newPosition, float newScale) {
        this.scale = newScale;
        scaledCornerRadius = unscaledCornerRadius * scale;

        cardRect.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        cardRect.setPosition(newPosition);

        verticalRect.setPosition(cardRect.x + scaledCornerRadius, cardRect.y);
        verticalRect.setSize((CARD_WIDTH * scale) - (2 * scaledCornerRadius), CARD_HEIGHT * scale);

        horizontalRect.setPosition(cardRect.x, cardRect.y + scaledCornerRadius);
        horizontalRect.setSize(CARD_WIDTH * scale, (CARD_HEIGHT * scale) - (2 * scaledCornerRadius));

        bottomLeftCircleCenter.set(cardRect.x + scaledCornerRadius, cardRect.y + scaledCornerRadius);
        bottomRightCircleCenter.set(cardRect.x + (CARD_WIDTH * scale) - scaledCornerRadius, cardRect.y + scaledCornerRadius);
        topLeftCircleCenter.set(cardRect.x + scaledCornerRadius, cardRect.y + (CARD_HEIGHT * scale) - scaledCornerRadius);
        topRightCircleCenter.set(cardRect.x + (CARD_WIDTH * scale) - scaledCornerRadius, cardRect.y + (CARD_HEIGHT * scale) - scaledCornerRadius);
    }

    RenderableCard setScale(float newScale) {
        this.scale = newScale;
        updateShapes(cardRect.getPosition(new Vector2()), newScale);
        return this;
    }

    RenderableCard setPosition(Vector2 newPosition) {
        updateShapes(newPosition, scale);
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
        return cardRect.getPosition(new Vector2());
    }

    boolean getFaceUp() {
        return faceUp;
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

        faceSprite.setSize(CARD_WIDTH * scale * designScale, CARD_HEIGHT * scale * designScale);
        faceSprite.setPosition(cardRect.x + (0.5f * scale * (CARD_WIDTH - (CARD_WIDTH * designScale))), cardRect.y + (0.5f * scale * (CARD_HEIGHT - (CARD_HEIGHT * designScale))));
        faceSprite.draw(batch);
    }

    private void renderFaceBackground(SpriteBatch batch, ShapeRenderer renderer) {
        batch.end();
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderBorder(renderer, faceBorderColor, faceBorderWidth);

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

    private void renderBorder(ShapeRenderer renderer, Color borderColor, float borderWidth) {
        // Drawing outer rectangle borders
        renderer.setColor(borderColor);

        // Vertical rectangle bottom border
        renderer.rectLine(verticalRect.x,                // x1
                verticalRect.y,                          // y1
                verticalRect.x + verticalRect.width, // x2
                verticalRect.y,                          // y2
                borderWidth * 2);                 // width

        // Vertical rectangle top border
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

        renderBorder(renderer, backBorderColor, faceBorderWidth);

        renderer.end();
        batch.begin();

        backSprite.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        backSprite.setPosition(cardRect.x, cardRect.y);

        backSprite.draw(batch);
    }
}
