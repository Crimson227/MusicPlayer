import fs from 'node:fs/promises';
const artifact = await import('file:///C:/Users/ASUS/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/@oai/artifact-tool/dist/artifact_tool.mjs');
const p='D:/MusicPlayer3/outputs/imagegen-musicplayer3-ppt/output/MusicPlayer3_本地音乐播放器_可编辑版课程汇报.pptx';
const pres=await artifact.PresentationFile.importPptx(await fs.readFile(p));
let fullImages=0, totalShapes=0, totalImages=0;
const rows=[];
for(let i=0;i<pres.slides.count;i++){
  const s=pres.slides.getItem(i);
  totalShapes += s.shapes.items.length;
  totalImages += s.images.items.length;
  for(const img of s.images.items){
    const pos=img.position||{};
    if((pos.left||0)===0 && (pos.top||0)===0 && pos.width>=1900 && pos.height>=1070) fullImages++;
  }
  rows.push((i+1)+':' + s.shapes.items.length + '/' + s.images.items.length);
}
console.log(JSON.stringify({slides:pres.slides.count,totalShapes,totalImages,fullPageImages:fullImages,perSlide:rows}, null, 2));
