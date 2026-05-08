package com.greenforest.entity;

import com.greenforest.DifficultySettings;

import java.awt.*;

public class Enemy extends Entity {

    protected float speed;
    protected int   damage;
    protected int   xpValue;
    protected int   type;
    private boolean xpAwarded = false;
    private float   animTimer = 0f;

    public Enemy(float x, float y, int type) {
        super(x, y, hpForType(type), sizeForType(type));
        this.type    = type;
        this.damage  = (int)(damageForType(type) * DifficultySettings.enemyDamageMult());
        this.speed   = speedForType(type)         * DifficultySettings.enemySpeedMult();
        this.xpValue = xpForType(type);
        this.maxHp   = this.hp;
    }

    private static int hpForType(int t) {
        return switch (t) {
            case 0 -> 30; case 1 -> 50; case 2 -> 80;
            case 3 -> 40; case 4 -> 25; default -> 30;
        };
    }
    private static int damageForType(int t) {
        return switch (t) {
            case 0 -> 8; case 1 -> 12; case 2 -> 18;
            case 3 -> 10; case 4 -> 6; default -> 8;
        };
    }
    private static float speedForType(int t) {
        return switch (t) {
            case 0 -> 1.4f; case 1 -> 1.1f; case 2 -> 0.8f;
            case 3 -> 1.6f; case 4 -> 2.0f; default -> 1.2f;
        };
    }
    private static int xpForType(int t) {
        return switch (t) {
            case 0 -> 10; case 1 -> 15; case 2 -> 22;
            case 3 -> 12; case 4 -> 8; default -> 10;
        };
    }
    private static int sizeForType(int t) {
        return switch (t) {
            case 2 -> 28; case 3 -> 30; default -> 22;
        };
    }

    @Override
    public void update(float dt, float playerX, float playerY) {
        animTimer += dt;
        float dx   = playerX - x;
        float dy   = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
        }
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        int sx = (int)(x - camX) - size / 2;
        int sy = (int)(y - camY) - size / 2;

        switch (type) {
            case 0 -> drawSacola(g2, sx, sy);
            case 1 -> drawLata(g2, sx, sy);
            case 2 -> drawPneu(g2, sx, sy);
            case 3 -> drawNuvem(g2, sx, sy, animTimer);
            case 4 -> drawCigarro(g2, sx, sy, animTimer);
        }

