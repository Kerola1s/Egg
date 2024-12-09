package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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
    private Music Shopmusic;

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
        slotImage1 = new Texture("Medkit12.png");
        slotImage2 = new Texture("NonStop12.png");

        // Инициализация шрифта
        font = new BitmapFont();

        // Позиции и размеры ячеек
        slotWidth = 50;
        slotHeight = 50;

        float centerX = camera.viewportWidth / 2 - (3 * slotWidth + 2 * 20) / 2;
        float centerY = camera.viewportHeight / 2 + 24;

        slot1X = centerX;
        slot1Y = centerY;

        slot2X = slot1X + slotWidth + 70;
        slot2Y = centerY;

        slot3X = slot2X + slotWidth + 70;
        slot3Y = centerY;

        // Кнопки для покупки предметов
        createMedkitButton(slot1X, slot1Y - 60);
        createNonStopButton(slot2X, slot2Y - 60);

        createBackButton(); // Кнопка "Назад"
        Shopmusic = Gdx.audio.newMusic(Gdx.files.internal("ShopMusic.mp3"));
        Shopmusic.setLooping(true);
        Shopmusic.setVolume(0.5f);
        Shopmusic.play();
    }

    private void createMedkitButton(float x, float y) {
        createBuyButton(x, y, "Medkit", 100000, () -> {
            if (!game.scoreManager.isMedkitPurchased()) {
                game.scoreManager.setMedkitPurchased(true);
                game.gameScreen.increaseLives(3); // Увеличиваем максимальные жизни
            }
        });
    }

    private void createNonStopButton(float x, float y) {
        createBuyButton(x, y, "NonStop", 200000, () -> {
            if (!game.scoreManager.isNonStopPurchased()) {
                game.scoreManager.setNonStopPurchased(true);
                game.gameScreen.increaseMaxStamina(3); // Увеличиваем максимум выносливости
                System.out.println("NonStop куплен!");
            }
        });
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
        button.setPosition(x, y - 50); // Поднял кнопку выше

        button.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (game.scoreManager.getTotalScore() >= cost) {
                    // Списываем деньги
                    game.scoreManager.addToTotalScore(-cost);

                    // Выполняем покупку
                    onPurchase.run();

                    // Обновляем кнопку
                    button.setText("Purchased");
                    button.setDisabled(true);
                } else {
                    System.out.println("Недостаточно средств для покупки " + buttonText);
                }
            }
        });

        stage.addActor(button);
    }

    private void createBackButton() {
        Texture buttonUpTexture = new Texture("Button5.png");

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        textButtonStyle.font = font;

        TextButton backButton = new TextButton("Back", textButtonStyle);
        backButton.setSize(150, 50);
        backButton.setPosition(10, 10);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                Shopmusic.stop(); // Останавливаем музыку
                game.setScreen(new MainMenuScreen(game)); // Переход к экрану меню
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

        // Отображение фона магазина
        game.batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);

        // Отображение товаров с текстурами и текстом
        drawItemWithImageAndText(slot1X, slot1Y, slotImage1, "Medkit", "Cost: 100k", "+ 3 lives for you");
        drawItemWithImageAndText(slot2X, slot2Y, slotImage2, "NonStop", "Cost: 200k", "+3 energy power for you");


        game.batch.end();

        stage.act(delta);
        stage.draw();
    }


    private void drawItemWithImageAndText(float x, float y, Texture image, String topText, String bottomText, String additionalText) {
        // Отображение названия предмета сверху
        font.draw(game.batch, topText, x + slotWidth / 2 - font.getSpaceXadvance() * topText.length() / 2, y + slotHeight + 15);

        // Отображение изображения предмета
        float imageX = x + (slotWidth - 60) / 2;
        float imageY = y + (slotHeight - 60) / 2;
        game.batch.draw(image, imageX, imageY, 60, 60);

        // Отображение описания/стоимости предмета снизу
        font.draw(game.batch, bottomText, x + slotWidth / 2 - font.getSpaceXadvance() * bottomText.length() / 2, imageY - 20);

        // Отображение дополнительного текста под стоимостью
        font.draw(game.batch, additionalText, x + slotWidth / 2 - font.getSpaceXadvance() * additionalText.length() / 2, imageY - 40);
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
        Shopmusic.dispose();
    }
}
