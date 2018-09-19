package com.sage.shengji;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import static com.sage.shengji.RenderableCard.*;

@SuppressWarnings("unchecked")
abstract class AbstractRenderableCard<T extends AbstractRenderableCard> extends Card {
    // Default variable values:
    @SuppressWarnings("WeakerAccess")
    static final float defaultCornerRadiusScale = 0.075f;
    @SuppressWarnings("WeakerAccess")
    static final float defaultCornerRadius = defaultCornerRadiusScale * CARD_WIDTH;

    @SuppressWarnings("WeakerAccess")
    static final int defaultFaceBorderThicknessInPixels = (int)(0.015f * CARD_WIDTH_IN_PIXELS);
    @SuppressWarnings("WeakerAccess")
    static final int defaultBackBorderThicknessInPixels = (int)(defaultCornerRadiusScale * CARD_WIDTH_IN_PIXELS);

    @SuppressWarnings("WeakerAccess")
    static final float defaultFaceDesignHeightScale = 0.95f;
    @SuppressWarnings("WeakerAccess")
    static final float defaultFaceDesignWidthScale = 0.95f;

    @SuppressWarnings("WeakerAccess")
    static final float defaultBackDesignHeightScale = ((float)CARD_HEIGHT_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_HEIGHT_IN_PIXELS;
    @SuppressWarnings("WeakerAccess")
    static final float defaultBackDesignWidthScale = ((float)CARD_WIDTH_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_WIDTH_IN_PIXELS;
    @SuppressWarnings("WeakerAccess")
    static final float defaultScale = 1f;

    @SuppressWarnings("WeakerAccess")
    static final float defaultHeightChangeOnSelect = CARD_HEIGHT / 10;

    @SuppressWarnings("WeakerAccess")
    static final Color defaultFaceBorderColor = new Color(0, 0, 0, 1);
    @SuppressWarnings("WeakerAccess")
    static final Color defaultBackBorderColor = new Color(1, 1, 1, 1);
    @SuppressWarnings("WeakerAccess")
    static final Color defaultFaceBackgroundColor = new Color(1, 1, 1, 1);
    @SuppressWarnings("WeakerAccess")
    static final Color defaultBackBackgroundColor = new Color(0, 0, 0, 1);

    // Member variables:
    @SuppressWarnings("FieldCanBeLocal")
    private float cornerRadiusScale = defaultCornerRadiusScale;
    private float cornerRadius = defaultCornerRadius;

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

    private float scale = defaultScale; // Overall scale of the card with respect to the world

    private float heightChangeOnSelect = defaultHeightChangeOnSelect;
    
    private Color faceBorderColor = new Color(defaultFaceBorderColor);
    private Color backBorderColor = new Color(defaultBackBorderColor);

    private Color faceBackgroundColor = new Color(defaultFaceBackgroundColor);
    private Color backBackgroundColor = new Color(defaultBackBackgroundColor);

    // cardRect represents overall rectangle before rounding corners
    private Rectangle cardRect = new Rectangle(0, 0, CARD_WIDTH, CARD_HEIGHT);

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

    abstract void displayParametersChanged();

    abstract void render(SpriteBatch batch);

    @SuppressWarnings("unused")
    boolean containsPoint(Vector2 point) {
        return containsPoint(point.x, point.y);
    }

    @SuppressWarnings("WeakerAccess")
    boolean containsPoint(float x, float y) {
        return (!isSelected &&
                cardRect.contains(x, y))
                || (isSelected && new Rectangle(getX(), getY() + (getHeightChangeOnSelect() * getScale()), getWidth(), getHeight())
                .contains(x, y));
    }

    // --- SETTERS ---
    // Face value setters:
    T setFaceDesignScale(float scale) {
        faceDesignHeightScale = scale;
        faceDesignWidthScale = scale;
        displayParametersChanged();
        return (T) this;
    }

    T setFaceDesignHeightScale(float scale) {
        this.faceDesignHeightScale = scale;
        displayParametersChanged();
        return (T) this;
    }

    T setFaceDesignWidthScale(float scale) {
        this.faceDesignWidthScale = scale;
        displayParametersChanged();
        return (T) this;
    }

    Color getFaceBackgroundColor() {
        displayParametersChanged();
        return this.faceBackgroundColor;
    }

    T setFaceBackgroundColor(Color faceBackgroundColor) {
        this.faceBackgroundColor = faceBackgroundColor;
        displayParametersChanged();
        return (T) this;
    }

    T setFaceBorderColor(Color faceBorderColor) {
        this.faceBorderColor = faceBorderColor;
        displayParametersChanged();
        return (T) this;
    }

    T setFaceBorderThicknessRelativeToHeight(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        displayParametersChanged();
        return (T) this;
    }

    T setFaceBorderThicknessRelativeToWidth(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        displayParametersChanged();
        return (T) this;
    }

    T setFaceBorderThicknessInPixels(int faceBorderThicknessInPixels) {
        this.faceBorderThicknessInPixels = faceBorderThicknessInPixels;
        displayParametersChanged();
        return (T) this;
    }

    // Back value setters:
    T setBackDesignScale(float scale) {
        backDesignHeightScale = scale;
        backDesignWidthScale = scale;
        displayParametersChanged();
        return (T) this;
    }

    T setBackDesignHeightScale(float backDesignHeightScale) {
        this.backDesignHeightScale = backDesignHeightScale;
        displayParametersChanged();
        return (T) this;
    }

    T setBackDesignWidthScale(float backDesignWidthScale) {
        this.backDesignWidthScale = backDesignWidthScale;
        displayParametersChanged();
        return (T) this;
    }

    T setBackBackgroundColor(Color backBackgroundColor) {
        this.backBackgroundColor = backBackgroundColor;
        return (T) this;
    }

    T setBackBorderColor(Color backBorderColor) {
        this.backBorderColor = backBorderColor;
        displayParametersChanged();
        return (T) this;
    }

    T setBackBorderThicknessRelativeToHeight(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        displayParametersChanged();
        return (T) this;
    }

    T setBackBorderThicknessRelativeToWidth(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        displayParametersChanged();
        return (T) this;
    }

    T setBackBorderThicknessInPixels(int backBorderThicknessInPixels) {
        this.backBorderThicknessInPixels = backBorderThicknessInPixels;
        displayParametersChanged();
        return (T) this;
    }

    T setScale(float newScale) {
        scale = newScale;
        cardRect.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        return (T) this;
    }

    T setWidth(float width) {
        cardRect.setWidth(width);
        return (T) this;
    }

    T setHeight(float height) {
        cardRect.setHeight(height);
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

    // General value getters:
    float getCornerRadiusScale() {
        return cornerRadiusScale;
    }

    // General value setters:
    T setCornerRadiusScale(float cornerRadiusScale) {
        this.cornerRadiusScale = cornerRadiusScale;
        cornerRadius = cornerRadiusScale * CARD_WIDTH;
        displayParametersChanged();
        return (T) this;
    }

    boolean isFaceUp() {
        return faceUp;
    }

    // --- GETTERS ---
    // Face value getters:
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

    Color getFaceBorderColor() {
        return faceBorderColor;
    }

    Color getBackBorderColor() {
        return backBorderColor;
    }

    Color getBackBackgroundColor() {
        return backBackgroundColor;
    }

    T setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        return (T) this;
    }

    float getCornerRadius() {
        return cornerRadius;
    }

    float getScale() {
        return this.scale;
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
}
