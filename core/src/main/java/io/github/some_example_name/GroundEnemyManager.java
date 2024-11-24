package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GroundEnemyManager {
    private final Texture groundEnemyTexture;
    private final Array<Sprite> groundEnemies;
    private final Viewport viewport;
    private float spawnTimer = 0;
    private float spawnInterval = 7f;

    public GroundEnemyManager(Texture groundEnemyTexture, Viewport viewport) {
        this.groundEnemyTexture = groundEnemyTexture;
        this.viewport = viewport;
        this.groundEnemies = new Array<>();
    }

    public void updateGroundEnemies(float delta, Rectangle playerRectangle, Runnable onPlayerHit) {
        spawnTimer += delta;

        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            spawnGroundEnemy();
            spawnInterval = Math.max(2f, spawnInterval * 0.98f); // Ускоряем спавн со временем
        }

        for (int i = groundEnemies.size - 1; i >= 0; i--) {
            Sprite groundEnemy = groundEnemies.get(i);
            float speed = 2f; // Скорость движения врага

            // Движение врага
            if (groundEnemy.getX() < viewport.getWorldWidth() - groundEnemy.getWidth()) {
                groundEnemy.translateX(speed * delta); // Движение вправо
            } else {
                // Удаляем врагов, которые ушли за пределы экрана
                groundEnemies.removeIndex(i);
            }

            if (groundEnemy.getBoundingRectangle().overlaps(playerRectangle)) {
                groundEnemies.removeIndex(i);
                onPlayerHit.run(); // Уведомляем о попадании врага в игрока
            }
        }
    }

    public void drawGroundEnemies(Batch batch) {
        for (Sprite groundEnemy : groundEnemies) {
            groundEnemy.draw(batch);
        }
    }

    private void spawnGroundEnemy() {
        Sprite groundEnemy = new Sprite(groundEnemyTexture);
        groundEnemy.setSize(1f, 1f);

        // Спавн врага за пределами экрана
        float startY = 0; // Земля всегда на высоте 0
        boolean spawnFromLeft = MathUtils.randomBoolean(); // Случайно выбираем сторону появления
        float startX = spawnFromLeft
            ? -groundEnemy.getWidth() // Появление слева за экраном
            : viewport.getWorldWidth(); // Появление справа за экраном
        groundEnemy.setPosition(startX, startY);

        groundEnemies.add(groundEnemy);
    }

    public void dispose() {
        groundEnemyTexture.dispose();
    }
}
