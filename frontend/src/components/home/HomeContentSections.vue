<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchHomeData } from '@/api/home'
import type { HomeData } from '@/api/home'
import type { SearchItem } from '@/api/search'
import { homeFallbackData, normalizeHomeData } from '@/config/home-fallback'
import HomeSectionHeader from '@/components/home/HomeSectionHeader.vue'
import UiIcon from '@/components/ui/UiIcon.vue'

const loading = ref(true)
const homeData = ref<HomeData | null>(null)
const loadError = ref('')

const NEWS_SLOT_COUNT = 6
const JOB_SLOT_COUNT = 6

interface DisplayRow extends SearchItem {
  isPlaceholder?: boolean
}

const modulePaths: Record<string, string> = {
  course: '/resources',
  credit: '/credit',
  activity: '/news?type=activity',
  news: '/news?type=policy',
  job: '/news?type=job',
  forum: '/forum',
  partner: '/enterprise',
}

function showCoverImage(url?: string) {
  if (!url) return false
  return !/\.(pdf|zip|doc|docx|ppt|pptx|xlsx)(\?.*)?$/i.test(url)
}

/** 普通条目跳转 */
function itemPath(item: SearchItem) {
  switch (item.type) {
    case 'credit':
      return `/credit/products/${item.id}`
    case 'course':
      return item.id > 0 ? `/resources/${item.id}` : '/resources'
    case 'partner':
      return `/enterprise/${item.id}`
    case 'activity':
      return { path: '/news', query: { type: 'activity', id: String(item.id) } }
    case 'job':
      return { path: '/news', query: { type: 'job', id: String(item.id) } }
    case 'news':
      return { path: '/news', query: { type: 'policy', id: String(item.id) } }
    case 'forum':
      return { path: '/forum', query: { id: String(item.id) } }
    default:
      return modulePaths[item.type] ?? '/'
  }
}

/** 微专业：按学习资源技能标签筛选 */
function microMajorPath(item: SearchItem) {
  const tag = item.typeName && item.typeName !== '课程' ? item.typeName : undefined
  return {
    path: '/resources',
    query: {
      ...(tag ? { tag } : {}),
      ...(item.id ? { courseId: String(item.id) } : {}),
    },
  }
}

function formatDate(value?: string) {
  if (!value) return ''
  return value.slice(0, 10)
}

function truncate(text?: string, max = 60) {
  if (!text) return ''
  return text.length > max ? `${text.slice(0, max)}…` : text
}

function padRows(
  items: SearchItem[],
  count: number,
  type: SearchItem['type'],
  emptyTitle: string,
  idOffset: number,
): DisplayRow[] {
  const rows: DisplayRow[] = items.map((item) => ({ ...item, isPlaceholder: false }))
  while (rows.length < count) {
    rows.push({
      type,
      typeName: '',
      id: idOffset - rows.length,
      title: emptyTitle,
      extra: '敬请期待',
      isPlaceholder: true,
    })
  }
  return rows.slice(0, count)
}

const displayNews = computed(() =>
  padRows(homeData.value?.hotNews ?? [], NEWS_SLOT_COUNT, 'news', '更多热点资讯即将发布', -100),
)

const displayJobs = computed(() =>
  padRows(homeData.value?.jobs ?? [], JOB_SLOT_COUNT, 'job', '更多招聘岗位即将发布', -200),
)

const featuredCourse = computed(() => homeData.value?.courses?.[0] ?? null)
const courseList = computed(() => (homeData.value?.courses ?? []).slice(1, 5))
const productShelf = computed(() => (homeData.value?.hotProducts ?? []).slice(0, 6))

function newsSubText(item: DisplayRow) {
  if (item.isPlaceholder) return item.extra ?? ''
  const date = formatDate(item.createTime)
  return date ? `${item.extra ?? ''} · ${date}` : (item.extra ?? '')
}

async function loadHomeData() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await fetchHomeData()
    if (res.code === 200 && res.data) {
      homeData.value = normalizeHomeData(res.data)
      return
    }
    throw new Error(res.message || '加载首页数据失败')
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : '加载首页数据失败'
    homeData.value = homeFallbackData
  } finally {
    loading.value = false
  }
}

