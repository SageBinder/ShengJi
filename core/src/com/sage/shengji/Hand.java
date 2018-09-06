package com.sage.shengji;

import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;

import java.util.ArrayList;
import java.util.Iterator;

class Hand {
    ArrayList<Card> cards;

    Hand() {
        cards = new ArrayList<>();
    }

    public void add(Rank rank, Suit suit) {
        cards.add(new Card(rank, suit));
    }

    public void add(Card c) {
        cards.add(c);
    }

    public void remove(Rank rank, Suit suit) {
        for(Iterator<Card> i = cards.iterator(); i.hasNext();) {
            Card c = i.next();
            if(c.suit() == suit && c.rank() == rank) {
                i.remove();
                return;
            }
        }
    }

    public ArrayList<Card> getCards() {
        return cards;
    }
}
