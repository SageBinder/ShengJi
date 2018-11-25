package com.sage.shengji;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.Rank;
import com.sage.Team;

class RenderablePlayer extends Player {
    static BitmapFont nameFont = new BitmapFont();
    static int maxNameChars = 24;

    private GlyphLayout nameLayout = new GlyphLayout(nameFont, getName(17));
    Color nameColor = new Color(Color.WHITE);

    Vector2 pos = new Vector2();

    private RenderablePlay play = new RenderablePlay();

    RenderablePlayer(int playerNum, String name) {
        super(playerNum, name);
    }

    RenderablePlayer(int playerNum, String name, Rank callRank) {
        super(playerNum, name, callRank);
    }

    void render(SpriteBatch batch, Viewport viewport) {
        nameFont.setColor(nameColor);
        nameFont.draw(batch, getName(17),
                pos.x, pos.y + (nameFont.getXHeight() * 2),
                0, Align.center, false);

        play.cardHeight = viewport.getWorldHeight() * 0.11f;
        play.regionWidth = ((play.prefDivisionProportion * 6) + (1)) * play.cardHeight * RenderableCard.WIDTH_TO_HEIGHT_RATIO;
        play.pos.y = this.pos.y - play.cardHeight;
        play.pos.x = this.pos.x - (play.regionWidth / 2);

        play.render(batch, viewport);
    }

    RenderablePlayer setX(float x) {
        pos.x = x;
        return this;
    }

    RenderablePlayer setY(float y) {
        pos.y = y;
        return this;
    }

    RenderablePlayer setPos(Vector2 pos) {
        this.pos.set(pos);
        return this;
    }

    void addToPlay(RenderableCardList list) {
        play.addAll(list);
    }

    void addToPlay(RenderableCard c) {
        play.add(c);
    }

    void clearPlay() {
        play.clear();
    }

    RenderablePlay getPlay() {
        return play;
    }

    @Override
    void setTeam(Team team) {
        super.setTeam(team);
        nameColor.set(
                team == Team.COLLECTORS ? Color.ORANGE :
                team == Team.KEEPERS ? Color.GREEN :
                Color.WHITE);
    }

    @Override
    void setName(String newName) {
        super.setName(newName);
        nameLayout.setText(nameFont, newName);
    }
}
