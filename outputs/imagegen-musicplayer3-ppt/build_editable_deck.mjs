import fs from "node:fs/promises";
import path from "node:path";

const artifact = await import("file:///C:/Users/ASUS/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/@oai/artifact-tool/dist/artifact_tool.mjs");
const { createSlideContext } = await import("file:///C:/Users/ASUS/.codex/plugins/cache/openai-primary-runtime/presentations/26.521.10419/skills/presentations/scripts/artifact_tool_utils.mjs");

const WORK = "D:/MusicPlayer3/outputs/imagegen-musicplayer3-ppt";
const TEMPLATE = "D:/MusicPlayer3/outputs/manual-musicplayer3-ppt/presentations/musicplayer3-report/template.pptx";
const OUT = `${WORK}/output/MusicPlayer3_本地音乐播放器_可编辑版课程汇报.pptx`;
const PREVIEW = `${WORK}/editable-preview`;
const APP = {
  library: `${WORK}/assets/library-phone.png`,
  player: "D:/MusicPlayer3/current_musicplayer3.png",
  dialog: "D:/MusicPlayer3/more_dialog_musicplayer3.png",
  create: "D:/MusicPlayer3/create_dialog_musicplayer3_2.png",
  coverPhone: `${WORK}/assets/cover-phone.png`,
  summaryPhonePhoto: `${WORK}/assets/summary-phone-photo.png`,
  backgroundPhotoPanel: `${WORK}/assets/background-photo-panel.png`,
  result1: `${WORK}/assets/result-phone-1.png`,
  result2: `${WORK}/assets/result-phone-2.png`,
  result3: `${WORK}/assets/result-phone-3.png`,
  result4: `${WORK}/assets/result-phone-4.png`,
};

const C = {
  bg: "#FFF8ED",
  paper: "#FFFDF8",
  ink: "#161616",
  muted: "#6D6A66",
  line: "#24201D",
  coral: "#F05A52",
  coral2: "#F9C7BC",
  coral3: "#FFE4DC",
  green: "#43A779",
};

const presentation = await artifact.PresentationFile.importPptx(await fs.readFile(TEMPLATE));
const ctx = createSlideContext(artifact, {
  slideSize: { width: 1920, height: 1080 },
  titleFont: "Microsoft YaHei UI",
  bodyFont: "Microsoft YaHei UI",
  monoFont: "Consolas",
});

const sourceSlides = Array.from({ length: presentation.slides.count }, (_, i) => presentation.slides.getItem(i));
const slides = [];
for (let i = 0; i < 17; i++) slides.push(i === 0 ? sourceSlides[0] : sourceSlides[0].duplicate());
for (const slide of sourceSlides.slice(1)) slide.delete();
slides.forEach((slide, i) => slide.moveTo(i));

function clear(slide) {
  for (const shape of [...slide.shapes.items]) shape.delete();
  for (const image of [...slide.images.items]) image.delete();
}

function rect(slide, x, y, w, h, fill = C.paper, line = C.line, width = 2) {
  return ctx.addShape(slide, {
    left: x,
    top: y,
    width: w,
    height: h,
    fill,
    line: { style: "solid", fill: line, width },
  });
}

function line(slide, x, y, w, h = 3, fill = C.line) {
  return rect(slide, x, y, w, h, fill, fill, 0);
}

function text(slide, value, x, y, w, h, options = {}) {
  return ctx.addText(slide, {
    text: value,
    left: x,
    top: y,
    width: w,
    height: h,
    fontSize: options.size ?? 28,
    bold: options.bold ?? false,
    color: options.color ?? C.ink,
    typeface: options.face ?? "Microsoft YaHei UI",
    align: options.align ?? "left",
    valign: options.valign ?? "top",
    fill: "#00000000",
    line: { style: "solid", fill: "#00000000", width: 0 },
    insets: options.insets ?? { left: 0, right: 0, top: 0, bottom: 0 },
  });
}

