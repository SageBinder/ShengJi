package com.sage.server;

import com.badlogic.gdx.Gdx;
import com.sage.Suit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

class Play extends ServerCardList {
    private final int playOrder;
    private final int playHierarchicalNum; // This value is 0 if the play is a trash play, otherwise it's hierarchicalNum of the lowest card in the play
    private final int[] playStructure; // playStructure represents the number of cards in each group (groups ordered by ascending hierarchicalValue)
    private final Suit basePlayEffectiveSuit;
    private final Play trickBasePlay;
    private final Player belongsToPlayer;
    ArrayList<ServerCardList> groupedPlay;

    Play(int playOrder, ServerCardList cardsInPlay, Player belongsToPlayer, Play basePlay) {
        super(cardsInPlay);
        this.playOrder = playOrder;
        this.belongsToPlayer = belongsToPlayer;

        // If this is the first play in the trick, set it as base play
        this.trickBasePlay = playOrder == 0 ? this : basePlay;
        this.basePlayEffectiveSuit = playOrder == 0 ? get(0).getEffectiveSuit() : trickBasePlay.basePlayEffectiveSuit;

        groupedPlay = groupServerCardList(this);

        playStructure = getPlayStructure(groupedPlay);
        playHierarchicalNum = getPlayHierarchicalValue(groupedPlay);
    }

    private ArrayList<ServerCardList> groupServerCardList(ArrayList<ServerCard> list) {
        // Key to hashmap corresponds to a card's cardnum
        HashMap<Integer, ServerCardList> cardGroupsHashMap = new HashMap<>();
        for(ServerCard c : list) {
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
        // Each ServerCardList in cardGroups contains identical cards,
        // and cardGroups is sorted by ascending hierarchicalValue

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
            if(!cardGroups.get(0).get(0).isTrump()
                    && cardGroups.get(0).get(0).getEffectiveSuit() != basePlayEffectiveSuit) {
                return 0;
            }

            // At this point all we know is that the FIRST GROUP suit matches the base play's suit, that the number of cards
            // in this play is equal to the number of cards in the base play, and that the play structures are identical.
            // The hierarchy values still may not be valid (meaning, they may not be consecutive)
        }

        // If there's only one group, it contains identical cards and is a valid play
        // (suit of first group is already confirmed to be valid)
        if(cardGroups.size() == 1) {
            return get(0).getHierarchicalValue();
        }

        // IMPORTANT: At this point we know only the first card group complies with the base play's suit. We need to
        // make sure all the other groups in this play match the first group's suit.

        // Checks if hierarchical values are consecutive, and if suit of every group is valid.
        int lastGroupHierarchicalValue = cardGroups.get(0).get(0).getHierarchicalValue() - 1;
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
        return belongsToPlayer;
    }

    boolean isTrashPlay() {
        return playHierarchicalNum == 0;
    }

    boolean isLegal() {
        // If player's hand doesn't contain all cards in play, obviously it's not legal
        if(!belongsToPlayer.getHand().containsAll(this)) {
            return false;
        }


        // We can assume the play is determined to be a trash play if it's not trump and a different suit than base play
        if(isTrashPlay()) {
            // You cannot start a trick with a trash play
            if(trickBasePlay == this) {
                return false;
            }

            int[] basePlayStructure = Arrays.copyOf(trickBasePlay.playStructure, trickBasePlay.playStructure.length);
            Arrays.sort(basePlayStructure);
            ArrayList<ServerCardList> groupedHandInBaseSuit =
                    groupServerCardList(
                            (ArrayList<ServerCard>)belongsToPlayer.getHand().stream()
                                    .filter(c -> c.getEffectiveSuit() == basePlayEffectiveSuit)
                                    .collect(Collectors.toList()));
            ArrayList<ServerCardList> thisGroupedPlayCopy = new ArrayList<>(groupedPlay);

            // Iterate backwards because basePlayStructure is sorted in ascending order but needs to be checked in
            // descending order
            for(int playStructureIdx = basePlayStructure.length - 1; playStructureIdx >= 0; playStructureIdx--) {
                // This inner loop is because even if hand can't satisfy basePlayStructure[i], the player still needs
                // to satisfy it as much as possible (i.e if base play is a triple, the player must play a double when
                // the hand can't satisfy a triple).
                for(int i = basePlayStructure[playStructureIdx]; i >= 1; i--) {
                    final int iLambda = i; // rrrrrrreeeeeeeeeeeeeeeeeeeee variables used in lambda must be final reeeee

                    // If the hand has cards which adhere to basePlayStructure[i], but this play doesn't, then it can't
                    // be a legal play.
                    if(groupedHandInBaseSuit.stream().anyMatch(handGroup -> handGroup.size() == iLambda)) {
                        if(thisGroupedPlayCopy.stream().noneMatch(
                                playGroup -> playGroup.size() == iLambda
                                        && playGroup.get(0).getEffectiveSuit() == basePlayEffectiveSuit)) {
                            return false;
                        }

                        // Remove the hand group that adheres to basePlayStructure because it can't be used to adhere to
                        // another value in basePlayStructure.
                        for(var group : groupedHandInBaseSuit) {
                            if(group.size() == i) {
                                groupedHandInBaseSuit.remove(group);
                                break;
                            }
                        }

                        // Remove that group that satisfied the basePlayStructure[i] so that we don't count it twice
                        for(var group : thisGroupedPlayCopy) {
                            if(group.size() == i && group.get(0).getEffectiveSuit() == basePlayEffectiveSuit) {
                                thisGroupedPlayCopy.remove(group);
                                break;
                            }
                        }

                        // If the largest matching group didn't match all of basePlayStructure[playStructureIdx], then
                        // basePlayStructure[playStructureIdx] still needs to be matched by the next largest group
                        if(i != basePlayStructure[playStructureIdx]) {
                            basePlayStructure[playStructureIdx] -= i;
                            i++; // Prevent i from decreasing
                        } else {
                            break;
                        }
                    }
                }
            }
            // The previous loop would have already returned false if the hand contained a possible match which the play
            // didn't contain, so we know the play contains all possible matches to base play and is legal
            return true;
        } else {
            // If it's not a trash play and it's the base play, it's legal
            if(trickBasePlay == this) {
                return true;
            }

            // If this play is trump and the basePlay is trump, this play must be legal because it's already known to be
            // a non-trash play.
            if(get(0).isTrump() && basePlayEffectiveSuit != RoundRunner.trumpSuit) {
                // If this play is a non-trash trump play, but the player still has basePlaySuit in their hand, it's not
                // a legal play.
                return belongsToPlayer.getHand().stream().noneMatch(c -> c.getEffectiveSuit() == basePlayEffectiveSuit);
            } else { // If this play is not trash and not trump
                return true;
            }
        }
    }
}
