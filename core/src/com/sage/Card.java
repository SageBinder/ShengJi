package com.sage;

import java.util.Random;

public class Card {
    private Rank rank;
    private Suit suit;
    private int cardNum;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;

        if(this.suit == Suit.SMALL_JOKER) {
            cardNum = 52;
        } else if(this.suit == Suit.BIG_JOKER) {
            cardNum = 53;
        } else {
            cardNum = (rank.toInt() * 4) + suit.toInt();
        }
    }

    public Card(int cardNum) {
        this.cardNum = cardNum;
        this.rank = getRankFromCardNum(cardNum);
        this.suit = getSuitFromCardNum(cardNum);
    }

    public Card(Card c) {
        this.suit = c.suit();
        this.rank = c.rank();
        this.cardNum = c.getCardNum();
    }

    // Sets this card to be a random card
    public Card() {
        this(new Random().nextInt(54));
    }

    public Suit suit() {
        return suit;
    }

    public Rank rank() {
        return rank;
    }

    public int getCardNum() {
        return cardNum;
    }

    public static Rank getRankFromCardNum(int cardNum) {
        if(cardNum >= 52) {
            return Rank.JOKER;
        } else {
            return Rank.values()[cardNum / 4];
        }
    }

    public static Suit getSuitFromCardNum(int cardNum) {
        if(cardNum == 52) {
            return Suit.SMALL_JOKER;
        } else if(cardNum == 53) {
            return Suit.BIG_JOKER;
        } else {
            return Suit.values()[cardNum % 4];
        }
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return this.rank == ((Card)obj).rank() && this.suit == ((Card)obj).suit();
        } catch(ClassCastException e) {
            return super.equals(obj);
        }
    }
}