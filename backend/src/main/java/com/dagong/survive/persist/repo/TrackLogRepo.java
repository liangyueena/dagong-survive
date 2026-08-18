package com.dagong.survive.persist.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dagong.survive.persist.entity.TrackLogEntity;

public interface TrackLogRepo extends JpaRepository<TrackLogEntity, Long> {
}
