package com.dagong.survive.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.dagong.survive.common.GameConstants;
import com.dagong.survive.domain.CareerDef;
import com.dagong.survive.domain.EndingDef;
import com.dagong.survive.domain.EventDef;
import com.dagong.survive.domain.GameData;
import com.dagong.survive.domain.GameState;
import com.dagong.survive.domain.SkillDef;
import com.dagong.survive.engine.GameEngine;
import com.dagong.survive.engine.GameEngine.ChoiceResult;
import com.dagong.survive.persist.entity.AdLogEntity;
import com.dagong.survive.persist.entity.GameEventLogEntity;
import com.dagong.survive.persist.entity.GameRecordEntity;
import com.dagong.survive.persist.entity.TrackLogEntity;
import com.dagong.survive.persist.repo.AdLogRepo;
import com.dagong.survive.persist.repo.GameEventLogRepo;
import com.dagong.survive.persist.repo.GameRecordRepo;
import com.dagong.survive.persist.repo.TrackLogRepo;
import com.dagong.survive.store.GameStateStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GameService {

    private final GameEngine engine;
    private final GameData data;
    private final GameStateStore store;
    private final GameRecordRepo gameRecordRepo;
    private final GameEventLogRepo eventLogRepo;
    private final TrackLogRepo trackLogRepo;
    private final AdLogRepo adLogRepo;
    private final ObjectMapper objectMapper;
    private final QwenClient qwenClient;

    public GameService(GameEngine engine, GameData data, GameStateStore store, GameRecordRepo gameRecordRepo,
            GameEventLogRepo eventLogRepo, TrackLogRepo trackLogRepo, AdLogRepo adLogRepo, ObjectMapper objectMapper,
            QwenClient qwenClient) {
        this.engine = engine;
        this.data = data;
        this.store = store;
        this.gameRecordRepo = gameRecordRepo;
        this.eventLogRepo = eventLogRepo;
        this.trackLogRepo = trackLogRepo;
        this.adLogRepo = adLogRepo;
        this.objectMapper = objectMapper;
        this.qwenClient = qwenClient;
    }

    @Transactional
    public Map<String, Object> start(String userId, String careerId) {
        GameState state = engine.start(userId, careerId);
        persist(state);
        track(userId, state.getGameId(), "game_start", mapOf("career", careerId));
        track(userId, state.getGameId(), "career_select", mapOf("career", careerId));
        Map<String, Object> view = view(state, null, null, null, null);
        trackEventShow(state);
        return view;
    }

    @Transactional
    public Map<String, Object> choose(String userId, String gameId, String optionId) {
        GameState state = require(userId, gameId);
        String eventId = state.getCurrentEventId();
        track(userId, gameId, "event_choose", mapOf("eventId", eventId, "optionId", optionId));
        ChoiceResult result = engine.choose(state, optionId);
        persist(state);
        logEvent(state, eventId, optionId, result.getApplied());
        track(userId, gameId, "event_result", mapOf("eventId", eventId, "optionId", optionId));
        if (result.getNewSkill() != null) {
            track(userId, gameId, "skill_get", mapOf("skillId", result.getNewSkill().getId()));
        }
        if (result.getEnding() != null) {
            track(userId, gameId, "game_end", mapOf("endingId", result.getEnding().getId()));
        }
        Map<String, Object> view = view(state, result.getApplied(), result.getFlavor(), result.getNewSkill(),
                result.getEnding());
        view.put("skipSettle", Boolean.valueOf(result.isSkipSettle()));
        trackEventShow(state);
        return view;
    }

    @Transactional
    public Map<String, Object> minigame(String userId, String gameId, boolean success) {
        GameState state = require(userId, gameId);
        String eventId = state.getCurrentEventId();
        ChoiceResult result = engine.minigame(state, success);
        persist(state);
        logEvent(state, eventId, success ? "SUCCESS" : "FAIL", result.getApplied());
        if (result.getEnding() != null) {
            track(userId, gameId, "game_end", mapOf("endingId", result.getEnding().getId()));
        }
        Map<String, Object> view = view(state, result.getApplied(), result.getFlavor(), result.getNewSkill(),
                result.getEnding());
        trackEventShow(state);
        return view;
    }

    @Transactional
    public Map<String, Object> fight(String userId, String gameId, int hits) {
        GameState state = require(userId, gameId);
        String eventId = state.getCurrentEventId();
        ChoiceResult result = engine.fight(state, hits);
        persist(state);
        logEvent(state, eventId, hits >= 6 ? "WIN" : "LOSE", result.getApplied());
        if (result.getEnding() != null) {
            track(userId, gameId, "game_end", mapOf("endingId", result.getEnding().getId()));
        }
        Map<String, Object> view = view(state, result.getApplied(), result.getFlavor(), result.getNewSkill(),
                result.getEnding());
        trackEventShow(state);
        return view;
    }

    @Transactional
    public Map<String, Object> patrol(String userId, String gameId, boolean success) {
        GameState state = require(userId, gameId);
        ChoiceResult result = engine.patrol(state, success);
        persist(state);
        Map<String, Object> view = view(state, result.getApplied(), result.getFlavor(), null, null);
        view.put("skipSettle", Boolean.TRUE);
        return view;
    }

    @Transactional
    public Map<String, Object> chat(String userId, String gameId, String text, boolean match) {
        GameState state = require(userId, gameId);
        CareerDef career = data.career(state.getCareerId());
        String careerName = career == null ? "打工人" : career.getName();
        ChoiceResult result;
        if (match || state.getFlags().get(GameConstants.FLAG_GIRLFRIEND) == null
                || state.getFlags().get(GameConstants.FLAG_GIRLFRIEND).intValue() <= 0) {
            String her = qwenClient.match(careerName);
            result = engine.startChat(state, her);
        } else {
            if (!StringUtils.hasText(text)) {
                throw new IllegalArgumentException("先写一句再发");
            }
            String her = qwenClient.reply(careerName, state.getChat(), text);
            result = engine.chat(state, text, her);
        }
        persist(state);
        Map<String, Object> view = view(state, result.getApplied(), result.getFlavor(), null, null);
        view.put("skipSettle", Boolean.TRUE);
        return view;
    }

    @Transactional
    public Map<String, Object> sellAsset(String userId, String gameId, String item) {
        GameState state = require(userId, gameId);
        ChoiceResult result = engine.sellAsset(state, item);
        persist(state);
        track(userId, gameId, "asset_sell", mapOf("item", item));
        return view(state, result.getApplied(), result.getFlavor(), null, null);
    }

    @Transactional
    public Map<String, Object> ad(String userId, String gameId, String type) {
        GameState state = require(userId, gameId);
        track(userId, gameId, "ad_show", mapOf("adType", type));
        if (GameConstants.AD_REVIVE.equals(type)) {
            engine.revive(state);
        } else if (GameConstants.AD_RECHOOSE.equals(type)) {
            engine.rechoose(state);
        } else if (GameConstants.AD_REWARD.equals(type)) {
            engine.doubleCoins(state);
        } else {
            throw new IllegalArgumentException("未知广告类型");
        }
        persist(state);
        saveAd(userId, gameId, type, "COMPLETE");
        track(userId, gameId, "ad_complete", mapOf("adType", type));
        return view(state, null, null, null, state.getEndingId() == null ? null : data.ending(state.getEndingId()));
    }

    public Map<String, Object> get(String userId, String gameId) {
        GameState state = require(userId, gameId);
        EndingDef ending = state.getEndingId() == null ? null : data.ending(state.getEndingId());
        return view(state, null, null, null, ending);
    }

    public List<CareerDef> careers() {
        return data.careerList();
    }

    public Map<String, Object> meta() {
        Map<String, Object> meta = new LinkedHashMap<String, Object>();
        meta.put("careers", data.careerList());
        meta.put("skills", data.skillList());
        meta.put("endings", data.endingList());
        return meta;
    }

    public List<Map<String, Object>> rankSurvive() {
        return toRank(gameRecordRepo.findTop20ByStatusOrderByDayDesc(GameConstants.STATUS_ENDED), "day");
    }

    public List<Map<String, Object>> rankWealth() {
        return toRank(gameRecordRepo.findTop20ByStatusOrderByMoneyDesc(GameConstants.STATUS_ENDED), "money");
    }

    @Transactional
    public void track(String userId, String gameId, String eventName, Map<String, Object> payload) {
        TrackLogEntity log = new TrackLogEntity();
        log.setUserId(userId);
        log.setGameId(gameId);
        log.setEventName(eventName);
        log.setPayloadJson(writeJson(payload));
        log.setCreatedAt(new Date());
        trackLogRepo.save(log);
    }

    private void trackEventShow(GameState state) {
        if (GameConstants.STATUS_PLAYING.equals(state.getStatus()) && state.getCurrentEventId() != null) {
            track(state.getUserId(), state.getGameId(), "event_show", mapOf("eventId", state.getCurrentEventId()));
        }
    }

    private GameState require(String userId, String gameId) {
        GameState state = store.get(gameId);
        if (state == null) {
            throw new IllegalArgumentException("对局不存在或已过期");
        }
        if (userId != null && !userId.equals(state.getUserId())) {
            throw new IllegalArgumentException("对局不属于当前用户");
        }
        return state;
    }

    private void persist(GameState state) {
        store.save(state);
        GameRecordEntity record = gameRecordRepo.findByGameId(state.getGameId()).orElse(new GameRecordEntity());
        boolean creating = record.getGameId() == null;
        record.setGameId(state.getGameId());
        record.setUserId(state.getUserId());
        record.setCareerId(state.getCareerId());
        record.setDay(state.getDay());
        record.setMoney(state.getAttrs().getMoney());
        record.setEventCount(state.getEventCount());
        record.setStatus(state.getStatus());
        record.setEndingId(state.getEndingId());
        record.setStateJson(writeJson(state));
        Date now = new Date();
        if (creating) {
            record.setCreatedAt(now);
        }
        record.setUpdatedAt(now);
        gameRecordRepo.save(record);
    }

    private void logEvent(GameState state, String eventId, String optionId, Map<String, Integer> applied) {
        GameEventLogEntity log = new GameEventLogEntity();
        log.setGameId(state.getGameId());
        log.setEventId(eventId);
        log.setOptionId(optionId);
        log.setDay(state.getDay());
        log.setEffectsJson(writeJson(applied));
        log.setCreatedAt(new Date());
        eventLogRepo.save(log);
    }

    private void saveAd(String userId, String gameId, String type, String status) {
        AdLogEntity log = new AdLogEntity();
        log.setUserId(userId);
        log.setGameId(gameId);
        log.setAdType(type);
        log.setStatus(status);
        log.setCreatedAt(new Date());
        adLogRepo.save(log);
    }

    private Map<String, Object> view(GameState state, Map<String, Integer> applied, String flavor, SkillDef newSkill,
            EndingDef ending) {
        Map<String, Object> view = new LinkedHashMap<String, Object>();
        CareerDef career = data.career(state.getCareerId());
        view.put("gameId", state.getGameId());
        view.put("status", state.getStatus());
        view.put("careerId", state.getCareerId());
        view.put("careerName", career == null ? state.getCareerId() : career.getName());
        view.put("workApp", career == null ? "Excel" : career.getWorkApp());
        view.put("day", state.getDay());
        view.put("age", engine.ageOf(state));
        view.put("eventCount", state.getEventCount());
        view.put("attrs", state.getAttrs());
        view.put("skills", skillViews(state));
        view.put("coins", state.getCoins());
        view.put("applied", applied);
        view.put("flavor", flavor);
        view.put("newSkill", newSkill);
        view.put("event", toEventView(state));
        view.put("ending", ending == null ? null : toEndingView(state, ending, career));
        view.put("lastOptionId", state.getLastOptionId());
        view.put("flags", state.getFlags());
        view.put("chat", state.getChat());
        view.put("canRevive", GameConstants.ENDING_FIRED.equals(state.getEndingId()));
        view.put("canRechoose", state.getSnapshot() != null && GameConstants.STATUS_PLAYING.equals(state.getStatus()));
        return view;
    }

    private Map<String, Object> toEventView(GameState state) {
        EventDef event = engine.currentEvent(state);
        if (event == null) {
            return null;
        }
        EventDef.EventCopy copy = event.resolveCopy(state.getCareerId(), state.getDay());
        Map<String, Object> view = new LinkedHashMap<String, Object>();
        view.put("id", event.getId());
        view.put("type", event.getType());
        view.put("title", copy.getTitle());
        view.put("description", copy.getDescription());
        view.put("minigame", event.isMinigame());
        view.put("fight", event.isFight());
        view.put("ui", event.getUi());
        view.put("options", event.getOptions());
        List<String> apps = minigameApps();
        Collections.shuffle(apps);
        view.put("apps", apps);
        return view;
    }

    private List<String> minigameApps() {
        List<String> apps = new ArrayList<String>();
        apps.add("IDEA");
        apps.add("Excel");
        apps.add("微信");
        apps.add("股票");
        apps.add("小说");
        return apps;
    }

    private List<SkillDef> skillViews(GameState state) {
        List<SkillDef> list = new ArrayList<SkillDef>();
        for (String id : state.getSkills()) {
            SkillDef skill = data.skill(id);
            if (skill != null) {
                list.add(skill);
            }
        }
        return list;
    }

    private Map<String, Object> toEndingView(GameState state, EndingDef ending, CareerDef career) {
        Map<String, Object> view = new LinkedHashMap<String, Object>();
        view.put("id", ending.getId());
        view.put("name", ending.getName());
        view.put("lines", ending.getLines());
        view.put("shareHook", ending.getShareHook());
        view.put("age", engine.ageOf(state));
        view.put("day", state.getDay());
        view.put("careerName", career == null ? state.getCareerId() : career.getName());
        view.put("attrs", state.getAttrs());
        view.put("years", Math.max(1, state.getDay() / 365));
        return view;
    }

    private List<Map<String, Object>> toRank(List<GameRecordEntity> records, String metric) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        int rank = 1;
        for (GameRecordEntity record : records) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("rank", rank++);
            row.put("userId", mask(record.getUserId()));
            row.put("careerId", record.getCareerId());
            row.put("day", record.getDay());
            row.put("money", record.getMoney());
            row.put("endingId", record.getEndingId());
            row.put("metric", "day".equals(metric) ? record.getDay() : record.getMoney());
            list.add(row);
        }
        return list;
    }

    private String mask(String userId) {
        if (!StringUtils.hasText(userId) || userId.length() < 4) {
            return "打工人";
        }
        return "工号" + userId.substring(userId.length() - 4);
    }

    private Map<String, Object> mapOf(String k1, Object v1) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(k1, v1);
        return map;
    }

    private Map<String, Object> mapOf(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> map = mapOf(k1, v1);
        map.put(k2, v2);
        return map;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON 写入失败", e);
        }
    }
}
