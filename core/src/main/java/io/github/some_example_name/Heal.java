package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Heal {
    private Texture healTexture;
    private Array<Sprite> healSprites;
    private Rectangle healRectangle;
    private float healSpawnTimer;
    private float healSpawnInterval = 5f; // Интервал появления хилок
    private float fallSpeed = 2f;
    private Viewport viewport;

    public Heal(Texture healTexture, Viewport viewport) {
        this.healTexture = healTexture;
        this.viewport = viewport;
        this.healSprites = new Array<>();
        this.healRectangle = new Rectangle();
        this.healSpawnTimer = 0;
    }

    public void update(float delta, Rectangle playerRectangle, Runnable onHealCaught) {
        // Таймер появления новых хилок
        healSpawnTimer += delta;
        if (healSpawnTimer >= healSpawnInterval) {
            healSpawnTimer = 0;
            createHeal();
        }

        // Обновление положения хилок
        for (int i = healSprites.size - 1; i >= 0; i--) {
            Sprite healSprite = healSprites.get(i);
            healSprite.translateY(-fallSpeed * delta);
            healRectangle.set(healSprite.getX(), healSprite.getY(), healSprite.getWidth(), healSprite.getHeight());

            // Если хилки пропадают за нижней границей экрана
            if (healSprite.getY() < -healSprite.getHeight()) {
                healSprites.removeIndex(i);
            } else if (playerRectangle.overlaps(healRectangle)) {
                // Если персонаж поймал хилку
                healSprites.removeIndex(i);
                onHealCaught.run();
            }
        }
    }

    private void createHeal() {
        float healWidth = 0.5f;
        float healHeight = 0.5f;
        float worldWidth = viewport.getWorldWidth();

        Sprite healSprite = new Sprite(healTexture);
        healSprite.setSize(healWidth, healHeight);
        healSprite.setX(MathUtils.random(0f, worldWidth - healWidth));
        healSprite.setY(viewport.getWorldHeight());

        healSprites.add(healSprite);
    }

    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (Sprite healSprite : healSprites) {
            healSprite.draw(batch);
        }
    }

    public void dispose() {
        healTexture.dispose();
    }
}
