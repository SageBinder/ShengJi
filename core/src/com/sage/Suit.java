package com.sage;

public enum Suit {
    HEARTS(0),
    CLUBS(1),
    DIAMONDS(2),
    SPADES(3),
    SMALL_JOKER(-2),
    BIG_JOKER(-1);

    public int suitNum;

    Suit(int suitNum) {
        this.suitNum = suitNum;
    }

    public boolean isJoker() {
        return this == SMALL_JOKER || this == BIG_JOKER;
    }

    @Override
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
