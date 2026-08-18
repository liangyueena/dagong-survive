# 打工人：活下去 V0.1

副标题：你的职场，能撑到第几年？  
核心卖点：每一次选择，都可能改变你的职场人生。

这是一个两周 MVP 实验，不是完整创业项目。目标是验证：第一局打完，有没有人愿意再来一局。

## 范围

- 4 职业：程序员 / 销售 / 行政 / 财务
- 20 事件、5 技能、5 结局
- 1 个「老板来了」反应小游戏
- 激励视频广告 3 处（复活 / 重选 / 金币翻倍）
- 生存榜、财富榜、分享卡
- 事件全部配置化，改 JSON 不用改代码

内容配置在两处保持一致：

- `configs/` 给人看和改
- `backend/src/main/resources/game/` 给后端加载

改事件时请两处一起改，或改完 `configs/` 后复制回 `resources/game/`。

## 本地运行（推荐）

需要 JDK 8 和 Maven。默认用 H2 + 内存状态，不需要安装 MySQL / Redis。

```bash
cd backend
mvn spring-boot:run
```

浏览器打开 [http://localhost:8080](http://localhost:8080)

- 首页 → 开始打工 → 选职业 → 事件卡 → 结局卡
- 一局 10 个事件，大约每 30～60 秒一次决策
- 每 2 个事件发 1 个技能，最多 5 个

## 微信小游戏

用微信开发者工具导入 `minigame/` 目录，编译类型是「小游戏」。

1. 先把后端跑起来
2. 开发者工具里关闭 URL 校验（`project.config.json` 已设 `urlCheck: false`）
3. 模拟器默认请求 `http://127.0.0.1:8080`
4. 真机预览请把 API 改成电脑局域网地址，并在微信后台配 request 合法域名（体验版可先不配）

## MySQL + Redis

```bash
docker compose up -d
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

- 游戏过程状态：Redis（`dagong:game:{gameId}`，2 天过期）
- 对局、事件日志、埋点、广告日志、排行榜：MySQL

## 主要接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/careers` | 职业列表 |
| GET | `/api/meta` | 技能 / 结局图鉴 |
| POST | `/api/game/start` | 开局，body: `{ "careerId": "programmer" }` |
| POST | `/api/game/{id}/choose` | 选择，body: `{ "optionId": "A" }` |
| POST | `/api/game/{id}/minigame` | 老板来了，body: `{ "success": true }` |
| POST | `/api/game/{id}/ad` | `REVIVE` / `RECHOOSE` / `REWARD` |
| GET | `/api/rank/survive` | 生存榜 |
| GET | `/api/rank/wealth` | 财富榜 |
| POST | `/api/track` | 埋点 |

请求头带 `X-User-Id`。

埋点事件：`game_start` `career_select` `event_show` `event_choose` `event_result` `skill_get` `ad_show` `ad_complete` `game_end` `game_restart` `share_click`

## 结局判定（从上到下命中即停）

1. 心态 ≤ 0 → 精神离职
2. 老板好感 ≤ 0 → 被优化
3. 10 个事件后：存款 ≥ 150000 → 财富自由
4. 能力 > 80 且老板好感 > 70 且心态 > 50 → 公司高管
5. 摸鱼 > 80 且心态 > 80 且老板好感 > 30 → 高级老油条
6. 否则 → 被优化

V0.1 把财富自由门槛收到 15 万，否则 10 个事件几乎摸不到 100 万；只靠每月发工资也到不了。结局没有输赢，分享文案走「我居然活成了这个结局 / 看看你朋友能活到多少岁」。

## 目录

```
dagong-survive/
  configs/                 事件 / 职业 / 技能 / 结局 JSON
  backend/                 Spring Boot 2.7 / Java 8
  minigame/                微信小游戏
  docker-compose.yml       MySQL 8 + Redis 7
```