function header(slide, label = "移动软件设计课程大作业汇报") {
  text(slide, "▣", 54, 48, 40, 34, { size: 22, color: C.ink });
  text(slide, label, 94, 48, 620, 34, { size: 20, color: C.ink });
  text(slide, "MusicPlayer3 汇报 ✽", 1550, 48, 300, 34, { size: 18, color: C.ink, align: "right" });
  line(slide, 54, 92, 1812, 2, C.line);
}

function title(slide, value, x = 70, y = 150, w = 1000, size = 72) {
  text(slide, value, x, y, w, 120, { size, bold: true });
  text(slide, "✽", x + Math.min(w - 50, value.length * size * 0.48 + 15), y + 20, 60, 60, { size: 42, color: C.coral2, bold: true });
}

function flower(slide, x, y, size = 240, opacityColor = C.coral2) {
  text(slide, "✽", x, y, size, size, { size, color: opacityColor, bold: true, align: "center" });
}

function card(slide, x, y, w, h, heading, body, icon = "✽") {
  rect(slide, x, y, w, h, C.paper, C.line, 2);
  text(slide, icon, x + 26, y + 22, 44, 44, { size: 30, color: C.coral, bold: true, align: "center" });
  text(slide, heading, x + 88, y + 26, w - 115, 42, { size: 30, bold: true });
  if (body) text(slide, body, x + 88, y + 84, w - 120, h - 92, { size: 21, color: C.muted });
}

function bulletList(slide, items, x, y, w, gap = 58, size = 26) {
  items.forEach((item, i) => {
    const yy = y + i * gap;
    text(slide, "●", x, yy + 4, 34, 34, { size: 18, color: C.coral });
    text(slide, item, x + 42, yy, w - 42, 42, { size, color: C.ink });
  });
}

function sectionCard(slide, x, y, w, h, label, index) {
  rect(slide, x, y, w, h, "#FFF7EF", C.line, 2);
  text(slide, "✽", x + 18, y + 18, 42, 42, { size: 28, color: C.coral, bold: true });
  text(slide, label, x + 78, y + 22, w - 145, 38, { size: 27, bold: true });
  text(slide, String(index).padStart(2, "0"), x + w - 70, y + 24, 46, 32, { size: 21, color: C.muted, align: "right" });
}

async function image(slide, file, x, y, w, h, fit = "cover") {
  const bytes = await fs.readFile(file);
  const dataUrl = `data:image/png;base64,${bytes.toString("base64")}`;
  const img = await ctx.addImage(slide, { dataUrl, left: x, top: y, width: w, height: h, fit });
  rect(slide, x, y, w, h, "#00000000", C.line, 3);
  return img;
}

function base(slide) {
  clear(slide);
  rect(slide, 0, 0, 1920, 1080, C.bg, C.bg, 0);
  flower(slide, 1510, 730, 330, C.coral3);
  header(slide);
}

function smallBadge(slide, x, y, value) {
  rect(slide, x, y, 210, 46, "#FFF4ED", C.line, 2);
  text(slide, value, x + 16, y + 10, 178, 24, { size: 18, align: "center" });
}

function phoneMock(slide, x, y, w, h, label = "MusicPlayer3") {
  rect(slide, x, y, w, h, "#2F2A2D", "#2F2A2D", 0);
  rect(slide, x + 24, y + 24, w - 48, h - 48, "#FBF6EF", "#FBF6EF", 0);
  text(slide, label, x + 60, y + 58, w - 120, 50, { size: 28, bold: true });
  rect(slide, x + 60, y + 130, w - 120, 88, "#FFFFFF", "#E6D9CE", 1);
  rect(slide, x + 80, y + 152, 52, 52, C.coral3, C.coral3, 0);
  text(slide, "♪", x + 94, y + 154, 40, 38, { size: 28, color: C.coral, bold: true });
  text(slide, "夜航星", x + 150, y + 147, 180, 34, { size: 24, bold: true });
  text(slide, "导入后即可播放", x + 150, y + 183, 220, 26, { size: 18, color: C.muted });
}

