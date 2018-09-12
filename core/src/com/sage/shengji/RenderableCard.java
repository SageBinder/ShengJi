package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import java.util.Arrays;
import java.util.HashMap;

import static com.sage.shengji.TableScreen.*;

class RenderableCard extends Card {
    // These fields aren't final because they need to change if the card sprites are changed
    // and the dimensions of the new card sprites are different
    @SuppressWarnings("WeakerAccess")
    static final int CARD_HEIGHT_IN_PIXELS = 350;
    @SuppressWarnings("WeakerAccess")
    static final int CARD_WIDTH_IN_PIXELS = 225;
    @SuppressWarnings("WeakerAccess")
    static final float CARD_WIDTH = TABLE_WORLD_SIZE / 5f;
    @SuppressWarnings("WeakerAccess")
    static final float CARD_HEIGHT = ((float) CARD_HEIGHT_IN_PIXELS / (float) CARD_WIDTH_IN_PIXELS) * CARD_WIDTH;

    private static float unscaledCornerRadius = 0.075f;
    @SuppressWarnings("FieldCanBeLocal")
    private static float designScale = 0.95f; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle
    @SuppressWarnings("FieldCanBeLocal")
    private static float faceBorderWidth = 0.009f;
    @SuppressWarnings("FieldCanBeLocal")
    private static float backBorderWidth = 0.009f;

    private float scale = 1f; // Overall scale of the card with respect to the world

    // cardRect represents overall rectangle before rounding corners
    private Rectangle cardRect = new Rectangle();

    // verticalRect and horizontalRect are for the rounded corners effect
    private Rectangle verticalRect = new Rectangle(), // VerticalRect borders top and bottom, horizontalRect borders left and right
            horizontalRect = new Rectangle();

    private Circle[] cornerCircles = new Circle[] {
            new Circle(),
            new Circle(),
            new Circle(),
            new Circle()
    };

    private Color faceBackgroundColor = new Color(1, 1, 1, 1);
    private Color faceBorderColor = new Color(0, 0, 0, 0);
    private Color backBorderColor = new Color(0.5f, 0.5f, 0.5f, 0.5f);

    private static FileHandle defaultSpriteFolder = Gdx.files.internal("playing_cards/");
    private static FileHandle spriteFolder = defaultSpriteFolder;

    private static HashMap<Integer, Sprite> faceSprites = new HashMap<>();
    private static Sprite backSprite = null;

    private boolean faceUp = true;

    RenderableCard(Rank rank, Suit suit) {
        super(rank, suit);
    }

    RenderableCard(Card c) {
        super(c);
    }

    RenderableCard(int cardNum) {
        super(cardNum);
    }

    RenderableCard() {
        super();
    }

    static void setSpriteFolder(FileHandle newSpriteFolder) {
        spriteFolder = newSpriteFolder;
        resetForNewSprites();
    }

    static void useDefaultSpriteFolder() {
        spriteFolder = defaultSpriteFolder;
        resetForNewSprites();
    }

    private static void resetForNewSprites() {
        // This method encapsulation just in case additional logic needs to be added
        clearSprites();
    }

    private static void clearSprites() {
        backSprite = null;
        faceSprites.clear();
    }

    private static void loadBackSprite() {
        Pixmap originalBackPixmap = new Pixmap(spriteFolder.child("back.png"));
        Pixmap resizedBackPixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalBackPixmap.getFormat());
        resizedBackPixmap.drawPixmap(originalBackPixmap,
                0, 0, originalBackPixmap.getWidth(), originalBackPixmap.getHeight(),
                0, 0, resizedBackPixmap.getWidth(), resizedBackPixmap.getHeight());
        Pixmap roundedCornersPixmap = new Pixmap(resizedBackPixmap.getWidth(), resizedBackPixmap.getHeight(), Pixmap.Format.RGBA8888);

        originalBackPixmap.dispose();

        int backPixmapHeight = resizedBackPixmap.getHeight();
        int backPixmapWidth = resizedBackPixmap.getWidth();
        int cornerRadiusInPixels = (int)(((unscaledCornerRadius) * backPixmapWidth));


        // These loops create the rounded rectangle pixmap by adding transparent pixels at the corners
        for(int x = 0; x < resizedBackPixmap.getWidth(); x++) {
            nextIter:
            for(int y = 0; y < resizedBackPixmap.getHeight(); y++) {
                // These two innermost loops check conditions for adding a transparent pixel for each of the four corners
                for(int i = 0; i < 2; i++) {
                    for(int j = 0; j < 2; j++) {
                        // Top left corner: i == 0, j == 0
                        // Bottom left corner: i == 1, j == 0
                        // Top right corner: i == 0, j == 1
                        // Bottom right corner: i == 1, j == 1
                        int circleCenter_y = (backPixmapHeight * i) - (cornerRadiusInPixels * (-1 + (i * 2)));
                        int circleCenter_x = (backPixmapWidth * j) - (cornerRadiusInPixels * (-1 + (j * 2)));

                        // Using (<= and >=) vs (< and >) doesn't seem to make any visual difference
                        if(((i == 0 && y <= circleCenter_y) || (i == 1 && y >= circleCenter_y))
                                && ((j == 0 && x <= circleCenter_x) || (j == 1 && x >= circleCenter_x))
                                && Math.sqrt(Math.pow(x - circleCenter_x, 2) + Math.pow(y - circleCenter_y, 2)) >= cornerRadiusInPixels) {
                            roundedCornersPixmap.drawPixel(x, y, 0);

                            // Since it was determined that pixel (x, y) should be transparent,
                            // the rest of the conditions shouldn't be checked, so exit the two innermost loops.
                            continue nextIter;
                        }
                    }
                }

                // If all four condition checks failed, pixel (x, y) shouldn't be transparent,
                // so add the pixel at (x, y) from the back image pixmap to the new pixmap
                roundedCornersPixmap.drawPixel(x, y, resizedBackPixmap.getPixel(x, y));
            }
        }

