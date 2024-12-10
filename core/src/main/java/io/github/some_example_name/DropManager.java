package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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
    private Sound dropSound;
    private final ScoreManager scoreManager;
    private int caughtEggsCount = 0; // Счётчик пойманных яиц
    private int victoryEggCount = 10; // Количество для победы

    private float dropTimer;
    private float spawnInterval = 3f; // Начальный интервал спавна
    private float dropSpeed = 1f; // Начальная скорость падения

    public DropManager(Texture[] dropTextures, Texture goldenEggTexture, Viewport viewport, ScoreManager scoreManager) {
        this.dropTextures = dropTextures;
        this.goldenEggTexture = goldenEggTexture;
        this.viewport = viewport;
        this.scoreManager = scoreManager; // Инициализация переданного ScoreManager
        this.drops = new Array<>();
        this.dropTimer = 0;
        this.dropSound = Gdx.audio.newSound(Gdx.files.internal("CatchEggSound.mp3"));

        // Устанавливаем стоимость для всех 15 яиц
        eggScores = new HashMap<>();
        for (int i = 0; i < 5; i++) eggScores.put(dropTextures[i], MathUtils.random(200, 250)); // Дешевые
        for (int i = 5; i < 10; i++) eggScores.put(dropTextures[i], MathUtils.random(550, 650)); // Средние
        for (int i = 10; i < 13; i++) eggScores.put(dropTextures[i], MathUtils.random(20000, 20001)); // Дорогие
        eggScores.put(dropTextures[13], 30000); // Очень дорогие
        eggScores.put(dropTextures[14], 30000); // Очень дорогие

        eggScores.put(goldenEggTexture, 40000);

        // Устанавливаем редкость для всех 15 яиц
        eggSpawnRates = new HashMap<>();
        for (int i = 0; i < 5; i++) eggSpawnRates.put(dropTextures[i], 0.4f); // Дешевые чаще всего
        for (int i = 5; i < 10; i++) eggSpawnRates.put(dropTextures[i], 0.3f); // Средние
        for (int i = 10; i < 13; i++) eggSpawnRates.put(dropTextures[i], 0.15f); // Дорогие
        eggSpawnRates.put(dropTextures[13], 0.05f); // Очень редкие
        eggSpawnRates.put(dropTextures[14], 0.02f); // Очень редкие
        eggSpawnRates.put(goldenEggTexture, 0.01f); // Золотое яйцо — самое редкое
    }

    public void update(float delta, Rectangle bucketRectangle, Runnable onMissedDrop) {
        dropTimer += delta;

        if (spawnInterval > 1f) spawnInterval -= delta * 0.01f;
        dropSpeed += delta * 0.02f;

        if (dropTimer > spawnInterval) {
            dropTimer = 0;
            spawnDrop();
        }

        for (int i = drops.size - 1; i >= 0; i--) {
            Sprite drop = drops.get(i);
            drop.translateY(-dropSpeed * delta);

            if (drop.getY() < -drop.getHeight()) {
                drops.removeIndex(i);
                onMissedDrop.run();
            } else if (drop.getBoundingRectangle().overlaps(bucketRectangle)) {
                drops.removeIndex(i);
                dropSound.play();

                caughtEggsCount++;

                int score = getScoreForTexture(drop.getTexture());
                scoreManager.addToTotalScore(score);
                Gdx.app.log("DropManager", "Caught egg! + " + score + " points. Total: " + scoreManager.getTotalScore());
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (Sprite drop : drops) {
            drop.draw(batch);
        }
    }

    public int getCaughtEggsCount() {
        return caughtEggsCount;
    }

    public boolean isVictoryAchieved() {
        return caughtEggsCount >= victoryEggCount;
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

    public void dispose() {
        for (Texture texture : dropTextures) {
            texture.dispose();
        }
        goldenEggTexture.dispose();
        dropSound.dispose();
    }

    private void spawnDrop() {
        float worldWidth = viewport.getWorldWidth();
        Texture selectedTexture = getRandomDropTexture();

        Sprite drop = new Sprite(selectedTexture);
        drop.setSize(0.5f, 0.5f); // Уменьшаем размер спрайтов
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
