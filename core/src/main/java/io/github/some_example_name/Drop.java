package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Drop extends Game {
    public SpriteBatch batch;
    public ScoreManager scoreManager;
    public GameScreen gameScreen;
    public BitmapFont font; // Добавляем объект шрифта

    @Override
    public void create() {
        batch = new SpriteBatch();
        scoreManager = new ScoreManager();
        gameScreen = new GameScreen(this);

        // Инициализация шрифта
        font = new BitmapFont();
        font.getData().setScale(2); // Настройка размера шрифта
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose(); // Не забудьте освободить ресурсы
    }

}
