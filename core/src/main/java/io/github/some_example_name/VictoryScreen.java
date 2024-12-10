package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class VictoryScreen implements Screen {
    private final Drop game;
    private final int totalScore;

    private Texture backgroundTexture;
    private Texture restartButtonTexture;
    private Texture mainMenuButtonTexture;
    Music winmusic;

    private OrthographicCamera camera;

    private float restartButtonX, restartButtonY, restartButtonWidth, restartButtonHeight;
    private float mainMenuButtonX, mainMenuButtonY, mainMenuButtonWidth, mainMenuButtonHeight;

    public VictoryScreen(Drop game, int totalScore) {
        this.game = game;
        this.totalScore = totalScore;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 500);
    }

    @Override
    public void show() {
        // Загрузка текстур
        backgroundTexture = new Texture("Win.png");
        restartButtonTexture = new Texture("Button2.png");
        mainMenuButtonTexture = new Texture("Button5.png");

        // Настройки кнопки "Рестарт"
        restartButtonWidth = 150;
        restartButtonHeight = 60;
        restartButtonX = Gdx.graphics.getWidth() / 2 - restartButtonWidth / 2;
        restartButtonY = Gdx.graphics.getHeight() / 2 - restartButtonHeight - 20;

        // Настройки кнопки "Главное меню"
        mainMenuButtonWidth = 150;
        mainMenuButtonHeight = 60;
        mainMenuButtonX = Gdx.graphics.getWidth() / 2 - mainMenuButtonWidth / 2;
        mainMenuButtonY = restartButtonY - mainMenuButtonHeight - 20;
        winmusic = Gdx.audio.newMusic(Gdx.files.internal("Win.mp3"));
        winmusic.setLooping(true);
        winmusic.setVolume(0.4f);
        winmusic.play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();


        game.batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);


        game.font.getData().setScale(1.5f);
        game.font.setColor(Color.WHITE);


        game.font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        game.font.draw(game.batch, "You Win!", Gdx.graphics.getWidth() / 2f - 75, Gdx.graphics.getHeight() / 1.5f);
        game.font.draw(game.batch, "Score: " + totalScore, Gdx.graphics.getWidth() / 2f - 75, Gdx.graphics.getHeight() / 1.8f);


        game.batch.draw(restartButtonTexture, restartButtonX, restartButtonY, restartButtonWidth, restartButtonHeight);
        game.batch.draw(mainMenuButtonTexture, mainMenuButtonX, mainMenuButtonY, mainMenuButtonWidth, mainMenuButtonHeight);

        game.batch.end();

        // Обработка нажатий на кнопки
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();


            if (touchX >= restartButtonX && touchX <= restartButtonX + restartButtonWidth &&
                touchY >= restartButtonY && touchY <= restartButtonY + restartButtonHeight) {
                game.setScreen(new GameScreen(game)); // Переход на игровой экран
                dispose();
            }


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
        winmusic.dispose();
    }
}
