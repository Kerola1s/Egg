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
        backgroundTexture = new Texture("54.png");
        slotTexture = new Texture("Slot.png");
        slotImage1 = new Texture("Medkit.png");
        slotImage2 = new Texture("NonStop.png");
        slotImage3 = new Texture("Armor.png");

        // Инициализация шрифта
        font = new BitmapFont();

        // Позиции и размеры ячеек
        slotWidth = 100;
        slotHeight = 100;
        slot1X = 150;
        slot1Y = 300;
        slot2X = 400;
        slot2Y = 300;
        slot3X = 650;
        slot3Y = 300;

        // Добавление кнопок "Купить"
        createBuyButton(slot1X, slot1Y - 50, "Slot 1");
        createBuyButton(slot2X, slot2Y - 50, "Slot 2");
        createBuyButton(slot3X, slot3Y - 50, "Slot 3");

        // Добавление кнопки возврата в меню
        createBackButton();
    }

    private void createBuyButton(float x, float y, String buttonText) {
        Texture buttonUpTexture = new Texture("7.png");
        Texture buttonDownTexture = new Texture("46.png");

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        textButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        textButtonStyle.font = font;

        TextButton button = new TextButton("Купить", textButtonStyle);
        button.setSize(slotWidth, 30);
        button.setPosition(x, y - 80);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                System.out.println(buttonText + " clicked");
            }
        });
        stage.addActor(button);
    }

    private void createBackButton() {
        Texture buttonUpTexture = new Texture("Button5.png");

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        textButtonStyle.font = font;

        TextButton backButton = new TextButton("Назад", textButtonStyle);
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
        drawSlotWithImageAndText(slot1X, slot1Y, slotImage1, "Medical Kit", "Cost: 100,000");
        drawSlotWithImageAndText(slot2X, slot2Y, slotImage2, "NonStop Ultra Max Edition", "Cost: 200,000");
        drawSlotWithImageAndText(slot3X, slot3Y, slotImage3, "Armor", "Cost: 300,000");

        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void drawSlotWithImageAndText(float x, float y, Texture image, String topText, String bottomText) {
        font.draw(game.batch, topText, x + slotWidth / 2 - font.getSpaceXadvance() * topText.length() / 2, y + slotHeight + 20);
        game.batch.draw(slotTexture, x, y, slotWidth, slotHeight);
        float imageX = x + (slotWidth - 80) / 2;
        float imageY = y + (slotHeight - 80) / 2;
        game.batch.draw(image, imageX, imageY, 80, 80);
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
