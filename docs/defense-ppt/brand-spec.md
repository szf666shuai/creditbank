# 星秩存册 · brand-spec.md

> huashu-design §1.a 品牌资产协议 · 答辩 PPT v2

## 产品事实

- **名称**：星秩存册（Singularis Ledger）
- **定位**：终身学习秩点银行平台
- **Slogan**：秩然有序，册录一生
- **来源**：`frontend/src/config/brand.ts`

## 色板（答辩场景 · 克制版）

| 角色 | 色值 | 用途 |
|------|------|------|
| 舞台深底 | `#06080f` | 外层背景 |
| 幻灯片底 | `#0c0a16` | 单页 canvas |
| 靛紫面板 | `#1a1630` / `#2b2450` | 卡片、侧栏 |
| 金色主 accent | `#c9a84c` | 标题强调、秩点 |
| 金色浅 | `#e8d5a3` | 大标题渐变 |
| 学员青 | `#3d9ec4` | 链接、科技模块 |
| 纸色 | `#f8f5ef` | 极少用于高亮块 |
| 正文 | `#f2f7fb` | 主文字 |
| 次级文字 | `rgba(176,204,222,.88)` | 副标题 |

## 字体

- **Display / 中文标题**：Noto Serif SC 600–900
- **UI / 正文**：Source Sans 3 400–600
- **英文标签**：Source Sans 3，letter-spacing 0.12–0.22em

## 视觉母题（form 来自内容）

- **册 / 秩**：左侧章节目录像「目录页码」；金色竖线像书签
- **银行闭环**：学习 → 秩点 → 诚信 → 就业 的流程节点
- **深空靛紫 + 克制金**：机构权威感，不是 SaaS 霓虹

## 禁区

- 不用 Inter / Roboto 作 display
- 不用高饱和 `#00a1d6` 大面积铺底
- 单页内不写页码（由 deck_index 外壳承载）
- 不用 emoji 作图标

## Logo

- 暂无独立 SVG logo 文件；封面用「秩」字 mark + 品牌名文字
- 待补：如有官方 logo 文件，base64 内嵌到 `slides/01-cover.html`
