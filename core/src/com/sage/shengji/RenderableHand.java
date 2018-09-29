package com.sage.shengji;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;

class RenderableHand extends RenderableCardList {
    RenderableHand() {
        super();
    }

    RenderableHand(ArrayList<RenderableCard> cards) {
        super(cards);
    }

    void render(SpriteBatch batch, ExtendViewport viewport) {
        float width = viewport.getWorldWidth() - 0.2f - (RenderableCard.CARD_WIDTH / 2);
        float pixelDivision = width / size();

        for(int i = 0; i < size(); i++) {
            get(i).setScale(0.5f).setPosition(new Vector2((i * pixelDivision) + 0.1f, 0.2f));
        }

        super.render(batch);
    }

    RenderableCard getClickedCard(Vector2 clickPos) {
        return getClickedCard(clickPos.x, clickPos.y);
    }

    @SuppressWarnings("WeakerAccess")
    RenderableCard getClickedCard(float x, float y) {
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
        RenderableCard c = getClickedCard(x, y);
        if(c != null) {
            c.toggleSelected();
            return true;
        }
        return false;
    }
}
