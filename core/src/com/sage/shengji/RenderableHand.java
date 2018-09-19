package com.sage.shengji;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.Collections;

import static com.sage.shengji.RenderableCard.CARD_HEIGHT;
import static com.sage.shengji.RenderableCard.CARD_WIDTH;

class RenderableHand extends RenderableCardList {
    private ExtendViewport viewport;

    private float maxWidth, maxHeight;

    RenderableHand(ExtendViewport viewport) {
        super();
        this.viewport = viewport;
        maxWidth = viewport.getWorldWidth();
    }

    RenderableHand(ArrayList<RenderableCard> cards, ExtendViewport viewport) {
        super(cards);
        this.viewport = viewport;
    }

    @Override
    void render(SpriteBatch batch) {
        float width = viewport.getWorldWidth() - 0.2f - (RenderableCard.CARD_WIDTH / 2);
        float pixelDivision = width / size();

        for(int i = 0; i < size(); i++) {
            (get(i).getFacets()).setScale(0.5f).setPosition(new Vector2((i * pixelDivision) + 0.1f, 0.2f));
        }

        super.render(batch);
    }

    RenderableCard click(Vector2 clickPos) {
        return click(clickPos.x, clickPos.y);
    }

    @SuppressWarnings("WeakerAccess")
    RenderableCard click(float x, float y) {
        for(int i = size() - 1; i >= 0; i--) {
            RenderableCard c = get(i);
            if(c.containsPoint(x, y)) {
                return c;
            }
        }

        return null;
    }

    boolean toggleSelectedFromClick(Vector2 clickPos) {
        return toggleSelectedFromClick(clickPos.x, clickPos.y);
    }

    boolean toggleSelectedFromClick(float x, float y) {
        RenderableCard c = click(x, y);
        if(c != null) {
            c.toggleSelected();
            return true;
        }
        return false;
    }
}
