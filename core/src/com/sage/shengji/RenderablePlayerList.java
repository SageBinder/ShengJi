package com.sage.shengji;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.badlogic.gdx.math.MathUtils.*;

class RenderablePlayerList extends PlayerList<RenderablePlayer> {
    void render(SpriteBatch batch, Viewport viewport) {
        Vector2 center = new Vector2(viewport.getWorldWidth() / 2, (viewport.getWorldHeight() * 0.65f));

        float heightRadius = (viewport.getWorldHeight() / 5),
                widthRadius = (viewport.getWorldWidth() / 4);
        float pi2OverSize = PI2 / size();
        float shift = (indexOf(getThisPlayer()) * (PI2 / size())) + (PI / 2);

        for(int i = 0; i < size(); i++) {
            RenderablePlayer toRender = get(i);
            toRender.pos.x = (cos((i * pi2OverSize) - shift) * widthRadius) + center.x;
            toRender.pos.y = (sin((i * pi2OverSize) - shift) * heightRadius) + center.y;
            toRender.render(batch, viewport);
        }
    }
}
