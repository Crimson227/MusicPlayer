import fs from 'node:fs/promises';
import path from 'node:path';
const artifact = await import('file:///C:/Users/ASUS/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/@oai/artifact-tool/dist/artifact_tool.mjs');
const template='D:/MusicPlayer3/outputs/manual-musicplayer3-ppt/presentations/musicplayer3-report/template.pptx';
const pres = await artifact.PresentationFile.importPptx(await fs.readFile(template));
const source=pres.slides.getItem(0);
const slides=[];
for(let i=0;i<17;i++) slides.push(i===0?source:source.duplicate());
while(pres.slides.count>17) pres.slides.getItem(17).delete();
for(let i=0;i<17;i++) slides[i].moveTo(i);
for(let i=0;i<17;i++) {
  const s=pres.slides.getItem(i);
  for (const sh of [...s.shapes.items]) sh.delete();
  for (const img of [...s.images.items]) img.delete();
  const p=`D:/MusicPlayer3/outputs/imagegen-musicplayer3-ppt/slides/slide-${String(i+1).padStart(2,'0')}.png`;
  const buf=await fs.readFile(p);
  const blob=buf.buffer.slice(buf.byteOffset, buf.byteOffset+buf.byteLength);
  const image=s.images.add({ blob, fit:'cover', alt:`slide ${i+1}` });
  image.position={left:0, top:0, width:1920, height:1080};
}
await fs.mkdir('D:/MusicPlayer3/outputs/imagegen-musicplayer3-ppt/output', {recursive:true});
const out='D:/MusicPlayer3/outputs/imagegen-musicplayer3-ppt/output/test_artifact_images.pptx';
const pptx=await artifact.PresentationFile.exportPptx(pres);
await pptx.save(out);
console.log(out);
const imp=await artifact.PresentationFile.importPptx(await fs.readFile(out));
console.log('slides', imp.slides.count, 'slide1 images', imp.slides.getItem(0).images.items.length, 'shapes', imp.slides.getItem(0).shapes.items.length);
