package com.greenforest.entity;

import com.greenforest.DifficultySettings;

import java.awt.*;

public class Boss extends Enemy {

    public enum BossType {
        E_WASTE,
        FACTORY,
        GARBAGE_TRUCK
    }

    private final BossType bossType;
    private final String   lesson;

    private float   specialTimer   = 0f;
    private final float SPECIAL_CD = 4.0f;
    private boolean specialActive  = false;
    private float   specialRadius  = 0f;
    private final float SPECIAL_MAX_R = 120f;

    private float animTimer = 0f;

    public Boss(float x, float y, BossType type) {
        super(x, y, 0);
        this.bossType = type;

        float hpBase = switch (type) {
            case E_WASTE       -> 800f;
            case FACTORY       -> 1000f;
            case GARBAGE_TRUCK -> 700f;
        };
        this.maxHp  = (int)(hpBase * DifficultySettings.bossHpMult());
        this.hp     = this.maxHp;
        this.speed  = 0.7f * DifficultySettings.enemySpeedMult();
        this.damage = (int)(30  * DifficultySettings.bossDamageMult());
        this.xpValue = 150;
        this.size    = 68;

        this.lesson = switch (type) {
            case E_WASTE ->
                    "O lixo eletronico e o residuo que mais cresce no mundo.\n" +
                            "Celulares, computadores e pilhas contem metais toxicos\n" +
                            "como chumbo e mercurio. Descarte nos pontos especializados!";
            case FACTORY ->
                    "Fabricas sem controle lancam gases como CO2 e SO2\n" +
                            "na atmosfera, causando chuva acida e aquecimento global.\n" +
                            "Cobre setores sustentaveis e exija politicas de emissao zero!";
            case GARBAGE_TRUCK ->
                    "O Brasil gera mais de 80 milhoes de toneladas de\n" +
                            "residuos por ano. Apenas 4% e reciclado.\n" +
                            "Separe o lixo e apoie a coleta seletiva no seu bairro!";
        };
    }

    @Override
    public void update(float dt, float playerX, float playerY) {
        super.update(dt, playerX, playerY);
        animTimer += dt;

        specialTimer += dt;
        if (specialTimer >= SPECIAL_CD) {
            specialTimer = 0f;
            specialActive = true;
            specialRadius = 0f;
        }
        if (specialActive) {
            specialRadius += 220f * dt;
            if (specialRadius >= SPECIAL_MAX_R) {
                specialActive = false;
                specialRadius = 0f;
            }
        }
    }

    public boolean hitsWithSpecial(float px, float py) {
        if (!specialActive) return false;
        float dx   = px - x;
        float dy   = py - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist >= specialRadius - 10f && dist <= specialRadius + 10f;
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        int sx = (int)(x - camX) - size / 2;
        int sy = (int)(y - camY) - size / 2;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(sx + 4, sy + size - 6, size - 8, 10);

        switch (bossType) {
            case E_WASTE       -> drawEWaste(g2, sx, sy);
            case FACTORY       -> drawFactory(g2, sx, sy);
            case GARBAGE_TRUCK -> drawGarbageTruck(g2, sx, sy);
        }

        // Onda do ataque especial
        if (specialActive) {
            int cx = (int)(x - camX);
            int cy = (int)(y - camY);
            int r  = (int) specialRadius;
            g2.setColor(new Color(255, 100, 0, 100));
            g2.setStroke(new BasicStroke(10f));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(new Color(255, 200, 0, 60));
            g2.setStroke(new BasicStroke(5f));
            g2.drawOval(cx - r + 3, cy - r + 3, r * 2 - 6, r * 2 - 6);
            g2.setStroke(new BasicStroke(1f));
        }

        drawBossHealthBar(g2, (int)(x - camX), (int)(y - camY));
    }

