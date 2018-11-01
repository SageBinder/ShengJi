package com.sage;

public enum Rank {
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13),
    ACE(14),
    JOKER(15);

    public final int rankNum;

    Rank(int rankNum) {
        this.rankNum = rankNum;
    }

    @Override
    public String toString() {
        if(rankNum <= 10) {
            return Integer.toString(rankNum);
        } else {
            switch(this) {
                case JACK:
                    return "jack";
                case QUEEN:
                    return "queen";
                case KING:
                    return "king";
                case ACE:
                    return "ace";
                case JOKER:
                    return "joker";
                default:
                    return "";
            }
        }
    }

    public static Rank fromInt(int rankNum) {
        try {
            return values()[rankNum - 2];
        } catch(IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("No rank with value " + rankNum);
        }
    }
}
