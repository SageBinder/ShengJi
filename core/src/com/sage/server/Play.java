package com.sage.server;

import com.badlogic.gdx.Gdx;
import com.sage.Suit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// This class honestly should be immutable but I don't feel like doing that right now
class Play extends ServerCardList {
    private final int playOrder;
    private final int playHierarchicalNum; // This value is 0 if the play is a trash play, otherwise it's hierarchicalNum of the lowest card in the play
    private final int[] playStructure; // playStructure represents the number of cards in each group (groups ordered by ascending hierarchicalValue)
    private final Suit basePlayEffectiveSuit;
    private final Play trickBasePlay;
    private final Player belongsTo;

    Play(int playOrder, ServerCardList cardsInPlay, Player belongsTo, Play basePlay) {
        super(cardsInPlay);
        this.playOrder = playOrder;
        this.belongsTo = belongsTo;

        // If this is the first play in the trick, set it as base play
        this.trickBasePlay = playOrder == 0 ? this : basePlay;
        this.basePlayEffectiveSuit = playOrder == 0 ? get(0).getEffectiveSuit() : trickBasePlay.basePlayEffectiveSuit;

//        sort();
        ArrayList<ServerCardList> groupedPlay = groupPlay();

        playHierarchicalNum = getPlayHierarchicalValue(groupedPlay);

        playStructure = getPlayStructure(groupedPlay);
    }

    private ArrayList<ServerCardList> groupPlay() {
        // TODO: Use ServerCard object as key instead of Integer?
        // Key to hashmap corresponds to a card's cardnum
        HashMap<Integer, ServerCardList> cardGroupsHashMap = new HashMap<>();
        for(ServerCard c : this) {
            int cardNum = c.cardNum();
            if(cardGroupsHashMap.get(cardNum) == null) {
                cardGroupsHashMap.put(cardNum, new ServerCardList());
            }

            cardGroupsHashMap.get(cardNum).add(c);
        }

        ArrayList<ServerCardList> cardGroups = new ArrayList<>(cardGroupsHashMap.values());
        cardGroups.sort((c1, c2) -> {
            Integer c1value = c1.get(0).getHierarchicalValue();
            Integer c2value = c2.get(0).getHierarchicalValue();

            return c1value.compareTo(c2value);
        });

        return cardGroups;
    }

    private int getPlayHierarchicalValue(ArrayList<ServerCardList> cardGroups) {
        // Each ServerCardList in cardGroups contains identical cards, and cardGroups is sorted by ascending hierarchicalValue

        // These checks make sure this play complies with the base play. If this play IS the base play, then obviously
        // it complies with itself.
        if(this != trickBasePlay) {

            // playStructure of card groups (when groups are both sorted in ascending order) should be equivalent
            if(!Arrays.equals(playStructure, trickBasePlay.playStructure)) {
                return 0;
            }

            // The number of cards in this play should obviously be the same as the number of cards in the base play
            if(this.size() != trickBasePlay.size()) {
                return 0;
            }

            // If the first group isn't in a valid suit, it's a trash play.
            // If first group is a trump suit, it still may be a non-trash play.
            // All other groups will be checked against the first group to ensure that the entire play is the same suit
            if(!cardGroups.get(0).get(0).isTrump() && cardGroups.get(0).get(0).getEffectiveSuit() != basePlayEffectiveSuit) {
                return 0;
            }

            // At this point all we know is that the FIRST GROUP suit matches the base play's suit, that the number of cards
            // in this play is equal to the number of cards in the base play, and that the play structures are identical.
            // The hierarchy values still may not be valid (meaning, they may not be consecutive)
        }

        if(cardGroups.size() == 1) { // If there's only one group, it contains identical cards and is a valid play (suit of first group is already confirmed to be valid)
            return get(0).getHierarchicalValue();
        }

        // IMPORTANT: At this point we know only the first card group complies with the base play's suit. We need to
        // make sure all the other groups in this play match the first group's suit.

        // Checks if hierarchical values are consecutive, and if suit of every group is valid.
        int lastGroupHierarchicalValue = cardGroups.get(0).get(0).getHierarchicalValue();
        for(ServerCardList group : cardGroups) {
            // This check is theoretically only needed if this play is a base play, as a base play shouldn't have any
            // group with size() == 1, and we already know that this play follows the base play's structure.

            // If length of a group is 1, it's a trash play, but ONLY because we know there's more than one group
            if(group.size() == 1) {
                if(this != trickBasePlay) {
                    Gdx.app.log("Play.getPlayHierarchicalValue", "This play isn't the base play, matched " +
                            "the base play's play structure, but has a group of size 1. This shouldn't happen because " +
                            "the base play shouldn't have been allowed with a group size of 1 (at this point it is " +
                            "known that the play has more than one group.");
                }
                return 0;
            }

            // If the hierarchical value isn't consecutive, it's a trash play.
            if(group.get(0).getHierarchicalValue() != lastGroupHierarchicalValue + 1) {
                return 0;
            }

            // If any group has a different suit than the the first group, it's a trash play
            if(group.get(0).getEffectiveSuit() != cardGroups.get(0).get(0).getEffectiveSuit()) {
                return 0;
            }

            lastGroupHierarchicalValue = group.get(0).getHierarchicalValue();
        }

        // At this point we know the play is a valid play, in either the base or trump suit.
        // This doesn't mean that it is legal, however, as the player may have played trump when
        // they still had the base suit in their hand.
        // The returned value is the lowest value in the play
        return cardGroups.get(0).get(0).getHierarchicalValue();
    }

    private int[] getPlayStructure(ArrayList<ServerCardList> cardGroups) {
        int[] playStructure = new int[cardGroups.size()];
        for(int i = 0; i < cardGroups.size(); i++) {
            playStructure[i] = cardGroups.get(i).size();
        }
        return playStructure;
    }

    int getPlayHierarchicalNum() {
        return playHierarchicalNum;
    }

    int getPlayOrder() {
        return playOrder;
    }

    Player getPlayer() {
        return belongsTo;
    }

    // TODO: isLegal() function
    boolean isLegal() {
        // You cannot start a trick with a trash play
        return trickBasePlay != this || playHierarchicalNum != 0;
    }

//    void sort() {
//        class CustomComparator implements Comparator<ServerCard> {
//            @Override
//            public int compare(ServerCard c1, ServerCard c2) {
//                Integer c1value = c1.getHierarchicalValue();
//                Integer c2value = c2.getHierarchicalValue();
//
//                return c1value.compareTo(c2value);
//            }
//        }
//
//        sort(new CustomComparator());
//    }
}
