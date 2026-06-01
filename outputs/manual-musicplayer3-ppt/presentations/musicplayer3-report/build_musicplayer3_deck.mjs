import fs from "node:fs/promises";
import path from "node:path";

const artifact = await import("file:///C:/Users/ASUS/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/@oai/artifact-tool/dist/artifact_tool.mjs");

const WORK = "D:/MusicPlayer3/outputs/manual-musicplayer3-ppt/presentations/musicplayer3-report";
const TEMPLATE = `${WORK}/template.pptx`;
const OUTPUT = `${WORK}/output/MusicPlayer3_本地音乐播放器_课程汇报.pptx`;
const PREVIEW = `${WORK}/preview`;
const ASSETS = {
  library: "D:/MusicPlayer3/phase2_launch.png",
  player: "D:/MusicPlayer3/current_musicplayer3.png",
  dialog: "D:/MusicPlayer3/more_dialog_musicplayer3.png",
  create: "D:/MusicPlayer3/create_dialog_musicplayer3_2.png",
};

const deckBytes = await fs.readFile(TEMPLATE);
const presentation = await artifact.PresentationFile.importPptx(deckBytes);
const sourceSlides = Array.from({ length: presentation.slides.count }, (_, i) => presentation.slides.getItem(i));

const slideMap = [1, 2, 3, 4, 5, 8, 7, 10, 11, 10, 11, 13, 14, 12, 13, 14, 15];
const outputSlides = slideMap.map((sourceIndex) => sourceSlides[sourceIndex - 1].duplicate());
for (const slide of sourceSlides) slide.delete();
outputSlides.forEach((slide, index) => slide.moveTo(index));

function normalize(text) {
  return String(text || "").replace(/\s+/g, " ").trim();
}

function textShapes(slide) {
  return slide.shapes.items.filter((shape) => normalize(shape.text?.toString?.()).length > 0);
}

function set(shape, text) {
  if (!shape) return;
  shape.text.set(text);
}

function tuneText(shape, options = {}) {
  if (!shape) return;
  if (options.position) shape.position = { ...shape.position, ...options.position };
  if (options.fontSize != null) shape.text.fontSize = options.fontSize;
  if (options.bold != null) shape.text.bold = options.bold;
  if (options.color) shape.text.color = options.color;
}

function byText(slide, exact) {
  return textShapes(slide).find((shape) => normalize(shape.text.toString()) === exact);
}

function byPrefix(slide, prefix) {
  return textShapes(slide).find((shape) => normalize(shape.text.toString()).startsWith(prefix));
}

function replaceHeader(slide) {
  set(byText(slide, "Rimberio University"), "移动软件设计课程");
  set(byText(slide, "October 2025"), "MusicPlayer3 汇报");
}

function replaceLorem(slide, texts) {
  const lorem = textShapes(slide).filter((shape) => normalize(shape.text.toString()).startsWith("Lorem ipsum"));
  lorem.forEach((shape, index) => set(shape, texts[index] || ""));
}

function replaceSequential(slide, pairs) {
  for (const [oldText, newText] of pairs) set(byText(slide, oldText), newText);
}

function setTitle(slide, oldTitle, newTitle) {
  set(byText(slide, oldTitle), newTitle);
}

async function addImage(slide, imagePath, frame) {
  const blob = new artifact.FileBlob(await fs.readFile(imagePath), "image/png");
  const image = slide.images.add({ blob, fit: "cover", alt: path.basename(imagePath) });
  image.position = frame;
  return image;
}

for (const slide of outputSlides) replaceHeader(slide);

// 1 封面
{
  const s = outputSlides[0];
  set(byText(s, "Thesis"), "MusicPlayer3");
  set(byText(s, "Presentation"), "本地音乐播放器");
  set(byText(s, "Defense"), "课程汇报");
  set(byText(s, "Rimberio University"), "移动软件设计课程大作业");
  set(byText(s, "October 2025"), "2026");
  replaceLorem(s, ["基于 Android Java 的本地音乐播放器，围绕音乐导入、资料库管理、播放控制、搜索收藏、歌词封面和状态持久化形成完整应用闭环。"]);
  set(byText(s, "Presented by:"), "汇报小组：");
  set(byText(s, "Ketut Susilo"), "第六组");
  set(byText(s, "www.reallygreatsite.com"), "MusicPlayer3");
  set(byText(s, "课程汇报"), "");
  tuneText(byText(s, "MusicPlayer3"), { position: { left: 90.0789501312336, top: 347.67433070866144, width: 946.5332283464568, height: 461.36 } });
  tuneText(byText(s, "本地音乐播放器"), { position: { left: 108, top: 645, width: 900, height: 120 } });
  tuneText(byPrefix(s, "基于 Android Java"), { position: { left: 108, top: 780, width: 1125.9405774278216, height: 99.16 } });
}

