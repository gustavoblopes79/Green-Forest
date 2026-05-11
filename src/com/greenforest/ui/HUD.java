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
            "Pilhas e baterias contem metais pesados: descarte correto!"
    };

    private int   tipIndex = 0;
    private float tipTimer = 0f;
    private static final float TIP_INTERVAL = 6f;

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

        // HP
        drawBar(g2, 12, 12, 190, 20,
                (float) player.getHp() / player.getMaxHp(),
                new Color(220, 40, 40), new Color(60, 0, 0), "HP");

        // XP
        drawBar(g2, 12, 38, 190, 13,
                (float) player.getXP() / player.getXPToNext(),
                new Color(60, 190, 255), new Color(0, 30, 60), "XP");

        // Escudo (so aparece se o jogador tiver escudo)
        if (player.getMaxShield() > 0) {
            drawBar(g2, 12, 57, 190, 9,
                    (float) player.getShield() / player.getMaxShield(),
                    new Color(100, 180, 255), new Color(10, 30, 70),
                    "ESC");
        }

        // Nivel e kills
        int statsY = player.getMaxShield() > 0 ? 82 : 68;
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(new Color(220, 255, 220));
        g2.drawString("Nivel " + player.getLevel(), 12, statsY);
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("Kills: " + player.getKills(), 12, statsY + 16);

        // Timer central
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

        // Info canto superior direito
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(160, 240, 160));
        String infoStr = DifficultySettings.label()
                + "  |  " + attackModeLabel;
        fm = g2.getFontMetrics();
        g2.drawString(infoStr,
                screenW - fm.stringWidth(infoStr) - 12, 18);

        // Progresso de horda (centro, abaixo do timer)
        drawHordaProgress(g2, em, screenW);

        // Aviso de boss
        if (bossImminent) {
            long t        = System.currentTimeMillis();
            long interval = (long)(500 - bossTimerRatio * 300);
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

        // Rodape com dica
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, screenH - 28, screenW, 28);
        g2.setFont(new Font("Arial", Font.ITALIC, 13));
        g2.setColor(new Color(160, 240, 160));
        String tip = "Dica: " + TIPS[tipIndex];
        fm = g2.getFontMetrics();
        g2.drawString(tip,
                screenW / 2 - fm.stringWidth(tip) / 2,
                screenH - 8);
    }

    private void drawHordaProgress(Graphics2D g2,
                                   EnemyManager em, int screenW) {
        int cx = screenW / 2;
        int y  = 62;

        if (em.isInBreak()) {
            // Contagem regressiva entre hordas
            String msg = "PROXIMA HORDA EM "
                    + (int) Math.ceil(em.getBreakTimer()) + "s...";
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int tx = cx - fm.stringWidth(msg) / 2;
            g2.setColor(new Color(0, 0, 0, 140));
            g2.drawString(msg, tx + 1, y + 1);
            g2.setColor(new Color(255, 220, 60));
            g2.drawString(msg, tx, y);
            return;
        }

        // Barra de progresso
        int barW = 150;
        int barH = 8;
        int bx   = cx - barW / 2;
        int by   = y - barH;

        // Fundo
        g2.setColor(new Color(20, 40, 20, 180));
        g2.fillRoundRect(bx - 1, by - 1, barW + 2, barH + 2, 5, 5);

        // Fill
        float ratio = Math.min(1f,
                (float) em.getKillsThisHorda()
                        / em.getKillsToNextHorda());
        int filled = (int)(barW * ratio);
        if (filled > 0) {
            GradientPaint gp = new GradientPaint(
                    bx, by, new Color(80, 220, 80),
                    bx + barW, by, new Color(40, 160, 40));
            g2.setPaint(gp);
            g2.fillRoundRect(bx, by, filled, barH, 4, 4);
        }

        // Borda
        g2.setColor(new Color(80, 160, 80, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(bx, by, barW, barH, 5, 5);

        // Texto: "Horda 3  12/40 kills"
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        String hordaStr = "Horda " + em.getHordaNumber();
        String killsStr = em.getKillsThisHorda()
                + "/" + em.getKillsToNextHorda() + " kills";

        // Horda acima da barra
        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(hordaStr,
                cx - fm.stringWidth(hordaStr) / 2 + 1, by - 1);
        g2.setColor(new Color(180, 255, 180));
        g2.drawString(hordaStr,
                cx - fm.stringWidth(hordaStr) / 2, by - 2);

        // Kills abaixo da barra
        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(killsStr,
                cx - fm.stringWidth(killsStr) / 2 + 1,
                by + barH + 12);
        g2.setColor(Color.WHITE);
        g2.drawString(killsStr,
                cx - fm.stringWidth(killsStr) / 2,
                by + barH + 11);
    }

    private void drawBar(Graphics2D g2, int x, int y, int w, int h,
                         float ratio, Color fill, Color bg,
                         String label) {
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