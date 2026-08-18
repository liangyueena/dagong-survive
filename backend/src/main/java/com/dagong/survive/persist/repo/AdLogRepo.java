package com.dagong.survive.persist.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dagong.survive.persist.entity.AdLogEntity;

public interface AdLogRepo extends JpaRepository<AdLogEntity, Long> {
}
