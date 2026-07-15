<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import StudentLearningStrip from '@/components/home/StudentLearningStrip.vue'
import StudentTodoStrip from '@/components/home/StudentTodoStrip.vue'
import HomeContentSections from '@/components/home/HomeContentSections.vue'
import HomeBannerCarousel from '@/components/home/HomeBannerCarousel.vue'
import UiIcon from '@/components/ui/UiIcon.vue'
import {
  BRAND_DESC,
  BRAND_NAME,
  BRAND_NAME_EN,
  BRAND_PLATFORM,
  BRAND_SLOGAN,
} from '@/config/brand'

const router = useRouter()
const authStore = useAuthStore()

const isStudentPortal = computed(() => authStore.isLoggedIn && authStore.isStudent)

const valueProps = [
  {
    icon: 'course',
    title: '按自己的节奏学',
    desc: '课程随时可学，进度与打卡留在学习档案里。',
    tint: 'green',
  },
  {
    icon: 'school',
    title: '机构与认证同行',
    desc: '加盟机构资源汇入平台，学习成果可被记录与核验。',
    tint: 'blue',
  },
  {
    icon: 'gift',
    title: '秩点激励成长',
    desc: '学习积累秩点，成长轨迹留存档案，让坚持可见。',
    tint: 'yellow',
  },
  {
    icon: 'forum',
    title: '社区互助交流',
    desc: '资讯、论坛与招聘通道打通，学用一体。',
    tint: 'pink',
  },
] as const

const highlightStats = [
  { label: '学习资源', hint: '课程随时开讲' },
  { label: '秩点激励', hint: '学有所得可查' },
  { label: '机构加盟', hint: '成果可核验' },
] as const

function go(path: string) {
  router.push(path)
}

onMounted(async () => {
  if (authStore.isLoggedIn && !authStore.userInfo) {
    await authStore.fetchUserInfo()
  }
})
</script>

