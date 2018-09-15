package com.sage.shengji;

import com.badlogic.gdx.graphics.Color;

import static com.sage.shengji.RenderableCard.CARD_HEIGHT_IN_PIXELS;
import static com.sage.shengji.RenderableCard.CARD_WIDTH;
import static com.sage.shengji.RenderableCard.CARD_WIDTH_IN_PIXELS;

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
    int faceBorderThicknessInPixels = defaultFaceBorderThicknessInPixels;
    @SuppressWarnings("FieldCanBeLocal")
    int backBorderThicknessInPixels = defaultBackBorderThicknessInPixels;

    @SuppressWarnings("FieldCanBeLocal")
    float faceDesignHeightScale = defaultFaceDesignHeightScale; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle
    @SuppressWarnings("FieldCanBeLocal")
    float faceDesignWidthScale = defaultFaceDesignWidthScale;

    float backDesignHeightScale = defaultBackDesignHeightScale;
    float backDesignWidthScale = defaultBackDesignWidthScale;

    float scale = defaultScale; // Overall scale of the card with respect to the world

    Color faceBorderColor = defaultFaceBorderColor;
    Color backBorderColor = defaultBackBorderColor;

    Color faceBackgroundColor = defaultFaceBackgroundColor;
    Color backBackgroundColor = defaultBackBackgroundColor;

    RenderableCardDesignFacets() {

    }

    RenderableCardDesignFacets setFaceDesignScale(float scale) {
        faceDesignHeightScale = scale;
        faceDesignWidthScale = scale;
        return this;
    }

    RenderableCardDesignFacets setBackDesignScale(float scale) {
        backDesignHeightScale = scale;
        backDesignWidthScale = scale;
        return this;
    }

    RenderableCardDesignFacets setCornerRadiusScale(float cornerRadiusScale) {
        this.cornerRadiusScale = cornerRadiusScale;
        cornerRadius = cornerRadiusScale * CARD_WIDTH;
        return this;
    }

    float getCornerRadiusScale() {
        return cornerRadiusScale;
    }

    float getCornerRadius() {
        return cornerRadius;
    }
}
