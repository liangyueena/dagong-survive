package com.dagong.survive.store;

import com.dagong.survive.domain.GameState;

public interface GameStateStore {

    void save(GameState state);

    GameState get(String gameId);
}