onMounted(loadHomeData)
</script>

<template>
  <section class="home-content-sections">
    <div class="section-inner">
      <el-skeleton v-if="loading" :rows="12" animated />

      <template v-else-if="homeData">
        <el-alert
          v-if="loadError"
          :title="loadError"
          type="warning"
          show-icon
          :closable="false"
          class="load-alert"
        >
          <template #default>
            <span>当前展示示例数据，请确认后端已启动。</span>
            <el-button link type="primary" @click="loadHomeData">重新加载</el-button>
          </template>
        </el-alert>
        <!-- 热门课程：大图精选 + 侧栏列表 -->
        <section class="home-block">
          <HomeSectionHeader title="热门课程" icon="course" more-to="/resources" />
          <el-empty v-if="!homeData.courses.length" description="暂无课程" :image-size="64" />
          <div v-else class="course-showcase">
            <router-link
              v-if="featuredCourse"
              :to="itemPath(featuredCourse)"
              class="course-feature"
            >
              <div class="course-feature__cover">
                <img
                  v-if="showCoverImage(featuredCourse.coverUrl)"
                  :src="featuredCourse.coverUrl"
                  :alt="featuredCourse.title"
                  loading="lazy"
                />
                <div v-else class="cover-placeholder course-placeholder">
                  <UiIcon name="course" :size="36" />
                </div>
                <span class="play-badge" aria-hidden="true">▶</span>
              </div>
              <div class="course-feature__body">
                <span class="course-kicker">本周精选</span>
                <h3>{{ featuredCourse.title }}</h3>
                <p v-if="featuredCourse.summary">{{ truncate(featuredCourse.summary, 90) }}</p>
                <div class="course-feature__meta">
                  <span v-if="featuredCourse.extra">{{ featuredCourse.extra }}</span>
                  <em>进入学习</em>
                </div>
              </div>
            </router-link>

            <div class="course-rail">
              <router-link
                v-for="(item, index) in courseList"
                :key="`course-${item.id}`"
                :to="itemPath(item)"
                class="course-rail-item"
              >
                <span class="course-rail-index">{{ String(index + 2).padStart(2, '0') }}</span>
                <div class="course-rail-thumb">
                  <img
                    v-if="showCoverImage(item.coverUrl)"
                    :src="item.coverUrl"
                    :alt="item.title"
                    loading="lazy"
                  />
                  <UiIcon v-else name="course" :size="20" />
                </div>
                <div class="course-rail-copy">
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.extra || truncate(item.summary, 28) }}</p>
                </div>
              </router-link>
            </div>
          </div>
        </section>

        <!-- 热卖商品：价签货架 -->
        <section class="home-block">
          <HomeSectionHeader title="热卖商品" icon="hot" more-to="/credit" />
          <el-empty v-if="!homeData.hotProducts.length" description="暂无商品" :image-size="64" />
          <div v-else class="product-shelf">
            <router-link
              v-for="(item, index) in productShelf"
              :key="`product-${item.id}`"
              :to="itemPath(item)"
              class="product-tile"
            >
              <span class="product-rank" :class="{ hot: index < 3 }">{{ index + 1 }}</span>
              <div class="product-tile__cover">
                <img
                  v-if="showCoverImage(item.coverUrl)"
                  :src="item.coverUrl"
                  :alt="item.title"
                  loading="lazy"
                />
                <div v-else class="cover-placeholder product-placeholder">
                  <UiIcon name="gift" :size="28" />
                </div>
              </div>
              <h3>{{ item.title }}</h3>
              <div class="product-tile__price">
                <strong>{{ item.extra || '秩点兑换' }}</strong>
                <span>去兑换</span>
              </div>
            </router-link>
          </div>
        </section>

        <!-- 热门活动 -->
        <section class="home-block">
          <HomeSectionHeader title="热门活动" icon="activity" more-to="/news?type=activity" />
          <el-empty v-if="!homeData.hotActivities.length" description="暂无活动" :image-size="64" />
          <div v-else class="card-grid card-grid--3">
            <router-link
              v-for="item in homeData.hotActivities"
              :key="`activity-${item.id}`"
              :to="itemPath(item)"
              class="activity-card"
            >
              <div class="activity-icon"><UiIcon name="activity" :size="28" /></div>
              <div class="activity-body">
                <h3 class="activity-title">{{ item.title }}</h3>
                <p v-if="item.extra" class="activity-meta">{{ item.extra }}</p>
                <p v-if="item.createTime" class="activity-date">{{ formatDate(item.createTime) }}</p>
              </div>
            </router-link>
          </div>
        </section>

        <!-- 微专业（按学习资源技能标签聚合） -->
        <section class="home-block">
          <HomeSectionHeader title="微专业" icon="school" more-to="/resources" />
          <el-empty v-if="!homeData.microMajors.length" description="暂无微专业" :image-size="64" />
          <div v-else class="card-grid card-grid--3">
            <router-link
              v-for="item in homeData.microMajors"
              :key="`micro-${item.typeName || item.id}`"
              :to="microMajorPath(item)"
              class="micro-card"
            >
              <div class="micro-cover">
                <img
                  v-if="showCoverImage(item.coverUrl)"
                  :src="item.coverUrl"
                  :alt="item.title"
                  loading="lazy"
                />
                <div v-else class="cover-placeholder">
                  <UiIcon name="school" :size="36" />
                </div>
              </div>
              <div class="micro-body">
                <p v-if="item.typeName" class="micro-tag">{{ item.typeName }}</p>
                <h3 class="micro-title">{{ item.title }}</h3>
                <p v-if="item.summary" class="micro-summary">{{ truncate(item.summary, 48) }}</p>
                <p v-if="item.extra" class="grid-meta">{{ item.extra }}</p>
              </div>
            </router-link>
          </div>
        </section>

        <!-- 最新热点 + 招聘信息 -->
        <div class="dual-row dual-row--ghost">
          <section class="dual-panel">
            <HomeSectionHeader title="最新热点" icon="news" more-to="/news?type=policy" />
            <ul class="split-list">
              <li v-for="item in displayNews" :key="`news-${item.id}`">
                <router-link
                  v-if="!item.isPlaceholder"
                  :to="itemPath(item)"
                  class="split-item"
                >
                  <span class="text-dot" />
                  <div class="text-body">
                    <p class="text-title">{{ item.title }}</p>
                    <p class="text-sub">{{ newsSubText(item) }}</p>
                  </div>
                </router-link>
                <div v-else class="split-item is-placeholder">
                  <span class="text-dot is-dim" />
                  <div class="text-body">
                    <p class="text-title">{{ item.title }}</p>
                    <p class="text-sub">{{ item.extra }}</p>
                  </div>
                </div>
              </li>
            </ul>
          </section>

          <section class="dual-panel">
            <HomeSectionHeader title="招聘信息" icon="job" more-to="/news?type=job" />
            <ul class="split-list">
              <li v-for="item in displayJobs" :key="`job-${item.id}`">
                <router-link
                  v-if="!item.isPlaceholder"
                  :to="itemPath(item)"
                  class="split-item"
                >
                  <span class="text-dot job" />
                  <div class="text-body">
                    <p class="text-title">{{ item.title }}</p>
                    <p class="text-sub">{{ item.extra }}</p>
                  </div>
                </router-link>
                <div v-else class="split-item is-placeholder">
                  <span class="text-dot job is-dim" />
                  <div class="text-body">
                    <p class="text-title">{{ item.title }}</p>
                    <p class="text-sub">{{ item.extra }}</p>
                  </div>
                </div>
              </li>
            </ul>
          </section>
        </div>

        <!-- 大家都在聊 -->
        <section class="home-block">
          <HomeSectionHeader title="大家都在聊" icon="forum" more-to="/forum" />
          <el-empty v-if="!homeData.forumPosts.length" description="暂无帖子" :image-size="64" />
          <div v-else class="forum-list">
            <router-link
              v-for="item in homeData.forumPosts"
              :key="`forum-${item.id}`"
              :to="itemPath(item)"
              class="forum-item"
            >
              <span class="forum-tag">热帖</span>
              <div class="forum-body">
                <h3 class="forum-title">{{ item.title }}</h3>
                <p v-if="item.summary" class="forum-summary">{{ truncate(item.summary, 72) }}</p>
              </div>
              <span v-if="item.extra" class="forum-extra">{{ item.extra }}</span>
            </router-link>
          </div>
        </section>

        <!-- 合作单位 -->
        <section class="home-block">
          <HomeSectionHeader title="合作单位" icon="enterprise" more-to="/enterprise" />
          <el-empty v-if="!homeData.partners.length" description="暂无合作单位" :image-size="64" />
          <div v-else class="partner-grid">
            <router-link
              v-for="item in homeData.partners"
              :key="`partner-${item.id}`"
              :to="itemPath(item)"
              class="partner-card"
            >
              <div class="partner-logo">
                <img
                  v-if="showCoverImage(item.coverUrl)"
                  :src="item.coverUrl"
                  :alt="item.title"
                />
                <span v-else>{{ item.title.slice(0, 1) }}</span>
              </div>
              <p class="partner-name">{{ item.title }}</p>
            </router-link>
          </div>
        </section>
      </template>
    </div>
  </section>
