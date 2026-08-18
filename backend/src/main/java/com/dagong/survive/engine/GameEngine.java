package com.dagong.survive.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.dagong.survive.common.GameConstants;
import com.dagong.survive.config.GameProperties;
import com.dagong.survive.domain.Attrs;
import com.dagong.survive.domain.CareerDef;
import com.dagong.survive.domain.ChatLine;
import com.dagong.survive.domain.EndingDef;
import com.dagong.survive.domain.EventDef;
import com.dagong.survive.domain.GameData;
import com.dagong.survive.domain.GameState;
import com.dagong.survive.domain.OptionDef;
import com.dagong.survive.domain.SkillDef;

public class GameEngine {

    private final GameData data;
    private final GameProperties props;
    private final Random random;

    public GameEngine(GameData data, GameProperties props, Random random) {
        this.data = data;
        this.props = props;
        this.random = random == null ? new Random() : random;
    }

    public GameState start(String userId, String careerId) {
        CareerDef career = data.career(careerId);
        if (career == null) {
            throw new IllegalArgumentException("未知职业");
        }
        GameState state = new GameState();
        state.setGameId(UUID.randomUUID().toString().replace("-", ""));
        state.setUserId(userId);
        state.setCareerId(careerId);
        state.setDay(1);
        state.setEventCount(0);
        state.setAttrs(Attrs.copyOf(career.getAttrs()));
        state.setStatus(GameConstants.STATUS_PLAYING);
        List<String> queue = new ArrayList<String>();
        for (SkillDef skill : data.skillList()) {
            queue.add(skill.getId());
        }
        Collections.shuffle(queue, random);
        state.setSkillQueue(queue);
        state.setCurrentEventId("E61");
        return state;
    }

    public ChoiceResult choose(GameState state, String optionId) {
        if (!GameConstants.STATUS_PLAYING.equals(state.getStatus())) {
            throw new IllegalStateException("本局已经结束");
        }
        EventDef event = data.event(state.getCurrentEventId());
        if (event == null) {
            throw new IllegalStateException("当前事件不存在");
        }
        if (event.isMinigame()) {
            throw new IllegalStateException("请完成老板来了小游戏");
        }
        if (event.isFight()) {
            throw new IllegalStateException("请完成对打");
        }
        OptionDef option = event.findOption(optionId);
        if (option == null) {
            throw new IllegalArgumentException("无效选项");
        }
        state.setSnapshot(state.toSnapshot());
        if (!optionOpen(state, option) && GameConstants.FLAG_CAR.equals(option.getRequireFlag())
                && flag(state, GameConstants.FLAG_CAR) <= 0) {
            OptionDef taxi = event.findOption("D");
            if ("E61".equals(event.getId()) && taxi != null && optionOpen(state, taxi)) {
                ChoiceResult taxiResult = settle(state, event, taxi);
                if (taxiResult.getFlavor() == null) {
                    taxiResult.setFlavor("你没车，打开了打车软件。司机说马上到。");
                }
                taxiResult.setSkipSettle(true);
                return taxiResult;
            }
            if ("E60".equals(event.getId())) {
                Map<String, Integer> taxiHome = new HashMap<String, Integer>();
                taxiHome.put("money", Integer.valueOf(-35));
                taxiHome.put("hp", Integer.valueOf(2));
                taxiHome.put("mind", Integer.valueOf(2));
                ChoiceResult taxiHomeResult = applyAndContinue(state, event, taxiHome, Collections.<String>emptyList(),
                        "你没车，只能打车回去。司机把导航开成了避开拥堵。", "D");
                taxiHomeResult.setSkipSettle(true);
                return taxiHomeResult;
            }
        }
        if (!optionOpen(state, option)) {
            Map<String, Integer> failed = new HashMap<String, Integer>();
            failed.put("mind", -2);
            return applyAndContinue(state, event, failed, Collections.<String>emptyList(),
                    "你掏出手机付款，余额不够，或这事还没轮到你。", optionId);
        }
        return settle(state, event, option);
    }

