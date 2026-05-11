package com.greenforest.ui;

import com.greenforest.DifficultySettings;
import com.greenforest.entity.Player;
import com.greenforest.manager.EnemyManager;

import java.awt.*;

public class HUD {

    private static final String[] TIPS = {
            "Separe o lixo reciclavel: papel, plastico, vidro e metal",
            "Nunca jogue bituca de cigarro no chao: ela contem microplasticos",
            "Pneus velhos acumulam agua parada e propagam dengue",
            "Lata de aluminio reciclada economiza 95% de energia",
            "Sacolas plasticas levam ate 400 anos para se decompor",
            "Prefira embalagens retornaveis e biodegradaveis",
            "Pilhas e baterias contem metais pesados: descarte correto!",
            "O Brasil gera 80 milhoes de toneladas de residuos por ano",
            "Apenas 4% do lixo brasileiro e reciclado. Voce pode mudar isso!",
            "Oleo de cozinha usado contamina ate 1 milhao de litros de agua",
            "Isopor nao e reciclado pela maioria das cooperativas",
            "Agrotoxicos contaminam o solo por decadas",
            "Entulho descartado incorretamente causa enchentes",
            "Microplasticos ja foram encontrados no sangue humano",
            "Desmatamento destruiu 20% da Amazonia original"
    };

    private int   tipIndex  = 0;
    private float tipTimer  = 0f;
    private static final float TIP_INTERVAL = 8f;

    private String attackModeLabel = "AUTO";

    public void setAttackMode(boolean manual) {
        attackModeLabel = manual ? "MANUAL" : "AUTO";
    }

    public void update(float dt) {
        tipTimer += dt;
        if (tipTimer >= TIP_INTERVAL) {
            tipTimer = 0f;
            tipIndex = (tipIndex + 1) % TIPS.length;
        }
    }

    public void draw(Graphics2D g2, int screenW, int screenH,
                     Player player, EnemyManager em,
                     float gameTimeSec,
                     boolean bossImminent, float bossTimerRatio) {

        drawBar(g2, 12, 12, 190, 20,
                (float) player.getHp() / player.getMaxHp(),
                new Color(220, 40, 40), new Color(60, 0, 0), "HP");

        drawBar(g2, 12, 38, 190, 13,
                (float) player.getXP() / player.getXPToNext(),
                new Color(60, 190, 255), new Color(0, 30, 60), "XP");

        if (player.getMaxShield() > 0) {
            drawBar(g2, 12, 57, 190, 9,
                    (float) player.getShield() / player.getMaxShield(),
                    new Color(100, 180, 255), new Color(10, 30, 70), "ESC");
        }

        int statsY = player.getMaxShield() > 0 ? 82 : 68;
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(new Color(220, 255, 220));
        g2.drawString("Nivel " + player.getLevel(), 12, statsY);
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("Kills: " + player.getKills(), 12, statsY + 16);

        int mins = (int)(gameTimeSec / 60);
        int secs = (int)(gameTimeSec % 60);
        String timeStr = String.format("%02d:%02d", mins, secs);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        int tx = screenW / 2 - fm.stringWidth(timeStr) / 2;
        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(timeStr, tx + 1, 29);
        g2.setColor(Color.WHITE);
        g2.drawString(timeStr, tx, 28);

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(160, 240, 160));
        String infoStr = DifficultySettings.label() + "  |  " + attackModeLabel;
        fm = g2.getFontMetrics();
        g2.drawString(infoStr, screenW - fm.stringWidth(infoStr) - 12, 18);

        drawHordaProgress(g2, em, screenW);

        if (bossImminent) {
            long t        = System.currentTimeMillis();
            long interval = (long)(500 - bossTimerRatio * 300);
            if (interval < 50) interval = 50;
            if ((t / interval) % 2 == 0) {
                g2.setFont(new Font("Arial", Font.BOLD, 15));
                fm = g2.getFontMetrics();
                String warn = "!! BOSS SE APROXIMA !!";
                int wx = screenW / 2 - fm.stringWidth(warn) / 2;
                g2.setColor(new Color(0, 0, 0, 160));
                g2.drawString(warn, wx + 1, 57);
                g2.setColor(new Color(255, 70, 70));
                g2.drawString(warn, wx, 56);
            }
        }

