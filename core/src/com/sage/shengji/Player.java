package com.sage.shengji;

public class Player {
    Hand hand = new Hand();
    int callRank = 2;

    public void addToHand(Card c) {
        hand.add(c);
    }

    public void addToHand(Suit suit, Rank rank) {
        hand.add(rank, suit);
    }

    public void removeFromHand(Suit suit, Rank rank) {
        hand.remove(rank, suit);
    }

    public void incrementCallRank() {
        callRank++;
    }
}