</template>

<style scoped>
.home-content-sections {
  margin-top: 32px;
  padding: 32px 0 48px;
  background: transparent;
  position: relative;
  z-index: 1;
}

.home-content-sections :deep(.header-title) {
  color: #fff;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.35);
}

.home-content-sections :deep(.header-more) {
  color: rgba(255, 255, 255, 0.92);
}

.home-content-sections :deep(.header-more:hover) {
  color: #fff;
}

.load-alert {
  margin-bottom: 20px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 16px;
}

.home-block {
  margin-bottom: 36px;
}

/* —— 热门课程：大图精选 + 列表 —— */
.course-showcase {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.9fr);
  gap: 16px;
  align-items: stretch;
}

.course-feature {
  display: grid;
  grid-template-rows: minmax(200px, 1fr) auto;
  border-radius: 18px;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  background: rgba(8, 30, 48, 0.55);
  border: 1px solid rgba(103, 232, 249, 0.22);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.22);
  transition: transform 0.18s, border-color 0.18s;
}

.course-feature:hover {
  transform: translateY(-2px);
  border-color: rgba(103, 232, 249, 0.45);
}

.course-feature__cover {
  position: relative;
  min-height: 220px;
  background: #0b1527;
}

.course-feature__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.play-badge {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: rgba(8, 145, 178, 0.88);
  color: #ecfeff;
  font-size: 18px;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.35);
  border: 2px solid rgba(255, 255, 255, 0.35);
}

