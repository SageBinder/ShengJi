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
    @SuppressWarnings("WeakerAccess")
    static final int CARD_HEIGHT_IN_PIXELS = 350;
    @SuppressWarnings("WeakerAccess")
    static final int CARD_WIDTH_IN_PIXELS = 225;
    @SuppressWarnings("WeakerAccess")
    static final float CARD_WIDTH = TABLE_WORLD_SIZE / 5f;
    @SuppressWarnings("WeakerAccess")
    static final float CARD_HEIGHT = ((float) CARD_HEIGHT_IN_PIXELS / (float) CARD_WIDTH_IN_PIXELS) * CARD_WIDTH;

    // Default variable values:
    @SuppressWarnings("WeakerAccess")
    static final float defaultCornerRadiusScale = 0.075f;
    static final float defaultCornerRadius = defaultCornerRadiusScale * CARD_WIDTH;

    static final int defaultFaceBorderThicknessInPixels = (int)(0.015f * CARD_WIDTH_IN_PIXELS);
    @SuppressWarnings("WeakerAccess")
    static final int defaultBackBorderThicknessInPixels = (int)(defaultCornerRadiusScale * CARD_WIDTH_IN_PIXELS);

    static final float defaultFaceDesignHeightScale = 0.95f;
    static final float defaultFaceDesignWidthScale = 0.95f;

    static final float defaultBackDesignHeightScale = ((float)CARD_HEIGHT_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_HEIGHT_IN_PIXELS;
    static final float defaultBackDesignWidthScale = ((float)CARD_WIDTH_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_WIDTH_IN_PIXELS;

    static final float defaultScale = 1f;

    static final Color defaultFaceBorderColor = new Color(0, 0, 0, 1);
    static final Color defaultBackBorderColor = new Color(1, 1, 1, 1);

    static final Color defaultFaceBackgroundColor = new Color(1, 1, 1, 1);

    // Member variables:
    private float cornerRadiusScale = 0.075f;
    private float cornerRadius = cornerRadiusScale * CARD_WIDTH;

    @SuppressWarnings("FieldCanBeLocal")
    private int faceBorderThicknessInPixels = (int)(0.015f * CARD_WIDTH_IN_PIXELS);
    @SuppressWarnings("FieldCanBeLocal")
    private int backBorderThicknessInPixels = (int)(cornerRadiusScale * CARD_WIDTH_IN_PIXELS);

    @SuppressWarnings("FieldCanBeLocal")
    private float faceDesignHeightScale = 0.95f; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle
    @SuppressWarnings("FieldCanBeLocal")
    private float faceDesignWidthScale = 0.95f;

    private float backDesignHeightScale = ((float)CARD_HEIGHT_IN_PIXELS - (2 * (float)backBorderThicknessInPixels)) / (float)CARD_HEIGHT_IN_PIXELS;
    private float backDesignWidthScale = ((float)CARD_WIDTH_IN_PIXELS - (2 * (float)backBorderThicknessInPixels)) / (float)CARD_WIDTH_IN_PIXELS;

    private float scale = 1f; // Overall scale of the card with respect to the world

    private Color faceBorderColor = new Color(0, 0, 0, 1);
    private Color backBorderColor = new Color(1, 1, 1, 1);

    private Color faceBackgroundColor = new Color(1, 1, 1, 1);

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

    private static FileHandle defaultSpriteFolder = Gdx.files.internal("playing_cards/");
    private static FileHandle spriteFolder = defaultSpriteFolder;

    private static HashMap<Integer, Pixmap> faceDesignPixmaps = new HashMap<>();
    private static Pixmap backPixmap = null;

    private Sprite thisCardBackSprite = null;
    private Sprite thisCardFaceSprite = null;

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
        resetPixmaps();
    }

    static void useDefaultSpriteFolder() {
        spriteFolder = defaultSpriteFolder;
        resetPixmaps();
    }

    private static void resetPixmaps() {
        resetBackPixmap();
        resetFaceDesignPixmaps();
    }

    private static void resetBackPixmap() {
        backPixmap.dispose();
        backPixmap = null;
    }

    private static void resetFaceDesignPixmaps() {
        for(Pixmap p : faceDesignPixmaps.values()) {
            p.dispose();
        }
        faceDesignPixmaps.clear();
    }

    private static void loadBackPixmap() {
        Pixmap originalImagePixmap = new Pixmap(spriteFolder.child("back.png"));
        backPixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalImagePixmap.getFormat());
        backPixmap.drawPixmap(originalImagePixmap,
                0, 0, originalImagePixmap.getWidth(), originalImagePixmap.getHeight(),
                0, 0, backPixmap.getWidth(), backPixmap.getHeight());
    }

    private static void loadFaceDesignPixmapForCard(int cardNum) {
        String cardImageName;

        Suit suit = Card.getSuitFromCardNum(cardNum);
        Rank rank = Card.getRankFromCardNum(cardNum);

        if(!suit.isJoker()) {
            cardImageName = rank.toStringName() + "_of_" + suit.toString() + ".png";
        } else {
            cardImageName = suit.toString() + ".png";
        }

        Pixmap originalImagePixmap = new Pixmap(spriteFolder.child(cardImageName));
        Pixmap resizedImagePixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalImagePixmap.getFormat());
        resizedImagePixmap.drawPixmap(originalImagePixmap,
                0, 0, originalImagePixmap.getWidth(), originalImagePixmap.getHeight(),
                0, 0, resizedImagePixmap.getWidth(), resizedImagePixmap.getHeight());
