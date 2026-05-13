package com.greenforest.entity;

import com.greenforest.AttackType;
import com.greenforest.powerup.PowerUp;

import java.awt.*;

public class Player extends Entity {

    private int level         = 1;
    private int xp            = 0;
    private int xpToNextLevel = 20;
    private boolean levelUpPending = false;
    private static final int POWERUP_INTERVAL = 3;

    private int   kills        = 0;
    private float attackDamage = 20f;
    private float attackSpeed  = 1.5f;

    private float damageMult    = 1f;
    private float speedMult     = 1f;
    private float fireRateMult  = 1f;
    private int   extraShots    = 0;
    private int   vampHeal      = 0;
    private int   shield        = 0;
    private int   maxShield     = 0;

    private float iFrameTimer = 0f;
    private static final float I_FRAME_DURATION = 1.0f;

    private float aimDirX  = 1f, aimDirY = 0f;
    private float animTimer = 0f;

    private AttackType attackType = AttackType.PROJECTILE;

    private static final float AREA_COOLDOWN    = 1.8f;
    private static final float AREA_RADIUS      = 140f;
    private static final float AREA_DAMAGE_BASE = 35f;

    private float areaCooldownTimer = 0f;
    private float areaPulseAnim = -1f;
    private static final float PULSE_ANIM_DURATION = 0.35f;

    private int nextScreenClearKillMilestone = 100;
    private boolean screenClearReady = false;
    private boolean screenClearJustTriggered = false;

    private float screenClearWaveAnim = -1f;
    private static final float SCREEN_CLEAR_WAVE_DURATION = 0.55f;
    private static final float SCREEN_CLEAR_WAVE_RADIUS = 260f;
    private static final int SCREEN_CLEAR_LEAF_COUNT = 18;

    public Player(float x, float y) {
        super(x, y, 100, 26);
    }

    @Override
    public void update(float dt, float playerX, float playerY) {
        animTimer += dt;
        if (iFrameTimer > 0f) iFrameTimer -= dt;

        if (attackType == AttackType.AREA) {
            if (areaCooldownTimer > 0f) {
                areaCooldownTimer -= dt;
            }
        }

        if (areaPulseAnim >= 0f) {
            areaPulseAnim += dt;
            if (areaPulseAnim > PULSE_ANIM_DURATION) areaPulseAnim = -1f;
        }

        if (screenClearWaveAnim >= 0f) {
            screenClearWaveAnim += dt;
            if (screenClearWaveAnim > SCREEN_CLEAR_WAVE_DURATION) {
                screenClearWaveAnim = -1f;
            }
        }
    }

    public boolean pollAreaPulse() {
        if (attackType != AttackType.AREA) return false;
        if (areaCooldownTimer <= 0f) {
            areaCooldownTimer = AREA_COOLDOWN;
            areaPulseAnim = 0f;
            return true;
        }
        return false;
    }

    public float getAreaRadius() { return AREA_RADIUS; }
    public float getAreaDamage() { return AREA_DAMAGE_BASE * damageMult; }

    public float getAreaPulseProgress() {
        if (areaPulseAnim < 0f) return -1f;
        return areaPulseAnim / PULSE_ANIM_DURATION;
    }

    public float getAreaCooldownRatio() {
        if (attackType != AttackType.AREA) return 0f;
        return Math.max(0f, Math.min(1f, areaCooldownTimer / AREA_COOLDOWN));
    }

    public AttackType getAttackType() { return attackType; }

    public void setAttackType(AttackType t) {
        this.attackType = t;
        if (t == AttackType.AREA) areaCooldownTimer = AREA_COOLDOWN * 0.5f;
    }

    public void toggleAttackType() {
        setAttackType(attackType == AttackType.PROJECTILE
                ? AttackType.AREA
                : AttackType.PROJECTILE);
    }

