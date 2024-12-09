package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ShopScreen implements Screen {
    final Drop game;
    OrthographicCamera camera;
    Texture backgroundTexture;

    // Текстуры для картинок
    Texture slotTexture;
    Texture slotImage1, slotImage2, slotImage3;
    float slotWidth, slotHeight;
    float slot1X, slot1Y, slot2X, slot2Y, slot3X, slot3Y;

    // UI
    BitmapFont font;
    Stage stage;

    public ShopScreen(Drop game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 500);
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {
        // Загрузка текстур
        backgroundTexture = new Texture("Shop.png");
        slotTexture = new Texture("Slot.png");
        slotImage1 = new Texture("Medkit.png");
        slotImage2 = new Texture("NonStop.png");
        slotImage3 = new Texture("Armor.png");

        // Инициализация шрифта
        font = new BitmapFont();

        // Позиции и размеры ячеек
        slotWidth = 50; // Уменьшенные размеры слота
        slotHeight = 50;

        // Центрируем элементы в горизонтальном ряду
        float centerX = camera.viewportWidth / 2 - (3 * slotWidth + 2 * 20) / 2; // Начальная позиция (с учетом отступов)
        float centerY = camera.viewportHeight / 2 + 24; // Вертикальная позиция слотов

        // Уникальные координаты для каждого предмета
        slot1X = centerX;
        slot1Y = centerY;

        slot2X = slot1X + slotWidth + 40; // Вторая ячейка с отступом
        slot2Y = centerY;

        slot3X = slot2X + slotWidth + 40; // Третья ячейка с отступом
        slot3Y = centerY;

        // Кнопки для каждого предмета
        createBuyButton(slot1X, slot1Y - 60, "MedKit", 100000, () -> {
            game.gameScreen.increaseLives(2); // Увеличиваем жизни
        });

        createBuyButton(slot2X, slot2Y - 60, "NonStop", 200000, () -> {
            System.out.println("NonStop куплен!");
        });

        createBuyButton(slot3X, slot3Y - 60, "Armor", 300000, () -> {
            System.out.println("Броня куплена!");
        });

        createBackButton(); // Кнопка "Назад"
    }



    private void createBuyButton(float x, float y, String buttonText, int cost, Runnable onPurchase) {
        Texture buttonUpTexture = new Texture("7.png");
        Texture buttonDownTexture = new Texture("46.png");

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        textButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        textButtonStyle.font = font;

        TextButton button = new TextButton("Buy", textButtonStyle);
        button.setSize(slotWidth, 30);
        button.setPosition(x, y - 80);

        if (game.scoreManager.isMedkitPurchased()) {
            button.setText("Purchased");
            button.setDisabled(true);
        } else {
            button.addListener(new ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    if (game.scoreManager.getTotalScore() >= cost) {
                        // Списываем деньги
                        game.scoreManager.addToTotalScore(-cost);

                        // Выполняем покупку
                        onPurchase.run();
                        game.scoreManager.setMedkitPurchased(true);

                        // Обновляем кнопку
                        button.setText("Purchased");
                        button.setDisabled(true);
                    } else {
                        System.out.println("Недостаточно средств для покупки " + buttonText);
                    }
                }
            });
        }


        stage.addActor(button);
    }

    private void createBackButton() {
        Texture buttonUpTexture = new Texture("Button5.png");

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        textButtonStyle.font = font;

        TextButton backButton = new TextButton("Back", textButtonStyle);
        backButton.setSize(150, 50);
        backButton.setPosition(10, 10); // Расположение в левом нижнем углу
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game)); // Переход в главное меню
                dispose();
            }
        });
        stage.addActor(backButton);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        game.batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);
        drawSlotWithImageAndText(slot1X, slot1Y, slotImage1, "Medkit", "Coast: 100k");
        drawSlotWithImageAndText(slot2X, slot2Y, slotImage2, "NonStop", "Coast: 200k");
        drawSlotWithImageAndText(slot3X, slot3Y, slotImage3, "Armor", "Coast: 300k");

        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void drawSlotWithImageAndText(float x, float y, Texture image, String topText, String bottomText) {
        // Центрируем текст относительно слота
        font.draw(game.batch, topText, x + slotWidth / 2 - font.getSpaceXadvance() * topText.length() / 2, y + slotHeight + 15);
        game.batch.draw(slotTexture, x, y, slotWidth, slotHeight);
        float imageX = x + (slotWidth - 60) / 2; // Уменьшаем размер изображения внутри слота
        float imageY = y + (slotHeight - 60) / 2;
        game.batch.draw(image, imageX, imageY, 60, 60); // Новые размеры изображения
        font.draw(game.batch, bottomText, x + slotWidth / 2 - font.getSpaceXadvance() * bottomText.length() / 2, y - 10);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        slotTexture.dispose();
        slotImage1.dispose();
        slotImage2.dispose();
        slotImage3.dispose();
        font.dispose();
        stage.dispose();
    }
}

