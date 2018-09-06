package com.sage.server;

import com.sage.Card;
import com.sage.CardList;
import com.sage.Rank;
import com.sage.Suit;

import java.util.ArrayList;

class ServerCardList extends CardList<ServerCard> {
    ServerCardList() {
        super();
    }

    ServerCardList(ArrayList<ServerCard> cards) {
        super(cards);
    }

    public void add(Rank rank, Suit suit) {
        add(new ServerCard(rank, suit));
    }

    int getTotalPoints() {
        int sum = 0;

        for(ServerCard c : this) {
            sum += c.getPointValue();
        }

        return sum;
    }

    ServerCardList getPointCards() {
        ServerCardList pointCards = new ServerCardList();

        for(ServerCard c : this) {
            if(c.rank() == Rank.KING
                    || c.rank() == Rank.TEN
                    || c.rank() == Rank.FIVE) {
                pointCards.add(c);
            }
        }

        return pointCards;
    }
}
