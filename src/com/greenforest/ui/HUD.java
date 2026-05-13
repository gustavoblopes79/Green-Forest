package com.greenforest.ui;

import com.greenforest.AttackType;
import com.greenforest.DifficultySettings;
import com.greenforest.ScoreManager;
import com.greenforest.entity.Player;
import com.greenforest.manager.EnemyManager;

import java.awt.*;

public class HUD {

    private static final String[] TIPS = {
            "Separe o lixo reciclável: papel, plástico, vidro e metal",
            "Nunca jogue bituca de cigarro no chão: ela contém microplásticos",
            "Pneus velhos acumulam água parada e propagam dengue",
            "Lata de alumínio reciclada economiza 95% de energia",
            "Sacolas plásticas levam até 400 anos para se decompor",
            "Prefira embalagens retornáveis e biodegradáveis",
            "Pilhas e baterias contém metais pesados: descarte corretamente!",
            "O Brasil gera 80 milhões de toneladas de resíduos por ano",
            "Apenas 4% do lixo brasileiro é reciclado. Você pode mudar isso!",
            "òleo de cozinha usado contamina até 1 milhão de litros de água",
            "Isopor não é reciclado pela maioria das cooperativas",
            "Agrotóxicos contaminam o solo por décadas",
            "Entulho descartado incorretamente causa enchentes",
            "Microplásticos já foram encontrados no sangue humano",
            "Desmatamento destruiu 20% da Amazonia original"
    };

    private int   tipIndex  = 0;
    private float tipTimer  = 0f;
    private static final float TIP_INTERVAL = 8f;

    private String attackModeLabel = "AUTO";

    // Score display pulse
    private long  lastScore     = 0L;
    private float scorePulse    = 0f;
    private float screenClearMsgTimer = 0f;

    public void setAttackMode(boolean manual) {
        attackModeLabel = manual ? "MANUAL" : "AUTO";
    }

    public void update(float dt) {
        tipTimer += dt;
        if (tipTimer >= TIP_INTERVAL) {
            tipTimer = 0f;
            tipIndex = (tipIndex + 1) % TIPS.length;
        }
        if (scorePulse > 0f) scorePulse -= dt * 3f;
        if (scorePulse < 0f) scorePulse = 0f;

        if (screenClearMsgTimer > 0f) screenClearMsgTimer -= dt;
        if (screenClearMsgTimer < 0f) screenClearMsgTimer = 0f;
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

        if (player.pollScreenClearTriggered()) {
            screenClearMsgTimer = 2.0f;
        }

        drawAttackTypeWidget(g2, player, statsY + 36);

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

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(160, 240, 160));
        String infoStr = DifficultySettings.label()
                + "  |  " + attackModeLabel;
        fm = g2.getFontMetrics();
        g2.drawString(infoStr,
                screenW - fm.stringWidth(infoStr) - 12, 18);

        // Score no canto superior direito
        long currentScore = ScoreManager.calcRunScore(
                player.getKills(),
                em.getHordaNumber(),
                player.getLevel());
        if (currentScore != lastScore) {
            scorePulse = 1f;
            lastScore  = currentScore;
        }
        drawScoreWidget(g2, screenW, currentScore);

        drawHordaProgress(g2, em, screenW);
        drawScreenClearMessage(g2, screenW);

        if (bossImminent) {
            long t        = System.currentTimeMillis();
            long interval = (long)(500 - bossTimerRatio * 300);
            if (interval < 50) interval = 50;
            if ((t / interval) % 2 == 0) {
                g2.setFont(new Font("Arial", Font.BOLD, 15));
                fm = g2.getFontMetrics();
                String warn = "!! BOSS SE APROXIMA !!";
                int wx = screenW / 2 - fm.stringWidth(warn) / 2;
                int wy = 98;
                g2.setColor(new Color(0, 0, 0, 160));
                g2.drawString(warn, wx + 1, wy + 1);
                g2.setColor(new Color(255, 70, 70));
                g2.drawString(warn, wx, wy);
            }
        }

