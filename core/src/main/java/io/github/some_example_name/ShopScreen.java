package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class ShopScreen implements Screen {
    final Drop game;
    OrthographicCamera camera;
    Texture backgroundTexture;
    Texture exitButtonTexture;
    float exitButtonX, exitButtonY, exitButtonWidth, exitButtonHeight;

    // Пустые ячейки магазина
    Texture slotTexture;
    float slotWidth, slotHeight;
    float slot1X, slot1Y, slot2X, slot2Y, slot3X, slot3Y;

    // Текст
    BitmapFont font;

    public ShopScreen(Drop game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 500);
    }

    @Override
    public void show() {
        // Загрузка текстур
        backgroundTexture = new Texture("54.png");
        exitButtonTexture = new Texture("Button5.png");
        slotTexture = new Texture("Slot.png");

        // Инициализация шрифта
        font = new BitmapFont();

        // Позиции и размеры кнопки "Выход"
        exitButtonWidth = 50;
        exitButtonHeight = 50;
        exitButtonX = Gdx.graphics.getWidth() - exitButtonWidth - 10;
        exitButtonY = Gdx.graphics.getHeight() - exitButtonHeight - 10;

        // Позиции и размеры ячеек
        slotWidth = 100;
        slotHeight = 100;
        slot1X = 150;
        slot1Y = 300;
        slot2X = 400;
        slot2Y = 300;
        slot3X = 650;
        slot3Y = 300;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Отрисовка фона
        game.batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);

        // Отрисовка кнопки "Выход"
        game.batch.draw(exitButtonTexture, exitButtonX, exitButtonY, exitButtonWidth, exitButtonHeight);

        // Отрисовка ячеек
        game.batch.draw(slotTexture, slot1X, slot1Y, slotWidth, slotHeight);
        game.batch.draw(slotTexture, slot2X, slot2Y, slotWidth, slotHeight);
        game.batch.draw(slotTexture, slot3X, slot3Y, slotWidth, slotHeight);

        // Отрисовка текста над и под ячейками
        font.draw(game.batch, "Text1", slot1X + slotWidth / 2 - 20, slot1Y + slotHeight + 20); // Над первой ячейкой
        font.draw(game.batch, "Text2", slot1X + slotWidth / 2 - 20, slot1Y - 10);             // Под первой ячейкой

        font.draw(game.batch, "Text3", slot2X + slotWidth / 2 - 20, slot2Y + slotHeight + 20); // Над второй ячейкой
        font.draw(game.batch, "Text4", slot2X + slotWidth / 2 - 20, slot2Y - 10);             // Под второй ячейкой

        font.draw(game.batch, "Text5", slot3X + slotWidth / 2 - 20, slot3Y + slotHeight + 20); // Над третьей ячейкой
        font.draw(game.batch, "Text6", slot3X + slotWidth / 2 - 20, slot3Y - 10);             // Под третьей ячейкой

        game.batch.end();

        // Проверка клика по кнопке "Выход"
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (touchX >= exitButtonX && touchX <= exitButtonX + exitButtonWidth &&
                touchY >= exitButtonY && touchY <= exitButtonY + exitButtonHeight) {
                game.setScreen(new MainMenuScreen(game)); // Возвращение в главное меню
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
    public void hide() {}

    @Override
    public void dispose() {
        // Освобождение ресурсов
        backgroundTexture.dispose();
        exitButtonTexture.dispose();
        slotTexture.dispose();
        font.dispose();
    }
}