// 2 摘要
{
  const s = outputSlides[1];
  setTitle(s, "Abstract", "项目摘要");
  replaceLorem(s, [
    "MusicPlayer3 是一个基于 Android Java 开发的本地音乐播放器，实现音乐导入、资料库管理、播放控制、搜索、收藏、歌词、封面、最近播放和状态持久化等功能。",
    "项目重点在于完成一个可运行、可交互、可保存状态的移动端音乐播放应用，并通过较清晰的页面结构和数据模型展示移动端多媒体应用的完整开发流程。"
  ]);
}

// 3 目录
{
  const s = outputSlides[2];
  setTitle(s, "Overview", "汇报目录");
  replaceSequential(s, [
    ["Introduction", "项目背景"],
    ["Objectives", "需求分析"],
    ["Problem", "系统设计"],
    ["Hypothesis", "数据结构"],
    ["Literary Review", "页面交互"],
    ["Methodology", "音乐导入"],
    ["Framework", "播放控制"],
    ["Implementation", "搜索收藏"],
    ["Result", "歌词封面"],
    ["Conclusion", "状态恢复"],
    ["Recommendation", "运行测试"],
    ["Thank You", "总结展望"],
  ]);
  replaceLorem(s, ["本次汇报按照“背景—需求—设计—实现—结果—总结”的顺序展开，重点说明 MusicPlayer3 如何从本地文件访问、媒体播放和状态保存三个方向形成应用闭环。"]);
}

// 4 背景
{
  const s = outputSlides[3];
  setTitle(s, "Introduction", "项目背景");
  replaceLorem(s, [
    "本地音乐仍然是典型的移动端多媒体应用场景。它涉及系统文件选择、音频播放、资源引用、界面反馈和状态持久化等多个 Android 开发知识点。",
    "选择音乐播放器作为课程大作业，可以综合训练移动端 UI 组织、MediaPlayer 生命周期管理、SharedPreferences 数据保存和多页面状态同步能力。"
  ]);
}

// 5 问题与目标
{
  const s = outputSlides[4];
  setTitle(s, "Problems", "问题与设计目标");
  replaceSequential(s, [
    ["Problem 01", "现有问题"],
    ["Problem 02", "设计目标"],
  ]);
  replaceLorem(s, [
    "本地文件分散；播放状态难管理；歌词、封面和背景资源不统一；应用重启后状态容易丢失。",
    "实现稳定播放、清晰资料库、跨页面状态同步、数据可恢复和更友好的移动端界面。",
    "MusicPlayer3 的设计重点不是堆叠复杂功能，而是围绕基础播放器体验建立稳定、可验证、可演示的功能闭环。"
  ]);
}

// 6 功能需求
{
  const s = outputSlides[5];
  setTitle(s, "Objectives", "功能需求分析");
  replaceSequential(s, [
    ["Objective 01", "基础播放与资料库"],
    ["Objective 02", "资源管理与状态恢复"],
  ]);
  replaceLorem(s, [
    "需求覆盖资料库管理、播放控制、搜索与喜欢、歌词与封面、最近播放和状态保存六个方面。",
    "播放控制包括播放、暂停、停止、上一首、下一首、SeekBar 进度同步和播放模式切换。",
    "资源管理包括歌词关联、封面设置、播放背景、自动元数据解析和应用重启后的状态恢复。"
  ]);
}

