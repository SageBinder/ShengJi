package com.sage.server;

import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

class ServerCard extends Card {
    private int hierarchicalValue;

    ServerCard(Rank rank, Suit suit) {
        super(rank, suit);
        establishHierarchicalValue();
    }

    ServerCard(int cardNum) {
        super(cardNum);
        establishHierarchicalValue();
    }

    ServerCard(ServerCard c) {
        super(c.cardNum());
        establishHierarchicalValue();
    }

    // Sets this card to be a random card
    ServerCard() {
        super();
        establishHierarchicalValue();
    }

    private void establishHierarchicalValue() {
        if(suit() == Suit.BIG_JOKER) {
            hierarchicalValue = 31;
        } else if(suit() == Suit.SMALL_JOKER) {
            hierarchicalValue = 30;
        } else if(suit().isTrumpSuit() && rank().isTrumpRank()) {
            hierarchicalValue = 29;
        } else if(rank().isTrumpRank()) {
            hierarchicalValue = 28;
        } else if(suit().isTrumpSuit()) {
            hierarchicalValue = 13 + rank().toInt();
        } else {
            hierarchicalValue = rank().toInt();
        }
    }

    int getHierarchicalValue() {
        return hierarchicalValue;
    }

    Suit getEffectiveSuit() {
        return suit().getEffectiveSuit();
    }

    int getPointValue() {
        if(rank() == Rank.TEN || rank() == Rank.KING) {
            return 10;
        }
        if(rank() == Rank.FIVE) {
            return 5;
        }
        return 0;
    }

    boolean isTrump() {
        return suit().isTrumpSuit() || rank().isTrumpRank();
    }

    public boolean isJoker() {
        return suit().isJoker();
    }
}
