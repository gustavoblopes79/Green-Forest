package com.greenforest;

public class DifficultySettings {

    public enum Difficulty {
        EASY, NORMAL, HARD
    }
    public static Difficulty current() { return current; }
    private static Difficulty current = Difficulty.NORMAL;

    public static void set(Difficulty d) { current = d; }
    public static Difficulty get()       { return current; }

    // Enemy stats
    public static float enemySpeedMult() {
        return switch (current) {
            case EASY   -> 0.65f;
            case NORMAL -> 1.00f;
            case HARD   -> 1.45f;
        };
    }

    public static float enemyDamageMult() {
        return switch (current) {
            case EASY   -> 0.50f;
            case NORMAL -> 1.00f;
            case HARD   -> 1.75f;
        };
    }

    public static float spawnIntervalMult() {
        // higher = longer between spawns = easier
        return switch (current) {
            case EASY   -> 1.60f;
            case NORMAL -> 1.00f;
            case HARD   -> 0.60f;
        };
    }

    // Boss stats
    public static float bossHpMult() {
        return switch (current) {
            case EASY   -> 0.60f;
            case NORMAL -> 1.00f;
            case HARD   -> 1.80f;
        };
    }

    public static float bossDamageMult() {
        return switch (current) {
            case EASY   -> 0.50f;
            case NORMAL -> 1.00f;
            case HARD   -> 1.80f;
        };
    }

    public static float bossIntervalBonus() {
        return switch (current) {
            case EASY   -> 30f;   // +30s = 150s total
            case NORMAL -> 0f;    // 120s base
            case HARD   -> -15f;  // -15s = 105s total
        };
    }

    public static String label() {
        return switch (current) {
            case EASY   -> "FACIL";
            case NORMAL -> "NORMAL";
            case HARD   -> "DIFICIL";
        };
    }
}