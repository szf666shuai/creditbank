#!/usr/bin/env node
/**
 * gen-animated-deck.js — 基于真实 UI 截图的带动画效果 HTML 幻灯片生成器
 *
 * 输出：D:/creditbank-1/deck-export/deck-animated.html（单文件，浏览器打开即可播放）
 *
 * 视觉参考：6 张真实产品截图（学员首页/积分商城/企业中心/企业管理台/管理主页/管理台）
 * 动画类型：fadeInUp / slideInLeft / slideInRight / scaleIn / float / stagger / progressFill / pulse
 */

import fs from 'fs';
import { dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const OUT = __dirname + '/deck-animated.html';

// ═══════════════════════════════════════════
// 真实 UI 截图（从用户提供的 6 张截图）
// ═══════════════════════════════════════════
const IMG_DIR = __dirname + '/imgs/';
const IMG_FILES = [
  { id: 's01', name: '学员首页', file: 's01-homepage.png' },
  { id: 's02', name: '积分商城', file: 's02-mall.jpg' },
  { id: 's03', name: '企业中心', file: 's03-enterprise.png' },
  { id: 's04', name: '企业管理台', file: 's04-enterprise-admin.png' },
  { id: 's05', name: '系统管理主页', file: 's05-admin-home.png' },
  { id: 's06', name: '系统管理台', file: 's06-admin-console.png' },
];

function loadImages() {
  return IMG_FILES.map(img => {
    const buf = fs.readFileSync(IMG_DIR + img.file);
    const b64 = buf.toString('base64');
    const mime = img.file.endsWith('.jpg') ? 'image/jpeg' : 'image/png';
    return {
      id: img.id,
      name: img.name,
      src: `data:${mime};base64,${b64}`,
      w: buf.length > 0 ? null : 0, // will measure via JS
    };
  });
}

// ═══════════════════════════════════════════
// 色彩系统（从截图提取）
// ═══════════════════════════════════════════
const C = {
  cream:    '#FFF9F0',
  ink:      '#1A202C',
  green:    '#22C55E',
  blue:     '#BEE3F8',
  blueDark:'#3B82F6',
  purple:   '#7C3AED',
  purpleLt: '#DDD6FE',
  pink:     '#EF4444',
  pinkLt:   '#FECACA',
  yellow:   '#FEF08A',
  darkBg:   '#1E293B',
  white:    '#FFFFFF',
  gray600:  '#4A5568',
  gray400:  '#A0AEC0',
  // 从截图提取的补充色
  coral:    '#FB7185',
  teal:     '#2DD4BF',
  orange:   '#FB923C',
};

// ═══════════════════════════════════════════
// 28 页内容数据（来自答辩内容方案.md）
// ═══════════════════════════════════════════
const SLIDES = [
  { id: 1, role: 'cover', title: '星秩存册', subtitle: 'Singularis Ledger · 终身学习学分银行平台', badge: '界面设计答辩', meta: '答辩人：____ · 指导教师：____ · 日期：2026.07' },
  { id: 2, role: 'hook', title: '我们的平台，是学习的图书馆，也是"健身房"。', sub: '秩然有序，册录一生。' },
  { id: 3, role: 'agenda', items: ['为什么是星秩存册','项目背景与需求','产品介绍','技术路线','组织管理 & 小结'] },
  { id: 4, role: 'chapter', num: '01', label: 'ACT 01', title: '为什么是星秩存册' },
  { id: 5, role: 'breakpoints', cards: [{t:'脱节',d:'教授教的 ≠ 职场要的'},{t:'无氛围/无激励',d:'氛围与心理暗示无可替代'},{t:'难认证',d:'学分零散无法累积流通'}] },
  { id: 6, role: 'color', chips: [{n:'绿 #22C55E',d:'成长/生机，缓释学习焦虑'},{n:'蓝 #BEE3F8',d:'信任/专业，呼应学分银行'},{n:'奶油底 #FFF9F0',d:'温和亲和，跨越年龄门槛'}] },
  { id: 7, role: 'flywheel', roles:['学员','平台','企业/机构'] },
  { id: 8, role: 'chapter', num: '02', label: 'ACT 02', title: '项目背景与需求' },
  { id: 9, role: 'bg', points:[{t:'政策驱动',d:['终身学习写入国家战略','学分银行是国家级方向','数字化转型加速']},{t:'现实痛点',d:['各平台"学分孤岛"','认证能力弱','缺乏统一激励体系']},{t:'机会',d:['用学分银行+哈希存证+Agent把断点接起来']}] },
  { id: 10, role: 'research', left:{t:'学员侧',d:['有氛围能坚持','学完有凭证','能兑换权益']}, right:{t:'机构/企业侧',d:['可验证的能力证明','能定向招人','低成本触达']} },
  { id: 11, role: 'modules', mods:['积分商城','学习档案','学习成果','论坛','报名签到','招聘求职','区块链校验','诚信评定'] },
  { id: 12, role: 'nonfunc', quads:[{t:'性能',d:['Redis 缓存加速','响应 < 1s']},{t:'安全',d:['BCrypt 加密','SHA-256 防伪']},{t:'可用性',d:['三端适配','清晰提示']},{t:'可扩展',d:['模块化分包','预留接口']}] },
  { id: 13, role: 'chapter', num: '03', label: 'ACT 03', title: '产品介绍' },
  { id: 14, role: 'overview', desc: '三端一体：学员 / 机构 / 管理员 —— 围绕"学分银行"核心运转的 8 大模块' },
  { id: 15, role: 'users', roles:[{r:'学员',d:['学·攒分·看档案','兑权益·求职']},{r:'机构/教师',d:['发课·签到·评诚信','发凭证证书']},{r:'管理员',d:['审核·配置·看数据']}] },
  { id: 16, role: 'showcase', placeholder: true },
  { id: 17, role: 'agent', title:'Agent 智能学伴', desc:'LLM 驱动的学伴 —— 把"健身房私教"做成"学习私教"', tags:['对话答疑','学习路径推荐','陪练伴学'], detail:'档案即上下文 —— 注入信用余额/诚信分/课程进度/目标技能' },
  { id: 18, role: 'hash', title:'证书哈希存证', flow:['学习成果','SHA-256 哈希','凭证+二维码','扫码验真'], note:'诚实口径：SHA-256 防伪哈希（非上链），预留联盟链演进空间' },
  { id: 19, role: 'points', left:{t:'积分商城',d:['学习→攒积分','积分→兑换权益','兑换→刺激学习']}, right:{t:'诚信评定',d:['观看完成度','作答质量','提交原创度','诚信分门控权益']} },
  { id: 20, role: 'recruit', title:'全链路求职闭环', flow:['投递简历','机构筛选','TRTC在线面试','结果通知'], desc:"学员的可信学分直接对接企业需求 —— 吸引机构入驻的关键" },
  { id: 21, role: 'learning', features:[{i:'💬',t:'弹幕互动',d:'边看边发弹幕'},{i:'✅',t:'打卡签到',d:'养成习惯积攒积分'},{i:'🎓',t:'完课自动发证',d:'即时正反馈'}] },
  { id: 22, role: 'chapter', num: '04', label: 'ACT 04', title: '技术路线' },
  { id: 23, role: 'arch', title:'架构总览 + 技术选型', layers:['前端 SPA (Vue3+TS)','JWT 鉴权网关','业务分包 (Spring Boot)','数据层 MySQL+Redis'], ext:['DeepSeek LLM','腾讯云 TRTC'], techs:['Java 21','Spring Boot 3.4','MyBatis-Plus','Vue3+TypeScript','MySQL 8','Redis','Docker Compose'], note:'分层单体（非微服务），可平滑演进' },
  { id: 24, role: 'highlights', items:[{n:'01',t:'Agent 智能学伴',d:'真调 DeepSeek LLM',tag:'P25深讲'},{n:'02',t:'TRTC 线上面试',d:'腾讯云 TRTC 音视频',tag:'P26深讲'},{n:'03',t:'JWT 三端隔离',d:'自定义拦截器+角色校验'},{n:'04',t:'哈希存证+验真',d:'SHA-256+二维码'},{n:'05',t:'学分领域模型',d:'累积-兑换-门控建模'},{n:'06',t:'工程化基座',d:'MBP+BCrypt+Redis+Docker'}] },
  { id: 25, role: 'agentDeep', title:'Agent 智能学伴 LLM 调用链路', flow:['用户提问','AgentService','拼装学习画像上下文','DeepSeek API','个性化回答/AI简历'], ctxFields:['信用余额','诚信分','课程进度','目标技能','心仪职位','学习偏好'] },
  { id: 26, role: 'trtcDeep', title:'TRTC 线上视频面试', flow:['机构约面','InterviewRtcService','TlsSigApiV2 签发 UserSig','trtc-sdk-v5 enterRoom','双向音视频通话'], sec:['UserSig 含房间号+时间窗口','参与者身份校验','密钥不出后端','HmacSHA256 签名防伪造'] },
  { id: 27, role: 'org',
    members: [
      { name:'邵卓凡', role:'队长/后端·Agent', duts:['主界面·全局搜索','诚信评定(后端)','学分经济(后端)','Agent学伴','数据库管理'] },
      { name:'姜雅文', role:'组员1/前端·个人中心', duts:['企业中心','个人中心','面试邀请'] },
      { name:'邓博', role:'组员2/前端·商城', duts:['积分商城','学习资源','学习证书','支付与订单','资源标签'] },
      { name:'陈赣', role:'组员3/前端·社区', duts:['论坛(全功能)','资讯中心','招聘/活动/政策'] }
    ],
    collab:'每周例会 · Git 分支策略 · 接口约定文档 · 前后端联调 · Code Review' },
  { id: 28, role: 'closing', headline:'秩然有序，册录一生。', outlook:'展望：哈希存证 → 联盟链 ｜ 更多机构入驻 ｜ Redis 缓存全面落地', thanks:'感谢评委老师的聆听与指导', qa:true },
];

// ═══════════════════════════════════════════
// HTML 生成器
// ═══════════════════════════════════════════
function buildDeck(images) {
  const slidesHtml = SLIDES.map((s, i) => buildSlide(s, i, images)).join('\n');

  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>星秩存册 · 界面设计答辩（动画版）</title>
<style>
/* ═══ Reset & Base ═══ */
*, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }

@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700;900&display=swap');

html, body {
  width: 100vw; height: 100vh;
  overflow: hidden;
  background: #1a1a2e;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* ═══ Slide Container ═══ */
.deck {
  width: 100vw; height: 100vh;
  position: relative;
  overflow: hidden;
}

.slide {
  position: absolute; top: 0; left: 0;
  width: 100vw; height: 100vh;
  display: none;
  align-items: center; justify-content: center;
  padding: 60px 80px;
  overflow: hidden;
}
.slide.active { display: flex; }
.slide[data-role="cover"], .slide[data-role="closing"] { justify-content: flex-start; align-items: stretch; padding-top: 10vh; flex-direction: column; }

/* ═══ Background Variants ═══ */
.bg-cream { background: ${C.cream}; }
.bg-dark  { background: ${C.darkBg}; }
.bg-gradient-green {
  background: linear-gradient(135deg, ${C.cream} 0%, #E8FDE7 50%, ${C.blue} 100%);
}
.bg-gradient-warm {
  background: linear-gradient(135deg, ${C.cream} 0%, #FEF9E7 50%, ${C.yellow} 200%);
}

/* ═══ Typography ═══ */
.t-hero { font-size: clamp(36px, 5vw, 72px); font-weight: 900; color: ${C.ink}; line-height: 1.15; letter-spacing: -0.02em; }
.t-title { font-size: clamp(28px, 3.5vw, 52px); font-weight: 800; color: ${C.ink}; line-height: 1.25; }
.t-sub { font-size: clamp(16px, 1.8vw, 24px); font-weight: 600; color: ${C.gray600}; line-height: 1.5; }
.t-body { font-size: clamp(14px, 1.5vw, 20px); color: ${C.ink}; line-height: 1.65; }
.t-small { font-size: clamp(12px, 1.2vw, 16px); color: ${C.gray600}; line-height: 1.5; }
.t-mono { font-family: 'Courier New', monospace; letter-spacing: 0.05em; }

/* Dark bg text */
.bg-dark .t-hero, .bg-dark .t-title, .bg-dark .t-body { color: ${C.white}; }
.bg-dark .t-sub, .bg-dark .t-small { color: ${C.gray400}; }

/* ═══ Cards (Neo-brutalist from screenshots) ═══ */
.card {
  background: ${C.white};
  border: 3px solid ${C.ink};
  border-radius: 16px;
  padding: clamp(16px, 2vw, 32px);
  position: relative;
}
.card-green  { background: ${C.green}; }
.card-blue   { background: ${C.blue}; }
.card-purple { background: ${C.purpleLt}; }
.card-yellow { background: ${C.yellow}; }
.card-pink   { background: ${C.pinkLt}; }
.card-cream  { background: ${C.cream}; }

/* Hard shadow (neo-brutalism) */
.shadow-hard { box-shadow: 6px 6px 0 ${C.ink}; }
.shadow-soft { box-shadow: 4px 4px 0 rgba(26,32,44,0.15); }

/* ═══ Buttons & Badges (from screenshots) ═══ */
.btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 12px 28px;
  border: 3px solid ${C.ink};
  border-radius: 999px;
  font-weight: 700; font-size: clamp(14px, 1.5vw, 20px);
  cursor: pointer;
  transition: transform 0.15s;
}
.btn:hover { transform: translate(-2px, -2px); box-shadow: 6px 6px 0 ${C.ink}; }
.btn:active { transform: translate(0, 0); box-shadow: none; }
.btn-green { background: ${C.green}; color: ${C.ink}; }
.btn-blue  { background: ${C.blue}; color: ${C.ink}; }
.btn-white { background: ${C.white}; color: ${C.ink}; }

.badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 16px; border-radius: 999px;
  font-weight: 700; font-size: clamp(11px, 1.2vw, 15px);
  border: 2px solid ${C.ink};
}
.badge-green  { background: ${C.green}; }
.badge-blue   { background: ${C.blue}; }
.badge-pink   { background: ${C.pinkLt}; }
.badge-purple { background: ${C.purpleLt}; }
.badge-yellow { background: ${C.yellow}; }

/* Number badge (from screenshot - circled numbers) */
.num-badge {
  width: 36px; height: 36px; min-width: 36px;
  border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center;
  border: 2.5px solid ${C.ink};
  font-weight: 900; font-size: 16px;
}

/* ═══ KPI Card (from screenshot data cards) ═══ */
.kpi-card {
  background: ${C.white};
  border: 3px solid ${C.ink};
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  min-width: 160px;
}
.kpi-num { font-size: clamp(36px, 4vw, 64px); font-weight: 900; color: ${C.green}; }
.kpi-label { font-size: clamp(13px, 1.3vw, 17px); color: ${C.gray600}; margin-top: 4px; }

/* ═══ Progress Bar (from screenshot) ═══ */
.progress-track {
  width: 100%; height: 12px;
  background: #E5E7EB;
  border-radius: 999px;
  border: 2px solid ${C.ink};
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: ${C.green};
  border-radius: 999px;
  animation: fillProgress 1.5s ease-out forwards;
  animation-delay: var(--delay, 0.6s);
  width: 0;
}
@keyframes fillProgress { to { width: var(--pct, 68%); } }

/* ═══ Decorative Elements (extracted from screenshots) ═══ */

/* Star shape (clip-path polygon) */
.star {
  clip-path: polygon(50% 0%, 61% 35%, 98% 35%, 68% 57%, 79% 91%, 50% 70%, 21% 91%, 32% 57%, 2% 35%, 39% 35%);
}

/* Floating icon buttons (like 📘 ⭐ 🏆 from screenshot) */
.float-icon {
  width: 56px; height: 56px; min-width: 56px;
  border-radius: 16px;
  border: 2.5px solid ${C.ink};
  display: flex; align-items: center; justify-content: center;
  font-size: 26px;
  box-shadow: 3px 3px 0 ${C.ink};
}

/* Corner blob decoration (blue/purple shapes from screenshots 3,5,6) */
.corner-blob {
  position: absolute;
  border-radius: 40% 60% 70% 30% / 50% 30% 70% 50%;
  opacity: 0.6;
}

/* AI widget smiley (from screenshots 4,6) */
.ai-widget {
  width: 52px; height: 52px; min-width: 52px;
  border-radius: 50%;
  background: ${C.green};
  border: 3px solid ${C.ink};
  display: flex; align-items: center; justify-content: center;
  font-size: 28px;
  box-shadow: 3px 3px 0 ${C.ink};
  position: fixed; bottom: 32px; right: 32px;
  z-index: 100;
  animation: floatBob 3s ease-in-out infinite;
}

/* Nav bar style (from top of screenshots) */
.nav-bar {
  position: absolute; top: 0; left: 0; right: 0;
  height: 64px;
  background: ${C.white};
  border-bottom: 3px solid ${C.ink};
  display: flex; align-items: center; padding: 0 32px;
  gap: 24px;
  z-index: 10;
}
.nav-logo {
  display: flex; align-items: center; gap: 10px;
  font-weight: 800; font-size: 18px;
}
.nav-logo-icon {
  width: 36px; height: 36px;
  background: ${C.pink};
  border: 2px solid ${C.ink};
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
}
.nav-item {
  padding: 6px 16px;
  border-radius: 999px;
  font-weight: 600; font-size: 14px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
}
.nav-item:hover, .nav-item.active {
  border-color: ${C.ink};
  background: ${C.green};
}

/* ════════════════════════════════
 * ANIMATIONS
 * ════════════════════════════════ */

/* Base: hidden initially, revealed when slide active */
.anima { opacity: 0; }
.slide.active .anima { animation-fill-mode: forwards; }

/* fadeInUp */
.au-up { animation-name: auUp; animation-duration: 0.7s; animation-timing-function: cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes auUp {
  from { opacity: 0; transform: translateY(30px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* slideInLeft */
.sl-left { animation-name: slLeft; animation-duration: 0.65s; animation-timing-function: cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes slLeft {
  from { opacity: 0; transform: translateX(-50px); }
  to   { opacity: 1; transform: translateX(0); }
}

/* slideInRight */
.sl-right { animation-name: slRight; animation-duration: 0.65s; animation-timing-function: cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes slRight {
  from { opacity: 0; transform: translateX(50px); }
  to   { opacity: 1; transform: translateX(0); }
}

/* scaleIn */
.sc-in { animation-name: scIn; animation-duration: 0.5s; animation-timing-function: cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes scIn {
  from { opacity: 0; transform: scale(0.6); }
  to   { opacity: 1; transform: scale(1); }
}

/* float (continuous bobbing) */
.fl-bob { animation: flBob 3s ease-in-out infinite; }
@keyframes flBob {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-10px); }
}

/* pulse glow */
.pl-glow { animation: plGlow 2s ease-in-out infinite; }
@keyframes plGlow {
  0%, 100% { box-shadow: 4px 4px 0 ${C.ink}; }
  50%      { box-shadow: 6px 6px 0 ${C.ink}, 0 0 20px rgba(34,197,94,0.3); }
}

/* typewriter for key statements */
.typewriter { overflow: hidden; white-space: nowrap; animation: typing 1.5s steps(30, end), blinkCaret 0.75s step-end infinite; }
@keyframes typing { from { width: 0; } to { width: 100%; } }
@keyframes blinkCaret { 50% { border-color: transparent; } }

/* stagger delay helpers */
.d1 { animation-delay: 0.1s !important; }
.d2 { animation-delay: 0.2s !important; }
.d3 { animation-delay: 0.3s !important; }
.d4 { animation-delay: 0.4s !important; }
.d5 { animation-delay: 0.5s !important; }
.d6 { animation-delay: 0.6s !important; }
.d7 { animation-delay: 0.7s !important; }
.d8 { animation-delay: 0.8s !important; }

/* ═══ Page number & navigation ═══ */
.page-num {
  position: absolute; bottom: 24px; right: 40px;
  font-size: 14px; color: ${C.gray400};
  font-weight: 600;
}
.bg-dark .page-num { color: ${C.gray600}; }

.nav-hint {
  position: absolute; bottom: 24px; left: 40px;
  font-size: 12px; color: ${C.gray400};
}
.bg-dark .nav-hint { color: ${C.gray600}; }

/* Click zones for navigation */
.click-zone {
  position: absolute; top: 0; bottom: 0; width: 50%; cursor: pointer; z-index: 5;
}
.click-zone.left  { left: 0; }
.click-zone.right { right: 0; }

/* ═══ Progress dots ═══ */
.progress-dots {
  position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 8px; z-index: 50;
}
.dot {
  width: 10px; height: 10px; border-radius: 50%;
  background: rgba(255,255,255,0.3);
  border: 1.5px solid rgba(255,255,255,0.5);
  cursor: pointer; transition: all 0.2s;
}
.dot.active { background: ${C.green}; border-color: ${C.ink}; transform: scale(1.3); }
</style>
</head>
<body>

<div class="deck" id="deck">${slidesHtml}</div>
<div class="progress-dots" id="dots"></div>
<div class="ai-widget">😊</div>

<script>
(() => {
  const slides = document.querySelectorAll('.slide');
  const total = slides.length;
  let current = 0;

  function show(n) {
    if (n < 0) n = total - 1;
    if (n >= total) n = 0;
    slides.forEach(s => s.classList.remove('active'));
    slides[n].classList.add('active');
    current = n;
    updateDots();
    // Re-trigger animations by cloning and replacing
    slides[n].querySelectorAll('.anima').forEach(el => {
      el.style.animation = 'none';
      void el.offsetWidth; // reflow
      el.style.animation = '';
    });
  }

  function updateDots() {
    const container = document.getElementById('dots');
    container.innerHTML = '';
    for (let i = 0; i < total; i++) {
      const d = document.createElement('div');
      d.className = 'dot' + (i === current ? ' active' : '');
      d.onclick = () => show(i);
      container.appendChild(d);
    }
  }

  // Keyboard nav
  document.addEventListener('keydown', e => {
    if (e.key === 'ArrowRight' || e.key === ' ' || e.key === 'Enter') show(current + 1);
    else if (e.key === 'ArrowLeft') show(current - 1);
    else if (e.key === 'Home') show(0);
    else if (e.key === 'End') show(total - 1);
  });

  // Click zone nav
  document.addEventListener('click', e => {
    if (e.target.classList.contains('click-zone')) {
      if (e.target.classList.contains('left')) show(current - 1);
      else show(current + 1);
    }
  });

  // Touch swipe
  let tx = 0;
  document.addEventListener('touchstart', e => { tx = e.touches[0].clientX; }, {passive: true});
  document.addEventListener('touchend', e => {
    const diff = e.changedTouches[0].clientX - tx;
    if (Math.abs(diff) > 50) diff > 0 ? show(current - 1) : show(current + 1);
  }, {passive: true});

  // Init
  show(0);
})();
</script>
</body>
</html>`;
}

// ═══════════════════════════════════════════
// Per-slide builders
// ═══════════════════════════════════════════

function buildSlide(s, idx, images) {
  const builders = {
    cover: buildCover,
    hook: buildHook,
    agenda: buildAgenda,
    chapter: buildChapter,
    breakpoints: buildBreakpoints,
    color: buildColor,
    flywheel: buildFlywheel,
    bg: buildBg,
    research: buildResearch,
    modules: buildModules,
    nonfunc: buildNonFunc,
    overview: buildOverview,
    users: buildUsers,
    showcase: buildShowcase,
    agent: buildAgent,
    hash: buildHash,
    points: buildPoints,
    recruit: buildRecruit,
    learning: buildLearning,
    arch: buildArch,
    highlights: buildHighlights,
    agentDeep: buildAgentDeep,
    trtcDeep: buildTrtcDeep,
    org: buildOrg,
    closing: buildClosing,
  };
  const fn = builders[s.role] || (() => `<div class="slide bg-cream"><h1 class="t-title anima au-up">Slide ${s.id}</h1></div>`);
  return fn(s, idx, images);
}

function esc(s) { return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }

function attr(o) { return Object.entries(o).map(([k,v])=>`${k}="${v}`).join(' '); }

// ── P1 Cover ──
function buildCover(s) {
  return `<div class="slide bg-gradient-green" data-role="cover" data-id="${s.id}">
  <!-- Top nav bar mock -->
  <div class="nav-bar anima sl-left">
    <div class="nav-logo">
      <div class="nav-logo-icon">★</div>
      <span>星秩存册</span>
    </div>
    <div class="nav-item active">🏠 首页</div>
    <div class="nav-item">秩点商城 ▾</div>
    <div class="nav-item">学习资源 ▾</div>
    <div class="nav-item">论坛 ▾</div>
    <div class="nav-item">资讯中心 ▾</div>
    <div class="nav-item">企业中心 ▾</div>
  </div>

  <!-- Left star -->
  <div class="star anima sc-in d1" style="position:absolute;top:12vh;left:8vw;width:72px;height:72px;background:${C.green};border:none;"></div>

  <!-- Main content -->
  <div style="margin-left:6vw;margin-top:2vh;">
    <div class="badge badge-green anima sl-left d2" style="margin-bottom:16px;">● 终身学习 · 秩点银行</div>
    <h1 class="t-hero anima au-up d2" style="max-width:700px;">${esc(s.title)}</h1>
    <p class="t-sub anima au-up d3" style="margin-top:12px;color:${C.gray600};">${esc(s.subtitle)}</p>
    <p class="t-body anima au-up d4" style="margin-top:20px;max-width:560px;font-size:clamp(18px,2vw,28px);font-weight:700;line-height:1.5;">随时学、随时记<br/><span style="color:${C.green};">成长有序入册</span></p>
    <p class="t-small anima au-up d5" style="margin-top:16px;max-width:520px;color:${C.gray600};line-height:1.6;">
      汇聚机构加盟资源，运用积分管理手段，提升学员终身学习积极性，
      以档案与册籍承载每一份成长。
    </p>
    <div style="display:flex;gap:16px;margin-top:32px;">
      <button class="btn btn-green anima sc-in d5 fl-bob shadow-hard">继续学习 →</button>
      <button class="btn btn-blue anima sc-in d6 shadow-hard">我的学习档案</button>
    </div>
    <div style="display:flex;gap:32px;margin-top:48px;opacity:0.7;">
      <div class="anima au-up d7"><p class="t-small" style="font-weight:600;">学习资源</p><p style="font-size:12px;color:${C.gray400};">课程随时开讲</p></div>
      <div class="anima au-up d7" style="animation-delay:0.8s;"><p class="t-small" style="font-weight:600;">秩点激励</p><p style="font-size:12px;color:${C.gray400};">学有所得可兑</p></div>
      <div class="anima au-up d7" style="animation-delay:0.9s;"><p class="t-small" style="font-weight:600;">机构加盟</p><p style="font-size:12px;color:${C.gray400};">成果可核验</p></div>
    </div>
  </div>

  <!-- Right card: Learning profile (from screenshot!) -->
  <div class="card anima sl-right d3 shadow-hard" style="position:absolute;top:18vh;right:8vw;width:320px;padding:24px;">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <p class="t-small" style="font-weight:700;">学习档案</p>
      <span class="badge badge-yellow" style="font-size:11px;padding:4px 12px;">进行中</span>
    </div>
    <p class="t-body" style="font-size:clamp(16px,1.8vw,22px);font-weight:700;margin-bottom:8px;">秩然有序，册录一生</p>
    <p class="t-small" style="margin-bottom:16px;">从一门课开始，把进度、打卡与秩点写进册里。</p>
    <div style="margin-bottom:12px;">
      <div style="display:flex;justify-content:space-between;margin-bottom:6px;">
        <span style="font-size:12px;font-weight:600;">本周学习节奏</span>
        <span style="font-size:12px;font-weight:700;color:${C.green};">68%</span>
      </div>
      <div class="progress-track"><div class="progress-fill" style="--pct:68%;--delay:1.2s;"></div></div>
    </div>
    <button class="btn btn-green" style="width:100%;justify-content:center;">继续学习 →</button>
  </div>

  <!-- Floating icons (from screenshot) -->
  <div class="float-icon anima sc-in d5 fl-bob" style="position:absolute;top:42vh;left:calc(8vw + 480px);background:#DDD6FE;">⭐</div>
  <div class="float-icon anima sc-in d6 fl-bob" style="position:absolute;top:58vh;left:calc(8vw + 520px);background:${C.blue};animation-delay:-1s;">📘</div>
  <div class="float-icon anima sc-in d7 fl-bob" style="position:absolute;top:38vh;right:4vw;background:${C.green};animation-delay:-2s;">🏆</div>

  <!-- Bottom meta -->
  <div style="position:absolute;bottom:5vh;left:8vw;" class="anima au-up d8">
    <p class="t-small" style="font-weight:600;">${esc(s.badge)}</p>
    <p style="font-size:13px;color:${C.gray400};margin-top:6px;">${esc(s.meta)}</p>
  </div>

  <!-- Right big star -->
  <div class="star anima sc-in d6" style="position:absolute;top:20vh;right:6vw;width:140px;height:140px;background:${C.blue};border:none;opacity:0.8;"></div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P2 Hook ──
function buildHook(s) {
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:48px;left:80px;"><span class="badge badge-green" style="font-size:13px;">一句话定位 · POSITIONING</span></div>
  <div class="anima au-up d1" style="position:absolute;top:90px;left:80px;width:6pt;height:28px;background:${C.green};border-radius:3px;"></div>

  <h1 class="t-hero anima au-up d2" style="position:absolute;top:130px;left:80px;right:120px;max-width:900px;line-height:1.3;">
    我们的平台，是学习的图书馆，也是<span style="background:${C.purpleLt};padding:0 12px;border-radius:6px;display:inline-block;border:2pt solid ${C.ink};box-shadow:3px 3px 0 ${C.ink};">"健身房"</span>。
  </h1>

  <p class="t-sub anima au-up d4" style="position:absolute;top:260px;left:80px;font-size:clamp(20px,2.2vw,32px);font-weight:700;color:${C.ink};">
    秩然有序，册录一生。
  </p>

  <!-- Pink star decoration -->
  <div class="star anima sc-in d4 fl-bob" style="position:absolute;top:90px;right:100px;width:72px;height:72px;background:${C.pinkLt};border:none;"></div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P3 Agenda ──
function buildAgenda(s) {
  const colors = [C.green, C.blue, C.pinkLt, C.purpleLt, C.yellow];
  const nums = ['01','02','03','04','05'];
  const items = s.items.map((text, i) => `
    <div class="anima sl-left" style="display:flex;align-items:center;gap:18px;margin-bottom:20px;animation-delay:${i*0.1}s;">
      <div class="num-badge" style="background:${colors[i]};width:40px;height:40px;font-size:17px;">${nums[i]}</div>
      <p class="t-body" style="font-weight:700;font-size:clamp(18px,2vw,26px);">${esc(text)}</p>
    </div>`).join('');

  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:50px;left:80px;">
    <h1 class="t-title" style="font-size:clamp(32px,3.5vw,52px);">目录 / CONTENTS</h1>
    <div style="width:70px;height:5pt;background:${C.green};border-radius:3px;margin-top:10px;"></div>
  </div>

  <div style="position:absolute;top:130px;left:80px;">${items}</div>

  <div class="star anima sc-in d5 fl-bob" style="position:absolute;bottom:60px;right:80px;width:96px;height:96px;background:${C.green};border:none;"></div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── Chapter page template (P4/P8/P13/P22) ──
function buildChapter(s) {
  const accentColors = { '01': C.green, '02': C.blue, '03': C.purple, '04': C.blueDark };
  const ac = accentColors[s.num] || C.green;
  return `<div class="slide bg-dark" data-id="${s.id}">
  <p class="anima sl-left t-small" style="position:absolute;top:80px;left:100px;font-size:clamp(16px,1.8vw,22px);letter-spacing:4px;color:${C.blue};font-weight:700;">${esc(s.label)}</p>
  <h1 class="anima sc-in" style="position:absolute;top:150px;left:100px;font-size:clamp(100px,10vw,180px);color:${ac};font-weight:900;line-height:1;letter-spacing:-0.02em;animation-delay:0.2s;">${esc(s.num)}</h1>
  <h2 class="anima au-up" style="position:absolute;top:300px;left:100px;font-size:clamp(32px,3.5vw,48px);color:${C.white};font-weight:800;animation-delay:0.4s;">${esc(s.title)}</h2>
  <div class="star anima sc-in fl-bob" style="position:absolute;bottom:60px;right:100px;width:96px;height:96px;background:${ac};border:none;opacity:0.85;animation-delay:0.6s;"></div>
  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P5 Three Breakpoints ──
function buildBreakpoints(s) {
  const bgs = [C.blue, C.yellow, C.pinkLt];
  return `<div class="slide bg-cream" data-id="${s.id}">
  <h1 class="t-title anima au-up" style="position:absolute;top:45px;left:80px;">真问题：终身学习的三个断点</h1>

  ${s.cards.map((c, i) => `
    <div class="card anima sl-${i===0?'left':'right'} shadow-hard"
         style="position:absolute;top:120px;left:${60+i*310}px;width:280px;height:240px;background:${bgs[i]};animation-delay:${i*0.15}s;">
      <h3 class="t-body" style="font-size:clamp(18px,2vw,26px);font-weight:900;margin-bottom:14px;">${esc(c.t)}</h3>
      <p class="t-small" style="line-height:1.65;font-size:clamp(13px,1.4vw,17px);">${esc(c.d)}</p>
    </div>`).join('')}

  <div class="anima sc-in d5" style="position:absolute;top:390px;left:80px;">
    <span class="badge" style="background:${C.pink};color:${C.white};padding:10px 22px;font-size:clamp(13px,1.4vw,17px);">第三个"断"最痛 —— 学了 = 白学</span>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P6 Color Psychology ──
function buildColor(s) {
  const chipData = [
    { bg: C.green, border: '', circle: C.green },
    { bg: C.blue, border: '', circle: C.blue },
    { bg: C.cream, border: `2pt solid ${C.ink}`, circle: C.ink },
  ];
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima au-up" style="position:absolute;top:42px;left:80px;right:80px;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,42px);">为什么是这套界面？</h1>
    <p class="t-small" style="margin-top:10px;line-height:1.6;">色彩先于文字影响人的潜意识——麦当劳、肯德基用红色留客；界面同理，配色悄悄决定用户愿不愿意留下。</p>
  </div>

  ${s.chips.map((c, i) => `
    <div class="card anima sl-${i%2===0?'left':'right'} shadow-hard"
         style="position:absolute;top:130pt;left:${60+i*305}px;width:280px;height:240pt;background:${c.bg};${c.border?`border:${c.border};`:''}animation-delay:${i*0.15}s;">
      <div style="width:28pt;height:28pt;border-radius:50%;background:${c.circle};${c.circle===C.ink?`border:2pt solid ${C.ink};`:''}margin-bottom:12pt;"></div>
      <h3 class="t-body" style="font-size:clamp(16px,1.8vw,22px);font-weight:800;margin-bottom:10pt;">${esc(c.n)}</h3>
      <p class="t-small" style="line-height:1.6;">${esc(c.d)}</p>
    </div>`).join('')}

  <div class="anima au-up d6" style="position:absolute;bottom:40pt;left:80pt;">
    <p class="t-body" style="font-weight:700;font-size:clamp(15px,1.6vw,20px);">界面即品牌 —— 好的配色，让用户第一眼就愿意留下。</p>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P7 Value Flywheel ──
function buildFlywheel(s) {
  return `<div class="slide bg-gradient-warm" data-id="${s.id}">
  <h1 class="t-title anima au-up" style="position:absolute;top:42pt;left:80pt;">产品定位与价值飞轮</h1>

  <!-- Center platform card -->
  <div class="card card-blue anima sc-in shadow-hard" style="position:absolute;top:170pt;left:420pt;width:220pt;height:150pt;display:flex;align-items:center;justify-content:center;animation-delay:0.3s;">
    <div style="text-align:center;"><h3 style="font-size:clamp(16px,1.8vw,22px);font-weight:800;">平台</h3><p style="font-size:clamp(11px,1.2vw,15px);margin-top:6pt;">健身房+图书馆<br/>可信学分银行</p></div>
  </div>

  <!-- 3 role cards around it -->
  <div class="card card-green anima sl-left shadow-hard" style="position:absolute;top:130pt;left:80pt;width:200pt;height:120pt;animation-delay:0.2s;">
    <h3 style="font-size:clamp(15px,1.7vw,20px);font-weight:800;">学员</h3><p style="font-size:clamp(11px,1.2vw,14px);margin-top:6pt;">被丰富吸引<br/>攒分 / 兑换权益</p>
  </div>
  <div class="card card-purple anima sl-right shadow-hard" style="position:absolute;top:130pt;right:80pt;width:200pt;height:120pt;animation-delay:0.4s;">
    <h3 style="font-size:clamp(15px,1.7vw,20px);font-weight:800;">企业 / 机构</h3><p style="font-size:clamp(11px,1.2vw,14px);margin-top:6pt;">招到高质量人才<br/>入驻平台</p>
  </div>

  <!-- Arrows -->
  <p class="anima sc-in" style="position:absolute;top:175pt;left:295pt;font-size:20px;font-weight:700;color:${C.green};animation-delay:0.5s;">→</p>
  <p class="anima sc-in" style="position:absolute;top:175pt;right:295pt;font-size:20px;font-weight:700;color:${C.purple};animation-delay:0.5s;">←</p>
  <p class="anima sc-in" style="position:absolute;top:210pt;left:185pt;font-size:16px;font-weight:700;color:${C.green};animation-delay:0.6s;">↰</p>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P9 Background ──
function buildBg(s) {
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:42pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">1.1 项目背景</h1>
      <div style="width:56pt;height:5pt;background:${C.green};border-radius:3px;margin-top:8pt;"></div>
  </div>

  <div style="position:absolute;top:105pt;left:80pt;width:520pt;">
    ${s.points.map((p, i) => `
      <div class="card anima sl-left shadow-hard" style="background:${[C.green,C.blue,C.yellow][i]};margin-bottom:16pt;padding:18pt 22pt;animation-delay:${i*0.15}s;">
        <h3 style="font-size:clamp(15px,1.7vw,20px);font-weight:800;margin-bottom:10pt;">${esc(p.t)}</h3>
        ${p.d.map(d => `<p style="font-size:clamp(12px,1.3vw,16px);line-height:1.6;margin:3pt 0;">· ${esc(d)}</p>`).join('')}
      </div>`).join('')}
  </div>

  <!-- Right trend visual -->
  <div class="card anima sl-right" style="position:absolute;top:105pt;right:80pt;width:300pt;height:340pt;background:#F0FDF4;border:3pt solid ${C.ink};border-radius:16pt;padding:24pt;text-align:center;animation-delay:0.3s;">
    <h3 style="font-size:clamp(15px,1.7vw,20px);margin-bottom:16pt;">趋势示意</h3>
    <p style="font-size:clamp(12px,1.3vw,16px);color:${C.gray600};line-height:1.8;">政策推动 → 孤岛痛点 ↓<br/>学分银行兴起 ↓<br/><b style="color:${C.green};">★ 星秩存册</b></p>
    <!-- Mini chart bars -->
    <div style="margin-top:20pt;display:flex;flex-direction:column;gap:8pt;align-items:center;">
      ${[85,60,95,70].map((v,i)=>`<div style="display:flex;align-items:center;gap:8pt;width:100%;"><span style="font-size:10px;width:60pt;text-align:right;color:${C.gray600};">${['政策','孤岛','方向','我们'][i]}</span><div style="height:14pt;background:${[C.gray200,C.gray200,C.green,C.green][i]};border:2pt solid ${C.ink};border-radius:7pt;width:${v}%;"></div></div>`).join('')}
    </div>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P10 User Research ──
function buildResearch(s) {
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:42pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">1.2 用户调研</h1>
      <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
  </div>

  <div class="card card-green anima sl-left shadow-hard" style="position:absolute;top:110pt;left:80pt;width:420pt;height:240pt;animation-delay:0.2s;">
    <h3 style="font-size:clamp(18px,2vw,26px);font-weight:800;margin-bottom:16pt;">${esc(s.left.t)}侧</h3>
    ${s.left.d.map(d => `<p style="font-size:clamp(14px,1.5vw,19px);line-height:1.75;margin:6pt 0;">✓ ${esc(d)}</p>`).join('')}
  </div>

  <div class="card card-blue anima sl-right shadow-hard" style="position:absolute;top:110pt;right:80pt;width:420pt;height:240pt;animation-delay:0.35s;">
    <h3 style="font-size:clamp(18px,2vw,26px);font-weight:800;margin-bottom:16pt;">${esc(s.right.t)}侧</h3>
    ${s.right.d.map(d => `<p style="font-size:clamp(14px,1.5vw,19px);line-height:1.75;margin:6pt 0;">✓ ${esc(d)}</p>`).join('')}
  </div>

  <div class="anima au-up d6" style="position:absolute;bottom:45pt;left:80pt;right:80pt;">
    <p style="font-size:clamp(14px,1.5vw,19px);font-style:italic;color:${C.ink};font-weight:600;">
      核心诉求：可信、有激励、能流通 —— 这正是星秩存册要解决的。
    </p>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P11 Functional Requirements Matrix ──
function buildModules(s) {
  const colors = [C.green, C.blue, C.purpleLt, C.yellow];
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:42pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(24px,2.6vw,38px);">1.3 功能性需求 — 八大模块矩阵</h1>
      <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
  </div>

  ${s.mods.map((m, i) => {
    const row = Math.floor(i / 4);
    const col = i % 4;
    const top = 115 + row * 185;
    const left = 60 + col * 225;
    return `<div class="card anima sc-in shadow-hard" style="position:absolute;top:${top}pt;left:${left}pt;width:205pt;height:165pt;background:${colors[col]};animation-delay:${i*0.08}s;">
      <h3 style="font-size:clamp(14px,1.6vw,20px);font-weight:800;margin-bottom:8pt;">${esc(m)}</h3>
      <p style="font-size:clamp(11px,1.2vw,15px);line-height:1.5;">完整功能覆盖</p>
    </div>`;
  }).join('')}

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P12 Non-functional Requirements ──
function buildNonFunc(s) {
  const colors = [C.green, C.blue, C.purpleLt, C.yellow];
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:42pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">1.4 非功能性需求</h1>
      <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
  </div>

  ${s.quads.map((q, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    return `<div class="card anima sl-${col===0?'left':'right'} shadow-hard" style="position:absolute;top:${115+row*185}pt;left:${60+col*460}pt;width:430pt;height:175pt;background:${colors[i]};animation-delay:${i*0.12}s;">
      <h3 style="font-size:clamp(17px,1.9vw,24px);font-weight:800;margin-bottom:10pt;">${esc(q.t)}</h3>
      ${q.d.map(d => `<p style="font-size:clamp(13px,1.4vw,17px);line-height:1.6;">· ${esc(d)}</p>`).join('')}
    </div>`;
  }).join('')}

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P14 Overview ──
function buildOverview(s) {
  return `<div class="slide bg-gradient-green" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:42pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">2.1 项目概况 + 功能全景</h1>
      <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
  </div>

  <p class="anima au-up d2" style="position:absolute;top:100pt;left:80pt;font-size:clamp(15px,1.6vw,21px);font-weight:600;">
    三端一体：学员 / 机构 / 管理员 —— 围绕"学分银行"核心运转的 8 大模块
  </p>

  <!-- Center hub (like screenshot's central concept) -->
  <div class="card anima sc-in shadow-hard pl-glow" style="position:absolute;top:155pt;left:380pt;width:220pt;height:76pt;background:${C.green};border-width:3pt;display:flex;align-items:center;justify-content:center;animation-delay:0.3s;">
    <h3 style="font-size:clamp(17px,1.9vw,24px);font-weight:900;">学分银行 核心</h3>
  </div>

  <!-- 8 modules around hub -->
  ${['积分商城','学习档案','学习成果','论坛','报名签到','招聘求职','诚信评定','区块链'].map((m, i) => {
    const positions = [[125,145],[125,310],[255,370],[435,370],[595,310],[595,145],[435,80],[255,80]];
    const clrs = [C.blue,C.purpleLt,C.yellow,C.green,C.blue,C.purpleLt,C.yellow,C.green];
    return `<div class="card anima sc-in shadow-hard" style="position:absolute;top:${positions[i][1]}pt;left:${positions[i][0]}pt;width:138pt;height:62pt;background:${clrs[i]};animation-delay:${0.35+i*0.06}s;padding:10pt;">
      <p style="font-size:clamp(12px,1.3vw,16px);font-weight:700;text-align:center;">${esc(m)}</p>
    </div>`;
  }).join('')}

  <!-- Three-end badges at bottom -->
  <div class="anima au-up d7" style="position:absolute;bottom:45pt;left:80pt;right:80pt;display:flex;gap:20pt;justify-content:center;">
    ${['学员端','机构端','管理端'].map((t, i) =>
      `<div class="btn btn-white" style="font-size:clamp(12px,1.3vw,16px);padding:8pt 22pt;${['background:'+C.green,'background:'+C.blue,'background:'+C.purpleLt][i]};animation-delay:${0.6+i*0.1}s;">${esc(t)}</div>`
    ).join('')}
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P15 User Groups (Three ends) ──
function buildUsers(s) {
  const rc = [C.green, C.blue, C.purpleLt];
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:42pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">2.2 用户群体（三端）</h1>
      <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
  </div>

  ${s.roles.map((r, i) =>
    `<div class="card anima sl-${i===0?'left':'right'} shadow-hard" style="position:absolute;top:110pt;left:${60+i*310}pt;width:280pt;height:330pt;background:${rc[i]};animation-delay:${0.15+i*0.12}s;">
      <h3 style="font-size:clamp(20px,2.2vw,28px);font-weight:900;margin-bottom:16pt;">${esc(r.r)}</h3>
      ${r.d.map(d => `<p style="font-size:clamp(13px,1.4vw,17px);line-height:1.8;">· ${esc(d)}</p>`).join('')}
    </div>`
  ).join('')}

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P16 Showcase (real screenshot gallery) ──
function buildShowcase(s, idx, images) {
  const galleryItems = images.map((img, i) => {
    const isWide = i === 0 || i === 3;
    const h = isWide ? '300' : '200';
    const delay = (0.12 + i * 0.08).toFixed(2);
    return `
      <div class="anima sc-in shadow-hard" style="animation-delay:${delay}s;">
        <div class="card" style="padding:10pt;border:3pt solid ${C.ink};border-radius:14pt;overflow:hidden;background:${C.white};">
          <img src="${img.src}" alt="${esc(img.name)}" style="width:100%;height:${h}px;object-fit:cover;border-radius:6pt;border:2pt solid ${C.ink};display:block;" loading="lazy"/>
          <div style="padding:8pt 10pt;display:flex;justify-content:space-between;align-items:center;">
            <span style="font-weight:800;font-size:clamp(11px,1.2vw,15px);">${esc(img.name)}</span>
            <span class="badge badge-${['green','blue','purpleLt','yellow','green','blue'][i]}" style="font-size:9px;padding:3pt 8pt;">${i+1}</span>
          </div>
        </div>
      </div>`;
  }).join('');

  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:36pt;left:60pt;">
    <h1 class="t-title" style="font-size:clamp(24px,2.6vw,38px);">2.3 项目展示 \u2014 \u771F\u5B9E\u754C\u9762\u622A\u56FE</h1>
    <div style="width:64pt;height:5pt;background:${C.green};border-radius:3px;margin-top:8pt;"></div>
    <p class="t-small" style="margin-top:8pt;color:${C.gray600};">\u4E09\u7AEF\u516D\u89C6\u56FE\uFF1A\u5B66\u5458\u7AEF \u00B7 \u4F01\u4E1A\u7AEF \u00B7 \u7BA1\u7406\u7AEF \u2014 \u5168\u90E8\u57FA\u4E8E\u5B9E\u9645\u4EA7\u54C1\u8FD0\u884C\u754C\u9762</p>
  </div>

  <div style="position:absolute;top:110pt;left:50pt;right:50pt;display:flex;flex-wrap:wrap;gap:16pt;justify-content:center;align-content:flex-start;">
    ${galleryItems}
  </div>

  <div class="anima au-up" style="position:absolute;bottom:36pt;left:50pt;right:50pt;text-align:center;animation-delay:0.7s;">
    <span class="badge" style="background:${C.purpleLt};padding:8pt 20pt;font-size:clamp(12px,1.3vw,16px);">
      Neo-brutalism \u65B0\u7C97\u91CE\u4E3B\u4E49 \u2014 \u5976\u6CB9\u5E95\u8272 + \u58A8\u9ED1\u8FB9\u6846 + \u786C\u504F\u79FB\u9634\u5F71 + \u5706\u89D2\u5361\u7247
    </span>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">\u2190 \u4E0A\u4E00\u9875 \u00B7 \u4E0B\u4E00\u9875 \u2192</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P17 Agent ──
function buildAgent(s, idx, images) {
  return `<div class="slide bg-gradient-green" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:38pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">亮点① Agent 智能学伴</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
    <p class="t-small" style="margin-top:8pt;">LLM 驱动的学伴 —— 把"健身房私教"做成"学习私教"</p>
  </div>

  <div style="position:absolute;top:115pt;left:60pt;width:560pt;">
    <div class="card card-green anima sl-left shadow-hard" style="padding:22pt 28pt;margin-bottom:18pt;animation-delay:0.15s;">
      <h3 style="font-size:clamp(17px,1.9vw,24px);font-weight:900;margin-bottom:14pt;">三大能力</h3>
      <div style="display:flex;gap:14pt;flex-wrap:wrap;">
        ${s.tags.map(t => `<div class="badge badge-${['green','blue','purple'][s.tags.indexOf(t)]}" style="padding:10pt 18pt;font-size:clamp(13px,1.4vw,17px);">${esc(t)}</div>`).join('')}
      </div>
      <p class="t-small" style="margin-top:14pt;line-height:1.6;">用户提问 → AgentService 拼装学习画像上下文 → DeepSeek LLM → 个性化回答 / AI简历生成</p>
    </div>

    <div class="card card-blue anima sl-left shadow-hard" style="padding:20pt 28pt;animation-delay:0.3s;">
      <h3 style="font-size:clamp(15px,1.7vw,22px);font-weight:800;margin-bottom:10pt;">💡 亮点：档案即上下文</h3>
      <p class="t-small" style="line-height:1.6;">不是简单套壳问答 —— 注入信用余额 / 诚信分 / 课程进度 / 目标技能 / 心仪职位，让回答贴合个人情况。</p>
    </div>
  </div>

  <!-- Right: Real screenshot of admin dashboard showing AI widget -->
  ${(() => { const ai = images.find(i => i.id === 's04') || images[3]; return `
  <div class="card anima sl-right shadow-hard" style="position:absolute;top:115pt;right:60pt;width:340pt;height:320pt;padding:10pt;border-radius:16pt;overflow:hidden;background:${C.white};animation-delay:0.25s;">
    <img src="${ai.src}" alt="${esc(ai.name)} - AI Widget" style="width:100%;height:260px;object-fit:cover;border-radius:10pt;border:2pt solid ${C.ink};display:block;" loading="lazy"/>
    <div style="padding:6pt 4pt;display:flex;justify-content:center;align-items:center;gap:8pt;">
      <div class="ai-widget" style="position:static;width:32px;height:32px;font-size:16px;"></div>
      <span style="font-size:11px;font-weight:700;color:${C.gray600};">AI \u667A\u80FD\u52A9\u624B\u60AC\u6D6A\u7A97</span>
    </div>
  </div>`; })()}

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P18 Hash Attestation ──
function buildHash(s) {
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:38pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">亮点② 学分凭证哈希存证</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
    <p class="t-small" style="margin-top:8pt;">诚实口径：SHA-256 防伪哈希（非上链），预留联盟链演进空间</p>
  </div>

  <!-- Flow diagram -->
  <div class="anima au-up d2" style="position:absolute;top:108pt;left:60pt;right:60pt;display:flex;align-items:center;gap:10pt;justify-content:center;flex-wrap:wrap;">
    ${s.flow.map((txt, i) => `
      <div class="card anima sc-in shadow-hard" style="background:${[C.green,C.blue,C.purpleLt,C.yellow][i]};padding:16pt 20pt;text-align:center;min-width:130pt;animation-delay:${0.15+i*0.12}s;">
        <p style="font-size:clamp(13px,1.4vw,17px);font-weight:700;white-space:pre-line;line-height:1.4;">${esc(txt)}</p>
      </div>
      ${i < 3 ? '<p style="font-size:24px;font-weight:700;color:#1A202C;">→</p>' : ''}
    `).join('')}
  </div>

  <!-- Key features -->
  <div class="card anima sl-left shadow-hard" style="position:absolute;top:228pt;left:60pt;right:60pt;padding:22pt 28pt;background:${C.cream};border-color:#CBD5E1;animation-delay:0.5s;">
    <h3 style="font-size:clamp(16px,1.8vw,22px);font-weight:700;margin-bottom:12pt;">关键特性</h3>
    <div style="display:flex;gap:14pt;flex-wrap:wrap;">
      ${['防伪造：哈希唯一绑定内容','防篡改：任何改动都会导致不一致','可验证：公开接口 + 二维码扫码验真','预留演进空间：可对接联盟链上链'].map(t =>
        `<div style="background:${C.white};border:2pt solid ${C.ink};border-radius:8pt;padding:8pt 16pt;"><p style="font-size:clamp(12px,1.3vw,16px);">✓ ${esc(t)}</p></div>`
      ).join('')}
    </div>
  </div>

  <div class="anima au-up d7" style="position:absolute;bottom:38pt;left:60pt;">
    <span class="badge" style="background:${C.pink};color:${C.white};font-size:clamp(12px,1.3vw,16px);">⚠️ 口径说明：当前为"区块链校验"功能的哈希存证实现，未使用智能合约或公链。</span>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P19 Points + Integrity ──
function buildPoints(s) {
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:38pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">亮点③ 积分激励 + ④ 诚信评定</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
  </div>

  <div class="card card-yellow anima sl-left shadow-hard" style="position:absolute;top:105pt;left:60pt;width:440pt;height:300pt;animation-delay:0.15s;">
    <h3 style="font-size:clamp(19px,2.1vw,28px);font-weight:900;margin-bottom:14pt;">积分商城 — 游戏化激励闭环</h3>
    <p class="t-small" style="margin-bottom:14pt;font-weight:700;">对应"健身房打卡激励"</p>
    ${s.left.d.map(d => `<p style="font-size:clamp(13px,1.4vw,17px);line-height:1.7;margin:4pt 0;">· ${esc(d)}</p>`).join('')}
  </div>

  <div class="card card-purple anima sl-right shadow-hard" style="position:absolute;top:105pt;right:60pt;width:440pt;height:300pt;animation-delay:0.3s;">
    <h3 style="font-size:clamp(19px,2.1vw,28px);font-weight:900;margin-bottom:14pt;">诚信评定 — 可信分的门控机制</h3>
    <p class="t-small" style="margin-bottom:14pt;font-weight:700;">基于行为数据的动态评分</p>
    ${s.right.d.map(d => `<p style="font-size:clamp(13px,1.4vw,17px);line-height:1.7;margin:4pt 0;">· ${esc(d)}</p>`).join('')}
    <p style="font-size:clamp(12px,1.3vw,16px);margin-top:12pt;font-weight:600;color:${C.purple};">诚信分低 → 门控部分高阶权益</p>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P20 Recruitment ──
function buildRecruit(s) {
  return `<div class="slide bg-gradient-green" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:38pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">亮点⑤ 全链路求职闭环</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
    <p class="t-small" style="margin-top:8pt;">从简历到 offer 一站式 —— 可信学分直接背书求职</p>
  </div>

  <!-- Flow chain -->
  <div class="anima au-up d2" style="position:absolute;top:108pt;left:50pt;right:50pt;display:flex;align-items:center;gap:10pt;justify-content:center;flex-wrap:wrap;">
    ${s.flow.map((txt, i) => `
      <div class="card anima sc-in shadow-hard" style="background:${[C.green,C.blue,C.purpleLt,C.yellow][i]};padding:18pt 22pt;text-align:center;min-width:140pt;animation-delay:${0.15+i*0.12}s;">
        <p style="font-size:clamp(12px,1.3vw,16px);font-weight:700;white-space:pre-line;line-height:1.4;">${esc(txt)}</p>
      </div>
      ${i < 3 ? '<p style="font-size:24px;font-weight:700;color:#1A202C;">→</p>' : ''}
    `).join('')}
  </div>

  <div class="card anima sl-left shadow-hard" style="position:absolute;top:228pt;left:50pt;right:50pt;padding:22pt 28pt;background:${C.cream};border-color:#CBD5E1;animation-delay:0.5s;">
    <h3 style="font-size:clamp(15px,1.7vw,22px);font-weight:700;margin-bottom:10pt;">为什么这是亮点？</h3>
    <p style="font-size:clamp(13px,1.4vw,17px);line-height:1.65;">
      学员用平台累积的<b>可信学分 / 区块链凭证</b>给简历背书，机构在平台内直接完成筛选、约面（TRTC 真实音视频）、发结果。<br/>
      呼应价值飞轮：<b>学员的"可信能力"直接对接企业需求</b> —— 这是我们吸引机构入驻的关键。
    </p>
  </div>

  <div class="card anima sc-in" style="position:absolute;bottom:38pt;left:60pt;width:360pt;height:80pt;border:2pt dashed ${C.gray400};border-radius:14pt;display:flex;align-items:center;justify-content:center;background:#FAFAF5;animation-delay:0.6s;">
    <p style="font-size:clamp(12px,1.3vw,16px);color:${C.gray600};">在线面试界面截图占位</p>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P21 Learning Experience ──
function buildLearning(s) {
  const bgs = [C.green, C.blue, C.purpleLt];
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:38pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">亮点⑥ 轻松活泼的教学体验</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
    <p class="t-small" style="margin-top:8pt;">让学习不孤单 —— "图书馆/健身房"氛围的产品化落地</p>
  </div>

  ${s.features.map((f, i) => `
    <div class="card anima sc-in shadow-hard" style="position:absolute;top:115pt;left:${60+i*308}pt;width:280pt;height:250pt;background:${bgs[i]};animation-delay:${0.15+i*0.15}s;">
      <p style="font-size:36pt;margin-bottom:10pt;">${f.i}</p>
      <h3 style="font-size:clamp(18px,2vw,26px);font-weight:800;margin:8pt 0;">${esc(f.t)}</h3>
      <p class="t-small" style="line-height:1.6;">${esc(f.d)}</p>
    </div>`).join('')}

  <div class="anima au-up d7" style="position:absolute;bottom:38pt;left:80pt;right:80pt;">
    <p style="font-size:clamp(13px,1.4vw,17px);font-style:italic;">还有：评论区交流心得 + 课件一键获取 —— 氛围感 + 即时反馈，帮学员坚持下去。</p>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P23 Architecture ──
function buildArch(s) {
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:36pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(25px,2.7vw,38px);">架构总览 + 技术选型</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
    <p class="badge badge-pink" style="margin-top:8pt;font-size:11px;padding:4pt 12px;">分层单体（非微服务），可平滑演进</p>
  </div>

  <!-- Layered architecture -->
  <div style="position:absolute;top:98pt;left:46pt;width:620pt;height:340pt;">
    ${s.layers.map((l, i) => `
      <div class="card anima sl-left shadow-hard" style="position:absolute;top:${i*58}px;left:0;width:590px;height:50pt;background:${[C.green,C.blue,C.purpleLt,C.yellow][i]};animation-delay:${0.1+i*0.1}s;padding:0;display:flex;align-items:center;justify-content:center;">
        <p style="font-size:clamp(13px,1.4vw,17px);font-weight:700;text-align:center;">${esc(l)}</p>
      </div>`).join('')}

    <!-- External services -->
    <div class="anima sc-in" style="position:absolute;top:62px;left:-12px;background:${C.pinkLt};border:2pt solid ${C.ink};border-radius:8pt;padding:6pt 14px;box-shadow:2pt 2pt 0 ${C.ink};animation-delay:0.4s;">
      <p style="font-size:11px;font-weight:600;">DeepSeek LLM</p>
    </div>
    <div class="anima sc-in" style="position:absolute;top:62px;right:-12px;background:${C.blue};border:2pt solid ${C.ink};border-radius:8pt;padding:6pt 14px;box-shadow:2pt 2pt 0 ${C.ink};animation-delay:0.45s;">
      <p style="font-size:11px;font-weight:600;">腾讯云 TRTC</p>
    </div>
  </div>

  <!-- Tech stack wall -->
  <div style="position:absolute;top:98pt;right:46pt;width:270pt;">
    <h3 class="anima sl-right" style="font-size:clamp(15px,1.7vw,20px);margin-bottom:12pt;animation-delay:0.2s;">技术栈</h3>
    <div style="display:flex;flex-wrap:wrap;gap:8pt;">
      ${s.techs.map((t, i) => `
        <div class="anima sc-in" style="background:${C.cream};border:2pt solid ${C.ink};border-radius:6pt;padding:6pt 14pt;box-shadow:1pt 1pt 0 ${C.ink};animation-delay:${0.25+i*0.05}s;">
          <p style="font-size:clamp(10px,1.1vw,13px);font-weight:600;">${esc(t)}</p>
        </div>`).join('')}
    </div>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P24 Tech Highlights ──
function buildHighlights(s) {
  const clrs = [C.green, C.blue, C.purpleLt, C.yellow, C.green, C.blue];
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:34pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(25px,2.7vw,38px);">技术亮点全景 — 6 个真实技术点</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
  </div>
  <div class="anima sc-in" style="position:absolute;top:34pt;right:80pt;">
    <div class="badge badge-green pl-glow" style="font-size:11px;padding:5pt 14px;">全部有代码支撑 ✓</div>
  </div>

  ${s.items.map((it, i) => {
    const row = Math.floor(i / 3);
    const col = i % 3;
    return `<div class="card anima sc-in shadow-hard" style="position:absolute:${104+row*172}px;${56+col*304}px;width:286pt;height:158pt;background:${clrs[i]};animation-delay:${0.1+i*0.08}s;">
      <div style="display:flex;align-items:center;gap:10pt;margin-bottom:8pt;">
        <div style="background:${C.ink};color:${C.white};border-radius:6pt;padding:3pt 10pt;"><p style="font-size:13pt;font-weight:700;">${it.n}</p></div>
        <h3 style="font-size:clamp(14px,1.6vw,19px);font-weight:700;">${esc(it.t)}</h3>
      </div>
      <p class="t-small" style="line-height:1.5;">${esc(it.d)}</p>
      ${it.tag ? `<p style="font-size:10pt;color:${C.green};font-weight:600;margin-top:6pt;">${esc(it.tag)}</p>` : ''}
    </div>`;
  }).join('')}

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P25 Agent Deep Dive ──
function buildAgentDeep(s) {
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:36pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(25px,2.7vw,38px);">深讲① Agent 智能学伴（LLM 调用链路）</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
  </div>

  <!-- Sequence flow -->
  <div class="anima au-up d2" style="position:absolute;top:98pt;left:46pt;right:46pt;display:flex;align-items:center;gap:8pt;justify-content:center;flex-wrap:wrap;">
    ${s.flow.map((txt, i) => `
      <div class="card anima sc-in shadow-hard" style="background:${[C.green,C.blue,C.purpleLt,C.yellow,C.green][i]};padding:12pt 18pt;text-align:center;animation-delay:${0.12+i*0.1}s;">
        <p style="font-size:clamp(11px,1.2vw,15px);font-weight:600;white-space:pre-line;line-height:1.35;">${esc(txt)}</p>
      </div>
      ${i < 4 ? '<p style="font-size:18px;font-weight:700;color:#1A202C;">→</p>' : ''}
    `).join('')}
  </div>

  <!-- Context fields -->
  <div class="card anima sl-left shadow-hard" style="position:absolute;top:198pt;left:46pt;right:46pt;padding:20pt 26pt;background:${C.cream};border-color:#CBD5E1;animation-delay:0.5s;">
    <h3 style="font-size:clamp(15px,1.7vw,22px);font-weight:700;margin-bottom:12pt;">上下文注入字段</h3>
    <div style="display:flex;gap:10pt;flex-wrap:wrap;">
      ${s.ctxFields.map(f => `<div style="background:${C.white};border:2pt solid ${C.ink};border-radius:6pt;padding:5pt 14pt;"><p style="font-size:clamp(11px,1.2vw,14px);">${esc(f)}</p></div>`).join('')}
    </div>
    <p class="t-small" style="margin-top:14pt;"><b>关键技术：</b>RestClient 调用 DeepSeek API · 同一条链路复用于 AI 简历生成 · 上下文窗口管理</p>
  </div>

  <div class="card card-green anima sc-in shadow-hard" style="position:absolute;top:350pt;left:46pt;right:46pt;padding:18pt 26pt;animation-delay:0.65s;">
    <h3 style="font-size:clamp(14px,1.6vw,20px);font-weight:700;margin-bottom:8pt;">为什么不是简单套壳？</h3>
    <p class="t-small" style="line-height:1.55;">每个用户的回答都基于其真实的学习档案 —— 同样问"推荐什么课"，新手和进阶者得到的答案完全不同。"档案即上下文"是核心差异化。</p>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P26 TRTC Deep Dive ──
function buildTrtcDeep(s) {
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:36pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(25px,2.7vw,38px);">深讲② TRTC 线上视频面试</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
    <p class="t-small" style="margin-top:6pt;">真实音视频集成 —— 自建签名服务，不裸奔密钥</p>
  </div>

  <div class="anima au-up d2" style="position:absolute;top:98pt;left:46pt;right:46pt;display:flex;align-items:center;gap:7pt;justify-content:center;flex-wrap:wrap;">
    ${s.flow.map((txt, i) => `
      <div class="card anima sc-in shadow-hard" style="background:${[C.green,C.blue,C.purpleLt,C.yellow,C.green][i]};padding:12pt 16pt;text-align:center;animation-delay:${0.1+i*0.1}s;">
        <p style="font-size:clamp(10pt,1.1vw,14px);font-weight:600;white-space:pre-line;line-height:1.35;">${esc(txt)}</p>
      </div>
      ${i < 4 ? '<p style="font-size:20px;font-weight:700;color:#1A202C;">→</p>' : ''}
    `).join('')}
  </div>

  <div class="card anima sl-left shadow-hard" style="position:absolute;top:198pt;left:46pt;right:46pt;padding:18pt 24pt;background:${C.cream};border-color:#CBD5E1;animation-delay:0.5s;">
    <h3 style="font-size:clamp(15px,1.7vw,22px);font-weight:700;margin-bottom:10pt;">🔒 安全设计要点</h3>
    <div style="display:flex;gap:10pt;flex-wrap:wrap;">
      ${s.sec.map(st => `<div style="background:${C.white};border:2pt solid ${C.ink};border-radius:6pt;padding:5pt 14pt;"><p style="font-size:clamp(10pt,1.1vw,14pt);">🔒 ${esc(st)}</p></div>`).join('')}
    </div>
  </div>

  <div class="card anima sc-in" style="position:absolute;top:345pt;left:46pt;width:500pt;height:78pt;border:2pt dashed ${C.gray400};border-radius:14pt;display:flex;align-items:center;justify-content:center;background:#FAFAF5;animation-delay:0.6s;">
    <p style="font-size:clamp(12px,1.3vw,16px);color:${C.gray600};">面试房间界面截图占位</p>
  </div>
  <div class="card card-blue anima sl-right shadow-hard" style="position:absolute;top:345pt;right:46pt;width:420pt;height:78pt;padding:14pt 20pt;animation-delay:0.6s;">
    <h3 style="font-size:clamp(13px,1.5vw,18px);font-weight:700;margin-bottom:6pt;">🎥 现场可演示！</h3>
    <p style="font-size:clamp(11px,1.2vw,15px);line-height:1.5;">答辩时可实时演示浏览器内音视频通话 —— 不是 mock，是真跑通 TRTC。</p>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P27 Organization ──
function buildOrg(s) {
  const mc = [C.green, C.blue, C.purpleLt, C.yellow];
  return `<div class="slide bg-cream" data-id="${s.id}">
  <div class="anima sl-left" style="position:absolute;top:40pt;left:80pt;">
    <h1 class="t-title" style="font-size:clamp(26px,2.8vw,40px);">组织管理（4 人团队）</h1>
    <div style="width:56pt;height:5pt;background:${C.green};border-radius:3pt;margin-top:8pt;"></div>
  </div>

  ${s.members.map((m, i) => `
    <div class="card anima sc-in shadow-hard" style="position:absolute;top:105pt;left:${50+i*232}pt;width:216pt;height:290pt;background:${mc[i]};animation-delay:${0.1+i*0.12}s;">
      <h3 style="font-size:clamp(17px,1.9vw,24px);font-weight:900;">${esc(m.name)}</h3>
      <p style="font-size:clamp(11px,1.2vw,15px);color:${C.gray600};margin-bottom:12pt;">${esc(m.role)}</p>
      ${m.duts.slice(0,4).map(d => `<p style="font-size:clamp(10pt,1.1vw,13px);line-height:1.55;">· ${esc(d)}</p>`).join('')}
      ${m.duts.length > 4 ? `<p style="font-size:10pt;color:${C.gray400};">+${m.duts.length-4} 项...</p>` : ''}
    </div>`).join('')}

  <div class="card anima au-up" style="position:absolute;top:410pt;left:50pt;right:50pt;padding:16pt 24pt;background:${C.cream};border-color:#CBD5E1;animation-delay:0.6s;">
    <h3 style="font-size:clamp(14px,1.6vw,20px);font-weight:700;margin-bottom:6pt;">协作方式</h3>
    <p style="font-size:clamp(12px,1.3vw,16px);">${esc(s.collab)}</p>
  </div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ── P28 Closing ──
function buildClosing(s) {
  return `<div class="slide bg-dark" data-id="${s.id}" style="justify-content:center;align-items:center;flex-direction:column;padding-top:0;">
  <div style="text-align:center;">
    <h1 class="anima sc-in" style="font-size:clamp(36px,4vw,56px);color:${C.green};font-weight:900;line-height:1.4;animation-delay:0.2s;">${esc(s.headline)}</h1>
    <p class="anima au-up" style="font-size:clamp(18px,2vw,26px);color:${C.gray400};margin-top:28px;line-height:1.6;animation-delay:0.4s;">${esc(s.thanks)}</p>
    <p class="anima au-up" style="font-size:clamp(13px,1.4vw,17px);color:${C.gray600};margin-top:36px;line-height:1.6;animation-delay:0.55s;">${esc(s.outlook)}</p>
    <div class="anima sc-in" style="margin-top:48px;display:inline-block;background:${C.green};border:3pt solid #fff;border-radius:14pt;padding:14pt 44px;box-shadow:6px 6px 0 rgba(0,0,0,0.3);animation-delay:0.7s;">
      <p style="font-size:clamp(20px,2.2vw,28px);color:${C.darkBg};font-weight:900;">Q & A</p>
    </div>
    <p class="anima au-up" style="font-size:12px;color:${C.gray600};margin-top:48px;animation-delay:0.85s;">星秩存册 Singularis Ledger · 界面设计答辩 · 2026.07</p>
  </div>

  <!-- Floating stars -->
  <div class="star anima sc-in fl-bob" style="position:absolute;top:15vh;left:10vw;width:60px;height:60px;background:${C.green};border:none;opacity:0.5;animation-delay:1s;"></div>
  <div class="star anima sc-in fl-bob" style="position:absolute;bottom:15vh;right:12vw;width:80px;height:80px;background:${C.yellow};border:none;opacity:0.4;animation-delay:1.2s;"></div>

  <div class="page-num">${s.id} / 28</div>
  <div class="nav-hint">← 上一页 · 下一页 →</div>
  <div class="click-zone left"></div><div class="click-zone right"></div>
</div>`;
}

// ═══ Main ═══
try {
  const images = loadImages();
  fs.writeFileSync(OUT, buildDeck(images), 'utf-8');
  const sizeKB = (fs.statSync(OUT).size / 1024);
  console.log(`✅ Animated deck written to ${OUT}`);
  console.log(`   Size: ${sizeKB.toFixed(1)} KB`);
  console.log(`   Slides: ${SLIDES.length}`);
  console.log(`   Images embedded: ${images.length}`);
  console.log(`\nOpen in browser: file://${OUT.replace(/\\/g, '/')}`);
} catch(e) {
  console.error(e.message);
  process.exit(1);
}
