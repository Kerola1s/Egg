package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.Animation;

public class GameScreen implements Screen {
    private static final float WALK_SPEED = 2f;
    private static final float RUN_SPEED = 4f;
    public static float MAX_STAMINA = 3f;
    private static final float STAMINA_REGEN = 1f;
    private static final float STAMINA_USAGE = 1f;
    private static final float JUMP_VELOCITY = 7f;
    private static final float GRAVITY = -10.8f;
    public static int MAX_LIVES = 3;
    private static final float STAMINA_COOLDOWN = 5f; // Время восстановления

    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> currentAnimation;
    private float animationTime = 0f;

    private final Drop game;
    private final FitViewport viewport;
    private final BitmapFont font;
    private final Texture backgroundTexture;
    private final Texture bucketTexture;
    private final Texture staminaIcon;
    private final Texture lifeIcon;
    private Music music;
    private int caughtEggsCount = 0;


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
    private float staminaCooldownTimer = 0f; // Таймер восстановления

    public GameScreen(Drop game) {
        this.game = game;
        this.viewport = new FitViewport(8, 5);

        this.backgroundTexture = new Texture("GameBG.png");
        this.bucketTexture = new Texture("wolf.png");
        this.staminaIcon = new Texture("stamina.png");
        this.lifeIcon = new Texture("НР.png");
        this.font = new BitmapFont();

        this.bucketSprite = new Sprite(bucketTexture);
        this.bucketSprite.setSize(2, 2);
        this.bucketRectangle = new Rectangle();
        this.scoreManager = new ScoreManager();
        this.dropManager = new DropManager(
            new Texture[]{
                new Texture("Egg1.png"),
                new Texture("Egg2.png"),
                new Texture("Egg3.png"),
                new Texture("Egg4.png"),
                new Texture("Egg5.png"),
                new Texture("Egg6.png"),
                new Texture("Egg7.png"),
                new Texture("Egg8.png"),
                new Texture("Egg9.png"),
                new Texture("Egg10.png"),
                new Texture("Egg11.png"),
                new Texture("Egg12.png"),
                new Texture("Egg13.png"),
                new Texture("Egg14.png"),
                new Texture("Egg15.png")
            },
            new Texture("golden-egg.png"),
            viewport,
            scoreManager
        );
        this.enemyManager = new EnemyManager(new Texture("FallEnemy.png"), viewport);
        this.groundEnemyManager = new GroundEnemyManager(new Texture("Enemy.png"), viewport);
        this.healManager = new Heal(new Texture("heal.png"), viewport);

    //    music.setLooping(true);
      //  music.setVolume(0.5f);
     //   music.play();

        font.setColor(Color.WHITE);
        font.getData().setScale(3f);

        Texture wolfSheet = new Texture("sprite1.png");
        TextureRegion[][] frames = TextureRegion.split(wolfSheet, wolfSheet.getWidth() / 3, wolfSheet.getHeight() / 1);

        walkAnimation = new Animation<>(0.1f, frames[0]);
        walkAnimation.setPlayMode(Animation.PlayMode.LOOP);

        Texture idleTexture = new Texture("wolf2.png");
        idleAnimation = new Animation<>(1f, new TextureRegion(idleTexture));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        currentAnimation = idleAnimation;
    }

    @Override
    public void show() {
        music = Gdx.audio.newMusic(Gdx.files.internal("Music.mp3"));
        music.setLooping(true);
        music.setVolume(0.4f);
        music.play();
        if (game.scoreManager.isMedkitPurchased() && MAX_LIVES == 3) {
            increaseLives(3); // Применяем увеличение жизней только один раз
        }
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        updateGameLogic(delta);
        drawGame();
    }

