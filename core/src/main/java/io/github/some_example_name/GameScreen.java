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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {
    float walkSpeed = 2f;
    private float maxStamina = 5f;
    private float currentStamina = maxStamina;
    private float staminaRegen = 1f;
    private float staminaUsage = 1f;
    private boolean isRunning = false;

    final Drop game;
    private FitViewport viewport;
    private boolean isFacingRight = true;
    Texture backgroundTexture;
    Texture bucketTexture;
    Sound dropSound;
    Music music;
    Texture staminaIcon;
    Sprite bucketSprite;
    Rectangle bucketRectangle;
    int dropCount = 0;
    BitmapFont font;
    private int lives = 5;
    private final int maxLives = 5;
    private Texture lifeIcon;
    private float jumpVelocity = 7f;
    private float gravity = -10.8f;
    private float verticalVelocity = 0;
    private boolean isJumping = false;

    EnemyManager enemyManager;
    GroundEnemyManager groundEnemyManager;
    Heal healManager;
    DropManager dropManager;

    public GameScreen(Drop game) {
        this.game = game;

        viewport = new FitViewport(8, 5);

        backgroundTexture = new Texture("Background.png");
        bucketTexture = new Texture("wolf.png");
        staminaIcon = new Texture("stamina.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(2, 2);
        bucketRectangle = new Rectangle();

        enemyManager = new EnemyManager(new Texture("Bimba.png"), viewport);
        groundEnemyManager = new GroundEnemyManager(new Texture("pituh.png"), viewport);

        music.setLooping(true);
        music.setVolume(.5f);
        music.play();

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(3f);
        lifeIcon = new Texture("hear.png");
        healManager = new Heal(new Texture("heal.png"), viewport);

        dropManager = new DropManager(
            new Texture[]{
                new Texture("egg_1.png"),
                new Texture("egg_2.png"),
                new Texture("egg_3.png"),
                new Texture("egg_4.png"),
                new Texture("egg_5.png")
            },
            new Texture("golden-egg.png"),
            viewport
        );
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            verticalVelocity = jumpVelocity;
            isJumping = true;
        }
    }

    private void logic(float delta) {
        float worldWidth = viewport.getWorldWidth();
        float bucketWidth = bucketSprite.getWidth();

        // Обновление позиции корзины
        bucketSprite.setX(MathUtils.clamp(
            bucketSprite.getX(),
            0,
            worldWidth - bucketWidth
        ));
        bucketRectangle.set(
            bucketSprite.getX(),
            bucketSprite.getY(),
            bucketWidth,
            bucketSprite.getHeight()
        );

        // Обновление капель и проверка коллизий
        dropManager.update(delta, bucketRectangle);

        Sprite caughtDrop = dropManager.getCaughtDrop(bucketRectangle);
        if (caughtDrop != null) {
            dropCount += dropManager.getScoreForTexture(caughtDrop.getTexture());
            Gdx.app.log("Score", "Score: " + dropCount);
            dropSound.play();
        }

        // Проверяем здоровье, коллизии и прочие игровые состояния
        healManager.update(delta, bucketRectangle, () -> {
            if (lives < maxLives) {
                lives++;
            }
        });

        healManager.update(delta, bucketRectangle, () -> {
            if (lives < maxLives) {
                lives++;
            }
        });

        enemyManager.updateEnemies(delta, bucketRectangle, () -> {
            lives--;
            if (lives <= 0) {
                gameOver();
            }
        });

        groundEnemyManager.updateGroundEnemies(delta, bucketRectangle, () -> {
            lives--;
            if (lives <= 0) {
                gameOver();
            }
        });

        verticalVelocity += gravity * delta;
        bucketSprite.translateY(verticalVelocity * delta);

        if (bucketSprite.getY() <= 0) {
            bucketSprite.setY(0);
            verticalVelocity = 0;
            isJumping = false;
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

        dropManager.draw(game.batch);
        enemyManager.drawEnemies(game.batch);
        groundEnemyManager.drawGroundEnemies(game.batch);

        drawStaminaIcons();
        drawScore();
        drawLives();
        healManager.draw(game.batch);

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

    private void drawLives() {
        float iconSize = 0.5f;
        float spacing = 0.3f;
        for (int i = 0; i < lives; i++) {
            game.batch.draw(lifeIcon, spacing + i * (iconSize + spacing), viewport.getWorldHeight() - iconSize - spacing, iconSize, iconSize);
        }
    }

    private void drawScore() {
        float worldHeight = viewport.getWorldHeight();
        font.setColor(Color.BLACK);
        font.getData().setScale(0.2f);
        font.draw(game.batch, "Stamina: " + (int) currentStamina, 10, worldHeight - 20);
        font.draw(game.batch, "Поймано капель: " + dropCount, 10, worldHeight - 60);
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
        dropManager.dispose();
        enemyManager.dispose();
        groundEnemyManager.dispose();
        music.dispose();
        dropSound.dispose();
        staminaIcon.dispose();
        font.dispose();
        lifeIcon.dispose();
        healManager.dispose();
    }
}
