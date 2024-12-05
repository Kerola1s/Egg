package io.github.some_example_name;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.audio.Music;

public class GameOverScreen implements Screen {
    final Drop game;
    Texture backgroundTexture;
    Texture restartButtonTexture;
    Texture mainMenuButtonTexture;
    OrthographicCamera camera;
    Music gameOverMusic;

    float restartButtonX, restartButtonY, restartButtonWidth, restartButtonHeight;
    float mainMenuButtonX, mainMenuButtonY, mainMenuButtonWidth, mainMenuButtonHeight;

    public GameOverScreen(Drop game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 500);
    }

    @Override
    public void show() {
        backgroundTexture = new Texture("GameOverScreen.png");
        restartButtonTexture = new Texture("Button2.png");
        mainMenuButtonTexture = new Texture("Button5.png");

        // Позиции и размеры кнопки "Рестарт"
        restartButtonWidth = 150;
        restartButtonHeight = 60;
        restartButtonX = Gdx.graphics.getWidth() / 2 - restartButtonWidth / 2;
        restartButtonY = Gdx.graphics.getHeight() / 2 - restartButtonHeight - 20;

        // Позиции и размеры кнопки "Главное меню"
        mainMenuButtonWidth = 150;
        mainMenuButtonHeight = 60;
        mainMenuButtonX = Gdx.graphics.getWidth() / 2 - mainMenuButtonWidth / 2;
        mainMenuButtonY = restartButtonY - mainMenuButtonHeight - 20;

        // Загрузка музыки
        gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("pogodi.mp3"));
        gameOverMusic.setLooping(false);
        gameOverMusic.setVolume(0.5f);
        gameOverMusic.play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);  // Очищаем экран
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Отрисовка фона
        game.batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);

        // Отрисовка кнопок
        game.batch.draw(restartButtonTexture, restartButtonX, restartButtonY, restartButtonWidth, restartButtonHeight);
        game.batch.draw(mainMenuButtonTexture, mainMenuButtonX, mainMenuButtonY, mainMenuButtonWidth, mainMenuButtonHeight);

        game.batch.end();

        // Проверка кликов по кнопкам
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Инвертируем Y-координату

            // Если нажали на кнопку "Рестарт"
            if (touchX >= restartButtonX && touchX <= restartButtonX + restartButtonWidth &&
                touchY >= restartButtonY && touchY <= restartButtonY + restartButtonHeight) {
                game.setScreen(new GameScreen(game)); // Переход на игровой экран
                dispose();
            }

            // Если нажали на кнопку "Главное меню"
            if (touchX >= mainMenuButtonX && touchX <= mainMenuButtonX + mainMenuButtonWidth &&
                touchY >= mainMenuButtonY && touchY <= mainMenuButtonY + mainMenuButtonHeight) {
                game.setScreen(new MainMenuScreen(game)); // Переход на экран главного меню
                dispose();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        // Освобождение ресурсов
        backgroundTexture.dispose();
        restartButtonTexture.dispose();
        mainMenuButtonTexture.dispose();
        gameOverMusic.dispose();
    }
}