        backSprite = new Sprite(new Texture(roundedCornersPixmap));

        roundedCornersPixmap.dispose();
        resizedBackPixmap.dispose();
    }

    private static void loadFaceSpriteForCard(int cardNum) {
        String cardImageName;

        Suit suit = Card.getSuitFromCardNum(cardNum);
        Rank rank = Card.getRankFromCardNum(cardNum);

        if(!suit.isJoker()) {
            cardImageName = rank.toStringName() + "_of_" + suit.toString() + ".png";
        } else {
            cardImageName = suit.toString() + ".png";
        }

        faceSprites.put(cardNum, new Sprite(new Texture(spriteFolder.child(cardImageName))));
    }

    private void updateShapes(Vector2 newPosition, float newScale) {
        this.scale = newScale;
        float scaledCornerRadius = unscaledCornerRadius * scale;

        cardRect.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        cardRect.setPosition(newPosition);

        verticalRect.setPosition(cardRect.x + scaledCornerRadius, cardRect.y);
        verticalRect.setSize((CARD_WIDTH * scale) - (2 * scaledCornerRadius), CARD_HEIGHT * scale);

        horizontalRect.setPosition(cardRect.x, cardRect.y + scaledCornerRadius);
        horizontalRect.setSize(CARD_WIDTH * scale, (CARD_HEIGHT * scale) - (2 * scaledCornerRadius));

        cornerCircles[0].set(cardRect.x + scaledCornerRadius,
                cardRect.y + scaledCornerRadius,
                scaledCornerRadius);

        cornerCircles[1].set(cardRect.x + (CARD_WIDTH * scale) - scaledCornerRadius,
                cardRect.y + scaledCornerRadius,
                scaledCornerRadius);

        cornerCircles[2].set(cardRect.x + scaledCornerRadius,
                cardRect.y + (CARD_HEIGHT * scale) - scaledCornerRadius,
                scaledCornerRadius);

        cornerCircles[3].set(cardRect.x + (CARD_WIDTH * scale) - scaledCornerRadius,
                cardRect.y + (CARD_HEIGHT * scale) - scaledCornerRadius,
                scaledCornerRadius);
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

    @SuppressWarnings("UnusedReturnValue")
    RenderableCard flip() {
        faceUp = !faceUp;
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
            if(faceSprites.get(cardNum()) == null) {
                loadFaceSpriteForCard(cardNum());
            }
            renderFace(batch, renderer);
        } else {
            if(backSprite == null) {
                loadBackSprite();
            }
            renderBack(batch, renderer);
        }
    }

    private void renderFace(SpriteBatch batch, ShapeRenderer renderer) {
        renderFaceBackground(batch, renderer);

        faceSprites.get(cardNum()).setSize(CARD_WIDTH * scale * designScale, CARD_HEIGHT * scale * designScale);
        faceSprites.get(cardNum()).setPosition(cardRect.x + (0.5f * scale * (CARD_WIDTH - (CARD_WIDTH * designScale))), cardRect.y + (0.5f * scale * (CARD_HEIGHT - (CARD_HEIGHT * designScale))));
        faceSprites.get(cardNum()).draw(batch);
    }

    private void renderFaceBackground(SpriteBatch batch, ShapeRenderer renderer) {
        batch.end();
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderBorder(renderer, faceBorderColor, faceBorderWidth);

        // Draw corner circles for rounded corners
        renderer.setColor(faceBackgroundColor);

        for(Circle cornerCircle : cornerCircles) {
            renderer.circle(cornerCircle.x, cornerCircle.y, cornerCircle.radius, 30);
        }

        // Draw each rectangle
        renderer.rect(verticalRect.x, verticalRect.y, verticalRect.width, verticalRect.height);
        renderer.rect(horizontalRect.x, horizontalRect.y, horizontalRect.width, horizontalRect.height);

        renderer.end();
        batch.begin();
    }

    private void renderBack(SpriteBatch batch, ShapeRenderer renderer) {
        batch.end();
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderBorder(renderer, backBorderColor, backBorderWidth);

        renderer.end();
        batch.begin();

        backSprite.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        backSprite.setPosition(cardRect.x, cardRect.y);

        backSprite.draw(batch);
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
        for(Circle cornerCircle : cornerCircles) {
            renderer.circle(cornerCircle.x, cornerCircle.y, cornerCircle.radius + borderWidth, 30);
        }
    }

    @SuppressWarnings("unused")
    boolean containsPoint(Vector2 point) {
        return containsPoint(point.x, point.y);
    }

    @SuppressWarnings("WeakerAccess")
    boolean containsPoint(float x, float y) {
        return cardRect.contains(x, y) || Arrays.stream(cornerCircles).anyMatch(circle -> circle.contains(x, y));
    }
}