// 01
{
  const s = slides[0];
  base(s);
  flower(s, 1160, 565, 360, C.coral2);
  title(s, "MusicPlayer3", 96, 236, 760, 82);
  text(s, "本地音乐播放器", 96, 355, 820, 86, { size: 68, bold: true });
  text(s, "移动软件设计课程大作业汇报", 96, 493, 720, 46, { size: 28, color: C.muted });
  smallBadge(s, 96, 882, "第六组");
  text(s, "移动软件设计课程", 360, 892, 260, 26, { size: 20, color: C.muted });
  text(s, "2026", 700, 892, 120, 26, { size: 20, color: C.muted });
  await image(s, APP.coverPhone, 1260, 205, 470, 515, "contain");
}

// 02
{
  const s = slides[1];
  base(s);
  title(s, "项目摘要", 86, 170, 650, 74);
  text(s, "MusicPlayer3 是一个基于 Android Java 开发的本地音乐播放器，实现音乐导入、资料库管理、播放控制、搜索、收藏、歌词、封面、最近播放和状态持久化等功能。", 94, 340, 820, 180, { size: 30, color: C.ink });
  text(s, "项目重点在于完成一个可运行、可交互、可保存状态的移动端音乐播放应用。", 94, 565, 820, 100, { size: 30, color: C.ink });
  await image(s, APP.summaryPhonePhoto, 1124, 154, 585, 760, "cover");
}

// 03
{
  const s = slides[2];
  base(s);
  text(s, "本次汇报按照“背景—需求—设计—实现—结果—总结”的顺序展开，重点说明 MusicPlayer3 如何形成应用闭环。", 88, 160, 760, 96, { size: 23, color: C.ink });
  title(s, "汇报目录", 1040, 185, 620, 76);
  const items = ["项目背景", "需求分析", "系统设计", "核心功能", "关键实现", "运行结果", "总结展望"];
  items.forEach((it, i) => {
    const col = i % 4;
    const row = Math.floor(i / 4);
    sectionCard(s, 520 + col * 310, 430 + row * 145, 270, 74, it, i + 1);
  });
}

// 04
{
  const s = slides[3];
  base(s);
  title(s, "项目背景", 86, 160, 650, 74);
  card(s, 100, 340, 800, 140, "本地音乐仍是典型移动端多媒体应用场景", "涉及文件访问、媒体播放、状态管理、界面交互和数据持久化。", "♪");
  card(s, 100, 515, 800, 140, "适合作为移动软件设计课程综合实践", "能够综合训练 Android UI、MediaPlayer、SharedPreferences 等知识点。", "▣");
  card(s, 100, 690, 800, 140, "课程实践价值明确", "从需求、设计、编码、调试到报告汇报，形成完整项目流程。", "↻");
  await image(s, APP.backgroundPhotoPanel, 1200, 150, 610, 635, "cover");
  smallBadge(s, 185, 900, "Android");
  smallBadge(s, 430, 900, "MediaPlayer");
  smallBadge(s, 700, 900, "SharedPreferences");
}

// 05
{
  const s = slides[4];
  base(s);
  title(s, "问题与设计目标", 86, 170, 850, 70);
  rect(s, 120, 365, 730, 430, C.paper, C.line, 3);
  rect(s, 1010, 365, 730, 430, C.paper, C.line, 3);
  text(s, "✽  现有问题", 160, 415, 520, 54, { size: 36, bold: true });
  text(s, "◎  设计目标", 1050, 415, 520, 54, { size: 36, bold: true });
  line(s, 160, 500, 620, 2, C.line);
  line(s, 1050, 500, 620, 2, C.line);
  bulletList(s, ["本地文件分散", "播放状态难管理", "歌词封面资源不统一", "重启后状态容易丢失"], 170, 545, 600, 64, 27);
  bulletList(s, ["稳定播放", "资料库清晰", "状态同步", "数据可恢复", "界面友好"], 1060, 540, 600, 56, 27);
}

