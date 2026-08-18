package com.dagong.survive.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState {

    private String gameId;
    private String userId;
    private String careerId;
    private int day;
    private int eventCount;
    private Attrs attrs;
    private List<String> skills = new ArrayList<String>();
    private List<String> skillQueue = new ArrayList<String>();
    private List<String> usedEventIds = new ArrayList<String>();
    private Map<String, Integer> flags = new HashMap<String, Integer>();
    private String currentEventId;
    private String lastOptionId;
    private String status;
    private String endingId;
    private int coins;
    private int reviveUsed;
    private int rechooseUsed;
    private String queuedEventId;
    private GameSnapshot snapshot;

    public GameSnapshot toSnapshot() {
        GameSnapshot snap = new GameSnapshot();
        snap.setDay(day);
        snap.setEventCount(eventCount);
        snap.setAttrs(Attrs.copyOf(attrs));
        snap.setSkills(new ArrayList<String>(skills));
        snap.setSkillQueue(new ArrayList<String>(skillQueue));
        snap.setUsedEventIds(new ArrayList<String>(usedEventIds));
        snap.setFlags(new HashMap<String, Integer>(flags));
        snap.setCurrentEventId(currentEventId);
        snap.setStatus(status);
        snap.setEndingId(endingId);
        snap.setCoins(coins);
        snap.setQueuedEventId(queuedEventId);
        return snap;
    }

    public void restore(GameSnapshot snap) {
        this.day = snap.getDay();
        this.eventCount = snap.getEventCount();
        this.attrs = Attrs.copyOf(snap.getAttrs());
        this.skills = new ArrayList<String>(snap.getSkills());
        this.skillQueue = new ArrayList<String>(snap.getSkillQueue());
        this.usedEventIds = new ArrayList<String>(snap.getUsedEventIds());
        this.flags = new HashMap<String, Integer>(snap.getFlags());
        this.currentEventId = snap.getCurrentEventId();
        this.status = snap.getStatus();
        this.endingId = snap.getEndingId();
        this.coins = snap.getCoins();
        this.queuedEventId = snap.getQueuedEventId();
        this.lastOptionId = null;
    }

    public boolean hasSkill(String skillId) {
        return skills.contains(skillId);
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCareerId() {
        return careerId;
    }

    public void setCareerId(String careerId) {
        this.careerId = careerId;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public Attrs getAttrs() {
        return attrs;
    }

    public void setAttrs(Attrs attrs) {
        this.attrs = attrs;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getSkillQueue() {
        return skillQueue;
    }

    public void setSkillQueue(List<String> skillQueue) {
        this.skillQueue = skillQueue;
    }

    public List<String> getUsedEventIds() {
        return usedEventIds;
    }

    public void setUsedEventIds(List<String> usedEventIds) {
        this.usedEventIds = usedEventIds;
    }

    public Map<String, Integer> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, Integer> flags) {
        this.flags = flags;
    }

    public String getCurrentEventId() {
        return currentEventId;
    }

    public void setCurrentEventId(String currentEventId) {
        this.currentEventId = currentEventId;
    }

    public String getLastOptionId() {
        return lastOptionId;
    }

    public void setLastOptionId(String lastOptionId) {
        this.lastOptionId = lastOptionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEndingId() {
        return endingId;
    }

    public void setEndingId(String endingId) {
        this.endingId = endingId;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getReviveUsed() {
        return reviveUsed;
    }

    public void setReviveUsed(int reviveUsed) {
        this.reviveUsed = reviveUsed;
    }

    public int getRechooseUsed() {
        return rechooseUsed;
    }

    public void setRechooseUsed(int rechooseUsed) {
        this.rechooseUsed = rechooseUsed;
    }

    public String getQueuedEventId() {
        return queuedEventId;
    }

    public void setQueuedEventId(String queuedEventId) {
        this.queuedEventId = queuedEventId;
    }

    public GameSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(GameSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public static class GameSnapshot {
        private int day;
        private int eventCount;
        private Attrs attrs;
        private List<String> skills = new ArrayList<String>();
        private List<String> skillQueue = new ArrayList<String>();
        private List<String> usedEventIds = new ArrayList<String>();
        private Map<String, Integer> flags = new HashMap<String, Integer>();
        private String currentEventId;
        private String status;
        private String endingId;
        private int coins;
        private String queuedEventId;

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public int getEventCount() {
            return eventCount;
        }

        public void setEventCount(int eventCount) {
            this.eventCount = eventCount;
        }

        public Attrs getAttrs() {
            return attrs;
        }

        public void setAttrs(Attrs attrs) {
            this.attrs = attrs;
        }

        public List<String> getSkills() {
            return skills;
        }

        public void setSkills(List<String> skills) {
            this.skills = skills;
        }

        public List<String> getSkillQueue() {
            return skillQueue;
        }

        public void setSkillQueue(List<String> skillQueue) {
            this.skillQueue = skillQueue;
        }

        public List<String> getUsedEventIds() {
            return usedEventIds;
        }

        public void setUsedEventIds(List<String> usedEventIds) {
            this.usedEventIds = usedEventIds;
        }

        public Map<String, Integer> getFlags() {
            return flags;
        }

        public void setFlags(Map<String, Integer> flags) {
            this.flags = flags;
        }

        public String getCurrentEventId() {
            return currentEventId;
        }

        public void setCurrentEventId(String currentEventId) {
            this.currentEventId = currentEventId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getEndingId() {
            return endingId;
        }

        public void setEndingId(String endingId) {
            this.endingId = endingId;
        }

        public int getCoins() {
            return coins;
        }

        public void setCoins(int coins) {
            this.coins = coins;
        }

        public String getQueuedEventId() {
            return queuedEventId;
        }

        public void setQueuedEventId(String queuedEventId) {
            this.queuedEventId = queuedEventId;
        }
    }
}
