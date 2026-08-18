package com.dagong.survive.store;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dagong.survive.domain.GameState;

@Component
@ConditionalOnProperty(prefix = "app", name = "store", havingValue = "memory", matchIfMissing = true)
public class MemoryGameStateStore implements GameStateStore {

    private final ConcurrentHashMap<String, GameState> map = new ConcurrentHashMap<String, GameState>();

    @Override
    public void save(GameState state) {
        map.put(state.getGameId(), state);
    }

    @Override
    public GameState get(String gameId) {
        return map.get(gameId);
    }
}
