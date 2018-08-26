package com.sage.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

class CardList extends ArrayList<Card> {
    private Random random = new Random();

    CardList() {
        super();
    }

    CardList(ArrayList<Card> cards) {
        super(cards);
    }

    void add(Rank rank, Suit suit) {
        add(new Card(rank, suit));
    }

    void remove(Rank rank, Suit suit) {
        for(Iterator<Card> i = iterator(); i.hasNext();) {
            Card c = i.next();
            if(c.getSuit() == suit && c.getRank() == rank) {
                i.remove();
                return;
            }
        }
    }

    Card getRandomCard() {
        int dealIndex = random.nextInt(size());
        return get(dealIndex);
    }

    int getTotalPoints() {
        int sum = 0;

        for(Card c : this) {
            sum += c.getPointValue();
        }

        return sum;
    }

    CardList getPointCards() {
        CardList pointCards = new CardList();

        for(Card c : this) {
            if(c.getRank() == Rank.KING
                    || c.getRank() == Rank.TEN
                    || c.getRank() == Rank.FIVE) {
                pointCards.add(c);
            }
        }

        return pointCards;
    }

    boolean contains(Rank rank, Suit suit) {
        for(Card c : this) {
            if(c.getRank() == rank && c.getSuit() == suit) {
                return true;
            }
        }

        return false;
    }

    boolean allSameCard() {
        int compare = get(0).getCardNum();
        for(Card c : this) {
            if(c.getCardNum() != compare) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean contains(Object o) {
        try {
            Card card = (Card) o;
            for(Card c : this) {
                if(c.equals(card)) {
                    return true;
                }
            }
            return false;
        } catch(ClassCastException e) {
            return super.contains(o);
        }
    }

    boolean containsAny(CardList cardList) {
        for(Card c : cardList) {
            if(contains(c)) {
                return true;
            }
        }

        return false;
    }
}
