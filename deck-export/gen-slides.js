#!/usr/bin/env node
/**
 * gen-slides.js — 数据驱动的 28 页 HTML 幻灯片生成器
 *
 * 用法：node gen-slides.js
 *
 * 输出：D:/creditbank-1/deck-export/slides-html/01-cover.html ... 28-closing.html
 *
 * 约束遵守：
 *   1. 文字全部在 <p>/<h1>-<h6> 里
 *   2. 纯色背景（不用渐变）
 *   3. 背景/边框/阴影只在 DIV 上
 *   4. 图片用 <img> 标签
 *   5. body = 960pt x 540pt (LAYOUT_WIDE)
 */

import fs from 'fs';
import path from 'path';

const OUT_DIR = path.resolve('D:/creditbank-1/deck-export/slides-html');

// ── 色彩系统 (neo-brutalism) ──
const C = {
  cream:    '#FFF9F0',
  ink:      '#1A202C',
  green:    '#22C55E',
  blue:     '#BEE3F8',
  blueDark: '#3B82F6',
  purple:   '#7C3AED',
  purpleLt: '#DDD6FE',
  pink:     '#EF4444',
  pinkLt:   '#FECACA',
  yellow:   '#FEF08A',
  darkBg:   '#1E293B',      // 章节页深色背景
  white:    '#FFFFFF',
  gray600:  '#4A5568',
  gray400:  '#A0AEC0',
};

// ── 公共 CSS reset + body ──
const BASE_CSS = `
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  width: 960pt; height: 540pt;
  font-family: "PingFang SC", "Microsoft YaHei", system-ui, -apple-system, sans-serif;
  background: ${C.cream};
  overflow: hidden;
  position: relative;
}`;

// ═══════════════════════════════════════════
// 布局模板函数 — 每个返回完整 HTML string
// ═══════════════════════════════════════════

/** 通用辅助：绝对定位 div 容器 */
function absDiv(style, children) {
  return `<div style="position:absolute;${style}">${children || ''}</div>`;
}

/** 卡片容器：粗黑边框 + 硬阴影 */
function cardDiv(top, left, w, h, bg, extraStyle, children) {
  const s = `top:${top};left:${left};width:${w};height:${h};background:${bg};border:2pt solid ${C.ink};border-radius:6pt;padding:12pt 16pt;box-shadow:4pt 4pt 0 ${C.ink};${extraStyle||''}`;
  return absDiv(s, children);
}

// ── P1 封面 ──
function p1_cover() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  <!-- 左上绿星 -->
  ${absDiv('top:50pt;left:60pt;width:56pt;height:56pt;', `<div style="width:100%;height:100%;background:${C.green};clip-path:polygon(50% 0%,61% 35%,98% 35%,68% 57%,79% 91%,50% 70%,21% 91%,32% 57%,2% 35%,39% 35%);"></div>`)}
  <!-- 主标题 -->
  ${absDiv('top:120pt;left:60pt;', `<h1 style="font-size:52pt;color:${C.ink};font-weight:900;letter-spacing:2pt;">星秩存册</h1>
  <p style="font-size:20pt;color:${C.gray600};margin-top:8pt;">Singularis Ledger</p>
  <p style="font-size:18pt;color:${C.gray600};margin-top:6pt;">终身学习学分银行平台</p>`)}
  <!-- 绿色按钮标签 -->
  ${absDiv('top:260pt;left:60pt;', `<div style="background:${C.green};border:2pt solid ${C.ink};border-radius:8pt;padding:10pt 28pt;box-shadow:3pt 3pt 0 ${C.ink};display:inline-block;"><p style="font-size:18pt;color:${C.ink};font-weight:700;">界面设计答辩</p></div>`)}
  <!-- 底部信息 -->
  ${absDiv('bottom:40pt;left:60pt;', `<p style="font-size:13pt;color:${C.gray600};">答辩人：____ &nbsp;&nbsp; 指导教师：____ &nbsp;&nbsp; 日期：2026.07</p>`)}
  <!-- 右侧大星 -->
  ${absDiv('top:140pt;right:50pt;width:160pt;height:160pt;', `<div style="width:100%;height:100%;background:${C.blue};clip-path:polygon(50% 0%,61% 35%,98% 35%,68% 57%,79% 91%,50% 70%,21% 91%,32% 57%,2% 35%,39% 35%);"></div>`)}
  <!-- 右下装饰圆点 -->
  ${absDiv('bottom:80pt;right:120pt;width:28pt;height:28pt;border-radius:50%;background:#FCA5A5;')}
  ${absDiv('bottom:50pt;right:80pt;width:28pt;height:28pt;border-radius:50%;background:${C.purpleLt};')}
</body></html>`;
}

// ── P2 一句话定位 ──
function p2_positioning() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:48pt;left:60pt;', `<p style="font-size:14pt;color:${C.green};font-weight:600;letter-spacing:1pt;">一句话定位 · POSITIONING</p>`)}
  ${absDiv('top:85pt;left:60pt;width:6pt;height:24pt;background:${C.green};border-radius:3pt;','')}
  <!-- 主句 -->
  ${absDiv('top:130pt;left:60pt;right:80pt;', `<h1 style="font-size:36pt;color:${C.ink};font-weight:800;line-height:1.4;">我们的平台，是学习的图书馆，也是<span style="background:${C.purpleLt};padding:0 6pt;">"健身房"</span>。</h1>`)}
  <!-- 副句 -->
  ${absDiv('top:260pt;left:60pt;', `<p style="font-size:22pt;color:${C.ink};font-weight:600;">秩然有序，册录一生。</p>`)}
  <!-- 装饰星 -->
  ${absDiv('top:90pt;right:60pt;width:72pt;height:72pt;', `<div style="width:100%;height:100%;background:${C.pinkLt};clip-path:polygon(50% 0%,61% 35%,98% 35%,68% 57%,79% 91%,50% 70%,21% 91%,32% 57%,2% 35%,39% 35%);"></div>`)}
</body></html>`;
}

