package com.sage.shengji;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import static com.sage.shengji.RenderableCard.*;

@SuppressWarnings("unchecked")
abstract class AbstractRenderableCard<T extends AbstractRenderableCard<T>> extends Card {
    // Default variable values:
    static final int defaultCornerRadiusInPixels = (int)(0.075f * CARD_WIDTH_IN_PIXELS);

    static final int defaultFaceBorderThicknessInPixels = (int)(0.018f * CARD_WIDTH_IN_PIXELS);
    static final int defaultBackBorderThicknessInPixels = defaultCornerRadiusInPixels;

    static final float defaultFaceDesignHeightScale = 0.95f;
    static final float defaultFaceDesignWidthScale = 0.95f;

    static final float defaultBackDesignHeightScale = ((float)CARD_HEIGHT_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_HEIGHT_IN_PIXELS;
    static final float defaultBackDesignWidthScale = ((float)CARD_WIDTH_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_WIDTH_IN_PIXELS;

    static final float defaultHeightChangeOnSelect = 0.9f; // Relative to card height

    static final Color defaultFaceBorderColor = new Color(0, 0, 0, 1);
    static final Color defaultBackBorderColor = new Color(1, 1, 1, 1);

    static final Color defaultTrumpFaceBorderColor = new Color(0.75f, 0.75f, 0f, 1f);
    static final Color defaultTrumpBackBorderColor = new Color(defaultBackBorderColor);

    static final Color defaultFaceUnselectedBackgroundColor = new Color(1, 1, 1, 1);
    static final Color defaultBackUnselectedBackgroundColor = new Color(0, 0, 0, 1);

    static final Color defaultFaceSelectedBackgroundColor = new Color(defaultFaceUnselectedBackgroundColor).sub(0.5f, 0.5f, 0.5f, 0);
    static final Color defaultBackSelectedBackgroundColor = new Color(defaultBackUnselectedBackgroundColor);

    static final Color defaultFaceHighlightedBackgroundColor = new Color(1.0f, 1.0f, 0.5f, 1.0f);
    static final Color defaultBackHighlightedBackgroundColor = new Color(defaultBackUnselectedBackgroundColor);

    // Member variables:
    private int cornerRadiusInPixels = defaultCornerRadiusInPixels;

    private int faceBorderThicknessInPixels = defaultFaceBorderThicknessInPixels;
    private int backBorderThicknessInPixels = defaultBackBorderThicknessInPixels;

    private float faceDesignHeightScale = defaultFaceDesignHeightScale; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle
    private float faceDesignWidthScale = defaultFaceDesignWidthScale;

    private float backDesignHeightScale = defaultBackDesignHeightScale;
    private float backDesignWidthScale = defaultBackDesignWidthScale;

    private float heightChangeOnSelect = defaultHeightChangeOnSelect;
    
    private final Color faceBorderColor = new Color(defaultFaceBorderColor);
    private final Color backBorderColor = new Color(defaultBackBorderColor);

    private final Color faceBackgroundColor = new Color(defaultFaceUnselectedBackgroundColor);
    private final Color backBackgroundColor = new Color(defaultBackUnselectedBackgroundColor);

    final Color faceUnselectedBackgroundColor = new Color(defaultFaceUnselectedBackgroundColor);
    final Color backUnselectedBackgroundColor = new Color(defaultBackUnselectedBackgroundColor);

    final Color faceSelectedBackgroundColor = new Color(defaultFaceSelectedBackgroundColor);
    final Color backSelectedBackgroundColor = new Color(defaultBackSelectedBackgroundColor);

    final Color faceHighlightedBackgroundColor = new Color(defaultFaceHighlightedBackgroundColor);
    final Color backHighlightedBackgroundColor = new Color(defaultBackHighlightedBackgroundColor);

    // baseCardRect represents overall rectangle before rounding corners
    private final Rectangle baseCardRect = new Rectangle(0, 0, CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS);
    private final Rectangle displayCardRect = new Rectangle(baseCardRect);

    private boolean moveDisplayWithBase = true;

    private boolean faceUp = true;
    private boolean isSelected = false;

    private boolean selectable = true;
    private boolean flippable = true;

    AbstractRenderableCard(Rank rank, Suit suit) {
        super(rank, suit);
    }

    AbstractRenderableCard(Card c) {
        super(c);
    }

    AbstractRenderableCard(int cardNum) {
        super(cardNum);
    }

    AbstractRenderableCard() {
        super();
    }

    abstract void invalidateSprites();

    abstract void render(SpriteBatch batch, Viewport viewport);

    boolean baseRectContainsPoint(Vector2 point) {
        return baseRectContainsPoint(point.x, point.y);
    }

    boolean baseRectContainsPoint(float x, float y) {
        return baseCardRect.contains(x, y);
    }

    boolean displayRectContainsPoint(Vector2 point) {
        return displayRectContainsPoint(point.x, point.y);
    }

    boolean displayRectContainsPoint(float x, float y) {
        return displayCardRect.contains(x, y);
    }

    boolean displayRectEqualsBaseRect() {
        return displayCardRect.equals(baseCardRect);
    }

    // invalidateSprites() is automatically called after a display parameter is set. Maybe it's better to manually call
    // invalidateSprites() after setting necessary display parameters?

    // --- SETTERS ---
    // Face value setters:
    T setFaceDesignScale(float scale) {
        faceDesignHeightScale = scale;
        faceDesignWidthScale = scale;
        invalidateSprites();
        return (T) this;
    }

    T setFaceDesignHeightScale(float scale) {
        this.faceDesignHeightScale = scale;
        invalidateSprites();
        return (T) this;
    }

    T setFaceDesignWidthScale(float scale) {
        this.faceDesignWidthScale = scale;
        invalidateSprites();
        return (T) this;
    }

    T setFaceBackgroundColor(Color faceBackgroundColor) {
        this.faceBackgroundColor.set(faceBackgroundColor);
        invalidateSprites();
        return (T) this;
    }

    T resetFaceBackgroundColor() {
        if(isSelected) {
            setFaceBackgroundColor(faceSelectedBackgroundColor);
        } else {
            setFaceBackgroundColor(faceUnselectedBackgroundColor);
        }
        return (T) this;
    }

    T setFaceBorderColor(Color faceBorderColor) {
        this.faceBorderColor.set(faceBorderColor);
        invalidateSprites();
        return (T) this;
    }

    T resetFaceBorderColor() {
        if(isTrump()) {
            setFaceBorderColor(defaultTrumpFaceBorderColor);
        } else {
            setFaceBorderColor(defaultFaceBorderColor);
        }
        return (T) this;
    }

    T setFaceBorderThicknessRelativeToHeight(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    T setFaceBorderThicknessRelativeToWidth(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    T setFaceBorderThicknessInPixels(int faceBorderThicknessInPixels) {
        this.faceBorderThicknessInPixels = faceBorderThicknessInPixels;
        invalidateSprites();
        return (T) this;
    }

    // Back value setters:
    T setBackDesignScale(float scale) {
        backDesignHeightScale = scale;
        backDesignWidthScale = scale;
        invalidateSprites();
        return (T) this;
    }

    T setBackDesignHeightScale(float backDesignHeightScale) {
        this.backDesignHeightScale = backDesignHeightScale;
        invalidateSprites();
        return (T) this;
    }

    T setBackDesignWidthScale(float backDesignWidthScale) {
        this.backDesignWidthScale = backDesignWidthScale;
        invalidateSprites();
        return (T) this;
    }

    T setBackBackgroundColor(Color backBackgroundColor) {
        this.backBackgroundColor.set(backBackgroundColor);
        return (T) this;
    }

    T resetBackBackgroundColor() {
        if(isSelected) {
            setBackBackgroundColor(backSelectedBackgroundColor);
        } else {
            setBackBackgroundColor(backUnselectedBackgroundColor);
        }
        return (T) this;
    }

    T setBackBorderColor(Color backBorderColor) {
        this.backBorderColor.set(backBorderColor);
        invalidateSprites();
        return (T) this;
    }

    T resetBackBorderColor() {
        if(isTrump()) {
            setBackBorderColor(defaultTrumpBackBorderColor);
        } else {
            setBackBorderColor(defaultBackBorderColor);
        }
        return (T) this;
    }

    T setBackBorderThicknessRelativeToHeight(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    T setBackBorderThicknessRelativeToWidth(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    T setBackBorderThicknessInPixels(int backBorderThicknessInPixels) {
        this.backBorderThicknessInPixels = backBorderThicknessInPixels;
        invalidateSprites();
        return (T) this;
    }

    // General value setters:
    T setBothBackgroundColors(Color newColor) {
        setFaceBackgroundColor(newColor);
        setBackBackgroundColor(newColor);
        return (T) this;
    }

    T setBothBorderColors(Color newColor) {
        setFaceBorderColor(newColor);
        setBackBorderColor(newColor);
        return (T) this;
    }

    T resetBothBackgroundColors() {
        resetFaceBackgroundColor();
        resetBackBackgroundColor();
        return (T) this;
    }

    T resetBothBorderColors() {
        resetFaceBorderColor();
        resetBackBorderColor();
        return (T) this;
    }

    T setCornerRadiusRelativeToWidth(float scale) {
        cornerRadiusInPixels = (int)(scale * CARD_WIDTH_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    T setCornerRadiusRelativeToHeight(float scale) {
        cornerRadiusInPixels = (int)(scale * CARD_HEIGHT_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    T scale(float newScale) {
        setWidth(getWidth() * newScale);
        return (T) this;
    }

    T setPosition(Vector2 newPosition) {
        return setPosition(newPosition.x, newPosition.y);
    }

    T setPosition(float x, float y) {
        setX(x);
        setY(y);
        return (T) this;
    }

    T setX(float x) {
        float change = x - baseCardRect.x;
        baseCardRect.x += change;
        if(moveDisplayWithBase) displayCardRect.x += change;

        return (T) this;
    }

    T setY(float y) {
        float change = y - baseCardRect.y;
        baseCardRect.y += change;
        if(moveDisplayWithBase) displayCardRect.y += change;

        return (T) this;
    }

    T setWidth(float width) {
        float widthChangeProportion = width / baseCardRect.width;

        baseCardRect.width *= widthChangeProportion;
        baseCardRect.height = HEIGHT_TO_WIDTH_RATIO * baseCardRect.width;

        if(moveDisplayWithBase) {
            displayCardRect.width *= widthChangeProportion;
            displayCardRect.height = HEIGHT_TO_WIDTH_RATIO * displayCardRect.width;
        }

        return (T) this;
    }

    T setHeight(float height) {
        float heightChangeProportion = height / baseCardRect.height;

        baseCardRect.height *= heightChangeProportion;
        baseCardRect.width = WIDTH_TO_HEIGHT_RATIO * baseCardRect.height;

        if(moveDisplayWithBase) {
            displayCardRect.height *= heightChangeProportion;
            displayCardRect.width = WIDTH_TO_HEIGHT_RATIO * displayCardRect.height;
        }

        return (T) this;
    }

    T setDisplayPosition(Vector2 newPosition) {
        return setDisplayPosition(newPosition.x, newPosition.y);
    }

    T setDisplayPosition(float x, float y) {
        displayCardRect.setPosition(x, y);
        return (T) this;
    }

    T setDisplayX(float x) {
        displayCardRect.x = x;
        return (T) this;
    }

    T setDisplayY(float y) {
        displayCardRect.y = y;
        return (T) this;
    }

    T setDisplayWidth(float width) {
        displayCardRect.width = width;
        displayCardRect.height = RenderableCard.HEIGHT_TO_WIDTH_RATIO * width;
        return (T) this;
    }

    T setDisplayHeight(float height) {
        displayCardRect.height = height;
        displayCardRect.width = RenderableCard.WIDTH_TO_HEIGHT_RATIO * height;
        return (T) this;
    }

    T setMoveDisplayWithBase(boolean moveDisplayWithBase) {
        this.moveDisplayWithBase = moveDisplayWithBase;
        return (T) this;
    }

    T resetDisplayRect() {
        displayCardRect.set(baseCardRect);
        return (T) this;
    }

    T select() {
        setSelected(true);
        return (T) this;
    }

    T deselect() {
        setSelected(false);
        return (T) this;
    }

    T toggleSelected() {
        return setSelected(!isSelected);
    }

    T setSelected(boolean selected) {
        if(isSelected == selected || !selectable) {
            return (T) this;
        }
        isSelected = selected;

        if(isSelected) {
            setFaceBackgroundColor(faceSelectedBackgroundColor);
            setBackBackgroundColor(backSelectedBackgroundColor);
            setDisplayPosition(baseCardRect.x, baseCardRect.y + (heightChangeOnSelect * baseCardRect.height));
        } else {
            setBackBackgroundColor(backUnselectedBackgroundColor);
            setFaceBackgroundColor(faceUnselectedBackgroundColor);
            resetDisplayRect();
        }

        return (T) this;
    }

    T setSelectable(boolean selectable) {
        if(!selectable) setSelected(false);
        this.selectable = selectable;
        return (T) this;
    }

    T setHeightChangeOnSelect(float heightChangeOnSelect) {
        this.heightChangeOnSelect = heightChangeOnSelect;
        return (T) this;
    }

    T setFaceUp(boolean faceUp) {
        if(flippable) {
            this.faceUp = faceUp;
        }
        return (T) this;
    }

    T flip() {
        if(flippable) {
            faceUp = !faceUp;
        }
        return (T) this;
    }

    T setFlippable(boolean flippable) {
        if(!flippable) setFaceUp(true);
        this.flippable = flippable;
        return (T) this;
    }

    // --- GETTERS ---
    // Face value getters:
    Color getFaceBackgroundColor() {
        return new Color(this.faceBackgroundColor);
    }

    Color getFaceBorderColor() {
        return new Color(faceBorderColor);
    }

    int getFaceBorderThicknessInPixels() {
        return faceBorderThicknessInPixels;
    }

    int getBackBorderThicknessInPixels() {
        return backBorderThicknessInPixels;
    }

    float getFaceDesignHeightScale() {
        return faceDesignHeightScale;
    }

    float getFaceDesignWidthScale() {
        return faceDesignWidthScale;
    }

    // Back value getters:
    float getBackDesignHeightScale() {
        return backDesignHeightScale;
    }

    float getBackDesignWidthScale() {
        return backDesignWidthScale;
    }

    float getHeightChangeOnSelect() {
        return heightChangeOnSelect;
    }

    Color getBackBorderColor() {
        return new Color(backBorderColor);
    }

    Color getBackBackgroundColor() {
        return new Color(backBackgroundColor);
    }

    // General value getters:
    int getCornerRadiusInPixels() {
        return cornerRadiusInPixels;
    }

    float getX() {
        return baseCardRect.x;
    }

    float getY() {
        return baseCardRect.y;
    }

    float getHeight() {
        return baseCardRect.height;
    }

    float getWidth() {
        return baseCardRect.width;
    }

    float getDisplayX() {
        return displayCardRect.x;
    }

    float getDisplayY() {
        return displayCardRect.y;
    }

    float getDisplayWidth() {
        return displayCardRect.width;
    }

    float getDisplayHeight() {
        return displayCardRect.height;
    }

    Vector2 getPosition() {
        return baseCardRect.getPosition(new Vector2());
    }

    boolean isSelected() {
        return isSelected;
    }

    boolean isSelectable() {
        return selectable;
    }

    boolean isFaceUp() {
        return faceUp;
    }

    boolean isFlippable() {
        return flippable;
    }

    @Override
    public boolean isTrumpSuit() {
        return suit() == GameState.trumpSuit;
    }

    @Override
    public boolean isTrumpRank() {
        return rank() == GameState.trumpRank;
    }

    @Override
    public boolean isTrump() {
        return isTrumpSuit() || isTrumpRank() || isJoker();
    }

    @Override
    public void cardChanged() {
        invalidateSprites();
    }

    // Other:
    @Override
    public int compareTo(Card c) {
        return super.compareTo(c);
    }
}
