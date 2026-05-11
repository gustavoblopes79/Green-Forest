package com.greenforest.manager;

import com.greenforest.DifficultySettings;
import com.greenforest.entity.Enemy;
import com.greenforest.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EnemyManager {

    private final List<Enemy> enemies = new ArrayList<>();

    // horda
    private int hordaNumber      = 1;
    private int killsThisHorda   = 0;
    private int killsToNextHorda = 15;
    private static final int MAX_ON_SCREEN_CAP = 40;
    private int   maxOnScreen    = 8;

    // spawn
    private float spawnTimer    = 0f;
    private float spawnInterval = 2.0f;

    // pausa entre hordas
    private boolean inBreak    = false;
    private float   breakTimer = 0f;
    private static final float BREAK_DURATION = 3.0f;

    // callback
    private int lastHordaType = -1;
    private Consumer<Integer> onHordaChange;

    public void setOnHordaChange(Consumer<Integer> cb) {
        this.onHordaChange = cb;
    }

    public static int killsRequired(int horda) {
        if (horda == 1) return 15;
        return killsRequired(horda - 1) + 10 + (horda - 2) * 5;
    }

    public void update(float dt, float playerX, float playerY,
                       float gameTimeSec, Player player) {

        // Atualiza inimigos vivos
        for (Enemy e : enemies) {
            if (!e.isDead()) e.update(dt, playerX, playerY);
        }

        // Remove mortos e concede XP/kill ao jogador
        for (Enemy e : enemies) {
            if (e.isDead() && !e.isXpAwarded()) {
                player.gainXP(e.getXpValue());
                player.addKill();
                killsThisHorda++;
                e.markXpAwarded();
            }
        }
        enemies.removeIf(Enemy::isDead);

        // Pausa entre hordas
        if (inBreak) {
            breakTimer -= dt;
            if (breakTimer <= 0f) {
                inBreak = false;
                advanceHorda();
            }
            return;
        }

        // Avança horda se matou o suficiente
        if (killsThisHorda >= killsToNextHorda) {
            killsThisHorda = 0;
            inBreak        = true;
            breakTimer     = BREAK_DURATION;
            return;
        }

        // Spawn
        spawnTimer += dt;
        float effectiveInterval =
                spawnInterval * DifficultySettings.spawnIntervalMult();
        if (spawnTimer >= effectiveInterval
                && enemies.size() < maxOnScreen) {
            spawnTimer = 0f;
            spawnBatch(playerX, playerY);
        }
    }

    private void advanceHorda() {
        hordaNumber++;
        killsToNextHorda = killsRequired(hordaNumber);

        if (hordaNumber % 2 == 0)
            maxOnScreen = Math.min(maxOnScreen + 3, MAX_ON_SCREEN_CAP);

        if (hordaNumber % 3 == 0)
            spawnInterval = Math.max(0.6f, spawnInterval - 0.2f);

        int maxType = Math.min(4, (hordaNumber - 1) / 2);
        int newType = (hordaNumber - 1) % (maxType + 1);
        if (newType != lastHordaType) {
            lastHordaType = newType;
            if (onHordaChange != null) onHordaChange.accept(newType);
        }
    }

    private void spawnBatch(float px, float py) {
        int slots = maxOnScreen - enemies.size();
        if (slots <= 0) return;
        int batchSize = Math.min(slots, 1 + hordaNumber / 5);
        int maxType   = Math.min(4, (hordaNumber - 1) / 2);
        int dominant  = (hordaNumber - 1) % (maxType + 1);

        for (int i = 0; i < batchSize; i++) {
            int type = (Math.random() < 0.70)
                    ? dominant
                    : (int)(Math.random() * (maxType + 1));

            float angle  = (float)(Math.random() * Math.PI * 2);
            float radius = 350f + (float)(Math.random() * 150f);
            float ex     = px + (float) Math.cos(angle) * radius;
            float ey     = py + (float) Math.sin(angle) * radius;
            enemies.add(new Enemy(ex, ey, type));
        }
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        for (Enemy e : enemies) e.draw(g2, camX, camY);
    }

    public List<Enemy> getEnemies()          { return enemies; }
    public int         getHordaNumber()      { return hordaNumber; }
    public int         getKillsThisHorda()   { return killsThisHorda; }
    public int         getKillsToNextHorda() { return killsToNextHorda; }
    public boolean     isInBreak()           { return inBreak; }
    public float       getBreakTimer()       { return breakTimer; }

    public void clear() {
        enemies.clear();
        lastHordaType    = -1;
        hordaNumber      = 1;
        killsThisHorda   = 0;
        killsToNextHorda = 15;
        maxOnScreen      = 8;
        spawnTimer       = 0f;
        spawnInterval    = 2.0f;
        inBreak          = false;
        breakTimer       = 0f;
    }
    public void advanceHordaPublic() {
        advanceHorda();
    }
}