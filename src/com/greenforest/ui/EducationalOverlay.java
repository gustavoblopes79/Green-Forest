package com.greenforest.ui;

import java.awt.*;

public class EducationalOverlay {

    private String hordaMessage = null;
    private float  hordaTimer   = 0f;
    private static final float HORDA_DURATION = 4.0f;

    public void showHordaMessage(String msg) {
        hordaMessage = msg;
        hordaTimer   = HORDA_DURATION;
    }

    public void update(float dt) {
        if (hordaMessage != null) {
            hordaTimer -= dt;
            if (hordaTimer <= 0f) hordaMessage = null;
        }
    }

    public void drawHordaMessage(Graphics2D g2, int screenW, int screenH) {
        if (hordaMessage == null) return;

        float alpha = Math.min(1f, hordaTimer / 0.6f);
        int a = (int)(alpha * 230);

        String[] lines = hordaMessage.split("\n");
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();

        int lineH   = fm.getHeight() + 3;
        int padding = 20;
        int maxW    = 0;
        for (String l : lines) maxW = Math.max(maxW, fm.stringWidth(l));

        int boxW = maxW + padding * 2;
        int boxH = lines.length * lineH + padding * 2;
        int bx   = (screenW - boxW) / 2;
        int by   = screenH / 2 - 90;

        // Sombra da caixa
        g2.setColor(new Color(0, 0, 0, Math.min(a / 2, 100)));
        g2.fillRoundRect(bx + 3, by + 3, boxW, boxH, 16, 16);

        // Fundo
        g2.setColor(new Color(10, 30, 10, Math.min(a, 210)));
        g2.fillRoundRect(bx, by, boxW, boxH, 16, 16);

        // Borda
        g2.setColor(new Color(80, 200, 80, a));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx, by, boxW, boxH, 16, 16);
        g2.setStroke(new BasicStroke(1f));

        // Texto
        for (int i = 0; i < lines.length; i++) {
            g2.setFont(new Font("Arial",
                    i == 0 ? Font.BOLD : Font.PLAIN, i == 0 ? 16 : 14));
            fm = g2.getFontMetrics();
            g2.setColor(new Color(
                    i == 0 ? 120 : 190,
                    i == 0 ? 255 : 245,
                    i == 0 ? 120 : 180, a));
            int tx = bx + padding + (maxW - fm.stringWidth(lines[i])) / 2;
            int ty = by + padding + fm.getAscent() + i * lineH;
            g2.drawString(lines[i], tx, ty);
        }
    }

    public void drawBossLesson(Graphics2D g2, int screenW, int screenH,
                               String lesson, String bossName) {
        // Fundo
        g2.setColor(new Color(0, 0, 0, 215));
        g2.fillRect(0, 0, screenW, screenH);

        int cx = screenW / 2;

        // Titulo do boss
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        g2.setColor(new Color(255, 70, 70));
        String title = "BOSS DERROTADO: " + bossName;
        FontMetrics fm = g2.getFontMetrics();
        // Sombra
        g2.setColor(new Color(100, 0, 0));
        g2.drawString(title, cx - fm.stringWidth(title) / 2 + 2, 124);
        g2.setColor(new Color(255, 70, 70));
        g2.drawString(title, cx - fm.stringWidth(title) / 2, 122);

        // Subtitulo
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(new Color(255, 210, 60));
        String sub = "VOCE APRENDEU:";
        fm = g2.getFontMetrics();
        g2.drawString(sub, cx - fm.stringWidth(sub) / 2, 158);

        // Caixa da licao
        String[] lines = lesson.split("\n");
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g2.getFontMetrics();
        int lineH   = fm.getHeight() + 5;
        int padding = 28;
        int maxW    = 0;
        for (String l : lines) maxW = Math.max(maxW, fm.stringWidth(l));
        int boxW = maxW + padding * 2;
        int boxH = lines.length * lineH + padding * 2;
        int bx   = cx - boxW / 2;
        int by   = 176;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(bx + 4, by + 4, boxW, boxH, 18, 18);

        // Fundo
        g2.setColor(new Color(15, 35, 15, 230));
        g2.fillRoundRect(bx, by, boxW, boxH, 18, 18);

        // Borda brilhante
        g2.setColor(new Color(60, 180, 60));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(bx, by, boxW, boxH, 18, 18);
        g2.setStroke(new BasicStroke(1f));

        // Texto da licao
        g2.setColor(new Color(200, 255, 175));
        for (int i = 0; i < lines.length; i++) {
            fm = g2.getFontMetrics();
            int tx = bx + padding + (maxW - fm.stringWidth(lines[i])) / 2;
            int ty = by + padding + fm.getAscent() + i * lineH;
            g2.drawString(lines[i], tx, ty);
        }

        // Instrucao
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(new Color(140, 240, 140));
        String cont = "Pressione ENTER para continuar";
        fm = g2.getFontMetrics();
        g2.drawString(cont,
                cx - fm.stringWidth(cont) / 2,
                by + boxH + 42);
    }

    private static final String[] GAME_OVER_LESSONS = {
            "O Brasil gera 80 milhoes de toneladas de residuos por ano.\n" +
                    "Apenas 4% e reciclado. Separe o seu lixo!",
            "Sacolas plasticas levam ate 400 anos para se decompor.\n" +
                    "Prefira sacolas reutilizaveis!",
            "Pneus descartados acumulam agua parada e transmitem dengue.\n" +
                    "Descarte em borracharias ou pontos de coleta!",
            "Cigarros sao o item mais catado nas limpezas de praias do mundo.\n" +
                    "Seu filtro contem microplasticos toxicos!",
            "Latas de aluminio podem ser recicladas infinitas vezes.\n" +
                    "Reciclar aluminio gasta 95% menos energia!",
            "A poluicao nas cidades causa doencas respiratorias.\n" +
                    "Andando de bike ou a pe voce ajuda a reduzir as emissoes!"
    };

    public String randomGameOverLesson() {
        return GAME_OVER_LESSONS[(int)(Math.random() * GAME_OVER_LESSONS.length)];
    }

    public boolean hasHordaMessage() { return hordaMessage != null; }
}