    private void drawBossHealthBar(Graphics2D g2, int cx, int cy) {
        int barW = 130;
        int barH = 14;
        int bx   = cx - barW / 2;
        int by   = cy - size / 2 - 24;

        // Fundo
        g2.setColor(new Color(20, 0, 0, 200));
        g2.fillRoundRect(bx - 2, by - 2, barW + 4, barH + 4, 6, 6);

        // Barra vazia
        g2.setColor(new Color(60, 10, 10));
        g2.fillRoundRect(bx, by, barW, barH, 4, 4);

        // Barra de HP com gradiente simulado
        float ratio = (float) hp / maxHp;
        Color barColor = ratio > 0.5f
                ? new Color(200, 40, 40)
                : new Color(255, 80, 0);
        g2.setColor(barColor);
        g2.fillRoundRect(bx, by, (int)(barW * ratio), barH, 4, 4);

        // Brilho
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRoundRect(bx, by, (int)(barW * ratio), barH / 2, 4, 4);

        // Label
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(Color.WHITE);
        String label = bossTypeLabel() + "  " + hp + "/" + maxHp;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, cx - fm.stringWidth(label) / 2, by + barH - 1);
    }

    private String bossTypeLabel() {
        return switch (bossType) {
            case E_WASTE       -> "LIXO ELETRONICO";
            case FACTORY       -> "FABRICA POLUENTE";
            case GARBAGE_TRUCK -> "CAMINHAO DE LIXO";
        };
    }

    // Lixo eletronico
    private void drawEWaste(Graphics2D g2, int sx, int sy) {
        // Base de equipamentos empilhados
        g2.setColor(new Color(65, 65, 75));
        g2.fillRoundRect(sx + 6, sy + 30, 56, 32, 8, 8);
        g2.setColor(new Color(80, 80, 90));
        g2.fillRoundRect(sx + 10, sy + 24, 48, 14, 6, 6);

        // Monitor
        g2.setColor(new Color(30, 30, 40));
        g2.fillRoundRect(sx + 12, sy + 6, 44, 30, 5, 5);

        // Tela com brilho toxico
        g2.setColor(new Color(0, 30, 50));
        g2.fillRect(sx + 15, sy + 9, 38, 24);
        g2.setColor(new Color(0, 180, 255, 130));
        g2.fillRect(sx + 15, sy + 9, 38, 24);

        // Rachaduras na tela
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(sx + 22, sy + 9,  sx + 32, sy + 33);
        g2.drawLine(sx + 32, sy + 11, sx + 45, sy + 28);
        g2.drawLine(sx + 22, sy + 20, sx + 38, sy + 24);
        g2.setStroke(new BasicStroke(1f));

        // Suporte do monitor
        g2.setColor(new Color(55, 55, 65));
        g2.fillRect(sx + 28, sy + 36, 12, 6);

        // Cabos coloridos
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(220, 30, 30));
        g2.drawLine(sx + 16, sy + 60, sx + 10, sy + 68);
        g2.setColor(new Color(30, 30, 220));
        g2.drawLine(sx + 26, sy + 60, sx + 22, sy + 68);
        g2.setColor(new Color(30, 200, 30));
        g2.drawLine(sx + 36, sy + 60, sx + 38, sy + 68);
        g2.setColor(new Color(220, 200, 0));
        g2.drawLine(sx + 46, sy + 60, sx + 50, sy + 68);
        g2.setStroke(new BasicStroke(1f));

        // Olhos brilhantes
        g2.setColor(new Color(0, 255, 80));
        g2.fillOval(sx + 19, sy + 13, 10, 10);
        g2.fillOval(sx + 38, sy + 13, 10, 10);
        g2.setColor(new Color(0, 100, 30));
        g2.fillOval(sx + 21, sy + 15, 6, 6);
        g2.fillOval(sx + 40, sy + 15, 6, 6);
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 23, sy + 16, 2, 2);
        g2.fillOval(sx + 42, sy + 16, 2, 2);

        // Aura vermelha
        g2.setColor(new Color(255, 0, 0, 80));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(sx + 6, sy + 30, 56, 32, 8, 8);
        g2.drawRoundRect(sx + 12, sy + 6, 44, 30, 5, 5);
        g2.setStroke(new BasicStroke(1f));
    }

    // Fabrica
    private void drawFactory(Graphics2D g2, int sx, int sy) {
        // Corpo principal
        g2.setColor(new Color(95, 85, 75));
        g2.fillRect(sx + 8, sy + 34, 52, 30);

        // Telhado dentado
        g2.setColor(new Color(75, 68, 60));
        int[] rx = {sx+8, sx+16, sx+24, sx+32, sx+40, sx+48, sx+56, sx+60, sx+8};
        int[] ry = {sy+34, sy+26, sy+34, sy+26, sy+34, sy+26, sy+34, sy+34, sy+34};
        g2.fillPolygon(rx, ry, 9);

        // Chamine esquerda
        g2.setColor(new Color(68, 60, 52));
        g2.fillRect(sx + 12, sy + 10, 14, 26);
        g2.setColor(new Color(55, 48, 42));
        g2.fillRect(sx + 10, sy + 8, 18, 6);

        // Chamine direita
        g2.fillRect(sx + 40, sy + 16, 12, 20);
        g2.setColor(new Color(55, 48, 42));
        g2.fillRect(sx + 38, sy + 14, 16, 6);

        // Fumaca esquerda
        float ft = animTimer;
        for (int i = 0; i < 4; i++) {
            int wobble = (int)(Math.sin(ft * 1.5f + i) * 4);
            int alpha  = 180 - i * 35;
            g2.setColor(new Color(50, 50, 50, alpha));
            g2.fillOval(sx + 12 + wobble, sy - 2 - i * 9, 16 + i * 2, 12 + i);
        }
        // Fumaca direita (alaranjada — mais toxica)
        for (int i = 0; i < 3; i++) {
            int wobble = (int)(Math.sin(ft * 2f + i + 1) * 3);
            int alpha  = 160 - i * 40;
            g2.setColor(new Color(80, 55, 40, alpha));
            g2.fillOval(sx + 40 + wobble, sy + 4 - i * 8, 14 + i, 10 + i);
        }

        // Janelas iluminadas
        g2.setColor(new Color(255, 200, 60, 200));
        g2.fillRect(sx + 12, sy + 38, 12, 10);
        g2.fillRect(sx + 30, sy + 38, 12, 10);
        g2.setColor(new Color(255, 255, 200, 60));
        g2.fillRect(sx + 13, sy + 39, 5, 4);
        g2.fillRect(sx + 31, sy + 39, 5, 4);

        // Porta
        g2.setColor(new Color(50, 36, 20));
        g2.fillRoundRect(sx + 26, sy + 50, 14, 14, 4, 4);
        g2.setColor(new Color(180, 140, 80));
        g2.fillOval(sx + 31, sy + 57, 3, 3);

        // Olhos nas janelas
        g2.setColor(new Color(255, 80, 0));
        g2.fillOval(sx + 14, sy + 40, 7, 7);
        g2.fillOval(sx + 32, sy + 40, 7, 7);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 16, sy + 42, 3, 3);
        g2.fillOval(sx + 34, sy + 42, 3, 3);

        // Aura laranja
        g2.setColor(new Color(255, 80, 0, 70));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(sx + 8, sy + 34, 52, 30);
        g2.setStroke(new BasicStroke(1f));
    }

    // Caminhão de lixo
    private void drawGarbageTruck(Graphics2D g2, int sx, int sy) {
        // Cacamba
        g2.setColor(new Color(70, 72, 70));
        g2.fillRect(sx + 2, sy + 26, 40, 28);
        g2.setColor(new Color(85, 88, 85));
        g2.fillRect(sx + 2, sy + 24, 40, 6);

        // Lixo transbordando
        int[] lxs = {sx+4, sx+8,  sx+14, sx+20, sx+27, sx+35, sx+40};
        int[] lys = {sy+26, sy+18, sy+22, sy+16, sy+20, sy+17, sy+26};
        g2.setColor(new Color(100, 80, 40));
        g2.fillPolygon(lxs, lys, 7);
        // Detalhes do lixo
        g2.setColor(new Color(160, 50, 50));
        g2.fillOval(sx + 6,  sy + 17, 9, 9);
        g2.setColor(new Color(80, 140, 50));
        g2.fillOval(sx + 18, sy + 15, 8, 8);
        g2.setColor(new Color(200, 180, 40));
        g2.fillOval(sx + 30, sy + 16, 8, 8);

        // Cabine
        g2.setColor(new Color(50, 120, 55));
        g2.fillRoundRect(sx + 40, sy + 18, 26, 36, 8, 8);

        // Para-brisa
        g2.setColor(new Color(140, 210, 255, 160));
        g2.fillRoundRect(sx + 43, sy + 21, 18, 14, 5, 5);
        // Reflexo
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillRoundRect(sx + 44, sy + 22, 7, 5, 3, 3);

        // Grade frontal
        g2.setColor(new Color(40, 90, 45));
        g2.fillRect(sx + 42, sy + 36, 22, 8);
        g2.setColor(new Color(30, 70, 35));
        for (int i = 0; i < 4; i++) {
            g2.drawLine(sx + 45 + i * 5, sy + 36, sx + 45 + i * 5, sy + 44);
        }

        // Rodas
        g2.setColor(new Color(25, 25, 25));
        g2.fillOval(sx + 6,  sy + 48, 18, 18);
        g2.fillOval(sx + 44, sy + 48, 18, 18);
        g2.setColor(new Color(55, 55, 55));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(sx + 6,  sy + 48, 18, 18);
        g2.drawOval(sx + 44, sy + 48, 18, 18);
        g2.setStroke(new BasicStroke(1f));
        // Aros
        g2.setColor(new Color(110, 110, 120));
        g2.fillOval(sx + 10, sy + 52, 10, 10);
        g2.fillOval(sx + 48, sy + 52, 10, 10);
        g2.setColor(new Color(140, 140, 150));
        g2.fillOval(sx + 12, sy + 54, 6, 6);
        g2.fillOval(sx + 50, sy + 54, 6, 6);

        // Olho malvado na cabine
        g2.setColor(new Color(255, 30, 30));
        g2.fillOval(sx + 46, sy + 24, 8, 8);
        g2.setColor(new Color(100, 0, 0));
        g2.fillOval(sx + 48, sy + 26, 4, 4);
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 49, sy + 26, 2, 2);

        // Aura roxa
        g2.setColor(new Color(180, 0, 220, 70));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(sx + 40, sy + 18, 26, 36, 8, 8);
        g2.drawRect(sx + 2, sy + 26, 40, 28);
        g2.setStroke(new BasicStroke(1f));
    }

    public String   getLesson()          { return lesson; }
    public BossType getBossType()        { return bossType; }
    public boolean  isSpecialActive()    { return specialActive; }
    public int      getSpecialDamage()   {
        return (int)(15 * DifficultySettings.bossDamageMult());
    }
}