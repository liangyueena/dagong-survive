package com.dagong.survive.persist.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "game_event_log")
public class GameEventLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String gameId;

    @Column(length = 32)
    private String eventId;

    @Column(length = 16)
    private String optionId;

    @Column(name = "game_day")
    private int day;

    @Lob
    private String effectsJson;

    private Date createdAt;

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setEffectsJson(String effectsJson) {
        this.effectsJson = effectsJson;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
