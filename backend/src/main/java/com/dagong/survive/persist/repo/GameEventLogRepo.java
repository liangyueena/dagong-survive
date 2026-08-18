package com.dagong.survive.persist.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dagong.survive.persist.entity.GameEventLogEntity;

public interface GameEventLogRepo extends JpaRepository<GameEventLogEntity, Long> {
}
