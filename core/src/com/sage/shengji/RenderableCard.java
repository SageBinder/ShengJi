package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    @SuppressWarnings("FieldCanBeLocal")
    private static float designScale = 0.95f; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle
    private static float unscaledCornerRadius = 0.075f * CARD_WIDTH;
    @SuppressWarnings("FieldCanBeLocal")
    private static int faceBorderThicknessInPixels = (int)(0.015f * CARD_WIDTH_IN_PIXELS);
    @SuppressWarnings("FieldCanBeLocal")
    private static int backBorderThicknessInPixels = (int)(0.015f * CARD_WIDTH_IN_PIXELS);

    private float scale = 1f; // Overall scale of the card with respect to the world

    // cardRect represents overall rectangle before rounding corners
    private Rectangle cardRect = new Rectangle();

    // verticalRect and horizontalRect are for the rounded corners effect
    private Rectangle verticalRect = new Rectangle(), // VerticalRect borders top and bottom, horizontalRect borders left and right
            horizontalRect = new Rectangle();

    private Circle[] cornerCircles = new Circle[] {
            new Circle(), // TOP LEFT
            new Circle(), // TOP RIGHT
            new Circle(), // BOTTOM LEFT
            new Circle()  // BOTTOM RIGHT
    };

    private Color faceBackgroundColor = new Color(1, 1, 1, 1);
    private Sprite thisCardFaceSprite = null;

    private static Color faceBorderColor = new Color(0, 0, 0, 1);
    private static Color backBorderColor = new Color(0.5f, 0.5f, 0.5f, 1);

    private static FileHandle defaultSpriteFolder = Gdx.files.internal("playing_cards/");
    private static FileHandle spriteFolder = defaultSpriteFolder;

    private static HashMap<Integer, Pixmap> faceDesignPixmaps = new HashMap<>();
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
        clearSprites();
    }

    static void useDefaultSpriteFolder() {
        spriteFolder = defaultSpriteFolder;
        clearSprites();
    }

    private static void clearSprites() {
        backSprite = null;
        for(Pixmap p : faceDesignPixmaps.values()) {
            p.dispose();
        }
        faceDesignPixmaps.clear();
    }

    private static void loadBackSprite() {
        Pixmap originalBackPixmap = new Pixmap(spriteFolder.child("back.png"));
        Pixmap resizedBackPixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalBackPixmap.getFormat());

        resizedBackPixmap.drawPixmap(originalBackPixmap,
                0, 0, originalBackPixmap.getWidth(), originalBackPixmap.getHeight(),
                0, 0, resizedBackPixmap.getWidth(), resizedBackPixmap.getHeight());

        int cornerRadiusInPixels = (int)((unscaledCornerRadius / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS);
        roundPixmapCorners(resizedBackPixmap, cornerRadiusInPixels);
        drawCurvedBorderOnPixmap(resizedBackPixmap,
                cornerRadiusInPixels,
                backBorderThicknessInPixels,
                backBorderColor);
        backSprite = new Sprite(new Texture(resizedBackPixmap));

        originalBackPixmap.dispose();
        resizedBackPixmap.dispose();
    }

    private static void loadFaceDesignTextureForCard(int cardNum) {
        String cardImageName;

        Suit suit = Card.getSuitFromCardNum(cardNum);
        Rank rank = Card.getRankFromCardNum(cardNum);

        if(!suit.isJoker()) {
            cardImageName = rank.toStringName() + "_of_" + suit.toString() + ".png";
        } else {
            cardImageName = suit.toString() + ".png";
        }

        Pixmap originalUnscaledImagePixmap = new Pixmap(spriteFolder.child(cardImageName));
        Pixmap originalScaledImagePixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalUnscaledImagePixmap.getFormat());

        originalScaledImagePixmap.drawPixmap(originalUnscaledImagePixmap,
                0, 0, originalUnscaledImagePixmap.getWidth(), originalUnscaledImagePixmap.getHeight(),
                (int)(0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * designScale))), (int)(0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * designScale))),
                (int)(CARD_WIDTH_IN_PIXELS * designScale), (int)(CARD_HEIGHT_IN_PIXELS * designScale));

        drawCurvedBorderOnPixmap(originalScaledImagePixmap,
                (int)((unscaledCornerRadius / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS),
                faceBorderThicknessInPixels,
                faceBorderColor);

        faceDesignPixmaps.put(cardNum, originalScaledImagePixmap);

        originalUnscaledImagePixmap.dispose();
    }

    private static void roundPixmapCorners(Pixmap pixmap, int radius) {
        int pixmapHeight = pixmap.getHeight();
        int pixmapWidth = pixmap.getWidth();

        Pixmap.setBlending(Pixmap.Blending.None);

        // These loops create the rounded rectangle pixmap by adding transparent pixels at the corners
        for(int x = 0; x < pixmapWidth; x++) {
            nextIter:
            for(int y = 0; y < pixmapHeight; y++) {
                // These two innermost loops check conditions for adding a transparent pixel for each of the four corners
                for(int i = 0; i < 2; i++) {
                    for(int j = 0; j < 2; j++) {
                        // Top left corner: i == 0, j == 0
                        // Bottom left corner: i == 1, j == 0
                        // Top right corner: i == 0, j == 1
                        // Bottom right corner: i == 1, j == 1
                        int circleCenter_y = (pixmapHeight * i) - (radius * (-1 + (i * 2)));
                        int circleCenter_x = (pixmapWidth * j) - (radius * (-1 + (j * 2)));

                        // Using (<= and >=) as opposed to (< and >) doesn't seem to make any visual difference
                        if(((i == 0 && y <= circleCenter_y) || (i == 1 && y >= circleCenter_y))
                                && ((j == 0 && x <= circleCenter_x) || (j == 1 && x >= circleCenter_x))
                                && Math.sqrt(Math.pow(x - circleCenter_x, 2) + Math.pow(y - circleCenter_y, 2)) >= radius) {
                            pixmap.drawPixel(x, y, 0);

                            // Since it was determined that pixel (x, y) should be transparent,
                            // the rest of the conditions shouldn't be checked, so exit the two innermost loops.
                            continue nextIter;
                        }
                    }
                }

                // If all four condition checks failed, pixel (x, y) shouldn't be transparent,
                // so add the pixel at (x, y) from the back image pixmap to the new pixmap
                pixmap.drawPixel(x, y, pixmap.getPixel(x, y));
            }
        }
    }

    private static void drawCurvedBorderOnPixmap(Pixmap pixmap, int radius, int borderThickness, Color color) {
        int pixmapHeight = pixmap.getHeight();
        int pixmapWidth = pixmap.getWidth();

        Pixmap.setBlending(Pixmap.Blending.None);

        pixmap.setColor(color);

        // Left border
        pixmap.fillRectangle(0, radius, borderThickness, pixmapHeight - (2 * radius));

        // Right border
        pixmap.fillRectangle(pixmapWidth - borderThickness, radius, borderThickness, pixmapHeight - (2 * radius));

        // Top border
        pixmap.fillRectangle(radius, 0, pixmapWidth - (2 * radius), borderThickness);

        // Bottom border
        pixmap.fillRectangle(radius, pixmapHeight - borderThickness, pixmapWidth - (2 * radius), borderThickness);

        // TODO: Corner borders
    }

    static void dispose() {
        clearSprites();
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

        cornerCircles[0].set(cardRect.x + scaledCornerRadius,                           // BOTTOM LEFT
                cardRect.y + scaledCornerRadius,
                scaledCornerRadius);

        cornerCircles[1].set(cardRect.x + (CARD_WIDTH * scale) - scaledCornerRadius,    // BOTTOM RIGHT
                cardRect.y + scaledCornerRadius,
                scaledCornerRadius);

        cornerCircles[2].set(cardRect.x + (CARD_WIDTH * scale) - scaledCornerRadius,    // TOP RIGHT
                cardRect.y + (CARD_HEIGHT * scale) - scaledCornerRadius,
                scaledCornerRadius);

        cornerCircles[3].set(cardRect.x + scaledCornerRadius,                           // TOP LEFT
                cardRect.y + (CARD_HEIGHT * scale) - scaledCornerRadius,
                scaledCornerRadius);
    }

    private void setupThisCardFaceSprite() {
        if(faceDesignPixmaps.get(cardNum()) == null) {
            loadFaceDesignTextureForCard(cardNum());
        } else if(thisCardFaceSprite != null) {
            return;
        }
        System.out.println("in here");
        Pixmap thisCardFaceSpritePixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, Pixmap.Format.RGBA8888);
        Pixmap faceDesignPixmap = faceDesignPixmaps.get(cardNum());

        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        thisCardFaceSpritePixmap.setColor(faceBackgroundColor);
        thisCardFaceSpritePixmap.fill();

        roundPixmapCorners(thisCardFaceSpritePixmap, (int)((unscaledCornerRadius / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS));
        drawCurvedBorderOnPixmap(thisCardFaceSpritePixmap,
                (int)((unscaledCornerRadius / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS),
                faceBorderThicknessInPixels,
                faceBorderColor);

        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        thisCardFaceSpritePixmap.drawPixmap(faceDesignPixmap,
                0, 0, faceDesignPixmap.getWidth(), faceDesignPixmap.getHeight(),
                0, 0, thisCardFaceSpritePixmap.getWidth(), thisCardFaceSpritePixmap.getHeight());

        thisCardFaceSprite = new Sprite(new Texture(thisCardFaceSpritePixmap));

        thisCardFaceSpritePixmap.dispose();
    }

    void render(SpriteBatch batch) {
        if(faceUp) {
            setupThisCardFaceSprite();
            batch.setColor(faceBackgroundColor);
            renderFace(batch);
        } else {
            if(backSprite == null) {
                loadBackSprite();
            }
            renderBack(batch);
        }
    }

    private void renderFace(SpriteBatch batch) {
        batch.begin();

        thisCardFaceSprite.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        thisCardFaceSprite.setPosition(cardRect.x + (0.5f * scale * (CARD_WIDTH - (CARD_WIDTH))), cardRect.y + (0.5f * scale * (CARD_HEIGHT - (CARD_HEIGHT))));
        thisCardFaceSprite.draw(batch);

        batch.end();
    }

    private void renderBack(SpriteBatch batch) {
        batch.begin();
        backSprite.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        backSprite.setPosition(cardRect.x, cardRect.y);
        backSprite.draw(batch);
        batch.end();
    }

    @SuppressWarnings("unused")
    boolean containsPoint(Vector2 point) {
        return containsPoint(point.x, point.y);
    }

    @SuppressWarnings("WeakerAccess")
    boolean containsPoint(float x, float y) {
        return cardRect.contains(x, y) || Arrays.stream(cornerCircles).anyMatch(circle -> circle.contains(x, y));
    }

    RenderableCard setFaceColor(Color c) {
        faceBackgroundColor = c;
        thisCardFaceSprite = null;
        return this;
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

    Color getFaceColor() {
        return faceBackgroundColor;
    }

    float getScale() {
        return scale;
    }

    Vector2 getPosition() {
        return cardRect.getPosition(new Vector2());
    }

    boolean isFaceUp() {
        return faceUp;
    }
}
