package com.dagong.survive.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.dagong.survive.domain.CareerDef;
import com.dagong.survive.domain.EndingDef;
import com.dagong.survive.domain.EventDef;
import com.dagong.survive.domain.GameData;
import com.dagong.survive.domain.SkillDef;
import com.dagong.survive.engine.GameEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class GameConfig {

    @Bean
    public GameData gameData(ObjectMapper objectMapper) throws IOException {
        GameData data = new GameData();
        for (CareerDef item : read(objectMapper, "game/careers.json", new TypeReference<List<CareerDef>>() {
        })) {
            data.getCareers().put(item.getId(), item);
        }
        for (EventDef item : read(objectMapper, "game/events.json", new TypeReference<List<EventDef>>() {
        })) {
            data.getEvents().put(item.getId(), item);
        }
        for (SkillDef item : read(objectMapper, "game/skills.json", new TypeReference<List<SkillDef>>() {
        })) {
            data.getSkills().put(item.getId(), item);
        }
        for (EndingDef item : read(objectMapper, "game/endings.json", new TypeReference<List<EndingDef>>() {
        })) {
            data.getEndings().put(item.getId(), item);
        }
        if (data.getCareers().size() != 4 || data.getEvents().size() < 20
                || data.getSkills().size() != 5 || data.getEndings().size() < 5) {
            throw new IllegalStateException("配置数量不正确：职业 " + data.getCareers().size()
                    + " / 事件 " + data.getEvents().size() + " / 技能 " + data.getSkills().size()
                    + " / 结局 " + data.getEndings().size());
        }
        return data;
    }

    @Bean
    public GameEngine gameEngine(GameData gameData, GameProperties properties) {
        return new GameEngine(gameData, properties, null);
    }

    private <T> T read(ObjectMapper objectMapper, String path, TypeReference<T> type) throws IOException {
        InputStream in = new ClassPathResource(path).getInputStream();
        try {
            return objectMapper.readValue(in, type);
        } finally {
            in.close();
        }
    }
}
