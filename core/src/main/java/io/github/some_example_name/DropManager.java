package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;
import java.util.Map;

public class DropManager {
    private final Array<Sprite> drops;
    private final Texture[] dropTextures;
    private final Texture goldenEggTexture;
    private final Viewport viewport;
    private final Map<Texture, Integer> eggScores;
    private final Map<Texture, Float> eggSpawnRates;

    private float dropTimer;
    private float spawnInterval = 1f; // Базовый интервал спавна

    public DropManager(Texture[] dropTextures, Texture goldenEggTexture, Viewport viewport) {
        this.dropTextures = dropTextures;
        this.goldenEggTexture = goldenEggTexture;
        this.viewport = viewport;
        this.drops = new Array<>();
        this.dropTimer = 0;

        // Устанавливаем стоимость яиц
        eggScores = new HashMap<>();
        eggScores.put(dropTextures[0], 100);
        eggScores.put(dropTextures[1], 150);
        eggScores.put(dropTextures[2], 90);
        eggScores.put(dropTextures[3], 200);
        eggScores.put(dropTextures[4], 110);
        eggScores.put(goldenEggTexture, 2500);

        // Устанавливаем редкость яиц
        eggSpawnRates = new HashMap<>();
        eggSpawnRates.put(dropTextures[0], 0.4f); // Чаще всего
        eggSpawnRates.put(dropTextures[1], 0.3f);
        eggSpawnRates.put(dropTextures[2], 0.2f);
        eggSpawnRates.put(dropTextures[3], 0.1f);
        eggSpawnRates.put(dropTextures[4], 0.15f);
        eggSpawnRates.put(goldenEggTexture, 0.05f); // Очень редко
    }

    public void update(float delta, Rectangle bucketRectangle) {
        dropTimer += delta;

        // Спавн новых капель
        if (dropTimer > spawnInterval) {
            dropTimer = 0;
            spawnDrop();
        }

        // Обновление капель
        for (int i = drops.size - 1; i >= 0; i--) {
            Sprite drop = drops.get(i);
            drop.translateY(-2f * delta); // Скорость падения яйца

            if (drop.getY() < -drop.getHeight()) {
                drops.removeIndex(i); // Удаляем каплю, если она ушла за экран
                Gdx.app.log("DropManager", "Яйцо пропущено!");
            } else if (drop.getBoundingRectangle().overlaps(bucketRectangle)) {
                // Проверяем на пересечение с корзиной
                Gdx.app.log("DropManager", "Яйцо поймано!");
                int score = getScoreForTexture(drop.getTexture());
                Gdx.app.log("Score", "Текущий счёт: " + score);
                drops.removeIndex(i); // Удаляем пойманное яйцо
            }
        }
    }


    public Sprite getCaughtDrop(Rectangle bucketRectangle) {
        for (int i = drops.size - 1; i >= 0; i--) {
            Sprite drop = drops.get(i);
            if (bucketRectangle.overlaps(drop.getBoundingRectangle())) {
                drops.removeIndex(i);
                return drop;
            }
        }
        return null;
    }

    public void draw(SpriteBatch batch) {
        for (Sprite drop : drops) {
            drop.draw(batch);
        }
    }

    public void dispose() {
        for (Texture texture : dropTextures) {
            texture.dispose();
        }
        goldenEggTexture.dispose();
    }

    private void spawnDrop() {
        float worldWidth = viewport.getWorldWidth();
        Texture selectedTexture = getRandomDropTexture();

        Sprite drop = new Sprite(selectedTexture);
        drop.setSize(1, 1);
        drop.setPosition(MathUtils.random(0, worldWidth - drop.getWidth()), viewport.getWorldHeight());
        drops.add(drop);
    }

    private Texture getRandomDropTexture() {
        float random = MathUtils.random();
        float cumulativeProbability = 0;

        for (Map.Entry<Texture, Float> entry : eggSpawnRates.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (random <= cumulativeProbability) {
                return entry.getKey();
            }
        }

        return dropTextures[0]; // На случай ошибки, возвращаем самое дешевое яйцо
    }

    public int getScoreForTexture(Texture texture) {
        return eggScores.getOrDefault(texture, 0);
    }
}