// 06
{
  const s = slides[5];
  base(s);
  title(s, "功能需求分析", 86, 170, 720, 70);
  text(s, "需求覆盖资料库、播放、检索、收藏、资源管理与状态恢复六个方面。", 92, 285, 760, 40, { size: 24, color: C.muted });
  const modules = [
    ["资料库管理", "导入与分类", "▤"],
    ["播放控制", "播放、暂停、切歌", "▶"],
    ["搜索与喜欢", "关键词检索与收藏", "♡"],
    ["歌词与封面", "解析歌词，显示封面", "▧"],
    ["最近播放", "记录播放历史", "◷"],
    ["状态保存", "SharedPreferences 恢复", "☁"],
  ];
  modules.forEach(([h, b, ic], i) => {
    const x = 260 + (i % 3) * 480;
    const y = 390 + Math.floor(i / 3) * 220;
    card(s, x, y, 390, 150, h, b, ic);
  });
  line(s, 1170, 255, 430, 3, C.coral2);
}

// 07
{
  const s = slides[6];
  base(s);
  title(s, "系统总体架构", 86, 170, 760, 70);
  const layers = [
    ["UI层", "资料库页 / 搜索页 / 喜欢页 / 设置页 / 播放详情页"],
    ["业务逻辑层", "导入 / 播放 / 搜索 / 收藏 / 歌词 / 元数据解析"],
    ["数据状态层", "Song / Playlist / favoriteUris / recentPlayedUris"],
    ["系统能力层", "MediaPlayer / ACTION_OPEN_DOCUMENT / SharedPreferences"],
  ];
  layers.forEach(([h, b], i) => {
    const y = 330 + i * 135;
    rect(s, 200, y, 1220, 88, i % 2 ? "#FFF4ED" : C.paper, C.line, 3);
    text(s, h, 230, y + 24, 210, 36, { size: 28, bold: true, color: C.coral });
    text(s, b, 470, y + 24, 900, 36, { size: 25 });
    if (i < layers.length - 1) text(s, "↓", 790, y + 90, 50, 40, { size: 28, color: C.coral, bold: true, align: "center" });
  });
  phoneMock(s, 1510, 380, 220, 360, "App");
}

// 08
{
  const s = slides[7];
  base(s);
  title(s, "数据结构设计", 86, 160, 720, 70);
  card(s, 100, 330, 520, 380, "Song", "title、uri、fileName、artist、album、lyricsUri、coverUri、embeddedCoverUri、playCount、addedAt、lastPlayedAt", "♪");
  card(s, 700, 330, 410, 260, "Playlist", "name、songs", "▤");
  card(s, 1190, 330, 560, 380, "集合", "librarySongs、favoriteUris、recentPlayedUris、recentSearchQueries", "▣");
  text(s, "Song 以 Uri 作为唯一标识，播放列表只保存引用，避免状态错乱。", 730, 690, 580, 80, { size: 26, color: C.muted });
  line(s, 620, 520, 80, 3, C.line);
  line(s, 1110, 520, 80, 3, C.line);
}

// 09
{
  const s = slides[8];
  base(s);
  title(s, "页面与交互设计", 86, 160, 780, 70);
  card(s, 100, 400, 420, 150, "四栏导航", "资料库 / 搜索 / 喜欢 / 设置", "▦");
  card(s, 100, 610, 420, 150, "跨页面迷你播放器", "持续显示当前歌曲与控制入口", "♪");
  await image(s, APP.library, 650, 235, 310, 660, "contain");
  await image(s, APP.player, 1010, 235, 310, 660, "contain");
  card(s, 1390, 455, 380, 170, "播放详情页", "封面、歌词、进度条与控制按钮集中呈现。", "▶");
}

