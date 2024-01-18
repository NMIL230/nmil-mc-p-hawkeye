package nmil.mceeg.plugin.buildmaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRecord {
    private String playerName;
    private long startTime;
    private int highestLevel;
    private Map<Integer, List<LevelRecord>> levelRecords;

    public GameRecord(String playerName) {
        this.playerName = playerName;
        this.startTime = System.currentTimeMillis();
        this.levelRecords = new HashMap<>();
        highestLevel = 0;
    }

    public void addLevelRecord(int level) {
        long duration = System.currentTimeMillis() - startTime;
        LevelRecord levelRecord = new LevelRecord();
        levelRecords.computeIfAbsent(level, k -> new ArrayList<>()).add(levelRecord);
    }
    public void updateLevelRecord(int level, boolean passed, int score) {
        long duration = System.currentTimeMillis() - startTime;
        List<LevelRecord> records = levelRecords.get(level);
        if (records != null && !records.isEmpty()) {
            LevelRecord latestRecord = records.get(records.size() - 1);
            latestRecord.setPassed(passed);
            latestRecord.setScore(score);
            latestRecord.setDuration(duration);
        }
    }

    public Map<Integer, List<LevelRecord>> getLevelRecords() {
        return levelRecords;
    }

    public int getHighestLevel() {
        return highestLevel;
    }

    public void updateHighestLevel(int difficulty) {
        this.highestLevel = Math.max(this.highestLevel, difficulty);
    }
    public static class LevelRecord {

        private boolean passed;
        private int score;
        private long levelStartTime;
        private long duration;
        public LevelRecord() {
            this.levelStartTime = System.currentTimeMillis();
        }
        public LevelRecord(boolean passed, int score, long duration) {
            this.passed = passed;
            this.score = score;
            this.duration = duration;
            this.levelStartTime = System.currentTimeMillis();
        }
        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public int getScore() {
            return score;
        }
        public long getLevelStartTime() {
            return levelStartTime;
        }

        public void setLevelStartTime(long levelStartTime) {
            this.levelStartTime = levelStartTime;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

    }
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

}