    public ChoiceResult minigame(GameState state, boolean success) {
        if (!GameConstants.STATUS_PLAYING.equals(state.getStatus())) {
            throw new IllegalStateException("本局已经结束");
        }
        EventDef event = data.event(state.getCurrentEventId());
        if (event == null || !event.isMinigame()) {
            throw new IllegalStateException("当前不是老板来了");
        }
        state.setSnapshot(state.toSnapshot());
        Map<String, Integer> effects = new HashMap<String, Integer>();
        String flavor;
        if (success) {
            effects.put("boss", 5);
            flavor = "老板：「认真工作啊。」";
        } else {
            int penalty = -20;
            if (state.hasSkill(GameConstants.SKILL_VETERAN)) {
                penalty = (int) Math.round(penalty * 0.7);
            }
            effects.put("boss", penalty);
            effects.put("slack", 10);
            flavor = "老板：「你刚才在看什么？」";
        }
        return applyAndContinue(state, event, effects, Collections.<String>emptyList(), flavor, "minigame");
    }

    public ChoiceResult fight(GameState state, int hits) {
        if (!GameConstants.STATUS_PLAYING.equals(state.getStatus())) {
            throw new IllegalStateException("本局已经结束");
        }
        EventDef event = data.event(state.getCurrentEventId());
        if (event == null || !event.isFight()) {
            throw new IllegalStateException("当前不是打斗");
        }
        state.setSnapshot(state.toSnapshot());
        boolean win = hits >= 6;
        Map<String, Integer> effects = new HashMap<String, Integer>();
        String flavor;
        boolean vsBoss = "E22".equals(event.getId());
        if (win) {
            effects.put("mind", 16);
            effects.put("hp", -12);
            effects.put("slack", 8);
            if (vsBoss) {
                effects.put("boss", -22);
                flavor = "你把老板按在茶水间的冰箱上。监控灯还在闪。";
            } else {
                effects.put("boss", -8);
                flavor = "他的工牌掉了。你也摔疼了。但你没先怂。";
            }
        } else {
            effects.put("hp", -26);
            effects.put("mind", -14);
            if (vsBoss) {
                effects.put("boss", -10);
                flavor = "你被按在自动贩卖机上。纸杯还在转。";
            } else {
                effects.put("boss", -6);
                flavor = "监控拍到了。HR 明天会找你。";
            }
        }
        return applyAndContinue(state, event, effects, Collections.<String>emptyList(), flavor, win ? "WIN" : "LOSE");
    }

    public ChoiceResult patrol(GameState state, boolean success) {
        if (!GameConstants.STATUS_PLAYING.equals(state.getStatus())) {
            throw new IllegalStateException("本局已经结束");
        }
        Map<String, Integer> effects = new HashMap<String, Integer>();
        String flavor;
        if (success) {
            effects.put("boss", Integer.valueOf(2));
            flavor = "你切回去了。他在你工位停了一秒，走了。";
        } else {
            int penalty = -10;
            if (state.hasSkill(GameConstants.SKILL_VETERAN)) {
                penalty = -7;
            }
            effects.put("boss", Integer.valueOf(penalty));
            effects.put("slack", Integer.valueOf(6));
            flavor = "他看见了你的屏幕。走廊里有人假装没看见。";
        }
        Map<String, Integer> applied = state.getAttrs().apply(effects);
        ChoiceResult result = new ChoiceResult();
        result.setApplied(applied);
        result.setFlavor(flavor);
        result.setNextEvent(currentEvent(state));
        result.setSkipSettle(true);
        return result;
    }

    public ChoiceResult sellAsset(GameState state, String item) {
        if (!GameConstants.STATUS_PLAYING.equals(state.getStatus())) {
            throw new IllegalStateException("本局已经结束");
        }
        state.setSnapshot(state.toSnapshot());
        Map<String, Integer> effects = new HashMap<String, Integer>();
        String flavor;
        if ("car".equals(item)) {
            if (flag(state, GameConstants.FLAG_CAR) <= 0) {
                throw new IllegalStateException("你还没有车");
            }
            int paid = Math.max(flag(state, "carPaid"), 32000);
            int gain = (int) Math.round(paid * 0.62);
            add(effects, "money", gain);
            add(effects, "mind", -4);
            state.getFlags().remove(GameConstants.FLAG_CAR);
            state.getFlags().remove("carPaid");
            flavor = "车过户了。钥匙不在你手上了。回了 " + gain + "。";
        } else if ("house".equals(item)) {
            if (flag(state, GameConstants.FLAG_HOUSE) <= 0) {
                throw new IllegalStateException("你还没有房");
            }
            int paid = Math.max(flag(state, "housePaid"), 80000);
            int gain = (int) Math.round(paid * 0.78);
            add(effects, "money", gain);
            add(effects, "mind", -6);
            state.getFlags().remove(GameConstants.FLAG_HOUSE);
            state.getFlags().remove("housePaid");
            flavor = "房子挂出去了。中介又在笑。回了 " + gain + "。";
        } else if ("married".equals(item) || "wife".equals(item)) {
            if (flag(state, GameConstants.FLAG_MARRIED) <= 0) {
                throw new IllegalStateException("你还没成家");
            }
            int paid = Math.max(flag(state, "marriedPaid"), 18000);
            int gain = (int) Math.round(paid * 0.4);
            add(effects, "money", gain);
            add(effects, "mind", -14);
            add(effects, "hp", -4);
            state.getFlags().remove(GameConstants.FLAG_MARRIED);
            state.getFlags().remove("marriedPaid");
            flavor = "她把钥匙放下。彩礼剩了一点回来：" + gain + "。";
        } else {
            throw new IllegalArgumentException("未知资产");
        }
        Map<String, Integer> applied = state.getAttrs().apply(effects);
        ChoiceResult result = new ChoiceResult();
        result.setApplied(applied);
        result.setFlavor(flavor);
        result.setNextEvent(currentEvent(state));
        return result;
    }