        drawTicker(g2, screenW, screenH);
    }

    private void drawTicker(Graphics2D g2, int screenW, int screenH) {
        int barH = 26;
        int barY = screenH - barH;

        g2.setColor(new Color(0, 0, 0, 165));
        g2.fillRect(0, barY, screenW, barH);

        g2.setColor(new Color(60, 120, 60, 200));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, barY, screenW, barY);

        g2.setFont(new Font("Arial", Font.ITALIC, 13));
        FontMetrics fm = g2.getFontMetrics();

        String prefix = "  Eco: ";
        String tip    = TIPS[tipIndex];

        // Progresso de fade entre dicas
        float progress = tipTimer / TIP_INTERVAL;
        int alpha;
        if (progress < 0.1f)       alpha = (int)(progress / 0.1f * 255);
        else if (progress > 0.85f) alpha = (int)((1f - (progress - 0.85f) / 0.15f) * 255);
        else                       alpha = 255;
        alpha = Math.max(0, Math.min(255, alpha));

        int textY = barY + barH - 7;

        g2.setColor(new Color(80, 180, 80, Math.min(alpha, 200)));
        g2.drawString(prefix, 4, textY);

        int prefixW = fm.stringWidth(prefix);
        g2.setColor(new Color(200, 255, 190, alpha));
        g2.drawString(tip, 4 + prefixW, textY);
    }

    private void drawHordaProgress(Graphics2D g2, EnemyManager em, int screenW) {
        int cx = screenW / 2;
        int y  = 62;

        if (em.isInBreak()) {
            String msg = "PROXIMA HORDA EM " + (int) Math.ceil(em.getBreakTimer()) + "s...";
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int ttx = cx - fm.stringWidth(msg) / 2;
            g2.setColor(new Color(0, 0, 0, 140));
            g2.drawString(msg, ttx + 1, y + 1);
            g2.setColor(new Color(255, 220, 60));
            g2.drawString(msg, ttx, y);
            return;
        }

        int barW = 150;
        int barH = 8;
        int bx   = cx - barW / 2;
        int by   = y - barH;

        g2.setColor(new Color(20, 40, 20, 180));
        g2.fillRoundRect(bx - 1, by - 1, barW + 2, barH + 2, 5, 5);

        float ratio  = Math.min(1f,
                (float) em.getKillsThisHorda() / em.getKillsToNextHorda());
        int   filled = (int)(barW * ratio);
        if (filled > 0) {
            GradientPaint gp = new GradientPaint(
                    bx, by, new Color(80, 220, 80),
                    bx + barW, by, new Color(40, 160, 40));
            g2.setPaint(gp);
            g2.fillRoundRect(bx, by, filled, barH, 4, 4);
            g2.setPaint(null);
        }

        g2.setColor(new Color(80, 160, 80, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(bx, by, barW, barH, 5, 5);

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        String hordaStr = "Horda " + em.getHordaNumber();
        String killsStr = em.getKillsThisHorda()
                + "/" + em.getKillsToNextHorda() + " kills";

        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(hordaStr, cx - fm.stringWidth(hordaStr) / 2 + 1, by - 1);
        g2.setColor(new Color(180, 255, 180));
        g2.drawString(hordaStr, cx - fm.stringWidth(hordaStr) / 2, by - 2);

        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(killsStr,
                cx - fm.stringWidth(killsStr) / 2 + 1, by + barH + 12);
        g2.setColor(Color.WHITE);
        g2.drawString(killsStr,
                cx - fm.stringWidth(killsStr) / 2, by + barH + 11);
    }

    private void drawBar(Graphics2D g2, int x, int y, int w, int h,
                         float ratio, Color fill, Color bg, String label) {
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, 7, 7);

        int fw = (int)(w * Math.max(0f, Math.min(1f, ratio)));
        if (fw > 0) {
            g2.setColor(fill);
            g2.fillRoundRect(x, y, fw, h, 7, 7);
            g2.setColor(new Color(255, 255, 255, 55));
            g2.fillRoundRect(x, y, fw, h / 2, 7, 7);
        }

        g2.setColor(new Color(255, 255, 255, 70));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x, y, w, h, 7, 7);

        g2.setFont(new Font("Arial", Font.BOLD, Math.max(9, h - 5)));
        g2.setColor(Color.WHITE);
        g2.drawString(label, x + 5, y + h - 3);
    }
}