// 7 架构
{
  const s = outputSlides[6];
  setTitle(s, "Theoritical Framework", "系统总体架构");
  replaceSequential(s, [
    ["Overview", "单 Activity 结构"],
    ["Proponent", "四层职责划分"],
  ]);
  replaceLorem(s, [
    "MainActivity 统一管理页面渲染、MediaPlayer、歌曲数据、播放列表和 SharedPreferences，减少多 Activity 状态传递带来的复杂度。",
    "UI 层：资料库 / 搜索 / 喜欢 / 设置 / 播放详情页\n业务逻辑层：导入 / 播放 / 搜索 / 收藏 / 歌词 / 元数据解析\n数据状态层：Song / Playlist / favoriteUris / recentPlayedUris\n系统能力层：MediaPlayer / ACTION_OPEN_DOCUMENT / SharedPreferences",
    "这种结构适合课程项目展示，可以直观体现事件处理、动态视图渲染、媒体播放和本地持久化之间的配合关系。"
  ]);
}

// 8 数据结构
{
  const s = outputSlides[7];
  setTitle(s, "Methodology", "数据结构设计");
  replaceSequential(s, [
    ["Qualitative Method", "Song"],
    ["Qualitative Method", "Playlist & 集合"],
  ]);
  replaceLorem(s, [
    "Song 保存 title、uri、fileName、artist、album、lyricsUri、coverUri、embeddedCoverUri、playCount、addedAt 和 lastPlayedAt。",
    "Playlist 保存 name 与 songs 引用集合；librarySongs、favoriteUris、recentPlayedUris、recentSearchQueries 分别维护资料库、收藏、最近播放和搜索历史。",
    "系统以 Uri 作为歌曲唯一标识，避免因歌曲在不同列表中的位置变化造成状态错乱。"
  ]);
}

// 9 页面交互
{
  const s = outputSlides[8];
  setTitle(s, "Implementation", "页面与交互设计");
  replaceSequential(s, [
    ["Phase 01", "四栏导航"],
    ["Phase 02", "播放详情页"],
  ]);
  replaceLorem(s, [
    "资料库、搜索、喜欢、设置四个主页面分别承担音乐管理、歌曲检索、收藏展示和应用配置职责。",
    "底部迷你播放器作为跨页面组件，持续显示当前歌曲和基础控制按钮。",
    "播放详情页集中呈现封面、歌词、进度条、播放模式、喜欢按钮和更多资源设置入口。"
  ]);
}

// 10 导入
{
  const s = outputSlides[9];
  setTitle(s, "Methodology", "音乐导入与元数据解析");
  replaceSequential(s, [
    ["Qualitative Method", "导入流程"],
    ["Qualitative Method", "信息整理"],
  ]);
  replaceLorem(s, [
    "ACTION_OPEN_DOCUMENT 选择音频；保存 Uri 权限；创建 Song 对象；加入资料库和当前播放列表。",
    "MediaMetadataRetriever 解析标题、歌手、专辑和内嵌封面；当标签缺失时，按“歌手 - 歌名”格式进行文件名兜底拆分。",
    "导入流程同时完成文件访问、媒体信息整理和播放列表引用建立，使资料库展示更接近真实音乐播放器。"
  ]);
}

// 11 播放
{
  const s = outputSlides[10];
  setTitle(s, "Implementation", "播放控制与播放模式");
  replaceSequential(s, [
    ["Phase 01", "MediaPlayer 生命周期"],
    ["Phase 02", "播放模式算法"],
  ]);
  replaceLorem(s, [
    "播放新歌曲前释放旧播放器，根据 Uri 创建 MediaPlayer，并设置播放完成和播放错误回调。",
    "支持播放、暂停、停止、上一首、下一首和 SeekBar 进度同步。",
    "播放模式包括顺序循环、单曲循环和随机播放；播放完成后根据当前模式计算下一首歌曲索引。"
  ]);
}

// 12 搜索收藏
{
  const s = outputSlides[11];
  setTitle(s, "Conclusion", "搜索、喜欢与最近播放");
  replaceSequential(s, [
    ["Conclusion 01", "搜索与收藏"],
    ["Conclusion 02", "最近播放"],
  ]);
  replaceLorem(s, [
    "搜索匹配 title、fileName、artist 和 album，并维护最近搜索关键词。",
    "favoriteUris 使用歌曲 Uri 保存收藏状态，使播放页和喜欢页能够同步刷新。",
    "最近播放只在真正开始播放时记录，通过去重、插入首位和限制数量保持记录准确。"
  ]);
}

