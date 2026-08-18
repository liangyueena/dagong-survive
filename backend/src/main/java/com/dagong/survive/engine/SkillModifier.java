package com.dagong.survive.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dagong.survive.common.GameConstants;
import com.dagong.survive.domain.EventDef;
import com.dagong.survive.domain.GameState;
import com.dagong.survive.domain.OptionDef;

public final class SkillModifier {

    private SkillModifier() {
    }

    public static Map<String, Integer> modify(GameState state, EventDef event, OptionDef option,
            Map<String, Integer> raw) {
        Map<String, Integer> out = new HashMap<String, Integer>(raw);
        List<String> tags = option.getTags();
        boolean overtime = tags.contains(GameConstants.TAG_OVERTIME);
        boolean slack = tags.contains(GameConstants.TAG_SLACK);
        boolean ppt = tags.contains(GameConstants.TAG_PPT);
        String type = event.getType();

        if (state.hasSkill(GameConstants.SKILL_SLACKER) && slack) {
            scalePositive(out, 1.2);
            scaleNegative(out, 0.8);
        }
        if (state.hasSkill(GameConstants.SKILL_GRINDER) && overtime) {
            scaleKey(out, "ability", 2.0);
            scaleKeyNegative(out, "hp", 1.5);
        }
        if (state.hasSkill(GameConstants.SKILL_EQ)
                && (GameConstants.TYPE_BOSS.equals(type) || GameConstants.TYPE_COLLEAGUE.equals(type))) {
            scalePositive(out, 1.3);
        }
        if (state.hasSkill(GameConstants.SKILL_PPT) && (ppt || hasPositive(out, "boss"))) {
            scaleKeyPositive(out, "boss", 1.2);
        }
        if (state.hasSkill(GameConstants.SKILL_VETERAN) && slack) {
            scaleKeyNegative(out, "boss", 0.7);
        }
        return out;
    }

    private static boolean hasPositive(Map<String, Integer> effects, String key) {
        Integer value = effects.get(key);
        return value != null && value > 0;
    }

    private static void scalePositive(Map<String, Integer> effects, double factor) {
        for (Map.Entry<String, Integer> entry : new HashMap<String, Integer>(effects).entrySet()) {
            if (entry.getValue() > 0) {
                effects.put(entry.getKey(), (int) Math.round(entry.getValue() * factor));
            }
        }
    }

    private static void scaleNegative(Map<String, Integer> effects, double factor) {
        for (Map.Entry<String, Integer> entry : new HashMap<String, Integer>(effects).entrySet()) {
            if (entry.getValue() < 0) {
                effects.put(entry.getKey(), (int) Math.round(entry.getValue() * factor));
            }
        }
    }

    private static void scaleKey(Map<String, Integer> effects, String key, double factor) {
        Integer value = effects.get(key);
        if (value != null && value > 0) {
            effects.put(key, (int) Math.round(value * factor));
        }
    }

    private static void scaleKeyPositive(Map<String, Integer> effects, String key, double factor) {
        Integer value = effects.get(key);
        if (value != null && value > 0) {
            effects.put(key, (int) Math.round(value * factor));
        }
    }

    private static void scaleKeyNegative(Map<String, Integer> effects, String key, double factor) {
        Integer value = effects.get(key);
        if (value != null && value < 0) {
            effects.put(key, (int) Math.round(value * factor));
        }
    }
}
