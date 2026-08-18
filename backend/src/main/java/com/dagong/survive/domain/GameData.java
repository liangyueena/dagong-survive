package com.dagong.survive.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameData {

    private final Map<String, CareerDef> careers = new LinkedHashMap<String, CareerDef>();
    private final Map<String, EventDef> events = new LinkedHashMap<String, EventDef>();
    private final Map<String, SkillDef> skills = new LinkedHashMap<String, SkillDef>();
    private final Map<String, EndingDef> endings = new LinkedHashMap<String, EndingDef>();

    public CareerDef career(String id) {
        return careers.get(id);
    }

    public EventDef event(String id) {
        return events.get(id);
    }

    public SkillDef skill(String id) {
        return skills.get(id);
    }

    public EndingDef ending(String id) {
        return endings.get(id);
    }

    public List<CareerDef> careerList() {
        return new ArrayList<CareerDef>(careers.values());
    }

    public List<EventDef> eventList() {
        return new ArrayList<EventDef>(events.values());
    }

    public List<SkillDef> skillList() {
        return new ArrayList<SkillDef>(skills.values());
    }

    public List<EndingDef> endingList() {
        return new ArrayList<EndingDef>(endings.values());
    }

    public Map<String, CareerDef> getCareers() {
        return careers;
    }

    public Map<String, EventDef> getEvents() {
        return events;
    }

    public Map<String, SkillDef> getSkills() {
        return skills;
    }

    public Map<String, EndingDef> getEndings() {
        return endings;
    }
}
