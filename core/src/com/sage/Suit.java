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

    public static Suit getSuitFromNum(int num) {
        switch(num) {
            case 0:
                return HEARTS;
            case 1:
                return CLUBS;
            case 2:
                return DIAMONDS;
            case 3:
                return SPADES;
            case -2:
                return SMALL_JOKER;
            case -1:
                return BIG_JOKER;
            default:
                return HEARTS;
        }
    }
}
