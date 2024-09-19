package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements Screen {
    Texture buttonTexture;
    float buttonX, buttonY, buttonWidth, buttonHeight;
    final Drop game;
    OrthographicCamera camera;
    Texture backgroundTexture;
    public MainMenuScreen(Drop drop) {
        this.game = drop;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 500);
    }

    @Override
    public void show() {
        backgroundTexture = new Texture("starmenu.png");
        buttonTexture = new Texture("button.png");
        buttonWidth = 200;
        buttonHeight = 150;
        buttonX = Gdx.graphics.getWidth() / 2 - buttonWidth / 2-250;
        buttonY = Gdx.graphics.getHeight() / 2 - buttonHeight / 2-150;
    }

    @Override
    public void render(float v) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);
        game.batch.draw(buttonTexture, buttonX, buttonY, buttonWidth, buttonHeight);
        game.font.draw(game.batch, "", 100, 150);
        game.font.draw(game.batch, "", 100, 100);
        game.batch.end();
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Инвертируем Y-координату

            // Проверяем, нажата ли кнопка
            if (touchX >= buttonX && touchX <= buttonX + buttonWidth && touchY >= buttonY && touchY <= buttonY + buttonHeight) {
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
            buttonTexture.dispose();
    }
}
