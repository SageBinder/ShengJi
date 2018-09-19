package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import java.util.HashMap;

import static com.sage.shengji.TableScreen.TABLE_WORLD_SIZE;

class RenderableCard extends AbstractRenderableCard<RenderableCard> {
    @SuppressWarnings("WeakerAccess")
    static final int CARD_HEIGHT_IN_PIXELS = 350;
    @SuppressWarnings("WeakerAccess")
    static final int CARD_WIDTH_IN_PIXELS = 225;
    @SuppressWarnings("WeakerAccess")
    static final float CARD_WIDTH = TABLE_WORLD_SIZE / 5f;
    @SuppressWarnings("WeakerAccess")
    static final float CARD_HEIGHT = ((float) CARD_HEIGHT_IN_PIXELS / (float) CARD_WIDTH_IN_PIXELS) * CARD_WIDTH;

    private static FileHandle defaultSpriteFolder = Gdx.files.internal("playing_cards/");
    private static FileHandle spriteFolder = defaultSpriteFolder;

    private static HashMap<Integer, Pixmap> faceDesignPixmaps = new HashMap<>();
    private static Pixmap backPixmap = null;

    private Sprite thisCardBackSprite = null;
    private Sprite thisCardFaceSprite = null;

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
                        double distance = Math.sqrt(Math.pow(x - circleCenter_x, 2) + Math.pow(y - circleCenter_y, 2));

                        // Using (<= and >=) as opposed to (< and >) doesn't seem to make any visual difference
                        if(((i == 0 && y <= circleCenter_y) || (i == 1 && y >= circleCenter_y))
                                && ((j == 0 && x <= circleCenter_x) || (j == 1 && x >= circleCenter_x))
                                && distance >= radius) {
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

        // This code is almost the exact same as the code in roundPixmapCorners()
        for(int x = 0; x < pixmapWidth; x++) {
            nextIter:
            for(int y = 0; y < pixmapHeight; y++) {
                // These two innermost loops check conditions for adding a border pixel for each of the four corners
                for(int i = 0; i < 2; i++) {
                    for(int j = 0; j < 2; j++) {
                        // Top left corner: i == 0, j == 0
                        // Bottom left corner: i == 1, j == 0
                        // Top right corner: i == 0, j == 1
                        // Bottom right corner: i == 1, j == 1
                        int circleCenter_y = (pixmapHeight * i) - (radius * (-1 + (i * 2)));
                        int circleCenter_x = (pixmapWidth * j) - (radius * (-1 + (j * 2)));
                        double distance = Math.sqrt(Math.pow(x - circleCenter_x, 2) + Math.pow(y - circleCenter_y, 2));

                        // Using (<= and >=) as opposed to (< and >) doesn't seem to make any visual difference
                        if(((i == 0 && y <= circleCenter_y) || (i == 1 && y >= circleCenter_y))
                                && ((j == 0 && x <= circleCenter_x) || (j == 1 && x >= circleCenter_x))
                                && distance <= radius
                                && distance >= radius - borderThickness) {
                            pixmap.drawPixel(x, y, Color.rgba8888(color));

                            // Since it was determined that pixel (x, y) is a border pixel,
                            // the rest of the conditions shouldn't be checked, so exit the two innermost loops.
                            continue nextIter;
                        }
                    }
                }
            }
        }
    }

    static void dispose() {
        resetPixmaps();
    }

    private void setupThisCardFaceSprite() {
        if(faceDesignPixmaps.get(cardNum()) == null) {
            loadFaceDesignPixmapForCard(cardNum());
        }

        Pixmap resizedFacePixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, Pixmap.Format.RGBA8888);
        Pixmap faceDesignPixmap = faceDesignPixmaps.get(cardNum());

        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        resizedFacePixmap.setColor(getFaceBackgroundColor());
        resizedFacePixmap.fill();

        roundPixmapCorners(resizedFacePixmap, (int) ((getCornerRadius() / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS));
        drawCurvedBorderOnPixmap(resizedFacePixmap,
                (int) ((getCornerRadius() / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS),
                getFaceBorderThicknessInPixels(),
                getFaceBorderColor());

        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        resizedFacePixmap.drawPixmap(faceDesignPixmap,
                0, 0, faceDesignPixmap.getWidth(), faceDesignPixmap.getHeight(),
                (int) (0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * getFaceDesignWidthScale()))), (int) (0.5f * (CARD_HEIGHT_IN_PIXELS - (CARD_HEIGHT_IN_PIXELS * getFaceDesignHeightScale()))),
                (int) (CARD_WIDTH_IN_PIXELS * getFaceDesignWidthScale()), (int) (CARD_HEIGHT_IN_PIXELS * getFaceDesignHeightScale()));

        thisCardFaceSprite = new Sprite(new Texture(resizedFacePixmap));

        resizedFacePixmap.dispose();
    }

    private void setupThisCardBackSprite() {
        if(backPixmap == null) {
            loadBackPixmap();
        }

        Pixmap resizedBackPixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, backPixmap.getFormat());

        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        resizedBackPixmap.setColor(getBackBackgroundColor());
        resizedBackPixmap.fill();

        resizedBackPixmap.drawPixmap(backPixmap,
                0, 0,
                backPixmap.getWidth(), backPixmap.getHeight(),
                (int) (0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * getBackDesignWidthScale()))), (int) (0.5f * (CARD_HEIGHT_IN_PIXELS - (CARD_HEIGHT_IN_PIXELS * getBackDesignHeightScale()))),
                (int) (CARD_WIDTH_IN_PIXELS * getBackDesignWidthScale()), (int) (CARD_HEIGHT_IN_PIXELS * getBackDesignHeightScale()));

        int cornerRadiusInPixels = (int) ((getCornerRadius() / CARD_WIDTH) * CARD_WIDTH_IN_PIXELS);
        roundPixmapCorners(resizedBackPixmap, cornerRadiusInPixels);
        drawCurvedBorderOnPixmap(resizedBackPixmap,
                cornerRadiusInPixels,
                getBackBorderThicknessInPixels(),
                getBackBorderColor());
        thisCardBackSprite = new Sprite(new Texture(resizedBackPixmap));

        resizedBackPixmap.dispose();
    }

    void render(SpriteBatch batch) {
        if(isFaceUp()) {
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

        thisCardFaceSprite.setSize(getWidth(), getHeight());
        thisCardFaceSprite.setPosition(getX(),
                getY() + ((isSelected() ? 1 : 0) * getHeightChangeOnSelect() * getScale()));
        thisCardFaceSprite.draw(batch);

        batch.end();
    }

    private void renderBack(SpriteBatch batch) {
        batch.begin();
        thisCardBackSprite.setSize(CARD_WIDTH * getScale(), CARD_HEIGHT * getScale());
        thisCardBackSprite.setPosition(getX(),
                getY() + ((isSelected() ? 1 : 0) * getHeightChangeOnSelect() * getScale()));
        thisCardBackSprite.draw(batch);
        batch.end();
    }

    void displayParametersChanged() {
        thisCardFaceSprite = null;
        thisCardBackSprite = null;
    }
}
