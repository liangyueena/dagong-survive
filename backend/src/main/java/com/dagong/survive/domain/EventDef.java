package com.dagong.survive.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EventDef {

    private String id;
    private String type;
    private int weight;
    private List<String> careers = Collections.emptyList();
    private String title;
    private String description;
    private boolean minigame;
    private boolean once;
    private boolean fight;
    private boolean repeatable;
    private Integer minMoney;
    private String requireFlag;
    private String forbidFlag;
    private String ui;
    private Map<String, EventCopy> variants;
    private List<EventCopy> alts = Collections.emptyList();
    private List<OptionDef> options = Collections.emptyList();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<String> getCareers() {
        return careers == null ? Collections.<String>emptyList() : careers;
    }

    public void setCareers(List<String> careers) {
        this.careers = careers;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMinigame() {
        return minigame;
    }

    public void setMinigame(boolean minigame) {
        this.minigame = minigame;
    }

    public boolean isOnce() {
        return once;
    }

    public void setOnce(boolean once) {
        this.once = once;
    }

    public boolean isFight() {
        return fight;
    }

    public void setFight(boolean fight) {
        this.fight = fight;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public Integer getMinMoney() {
        return minMoney;
    }

    public void setMinMoney(Integer minMoney) {
        this.minMoney = minMoney;
    }

    public String getRequireFlag() {
        return requireFlag;
    }

    public void setRequireFlag(String requireFlag) {
        this.requireFlag = requireFlag;
    }

    public String getForbidFlag() {
        return forbidFlag;
    }

    public void setForbidFlag(String forbidFlag) {
        this.forbidFlag = forbidFlag;
    }

    public String getUi() {
        return ui;
    }

    public void setUi(String ui) {
        this.ui = ui;
    }

    public Map<String, EventCopy> getVariants() {
        return variants;
    }

    public void setVariants(Map<String, EventCopy> variants) {
        this.variants = variants;
    }

    public List<EventCopy> getAlts() {
        return alts == null ? Collections.<EventCopy>emptyList() : alts;
    }

    public void setAlts(List<EventCopy> alts) {
        this.alts = alts;
    }

    public List<OptionDef> getOptions() {
        return options == null ? Collections.<OptionDef>emptyList() : options;
    }

    public void setOptions(List<OptionDef> options) {
        this.options = options;
    }

    public EventCopy resolveCopy(String careerId) {
        return resolveCopy(careerId, 0);
    }

    public EventCopy resolveCopy(String careerId, int salt) {
        EventCopy copy = new EventCopy();
        copy.setTitle(title);
        copy.setDescription(description);
        List<EventCopy> altList = getAlts();
        if (!altList.isEmpty()) {
            EventCopy alt = altList.get(Math.abs(salt) % altList.size());
            if (alt.getTitle() != null) {
                copy.setTitle(alt.getTitle());
            }
            if (alt.getDescription() != null) {
                copy.setDescription(alt.getDescription());
            }
        }
        if (variants != null && careerId != null && variants.containsKey(careerId)) {
            EventCopy variant = variants.get(careerId);
            if (variant.getTitle() != null) {
                copy.setTitle(variant.getTitle());
            }
            if (variant.getDescription() != null) {
                copy.setDescription(variant.getDescription());
            }
        }
        return copy;
    }

    public OptionDef findOption(String optionId) {
        for (OptionDef option : getOptions()) {
            if (option.getId().equals(optionId)) {
                return option;
            }
        }
        return null;
    }

    public boolean supportsCareer(String careerId) {
        List<String> list = getCareers();
        return list.isEmpty() || list.contains("ALL") || list.contains(careerId);
    }

    public static class EventCopy {
        private String title;
        private String description;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