// ── P3 目录 ──
function p3_contents() {
  const items = [
    { num: '01', text: '为什么是星秩存册', color: C.green },
    { num: '02', text: '项目背景与需求', color: C.blue },
    { num: '03', text: '产品介绍',        color: C.pinkLt },
    { num: '04', text: '技术路线',         color: C.purpleLt },
    { num: '05', text: '组织管理 & 小结',  color: C.yellow },
  ];
  const listItems = items.map((it, i) =>
    absDiv(`top:${130 + i * 64}pt;left:60pt;`, `
      <div style="display:flex;align-items:center;gap:16pt;">
        <div style="width:28pt;height:28pt;border-radius:50%;background:${it.color};border:2pt solid ${C.ink};display:flex;align-items:center;justify-content:center;"><p style="font-size:11pt;color:${C.ink};font-weight:700;">${it.num}</p></div>
        <p style="font-size:22pt;color:${C.ink};font-weight:600;">${it.text}</p>
      </div>`)
  ).join('\n');
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:50pt;left:60pt;', `<h1 style="font-size:36pt;color:${C.ink};font-weight:900;">目录 / CONTENTS</h1>
  <div style="width:60pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:8pt;"></div>`)}
  ${listItems}
  ${absDiv('bottom:50pt;right:60pt;width:96pt;height:96pt;', `<div style="width:100%;height:100%;background:${C.green};clip-path:polygon(50% 0%,61% 35%,98% 35%,68% 57%,79% 91%,50% 70%,21% 91%,32% 57%,2% 35%,39% 35%);"></div>`)}
</body></html>`;
}

// ── 章节页模板 (P4/P8/P13/P22) ──
function chapterPage(actNum, title, accentColor) {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS.replace(C.cream, C.darkBg)}</style></head><body style="background:${C.darkBg};">
  ${absDiv('top:70pt;left:80pt;', `<p style="font-size:16pt;color:${C.blue};font-weight:600;letter-spacing:3pt;">ACT ${String(actNum).padStart(2,'0')}</p>`)}
  ${absDiv('top:150pt;left:80pt;', `<h1 style="font-size:110pt;color:${accentColor};font-weight:900;line-height:1;font-style:normal;">${String(actNum).padStart(2,'0')}</h1>`)}
  ${absDiv('top:290pt;left:80pt;', `<h2 style="font-size:32pt;color:${C.white};font-weight:700;">${title}</h2>`)}
  ${absDiv('bottom:50pt;right:60pt;width:88pt;height:88pt;', `<div style="width:100%;height:100%;background:${accentColor === C.green ? C.yellow : accentColor};opacity:0.85;clip-path:polygon(50% 0%,61% 35%,98% 35%,68% 57%,79% 91%,50% 70%,21% 91%,32% 57%,2% 35%,39% 35%);"></div>`)}
</body></html>`;
}

// ── P5 三个断点 ──
function p5_breakpoints() {
  const cards = [
    { title: '脱节', desc: '技术迭代日新月异，可大学课堂的知识更新跟不上，于是教授教的 ≠ 职场要的。', color: C.blue, top: 130 },
    { title: '无氛围 / 无激励', desc: '在家就能练，为什么还有人去健身房？氛围与心理暗示无可替代。', color: C.yellow, top: 130 },
    { title: '难认证', desc: '学分零散在各平台，无法累积、无法流通、企业不认。', color: C.pinkLt, top: 130 },
  ];
  const cardHtml = cards.map((c, i) => {
    const left = 50 + i * 300;
    return cardDiv(c.top, `${left}pt`, '270pt', '220pt', c.color, '', `
      <h2 style="font-size:22pt;color:${C.ink};font-weight:800;margin-bottom:12pt;">${c.title}</h2>
      <p style="font-size:13pt;color:${C.ink};line-height:1.6;">${c.desc}</p>
    `);
  }).join('\n');
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:45pt;left:60pt;', `<h1 style="font-size:30pt;color:${C.ink};font-weight:900;">真问题：终身学习的三个断点</h1>`)}
  ${cardHtml}
  ${absDiv('top:385pt;left:60pt;', `<div style="background:${C.pink};border:2pt solid ${C.ink};border-radius:6pt;padding:10pt 18pt;display:inline-block;box-shadow:3pt 3pt 0 ${C.ink};"><p style="font-size:14pt;color:${C.white};font-weight:600;">第三个"断"最痛 —— 学了 = 白学</p></div>`)}
</body></html>`;
}

// ── P6 色彩心理学 ──
function p6_colorPsychology() {
  const chips = [
    { name: '绿 · #22C55E', desc: '成长 / 生机，缓释学习焦虑', color: C.green },
    { name: '蓝 · #BEE3F8', desc: '信任 / 专业，呼应学分银行的严谨', color: C.blue },
    { name: '奶油底 · #FFF9F0', desc: '温和亲和，跨越儿童到长者，契合终生学习', color: C.cream, border: true },
  ];
  const chipHtml = chips.map((c, i) =>
    cardDiv('130pt', `${50 + i * 300}pt`, '270pt', '230pt', c.color, c.border ? `border:2pt solid ${C.ink};` : '', `
      <div style="width:24pt;height:24pt;border-radius:50%;background:${c.border ? C.ink : c.color};border:${c.border?'2pt solid '+C.ink:'none'};margin-bottom:10pt;"></div>
      <h3 style="font-size:17pt;color:${C.ink};font-weight:700;margin-bottom:10pt;">${c.name}</h3>
      <p style="font-size:13pt;color:${C.ink};line-height:1.6;">${c.desc}</p>
    `)
  ).join('\n');
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;right:60pt;', `<h1 style="font-size:28pt;color:${C.ink};font-weight:900;">为什么是这套界面？</h1>
  <p style="font-size:13pt;color:${C.gray600};margin-top:8pt;line-height:1.5;">色彩先于文字影响人的潜意识——正如麦当劳、肯德基用红色留客；界面同理，配色悄悄决定用户愿不愿意留下。</p>`)}
  ${chipHtml}
  ${absDiv('bottom:38pt;left:60pt;', `<p style="font-size:16pt;color:${C.ink};font-weight:700;">界面即品牌 —— 好的配色，让用户第一眼就愿意留下。</p>`)}
</body></html>`;
}

