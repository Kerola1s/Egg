package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements Screen {
    Texture playButtonTexture;
    Texture exitButtonTexture;
    float playButtonX, playButtonY, playButtonWidth, playButtonHeight;
    float exitButtonX, exitButtonY, exitButtonWidth, exitButtonHeight;
    final Drop game;
    OrthographicCamera camera;
    Texture backgroundTexture;
    private ScoreManager scoreManager;
    private Music backgroundMusic;
    private Sound buttonSound;

    public MainMenuScreen(Drop drop) {
        this.game = drop;
        scoreManager = new ScoreManager();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 500);
    }

    @Override
    public void show() {
        // Загрузка текстур
        backgroundTexture = new Texture("BackgroundMenu.png");
        playButtonTexture = new Texture("Button1.png");
        exitButtonTexture = new Texture("Button5.png");

        // Установка размеров и позиций кнопок
        playButtonWidth = 200;
        playButtonHeight = 150;
        playButtonX = Gdx.graphics.getWidth() / 2 - playButtonWidth / 2 - 250;
        playButtonY = Gdx.graphics.getHeight() / 2 - playButtonHeight / 2 - 150;

        exitButtonWidth = 50; // Уменьшенная ширина кнопки выхода
        exitButtonHeight = 50; // Уменьшенная высота кнопки выхода
        exitButtonX = Gdx.graphics.getWidth() - exitButtonWidth - 10; // Смещение от правого края экрана
        exitButtonY = Gdx.graphics.getHeight() - exitButtonHeight - 10; // Смещение от верхнего края экрана

        // Загрузка фоновой музыки и звука кнопки
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("MainTheme.mp3"));
        buttonSound = Gdx.audio.newSound(Gdx.files.internal("ButtonSound.mp3"));

        // Запуск фоновой музыки
        backgroundMusic.setLooping(true); // Повтор музыки
        backgroundMusic.setVolume(0.5f); // Громкость (0.0 до 1.0)
        backgroundMusic.play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Отрисовка фона
        game.batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);

        // Отрисовка кнопок
        game.batch.draw(playButtonTexture, playButtonX, playButtonY, playButtonWidth, playButtonHeight);
        game.batch.draw(exitButtonTexture, exitButtonX, exitButtonY, exitButtonWidth, exitButtonHeight);

        // Отрисовка общего счёта
        int totalScore = scoreManager.getTotalScore();
        game.font.draw(game.batch, "Total Score: " + totalScore, 10, camera.viewportHeight - 30);

        game.batch.end();

        // Проверка клика по кнопкам
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Инвертируем Y-координату

            // Проверка нажатия кнопки "Играть"
            if (touchX >= playButtonX && touchX <= playButtonX + playButtonWidth &&
                touchY >= playButtonY && touchY <= playButtonY + playButtonHeight) {
                buttonSound.play(); // Воспроизведение звука кнопки
                game.setScreen(new GameScreen(game)); // Переход на новый экран
                dispose();
            }

            // Проверка нажатия кнопки "Выход"
            if (touchX >= exitButtonX && touchX <= exitButtonX + exitButtonWidth &&
                touchY >= exitButtonY && touchY <= exitButtonY + exitButtonHeight) {
                buttonSound.play(); // Воспроизведение звука кнопки
                Gdx.app.exit(); // Завершение игры
            }
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        // Освобождение ресурсов
        playButtonTexture.dispose();
        exitButtonTexture.dispose();
        backgroundTexture.dispose();
        backgroundMusic.dispose();
        buttonSound.dispose();
    }
}
