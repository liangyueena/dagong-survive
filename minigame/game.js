const api = require("./js/api.js");

const canvas = wx.createCanvas();
const ctx = canvas.getContext("2d");
const sys = wx.getSystemInfoSync();
const W = sys.windowWidth;
const H = sys.windowHeight;
const dpr = sys.pixelRatio || 1;
canvas.width = W * dpr;
canvas.height = H * dpr;
ctx.scale(dpr, dpr);

let scene = "home";
let game = null;
let careers = [];
let hit = [];
let notice = "";

function color(c) { ctx.fillStyle = c; }
function text(str, x, y, size, align) {
  ctx.font = (size || 16) + "px sans-serif";
  ctx.textAlign = align || "left";
  ctx.fillStyle = ctx.fillStyle || "#f3f1ea";
  ctx.fillText(str, x, y);
}
function drawText(str, x, y, size, c, align) {
  ctx.fillStyle = c || "#f3f1ea";
  ctx.font = "600 " + (size || 16) + "px sans-serif";
  ctx.textAlign = align || "left";
  ctx.fillText(str, x, y);
}
function roundRect(x, y, w, h, r) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}
function btn(id, label, x, y, w, h, bg, fg) {
  color(bg || "#e6c36a");
  roundRect(x, y, w, h, 12);
  ctx.fill();
  drawText(label, x + w / 2, y + h / 2 + 6, 16, fg || "#1a1a1a", "center");
  hit.push({ id: id, x: x, y: y, w: w, h: h });
}

function clear() {
  hit = [];
  color("#111214");
  ctx.fillRect(0, 0, W, H);
}

function stats() {
  if (!game) return;
  const a = game.attrs;
  drawText("第 " + game.day + " 天   💰" + a.money + "  ❤️" + a.mind + "  ⚡" + a.hp, 20, 40, 13, "#9a9588");
  drawText("📈" + a.ability + "  👔" + a.boss + "  🐟" + a.slack, 20, 62, 13, "#9a9588");
}

function render() {
  clear();
  if (notice) drawText(notice, 20, H - 24, 12, "#d36a6a");
  if (scene === "home") {
    drawText("打工人：活下去", W / 2, H * 0.28, 28, "#f3f1ea", "center");
    drawText("你的职场，能撑到第几年？", W / 2, H * 0.28 + 36, 14, "#9a9588", "center");
    btn("start", "开始打工", 40, H * 0.45, W - 80, 48);
    btn("rank", "排行榜", 40, H * 0.45 + 64, (W - 96) / 3, 40, "#25262b", "#f3f1ea");
    btn("book", "图鉴", 40 + (W - 96) / 3 + 8, H * 0.45 + 64, (W - 96) / 3, 40, "#25262b", "#f3f1ea");
    btn("home", "首页", 40 + 2 * ((W - 96) / 3 + 8), H * 0.45 + 64, (W - 96) / 3, 40, "#25262b", "#f3f1ea");
  } else if (scene === "career") {
    drawText("选择职业", 20, 48, 22);
    careers.forEach(function (c, i) {
      btn("c:" + c.id, c.name + "  " + c.blurb, 20, 80 + i * 92, W - 40, 80, "#1c1d21", "#f3f1ea");
    });
  } else if (scene === "play" && game && game.event) {
    stats();
    const e = game.event;
    color("#1c1d21");
    roundRect(20, 88, W - 40, 120, 16);
    ctx.fill();
    drawText(e.title, 36, 128, 18);
    wrap(e.description, 36, 156, W - 72, 14, "#9a9588");
    (e.options || []).forEach(function (o, i) {
      btn("o:" + o.id, "[ " + o.id + " ]  " + o.text, 20, 228 + i * 62, W - 40, 52, "#25262b", "#f3f1ea");
    });
  } else if (scene === "boss" && game && game.event) {
    drawText("警告！老板距离你还有 5 米", 20, 48, 16, "#d36a6a");
    stats();
    (game.event.apps || []).forEach(function (app, i) {
      const x = 20 + (i % 2) * ((W - 50) / 2);
      const y = 160 + Math.floor(i / 2) * 70;
      btn("app:" + app, app, x, y, (W - 60) / 2, 56);
    });
  } else if (scene === "settle" && game) {
    stats();
    drawText("事件结算", 20, 100, 18);
    const applied = game.applied || {};
    let y = 140;
    Object.keys(applied).forEach(function (k) {
      if (k === "day" || !applied[k]) return;
      drawText(k + " " + (applied[k] > 0 ? "+" : "") + applied[k], 20, y, 16, applied[k] < 0 ? "#d36a6a" : "#7dba86");
      y += 28;
    });
    if (game.flavor) drawText(game.flavor, 20, y + 8, 14, "#9a9588");
    btn("next", "下一件事", 20, H - 140, W - 40, 48);
    btn("rechoose", "看广告重选", 20, H - 80, W - 40, 44, "#25262b", "#f3f1ea");
  } else if (scene === "skill" && game && game.newSkill) {
    drawText("获得技能", W / 2, H * 0.32, 16, "#9a9588", "center");
    drawText(game.newSkill.name, W / 2, H * 0.32 + 40, 28, "#e6c36a", "center");
    wrap(game.newSkill.desc, 40, H * 0.32 + 80, W - 80, 14, "#9a9588");
    btn("got", "收下", 40, H * 0.62, W - 80, 48);
  } else if (scene === "ending" && game) {
    const ed = game.ending || {};
    const a = game.attrs;
    drawText("打工人生", W / 2, 70, 22, "#e6c36a", "center");
    drawText("你活到了 " + game.age + " 岁", W / 2, 110, 18, "#f3f1ea", "center");
    drawText((ed.name || "被优化"), W / 2, 150, 24, "#f3f1ea", "center");
    drawText(game.careerName + "  存款 " + a.money, W / 2, 190, 14, "#9a9588", "center");
    btn("again", "再来一局", 40, H - 190, W - 80, 48);
    btn("share", "分享给朋友", 40, H - 130, W - 80, 44, "#25262b", "#f3f1ea");
    if (game.canRevive) btn("revive", "看广告复活", 40, H - 70, W - 80, 44, "#25262b", "#f3f1ea");
  } else if (scene === "rank") {
    drawText("生存榜 / 打开浏览器版看完整榜", 20, 64, 16);
    btn("home", "返回", 20, H - 80, W - 40, 48);
  }
}

