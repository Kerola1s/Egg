package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

public class EnemyManager {
    private final Texture enemyTexture;
    private final Array<Sprite> enemies;
    private final Viewport viewport;
    private float spawnTimer = 0;
    private float spawnInterval = 5f;

    public EnemyManager(Texture enemyTexture, Viewport viewport) {
        this.enemyTexture = enemyTexture;
        this.viewport = viewport;
        this.enemies = new Array<>();
    }

    public void updateEnemies(float delta, Rectangle playerRectangle, Runnable onPlayerHit) {
        spawnTimer += delta;

        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            spawnEnemy();
            spawnInterval = Math.max(1f, spawnInterval * 0.95f); // Ускоряем спавн со временем
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Sprite enemy = enemies.get(i);
            enemy.translateY(-3f * delta); // Скорость падения врагов

            if (enemy.getY() + enemy.getHeight() < 0) {
                enemies.removeIndex(i); // Удаляем врагов, упавших за экран
            } else if (enemy.getBoundingRectangle().overlaps(playerRectangle)) {
                enemies.removeIndex(i);
                onPlayerHit.run(); // Уведомляем о попадании врага в игрока
            }
        }
    }

    public void drawEnemies(Batch batch) {
        for (Sprite enemy : enemies) {
            enemy.draw(batch);
        }
    }

    private void spawnEnemy() {
        Sprite enemy = new Sprite(enemyTexture);
        enemy.setSize(1f, 1f);
        enemy.setPosition(MathUtils.random(0, viewport.getWorldWidth() - enemy.getWidth()), viewport.getWorldHeight());
        enemies.add(enemy);
    }

    public void dispose() {
        enemyTexture.dispose();
    }
}
