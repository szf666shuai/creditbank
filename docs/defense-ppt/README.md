# 星秩存册 · 答辩 HTML 幻灯片 · Style B 靛紫剧场

> **40 页完整版** · huashu-design 多文件 deck · 深靛紫 + 克制金

## 打开方式

**Chrome / Edge** 打开：

```
docs/defense-ppt/index.html
```

1. 默认进入 **概览墙**（40 张卡片斜铺预览）
2. 点击任意卡片，或点右下角 **▶ 开始演示**
3. `ESC` 回概览 · `P` 打印 PDF

**请 Ctrl+F5 强刷** 避免缓存旧 manifest。

## 五章结构

| 章节 | 页码 | 内容 |
|------|------|------|
| 01 项目背景 | P3–P7 | 痛点 · 回应 · 市场 · 竞品 |
| 02 产品介绍 | P8–P23 | 三角色 · 功能模块 · 演示 · 六大创新 |
| 03 技术路线 | P24–P36 | 架构 · 选型 · 创新点 · 数据库 · 部署 |
| 04 组织管理 | P37–P39 | 成员 · 里程碑 |
| 05 项目小结 | P40 | 致谢 |

## 操作

| 操作 | 功能 |
|------|------|
| 概览墙点击 | 进入该页演示 |
| `→` / 空格 / 点右侧 | 页内展开 beat（展完才翻页） |
| `←` | 收回一步 / 上一页 |
| `ESC` | 回概览墙 |
| `Home` / `End` | 首末页 |

## 演示剧场（GIF/MP4）

P13 / P14 / P17 / P21 内置 **▶ 观看操作演示** 按钮，点击后在深色 screen 框播放。

素材路径：`assets/demos/`（见该目录 README）

## 目录结构

```
docs/defense-ppt/
├── index.html              ← deck 外壳（MANIFEST 在此）
├── manifest.json           ← 40 页清单（生成器输出）
├── brand-spec.md
├── scripts/
│   └── generate-b-deck.mjs ← 重新生成全部 slide
├── shared/
│   ├── grammar.css
│   ├── themes-b-e.css      ← Style B 调色
│   ├── theme-b-layouts.css ← 目录/章节/双栏等布局
│   ├── beat.js
│   ├── demo-theater.js
│   └── showcase-nav.js
├── slides/                 ← 01-cover.html … 40-thanks.html
└── v1-monolithic/          ← 旧版单文件备份
```

## 重新生成

修改 `scripts/generate-b-deck.mjs` 后：

```bash
node docs/defense-ppt/scripts/generate-b-deck.mjs
```

然后同步更新 `index.html` 中的 `DECK_MANIFEST`（或与 manifest.json 对齐）。

## 导出 PDF

```bash
cd .skills/huashu-design-full
node scripts/export_deck_pdf.mjs --slides ../../docs/defense-ppt/slides --out ../../docs/defense-ppt/defense.pdf
```

（需 `npm i playwright pdf-lib`）

## 内容修改

文案在 `scripts/generate-b-deck.mjs`，改完后运行：

```bash
node docs/defense-ppt/scripts/generate-b-deck.mjs
```

## 待你填写

- P1 封面：答辩人 / 指导老师姓名
- P38 团队成员：四位成员姓名

## 发给队友

打包整个 `docs/defense-ppt/` 文件夹（含 `slides/`、`shared/`、`assets/`），或推 Git 后队友打开 `index.html` 即可。
