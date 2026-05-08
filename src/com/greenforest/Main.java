package com.greenforest;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            int width  = 1280;
            int height = 720;

            JFrame frame = new JFrame("greenforest_");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel(width, height);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            panel.requestFocusInWindow();

            GameLoop loop = new GameLoop(panel);
            loop.start();
        });
    }
}