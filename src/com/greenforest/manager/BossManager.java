package com.greenforest.manager;

import com.greenforest.DifficultySettings;
import com.greenforest.entity.Boss;
import com.greenforest.entity.Boss.BossType;
import java.awt.*;

public class BossManager {

    private static final float BOSS_INTERVAL_BASE = 120f;

    private float bossInterval;
    private float timer      = 0f;
    private Boss  activeBoss = null;
    private int   bossIndex  = 0;

    private final BossType[] sequence = {
            BossType.E_WASTE,
            BossType.GARBAGE_TRUCK,
            BossType.FACTORY,
            BossType.PETROLEO,
            BossType.DESMATAMENTO,
            BossType.PLASTICO_OCEANO
    };

    public BossManager() {
        // Aplica modificador de dificuldade no intervalo
        this.bossInterval = BOSS_INTERVAL_BASE
                + DifficultySettings.bossIntervalBonus();
    }

    public void update(float dt, float playerX, float playerY,
                       float worldW, float worldH) {
        if (activeBoss != null) {
            if (activeBoss.isDead()) return; // GamePanel consome com pollDeadBoss
            activeBoss.update(dt, playerX, playerY);
            return;
        }

        timer += dt;
        if (timer >= bossInterval) {
            timer = 0f;
            spawnBoss(playerX, playerY, worldW, worldH);
        }
    }

    private void spawnBoss(float px, float py, float worldW, float worldH) {
        BossType type = sequence[bossIndex % sequence.length];
        bossIndex++;

        // Spawna em borda aleatoria, longe do player
        float bx, by;
        int edge = (int)(Math.random() * 4);
        bx = switch (edge) {
            case 0  -> 120f;
            case 1  -> worldW - 120f;
            default -> px + (Math.random() > 0.5 ? 420f : -420f);
        };
        by = switch (edge) {
            case 2  -> 120f;
            case 3  -> worldH - 120f;
            default -> py + (Math.random() > 0.5 ? 420f : -420f);
        };

        bx = Math.max(120f, Math.min(worldW - 120f, bx));
        by = Math.max(120f, Math.min(worldH - 120f, by));

        activeBoss = new Boss(bx, by, type);
    }

    public Boss pollDeadBoss() {
        if (activeBoss != null && activeBoss.isDead()) {
            Boss dead = activeBoss;
            activeBoss = null;
            return dead;
        }
        return null;
    }

    public Boss    getActiveBoss()  { return activeBoss; }
    public boolean hasBoss()        { return activeBoss != null && !activeBoss.isDead(); }
    public boolean isBossImminent() { return activeBoss == null && timer >= bossInterval - 10f; }
    public float   getTimerRatio()  { return Math.min(timer / bossInterval, 1f); }

    public void draw(Graphics2D g2, int camX, int camY) {
        if (hasBoss()) activeBoss.draw(g2, camX, camY);
    }

    public void reset() {
        timer      = 0f;
        activeBoss = null;
        bossIndex  = 0;
        bossInterval = BOSS_INTERVAL_BASE + DifficultySettings.bossIntervalBonus();
    }
}