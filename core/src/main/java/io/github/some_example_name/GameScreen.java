package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
    float dropTimer = 0;
    float reload = 5f;
    boolean onreload;
    float reloadtime = 0f;
    float walkSpeed = 2f;
    private float maxStamina = 5f;
    private float currentStamina = maxStamina;
    private float staminaRegen = 1f;
    private float staminaUsage = 1f;
    private boolean isRunning = false;
    final Drop game;
    private FitViewport viewport;
    int missedDrops = 0;
    final int maxMissedDrops = 3;
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
    BitmapFont font;  // Для отображения счётчика на экране

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

        // Инициализация шрифта
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(3f);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        input();
        logic(delta);
        draw();
    }

    private void input() {
        float speed = walkSpeed;
        float runSpeed = 4f;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && currentStamina > 0) {
            speed = runSpeed;
            currentStamina -= staminaUsage * Gdx.graphics.getDeltaTime();
            if (currentStamina <= 0) {
                currentStamina = 0;
                isRunning = false;
                speed = walkSpeed;
            } else {
                isRunning = true;
            }
        } else {
            isRunning = false;
            if (currentStamina < maxStamina) {
                currentStamina += staminaRegen * Gdx.graphics.getDeltaTime();
                if (currentStamina > maxStamina) {
                    currentStamina = maxStamina;
                }
            }
        }

        currentStamina = MathUtils.clamp(currentStamina, 0, maxStamina);

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (isFacingRight) {
                bucketSprite.flip(true, false);
                isFacingRight = false;
            }
            bucketSprite.translateX(speed * Gdx.graphics.getDeltaTime());
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (!isFacingRight) {
                bucketSprite.flip(true, false);
                isFacingRight = true;
            }
            bucketSprite.translateX(-speed * Gdx.graphics.getDeltaTime());
        }
    }

    private void logic(float delta) {
        float worldWidth = viewport.getWorldWidth();
        float bucketWidth = bucketSprite.getWidth();
        dropSpeed += dropSpeedIncreaseRate * delta;
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketSprite.getHeight());

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            dropSprite.translateY(-dropSpeed * delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropSprite.getWidth(), dropSprite.getHeight());

            if (dropSprite.getY() < -dropSprite.getHeight()) {
                dropSprites.removeIndex(i);
                missedDrops++;
                if (missedDrops >= maxMissedDrops) {
                    gameOver();
                    return;
                }
            } else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
                dropCount++;
            }
        }

        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        game.batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        bucketSprite.draw(game.batch);
        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(game.batch);
        }

        drawStaminaIcons();

        // Отображение счётчика стамины и количества пойманных капель
        font.draw(game.batch, "Stamina: " + (int) currentStamina, 50, worldHeight - 50);
        font.draw(game.batch, "Поймано капель: " + dropCount, 50, worldHeight - 100);

        game.batch.end();
    }

    private void drawStaminaIcons() {
        float iconSize = 0.5f;
        float spacing = 0.3f;
        int numIcons = (int) Math.ceil(currentStamina);
        for (int i = 0; i < numIcons; i++) {
            game.batch.draw(staminaIcon, viewport.getWorldWidth() - (iconSize + spacing) * (i + 1), viewport.getWorldHeight() - iconSize, iconSize, iconSize);
        }
    }

    private void createDroplet() {
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        Texture randomDropTexture = dropTexture[MathUtils.random(0, dropTexture.length - 1)];
        Sprite dropSprite = new Sprite(randomDropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(viewport.getWorldHeight());
        dropSprites.add(dropSprite);
    }

    private void gameOver() {
        music.stop();
        game.setScreen(new GameOverScreen(game));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        bucketTexture.dispose();
        for (Texture texture : dropTexture) {
            texture.dispose();
        }
        music.dispose();
        dropSound.dispose();
        staminaIcon.dispose();
        font.dispose();
    }
}
