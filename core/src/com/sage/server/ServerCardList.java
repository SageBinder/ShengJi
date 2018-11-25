package com.sage.server;

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
}