// ── P7 价值飞轮 ──
function p7_valueFlywheel() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:28pt;color:${C.ink};font-weight:900;">产品定位与价值飞轮</h1>`)}
  <!-- 飞轮：三角 + 箭头示意 -->
  ${absDiv('top:120pt;left:180pt;width:580pt;height:360pt;', `
    <!-- 学员 -->
    ${cardDiv('20pt','0pt','180pt','120pt',C.green,'',`<h3 style="font-size:16pt;color:${C.ink};font-weight:700;">学员</h3><p style="font-size:11pt;color:${C.ink};line-height:1.5;">课程丰富被吸引<br/>攒分 / 兑换权益</p>`)}
    <!-- 平台（中心） -->
    ${cardDiv('120pt','200pt','180pt','120pt',C.blue,'',`<h3 style="font-size:16pt;color:${C.ink};font-weight:700;">平台</h3><p style="font-size:11pt;color:${C.ink};line-height:1.5;">健身房+图书馆<br/>可信学分银行</p>`)}
    <!-- 企业 -->
    ${cardDiv('20pt','400pt','180pt','120pt',C.purpleLt,'',`<h3 style="font-size:16pt;color:${C.ink};font-weight:700;">企业 / 机构</h3><p style="font-size:11pt;color:${C.ink};line-height:1.5;">招到高质量人才<br/>入驻平台</p>`)}
    <!-- 箭头标注 -->
    ${absDiv('top:65pt;left:185pt;', `<p style="font-size:12pt;color:${C.green};font-weight:700;">→ 吸引</p>`)}
    ${absDiv('top:165pt;left:385pt;', `<p style="font-size:12pt;color:${C.blue};font-weight:700;">→ 氛围</p>`)}
    ${absDiv('top:165pt;left:15pt;', `<p style="font-size:12pt;color:${C.purple};font-weight:700;">← 人才</p>`)}
  `)}
</body></html>`;
}

// ── P9 项目背景 ──
function p9_background() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">1.1 项目背景</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  ${absDiv('top:105pt;left:60pt;width:420pt;', `
    ${cardDiv('0pt','0pt','400pt','100pt',C.green,'',`<h3 style="font-size:15pt;color:${C.ink};font-weight:700;">政策驱动</h3><p style="font-size:12pt;color:${C.ink};line-height:1.55;margin-top:6pt;">· 终身学习已写入国家发展战略<br/>· 学分银行是国家级方向（教育部试点）<br/>· 数字化转型加速在线教育普及</p>`)}
    ${cardDiv('115pt','0pt','400pt','100pt',C.blue,'',`<h3 style="font-size:15pt;color:${C.ink};font-weight:700;">现实痛点</h3><p style="font-size:12pt;color:${C.ink};line-height:1.55;margin-top:6pt;">· 各平台"学分孤岛"，互不互通<br/>· 认证能力弱，企业不认可<br/>· 缺乏统一的学习激励体系</p>`)}
    ${cardDiv('230pt','0pt','400pt','95pt',C.yellow,'',`<h3 style="font-size:15pt;color:${C.ink};font-weight:700;">我们的机会</h3><p style="font-size:12pt;color:${C.ink};line-height:1.55;margin-top:6pt;">用"学分银行 + 哈希存证 + Agent"把断点接起来</p>`)}
  `)}
  <!-- 右侧趋势示意 -->
  ${absDiv('top:115pt;right:60pt;width:280pt;height:330pt;background:#F0FDF4;border:2pt solid ${C.ink};border-radius:8pt;padding:16pt;box-shadow:4pt 4pt 0 ${C.ink};',`
    <h3 style="font-size:14pt;color:${C.ink};font-weight:700;text-align:center;margin-bottom:14pt;">趋势示意</h3>
    <p style="font-size:11pt;color:${C.gray600};text-align:center;line-height:1.7;">政策推动 → 孤岛痛点 →<br/>学分银行兴起 →<br/><b style="color:${C.green};">星秩存册</b></p>
  `)}
</body></html>`;
}

// ── P10 用户调研 ──
function p10_userResearch() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">1.2 用户调研</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  ${absDiv('top:105pt;left:60pt;width:400pt;', cardDiv('0pt','0pt','380pt','210',C.green,'',`
    <h3 style="font-size:17pt;color:${C.ink};font-weight:800;margin-bottom:14pt;">学员侧</h3>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">✓ 有氛围能坚持（不想一个人孤零零地学）</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">✓ 学完要有凭证（不是一张废纸）</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">✓ 能兑换实际权益（积分 ≠ 虚拟数字）</p>
  `))}
  ${absDiv('top:105pt;right:60pt;width:400pt;', cardDiv('0pt','0pt','380pt','210',C.blue,'',`
    <h3 style="font-size:17pt;color:${C.ink};font-weight:800;margin-bottom:14pt;">机构 / 企业侧</h3>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">✓ 可验证的能力证明（不是自说自话）</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">✓ 能定向招人（精准匹配）</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">✓ 低成本触达潜在学员</p>
  `))}
  ${absDiv('bottom:40pt;left:60pt;', `<p style="font-size:14pt;color:${C.ink};font-weight:600;font-style:italic;">核心诉求：可信、有激励、能流通 —— 这正是星秩存册要解决的。</p>`)}
</body></html>`;
}

// ── P11 功能性需求（八大模块矩阵）──
function p11_functionalReqs() {
  const modules = [
    ['积分商城', '学→攒分→兑换，游戏化激励闭环'],
    ['学习档案', '可信累积，跨模块学分汇总'],
    ['学习成果', '证书编号+二维码+哈希存证'],
    ['论坛', '发帖/回复/点赞/举报/板块'],
    ['报名签到', '课程签到+活动报名'],
    ['招聘求职', '投递→视频面试→结果通知'],
    ['区块链校验', 'SHA-256哈希+扫码验真'],
    ['诚信评定', '行为数据计算可信分，门控权益'],
  ];
  const grid = modules.map((m, i) => {
    const row = Math.floor(i / 4);
    const col = i % 4;
    const colors = [C.green, C.blue, C.purpleLt, C.yellow];
    const bg = colors[col % 4];
    const t = 115 + row * 175;
    const l = 60 + col * 215;
    return cardDiv(`${t}pt`,`${l}pt`,'195pt','155pt',bg,'',`
      <h3 style="font-size:14pt;color:${C.ink};font-weight:700;margin-bottom:8pt;">${m[0]}</h3>
      <p style="font-size:11pt;color:${C.ink};line-height:1.5;">${m[1]}</p>
    `);
  }).join('\n');
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">1.3 功能性需求 — 八大模块矩阵</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  ${grid}
</body></html>`;
}

// ── P12 非功能性需求 ──
function p12_nonFunctionalReqs() {
  const quads = [
    { title: '性能', items: ['并发支持 Redis 缓存', '首页聚合数据 < 1s'], color: C.green },
    { title: '安全', items: ['BCrypt 密码加密', 'SHA-256 凭证防伪', '参数校验防注入'], color: C.blue },
    { title: '可用性', items: ['三端适配（学员/机构/管理员）', '清晰错误提示与引导'], color: C.purpleLt },
    { title: '可维护 / 可扩展', items: ['模块化业务分包', '预留扩展接口'], color: C.yellow },
  ];
  const qHtml = quads.map((q, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    return cardDiv(`${115 + row * 185}pt`, `${60 + col * 450}pt`, '420pt', '170pt', q.color, '', `
      <h3 style="font-size:17pt;color:${C.ink};font-weight:800;margin-bottom:10pt;">${q.title}</h3>
      ${q.items.map(it => `<p style="font-size:13pt;color:${C.ink};line-height:1.6;">· ${it}</p>`).join('')}
    `);
  }).join('\n');
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">1.4 非功能性需求</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  ${qHtml}
</body></html>`;
}

