import fs from 'node:fs/promises';
import path from 'node:path';
const artifact = await import('file:///C:/Users/ASUS/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/@oai/artifact-tool/dist/artifact_tool.mjs');
const WORK='D:/MusicPlayer3/outputs/musicplayer-5min-image-ppt';
const TEMPLATE='D:/MusicPlayer3/outputs/manual-musicplayer3-ppt/presentations/musicplayer3-report/template.pptx';
const OUT=`${WORK}/output/MusicPlayer_本地音乐播放器_5分钟汇报版.pptx`;
const PREVIEW=`${WORK}/preview`;
const pres = await artifact.PresentationFile.importPptx(await fs.readFile(TEMPLATE));
const baseSlides = Array.from({ length: pres.slides.count }, (_, i) => pres.slides.getItem(i));
const source = baseSlides[0];
const slides=[];
for(let i=0;i<10;i++) slides.push(i===0 ? source : source.duplicate());
for (const s of baseSlides.slice(1)) s.delete();
for(let i=0;i<10;i++) slides[i].moveTo(i);
for(let i=0;i<10;i++) {
  const s=pres.slides.getItem(i);
  for (const sh of [...s.shapes.items]) sh.delete();
  for (const img of [...s.images.items]) img.delete();
  const imgPath=`${WORK}/slides/slide-${String(i+1).padStart(2,'0')}.png`;
  const bytes=await fs.readFile(imgPath);
  const dataUrl=`data:image/png;base64,${bytes.toString('base64')}`;
  const image=s.images.add({ dataUrl, fit:'cover', alt:`MusicPlayer 5min slide ${i+1}` });
  image.position={left:0, top:0, width:1920, height:1080};
}
await fs.mkdir(path.dirname(OUT), {recursive:true});
const pptx=await artifact.PresentationFile.exportPptx(pres);
await pptx.save(OUT);
await fs.mkdir(PREVIEW, {recursive:true});
for(let i=0;i<pres.slides.count;i++) {
  const slide=pres.slides.getItem(i);
  const png=await pres.export({slide, format:'png'});
  await fs.writeFile(`${PREVIEW}/slide-${String(i+1).padStart(2,'0')}.png`, new Uint8Array(await png.arrayBuffer()));
}
console.log(OUT);
