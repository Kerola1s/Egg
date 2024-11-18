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
    float dropSpeed = 2f;
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
    final int maxMissedDrops = 6;
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
    BitmapFont font;
    Texture enemyTexture;
    Array<Sprite> enemySprites;
    private float jumpForce = 6f;         // Сила прыжка
    private float gravity = -9.8f;        // Сила гравитации
    private boolean isJumping = false;    // Проверка, находится ли персонаж в прыжке
    private float verticalVelocity = 0f;  // Вертикальная скорость для прыжка
    float enemySpawnTimer = 0;
    float enemySpawnInterval = 20f;  // Начальная редкость появления противника
    Array<Sprite> groundEnemies; // Список врагов, которые бегают по земле.
    float groundEnemySpeed = 2f; // Скорость бегущих врагов.
    float groundEnemySpawnTimer = 0; // Таймер появления врагов.
    float groundEnemySpawnInterval = 5f; // Интервал появления врагов.
    Texture groundEnemyTexture;


    public GameScreen(Drop game) {
        this.game = game;
        enemyTexture = new Texture("Bimba.png");
        enemySprites = new Array<>();
        viewport = new FitViewport(8, 5);
        dropTexture = new Texture[]{
            new Texture("egg_1.png"),
            new Texture("egg_2.png"),
            new Texture("egg_3.png"),
            new Texture("egg_4.png"),
            new Texture("egg_5.png")
        };
        groundEnemies = new Array<>();
        groundEnemyTexture = new Texture("pituh.png");
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && currentStamina >= maxStamina / 4) {
            jump();  // Метод прыжка
            currentStamina -= maxStamina / 4;  // Уменьшаем стамину на четверть
        }

    }
    // Метод прыжка
    private void jump() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            verticalVelocity = jumpForce;
            isJumping = true;
        }
    }
    private void logic(float delta) {
        float worldWidth = viewport.getWorldWidth();
        float bucketWidth = bucketSprite.getWidth();
        dropSpeed += dropSpeedIncreaseRate * delta;
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketSprite.getHeight());
        if (isJumping) {
            verticalVelocity += gravity * delta;   // Применяем гравитацию к вертикальной скорости
            bucketSprite.translateY(verticalVelocity * delta);  // Обновляем позицию

            // Проверка приземления
            if (bucketSprite.getY() <= 0) {    // Если персонаж достиг земли
                bucketSprite.setY(0);          // Возвращаем на уровень земли
                isJumping = false;             // Завершаем прыжок
                verticalVelocity = 0;          // Сбрасываем вертикальную скорость
            }
        }

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

        updateEnemies(delta);
        updateGroundEnemies(delta); // <-- Обновление бегущих врагов.
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
        for (Sprite enemySprite : enemySprites) {
            enemySprite.draw(game.batch);
        }
        for (Sprite groundEnemy : groundEnemies) {
            groundEnemy.draw(game.batch);
        }


        drawStaminaIcons();
        drawScore();

        game.batch.end();
    }
    private Rectangle getGroundEnemyHitbox(Sprite groundEnemy) {
        float hitboxWidth = groundEnemy.getWidth() * 0.7f;  // 70% ширины спрайта.
        float hitboxHeight = groundEnemy.getHeight() * 0.5f; // 50% высоты спрайта.
        float hitboxX = groundEnemy.getX() + (groundEnemy.getWidth() - hitboxWidth) / 2; // Центрируем хитбокс по ширине.
        float hitboxY = groundEnemy.getY(); // По высоте хитбокс совпадает с основанием врага.
        return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }
    private void drawStaminaIcons() {
        float iconSize = 0.5f;
        float spacing = 0.3f;
        int numIcons = (int) Math.ceil(currentStamina);
        for (int i = 0; i < numIcons; i++) {
            game.batch.draw(staminaIcon, viewport.getWorldWidth() - (iconSize + spacing) * (i + 1), viewport.getWorldHeight() - iconSize, iconSize, iconSize);
        }
    }

    private void drawScore() {
        float worldHeight = viewport.getWorldHeight();
        font.setColor(Color.BLACK);          // Установка цвета текста в черный
        font.getData().setScale(0.2f);        // Настройка масштаба текста (можно менять)


        font.draw(game.batch, "Stamina: " + (int) currentStamina, 10, worldHeight - 20);
        font.draw(game.batch, "Поймано капель: " + dropCount, 10, worldHeight - 60);
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
    private void updateEnemies(float delta) {
        enemySpawnTimer += delta;


        if (enemySpawnTimer >= enemySpawnInterval) {
            enemySpawnTimer = 0;
            spawnEnemy();


            enemySpawnInterval = Math.max(1f, enemySpawnInterval * 0.95f);
        }


        for (int i = enemySprites.size - 1; i >= 0; i--) {
            Sprite enemySprite = enemySprites.get(i);
            enemySprite.translateY(-dropSpeed * delta);


            if (enemySprite.getY() < -enemySprite.getHeight() || bucketRectangle.overlaps(enemySprite.getBoundingRectangle())) {
                enemySprites.removeIndex(i);

                // Дополнительная логика при столкновении с игроком
                if (bucketRectangle.overlaps(enemySprite.getBoundingRectangle())) {
                    missedDrops++;
                    if (missedDrops >= maxMissedDrops) {
                        gameOver();
                        return;
                    }
                }
            }
        }
    }
    private void updateGroundEnemies(float delta) {
        groundEnemySpawnTimer += delta;

        if (groundEnemySpawnTimer >= groundEnemySpawnInterval) {
            groundEnemySpawnTimer = 0;
            spawnGroundEnemy();
        }

        for (int i = groundEnemies.size - 1; i >= 0; i--) {
            GroundEnemy groundEnemy = (GroundEnemy) groundEnemies.get(i);

            // Обновляем позицию в зависимости от направления
            if (groundEnemy.isMovingRight()) {
                groundEnemy.translateX(groundEnemySpeed * delta);
            } else {
                groundEnemy.translateX(-groundEnemySpeed * delta);
            }

            Rectangle groundEnemyHitbox = getGroundEnemyHitbox(groundEnemy);

            // Проверяем столкновение с игроком
            if (bucketRectangle.overlaps(groundEnemyHitbox)) {
                groundEnemies.removeIndex(i);
                missedDrops++;
                if (missedDrops >= maxMissedDrops) {
                    gameOver();
                    return;
                }
            }

            // Удаляем врагов, которые вышли за пределы экрана
            if (groundEnemy.isMovingRight() && groundEnemy.getX() > viewport.getWorldWidth() ||
                !groundEnemy.isMovingRight() && groundEnemy.getX() < -groundEnemy.getWidth()) {
                groundEnemies.removeIndex(i);
            }
        }
    }


    // Метод для создания нового врага
    private void spawnEnemy() {
        Sprite enemySprite = new Sprite(enemyTexture);
        enemySprite.setSize(1, 1);
        enemySprite.setX(MathUtils.random(0, viewport.getWorldWidth() - enemySprite.getWidth()));
        enemySprite.setY(viewport.getWorldHeight());
        enemySprites.add(enemySprite);
    }
    private void spawnGroundEnemy() {
        boolean moveRight = MathUtils.randomBoolean(); // Случайное направление
        GroundEnemy groundEnemy = new GroundEnemy(groundEnemyTexture, moveRight);

        groundEnemy.setSize(1, 1);

        if (moveRight) {
            groundEnemy.setX(-groundEnemy.getWidth()); // Появляется за левым краем
        } else {
            groundEnemy.setX(viewport.getWorldWidth()); // Появляется за правым краем
        }

        groundEnemy.setY(0); // Уровень земли
        groundEnemies.add(groundEnemy);
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
        groundEnemyTexture.dispose();
        enemyTexture.dispose();
        music.dispose();
        dropSound.dispose();
        staminaIcon.dispose();
        font.dispose();

    }
}
