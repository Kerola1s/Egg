package io.github.some_example_name;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameOverScreen implements Screen {
    final Drop game;
    Texture backgroundTexture;
    OrthographicCamera camera;
    Music GM;

    public GameOverScreen(Drop game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 500);
    }

    @Override
    public void show() {
        backgroundTexture = new Texture("GameOverScreen.png");
        GM = Gdx.audio.newMusic(Gdx.files.internal("pogodi.mp3"));
        GM .setLooping(false);
        GM .setVolume(750f);
        GM .play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);  // Очищаем экран
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Отрисовываем фон, подгоняя его под размеры камеры
        game.batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);

        // Проверка нажатия любой клавиши и клика
        if (Gdx.input.isKeyJustPressed(Keys.ANY_KEY)) {
            game.setScreen(new MainMenuScreen(game));  // Переход в главное меню по нажатию клавиши
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            game.setScreen(new MainMenuScreen(game));  // Переход в главное меню по нажатию левой кнопки мыши
        }else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            game.setScreen(new MainMenuScreen(game));  // Переход в главное меню по нажатию правой кнопки мыши
        }

        game.batch.end();  // Завершаем отрисовку
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
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
        backgroundTexture.dispose();  // Освобождаем ресурсы
    }
}
