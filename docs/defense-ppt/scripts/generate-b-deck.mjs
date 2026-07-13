/**
 * 生成 Style B · 靛紫剧场 40 页答辩 deck
 * node scripts/generate-b-deck.mjs
 */
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, '..');
const SLIDES = path.join(ROOT, 'slides');

const CHAPTERS = [
  { idx: '01', label: '背景', pages: [3, 7] },
  { idx: '02', label: '产品', pages: [8, 23] },
  { idx: '03', label: '技术', pages: [24, 36] },
  { idx: '04', label: '组织', pages: [37, 39] },
  { idx: '05', label: '小结', pages: [40, 40] },
];

function activeChapter(page) {
  for (const ch of CHAPTERS) {
    if (page >= ch.pages[0] && page <= ch.pages[1]) return ch.idx;
  }
  return null;
}

function head(title) {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=1920" />
  <title>${title}</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800;900&display=swap" rel="stylesheet" />
  <link rel="stylesheet" href="../shared/grammar.css" />
  <link rel="stylesheet" href="../shared/themes-b-e.css" />
  <link rel="stylesheet" href="../shared/theme-b-layouts.css" />
</head>`;
}

function nav(page) {
  const active = activeChapter(page);
  const items = CHAPTERS.map(
    (ch) =>
      `<span class="ch${active === ch.idx ? ' active' : ''}"><span class="idx">${ch.idx}</span>${ch.label}</span>`
  ).join('\n      ');
  return `<header class="top-nav">
    <div class="brand"><span class="brand-mark">秩</span> 星秩存册</div>
    <nav class="chapters">${items}</nav>
  </header>`;
}

function beatCard(i, total, quote, detail, extra = '') {
  const active = i === 0 ? ' active' : '';
  return `<div class="beat-card${active}"><div class="beat-num">${String(i + 1).padStart(2, '0')} / ${String(total).padStart(2, '0')}</div><p class="beat-quote">${quote}</p><p class="beat-detail">${detail}</p>${extra}</div>`;
}

function beatBlock(cards, timeline = null) {
  const n = cards.length;
  const stack = cards
    .map((c, i) => beatCard(i, n, c.quote, c.detail, c.extra || ''))
    .join('\n          ');
  const tl = timeline
    ? `<div class="timeline-bar">
      <div class="timeline-track"><div class="timeline-fill"></div></div>
      <div class="timeline-nodes">${timeline.map((t, i) => `<div class="t-node${i === 0 ? ' lit' : ''}"><div class="t-dot"></div><span class="t-label">${t}</span></div>`).join('')}</div>
    </div>`
    : '';
  return `<div class="content-page">
    <div class="content-body${timeline ? '' : ' tight-left'}">
      <div class="content-left">
        <p class="chapter-label">${cards._label || ''}</p>
        <h1 class="content-headline">${cards._headline || ''}</h1>
        <p class="content-deck">${cards._deck || '按 → 逐条展开'}</p>
      </div>
      <div class="spotlight-stage">
        <div class="beat-stack" data-beat-deck>
          ${stack}
        </div>
      </div>
    </div>
    ${tl}
  </div>
  <div class="beat-progress"><div class="beat-progress-fill"></div></div>
  <div class="beat-hint show">→ 1 / ${n}</div>
  <script src="../shared/beat.js"></script>`;
}

function demoBtn(title, src, type) {
  const typeAttr = type ? ` data-demo-type="${type}"` : '';
  return `<button type="button" class="demo-play-btn" data-demo-open data-demo-title="${title}" data-demo-src="${src}"${typeAttr}><span class="play-icon">▶</span>观看操作演示</button>`;
}

function shell(page, title, body, scripts = []) {
  const extra = scripts.includes('demo-theater')
    ? '\n  <script src="../shared/demo-theater.js"></script>'
    : '';
  const navScript = scripts.includes('showcase-nav')
    ? '\n  <script src="../shared/showcase-nav.js"></script>'
    : '';
  return `${head(title)}
<body class="theme-b">
${body}${extra}${navScript}
</body>
</html>
`;
}

const slides = [];

slides.push({
  file: '01-cover.html',
  label: '封面',
  html: shell(
    1,
    'P1 · 封面',
    `  <div class="cover-page">
    <div class="cover-left">
      <p class="cover-tag reveal-on-load">SINGULARIS LEDGER · 2026</p>
      <h1 class="cover-title reveal-on-load">星秩<span class="accent">存册</span></h1>
      <div class="cover-rule reveal-on-load"></div>
      <p class="cover-slogan reveal-on-load">秩然有序，册录一生</p>
      <p class="cover-meta reveal-on-load">终身学习秩点银行平台 · 项目答辩<br /><br />答辩人：________　　指导老师：________</p>
    </div>
  </div>`,
    ['showcase-nav']
  ),
});

slides.push({
  file: '02-toc.html',
  label: '目录',
  html: shell(
    2,
    'P2 · 目录',
    `${nav(2)}
  <div class="b-page">
    <div class="b-page-inner">
      <p class="chapter-label">CONTENTS</p>
      <h1 class="content-headline">五章结构</h1>
      <div class="b-toc-grid">
        <div class="b-toc-card"><span class="tc-num">01</span><div><div class="tc-title">项目背景</div><div class="tc-sub">P3–P7</div></div></div>
        <div class="b-toc-card star"><span class="tc-num">02</span><div><div class="tc-title">产品介绍</div><div class="tc-sub">P8–P23 · 核心章节</div></div></div>
        <div class="b-toc-card star"><span class="tc-num">03</span><div><div class="tc-title">技术路线</div><div class="tc-sub">P24–P36 · 核心章节</div></div></div>
        <div class="b-toc-card"><span class="tc-num">04</span><div><div class="tc-title">组织管理</div><div class="tc-sub">P37–P39</div></div></div>
        <div class="b-toc-card span-2"><span class="tc-num">05</span><div><div class="tc-title">项目小结</div><div class="tc-sub">P40</div></div></div>
      </div>
    </div>
  </div>`,
    ['showcase-nav']
  ),
});

function heroPage(page, label, headline, deck, mega, extra = '') {
  return shell(
    page,
    `P${page} · ${label}`,
    `${nav(page)}
  <div class="b-page">
    <div class="chapter-watermark">${label.match(/^\d+/) ? label.match(/^\d+/)[0].padStart(2, '0') : ''}</div>
    <div class="b-hero-only">
      <p class="chapter-label">${label}</p>
      <h1 class="content-headline">${headline}</h1>
      ${deck ? `<p class="content-deck">${deck}</p>` : ''}
      ${mega ? `<p class="b-mega">${mega}</p>` : ''}
      ${extra}
    </div>
  </div>`,
    ['showcase-nav']
  );
}

slides.push({
  file: '03-bg-open.html',
  label: '背景 · 开场',
  html: heroPage(
    3,
    '01 · 项目背景',
    '学习成果，<br/>散落各处。',
    '国家推动终身学习与学分银行，但记录分散、激励不足、招聘脱节——学习者缺一座「资产银行」。'
  ),
});

(function () {
  const cards = [
    { quote: '学习记录分散，成果难沉淀互认', detail: 'Pain 01 · 跨机构互认缺失' },
    { quote: '激励单一，缺可持续积分经济', detail: 'Pain 02 · 学习动力不足' },
    { quote: '企业招聘与学员能力画像脱节', detail: 'Pain 03 · 数据孤岛' },
    { quote: '平台缺诚信约束与监管手段', detail: 'Pain 04 · 治理真空' },
  ];
  cards._label = '01 · 项目背景';
  cards._headline = '四个<em>痛点</em>';
  cards._deck = '按 → 逐条展开';
  slides.push({
    file: '04-pain-points.html',
    label: '四个痛点',
    html: shell(4, 'P4 · 痛点', nav(4) + '\n' + beatBlock(cards)),
  });
})();

(function () {
  const cards = [
    { quote: '多机构加盟的终身学习秩点银行', detail: '打通学习 → 秩点 → 诚信 → 就业闭环' },
    { quote: '学习行为可记录、可激励、可约束', detail: '可对接招聘 · 三端协同' },
  ];
  cards._label = '01 · 项目背景';
  cards._headline = '星秩存册的<em>回应</em>';
  cards._deck = '按 → 点亮业务闭环';
  const flow = `<div class="b-flow-row" data-flow-sync>
        <span class="b-flow-node lit">学习</span><span class="b-flow-arr">→</span>
        <span class="b-flow-node">秩点</span><span class="b-flow-arr">→</span>
        <span class="b-flow-node">诚信</span><span class="b-flow-arr">→</span>
        <span class="b-flow-node">就业</span>
      </div>`;
  slides.push({
    file: '05-response.html',
    label: '平台回应',
    html: shell(
      5,
      'P5 · 回应',
      `${nav(5)}
  <div class="content-page">
    <div class="content-body tight-left">
      <div class="content-left">
        <p class="chapter-label">${cards._label}</p>
        <h1 class="content-headline">${cards._headline}</h1>
        <p class="content-deck">${cards._deck}</p>
        ${flow}
      </div>
      <div class="spotlight-stage">
        <div class="beat-stack" data-beat-deck>
          ${cards.map((c, i) => beatCard(i, cards.length, c.quote, c.detail)).join('\n          ')}
        </div>
      </div>
    </div>
    <div class="timeline-bar">
      <div class="timeline-track"><div class="timeline-fill"></div></div>
      <div class="timeline-nodes">
        <div class="t-node lit"><div class="t-dot"></div><span class="t-label">学习</span></div>
        <div class="t-node"><div class="t-dot"></div><span class="t-label">秩点</span></div>
        <div class="t-node"><div class="t-dot"></div><span class="t-label">诚信</span></div>
        <div class="t-node"><div class="t-dot"></div><span class="t-label">就业</span></div>
      </div>
    </div>
  </div>
  <div class="beat-progress"><div class="beat-progress-fill"></div></div>
  <div class="beat-hint show">→ 1 / 2</div>
  <script src="../shared/beat.js"></script>`
    ),
  });
})();

function beatSlide(page, file, label, chLabel, headline, deck, cards, timeline) {
  cards._label = chLabel;
  cards._headline = headline;
  cards._deck = deck || '按 → 逐条展开';
  slides.push({
    file,
    label,
    html: shell(page, `P${page} · ${label}`, nav(page) + '\n' + beatBlock(cards, timeline)),
  });
}

beatSlide(6, '06-market.html', '市场机会', '01 · 项目背景', '市场<em>机会</em>', '政策 · 需求 · 空白', [
  { quote: '政策驱动：学分银行与产教融合', detail: '终身学习上升为国家战略方向' },
  { quote: '用户需求：学员要激励，企业要画像', detail: '平台要可监管、可扩展' },
  { quote: '市场空白：LMS 与招聘平台各缺一半', detail: '市面缺三端协同闭环' },
]);

beatSlide(7, '07-vs-competitors.html', '竞品对比', '01 · 项目背景', '我们 vs <em>竞品</em>', '每次只展开一个维度', [
  { quote: '学习管理：课程 / 打卡 / 档案', detail: '传统 LMS 仅部分支持 · 我们全链路覆盖' },
  { quote: '积分激励：秩点银行全链路', detail: '学习获秩点 · 商城消耗 · 规则引擎' },
  { quote: '招聘求职：投递 + TRTC 视频面试', detail: '平台内闭环 · 非外链会议' },
  { quote: '诚信 · AI · 社区', detail: '诚信分 + 四层 AI + 四频道论坛' },
  { quote: '非功能性保障', detail: 'JWT + RBAC · TRTC · Docker · 证书哈希校验' },
]);

beatSlide(8, '08-roles.html', '三类用户', '02 · 产品介绍', '三类用户，<em>一个平台</em>', null, [
  { quote: '学员：学习成长 · 秩点激励 · 简历投递', detail: '播放器 / 论坛 / 诚信 / 消息 / 视频面试' },
  { quote: '企业：招聘人才 · 活动运营 · 商城发品', detail: '投递管理 · 现场/视频面试 · 定向邀请' },
  { quote: '管理员：全平台监管与审核', detail: '机构加盟 · 举报处理 · 秩点/诚信监察' },
]);

slides.push({
  file: '09-overview.html',
  label: '产品概况',
  html: shell(
    9,
    'P9 · 概况',
    `${nav(9)}
  <div class="b-page">
    <div class="b-hero-only">
      <p class="chapter-label">02 · 产品介绍</p>
      <h1 class="content-headline">学习即资产<br/>秩点即货币</h1>
      <p class="content-deck">Spring Boot 3.4 · Vue 3 · DeepSeek · 腾讯云 TRTC</p>
      <div class="b-stat-row">
        <div class="b-stat-block"><div class="num">3</div><div class="lbl">角色端闭环</div></div>
        <div class="b-stat-block"><div class="num">40+</div><div class="lbl">数据表</div></div>
        <div class="b-stat-block"><div class="num">6</div><div class="lbl">创新亮点</div></div>
      </div>
    </div>
  </div>`,
    ['showcase-nav']
  ),
});

beatSlide(10, '10-portal.html', '平台门户', '02 · 产品介绍', '平台<em>门户</em>', '模块聚焦 · 一次一个', [
  { quote: '学习 & 商城', detail: '课程播放器 · 学习档案 · 秩点账户与商城兑换' },
  { quote: '社区 & 资讯', detail: '四频道论坛 + 招聘/活动/政策资讯中心' },
  { quote: '搜索 & 消息', detail: '全站聚合搜索 + AI 追问 · 私信与系统通知' },
  { quote: '企业与首页', detail: '角色化工作台 · 加盟企业展示与机构品牌页' },
]);

beatSlide(11, '11-student.html', '学员端', '02 · 产品介绍', '学员<em>端</em>', null, [
  { quote: '学习互动', detail: '资源列表 · 沉浸式播放器 · 课件 · 每日打卡' },
  { quote: '秩点简历', detail: '秩点账户与流水 · 简历编辑 · AI 一键生成' },
  { quote: '求职面试', detail: '职位投递 · 面试邀请 · TRTC 视频房' },
  { quote: '社区诚信', detail: '四频道论坛 · 诚信分 · 消息中心' },
]);

beatSlide(12, '12-enterprise-admin.html', '企业与管理', '02 · 产品介绍', '企业 & <em>管理端</em>', null, [
  { quote: '企业工作台', detail: '职位发布 · 投递/面试管理 · 活动运营 · 商城发品' },
  { quote: '监管后台', detail: '机构审核 · 举报处理 · 商品审批 · 秩点/诚信监察' },
]);

function twoColSlide(page, file, label, chLabel, headline, visualTitle, visualHint, cards, demo) {
  const n = cards.length;
  slides.push({
    file,
    label,
    html: shell(
      page,
      `P${page} · ${label}`,
      `${nav(page)}
  <div class="b-page">
    <div class="b-page-inner">
      <p class="chapter-label">${chLabel}</p>
      <h1 class="content-headline" style="font-size:72px;margin-bottom:32px">${headline}</h1>
      <div class="b-two-col">
        <div class="b-visual-stage">
          <div class="b-visual-label"><strong>${visualTitle}</strong><span>${visualHint}</span>${demo || ''}</div>
        </div>
        <div class="spotlight-stage" style="min-height:420px">
          <div class="beat-stack" data-beat-deck>
            ${cards.map((c, i) => beatCard(i, n, c.quote, c.detail)).join('\n            ')}
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="beat-progress"><div class="beat-progress-fill"></div></div>
  <div class="beat-hint show">→ 1 / ${n}</div>
  <script src="../shared/beat.js"></script>`,
      demo ? ['demo-theater'] : []
    ),
  });
}

twoColSlide(13, '13-player.html', '播放器', '02 · 项目展示', '沉浸式<em>播放器</em>', 'CoursePlayerView', '视频 + 弹幕 + 评论 + 课件 + 打卡', [
  { quote: '一个空间学完', detail: '多集进度自动上报' },
  { quote: '完课发证 · 秩点购课', detail: '学习行为实时计入账户' },
], demoBtn('课程播放器', '../assets/demos/course-player.gif'));

twoColSlide(14, '14-danmaku.html', '弹幕评论', '02 · 项目展示', '弹幕与<em>评论</em>', '动图演示', '15s 弹幕 + 树形评论 GIF', [
  { quote: 'B 站式弹幕层', detail: '时间点绑定 · 实时互动' },
  { quote: '树形评论 + 楼中楼', detail: '深度讨论不丢上下文' },
  { quote: '互动获秩点', detail: '社区活跃纳入激励体系' },
], demoBtn('弹幕与评论', '../assets/demos/forum.gif'));

beatSlide(15, '15-forum.html', '四频道论坛', '02 · 项目展示', '四频道<em>论坛</em>', null, [
  { quote: '校园频道', detail: '课程讨论与学习心得 · 发帖互动获秩点' },
  { quote: '校园集市', detail: '二手交易与互助 · 校园生活圈' },
  { quote: '求职经验', detail: '面试与简历交流 · 打通就业社区' },
  { quote: '政策解读', detail: '学分银行政策资讯 · 与平台定位呼应' },
]);

beatSlide(16, '16-info-search.html', '资讯搜索', '02 · 项目展示', '资讯 & <em>搜索</em>', null, [
  { quote: '资讯中心', detail: '招聘 · 活动 · 政策三类 Tab 聚合分发' },
  { quote: '全局搜索', detail: '课程/论坛/招聘/商品多类型 + SearchAgentPanel AI 追问' },
]);

twoColSlide(17, '17-ai-chat.html', 'AI助手', '02 · 项目展示', 'AI 秩点<em>助手</em>', 'AiAssistantPet', '登录即懂你的学习进度', [
  { quote: '不是通用聊天机器人', detail: 'DeepSeek · 自动注入学习画像上下文' },
  { quote: '全局悬浮 · 随时可问', detail: '课程进度 · 档案 · 秩点一屏掌握' },
], demoBtn('AI 秩点助手', '../assets/demos/ai-assistant.gif'));

twoColSlide(18, '18-ai-profile.html', 'AI画像', '02 · 项目展示', 'AI 学习<em>画像</em>', '结构化输出', '岗位 / 技能 / 短板 / 建议', [
  { quote: '目标岗位', detail: '基于真实学习数据定位' },
  { quote: '技能 · 优势 · 短板', detail: '可视化能力雷达' },
  { quote: '可执行提升建议', detail: '对接课程与活动推荐' },
]);

beatSlide(19, '19-ai-chain.html', 'AI四层链', '02 · 项目展示', 'AI 四层<em>智能链</em>', '每次只讲一层', [
  { quote: 'L1 · 对话助手', detail: '全局悬浮 · 注入课程进度与档案上下文' },
  { quote: 'L2 · 学习画像', detail: '结构化能力分析 · 真实数据驱动' },
  { quote: 'L3 · AI 简历', detail: '一键将档案与成果写入简历字段' },
  { quote: 'L4 · 搜索分析', detail: '全站搜索结果 + AI 追问辅助决策' },
]);

(function () {
  const cards = [
    { quote: '完课 / 打卡 / 论坛互动 → 获秩点', detail: '多场景规则引擎驱动' },
    { quote: '诚信分约束异常行为', detail: '管理员可监察 · 双轨约束' },
  ];
  cards._label = '02 · 项目展示';
  cards._headline = '秩点 × <em>诚信</em>';
  cards._deck = '激励与约束 · 同一套经济系统';
  slides.push({
    file: '20-credit-integrity.html',
    label: '秩点诚信',
    html: shell(
      20,
      'P20 · 秩点诚信',
      `${nav(20)}
  <div class="content-page">
    <div class="content-body tight-left">
      <div class="content-left">
        <p class="chapter-label">${cards._label}</p>
        <h1 class="content-headline">${cards._headline}</h1>
        <p class="content-deck">${cards._deck}</p>
        <div class="b-flow-row" data-flow-sync>
          <span class="b-flow-node lit">行为</span><span class="b-flow-arr">→</span>
          <span class="b-flow-node">秩点</span><span class="b-flow-arr">⇄</span>
          <span class="b-flow-node">诚信</span><span class="b-flow-arr">→</span>
          <span class="b-flow-node">商城</span>
        </div>
      </div>
      <div class="spotlight-stage">
        <div class="beat-stack" data-beat-deck>
          ${cards.map((c, i) => beatCard(i, cards.length, c.quote, c.detail)).join('\n          ')}
        </div>
      </div>
    </div>
  </div>
  <div class="beat-progress"><div class="beat-progress-fill"></div></div>
  <div class="beat-hint show">→ 1 / 2</div>
  <script src="../shared/beat.js"></script>`
    ),
  });
})();

twoColSlide(21, '21-trtc.html', '视频面试', '02 · 项目展示', '平台内<em>视频面试</em>', '全流程演示 ★', '投递 → 邀请 → 进房 15–20s', [
  { quote: '无需第三方会议链接', detail: '平台内闭环 · 状态同步' },
  { quote: 'TRTC UserSig 安全鉴权', detail: 'TlsSigApiV2 · 低延迟' },
  { quote: '投递状态机 + 录用私信联动', detail: 'canJoinVideo · 进房时间窗' },
], demoBtn('视频面试全流程', '../assets/demos/interview-trtc.gif'));

beatSlide(22, '22-cert-audit.html', '证书审核', '02 · 项目展示', '证书存证 & <em>审核</em>', null, [
  { quote: '区块链证书 · 哈希存证', detail: '完课发证 · SHA256 · 公开校验' },
  { quote: '全平台审核监管', detail: '论坛举报 · 商品审批 · 机构加盟 · 秩点监察' },
]);

beatSlide(23, '23-innovations.html', '六大创新', '02 · 项目展示', '六大<em>创新</em>', '按 → 逐个展开', [
  { quote: '秩点银行', detail: '多场景规则引擎 · 学习/论坛/打卡获秩点 · 商城消耗闭环' },
  { quote: '沉浸学习', detail: '弹幕 · 树形评论 · 课件预览 · 每日打卡' },
  { quote: 'AI 智能链', detail: '对话→画像→简历→搜索 · LlmService 统一对接 DeepSeek' },
  { quote: 'TRTC 视频面试', detail: '平台内闭环 · UserSig 鉴权 · 状态机联动录用' },
  { quote: '校园社区', detail: '四频道论坛 + Admin 监管矩阵并行' },
  { quote: '链上存证', detail: '证书 SHA256 哈希 POC · 可扩展公链对接' },
]);

slides.push({
  file: '24-chapter-tech.html',
  label: '章 · 技术',
  html: shell(
    24,
    'P24 · 技术章节',
    `  <div class="b-chapter-slide">
    <div class="b-chapter-big">03</div>
    <div class="b-chapter-name">技术路线</div>
    <div class="b-chapter-en">Architecture & Innovation</div>
  </div>`,
    ['showcase-nav']
  ),
});

beatSlide(25, '25-architecture.html', '三层架构', '03 · 技术路线', '三层<em>架构</em>', '点击展开 · 一次一层', [
  { quote: '表现层', detail: 'Vue 3 + TS + Element Plus · TRTC SDK v5 · ECharts' },
  { quote: '业务层', detail: 'Spring Boot 3.4 模块化 Service · JWT + RBAC · 分域 API' },
  { quote: '数据与外部', detail: 'MySQL 40+ 表 · Redis · Docker · DeepSeek · TRTC' },
]);

beatSlide(26, '26-frontend.html', '前端架构', '03 · 技术路线', '前端<em>架构</em>', null, [
  { quote: 'Vue 3 + Vite', detail: 'TypeScript · Pinia · 角色路由与菜单分权' },
  { quote: '模块划分', detail: 'views 页面 · components/learning · components/agent · api 封装' },
  { quote: '特色组件', detail: 'CourseVideoPlayer · DanmakuLayer · AiAssistantPet · InterviewVideoRoomView' },
]);

beatSlide(27, '27-backend.html', '后端架构', '03 · 技术路线', '后端<em>架构</em>', null, [
  { quote: '业务模块', detail: 'profile · enterprise · admin · forum 四大域' },
  { quote: '核心服务', detail: 'Learning · Llm · Agent · InterviewRtc 横切能力' },
  { quote: '安全', detail: 'AuthInterceptor + JWT + 企业 orgId 数据隔离' },
]);

beatSlide(28, '28-tech-stack.html', '技术选型', '03 · 技术路线', '技术<em>选型</em>', null, [
  { quote: 'Vue 3 + Spring Boot 3.4', detail: '前后端分离 · 模块化规范' },
  { quote: 'JWT + MyBatis-Plus', detail: '无状态认证 · 高效 ORM' },
  { quote: 'DeepSeek + TRTC', detail: '统一 LLM 层 · 低延迟音视频' },
  { quote: 'Docker Compose', detail: 'MySQL · Redis · Nginx 一键编排' },
]);

slides.push({
  file: '29-innov-credit.html',
  label: '创新 · 秩点',
  html: heroPage(29, '03 · 技术创新', '秩点<em>银行</em>', 'account · rule · transaction — 多场景规则驱动', '学习即资产，秩点即货币。'),
});

slides.push({
  file: '30-innov-learning.html',
  label: '创新 · 学习',
  html: shell(
    30,
    'P30 · 沉浸学习',
    `${nav(30)}
  <div class="b-page">
    <div class="b-hero-only">
      <p class="chapter-label">03 · 技术创新</p>
      <h1 class="content-headline">沉浸学习<em>引擎</em></h1>
      <p class="content-deck">弹幕时间点绑定 · 树形评论 · 打卡连续天数 · 课件联动</p>
      <div class="b-stat-row">
        <div class="b-stat-block"><div class="num">4</div><div class="lbl">互动形态合一</div></div>
        <div class="b-stat-block"><div class="num">B站</div><div class="lbl">式弹幕体验</div></div>
      </div>
    </div>
  </div>`,
    ['showcase-nav']
  ),
});

slides.push({
  file: '31-innov-ai.html',
  label: '创新 · AI',
  html: heroPage(
    31,
    '03 · 技术创新',
    'AI 不是<em>噱头</em>',
    'LearningProfileService.buildChatContext()',
    '基于真实学习档案的上下文注入，不是凭空生成。'
  ),
});

slides.push({
  file: '32-innov-trtc.html',
  label: '创新 · TRTC',
  html: heroPage(
    32,
    '03 · 技术创新',
    '招聘<em>最后一公里</em>',
    '投递状态机 · canJoinVideo · 进房时间窗 · 录用私信',
    null
  ),
});

slides.push({
  file: '33-innov-community.html',
  label: '创新 · 社区',
  html: heroPage(
    33,
    '03 · 技术创新',
    '社区 + <em>治理</em>',
    null,
    '四频道垂直社区，与 Admin 监管矩阵并行设计。'
  ),
});

slides.push({
  file: '34-innov-blockchain.html',
  label: '创新 · 存证',
  html: shell(
    34,
    'P34 · 证书存证',
    `${nav(34)}
  <div class="b-page">
    <div class="b-hero-only">
      <p class="chapter-label">03 · 技术创新</p>
      <h1 class="content-headline">证书<em>可验证</em></h1>
      <div class="b-flow-row" data-flow-sync style="justify-content:flex-start">
        <span class="b-flow-node lit">完课</span><span class="b-flow-arr">→</span>
        <span class="b-flow-node">编号</span><span class="b-flow-arr">→</span>
        <span class="b-flow-node">哈希</span><span class="b-flow-arr">→</span>
        <span class="b-flow-node">校验</span>
      </div>
    </div>
  </div>`,
    ['showcase-nav']
  ),
});

beatSlide(35, '35-database.html', '数据库', '03 · 技术路线', '40+ <em>数据表</em>', null, [
  { quote: '用户与机构', detail: 'sys_user · sys_organization · sys_tag' },
  { quote: '学习秩点', detail: 'course_danmaku · credit_account · mall_order · learning_archive' },
  { quote: '招聘论坛', detail: 'job_application · interview_invitation · forum_post · user_message' },
]);

beatSlide(36, '36-deploy.html', '部署', '03 · 技术路线', 'Docker <em>部署</em>', null, [
  { quote: '基础设施', detail: 'MySQL 3307 · Redis 6379 · Nginx · 静态资源' },
  { quote: '应用服务', detail: 'Spring Boot :8080 · Vue :5173 · mvnw / npm 启动' },
]);

slides.push({
  file: '37-chapter-team.html',
  label: '章 · 组织',
  html: shell(
    37,
    'P37 · 组织章节',
    `  <div class="b-chapter-slide">
    <div class="b-chapter-big">04</div>
    <div class="b-chapter-name">组织管理</div>
    <div class="b-chapter-en">Team & Collaboration</div>
  </div>`,
    ['showcase-nav']
  ),
});

beatSlide(38, '38-team.html', '团队成员', '04 · 组织管理', '团队<em>成员</em>', '填写姓名后答辩', [
  { quote: '成员 1 · ________', detail: '架构设计 · 认证权限 · 秩点商城 · 部署' },
  { quote: '成员 2 · ________', detail: 'Vue 架构 · 角色首页 · UI 体系' },
  { quote: '成员 3 · ________', detail: '招聘求职 · TRTC 视频面试 · 消息中心' },
  { quote: '成员 4 · ________', detail: '学习模块 · AI 集成 · 文档与测试' },
]);

beatSlide(39, '39-milestones.html', '里程碑', '04 · 组织管理', '项目<em>里程碑</em>', null, [
  { quote: '需求分析 → 需求文档', detail: 'Milestone 01' },
  { quote: '架构设计 → 表结构 / 接口', detail: 'Milestone 02' },
  { quote: '基础模块 → 学习 + 秩点 + 论坛', detail: 'Milestone 03' },
  { quote: '核心业务 → 招聘 + 活动 + 商城', detail: 'Milestone 04' },
  { quote: '创新功能 → TRTC + AI + 存证', detail: 'Milestone 05' },
  { quote: '联调答辩 → 全流程演示', detail: 'Milestone 06' },
]);

slides.push({
  file: '40-thanks.html',
  label: '致谢',
  html: shell(
    40,
    'P40 · 致谢',
    `  <div class="b-ending">
    <p class="chapter-label">05 · 项目小结</p>
    <h1 class="content-headline">感谢<em>聆听</em></h1>
    <div class="b-stat-row" style="justify-content:center;margin-top:24px">
      <div class="b-stat-block"><div class="num">3</div><div class="lbl">角色闭环</div></div>
      <div class="b-stat-block"><div class="num">6</div><div class="lbl">创新亮点</div></div>
      <div class="b-stat-block"><div class="num">40+</div><div class="lbl">数据表</div></div>
    </div>
    <p class="cover-slogan" style="margin-top:48px">欢迎提问</p>
    <p class="cover-meta" style="margin-top:20px">演示账号 · student1 / enterprise1 / admin · admin123</p>
  </div>`,
    ['showcase-nav']
  ),
});

for (const s of slides) {
  fs.writeFileSync(path.join(SLIDES, s.file), s.html, 'utf8');
  console.log('wrote', s.file);
}

const manifest = slides.map((s) => ({
  file: `slides/${s.file}`,
  label: s.label,
}));

fs.writeFileSync(
  path.join(ROOT, 'manifest.json'),
  JSON.stringify({ slides: manifest }, null, 2),
  'utf8'
);

console.log(`\nGenerated ${slides.length} slides.`);