// ── P14 项目概况 + 功能全景 ──
function p14_overview() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">2.1 项目概况 + 功能全景</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  ${absDiv('top:100pt;left:60pt;', `<p style="font-size:15pt;color:${C.ink};font-weight:600;line-height:1.5;">三端一体：学员 / 机构 / 管理员 —— 围绕"学分银行"核心运转的 8 大模块</p>`)}
  <!-- 中心节点 -->
  ${absDiv('top:145pt;left:370pt;width:200pt;height:70pt;background:${C.green};border:3pt solid ${C.ink};border-radius:10pt;display:flex;align-items:center;justify-content:center;box-shadow:4pt 4pt 0 ${C.ink};',`<h3 style="font-size:18pt;color:${C.ink};font-weight:800;">学分银行 核心</h3>`)}
  <!-- 8 个环绕模块 -->
  ${(() => {
    const mods = ['积分商城','学习档案','学习成果','论坛','报名签到','招聘求职','诚信评定','区块链'];
    const positions = [
      [130,145],[130,300],[255,365],[430,365],[580,300],[580,145],[430,80],[255,80]
    ];
    const clrs = [C.blue,C.purpleLt,C.yellow,C.green,C.blue,C.purpleLt,C.yellow,C.green];
    return mods.map((m,i)=>cardDiv(`${positions[i][1]}pt`,`${positions[i][0]}pt`,'130pt','58pt',clrs[i],'',`<p style="font-size:12pt;color:${C.ink};font-weight:600;text-align:center;">${m}</p>`)).join('\n');
  })()}
  <!-- 三端标识 -->
  ${absDiv('bottom:40pt;left:60pt;right:60pt;', `
    <div style="display:flex;gap:20pt;justify-content:center;">
      ${['学员端','机构端','管理端'].map(t=>`<div style="background:${t==='学员端'?C.green:t==='机构端'?C.blue:C.purpleLt};border:2pt solid ${C.ink};border-radius:6pt;padding:6pt 18pt;box-shadow:2pt 2pt 0 ${C.ink};"><p style="font-size:13pt;color:${C.ink};font-weight:600;">${t}</p></div>`).join('')}
    </div>
  `)}
</body></html>`;
}

// ── P15 用户群体（三端）──
function p15_userGroups() {
  const roles = [
    { role: '学员', color: C.green, items: ['学课程、攒积分','查看学习档案','兑换积分权益','求职投递'] },
    { role: '机构 / 教师', color: C.blue, items: ['发布课程与管理','签到考勤','评定诚信分','发放凭证证书'] },
    { role: '管理员', color: C.purpleLt, items: ['审核内容与用户','系统配置与监控','数据分析看板','缓存管理与运维'] },
  ];
  const cols = roles.map((r, i) =>
    cardDiv('105pt', `${60 + i * 300}pt`, '270pt', '310pt', r.color, '', `
      <h3 style="font-size:20pt;color:${C.ink};font-weight:800;margin-bottom:16pt;">${r.role}</h3>
      ${r.items.map(it => `<p style="font-size:13pt;color:${C.ink};line-height:1.8;">· ${it}</p>`).join('')}
    `)
  ).join('\n');
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">2.2 用户群体（三端）</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  ${cols}
</body></html>`;
}

// ── P16 项目展示（占位）──
function p16_showcase() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">2.3 项目展示（界面截图 / 动图）</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  ${absDiv('top:100pt;left:60pt;right:60pt;height:350pt;border:3pt dashed ${C.gray400};border-radius:10pt;display:flex;flex-direction:column;align-items:center;justify-content:center;background:#FAFAF5;',`
    <p style="font-size:18pt;color:${C.gray600};font-weight:600;">📷 截图占位区域</p>
    <p style="font-size:13pt;color:${C.gray400};margin-top:10pt;text-align:center;width:500pt;">请在此处放置：<br/>学员首页（氛围感）| 学习档案（可信累积）| 积分商城（激励闭环）<br/>或项目运行录屏动图</p>
  `)}
</body></html>`;
}

// ── P17 Agent 智能学伴 ──
function p17_agent() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">亮点① Agent 智能学伴</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>
  <p style="font-size:13pt;color:${C.gray600};margin-top:8pt;">LLM 驱动的学伴 —— 把"健身房私教"做成"学习私教"</p>`)}
  ${absDiv('top:125pt;left:60pt;width:520pt;', `
    ${cardDiv('0pt','0pt','500pt','140pt',C.green,'',`
      <h3 style="font-size:16pt;color:${C.ink};font-weight:800;margin-bottom:12pt;">三大能力</h3>
      <div style="display:flex;gap:12pt;">
        ${['对话答疑','学习路径推荐','陪练伴学'].map(t=>`<div style="background:${C.cream};border:2pt solid ${C.ink};border-radius:6pt;padding:8pt 14pt;box-shadow:2pt 2pt 0 ${C.ink};"><p style="font-size:13pt;color:${C.ink};font-weight:600;">${t}</p></div>`).join('')}
      </div>
      <p style="font-size:12pt;color:${C.ink};line-height:1.6;margin-top:12pt;">用户提问 → AgentService 拼装学习画像上下文 → DeepSeek LLM → 个性化回答 / AI简历生成</p>
    `)}
    ${cardDiv('155pt','0pt','500pt','130pt',C.blue,'',`
      <h3 style="font-size:15pt;color:${C.ink};font-weight:700;margin-bottom:8pt;">亮点：档案即上下文</h3>
      <p style="font-size:12pt;color:${C.ink};line-height:1.6;">不是简单套壳问答 —— 注入信用余额 / 诚信分 / 课程进度 / 目标技能 / 心仪职位，让回答贴合个人情况。</p>
    `)}
  `)}
  ${absDiv('top:125pt;right:60pt;width:300pt;height:285pt;border:3pt dashed ${C.gray400};border-radius:10pt;display:flex;flex-direction:column;align-items:center;justify-content:center;background:#FAFAF5;',`
    <p style="font-size:14pt;color:${C.gray600};">Agent 对话界面截图占位</p>
  `)}
</body></html>`;
}