    public void move(float dx, float dy, float worldW, float worldH) {
        float spd = speedMult;
        x = Math.max(size / 2f,
                Math.min(worldW - size / 2f, x + dx * spd));
        y = Math.max(size / 2f,
                Math.min(worldH - size / 2f, y + dy * spd));
    }

    @Override
    public void takeDamage(int dmg) {
        if (iFrameTimer > 0f) return;
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
            xpToNextLevel = 20 + (level - 1) * 15;
            if (level % POWERUP_INTERVAL == 0) levelUpPending = true;
        }
    }

    public boolean isLevelUpPending() { return levelUpPending; }
    public void clearLevelUpPending() { levelUpPending = false; }

    public void applyPowerUp(PowerUp pu) {
        switch (pu) {
            case FERTILIZANTE   -> damageMult *= 1.25f;
            case COMPOSTAGEM    -> speedMult *= 1.20f;
            case FOTOSINTESE    -> restoreHP(40);
            case SEMENTES       -> extraShots++;
            case BIOLOGICO      -> vampHeal += 3;
            case BIOFILTRO      -> {
                maxShield += 30;
                shield = Math.min(shield + 30, maxShield);
            }
            case ENERGIA_SOLAR  -> fireRateMult *= 1.25f;
        }
    }

    public void onKill() {
        kills++;
        if (vampHeal > 0) restoreHP(vampHeal);

        if (kills >= nextScreenClearKillMilestone) {
            screenClearReady = true;
            nextScreenClearKillMilestone += 100;
        }
    }

    public void addKill() { onKill(); }

    public boolean hasScreenClearReady() {
        return screenClearReady;
    }

    public int getNextScreenClearKillMilestone() {
        return nextScreenClearKillMilestone;
    }

    public void consumeScreenClear() {
        screenClearReady = false;
        screenClearJustTriggered = true;
        screenClearWaveAnim = 0f;
    }

    public boolean pollScreenClearTriggered() {
        boolean triggered = screenClearJustTriggered;
        screenClearJustTriggered = false;
        return triggered;
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

        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillOval(sx - 16, sy + 12, 32, 9);

        if (iFrameTimer > 0f && (int)(iFrameTimer * 10) % 2 == 0) return;

        drawAreaAura(g2, sx, sy);
        drawScreenClearWave(g2, sx, sy);
        drawPlant(g2, sx, sy);

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

    private void drawAreaAura(Graphics2D g2, int sx, int sy) {
        if (attackType != AttackType.AREA) return;

        int r = (int) AREA_RADIUS;

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(120, 255, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(sx - r, sy - r, r * 2, r * 2);
        g2.setStroke(new BasicStroke(1f));
        g2.setComposite(old);

        float coolRatio = 1f - getAreaCooldownRatio();
        if (coolRatio < 1f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            g2.setColor(new Color(80, 220, 80));
            g2.setStroke(new BasicStroke(3f));
            int arcR = size / 2 + 10;
            int arcAngle = (int)(360 * coolRatio);
            g2.drawArc(sx - arcR, sy - arcR, arcR * 2, arcR * 2,
                    90, arcAngle);
            g2.setStroke(new BasicStroke(1f));
            g2.setComposite(old);
        }

        float pulse = getAreaPulseProgress();
        if (pulse >= 0f) {
            float pr = pulse;
            int prPixels = (int)(pr * r);
            int alphaVal = (int)(200 * (1f - pr));
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alphaVal / 255f));
            g2.setColor(new Color(140, 255, 140));
            g2.setStroke(new BasicStroke(3f - pr * 2f));
            g2.drawOval(sx - prPixels, sy - prPixels,
                    prPixels * 2, prPixels * 2);

            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alphaVal / 600f));
            g2.setColor(new Color(180, 255, 160));
            g2.fillOval(sx - prPixels, sy - prPixels,
                    prPixels * 2, prPixels * 2);

            g2.setStroke(new BasicStroke(1f));
            g2.setComposite(old);
        }
    }

    private void drawScreenClearWave(Graphics2D g2, int sx, int sy) {
        if (screenClearWaveAnim < 0f) return;

        float p = screenClearWaveAnim / SCREEN_CLEAR_WAVE_DURATION;
        if (p < 0f) p = 0f;
        if (p > 1f) p = 1f;

        int radius = (int)(SCREEN_CLEAR_WAVE_RADIUS * p);
        int alpha = (int)(210 * (1f - p));

        Composite old = g2.getComposite();

        // Onda base suave
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, Math.max(0.08f, alpha / 255f)));
        g2.setColor(new Color(170, 255, 170));
        g2.setStroke(new BasicStroke(Math.max(1.5f, 6f - p * 4f)));
        g2.drawOval(sx - radius, sy - radius, radius * 2, radius * 2);

        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, Math.max(0.02f, alpha / 800f)));
        g2.setColor(new Color(120, 255, 140));
        g2.fillOval(sx - radius, sy - radius, radius * 2, radius * 2);

        // Explosao de folhas
        for (int i = 0; i < SCREEN_CLEAR_LEAF_COUNT; i++) {
            double ang = Math.toRadians((360.0 / SCREEN_CLEAR_LEAF_COUNT) * i
                    + p * 55.0
                    + (i % 2 == 0 ? 0 : 12));
            float dist = 18f + p * (SCREEN_CLEAR_WAVE_RADIUS - 22f)
                    * (0.72f + (i % 4) * 0.08f);

            int lx = sx + (int)(Math.cos(ang) * dist);
            int ly = sy + (int)(Math.sin(ang) * dist);

            Graphics2D leaf = (Graphics2D) g2.create();
            leaf.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            leaf.translate(lx, ly);
            leaf.rotate(ang + Math.PI / 2.0 + p * 2.2);

            float a = Math.max(0.08f, alpha / 255f);
            leaf.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, a));

            int leafW = 10 + (i % 3) * 2;
            int leafH = 18 + (i % 4) * 2;

            leaf.setColor(new Color(40, 170, 70));
            leaf.fillOval(-leafW / 2, -leafH / 2, leafW, leafH);

            leaf.setColor(new Color(90, 230, 110));
            leaf.fillOval(-leafW / 4, -leafH / 2 + 2, leafW / 2, leafH / 2);

            leaf.setColor(new Color(20, 110, 40));
            leaf.setStroke(new BasicStroke(1.2f));
            leaf.drawLine(0, -leafH / 2 + 2, 0, leafH / 2 - 2);

            leaf.dispose();
        }

        g2.setStroke(new BasicStroke(1f));
        g2.setComposite(old);
    }

    private void drawPlant(Graphics2D g2, int sx, int sy) {
        float bob = (float) Math.sin(animTimer * 3.2f) * 2f;
        int b = (int) bob;

        g2.setColor(new Color(101, 67, 33));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(sx - 4, sy + 13 + b, sx - 10, sy + 22 + b);
        g2.drawLine(sx,     sy + 14 + b, sx,       sy + 22 + b);
        g2.drawLine(sx + 4, sy + 13 + b, sx + 10,  sy + 22 + b);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(new Color(85, 140, 55));
        g2.fillRoundRect(sx - 5, sy - 2 + b, 10, 18, 5, 5);

        g2.setColor(new Color(50, 160, 50));
        int[] lx  = {sx - 5, sx - 20, sx - 3};
        int[] ly  = {sy + 4 + b, sy - 4 + b, sy - 6 + b};
        g2.fillPolygon(lx, ly, 3);
        g2.setColor(new Color(70, 200, 70));
        int[] lx2 = {sx - 5, sx - 15, sx - 3};
        int[] ly2 = {sy + 3 + b, sy - 2 + b, sy - 4 + b};
        g2.fillPolygon(lx2, ly2, 3);

        g2.setColor(new Color(50, 160, 50));
        int[] rx  = {sx + 5, sx + 20, sx + 3};
        int[] ry  = {sy + 4 + b, sy - 2 + b, sy - 8 + b};
        g2.fillPolygon(rx, ry, 3);
        g2.setColor(new Color(70, 200, 70));
        int[] rx2 = {sx + 5, sx + 14, sx + 3};
        int[] ry2 = {sy + 3 + b, sy - 1 + b, sy - 6 + b};
        g2.fillPolygon(rx2, ry2, 3);

        g2.setColor(new Color(30, 170, 60));
        g2.fillOval(sx - 14, sy - 26 + b, 28, 24);
        g2.setColor(new Color(55, 210, 85));
        g2.fillOval(sx - 10, sy - 28 + b, 20, 18);

        g2.setColor(new Color(255, 100, 150));
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60);
            int px = sx + (int)(Math.cos(angle) * 6);
            int py = sy - 30 + b + (int)(Math.sin(angle) * 6);
            g2.fillOval(px - 3, py - 3, 7, 7);
        }

        g2.setColor(new Color(255, 230, 0));
        g2.fillOval(sx - 4, sy - 34 + b, 9, 9);
        g2.setColor(new Color(220, 160, 0));
        g2.fillOval(sx - 2, sy - 32 + b, 5, 5);
    }

    public int getLevel()        { return level; }
    public int getXP()           { return xp; }
    public int getXPToNext()     { return xpToNextLevel; }
    public int getKills()        { return kills; }
    public float getAttackDamage() { return attackDamage * damageMult; }
    public float getAttackSpeed()  { return attackSpeed * fireRateMult; }
    public float getSpeedMult()    { return speedMult; }
    public int getExtraShots()     { return extraShots; }
    public int getShield()         { return shield; }
    public int getMaxShield()      { return maxShield; }
    public float getAimDirX()      { return aimDirX; }
    public float getAimDirY()      { return aimDirY; }
    public boolean isInvincible()  { return iFrameTimer > 0f; }

    public com.greenforest.SaveData toSaveData(float gameTimeSec,
                                           int hordaNumber,
                                           int diffOrdinal) {
        com.greenforest.SaveData d = new com.greenforest.SaveData();
        d.level         = this.level;
        d.xp            = this.xp;
        d.xpToNextLevel = this.xpToNextLevel;
        d.kills         = this.kills;
        d.attackDamage  = this.attackDamage;
        d.attackSpeed   = this.attackSpeed;
        d.damageMult    = this.damageMult;
        d.speedMult     = this.speedMult;
        d.fireRateMult  = this.fireRateMult;
        d.extraShots    = this.extraShots;
        d.vampHeal      = this.vampHeal;
        d.shield        = this.shield;
        d.maxShield     = this.maxShield;
        d.hp            = this.hp;
        d.maxHp         = this.maxHp;
        d.gameTimeSec   = gameTimeSec;
        d.hordaNumber   = hordaNumber;
        d.diffOrdinal   = diffOrdinal;
        return d;
    }

    public void loadFromSave(com.greenforest.SaveData d) {
        this.level         = d.level;
        this.xp            = d.xp;
        this.xpToNextLevel = d.xpToNextLevel;
        this.kills         = d.kills;
        this.attackDamage  = d.attackDamage;
        this.attackSpeed   = d.attackSpeed;
        this.damageMult    = d.damageMult;
        this.speedMult     = d.speedMult;
        this.fireRateMult  = d.fireRateMult;
        this.extraShots    = d.extraShots;
        this.vampHeal      = d.vampHeal;
        this.shield        = d.shield;
        this.maxShield     = d.maxShield;
        this.hp            = d.hp;
        this.maxHp         = d.maxHp;

        this.nextScreenClearKillMilestone =
                ((this.kills / 100) + 1) * 100;
        this.screenClearReady = false;
        this.screenClearJustTriggered = false;
        this.screenClearWaveAnim = -1f;
    }
}