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
            "Fotossintese",
            "Recupera 40 HP com energia solar",
            new Color(50, 205, 50)
    ),
    SEMENTES(
            "Sementes",
            "Dispara 2 projeteis extras (polinizacao)",
            new Color(34, 139, 34)
    ),
    BIOLOGICO(
            "Controle Biologico",
            "Recupera 3 HP ao eliminar pragas",
            new Color(65, 105, 225)
    ),
    BIOFILTRO(
            "Biofiltro",
            "Absorve ate 30 de dano (raizes protetoras)",
            new Color(70, 130, 180)
    ),
    ENERGIA_SOLAR(
            "Energia Solar",
            "Atira 25% mais rapido com luz renovavel",
            new Color(255, 215, 0)
    );

    public final String name;
    public final String description;
    public final Color  color;

    PowerUp(String name, String description, Color color) {
        this.name        = name;
        this.description = description;
        this.color       = color;
    }

    public static List<PowerUp> getRandomThree(Random rng) {
        List<PowerUp> all = new ArrayList<>(List.of(values()));
        Collections.shuffle(all, rng);
        return all.subList(0, 3);
    }

    public void drawIcon(Graphics2D g, int cx, int cy, int size) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        switch (this) {
            case FERTILIZANTE  -> drawIconFertilizante(g2, cx, cy, size);
            case COMPOSTAGEM   -> drawIconCompostagem(g2, cx, cy, size);
            case FOTOSINTESE   -> drawIconFotossintese(g2, cx, cy, size);
            case SEMENTES      -> drawIconSementes(g2, cx, cy, size);
            case BIOLOGICO     -> drawIconBiologico(g2, cx, cy, size);
            case BIOFILTRO     -> drawIconBiofiltro(g2, cx, cy, size);
            case ENERGIA_SOLAR -> drawIconEnergiaSolar(g2, cx, cy, size);
        }

        g2.dispose();
    }

    // -------------------------------------------------------------------------
    // FERTILIZANTE — saco de adubo com planta brotando
    // -------------------------------------------------------------------------
    private void drawIconFertilizante(Graphics2D g, int cx, int cy, int size) {
        int s = size / 2;

        // Saco de adubo
        g.setColor(new Color(101, 67, 33));
        int[] bx = { cx - s + 4, cx + s - 4, cx + s - 8, cx - s + 8 };
        int[] by = { cy,         cy,          cy + s,      cy + s };
        g.fillPolygon(bx, by, 4);

        // Gradiente simulado — lado claro
        g.setColor(new Color(139, 90, 43));
        g.fillRoundRect(cx - s + 4, cy, (s * 2) - 8, s - 2, 6, 6);

        // Linha horizontal do saco
        g.setColor(new Color(80, 50, 20));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(cx - s + 6, cy + s / 2, cx + s - 6, cy + s / 2);
        g.setStroke(new BasicStroke(1f));

        // Texto "NPK" no saco
        g.setFont(new Font("Arial", Font.BOLD, size / 7));
        g.setColor(new Color(200, 160, 80));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("NPK", cx - fm.stringWidth("NPK") / 2, cy + s / 2 - 1);

        // Amarracao do topo
        g.setColor(new Color(160, 120, 60));
        g.fillRoundRect(cx - s / 3, cy - 4, s * 2 / 3, 8, 4, 4);

        // Caule
        g.setColor(new Color(34, 120, 34));
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(cx, cy - 4, cx, cy - s + 2);
        g.setStroke(new BasicStroke(1f));

        // Folha esquerda
        g.setColor(new Color(50, 160, 50));
        int leafLX = cx - s / 2;
        int leafLY = cy - s / 2;
        g.fillOval(leafLX - s / 3, leafLY - s / 5,
                s * 2 / 3, s / 3);

        // Folha direita
        int leafRX = cx + s / 6;
        int leafRY = cy - s * 3 / 4;
        g.fillOval(leafRX, leafRY - s / 5,
                s * 2 / 3, s / 3);

        // Nervura folha esquerda
        g.setColor(new Color(30, 100, 30));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(leafLX - s / 3 + 2, leafLY,
                leafLX + s / 3 - 2, leafLY);
        g.drawLine(leafRX + 2, leafRY,
                leafRX + s / 2, leafRY);
        g.setStroke(new BasicStroke(1f));
    }

    // -------------------------------------------------------------------------
    // COMPOSTAGEM — minhoca saindo de monte de terra
    // -------------------------------------------------------------------------
    private void drawIconCompostagem(Graphics2D g, int cx, int cy, int size) {
        int s = size / 2;

        // Monte de composto (terra escura)
        g.setColor(new Color(72, 45, 20));
        g.fillOval(cx - s, cy, s * 2, s);

        // Camada superior mais clara
        g.setColor(new Color(101, 67, 33));
        g.fillOval(cx - s + 4, cy + 2, s * 2 - 8, s - 6);

        // Folhas / materia organica no composto
        g.setColor(new Color(80, 120, 30));
        g.fillOval(cx - s / 2, cy + 4, s / 2, s / 4);
        g.setColor(new Color(60, 90, 20));
        g.fillOval(cx + s / 6, cy + 6, s / 2, s / 4);
        g.setColor(new Color(150, 100, 40));
        g.fillOval(cx - s / 4, cy + s / 2, s / 3, s / 5);

        // Minhoca principal (curva)
        g.setColor(new Color(180, 100, 80));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        // corpo em arco
        g.drawArc(cx - s / 2, cy - s / 2, s, s, 200, 200);
        g.setStroke(new BasicStroke(1f));

        // Cabeca da minhoca
        g.setColor(new Color(210, 130, 100));
        g.fillOval(cx - s / 4 - 4, cy - s / 2 - 2, 10, 10);

        // Olhinhos da minhoca
        g.setColor(Color.BLACK);
        g.fillOval(cx - s / 4 - 2, cy - s / 2, 3, 3);
        g.fillOval(cx - s / 4 + 2, cy - s / 2, 3, 3);

        // Segunda minhoca menor
        g.setColor(new Color(160, 90, 70));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.drawArc(cx + s / 4, cy - s / 3, s / 2, s / 2, 180, 180);
        g.setStroke(new BasicStroke(1f));

        // Particulas de nutricao
        g.setColor(new Color(180, 220, 80, 200));
        int[][] dots = {
                { cx - s + 8, cy - 4 },
                { cx + s - 10, cy + 2 },
                { cx,         cy - s / 3 }
        };
        for (int[] d : dots) g.fillOval(d[0], d[1], 5, 5);
    }

    // -------------------------------------------------------------------------
    // FOTOSINTESE — folha com raios de sol
    // -------------------------------------------------------------------------
    private void drawIconFotossintese(Graphics2D g, int cx, int cy, int size) {
        int s = size / 2;

        // Raios de sol ao fundo
        g.setColor(new Color(255, 220, 50, 160));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double ang = Math.toRadians(i * 45);
            int x1 = cx + (int)(Math.cos(ang) * (s - 4));
            int y1 = cy + (int)(Math.sin(ang) * (s - 4));
            int x2 = cx + (int)(Math.cos(ang) * s);
            int y2 = cy + (int)(Math.sin(ang) * s);
            g.drawLine(x1, y1, x2, y2);
        }
        g.setStroke(new BasicStroke(1f));

        // Circulo do sol
        g.setColor(new Color(255, 230, 60));
        g.fillOval(cx - s / 3, cy - s / 3, s * 2 / 3, s * 2 / 3);
        g.setColor(new Color(255, 200, 0));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(cx - s / 3, cy - s / 3, s * 2 / 3, s * 2 / 3);
        g.setStroke(new BasicStroke(1f));

        // Folha em frente ao sol
        g.setColor(new Color(40, 180, 40));
        // forma de folha com bezier simulado por poligono
        int lw = s * 3 / 4;
        int lh = s / 2;
        int lx = cx - lw / 2;
        int ly = cy + s / 6;
        int[] leafX = {
                cx,
                cx + lw / 2,
                cx + lw / 3,
                cx,
                cx - lw / 3,
                cx - lw / 2
        };
        int[] leafY = {
                ly - lh / 2,
                ly,
                ly + lh / 2,
                ly + lh,
                ly + lh / 2,
                ly
        };
        g.fillPolygon(leafX, leafY, 6);

        // Nervura central
        g.setColor(new Color(20, 120, 20));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(cx, ly - lh / 2, cx, ly + lh);
        // Nervuras laterais
        g.drawLine(cx, ly + lh / 4,
                cx + lw / 3, ly + lh / 8);
        g.drawLine(cx, ly + lh / 4,
                cx - lw / 3, ly + lh / 8);
        g.setStroke(new BasicStroke(1f));

        // Brilho na folha
        g.setColor(new Color(120, 230, 100, 120));
        g.fillOval(cx - lw / 4, ly, lw / 4, lh / 4);

        // Cruz de HP (cura)
        g.setColor(new Color(255, 80, 80, 220));
        int cs = size / 10;
        g.fillRect(cx + s / 2, cy - s, cs * 3, cs);
        g.fillRect(cx + s / 2 + cs, cy - s - cs, cs, cs * 3);
    }

    // -------------------------------------------------------------------------
    // SEMENTES — 3 projeteis saindo de flor/semente central
    // -------------------------------------------------------------------------
    private void drawIconSementes(Graphics2D g, int cx, int cy, int size) {
        int s = size / 2;

        // Semente central
        g.setColor(new Color(180, 130, 40));
        g.fillOval(cx - s / 4, cy - s / 4, s / 2, s / 2);
        g.setColor(new Color(220, 170, 60));
        g.fillOval(cx - s / 5, cy - s / 5, s / 5 * 2, s / 5 * 2);

        // Linhas de disparo (3 direcoes)
        double[] angles = { -Math.PI / 2, -Math.PI / 6, -5 * Math.PI / 6 };
        Color[] trailColors = {
                new Color(100, 200, 100, 180),
                new Color(80, 180, 80, 140),
                new Color(60, 160, 60, 100)
        };

        for (int i = 0; i < 3; i++) {
            double ang = angles[i];

            // Trilha
            for (int t = 1; t <= 3; t++) {
                float dist = s * 0.3f * t;
                int tx = cx + (int)(Math.cos(ang) * dist);
                int ty = cy + (int)(Math.sin(ang) * dist);
                g.setColor(trailColors[Math.min(t - 1, 2)]);
                int ts = Math.max(3, 7 - t * 2);
                g.fillOval(tx - ts / 2, ty - ts / 2, ts, ts);
            }

            // Projetil na ponta
            int px = cx + (int)(Math.cos(ang) * (s - 4));
            int py = cy + (int)(Math.sin(ang) * (s - 4));

            // Forma de semente/projetil oval rotacionado
            Graphics2D grot = (Graphics2D) g.create();
            grot.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            grot.translate(px, py);
            grot.rotate(ang + Math.PI / 2);
            grot.setColor(new Color(50, 200, 80));
            grot.fillOval(-4, -7, 8, 14);
            grot.setColor(new Color(100, 230, 120));
            grot.fillOval(-2, -5, 4, 6);
            grot.dispose();
        }

        // Petalas ao redor da semente
        g.setColor(new Color(200, 230, 100, 180));
        for (int i = 0; i < 6; i++) {
            double ang = Math.toRadians(i * 60);
            int px = cx + (int)(Math.cos(ang) * s / 3);
            int py = cy + (int)(Math.sin(ang) * s / 3);
            g.fillOval(px - 4, py - 4, 8, 8);
        }
    }

    // -------------------------------------------------------------------------
    // BIOLOGICO — joaninha (simbolo de controle biologico)
    // -------------------------------------------------------------------------
    private void drawIconBiologico(Graphics2D g, int cx, int cy, int size) {
        int s = size / 2;

        // Folha de fundo
        g.setColor(new Color(40, 130, 40, 160));
        int[] leafX = { cx - s, cx, cx + s, cx };
        int[] leafY = { cy,     cy - s / 2, cy, cy + s / 2 };
        g.fillPolygon(leafX, leafY, 4);

        // Corpo vermelho da joaninha (elipse)
        g.setColor(new Color(200, 30, 30));
        g.fillOval(cx - s / 2, cy - s / 2, s, s);

        // Divisao central (linha preta)
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(cx, cy - s / 2, cx, cy + s / 2);
        g.setStroke(new BasicStroke(1f));

        // Pontos pretos
        int dotR = size / 14;
        g.fillOval(cx - s / 3 - dotR, cy - s / 4 - dotR,
                dotR * 2, dotR * 2);
        g.fillOval(cx + s / 3 - dotR, cy - s / 4 - dotR,
                dotR * 2, dotR * 2);
        g.fillOval(cx - s / 3 - dotR, cy + s / 8 - dotR,
                dotR * 2, dotR * 2);
        g.fillOval(cx + s / 3 - dotR, cy + s / 8 - dotR,
                dotR * 2, dotR * 2);

        // Cabeca preta
        g.setColor(Color.BLACK);
        g.fillOval(cx - s / 4, cy - s / 2 - s / 5,
                s / 2, s / 3);

        // Olhinhos brancos
        g.setColor(Color.WHITE);
        g.fillOval(cx - s / 5, cy - s / 2 - s / 8, 4, 4);
        g.fillOval(cx + s / 10, cy - s / 2 - s / 8, 4, 4);

        // Antenas
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(cx - s / 6, cy - s / 2 - s / 5,
                cx - s / 3, cy - s * 3 / 4);
        g.drawLine(cx + s / 6, cy - s / 2 - s / 5,
                cx + s / 3, cy - s * 3 / 4);
        // bolinha ponta da antena
        g.setStroke(new BasicStroke(1f));
        g.fillOval(cx - s / 3 - 2, cy - s * 3 / 4 - 2, 4, 4);
        g.fillOval(cx + s / 3 - 2, cy - s * 3 / 4 - 2, 4, 4);

        // Brilho no corpo
        g.setColor(new Color(255, 120, 120, 130));
        g.fillOval(cx - s / 4, cy - s / 3, s / 4, s / 6);

        // Simbolo de cura (cruz verde)
        int cs = size / 12;
        g.setColor(new Color(60, 220, 60, 220));
        g.fillRect(cx + s / 2 - cs * 2, cy - cs / 2, cs * 2, cs);
        g.fillRect(cx + s / 2 - cs - cs / 2, cy - cs, cs, cs * 2);
    }

    // -------------------------------------------------------------------------
    // BIOFILTRO — escudo de raizes / escudo organico
    // -------------------------------------------------------------------------
    private void drawIconBiofiltro(Graphics2D g, int cx, int cy, int size) {
        int s = size / 2;

        // Escudo base
        int[] shX = {
                cx,
                cx + s,
                cx + s,
                cx + s * 3 / 4,
                cx,
                cx - s * 3 / 4,
                cx - s,
                cx - s
        };
        int[] shY = {
                cy - s,
                cy - s / 2,
                cy + s / 4,
                cy + s * 3 / 4,
                cy + s,
                cy + s * 3 / 4,
                cy + s / 4,
                cy - s / 2
        };

        // Sombra do escudo
        g.setColor(new Color(20, 60, 100, 150));
        int[] shXs = new int[shX.length];
        int[] shYs = new int[shY.length];
        for (int i = 0; i < shX.length; i++) {
            shXs[i] = shX[i] + 3;
            shYs[i] = shY[i] + 3;
        }
        g.fillPolygon(shXs, shYs, shX.length);

        // Escudo preenchido com gradiente simulado
        g.setColor(new Color(40, 100, 160));
        g.fillPolygon(shX, shY, shX.length);
        g.setColor(new Color(70, 140, 200));
        // borda interna clara
        int[] shXi = new int[shX.length];
        int[] shYi = new int[shY.length];
        int margin = 5;
        for (int i = 0; i < shX.length; i++) {
            float dx = shX[i] - cx;
            float dy = shY[i] - cy;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            shXi[i] = cx + (int)(dx * (len - margin) / len);
            shYi[i] = cy + (int)(dy * (len - margin) / len);
        }
        g.fillPolygon(shXi, shYi, shX.length);

        // Raizes sobre o escudo
        g.setColor(new Color(60, 140, 60));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        // Raiz esquerda
        g.drawArc(cx - s * 3 / 4, cy - s / 4, s / 2, s, 0, 180);
        // Raiz direita
        g.drawArc(cx + s / 4, cy - s / 4, s / 2, s, 0, 180);
        // Raiz central
        g.drawLine(cx, cy - s / 2, cx, cy + s / 3);
        g.drawLine(cx, cy, cx - s / 3, cy + s / 2);
        g.drawLine(cx, cy, cx + s / 3, cy + s / 2);
        g.setStroke(new BasicStroke(1f));

        // Brilho no escudo (topo)
        g.setColor(new Color(150, 200, 255, 100));
        g.fillOval(cx - s / 3, cy - s + 4, s * 2 / 3, s / 3);

        // Borda do escudo
        g.setColor(new Color(100, 180, 220));
        g.setStroke(new BasicStroke(2f));
        g.drawPolygon(shX, shY, shX.length);
        g.setStroke(new BasicStroke(1f));

        // Numero +30 no escudo
        g.setFont(new Font("Arial", Font.BOLD, size / 7));
        g.setColor(new Color(200, 240, 255));
        FontMetrics fm = g.getFontMetrics();
        String label = "+30";
        g.drawString(label, cx - fm.stringWidth(label) / 2, cy + s / 5);
    }

    // -------------------------------------------------------------------------
    // ENERGIA SOLAR — painel solar com raios
    // -------------------------------------------------------------------------
    private void drawIconEnergiaSolar(Graphics2D g, int cx, int cy, int size) {
        int s = size / 2;

        // Raios de sol (atras do painel)
        g.setColor(new Color(255, 200, 0, 180));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double ang = Math.toRadians(i * 45 - 22);
            int r1 = s * 2 / 3;
            int r2 = s - 2;
            g.drawLine(
                    cx + (int)(Math.cos(ang) * r1),
                    cy + (int)(Math.sin(ang) * r1),
                    cx + (int)(Math.cos(ang) * r2),
                    cy + (int)(Math.sin(ang) * r2)
            );
        }
        g.setStroke(new BasicStroke(1f));

        // Sol central pequeno
        g.setColor(new Color(255, 230, 60));
        g.fillOval(cx - s / 4, cy - s / 4, s / 2, s / 2);

        // Painel solar (retangulo inclinado)
        Graphics2D gp = (Graphics2D) g.create();
        gp.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        gp.translate(cx, cy);
        gp.rotate(Math.toRadians(-20));

        int pw = s + s / 3;
        int ph = s * 2 / 3;

        // Moldura do painel
        gp.setColor(new Color(60, 60, 80));
        gp.fillRoundRect(-pw / 2 - 2, -ph / 2 - 2,
                pw + 4, ph + 4, 6, 6);

        // Celulas do painel (3x2)
        Color[] cellColors = {
                new Color(20, 40, 120),
                new Color(25, 50, 140),
                new Color(15, 35, 110)
        };
        int cols = 3, rows = 2;
        int cellW = pw / cols;
        int cellH = ph / rows;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int ccx = -pw / 2 + c * cellW;
                int ccy = -ph / 2 + r * cellH;
                gp.setColor(cellColors[(r * cols + c) % cellColors.length]);
                gp.fillRect(ccx + 1, ccy + 1, cellW - 2, cellH - 2);
                // Reflexo em cada celula
                gp.setColor(new Color(100, 150, 255, 80));
                gp.fillRect(ccx + 2, ccy + 2,
                        cellW / 2 - 2, cellH / 2 - 2);
            }
        }

        // Grade do painel
        gp.setColor(new Color(80, 80, 100));
        gp.setStroke(new BasicStroke(1f));
        for (int c = 1; c < cols; c++)
            gp.drawLine(-pw / 2 + c * cellW, -ph / 2,
                    -pw / 2 + c * cellW, ph / 2);
        gp.drawLine(-pw / 2, 0, pw / 2, 0);

        gp.dispose();

        // Raio / seta de energia saindo do painel
        g.setColor(new Color(255, 230, 0));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        int bx = cx + s / 2;
        int by = cy + s / 3;
        g.drawLine(bx, by, bx + s / 3, by - s / 4);
        // Seta
        g.fillOval(bx + s / 3 - 3, by - s / 4 - 3, 7, 7);
        g.setStroke(new BasicStroke(1f));

        // Simbolo de velocidade (listras)
        g.setColor(new Color(255, 255, 100, 200));
        int lx = cx - s;
        for (int i = 0; i < 3; i++) {
            int lw2 = (3 - i) * s / 4;
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(lx, cy + i * 5, lx + lw2, cy + i * 5);
        }
        g.setStroke(new BasicStroke(1f));
    }
}