// ── P18 证书哈希存证 ──
function p18_hashAttestation() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">亮点② 学分凭证哈希存证</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>
  <p style="font-size:13pt;color:${C.gray600};margin-top:8pt;">诚实口径：SHA-256 防伪哈希（非上链），预留联盟链演进空间</p>`)}
  <!-- 流程 -->
  ${absDiv('top:115pt;left:60pt;right:60pt;', `
    <div style="display:flex;align-items:center;gap:8pt;justify-content:center;">
      ${[
        ['学习成果',C.green],
        ['SHA-256\n哈希',C.blue],
        ['凭证+二维码',C.purpleLt],
        ['扫码/接口\n一键验真',C.yellow],
      ].map(([txt,bg],i)=>`
        <div style="background:${bg};border:2pt solid ${C.ink};border-radius:8pt;padding:14pt 18pt;box-shadow:3pt 3pt 0 ${C.ink};text-align:center;min-width:120pt;">
          <p style="font-size:13pt;color:${C.ink};font-weight:700;white-space:pre-line;">${txt}</p>
        </div>
        ${i<3?`<p style="font-size:20pt;color:${C.ink};font-weight:700;">→</p>`:''}
      `.trim()).join('')}
    </div>
  `)}
  ${cardDiv('220pt','60pt','840pt','130pt',C.cream,'border:2pt solid #CBD5E1;',`
    <h3 style="font-size:15pt;color:${C.ink};font-weight:700;margin-bottom:10pt;">关键特性</h3>
    <div style="display:flex;gap:20pt;flex-wrap:wrap;">
      ${['防伪造：哈希唯一绑定内容','防篡改：任何改动都会导致哈希不一致','可验证：公开接口 + 二维码扫码验真','预留演进空间：可对接联盟链上链'].map(t=>`<div style="background:${C.white};border:2pt solid ${C.ink};border-radius:6pt;padding:8pt 14pt;"><p style="font-size:12pt;color:${C.ink};">✓ ${t}</p></div>`).join('')}
    </div>
  `)}
  ${absDiv('bottom:38pt;left:60pt;', `<p style="font-size:13pt;color:${C.pink};font-weight:600;">⚠️ 口径说明：当前为"区块链校验"功能的哈希存证实现，未使用智能合约或公链。</p>`)}
</body></html>`;
}

// ── P19 积分激励 + 诚信评定 ──
function p19_pointsIntegrity() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">亮点③ 积分激励 + ④ 诚信评定</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  ${absDiv('top:110pt;left:60pt;width:420pt;', cardDiv('0pt','0pt','400pt','290',C.yellow,'',`
    <h3 style="font-size:18pt;color:${C.ink};font-weight:800;margin-bottom:14pt;">积分商城 — 游戏化激励闭环</h3>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">对应"健身房打卡激励"</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;margin-top:10pt;">· 学习 → 自动攒积分</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">· 积累到阈值 → 解锁兑换资格</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">· 商城兑换 → 实体/虚拟权益</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">· 兑换行为 → 反向刺激继续学习</p>
  `))}
  ${absDiv('top:110pt;right:60pt;width:420pt;', cardDiv('0pt','0pt','400pt','290',C.purpleLt,'',`
    <h3 style="font-size:18pt;color:${C.ink};font-weight:800;margin-bottom:14pt;">诚信评定 — 可信分的门控机制</h3>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">基于行为数据的动态评分</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;margin-top:10pt;">· 观看完成度</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">· 作答质量</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;">· 提交原创度检测</p>
    <p style="font-size:13pt;color:${C.ink};line-height:1.7;margin-top:10pt;">诚信分低 → 门控部分高阶权益</p>
  `))}
</body></html>`;
}

// ── P20 全链路求职闭环 ──
function p20_recruitment() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">亮点⑤ 全链路求职闭环</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>
  <p style="font-size:13pt;color:${C.gray600};margin-top:8pt;">从简历到 offer 一站式 —— 可信学分直接背书求职</p>`)}
  <!-- 流程链路 -->
  ${absDiv('top:115pt;left:60pt;right:60pt;', `
    <div style="display:flex;align-items:center;gap:10pt;justify-content:center;">
      ${[
        ['投递简历\n(带可信凭证)',C.green],
        ['机构筛选',C.blue],
        ['TRTC\n在线面试',C.purpleLt],
        ['结果通知',C.yellow],
      ].map(([txt,bg],i)=>`
        <div style="background:${bg};border:2pt solid ${C.ink};border-radius:8pt;padding:16pt 20pt;box-shadow:3pt 3pt 0 ${C.ink};text-align:center;min-width:130pt;">
          <p style="font-size:13pt;color:${C.ink};font-weight:700;white-space:pre-line;">${txt}</p>
        </div>
        ${i<3?`<p style="font-size:22pt;color:${C.ink};font-weight:700;">→</p>`:''}
      `.trim()).join('')}
    </div>
  `)}
  ${cardDiv('225pt','60pt','840pt','135pt',C.cream,'border:2pt solid #CBD5E1;',`
    <h3 style="font-size:15pt;color:${C.ink};font-weight:700;margin-bottom:10pt;">为什么这是亮点？</h3>
    <p style="font-size:13pt;color:${C.ink};line-height:1.65;">学员用平台累积的<b>可信学分 / 区块链凭证</b>给简历背书，机构在平台内直接完成筛选、约面（TRTC 真实音视频）、发结果。<br/>呼应价值飞轮：<b>学员的"可信能力"直接对接企业需求</b> —— 这是我们吸引机构入驻的关键。</p>
  `)}
  ${absDiv('bottom:38pt;left:60pt;width:340pt;height:80pt;border:2pt dashed ${C.gray400};border-radius:8pt;display:flex;align-items:center;justify-content:center;background:#FAFAF5;',`<p style="font-size:12pt;color:${C.gray600};">在线面试界面截图占位</p>`)}
</body></html>`;
}

