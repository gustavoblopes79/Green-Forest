package com.greenforest.ui;

import com.greenforest.powerup.PowerUp;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PowerUpScreen {

    private static final int CARD_W = 200;
    private static final int CARD_H = 280;
    private static final int CARD_GAP = 30;
    private static final Color BG_COLOR =
            new Color(10, 5, 25, 210);

    private List<PowerUp> options;
    private int selected = 0;
    private PowerUp chosen = null;

    /** Define as 3 opções e reseta o estado. */
    public void setOptions(List<PowerUp> options) {
        this.options = options;
        this.selected = 0;
        this.chosen = null;
    }

    /** Retorna o power-up escolhido (null se ainda não escolheu). */
    public PowerUp getChosen() {
        return chosen;
    }

    public void moveLeft() {
        if (options == null) return;
        selected = (selected + options.size() - 1) % options.size();
    }

    public void moveRight() {
        if (options == null) return;
        selected = (selected + 1) % options.size();
    }

    public void confirm() {
        if (options != null) chosen = options.get(selected);
    }

    public void handleClick(int mx, int my, int screenW, int screenH) {
        if (options == null) return;
        int totalW = options.size() * CARD_W +
                (options.size() - 1) * CARD_GAP;
        int startX = (screenW - totalW) / 2;
        int cardY = screenH / 2 - CARD_H / 2;

        for (int i = 0; i < options.size(); i++) {
            int cx = startX + i * (CARD_W + CARD_GAP);
            Rectangle r = new Rectangle(cx, cardY, CARD_W, CARD_H);
            if (r.contains(mx, my)) {
                if (selected == i) {
                    confirm();
                } else {
                    selected = i;
                }
                return;
            }
        }
    }

    public void draw(Graphics2D g, int screenW, int screenH, long tick) {
        if (options == null) return;

        // Fundo escurecido
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, screenW, screenH);

        // Título
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        String title = "LEVEL UP!  Escolha um poder:";
        FontMetrics fm = g.getFontMetrics();
        int tx = (screenW - fm.stringWidth(title)) / 2;

        // sombra
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(title, tx + 2, screenH / 2 - CARD_H / 2 - 38);
        // texto dourado
        g.setColor(new Color(255, 220, 60));
        g.drawString(title, tx, screenH / 2 - CARD_H / 2 - 40);

        // Cards
        int totalW = options.size() * CARD_W +
                (options.size() - 1) * CARD_GAP;
        int startX = (screenW - totalW) / 2;
        int cardY = screenH / 2 - CARD_H / 2;

        for (int i = 0; i < options.size(); i++) {
            drawCard(g, options.get(i), i == selected,
                    startX + i * (CARD_W + CARD_GAP), cardY, tick);
        }

        // Dica
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        String hint = "Setas: navegar   ENTER / Clique: selecionar";
        fm = g.getFontMetrics();
        g.setColor(new Color(180, 180, 180, 200));
        g.drawString(hint,
                (screenW - fm.stringWidth(hint)) / 2,
                screenH / 2 + CARD_H / 2 + 36);
    }

    private void drawCard(Graphics2D g, PowerUp pu, boolean sel,
                          int x, int y, long tick) {
        Color base = pu.color;

        // Sombra do card
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(x + 5, y + 5, CARD_W, CARD_H, 20, 20);

        // Fundo do card
        GradientPaint grad = new GradientPaint(
                x, y, new Color(20, 15, 40),
                x, y + CARD_H, new Color(35, 25, 60)
        );
        g.setPaint(grad);
        g.fillRoundRect(x, y, CARD_W, CARD_H, 20, 20);

        // Borda
        float borderW = sel ? 3f : 1.5f;
        g.setStroke(new BasicStroke(borderW));
        if (sel) {
            // brilho pulsante
            float pulse = 0.7f + 0.3f *
                    (float) Math.sin(tick * 0.08);
            g.setColor(new Color(
                    (int) (base.getRed() * pulse),
                    (int) (base.getGreen() * pulse),
                    (int) (base.getBlue() * pulse)
            ));
        } else {
            g.setColor(base.darker().darker());
        }
        g.drawRoundRect(x, y, CARD_W, CARD_H, 20, 20);

        // Ícone
        int iconSize = 80;
        int iconCX = x + CARD_W / 2;
        int iconCY = y + 30 + iconSize / 2;

        // Fundo circular do ícone
        if (sel) {
            g.setColor(new Color(base.getRed(), base.getGreen(),
                    base.getBlue(), 40));
            g.fillOval(iconCX - iconSize / 2, iconCY - iconSize / 2,
                    iconSize, iconSize);
        }
        pu.drawIcon(g, iconCX, iconCY, iconSize);

        // Nome
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        String nameStr = pu.name;
        int nx = x + (CARD_W - fm.stringWidth(nameStr)) / 2;
        int ny = y + 30 + iconSize + 24;

        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(nameStr, nx + 1, ny + 1);
        g.setColor(sel ? Color.WHITE : new Color(200, 200, 200));
        g.drawString(nameStr, nx, ny);

        // Separador
        g.setColor(new Color(base.getRed(), base.getGreen(),
                base.getBlue(), sel ? 180 : 80));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x + 20, ny + 10, x + CARD_W - 20, ny + 10);

        // Descrição (quebra de linha manual)
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        fm = g.getFontMetrics();
        String desc = pu.description;
        // Quebra em ~22 chars
        String[] lines = wrapText(desc, 22);
        int lineH = fm.getHeight();
        int descY = ny + 26;
        for (String line : lines) {
            int lx = x + (CARD_W - fm.stringWidth(line)) / 2;
            g.setColor(new Color(0, 0, 0, 120));
            g.drawString(line, lx + 1, descY + 1);
            g.setColor(new Color(180, 200, 180));
            g.drawString(line, lx, descY);
            descY += lineH;
        }

        // Indicador de selecionado
        if (sel) {
            g.setFont(new Font("Arial", Font.BOLD, 13));
            String pick = "[ ENTER para escolher ]";
            fm = g.getFontMetrics();
            g.setColor(new Color(base.getRed(), base.getGreen(),
                    base.getBlue(), 200));
            g.drawString(pick,
                    x + (CARD_W - fm.stringWidth(pick)) / 2,
                    y + CARD_H - 14);
        }
    }

    private String[] wrapText(String text, int maxChars) {
        if (text.length() <= maxChars) return new String[]{text};
        // tenta quebrar no último espaço antes de maxChars
        int breakAt = text.lastIndexOf(' ', maxChars);
        if (breakAt <= 0) breakAt = maxChars;
        String first = text.substring(0, breakAt).trim();
        String rest = text.substring(breakAt).trim();
        String[] restWrapped = wrapText(rest, maxChars);
        String[] result = new String[1 + restWrapped.length];
        result[0] = first;
        System.arraycopy(restWrapped, 0, result, 1, restWrapped.length);
        return result;
    }
}