    private void handleInput(float delta) {
        float speed = WALK_SPEED;

        if (staminaCooldownTimer > 0) {
            staminaCooldownTimer -= delta; // Уменьшаем таймер
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && currentStamina > 0 && staminaCooldownTimer <= 0) {
            speed = RUN_SPEED;
            currentStamina -= STAMINA_USAGE * delta;
            isRunning = true;
        } else {
            isRunning = false;
            if (currentStamina <= 0) {
                staminaCooldownTimer = STAMINA_COOLDOWN; // Активируем таймер
            }
            currentStamina = Math.min(currentStamina + STAMINA_REGEN * delta, MAX_STAMINA);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (!isFacingRight) {
                bucketSprite.flip(true, false);
                isFacingRight = true;
            }
            bucketSprite.translateX(speed * delta);
            currentAnimation = walkAnimation;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (isFacingRight) {
                bucketSprite.flip(true, false);
                isFacingRight = false;
            }
            bucketSprite.translateX(-speed * delta);
            currentAnimation = walkAnimation;
        } else {
            currentAnimation = idleAnimation;
        }

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



        dropManager.update(delta, bucketRectangle, this::reduceLife);

        if (dropManager.isVictoryAchieved()) {
            music.stop();
            game.setScreen(new VictoryScreen(game, scoreManager.getTotalScore()));
            return;
        }

        if (caughtEggsCount >= 25) {
            music.stop();
            game.setScreen(new VictoryScreen(game, scoreManager.getTotalScore()));
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
        animationTime += delta;

    }

    public void increaseLives(int extraLives) {
        MAX_LIVES += extraLives; // Увеличиваем максимум
        lives = Math.min(lives + extraLives, MAX_LIVES); // Увеличиваем текущие жизни
    }
    public void increaseMaxStamina(float amount) {
        MAX_STAMINA += amount;
        currentStamina = MAX_STAMINA; // Обновляем текущую выносливость
        System.out.println("Максимальная выносливость увеличена до: " + MAX_STAMINA);
    }



    private void drawGame() {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();

        // Рисуем фон
        game.batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Получаем текущий кадр анимации
        TextureRegion currentFrame = currentAnimation.getKeyFrame(animationTime);

        // Определяем размер уменьшенной анимации
        float reducedWidth = bucketSprite.getWidth() * 0.8f; // Уменьшение размера (80% от оригинала)
        float reducedHeight = bucketSprite.getHeight() * 0.8f;

        // Отрисовка с учётом направления
        float drawX = bucketSprite.getX();
        float drawY = bucketSprite.getY();
        if (isFacingRight) {
            game.batch.draw(currentFrame, drawX + reducedWidth, drawY, -reducedWidth, reducedHeight);
        } else {
            game.batch.draw(currentFrame, drawX, drawY, reducedWidth, reducedHeight);
        }

        // Рисуем остальные элементы
        dropManager.draw(game.batch);
        enemyManager.drawEnemies(game.batch);
        groundEnemyManager.drawGroundEnemies(game.batch);
        healManager.draw(game.batch);

        drawUI();
        drawText();

        game.batch.end();
    }

    private void drawUI() {
        float iconSize = 0.5f;
        float spacing = 0.3f;

        for (int i = 0; i < Math.ceil(currentStamina); i++) {
            game.batch.draw(staminaIcon, viewport.getWorldWidth() - (iconSize + spacing) * (i + 1), viewport.getWorldHeight() - iconSize, iconSize, iconSize);
        }

        for (int i = 0; i < lives; i++) {
            game.batch.draw(lifeIcon, spacing + i * (iconSize + spacing), viewport.getWorldHeight() - iconSize - spacing, iconSize, iconSize);
        }

        font.draw(game.batch, "Score: " + currentScore, 10, viewport.getWorldHeight() - 10);
    }
    private void drawText() {
        // Отображение общего счёта в левом верхнем углу экрана
        int totalScore = scoreManager.getTotalScore();
        font.draw(game.batch, "Total Score: " + totalScore, 10, viewport.getWorldHeight() - 30);
        font.draw(game.batch, "Eggs Caught: " + dropManager.getCaughtEggsCount(), 10, viewport.getWorldHeight() - 50);


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
        backgroundTexture.dispose();
        bucketTexture.dispose();
        dropManager.dispose();
        enemyManager.dispose();
        groundEnemyManager.dispose();
       music.dispose();
        staminaIcon.dispose();
        font.dispose();
        lifeIcon.dispose();
        healManager.dispose();
    }
}
