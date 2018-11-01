package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

class RenderableCardGroup extends RenderableCardList {
    Vector2 pos = new Vector2();

    float cardHeight = Gdx.graphics.getHeight() / 15f,
            regionWidth = Gdx.graphics.getWidth() / 10f;

    RenderableCardGroup() {
        super();
    }

    RenderableCardGroup(ArrayList<RenderableCard> cards) {
        super(cards);
    }

    @Override
    void render(SpriteBatch batch, Viewport viewport) {
        float cardPositionRegionWidth = regionWidth - (RenderableCard.WIDTH_TO_HEIGHT_RATIO * cardHeight);
        float division = cardPositionRegionWidth / size();

        for(int i = 0; i < size(); i++) {
            RenderableCard c = get(i);
            c.setHeight(cardHeight).setPosition((i * division) + pos.x, pos.y);
        }

        super.render(batch, viewport);
    }

    RenderableCardGroup setCardHeight(float height) {
        cardHeight = height;
        return this;
    }

    RenderableCardGroup setRegionWidth(float width) {
        regionWidth = width;
        return this;
    }
}