// ── P21 轻松活泼教学体验 ──
function p21_learningExperience() {
  const features = [
    { icon: '💬', title: '弹幕互动', desc: '课程视频边看边发弹幕，像刷视频一样有陪伴感' },
    { icon: '✅', title: '打卡签到', desc: '养成学习习惯，同时积攒积分' },
    { icon: '🎓', title: '完课自动发证', desc: '学完即得证书，即时正反馈' },
  ];
  const featHtml = features.map((f, i) =>
    cardDiv('115pt', `${60 + i * 300}pt`, '270pt', '240pt', [C.green, C.blue, C.purpleLt][i], '', `
      <p style="font-size:28pt;">${f.icon}</p>
      <h3 style="font-size:17pt;color:${C.ink};font-weight:800;margin:10pt 0 8pt 0;">${f.title}</h3>
      <p style="font-size:13pt;color:${C.ink};line-height:1.6;">${f.desc}</p>
    `)
  ).join('\n');
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">亮点⑥ 轻松活泼的教学体验</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>
  <p style="font-size:13pt;color:${C.gray600};margin-top:8pt;">让学习不孤单 —— "图书馆/健身房"氛围的产品化落地</p>`)}
  ${featHtml}
  ${absDiv('bottom:38pt;left:60pt;right:60pt;', `<p style="font-size:13pt;color:${C.ink};font-style:italic;">还有：评论区交流心得 + 课件一键获取 —— 氛围感 + 即时反馈，帮学员坚持下去。</p>`)}
</body></html>`;
}

// ── P23 架构总览 + 技术选型 ──
function p23_architecture() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:38pt;left:60pt;', `<h1 style="font-size:25pt;color:${C.ink};font-weight:900;">架构总览 + 技术选型</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>
  <p style="font-size:12pt;color:${C.pink};margin-top:4pt;font-weight:600;">分层单体（非微服务），可平滑演进</p>`)}
  ${absDiv('top:100pt;left:50pt;width:600pt;height:320pt;', `
    <!-- 分层架构示意 -->
    ${cardDiv('0pt','0pt','580pt','52pt',C.green,'',`<p style="font-size:14pt;color:${C.ink};font-weight:700;text-align:center;">前端 SPA（Vue3 + TS + Vite）— 三端合一</p>`)}
    ${cardDiv('62pt','0pt','580pt','52pt',C.blue,'',`<p style="font-size:14pt;color:${C.ink};font-weight:700;text-align:center;">统一网关拦截器（JWT 鉴权 + 角色归属校验）</p>`)}
    ${cardDiv('124pt','0pt','580pt','100pt',C.purpleLt,'',`
      <p style="font-size:13pt;color:${C.ink};font-weight:700;text-align:center;margin-bottom:6pt;">业务层（Spring Boot 3.4 分包）</p>
      <p style="font-size:10pt;color:${C.ink};text-align:center;line-height:1.5;">学习 / 档案 / 积分 / 诚信 / 论坛 / 机构 / Agent / 面试 / …</p>
    `)}
    ${cardDiv('234pt','0pt','580pt','46pt',C.yellow,'',`<p style="font-size:14pt;color:${C.ink};font-weight:700;text-align:center;">数据层 MySQL 8 + Redis 缓存</p>`)}
    <!-- 外部服务 -->
    ${absDiv('top:72pt;left:-10pt;', `<div style="background:${C.pinkLt};border:2pt solid ${C.ink};border-radius:6pt;padding:6pt 12pt;box-shadow:2pt 2pt 0 ${C.ink};"><p style="font-size:11pt;color:${C.ink};font-weight:600;">DeepSeek LLM</p></div>`)}
    ${absDiv('top:72pt;right:-10pt;', `<div style="background:${C.blue};border:2pt solid ${C.ink};border-radius:6pt;padding:6pt 12pt;box-shadow:2pt 2pt 0 ${C.ink};"><p style="font-size:11pt;color:${C.ink};font-weight:600;">腾讯云 TRTC</p></div>`)}
  `)}
  <!-- 技术栈标签墙 -->
  ${absDiv('top:100pt;right:40pt;width:250pt;', `
    <h3 style="font-size:14pt;color:${C.ink};font-weight:700;margin-bottom:10pt;">技术栈</h3>
    ${['Java 21','Spring Boot 3.4','MyBatis-Plus','Vue3 + TS + Vite','Element Plus','MySQL 8','Redis','Docker Compose','DeepSeek API','TRTC SDK'].map(t=>
      `<div style="display:inline-block;background:${C.cream};border:2pt solid ${C.ink};border-radius:4pt;padding:4pt 10pt;margin:3pt;box-shadow:1pt 1pt 0 ${C.ink};"><p style="font-size:10pt;color:${C.ink};font-weight:600;">${t}</p></div>`
    ).join('')}
  `)}
</body></html>`;
}

// ── P24 技术亮点全景 ──
function p24_techHighlights() {
  const items = [
    { n: '01', title: 'Agent 智能学伴', desc: '真调 DeepSeek LLM，对话答疑 + AI简历生成 + 画像注入', tag: '→ P25 深讲' },
    { n: '02', title: 'TRTC 线上面试', desc: '腾讯云 TRTC 真实音视频，后端签发 UserSig', tag: '→ P26 深讲' },
    { n: '03', title: 'JWT 三端隔离', desc: '自定义拦截器 + 角色/归属校验，防越权', tag: '' },
    { n: '04', title: '哈希存证+验真', desc: 'SHA-256 防伪哈希 + 二维码/接口一键验真', tag: '' },
    { n: '05', title: '学分领域模型', desc: '学分/积分累积—兑换—诚信门控建模', tag: '' },
    { n: '06', title: '工程化基座', desc: 'MBP分页/BCrypt/Redis缓存/Docker部署', tag: '' },
  ];
  const grid = items.map((it, i) => {
    const row = Math.floor(i / 3);
    const col = i % 3;
    const colors = [C.green, C.blue, C.purpleLt, C.yellow, C.green, C.blue];
    return cardDiv(`${110 + row * 175}pt`, `${60 + col * 295}pt`, '275pt', '155pt', colors[i], '', `
      <div style="display:flex;align-items:center;gap:8pt;margin-bottom:8pt;">
        <div style="background:${C.ink};color:${C.white};border-radius:4pt;padding:2pt 8pt;"><p style="font-size:12pt;font-weight:700;">${it.n}</p></div>
        <h3 style="font-size:14pt;color:${C.ink};font-weight:700;">${it.title}</h3>
      </div>
      <p style="font-size:11pt;color:${C.ink};line-height:1.5;">${it.desc}</p>
      ${it.tag ? `<p style="font-size:10pt;color:${C.green};font-weight:600;margin-top:6pt;">${it.tag}</p>` : ''}
    `);
  }).join('\n');
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:38pt;left:60pt;', `<h1 style="font-size:25pt;color:${C.ink};font-weight:900;">技术亮点全景 — 6 个真实技术点</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>
  `)}
  ${absDiv('top:34pt;right:60pt;', `<div style="background:${C.green};border:2pt solid ${C.ink};border-radius:6pt;padding:4pt 12pt;box-shadow:2pt 2pt 0 ${C.ink};display:inline-block;"><p style="font-size:11pt;color:${C.ink};font-weight:700;">全部有代码支撑 ✓</p></div>`)}
  ${grid}
</body></html>`;
}

