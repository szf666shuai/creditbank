# Design System Master File

> **LOGIC:** When building a specific page, first check `design-system/singularis-ledger/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

---

**Project:** 星秩存册 / Singularis Ledger  
**Generated:** 2026-07-13  
**Updated:** 2026-07-13 — Neo-brutal Light lock  
**Source:** ui-ux-pro-max + LearnHub neo-brutal reference vibe (not copy)  
**Category:** LMS / Lifelong Learning Credit Bank  

---

## Intent

Transform the student-facing public UI to a **Neo-brutal Light** educational look:

- Cream canvas `#FFF9F0`, thick dark borders, **hard offset shadows** (no blur)
- Vibrant green primary CTA `#22C55E`, pastel blue secondary `#BEE3F8`
- Floating pill header, course cards with icon boxes, sky-blue CTA band → cream footer
- Brand stays **星秩存册 / Singularis Ledger** — never LearnHub copy or fake stats

**Out of scope for early phases:** full admin/enterprise CRUD restyle (keep functional; optional light polish later).

---

## Global Rules

### Brand (non-negotiable)

| Token | Value |
|-------|--------|
| 中文名 | 星秩存册 |
| 英文名 | Singularis Ledger |
| 平台 | 终身学习秩点银行平台 |
| Slogan | 秩然有序，册录一生 |
| Site | singularis.edu.cn |

- Hero: brand is a **hero-level** signal (not only nav text).
- Do not paste LearnHub headlines, fake “2M students”, or emoji-as-icons.
- Icons: SVG (Heroicons / Lucide / existing `UiIcon`), not emoji.

### Style Stack

| Layer | Choice | Notes |
|-------|--------|--------|
| Primary style | **Neo-brutalism (light)** | Thick ink borders, solid fills, pastel accents |
| Depth layer | **Hard offset shadow** | `box-shadow: 4px 4px 0 0 #1A202C` — zero blur |
| Interaction | **Press** | Hover: `translate(2px,2px)` + smaller shadow |
| Layout rhythm | Sectioned landing + 2-col course grid | Hero → content sections → sky CTA card → cream footer |
| Mode | **Light only** for student public surfaces | No particle canvas as primary atmosphere |

**Avoid**

- Soft blurred shadows / glass `backdrop-filter` on marketing chrome
- Particle network as primary atmosphere
- Soft teal LMS palette (`#0D9488` / mint wash) — superseded
- Kids fonts (Baloo 2 / Comic Neue), AI purple gradients
- Floating promo stickers on hero media

### Color Palette (Neo-brutal Light)

| Role | Hex | CSS Variable | Usage |
|------|-----|--------------|--------|
| Primary | `#22C55E` | `--color-primary` / `--nb-green` | Primary CTA, success |
| Primary Deep | `#16A34A` | `--color-primary-dark` / `--nb-green-deep` | Hover / emphasis |
| On Primary | `#FFFFFF` | `--color-on-primary` | Text on green buttons |
| Secondary | `#BEE3F8` | `--color-secondary` / `--nb-blue` | Secondary buttons, badges |
| Sky band | `#BAE6FD` | `--nb-sky` | CTA section background |
| Accent Pink | `#FECDD3` | `--color-accent` / `--nb-pink` | Logo mark, icon boxes |
| Purple | `#DDD6FE` | `--nb-purple` | Icon boxes |
| Yellow | `#FEF08A` | `--nb-yellow` | Badges / chips |
| Background | `#FFF9F0` | `--color-background` / `--nb-cream` | Page canvas |
| Ink | `#1A202C` | `--nb-ink` / borders & text | Borders, shadows, headings |
| Card | `#FFFFFF` | `--color-card` | Cards, header pill |
| Muted Foreground | `#64748B` | `--color-muted-foreground` | Secondary text |

```css
--nb-shadow: 4px 4px 0 0 var(--nb-ink);
--nb-shadow-sm: 3px 3px 0 0 var(--nb-ink);
--nb-shadow-lg: 6px 6px 0 0 var(--nb-ink);
```

### Typography

| Role | Font | Weight |
|------|------|--------|
| Heading | **Nunito** | 700–900 |
| Body | **Source Sans 3** | 400–700 |
| CJK fallback | `"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif` | — |

```css
--font-heading: 'Nunito', 'PingFang SC', 'Microsoft YaHei', sans-serif;
--font-body: 'Source Sans 3', 'PingFang SC', 'Microsoft YaHei', sans-serif;
```

### Radius & Shadow

