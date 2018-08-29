package com.sage.server;

public class ServerCodes { // Codes are negative so as to not clash with the sending of card information
    public static final int INVALID_CALL = -1;
    public static final int NO_CALL = -2;
    public static final int SUCCESSFUL_CALL = -3;
    public static final int SEND_CALL = -4;
    public static final int WAIT_FOR_OTHER_PLAYER_CALL = -5;
    public static final int WAIT_FOR_CALL_WINNER = -6;

    public static final int WAIT_FOR_KITTY = -100;
    public static final int WAIT_FOR_HAND = -101;
    public static final int SEND_KITTY_REPLACEMENTS = -102;
    public static final int SEND_FRIEND_CARDS = -102;
    public static final int WAIT_FOR_FRIEND_CARDS = -103;

    public static final int SEND_PLAY_LENGTH = -200;
    public static final int WAIT_FOR_PLAY_LENGTH = -201;
    public static final int SEND_PLAY = -202;
    public static final int WAIT_FOR_PLAY = -203;
    public static final int INVALID_PLAY = -204;
    public static final int WAIT_FOR_NEW_PLAYER_TEAM = -205;
    public static final int WAIT_FOR_TRICK_WINNER = -206;
    public static final int WAIT_FOR_TRICK_POINT_CARDS = -207;
    public static final int WAIT_FOR_ROUND_WINNERS = -208;
    public static final int WAIT_FOR_COLLECTED_POINTS = -209;
    public static final int WAIT_FOR_CALLING_NUMBERS = -210;

    public static final int WAIT_FOR_PLAYER_ORDER = -300;
}
