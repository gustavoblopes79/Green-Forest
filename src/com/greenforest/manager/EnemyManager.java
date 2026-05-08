package com.greenforest.manager;

import com.greenforest.DifficultySettings;
import com.greenforest.entity.Enemy;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EnemyManager {

    private final List<Enemy> enemies = new ArrayList<>();

    // Horda por kills
    private int   hordaNumber      = 1;
    private int   killsThisHorda   = 0;
    private int   killsToNextHorda = 15;   // kills necessários para avançar
    private int   maxOnScreen      = 8;    // inimigos simultâneos no início
    private static final int MAX_ON_SCREEN_CAP = 40;

    // Spawn
    private float spawnTimer    = 0f;
    private float spawnInterval = 2.0f;    // segundos entre spawns

    // Pausa entre hordas
    private boolean inBreak     = false;
    private float   breakTimer  = 0f;
    private static final float BREAK_DURATION = 3.0f;

    // Tipo predominante
    private int   lastHordaType = -1;
    private float gameTime      = 0f;
    private Consumer<Integer> onHordaChange;

    public void setOnHordaChange(Consumer<Integer> cb) { this.onHordaChange = cb; }

    // Update
    public void update(float dt, float playerX, float playerY,
                       float gameTimeSec) {
        this.gameTime = gameTimeSec;

        // Conta kills desta horda
        int newKills = 0;
        for (Enemy e : enemies) {
            if (e.isDead() && !e.isXpAwarded()) newKills++;
        }
        killsThisHorda += newKills;

        enemies.removeIf(Enemy::isDead);
        for (Enemy e : enemies) e.update(dt, playerX, playerY);

        // Pausa entre hordas
        if (inBreak) {
            breakTimer -= dt;
            if (breakTimer <= 0f) {
                inBreak = false;
                advanceHorda();
            }
            return; // não spawna durante a pausa
        }

        // Verificar se horda foi concluída
        if (killsThisHorda >= killsToNextHorda) {
            killsThisHorda = 0;
            inBreak        = true;
            breakTimer     = BREAK_DURATION;
            return;
        }

        // Spawn normal
        spawnTimer += dt;
        float effectiveInterval =
                spawnInterval * DifficultySettings.spawnIntervalMult();
        if (spawnTimer >= effectiveInterval
                && enemies.size() < maxOnScreen) {
            spawnTimer = 0f;
            spawnBatch(playerX, playerY);
        }
    }

    // Avança para a próxima horda
    private void advanceHorda() {
        hordaNumber++;

        // Kills necessários cresce ~30% por horda
        killsToNextHorda = (int)(killsToNextHorda * 1.3f);

        // Mais inimigos simultâneos a cada 2 hordas
        if (hordaNumber % 2 == 0) {
            maxOnScreen = Math.min(maxOnScreen + 3, MAX_ON_SCREEN_CAP);
        }

        // Spawn mais rápido a cada 3 hordas
        if (hordaNumber % 3 == 0) {
            spawnInterval = Math.max(0.6f, spawnInterval - 0.2f);
        }

        // Notifica mudança de tipo predominante
        int maxType = Math.min(4, (hordaNumber - 1) / 2);
        int newType = (hordaNumber - 1) % (maxType + 1);
        if (newType != lastHordaType) {
            lastHordaType = newType;
            if (onHordaChange != null) onHordaChange.accept(newType);
        }
    }

    // Spawna um lote de inimigos
    private void spawnBatch(float px, float py) {
        // Quantos podemos ainda colocar na tela
        int slots = maxOnScreen - enemies.size();
        if (slots <= 0) return;

        // Por horda avançada spawna mais de uma vez por tick
        int batchSize = Math.min(slots, 1 + hordaNumber / 5);

        // Tipos disponíveis crescem com a horda
        int maxType = Math.min(4, (hordaNumber - 1) / 2);

        for (int i = 0; i < batchSize; i++) {
            // Bias para o tipo predominante da horda (70% chance)
            int predominant = (hordaNumber - 1) % (maxType + 1);
            int type = (Math.random() < 0.70)
                    ? predominant
                    : (int)(Math.random() * (maxType + 1));

            float angle  = (float)(Math.random() * Math.PI * 2);
            float radius = 350f + (float)(Math.random() * 150f);
            float ex     = px + (float) Math.cos(angle) * radius;
            float ey     = py + (float) Math.sin(angle) * radius;
            enemies.add(new Enemy(ex, ey, type));
        }
    }

    // Draw
    public void draw(Graphics2D g2, int camX, int camY) {
        for (Enemy e : enemies) e.draw(g2, camX, camY);
    }

    // Getters
    public List<Enemy> getEnemies()     { return enemies; }
    public int         getHordaNumber() { return hordaNumber; }
    public int         getKillsThisHorda()   { return killsThisHorda; }
    public int         getKillsToNextHorda() { return killsToNextHorda; }
    public boolean     isInBreak()      { return inBreak; }
    public float       getBreakTimer()  { return breakTimer; }

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
}