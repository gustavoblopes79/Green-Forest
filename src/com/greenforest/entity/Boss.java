package com.greenforest.entity;

import com.greenforest.DifficultySettings;
import java.awt.*;

public class Boss extends Enemy {

    public enum BossType {
        E_WASTE,
        FACTORY,
        GARBAGE_TRUCK,
        PETROLEO,
        DESMATAMENTO,
        PLASTICO_OCEANO
    }

    private float   specialTimer  = 0f;
    private float   specialCd;
    private boolean specialActive = false;
    private float   specialRadius = 0f;
    private float   specialMaxR;

    // E_WASTE: pulso eletromagnetico (flash na tela, dano em area menor)
    private float   empPulseTimer  = 0f;
    private boolean empActive      = false;
    private float   empAlpha       = 0f;

    // FACTORY: nuvem de fumaca que expande e persiste
    private float   smokeTimer     = 0f;
    private float   smokeRadius    = 0f;
    private boolean smokeActive    = false;

    // GARBAGE_TRUCK: carga de lixo atirada (projetil visual)
    private float   trashTimer     = 0f;
    private float   trashX, trashY;
    private float   trashTargetX, trashTargetY;
    private boolean trashFlying    = false;
    private float   trashProgress  = 0f;

    // PETROLEO: mancha de oleo que cresce
    private float   oilTimer       = 0f;
    private float   oilRadius      = 0f;
    private boolean oilActive      = false;

    // DESMATAMENTO: corrida em linha reta (dash)
    private boolean dashActive     = false;
    private float   dashTimer      = 0f;
    private float   dashCd         = 5.0f;
    private float   dashDx, dashDy;
    private float   dashDuration   = 0.5f;
    private float   dashElapsed    = 0f;

    // PLASTICO_OCEANO: tentaculos de plastico (linhas animadas)
    private float   tentacleTimer  = 0f;
    private float[] tentacleAngles = new float[6];
    private float   tentacleLen    = 0f;
    private boolean tentacleActive = false;

    private float   animTimer = 0f;
    private final BossType bossType;
    private final String   lesson;

    public Boss(float x, float y, BossType type) {
        super(x, y, 0);
        this.bossType = type;

        float hpBase = switch (type) {
            case E_WASTE        -> 800f;
            case FACTORY        -> 1000f;
            case GARBAGE_TRUCK  -> 700f;
            case PETROLEO       -> 900f;
            case DESMATAMENTO   -> 850f;
            case PLASTICO_OCEANO-> 950f;
        };
        this.maxHp   = (int)(hpBase * DifficultySettings.bossHpMult());
        this.hp      = this.maxHp;
        this.speed   = speedForBoss(type) * DifficultySettings.enemySpeedMult();
        this.damage  = (int)(damageForBoss(type) * DifficultySettings.bossDamageMult());
        this.xpValue = 150;
        this.size    = 68;

        // CD do especial radial varia por boss
        this.specialCd   = specialCdForBoss(type);
        this.specialMaxR = specialMaxRForBoss(type);

        // Inicializa angulos dos tentaculos
        for (int i = 0; i < tentacleAngles.length; i++)
            tentacleAngles[i] = (float)(i * Math.PI * 2 / tentacleAngles.length);

        this.lesson = buildLesson(type);
    }

    private static float speedForBoss(BossType t) {
        return switch (t) {
            case E_WASTE         -> 0.65f;
            case FACTORY         -> 0.45f; // fabrica quase nao se move
            case GARBAGE_TRUCK   -> 0.90f; // caminhao e rapido
            case PETROLEO        -> 0.55f;
            case DESMATAMENTO    -> 0.80f;
            case PLASTICO_OCEANO -> 0.50f;
        };
    }

    private static int damageForBoss(BossType t) {
        return switch (t) {
            case E_WASTE         -> 28;
            case FACTORY         -> 35;
            case GARBAGE_TRUCK   -> 30;
            case PETROLEO        -> 32;
            case DESMATAMENTO    -> 38;
            case PLASTICO_OCEANO -> 25;
        };
    }

    private static float specialCdForBoss(BossType t) {
        return switch (t) {
            case E_WASTE         -> 3.5f;
            case FACTORY         -> 5.0f;
            case GARBAGE_TRUCK   -> 4.0f;
            case PETROLEO        -> 4.5f;
            case DESMATAMENTO    -> 3.0f;
            case PLASTICO_OCEANO -> 4.0f;
        };
    }

    private static float specialMaxRForBoss(BossType t) {
        return switch (t) {
            case E_WASTE         -> 130f;
            case FACTORY         -> 150f;
            case GARBAGE_TRUCK   -> 110f;
            case PETROLEO        -> 140f;
            case DESMATAMENTO    -> 100f;
            case PLASTICO_OCEANO -> 160f;
        };
    }

