package com.greenforest.ui;

import java.awt.*;

public class EducationalOverlay {

    private String hordaMessage = null;
    private float  hordaTimer   = 0f;
    private static final float HORDA_DURATION = 4.5f;

    private static final String[] HORDA_LESSONS = {
            /* horda 1  */ "HORDA 1: SACOLAS PLÁSTICAS\nUm brasileiro usa em média 45 sacolas plásticas por mês.\nSubstitua por sacolas reutilizáveis!",
            /* horda 2  */ "HORDA 2: LATAS DE ALUMÍNIO\nO alumínio pode ser reciclado infinitas vezes.\nReciclar gasta 95% menos energia que produzir do zero!",
            /* horda 3  */ "HORDA 3: PNEUS DESCARTADOS\nPneus levam até 600 anos para se decompor.\nAcumulam água parada e aumentam o risco de dengue!",
            /* horda 4  */ "HORDA 4: NUVEM TÓXICA\nA poluição do ar causa asma, bronquite e câncer de pulmão.\nPrefira transporte público, bicicleta ou caminhada!",
            /* horda 5  */ "HORDA 5: CIGARROS\nO filtro do cigarro contém microplásticos e celulosa de acetato.\nE o item mais coletado em limpezas de praias no mundo!",
            /* horda 6  */ "HORDA 6: GARRAFAS PLÁSTICAS\nGarrafas PET levam até 450 anos para se decompor.\nPrefira garrafas reutilizáveis de inox ou vidro!",
            /* horda 7  */ "HORDA 7: ÓLEO RESIDUAL\nUm litro de óleo de cozinha contamina até 1 milhão de litros de agua.\nDescarte em postos de coleta de óleo!",
            /* horda 8  */ "HORDA 8: ISOPOR\nIsopor (EPS) demora mais de 400 anos para se decompor.\nA maioria das cooperativas não recicla isopor!",
            /* horda 9  */ "HORDA 9: AGROTÓXICOS\nO Brasil é o maior consumidor de agrotóxicos do mundo.\nContaminam solo, rios e chegam à sua mesa!",
            /* horda 10 */ "HORDA 10: ENTULHO\nO entulho responde por mais de 50% do lixo urbano do Brasil.\nDescarte apenas em ecopontos e cacarecas licenciadas!"
    };

    public void showHordaMessage(String msg) {
        hordaMessage = msg;
        hordaTimer   = HORDA_DURATION;
    }

    public void showHordaLesson(int hordaNumber) {
        int idx = Math.max(0, Math.min(hordaNumber - 1, HORDA_LESSONS.length - 1));
        showHordaMessage(HORDA_LESSONS[idx]);
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

        g2.setColor(new Color(0, 0, 0, Math.min(a / 2, 100)));
        g2.fillRoundRect(bx + 3, by + 3, boxW, boxH, 16, 16);

        g2.setColor(new Color(10, 30, 10, Math.min(a, 210)));
        g2.fillRoundRect(bx, by, boxW, boxH, 16, 16);

        g2.setColor(new Color(80, 200, 80, a));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx, by, boxW, boxH, 16, 16);
        g2.setStroke(new BasicStroke(1f));

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
        g2.setColor(new Color(0, 0, 0, 215));
        g2.fillRect(0, 0, screenW, screenH);

        int cx = screenW / 2;

        g2.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics fm = g2.getFontMetrics();
        String title = "BOSS DERROTADO: " + bossName;
        g2.setColor(new Color(100, 0, 0));
        g2.drawString(title, cx - fm.stringWidth(title) / 2 + 2, 124);
        g2.setColor(new Color(255, 70, 70));
        g2.drawString(title, cx - fm.stringWidth(title) / 2, 122);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(new Color(255, 210, 60));
        String sub = "VOCE APRENDEU:";
        fm = g2.getFontMetrics();
        g2.drawString(sub, cx - fm.stringWidth(sub) / 2, 158);

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

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(bx + 4, by + 4, boxW, boxH, 18, 18);

        g2.setColor(new Color(15, 35, 15, 230));
        g2.fillRoundRect(bx, by, boxW, boxH, 18, 18);

        g2.setColor(new Color(60, 180, 60));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(bx, by, boxW, boxH, 18, 18);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(new Color(200, 255, 175));
        for (int i = 0; i < lines.length; i++) {
            fm = g2.getFontMetrics();
            int tx = bx + padding + (maxW - fm.stringWidth(lines[i])) / 2;
            int ty = by + padding + fm.getAscent() + i * lineH;
            g2.drawString(lines[i], tx, ty);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(new Color(140, 240, 140));
        String cont = "Pressione ENTER para continuar";
        fm = g2.getFontMetrics();
        g2.drawString(cont, cx - fm.stringWidth(cont) / 2, by + boxH + 42);
    }

    private static final String[] GAME_OVER_LESSONS = {
            "O Brasil gera 80 milhoes de toneladas de residuos por ano.\nApenas 4% e reciclado. Separe o seu lixo!",
            "Sacolas plasticas levam ate 400 anos para se decompor.\nPrefira sacolas reutilizaveis!",
            "Pneus descartados acumulam agua parada e transmitem dengue.\nDescarte em borracharias ou pontos de coleta!",
            "Cigarros sao o item mais catado nas limpezas de praias do mundo.\nSeu filtro contem microplasticos toxicos!",
            "Latas de aluminio podem ser recicladas infinitas vezes.\nReciclar aluminio gasta 95% menos energia!",
            "A poluicao nas cidades causa doencas respiratorias.\nAndando de bike ou a pe voce ajuda a reduzir as emissoes!"
    };

    public String randomGameOverLesson() {
        return GAME_OVER_LESSONS[(int)(Math.random() * GAME_OVER_LESSONS.length)];
    }

    public boolean hasHordaMessage() { return hordaMessage != null; }
}