// 10
{
  const s = slides[9];
  base(s);
  title(s, "音乐导入与元数据解析", 86, 170, 950, 64);
  text(s, "通过系统文件选择器导入音频，同时解析媒体标签并建立资料库记录。", 96, 275, 980, 40, { size: 24, color: C.muted });
  const steps = ["选择音频", "保存 Uri 权限", "解析标题/歌手/专辑/封面", "文件名兜底拆分", "创建 Song", "加入资料库和当前列表"];
  steps.forEach((step, i) => {
    const x = 100 + i * 295;
    rect(s, x, 520, 240, 115, i % 2 ? "#FFF4ED" : C.paper, C.line, 2);
    text(s, String(i + 1), x + 18, 536, 40, 34, { size: 22, bold: true, color: C.coral, align: "center" });
    text(s, step, x + 58, 535, 160, 64, { size: 22, bold: true });
    if (i < steps.length - 1) text(s, "→", x + 242, 555, 50, 44, { size: 34, color: C.coral, bold: true });
  });
  phoneMock(s, 1455, 210, 260, 290, "导入");
  smallBadge(s, 330, 760, "ACTION_OPEN_DOCUMENT");
  smallBadge(s, 620, 760, "MediaMetadataRetriever");
  smallBadge(s, 935, 760, "ContentResolver Uri");
  smallBadge(s, 1235, 760, "Song 对象");
}

// 11
{
  const s = slides[10];
  base(s);
  title(s, "播放控制与播放模式", 86, 170, 900, 64);
  card(s, 100, 355, 500, 240, "核心播放控制", "MediaPlayer 创建、播放、暂停、停止、上一首、下一首、SeekBar 进度同步。", "▶");
  rect(s, 735, 355, 500, 240, "#FFF4ED", C.line, 3);
  text(s, "播放面板", 780, 390, 160, 36, { size: 30, bold: true });
  text(s, "◀   ▶ / ❚❚   ▶", 790, 460, 360, 70, { size: 44, bold: true, align: "center" });
  line(s, 790, 555, 360, 6, C.coral);
  card(s, 1345, 355, 400, 240, "播放模式", "顺序循环 / 单曲循环 / 随机播放", "↻");
  const flow = ["create", "prepare", "start", "pause/stop", "release"];
  flow.forEach((f, i) => {
    rect(s, 255 + i * 285, 760, 190, 58, C.paper, C.line, 2);
    text(s, f, 270 + i * 285, 776, 160, 26, { size: 20, align: "center" });
    if (i < flow.length - 1) text(s, "→", 445 + i * 285, 770, 60, 34, { size: 26, color: C.coral, bold: true });
  });
}

// 12
{
  const s = slides[11];
  base(s);
  title(s, "搜索、喜欢与最近播放", 86, 170, 900, 64);
  card(s, 120, 355, 500, 315, "搜索", "匹配 title / fileName / artist / album，并保存最近搜索关键词。", "⌕");
  card(s, 710, 355, 500, 315, "喜欢", "favoriteUris 使用歌曲 Uri 保存收藏状态，播放页与喜欢页同步刷新。", "♡");
  card(s, 1300, 355, 500, 315, "最近播放", "真正开始播放时记录，去重、插入首位并限制数量。", "◷");
  line(s, 300, 780, 1240, 3, C.line);
  text(s, "Song Uri", 245, 820, 180, 34, { size: 23, bold: true, align: "center" });
  text(s, "→ favoriteUris", 710, 820, 260, 34, { size: 23, color: C.coral, bold: true, align: "center" });
  text(s, "→ recentPlayedUris", 1210, 820, 320, 34, { size: 23, color: C.coral, bold: true, align: "center" });
}

// 13
{
  const s = slides[12];
  base(s);
  title(s, "歌词、封面与播放体验", 86, 170, 930, 64);
  card(s, 110, 350, 470, 190, "LRC 歌词解析", "通过正则表达式解析时间标签。", "[]");
  card(s, 110, 590, 470, 190, "TXT 歌词兜底", "按行读取并生成默认时间。", "TXT");
  card(s, 680, 350, 480, 430, "封面优先级", "手动封面 > 内嵌封面 > 默认封面\n列表、迷你播放器和播放页统一圆角显示。", "▧");
  await image(s, APP.player, 1260, 250, 350, 650, "contain");
  text(s, "歌词随播放进度切换，并加入轻量淡入上滑动画。", 690, 815, 560, 42, { size: 26, color: C.muted });
}

