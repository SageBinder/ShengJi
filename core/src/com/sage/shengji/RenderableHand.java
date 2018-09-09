package com.sage.shengji;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.Collections;

class RenderableHand extends RenderableCardList {
    private ExtendViewport viewport;

    RenderableHand(ExtendViewport viewport) {
        super();
        this.viewport = viewport;
    }

    RenderableHand(ArrayList<RenderableCard> cards, ExtendViewport viewport) {
        super(cards);
        this.viewport = viewport;
    }

    @Override
    void render(SpriteBatch batch, ShapeRenderer renderer) {
        float width = viewport.getWorldWidth() - 0.2f - (RenderableCard.getCardWidth() / 2);
        float pixelDivision = width / size();

        for(int i = 0; i < size(); i++) {
            get(i).setScale(0.5f).setPosition(new Vector2((i * pixelDivision) + 0.1f, 0.2f));
        }

        super.render(batch, renderer);
    }

    boolean click(Vector2 clickPos) {
        return click(clickPos.x, clickPos.y);
    }

    @SuppressWarnings("WeakerAccess")
    boolean click(float x, float y) {
        for(int i = size() - 1; i >= 0; i--) {
            RenderableCard c = get(i);
            if(c.containsPoint(x, y)) {
                c.flip();
                return true;
            }
        }

        return false;
    }
}
