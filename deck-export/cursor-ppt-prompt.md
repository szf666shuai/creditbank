# Cursor Agent 提示词 · 用 huashu-design 做「星秩存册」答辩 PPT

> 把下面整段复制进 Cursor 的 Agent / Chat 输入框即可。

---

你是一名使用 **huashu-design skill** 的幻灯片设计师。请用该 skill（位于 `D:\creditbank-1\.skills\huashu-design-full\`）的工作流，为我们的毕业设计项目 **「星秩存册 / Singularis Ledger」**（终身学习学分银行平台）制作一套**答辩用 PPT**。

## 一、必须先读取的参考资料
1. **内容源（逐字遵守）**：`D:\creditbank-1\答辩内容方案.md` —— 共 **29 页**，每页的「放什么 / 说什么 / 时长」都已定好。严格按它的结构与文案生成，**不要自行增减页面、不要杜撰数据或模块**。
2. **风格基准（真实产品 UI）**：`D:\creditbank-1\deck-export\imgs\` 下的 6 张截图（`s01-homepage.png` ~ `s06-admin-console.png`）。这是项目真实界面的 **neo-brutalism（新粗野风）** 视觉，是全部设计决策的唯一风格基准。
3. **skill 文档**：先读 `D:\creditbank-1\.skills\huashu-design-full\SKILL.md`，再按需读 `references/slide-decks.md` 与 `references/editable-pptx.md`。

> ⚠️ 注意：`frontend/src/style.css` 只是 Vue 脚手架默认样式（紫色 `#aa3bff`、`#app` 宽 1126px），**不是项目真实风格**，不要照它抽色板。真实风格只看那 6 张截图。

## 二、风格系统（抽到 `shared/tokens.css`，全 deck 共用，保证"一套"）
从截图提取的设计语言，写成 CSS 变量（所有幻灯片共用）：
- 背景 `--cream: #FFF9F0`；深色 section 用 `--ink: #1A202C`
- 文字 / 描边 `--ink: #1A202C`
- 主色 `--green: #22C55E`；浅蓝 `--blue: #BEE3F8`；浅紫 `--purple-l: #DDD6FE`；深紫 `--purple: #7C3AED`
- 边框：粗黑边 `3px solid var(--ink)`
- 硬阴影（粗野风灵魂）：`box-shadow: 6px 6px 0 var(--ink)`（无模糊的偏移阴影）
- 圆角：卡片 `14px`，pill 按钮 `999px`
- 字体：中文 **Noto Sans SC**（或 `system-ui` 兜底），标题用 900 字重；数字 / 英文用等宽粗体
- 组件母题（每页至少复用 2 个）：顶部导航条（logo ★ + 菜单 + 搜索框）、KPI 卡片（大数字 + 标签）、进度条、pill badge / 按钮（带 `→` 箭头）、角部有机 blob 装饰（蓝 / 紫）、浮动图标徽章

## 三、小图标怎么来（重要）
项目前端**没有独立的图标字体 / SVG 库**（`frontend/src/assets` 只有 `hero.png` / `vite.svg` / `vue.svg` 占位图）。所以「项目里的小图标」指的是**截图里那些 UI 图标**（★ 星、📘 书、🏆 奖杯、✓ 对勾、🎓 毕业帽、💬 对话、😊 AI 笑脸、🔍 搜索）。处理规则：
- 把这些图标**重绘成内联 SVG**，沿用上面的「粗黑边 + 硬阴影」neo-brutalist 风格，放进 `shared/icons/` 复用。
- 🚫 **不要用 emoji 字符**：Chromium / Playwright 导出时 emoji 会变成空方框。统一用重绘的 SVG 或 Unicode 符号（★ ✓ → ✦）。
- 同一图标在全 deck 只用同一份 SVG，保证一致。

## 四、一致性硬要求（"一看就是一套"）
- 每页共用同一份 `shared/tokens.css` 与设计母题；顶部 masthead / 底部页码 / 角部 blob 位置**全局统一**。
- 每页只用 4–5 种 layout 模板（封面 / 章节封 / 内容 / 数据 / 截图展示），靠内容而非换皮制造节奏。
- 字体、间距、圆角、阴影规则全 deck 一致。

## 五、内容诚实边界（必须严格遵守，答辩被追问也不能错）
- 区块链相关：只写 **"SHA-256 链下哈希存证 + 扫码验真"**，**严禁**写"上链 / 智能合约 / 联盟链 / 共识 / 哈希链式串联"——这些都没实现。
- 架构：写 **"前后端分离 + 分层单体"**，不要写"微服务"。
- 只呈现内容方案里已有的功能，不杜撰截图里没有的模块。

## 六、架构与导出（可编辑 PPTX 是最终交付）
- 采用**多文件架构**：`index.html`（复制 skill 的 `assets/deck_index.html` 改 `MANIFEST`）+ `slides/01-*.html` … `slides/29-*.html`，每页 `1920×1080`，`body` 锁尺寸。
- 因为最终要可编辑 PPTX，**从第一行 HTML 就遵守 html2pptx 的 4 条硬约束**（详见 `references/editable-pptx.md`）：
  1. 所有文字包在 `<p>`/`<h1>`–`<h6>`/`<ul>`/`<ol>` 里，禁止裸文本 `div`；
  2. `<p>`/`<h*>` 自身不要 `background/border/shadow`，移到外层 `div`；
  3. 不用 CSS gradient、不用 web component、不用复杂 SVG 装饰；
  4. `div` 不用 `background-image`，改用 `<img>` 标签。
- 完成后用 skill 的 `scripts/export_deck_pptx.mjs` 导出可编辑 PPTX：
  `node scripts/export_deck_pptx.mjs --slides slides --out 星秩存册-答辩.pptx`
  （注意：硬阴影 `box-shadow` 在 PPTX 里可能弱化 / 丢失，可依靠**粗黑边**保住粗野风辨识度。）
- 输出位置：`D:\creditbank-1\deck-export\`

## 七、流程
1. 先做 **2 页 showcase**（建议：封面 + 项目展示截图页）截图确认 grammar（配色 / 字体 / 母题 / 间距），通过后再批量做剩下 27 页。
2. 每页做完用浏览器或 Playwright 截图肉眼过一遍，再合成 `index.html`。
3. 交付前清理 `TODO` / `placeholder` 残留。

## 八、开场动作
请先读取上述 3 个参考资料，把设计系统**口头说一遍**（色板 / 字型 / layout 节奏 / 组件母题），确认后做 showcase。
