package com.dagong.survive.domain;

import java.util.HashMap;
import java.util.Map;

public class Attrs {

    private int salary;
    private int money;
    private int hp;
    private int mind;
    private int ability;
    private int boss;
    private int slack;

    public static Attrs copyOf(Attrs src) {
        Attrs copy = new Attrs();
        copy.salary = src.salary;
        copy.money = src.money;
        copy.hp = src.hp;
        copy.mind = src.mind;
        copy.ability = src.ability;
        copy.boss = src.boss;
        copy.slack = src.slack;
        return copy;
    }

    public Map<String, Integer> apply(Map<String, Integer> effects) {
        Map<String, Integer> applied = new HashMap<String, Integer>();
        if (effects == null || effects.isEmpty()) {
            return applied;
        }
        for (Map.Entry<String, Integer> entry : effects.entrySet()) {
            if (entry.getValue() == null || entry.getValue() == 0) {
                continue;
            }
            add(entry.getKey(), entry.getValue());
            applied.put(entry.getKey(), entry.getValue());
        }
        clamp();
        return applied;
    }

    public void add(String key, int delta) {
        if ("salary".equals(key)) {
            salary += delta;
        } else if ("money".equals(key)) {
            money += delta;
        } else if ("hp".equals(key)) {
            hp += delta;
        } else if ("mind".equals(key)) {
            mind += delta;
        } else if ("ability".equals(key)) {
            ability += delta;
        } else if ("boss".equals(key)) {
            boss += delta;
        } else if ("slack".equals(key)) {
            slack += delta;
        }
    }

    public void clamp() {
        salary = Math.max(0, salary);
        money = Math.max(0, money);
        hp = clamp100(hp);
        mind = clamp100(mind);
        ability = clamp100(ability);
        boss = clamp100(boss);
        slack = clamp100(slack);
    }

    private int clamp100(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMind() {
        return mind;
    }

    public void setMind(int mind) {
        this.mind = mind;
    }

    public int getAbility() {
        return ability;
    }

    public void setAbility(int ability) {
        this.ability = ability;
    }

    public int getBoss() {
        return boss;
    }

    public void setBoss(int boss) {
        this.boss = boss;
    }

    public int getSlack() {
        return slack;
    }

    public void setSlack(int slack) {
        this.slack = slack;
    }
}
