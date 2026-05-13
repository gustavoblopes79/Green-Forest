package com.greenforest.manager;

import com.greenforest.DifficultySettings;
import com.greenforest.entity.Enemy;
import com.greenforest.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EnemyManager {

    private static final int   TOTAL_TYPES       = 10;
    private static final int   MAX_ON_SCREEN_CAP = 42;
    private static final float BREAK_DURATION    = 3.0f;

    private static final int[][] SPAWN_WEIGHTS = {
            { 50, 10,  0,  0, 30,  0,  0, 10,  0,  0 },
            { 40,  8,  0,  5, 25,  0,  0, 22,  0,  0 },
            { 30, 20,  0, 10, 20,  0,  0, 18,  2,  0 },
            { 22, 22,  5, 12, 15, 14,  0, 10,  0,  0 },
            { 18, 18, 12, 12, 12, 14,  0, 10,  4,  0 },
            { 14, 16, 12, 12, 10, 12,  8,  8,  8,  0 },
            { 12, 14, 12, 10,  8, 12, 12,  8,  8,  4 },
            { 10, 12, 12, 10,  8, 10, 12,  8, 10,  8 },
            {  8, 10, 14,  8,  6, 10, 14,  6, 12, 12 },
            {  8,  8, 14,  8,  6, 10, 14,  6, 14, 12 },
    };

    private final List<Enemy> enemies = new ArrayList<>();

    private int   hordaNumber       = 1;
    private int   killsThisHorda    = 0;
    private int   killsToNextHorda  = totalEnemiesForHorda(1);
    private int   inimigosSpawnados = 0;
    private int   maxOnScreen       = maxOnScreenForHorda(1);

    private float spawnTimer        = 0f;
    private float spawnInterval     = spawnIntervalForHorda(1);

    private boolean inBreak         = false;
    private float   breakTimer      = 0f;

    private int lastHordaType = -1;
    private Consumer<Integer> onHordaChange;

    public void setOnHordaChange(Consumer<Integer> cb) {
        this.onHordaChange = cb;
    }

    public static int killsRequired(int horda) {
        return totalEnemiesForHorda(horda);
    }

    private static int totalEnemiesForHorda(int horda) {
        if (horda <= 1) return 15;
        if (horda == 2) return 22;
        if (horda == 3) return 30;
        if (horda == 4) return 40;
        return Math.min(40 + (horda - 4) * 10, 120);
    }

    private static int maxOnScreenForHorda(int horda) {
        return Math.min(12 + (horda - 1) * 3, MAX_ON_SCREEN_CAP);
    }

    private static float spawnIntervalForHorda(int horda) {
        return Math.max(0.20f, 0.50f - (horda - 1) * 0.04f);
    }

    private int batchSizeForHorda() {
        return Math.min(2 + (hordaNumber - 1) / 2, 5);
    }

    public void update(float dt, float playerX, float playerY,
                       float gameTimeSec, Player player) {

        for (Enemy e : enemies)
            if (!e.isDead()) e.update(dt, playerX, playerY);

        for (Enemy e : enemies) {
            if (e.isDead() && !e.isXpAwarded()) {
                player.gainXP(e.getXpValue());
                player.addKill();
                killsThisHorda++;
                e.markXpAwarded();
            }
        }
        enemies.removeIf(Enemy::isDead);

        if (player.hasScreenClearReady()) {
            clearAllEnemiesOnScreen(player);
            player.consumeScreenClear();
        }

        if (inBreak) {
            breakTimer -= dt;
            if (breakTimer <= 0f) {
                inBreak = false;
                advanceHorda();
            }
            return;
        }



        boolean hordaTotalSpawnada = inimigosSpawnados >= killsToNextHorda;
        boolean hordaFoiConcluida = hordaTotalSpawnada
                && killsThisHorda >= killsToNextHorda
                && enemies.isEmpty();

        if (hordaFoiConcluida) {
            inBreak    = true;
            breakTimer = BREAK_DURATION;
            spawnTimer = 0f;
            return;
        }

        if (hordaTotalSpawnada) return;

        spawnTimer += dt;
        float effectiveInterval =
                spawnInterval * DifficultySettings.spawnIntervalMult();

        if (spawnTimer >= effectiveInterval
                && enemies.size() < maxOnScreen) {
            spawnTimer = 0f;
            spawnBatch(playerX, playerY);
        }
    }

    public void applyAreaDamage(float cx, float cy, float radius,
                                float damage, Player player) {
        for (Enemy e : enemies) {
            if (e.isDead()) continue;
            float dx   = e.getX() - cx;
            float dy   = e.getY() - cy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist <= radius) {
                e.takeDamage((int) damage);
            }
        }
    }

    private void clearAllEnemiesOnScreen(Player player) {
        for (Enemy e : enemies) {
            if (!e.isDead()) {
                e.takeDamage(999999);
            }
        }
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        for (Enemy e : enemies) e.draw(g2, camX, camY);
    }

    private void spawnBatch(float px, float py) {
        int slots = maxOnScreen - enemies.size();
        if (slots <= 0) return;

        int faltandoNaHorda = killsToNextHorda - inimigosSpawnados;
        if (faltandoNaHorda <= 0) return;

        int batchSize = Math.min(slots, batchSizeForHorda());
        batchSize = Math.min(batchSize, faltandoNaHorda);

        for (int i = 0; i < batchSize; i++) {
            int   type   = pickType();
            float angle  = (float)(Math.random() * Math.PI * 2);
            float radius = 340f + (float)(Math.random() * 160f);
            float ex     = px + (float)Math.cos(angle) * radius;
            float ey     = py + (float)Math.sin(angle) * radius;
            enemies.add(new Enemy(ex, ey, type));
            inimigosSpawnados++;
        }
    }

    private int pickType() {
        int row = Math.min(hordaNumber - 1, SPAWN_WEIGHTS.length - 1);
        int[] weights = SPAWN_WEIGHTS[row];
        int total = 0;
        for (int w : weights) total += w;
        int roll = (int)(Math.random() * total);
        int acc  = 0;
        for (int t = 0; t < TOTAL_TYPES; t++) {
            acc += weights[t];
            if (roll < acc) return t;
        }
        return 0;
    }

    private void advanceHorda() {
        hordaNumber++;
        killsThisHorda    = 0;
        inimigosSpawnados = 0;
        killsToNextHorda  = totalEnemiesForHorda(hordaNumber);
        maxOnScreen       = maxOnScreenForHorda(hordaNumber);
        spawnInterval     = spawnIntervalForHorda(hordaNumber);

        int row       = Math.min(hordaNumber - 1, SPAWN_WEIGHTS.length - 1);
        int dominant  = 0;
        int maxWeight = 0;
        for (int t = 0; t < TOTAL_TYPES; t++) {
            if (SPAWN_WEIGHTS[row][t] > maxWeight) {
                maxWeight = SPAWN_WEIGHTS[row][t];
                dominant  = t;
            }
        }
        if (dominant != lastHordaType) {
            lastHordaType = dominant;
            if (onHordaChange != null) onHordaChange.accept(dominant);
        }
    }

    public List<Enemy> getEnemies()          { return enemies; }
    public int getHordaNumber()              { return hordaNumber; }
    public int getKillsThisHorda()           { return killsThisHorda; }
    public int getKillsToNextHorda()         { return killsToNextHorda; }
    public boolean isInBreak()               { return inBreak; }
    public float getBreakTimer()             { return breakTimer; }

    public void clear() {
        enemies.clear();
        lastHordaType     = -1;
        hordaNumber       = 1;
        killsThisHorda    = 0;
        killsToNextHorda  = totalEnemiesForHorda(1);
        inimigosSpawnados = 0;
        maxOnScreen       = maxOnScreenForHorda(1);
        spawnTimer        = 0f;
        spawnInterval     = spawnIntervalForHorda(1);
        inBreak           = false;
        breakTimer        = 0f;
    }

    public void advanceHordaPublic() {
        advanceHorda();
    }
}