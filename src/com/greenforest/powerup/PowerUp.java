package com.greenforest.powerup;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum PowerUp {

    FERTILIZANTE(
            "Fertilizante",
            "Nutrientes que aumentam dano em 25%",
            new Color(139, 90, 43)
    ),
    COMPOSTAGEM(
            "Compostagem",
            "Velocidade +20% com nutrientes naturais",
            new Color(107, 142, 35)
    ),
    FOTOSINTESE(
            "Fotossíntese",
            "Recupera 40 HP com energia solar",
            new Color(50, 205, 50)
    ),
    SEMENTES(
            "Sementes",
            "Dispara 2 projéteis extras (polinização)",
            new Color(34, 139, 34)
    ),
    BIOLOGICO(
            "Controle Biológico",
            "Recupera 3 HP ao eliminar pragas",
            new Color(65, 105, 225)
    ),
    BIOFILTRO(
            "Biofiltro",
            "Absorve até 30 de dano (raízes protetoras)",
            new Color(70, 130, 180)
    ),
    ENERGIA_SOLAR(
            "Energia Solar",
            "Atira 25% mais rápido com luz renovável",
            new Color(255, 215, 0)
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
            case FERTILIZANTE -> drawIconFertilizante(g2, cx, cy, h);
            case COMPOSTAGEM -> drawIconCompostagem(g2, cx, cy, h);
            case FOTOSINTESE -> drawIconFotossintese(g2, cx, cy, h);
            case SEMENTES -> drawIconSementes(g2, cx, cy, h);
            case BIOLOGICO -> drawIconBiologico(g2, cx, cy, h);
            case BIOFILTRO -> drawIconBiofiltro(g2, cx, cy, h);
            case ENERGIA_SOLAR -> drawIconEnergiaSolar(g2, cx, cy, h);
        }

        g2.dispose();
    }

    private void drawIconFertilizante(Graphics2D g, int cx, int cy, int h) {
        g.setColor(new Color(139, 90, 43));
        g.fillRect(cx - h/2, cy - h, h, h*2);
        g.setColor(new Color(160, 110, 60));
        g.fillOval(cx - h/2, cy - h - h/4, h, h/2);
        g.setColor(new Color(100, 60, 30));
        g.drawLine(cx, cy - h, cx, cy + h);
    }

    private void drawIconCompostagem(Graphics2D g, int cx, int cy, int h) {
        g.setColor(new Color(107, 142, 35));
        g.fillOval(cx - h, cy - h/2, h*2, h);
        g.setColor(new Color(80, 110, 25));
        g.fillOval(cx - h + 4, cy - h/2 + 4, h*2 - 8, h - 8);
    }

    private void drawIconFotossintese(Graphics2D g, int cx, int cy, int h) {
        g.setColor(new Color(50, 205, 50));
        g.fillOval(cx - h, cy - h, h*2, h*2);
        g.setColor(new Color(255, 255, 0));
        for(int i = 0; i < 8; i++){
            double ang = Math.toRadians(i * 45);
            int sx = cx + (int)(Math.cos(ang) * h * 1.2);
            int sy = cy + (int)(Math.sin(ang) * h * 1.2);
            g.fillOval(sx - 2, sy - 2, 4, 4);
        }
    }

    private void drawIconSementes(Graphics2D g, int cx, int cy, int h) {
        g.setColor(new Color(34, 139, 34));
        int r = h/3;
        g.fillOval(cx - r, cy - r, r*2, r*2);
        g.fillOval(cx - h, cy, r*2, r*2);
        g.fillOval(cx + h - r*2, cy, r*2, r*2);
    }

    private void drawIconBiologico(Graphics2D g, int cx, int cy, int h) {
        g.setColor(new Color(65, 105, 225));
        g.fillOval(cx - h, cy - h/2, h*2, h);
        g.setColor(new Color(100, 149, 237));
        g.fillOval(cx - h + 4, cy - h/2 + 4, h*2 - 8, h - 8);
    }

    private void drawIconBiofiltro(Graphics2D g, int cx, int cy, int h) {
        g.setColor(new Color(70, 130, 180));
        int[] x = {cx, cx + h, cx, cx - h};
        int[] y = {cy - h, cy, cy + h, cy};
        g.fillPolygon(x, y, 4);
        g.setColor(new Color(100, 160, 210));
        g.drawPolygon(x, y, 4);
    }

    private void drawIconEnergiaSolar(Graphics2D g, int cx, int cy, int h) {
        g.setColor(new Color(255, 215, 0));
        g.fillRect(cx - h, cy - h/2, h*2, h);
        g.setColor(new Color(200, 170, 0));
        g.drawRect(cx - h, cy - h/2, h*2, h);
    }
}