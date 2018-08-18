package com.sage.shengji;

import com.badlogic.gdx.Game;

public class ShengJiGame extends Game {
    @Override
    public void create() {
        setScreen(new StartScreen(this));
    }

    public void showGameScreen() {
        setScreen(new GameScreen(this));
    }
}