// 13 歌词封面
{
  const s = outputSlides[12];
  setTitle(s, "Recommendation", "歌词、封面与播放体验");
  replaceSequential(s, [
    ["Recommendation 01", "歌词同步"],
    ["Recommendation 02", "封面策略"],
  ]);
  replaceLorem(s, [
    "LRC 歌词通过正则表达式解析时间标签，TXT 歌词按行生成默认时间。",
    "播放过程中根据当前进度切换歌词行，并加入轻量淡入上滑动画。",
    "封面优先级为手动封面 > 内嵌封面 > 默认封面，并在列表、迷你播放器和播放页统一圆角显示。"
  ]);
}

// 14 状态恢复
{
  const s = outputSlides[13];
  setTitle(s, "Result", "状态保存与恢复");
  replaceLorem(s, [
    "保存内容包括资料库、播放列表、收藏、最近播放、搜索历史、播放模式、主题和歌曲媒体信息。",
    "保存方式为对象 → JSON → SharedPreferences；恢复方式为读取 JSON → 重建 Song / Playlist / favoriteUris 等对象 → 刷新界面。"
  ]);
  if (s.images.items.length) s.images.items[0].delete();
}

// 15 运行结果
{
  const s = outputSlides[14];
  setTitle(s, "Conclusion", "运行结果与测试");
  replaceSequential(s, [
    ["Conclusion 01", "代表页面"],
    ["Conclusion 02", "测试验证"],
  ]);
  replaceLorem(s, [
    "资料库页、搜索页、喜欢页和播放详情页展示主要交互流程。",
    "测试覆盖启动、导入、播放、搜索、收藏、歌词、状态恢复和 assembleDebug 构建。",
    "结果表明应用能够稳定运行，主要按钮可点击，核心状态可以跨页面同步。"
  ]);
  // The source template exports more reliably when this page remains text-based.
  // App screenshots are described here and can be inserted manually if the projector needs larger visual proof.
  replaceLorem(s, [
    "代表页面包括资料库页、搜索页、喜欢页和播放详情页，覆盖导入、检索、收藏、播放控制与歌词展示流程。",
    "测试覆盖启动、导入、播放、搜索、收藏、歌词、状态恢复和 assembleDebug 构建。",
    "验证结果显示：应用启动正常，主要按钮可点击，核心状态能够跨页面同步并在重启后恢复。"
  ]);
}

// 16 总结
{
  const s = outputSlides[15];
  setTitle(s, "Recommendation", "总结与展望");
  replaceSequential(s, [
    ["Recommendation 01", "项目总结"],
    ["Recommendation 02", "后续展望"],
  ]);
  replaceLorem(s, [
    "项目完成本地音乐播放器主要功能闭环，实现文件导入、播放控制、资源管理和状态恢复。",
    "界面结构清晰，资料库、搜索、喜欢、设置和播放详情页之间能够保持状态联动。",
    "后续可扩展后台播放服务、通知栏控制、音频焦点管理、Room 数据库、更完整媒体标签读取和多主题方案。"
  ]);
}

// 17 致谢
{
  const s = outputSlides[16];
  set(byText(s, "Thank"), "Thank");
  set(byText(s, "You"), "You");
  set(byText(s, "Rimberio University"), "感谢老师指导");
  set(byText(s, "October 2025"), "欢迎批评指正");
  replaceLorem(s, ["感谢老师在课程学习和大作业开发过程中的指导，也感谢小组成员在设计、编码、测试和报告整理中的协作。"]);
  set(byText(s, "Presented by:"), "汇报结束");
  set(byText(s, "Ketut Susilo"), "MusicPlayer3");
  set(byText(s, "www.reallygreatsite.com"), "谢谢观看");
}

await fs.mkdir(path.dirname(OUTPUT), { recursive: true });
const pptx = await artifact.PresentationFile.exportPptx(presentation);
await pptx.save(OUTPUT);

await fs.mkdir(PREVIEW, { recursive: true });
for (let i = 0; i < presentation.slides.count; i++) {
  const slide = presentation.slides.getItem(i);
  const png = await presentation.export({ slide, format: "png" });
  await fs.writeFile(`${PREVIEW}/slide-${String(i + 1).padStart(2, "0")}.png`, new Uint8Array(await png.arrayBuffer()));
}

console.log(OUTPUT);
