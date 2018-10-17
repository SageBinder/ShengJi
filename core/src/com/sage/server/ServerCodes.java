package com.sage.server;

public class ServerCodes { // Codes are negative so as to not clash with the sending of card information
    // Calling codes:
    public static final int INVALID_CALL = -1;
    public static final int NO_CALL = -2;
    public static final int UNSUCCESSFUL_CALL = -3;
    public static final int SUCCESSFUL_CALL = -4;
    public static final int SEND_CALL = -5;
    public static final int WAIT_FOR_NEW_WINNING_CALL = -6;
    public static final int WAIT_FOR_CALL_WINNER = -7;
    public static final int NO_ONE_CALLED = -8;

    // Game setup codes:
    public static final int ROUND_START = -100;
    public static final int WAIT_FOR_PLAYER_ORDER = -101;
    public static final int WAIT_FOR_KITTY = -102;
    public static final int WAIT_FOR_HAND = -103;
    public static final int SEND_KITTY = -104;
    public static final int INVALID_KITTY = -105;
    public static final int SEND_FRIEND_CARDS = -106;
    public static final int WAIT_FOR_FRIEND_CARDS = -107;
    public static final int INVALID_FRIEND_CARDS = -108;

    // Game codes:
    public static final int TRICK_START = -200;
    public static final int SEND_BASE_PLAY = -201;
    public static final int SEND_PLAY = -202;
    public static final int WAIT_FOR_TURN_PLAYER = -203;
    public static final int WAIT_FOR_PLAY = -204;
    public static final int INVALID_PLAY = -205;
    public static final int WAIT_FOR_NEW_PLAYER_TEAM = -206;
    public static final int WAIT_FOR_PLAYER_IN_LEAD = -207;
    public static final int WAIT_FOR_TRICK_WINNER = -208;
    public static final int WAIT_FOR_TRICK_POINT_CARDS = -209;
    public static final int WAIT_FOR_ROUND_WINNERS = -210;
    public static final int WAIT_FOR_NUM_COLLECTED_POINTS = -211;
    public static final int WAIT_FOR_CALLING_NUMBERS = -212;
    public static final int ROUND_OVER = -213;

    // Lobby codes
    public static final int JOIN_SUCCESSFUL = -300;
    public static final int CONNECTION_DENIED = -301;
    public static final int WAIT_FOR_PLAYERS_LIST = -302;

    public static final int PING = -400;
}