    private static String buildLesson(BossType t) {
        return switch (t) {
            case E_WASTE ->
                    "O lixo eletrônico é o resíduo que mais cresce no mundo.\n" +
                            "Celulares e computadores contém metais tóxicos como\n" +
                            "chumbo e mercúrio. Descarte nos pontos especializados!";
            case FACTORY ->
                    "Fábricas sem controle lançam CO2 e SO2 na atmosfera,\n" +
                            "causando chuva ácida e aquecimento global.\n" +
                            "Exija políticas de emissão zero e setores sustentáveis!";
            case GARBAGE_TRUCK ->
                    "O Brasil gera mais de 80 milhões de toneladas de\n" +
                            "resíduos por ano. Apenas 4% é reciclado.\n" +
                            "Separe o lixo e apoie a coleta seletiva no seu bairro!";
            case PETROLEO ->
                    "Derramamentos de petróleo destroem ecossistemas marinhos\n" +
                            "e costeiros por décadas. Um litro de óleo contamina\n" +
                            "1 milhão de litros de água. Reduza o uso de combustíveis!";
            case DESMATAMENTO ->
                    "O Brasil perdeu mais de 20% da Amazônia. O desmatamento\n" +
                            "libera carbono, destrói habitats e seca rios.\n" +
                            "Consuma produtos certificados e denuncie o desmatamento ilegal!";
            case PLASTICO_OCEANO ->
                    "8 milhões de toneladas de plástico entram nos oceanos\n" +
                            "todo ano. Microplásticos já foram encontrados no sangue\n" +
                            "humano. Reduza, reutilize e recicle o plástico!";
        };
    }

    @Override
    public void update(float dt, float playerX, float playerY) {
        animTimer += dt;

        // Movimento base (fabrica nao se move muito)
        if (bossType != BossType.FACTORY) {
            float dx   = playerX - x;
            float dy   = playerY - y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            if (dist > 80f) {
                float spd = (dashActive && bossType == BossType.DESMATAMENTO)
                        ? speed * 4.5f : speed;
                x += (dx / dist) * spd;
                y += (dy / dist) * spd;
            }
        } else {
            // Fabrica oscila levemente no lugar
            x += (float)Math.sin(animTimer * 0.5f) * 0.3f;
        }

        // Ataque especial radial (todos os bosses)
        specialTimer += dt;
        if (specialTimer >= specialCd) {
            specialTimer  = 0f;
            specialActive = true;
            specialRadius = 0f;
        }
        if (specialActive) {
            specialRadius += 200f * dt;
            if (specialRadius >= specialMaxR) {
                specialActive = false;
                specialRadius = 0f;
            }
        }

        // Ataques secundarios por tipo
        switch (bossType) {
            case E_WASTE         -> updateEWaste(dt, playerX, playerY);
            case FACTORY         -> updateFactory(dt, playerX, playerY);
            case GARBAGE_TRUCK   -> updateGarbageTruck(dt, playerX, playerY);
            case PETROLEO        -> updatePetroleo(dt);
            case DESMATAMENTO    -> updateDesmatamento(dt, playerX, playerY);
            case PLASTICO_OCEANO -> updatePlasticoOceano(dt);
        }
    }

    private void updateEWaste(float dt, float px, float py) {
        empPulseTimer += dt;
        if (empPulseTimer >= 6.0f) {
            empPulseTimer = 0f;
            empActive     = true;
            empAlpha      = 1.0f;
        }
        if (empActive) {
            empAlpha -= dt * 1.5f;
            if (empAlpha <= 0f) {
                empAlpha  = 0f;
                empActive = false;
            }
        }
    }

    private void updateFactory(float dt, float px, float py) {
        smokeTimer += dt;
        if (smokeTimer >= 3.5f) {
            smokeTimer  = 0f;
            smokeActive = true;
            smokeRadius = 0f;
        }
        if (smokeActive) {
            smokeRadius += 60f * dt;
            if (smokeRadius >= 100f) {
                smokeActive = false;
                smokeRadius = 0f;
            }
        }
    }

    private void updateGarbageTruck(float dt, float px, float py) {
        trashTimer += dt;
        if (!trashFlying && trashTimer >= 3.0f) {
            trashTimer    = 0f;
            trashFlying   = true;
            trashProgress = 0f;
            trashX        = x;
            trashY        = y;
            trashTargetX  = px;
            trashTargetY  = py;
        }
        if (trashFlying) {
            trashProgress += dt * 1.2f;
            if (trashProgress >= 1.0f) {
                trashProgress = 0f;
                trashFlying   = false;
            }
        }
    }

    private void updatePetroleo(float dt) {
        oilTimer += dt;
        if (oilTimer >= 4.0f) {
            oilTimer  = 0f;
            oilActive = true;
            oilRadius = 0f;
        }
        if (oilActive) {
            oilRadius += 40f * dt;
            if (oilRadius >= 80f) {
                oilActive = false;
                oilRadius = 0f;
            }
        }
    }