        if (hp < maxHp) {
            int bw = size;
            int bx = sx;
            int by = sy - 7;
            g2.setColor(new Color(40, 0, 0, 180));
            g2.fillRoundRect(bx, by, bw, 4, 3, 3);
            g2.setColor(new Color(220, 40, 40));
            g2.fillRoundRect(bx, by, (int)(bw * ((float) hp / maxHp)), 4, 3, 3);
        }
    }

    // Sacola plástica
    private void drawSacola(Graphics2D g2, int sx, int sy) {
        // Corpo translúcido
        g2.setColor(new Color(200, 210, 255, 190));
        int[] xs = {sx + 3, sx + 1, sx + 7, sx + 15, sx + 21};
        int[] ys = {sy + 3, sy + 21, sy + 22, sy + 22, sy + 3};
        g2.fillPolygon(xs, ys, 5);

        // Vinco central
        g2.setColor(new Color(160, 170, 220, 160));
        g2.drawLine(sx + 11, sy + 8, sx + 11, sy + 21);

        // Contorno
        g2.setColor(new Color(140, 150, 210, 230));
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawPolygon(xs, ys, 5);
        g2.setStroke(new BasicStroke(1f));

        // Alças
        g2.setColor(new Color(140, 150, 210));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(sx + 5, sy - 1, 5, 8, 0, 180);
        g2.drawArc(sx + 11, sy - 1, 5, 8, 0, 180);
        g2.setStroke(new BasicStroke(1f));

        // Olhinhos malvados
        g2.setColor(new Color(255, 50, 50));
        g2.fillOval(sx + 6,  sy + 10, 4, 4);
        g2.fillOval(sx + 13, sy + 10, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 7,  sy + 11, 2, 2);
        g2.fillOval(sx + 14, sy + 11, 2, 2);
    }

    // Lata de alumínio
    private void drawLata(Graphics2D g2, int sx, int sy) {
        // Corpo
        g2.setColor(new Color(190, 50, 50));
        g2.fillRoundRect(sx + 3, sy + 3, 16, 18, 5, 5);

        // Faixa decorativa
        g2.setColor(new Color(230, 80, 80));
        g2.fillRect(sx + 3, sy + 5, 16, 5);

        // Reflexo lateral
        g2.setColor(new Color(255, 180, 180, 100));
        g2.fillRoundRect(sx + 5, sy + 4, 4, 14, 3, 3);

        // Tampa superior
        g2.setColor(new Color(160, 160, 170));
        g2.fillRoundRect(sx + 2, sy + 1, 18, 5, 4, 4);

        // Aba de abertura
        g2.setColor(new Color(200, 200, 210));
        g2.fillOval(sx + 9, sy + 2, 6, 3);

        // Olhinhos
        g2.setColor(new Color(255, 220, 0));
        g2.fillOval(sx + 5,  sy + 12, 4, 4);
        g2.fillOval(sx + 13, sy + 12, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 6,  sy + 13, 2, 2);
        g2.fillOval(sx + 14, sy + 13, 2, 2);

        // Contorno
        g2.setColor(new Color(120, 30, 30));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(sx + 3, sy + 3, 16, 18, 5, 5);
        g2.setStroke(new BasicStroke(1f));
    }

    // Pneu
    private void drawPneu(Graphics2D g2, int sx, int sy) {
        // Borracha externa
        g2.setColor(new Color(35, 35, 35));
        g2.fillOval(sx + 1, sy + 1, 26, 26);

        // Banda de rodagem (ranhuras)
        g2.setColor(new Color(55, 55, 55));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(sx + 1, sy + 1, 26, 26);
        g2.setStroke(new BasicStroke(1f));

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            int x1 = sx + 14 + (int)(Math.cos(angle) * 10);
            int y1 = sy + 14 + (int)(Math.sin(angle) * 10);
            int x2 = sx + 14 + (int)(Math.cos(angle) * 13);
            int y2 = sy + 14 + (int)(Math.sin(angle) * 13);
            g2.setColor(new Color(65, 65, 65));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x1, y1, x2, y2);
            g2.setStroke(new BasicStroke(1f));
        }

        // Aro interno
        g2.setColor(new Color(100, 100, 110));
        g2.fillOval(sx + 8, sy + 8, 12, 12);
        g2.setColor(new Color(130, 130, 140));
        g2.fillOval(sx + 10, sy + 10, 8, 8);

        // Olhinhos
        g2.setColor(new Color(255, 100, 0));
        g2.fillOval(sx + 8,  sy + 11, 4, 4);
        g2.fillOval(sx + 16, sy + 11, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 9,  sy + 12, 2, 2);
        g2.fillOval(sx + 17, sy + 12, 2, 2);
    }

    // Nuvem tóxica
    private void drawNuvem(Graphics2D g2, int sx, int sy, float t) {
        int w = (int)(Math.sin(t * 2.0f) * 3);

        // Sombra da nuvem
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(sx + 2, sy + 20, 26, 8);

        // Corpo da nuvem — camadas
        g2.setColor(new Color(60, 65, 60, 210));
        g2.fillOval(sx - 1,       sy + 10 - w, 16, 12);
        g2.fillOval(sx + 5,       sy + 5 - w,  18, 14);
        g2.fillOval(sx + 14,      sy + 9 - w,  14, 12);

        // Tom mais claro no topo
        g2.setColor(new Color(90, 100, 85, 180));
        g2.fillOval(sx + 6,  sy + 6 - w, 12, 9);
        g2.fillOval(sx + 14, sy + 10 - w, 8, 7);

        // Gotas tóxicas
        g2.setColor(new Color(100, 210, 60, 200));
        g2.fillOval(sx + 3,  sy + 20, 5, 6);
        g2.fillOval(sx + 12, sy + 21, 4, 5);
        g2.fillOval(sx + 20, sy + 19, 5, 6);

        // Olhinhos
        g2.setColor(new Color(200, 255, 0));
        g2.fillOval(sx + 8,  sy + 10 - w, 5, 5);
        g2.fillOval(sx + 16, sy + 11 - w, 5, 5);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 9,  sy + 11 - w, 2, 2);
        g2.fillOval(sx + 17, sy + 12 - w, 2, 2);
    }

    //Cigarro
    private void drawCigarro(Graphics2D g2, int sx, int sy, float t) {
        int flicker = (int)(Math.sin(t * 8f) * 1);

        // Fumacinha animada
        g2.setColor(new Color(190, 190, 190, 120));
        g2.fillOval(sx - 1, sy - 1 + flicker, 7, 7);
        g2.setColor(new Color(160, 160, 160, 80));
        g2.fillOval(sx,     sy - 6 + flicker, 6, 7);

        // Corpo branco
        g2.setColor(new Color(240, 235, 220));
        g2.fillRoundRect(sx + 1, sy + 7, 18, 7, 3, 3);

        // Linha de papel
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(sx + 1, sy + 10, sx + 17, sy + 10);

        // Filtro laranja
        g2.setColor(new Color(210, 140, 80));
        g2.fillRoundRect(sx + 17, sy + 7, 6, 7, 3, 3);
        // Textura do filtro
        g2.setColor(new Color(180, 110, 60));
        g2.drawLine(sx + 18, sy + 8, sx + 18, sy + 13);
        g2.drawLine(sx + 20, sy + 8, sx + 20, sy + 13);

        // Brasa
        g2.setColor(new Color(255, 120 + flicker * 20, 0));
        g2.fillOval(sx - 2, sy + 8, 5, 5);
        g2.setColor(new Color(255, 60, 0, 180));
        g2.fillOval(sx - 1, sy + 9, 3, 3);

        // Olhinhos
        g2.setColor(new Color(255, 50, 50));
        g2.fillOval(sx + 5,  sy + 8, 3, 3);
        g2.fillOval(sx + 11, sy + 8, 3, 3);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 6,  sy + 9, 2, 2);
        g2.fillOval(sx + 12, sy + 9, 2, 2);
    }

    public boolean isXpAwarded() { return xpAwarded; }
    public void markXpAwarded()  { xpAwarded = true; }
    public int getDamage()       { return damage; }
    public int getXpValue()      { return xpValue; }
    public int getType()         { return type; }
}