    public ChoiceResult startChat(GameState state, String herText) {
        if (!GameConstants.STATUS_PLAYING.equals(state.getStatus())) {
            throw new IllegalStateException("本局已经结束");
        }
        if (flag(state, GameConstants.FLAG_GIRLFRIEND) <= 0) {
            state.getFlags().put(GameConstants.FLAG_GIRLFRIEND, Integer.valueOf(1));
        }
        addLine(state, "her", herText);
        addFlag(state, "gfBond", 1);
        Map<String, Integer> effects = new HashMap<String, Integer>();
        effects.put("mind", Integer.valueOf(3));
        Map<String, Integer> applied = state.getAttrs().apply(effects);
        ChoiceResult result = new ChoiceResult();
        result.setApplied(applied);
        result.setFlavor(herText);
        result.setSkipSettle(true);
        result.setNextEvent(currentEvent(state));
        return result;
    }

    public ChoiceResult chat(GameState state, String userText, String herText) {
        if (!GameConstants.STATUS_PLAYING.equals(state.getStatus())) {
            throw new IllegalStateException("本局已经结束");
        }
        if (flag(state, GameConstants.FLAG_GIRLFRIEND) <= 0) {
            throw new IllegalStateException("你还没有在聊的人");
        }
        addLine(state, "me", userText);
        addLine(state, "her", herText);
        addFlag(state, "gfBond", 2);
        Map<String, Integer> effects = new HashMap<String, Integer>();
        effects.put("mind", Integer.valueOf(2));
        if (flag(state, "atOffice") > 0) {
            effects.put("slack", Integer.valueOf(4));
            addFlag(state, "slack_debt", 1);
        }
        Map<String, Integer> applied = state.getAttrs().apply(effects);
        ChoiceResult result = new ChoiceResult();
        result.setApplied(applied);
        result.setFlavor(herText);
        result.setSkipSettle(true);
        result.setNextEvent(currentEvent(state));
        return result;
    }

    private void addLine(GameState state, String role, String text) {
        state.getChat().add(new ChatLine(role, text));
        while (state.getChat().size() > 16) {
            state.getChat().remove(0);
        }
    }

    public void revive(GameState state) {
        if (!GameConstants.ENDING_FIRED.equals(state.getEndingId())) {
            throw new IllegalStateException("只有被优化可以复活");
        }
        Attrs attrs = state.getAttrs();
        if (attrs.getBoss() < 35) {
            attrs.setBoss(35);
        }
        if (attrs.getMind() < 20) {
            attrs.setMind(20);
        }
        state.setStatus(GameConstants.STATUS_PLAYING);
        state.setEndingId(null);
        state.setReviveUsed(state.getReviveUsed() + 1);
        pickNextEvent(state);
    }

    public void rechoose(GameState state) {
        if (state.getSnapshot() == null) {
            throw new IllegalStateException("没有可重选的步骤");
        }
        state.restore(state.getSnapshot());
        state.setSnapshot(null);
        state.setRechooseUsed(state.getRechooseUsed() + 1);
    }

    public int doubleCoins(GameState state) {
        if (!GameConstants.STATUS_ENDED.equals(state.getStatus())) {
            throw new IllegalStateException("结算后才能领取奖励");
        }
        state.setCoins(state.getCoins() * 2);
        return state.getCoins();
    }

    public int ageOf(GameState state) {
        return props.getStartAge() + state.getDay() / 365;
    }