    private void updateDesmatamento(float dt, float px, float py) {
        dashTimer += dt;
        if (!dashActive && dashTimer >= dashCd) {
            dashTimer   = 0f;
            dashActive  = true;
            dashElapsed = 0f;
            float dx   = px - x;
            float dy   = py - y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            dashDx = dist > 0 ? dx / dist : 1f;
            dashDy = dist > 0 ? dy / dist : 0f;
        }
        if (dashActive) {
            dashElapsed += dt;
            if (dashElapsed >= dashDuration) dashActive = false;
        }
    }

    private void updatePlasticoOceano(float dt) {
        tentacleTimer += dt;
        if (!tentacleActive && tentacleTimer >= 3.5f) {
            tentacleTimer  = 0f;
            tentacleActive = true;
            tentacleLen    = 0f;
        }
        if (tentacleActive) {
            tentacleLen += 120f * dt;
            // Rotaciona angulos
            for (int i = 0; i < tentacleAngles.length; i++)
                tentacleAngles[i] += dt * (i % 2 == 0 ? 1.2f : -0.8f);
            if (tentacleLen >= 90f) {
                tentacleActive = false;
                tentacleLen    = 0f;
            }
        }
    }

    /** Onda radial — todos os bosses */
    public boolean hitsWithSpecial(float px, float py) {
        if (!specialActive) return false;
        float dx   = px - x;
        float dy   = py - y;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        return dist >= specialRadius - 12f && dist <= specialRadius + 12f;
    }

    /** EMP — hit se estiver dentro do raio quando o flash esta ativo */
    public boolean hitsWithEmp(float px, float py) {
        if (!empActive || empAlpha < 0.5f) return false;
        float dx = px - x, dy = py - y;
        return Math.sqrt(dx * dx + dy * dy) < 150;
    }

    /** Nuvem de fumaca da fabrica */
    public boolean hitsWithSmoke(float px, float py) {
        if (!smokeActive) return false;
        float dx = px - x, dy = py - y;
        float d  = (float)Math.sqrt(dx * dx + dy * dy);
        return d <= smokeRadius;
    }

    /** Carga de lixo — hit quando chega no destino */
    public boolean hitsWithTrash(float px, float py) {
        if (!trashFlying) return false;
        float cx  = trashX + (trashTargetX - trashX) * trashProgress;
        float cy  = trashY + (trashTargetY - trashY) * trashProgress;
        float dx  = px - cx, dy = py - cy;
        return Math.sqrt(dx * dx + dy * dy) < 30 && trashProgress > 0.85f;
    }

    /** Mancha de oleo */
    public boolean hitsWithOil(float px, float py) {
        if (!oilActive) return false;
        float dx = px - x, dy = py - y;
        return Math.sqrt(dx * dx + dy * dy) <= oilRadius;
    }

    /** Dash da motosserra */
    public boolean hitsWithDash(float px, float py) {
        if (!dashActive) return false;
        float dx = px - x, dy = py - y;
        return Math.sqrt(dx * dx + dy * dy) < 60;
    }

    /** Tentaculos */
    public boolean hitsWithTentacles(float px, float py) {
        if (!tentacleActive) return false;
        for (float angle : tentacleAngles) {
            float tx = x + (float)Math.cos(angle) * tentacleLen;
            float ty = y + (float)Math.sin(angle) * tentacleLen;
            float dx = px - tx, dy = py - ty;
            if (Math.sqrt(dx * dx + dy * dy) < 18) return true;
        }
        return false;
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        int sx = (int)(x - camX) - size / 2;
        int sy = (int)(y - camY) - size / 2;

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(sx + 4, sy + size - 6, size - 8, 10);

        switch (bossType) {
            case E_WASTE         -> drawEWaste(g2, sx, sy);
            case FACTORY         -> drawFactory(g2, sx, sy);
            case GARBAGE_TRUCK   -> drawGarbageTruck(g2, sx, sy);
            case PETROLEO        -> drawPetroleo(g2, sx, sy);
            case DESMATAMENTO    -> drawDesmatamento(g2, sx, sy);
            case PLASTICO_OCEANO -> drawPlasticoOceano(g2, sx, sy);
        }

        drawSpecialEffects(g2, camX, camY);
        drawBossHealthBar(g2, (int)(x - camX), (int)(y - camY));
    }

