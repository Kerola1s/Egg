package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class ScoreManager {

    private static final String PREFERENCES_NAME = "game_scores";
    private static final String TOTAL_SCORE_KEY = "total_score";
    private boolean medkitPurchased = false;
    private boolean isNonStopPurchased = false;

    private final Preferences preferences;

    public ScoreManager() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
    }

    public void addToTotalScore(int score) {
        int totalScore = getTotalScore();
        totalScore += score;
        preferences.putInteger(TOTAL_SCORE_KEY, totalScore);
        preferences.flush();
    }

    public int getTotalScore() {
        return preferences.getInteger(TOTAL_SCORE_KEY, 0);
    }
    public boolean isMedkitPurchased() {
        return medkitPurchased;
    }
    public void setMedkitPurchased(boolean purchased) {
        this.medkitPurchased = purchased;
    }
    public boolean isNonStopPurchased() {
        return isNonStopPurchased;
    }

    public void setNonStopPurchased(boolean isNonStopPurchased) {
        this.isNonStopPurchased = isNonStopPurchased;
    }

        public void resetAllData() {
            preferences.clear();
            preferences.flush();
            resetLives();
        }
    public void resetLives() {

        GameScreen.MAX_LIVES = 3;
    }

    }