// ── P25 Agent 深讲 ──
function p25_agentDeepDive() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:38pt;left:60pt;', `<h1 style="font-size:25pt;color:${C.ink};font-weight:900;">深讲① Agent 智能学伴（LLM 调用链路）</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  <!-- 时序流程 -->
  ${absDiv('top:100pt;left:50pt;right:50pt;', `
    <div style="display:flex;align-items:center;gap:6pt;justify-content:center;flex-wrap:wrap;">
      ${[
        ['用户提问',C.green],
        ['AgentService',C.blue],
        ['拼装学习画像\n上下文',C.purpleLt],
        ['DeepSeek\n/chat/completions',C.yellow],
        ['个性化回答\n/ AI简历',C.green],
      ].map(([txt,bg],i)=>`
        <div style="background:${bg};border:2pt solid ${C.ink};border-radius:6pt;padding:10pt 14pt;box-shadow:2pt 2pt 0 ${C.ink};text-align:center;">
          <p style="font-size:11pt;color:${C.ink};font-weight:600;white-space:pre-line;">${txt}</p>
        </div>
        ${i<4?`<p style="font-size:16pt;color:${C.ink};">→</p>`:''}
      `.trim()).join('')}
    </div>
  `)}
  ${cardDiv('195pt','50pt','860pt','140pt',C.cream,'border:2pt solid #CBD5E1;',`
    <h3 style="font-size:15pt;color:${C.ink};font-weight:700;margin-bottom:10pt;">上下文注入字段</h3>
    <div style="display:flex;gap:12pt;flex-wrap:wrap;">
      ${['信用余额','诚信分','课程进度','目标技能','心仪职位','学习偏好'].map(f=>`<div style="background:${C.white};border:2pt solid ${C.ink};border-radius:4pt;padding:5pt 12pt;"><p style="font-size:11pt;color:${C.ink};">${f}</p></div>`).join('')}
    </div>
    <p style="font-size:12pt;color:${C.ink};line-height:1.5;margin-top:12pt;"><b>关键技术：</b>RestClient 调用 DeepSeek API · 同一条链路复用于 AI 简历生成 · 上下文窗口管理</p>
  `)}
  ${cardDiv('350pt','50pt','860pt','95pt',C.green,'',`
    <h3 style="font-size:14pt;color:${C.ink};font-weight:700;">为什么不是简单套壳？</h3>
    <p style="font-size:12pt;color:${C.ink};line-height:1.55;">每个用户的回答都基于其真实的学习档案 —— 同样问"推荐什么课"，新手和进阶者得到的答案完全不同。"档案即上下文"是核心差异化。</p>
  `)}
</body></html>`;
}

// ── P26 TRTC 深讲 ──
function p26_trtcDeepDive() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:38pt;left:60pt;', `<h1 style="font-size:25pt;color:${C.ink};font-weight:900;">深讲② TRTC 线上视频面试</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>
  <p style="font-size:12pt;color:${C.gray600};margin-top:6pt;">真实音视频集成 —— 自建签名服务，不裸奔密钥</p>`)}
  <!-- 流程 -->
  ${absDiv('top:100pt;left:50pt;right:50pt;', `
    <div style="display:flex;align-items:center;gap:6pt;justify-content:center;">
      ${[
        ['机构约面',C.green],
        ['InterviewRtcService',C.blue],
        ['TlsSigApiV2\n签发 UserSig\n(HmacSHA256)',C.purpleLt],
        ['trtc-sdk-v5\nenterRoom',C.yellow],
        ['双向音视频\n真实通话',C.green],
      ].map(([txt,bg],i)=>`
        <div style="background:${bg};border:2pt solid ${C.ink};border-radius:6pt;padding:10pt 14pt;box-shadow:2pt 2pt 0 ${C.ink};text-align:center;">
          <p style="font-size:10pt;color:${C.ink};font-weight:600;white-space:pre-line;">${txt}</p>
        </div>
        ${i<4?`<p style="font-size:16pt;color:${C.ink};">→</p>`:''}
      `.trim()).join('')}
    </div>
  `)}
  ${cardDiv('195pt','50pt','860pt','135pt',C.cream,'border:2pt solid #CBD5E1;',`
    <h3 style="font-size:15pt;color:${C.ink};font-weight:700;margin-bottom:10pt;">安全设计要点</h3>
    <div style="display:flex;gap:14pt;flex-wrap:wrap;">
      ${['UserSig 含房间号 + 时间窗口','参与者身份校验','密钥不出后端服务','HmacSHA256 签名防伪造'].map(t=>`<div style="background:${C.white};border:2pt solid ${C.ink};border-radius:4pt;padding:5pt 12pt;"><p style="font-size:11pt;color:${C.ink};">🔒 ${t}</p></div>`).join('')}
    </div>
  `)}
  ${absDiv('bottom:38pt;left:60pt;width:340pt;height:75pt;border:2pt dashed ${C.gray400};border-radius:8pt;display:flex;align-items:center;justify-content:center;background:#FAFAF5;',`<p style="font-size:12pt;color:${C.gray600};">面试房间界面截图占位</p>`)}
  ${cardDiv('348pt','440pt','480pt','82pt',C.blue,'',`
    <h3 style="font-size:13pt;color:${C.ink};font-weight:700;margin-bottom:6pt;">现场可演示！</h3>
    <p style="font-size:11pt;color:${C.ink};line-height:1.5;">答辩时可实时演示浏览器内音视频通话 —— 不是 mock，是真跑通 TRTC。</p>
  `)}
</body></html>`;
}