    private void drawSpecialEffects(Graphics2D g2, int camX, int camY) {
        int cx = (int)(x - camX);
        int cy = (int)(y - camY);

        // Onda radial — todos
        if (specialActive) {
            int r = (int)specialRadius;
            Color waveColor = switch (bossType) {
                case E_WASTE         -> new Color(0,   255, 100, 120);
                case FACTORY         -> new Color(150, 150,   0, 120);
                case GARBAGE_TRUCK   -> new Color(180,   0, 220, 110);
                case PETROLEO        -> new Color(20,   20,  20, 180);
                case DESMATAMENTO    -> new Color(80,  200,   0, 130);
                case PLASTICO_OCEANO -> new Color(0,   150, 255, 130);
            };
            g2.setColor(waveColor);
            g2.setStroke(new BasicStroke(10f));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(new Color(
                    waveColor.getRed(), waveColor.getGreen(),
                    waveColor.getBlue(), 60));
            g2.setStroke(new BasicStroke(4f));
            g2.drawOval(cx - r + 4, cy - r + 4, r * 2 - 8, r * 2 - 8);
            g2.setStroke(new BasicStroke(1f));
        }

        // EMP — flash verde
        if (empActive && empAlpha > 0f) {
            int alpha = (int)(empAlpha * 140);
            g2.setColor(new Color(0, 255, 80, alpha));
            g2.fillOval(cx - 150, cy - 150, 300, 300);
        }

        // Fumaca da fabrica
        if (smokeActive) {
            int r = (int)smokeRadius;
            int a = (int)(120 * (1f - smokeRadius / 100f));
            g2.setColor(new Color(60, 60, 60, Math.max(a, 0)));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        }

        // Carga de lixo voando
        if (trashFlying) {
            float tx = trashX - camX + (trashTargetX - trashX) * trashProgress;
            float ty = trashY - camY + (trashTargetY - trashY) * trashProgress;
            float arc = (float)Math.sin(trashProgress * Math.PI) * -60f;
            g2.setColor(new Color(120, 90, 40));
            g2.fillOval((int)tx - 12, (int)(ty + arc) - 12, 24, 24);
            g2.setColor(new Color(80, 160, 40));
            g2.fillOval((int)tx - 6,  (int)(ty + arc) - 6,  12, 10);
            g2.setColor(new Color(200, 30, 30));
            g2.fillOval((int)tx,      (int)(ty + arc) - 8,  8,  8);
        }

        // Mancha de oleo
        if (oilActive) {
            int r = (int)oilRadius;
            g2.setColor(new Color(15, 10, 25, 160));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(new Color(100, 0, 160, 80));
            g2.fillOval(cx - r / 2, cy - r / 2, r, r);
        }

        // Tentaculos
        if (tentacleActive) {
            g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (float angle : tentacleAngles) {
                int tx = cx + (int)(Math.cos(angle) * tentacleLen);
                int ty = cy + (int)(Math.sin(angle) * tentacleLen);
                g2.setColor(new Color(30, 100, 200, 200));
                g2.drawLine(cx, cy, tx, ty);
                g2.setColor(new Color(180, 220, 255, 150));
                g2.fillOval(tx - 9, ty - 9, 18, 18);
            }
            g2.setStroke(new BasicStroke(1f));
        }
    }

