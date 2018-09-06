package com.sage.server;

import java.util.ArrayList;
import java.util.Random;

class Deck extends ServerCardList {
    private Random random = new Random();

    Deck(int numFullDecks) {
        for(int i = 0; i < numFullDecks; i++) {
            for(int j = 0; j < 54; j++) {
                add(new ServerCard(j));
            }
        }
    }

    Deck(ArrayList<ServerCard> cards) {
        this.addAll(cards);
    }

    void dealRandomToPlayer(Player p) {
        ServerCard toDeal = getRandomCard();
        p.addToHand(toDeal);
        remove(toDeal);
    }

    void dealAllRandomly(ArrayList<Player> players) {
        while(size() > 0) {
            for(Player p : players) {
                dealRandomToPlayer(p);
            }
        }
    }
}
