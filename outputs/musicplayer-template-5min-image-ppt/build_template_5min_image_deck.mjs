import fs from "node:fs/promises";
import path from "node:path";

const artifact = await import("file:///C:/Users/ASUS/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/@oai/artifact-tool/dist/artifact_tool.mjs");

const WORK = "D:/MusicPlayer3/outputs/musicplayer-template-5min-image-ppt";
const TEMPLATE = "D:/MusicPlayer3/outputs/manual-musicplayer3-ppt/presentations/musicplayer3-report/template.pptx";
const OUT = `${WORK}/output/MusicPlayer_本地音乐播放器_5分钟汇报版_模板风格.pptx`;
const PREVIEW = `${WORK}/preview`;

const presentation = await artifact.PresentationFile.importPptx(await fs.readFile(TEMPLATE));
const sourceSlides = Array.from({ length: presentation.slides.count }, (_, i) => presentation.slides.getItem(i));
const source = sourceSlides[0];
const slides = [];

for (let i = 0; i < 10; i++) slides.push(i === 0 ? source : source.duplicate());
for (const slide of sourceSlides.slice(1)) slide.delete();
slides.forEach((slide, index) => slide.moveTo(index));

for (let i = 0; i < 10; i++) {
  const slide = presentation.slides.getItem(i);
  for (const shape of [...slide.shapes.items]) shape.delete();
  for (const image of [...slide.images.items]) image.delete();

  const imagePath = `${WORK}/slides/slide-${String(i + 1).padStart(2, "0")}.png`;
  const bytes = await fs.readFile(imagePath);
  const dataUrl = `data:image/png;base64,${bytes.toString("base64")}`;
  const fullSlideImage = slide.images.add({
    dataUrl,
    fit: "cover",
    alt: `MusicPlayer 5-minute presentation slide ${i + 1}`,
  });
  fullSlideImage.position = { left: 0, top: 0, width: 1920, height: 1080 };
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