| Token | Value |
|-------|--------|
| `--radius-sm` | 10px |
| `--radius-md` | 14px |
| `--radius-lg` | 18px |
| `--radius-xl` | 24px |
| Pills | `999px` |
| `--shadow-*` | Hard offsets (see `--nb-shadow*`) — **not** soft blur |

Shared utilities: `frontend/src/styles/neubrutal.css` (`.nb-btn`, `.nb-card`, `.nb-badge`, `.nb-icon-box`).

### Motion

- Press interactions: `120ms ease` on transform + box-shadow
- Avoid long blur/glow animations on chrome

---

## Implementation map (student public)

| Surface | Status |
|---------|--------|
| Tokens (`variables.css`, `neubrutal.css`, `role-theme`) | Locked neo-brutal |
| AppHeader floating pill | Done |
| AppFooter cream multi-col | Done |
| StudentHome hero + CTA | Done |
| HomeContentSections course grid | Done |
| Resources / news / mall / forum / auth / player | Cascade next (use tokens + hard borders) |
| Admin / enterprise CRUD | Deferred |

- Hover: slight lift (`translateY(-2px)`) or shadow deepen — not spring squish
- Respect `prefers-reduced-motion: reduce`

### Components (public student)

| Component | Spec |
|-----------|------|
| Primary button | bg `--color-primary`, text white, radius 10px, height 44–48px |
| Secondary button | outline primary or soft mint fill |
| Accent button | `--color-accent` (amber) for “继续学习 / CTA band” |
| Course card | white, 16px radius, cover image top, title, meta row, optional progress |
| Section header | eyebrow optional (muted), H2, one short support line |
| Header | light translucent white / mint, border bottom soft, sticky ok |
| Footer | light muted band (not dark `#2c3e50`), teal links |

### Landing pattern (student home)

Inspired by educational platform demos — **structure only**:

1. Hero (brand + one headline + one sentence + CTA group + one visual plane)
2. Featured / continue learning strip
3. Course / resource grid
4. Why / value props (4 max)
5. Soft CTA band

Do not overload hero with stats, schedules, or promo chips.

### Checklist (every page delivery)

- [ ] No emoji as icons
- [ ] `cursor-pointer` on clickable elements
- [ ] Hover 150–300ms
- [ ] Text contrast ≥ 4.5:1 on light surfaces
- [ ] Visible focus ring (`--color-ring`)
- [ ] `prefers-reduced-motion` respected
- [ ] Responsive: 375 / 768 / 1024 / 1440
- [ ] Brand name still strong if nav is ignored

---

## Token Mapping: 当前深色玻璃 → 目标浅色教育站

### A. `frontend/src/styles/variables.css` (legacy light tokens, currently underused)

| Current token | Current value | Target | Notes |
|---------------|---------------|--------|--------|
| `--color-primary` | `#2094f3` | `#0D9488` | Blue → education teal |
| `--color-primary-dark` | `#1a7fd4` | `#0F766E` | Darker teal for hover |
| `--color-primary-light` | `#e8f4fe` | `#CCFBF1` / `#F0FDFA` | Soft mint wash |
| `--color-text` | `#333333` | `#134E4A` | Align with LMS foreground |
| `--color-text-secondary` | `#666666` | `#475569` or keep muted scale | Prefer `--color-muted-foreground` |
| `--color-text-muted` | `#999999` | `#64748B` | |
| `--color-border` | `#e8e8e8` | `#99F6E4` or `#E2E8F0` | Use mint for brand surfaces; neutral slate for dense forms |
| `--color-bg` | `#f5f7fa` | `#F0FDFA` | Page background |
| `--color-white` | `#ffffff` | `#FFFFFF` | Card / header |
| `--footer-bg` | `#2c3e50` | `#E8F1F4` or `#134E4A` text on mint | Leave dark footer |
| `--footer-text` | `#bdc3c7` | `#64748B` | |
| *(new)* | — | `--color-accent: #D97706` | CTA amber |
| *(new)* | — | `--font-heading` / `--font-body` | Lexend / Source Sans 3 |

### B. `frontend/src/styles/global.css`

| Current | Target |
|---------|--------|
| `body` background `#0b1527` | `--color-background` `#F0FDFA` |
| `body` font Helvetica / system stack | `--font-body` (+ CJK fallbacks) |
| `color: var(--color-text)` (dark text on dark bg mismatch) | `--color-foreground` `#134E4A` on light bg |
| No Google Fonts import | Add Lexend + Source Sans 3 import (or self-host later) |

### C. `frontend/src/config/role-theme.ts` — **student** (primary migration path)

