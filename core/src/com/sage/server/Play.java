package com.sage.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

class Play extends ServerCardList {
    private int playOrder;
    private int playHierarchicalNum; // This value is 0 if the play is a trash play, otherwise it's hierarchicalNum of the lowest card in the play
    private int[] playStructure; // playStructure represents the number of cards in each group (groups ordered by ascending hierarchicalValue)
    private Suit basePlaySuit;
    private Play trickBasePlay;
    private Player belongsTo;

    Play(int playOrder, ServerCardList cardsInPlay, Player belongsTo, Play basePlay) {
        super(cardsInPlay);
        this.playOrder = playOrder;
        this.belongsTo = belongsTo;

        // If this is the first play in the trick, set it as base play
        this.trickBasePlay = playOrder == 0 ? this : basePlay;
        this.basePlaySuit = playOrder == 0 ? get(0).suit().getEffectiveSuit() : trickBasePlay.getBasePlaySuit().getEffectiveSuit();

        sort();
        ArrayList<ServerCardList> groupedPlay = groupPlay();

        playHierarchicalNum = getPlayHierarchicalValue(groupedPlay);

        playStructure = getPlayStructure(groupedPlay);
    }

    private ArrayList<ServerCardList> groupPlay() {
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
        cardGroups.sort((c1, c2)->{
            Integer c1value = c1.get(0).getHierarchicalValue();
            Integer c2value = c2.get(0).getHierarchicalValue();

            return c1value.compareTo(c2value);
        });

        return cardGroups;
    }

    private int getPlayHierarchicalValue(ArrayList<ServerCardList> cardGroups) {
        // Each ServerCardList in cardGroups contains identical cards, and cardGroups is sorted by ascending hierarchicalValue

        // If the first group isn't in a valid suit, it's a trash play
        if(!cardGroups.get(0).get(0).isTrump()
                || cardGroups.get(0).get(0).getEffectiveSuit() != basePlaySuit) {
            return 0; // If the first group isn't valid, it's a trash play. All other groups will be checked against the first group
        }

        if(cardGroups.size() == 1) { // If there's only one group, it contains identical cards and is a valid play (suit of first group is already confirmed to be valid)
            return get(0).getHierarchicalValue();
        }

        // Checks if group lengths and hierarchical values are consecutive, and if suit is valid
        int lastGroupHierarchy = cardGroups.get(0).get(0).getHierarchicalValue();
        int lastGroupLength = cardGroups.get(0).size();
        for(ServerCardList group : cardGroups) {
            if(group.size() != lastGroupLength // If group length isn't consecutive, it's a trash play
                    || group.size() != lastGroupLength + 1
                    || group.size() != lastGroupLength - 1
                    || group.size() == 1) { // If length of group is 1, it's a trash play
                return 0;
            }

            // If the hierarchical value isn't consecutive, it's a trash play
            if(group.get(0).getHierarchicalValue() != lastGroupHierarchy + 1) {
                return 0;
            }

            if(group.get(0).getEffectiveSuit() != cardGroups.get(0).get(0).getEffectiveSuit()) {
                return 0; // If any group has a different suit than the the first group, it's a trash play
            }

            lastGroupLength = group.size();
            lastGroupHierarchy = group.get(0).getHierarchicalValue();
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
            playStructure[i] = cardGroups.size();
        }
        return playStructure;
    }

    int getPlayHierarchicalNum() {
        return playHierarchicalNum;
    }

    int getPlayOrder() {
        return playOrder;
    }

    int[] getPlayStructure() {
        return playStructure;
    }

    Suit getBasePlaySuit() {
        return basePlaySuit;
    }

    Player getPlayer() {
        return belongsTo;
    }

    // TODO: isLegal() function
    boolean isLegal() {
        return true;
    }

    void sort() {
        class CustomComparator implements Comparator<ServerCard> {
            @Override
            public int compare(ServerCard c1, ServerCard c2) {
                Integer c1value = c1.getHierarchicalValue();
                Integer c2value = c2.getHierarchicalValue();

                return c1value.compareTo(c2value);
            }
        }

        sort(new CustomComparator());
    }
}
