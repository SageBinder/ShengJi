package com.sage.server;


import java.util.ArrayList;
import java.util.Random;

class Deck extends CardList {
    private Random random = new Random();

    Deck(int numFullDecks) {
        for(int i = 0; i < numFullDecks; i++) {
            for(int j = 0; j < 54; j++) {
                add(new Card(j));
            }
        }
    }

    Deck(ArrayList<Card> cards) {
        this.addAll(cards);
    }

    void dealRandomToPlayer(Player p) {
        Card toDeal = getRandomCard();
        p.addToHand(toDeal);
        remove(toDeal);
    }

    Card getRandomCard() {
        int dealIndex = random.nextInt(size());
        return get(dealIndex);
    }

    void dealAllRandomly(ArrayList<Player> players) {
        while(size() > 0) {
            for(Player p : players) {
                dealRandomToPlayer(p);
            }
        }
    }
}
