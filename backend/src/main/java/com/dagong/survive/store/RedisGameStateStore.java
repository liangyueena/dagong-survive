package com.dagong.survive.store;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.dagong.survive.common.GameConstants;
import com.dagong.survive.domain.GameState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "app", name = "store", havingValue = "redis")
public class RedisGameStateStore implements GameStateStore {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisGameStateStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(GameState state) {
        try {
            redis.opsForValue().set(GameConstants.REDIS_GAME_PREFIX + state.getGameId(),
                    objectMapper.writeValueAsString(state), 2, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("游戏状态序列化失败", e);
        }
    }

    @Override
    public GameState get(String gameId) {
        String json = redis.opsForValue().get(GameConstants.REDIS_GAME_PREFIX + gameId);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, GameState.class);
        } catch (Exception e) {
            throw new IllegalStateException("游戏状态反序列化失败", e);
        }
    }
}
