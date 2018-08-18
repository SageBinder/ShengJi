package com.sage.shengji;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

class Constants {
    static final Color BACKGROUND_COLOR = new Color(0, 0.2f, 0.11f, 1);

    // Game screen constants
    static final float GAME_WORLD_SIZE = 5f;

    static final float CARD_WIDTH = 1f;
    static final float CARD_HEIGHT = (323f / 222f) * CARD_WIDTH;


    // Start screen constants:
    static final float START_WORLD_SIZE = 100f;
    static final float START_WORLD_BUTTON_WIDTH = START_WORLD_SIZE * 0.3f;
    static final float START_WORLD_BUTTON_HEIGHT = START_WORLD_SIZE * 0.1f;

    static final Color START_WORLD_BUTTON_COLOR = new Color(0.827f, 0.827f, 0.827f, 1);

    static final float START_FONT_REFERENCE_SCREEN_SIZE = START_WORLD_SIZE / 220f;

    static final Vector2 CREATE_GAME_BUTTON_POS = new Vector2((START_WORLD_SIZE - START_WORLD_BUTTON_WIDTH) * 0.5f,
            (START_WORLD_SIZE * (5f / 6f)) - (START_WORLD_BUTTON_HEIGHT * 0.5f));
    static final Vector2 JOIN_GAME_BUTTON_POS = new Vector2((START_WORLD_SIZE - START_WORLD_BUTTON_WIDTH) * 0.5f,
            (START_WORLD_SIZE * (3f / 6f)) - (START_WORLD_BUTTON_HEIGHT * 0.5f));
    static final Vector2 OPTIONS_BUTTON_POS = new Vector2((START_WORLD_SIZE - START_WORLD_BUTTON_WIDTH) * 0.5f,
            (START_WORLD_SIZE * (1f / 6f)) - (START_WORLD_BUTTON_HEIGHT * 0.5f));
}