| Field | Current (dark glass) | Target (light educational) |
|-------|----------------------|----------------------------|
| `primary` | `#00a1d6` | `#0D9488` |
| `primaryDark` | `#67e8f9` (was light-on-dark) | `#0F766E` (true dark for hover) |
| `primarySoft` | `#22d3ee` | `#2DD4BF` |
| `surface` | `rgba(6,22,38,0.58)` | `#FFFFFF` or `rgba(255,255,255,0.92)` |
| `surfaceStrong` | `rgba(8,30,52,0.74)` | `#FFFFFF` |
| `surfaceCard` | `rgba(10,36,58,0.68)` | `#FFFFFF` |
| `border` | `rgba(0,161,214,0.28)` | `#99F6E4` / `rgba(13,148,136,0.18)` |
| `text` | `#e8f8ff` | `#134E4A` |
| `textMuted` | `#8ec8de` | `#64748B` |
| `textOnHero` | `#fff` | `#134E4A` (light hero) *or* `#FFFFFF` only on solid teal CTA band |
| `shadow` | `0 12px 40px rgba(0,0,0,0.35)` | `0 4px 12px rgba(15,23,42,0.08)` |
| `heroGradient` | dark cyan gradient | `linear-gradient(135deg, #F0FDFA, #CCFBF1, #FFFFFF)` |
| `headerBg` | `rgba(6,22,38,0.9)` | `rgba(255,255,255,0.92)` |
| `headerBorder` | `rgba(0,161,214,0.22)` | `rgba(13,148,136,0.12)` |
| `particleBg` | dark navy triad | Soft mint washes *or* disable particles on home |
| `particleDot` / `Line` / `Glow` | cyan neon | Very soft teal at low opacity **or unused** |
| `logoBgFrom` / `logoBgTo` | `#0c4a6e` → `#0891b2` | `#0D9488` → `#0F766E` |
| `logoStar` / `logoAccent` | cyan lights | Keep light mint `#CCFBF1` / accent `#D97706` sparingly |
| `explosionColors` | cyan fireworks | Teal / amber / soft mint (optional; reduce use) |

### D. `role-theme.ts` — **default** (guest; header already uses student on home)

| Current | Target direction |
|---------|------------------|
| Purple glass `#7c3aed` + dark particles | Align guest public chrome with **student light teal** so first visit matches educational platform vibe |
| Keep purple only if product insists on “admin-like guest” — **not recommended** for LearnHub-like pivot |

### E. `role-theme.ts` — enterprise / admin (defer)

| Role | Guidance |
|------|----------|
| `enterprise` | Keep blue family for now; optional later shift to same light surfaces + blue primary |
| `admin` | Keep purple; lighten surfaces only if needed for consistency inside profile shell |

Do **not** force LearnHub marketing look onto dense admin tables in phase 0–6.

### F. `page-common.css` (shared shells — later steps)

| Current class feel | Target |
|--------------------|--------|
| `.page-card` dark glass + blur | White card, soft shadow, no blur |
| `.page-header__main h1` `#e0f2fe` | `--color-foreground` |
| `.page-header__main p` icy muted | `--color-muted-foreground` |
| Glass search / dark tables | Light inputs; tables: white/muted zebra (profile phase) |

### G. Runtime vs docs brand colors (`brand.ts`)

| `BRAND_COLORS` | Relation to new system |
|----------------|------------------------|
| indigo `#2b2450` / gold `#d4af37` | Historical identity; **student public UI uses teal LMS palette** |
| gold | May appear as rare ceremonial accent; **CTA amber `#D97706`** is the interactive accent |
| Do not mix indigo glass into student home hero |

---

## Implementation Order (reference for later prompts)

0. **This file** — done  
1. `variables.css` + `global.css` + `page-common.css` + `role-theme` student/default  
2. Header / Footer / MainLayout / ParticleBackground  
3. Student home  
4. Learning resources  
5. Auth  
6. Forum / News / Mall / Search / Course player shell  
7. Profile light pass  
8. Audit residual dark glass

---

## File Locations

| Artifact | Path |
|----------|------|
| Master | `design-system/singularis-ledger/MASTER.md` |
| Page overrides | `design-system/singularis-ledger/pages/` |
| Existing theme code (unchanged in Step 0) | `frontend/src/config/role-theme.ts` |
| Existing CSS vars (unchanged in Step 0) | `frontend/src/styles/variables.css` |
| Existing global (unchanged in Step 0) | `frontend/src/styles/global.css` |

---

## Next step prompt

Use the “第 1 步：全局基础” prompt from the migration plan: apply this Master to `variables.css` / `global.css` / `page-common.css` / `role-theme.ts` (student + default) only — still no full page redesigns.
