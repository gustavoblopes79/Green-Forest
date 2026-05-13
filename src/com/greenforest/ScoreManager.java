package com.greenforest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreManager {

    public static final int MAX_ENTRIES = 10;

    private static final String SCORE_FILE =
            System.getProperty("user.home") + File.separator
                    + ".greenforest_scores.dat";

    public static class ScoreEntry implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public long   score;
        public int    kills;
        public int    horda;
        public int    level;
        public float  timeSec;

        public ScoreEntry(String name, long score,
                          int kills, int horda,
                          int level, float timeSec) {
            this.name    = name;
            this.score   = score;
            this.kills   = kills;
            this.horda   = horda;
            this.level   = level;
            this.timeSec = timeSec;
        }
    }

    public static long calcScore(int kills, int horda,
                                 int level, float timeSec) {
        return calcRunScore(kills, horda, level)
                + calcTimeBonus(timeSec);
    }

    public static long calcRunScore(int kills, int horda, int level) {
        long killScore  = kills * 100L;
        long hordaBonus = Math.max(0, horda - 1) * 500L;
        long levelBonus = Math.max(0, level - 1) * 300L;
        return killScore + hordaBonus + levelBonus;
    }

    public static long calcTimeBonus(float timeSec) {
        long minutes = (long)(timeSec / 60f);
        return minutes * 120L;
    }

    @SuppressWarnings("unchecked")
    public static List<ScoreEntry> load() {
        File f = new File(SCORE_FILE);
        if (!f.exists()) return new ArrayList<>();

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<ScoreEntry>) obj;
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }

    public static void save(List<ScoreEntry> entries) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(SCORE_FILE))) {
            oos.writeObject(entries);
        } catch (Exception ignored) {
        }
    }

    public static int submitScore(String name, long score,
                                  int kills, int horda,
                                  int level, float timeSec) {
        List<ScoreEntry> list = load();
        ScoreEntry entry =
                new ScoreEntry(name, score, kills, horda, level, timeSec);

        list.add(entry);
        list.sort((a, b) -> Long.compare(b.score, a.score));

        int pos = list.indexOf(entry);

        if (list.size() > MAX_ENTRIES) {
            list = new ArrayList<>(list.subList(0, MAX_ENTRIES));
        } else {
            list = new ArrayList<>(list);
        }

        save(list);
        return pos >= 0 && pos < MAX_ENTRIES ? pos : -1;
    }

    public static boolean isHighScore(long score) {
        List<ScoreEntry> list = load();
        if (list.size() < MAX_ENTRIES) return true;
        return score > list.get(list.size() - 1).score;
    }

    public static long getBestScore() {
        List<ScoreEntry> list = load();
        if (list.isEmpty()) return 0L;
        return list.get(0).score;
    }

    public static List<ScoreEntry> loadSafeTop() {
        List<ScoreEntry> list = load();
        if (list.size() <= MAX_ENTRIES) return list;
        return new ArrayList<>(list.subList(0, MAX_ENTRIES));
    }
}