        drawTicker(g2, screenW, screenH);
    }

    private void drawScoreWidget(Graphics2D g2, int screenW, long score) {
        String scoreStr = formatScore(score);
        String label    = "SCORE";

        float pulse = scorePulse;
        int   alpha = (int)(180 + pulse * 75);
        alpha = Math.min(255, alpha);
        float scale = 1f + pulse * 0.12f;

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fmL = g2.getFontMetrics();
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fmS = g2.getFontMetrics();

        int scoreW = fmS.stringWidth(scoreStr);
        int labelW = fmL.stringWidth(label);
        int boxW   = Math.max(scoreW, labelW) + 24;
        int boxH   = 42;
        int bx     = screenW - boxW - 10;
        int by     = 28;

        // fundo
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(bx, by, boxW, boxH, 10, 10);
        g2.setColor(new Color(255, 220, 50, (int)(pulse * 120)));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, boxW, boxH, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        // label "SCORE"
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        fmL = g2.getFontMetrics();
        g2.setColor(new Color(200, 200, 200, alpha));
        g2.drawString(label,
                bx + boxW / 2 - fmL.stringWidth(label) / 2,
                by + 15);

        // valor com pulse de cor
        Color scoreColor;
        if (pulse > 0.5f) {
            scoreColor = new Color(255, 255, 80, alpha);
        } else {
            scoreColor = new Color(255, 220, 50, alpha);
        }
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        fmS = g2.getFontMetrics();
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(scoreStr,
                bx + boxW / 2 - fmS.stringWidth(scoreStr) / 2 + 1,
                by + 36);
        g2.setColor(scoreColor);
        g2.drawString(scoreStr,
                bx + boxW / 2 - fmS.stringWidth(scoreStr) / 2,
                by + 35);
    }

    private String formatScore(long score) {
        if (score < 1_000L)       return String.valueOf(score);
        if (score < 1_000_000L)   return String.format("%,d", score)
                .replace(',', '.');
        return String.format("%.1fM", score / 1_000_000.0);
    }

    private void drawAttackTypeWidget(Graphics2D g2, Player player, int y) {
        boolean isArea = player.getAttackType() == AttackType.AREA;

        g2.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();

        String typeLabel = isArea ? "[AREA]" : "[TIRO]";
        Color  typeColor = isArea
                ? new Color(80, 255, 130)
                : new Color(100, 180, 255);

        int lw = fm.stringWidth(typeLabel) + 10;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(10, y - 13, lw, 17, 6, 6);

        g2.setColor(typeColor);
        g2.drawString(typeLabel, 15, y);

        if (isArea) {
            float coolRatio  = player.getAreaCooldownRatio();
            float readyRatio = 1f - coolRatio;
            int barW = 70;
            int barH = 5;
            int bx   = 15;
            int by   = y + 16;

            g2.setColor(new Color(20, 40, 20, 180));
            g2.fillRoundRect(bx, by, barW, barH, 3, 3);

            if (readyRatio > 0f) {
                Color barColor = readyRatio >= 1f
                        ? new Color(80, 255, 120)
                        : new Color(60, 180, 80);
                g2.setColor(barColor);
                g2.fillRoundRect(bx, by,
                        (int)(barW * readyRatio), barH, 3, 3);
            }

            g2.setColor(new Color(80, 160, 80, 180));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(bx, by, barW, barH, 3, 3);

            if (coolRatio <= 0f) {
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.setColor(new Color(120, 255, 120));
                g2.drawString("PRONTO", bx + barW + 4, by + barH);
            }
        }
    }


    private void drawScreenClearMessage(Graphics2D g2, int screenW) {
        if (screenClearMsgTimer <= 0f) return;

        float total = 2.0f;
        float t = screenClearMsgTimer / total;
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;

        float pulse = (float)Math.abs(Math.sin(System.currentTimeMillis() * 0.012));
        int alpha = (int)(140 + 115 * t);
        int y = 124;

        String text = "LIMPEZA DE TELA";
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        int x = screenW / 2 - fm.stringWidth(text) / 2;

        g2.setColor(new Color(0, 0, 0, Math.min(180, alpha)));
        g2.fillRoundRect(x - 18, y - 28, fm.stringWidth(text) + 36, 38, 12, 12);

        g2.setColor(new Color(255, 220, 80, (int)(110 + 90 * pulse)));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x - 18, y - 28, fm.stringWidth(text) + 36, 38, 12, 12);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(new Color(0, 0, 0, Math.min(170, alpha)));
        g2.drawString(text, x + 2, y + 2);

        g2.setColor(new Color(255, 240, 120, alpha));
        g2.drawString(text, x, y);
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

        float progress = tipTimer / TIP_INTERVAL;
        int alpha;
        if (progress < 0.1f)        alpha = (int)(progress / 0.1f * 255);
        else if (progress > 0.85f)  alpha = (int)((1f - (progress - 0.85f)
                / 0.15f) * 255);
        else                        alpha = 255;
        alpha = Math.max(0, Math.min(255, alpha));

        int textY = barY + barH - 7;

        g2.setColor(new Color(80, 180, 80, Math.min(alpha, 200)));
        g2.drawString(prefix, 4, textY);
        int prefixW = fm.stringWidth(prefix);
        g2.setColor(new Color(200, 255, 190, alpha));
        g2.drawString(tip, 4 + prefixW, textY);
    }

    private void drawHordaProgress(Graphics2D g2,
                                   EnemyManager em, int screenW) {
        int cx = screenW / 2;
        int y  = 62;

        if (em.isInBreak()) {
            String msg = "PROXIMA HORDA EM "
                    + (int) Math.ceil(em.getBreakTimer()) + "s...";
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
        g2.drawString(hordaStr,
                cx - fm.stringWidth(hordaStr) / 2 + 1, by - 1);
        g2.setColor(new Color(180, 255, 180));
        g2.drawString(hordaStr,
                cx - fm.stringWidth(hordaStr) / 2, by - 2);

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