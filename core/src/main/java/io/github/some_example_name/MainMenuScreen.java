package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements Screen {
    Texture playButtonTexture;
    Texture exitButtonTexture;
    Texture shopButtonTexture;
    float playButtonX, playButtonY, playButtonWidth, playButtonHeight;
    float exitButtonX, exitButtonY, exitButtonWidth, exitButtonHeight;
    float shopButtonX, shopButtonY, shopButtonWidth, shopButtonHeight;
    final Drop game;
    OrthographicCamera camera;
    Texture backgroundTexture;
    private ScoreManager scoreManager;
    private Music backgroundMusic;

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
        shopButtonTexture = new Texture("Button4.png");

        // Установка размеров и позиций кнопок
        playButtonWidth = 200;
        playButtonHeight = 150;
        playButtonX = Gdx.graphics.getWidth() / 2 - playButtonWidth / 2 - 250;
        playButtonY = Gdx.graphics.getHeight() / 2 - playButtonHeight / 2 - 150;

        exitButtonWidth = 50;
        exitButtonHeight = 50;
        exitButtonX = Gdx.graphics.getWidth() - exitButtonWidth - 10;
        exitButtonY = Gdx.graphics.getHeight() - exitButtonHeight - 10;

        shopButtonWidth = 200;
        shopButtonHeight = 150;
        shopButtonX = Gdx.graphics.getWidth() / 2 - shopButtonWidth / 2 + 250;
        shopButtonY = Gdx.graphics.getHeight() / 2 - shopButtonHeight / 2 - 150;

        // Загрузка фоновой музыки
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("MainTheme.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        backgroundMusic.play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Отрисовка фона и кнопок
        game.batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);
        game.batch.draw(playButtonTexture, playButtonX, playButtonY, playButtonWidth, playButtonHeight);
        game.batch.draw(exitButtonTexture, exitButtonX, exitButtonY, exitButtonWidth, exitButtonHeight);
        game.batch.draw(shopButtonTexture, shopButtonX, shopButtonY, shopButtonWidth, shopButtonHeight);

        // Отрисовка общего счёта
        int totalScore = scoreManager.getTotalScore();
        game.font.draw(game.batch, "Total Score: " + totalScore, 10, camera.viewportHeight - 30);

        game.batch.end();

        // Проверка клика по кнопкам
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Инвертируем Y-координату

            if (touchX >= playButtonX && touchX <= playButtonX + playButtonWidth &&
                touchY >= playButtonY && touchY <= playButtonY + playButtonHeight) {
                game.setScreen(new GameScreen(game));
                dispose();
            }

            if (touchX >= exitButtonX && touchX <= exitButtonX + exitButtonWidth &&
                touchY >= exitButtonY && touchY <= exitButtonY + exitButtonHeight) {
                Gdx.app.exit();
            }

            if (touchX >= shopButtonX && touchX <= shopButtonX + shopButtonWidth &&
                touchY >= shopButtonY && touchY <= shopButtonY + shopButtonHeight) {
                game.setScreen(new ShopScreen(game));
                dispose();
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
    public void hide() {
        backgroundMusic.stop();
    }

    @Override
    public void dispose() {
        playButtonTexture.dispose();
        exitButtonTexture.dispose();
        shopButtonTexture.dispose();
        backgroundTexture.dispose();
        backgroundMusic.dispose();
    }
}
