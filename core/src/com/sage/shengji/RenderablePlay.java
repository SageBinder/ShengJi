package com.sage.shengji;

import java.util.Collection;

class RenderablePlay extends RenderableCardGroup {
    @Override
    public boolean add(RenderableCard c) {
        c.setSelectable(false);
        return super.add(c);
    }

    @Override
    public void add(int index, RenderableCard c) {
        c.setSelectable(false);
        super.add(c);
    }

    @Override
    public boolean addAll(Collection<? extends RenderableCard> c) {
        c.forEach(card -> card.setSelectable(false));
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends RenderableCard> c) {
        c.forEach(card -> card.setSelectable(false));
        return super.addAll(index, c);
    }
}
