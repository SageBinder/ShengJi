package com.sage.shengji;

import java.util.Random;

public class Card {
    Rank rank;
    Suit suit;
    private int hierarchicalValue;

    Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;


        establishHierarchicalValue();
    }

    Card(Card c) {
        this.suit = c.getSuit();
        this.rank = c.getRank();
        establishHierarchicalValue();
    }

    private void establishHierarchicalValue() {
        if(suit == Suit.BIG_JOKER) {
            hierarchicalValue = 31;
        } else if(suit == Suit.SMALL_JOKER) {
            hierarchicalValue = 30;
        } else if(suit.isTrumpSuit() && rank.isTrumpRank()) {
            hierarchicalValue = 29;
        } else if(rank.isTrumpRank()) {
            hierarchicalValue = 28;
        } else if(suit.isTrumpSuit()) {
            hierarchicalValue = 13 + rank.toInt();
        } else {
            hierarchicalValue = rank.toInt();
        }
    }

    public int getHierarchicalValue() {
        return hierarchicalValue;
    }

    Suit getSuit() {
        return suit;
    }

    Rank getRank() {
        return rank;
    }

    static Card getRandomCard() {
        Random random = new Random();
        int cardNum = random.nextInt(54);

        if(cardNum == 52) {
            return new Card(Rank.JOKER, Suit.SMALL_JOKER);
        } else if(cardNum == 53) {
            return new Card(Rank.JOKER, Suit.BIG_JOKER);
        } else {
            return new Card(Rank.values()[cardNum / 4], Suit.values()[cardNum % 4]);
        }
    }

    public boolean isTrump() {
        return suit.isTrumpSuit() || rank.isTrumpRank();
    }

    public boolean isJoker() {
        return suit.isJoker();
    }
}
