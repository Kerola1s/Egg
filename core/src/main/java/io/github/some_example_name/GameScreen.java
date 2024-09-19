package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {
    float dropSpeed = 2f;  // Начальная скорость падения капель
    float dropSpeedIncreaseRate = 0.1f;
    float dropTimer = 0;// Скорость увеличения падения капель
    float reload = 5f;
    boolean onreload;
    float reloadtime = 0f;
    float walkSpeed = 2f;
    private float maxStamina = 5f;  // это макс сколько стамины
    private float currentStamina = maxStamina;  // сколько щас есть
    private float staminaRegen = 1f;  // Скорость восстановления стамины
    private float staminaUsage = 1f;  // Скорость расхода стамины
    private boolean isRunning = false;  // Флаг для проверки, бежит ли персонаж
    final Drop game;
    private FitViewport viewport;
    int missedDrops = 0;  // Счетчик пропущенных капель
    final int maxMissedDrops = 3;  // Лимит пропущенных капель для Game Over
    private boolean isFacingRight = true;
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture[] dropTexture;
    Sound dropSound;
    Music music;
    Texture staminaIcon;
    Sprite bucketSprite;
    Array<Sprite> dropSprites;
    Texture shellTexture;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;

    int dropCount = 0;

    public GameScreen(Drop game) {
        this.game = game;
        viewport = new FitViewport(8, 5);
        dropTexture = new Texture[]{
            new Texture("egg_1.png"),
            new Texture("egg_2.png"),
            new Texture("egg_3.png"),
            new Texture("egg_4.png"),
            new Texture("egg_5.png")
        };
        backgroundTexture = new Texture("Background.png");
        bucketTexture = new Texture("wolf.png");
        staminaIcon = new Texture("stamina.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(2, 2);

        dropSprites = new Array<>();

        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float v) {
        input();
        logic();
        draw();
    }

    private void input() {
        float speed = walkSpeed;  // Изначально скорость ходьбы
        float runSpeed = 4f;  // Скорость бега
        float delta = Gdx.graphics.getDeltaTime();

        // Проверка на бег (Shift), если стамина еще не полностью потрачена
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && currentStamina > 0) {
            speed = runSpeed;  // Увеличиваем скорость до скорости бега

            // Расходуем стамину
            currentStamina -= staminaUsage * delta;
            if (currentStamina <= 0) {
                currentStamina = 0;
                isRunning = false;
                speed = walkSpeed;  // Если стамина кончилась, переключаемся на ходьбу
            } else {
                isRunning = true;
            }

        } else {
            isRunning = false;

            // Восстанавливаем стамину, если персонаж не бежит
            if (currentStamina < maxStamina) {
                currentStamina += staminaRegen * delta;
                if (currentStamina > maxStamina) {
                    currentStamina = maxStamina;  // Чтобы не превысить максимум
                }
            }
        }

        // Не позволяем стамине уходить в отрицательные значения
        currentStamina = MathUtils.clamp(currentStamina, 0, maxStamina);

        // Движение вправо
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)||  Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (isFacingRight) {
                bucketSprite.flip(true, false);
                isFacingRight = false;
            }
            bucketSprite.translateX(speed * delta);
        }

        // Движение влево
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)||  Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (!isFacingRight) {
                bucketSprite.flip(true, false);
                isFacingRight = true;
            }
            bucketSprite.translateX(-speed * delta);
        }
    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float bucketWidth = bucketSprite.getWidth();
        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta
        dropSpeed += dropSpeedIncreaseRate * delta;
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketSprite.getHeight());

        // Loop through the sprites backwards to prevent out of bounds errors
        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i); // Get the sprite from the list
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-dropSpeed * delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) {
                dropSprites.removeIndex(i);
                missedDrops++;  // Увеличиваем счетчик пропущенных капель

                // Проверка на Game Over
                if (missedDrops >= maxMissedDrops) {
                    gameOver();  // Вызываем метод завершения игры
                    return;
                }
            } else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
                dropCount++;
            }
        }

        dropTimer += delta; // Adds the current delta to the timer
        if (dropTimer > 1f) { // Check if it has been more than a second
            dropTimer = 0; // Reset the timer
            createDroplet(); // Create the droplet
        }
    }

        private void draw () {
            game.font = new BitmapFont();
            ScreenUtils.clear(Color.BLACK);
            game.font.setColor(Color.WHITE);  // Установите видимый цвет
            game.font.getData().setScale(4f);  // Увеличьте масштаб шрифта
            viewport.apply();
            game.batch.setProjectionMatrix(viewport.getCamera().combined);
            game.batch.begin();

            float worldWidth = viewport.getWorldWidth();
            float worldHeight = viewport.getWorldHeight();
            game.font.draw(game.batch, "Stamina: " + (int) currentStamina, 50, 100);
            game.batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
            bucketSprite.draw(game.batch);
            drawStaminaIcons();
            for (Sprite dropSprite : dropSprites) {
                dropSprite.draw(game.batch);
            }

            game.font.draw(game.batch, "Піймали капель:" + dropCount, 50, 50);
            game.font.draw(game.batch, "Test Text", 50, 100);
            game.batch.end();
        }
private void drawStaminaIcons(){
        float iconsize = 0.5f;
        float spacing = 0.3f;
    int numIcons = (int) Math.ceil(currentStamina);
    for (int i = 0; i < numIcons; i++) {
        game.batch.draw(staminaIcon, viewport.getWorldWidth() - (iconsize + spacing) * (i + 1), viewport.getWorldHeight() - iconsize, iconsize, iconsize);
    }
}
        private void createDroplet () {
            // create local variables for convenience
            float dropWidth = 1;
            float dropHeight = 1;
            float worldWidth = viewport.getWorldWidth();
            float worldHeight = viewport.getWorldHeight();
            Texture randomDropTexture = dropTexture[MathUtils.random(0, dropTexture.length - 1)];
            // create the drop sprite
            Sprite dropSprite = new Sprite(randomDropTexture);
            dropSprite.setSize(dropWidth, dropHeight);
            dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
            dropSprite.setY(worldHeight);
            dropSprites.add(dropSprite); // Add it to the list
        }
        private void gameOver () {
            music.stop();  // Останавливаем фоновую музыку
            game.setScreen(new GameOverScreen(game));
        }
        @Override
        public void resize ( int width, int height){
            viewport.update(width, height, true);
        }

        @Override
        public void pause () {

        }

        @Override
        public void resume () {

        }

        @Override
        public void hide () {

        }

        @Override
        public void dispose () {
            backgroundTexture.dispose();
            bucketTexture.dispose();
            for (Texture texture : dropTexture) {
                texture.dispose();
            }
            music.dispose();
            dropSound.dispose();
            staminaIcon.dispose();
        }
    }
