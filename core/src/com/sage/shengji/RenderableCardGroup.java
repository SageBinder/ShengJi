package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

class RenderableCardGroup extends RenderableCardList {
    Vector2 pos = new Vector2();

    float cardHeight = Gdx.graphics.getHeight() / 15f,
            regionWidth = Gdx.graphics.getWidth() / 10f,
            prefDivisionProportion = 0.2f;
    // prefDivisionProportion is relative to card width. Maybe should be relative to viewport width instead?

    ShapeRenderer debugRenderer = new ShapeRenderer();
    private boolean inDebugMode = false;

    RenderableCardGroup() {
        super();
    }

    RenderableCardGroup(ArrayList<RenderableCard> cards) {
        super(cards);
    }

    @Override
    void render(SpriteBatch batch, Viewport viewport) {
        float cardPositionRegionWidth = regionWidth - (RenderableCard.WIDTH_TO_HEIGHT_RATIO * cardHeight);

        float division = Math.min(cardHeight * RenderableCard.WIDTH_TO_HEIGHT_RATIO * prefDivisionProportion,
                cardPositionRegionWidth / (size() - 1));

        float offset = MathUtils.clamp((cardPositionRegionWidth * 0.5f) - (0.5f * division * (size() - 1)),
                0,
                cardPositionRegionWidth * 0.5f);

        for(int i = 0; i < size(); i++) {
            RenderableCard c = get(i);
            c.setHeight(cardHeight).setPosition((i * division) + pos.x + offset, pos.y);
        }

        super.render(batch, viewport);

        if(inDebugMode) {
            batch.end();

            debugRenderer.setProjectionMatrix(viewport.getCamera().combined);
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);

            debugRenderer.setColor(0.0f, 0.0f, 1.0f, 1.0f);
            debugRenderer.rect(pos.x, pos.y, regionWidth, cardHeight);

            debugRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
            debugRenderer.line(pos.x + (regionWidth * 0.5f), pos.y + (cardHeight * 1.5f),
                    pos.x + (regionWidth * 0.5f), pos.y - (cardHeight * 0.5f));

            debugRenderer.end();

            batch.begin();
        }
    }

    RenderableCardGroup setCardHeight(float height) {
        cardHeight = height;
        return this;
    }

    RenderableCardGroup setCardWidth(float width) {
        cardHeight = RenderableCard.HEIGHT_TO_WIDTH_RATIO * width;
        return this;
    }

    RenderableCardGroup setPrefDivisionProportion(float division) {
        prefDivisionProportion = division;
        return this;
    }

    RenderableCardGroup setRegionWidth(float width) {
        regionWidth = width;
        return this;
    }

    void debug() {
        inDebugMode = true;
    }

    void setDebug(boolean debug) {
        inDebugMode = debug;
    }

    boolean inDebugMode() {
        return inDebugMode;
    }
}
