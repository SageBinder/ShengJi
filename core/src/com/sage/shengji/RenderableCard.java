package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import java.util.HashMap;

class RenderableCard extends AbstractRenderableCard<RenderableCard> {
    static final int CARD_HEIGHT_IN_PIXELS = 350;
    static final int CARD_WIDTH_IN_PIXELS = 225;
    static final float HEIGHT_TO_WIDTH_RATIO = (float)CARD_HEIGHT_IN_PIXELS / (float)CARD_WIDTH_IN_PIXELS;
    static final float WIDTH_TO_HEIGHT_RATIO = (float)CARD_WIDTH_IN_PIXELS / (float)CARD_HEIGHT_IN_PIXELS;

    private static FileHandle defaultSpriteFolder = Gdx.files.internal("playing_cards/");
    private static FileHandle spriteFolder = defaultSpriteFolder;

    private static HashMap<Integer, Pixmap> faceDesignPixmaps = new HashMap<>();
    private static Pixmap backPixmap = null;

    private Sprite thisCardBackSprite = null;
    private Sprite thisCardFaceSprite = null;

    private Rank lastKnownTrumpRank;
    private Suit lastKnownTrumpSuit;

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
        originalImagePixmap.dispose();
    }

    private static void loadFaceDesignPixmapForCard(int cardNum) {
        String cardImageName;

        Suit suit = Card.getSuitFromCardNum(cardNum);
        Rank rank = Card.getRankFromCardNum(cardNum);

        if(!suit.isJoker()) {
            cardImageName = rank.toString() + "_of_" + suit.toString() + ".png";
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

    @SuppressWarnings("ConstantConditions")
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
                        int circleCenter_y = i == 0 ? radius : pixmapHeight - radius;
                        int circleCenter_x = j == 0 ? radius : pixmapWidth - radius;
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
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
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
                        int circleCenter_y = i == 0 ? radius : pixmapHeight - radius;
                        int circleCenter_x = j == 0 ? radius : pixmapWidth - radius;
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
        thisCardFaceSprite = setupSpriteFromPixmap(faceDesignPixmaps.get(cardNum()),
                getFaceBackgroundColor(),
                getFaceDesignWidthScale(), getFaceDesignHeightScale(),
                getFaceBorderThicknessInPixels(), getFaceBorderColor());
    }

    private void setupThisCardBackSprite() {
        if(backPixmap == null) {
            loadBackPixmap();
        }
        thisCardBackSprite = setupSpriteFromPixmap(backPixmap, getBackBackgroundColor(),
                getBackDesignWidthScale(), getBackDesignHeightScale(),
                getBackBorderThicknessInPixels(), getBackBorderColor());
    }

    private Sprite setupSpriteFromPixmap(Pixmap designPixmap,
                                         Color backgroundColor,
                                         float designWidthScale, float designHeightScale,
                                         int borderThicknessInPixels, Color borderColor) {
        Pixmap spritePixmap = new Pixmap(designPixmap.getWidth(), designPixmap.getHeight(), designPixmap.getFormat());

        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        spritePixmap.setColor(backgroundColor);
        spritePixmap.fill();

        spritePixmap.drawPixmap(designPixmap,
                0, 0, designPixmap.getWidth(), designPixmap.getHeight(),
                (int) (0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * designWidthScale))), (int) (0.5f * (CARD_HEIGHT_IN_PIXELS - (CARD_HEIGHT_IN_PIXELS * designHeightScale))),
                (int) (CARD_WIDTH_IN_PIXELS * designWidthScale), (int) (CARD_HEIGHT_IN_PIXELS * designHeightScale));

        roundPixmapCorners(spritePixmap, getCornerRadiusInPixels());
        drawCurvedBorderOnPixmap(spritePixmap,
                getCornerRadiusInPixels(),
                borderThicknessInPixels,
                borderColor);

        Texture spriteTexture = new Texture(spritePixmap);

        Sprite sprite = new Sprite(spriteTexture);
        sprite.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sprite.setSize(getWidth(), getHeight());

        spritePixmap.dispose();
        return sprite;
    }

    @Override
    void render(SpriteBatch batch, Viewport viewport) {
        renderAt(batch, viewport, getDisplayX(), getDisplayY(), getDisplayWidth(), getDisplayHeight());
    }

    void renderBase(SpriteBatch batch, Viewport viewport) {
        renderAt(batch, viewport, getX(),  getY(), getWidth(), getHeight());
    }

    void renderAt(SpriteBatch batch, Viewport viewport, float x, float y, float width, float height) {
        // If trump suit/rank has changed, check whether or not this card is a trump and set border color accordingly
        if(GameState.trumpSuit != lastKnownTrumpSuit || GameState.trumpRank != lastKnownTrumpRank) {
            lastKnownTrumpSuit = GameState.trumpSuit;
            lastKnownTrumpRank = GameState.trumpRank;

            // setFaceBorderColor and setBackBorderColor both automatically call invalidateSprites().
            // Maybe better to call invalidateSprites manually?
            if(isTrump()) {
                setFaceBorderColor(defaultTrumpFaceBorderColor);
                setBackBorderColor(defaultTrumpBackBorderColor);
            }
        }

        if(isFaceUp()) {
            if(thisCardFaceSprite == null) {
                setupThisCardFaceSprite();
            }
            renderFace(batch, viewport, x, y, width, height);
        } else {
            if(thisCardBackSprite == null) {
                setupThisCardBackSprite();
            }
            renderBack(batch, viewport, x, y, width, height);
        }
    }

    private void renderFace(SpriteBatch batch, Viewport viewport,
                            float x, float y, float width, float height) {
        drawSprite(batch, viewport, thisCardFaceSprite, x, y, width, height);
    }

    private void renderBack(SpriteBatch batch, Viewport viewport,
                            float x, float y, float width, float height) {
        drawSprite(batch, viewport, thisCardBackSprite, x, y, width, height);
    }

    private void drawSprite(SpriteBatch batch, Viewport viewport, Sprite sprite,
                            float x, float y, float width, float height) {
        // TODO: Maybe this rounding should only be done when position changes, but I'm too lazy to do that right now

        // vecXY is initialized with world coordinates for card position
        Vector2 vecXY = new Vector2(x, y);

        // vecXY is then projected onto the screen, so now it represents the *screen* coordinates of the card
        viewport.project(vecXY);

        // viewport.project doesn't seem to account for the fact that with screen coordinates, y starts from the top of
        // the screen, so this line accounts for that
        vecXY.y = Gdx.graphics.getHeight() - vecXY.y;

        // Round vecXY so that the card's position isn't in between two pixels
        vecXY.x = MathUtils.round(vecXY.x);
        vecXY.y = MathUtils.round(vecXY.y);

        // Unproject vecXY back to world coordinates, so now we know the world coordinates of the card will be projected
        // to a whole pixel value, and thus the card's sprite won't have any weird subpixel stretching going on
        viewport.unproject(vecXY);

        sprite.setBounds(vecXY.x, vecXY.y, width, height);
        sprite.draw(batch);
    }

    @Override
    void invalidateSprites() {
        if(thisCardFaceSprite != null) {
            thisCardFaceSprite.getTexture().dispose();
        }
        thisCardFaceSprite = null;

        if(thisCardBackSprite != null) {
            thisCardBackSprite.getTexture().dispose();
        }
        thisCardBackSprite = null;
    }

    // finalize necessary?
    @Override
    public void finalize() {
        thisCardFaceSprite.getTexture().dispose();
        thisCardBackSprite.getTexture().dispose();
    }
}
