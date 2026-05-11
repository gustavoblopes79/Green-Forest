package com.greenforest.manager;

import com.greenforest.DifficultySettings;
import com.greenforest.entity.Enemy;
import com.greenforest.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EnemyManager {

    private static final int   TOTAL_TYPES        = 10;
    private static final int   MAX_ON_SCREEN_CAP  = 50;
    private static final float BREAK_DURATION     = 3.0f;

    private static final int[][] SPAWN_WEIGHTS = {
            // H1:  só SACOLA e CIGARRO com trace de LATA
            { 50, 10,  0,  0, 30,  0,  0, 10,  0,  0 },
            // H2:  SACOLA domina, CIGARRO e ISOPOR entram forte
            { 40,  8,  0,  5, 25,  0,  0, 22,  0,  0 },
            // H3:  LATA cresce, NUVEM aparece, ISOPOR presente
            { 30, 20,  0, 10, 20,  0,  0, 18,  2,  0 },
            // H4:  GARRAFA entra, LATA cresce mais
            { 22, 22,  5, 12, 15, 14,  0, 10,  0,  0 },
            // H5:  PNEU entra, mistura boa de 5 tipos
            { 18, 18, 12, 12, 12, 14,  0, 10,  4,  0 },
            // H6:  AGROTOX cresce, OLEO aparece
            { 14, 16, 12, 12, 10, 12,  8,  8,  8,  0 },
            // H7:  OLEO cresce, ENTULHO aparece
            { 12, 14, 12, 10,  8, 12, 12,  8,  8,  4 },
            // H8:  ENTULHO mais presente, todos ativos
            { 10, 12, 12, 10,  8, 10, 12,  8, 10,  8 },
            // H9:  tanques dominam mais
            {  8, 10, 14,  8,  6, 10, 14,  6, 12, 12 },
            // H10+: distribuicao mista pesando tanques e especiais
            {  8,  8, 14,  8,  6, 10, 14,  6, 14, 12 },
    };

    private final List<Enemy> enemies = new ArrayList<>();

    private int   hordaNumber      = 1;
    private int   killsThisHorda   = 0;
    private int   killsToNextHorda = 15;
    private int   maxOnScreen      = 10;

    private float spawnTimer    = 0f;
    private float spawnInterval = 1.6f; // mais agressivo no inicio

    private boolean inBreak    = false;
    private float   breakTimer = 0f;

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

        if (inBreak) {
            breakTimer -= dt;
            if (breakTimer <= 0f) {
                inBreak = false;
                advanceHorda();
            }
            return;
        }

        if (killsThisHorda >= killsToNextHorda) {
            killsThisHorda = 0;
            inBreak        = true;
            breakTimer     = BREAK_DURATION;
            return;
        }

        spawnTimer += dt;
        float effectiveInterval = spawnInterval * DifficultySettings.spawnIntervalMult();
        if (spawnTimer >= effectiveInterval && enemies.size() < maxOnScreen) {
            spawnTimer = 0f;
            spawnBatch(playerX, playerY);
        }
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        for (Enemy e : enemies) e.draw(g2, camX, camY);
    }

    private void spawnBatch(float px, float py) {
        int slots = maxOnScreen - enemies.size();
        if (slots <= 0) return;

        // Batch cresce com hordas: 1-2 no inicio, ate 4 nas altas
        int batchSize = Math.min(slots, 1 + Math.min(hordaNumber / 3, 3));

        for (int i = 0; i < batchSize; i++) {
            int type = pickType();

            float angle  = (float)(Math.random() * Math.PI * 2);
            float radius = 340f + (float)(Math.random() * 160f);
            float ex     = px + (float)Math.cos(angle) * radius;
            float ey     = py + (float)Math.sin(angle) * radius;
            enemies.add(new Enemy(ex, ey, type));
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
        killsToNextHorda = killsRequired(hordaNumber);

        // Mais inimigos na tela a cada 2 hordas
        if (hordaNumber % 2 == 0)
            maxOnScreen = Math.min(maxOnScreen + 3, MAX_ON_SCREEN_CAP);

        // Spawn mais rapido a cada 3 hordas
        if (hordaNumber % 3 == 0)
            spawnInterval = Math.max(0.5f, spawnInterval - 0.15f);

        // Notifica qual tipo sera dominante nesta horda (pico da tabela)
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
        maxOnScreen      = 10;
        spawnTimer       = 0f;
        spawnInterval    = 1.6f;
        inBreak          = false;
        breakTimer       = 0f;
    }

    public void advanceHordaPublic() { advanceHorda(); }
}