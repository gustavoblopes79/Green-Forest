package com.greenforest;

import java.io.*;
import java.nio.file.*;


public class SaveData {

    private static final int VERSION = 1;
    private static final String SAVE_FILE = "greenforest_save.dat";

    // Campos salvos
    public int   level;
    public int   xp;
    public int   xpToNextLevel;
    public int   kills;
    public float attackDamage;
    public float attackSpeed;
    public float damageMult;
    public float speedMult;
    public float fireRateMult;
    public int   extraShots;
    public int   vampHeal;
    public int   shield;
    public int   maxShield;
    public int   hp;
    public int   maxHp;
    public float gameTimeSec;
    public int   hordaNumber;
    public int   diffOrdinal;

    /** Salva os dados em arquivo. Retorna true se bem-sucedido. */
    public boolean save() {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(SAVE_FILE)))) {

            dos.writeInt(VERSION);
            dos.writeInt(level);
            dos.writeInt(xp);
            dos.writeInt(xpToNextLevel);
            dos.writeInt(kills);
            dos.writeFloat(attackDamage);
            dos.writeFloat(attackSpeed);
            dos.writeFloat(damageMult);
            dos.writeFloat(speedMult);
            dos.writeFloat(fireRateMult);
            dos.writeInt(extraShots);
            dos.writeInt(vampHeal);
            dos.writeInt(shield);
            dos.writeInt(maxShield);
            dos.writeInt(hp);
            dos.writeInt(maxHp);
            dos.writeFloat(gameTimeSec);
            dos.writeInt(hordaNumber);
            dos.writeInt(diffOrdinal);
            return true;
        } catch (IOException e) {
            System.err.println("[SaveData] Erro ao salvar: " + e.getMessage());
            return false;
        }
    }


    public static SaveData load() {
        if (!Files.exists(Paths.get(SAVE_FILE))) return null;
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(SAVE_FILE)))) {

            int version = dis.readInt();
            if (version != VERSION) {
                System.err.println("[SaveData] Versao incompativel: " + version);
                return null;
            }

            SaveData d = new SaveData();
            d.level        = dis.readInt();
            d.xp           = dis.readInt();
            d.xpToNextLevel = dis.readInt();
            d.kills        = dis.readInt();
            d.attackDamage = dis.readFloat();
            d.attackSpeed  = dis.readFloat();
            d.damageMult   = dis.readFloat();
            d.speedMult    = dis.readFloat();
            d.fireRateMult = dis.readFloat();
            d.extraShots   = dis.readInt();
            d.vampHeal     = dis.readInt();
            d.shield       = dis.readInt();
            d.maxShield    = dis.readInt();
            d.hp           = dis.readInt();
            d.maxHp        = dis.readInt();
            d.gameTimeSec  = dis.readFloat();
            d.hordaNumber  = dis.readInt();
            d.diffOrdinal  = dis.readInt();
            return d;
        } catch (IOException e) {
            System.err.println("[SaveData] Erro ao carregar: " + e.getMessage());
            return null;
        }
    }

    public static boolean hasSave() {
        return Files.exists(Paths.get(SAVE_FILE));
    }

    public static void deleteSave() {
        try { Files.deleteIfExists(Paths.get(SAVE_FILE)); }
        catch (IOException e) {
            System.err.println("[SaveData] Erro ao deletar: " + e.getMessage());
        }
    }
}