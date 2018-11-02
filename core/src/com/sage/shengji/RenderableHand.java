package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.ListIterator;

class RenderableHand extends RenderableCardGroup {
    private HandClickListener clickListener = (c, button) -> {
        if(button == Input.Buttons.LEFT) {
            c.toggleSelected();
        } else if(button == Input.Buttons.RIGHT) {
            c.flip();
        }
    };

    float bottomPaddingProportion = 0.025f,
            leftPaddingProportion = 0.05f,
            rightPaddingProportion = 0.05f;

    RenderableHand() {
        super();
        super.cardHeight = Gdx.graphics.getHeight() / 7f;
    }

    RenderableHand(ArrayList<RenderableCard> cards) {
        super(cards);
        super.cardHeight = Gdx.graphics.getHeight() / 7f;
    }

    RenderableHand sort() {
        sort(AbstractRenderableCard::compareTo);
        return this;
    }

    void render(SpriteBatch batch, Viewport viewport) {
        super.regionWidth = viewport.getWorldWidth() - (viewport.getWorldWidth() * leftPaddingProportion) - (viewport.getWorldWidth() * rightPaddingProportion);
        super.pos.x = viewport.getWorldWidth() * leftPaddingProportion;
        super.pos.y = viewport.getWorldHeight() * bottomPaddingProportion;

        super.render(batch, viewport);
    }

    void click(Vector2 pos, int button) {
        click(pos.x, pos.y, button);
    }

    void click(float x, float y, int button) {
        for(ListIterator<RenderableCard> i = reverseListIterator(); i.hasPrevious();) {
            final RenderableCard c = i.previous();

            if(c.displayRectContainsPoint(x, y)) {
                clickListener.click(c, button);
                return;
            }
        }
    }

    RenderableHand setOnClick(HandClickListener onClick) {
        this.clickListener = onClick;
        return this;
    }

    interface HandClickListener {
        void click(RenderableCard c, int button);
    }
}
