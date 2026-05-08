package com.greenforest.powerup;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum PowerUp {

    MAIS_DANO(
            "Mais Dano",
            "Projéteis causam +25% de dano",
            new Color(255, 80, 80)
    ),
    MAIS_VELOCIDADE(
            "Mais Velocidade",
            "Velocidade de movimento +20%",
            new Color(80, 200, 255)
    ),
    CURA(
            "Cura",
            "Recupera 40 HP imediatamente",
            new Color(80, 255, 120)
    ),
    MAIS_PROJETEIS(
            "Mais Projéteis",
            "Dispara 2 projéteis extras (spread)",
            new Color(255, 200, 50)
    ),
    VAMPIRISMO(
            "Vampirismo",
            "Recupera 3 HP ao matar inimigo",
            new Color(180, 50, 255)
    ),
    ESCUDO(
            "Escudo",
            "Absorve até 30 de dano",
            new Color(100, 180, 255)
    ),
    CADENCIA(
            "Cadência",
            "Atira 25% mais rápido",
            new Color(255, 150, 50)
    );

    public final String name;
    public final String description;
    public final Color color;

    PowerUp(String name, String description, Color color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }

    public static List<PowerUp> getRandomThree(Random rng) {
        List<PowerUp> all = new ArrayList<>(List.of(values()));
        Collections.shuffle(all, rng);
        return all.subList(0, 3);
    }


    public void drawIcon(Graphics2D g, int cx, int cy, int size) {
        int h = size / 2;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        switch (this) {
            case MAIS_DANO -> drawIconDano(g2, cx, cy, h);
            case MAIS_VELOCIDADE -> drawIconVelocidade(g2, cx, cy, h);
            case CURA -> drawIconCura(g2, cx, cy, h);
            case MAIS_PROJETEIS -> drawIconProjeteis(g2, cx, cy, h);
            case VAMPIRISMO -> drawIconVampirismo(g2, cx, cy, h);
            case ESCUDO -> drawIconEscudo(g2, cx, cy, h);
            case CADENCIA -> drawIconCadencia(g2, cx, cy, h);
        }

        g2.dispose();
    }

    // ícones individuais

    private void drawIconDano(Graphics2D g, int cx, int cy, int h) {
        // Espada diagonal
        g.setColor(new Color(220, 220, 220));
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        int d = (int) (h * 0.7);
        g.drawLine(cx - d, cy + d, cx + d, cy - d);
        // Guarda
        g.setStroke(new BasicStroke(2));
        g.drawLine(cx - d / 3, cy + d / 3 - d / 2,
                cx + d / 3, cy - d / 3 + d / 2);
        // Chamas
        g.setColor(new Color(255, 120, 30, 200));
        int[] fx = {cx + d - 4, cx + d + 4, cx + d};
        int[] fy = {cy - d - 4, cy - d - 4, cy - d - 12};
        g.fillPolygon(fx, fy, 3);
        g.setColor(new Color(255, 220, 50, 180));
        int[] fx2 = {cx + d - 2, cx + d + 2, cx + d};
        int[] fy2 = {cy - d - 4, cy - d - 4, cy - d - 10};
        g.fillPolygon(fx2, fy2, 3);
    }

    private void drawIconVelocidade(Graphics2D g, int cx, int cy, int h) {
        // Três setas apontando para a direita
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        int[] offsets = {-h / 3, 0, h / 3};
        for (int i = 0; i < 3; i++) {
            float alpha = 0.5f + 0.25f * i;
            g.setColor(new Color(80, 200, 255, (int) (alpha * 255)));
            int y = cy + offsets[i];
            int len = h / 2 + i * 4;
            g.drawLine(cx - len, y, cx + len, y);
            // ponta
            g.drawLine(cx + len, y, cx + len - h / 4, y - h / 4);
            g.drawLine(cx + len, y, cx + len - h / 4, y + h / 4);
        }
    }

    private void drawIconCura(Graphics2D g, int cx, int cy, int h) {
        // Cruz médica
        int w = h / 3;
        int l = (int) (h * 0.7);
        g.setColor(new Color(80, 255, 120));
        g.fillRect(cx - w, cy - l, w * 2, l * 2);
        g.fillRect(cx - l, cy - w, l * 2, w * 2);
        // brilho
        g.setColor(new Color(200, 255, 210, 160));
        g.fillRect(cx - w + 2, cy - l + 2, w - 2, l * 2 - 4);
    }

    private void drawIconProjeteis(Graphics2D g, int cx, int cy, int h) {
        // 3 sementes em spread
        g.setColor(new Color(100, 220, 60));
        int r = h / 5;
        // central
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        // esquerda
        int ox = (int) (h * 0.45);
        int oy = (int) (h * 0.3);
        g.fillOval(cx - ox - r, cy + oy - r, r * 2, r * 2);
        // direita
        g.fillOval(cx + ox - r, cy + oy - r, r * 2, r * 2);
        // linhas de trajetória
        g.setColor(new Color(180, 255, 100, 160));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.drawLine(cx, cy, cx, cy - h);
        g.drawLine(cx - ox, cy + oy, cx - ox - h / 3, cy - h / 2);
        g.drawLine(cx + ox, cy + oy, cx + ox + h / 3, cy - h / 2);
    }

    private void drawIconVampirismo(Graphics2D g, int cx, int cy, int h) {
        // Gota com sombra roxa
        g.setColor(new Color(100, 0, 180, 200));
        int[] xp = {cx, cx + h / 2, cx, cx - h / 2};
        int[] yp = {cy - h, cy, cy + h / 2, cy};
        g.fillPolygon(xp, yp, 4);
        g.setColor(new Color(200, 80, 255));
        g.setStroke(new BasicStroke(2));
        g.drawPolygon(xp, yp, 4);
        // brilho
        g.setColor(new Color(255, 200, 255, 120));
        g.fillOval(cx - h / 6, cy - h / 2, h / 5, h / 4);
    }

    private void drawIconEscudo(Graphics2D g, int cx, int cy, int h) {
        // Escudo hexagonal simplificado
        int[] xs = new int[6];
        int[] ys = new int[6];
        for (int i = 0; i < 6; i++) {
            double ang = Math.toRadians(i * 60 - 90);
            xs[i] = cx + (int) (h * 0.8 * Math.cos(ang));
            ys[i] = cy + (int) (h * 0.8 * Math.sin(ang));
        }
        g.setColor(new Color(30, 80, 180, 200));
        g.fillPolygon(xs, ys, 6);
        g.setColor(new Color(100, 180, 255));
        g.setStroke(new BasicStroke(2.5f));
        g.drawPolygon(xs, ys, 6);
        // símbolo
        g.setColor(new Color(200, 230, 255));
        g.setFont(new Font("Arial", Font.BOLD, h));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("+",
                cx - fm.stringWidth("+") / 2,
                cy + fm.getAscent() / 2 - 1);
    }

    private void drawIconCadencia(Graphics2D g, int cx, int cy, int h) {
        // Relógio com ponteiros rápidos
        g.setColor(new Color(255, 150, 50));
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(cx - h, cy - h, h * 2, h * 2);
        // ponteiro hora
        g.drawLine(cx, cy, cx, cy - (int) (h * 0.6));
        // ponteiro minuto (adiantado)
        g.drawLine(cx, cy,
                cx + (int) (h * 0.7 * Math.cos(Math.toRadians(-60))),
                cy + (int) (h * 0.7 * Math.sin(Math.toRadians(-60))));
        // linhas de velocidade
        g.setColor(new Color(255, 200, 100, 160));
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 3; i++) {
            int lx = cx + h + 3 + i * 4;
            g.drawLine(lx, cy - h / 2, lx, cy + h / 2);
        }
    }
}