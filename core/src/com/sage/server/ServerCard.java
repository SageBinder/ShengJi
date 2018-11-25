package com.sage.server;

import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import java.util.Objects;

class ServerCard extends Card {
    ServerCard(Rank rank, Suit suit) {
        super(rank, suit);
    }

    ServerCard(int cardNum) {
        super(cardNum);
    }

    ServerCard(ServerCard c) {
        super(c.cardNum());
    }

    // Sets this card to be a random card
    ServerCard() {
        super();
    }

    Suit getEffectiveSuit() {
        return suit() == Suit.BIG_JOKER
                || suit() == Suit.SMALL_JOKER
                || rank() == RoundRunner.trumpRank ? RoundRunner.trumpSuit
                : suit();
    }

    @Override
    public boolean isTrumpSuit() {
        return suit() == RoundRunner.trumpSuit;
    }

    @Override
    public boolean isTrumpRank() {
        return rank() == RoundRunner.trumpRank;
    }

    @Override
    public boolean isTrump() {
        return isTrumpSuit() || isTrumpRank() || isJoker();
    }

    @Override
    public void cardChanged() {
        // The abstract cardChanged method in Card is pretty much only to tell RenderableCard to update its shit
    }

    // Overriding equals this way is probably really stupid but I've already written everything else with this in mind
    // so it stays.

    @Override
    public boolean equals(Object obj) {
        try {
            return cardNum() == ((ServerCard)obj).cardNum();
        } catch(ClassCastException e) {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNum());
    }
}
