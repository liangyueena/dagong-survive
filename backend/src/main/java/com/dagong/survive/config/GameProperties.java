package com.dagong.survive.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class GameProperties {

    private String store = "memory";
    private int eventsPerRun = 16;
    private int skillEvery = 2;
    private int dayMin = 20;
    private int dayMax = 50;
    private int startAge = 23;
    private int richMoney = 150000;
    private int execAbility = 80;
    private int execBoss = 70;
    private int execMind = 50;
    private int oilSlack = 80;
    private int oilMind = 80;
    private int oilBoss = 30;

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public int getEventsPerRun() {
        return eventsPerRun;
    }

    public void setEventsPerRun(int eventsPerRun) {
        this.eventsPerRun = eventsPerRun;
    }

    public int getSkillEvery() {
        return skillEvery;
    }

    public void setSkillEvery(int skillEvery) {
        this.skillEvery = skillEvery;
    }

    public int getDayMin() {
        return dayMin;
    }

    public void setDayMin(int dayMin) {
        this.dayMin = dayMin;
    }

    public int getDayMax() {
        return dayMax;
    }

    public void setDayMax(int dayMax) {
        this.dayMax = dayMax;
    }

    public int getStartAge() {
        return startAge;
    }

    public void setStartAge(int startAge) {
        this.startAge = startAge;
    }

    public int getRichMoney() {
        return richMoney;
    }

    public void setRichMoney(int richMoney) {
        this.richMoney = richMoney;
    }

    public int getExecAbility() {
        return execAbility;
    }

    public void setExecAbility(int execAbility) {
        this.execAbility = execAbility;
    }

    public int getExecBoss() {
        return execBoss;
    }

    public void setExecBoss(int execBoss) {
        this.execBoss = execBoss;
    }

    public int getExecMind() {
        return execMind;
    }

    public void setExecMind(int execMind) {
        this.execMind = execMind;
    }

    public int getOilSlack() {
        return oilSlack;
    }

    public void setOilSlack(int oilSlack) {
        this.oilSlack = oilSlack;
    }

    public int getOilMind() {
        return oilMind;
    }

    public void setOilMind(int oilMind) {
        this.oilMind = oilMind;
    }

    public int getOilBoss() {
        return oilBoss;
    }

    public void setOilBoss(int oilBoss) {
        this.oilBoss = oilBoss;
    }

    public boolean useRedis() {
        return "redis".equalsIgnoreCase(store);
    }
}