    public EventDef currentEvent(GameState state) {
        return data.event(state.getCurrentEventId());
    }

    private ChoiceResult settle(GameState state, EventDef event, OptionDef option) {
        Map<String, Integer> effects = new HashMap<String, Integer>(option.getEffects());
        String flavor = null;
        if (option.getChancePercent() != null && option.getChancePercent() > 0) {
            if (random.nextInt(100) < option.getChancePercent()) {
                merge(effects, option.getChanceEffects());
                flavor = option.getChanceText();
            }
        }
        String specialFlavor = applySpecial(state, option, effects);
        if (flavor == null) {
            flavor = specialFlavor;
        }
        effects = SkillModifier.modify(state, event, option, effects);
        if (option.getHiddenFlag() != null && option.getHiddenDelta() != null) {
            Integer old = state.getFlags().get(option.getHiddenFlag());
            int next = (old == null ? 0 : old) + option.getHiddenDelta();
            state.getFlags().put(option.getHiddenFlag(), next);
        }
        if (option.getSetFlag() != null) {
            int value = option.getSetFlagTo() == null ? 1 : option.getSetFlagTo();
            state.getFlags().put(option.getSetFlag(), value);
            Integer spent = effects.get("money");
            if (spent != null && spent < 0
                    && ("car".equals(option.getSetFlag()) || "house".equals(option.getSetFlag())
                            || "married".equals(option.getSetFlag()))) {
                state.getFlags().put(option.getSetFlag() + "Paid", Integer.valueOf(-spent.intValue()));
            }
        }
        state.setLastOptionId(option.getId());
        if (option.getFollowUp() != null) {
            int chance = option.getFollowUpChance() == null ? 100 : option.getFollowUpChance();
            if (random.nextInt(100) < chance) {
                state.setQueuedEventId(option.getFollowUp());
            }
        }
        return applyAndContinue(state, event, effects, option.getTags(), flavor, option.getId());
    }

    private ChoiceResult applyAndContinue(GameState state, EventDef event, Map<String, Integer> effects,
            List<String> tags, String flavor, String optionId) {
        int dayGain = isEveningWrap(event, state) ? 1 : 0;
        state.setDay(state.getDay() + dayGain);
        Map<String, Integer> applied = state.getAttrs().apply(effects);
        Integer moneyDelta = applied.get("money");
        if (moneyDelta == null) {
            applied.put("money", Integer.valueOf(0));
        }
        applied.put("day", Integer.valueOf(dayGain));
        noteChoiceDebts(state, tags, event);

        if (state.getAttrs().getBoss() <= 0 && flag(state, GameConstants.FLAG_JOBLESS) == 0
                && state.getAttrs().getMind() > 0) {
            state.getAttrs().setBoss(12);
            state.getFlags().put(GameConstants.FLAG_JOBLESS, 1);
            state.setQueuedEventId("E49");
            if (flavor == null) {
                flavor = "HR 发来会议邀请：组织优化沟通。工牌暂时还在你脖子上。";
            }
        }

        if (!event.isRepeatable()) {
            state.getUsedEventIds().add(event.getId());
            state.setEventCount(state.getEventCount() + 1);
        }
        state.setLastOptionId(optionId);

        SkillDef newSkill = event.isRepeatable() ? null : maybeGrantSkill(state);
        maybeQueueLife(state, event);
        String endingId = resolveEnding(state, false);
        EventDef next = null;
        if (endingId == null) {
            next = pickNextEvent(state);
        } else {
            finish(state, endingId);
        }
        ChoiceResult result = new ChoiceResult();
        result.setApplied(applied);
        result.setFlavor(flavor);
        result.setNewSkill(newSkill);
        result.setNextEvent(next);
        result.setEnding(endingId == null ? null : data.ending(endingId));
        result.setTags(tags);
        result.setSkipSettle(shouldSkipSettle(event, flavor));
        return result;
    }

    private SkillDef maybeGrantSkill(GameState state) {
        if (state.getEventCount() <= 0 || state.getEventCount() % props.getSkillEvery() != 0) {
            return null;
        }
        if (state.getSkills().size() >= 5 || state.getSkillQueue().isEmpty()) {
            return null;
        }
        String skillId = state.getSkillQueue().remove(0);
        state.getSkills().add(skillId);
        return data.skill(skillId);
    }