// 14
{
  const s = slides[13];
  base(s);
  title(s, "状态保存与恢复", 86, 170, 780, 70);
  card(s, 105, 355, 420, 230, "保存内容", "资料库、播放列表、收藏、最近播放、搜索历史、播放模式、主题、歌曲媒体信息。", "▤");
  rect(s, 650, 420, 260, 100, "#FFF4ED", C.line, 3);
  text(s, "对象", 730, 450, 100, 34, { size: 28, bold: true, align: "center" });
  text(s, "→", 930, 450, 60, 34, { size: 34, color: C.coral, bold: true });
  rect(s, 1015, 420, 260, 100, C.paper, C.line, 3);
  text(s, "JSON", 1095, 450, 100, 34, { size: 28, bold: true, align: "center" });
  text(s, "→", 1295, 450, 60, 34, { size: 34, color: C.coral, bold: true });
  rect(s, 1380, 420, 360, 100, "#FFF4ED", C.line, 3);
  text(s, "SharedPreferences", 1410, 450, 300, 34, { size: 25, bold: true, align: "center" });
  card(s, 105, 685, 420, 180, "恢复方式", "读取 JSON，重建 Song / Playlist / favoriteUris 等对象后刷新界面。", "↻");
  text(s, "应用重启后仍可恢复播放列表、收藏、最近播放与界面状态。", 680, 705, 850, 50, { size: 29, color: C.ink });
}

// 15
{
  const s = slides[14];
  base(s);
  title(s, "运行结果与测试", 86, 160, 760, 70);
  await image(s, APP.result1, 120, 310, 260, 460, "contain");
  await image(s, APP.result2, 430, 310, 260, 460, "contain");
  await image(s, APP.result3, 740, 310, 260, 460, "contain");
  await image(s, APP.result4, 1050, 310, 260, 460, "contain");
  card(s, 1400, 315, 380, 300, "测试点", "启动正常、导入成功、播放正常、搜索可用、收藏同步、歌词显示、状态恢复。", "✓");
  rect(s, 1410, 685, 340, 72, "#E7FFF0", C.green, 3);
  text(s, "BUILD SUCCESSFUL", 1440, 705, 280, 30, { size: 25, bold: true, color: C.green, align: "center" });
}

// 16
{
  const s = slides[15];
  base(s);
  title(s, "总结与展望", 86, 170, 760, 70);
  card(s, 150, 380, 720, 390, "总结", "完成本地音乐播放器主要功能闭环；实现文件导入、播放控制、资源管理和状态恢复；界面结构清晰，支持多页面联动。", "▣");
  card(s, 1040, 380, 720, 390, "展望", "后台播放服务；通知栏控制；音频焦点管理；Room 数据库；更完整媒体标签读取；多主题方案。", "♧");
  line(s, 1040, 300, 600, 3, C.coral2);
  text(s, "↗", 1600, 240, 140, 100, { size: 78, color: C.coral2, bold: true });
}

// 17
{
  const s = slides[16];
  base(s);
  flower(s, 1280, 610, 420, C.coral2);
  text(s, "Thank", 120, 255, 640, 140, { size: 104, bold: true });
  text(s, "You", 120, 390, 430, 140, { size: 104, bold: true });
  text(s, "✽", 490, 405, 90, 90, { size: 62, color: C.coral, bold: true });
  text(s, "感谢老师指导与小组成员协作", 130, 610, 780, 48, { size: 34, bold: true });
  smallBadge(s, 130, 845, "欢迎批评指正");
  text(s, "MusicPlayer3", 420, 858, 240, 28, { size: 20, color: C.muted });
}

await fs.mkdir(path.dirname(OUT), { recursive: true });
const pptx = await artifact.PresentationFile.exportPptx(presentation);
await pptx.save(OUT);

await fs.mkdir(PREVIEW, { recursive: true });
for (let i = 0; i < presentation.slides.count; i++) {
  const slide = presentation.slides.getItem(i);
  const png = await presentation.export({ slide, format: "png" });
  await fs.writeFile(`${PREVIEW}/slide-${String(i + 1).padStart(2, "0")}.png`, new Uint8Array(await png.arrayBuffer()));
}

console.log(OUT);
