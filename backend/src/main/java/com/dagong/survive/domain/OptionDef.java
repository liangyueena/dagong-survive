package com.dagong.survive.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OptionDef {

    private String id;
    private String text;
    private Map<String, Integer> effects = Collections.emptyMap();
    private List<String> tags = Collections.emptyList();
    private String hiddenFlag;
    private Integer hiddenDelta;
    private Integer chancePercent;
    private Map<String, Integer> chanceEffects;
    private String chanceText;
    private String special;
    private String followUp;
    private Integer followUpChance;
    private Integer minMoney;
    private String requireFlag;
    private String forbidFlag;
    private String setFlag;
    private Integer setFlagTo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Integer> getEffects() {
        return effects == null ? Collections.<String, Integer>emptyMap() : effects;
    }

    public void setEffects(Map<String, Integer> effects) {
        this.effects = effects;
    }

    public List<String> getTags() {
        return tags == null ? Collections.<String>emptyList() : tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getHiddenFlag() {
        return hiddenFlag;
    }

    public void setHiddenFlag(String hiddenFlag) {
        this.hiddenFlag = hiddenFlag;
    }

    public Integer getHiddenDelta() {
        return hiddenDelta;
    }

    public void setHiddenDelta(Integer hiddenDelta) {
        this.hiddenDelta = hiddenDelta;
    }

    public Integer getChancePercent() {
        return chancePercent;
    }

    public void setChancePercent(Integer chancePercent) {
        this.chancePercent = chancePercent;
    }

    public Map<String, Integer> getChanceEffects() {
        return chanceEffects;
    }

    public void setChanceEffects(Map<String, Integer> chanceEffects) {
        this.chanceEffects = chanceEffects;
    }

    public String getChanceText() {
        return chanceText;
    }

    public void setChanceText(String chanceText) {
        this.chanceText = chanceText;
    }

    public String getSpecial() {
        return special;
    }

    public void setSpecial(String special) {
        this.special = special;
    }

    public String getFollowUp() {
        return followUp;
    }

    public void setFollowUp(String followUp) {
        this.followUp = followUp;
    }

    public Integer getFollowUpChance() {
        return followUpChance;
    }

    public void setFollowUpChance(Integer followUpChance) {
        this.followUpChance = followUpChance;
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

    public String getSetFlag() {
        return setFlag;
    }

    public void setSetFlag(String setFlag) {
        this.setFlag = setFlag;
    }

    public Integer getSetFlagTo() {
        return setFlagTo;
    }

    public void setSetFlagTo(Integer setFlagTo) {
        this.setFlagTo = setFlagTo;
    }
}
