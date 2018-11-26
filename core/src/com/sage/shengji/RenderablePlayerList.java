package com.sage.shengji;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.badlogic.gdx.math.MathUtils.*;

// TODO: https://stackoverflow.com/questions/6972331/how-can-i-generate-a-set-of-points-evenly-distributed-along-the-perimeter-of-an
// Make the arc length between two players the same (instead of having angle between two players the same)
class RenderablePlayerList extends PlayerList<RenderablePlayer> {
    Vector2 centerProportion = new Vector2(0.57f, 0.66f);
    // 0.6f, 0.7f

    void render(SpriteBatch batch, Viewport viewport) {
        Vector2 center =
                new Vector2(
                        this.centerProportion.x * viewport.getWorldWidth(),
                        this.centerProportion.y * viewport.getWorldHeight()
                );

        float heightRadius = viewport.getWorldHeight() * 0.22f,
                widthRadius = viewport.getWorldWidth() * 0.35f;
        float angleIncrement = PI2 / size();
        float shift = (indexOf(getThisPlayer()) * (PI2 / size())) + (PI / 2);

        for(int i = 0; i < size(); i++) {
            RenderablePlayer toRender = get(i);
            toRender.pos.x = (cos((i * angleIncrement) - shift) * widthRadius) + center.x;
            toRender.pos.y = (sin((i * angleIncrement) - shift) * heightRadius) + center.y;
            toRender.pos.y += toRender.getPlay().cardHeight * 0.5f;
            toRender.render(batch, viewport);

            toRender.getPoints().cardHeight = toRender.getPlay().cardHeight * 0.5f;
            toRender.getPoints().regionWidth = toRender.getPlay().regionWidth;
            toRender.getPoints().pos.x = toRender.getPlay().pos.x;
            toRender.getPoints().pos.y = toRender.getPlay().pos.y - (toRender.getPoints().cardHeight * 1.05f);
            toRender.getPoints().prefDivisionProportion = 1.1f;
            toRender.getPoints().render(batch, viewport, true);
        }
    }
}
