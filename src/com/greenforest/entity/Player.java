package com.greenforest.entity;

import com.greenforest.powerup.PowerUp;

import java.awt.*;

public class Player extends Entity {

    // level / XP
    private int level         = 1;
    private int xp            = 0;
    private int xpToNextLevel = 20;   // nivel 2 custa 20 XP
    private boolean levelUpPending = false;

    // stats base
    private int   kills        = 0;
    private float attackDamage = 20f;
    private float attackSpeed  = 1.5f;

    // power-up: multiplicadores
    private float damageMult    = 1f;
    private float speedMult     = 1f;
    private float fireRateMult  = 1f;   // >1 = atira mais rapido
    private int   extraShots    = 0;    // projéteis extras por tiro
    private int   vampHeal      = 0;    // HP recuperado ao matar inimigo
    private int   shield        = 0;    // HP do escudo atual
    private int   maxShield     = 0;    // HP maximo do escudo

    // i-frames
    private float iFrameTimer = 0f;
    private static final float I_FRAME_DURATION = 1.0f;

    // mira e animacao
    private float aimDirX  = 1f, aimDirY = 0f;
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
        float spd = speedMult;   // multiplicador aplicado em GamePanel
        x = Math.max(size / 2f,
                Math.min(worldW - size / 2f, x + dx * spd));
        y = Math.max(size / 2f,
                Math.min(worldH - size / 2f, y + dy * spd));
    }

    @Override
    public void takeDamage(int dmg) {
        if (iFrameTimer > 0f) return;

        // escudo absorve primeiro
        if (shield > 0) {
            int absorbed = Math.min(shield, dmg);
            shield -= absorbed;
            dmg    -= absorbed;
        }
        if (dmg > 0) super.takeDamage(dmg);
        iFrameTimer = I_FRAME_DURATION;
    }

    public void restoreHP(int amount) {
        hp = Math.min(hp + amount, maxHp);
    }

    public void gainXP(int amount) {
        xp += amount;
        if (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            level++;
            // proximo nivel custa 15 XP a mais
            xpToNextLevel = 20 + (level - 1) * 15;
            levelUpPending = true;
        }
    }

    public boolean isLevelUpPending() { return levelUpPending; }

    public void clearLevelUpPending() { levelUpPending = false; }

    public void applyPowerUp(PowerUp pu) {
        switch (pu) {
            case MAIS_DANO      -> damageMult  *= 1.25f;
            case MAIS_VELOCIDADE -> speedMult  *= 1.20f;
            case CURA           -> restoreHP(40);
            case MAIS_PROJETEIS -> extraShots++;
            case VAMPIRISMO     -> vampHeal    += 3;
            case ESCUDO         -> {
                maxShield += 30;
                shield     = Math.min(shield + 30, maxShield);
            }
            case CADENCIA       -> fireRateMult *= 1.25f;
        }
    }

    public void onKill() {
        kills++;
        if (vampHeal > 0) restoreHP(vampHeal);
    }

    public void addKill() { onKill(); }


    public void setAimDirection(float dx, float dy) {
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) { aimDirX = dx / len; aimDirY = dy / len; }
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        int sx = (int)(x - camX);
        int sy = (int)(y - camY);

        // Sombra
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillOval(sx - 16, sy + 12, 32, 9);

        // Piscar durante i-frames
        if (iFrameTimer > 0f && (int)(iFrameTimer * 10) % 2 == 0) return;

        drawPlant(g2, sx, sy);

        // Anel de escudo
        if (shield > 0) {
            float ratio = (float) shield / maxShield;
            int alpha = (int)(80 + 80 * ratio);
            g2.setColor(new Color(100, 180, 255, alpha));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(sx - size / 2 - 6, sy - size / 2 - 6,
                    size + 12, size + 12);
            g2.setStroke(new BasicStroke(1f));
        }
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

        // Folha esquerda
        g2.setColor(new Color(50, 160, 50));
        int[] lx  = {sx - 5, sx - 20, sx - 3};
        int[] ly  = {sy + 4 + b, sy - 4 + b, sy - 6 + b};
        g2.fillPolygon(lx, ly, 3);
        g2.setColor(new Color(70, 200, 70));
        int[] lx2 = {sx - 5, sx - 15, sx - 3};
        int[] ly2 = {sy + 3 + b, sy - 2 + b, sy - 4 + b};
        g2.fillPolygon(lx2, ly2, 3);

        // Folha direita
        g2.setColor(new Color(50, 160, 50));
        int[] rx  = {sx + 5, sx + 20, sx + 3};
        int[] ry  = {sy + 4 + b, sy - 2 + b, sy - 8 + b};
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

        // Petalas
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

    public int   getLevel()          { return level; }
    public int   getXP()             { return xp; }
    public int   getXPToNext()       { return xpToNextLevel; }
    public int   getKills()          { return kills; }
    public float getAttackDamage()   { return attackDamage * damageMult; }
    public float getAttackSpeed()    { return attackSpeed * fireRateMult; }
    public float getSpeedMult()      { return speedMult; }
    public int   getExtraShots()     { return extraShots; }
    public int   getShield()         { return shield; }
    public int   getMaxShield()      { return maxShield; }
    public float getAimDirX()        { return aimDirX; }
    public float getAimDirY()        { return aimDirY; }
    public boolean isInvincible()    { return iFrameTimer > 0f; }
}