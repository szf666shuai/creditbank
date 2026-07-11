<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchHomeData } from '@/api/home'
import type { HomeData } from '@/api/home'
import type { SearchItem } from '@/api/search'
import { homeFallbackData, normalizeHomeData } from '@/config/home-fallback'
import HomeSectionHeader from '@/components/home/HomeSectionHeader.vue'

const loading = ref(true)
const homeData = ref<HomeData | null>(null)
const loadError = ref('')

const discoveryPaths: Record<string, string> = {
  course: '/resources',
  credit: '/credit',
  activity: '/enterprise',
}

function itemLink(item: SearchItem) {
  if (item.type === 'course' && item.id > 0) return `/resources/${item.id}`
  if (item.type === 'credit' && item.id > 0) return `/credit/products/${item.id}`
  return discoveryPaths[item.type] ?? '/'
}

function showCoverImage(url?: string) {
  if (!url) return false
  return !/\.(pdf|zip|doc|docx|ppt|pptx|xlsx)(\?.*)?$/i.test(url)
}

const courses = computed(() => (homeData.value?.courses ?? []).slice(0, 4))
const activities = computed(() => (homeData.value?.hotActivities ?? []).slice(0, 3))
const products = computed(() => (homeData.value?.hotProducts ?? []).slice(0, 4))

async function loadHomeData() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await fetchHomeData()
    if (res.code !== 200 || !res.data) throw new Error(res.message || '加载失败')
    homeData.value = normalizeHomeData(res.data)
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : '加载失败'
    homeData.value = homeFallbackData
  } finally {
    loading.value = false
  }
}

onMounted(loadHomeData)
</script>

<template>
  <section class="discovery-sections">
    <el-skeleton v-if="loading" :rows="10" animated />

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
          <span>当前展示示例数据。</span>
          <el-button link type="primary" @click="loadHomeData">重新加载</el-button>
        </template>
      </el-alert>

      <section class="discovery-block">
        <HomeSectionHeader title="推荐课程" icon="📚" more-to="/resources" />
        <el-empty v-if="!courses.length" description="暂无课程" :image-size="64" />
        <div v-else class="card-grid card-grid--4">
          <router-link
            v-for="item in courses"
            :key="`course-${item.id}`"
            :to="itemLink(item)"
            class="media-card"
          >
            <div class="media-cover">
              <img
                v-if="showCoverImage(item.coverUrl)"
                :src="item.coverUrl"
                :alt="item.title"
                loading="lazy"
              />
              <span v-else class="cover-fallback">📚</span>
            </div>
            <h3>{{ item.title }}</h3>
            <p v-if="item.extra" class="meta highlight">{{ item.extra }}</p>
          </router-link>
        </div>
      </section>

      <section class="discovery-block">
        <HomeSectionHeader title="热门活动" icon="🎯" more-to="/enterprise" />
        <el-empty v-if="!activities.length" description="暂无活动" :image-size="64" />
        <div v-else class="card-grid card-grid--3">
          <router-link
            v-for="item in activities"
            :key="`activity-${item.id}`"
            :to="itemLink(item)"
            class="activity-card"
          >
            <span class="activity-icon">🎪</span>
            <div>
              <h3>{{ item.title }}</h3>
              <p v-if="item.extra">{{ item.extra }}</p>
            </div>
          </router-link>
        </div>
      </section>

      <section class="discovery-block">
        <HomeSectionHeader title="精选好物" icon="🎁" more-to="/credit" />
        <el-empty v-if="!products.length" description="暂无商品" :image-size="64" />
        <div v-else class="card-grid card-grid--4">
          <router-link
            v-for="item in products"
            :key="`product-${item.id}`"
            :to="itemLink(item)"
            class="media-card"
          >
            <div class="media-cover">
              <img
                v-if="showCoverImage(item.coverUrl)"
                :src="item.coverUrl"
                :alt="item.title"
                loading="lazy"
              />
              <span v-else class="cover-fallback">🎁</span>
            </div>
            <h3>{{ item.title }}</h3>
            <p v-if="item.extra" class="meta highlight">{{ item.extra }}</p>
          </router-link>
        </div>
      </section>
    </template>
  </section>
</template>

<style scoped>
.discovery-sections {
  display: grid;
  gap: 22px;
}

.load-alert {
  margin-bottom: 4px;
}

.discovery-block {
  padding: 20px 22px;
  border-radius: 18px;
  background: var(--role-surface, rgba(6, 22, 38, 0.58));
  border: 1px solid var(--role-border, rgba(0, 161, 214, 0.28));
  box-shadow: var(--role-shadow, 0 12px 40px rgba(0, 0, 0, 0.35));
  backdrop-filter: blur(12px);
}

.discovery-block :deep(.header-title) {
  color: var(--role-primary-dark, #67e8f9);
  text-shadow: none;
}

.discovery-block :deep(.header-more) {
  color: var(--role-primary-soft, #22d3ee);
}

.card-grid {
  display: grid;
  gap: 14px;
}

.card-grid--4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.card-grid--3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.media-card {
  text-decoration: none;
  color: inherit;
  transition: transform 0.15s;
}

.media-card:hover {
  transform: translateY(-2px);
}

.media-cover {
  aspect-ratio: 16 / 10;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(8, 30, 52, 0.8);
}

.media-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 32px;
  background: linear-gradient(135deg, rgba(8, 30, 52, 0.9), rgba(10, 36, 58, 0.8));
}

.media-card h3 {
  margin: 10px 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--role-text, #e8f8ff);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  font-size: 12px;
  color: var(--role-text-muted, #8ec8de);
}

.meta.highlight {
  color: var(--role-primary-soft, #22d3ee);
  font-weight: 600;
}

.activity-card {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  text-decoration: none;
  color: inherit;
  background: var(--role-surface-card, rgba(10, 36, 58, 0.68));
  border: 1px solid rgba(0, 161, 214, 0.16);
  transition: transform 0.15s, box-shadow 0.15s;
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 161, 214, 0.18);
}

.activity-icon {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: rgba(0, 161, 214, 0.16);
  font-size: 22px;
}

.activity-card h3 {
  margin: 0 0 6px;
  font-size: 14px;
  color: var(--role-text, #e8f8ff);
}

.activity-card p {
  margin: 0;
  font-size: 12px;
  color: var(--role-text-muted, #8ec8de);
}

@media (max-width: 900px) {
  .card-grid--4,
  .card-grid--3 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .card-grid--4,
  .card-grid--3 {
    grid-template-columns: 1fr;
  }
}
</style>