    private void drawBossHealthBar(Graphics2D g2, int cx, int cy) {
        int barW = 140;
        int barH = 14;
        int bx   = cx - barW / 2;
        int by   = cy - size / 2 - 26;

        g2.setColor(new Color(20, 0, 0, 200));
        g2.fillRoundRect(bx - 2, by - 2, barW + 4, barH + 4, 6, 6);

        g2.setColor(new Color(60, 10, 10));
        g2.fillRoundRect(bx, by, barW, barH, 4, 4);

        float ratio    = (float)hp / maxHp;
        Color barColor = ratio > 0.5f
                ? new Color(200, 40, 40)
                : new Color(255, 80, 0);
        g2.setColor(barColor);
        g2.fillRoundRect(bx, by, (int)(barW * ratio), barH, 4, 4);

        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRoundRect(bx, by, (int)(barW * ratio), barH / 2, 4, 4);

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(Color.WHITE);
        String label = bossTypeLabel() + "  " + hp + "/" + maxHp;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, cx - fm.stringWidth(label) / 2, by + barH - 1);
    }

    private String bossTypeLabel() {
        return switch (bossType) {
            case E_WASTE         -> "LIXO ELETRÔNICO";
            case FACTORY         -> "FÁBRICA POLUENTE";
            case GARBAGE_TRUCK   -> "CAMINHÃO DE LIXO";
            case PETROLEO        -> "DERRAMAMENTO DE PETRÓLEO";
            case DESMATAMENTO    -> "MOTOSSERRA DO DESMATE";
            case PLASTICO_OCEANO -> "ILHA DE PLÁSTICO";
        };
    }

    private void drawEWaste(Graphics2D g2, int sx, int sy) {
        g2.setColor(new Color(65, 65, 75));
        g2.fillRoundRect(sx + 6, sy + 30, 56, 32, 8, 8);
        g2.setColor(new Color(80, 80, 90));
        g2.fillRoundRect(sx + 10, sy + 24, 48, 14, 6, 6);

        g2.setColor(new Color(30, 30, 40));
        g2.fillRoundRect(sx + 12, sy + 6, 44, 30, 5, 5);
        g2.setColor(new Color(0, 30, 50));
        g2.fillRect(sx + 15, sy + 9, 38, 24);
        g2.setColor(new Color(0, 180, 255, 130));
        g2.fillRect(sx + 15, sy + 9, 38, 24);

        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(sx + 22, sy + 9,  sx + 32, sy + 33);
        g2.drawLine(sx + 32, sy + 11, sx + 45, sy + 28);
        g2.drawLine(sx + 22, sy + 20, sx + 38, sy + 24);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(new Color(55, 55, 65));
        g2.fillRect(sx + 28, sy + 36, 12, 6);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(220, 30, 30));
        g2.drawLine(sx + 16, sy + 60, sx + 10, sy + 68);
        g2.setColor(new Color(30, 30, 220));
        g2.drawLine(sx + 26, sy + 60, sx + 22, sy + 68);
        g2.setColor(new Color(30, 200, 30));
        g2.drawLine(sx + 36, sy + 60, sx + 38, sy + 68);
        g2.setColor(new Color(220, 200, 0));
        g2.drawLine(sx + 46, sy + 60, sx + 50, sy + 68);
        g2.setStroke(new BasicStroke(1f));

        // EMP — flash nos olhos quando ativo
        Color eyeColor = (empActive && empAlpha > 0.3f)
                ? new Color(255, 255, 255)
                : new Color(0, 255, 80);
        g2.setColor(eyeColor);
        g2.fillOval(sx + 19, sy + 13, 10, 10);
        g2.fillOval(sx + 38, sy + 13, 10, 10);
        g2.setColor(new Color(0, 100, 30));
        g2.fillOval(sx + 21, sy + 15, 6, 6);
        g2.fillOval(sx + 40, sy + 15, 6, 6);
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 23, sy + 16, 2, 2);
        g2.fillOval(sx + 42, sy + 16, 2, 2);

        g2.setColor(new Color(0, 255, 80, 80));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(sx + 6, sy + 30, 56, 32, 8, 8);
        g2.drawRoundRect(sx + 12, sy + 6, 44, 30, 5, 5);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawFactory(Graphics2D g2, int sx, int sy) {
        g2.setColor(new Color(95, 85, 75));
        g2.fillRect(sx + 8, sy + 34, 52, 30);

        g2.setColor(new Color(75, 68, 60));
        int[] rx = {sx+8,sx+16,sx+24,sx+32,sx+40,sx+48,sx+56,sx+60,sx+8};
        int[] ry = {sy+34,sy+26,sy+34,sy+26,sy+34,sy+26,sy+34,sy+34,sy+34};
        g2.fillPolygon(rx, ry, 9);

        g2.setColor(new Color(68, 60, 52));
        g2.fillRect(sx + 12, sy + 10, 14, 26);
        g2.setColor(new Color(55, 48, 42));
        g2.fillRect(sx + 10, sy + 8, 18, 6);

        g2.setColor(new Color(68, 60, 52));
        g2.fillRect(sx + 40, sy + 16, 12, 20);
        g2.setColor(new Color(55, 48, 42));
        g2.fillRect(sx + 38, sy + 14, 16, 6);

        float ft = animTimer;
        for (int i = 0; i < 4; i++) {
            int wobble = (int)(Math.sin(ft * 1.5f + i) * 4);
            int alpha  = 180 - i * 35;
            g2.setColor(new Color(50, 50, 50, alpha));
            g2.fillOval(sx + 12 + wobble, sy - 2 - i * 9, 16 + i * 2, 12 + i);
        }
        for (int i = 0; i < 3; i++) {
            int wobble = (int)(Math.sin(ft * 2f + i + 1) * 3);
            int alpha  = 160 - i * 40;
            g2.setColor(new Color(80, 55, 40, alpha));
            g2.fillOval(sx + 40 + wobble, sy + 4 - i * 8, 14 + i, 10 + i);
        }

        g2.setColor(new Color(255, 200, 60, 200));
        g2.fillRect(sx + 12, sy + 38, 12, 10);
        g2.fillRect(sx + 30, sy + 38, 12, 10);

        g2.setColor(new Color(50, 36, 20));
        g2.fillRoundRect(sx + 26, sy + 50, 14, 14, 4, 4);

        // Olhos com tom de fumaca quando ativo
        Color eyeC = smokeActive
                ? new Color(255, 255, 100)
                : new Color(255, 80, 0);
        g2.setColor(eyeC);
        g2.fillOval(sx + 14, sy + 40, 7, 7);
        g2.fillOval(sx + 32, sy + 40, 7, 7);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 16, sy + 42, 3, 3);
        g2.fillOval(sx + 34, sy + 42, 3, 3);

        g2.setColor(new Color(255, 80, 0, 70));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(sx + 8, sy + 34, 52, 30);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawGarbageTruck(Graphics2D g2, int sx, int sy) {
        g2.setColor(new Color(70, 72, 70));
        g2.fillRect(sx + 2, sy + 26, 40, 28);
        g2.setColor(new Color(85, 88, 85));
        g2.fillRect(sx + 2, sy + 24, 40, 6);

        int[] lxs = {sx+4,sx+8,sx+14,sx+20,sx+27,sx+35,sx+40};
        int[] lys = {sy+26,sy+18,sy+22,sy+16,sy+20,sy+17,sy+26};
        g2.setColor(new Color(100, 80, 40));
        g2.fillPolygon(lxs, lys, 7);
        g2.setColor(new Color(160, 50, 50));
        g2.fillOval(sx + 6,  sy + 17, 9, 9);
        g2.setColor(new Color(80, 140, 50));
        g2.fillOval(sx + 18, sy + 15, 8, 8);
        g2.setColor(new Color(200, 180, 40));
        g2.fillOval(sx + 30, sy + 16, 8, 8);

        g2.setColor(new Color(50, 120, 55));
        g2.fillRoundRect(sx + 40, sy + 18, 26, 36, 8, 8);
        g2.setColor(new Color(140, 210, 255, 160));
        g2.fillRoundRect(sx + 43, sy + 21, 18, 14, 5, 5);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillRoundRect(sx + 44, sy + 22, 7, 5, 3, 3);

        g2.setColor(new Color(40, 90, 45));
        g2.fillRect(sx + 42, sy + 36, 22, 8);

        g2.setColor(new Color(25, 25, 25));
        g2.fillOval(sx + 6,  sy + 48, 18, 18);
        g2.fillOval(sx + 44, sy + 48, 18, 18);
        g2.setColor(new Color(110, 110, 120));
        g2.fillOval(sx + 10, sy + 52, 10, 10);
        g2.fillOval(sx + 48, sy + 52, 10, 10);

        // Olho pisca quando trash esta voando
        Color trashEye = trashFlying
                ? new Color(255, 255, 0)
                : new Color(255, 30, 30);
        g2.setColor(trashEye);
        g2.fillOval(sx + 46, sy + 24, 8, 8);
        g2.setColor(new Color(100, 0, 0));
        g2.fillOval(sx + 48, sy + 26, 4, 4);
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 49, sy + 26, 2, 2);

        g2.setColor(new Color(180, 0, 220, 70));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(sx + 40, sy + 18, 26, 36, 8, 8);
        g2.drawRect(sx + 2, sy + 26, 40, 28);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawPetroleo(Graphics2D g2, int sx, int sy) {
        float pulse = (float)(Math.sin(animTimer * 1.2f) * 3);

        // Poco de petroleo / plataforma
        g2.setColor(new Color(30, 25, 20));
        g2.fillRoundRect(sx + 8, sy + 38, 52, 26, 6, 6);

        // Estrutura da plataforma
        g2.setColor(new Color(60, 50, 40));
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(sx + 14, sy + 38, sx + 14, sy + 14);
        g2.drawLine(sx + 54, sy + 38, sx + 54, sy + 14);
        g2.drawLine(sx + 14, sy + 14, sx + 54, sy + 14);
        g2.drawLine(sx + 34, sy + 14, sx + 34, sy + 4);
        g2.setStroke(new BasicStroke(1f));

        // Torre de perfuracao
        g2.setColor(new Color(80, 65, 50));
        g2.fillRect(sx + 28, sy + 4, 12, 12);
        g2.setColor(new Color(100, 85, 65));
        g2.fillRect(sx + 30, sy + 6, 8, 8);

        // Charisma da plataforma — chama de gas
        for (int i = 0; i < 3; i++) {
            int fw = (int)(Math.sin(animTimer * 3f + i) * 3);
            g2.setColor(new Color(255, 140 - i * 30, 0, 180 - i * 40));
            g2.fillOval(sx + 54 + fw, sy + 8 - i * 5, 10 + fw, 10);
        }

        // Derramamento de oleo
        g2.setColor(new Color(15, 10, 25, 200));
        int oilW = 46 + (int)pulse;
        g2.fillOval(sx + 8, sy + 56, oilW, 10);

        // Reflexo iridescente
        g2.setColor(new Color(100, 0, 200, 80));
        g2.fillOval(sx + 12, sy + 57, 20, 5);
        g2.setColor(new Color(0, 180, 100, 60));
        g2.fillOval(sx + 28, sy + 58, 16, 4);

        // Barris de oleo
        g2.setColor(new Color(40, 35, 30));
        g2.fillRoundRect(sx + 10, sy + 40, 14, 18, 4, 4);
        g2.fillRoundRect(sx + 28, sy + 40, 14, 18, 4, 4);
        g2.fillRoundRect(sx + 46, sy + 40, 12, 18, 4, 4);
        // Faixas dos barris
        g2.setColor(new Color(60, 55, 50));
        g2.drawLine(sx + 10, sy + 46, sx + 24, sy + 46);
        g2.drawLine(sx + 28, sy + 46, sx + 42, sy + 46);
        g2.drawLine(sx + 46, sy + 46, sx + 58, sy + 46);
        // Sinal de perigo
        g2.setColor(new Color(220, 180, 0));
        g2.fillRect(sx + 13, sy + 41, 8, 7);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 6));
        g2.drawString("OIL", sx + 13, sy + 47);

        // Oleo escorrendo
        g2.setColor(new Color(15, 10, 25, 230));
        g2.fillRoundRect(sx + 16, sy + 38, 3, 22, 2, 2);
        g2.fillRoundRect(sx + 34, sy + 38, 3, 20, 2, 2);

        // Olhinhos malvados na plataforma
        Color eyeC = oilActive
                ? new Color(255, 0, 200)
                : new Color(180, 0, 255);
        g2.setColor(eyeC);
        g2.fillOval(sx + 18, sy + 43, 7, 7);
        g2.fillOval(sx + 43, sy + 43, 7, 7);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 20, sy + 45, 3, 3);
        g2.fillOval(sx + 45, sy + 45, 3, 3);
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 21, sy + 45, 1, 1);
        g2.fillOval(sx + 46, sy + 45, 1, 1);

        // Aura escura de oleo
        g2.setColor(new Color(20, 10, 35, 90));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(sx + 8, sy + 38, 52, 26, 6, 6);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawDesmatamento(Graphics2D g2, int sx, int sy) {
        // Toco de arvore cortada
        g2.setColor(new Color(100, 65, 30));
        g2.fillRoundRect(sx + 16, sy + 44, 36, 22, 6, 6);
        // Anel de crescimento
        g2.setColor(new Color(120, 80, 40));
        g2.drawOval(sx + 20, sy + 48, 28, 14);
        g2.drawOval(sx + 24, sy + 51, 20, 10);
        g2.setColor(new Color(140, 95, 50));
        g2.drawOval(sx + 28, sy + 53, 12, 6);

        // Tronco cortado (madeira exposta)
        g2.setColor(new Color(190, 140, 80));
        g2.fillOval(sx + 18, sy + 44, 32, 16);
        g2.setColor(new Color(160, 115, 60));
        g2.drawOval(sx + 22, sy + 46, 24, 12);
        g2.drawOval(sx + 26, sy + 48, 16, 8);

        // Motosserra — cabo
        g2.setColor(new Color(220, 60, 30));
        g2.fillRoundRect(sx + 4, sy + 24, 30, 22, 6, 6);
        g2.setColor(new Color(180, 40, 10));
        g2.fillRoundRect(sx + 6, sy + 26, 26, 18, 5, 5);

        // Motor da motosserra
        g2.setColor(new Color(60, 60, 65));
        g2.fillRoundRect(sx + 8, sy + 28, 22, 14, 4, 4);
        g2.setColor(new Color(80, 80, 85));
        g2.fillRect(sx + 10, sy + 30, 8, 4);
        g2.fillRect(sx + 20, sy + 30, 8, 4);

        // Barra de corte
        g2.setColor(new Color(140, 130, 120));
        g2.fillRoundRect(sx + 32, sy + 30, 28, 8, 3, 3);
        g2.setColor(new Color(160, 150, 140));
        g2.fillRoundRect(sx + 34, sy + 31, 24, 6, 2, 2);

        // Corrente da motosserra (dentes)
        g2.setColor(new Color(80, 80, 80));
        for (int i = 0; i < 6; i++) {
            g2.fillRect(sx + 33 + i * 4, sy + 29, 3, 3);
            g2.fillRect(sx + 33 + i * 4, sy + 37, 3, 3);
        }

        // Vibration effect durante o dash
        int vx = dashActive ? (int)(Math.random() * 4 - 2) : 0;
        int vy = dashActive ? (int)(Math.random() * 4 - 2) : 0;

        // Serragem voando
        g2.setColor(new Color(200, 160, 80, 180));
        g2.fillOval(sx + 34 + vx, sy + 16 + vy, 6, 6);
        g2.fillOval(sx + 44 + vx, sy + 20 + vy, 4, 4);
        g2.fillOval(sx + 50 + vx, sy + 14 + vy, 5, 5);
        g2.fillOval(sx + 38 + vx, sy + 24 + vy, 3, 3);

        // Olhos da motosserra — vermelhos quando em dash
        Color eyeC = dashActive
                ? new Color(255, 255, 0)
                : new Color(255, 50, 0);
        g2.setColor(eyeC);
        g2.fillOval(sx + 10, sy + 30, 8, 8);
        g2.fillOval(sx + 20, sy + 30, 8, 8);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 12, sy + 32, 4, 4);
        g2.fillOval(sx + 22, sy + 32, 4, 4);
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 13, sy + 32, 2, 2);
        g2.fillOval(sx + 23, sy + 32, 2, 2);

        // Aura verde destruicao
        g2.setColor(new Color(100, 200, 0, 80));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(sx + 4, sy + 24, 30, 22, 6, 6);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawPlasticoOceano(Graphics2D g2, int sx, int sy) {
        float wave = (float)(Math.sin(animTimer * 1.5f) * 3);

        // Massa central de plastico boiando
        g2.setColor(new Color(200, 195, 180, 220));
        int[] px = {sx+8,  sx+4,  sx+10, sx+20, sx+30, sx+44, sx+58,
                sx+60, sx+52, sx+40, sx+28, sx+14};
        int[] py = {sy+28, sy+38, sy+50, sy+54, sy+56, sy+54, sy+48,
                sy+38, sy+28, sy+24, sy+26, sy+24};
        // Ajuste vertical com onda
        for (int i = 0; i < py.length; i++) py[i] += (int)wave;
        g2.fillPolygon(px, py, px.length);

        // Camada suja por cima
        g2.setColor(new Color(170, 160, 140, 180));
        int[] px2 = {sx+12, sx+8,  sx+16, sx+28, sx+40, sx+52, sx+56, sx+44, sx+30, sx+18};
        int[] py2 = {sy+30, sy+40, sy+50, sy+52, sy+50, sy+44, sy+34, sy+28, sy+28, sy+28};
        for (int i = 0; i < py2.length; i++) py2[i] += (int)wave;
        g2.fillPolygon(px2, py2, px2.length);

        // Itens de plastico visiveis na ilha
        // Garrafa
        g2.setColor(new Color(100, 180, 220, 200));
        g2.fillRoundRect(sx + 14, (int)(sy + 34 + wave), 8, 14, 3, 3);
        g2.setColor(new Color(80, 150, 190));
        g2.fillRect(sx + 16, (int)(sy + 32 + wave), 4, 4);
        // Sacola
        g2.setColor(new Color(230, 230, 255, 180));
        g2.fillOval(sx + 28, (int)(sy + 32 + wave), 10, 12);
        // Isopor
        g2.setColor(new Color(245, 245, 248, 200));
        g2.fillRect(sx + 42, (int)(sy + 36 + wave), 12, 8);
        // Cano
        g2.setColor(new Color(80, 160, 80, 180));
        g2.setStroke(new BasicStroke(4f));
        g2.drawLine(sx + 30, (int)(sy + 44 + wave),
                sx + 50, (int)(sy + 40 + wave));
        g2.setStroke(new BasicStroke(1f));

        // Agua ao redor (ondas)
        g2.setColor(new Color(30, 100, 180, 100));
        g2.setStroke(new BasicStroke(2f));
        g2.drawArc(sx - 4, sy + 40, 18, 10, 0, 180);
        g2.drawArc(sx + 50, sy + 44, 18, 10, 0, 180);
        g2.setStroke(new BasicStroke(1f));

        // Peixe morto boiando
        g2.setColor(new Color(180, 180, 160, 180));
        g2.fillOval(sx + 6, (int)(sy + 46 + wave), 12, 6);
        g2.setColor(new Color(140, 140, 120));
        // Olho do peixe (X de morto)
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(sx + 8,  (int)(sy + 47 + wave),
                sx + 10, (int)(sy + 49 + wave));
        g2.drawLine(sx + 10, (int)(sy + 47 + wave),
                sx + 8,  (int)(sy + 49 + wave));
        g2.setStroke(new BasicStroke(1f));

        // Olhos da ilha — tentaculos quando ativos
        Color eyeC = tentacleActive
                ? new Color(0, 255, 220)
                : new Color(30, 180, 255);
        g2.setColor(eyeC);
        g2.fillOval(sx + 22, (int)(sy + 36 + wave), 9, 9);
        g2.fillOval(sx + 36, (int)(sy + 34 + wave), 9, 9);
        g2.setColor(new Color(0, 60, 120));
        g2.fillOval(sx + 24, (int)(sy + 38 + wave), 5, 5);
        g2.fillOval(sx + 38, (int)(sy + 36 + wave), 5, 5);
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 25, (int)(sy + 38 + wave), 2, 2);
        g2.fillOval(sx + 39, (int)(sy + 36 + wave), 2, 2);

        // Aura azul plastico
        g2.setColor(new Color(30, 150, 255, 70));
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(sx + 4, sy + 22, 60, 38);
        g2.setStroke(new BasicStroke(1f));
    }

    public String   getLesson()        { return lesson; }
    public BossType getBossType()      { return bossType; }
    public boolean  isSpecialActive()  { return specialActive; }
    public boolean  isEmpActive()      { return empActive; }
    public boolean  isSmokeActive()    { return smokeActive; }
    public boolean  isTrashFlying()    { return trashFlying; }
    public boolean  isOilActive()      { return oilActive; }
    public boolean  isDashActive()     { return dashActive; }
    public boolean  isTentacleActive() { return tentacleActive; }

    public int getSpecialDamage() {
        return (int)(15 * DifficultySettings.bossDamageMult());
    }
    public int getSecondaryDamage() {
        return (int)(20 * DifficultySettings.bossDamageMult());
    }
}