<template>
  <div class="student-home nb">
    <section class="home-hero" aria-label="首页主视觉">
      <div class="home-hero__inner">
        <div class="home-hero__copy">
          <div class="system-badge">
            <span class="system-badge__label">终身学习</span>
            <span class="system-badge__divider">·</span>
            <span class="system-badge__highlight">学分银行平台</span>
          </div>

          <p class="home-hero__brand">{{ BRAND_NAME }}</p>
          <p class="home-hero__brand-en">{{ BRAND_NAME_EN }} · {{ BRAND_PLATFORM }}</p>

          <h1 class="home-hero__title">
            随时学、随时记<br />
            成长<span class="mark">有序入册</span>
          </h1>
          <p class="home-hero__desc">{{ BRAND_DESC }}</p>

          <div class="home-hero__actions">
            <template v-if="!authStore.isLoggedIn">
              <button type="button" class="nb-btn nb-btn--primary" @click="go('/resources')">
                开始学习
                <span class="nb-btn__arrow" aria-hidden="true">→</span>
              </button>
              <button type="button" class="nb-btn nb-btn--secondary" @click="go('/register')">
                免费注册
              </button>
            </template>
            <template v-else-if="isStudentPortal">
              <button type="button" class="nb-btn nb-btn--primary" @click="go('/resources')">
                继续学习
                <span class="nb-btn__arrow" aria-hidden="true">→</span>
              </button>
              <button type="button" class="nb-btn nb-btn--secondary" @click="go('/profile/learning')">
                我的学习档案
              </button>
            </template>
            <template v-else>
              <button type="button" class="nb-btn nb-btn--primary" @click="go('/resources')">
                进入学习资源
                <span class="nb-btn__arrow" aria-hidden="true">→</span>
              </button>
            </template>
          </div>

          <ul class="hero-stats" aria-label="平台能力">
            <li v-for="item in highlightStats" :key="item.label">
              <strong>{{ item.label }}</strong>
              <span>{{ item.hint }}</span>
            </li>
          </ul>
        </div>

        <div class="home-hero__stage">
          <div class="float-chip float-chip--pink" aria-hidden="true">
            <UiIcon name="star" :size="22" color="#1a202c" />
          </div>
          <div class="float-chip float-chip--purple" aria-hidden="true">
            <UiIcon name="collection" :size="22" color="#1a202c" />
          </div>
          <div class="float-chip float-chip--green" aria-hidden="true">
            <UiIcon name="medal" :size="22" color="#1a202c" />
          </div>

          <article class="course-card">
            <div class="course-card__top">
              <span class="course-card__tag">学习档案</span>
              <span class="course-card__live">进行中</span>
            </div>
            <h2 class="course-card__title">{{ BRAND_SLOGAN }}</h2>
            <p class="course-card__sub">从一门课开始，把进度、打卡与秩点写进册里。</p>
            <div class="course-card__progress">
              <div class="course-card__progress-meta">
                <span>本周学习节奏</span>
                <strong>68%</strong>
              </div>
              <div class="course-card__bar" role="presentation">
                <i style="width: 68%" />
              </div>
            </div>
            <button type="button" class="nb-btn nb-btn--primary nb-btn--block" @click="go('/resources')">
              继续学习
              <span class="nb-btn__arrow" aria-hidden="true">→</span>
            </button>
          </article>
        </div>
      </div>
    </section>

    <div class="student-home__body">
      <section v-if="isStudentPortal" class="portal-stack">
        <StudentLearningStrip />
        <StudentTodoStrip />
      </section>

      <HomeContentSections />

      <section class="value-section" aria-labelledby="value-heading">
        <div class="value-section__inner">
          <header class="value-section__head">
            <p class="section-eyebrow">为什么选择这里</p>
            <h2 id="value-heading">把每一次学习，都记进册里</h2>
            <p>平台连接机构、课程、秩点与社区，服务终身学习的完整链路。</p>
          </header>
          <ul class="value-grid">
            <li
              v-for="item in valueProps"
              :key="item.title"
              class="value-item"
              :class="`value-item--${item.tint}`"
            >
              <span class="value-item__icon" aria-hidden="true">
                <UiIcon :name="item.icon" :size="26" color="#1a202c" />
              </span>
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </li>
          </ul>
        </div>
      </section>

      <section class="banner-followup" aria-label="主题入口">
        <div class="banner-followup__inner">
          <header class="value-section__head banner-followup__head">
            <p class="section-eyebrow">探索更多</p>
            <h2>学习、秩点、论坛与就业入口</h2>
          </header>
          <HomeBannerCarousel compact />
        </div>
      </section>

      <section class="cta-band" aria-labelledby="cta-heading">
        <div class="cta-band__card">
          <h2 id="cta-heading">准备好开始学习了吗？</h2>
          <p>7 天体验学习资源与秩点激励——无需绑卡，随时开始。</p>
          <div class="cta-band__actions">
            <button type="button" class="nb-btn nb-btn--primary" @click="go('/resources')">
              免费开始
              <span class="nb-btn__arrow" aria-hidden="true">→</span>
            </button>
            <button
              type="button"
              class="nb-btn nb-btn--secondary"
              @click="go(authStore.isLoggedIn ? '/credit' : '/register')"
            >
              {{ authStore.isLoggedIn ? '我的秩点' : '了解注册' }}
            </button>
          </div>
          <ul class="cta-band__perks">
            <li>无需信用卡</li>
            <li>学习进度可追溯</li>
            <li>随时取消</li>
          </ul>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@600;700;800;900&family=Source+Sans+3:wght@400;500;600;700&display=swap');

.student-home.nb {
  --nb-cream: #fff9f0;
  --nb-ink: #1a202c;
  --nb-green: #22c55e;
  --nb-green-deep: #16a34a;
  --nb-blue: #bee3f8;
  --nb-pink: #fecdd3;
  --nb-purple: #ddd6fe;
  --nb-yellow: #fef08a;
  --nb-muted: #64748b;
  --nb-shadow: 4px 4px 0 0 var(--nb-ink);
  --nb-shadow-sm: 3px 3px 0 0 var(--nb-ink);
  --font-heading: 'Nunito', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-body: 'Source Sans 3', 'PingFang SC', 'Microsoft YaHei', sans-serif;

  position: relative;
  min-height: calc(100vh - var(--header-height));
  background: var(--nb-cream);
  color: var(--nb-ink);
  font-family: var(--font-body);
  padding-bottom: 0;
}

.home-hero {
  position: relative;
  padding: 48px 16px 40px;
  overflow: hidden;
  background:
    radial-gradient(circle at 88% 18%, rgba(34, 197, 94, 0.12), transparent 28%),
    radial-gradient(circle at 12% 80%, rgba(190, 227, 248, 0.45), transparent 32%),
    var(--nb-cream);
}

.home-hero__inner {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: var(--content-max-width);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(280px, 0.9fr);
  gap: 40px;
  align-items: center;
}

.system-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 14px 28px;
  border: 3px solid var(--nb-ink);
  border-radius: 16px;
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  box-shadow: var(--nb-shadow);
}

