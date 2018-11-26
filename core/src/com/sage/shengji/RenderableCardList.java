package com.sage.shengji;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.CardList;
import com.sage.Rank;
import com.sage.Suit;

import java.util.ArrayList;
import java.util.Iterator;

public class RenderableCardList extends CardList<RenderableCard> {
    RenderableCardList() {
        super();
    }

    RenderableCardList(ArrayList<RenderableCard> cards) {
        super(cards);
    }

    void add(Rank rank, Suit suit) {
        add(new RenderableCard(rank, suit));
    }

    @Override
    public void remove(Rank rank, Suit suit) {
        for(Iterator<RenderableCard> i = iterator(); i.hasNext();) {
            RenderableCard c = i.next();
            if(c.suit() == suit && c.rank() == rank) {
                i.remove();
                return;
            }
        }
    }

    void render(SpriteBatch batch, Viewport viewport) {
        for(RenderableCard c : this) {
            c.render(batch, viewport);
        }
    }

    void render(SpriteBatch batch, Viewport viewport, boolean renderBase) {
        for(RenderableCard c : this) {
            c.render(batch, viewport);
            if(renderBase && !c.displayRectEqualsBaseRect()) {
                c.renderBase(batch, viewport);
            }
        }
    }

    void setAllSelected(boolean selected) {
        forEach(c -> c.setSelected(selected));
    }
}
