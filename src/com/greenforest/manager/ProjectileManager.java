package com.greenforest.manager;

import com.greenforest.AttackType;
import com.greenforest.entity.Boss;
import com.greenforest.entity.Enemy;
import com.greenforest.entity.Player;
import com.greenforest.projectile.Projectile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectileManager {

    private final List<Projectile> projectiles = new ArrayList<>();
    private float attackTimer = 0f;


    public void update(float dt, Player player,
                       List<Enemy> enemies, Boss boss) {
        // Modo AREA: apenas avanca os projeteis existentes, nao gera novos
        if (player.getAttackType() == AttackType.AREA) {
            tickProjectiles();
            return;
        }

        attackTimer += dt;
        if (attackTimer >= 1f / player.getAttackSpeed()) {
            attackTimer = 0f;
            if (boss != null && !boss.isDead()) {
                float dx  = boss.getX() - player.getX();
                float dy  = boss.getY() - player.getY();
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len > 0) fireProjectile(player, dx / len, dy / len);
            } else {
                Enemy nearest = findNearest(player, enemies);
                if (nearest != null) {
                    float dx  = nearest.getX() - player.getX();
                    float dy  = nearest.getY() - player.getY();
                    float len = (float) Math.sqrt(dx * dx + dy * dy);
                    if (len > 0) fireProjectile(player, dx / len, dy / len);
                }
            }
        }
        tickProjectiles();
    }

    /** Sobrecarga de compatibilidade sem boss. */
    public void update(float dt, Player player, List<Enemy> enemies) {
        update(dt, player, enemies, null);
    }

    /** Modo MANUAL: tick sem auto-fire. */
    public void updateManual(float dt) {
        tickProjectiles();
    }

    /** Disparo manual acionado pelo clique. */
    public void manualFire(Player player) {
        // No modo AREA o clique nao dispara projeteis
        if (player.getAttackType() == AttackType.AREA) return;
        fireProjectile(player, player.getAimDirX(), player.getAimDirY());
    }

    private void fireProjectile(Player player, float dirX, float dirY) {
        projectiles.add(new Projectile(
                player.getX(), player.getY(),
                dirX, dirY,
                (int) player.getAttackDamage()
        ));
    }

    private void tickProjectiles() {
        for (Projectile p : projectiles) p.update();
        projectiles.removeIf(Projectile::isDead);
    }

    private Enemy findNearest(Player player, List<Enemy> enemies) {
        Enemy nearest = null;
        float minDist = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (e.isDead()) continue;
            float dx = e.getX() - player.getX();
            float dy = e.getY() - player.getY();
            float d  = dx * dx + dy * dy;
            if (d < minDist) { minDist = d; nearest = e; }
        }
        return nearest;
    }

    public void checkCollisions(List<Enemy> enemies, Player player) {
        for (Projectile p : projectiles) {
            if (p.isDead()) continue;
            for (Enemy e : enemies) {
                if (e.isDead()) continue;
                float dx   = p.getX() - e.getX();
                float dy   = p.getY() - e.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < e.getSize() / 2f + 5f) {
                    e.takeDamage(p.getDamage());
                    p.kill();
                    break;
                }
            }
        }
    }

    public void checkCollisionsWithBoss(Boss boss, Player player) {
        if (boss == null || boss.isDead()) return;
        for (Projectile p : projectiles) {
            if (p.isDead()) continue;
            float dx   = p.getX() - boss.getX();
            float dy   = p.getY() - boss.getY();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < boss.getSize() / 2f + 5f) {
                boss.takeDamage(p.getDamage());
                p.kill();
                break;
            }
        }
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        for (Projectile p : projectiles) p.draw(g2, camX, camY);
    }

    public void clear()                      { projectiles.clear(); }
    public List<Projectile> getProjectiles() { return projectiles; }
}