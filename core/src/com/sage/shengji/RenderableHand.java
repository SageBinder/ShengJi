package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Collection;

class RenderableHand extends RenderableCardGroup {
    float bottomPaddingProportion = 0.025f,
            leftPaddingProportion = 0.05f,
            rightPaddingProportion = 0.05f,
            cardHeightProportion = 1f / 7f;

    RenderableHand() {
        super();
        super.cardHeight = Gdx.graphics.getHeight() * cardHeightProportion;
    }

    RenderableHand(ArrayList<RenderableCard> cards) {
        super(cards);
        super.cardHeight = Gdx.graphics.getHeight() * cardHeightProportion;
    }

    RenderableHand sort() {
        sort(AbstractRenderableCard::compareTo);
        return this;
    }

    @Override
    void render(SpriteBatch batch, Viewport viewport) {
        this.render(batch, viewport, false);
    }

    @Override
    void render(SpriteBatch batch, Viewport viewport, boolean renderBase) {
        super.cardHeight = viewport.getWorldHeight() * cardHeightProportion;
        super.regionWidth = viewport.getWorldWidth()
                - (viewport.getWorldWidth() * leftPaddingProportion)
                - (viewport.getWorldWidth() * rightPaddingProportion);
        super.pos.x = viewport.getWorldWidth() * leftPaddingProportion;
        super.pos.y = viewport.getWorldHeight() * bottomPaddingProportion;

        super.render(batch, viewport, renderBase);
    }

    @Override
    public boolean add(RenderableCard c) {
        c.setSelectable(true);
        c.setFlippable(true);
        return super.add(c);
    }

    @Override
    public void add(int index, RenderableCard c) {
        c.setSelectable(true);
        c.setFlippable(true);
        super.add(c);
    }

    @Override
    public boolean addAll(Collection<? extends RenderableCard> c) {
        c.forEach(card -> {
            card.setSelectable(true);
            card.setFlippable(true);
        });
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends RenderableCard> c) {
        c.forEach(card -> {
            card.setSelectable(true);
            card.setFlippable(true);
        });
        return super.addAll(index, c);
    }
}
