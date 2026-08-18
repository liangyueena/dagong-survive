package com.dagong.survive.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.dagong.survive.config.GameProperties;
import com.dagong.survive.domain.CareerDef;
import com.dagong.survive.domain.EndingDef;
import com.dagong.survive.domain.EventDef;
import com.dagong.survive.domain.OptionDef;
import com.dagong.survive.domain.GameData;
import com.dagong.survive.domain.GameState;
import com.dagong.survive.domain.SkillDef;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        GameData data = new GameData();
        for (CareerDef item : mapper.readValue(new ClassPathResource("game/careers.json").getInputStream(),
                new TypeReference<List<CareerDef>>() {
                })) {
            data.getCareers().put(item.getId(), item);
        }
        for (EventDef item : mapper.readValue(new ClassPathResource("game/events.json").getInputStream(),
                new TypeReference<List<EventDef>>() {
                })) {
            data.getEvents().put(item.getId(), item);
        }
        for (SkillDef item : mapper.readValue(new ClassPathResource("game/skills.json").getInputStream(),
                new TypeReference<List<SkillDef>>() {
                })) {
            data.getSkills().put(item.getId(), item);
        }
        for (EndingDef item : mapper.readValue(new ClassPathResource("game/endings.json").getInputStream(),
                new TypeReference<List<EndingDef>>() {
                })) {
            data.getEndings().put(item.getId(), item);
        }
        engine = new GameEngine(data, new GameProperties(), new Random(1L));
    }

    @Test
    public void startShouldGiveCurrentEvent() {
        GameState state = engine.start("u1", "programmer");
        assertNotNull(state.getGameId());
        assertEquals("programmer", state.getCareerId());
        assertEquals("PLAYING", state.getStatus());
        assertNotNull(state.getCurrentEventId());
        assertEquals("E61", state.getCurrentEventId());
        assertEquals(20000, state.getAttrs().getMoney());
    }

    @Test
    public void fullRunWithoutCollapseEndsAsWorker() {
        GameState state = engine.start("u1", "programmer");
        state.setEventCount(20);
        assertEquals("worker", engine.resolveEnding(state, true));
        state.getAttrs().setMind(0);
        assertEquals("mental", engine.resolveEnding(state, true));
        state.getAttrs().setMind(75);
        state.getAttrs().setBoss(0);
        assertEquals("fired", engine.resolveEnding(state, true));
        state.getAttrs().setBoss(50);
        state.getFlags().put("jobless", Integer.valueOf(1));
        assertEquals("fired", engine.resolveEnding(state, true));
    }

    @Test
    public void twentyEventsShouldEnd() {
        GameState state = engine.start("u1", "programmer");
        int guard = 0;
        while ("PLAYING".equals(state.getStatus()) && guard++ < 240) {
            EventDef event = engine.currentEvent(state);
            if (event == null) {
                break;
            }
            if (event.isMinigame()) {
                engine.minigame(state, true);
            } else if (event.isFight()) {
                engine.fight(state, 8);
            } else {
                engine.choose(state, firstOpenOption(state, event));
            }
        }
        assertEquals("ENDED", state.getStatus());
        assertNotNull(state.getEndingId());
        assertTrue(state.getSkills().size() >= 1);
        if (state.getFlags().get("jobless") == null && state.getAttrs().getMind() > 0
                && state.getAttrs().getBoss() > 0) {
            assertNotEquals("fired", state.getEndingId());
        }
    }

    @Test
    public void morningCommuteThenArriveAtOffice() {
        GameState state = engine.start("u1", "programmer");
        assertEquals("E61", state.getCurrentEventId());
        engine.choose(state, "B");
        assertNotNull(state.getCurrentEventId());
        assertTrue(!"E61".equals(state.getCurrentEventId()));
        assertTrue(!"E62".equals(state.getCurrentEventId()));
    }

    @Test
    public void noCarDriveBecomesTaxiThenArrive() {
        GameState state = engine.start("u1", "programmer");
        assertTrue(state.getFlags().get("car") == null || state.getFlags().get("car") == 0);
        engine.choose(state, "A");
        assertTrue(!"E62".equals(state.getCurrentEventId()));
        assertTrue(state.getAttrs().getMoney() < 20000);
    }

    @Test
    public void afterCommuteStayAtDeskBeforeGoingHome() {
        GameState state = engine.start("u1", "programmer");
        engine.choose(state, "B");
        int desk = 0;
        int guard = 0;
        while ("PLAYING".equals(state.getStatus()) && guard++ < 8) {
            String id = state.getCurrentEventId();
            if ("E60".equals(id) || "E61".equals(id) || "E62".equals(id) || "E26".equals(id)) {
                break;
            }
            EventDef event = engine.currentEvent(state);
            assertNotNull(event);
            String type = event.getType();
            assertTrue("BOSS".equals(type) || "COLLEAGUE".equals(type) || "CLIENT".equals(type)
                    || "WORK".equals(type) || "MINIGAME".equals(type) || "SLACK".equals(type)
                    || "SPECIAL".equals(type) || "MONEY".equals(type) || "FIGHT".equals(type));
            desk++;
            if (event.isMinigame()) {
                engine.minigame(state, true);
            } else if (event.isFight()) {
                engine.fight(state, 8);
            } else {
                engine.choose(state, firstOpenOption(state, event));
            }
        }
        assertTrue(desk >= 3);
        assertEquals("E60", state.getCurrentEventId());
        assertEquals(1, state.getDay());
        engine.choose(state, "B");
        String night = state.getCurrentEventId();
        assertTrue("E26".equals(night) || "E81".equals(night) || "E82".equals(night)
                || "E83".equals(night) || "E84".equals(night) || "E85".equals(night));
        assertEquals(1, state.getDay());
        engine.choose(state, "C");
        assertEquals("E61", state.getCurrentEventId());
        assertEquals(2, state.getDay());
    }

    @Test
    public void refuseOvertimeCanFightBoss() {
        GameState state = engine.start("u1", "programmer");
        state.setCurrentEventId("E01");
        engine.choose(state, "B");
        assertEquals("E21", state.getCurrentEventId());
        engine.choose(state, "C");
        assertEquals("E22", state.getCurrentEventId());
        engine.fight(state, 8);
        assertEquals("WIN", state.getLastOptionId());
        assertTrue(state.getAttrs().getBoss() < 50);
    }

    @Test
    public void patrolDoesNotChangeEvent() {
        GameState state = engine.start("u1", "programmer");
        String id = state.getCurrentEventId();
        int boss = state.getAttrs().getBoss();
        engine.patrol(state, false);
        assertEquals(id, state.getCurrentEventId());
        assertTrue(state.getAttrs().getBoss() < boss);
    }

    @Test
    public void deskArgueCanFightInAisle() {
        GameState state = engine.start("u1", "programmer");
        state.setCurrentEventId("E98");
        engine.choose(state, "D");
        assertEquals("E99", state.getCurrentEventId());
        engine.fight(state, 8);
        assertEquals("WIN", state.getLastOptionId());
    }

    @Test
    public void canBuyCarWhenRichEnough() {
        GameState state = engine.start("u1", "programmer");
        state.getAttrs().setMoney(90000);
        state.setCurrentEventId("E43");
        engine.choose(state, "A");
        assertEquals(Integer.valueOf(1), state.getFlags().get("car"));
        assertEquals(Integer.valueOf(68000), state.getFlags().get("carPaid"));
        engine.sellAsset(state, "car");
        assertTrue(state.getFlags().get("car") == null || state.getFlags().get("car") == 0);
        assertTrue(state.getAttrs().getMoney() > 30000);
    }

    @Test
    public void losingBossGoesToUnemploymentNotInstantEnd() {
        GameState state = engine.start("u1", "programmer");
        state.getAttrs().setBoss(5);
        state.setCurrentEventId("E21");
        engine.choose(state, "C");
        assertEquals(Integer.valueOf(1), state.getFlags().get("jobless"));
        assertEquals("E49", state.getCurrentEventId());
        assertEquals("PLAYING", state.getStatus());
    }

    @Test
    public void salaryArrivesOnPayday() {
        GameState state = engine.start("u1", "programmer");
        int guard = 0;
        while (!"E70".equals(state.getCurrentEventId()) && "PLAYING".equals(state.getStatus()) && guard++ < 40) {
            EventDef event = engine.currentEvent(state);
            if (event == null) {
                break;
            }
            if (event.isMinigame()) {
                engine.minigame(state, true);
            } else if (event.isFight()) {
                engine.fight(state, 8);
            } else {
                engine.choose(state, firstOpenOption(state, event));
            }
        }
        assertEquals("E70", state.getCurrentEventId());
        int before = state.getAttrs().getMoney();
        engine.choose(state, "A");
        assertTrue(state.getAttrs().getMoney() >= before + 8000);
    }

    @Test
    public void deskSlackCanPlayDouyinStocksMeditate() {
        GameState state = engine.start("u1", "programmer");
        state.getFlags().put("atOffice", Integer.valueOf(1));
        state.setCurrentEventId("E27");
        engine.choose(state, "A");
        assertEquals("E28", state.getCurrentEventId());
        engine.choose(state, "A");
        assertEquals("E28", state.getCurrentEventId());
        engine.choose(state, "D");
        assertEquals(1, state.getDay());
        assertNotEquals("E61", state.getCurrentEventId());

        state.setCurrentEventId("E27");
        engine.choose(state, "B");
        assertEquals("E42", state.getCurrentEventId());
        engine.choose(state, "D");
        assertEquals(1, state.getDay());
        assertNotEquals("E61", state.getCurrentEventId());

        state.setCurrentEventId("E27");
        engine.choose(state, "C");
        assertEquals("E29", state.getCurrentEventId());
        engine.choose(state, "A");
        assertEquals("E29", state.getCurrentEventId());
        engine.choose(state, "D");
        assertEquals(1, state.getDay());
        assertNotEquals("E61", state.getCurrentEventId());
    }

    @Test
    public void deskChatStartsGirlfriend() {
        GameState state = engine.start("u1", "programmer");
        engine.startChat(state, "你是刚下班吗？");
        assertEquals(Integer.valueOf(1), state.getFlags().get("girlfriend"));
        assertEquals(1, state.getChat().size());
        int mind = state.getAttrs().getMind();
        engine.chat(state, "在工位偷发", "少摸鱼。");
        assertEquals(3, state.getChat().size());
        assertTrue(state.getAttrs().getMind() >= mind);
    }

    @Test
    public void overtimeLaterGetsTheBossShoe() {
        GameState state = engine.start("u1", "programmer");
        state.getFlags().put("overtime_debt", Integer.valueOf(2));
        engine.choose(state, "B");
        assertEquals("E30", state.getCurrentEventId());
        engine.choose(state, "A");
        assertTrue(state.getFlags().get("overtime_debt") == null
                || state.getFlags().get("overtime_debt").intValue() < 2);
        assertTrue(state.getUsedEventIds().contains("E30"));
    }

    @Test
    public void chattingHerAsksToMeetThenDinner() {
        GameState state = engine.start("u1", "programmer");
        engine.startChat(state, "你是刚下班吗？");
        engine.chat(state, "刚到工位", "少摸鱼。");
        engine.chat(state, "今晚有空吗", "看你。");
        engine.chat(state, "想听你说话", "那你先把活干完。");
        assertTrue(state.getFlags().get("gfBond").intValue() >= 6);
        state.setCurrentEventId("E60");
        engine.choose(state, "C");
        assertEquals("E32", state.getCurrentEventId());
        engine.choose(state, "A");
        assertEquals("E45", state.getCurrentEventId());
        engine.choose(state, "B");
        assertEquals("E61", state.getCurrentEventId());
        assertTrue(state.getUsedEventIds().contains("E32"));
    }

    private String firstOpenOption(GameState state, EventDef event) {
        String id = event.getId();
        if ("E27".equals(id) || "E28".equals(id) || "E29".equals(id) || "E42".equals(id)) {
            return "D";
        }
        for (OptionDef option : event.getOptions()) {
            if (option.getRequireFlag() != null) {
                Integer value = state.getFlags().get(option.getRequireFlag());
                if (value == null || value.intValue() <= 0) {
                    continue;
                }
            }
            if (option.getMinMoney() != null && state.getAttrs().getMoney() < option.getMinMoney()) {
                continue;
            }
            return option.getId();
        }
        return "B";
    }
}
