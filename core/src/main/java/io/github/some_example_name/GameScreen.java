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
    private static final float WALK_SPEED = 2f;
    private static final float RUN_SPEED = 4f;
    private static final float MAX_STAMINA = 5f;
    private static final float STAMINA_REGEN = 1f;
    private static final float STAMINA_USAGE = 1f;
    private static final float JUMP_VELOCITY = 7f;
    private static final float GRAVITY = -10.8f;
    private static final int MAX_LIVES = 5;

    private final Drop game;
    private final FitViewport viewport;
    private final BitmapFont font;
    private final Texture backgroundTexture;
    private final Texture bucketTexture;
    private final Texture staminaIcon;
    private final Texture lifeIcon;
    private final Sound dropSound;
    private final Music music;

    private final Sprite bucketSprite;
    private final Rectangle bucketRectangle;

    private final DropManager dropManager;
    private final EnemyManager enemyManager;
    private final GroundEnemyManager groundEnemyManager;
    private final Heal healManager;
    private final ScoreManager scoreManager;

    private float currentStamina = MAX_STAMINA;
    private float verticalVelocity = 0;
    private boolean isJumping = false;
    private boolean isRunning = false;
    private boolean isFacingRight = true;
    private int lives = MAX_LIVES;
    private int currentScore = 0;

    public GameScreen(Drop game) {
        this.game = game;
        this.viewport = new FitViewport(8, 5);

        this.backgroundTexture = new Texture("Background.png");
        this.bucketTexture = new Texture("wolf.png");
        this.staminaIcon = new Texture("stamina.png");
        this.lifeIcon = new Texture("hear.png");
        this.dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        this.music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        this.font = new BitmapFont();

        this.bucketSprite = new Sprite(bucketTexture);
        this.bucketSprite.setSize(2, 2);
        this.bucketRectangle = new Rectangle();

        this.dropManager = new DropManager(
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
        this.enemyManager = new EnemyManager(new Texture("Bimba.png"), viewport);
        this.groundEnemyManager = new GroundEnemyManager(new Texture("pituh.png"), viewport);
        this.healManager = new Heal(new Texture("heal.png"), viewport);
        this.scoreManager = new ScoreManager();

        music.setLooping(true);
        music.setVolume(0.5f);
        music.play();

        font.setColor(Color.WHITE);
        font.getData().setScale(3f);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        handleInput(delta);
        updateGameLogic(delta);
        drawGame();
    }

    private void handleInput(float delta) {
        float speed = WALK_SPEED;

        // Определяем скорость: бег или ходьба
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && currentStamina > 0) {
            speed = RUN_SPEED;
            currentStamina -= STAMINA_USAGE * delta;
            isRunning = true;
        } else {
            isRunning = false;
            currentStamina = Math.min(currentStamina + STAMINA_REGEN * delta, MAX_STAMINA);
        }

        // Движение вправо (теперь инверсия: спрайт переворачивается влево)
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (isFacingRight) { // Инверсия: был "вправо", теперь "влево"
                bucketSprite.flip(true, false);
                isFacingRight = false;
            }
            bucketSprite.translateX(speed * delta);
        }

        // Движение влево (теперь инверсия: спрайт переворачивается вправо)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (!isFacingRight) { // Инверсия: был "влево", теперь "вправо"
                bucketSprite.flip(true, false);
                isFacingRight = true;
            }
            bucketSprite.translateX(-speed * delta);
        }

        // Прыжок
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            verticalVelocity = JUMP_VELOCITY;
            isJumping = true;
        }
    }



    private void updateGameLogic(float delta) {
        bucketRectangle.set(
            bucketSprite.getX(),
            bucketSprite.getY(),
            bucketSprite.getWidth(),
            bucketSprite.getHeight()
        );

        dropManager.update(delta, bucketRectangle);
        Sprite caughtDrop = dropManager.getCaughtDrop(bucketRectangle);
        if (caughtDrop != null) {
            int score = dropManager.getScoreForTexture(caughtDrop.getTexture());
            currentScore += score;
            scoreManager.addToTotalScore(score);
            dropSound.play();
        }

        healManager.update(delta, bucketRectangle, () -> {
            if (lives < MAX_LIVES) {
                lives++;
            }
        });

        enemyManager.updateEnemies(delta, bucketRectangle, this::reduceLife);
        groundEnemyManager.updateGroundEnemies(delta, bucketRectangle, this::reduceLife);

        verticalVelocity += GRAVITY * delta;
        bucketSprite.translateY(verticalVelocity * delta);

        if (bucketSprite.getY() <= 0) {
            bucketSprite.setY(0);
            verticalVelocity = 0;
            isJumping = false;
        }

        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, viewport.getWorldWidth() - bucketSprite.getWidth()));
    }

    private void drawGame() {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();

        game.batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        bucketSprite.draw(game.batch);
        dropManager.draw(game.batch);
        enemyManager.drawEnemies(game.batch);
        groundEnemyManager.drawGroundEnemies(game.batch);
        healManager.draw(game.batch);

        drawUI();

        game.batch.end();
    }

    private void drawUI() {
        float iconSize = 0.5f;
        float spacing = 0.3f;

        // Draw stamina
        for (int i = 0; i < Math.ceil(currentStamina); i++) {
            game.batch.draw(staminaIcon, viewport.getWorldWidth() - (iconSize + spacing) * (i + 1), viewport.getWorldHeight() - iconSize, iconSize, iconSize);
        }

        // Draw lives
        for (int i = 0; i < lives; i++) {
            game.batch.draw(lifeIcon, spacing + i * (iconSize + spacing), viewport.getWorldHeight() - iconSize - spacing, iconSize, iconSize);
        }

        // Draw score
        font.draw(game.batch, "Score: " + currentScore, 10, viewport.getWorldHeight() - 10);
    }

    private void reduceLife() {
        lives--;
        if (lives <= 0) {
            gameOver();
        }
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
