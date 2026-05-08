package com.greenforest.manager;

import com.greenforest.entity.Boss;
import com.greenforest.entity.Boss.BossType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BossManager {

    // Boss spawna a cada 2 minutos (120 segundos)
    private static final float BOSS_INTERVAL = 120f;

    private float timer = 0f;
    private Boss  activeBoss = null;
    private int   bossIndex  = 0;

    private final BossType[] sequence = {
            BossType.E_WASTE,
            BossType.GARBAGE_TRUCK,
            BossType.FACTORY
    };

    // Chamado pelo GamePanel a cada frame enquanto PLAYING
    public void update(float dt, float playerX, float playerY, float worldW, float worldH) {
        if (activeBoss != null) {
            if (activeBoss.isDead()) {
                // Nao remove aqui — GamePanel pergunta com pollDeadBoss()
                return;
            }
            activeBoss.update(dt, playerX, playerY);
            return;
        }

        timer += dt;
        if (timer >= BOSS_INTERVAL) {
            timer = 0f;
            spawnBoss(playerX, playerY, worldW, worldH);
        }
    }

    private void spawnBoss(float px, float py, float worldW, float worldH) {
        BossType type = sequence[bossIndex % sequence.length];
        bossIndex++;

        // Spawna numa borda aleatoria, longe do jogador
        float bx, by;
        int edge = (int)(Math.random() * 4);
        bx = switch (edge) {
            case 0 -> 100f;
            case 1 -> worldW - 100f;
            default -> px + (Math.random() > 0.5 ? 400f : -400f);
        };
        by = switch (edge) {
            case 2 -> 100f;
            case 3 -> worldH - 100f;
            default -> py + (Math.random() > 0.5 ? 400f : -400f);
        };

        bx = Math.max(100f, Math.min(worldW  - 100f, bx));
        by = Math.max(100f, Math.min(worldH - 100f, by));

        activeBoss = new Boss(bx, by, type);
    }

    /**
     * Se o boss morreu, retorna ele e limpa o slot (GamePanel exibirá a licao).
     */
    public Boss pollDeadBoss() {
        if (activeBoss != null && activeBoss.isDead()) {
            Boss dead = activeBoss;
            activeBoss = null;
            return dead;
        }
        return null;
    }

    public Boss getActiveBoss() { return activeBoss; }

    public boolean hasBoss() { return activeBoss != null && !activeBoss.isDead(); }

    public void draw(Graphics2D g2, int camX, int camY) {
        if (hasBoss()) activeBoss.draw(g2, camX, camY);
    }

    /** Aviso visual quando o boss esta prestes a spawnar (ultimos 10s) */
    public boolean isBossImminent() {
        return activeBoss == null && timer >= BOSS_INTERVAL - 10f;
    }

    public float getTimerRatio() { return Math.min(timer / BOSS_INTERVAL, 1f); }

    public void reset() {
        timer      = 0f;
        activeBoss = null;
        bossIndex  = 0;
    }
}