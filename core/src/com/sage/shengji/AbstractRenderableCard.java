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
    @SuppressWarnings("WeakerAccess")
    static final int defaultCornerRadiusInPixels = (int)(0.075f * CARD_WIDTH_IN_PIXELS);

    @SuppressWarnings("WeakerAccess")
    static final int defaultFaceBorderThicknessInPixels = (int)(0.018f * CARD_WIDTH_IN_PIXELS);
    @SuppressWarnings("WeakerAccess")
    static final int defaultBackBorderThicknessInPixels = defaultCornerRadiusInPixels;

    @SuppressWarnings("WeakerAccess")
    static final float defaultFaceDesignHeightScale = 0.95f;
    @SuppressWarnings("WeakerAccess")
    static final float defaultFaceDesignWidthScale = 0.95f;

    @SuppressWarnings("WeakerAccess")
    static final float defaultBackDesignHeightScale = ((float)CARD_HEIGHT_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_HEIGHT_IN_PIXELS;
    @SuppressWarnings("WeakerAccess")
    static final float defaultBackDesignWidthScale = ((float)CARD_WIDTH_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_WIDTH_IN_PIXELS;

    @SuppressWarnings("WeakerAccess")
    static final float defaultHeightChangeOnSelect = 0.9f; // Relative to card height

    @SuppressWarnings("WeakerAccess")
    static final Color defaultFaceBorderColor = new Color(0, 0, 0, 1);
    @SuppressWarnings("WeakerAccess")
    static final Color defaultBackBorderColor = new Color(1, 1, 1, 1);

    @SuppressWarnings("WeakerAccess")
    static final Color defaultFaceBackgroundColor = new Color(1, 1, 1, 1);
    @SuppressWarnings("WeakerAccess")
    static final Color defaultBackBackgroundColor = new Color(0, 0, 0, 1);

    static final Color defaultTrumpFaceBorderColor = new Color(0.75f, 0.75f, 0f, 1f);
    static final Color defaultTrumpBackBorderColor = new Color(defaultBackBorderColor);

    // Member variables:
    @SuppressWarnings("FieldCanBeLocal")
    private int cornerRadiusInPixels = defaultCornerRadiusInPixels;

    @SuppressWarnings("FieldCanBeLocal")
    private int faceBorderThicknessInPixels = defaultFaceBorderThicknessInPixels;

    @SuppressWarnings("FieldCanBeLocal")
    private int backBorderThicknessInPixels = defaultBackBorderThicknessInPixels;

    @SuppressWarnings("FieldCanBeLocal")
    private float faceDesignHeightScale = defaultFaceDesignHeightScale; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle
    @SuppressWarnings("FieldCanBeLocal")
    private float faceDesignWidthScale = defaultFaceDesignWidthScale;

    private float backDesignHeightScale = defaultBackDesignHeightScale;
    private float backDesignWidthScale = defaultBackDesignWidthScale;

    private float heightChangeOnSelect = defaultHeightChangeOnSelect;
    
    private Color faceBorderColor = new Color(defaultFaceBorderColor);
    private Color backBorderColor = new Color(defaultBackBorderColor);

    private Color faceBackgroundColor = new Color(defaultFaceBackgroundColor);
    private Color backBackgroundColor = new Color(defaultBackBackgroundColor);

    // TODO: Have a Rectangle selectedRect which represents the location and size of the card when it's selected
    // cardRect represents overall rectangle before rounding corners
    private Rectangle cardRect = new Rectangle(0, 0, CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS);

    private boolean faceUp = true;
    private boolean isSelected = false;
    private boolean selectable = true;

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

    boolean containsPoint(Vector2 point) {
        return containsPoint(point.x, point.y);
    }

    boolean containsPoint(float x, float y) {
        return isSelected ? new Rectangle(getX(), getY() + (getHeightChangeOnSelect() * getHeight()), getWidth(), getHeight()).contains(x, y)
            : cardRect.contains(x, y);
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
        this.faceBackgroundColor = faceBackgroundColor;
        invalidateSprites();
        return (T) this;
    }

    T setFaceBorderColor(Color faceBorderColor) {
        this.faceBorderColor = faceBorderColor;
        invalidateSprites();
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
        this.backBackgroundColor = backBackgroundColor;
        return (T) this;
    }

    T setBackBorderColor(Color backBorderColor) {
        this.backBorderColor = backBorderColor;
        invalidateSprites();
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

    T scale(float newScale) {
        cardRect.setSize(getWidth() * newScale, getHeight() * newScale);
        return (T) this;
    }

    T setWidth(float width) {
        cardRect.setWidth(width);
        cardRect.setHeight(HEIGHT_TO_WIDTH_RATIO * width);
        return (T) this;
    }

    T setHeight(float height) {
        cardRect.setHeight(height);
        cardRect.setWidth(WIDTH_TO_HEIGHT_RATIO * height);
        return (T) this;
    }

    T setX(float x) {
        cardRect.x = x;
        return (T) this;
    }

    T setY(float y) {
        cardRect.y = y;
        return (T) this;
    }

    T setPosition(Vector2 newPosition) {
        return setPosition(newPosition.x, newPosition.y);
    }

    @SuppressWarnings("SameParameterValue")
    T setPosition(float x, float y) {
        cardRect.setPosition(x, y);
        return (T) this;
    }

    T setHeightChangeOnSelect(float heightChangeOnSelect) {
        this.heightChangeOnSelect = heightChangeOnSelect;
        return (T) this;
    }

    T setSelectable(boolean selectable) {
        this.selectable = selectable;
        return (T) this;
    }

    T toggleSelected() {
        return setSelected(!isSelected);
    }

    @SuppressWarnings("UnusedReturnValue")
    T flip() {
        faceUp = !faceUp;
        return (T) this;
    }

    // General value setters:
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

    T setSelected(boolean selected) {
        if(isSelected == selected || !selectable) {
            return (T) this;
        }
        isSelected = selected;

        if(isSelected) {
            setFaceBackgroundColor(new Color(getFaceBackgroundColor().sub(0.5f, 0.5f, 0.5f, 0)));
        } else {
            setFaceBackgroundColor(new Color(defaultFaceBackgroundColor));
        }

        return (T) this;
    }

    T setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        return (T) this;
    }

    // --- GETTERS ---
    // Face value getters:
    Color getFaceBackgroundColor() {
        return this.faceBackgroundColor;
    }

    Color getFaceBorderColor() {
        return faceBorderColor;
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
        return backBorderColor;
    }

    Color getBackBackgroundColor() {
        return backBackgroundColor;
    }

    // General value getters:
    int getCornerRadiusInPixels() {
        return cornerRadiusInPixels;
    }

    float getX() {
        return cardRect.x;
    }

    float getY() {
        return cardRect.y;
    }

    float getHeight() {
        return cardRect.height;
    }

    float getWidth() {
        return cardRect.width;
    }

    Vector2 getPosition() {
        return cardRect.getPosition(new Vector2());
    }

    boolean isSelected() {
        return isSelected;
    }

    boolean isFaceUp() {
        return faceUp;
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

    // Other:
    @Override
    public int compareTo(Object o) {
        return super.compareTo(o);
    }
}