    public String resolveEnding(GameState state, boolean force) {
        Attrs attrs = state.getAttrs();
        if (attrs.getMind() <= 0) {
            return GameConstants.ENDING_MENTAL;
        }
        if (attrs.getBoss() <= 0) {
            return GameConstants.ENDING_FIRED;
        }
        if (!force && attrs.getMind() > 0 && attrs.getBoss() > 0
                && (state.getEventCount() < props.getEventsPerRun() || state.getQueuedEventId() != null)) {
            return null;
        }
        if (flag(state, GameConstants.FLAG_JOBLESS) > 0) {
            return GameConstants.ENDING_FIRED;
        }
        if (flag(state, GameConstants.FLAG_HOUSE) > 0 && flag(state, GameConstants.FLAG_MARRIED) > 0) {
            return GameConstants.ENDING_HOME;
        }
        if (attrs.getMoney() >= props.getRichMoney()) {
            return GameConstants.ENDING_RICH;
        }
        if (attrs.getAbility() > props.getExecAbility()
                && attrs.getBoss() > props.getExecBoss()
                && attrs.getMind() > props.getExecMind()) {
            return GameConstants.ENDING_EXEC;
        }
        if (attrs.getSlack() > props.getOilSlack()
                && attrs.getMind() > props.getOilMind()
                && attrs.getBoss() > props.getOilBoss()) {
            return GameConstants.ENDING_OIL;
        }
        return GameConstants.ENDING_WORKER;
    }

    private void finish(GameState state, String endingId) {
        state.setStatus(GameConstants.STATUS_ENDED);
        state.setEndingId(endingId);
        state.setCurrentEventId(null);
        if (state.getCoins() <= 0) {
            state.setCoins(1000);
        }
    }

    private EventDef pickNextEvent(GameState state) {
        String queued = state.getQueuedEventId();
        if (queued != null) {
            state.setQueuedEventId(null);
            EventDef queuedEvent = data.event(queued);
            if (queuedEvent != null && !state.getUsedEventIds().contains(queued)
                    && eligible(state, queuedEvent)) {
                state.setCurrentEventId(queued);
                return queuedEvent;
            }
        }
        boolean deskOnly = flag(state, "atOffice") > 0;
        List<Weighted> pool = collectPool(state, deskOnly);
        if (pool.isEmpty() && deskOnly) {
            state.getFlags().put("atOffice", Integer.valueOf(0));
            pool = collectPool(state, false);
        }
        if (pool.isEmpty()) {
            String endingId = resolveEnding(state, true);
            finish(state, endingId);
            return null;
        }
        int total = 0;
        for (Weighted item : pool) {
            total += item.weight;
        }
        int ticket = random.nextInt(total);
        int cursor = 0;
        EventDef picked = pool.get(pool.size() - 1).event;
        for (Weighted item : pool) {
            cursor += item.weight;
            if (ticket < cursor) {
                picked = item.event;
                break;
            }
        }
        state.setCurrentEventId(picked.getId());
        return picked;
    }

