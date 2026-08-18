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
@Table(name = "track_log")
public class TrackLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String userId;

    @Column(length = 64)
    private String gameId;

    @Column(nullable = false, length = 64)
    private String eventName;

    @Lob
    private String payloadJson;

    private Date createdAt;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
