package com.greenforest.entity;

import com.greenforest.AttackType;
import com.greenforest.powerup.PowerUp;

import java.awt.*;

public class Player extends Entity {

    // level / XP
    private int level         = 1;
    private int xp            = 0;
    private int xpToNextLevel = 20;
    private boolean levelUpPending = false;
    private static final int POWERUP_INTERVAL = 3;

    // stats base
    private int   kills        = 0;
    private float attackDamage = 20f;
    private float attackSpeed  = 1.5f;

    // power-up: multiplicadores
    private float damageMult    = 1f;
    private float speedMult     = 1f;
    private float fireRateMult  = 1f;
    private int   extraShots    = 0;
    private int   vampHeal      = 0;
    private int   shield        = 0;
    private int   maxShield     = 0;

    // i-frames
    private float iFrameTimer = 0f;
    private static final float I_FRAME_DURATION = 1.0f;

    // mira e animacao
    private float aimDirX  = 1f, aimDirY = 0f;
    private float animTimer = 0f;

    // --- ataque em area ---
    private AttackType attackType = AttackType.PROJECTILE;

    // cooldown do pulso: dispara a cada AREA_COOLDOWN segundos
    private static final float AREA_COOLDOWN   = 1.8f;
    // raio base do pulso (pixels world)
    private static final float AREA_RADIUS     = 140f;
    // dano base do pulso (multiplicado por damageMult)
    private static final float AREA_DAMAGE_BASE = 35f;

    private float areaCooldownTimer = 0f;  // contador regressivo
    // para animacao visual: tempo desde o ultimo pulso (GamePanel usa isso)
    private float areaPulseAnim = -1f;     // -1 = sem pulso ativo
    private static final float PULSE_ANIM_DURATION = 0.35f;

    public Player(float x, float y) {
        super(x, y, 100, 26);
    }

    @Override
    public void update(float dt, float playerX, float playerY) {
        animTimer += dt;
        if (iFrameTimer > 0f) iFrameTimer -= dt;

        // cooldown de area
        if (attackType == AttackType.AREA) {
            if (areaCooldownTimer > 0f) {
                areaCooldownTimer -= dt;
            }
        }

        // animacao de pulso (decai independente do modo)
        if (areaPulseAnim >= 0f) {
            areaPulseAnim += dt;
            if (areaPulseAnim > PULSE_ANIM_DURATION) areaPulseAnim = -1f;
        }
    }

    /**
     * Retorna true se o pulso de area deve ser disparado agora.
     * O EnemyManager chama este metodo e, se retornar true, aplica o dano.
     * Reseta o timer automaticamente.
     */
    public boolean pollAreaPulse() {
        if (attackType != AttackType.AREA) return false;
        if (areaCooldownTimer <= 0f) {
            areaCooldownTimer = AREA_COOLDOWN;
            areaPulseAnim = 0f;    // inicia animacao
            return true;
        }
        return false;
    }

    public float getAreaRadius()  { return AREA_RADIUS; }
    public float getAreaDamage()  { return AREA_DAMAGE_BASE * damageMult; }

    /**
     * Retorna o progresso da animacao do pulso entre 0.0 e 1.0,
     * ou -1 se nenhum pulso esta sendo animado.
     */
    public float getAreaPulseProgress() {
        if (areaPulseAnim < 0f) return -1f;
        return areaPulseAnim / PULSE_ANIM_DURATION;
    }

    /**
     * Retorna o ratio do cooldown (0 = pronto, 1 = recarregando).
     */
    public float getAreaCooldownRatio() {
        if (attackType != AttackType.AREA) return 0f;
        return Math.max(0f, Math.min(1f, areaCooldownTimer / AREA_COOLDOWN));
    }

    public AttackType getAttackType() { return attackType; }

    public void setAttackType(AttackType t) {
        this.attackType = t;
        // ao trocar, reseta o timer para o jogador nao ter pulso imediato
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
            case FERTILIZANTE   -> damageMult  *= 1.25f;
            case COMPOSTAGEM    -> speedMult   *= 1.20f;
            case FOTOSINTESE    -> restoreHP(40);
            case SEMENTES       -> extraShots++;
            case BIOLOGICO      -> vampHeal    += 3;
            case BIOFILTRO      -> {
                maxShield += 30;
                shield     = Math.min(shield + 30, maxShield);
            }
            case ENERGIA_SOLAR  -> fireRateMult *= 1.25f;
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

        // Aura de area (desenhada atras do sprite)
        drawAreaAura(g2, sx, sy);

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

    private void drawAreaAura(Graphics2D g2, int sx, int sy) {
        if (attackType != AttackType.AREA) return;

        int r = (int) AREA_RADIUS;

        // Anel estatico de alcance (tenue)
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(120, 255, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(sx - r, sy - r, r * 2, r * 2);
        g2.setStroke(new BasicStroke(1f));
        g2.setComposite(old);

        // Barra de cooldown em arco (circulo ao redor do jogador)
        float coolRatio = 1f - getAreaCooldownRatio(); // 0=recarregando, 1=pronto
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

        // Animacao de pulso: onda se expandindo
        float pulse = getAreaPulseProgress();
        if (pulse >= 0f) {
            float pr = pulse; // 0..1
            int   pr_pixels = (int)(pr * r);
            int   alphaVal  = (int)(200 * (1f - pr));
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alphaVal / 255f));
            g2.setColor(new Color(140, 255, 140));
            g2.setStroke(new BasicStroke(3f - pr * 2f));
            g2.drawOval(sx - pr_pixels, sy - pr_pixels,
                    pr_pixels * 2, pr_pixels * 2);
            // preenchimento suave
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alphaVal / 600f));
            g2.setColor(new Color(180, 255, 160));
            g2.fillOval(sx - pr_pixels, sy - pr_pixels,
                    pr_pixels * 2, pr_pixels * 2);
            g2.setStroke(new BasicStroke(1f));
            g2.setComposite(old);
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
    }
}