.course-feature__body {
  padding: 18px 20px 20px;
  color: #e8f8ff;
}

.course-kicker {
  display: inline-block;
  margin-bottom: 8px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(34, 211, 238, 0.18);
  border: 1px solid rgba(103, 232, 249, 0.35);
  color: #67e8f9;
}

.course-feature__body h3 {
  margin: 0 0 8px;
  font-size: 22px;
  line-height: 1.35;
}

.course-feature__body p {
  margin: 0;
  line-height: 1.65;
  color: rgba(232, 248, 255, 0.72);
  font-size: 14px;
}

.course-feature__meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-top: 14px;
  font-size: 13px;
  color: rgba(186, 230, 253, 0.8);
}

.course-feature__meta em {
  font-style: normal;
  color: #67e8f9;
  font-weight: 600;
}

.course-rail {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.course-rail-item {
  display: grid;
  grid-template-columns: 28px 72px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 14px;
  text-decoration: none;
  color: inherit;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  transition: background 0.15s, transform 0.15s;
}

.course-rail-item:hover {
  background: rgba(34, 211, 238, 0.12);
  transform: translateX(2px);
}

.course-rail-index {
  font-size: 13px;
  font-weight: 700;
  color: rgba(103, 232, 249, 0.7);
  font-variant-numeric: tabular-nums;
}

.course-rail-thumb {
  width: 72px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(8, 30, 48, 0.8);
  display: grid;
  place-items: center;
  font-size: 18px;
}

.course-rail-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-rail-copy {
  min-width: 0;
}

.course-rail-copy h4 {
  margin: 0 0 4px;
  font-size: 14px;
  color: #f0f9ff;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-rail-copy p {
  margin: 0;
  font-size: 12px;
  color: rgba(186, 230, 253, 0.65);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-placeholder {
  background: linear-gradient(135deg, #0c4a6e, #0891b2) !important;
}

/* —— 热卖商品：价签货架 —— */
.product-shelf {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.product-tile {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 12px;
  border-radius: 14px;
  text-decoration: none;
  color: inherit;
  background: linear-gradient(180deg, rgba(255, 247, 237, 0.14), rgba(255, 255, 255, 0.08));
  border: 1px solid rgba(251, 191, 36, 0.22);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.16);
  backdrop-filter: blur(10px);
  transition: transform 0.15s, border-color 0.15s;
}

.product-tile:hover {
  transform: translateY(-3px);
  border-color: rgba(251, 191, 36, 0.5);
}

.product-rank {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 1;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  font-size: 11px;
  font-weight: 700;
  color: rgba(255, 247, 237, 0.8);
  background: rgba(15, 23, 42, 0.55);
}

.product-rank.hot {
  color: #431407;
  background: linear-gradient(135deg, #fcd34d, #f59e0b);
}

.product-tile__cover {
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.45);
  margin-bottom: 10px;
}

.product-tile__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.product-placeholder {
  background: linear-gradient(135deg, #78350f, #f59e0b) !important;
}

.product-tile h3 {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.4;
  color: #fff7ed;
  min-height: 36px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-tile__price {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  gap: 6px;
  align-items: baseline;
}

.product-tile__price strong {
  font-size: 13px;
  color: #fbbf24;
  font-weight: 700;
}

.product-tile__price span {
  font-size: 11px;
  color: rgba(253, 230, 138, 0.75);
}

.card-grid {
  display: grid;
  gap: 16px;
}

.card-grid--4 {
  grid-template-columns: repeat(4, 1fr);
}

.card-grid--3 {
  grid-template-columns: repeat(3, 1fr);
}

.grid-card {
  text-decoration: none;
  color: inherit;
  transition: transform 0.15s;
}

.grid-card:hover {
  transform: translateY(-2px);
}

.grid-cover {
  aspect-ratio: 16 / 10;
  border-radius: 8px;
  background: #f0f2f5;
  overflow: hidden;
}

.grid-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef2ff 0%, #f5f7fa 100%);
  font-size: 32px;
}

.grid-title {
  margin: 10px 0 4px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.grid-summary {
  margin: 0 0 4px;
  font-size: 12px;
  line-height: 1.45;
  color: var(--color-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.grid-meta {
  font-size: 12px;
  color: var(--color-text-muted);
}

.grid-meta.highlight {
  color: var(--color-primary);
  font-weight: 500;
}

/* 封面卡片标题在深色粒子背景上 */
.card-grid--media .grid-title {
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.45);
}

.card-grid--media .grid-summary {
  color: rgba(255, 255, 255, 0.68);
}

.card-grid--media .grid-meta {
  color: rgba(255, 255, 255, 0.72);
}

.card-grid--media .grid-meta.highlight {
  color: #7ec8ff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.35);
}

.activity-card {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: var(--color-white);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s, transform 0.15s;
}

.activity-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.activity-icon {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #13c2c218;
  border-radius: 10px;
  font-size: 24px;
}

.activity-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 6px;
  line-height: 1.4;
}

.activity-meta,
.activity-date {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}

.micro-card {
  display: flex;
  flex-direction: column;
  background: var(--color-white);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  text-decoration: none;
  color: inherit;
  transition: transform 0.15s, box-shadow 0.2s;
}

.micro-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.micro-cover {
  aspect-ratio: 16 / 9;
  background: #f0f2f5;
}

.micro-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.micro-body {
  padding: 14px 16px 16px;
}

.micro-tag {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-primary, #2094f3);
}

.micro-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px;
}

.micro-summary {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0 0 8px;
  line-height: 1.5;
}

.dual-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 36px;
}

.dual-row--ghost {
  gap: 32px;
  margin-bottom: 48px;
  min-height: 480px;
}

.dual-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.dual-row--ghost .split-list {
  flex: 1;
  gap: 0;
}

.dual-row--ghost .split-item {
  display: flex;
  gap: 14px;
  min-height: 72px;
  padding: 18px 8px;
  background: transparent;
  box-shadow: none;
  border-radius: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  text-decoration: none;
  color: inherit;
  transition: background 0.2s;
  align-items: flex-start;
}

.dual-row--ghost .split-item:last-child {
  border-bottom: none;
}

.dual-row--ghost .split-item:hover {
  background: rgba(255, 255, 255, 0.06);
  transform: none;
  box-shadow: none;
}

.dual-row--ghost .split-item.is-placeholder {
  cursor: default;
}

.dual-row--ghost .text-title {
  font-size: 16px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.94);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.35);
}

.dual-row--ghost .split-item.is-placeholder .text-title {
  color: rgba(255, 255, 255, 0.38);
  text-shadow: none;
}

.dual-row--ghost .text-sub {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.62);
  margin-top: 6px;
}