    private String applySpecial(GameState state, OptionDef option, Map<String, Integer> effects) {
        String special = option.getSpecial();
        if (special == null) {
            return null;
        }
        Attrs attrs = state.getAttrs();
        if ("deposit_salary".equals(special)) {
            add(effects, "money", attrs.getSalary());
        } else if ("invest".equals(special)) {
            int slice = (int) Math.round(attrs.getMoney() * 0.2);
            if (random.nextBoolean()) {
                add(effects, "money", (int) Math.round(slice * 0.15));
                return "这笔钱看起来变多了一点。";
            }
            add(effects, "money", -(int) Math.round(slice * 0.10));
            return "账户缩水了。你把 App 删了又装回来。";
        } else if ("headhunt".equals(special)) {
            if (attrs.getAbility() > 70) {
                add(effects, "money", attrs.getSalary() * 8);
                return "猎头说下周入职。你还没跟现在的老板说。";
            }
            add(effects, "mind", -6);
            return "对方看完简历，说再联系。";
        } else if ("raise_talk".equals(special)) {
            if (random.nextBoolean()) {
                add(effects, "money", attrs.getSalary() * 2);
                return "老板说可以谈谈。钱先打了一笔过来。";
            }
            add(effects, "boss", -10);
            return "老板说现在不是时候。";
        } else if ("buy_stock".equals(special)) {
            if (attrs.getMoney() < 4000) {
                add(effects, "mind", -2);
                return "券商 App 显示余额不足。";
            }
            add(effects, "money", -4000);
            addFlag(state, GameConstants.FLAG_STOCKS, 25);
            return "你买了一手。K 线看起来像心电图。";
        } else if ("sell_stock".equals(special)) {
            if (flag(state, GameConstants.FLAG_STOCKS) < 25) {
                add(effects, "mind", -2);
                return "你没有能卖的仓位。";
            }
            int gain = 2500 + random.nextInt(4500);
            add(effects, "money", gain);
            addFlag(state, GameConstants.FLAG_STOCKS, -25);
            return gain > 4000 ? "这次跑得还行。" : "又给市场交了学费。";
        } else if ("dividend".equals(special)) {
            int shares = flag(state, GameConstants.FLAG_STOCKS);
            if (shares <= 0) {
                return "你没有股票。";
            }
            add(effects, "money", shares * 18);
            return "账户进了一笔分红。税后没那么香。";
        } else if ("grant_rsu".equals(special)) {
            addFlag(state, GameConstants.FLAG_STOCKS, 80);
            state.getFlags().put(GameConstants.FLAG_RSU, 1);
            add(effects, "mind", 8);
            return "期权协议发到邮箱。行权价写得很小。";
        } else if ("vacation".equals(special)) {
            add(effects, "hp", 16);
            add(effects, "mind", 14);
            add(effects, "boss", -8);
            add(effects, "money", -800);
            return "你请了一天假。群里还在@你。";
        } else if ("job_hunt".equals(special) || "job_hunt_easy".equals(special)) {
            int chance = "job_hunt_easy".equals(special) ? 75 : (attrs.getAbility() > 60 ? 70 : 40);
            if (random.nextInt(100) < chance) {
                state.getFlags().put(GameConstants.FLAG_JOBLESS, 0);
                if (attrs.getBoss() < 42) {
                    attrs.setBoss(42);
                }
                if ("job_hunt_easy".equals(special)) {
                    add(effects, "salary", -1500);
                }
                add(effects, "mind", 10);
                return "你又有工位了。新工牌还是热的。";
            }
            add(effects, "mind", -8);
            add(effects, "hp", -6);
            return "简历石沉大海。HR 说考虑一下。";
        } else if ("payday".equals(special)) {
            add(effects, "money", attrs.getSalary());
            return "工资到账。扣完五险一金，就是这些。";
        }
        return null;
    }

    private void maybeQueueLife(GameState state, EventDef event) {
        String id = event.getId();
        boolean paydayNow = flag(state, GameConstants.FLAG_JOBLESS) == 0 && !event.isRepeatable()
                && state.getEventCount() > 0 && state.getEventCount() % 4 == 0;
        if (paydayNow) {
            if (state.getQueuedEventId() != null) {
                state.getFlags().put("pendingPayday", Integer.valueOf(1));
            } else {
                state.setQueuedEventId("E70");
                return;
            }
        }
        if (state.getQueuedEventId() != null) {
            return;
        }
        if (flag(state, GameConstants.FLAG_JOBLESS) > 0) {
            if (!event.isRepeatable()) {
                state.setQueuedEventId("E53");
            }
            return;
        }
        if (flag(state, "pendingPayday") > 0) {
            state.getFlags().put("pendingPayday", Integer.valueOf(0));
            state.setQueuedEventId("E70");
            return;
        }
        if ("E61".equals(id) || "E62".equals(id)) {
            state.getFlags().put("atOffice", Integer.valueOf(1));
            state.getFlags().put("officeBeat", Integer.valueOf(0));
            if (flag(state, "overtime_debt") >= 2 && !state.getUsedEventIds().contains("E30")) {
                state.setQueuedEventId("E30");
            }
            return;
        }
        if ("E32".equals(id) || "E45".equals(id)) {
            state.getFlags().put("atOffice", Integer.valueOf(0));
            if (state.getQueuedEventId() == null) {
                state.setQueuedEventId("E61");
            }
            return;
        }
        if ("E70".equals(id)) {
            state.getFlags().put("atOffice", Integer.valueOf(0));
            state.setQueuedEventId("E60");
            return;
        }
        if ("E51".equals(id)) {
            state.setQueuedEventId("E61");
            return;
        }
        if ("E60".equals(id)) {
            state.getFlags().put("atOffice", Integer.valueOf(0));
            state.getFlags().put("officeBeat", Integer.valueOf(0));
            state.setQueuedEventId(pickNight(state));
            return;
        }
        if (event.isRepeatable()) {
            if (("E28".equals(id) || "E29".equals(id) || "E42".equals(id))
                    && flag(state, "atOffice") > 0) {
                return;
            }
            if (isNightId(id) || "E40".equals(id) || "E41".equals(id) || "E42".equals(id)
                    || "E48".equals(id) || "E28".equals(id) || "E29".equals(id)) {
                state.setQueuedEventId("E61");
            }
            return;
        }
        if (isDeskWork(event)) {
            state.getFlags().put("atOffice", Integer.valueOf(1));
            int beats = flag(state, "officeBeat") + 1;
            state.getFlags().put("officeBeat", Integer.valueOf(beats));
            if (beats < 3) {
                return;
            }
            state.getFlags().put("officeBeat", Integer.valueOf(0));
            state.getFlags().put("atOffice", Integer.valueOf(0));
        } else {
            state.getFlags().put("officeBeat", Integer.valueOf(0));
            state.getFlags().put("atOffice", Integer.valueOf(0));
        }
        if (random.nextInt(100) < 85) {
            state.setQueuedEventId("E60");
        }
    }