.system-badge__label {
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--nb-green-deep);
}

.system-badge__divider {
  font-size: 1.5rem;
  font-weight: 900;
  color: var(--nb-ink);
  opacity: 0.5;
}

.system-badge__highlight {
  font-size: 1.4rem;
  font-weight: 900;
  color: var(--nb-green-deep);
}

.home-hero__brand {
  margin: 0;
  font-family: var(--font-heading);
  font-size: clamp(2.6rem, 6vw, 3.6rem);
  font-weight: 900;
  letter-spacing: -0.02em;
  line-height: 1.05;
  color: var(--nb-ink);
}

.home-hero__brand-en {
  margin: 8px 0 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--nb-muted);
}

.home-hero__title {
  margin: 22px 0 14px;
  font-family: var(--font-heading);
  font-size: clamp(1.7rem, 3.4vw, 2.35rem);
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: -0.02em;
  color: var(--nb-ink);
}

.home-hero__title .mark {
  color: var(--nb-green-deep);
  background: linear-gradient(180deg, transparent 62%, rgba(34, 197, 94, 0.28) 62%);
}

.home-hero__desc {
  margin: 0 0 26px;
  max-width: 34rem;
  font-size: 1.05rem;
  line-height: 1.7;
  color: #475569;
}

.home-hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 28px;
}

.nb-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 48px;
  padding: 0 22px;
  border: 2.5px solid var(--nb-ink);
  border-radius: 14px;
  font-family: var(--font-heading);
  font-size: 0.98rem;
  font-weight: 800;
  cursor: pointer;
  box-shadow: var(--nb-shadow);
  transition: transform 0.12s ease, box-shadow 0.12s ease, background 0.12s ease;
  user-select: none;
}

.nb-btn:hover {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 0 var(--nb-ink);
}

.nb-btn:active {
  transform: translate(4px, 4px);
  box-shadow: 0 0 0 0 var(--nb-ink);
}

.nb-btn--primary {
  background: var(--nb-green);
  color: #fff;
}

.nb-btn--primary:hover {
  background: var(--nb-green-deep);
}

.nb-btn--secondary {
  background: var(--nb-blue);
  color: var(--nb-ink);
}

.nb-btn--cream {
  background: var(--nb-cream);
  color: var(--nb-ink);
}

.nb-btn--block {
  width: 100%;
}

.nb-btn__arrow {
  font-size: 1.1em;
  line-height: 1;
}

.hero-stats {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  max-width: 420px;
}

.hero-stats li {
  display: grid;
  gap: 2px;
}

.hero-stats strong {
  font-family: var(--font-heading);
  font-size: 1rem;
  font-weight: 800;
  color: var(--nb-ink);
}

.hero-stats span {
  font-size: 0.78rem;
  color: var(--nb-muted);
}

.home-hero__stage {
  position: relative;
  min-height: 360px;
  display: grid;
  place-items: center;
}

.course-card {
  position: relative;
  z-index: 1;
  width: min(100%, 360px);
  padding: 22px;
  background: #fff;
  border: 2.5px solid var(--nb-ink);
  border-radius: 22px;
  box-shadow: 8px 8px 0 0 var(--nb-ink);
}

.course-card__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.course-card__tag {
  font-size: 12px;
  font-weight: 800;
  color: var(--nb-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.course-card__live {
  padding: 4px 10px;
  border: 2px solid var(--nb-ink);
  border-radius: 999px;
  background: var(--nb-yellow);
  font-size: 11px;
  font-weight: 800;
}

.course-card__title {
  margin: 0 0 8px;
  font-family: var(--font-heading);
  font-size: 1.55rem;
  font-weight: 900;
  line-height: 1.25;
  color: var(--nb-ink);
}

.course-card__sub {
  margin: 0 0 18px;
  font-size: 0.92rem;
  line-height: 1.55;
  color: var(--nb-muted);
}

.course-card__progress {
  margin-bottom: 18px;
}

.course-card__progress-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.85rem;
  font-weight: 700;
}

.course-card__bar {
  height: 12px;
  border: 2px solid var(--nb-ink);
  border-radius: 999px;
  background: #f1f5f9;
  overflow: hidden;
}

.course-card__bar i {
  display: block;
  height: 100%;
  background: var(--nb-green);
  border-radius: inherit;
}

.float-chip {
  position: absolute;
  width: 52px;
  height: 52px;
  border: 2.5px solid var(--nb-ink);
  border-radius: 16px;
  display: grid;
  place-items: center;
  box-shadow: var(--nb-shadow-sm);
  z-index: 2;
  color: var(--nb-ink);
}

.float-chip--pink {
  top: 18px;
  right: 18px;
  background: var(--nb-pink);
}

.float-chip--purple {
  left: 8px;
  top: 42%;
  background: var(--nb-purple);
}

.float-chip--green {
  right: 28px;
  bottom: 28px;
  background: #bbf7d0;
}

.student-home__body {
  position: relative;
  z-index: 1;
}

.portal-stack {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 28px 16px 0;
  display: grid;
  gap: 16px;
}

.section-eyebrow {
  margin: 0 0 8px;
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--nb-green-deep);
}

