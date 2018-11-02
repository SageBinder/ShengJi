package com.sage;

import java.util.Random;

public abstract class Card implements Comparable {
    private final Rank rank;
    private final Suit suit;
    private final int cardNum;
    private int hierarchicalValue;

    private boolean wasTrumpRank = false;
    private boolean wasTrumpSuit = false;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;

        cardNum = getCardNumFromRankAndSuit(rank, suit);
        establishHierarchicalValue();
    }

    public Card(int cardNum) {
        this.cardNum = cardNum;
        this.rank = getRankFromCardNum(cardNum);
        this.suit = getSuitFromCardNum(cardNum);
        establishHierarchicalValue();
    }

    public Card(Card c) {
        this.suit = c.suit;
        this.rank = c.rank;
        this.cardNum = c.cardNum;
        this.hierarchicalValue = c.hierarchicalValue;
        this.wasTrumpSuit = c.wasTrumpSuit;
        this.wasTrumpRank = c.wasTrumpRank;
    }

    // Sets this card to be a random card
    public Card() {
        this(new Random().nextInt(54));
        establishHierarchicalValue();
    }

    private void establishHierarchicalValue() {
        if(suit == Suit.BIG_JOKER) {
            hierarchicalValue = 31;
        } else if(suit == Suit.SMALL_JOKER) {
            hierarchicalValue = 30;
        } else if(isTrumpSuit() && isTrumpRank()) {
            hierarchicalValue = 29;
        } else if(isTrumpRank()) {
            hierarchicalValue = 28;
        } else if(isTrumpSuit()) {
            hierarchicalValue = 13 + rank.rankNum;
        } else {
            hierarchicalValue = rank.rankNum;
        }
    }

    public int getHierarchicalValue() {
        // If trump suit/rank has since changed, reestablish hierarchical value
        if(wasTrumpRank != isTrumpRank() || wasTrumpSuit != isTrumpSuit()) {
            establishHierarchicalValue();
            wasTrumpRank = isTrumpRank();
            wasTrumpSuit = isTrumpSuit();
        }

        return hierarchicalValue;
    }

    public Suit suit() {
        return suit;
    }

    public Rank rank() {
        return rank;
    }

    public boolean isJoker() {
        return suit.isJoker();
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
            return ((rank.rankNum - 2) * 4) + suit.suitNum;
        }
    }

    public abstract boolean isTrumpSuit();

    public abstract boolean isTrumpRank();

    public abstract boolean isTrump();

    // Two cards are considered equal simply if they have to same suit and rank.
    public boolean valueEquals(Card c) {
            return this.cardNum == c.cardNum;
    }

    @Override
    public int compareTo(Object o) {
        Card compareCard = (Card)o;

        if(this.isTrump() && compareCard.isTrump()) {
            int hierarchyCompare = Integer.compare(this.getHierarchicalValue(), compareCard.getHierarchicalValue());

            if(hierarchyCompare == 0) {
                return Integer.compare(suit.suitNum, compareCard.suit.suitNum);
            } else {
                return Integer.compare(this.getHierarchicalValue(), compareCard.getHierarchicalValue());
            }
        } else if(this.isTrump()) {
            return -1;
        } else if(compareCard.isTrump()) {
            return 1;
        }

        if(suit == compareCard.suit) {
            return Integer.compare(rank.rankNum, compareCard.rank.rankNum);
        } else {
            return Integer.compare(suit.suitNum, compareCard.suit.suitNum);
        }
    }

    @Override
    public String toString() {
        return rank.toString() + " of " + suit.toString();
    }
}