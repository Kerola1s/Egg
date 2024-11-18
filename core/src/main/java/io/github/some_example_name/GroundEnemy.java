package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class GroundEnemy extends Sprite {
    private boolean moveRight; // Направление движения

    public GroundEnemy(Texture texture, boolean moveRight) {
        super(texture);
        this.moveRight = moveRight;
    }

    public boolean isMovingRight() {
        return moveRight;
    }
}
