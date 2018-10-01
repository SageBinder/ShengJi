package com.sage;

import com.sage.server.Rank;
import com.sage.server.Suit;

import java.util.Random;

public class Card {
    private final Rank rank;
    private final Suit suit;
    private final int cardNum;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;

        cardNum = getCardNumFromRankAndSuit(rank, suit);
    }

    public Card(int cardNum) {
        this.cardNum = cardNum;
        this.rank = getRankFromCardNum(cardNum);
        this.suit = getSuitFromCardNum(cardNum);
    }

    public Card(Card c) {
        this.suit = c.suit;
        this.rank = c.rank;
        this.cardNum = c.cardNum;
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

    public int cardNum() {
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

    @SuppressWarnings("WeakerAccess")
    public static int getCardNumFromRankAndSuit(Rank rank, Suit suit) {
        if(suit == Suit.SMALL_JOKER) {
            return 52;
        } else if(suit == Suit.BIG_JOKER) {
            return 53;
        } else {
            return (rank.toInt() * 4) + suit.toInt();
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