// ── P27 组织管理 ──
function p27_organization() {
  const members = [
    { name: '邵卓凡', role: '队长 / 后端·Agent', color: C.green, duties: ['主界面·全局搜索','诚信评定(后端)','学分经济(后端)','Agent学伴','数据库管理'] },
    { name: '姜雅文', role: '组员1 / 前端·个人中心', color: C.blue, duties: ['企业中心','个人中心','私信·档案图表','面试邀请'] },
    { name: '邓博',   role: '组员2 / 前端·商城', color: C.purpleLt, duties: ['积分商城','学习资源','学习证书','支付与订单','资源标签'] },
    { name: '陈赣',   role: '组员3 / 前端·社区', color: C.yellow, duties: ['论坛(全功能)','资讯中心','招聘/活动/政策'] },
  ];
  const memberCards = members.map((m, i) =>
    cardDiv('105pt', `${50 + i * 230}pt`, '210pt', '280pt', m.color, '', `
      <h3 style="font-size:17pt;color:${C.ink};font-weight:800;">${m.name}</h3>
      <p style="font-size:11pt;color:${C.gray600};margin-bottom:10pt;">${m.role}</p>
      ${m.duties.slice(0, 4).map(d => `<p style="font-size:10pt;color:${C.ink};line-height:1.55;">· ${d}</p>`).join('')}
      ${m.duties.length > 4 ? `<p style="font-size:10pt;color:${C.gray400};">+${m.duties.length - 4} 项...</p>` : ''}
    `)
  ).join('\n');
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>${BASE_CSS}</style></head><body>
  ${absDiv('top:42pt;left:60pt;', `<h1 style="font-size:26pt;color:${C.ink};font-weight:900;">组织管理（4 人团队）</h1>
  <div style="width:50pt;height:4pt;background:${C.green};border-radius:2pt;margin-top:6pt;"></div>`)}
  ${memberCards}
  ${cardDiv('400pt','50pt','860pt','78pt',C.cream,'border:2pt solid #CBD5E1;',`
    <h3 style="font-size:14pt;color:${C.ink};font-weight:700;">协作方式</h3>
    <p style="font-size:12pt;color:${C.ink};">每周例会 · Git 分支策略 · 接口约定文档 · 前后端联调 · Code Review</p>
  `)}
</body></html>`;
}

// ── P28 小结 + 致谢 ──
function p28_closing() {
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><style>*{margin:0;padding:0;box-sizing:border-box;}body{width:960pt;height:540pt;font-family:"PingFang SC","Microsoft YaHei",system-ui,sans-serif;background:${C.darkBg};overflow:hidden;position:relative;}</style></head><body>
  ${absDiv('top:140pt;left:80pt;right:80pt;text-align:center;', `
    <h1 style="font-size:38px;color:${C.green};font-weight:900;line-height:1.4;">秩然有序，册录一生。</h1>
    <p style="font-size:18px;color:${C.gray400};margin-top:24px;line-height:1.6;">感谢评委老师的聆听与指导</p>
    <p style="font-size:14px;color:${C.gray600};margin-top:40px;">展望：哈希存证 → 联盟链 ｜ 更多机构入驻 ｜ Redis 缓存全面落地</p>
    <div style="margin-top:50px;display:inline-block;background:${C.green};border:2px solid #fff;border-radius:8px;padding:10px 36px;"><p style="font-size:20px;color:${C.darkBg};font-weight:800;">Q & A</p></div>
  `)}
  ${absDiv('bottom:50pt;left:80pt;right:80pt;text-align:center;', `<p style="font-size:12px;color:${C.gray600};">星秩存册 Singularis Ledger · 界面设计答辩 · 2026.07</p>`)}
</body></html>`;
}

// ═══════════════════════════════════════════
// 页面注册表（按顺序）
// ═══════════════════════════════════════════

const SLIDES = [
  { fn: p1_cover,            file: '01-cover.html' },
  { fn: p2_positioning,      file: '02-positioning.html' },
  { fn: p3_contents,         file: '03-contents.html' },
  { fn: () => chapterPage(1, '为什么是星秩存册', C.green), file: '04-chapter1.html' },
  { fn: p5_breakpoints,      file: '05-breakpoints.html' },
  { fn: p6_colorPsychology,  file: '06-color-psychology.html' },
  { fn: p7_valueFlywheel,    file: '07-value-flywheel.html' },
  { fn: () => chapterPage(2, '项目背景与需求', C.blue), file: '08-chapter2.html' },
  { fn: p9_background,       file: '09-background.html' },
  { fn: p10_userResearch,    file: '10-user-research.html' },
  { fn: p11_functionalReqs,  file: '11-functional-reqs.html' },
  { fn: p12_nonFunctionalReqs,file:'12-non-functional.html' },
  { fn: () => chapterPage(3, '产品介绍', C.purple), file: '13-chapter3.html' },
  { fn: p14_overview,        file: '14-overview.html' },
  { fn: p15_userGroups,      file: '15-user-groups.html' },
  { fn: p16_showcase,        file: '16-showcase.html' },
  { fn: p17_agent,           file: '17-agent.html' },
  { fn: p18_hashAttestation, file: '18-hash-attestation.html' },
  { fn: p19_pointsIntegrity, file: '19-points-integrity.html' },
  { fn: p20_recruitment,     file: '20-recruitment.html' },
  { fn: p21_learningExperience,file:'21-learning-exp.html' },
  { fn: () => chapterPage(4, '技术路线', C.blueDark), file: '22-chapter4.html' },
  { fn: p23_architecture,    file: '23-architecture.html' },
  { fn: p24_techHighlights,  file: '24-tech-highlights.html' },
  { fn: p25_agentDeepDive,   file: '25-agent-deep-dive.html' },
  { fn: p26_trtcDeepDive,    file: '26-trtc-deep-dive.html' },
  { fn: p27_organization,    file: '27-organization.html' },
  { fn: p28_closing,         file: '28-closing.html' },
];

// ═══════════════════════════════════════════
// 主函数
// ═══════════════════════════════════════════

async function main() {
  // 创建输出目录
  fs.mkdirSync(OUT_DIR, { recursive: true });

  console.log(`Generating ${SLIDES.length} slides to ${OUT_DIR} ...\n`);

  for (let i = 0; i < SLIDES.length; i++) {
    const s = SLIDES[i];
    const html = s.fn();
    const outPath = path.join(OUT_DIR, s.file);
    fs.writeFileSync(outPath, html, 'utf-8');
    console.log(`  [${String(i + 1).padStart(2, '0')}/${SLIDES.length}] ${s.file}  (${Math.round(html.length / 1024)} KB)`);
  }

  console.log(`\nDone! ${SLIDES.length} files written.`);

  // 输出下一步指令
  console.log(`\nNext step:`);
  console.log(`  cd D:\\creditbank-1\\.skills\\huashu-design-full`);
  console.log(`  node scripts/export_deck_pptx.mjs --slides "${OUT_DIR}" --out "D:\\creditbank-1\\deck-export\\星秩存册-界面设计答辩-editable.pptx"`);
}

main().catch(e => { console.error(e); process.exit(1); });