.value-section {
  padding: 56px 16px 32px;
  background: #fff;
  border-top: 2.5px solid var(--nb-ink);
}

.value-section__inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.value-section__head {
  max-width: 40rem;
  margin-bottom: 28px;
}

.value-section__head h2 {
  margin: 0 0 10px;
  font-family: var(--font-heading);
  font-size: clamp(1.6rem, 3vw, 2rem);
  font-weight: 900;
  color: var(--nb-ink);
}

.value-section__head p {
  margin: 0;
  color: var(--nb-muted);
  line-height: 1.7;
}

.value-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.value-item {
  padding: 18px;
  background: var(--nb-cream);
  border: 2.5px solid var(--nb-ink);
  border-radius: 18px;
  box-shadow: var(--nb-shadow);
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.value-item:hover {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 0 var(--nb-ink);
}

.value-item__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-bottom: 14px;
  border: 2.5px solid var(--nb-ink);
  border-radius: 14px;
  background: #fff;
  color: var(--nb-ink);
}

.value-item--green .value-item__icon { background: #bbf7d0; }
.value-item--blue .value-item__icon { background: var(--nb-blue); }
.value-item--yellow .value-item__icon { background: var(--nb-yellow); }
.value-item--pink .value-item__icon { background: var(--nb-pink); }

.value-item h3 {
  margin: 0 0 8px;
  font-family: var(--font-heading);
  font-size: 1.08rem;
  font-weight: 800;
  color: var(--nb-ink);
}

.value-item p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.65;
  color: var(--nb-muted);
}

.banner-followup {
  padding: 40px 16px 16px;
  background: var(--nb-cream);
}

.banner-followup__inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.banner-followup__head {
  margin-bottom: 20px;
}

.cta-band {
  margin-top: 8px;
  padding: 64px 16px 72px;
  background: var(--nb-sky, #bae6fd);
  border-top: 2.5px solid var(--nb-ink);
  color: var(--nb-ink);
}

.cta-band__card {
  max-width: 720px;
  margin: 0 auto;
  text-align: center;
  padding: 40px 32px 36px;
  background: #fff;
  border: 2.5px solid var(--nb-ink);
  border-radius: 28px;
  box-shadow: var(--nb-shadow-lg, 6px 6px 0 0 var(--nb-ink));
}

.cta-band h2 {
  margin: 0 0 10px;
  font-family: var(--font-heading);
  font-size: clamp(1.6rem, 3vw, 2.1rem);
  font-weight: 900;
  color: var(--nb-ink);
}

.cta-band p {
  margin: 0 0 24px;
  color: var(--nb-muted);
  font-size: 1rem;
  line-height: 1.6;
}

.cta-band__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.cta-band__perks {
  list-style: none;
  margin: 22px 0 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
  justify-content: center;
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--nb-muted);
}

.cta-band__perks li {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.cta-band__perks li::before {
  content: '✓';
  color: var(--nb-green-deep);
  font-weight: 900;
}

.cta-band .nb-btn--primary {
  background: var(--nb-green);
  color: #fff;
}

.cta-band .nb-btn--primary:hover {
  background: var(--nb-green-deep);
}

@media (max-width: 960px) {
  .home-hero__inner {
    grid-template-columns: 1fr;
  }

  .home-hero__stage {
    order: -1;
    min-height: auto;
  }

  .value-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .float-chip--purple {
    left: 0;
  }
}

@media (max-width: 560px) {
  .value-grid,
  .hero-stats {
    grid-template-columns: 1fr;
  }

  .home-hero__actions .nb-btn,
  .cta-band__actions .nb-btn {
    width: 100%;
  }

  .course-card {
    width: 100%;
    box-shadow: 5px 5px 0 0 var(--nb-ink);
  }
}

@media (prefers-reduced-motion: reduce) {
  .nb-btn,
  .value-item {
    transition: none;
  }
}
</style>