//
//        resizedImagePixmap.drawPixmap(originalImagePixmap,
//                0, 0, originalImagePixmap.getWidth(), originalImagePixmap.getHeight(),
//                (int)(0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * faceDesignWidthScale))), (int)(0.5f * (CARD_HEIGHT_IN_PIXELS - (CARD_HEIGHT_IN_PIXELS * faceDesignHeightScale))),
//                (int)(CARD_WIDTH_IN_PIXELS * faceDesignWidthScale), (int)(CARD_HEIGHT_IN_PIXELS * faceDesignHeightScale));
//
//        drawCurvedBorderOnPixmap(resizedImagePixmap,
//                (int)((cornerRadius / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS),
//                faceBorderThicknessInPixels,
//                faceBorderColor);

        faceDesignPixmaps.put(cardNum, resizedImagePixmap);

        originalImagePixmap.dispose();
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
        resetPixmaps();
    }

    private void updateShapes(Vector2 newPosition, float newScale) {
        this.scale = newScale;
        float scaledCornerRadius = cornerRadius * scale;

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
            loadFaceDesignPixmapForCard(cardNum());
        }

        Pixmap thisCardFaceSpritePixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, Pixmap.Format.RGBA8888);
        Pixmap faceDesignPixmap = faceDesignPixmaps.get(cardNum());

        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        thisCardFaceSpritePixmap.setColor(faceBackgroundColor);
        thisCardFaceSpritePixmap.fill();

        roundPixmapCorners(thisCardFaceSpritePixmap, (int)((cornerRadius / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS));
        drawCurvedBorderOnPixmap(thisCardFaceSpritePixmap,
                (int)((cornerRadius / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS),
                faceBorderThicknessInPixels,
                faceBorderColor);

        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        thisCardFaceSpritePixmap.drawPixmap(faceDesignPixmap,
                0, 0, faceDesignPixmap.getWidth(), faceDesignPixmap.getHeight(),
                (int)(0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * faceDesignWidthScale))), (int)(0.5f * (CARD_HEIGHT_IN_PIXELS - (CARD_HEIGHT_IN_PIXELS * faceDesignHeightScale))),
                (int)(CARD_WIDTH_IN_PIXELS * faceDesignWidthScale), (int)(CARD_HEIGHT_IN_PIXELS * faceDesignHeightScale));
//        thisCardFaceSpritePixmap.drawPixmap(faceDesignPixmap,
//                0, 0, faceDesignPixmap.getWidth(), faceDesignPixmap.getHeight(),
//                0, 0, thisCardFaceSpritePixmap.getWidth(), thisCardFaceSpritePixmap.getHeight());

        thisCardFaceSprite = new Sprite(new Texture(thisCardFaceSpritePixmap));

        thisCardFaceSpritePixmap.dispose();
    }

    private void setupThisCardBackSprite() {
        Pixmap originalBackPixmap = new Pixmap(spriteFolder.child("back.png"));
        Pixmap resizedBackPixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalBackPixmap.getFormat());

