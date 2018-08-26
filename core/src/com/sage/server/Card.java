package com.sage.server;

import java.util.Random;

class Card {
    Rank rank;
    Suit suit;
    private int hierarchicalValue;
    private int cardNum;

    Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;

        if(this.suit == Suit.SMALL_JOKER) {
            cardNum = 52;
        } else if(this.suit == Suit.BIG_JOKER) {
            cardNum = 53;
        } else {
            cardNum = (rank.toInt() * 4) + suit.toInt();
        }

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

    Suit getEffectiveSuit() {
        return suit.getEffectiveSuit();
    }

    Rank getRank() {
        return rank;
    }

    int getCardNum() {
        return cardNum;
    }

    int getPointValue() {
        if(rank == Rank.TEN || rank == Rank.KING) {
            return 10;
        }
        if(rank == Rank.FIVE) {
            return 5;
        }
        return 0;
    }

    static Card getCardFromCardNum(int cardNum) {
        if(cardNum == 52) {
            return new Card(Rank.JOKER, Suit.SMALL_JOKER);
        } else if(cardNum == 53) {
            return new Card(Rank.JOKER, Suit.BIG_JOKER);
        } else {
            return new Card(Rank.values()[cardNum / 4], Suit.values()[cardNum % 4]);
        }
    }

    static Rank getRankFromCardNum(int cardNum) {
        if(cardNum <= 52) {
            return Rank.JOKER;
        } else {
            return Rank.values()[cardNum / 4];
        }
    }

    static Suit getSuitFromCardNum(int cardNum) {
        if(cardNum == 52) {
            return Suit.SMALL_JOKER;
        } else if(cardNum == 53) {
            return Suit.BIG_JOKER;
        } else {
            return Suit.values()[cardNum % 4];
        }
    }

    static Card getRandomCard() {
        Random random = new Random();
        int cardNum = random.nextInt(54);

        return getCardFromCardNum(cardNum);
    }

    public boolean isTrump() {
        return suit.isTrumpSuit() || rank.isTrumpRank();
    }

    public boolean isJoker() {
        return suit.isJoker();
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return this.rank == ((Card)obj).getRank() && this.suit == ((Card)obj).getSuit();
        } catch(ClassCastException e) {
            return super.equals(obj);
        }
    }
}
