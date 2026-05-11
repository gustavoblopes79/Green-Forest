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
            case 0 -> 25;  // SACOLA    - fragil
            case 1 -> 50;  // LATA      - medio
            case 2 -> 90;  // PNEU      - tanque
            case 3 -> 40;  // NUVEM     - medio
            case 4 -> 20;  // CIGARRO   - fragil
            case 5 -> 55;  // GARRAFA   - medio-alto
            case 6 -> 100; // OLEO      - tanque
            case 7 -> 18;  // ISOPOR    - enxame fragil
            case 8 -> 45;  // AGROTOX   - medio
            case 9 -> 130; // ENTULHO   - tanque pesado
            default -> 30;
        };
    }

    private static int damageForType(int t) {
        return switch (t) {
            case 0 -> 7;   // SACOLA
            case 1 -> 12;  // LATA
            case 2 -> 20;  // PNEU
            case 3 -> 11;  // NUVEM
            case 4 -> 5;   // CIGARRO
            case 5 -> 14;  // GARRAFA
            case 6 -> 18;  // OLEO
            case 7 -> 4;   // ISOPOR
            case 8 -> 13;  // AGROTOX
            case 9 -> 25;  // ENTULHO
            default -> 8;
        };
    }

    private static float speedForType(int t) {
        return switch (t) {
            case 0 -> 1.5f;  // SACOLA    - rapido
            case 1 -> 1.1f;  // LATA      - medio
            case 2 -> 0.75f; // PNEU      - lento
            case 3 -> 1.3f;  // NUVEM     - medio
            case 4 -> 2.1f;  // CIGARRO   - muito rapido
            case 5 -> 1.0f;  // GARRAFA   - medio
            case 6 -> 0.65f; // OLEO      - muito lento
            case 7 -> 2.4f;  // ISOPOR    - muito rapido
            case 8 -> 1.2f;  // AGROTOX   - medio
            case 9 -> 0.55f; // ENTULHO   - lentissimo
            default -> 1.2f;
        };
    }

    private static int xpForType(int t) {
        return switch (t) {
            case 0 -> 8;   // SACOLA
            case 1 -> 15;  // LATA
            case 2 -> 25;  // PNEU
            case 3 -> 12;  // NUVEM
            case 4 -> 6;   // CIGARRO
            case 5 -> 18;  // GARRAFA
            case 6 -> 28;  // OLEO
            case 7 -> 5;   // ISOPOR
            case 8 -> 16;  // AGROTOX
            case 9 -> 35;  // ENTULHO
            default -> 10;
        };
    }

    private static int sizeForType(int t) {
        return switch (t) {
            case 2 -> 30;  // PNEU      - grande
            case 6 -> 28;  // OLEO      - grande
            case 9 -> 34;  // ENTULHO   - muito grande
            case 7 -> 18;  // ISOPOR    - pequeno
            case 4 -> 18;  // CIGARRO   - pequeno
            default -> 22;
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
            case 5 -> drawGarrafa(g2, sx, sy, animTimer);
            case 6 -> drawOleo(g2, sx, sy, animTimer);
            case 7 -> drawIsopor(g2, sx, sy, animTimer);
            case 8 -> drawAgrotoxicos(g2, sx, sy, animTimer);
            case 9 -> drawEntulho(g2, sx, sy, animTimer);
        }

        if (hp < maxHp) {
            int bw = size;
            int bx = sx;
            int by = sy - 7;
            g2.setColor(new Color(40, 0, 0, 180));
            g2.fillRoundRect(bx, by, bw, 4, 3, 3);
            g2.setColor(new Color(220, 40, 40));
            g2.fillRoundRect(bx, by,
                    (int)(bw * ((float) hp / maxHp)), 4, 3, 3);
        }
    }

    private void drawSacola(Graphics2D g2, int sx, int sy) {
        g2.setColor(new Color(200, 210, 255, 190));
        int[] xs = {sx + 3, sx + 1, sx + 7, sx + 15, sx + 21};
        int[] ys = {sy + 3, sy + 21, sy + 22, sy + 22, sy + 3};
        g2.fillPolygon(xs, ys, 5);

        g2.setColor(new Color(160, 170, 220, 160));
        g2.drawLine(sx + 11, sy + 8, sx + 11, sy + 21);

        g2.setColor(new Color(140, 150, 210, 230));
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawPolygon(xs, ys, 5);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(new Color(140, 150, 210));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(sx + 5,  sy - 1, 5, 8, 0, 180);
        g2.drawArc(sx + 11, sy - 1, 5, 8, 0, 180);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(new Color(255, 50, 50));
        g2.fillOval(sx + 6,  sy + 10, 4, 4);
        g2.fillOval(sx + 13, sy + 10, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 7,  sy + 11, 2, 2);
        g2.fillOval(sx + 14, sy + 11, 2, 2);
    }

    private void drawLata(Graphics2D g2, int sx, int sy) {
        g2.setColor(new Color(190, 50, 50));
        g2.fillRoundRect(sx + 3, sy + 3, 16, 18, 5, 5);

        g2.setColor(new Color(230, 80, 80));
        g2.fillRect(sx + 3, sy + 5, 16, 5);

        g2.setColor(new Color(255, 180, 180, 100));
        g2.fillRoundRect(sx + 5, sy + 4, 4, 14, 3, 3);

        g2.setColor(new Color(160, 160, 170));
        g2.fillRoundRect(sx + 2, sy + 1, 18, 5, 4, 4);

        g2.setColor(new Color(200, 200, 210));
        g2.fillOval(sx + 9, sy + 2, 6, 3);

        g2.setColor(new Color(255, 220, 0));
        g2.fillOval(sx + 5,  sy + 12, 4, 4);
        g2.fillOval(sx + 13, sy + 12, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 6,  sy + 13, 2, 2);
        g2.fillOval(sx + 14, sy + 13, 2, 2);

        g2.setColor(new Color(120, 30, 30));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(sx + 3, sy + 3, 16, 18, 5, 5);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawPneu(Graphics2D g2, int sx, int sy) {
        g2.setColor(new Color(35, 35, 35));
        g2.fillOval(sx + 1, sy + 1, 26, 26);

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

        g2.setColor(new Color(100, 100, 110));
        g2.fillOval(sx + 8,  sy + 8,  12, 12);
        g2.setColor(new Color(130, 130, 140));
        g2.fillOval(sx + 10, sy + 10, 8,  8);

        g2.setColor(new Color(255, 100, 0));
        g2.fillOval(sx + 8,  sy + 11, 4, 4);
        g2.fillOval(sx + 16, sy + 11, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 9,  sy + 12, 2, 2);
        g2.fillOval(sx + 17, sy + 12, 2, 2);
    }

    private void drawNuvem(Graphics2D g2, int sx, int sy, float t) {
        int w = (int)(Math.sin(t * 2.0f) * 3);

        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(sx + 2, sy + 20, 26, 8);

        g2.setColor(new Color(60, 65, 60, 210));
        g2.fillOval(sx - 1,  sy + 10 - w, 16, 12);
        g2.fillOval(sx + 5,  sy + 5  - w, 18, 14);
        g2.fillOval(sx + 14, sy + 9  - w, 14, 12);

        g2.setColor(new Color(90, 100, 85, 180));
        g2.fillOval(sx + 6,  sy + 6  - w, 12, 9);
        g2.fillOval(sx + 14, sy + 10 - w, 8,  7);

        g2.setColor(new Color(100, 210, 60, 200));
        g2.fillOval(sx + 3,  sy + 20, 5, 6);
        g2.fillOval(sx + 12, sy + 21, 4, 5);
        g2.fillOval(sx + 20, sy + 19, 5, 6);

        g2.setColor(new Color(200, 255, 0));
        g2.fillOval(sx + 8,  sy + 10 - w, 5, 5);
        g2.fillOval(sx + 16, sy + 11 - w, 5, 5);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 9,  sy + 11 - w, 2, 2);
        g2.fillOval(sx + 17, sy + 12 - w, 2, 2);
    }

    private void drawCigarro(Graphics2D g2, int sx, int sy, float t) {
        int flicker = (int)(Math.sin(t * 8f) * 1);

        g2.setColor(new Color(190, 190, 190, 120));
        g2.fillOval(sx - 1, sy - 1 + flicker, 7, 7);
        g2.setColor(new Color(160, 160, 160, 80));
        g2.fillOval(sx,     sy - 6 + flicker, 6, 7);

        g2.setColor(new Color(240, 235, 220));
        g2.fillRoundRect(sx + 1, sy + 7, 18, 7, 3, 3);

        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(sx + 1, sy + 10, sx + 17, sy + 10);

        g2.setColor(new Color(210, 140, 80));
        g2.fillRoundRect(sx + 17, sy + 7, 6, 7, 3, 3);
        g2.setColor(new Color(180, 110, 60));
        g2.drawLine(sx + 18, sy + 8, sx + 18, sy + 13);
        g2.drawLine(sx + 20, sy + 8, sx + 20, sy + 13);

        g2.setColor(new Color(255, 120 + flicker * 20, 0));
        g2.fillOval(sx - 2, sy + 8, 5, 5);
        g2.setColor(new Color(255, 60, 0, 180));
        g2.fillOval(sx - 1, sy + 9, 3, 3);

        g2.setColor(new Color(255, 50, 50));
        g2.fillOval(sx + 5,  sy + 8, 3, 3);
        g2.fillOval(sx + 11, sy + 8, 3, 3);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 6,  sy + 9, 2, 2);
        g2.fillOval(sx + 12, sy + 9, 2, 2);
    }

    private void drawGarrafa(Graphics2D g2, int sx, int sy, float t) {
        // Sombra
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(sx + 4, sy + 20, 14, 5);

        // Corpo da garrafa
        g2.setColor(new Color(80, 160, 80, 200));
        int[] bx = {sx + 6, sx + 4, sx + 4, sx + 18, sx + 18, sx + 16};
        int[] by = {sy + 6, sy + 11, sy + 22, sy + 22, sy + 11, sy + 6};
        g2.fillPolygon(bx, by, 6);

        // Reflexo no corpo
        g2.setColor(new Color(160, 230, 160, 100));
        g2.fillRoundRect(sx + 6, sy + 12, 3, 8, 2, 2);

        // Gargalo
        g2.setColor(new Color(60, 130, 60, 220));
        g2.fillRoundRect(sx + 8, sy + 2, 6, 7, 3, 3);

        // Tampa enferrujada
        g2.setColor(new Color(150, 90, 40));
        g2.fillOval(sx + 7, sy + 1, 8, 4);
        g2.setColor(new Color(120, 70, 30));
        g2.drawLine(sx + 8, sy + 3, sx + 14, sy + 3);

        // Contorno
        g2.setColor(new Color(40, 100, 40, 200));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawPolygon(bx, by, 6);
        g2.setStroke(new BasicStroke(1f));

        // Rachadura (vidro quebrado)
        g2.setColor(new Color(200, 255, 200, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(sx + 10, sy + 13, sx + 14, sy + 17);
        g2.drawLine(sx + 14, sy + 17, sx + 12, sy + 20);
        g2.setStroke(new BasicStroke(1f));

        // Olhinhos
        g2.setColor(new Color(255, 200, 0));
        g2.fillOval(sx + 6,  sy + 14, 3, 3);
        g2.fillOval(sx + 13, sy + 14, 3, 3);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 7,  sy + 15, 2, 2);
        g2.fillOval(sx + 14, sy + 15, 2, 2);
    }

    private void drawOleo(Graphics2D g2, int sx, int sy, float t) {
        float pulse = (float)(Math.sin(t * 1.5f) * 2);

        // Poça de oleo embaixo (mancha)
        g2.setColor(new Color(20, 15, 30, 180));
        g2.fillOval(sx - 2, sy + 22, 32 + (int)pulse, 8);

        // Reflexo iridescente da poca
        g2.setColor(new Color(100, 0, 180, 80));
        g2.fillOval(sx,     sy + 23, 16, 4);
        g2.setColor(new Color(0, 180, 100, 60));
        g2.fillOval(sx + 8, sy + 24, 12, 3);

        // Corpo do tambor de oleo
        g2.setColor(new Color(40, 35, 40));
        g2.fillRoundRect(sx + 2, sy + 4, 24, 20, 6, 6);

        // Faixas do tambor
        g2.setColor(new Color(60, 55, 60));
        g2.fillRect(sx + 2, sy + 10, 24, 3);
        g2.fillRect(sx + 2, sy + 17, 24, 2);

        // Etiqueta de perigo
        g2.setColor(new Color(220, 180, 0));
        g2.fillRect(sx + 6, sy + 6, 16, 10);
        g2.setColor(new Color(0, 0, 0));
        // Simbolo de caveira simplificado
        g2.fillOval(sx + 10, sy + 7, 8, 6);
        g2.fillRect(sx + 10, sy + 11, 3, 3);
        g2.fillRect(sx + 15, sy + 11, 3, 3);
        g2.fillRect(sx + 12, sy + 13, 4, 1);

        // Tampa com vazamento
        g2.setColor(new Color(70, 65, 70));
        g2.fillRoundRect(sx + 4, sy + 2, 20, 4, 3, 3);

        // Oleo escorrendo
        g2.setColor(new Color(15, 10, 25, 220));
        g2.fillRoundRect(sx + 9,  sy + 6, 3, 18, 2, 2);
        g2.fillRoundRect(sx + 17, sy + 6, 2, 14, 2, 2);

        // Brilho iridescente no oleo escorrendo
        g2.setColor(new Color(80, 0, 160, 120));
        g2.fillRect(sx + 10, sy + 10, 1, 10);

        // Olhinhos malvados
        g2.setColor(new Color(180, 0, 255));
        g2.fillOval(sx + 4,  sy + 14, 4, 4);
        g2.fillOval(sx + 20, sy + 14, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 5,  sy + 15, 2, 2);
        g2.fillOval(sx + 21, sy + 15, 2, 2);
    }

    private void drawIsopor(Graphics2D g2, int sx, int sy, float t) {
        float bounce = (float)(Math.abs(Math.sin(t * 5f)) * 3);

        // Corpo principal isopor
        g2.setColor(new Color(245, 245, 248));
        g2.fillRoundRect(sx + 1, sy + 2 - (int)bounce, 16, 14, 4, 4);

        // Textura granulada do isopor
        g2.setColor(new Color(220, 220, 225));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                g2.fillOval(
                        sx + 3 + col * 3,
                        sy + 4 - (int)bounce + row * 3,
                        2, 2
                );
            }
        }

        // Contorno
        g2.setColor(new Color(180, 180, 185));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(sx + 1, sy + 2 - (int)bounce, 16, 14, 4, 4);

        // Fragmentos voando (bolinhas ao redor)
        g2.setColor(new Color(240, 240, 245, 200));
        g2.fillOval(sx - 2, sy + 5, 4, 4);
        g2.fillOval(sx + 16, sy + 3, 3, 3);
        g2.fillOval(sx + 14, sy + 15, 3, 3);

        // Olhinhos frenéticos
        g2.setColor(new Color(255, 80, 0));
        g2.fillOval(sx + 4,  sy + 6  - (int)bounce, 3, 3);
        g2.fillOval(sx + 10, sy + 6  - (int)bounce, 3, 3);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 5,  sy + 7  - (int)bounce, 2, 2);
        g2.fillOval(sx + 11, sy + 7  - (int)bounce, 2, 2);

        // Boca nervosa
        g2.setColor(new Color(255, 120, 0));
        g2.drawArc(sx + 5, sy + 10 - (int)bounce, 8, 4, 0, -180);
    }

    private void drawAgrotoxicos(Graphics2D g2, int sx, int sy, float t) {
        float drip = (float)(Math.sin(t * 3f) * 2);

        // Sombra
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(sx + 3, sy + 21, 16, 5);

        // Corpo do frasco
        g2.setColor(new Color(180, 220, 60));
        g2.fillRoundRect(sx + 4, sy + 8, 14, 14, 4, 4);

        // Reflexo
        g2.setColor(new Color(230, 255, 130, 120));
        g2.fillRoundRect(sx + 6, sy + 10, 4, 8, 2, 2);

        // Pescoço do frasco
        g2.setColor(new Color(160, 200, 50));
        g2.fillRoundRect(sx + 7, sy + 4, 8, 6, 3, 3);

        // Tampa borrifadora
        g2.setColor(new Color(220, 80, 30));
        g2.fillRoundRect(sx + 6, sy + 1, 10, 5, 3, 3);

        // Gotícula tóxica caindo
        g2.setColor(new Color(100, 200, 0, 200));
        g2.fillOval(sx + 10, sy + 22 + (int)drip, 4, 5);

        // Simbolo de perigo (X) no frasco
        g2.setColor(new Color(200, 0, 0));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(sx + 7,  sy + 11, sx + 15, sy + 19);
        g2.drawLine(sx + 15, sy + 11, sx + 7,  sy + 19);
        g2.setStroke(new BasicStroke(1f));

        // Olhinhos
        g2.setColor(new Color(0, 255, 80));
        g2.fillOval(sx + 5,  sy + 12, 4, 4);
        g2.fillOval(sx + 13, sy + 12, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 6,  sy + 13, 2, 2);
        g2.fillOval(sx + 14, sy + 13, 2, 2);
    }

    private void drawEntulho(Graphics2D g2, int sx, int sy, float t) {
        // Base do monte de entulho
        g2.setColor(new Color(110, 90, 70));
        int[] ex = {sx,      sx + 5,  sx + 10, sx + 20, sx + 28, sx + 30, sx + 22};
        int[] ey = {sy + 30, sy + 20, sy + 15, sy + 18, sy + 16, sy + 30, sy + 30};
        g2.fillPolygon(ex, ey, 7);

        // Camada mais clara no topo
        g2.setColor(new Color(140, 115, 90));
        int[] tx = {sx + 5,  sx + 10, sx + 20, sx + 28, sx + 22};
        int[] ty = {sy + 20, sy + 15, sy + 18, sy + 16, sy + 22};
        g2.fillPolygon(tx, ty, 5);

        // Tijolos e pedacos de concreto
        g2.setColor(new Color(180, 80, 60));
        g2.fillRect(sx + 6,  sy + 20, 8, 5);
        g2.fillRect(sx + 18, sy + 18, 7, 4);
        g2.setColor(new Color(160, 155, 150));
        g2.fillRect(sx + 12, sy + 17, 9, 6);
        g2.fillRect(sx + 3,  sy + 23, 6, 5);

        // Rachaduras no concreto
        g2.setColor(new Color(90, 85, 80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(sx + 13, sy + 18, sx + 16, sy + 22);
        g2.drawLine(sx + 19, sy + 19, sx + 23, sy + 21);
        g2.setStroke(new BasicStroke(1f));

        // Haste de ferro saindo
        g2.setColor(new Color(100, 90, 85));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(sx + 22, sy + 14, sx + 26, sy + 8);
        g2.drawLine(sx + 8,  sy + 18, sx + 5,  sy + 12);
        g2.setStroke(new BasicStroke(1f));
        // Ferrugem nas hastes
        g2.setColor(new Color(160, 90, 40));
        g2.fillRect(sx + 24, sy + 9,  3, 2);
        g2.fillRect(sx + 5,  sy + 12, 3, 2);

        // Poeira levantando
        g2.setColor(new Color(180, 160, 130, 100));
        g2.fillOval(sx - 2, sy + 24, 8, 6);
        g2.fillOval(sx + 24, sy + 22, 7, 5);

        // Olhinhos profundos
        g2.setColor(new Color(255, 140, 0));
        g2.fillOval(sx + 9,  sy + 20, 5, 5);
        g2.fillOval(sx + 18, sy + 20, 5, 5);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 10, sy + 21, 3, 3);
        g2.fillOval(sx + 19, sy + 21, 3, 3);
        // Brilho nos olhos
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 10, sy + 21, 1, 1);
        g2.fillOval(sx + 19, sy + 21, 1, 1);
    }

    public boolean isXpAwarded()  { return xpAwarded; }
    public void markXpAwarded()   { xpAwarded = true; }
    public int getDamage()        { return damage; }
    public int getXpValue()       { return xpValue; }
    public int getType()          { return type; }
}