//        resizedBackPixmap.drawPixmap(originalBackPixmap,
//                0, 0, originalBackPixmap.getWidth(), originalBackPixmap.getHeight(),
//                0, 0, resizedBackPixmap.getWidth(), resizedBackPixmap.getHeight());

        resizedBackPixmap.drawPixmap(originalBackPixmap,
                0, 0,
                originalBackPixmap.getWidth(), originalBackPixmap.getHeight(),
                (int)(0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * backDesignWidthScale))), (int)(0.5f * (CARD_HEIGHT_IN_PIXELS - (CARD_HEIGHT_IN_PIXELS * backDesignHeightScale))),
                (int)(CARD_WIDTH_IN_PIXELS * backDesignWidthScale), (int)(CARD_HEIGHT_IN_PIXELS * backDesignHeightScale));

        int cornerRadiusInPixels = (int)((cornerRadius / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS);
        roundPixmapCorners(resizedBackPixmap, cornerRadiusInPixels);
        drawCurvedBorderOnPixmap(resizedBackPixmap,
                cornerRadiusInPixels,
                backBorderThicknessInPixels,
                backBorderColor);
        thisCardBackSprite = new Sprite(new Texture(resizedBackPixmap));

        originalBackPixmap.dispose();
        resizedBackPixmap.dispose();
    }

    void render(SpriteBatch batch) {
        if(faceUp) {
            if(thisCardFaceSprite == null) {
                setupThisCardFaceSprite();
            }
            renderFace(batch);
        } else {
            if(thisCardBackSprite == null) {
                setupThisCardBackSprite();
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
        thisCardBackSprite.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        thisCardBackSprite.setPosition(cardRect.x, cardRect.y);
        thisCardBackSprite.draw(batch);
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

    // Face value setters:
    RenderableCard setFaceDesignScale(float scale) {
        faceDesignHeightScale = scale;
        faceDesignWidthScale = scale;
        thisCardFaceSprite = null;
        return this;
    }

    RenderableCard setFaceDesignHeightScale(float scale) {
        faceDesignHeightScale = scale;
        thisCardFaceSprite = null;
        return this;
    }

    RenderableCard setFaceDesignWidthScale(float scale) {
        faceDesignWidthScale = scale;
        thisCardFaceSprite = null;
        return this;
    }

    RenderableCard setFaceBackgroundColor(Color faceBackgroundColor) {
        this.faceBackgroundColor = faceBackgroundColor;
        thisCardFaceSprite = null;
        return this;
    }

    RenderableCard setFaceBorderColor(Color faceBorderColor) {
        this.faceBorderColor = faceBorderColor;
        thisCardFaceSprite = null;
        return this;
    }

    RenderableCard setFaceBorderThicknessRelativeToHeight(float borderScale) {
        faceBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        thisCardFaceSprite = null;
        return this;
    }

    RenderableCard setFaceBorderThicknessRelativeToWidth(float borderScale) {
        faceBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        thisCardFaceSprite = null;
        return this;
    }

    RenderableCard setFaceBorderThicknessInPixels(int faceBorderThicknessInPixels) {
        this.faceBorderThicknessInPixels = faceBorderThicknessInPixels;
        thisCardFaceSprite = null;
        return this;
    }

    // Back value setters:
    RenderableCard setBackDesignScale(float scale) {
        backDesignHeightScale = scale;
        backDesignWidthScale = scale;
        thisCardBackSprite = null;
        return this;
    }

    RenderableCard setBackDesignHeightScale(float backDesignHeightScale) {
        this.backDesignHeightScale = backDesignHeightScale;
        thisCardBackSprite = null;
        return this;
    }

    RenderableCard setBackDesignWidthScale(float backDesignWidthScale) {
        this.backDesignWidthScale = backDesignWidthScale;
        thisCardBackSprite = null;
        return this;
    }

    RenderableCard setBackBorderColor(Color backBorderColor) {
        this.backBorderColor = backBorderColor;
        thisCardBackSprite = null;
        return this;
    }

    RenderableCard setBackBorderThicknessRelativeToHeight(float borderScale) {
        backBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        thisCardBackSprite = null;
        return this;
    }

    RenderableCard setBackBorderThicknessRelativeToWidth(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        thisCardBackSprite = null;
        return this;
    }

    RenderableCard setBackBorderThicknessInPixels(int backBorderThicknessInPixels) {
        this.backBorderThicknessInPixels = backBorderThicknessInPixels;
        thisCardBackSprite = null;
        return this;
    }

    // Shared value setters:
    RenderableCard setCornerRadiusScale(float cornerRadiusScale) {
        this.cornerRadiusScale = cornerRadiusScale;
        cornerRadius = cornerRadiusScale * CARD_WIDTH;
        thisCardFaceSprite = null;
        thisCardBackSprite = null;
        return this;
    }

    RenderableCard setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        return this;
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

    RenderableCard setPosition(float x, float y) {
        return setPosition(new Vector2(x, y));
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