    private boolean isEveningWrap(EventDef event, GameState state) {
        String id = event.getId();
        if ("E40".equals(id) || "E48".equals(id)) {
            return true;
        }
        if ("E42".equals(id) && flag(state, "atOffice") == 0) {
            return true;
        }
        if (isNightId(id) || "E32".equals(id) || "E45".equals(id) || "E41".equals(id) || "E28".equals(id) || "E29".equals(id)) {
            if (flag(state, "atOffice") > 0) {
                return false;
            }
            String queued = state.getQueuedEventId();
            return queued == null || "E61".equals(queued);
        }
        return false;
    }

    private boolean isNightId(String id) {
        return "E26".equals(id) || "E81".equals(id) || "E82".equals(id) || "E83".equals(id)
                || "E84".equals(id) || "E85".equals(id);
    }

    private String pickNight(GameState state) {
        if (flag(state, GameConstants.FLAG_GIRLFRIEND) > 0
                && flag(state, GameConstants.FLAG_MARRIED) == 0
                && flag(state, "gfBond") >= 6
                && !state.getUsedEventIds().contains("E32")) {
            return "E32";
        }
        String[] nights = new String[] { "E26", "E81", "E82", "E83", "E84", "E85" };
        int last = flag(state, "lastNight");
        int idx = random.nextInt(nights.length);
        if (nights.length > 1 && idx + 1 == last) {
            idx = (idx + 1) % nights.length;
        }
        state.getFlags().put("lastNight", Integer.valueOf(idx + 1));
        return nights[idx];
    }

    private boolean isDeskWork(EventDef event) {
        String type = event.getType();
        return "BOSS".equals(type) || "COLLEAGUE".equals(type) || "CLIENT".equals(type)
                || "WORK".equals(type) || "MINIGAME".equals(type) || "SLACK".equals(type)
                || "SPECIAL".equals(type) || "MONEY".equals(type) || "FIGHT".equals(type);
    }

    private boolean shouldSkipSettle(EventDef event, String flavor) {
        if (event.getUi() == null || !"commute".equals(event.getUi())) {
            return false;
        }
        if ("E62".equals(event.getId())) {
            return true;
        }
        return flavor == null || flavor.length() == 0;
    }

    private boolean eligible(GameState state, EventDef event) {
        if (event.getMinMoney() != null && state.getAttrs().getMoney() < event.getMinMoney()) {
            return false;
        }
        if (event.getRequireFlag() != null && flag(state, event.getRequireFlag()) <= 0) {
            return false;
        }
        if (event.getForbidFlag() != null && flag(state, event.getForbidFlag()) > 0) {
            return false;
        }
        boolean jobless = flag(state, GameConstants.FLAG_JOBLESS) > 0;
        String type = event.getType();
        if (jobless && isOfficeType(type)) {
            return false;
        }
        if (!jobless && "UNEMPLOYED".equals(type)) {
            return false;
        }
        return true;
    }

    private boolean isOfficeType(String type) {
        return "BOSS".equals(type) || "COLLEAGUE".equals(type) || "CLIENT".equals(type)
                || "MINIGAME".equals(type) || "SLACK".equals(type) || "WORK".equals(type)
                || "SPECIAL".equals(type) || "MONEY".equals(type);
    }

    private boolean optionOpen(GameState state, OptionDef option) {
        if (option.getMinMoney() != null && state.getAttrs().getMoney() < option.getMinMoney()) {
            return false;
        }
        if (option.getRequireFlag() != null && flag(state, option.getRequireFlag()) <= 0) {
            return false;
        }
        if (option.getForbidFlag() != null && flag(state, option.getForbidFlag()) > 0) {
            return false;
        }
        return true;
    }

