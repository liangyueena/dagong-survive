package com.dagong.survive.persist.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dagong.survive.persist.entity.GameRecordEntity;

public interface GameRecordRepo extends JpaRepository<GameRecordEntity, Long> {

    Optional<GameRecordEntity> findByGameId(String gameId);

    List<GameRecordEntity> findTop20ByStatusOrderByDayDesc(String status);

    List<GameRecordEntity> findTop20ByStatusOrderByMoneyDesc(String status);
}
