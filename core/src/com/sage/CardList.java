package com.sage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

@SuppressWarnings("serial")
public class CardList<T extends Card> extends ArrayList<T> {
    private Random random = new Random();

    public CardList() {
        super();
    }

    public CardList(ArrayList<T> cards) {
        super(cards);
    }

    public void remove(Rank rank, Suit suit) {
        for(Iterator<T> i = iterator(); i.hasNext();) {
            Card c = i.next();
            if(c.suit() == suit && c.rank() == rank) {
                i.remove();
                return;
            }
        }
    }

    public T getRandomCard() {
        int dealIndex = random.nextInt(size());
        return get(dealIndex);
    }

    public boolean contains(Rank rank, Suit suit) {
        for(Card c : this) {
            if(c.rank() == rank && c.suit() == suit) {
                return true;
            }
        }

        return false;
    }

    public ListIterator<T> reverseListIterator() {
        return listIterator(size());
    }

    public boolean containsAny(CardList<T> cardList) {
        for(Card c : cardList) {
            if(contains(c)) {
                return true;
            }
        }

        return false;
    }

    public int getTotalPoints() {
        return stream().mapToInt(Card::getPointValue).sum();
    }
}
