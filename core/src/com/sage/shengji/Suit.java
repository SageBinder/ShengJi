package com.sage.shengji;

public enum Suit {
    HEARTS,
    CLUBS,
    DIAMONDS,
    SPADES,
    SMALL_JOKER,
    BIG_JOKER;

    public static Suit trumpSuit;

    public static void setTrumpSuit(Suit suit) {
        trumpSuit = suit;
    }

    public boolean isTrumpSuit() {
        return this == trumpSuit || this == SMALL_JOKER || this == BIG_JOKER;
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
}
