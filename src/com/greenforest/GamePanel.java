package com.greenforest;

import com.greenforest.entity.Boss;
import com.greenforest.entity.Enemy;
import com.greenforest.entity.Player;
import com.greenforest.manager.BossManager;
import com.greenforest.manager.EnemyManager;
import com.greenforest.manager.ProjectileManager;
import com.greenforest.powerup.PowerUp;
import com.greenforest.ui.EducationalOverlay;
import com.greenforest.ui.HUD;
import com.greenforest.ui.PowerUpScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GamePanel extends JPanel
        implements KeyListener, MouseListener, MouseMotionListener {

    // World
    private static final int WORLD_W = 2400;
    private static final int WORLD_H = 1800;

    private static final int HORIZON_Y = WORLD_H - 300;

    // Screen
    private final int screenW, screenH;

    // Game state
    private GameState state = GameState.TITLE;

    // Input
    private final Set<Integer> keysDown = new HashSet<>();
    private int mouseX = 0, mouseY = 0;
    private boolean manualMode = false;

    private int titlePhase    = 0;
    private int diffSelection = 1;
    private int modeSelection = 0;
    private int continueSelection = 0;

    private int pauseOption = 0;

    // Entities & Managers
    private Player            player;
    private EnemyManager      enemyManager;
    private ProjectileManager projectileManager;
    private BossManager       bossManager;
    private HUD               hud;
    private EducationalOverlay overlay;
    private PowerUpScreen     powerUpScreen;

    private final Random rng = new Random();
    private long tickCount = 0;

    // Camera
    private int camX, camY;

    // Timing
    private float gameTimeSec = 0f;

    // Boss lesson
    private String pendingBossLesson = null;
    private String pendingBossName   = null;

    // Game over lesson
    private String gameOverLesson = "";

    // Crosswalk animation
    private float crosswalkTimer = 0f;
    private boolean crosswalkWhite = true;

    // Lamppost blink
    private float lampostTimer = 0f;
    private float lampostGlow  = 1.0f;

    // Predios proximos (paralaxe 0.75)
    private static final int BUILDING_COUNT = 55;
    private final int[][] buildings = new int[BUILDING_COUNT][7];

    // Predios fundo (paralaxe 0.35)
    private static final int BG_BUILDING_COUNT = 40;
    private final int[][] bgBuildings = new int[BG_BUILDING_COUNT][5];

    // Elementos do chao
    private static final int STREET_ELEM_COUNT = 80;
    private final int[][] streetElems = new int[STREET_ELEM_COUNT][2];

    // Paletas
    private static final Color[] BUILDING_COLORS = {
            new Color(55,  65,  80),
            new Color(70,  58,  50),
            new Color(45,  70,  55),
            new Color(80,  75,  60),
            new Color(50,  50,  70),
    };

    private static final Color[] BG_BUILDING_COLORS = {
            new Color(30, 38, 52),
            new Color(42, 36, 32),
            new Color(28, 42, 35),
            new Color(48, 45, 38),
    };

    public GamePanel(int w, int h) {
        this.screenW = w;
        this.screenH = h;
        setPreferredSize(new Dimension(w, h));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setBackground(Color.BLACK);

        overlay       = new EducationalOverlay();
        hud           = new HUD();
        powerUpScreen = new PowerUpScreen();

        generateBuildings();
        generateBgBuildings();
        generateStreetElements();

        if (SaveData.hasSave()) titlePhase = 2;
    }

    private long lcg(long s) {
        return Math.abs(s * 6364136223846793005L + 1442695040888963407L);
    }

    private void generateBuildings() {
        long seed = 0xDA_A1AAL;
        int slotW = WORLD_W / BUILDING_COUNT;
        for (int i = 0; i < BUILDING_COUNT; i++) {
            seed = lcg(seed);
            int bx = i * slotW + (int)(seed % slotW);
            seed = lcg(seed);
            int bw = 90 + (int)(seed % 150);
            seed = lcg(seed);
            int bh = 130 + (int)(seed % 270);
            seed = lcg(seed);
            int colorIdx     = (int)(seed % BUILDING_COLORS.length);
            seed = lcg(seed);
            int hasAntenna   = (seed % 3 == 0) ? 1 : 0;
            seed = lcg(seed);
            int hasWaterTank = (seed % 4 == 0) ? 1 : 0;
            seed = lcg(seed);
            int windowSeed   = (int)(seed & 0xFFFF);

            buildings[i][0] = bx;
            buildings[i][1] = bw;
            buildings[i][2] = bh;
            buildings[i][3] = colorIdx;
            buildings[i][4] = hasAntenna;
            buildings[i][5] = hasWaterTank;
            buildings[i][6] = windowSeed;
        }
    }

    private void generateBgBuildings() {
        long seed = 0xBEEFCAFEL;
        int slotW = WORLD_W / BG_BUILDING_COUNT;
        for (int i = 0; i < BG_BUILDING_COUNT; i++) {
            seed = lcg(seed);
            int bx = i * slotW + (int)(seed % slotW);
            seed = lcg(seed);
            int bw = 60 + (int)(seed % 120);
            seed = lcg(seed);
            int bh = 60 + (int)(seed % 140);
            seed = lcg(seed);
            int colorIdx   = (int)(seed % BG_BUILDING_COLORS.length);
            seed = lcg(seed);
            int windowSeed = (int)(seed & 0xFFFF);

            bgBuildings[i][0] = bx;
            bgBuildings[i][1] = bw;
            bgBuildings[i][2] = bh;
            bgBuildings[i][3] = colorIdx;
            bgBuildings[i][4] = windowSeed;
        }
    }

    private void generateStreetElements() {
        long seed = 0xC0FFEEL;
        int slotW = WORLD_W / STREET_ELEM_COUNT;
        for (int i = 0; i < STREET_ELEM_COUNT; i++) {
            seed = lcg(seed);
            int ex   = i * slotW + (int)(seed % slotW);
            seed = lcg(seed);
            int tipo = (int)(seed % 6);
            streetElems[i][0] = ex;
            streetElems[i][1] = tipo;
        }
    }

    private void startGame() {
        player            = new Player(WORLD_W / 2f, WORLD_H / 2f);
        enemyManager      = new EnemyManager();
        projectileManager = new ProjectileManager();
        bossManager       = new BossManager();
        powerUpScreen     = new PowerUpScreen();
        gameTimeSec       = 0f;
        tickCount         = 0;
        manualMode        = modeSelection == 1;

        hud.setAttackMode(manualMode);
        enemyManager.setOnHordaChange(this::onHordaChange);

        state = GameState.PLAYING;
    }

    private void continueGame() {
        SaveData d = SaveData.load();
        if (d == null) { startGame(); return; }

        // Restaura dificuldade
        DifficultySettings.set(switch (d.diffOrdinal) {
            case 0  -> DifficultySettings.Difficulty.EASY;
            case 2  -> DifficultySettings.Difficulty.HARD;
            default -> DifficultySettings.Difficulty.NORMAL;
        });

        player            = new Player(WORLD_W / 2f, WORLD_H / 2f);
        enemyManager      = new EnemyManager();
        projectileManager = new ProjectileManager();
        bossManager       = new BossManager();
        powerUpScreen     = new PowerUpScreen();
        tickCount         = 0;
        manualMode        = modeSelection == 1;

        player.loadFromSave(d);
        gameTimeSec = d.gameTimeSec;

        // Avanca o EnemyManager ate a horda salva
        for (int i = 1; i < d.hordaNumber; i++) enemyManager.advanceHordaPublic();

        hud.setAttackMode(manualMode);
        enemyManager.setOnHordaChange(this::onHordaChange);

        state = GameState.PLAYING;
    }

    /** Salva o jogo atual. Retorna true se bem-sucedido. */
    private boolean saveGame() {
        if (player == null) return false;
        int diffOrdinal = switch (DifficultySettings.current()) {
            case EASY  -> 0;
            case HARD  -> 2;
            default    -> 1;
        };
        SaveData d = player.toSaveData(
                gameTimeSec,
                enemyManager.getHordaNumber(),
                diffOrdinal);
        return d.save();
    }

    private void onHordaChange(int type) {
        String msg = switch (type) {
            case 0 -> "SACOLAS PLASTICAS\nLevam ate 400 anos para se decompor!";
            case 1 -> "LATAS DE ALUMINIO\nReciclavel infinitas vezes -- separe as suas!";
            case 2 -> "PNEUS\nLevam 600 anos para se decompor.\n"
                    + "Acumulam agua parada -- risco de dengue!";
            case 3 -> "NUVEM TOXICA\nPoluicao do ar causa doencas respiratorias.\n"
                    + "Ande de bike ou a pe quando puder!";
            case 4 -> "CIGARROS\nO filtro contem microplasticos.\n"
                    + "Sao o item mais catado nas limpezas de praia!";
            default -> null;
        };
        if (msg != null) overlay.showHordaMessage(msg);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public void update(float dt) {
        tickCount++;
        switch (state) {
            case PLAYING     -> updatePlaying(dt);
            case POWER_UP    -> updatePowerUp();
            case PAUSED      -> {}
            case BOSS_LESSON -> {}
            case GAME_OVER   -> {}
            case TITLE       -> {}
        }
    }

    private void updatePlaying(float dt) {
        gameTimeSec += dt;

        crosswalkTimer += dt;
        if (crosswalkTimer > 0.6f) {
            crosswalkTimer = 0f;
            crosswalkWhite = !crosswalkWhite;
        }

        lampostTimer += dt;
        lampostGlow = 0.85f + 0.15f * (float) Math.sin(lampostTimer * 3.0);

        // Movimento
        float baseSpeed = 2.8f;
        float speed     = baseSpeed * player.getSpeedMult();
        float dx = 0, dy = 0;
        if (keysDown.contains(KeyEvent.VK_W) ||
                keysDown.contains(KeyEvent.VK_UP))    dy -= 1;
        if (keysDown.contains(KeyEvent.VK_S) ||
                keysDown.contains(KeyEvent.VK_DOWN))  dy += 1;
        if (keysDown.contains(KeyEvent.VK_A) ||
                keysDown.contains(KeyEvent.VK_LEFT))  dx -= 1;
        if (keysDown.contains(KeyEvent.VK_D) ||
                keysDown.contains(KeyEvent.VK_RIGHT)) dx += 1;

        if (dx != 0 && dy != 0) { dx *= 0.7071f; dy *= 0.7071f; }
        player.move(dx * speed, dy * speed, WORLD_W, WORLD_H);
        player.update(dt, 0, 0);

        // Camera
        camX = (int)(player.getX() - screenW / 2f);
        camY = (int)(player.getY() - screenH / 2f);
        camX = Math.max(0, Math.min(WORLD_W - screenW, camX));
        camY = Math.max(0, Math.min(WORLD_H - screenH, camY));

        // Aim
        float worldMouseX = mouseX + camX;
        float worldMouseY = mouseY + camY;
        player.setAimDirection(
                worldMouseX - player.getX(),
                worldMouseY - player.getY());

        // Projeteis
        if (manualMode) {
            projectileManager.updateManual(dt);
        } else {
            projectileManager.update(dt, player, enemyManager.getEnemies());
        }
        projectileManager.checkCollisions(enemyManager.getEnemies(), player);

        enemyManager.update(dt, player.getX(), player.getY(), gameTimeSec, player);

        // Level up pendente
        // Level up pendente
        if (player.isLevelUpPending()) {
            player.clearLevelUpPending();
            powerUpScreen.setOptions(PowerUp.getRandomThree(rng));
            state = GameState.POWER_UP;
            return;
        }

        // Boss
        bossManager.update(dt, player.getX(), player.getY(), WORLD_W, WORLD_H);

        if (bossManager.hasBoss()) {
            Boss boss = bossManager.getActiveBoss();
            projectileManager.checkCollisionsWithBoss(boss, player);

            float bdx   = boss.getX() - player.getX();
            float bdy   = boss.getY() - player.getY();
            float bDist = (float) Math.sqrt(bdx * bdx + bdy * bdy);
            if (bDist < boss.getSize() / 2f + player.getSize() / 2f)
                player.takeDamage(boss.getDamage() / 60);

            if (boss.hitsWithSpecial(player.getX(), player.getY()))
                player.takeDamage(boss.getSpecialDamage());
        }

        // Dano de inimigos normais
        for (Enemy e : enemyManager.getEnemies()) {
            if (e.isDead()) continue;
            float ex   = e.getX() - player.getX();
            float ey   = e.getY() - player.getY();
            float dist = (float) Math.sqrt(ex * ex + ey * ey);
            if (dist < e.getSize() / 2f + player.getSize() / 2f)
                player.takeDamage(e.getDamage());
        }

        // Boss morreu
        Boss deadBoss = bossManager.pollDeadBoss();
        if (deadBoss != null) {
            player.gainXP(deadBoss.getXpValue() * 3);
            player.addKill();
            pendingBossLesson = deadBoss.getLesson();
            pendingBossName   = switch (deadBoss.getBossType()) {
                case E_WASTE       -> "LIXO ELETRONICO";
                case FACTORY       -> "FABRICA POLUENTE";
                case GARBAGE_TRUCK -> "CAMINHAO DE LIXO";
            };
            state = GameState.BOSS_LESSON;
        }

        // Jogador morreu — deleta o save
        if (player.isDead()) {
            SaveData.deleteSave();
            gameOverLesson = overlay.randomGameOverLesson();
            state = GameState.GAME_OVER;
        }

        hud.update(dt);
        overlay.update(dt);
    }

    private void updatePowerUp() {
        PowerUp chosen = powerUpScreen.getChosen();
        if (chosen != null) {
            player.applyPowerUp(chosen);
            state = GameState.PLAYING;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        switch (state) {
            case TITLE       -> drawTitle(g2);
            case PLAYING     -> drawPlaying(g2);
            case PAUSED      -> { drawPlaying(g2); drawPause(g2); }
            case POWER_UP    -> {
                drawPlaying(g2);
                powerUpScreen.draw(g2, screenW, screenH, tickCount);
            }
            case BOSS_LESSON -> { drawPlaying(g2); drawBossLesson(g2); }
            case GAME_OVER   -> drawGameOver(g2);
        }
    }

    private void drawPlaying(Graphics2D g2) {
        drawBackground(g2);
        enemyManager.draw(g2, camX, camY);
        bossManager.draw(g2, camX, camY);
        projectileManager.draw(g2, camX, camY);
        player.draw(g2, camX, camY);
        hud.draw(g2, screenW, screenH, player, enemyManager,
                gameTimeSec,
                bossManager.isBossImminent(), bossManager.getTimerRatio());
        overlay.drawHordaMessage(g2, screenW, screenH);

        if (manualMode) {
            g2.setColor(new Color(255, 255, 80, 200));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(mouseX - 12, mouseY, mouseX + 12, mouseY);
            g2.drawLine(mouseX, mouseY - 12, mouseX, mouseY + 12);
            g2.drawOval(mouseX - 8, mouseY - 8, 16, 16);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    private void drawBackground(Graphics2D g2) {
        drawSky(g2);
        drawBgBuildings(g2);
        drawBuildings(g2);
        drawGround(g2);
        drawStreetElements(g2);
    }

    private void drawSky(Graphics2D g2) {
        int horizonScreenY = HORIZON_Y - camY;
        int skyBottom = Math.min(horizonScreenY, screenH);
        if (skyBottom <= 0) return;

        GradientPaint skyGrad = new GradientPaint(
                0, 0,         new Color(8, 10, 22),
                0, skyBottom, new Color(35, 28, 55)
        );
        g2.setPaint(skyGrad);
        g2.fillRect(0, 0, screenW, skyBottom);
        g2.setPaint(null);

        long s = 0xABCDEFL;
        for (int i = 0; i < 90; i++) {
            s = lcg(s);
            int sx = (int)(s % screenW);
            s = lcg(s);
            int sy = (int)(s % Math.max(1, skyBottom - 30));
            s = lcg(s);
            int bright = 120 + (int)(s % 135);
            g2.setColor(new Color(bright, bright,
                    Math.min(255, bright + 30), 190));
            int sz = (i % 12 == 0) ? 2 : 1;
            g2.fillRect(sx, sy, sz, sz);
        }

        drawMoon(g2, skyBottom);
    }

    private void drawMoon(Graphics2D g2, int skyBottom) {
        if (skyBottom < 60) return;
        int mx = screenW - 90;
        int my = 55;
        g2.setColor(new Color(220, 215, 180, 18));
        g2.fillOval(mx - 22, my - 22, 76, 76);
        g2.setColor(new Color(220, 215, 180, 10));
        g2.fillOval(mx - 30, my - 30, 92, 92);
        g2.setColor(new Color(240, 235, 200));
        g2.fillOval(mx, my, 32, 32);
        g2.setColor(new Color(8, 10, 22));
        g2.fillOval(mx + 8, my - 4, 30, 30);
    }

    private void drawBgBuildings(Graphics2D g2) {
        int horizonScreenY = HORIZON_Y - camY;
        if (horizonScreenY <= 0) return;

        for (int[] b : bgBuildings) {
            int bxWorld    = b[0];
            int bw         = b[1];
            int bh         = b[2];
            int colorIdx   = b[3];
            int windowSeed = b[4];

            int screenX = (int)(bxWorld - camX * 0.35f);
            int screenY = horizonScreenY - bh;

            if (screenX + bw < 0 || screenX > screenW) continue;
            if (screenY + bh <= 0) continue;

            Color base = BG_BUILDING_COLORS[colorIdx];
            g2.setColor(base);
            g2.fillRect(screenX, screenY, bw, bh);
            drawBgBuildingWindows(g2, screenX, screenY, bw, bh, windowSeed);
        }
    }

    private void drawBgBuildingWindows(Graphics2D g2,
                                       int bx, int by, int bw, int bh,
                                       int seed) {
        int cols   = 2;
        int winW   = 7;
        int winH   = 9;
        int padX   = (bw - cols * winW) / (cols + 1);
        if (padX < 3) return;
        int gapY   = 16;
        int padTop = 12;
        int row    = 0;
        for (int y = by + padTop; y + winH < by + bh - 6; y += gapY) {
            for (int c = 0; c < cols; c++) {
                int wx  = bx + padX + c * (winW + padX);
                int bit = (seed >> ((row * cols + c) % 16)) & 1;
                if (bit == 1) {
                    g2.setColor(new Color(180, 160, 80, 120));
                    g2.fillRect(wx, y, winW, winH);
                } else {
                    g2.setColor(new Color(20, 22, 30, 100));
                    g2.fillRect(wx, y, winW, winH);
                }
            }
            row++;
        }
    }

    private void drawBuildings(Graphics2D g2) {
        int horizonScreenY = HORIZON_Y - camY;
        if (horizonScreenY <= 0) return;

        for (int[] b : buildings) {
            int bxWorld      = b[0];
            int bw           = b[1];
            int bh           = b[2];
            int colorIdx     = b[3];
            int hasAntenna   = b[4];
            int hasWaterTank = b[5];
            int windowSeed   = b[6];

            int screenX = (int)(bxWorld - camX * 0.75f);
            int screenY = horizonScreenY - bh;

            if (screenX + bw < 0 || screenX > screenW) continue;
            if (screenY + bh <= 0) continue;

            Color base = BUILDING_COLORS[colorIdx];

            g2.setColor(base);
            g2.fillRect(screenX, screenY, bw, bh);

            GradientPaint shadow = new GradientPaint(
                    screenX,      screenY, new Color(0, 0, 0, 0),
                    screenX + bw, screenY, new Color(0, 0, 0, 70)
            );
            g2.setPaint(shadow);
            g2.fillRect(screenX, screenY, bw, bh);
            g2.setPaint(null);

            g2.setColor(base.brighter());
            g2.drawLine(screenX, screenY, screenX + bw, screenY);

            drawStorefront(g2, screenX, screenY, bw, bh, windowSeed);
            drawBuildingWindows(g2, screenX, screenY, bw, bh - 30, windowSeed);

            if (hasAntenna == 1) {
                int ax = screenX + bw / 2;
                int ay = screenY;
                g2.setColor(new Color(160, 162, 170));
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(ax, ay, ax, ay - 32);
                g2.setStroke(new BasicStroke(1f));
                int glowA = (int)(lampostGlow * 200);
                g2.setColor(new Color(255, 80, 80, glowA));
                g2.fillOval(ax - 3, ay - 35, 6, 6);
            }

            if (hasWaterTank == 1) {
                int tx = screenX + bw / 2 - 12;
                int ty = screenY - 22;
                g2.setColor(new Color(90, 70, 50));
                g2.fillRect(tx, ty, 24, 16);
                g2.setColor(new Color(70, 52, 35));
                g2.fillRect(tx + 2,  ty + 14, 4, 8);
                g2.fillRect(tx + 18, ty + 14, 4, 8);
                g2.setColor(new Color(110, 90, 70));
                g2.fillRect(tx - 2, ty - 3, 28, 5);
            }
        }
    }

    private void drawStorefront(Graphics2D g2,
                                int bx, int by, int bw, int bh,
                                int seed) {
        int ty = by + bh - 30;
        int th = 30;

        g2.setColor(new Color(60, 58, 55));
        g2.fillRect(bx, ty, bw, th);

        int vw = Math.min(bw - 16, 50);
        int vx = bx + (bw - vw) / 2;
        g2.setColor(new Color(140, 200, 220, 80));
        g2.fillRect(vx, ty + 4, vw, th - 8);
        g2.setColor(new Color(180, 220, 240, 140));
        g2.drawRect(vx, ty + 4, vw, th - 8);

        Color[] awningColors = {
                new Color(180, 50,  50),
                new Color(50,  100, 180),
                new Color(50,  150, 60),
                new Color(160, 120, 40),
        };
        Color awning = awningColors[seed % awningColors.length];
        g2.setColor(awning);
        g2.fillRect(vx - 4, ty, vw + 8, 8);
        g2.setColor(new Color(255, 255, 255, 60));
        for (int sx = vx - 4; sx < vx + vw + 8; sx += 8)
            g2.fillRect(sx, ty, 4, 8);
    }

    private void drawBuildingWindows(Graphics2D g2,
                                     int bx, int by, int bw, int bh,
                                     int seed) {
        int cols   = 3;
        int winW   = 10;
        int winH   = 12;
        int padX   = (bw - cols * winW) / (cols + 1);
        int padTop = 14;
        int gapY   = 20;

        if (padX < 4) return;

        int row = 0;
        for (int y = by + padTop; y + winH < by + bh - 6; y += gapY) {
            for (int c = 0; c < cols; c++) {
                int wx  = bx + padX + c * (winW + padX);
                int bit = (seed >> ((row * cols + c) % 16)) & 1;
                if (bit == 1) {
                    g2.setColor(new Color(255, 230, 120, 200));
                    g2.fillRect(wx, y, winW, winH);
                    g2.setColor(new Color(255, 255, 180, 55));
                    g2.fillRect(wx - 1, y - 1, winW + 2, winH + 2);
                } else {
                    g2.setColor(new Color(28, 32, 42, 190));
                    g2.fillRect(wx, y, winW, winH);
                    g2.setColor(new Color(48, 52, 62, 110));
                    g2.drawRect(wx, y, winW, winH);
                }
            }
            row++;
        }
    }

    private void drawGround(Graphics2D g2) {
        int groundScreenY = HORIZON_Y - camY;
        if (groundScreenY > screenH) return;
        if (groundScreenY < 0) groundScreenY = 0;
        int groundH = screenH - groundScreenY;

        // Asfalto
        GradientPaint asphalt = new GradientPaint(
                0, groundScreenY,           new Color(38, 40, 43),
                0, groundScreenY + groundH, new Color(28, 30, 33)
        );
        g2.setPaint(asphalt);
        g2.fillRect(0, groundScreenY, screenW, groundH);
        g2.setPaint(null);

        int sidewalkH = 55;

        // Calcada superior
        g2.setColor(new Color(105, 103, 96));
        g2.fillRect(0, groundScreenY, screenW, sidewalkH);

        // Calcada inferior
        int lowerSidewalkY = groundScreenY + groundH - sidewalkH;
        if (lowerSidewalkY > groundScreenY + sidewalkH)
            g2.fillRect(0, lowerSidewalkY, screenW, sidewalkH);

        // Ladrilhos
        drawSidewalkTiles(g2, groundScreenY, sidewalkH);
        if (lowerSidewalkY > groundScreenY + sidewalkH)
            drawSidewalkTiles(g2, lowerSidewalkY, sidewalkH);

        // Faixa central tracejada
        int roadCenterY = groundScreenY + groundH / 2;
        drawDashedLine(g2, roadCenterY);

        // Faixas de pedestres — apenas na pista (entre as calcadas)
        int roadTop    = groundScreenY + sidewalkH;
        int roadBottom = lowerSidewalkY > groundScreenY + sidewalkH
                ? lowerSidewalkY
                : groundScreenY + groundH;
        int roadHeight = roadBottom - roadTop;
        if (roadHeight > 0)
            drawCrosswalks(g2, roadTop, roadHeight);

        // Bordas da calcada
        g2.setColor(new Color(18, 18, 18, 170));
        g2.fillRect(0, groundScreenY + sidewalkH - 3, screenW, 3);
        if (lowerSidewalkY > groundScreenY + sidewalkH)
            g2.fillRect(0, lowerSidewalkY, screenW, 3);
    }

    private void drawSidewalkTiles(Graphics2D g2, int y, int h) {
        int tileW = 40;
        int tileH = 20;
        g2.setColor(new Color(88, 86, 80, 115));
        g2.setStroke(new BasicStroke(0.5f));
        int offX = camX % tileW;
        int offY = camY % tileH;
        for (int tx = -offX; tx < screenW + tileW; tx += tileW)
            g2.drawLine(tx, y, tx, y + h);
        for (int ty = y - offY; ty < y + h + tileH; ty += tileH)
            g2.drawLine(0, ty, screenW, ty);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawDashedLine(Graphics2D g2, int y) {
        int dashLen = 40;
        int gapLen  = 30;
        int offX    = camX % (dashLen + gapLen);
        g2.setColor(new Color(215, 195, 55, 175));
        g2.setStroke(new BasicStroke(3f));
        for (int x = -offX; x < screenW + dashLen; x += dashLen + gapLen)
            g2.drawLine(x, y, x + dashLen, y);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawCrosswalks(Graphics2D g2, int roadTop, int roadHeight) {
        int spacing  = 400;
        int cwWidth  = 80;
        int stripeW  = 12;
        int stripeGap = 8;
        int offX     = camX % spacing;

        Color stripeColor = crosswalkWhite
                ? new Color(228, 228, 218, 210)
                : new Color(198, 198, 188, 175);

        Shape oldClip = g2.getClip();
        g2.setClip(0, roadTop, screenW, roadHeight);

        for (int wx = -offX; wx < screenW + spacing; wx += spacing) {
            int cx = wx + spacing / 2 - cwWidth / 2;
            for (int sx = cx; sx < cx + cwWidth; sx += stripeW + stripeGap) {
                g2.setColor(stripeColor);
                g2.fillRect(sx, roadTop, stripeW, roadHeight);
            }
        }

        g2.setClip(oldClip);
    }

    private void drawStreetElements(Graphics2D g2) {
        int groundScreenY = HORIZON_Y - camY;
        if (groundScreenY > screenH) return;

        for (int[] elem : streetElems) {
            int worldX = elem[0];
            int tipo   = elem[1];

            int screenX = worldX - camX;
            if (screenX < -80 || screenX > screenW + 80) continue;

            switch (tipo) {
                case 0 -> drawLamppost(g2, screenX, groundScreenY);
                case 1 -> drawTrashCan(g2, screenX, groundScreenY);
                case 2 -> drawHydrant(g2, screenX, groundScreenY);
                case 3 -> drawNewsstand(g2, screenX, groundScreenY);
                case 4 -> drawUrbanTree(g2, screenX, groundScreenY);
                case 5 -> drawManhole(g2, screenX, groundScreenY);
            }
        }
    }

    private void drawLamppost(Graphics2D g2, int x, int groundY) {
        int baseY = groundY + 48;
        int haloA = (int)(lampostGlow * 35);
        g2.setColor(new Color(255, 230, 120, haloA));
        g2.fillOval(x - 30, groundY + 44, 60, 16);
        g2.setColor(new Color(130, 132, 138));
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(x, baseY, x, baseY - 70);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(120, 122, 128));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(x, baseY - 70, x + 14, baseY - 70);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(80, 82, 88));
        g2.fillRect(x + 6, baseY - 75, 16, 8);
        int glowA = (int)(lampostGlow * 230);
        g2.setColor(new Color(255, 240, 160, glowA));
        g2.fillRect(x + 8, baseY - 73, 12, 5);
        g2.setColor(new Color(255, 230, 120, (int)(lampostGlow * 45)));
        g2.fillOval(x + 2, baseY - 82, 28, 22);
        g2.setColor(new Color(100, 102, 108));
        g2.fillRect(x - 4, baseY - 4, 8, 8);
    }

    private void drawTrashCan(Graphics2D g2, int x, int groundY) {
        int by = groundY + 30;
        g2.setColor(new Color(40, 100, 50));
        g2.fillRoundRect(x - 10, by, 20, 24, 4, 4);
        g2.setColor(new Color(30, 75, 38));
        g2.fillRect(x - 11, by, 22, 5);
        g2.setColor(new Color(50, 120, 60));
        g2.fillRoundRect(x - 12, by - 5, 24, 7, 3, 3);
        g2.setColor(new Color(180, 230, 180, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x - 5, by + 7, 10, 10);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(60, 60, 65));
        g2.fillRect(x - 3, by + 24, 6, 6);
    }

    private void drawHydrant(Graphics2D g2, int x, int groundY) {
        int by = groundY + 36;
        g2.setColor(new Color(180, 40, 40));
        g2.fillRoundRect(x - 7, by, 14, 18, 4, 4);
        g2.setColor(new Color(200, 50, 50));
        g2.fillOval(x - 7, by - 6, 14, 12);
        g2.setColor(new Color(160, 30, 30));
        g2.fillOval(x - 4, by - 3, 8, 6);
        g2.setColor(new Color(160, 35, 35));
        g2.fillRect(x - 12, by + 4, 5, 5);
        g2.fillRect(x + 7,  by + 4, 5, 5);
        g2.setColor(new Color(120, 120, 80));
        g2.fillOval(x - 11, by + 5, 3, 3);
        g2.fillOval(x + 8,  by + 5, 3, 3);
        g2.setColor(new Color(140, 30, 30));
        g2.fillRect(x - 9, by + 16, 18, 4);
    }

    private void drawNewsstand(Graphics2D g2, int x, int groundY) {
        int by = groundY + 10;
        g2.setColor(new Color(60, 80, 110));
        g2.fillRect(x - 20, by, 40, 44);
        int[] txs = {x - 24, x + 24, x + 20, x - 20};
        int[] tys = {by - 2,  by - 2,  by,     by};
        g2.setColor(new Color(40, 60, 90));
        g2.fillPolygon(txs, tys, 4);
        g2.setColor(new Color(80, 110, 150));
        g2.drawLine(x - 24, by - 2, x + 24, by - 2);
        g2.setColor(new Color(220, 215, 200, 180));
        g2.fillRect(x - 16, by + 6, 32, 20);
        g2.setColor(new Color(200, 60, 60, 200));
        g2.fillRect(x - 15, by + 7, 14, 8);
        g2.setColor(new Color(60, 120, 200, 200));
        g2.fillRect(x + 1, by + 7, 14, 8);
        g2.setColor(new Color(60, 160, 80, 200));
        g2.fillRect(x - 15, by + 16, 30, 8);
        g2.setColor(new Color(30, 48, 70));
        g2.drawRect(x - 16, by + 6, 32, 20);
        g2.setColor(new Color(80, 68, 55));
        g2.fillRect(x - 22, by + 30, 44, 6);
        g2.setColor(new Color(50, 50, 55));
        g2.fillRect(x - 18, by + 36, 36, 8);
    }

    private void drawUrbanTree(Graphics2D g2, int x, int groundY) {
        int trunkBaseY = groundY + 52;
        int trunkH     = 36;
        g2.setColor(new Color(60, 45, 30));
        g2.fillRect(x - 6, trunkBaseY - 4, 12, 4);
        g2.setColor(new Color(80, 58, 35));
        g2.fillRect(x - 5, trunkBaseY - trunkH, 10, trunkH);
        g2.setColor(new Color(65, 45, 25));
        g2.drawLine(x - 2, trunkBaseY - trunkH + 5,
                x - 2, trunkBaseY - 8);
        g2.drawLine(x + 2, trunkBaseY - trunkH + 10,
                x + 2, trunkBaseY - 6);
        g2.setColor(new Color(25, 65, 25, 180));
        g2.fillOval(x - 22, trunkBaseY - trunkH - 30, 46, 38);
        g2.setColor(new Color(35, 95, 35));
        g2.fillOval(x - 20, trunkBaseY - trunkH - 34, 40, 36);
        g2.setColor(new Color(55, 130, 50));
        g2.fillOval(x - 14, trunkBaseY - trunkH - 32, 22, 16);
        g2.setColor(new Color(70, 155, 60, 160));
        g2.fillOval(x - 8, trunkBaseY - trunkH - 36, 14, 10);
    }

    private void drawManhole(Graphics2D g2, int x, int groundY) {
        int my = groundY + 100;
        g2.setColor(new Color(55, 55, 58));
        g2.fillOval(x - 14, my - 6, 28, 14);
        g2.setColor(new Color(48, 48, 50));
        g2.fillOval(x - 12, my - 5, 24, 12);
        g2.setColor(new Color(38, 38, 40));
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawLine(x - 8, my - 2, x + 8, my - 2);
        g2.drawLine(x - 8, my + 2, x + 8, my + 2);
        g2.drawLine(x - 4, my - 4, x - 4, my + 4);
        g2.drawLine(x,     my - 4, x,     my + 4);
        g2.drawLine(x + 4, my - 4, x + 4, my + 4);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(42, 42, 44));
        g2.drawOval(x - 10, my - 4, 20, 10);
    }

    private void drawTitle(Graphics2D g2) {
        g2.setColor(new Color(10, 30, 10));
        g2.fillRect(0, 0, screenW, screenH);

        g2.setFont(new Font("Arial", Font.BOLD, 52));
        g2.setColor(new Color(80, 255, 80));
        String title = "greenforest_";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, screenW / 2 - fm.stringWidth(title) / 2, 100);

        g2.setFont(new Font("Arial", Font.ITALIC, 16));
        g2.setColor(new Color(150, 220, 150));
        String sub = "Defenda a cidade do lixo urbano!";
        fm = g2.getFontMetrics();
        g2.drawString(sub, screenW / 2 - fm.stringWidth(sub) / 2, 132);

        switch (titlePhase) {
            case 0 -> drawTitleDifficulty(g2);
            case 1 -> drawTitleMode(g2);
            case 2 -> drawTitleContinue(g2);
        }
    }

    private void drawTitleDifficulty(Graphics2D g2) {
        drawTitleSection(g2, "ESCOLHA A DIFICULDADE:", 195);
        String[] diffs   = {"FACIL", "NORMAL", "DIFICIL"};
        Color[]  dColors = {
                new Color(80,  200, 80),
                new Color(200, 200, 80),
                new Color(200, 80,  80)
        };
        for (int i = 0; i < diffs.length; i++)
            drawTitleOption(g2, diffs[i],
                    245 + i * 52, i == diffSelection, dColors[i]);
        drawTitleHint(g2,
                "Setas CIMA/BAIXO para selecionar  |  ENTER para confirmar");
    }

    private void drawTitleMode(Graphics2D g2) {
        drawTitleSection(g2, "MODO DE ATAQUE:", 195);
        drawTitleOption(g2,
                "AUTOMATICO  --  Atira sozinho no inimigo mais proximo",
                245, modeSelection == 0, new Color(80, 200, 255));
        drawTitleOption(g2,
                "MANUAL  --  Aponte com o mouse e clique para atirar",
                300, modeSelection == 1, new Color(255, 180, 80));
        drawTitleHint(g2,
                "Setas CIMA/BAIXO para selecionar  |  ENTER para comecar!");
    }

    private void drawTitleContinue(Graphics2D g2) {
        drawTitleSection(g2, "JOGO SALVO ENCONTRADO", 195);
        drawTitleOption(g2, "CONTINUAR JOGO SALVO",
                260, continueSelection == 0, new Color(80, 200, 255));
        drawTitleOption(g2, "NOVO JOGO",
                315, continueSelection == 1, new Color(255, 180, 80));
        drawTitleHint(g2,
                "Setas CIMA/BAIXO para selecionar  |  ENTER para confirmar");
    }

    private void drawTitleSection(Graphics2D g2, String text, int y) {
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, screenW / 2 - fm.stringWidth(text) / 2, y);
    }

    private void drawTitleOption(Graphics2D g2, String text, int y,
                                 boolean selected, Color color) {
        if (selected) {
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            String label = "> " + text + " <";
            int tw = fm.stringWidth(label);
            int tx = screenW / 2 - tw / 2;
            g2.setColor(new Color(
                    color.getRed(), color.getGreen(), color.getBlue(), 45));
            g2.fillRoundRect(tx - 14, y - 22, tw + 28, 30, 10, 10);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(tx - 14, y - 22, tw + 28, 30, 10, 10);
            g2.setStroke(new BasicStroke(1f));
            g2.drawString(label, tx, y);
        } else {
            g2.setFont(new Font("Arial", Font.PLAIN, 17));
            g2.setColor(new Color(140, 140, 140));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, screenW / 2 - fm.stringWidth(text) / 2, y);
        }
    }

    private void drawTitleHint(Graphics2D g2, String hint) {
        g2.setFont(new Font("Arial", Font.ITALIC, 14));
        g2.setColor(new Color(120, 200, 120));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint,
                screenW / 2 - fm.stringWidth(hint) / 2, screenH - 40);
    }

    private void drawPause(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 165));
        g2.fillRect(0, 0, screenW, screenH);
        int cx = screenW / 2;

        g2.setFont(new Font("Arial", Font.BOLD, 38));
        g2.setColor(new Color(200, 255, 200));
        String pauseTitle = "PAUSADO";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pauseTitle,
                cx - fm.stringWidth(pauseTitle) / 2, 185);

        String[] options = {
                "Resumir",
                "Salvar Jogo",
                "Trocar Modo de Ataque  (" + (manualMode ? "MANUAL" : "AUTO") + ")",
                "Voltar ao Menu"
        };
        for (int i = 0; i < options.length; i++) {
            boolean sel = pauseOption == i;
            g2.setFont(new Font("Arial", Font.BOLD, sel ? 22 : 18));
            g2.setColor(sel
                    ? new Color(80, 255, 80)
                    : new Color(160, 160, 160));
            fm = g2.getFontMetrics();
            String label = sel ? "> " + options[i] + " <" : options[i];
            g2.drawString(label,
                    cx - fm.stringWidth(label) / 2, 262 + i * 52);
        }

        g2.setFont(new Font("Arial", Font.ITALIC, 13));
        g2.setColor(new Color(120, 180, 120));
        String hint =
                "CIMA/BAIXO para navegar  |  ENTER para confirmar  |  ESC para resumir";
        fm = g2.getFontMetrics();
        g2.drawString(hint,
                cx - fm.stringWidth(hint) / 2, screenH - 40);
    }

    private void drawBossLesson(Graphics2D g2) {
        if (pendingBossLesson != null)
            overlay.drawBossLesson(g2, screenW, screenH,
                    pendingBossLesson, pendingBossName);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 235));
        g2.fillRect(0, 0, screenW, screenH);
        int cx = screenW / 2;

        g2.setFont(new Font("Arial", Font.BOLD, 50));
        g2.setColor(new Color(255, 60, 60));
        String go = "GAME OVER";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(go, cx - fm.stringWidth(go) / 2, 120);

        g2.setFont(new Font("Arial", Font.BOLD, 19));
        g2.setColor(Color.WHITE);
        String[] stats = {
                "Nivel: "  + (player != null ? player.getLevel() : 0),
                "Kills: "  + (player != null ? player.getKills() : 0),
                "Tempo: "  + String.format("%02d:%02d",
                        (int)(gameTimeSec / 60), (int)(gameTimeSec % 60))
        };
        for (int i = 0; i < stats.length; i++) {
            fm = g2.getFontMetrics();
            g2.drawString(stats[i],
                    cx - fm.stringWidth(stats[i]) / 2, 175 + i * 30);
        }

        String[] lines = gameOverLesson.split("\n");
        int padding = 22;
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        fm = g2.getFontMetrics();
        int lineH = fm.getHeight() + 2;
        int maxW  = 0;
        for (String l : lines) maxW = Math.max(maxW, fm.stringWidth(l));
        int boxW = maxW + padding * 2;
        int boxH = lines.length * lineH + padding * 2;
        int bx   = cx - boxW / 2;
        int by   = 280;

        g2.setColor(new Color(30, 60, 30, 210));
        g2.fillRoundRect(bx, by, boxW, boxH, 14, 14);
        g2.setColor(new Color(80, 200, 80));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx, by, boxW, boxH, 14, 14);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(new Color(200, 255, 180));
        for (int i = 0; i < lines.length; i++) {
            fm = g2.getFontMetrics();
            g2.drawString(lines[i],
                    bx + padding,
                    by + padding + fm.getAscent() + i * lineH);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(new Color(150, 255, 150));
        String restartHint =
                "R  --  Jogar novamente     |     M  --  Menu principal";
        fm = g2.getFontMetrics();
        g2.drawString(restartHint,
                cx - fm.stringWidth(restartHint) / 2, by + boxH + 48);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keysDown.add(e.getKeyCode());
        int k = e.getKeyCode();
        switch (state) {
            case TITLE       -> handleTitleKey(k);
            case PLAYING     -> handlePlayingKey(k);
            case PAUSED      -> handlePauseKey(k);
            case POWER_UP    -> handlePowerUpKey(k);
            case BOSS_LESSON -> {
                if (k == KeyEvent.VK_ENTER) {
                    pendingBossLesson = null;
                    pendingBossName   = null;
                    state = GameState.PLAYING;
                }
            }
            case GAME_OVER -> {
                if (k == KeyEvent.VK_R) {
                    titlePhase = 0;
                    startGame();
                } else if (k == KeyEvent.VK_M) {
                    titlePhase = SaveData.hasSave() ? 2 : 0;
                    state = GameState.TITLE;
                }
            }
        }
    }

    private void handleTitleKey(int k) {
        switch (titlePhase) {
            case 0 -> { // dificuldade
                if (k == KeyEvent.VK_UP)
                    diffSelection = Math.max(0, diffSelection - 1);
                if (k == KeyEvent.VK_DOWN)
                    diffSelection = Math.min(2, diffSelection + 1);
                if (k == KeyEvent.VK_ENTER) {
                    DifficultySettings.set(switch (diffSelection) {
                        case 0  -> DifficultySettings.Difficulty.EASY;
                        case 2  -> DifficultySettings.Difficulty.HARD;
                        default -> DifficultySettings.Difficulty.NORMAL;
                    });
                    titlePhase = 1;
                }
            }
            case 1 -> { // modo de ataque
                if (k == KeyEvent.VK_UP)
                    modeSelection = Math.max(0, modeSelection - 1);
                if (k == KeyEvent.VK_DOWN)
                    modeSelection = Math.min(1, modeSelection + 1);
                if (k == KeyEvent.VK_ENTER) startGame();
            }
            case 2 -> { // continuar ou novo jogo
                if (k == KeyEvent.VK_UP)
                    continueSelection = Math.max(0, continueSelection - 1);
                if (k == KeyEvent.VK_DOWN)
                    continueSelection = Math.min(1, continueSelection + 1);
                if (k == KeyEvent.VK_ENTER) {
                    if (continueSelection == 0) {
                        continueGame();
                    } else {
                        // Novo jogo: escolhe dificuldade normalmente
                        SaveData.deleteSave();
                        titlePhase = 0;
                    }
                }
            }
        }
    }

    private void handlePlayingKey(int k) {
        if (k == KeyEvent.VK_ESCAPE) {
            pauseOption = 0;
            state = GameState.PAUSED;
        }
    }

    private void handlePauseKey(int k) {
        if (k == KeyEvent.VK_ESCAPE) { state = GameState.PLAYING; return; }
        if (k == KeyEvent.VK_UP)
            pauseOption = Math.max(0, pauseOption - 1);
        if (k == KeyEvent.VK_DOWN)
            pauseOption = Math.min(3, pauseOption + 1); // agora tem 4 opcoes
        if (k == KeyEvent.VK_ENTER) {
            switch (pauseOption) {
                case 0 -> state = GameState.PLAYING;
                case 1 -> {
                    // Salvar jogo
                    boolean ok = saveGame();
                    // Exibe feedback breve (reusa overlay de horda)
                    overlay.showHordaMessage(ok
                            ? "Jogo salvo com sucesso!"
                            : "Erro ao salvar o jogo.");
                    state = GameState.PLAYING;
                }
                case 2 -> {
                    manualMode = !manualMode;
                    hud.setAttackMode(manualMode);
                    state = GameState.PLAYING;
                }
                case 3 -> {
                    titlePhase = SaveData.hasSave() ? 2 : 0;
                    state = GameState.TITLE;
                }
            }
        }
    }

    private void handlePowerUpKey(int k) {
        if (k == KeyEvent.VK_LEFT)  powerUpScreen.moveLeft();
        if (k == KeyEvent.VK_RIGHT) powerUpScreen.moveRight();
        if (k == KeyEvent.VK_ENTER) powerUpScreen.confirm();
    }

    @Override
    public void keyReleased(KeyEvent e) { keysDown.remove(e.getKeyCode()); }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (state == GameState.POWER_UP) {
            powerUpScreen.handleClick(e.getX(), e.getY(), screenW, screenH);
            return;
        }
        if (state == GameState.PLAYING
                && manualMode
                && e.getButton() == MouseEvent.BUTTON1) {
            float worldMouseX = mouseX + camX;
            float worldMouseY = mouseY + camY;
            player.setAimDirection(
                    worldMouseX - player.getX(),
                    worldMouseY - player.getY());
            projectileManager.manualFire(player);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (state == GameState.PLAYING
                && manualMode
                && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            float worldMouseX = mouseX + camX;
            float worldMouseY = mouseY + camY;
            player.setAimDirection(
                    worldMouseX - player.getX(),
                    worldMouseY - player.getY());
            projectileManager.manualFire(player);
        }
    }

    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}