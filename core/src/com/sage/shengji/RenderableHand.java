package com.sage.shengji;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import java.util.Iterator;

class RenderableHand extends Hand {
    private ExtendViewport viewport;

    RenderableHand(ExtendViewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public void add(Rank rank, Suit suit) {
        cards.add(new RenderableCard(rank, suit));
    }

    @Override
    public void add(Card c) {
        cards.add(c);
    }

    @Override
    public void remove(Rank rank, Suit suit) {
        for(Iterator<Card> i = cards.iterator(); i.hasNext();) {
            RenderableCard c = (RenderableCard)i.next();
            if(c.suit() == suit && c.rank() == rank) {
                i.remove();
                return;
            }
        }
    }

    void render(SpriteBatch batch, ShapeRenderer renderer) {
        float width = viewport.getWorldWidth() - 0.2f - (TableScreen.CARD_WIDTH / 2);
        //    Gdx.app.log("Hand.render", "width: " + width);
        float pixelDivision = width / cards.size();

        for(int i = 0; i < cards.size(); i++) {
            RenderableCard cardToRender = ((RenderableCard)cards.get(i)).setScale(0.5f).setPosition(new Vector2((i * pixelDivision) + 0.1f, 0.2f));
            cardToRender.render(batch, renderer);
        }
    }
}