    private List<Weighted> collectPool(GameState state, boolean deskOnly) {
        List<Weighted> pool = new ArrayList<Weighted>();
        for (EventDef event : data.eventList()) {
            if (state.getUsedEventIds().contains(event.getId())) {
                continue;
            }
            if (event.getWeight() <= 0) {
                continue;
            }
            if (!event.supportsCareer(state.getCareerId())) {
                continue;
            }
            if (!eligible(state, event)) {
                continue;
            }
            if (deskOnly && !isDeskWork(event)) {
                continue;
            }
            int weight = Math.max(1, event.getWeight());
            Integer extra = state.getFlags().get(event.getId());
            if (extra != null) {
                weight += extra;
            }
            Integer typeExtra = state.getFlags().get(event.getType() + "_weight");
            if (typeExtra != null) {
                weight += typeExtra;
            }
            if ("E01".equals(event.getId())) {
                Integer ot = state.getFlags().get("overtime_weight");
                if (ot != null) {
                    weight += ot;
                }
            }
            if ("E20".equals(event.getId())) {
                Integer hunt = state.getFlags().get("headhunt_weight");
                if (hunt != null) {
                    weight += hunt;
                }
            }
            if ("E15".equals(event.getId())) {
                int slackDebt = flag(state, "slack_debt");
                if (slackDebt < 3) {
                    continue;
                }
                weight += slackDebt * 6;
            }
            pool.add(new Weighted(event, weight));
        }
        return pool;
    }

    private int flag(GameState state, String key) {
        if (key == null) {
            return 0;
        }
        Integer value = state.getFlags().get(key);
        return value == null ? 0 : value;
    }

    private void addFlag(GameState state, String key, int delta) {
        state.getFlags().put(key, flag(state, key) + delta);
    }

    private void noteChoiceDebts(GameState state, List<String> tags, EventDef event) {
        if (tags != null) {
            if (tags.contains(GameConstants.TAG_OVERTIME)) {
                addFlag(state, "overtime_debt", 1);
            }
            if (tags.contains(GameConstants.TAG_SLACK)) {
                addFlag(state, "slack_debt", 1);
            }
        }
        if ("E15".equals(event.getId())) {
            addFlag(state, "slack_debt", -3);
            if (flag(state, "slack_debt") < 0) {
                state.getFlags().put("slack_debt", Integer.valueOf(0));
            }
        }
        if ("E30".equals(event.getId())) {
            addFlag(state, "overtime_debt", -2);
            if (flag(state, "overtime_debt") < 0) {
                state.getFlags().put("overtime_debt", Integer.valueOf(0));
            }
        }
    }

    private void merge(Map<String, Integer> target, Map<String, Integer> extra) {
        if (extra == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : extra.entrySet()) {
            add(target, entry.getKey(), entry.getValue());
        }
    }

    private void add(Map<String, Integer> target, String key, int delta) {
        Integer old = target.get(key);
        target.put(key, (old == null ? 0 : old) + delta);
    }

    private static final class Weighted {
        private final EventDef event;
        private final int weight;

        private Weighted(EventDef event, int weight) {
            this.event = event;
            this.weight = weight;
        }
    }

    public static final class ChoiceResult {
        private Map<String, Integer> applied;
        private String flavor;
        private SkillDef newSkill;
        private EventDef nextEvent;
        private EndingDef ending;
        private List<String> tags;
        private boolean skipSettle;

        public Map<String, Integer> getApplied() {
            return applied;
        }

        public void setApplied(Map<String, Integer> applied) {
            this.applied = applied;
        }

        public String getFlavor() {
            return flavor;
        }

        public void setFlavor(String flavor) {
            this.flavor = flavor;
        }

        public SkillDef getNewSkill() {
            return newSkill;
        }

        public void setNewSkill(SkillDef newSkill) {
            this.newSkill = newSkill;
        }

        public EventDef getNextEvent() {
            return nextEvent;
        }

        public void setNextEvent(EventDef nextEvent) {
            this.nextEvent = nextEvent;
        }

        public EndingDef getEnding() {
            return ending;
        }

        public void setEnding(EndingDef ending) {
            this.ending = ending;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public boolean isSkipSettle() {
            return skipSettle;
        }

        public void setSkipSettle(boolean skipSettle) {
            this.skipSettle = skipSettle;
        }
    }
}
