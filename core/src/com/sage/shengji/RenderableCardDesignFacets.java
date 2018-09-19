package com.sage.shengji;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import static com.sage.shengji.RenderableCard.*;

class RenderableCardDesignFacets {
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
    
    private final RenderableCard card;
    
    RenderableCardDesignFacets() {
        card = null;
    }
    
    RenderableCardDesignFacets(RenderableCard belongsTo) {
        card = belongsTo;
    }

    RenderableCardDesignFacets(RenderableCardDesignFacets other, RenderableCard card) {
        this.cornerRadiusScale = other.cornerRadiusScale;
        this.cornerRadius = other.cornerRadius;
        this.faceBorderThicknessInPixels = other.faceBorderThicknessInPixels;
        this.backBorderThicknessInPixels = other.backBorderThicknessInPixels;
        this.faceDesignHeightScale = other.faceDesignHeightScale;
        this.faceDesignWidthScale = other.faceDesignWidthScale;
        this.backDesignHeightScale = other.backDesignHeightScale;
        this.backDesignWidthScale = other.backDesignWidthScale;
        this.scale = other.scale;
        this.heightChangeOnSelect = other.heightChangeOnSelect;
        this.faceBorderColor = other.faceBorderColor;
        this.backBorderColor = other.backBorderColor;
        this.faceBackgroundColor = other.faceBackgroundColor;
        this.backBackgroundColor = other.backBackgroundColor;
        this.cardRect = other.cardRect;
        this.card = card;
    }

    // --- SETTERS ---
    // Face value setters:
    RenderableCardDesignFacets setFaceDesignScale(float scale) {
        faceDesignHeightScale = scale;
        faceDesignWidthScale = scale;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setFaceDesignHeightScale(float scale) {
        this.faceDesignHeightScale = scale;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setFaceDesignWidthScale(float scale) {
        this.faceDesignWidthScale = scale;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setFaceBackgroundColor(Color faceBackgroundColor) {
        this.faceBackgroundColor = faceBackgroundColor;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    Color getFaceBackgroundColor() {
        if(card != null) {
            card.facetsChanged();
        }
        return this.faceBackgroundColor;
    }

    RenderableCardDesignFacets setFaceBorderColor(Color faceBorderColor) {
        this.faceBorderColor = faceBorderColor;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setFaceBorderThicknessRelativeToHeight(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setFaceBorderThicknessRelativeToWidth(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setFaceBorderThicknessInPixels(int faceBorderThicknessInPixels) {
        this.faceBorderThicknessInPixels = faceBorderThicknessInPixels;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    // Back value setters:
    RenderableCardDesignFacets setBackDesignScale(float scale) {
        backDesignHeightScale = scale;
        backDesignWidthScale = scale;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setBackDesignHeightScale(float backDesignHeightScale) {
        this.backDesignHeightScale = backDesignHeightScale;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setBackDesignWidthScale(float backDesignWidthScale) {
        this.backDesignWidthScale = backDesignWidthScale;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setBackBackgroundColor(Color backBackgroundColor) {
        this.backBackgroundColor = backBackgroundColor;
        return this;
    }

    RenderableCardDesignFacets setBackBorderColor(Color backBorderColor) {
        this.backBorderColor = backBorderColor;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setBackBorderThicknessRelativeToHeight(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setBackBorderThicknessRelativeToWidth(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setBackBorderThicknessInPixels(int backBorderThicknessInPixels) {
        this.backBorderThicknessInPixels = backBorderThicknessInPixels;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    // Shared value setters:
    RenderableCardDesignFacets setCornerRadiusScale(float cornerRadiusScale) {
        this.cornerRadiusScale = cornerRadiusScale;
        cornerRadius = cornerRadiusScale * CARD_WIDTH;
        if(card != null) {
            card.facetsChanged();
        }
        return this;
    }

    RenderableCardDesignFacets setScale(float newScale) {
        scale = newScale;
        cardRect.setSize(CARD_WIDTH * scale, CARD_HEIGHT * scale);
        return this;
    }

    RenderableCardDesignFacets setWidth(float width) {
        cardRect.setWidth(width);
        return this;
    }

    RenderableCardDesignFacets setHeight(float height) {
        cardRect.setHeight(height);
        return this;
    }

    RenderableCardDesignFacets setX(float x) {
        cardRect.x = x;
        return this;
    }

    RenderableCardDesignFacets setY(float y) {
        cardRect.y = y;
        return this;
    }

    RenderableCardDesignFacets setPosition(Vector2 newPosition) {
        return setPosition(newPosition.x, newPosition.y);
    }

    @SuppressWarnings("SameParameterValue")
    RenderableCardDesignFacets setPosition(float x, float y) {
        cardRect.setPosition(x, y);
        return this;
    }

    RenderableCardDesignFacets setHeightChangeOnSelect(float heightChangeOnSelect) {
        this.heightChangeOnSelect = heightChangeOnSelect;
        return this;
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

    // Shared value getters:
    float getCornerRadiusScale() {
        return cornerRadiusScale;
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

    Rectangle getCardRect() {
        return cardRect;
    }
    
    boolean cardEquals(RenderableCard c) {
        return c == card;
    }
}
