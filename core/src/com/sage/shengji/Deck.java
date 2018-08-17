package com.sage.shengji;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class Deck {
    private ArrayList<Card> cards = new ArrayList<>();
    private Random random = new Random();

    Deck(ArrayList<Card> cards) {
        this.cards.addAll(cards);
    }

    public void addCard(Rank rank, Suit suit) {
        cards.add(new Card(rank, suit));
    }

    public void removeCard(Rank rank, Suit suit) {
        for(Iterator<Card> i = cards.iterator(); i.hasNext();) {
            Card c = i.next();
            if(c.getSuit() == suit && c.getRank() == rank) {
                i.remove();
                return;
            }
        }
    }

    public void dealRandomToPlayer(Player p) {
        int dealIndex = random.nextInt(cards.size());

        p.addToHand(cards.get(dealIndex));
        cards.remove(dealIndex);
    }

    public void dealAll(ArrayList<Player> players) {

    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public ArrayList<Card> getCards() {
        return cards;
    }
}