.dual-row--ghost .split-item.is-placeholder .text-sub {
  color: rgba(255, 255, 255, 0.28);
}

.dual-row--ghost .text-dot {
  width: 8px;
  height: 8px;
  margin-top: 10px;
  box-shadow: 0 0 6px rgba(32, 148, 243, 0.6);
}

.dual-row--ghost .text-dot.job {
  box-shadow: 0 0 6px rgba(235, 47, 150, 0.55);
}

.dual-row--ghost .text-dot.is-dim {
  background: rgba(255, 255, 255, 0.25);
  box-shadow: none;
}

.split-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.split-item {
  display: flex;
  gap: 10px;
  padding: 16px 18px;
  background: var(--color-white);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s, transform 0.15s;
}

.split-item:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.text-body {
  flex: 1;
  min-width: 0;
}

.text-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  margin-top: 8px;
  flex-shrink: 0;
}

.text-dot.job {
  background: #eb2f96;
}

.text-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.text-sub {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}

.forum-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.forum-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 18px;
  background: var(--color-white);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s;
}

.forum-item:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
}

.forum-tag {
  flex-shrink: 0;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #722ed118;
  color: #722ed1;
  font-weight: 500;
}

.forum-body {
  flex: 1;
  min-width: 0;
}

.forum-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px;
}

.forum-summary {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.5;
}

.forum-extra {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--color-text-muted);
}

.partner-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.partner-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  background: var(--color-white);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  text-decoration: none;
  color: inherit;
  transition: transform 0.15s, box-shadow 0.2s;
}

.partner-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
}

.partner-logo {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.partner-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.partner-name {
  font-size: 13px;
  text-align: center;
  line-height: 1.4;
  margin: 0;
}

@media (max-width: 1024px) {
  .card-grid--4 {
    grid-template-columns: repeat(3, 1fr);
  }

  .product-shelf {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .partner-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .course-showcase {
    grid-template-columns: 1fr;
  }

  .card-grid--4,
  .card-grid--3 {
    grid-template-columns: repeat(2, 1fr);
  }

  .product-shelf {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dual-row {
    grid-template-columns: 1fr;
  }

  .dual-row--ghost {
    min-height: auto;
    gap: 28px;
  }

  .dual-row--ghost .split-item {
    min-height: 64px;
  }

  .partner-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 480px) {
  .card-grid--4,
  .card-grid--3 {
    grid-template-columns: 1fr;
  }

  .product-shelf {
    grid-template-columns: 1fr 1fr;
  }

  .partner-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
