package com.sage.shengji;

import java.util.Collection;

class RenderablePlay extends RenderableCardGroup {
    @Override
    public boolean add(RenderableCard c) {
        c.setSelectable(false);
        c.setFlippable(false);
        return super.add(c);
    }

    @Override
    public void add(int index, RenderableCard c) {
        c.setSelectable(false);
        c.setFlippable(false);
        super.add(c);
    }

    @Override
    public boolean addAll(Collection<? extends RenderableCard> c) {
        c.forEach(card -> card.setSelectable(false));
        c.forEach(card -> card.setFlippable(false));
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends RenderableCard> c) {
        c.forEach(card -> card.setSelectable(false));
        c.forEach(card -> card.setFlippable(false));
        return super.addAll(index, c);
    }
}
