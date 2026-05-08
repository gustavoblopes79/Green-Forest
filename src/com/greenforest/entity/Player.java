package com.greenforest.entity;

import java.awt.*;

public class Player extends Entity {

    private int   level         = 1;
    private int   xp            = 0;
    private int   xpToNextLevel = 50;
    private int   kills         = 0;
    private float attackDamage  = 20f;
    private float attackSpeed   = 1.5f;

    private float iFrameTimer = 0f;
    private static final float I_FRAME_DURATION = 1.0f;

    private float aimDirX = 1f, aimDirY = 0f;
    private float animTimer = 0f;

    public Player(float x, float y) {
        super(x, y, 100, 26);
    }

    @Override
    public void update(float dt, float playerX, float playerY) {
        animTimer += dt;
        if (iFrameTimer > 0f) iFrameTimer -= dt;
    }

    public void move(float dx, float dy, float worldW, float worldH) {
        x = Math.max(size / 2f, Math.min(worldW - size / 2f, x + dx));
        y = Math.max(size / 2f, Math.min(worldH - size / 2f, y + dy));
    }

    public void takeDamage(int dmg) {
        if (iFrameTimer > 0f) return;
        super.takeDamage(dmg);
        iFrameTimer = I_FRAME_DURATION;
    }

    public void gainXP(int amount) {
        xp += amount;
        while (xp >= xpToNextLevel) {
            xp           -= xpToNextLevel;
            level++;
            xpToNextLevel = (int)(xpToNextLevel * 1.4f);
            attackDamage += 5f;
            attackSpeed  += 0.15f;
            hp            = Math.min(hp + 30, maxHp);
        }
    }

    public void setAimDirection(float dx, float dy) {
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            aimDirX = dx / len;
            aimDirY = dy / len;
        }
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        int sx = (int)(x - camX);
        int sy = (int)(y - camY);

        // Sombra
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillOval(sx - 16, sy + 12, 32, 9);

        boolean invincible = iFrameTimer > 0f;
        if (invincible && (int)(iFrameTimer * 10) % 2 == 0) return;

        drawPlant(g2, sx, sy);
    }

    private void drawPlant(Graphics2D g2, int sx, int sy) {
        float bob = (float) Math.sin(animTimer * 3.2f) * 2f;
        int b = (int) bob;

        // Raizes
        g2.setColor(new Color(101, 67, 33));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(sx - 4, sy + 13 + b, sx - 10, sy + 22 + b);
        g2.drawLine(sx,     sy + 14 + b, sx,       sy + 22 + b);
        g2.drawLine(sx + 4, sy + 13 + b, sx + 10,  sy + 22 + b);
        g2.setStroke(new BasicStroke(1f));

        // Caule
        g2.setColor(new Color(85, 140, 55));
        g2.fillRoundRect(sx - 5, sy - 2 + b, 10, 18, 5, 5);

        // Folhas laterais
        // Esquerda
        g2.setColor(new Color(50, 160, 50));
        int[] lx = {sx - 5, sx - 20, sx - 3};
        int[] ly = {sy + 4 + b, sy - 4 + b, sy - 6 + b};
        g2.fillPolygon(lx, ly, 3);
        g2.setColor(new Color(70, 200, 70));
        int[] lx2 = {sx - 5, sx - 15, sx - 3};
        int[] ly2 = {sy + 3 + b, sy - 2 + b, sy - 4 + b};
        g2.fillPolygon(lx2, ly2, 3);

        // Direita
        g2.setColor(new Color(50, 160, 50));
        int[] rx = {sx + 5, sx + 20, sx + 3};
        int[] ry = {sy + 4 + b, sy - 2 + b, sy - 8 + b};
        g2.fillPolygon(rx, ry, 3);
        g2.setColor(new Color(70, 200, 70));
        int[] rx2 = {sx + 5, sx + 14, sx + 3};
        int[] ry2 = {sy + 3 + b, sy - 1 + b, sy - 6 + b};
        g2.fillPolygon(rx2, ry2, 3);

        // Copa
        g2.setColor(new Color(30, 170, 60));
        g2.fillOval(sx - 14, sy - 26 + b, 28, 24);
        g2.setColor(new Color(55, 210, 85));
        g2.fillOval(sx - 10, sy - 28 + b, 20, 18);

        // Petala externa da flor
        g2.setColor(new Color(255, 100, 150));
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60);
            int px = sx + (int)(Math.cos(angle) * 6);
            int py = sy - 30 + b + (int)(Math.sin(angle) * 6);
            g2.fillOval(px - 3, py - 3, 7, 7);
        }
        // Centro da flor
        g2.setColor(new Color(255, 230, 0));
        g2.fillOval(sx - 4, sy - 34 + b, 9, 9);
        g2.setColor(new Color(220, 160, 0));
        g2.fillOval(sx - 2, sy - 32 + b, 5, 5);
    }

    public int   getLevel()        { return level; }
    public int   getXP()           { return xp; }
    public int   getXPToNext()     { return xpToNextLevel; }
    public int   getKills()        { return kills; }
    public float getAttackDamage() { return attackDamage; }
    public float getAttackSpeed()  { return attackSpeed; }
    public float getAimDirX()      { return aimDirX; }
    public float getAimDirY()      { return aimDirY; }
    public boolean isInvincible()  { return iFrameTimer > 0f; }

    public void addKill()   { kills++; }
    public void restoreHP(int amount) {
        hp = Math.min(hp + amount, maxHp);
    }
}