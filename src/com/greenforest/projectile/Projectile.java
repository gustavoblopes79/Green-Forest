package com.greenforest.projectile;

import java.awt.*;

public class Projectile {

    private float x, y;
    private final float dx, dy;
    private final float speed = 6f;
    private final int   damage;
    private final int   range = 700;
    private float traveledDist = 0f;
    private boolean dead       = false;

    // Trail (rastro)
    private static final int TRAIL_LEN = 6;
    private final float[] trailX = new float[TRAIL_LEN];
    private final float[] trailY = new float[TRAIL_LEN];
    private int trailHead = 0;

    public Projectile(float x, float y, float dirX, float dirY, int damage) {
        this.x      = x;
        this.y      = y;
        this.dx     = dirX;
        this.dy     = dirY;
        this.damage = damage;
        for (int i = 0; i < TRAIL_LEN; i++) {
            trailX[i] = x;
            trailY[i] = y;
        }
    }

    public void update() {
        trailX[trailHead] = x;
        trailY[trailHead] = y;
        trailHead = (trailHead + 1) % TRAIL_LEN;

        x += dx * speed;
        y += dy * speed;
        traveledDist += speed;
        if (traveledDist >= range) dead = true;
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        // Rastro
        for (int i = 0; i < TRAIL_LEN; i++) {
            int idx   = (trailHead + i) % TRAIL_LEN;
            float ratio = (float) i / TRAIL_LEN;
            int alpha = (int)(ratio * 130);
            int rad   = (int)(ratio * 7) + 2;
            int tx    = (int)(trailX[idx] - camX);
            int ty    = (int)(trailY[idx] - camY);
            g2.setColor(new Color(80, 220, 80, alpha));
            g2.fillOval(tx - rad / 2, ty - rad / 2, rad, rad);
        }

        int sx = (int)(x - camX);
        int sy = (int)(y - camY);

        // Brilho externo
        g2.setColor(new Color(100, 255, 100, 70));
        g2.fillOval(sx - 8, sy - 8, 16, 16);

        // Corpo da semente
        g2.setColor(new Color(50, 200, 60));
        g2.fillOval(sx - 5, sy - 5, 10, 10);

        // Destaque
        g2.setColor(new Color(160, 255, 160));
        g2.fillOval(sx - 3, sy - 3, 6, 6);

        // Ponto branco central
        g2.setColor(Color.WHITE);
        g2.fillOval(sx - 1, sy - 1, 3, 3);
    }

    public float   getX()      { return x; }
    public float   getY()      { return y; }
    public int     getDamage() { return damage; }
    public boolean isDead()    { return dead; }
    public void    kill()      { dead = true; }
}