function wrap(str, x, y, max, size, c) {
  if (!str) return;
  ctx.font = size + "px sans-serif";
  ctx.fillStyle = c;
  ctx.textAlign = "left";
  let line = "";
  let yy = y;
  for (let i = 0; i < str.length; i++) {
    const test = line + str[i];
    if (ctx.measureText(test).width > max) {
      ctx.fillText(line, x, yy);
      line = str[i];
      yy += size + 6;
    } else {
      line = test;
    }
  }
  ctx.fillText(line, x, yy);
}

function tap(id) {
  notice = "";
  if (id === "start") {
    api.request("/api/careers").then(function (list) {
      careers = list;
      scene = "career";
      render();
    }).catch(showErr);
  } else if (id.indexOf("c:") === 0) {
    api.request("/api/game/start", "POST", { careerId: id.slice(2) }).then(function (g) {
      game = g;
      scene = g.event && g.event.minigame ? "boss" : "play";
      render();
    }).catch(showErr);
  } else if (id.indexOf("o:") === 0) {
    api.request("/api/game/" + game.gameId + "/choose", "POST", { optionId: id.slice(2) }).then(afterChoice).catch(showErr);
  } else if (id.indexOf("app:") === 0) {
    const ok = id.slice(4) === game.workApp;
    api.request("/api/game/" + game.gameId + "/minigame", "POST", { success: ok }).then(afterChoice).catch(showErr);
  } else if (id === "next" || id === "got") {
    if (game.newSkill && id === "next") {
      scene = "skill";
    } else if (game.ending) {
      game.newSkill = null;
      scene = "ending";
    } else if (game.event && game.event.minigame) {
      game.newSkill = null;
      scene = "boss";
    } else {
      game.newSkill = null;
      scene = "play";
    }
    render();
  } else if (id === "again") {
    scene = "career";
    api.request("/api/careers").then(function (list) { careers = list; render(); }).catch(showErr);
  } else if (id === "share") {
    wx.shareAppMessage({
      title: (game.ending && game.ending.shareHook) || "看看你朋友能活到多少岁。",
      query: "from=ending"
    });
  } else if (id === "revive") {
    api.request("/api/game/" + game.gameId + "/ad", "POST", { type: "REVIVE" }).then(function (g) {
      game = g;
      scene = g.event && g.event.minigame ? "boss" : "play";
      render();
    }).catch(showErr);
  } else if (id === "rechoose") {
    api.request("/api/game/" + game.gameId + "/ad", "POST", { type: "RECHOOSE" }).then(function (g) {
      game = g;
      scene = g.event && g.event.minigame ? "boss" : "play";
      render();
    }).catch(showErr);
  } else if (id === "rank") {
    scene = "rank";
    render();
  } else if (id === "book" || id === "home") {
    scene = "home";
    render();
  }
}

function afterChoice(g) {
  game = g;
  scene = "settle";
  render();
}

function showErr(e) {
  notice = e.message || "出错了";
  render();
}

wx.onTouchStart(function (ev) {
  const t = ev.touches[0];
  const x = t.clientX;
  const y = t.clientY;
  for (let i = hit.length - 1; i >= 0; i--) {
    const b = hit[i];
    if (x >= b.x && x <= b.x + b.w && y >= b.y && y <= b.y + b.h) {
      tap(b.id);
      return;
    }
  }
});

render();
