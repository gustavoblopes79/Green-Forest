package com.greenforest;

public class GameLoop implements Runnable {

    private static final int TARGET_FPS   = 60;
    private static final long NANO_PER_FRAME =
            1_000_000_000L / TARGET_FPS;

    private final GamePanel panel;
    private volatile boolean running = false;
    private Thread thread;

    public GameLoop(GamePanel panel) {
        this.panel = panel;
    }

    public void start() {
        running = true;
        thread  = new Thread(this, "GameLoop");
        thread.setDaemon(true);
        thread.start();
    }

    public void stopLoop() {
        running = false;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (running) {
            long now     = System.nanoTime();
            long elapsed = now - lastTime;

            if (elapsed >= NANO_PER_FRAME) {
                lastTime = now;
                float dt = elapsed / 1_000_000_000f;

                // Limita dt para evitar saltos grandes se a janela
                // ficar em segundo plano por muito tempo
                if (dt > 0.05f) dt = 0.05f;

                panel.update(dt);
                panel.repaint();
            } else {
                // Dorme o tempo restante para nao consumir CPU
                long sleepMs = (NANO_PER_FRAME - elapsed) / 1_000_000L;
                if (sleepMs > 1) {
                    try {
                        Thread.sleep(sleepMs - 1);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}