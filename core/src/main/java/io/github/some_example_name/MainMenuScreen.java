package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements Screen {
    Texture buttonTexture;
    float buttonX, buttonY, buttonWidth, buttonHeight;
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
        buttonTexture = new Texture("Button1.png");

        // Установка размеров кнопки
        buttonWidth = 200;
        buttonHeight = 150;
        buttonX = Gdx.graphics.getWidth() / 2 - buttonWidth / 2 - 250;
        buttonY = Gdx.graphics.getHeight() / 2 - buttonHeight / 2 - 150;

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

        // Отрисовка кнопки
        game.batch.draw(buttonTexture, buttonX, buttonY, buttonWidth, buttonHeight);

        // Отрисовка общего счёта
        int totalScore = scoreManager.getTotalScore();
        game.font.draw(game.batch, "Total Score: " + totalScore, 10, camera.viewportHeight - 30);

        game.batch.end();

        // Проверка клика по кнопке
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Инвертируем Y-координату

            if (touchX >= buttonX && touchX <= buttonX + buttonWidth && touchY >= buttonY && touchY <= buttonY + buttonHeight) {
                buttonSound.play(); // Воспроизведение звука кнопки
                game.setScreen(new GameScreen(game)); // Переход на новый экран
                dispose();
            }
        }
    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        // Освобождение ресурсов
        buttonTexture.dispose();
        backgroundTexture.dispose();
        backgroundMusic.dispose();
        buttonSound.dispose();
    }
}
