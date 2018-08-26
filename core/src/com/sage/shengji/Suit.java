package com.sage.shengji;

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
}
