package com.sage.server;

enum Suit {
    HEARTS,
    CLUBS,
    DIAMONDS,
    SPADES,
    SMALL_JOKER,
    BIG_JOKER;

    public static Suit currentTrumpSuit;

    public static void setCurrentTrumpSuit(Suit suit) {
        currentTrumpSuit = suit;
    }

    public static Suit getCurrentTrumpSuit() {
        return currentTrumpSuit;
    }

    public Suit getEffectiveSuit() {
        if(this == Suit.BIG_JOKER || this == Suit.SMALL_JOKER) {
            return Suit.getCurrentTrumpSuit();
        } else {
            return this;
        }
    }

    public boolean isTrumpSuit() {
        return this == currentTrumpSuit || this == SMALL_JOKER || this == BIG_JOKER;
    }

    public boolean isJoker() {
        return this == SMALL_JOKER || this == BIG_JOKER;
    }

    public String toString() {
        switch(this) {
            case CLUBS:
                return "clubs";
            case HEARTS:
                return "hearts";
            case SPADES:
                return "spades";
            case DIAMONDS:
                return "diamonds";
            case SMALL_JOKER:
                return "small_joker";
            case BIG_JOKER:
                return "big_joker";
            default:
                return "";
        }
    }

    public int toInt() {
        switch(this) {
            case HEARTS:
                return 0;
            case CLUBS:
                return 1;
            case DIAMONDS:
                return 2;
            case SPADES:
                return 3;
            default:
                return -1;
        }
    }
}
