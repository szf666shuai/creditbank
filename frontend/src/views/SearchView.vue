<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchApi } from '@/api/search'
import type { SearchItem } from '@/api/search'
import {
  ALL_SEARCH_TYPE_VALUES,
  allSearchSections,
  gridSearchTypes,
  searchCategories,
} from '@/config/search-categories'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const items = ref<SearchItem[]>([])
const resultsMap = ref<Record<string, SearchItem[]>>({})

const keyword = computed(() => (route.query.q as string) || '')
const searchType = computed(() => (route.query.type as string) || 'all')
const isAllView = computed(() => searchType.value === 'all')
const isGridView = computed(() =>
  ['resource', 'course', 'credit'].includes(searchType.value),
)

const gridColumns = computed(() => {
  if (searchType.value === 'all') return gridSearchTypes
  return gridSearchTypes.filter((c) => c.value === searchType.value)
})

function showCoverImage(url?: string) {
  if (!url) return false
  return !/\.(pdf|zip|doc|docx|ppt|pptx|xlsx)(\?.*)?$/i.test(url)
}

function highlightKeyword(text: string) {
  const kw = keyword.value.trim()
  if (!kw || !text) return text
  const escaped = kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark>$1</mark>')
}

function gridIcon(type: string) {
  if (type === 'resource') return '📄'
  if (type === 'course') return '📚'
  return '🎁'
}

function limitForType(type: string) {
  if (type === 'enterprise') return 4
  if (type === 'news' || type === 'activity') return 6
  if (type === 'job' || type === 'forum') return 8
  return 15
}

async function fetchByTypes(types: string[]) {
  const results = await Promise.all(
    types.map((type) =>
      searchApi({
        q: keyword.value.trim(),
        type,
        limit: limitForType(type),
      }).then((res) => ({
        type,
        items: res.code === 200 && res.data ? res.data.items : [],
      })),
    ),
  )
  const next: Record<string, SearchItem[]> = {}
  for (const { type, items: list } of results) {
    next[type] = list
  }
  return next
}

async function fetchAllResults() {
  if (!isAllView.value) return
  resultsMap.value = await fetchByTypes([...ALL_SEARCH_TYPE_VALUES])
}

async function fetchGridResults() {
  if (!isGridView.value) return
  const types = searchType.value === 'all'
    ? gridSearchTypes.map((c) => c.value)
    : [searchType.value]
  resultsMap.value = await fetchByTypes(types)
}

async function fetchSingleTypeResults() {
  if (isAllView.value || isGridView.value) {
    items.value = []
    return
  }
  const res = await searchApi({
    q: keyword.value.trim(),
    type: searchType.value,
    limit: 20,
  })
  if (res.code !== 200 || !res.data) {
    throw new Error(res.message || '搜索失败')
  }
  items.value = res.data.items
}

async function fetchResults() {
  if (!keyword.value.trim()) return
  loading.value = true
  try {
    if (isAllView.value) {
      await fetchAllResults()
    } else if (isGridView.value) {
      await fetchGridResults()
    } else {
      await fetchSingleTypeResults()
    }
  } catch (e) {
    resultsMap.value = {}
    items.value = []
    ElMessage.error(e instanceof Error ? e.message : '搜索失败')
  } finally {
    loading.value = false
  }
}

function changeType(type: string) {
  router.push({ path: '/search', query: { q: keyword.value, type } })
}

onMounted(fetchResults)
watch(() => [route.query.q, route.query.type], fetchResults)
</script>

<template>
  <div class="search-page">
    <div class="section-inner">
      <div class="agent-slot">
        <div class="agent-slot-inner">
          <span class="agent-slot-icon">🤖</span>
          <div class="agent-slot-text">
            <span class="agent-slot-title">智能搜索助手</span>
            <span class="agent-slot-desc">Agent 模块接入后，将在此提供语义理解、学习路径推荐等能力</span>
          </div>
        </div>
      </div>

      <div class="type-tabs">
        <el-check-tag
          v-for="cat in searchCategories"
          :key="cat.value"
          :checked="searchType === cat.value"
          @click="changeType(cat.value)"
        >
          {{ cat.label }}
        </el-check-tag>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />

      <!-- 全部：分区展示所有分类 -->
      <template v-else-if="isAllView">
        <div class="all-sections">
          <!-- 封面网格：学习资源 / 课程 / 积分商品 -->
          <section
            v-for="col in gridSearchTypes"
            :key="col.value"
            class="block-section"
          >
            <h3 class="block-title">{{ col.label }}</h3>
            <el-empty
              v-if="!resultsMap[col.value]?.length"
              description="暂无结果"
              :image-size="56"
            />
            <div v-else class="card-grid">
              <article
                v-for="item in resultsMap[col.value]"
                :key="`${item.type}-${item.id}`"
                class="grid-card"
              >
                <div class="grid-cover">
                  <img
                    v-if="showCoverImage(item.coverUrl)"
                    :src="item.coverUrl"
                    :alt="item.title"
                    loading="lazy"
                  />
                  <div v-else class="cover-placeholder">
                    <span class="placeholder-icon">{{ gridIcon(col.value) }}</span>
                  </div>
                </div>
                <h4 class="grid-title" v-html="highlightKeyword(item.title)" />
              </article>
            </div>
          </section>

          <!-- 资讯 + 活动：左右并排卡片 -->
          <div class="dual-row">
            <div
              v-for="panel in allSearchSections.dual"
              :key="panel.value"
              class="identity-card"
            >
              <div class="identity-header">
                <span
                  class="identity-icon"
                  :style="{ background: panel.color + '18', color: panel.color }"
                >
                  {{ panel.icon }}
                </span>
                <span class="identity-title">{{ panel.label }}</span>
              </div>
              <el-empty
                v-if="!resultsMap[panel.value]?.length"
                description="暂无结果"
                :image-size="48"
              />
              <ul v-else class="panel-list">
                <li
                  v-for="item in resultsMap[panel.value]"
                  :key="`${item.type}-${item.id}`"
                  class="panel-item"
                >
                  <p class="panel-item-title" v-html="highlightKeyword(item.title)" />
                  <p v-if="item.summary" class="panel-item-summary">{{ item.summary }}</p>
                </li>
              </ul>
            </div>
          </div>

          <!-- 招聘、论坛：上下排列 -->
          <section
            v-for="block in allSearchSections.stack"
            :key="block.value"
            class="block-section stack-section"
          >
            <div class="stack-header">
              <span
                class="stack-icon"
                :style="{ background: block.color + '18', color: block.color }"
              >
                {{ block.icon }}
              </span>
              <h3 class="block-title inline">{{ block.label }}</h3>
            </div>
            <el-empty
              v-if="!resultsMap[block.value]?.length"
              description="暂无结果"
              :image-size="56"
            />
            <div v-else class="stack-list">
              <article
                v-for="item in resultsMap[block.value]"
                :key="`${item.type}-${item.id}`"
                class="stack-card"
              >
                <h4 class="stack-title" v-html="highlightKeyword(item.title)" />
                <p v-if="item.summary" class="stack-summary">{{ item.summary }}</p>
                <span v-if="item.extra" class="stack-extra">{{ item.extra }}</span>
              </article>
            </div>
          </section>

          <!-- 企业：每行两个 -->
          <section class="block-section">
            <div class="stack-header">
              <span
                class="stack-icon"
                :style="{
                  background: allSearchSections.enterprise.color + '18',
                  color: allSearchSections.enterprise.color,
                }"
              >
                {{ allSearchSections.enterprise.icon }}
              </span>
              <h3 class="block-title inline">{{ allSearchSections.enterprise.label }}</h3>
            </div>
            <el-empty
              v-if="!resultsMap.enterprise?.length"
              description="暂无结果"
              :image-size="56"
            />
            <div v-else class="enterprise-grid">
              <article
                v-for="item in resultsMap.enterprise"
                :key="`${item.type}-${item.id}`"
                class="enterprise-card"
              >
                <h4 class="enterprise-title" v-html="highlightKeyword(item.title)" />
                <p v-if="item.summary" class="enterprise-summary">{{ item.summary }}</p>
                <span v-if="item.extra" class="enterprise-code">{{ item.extra }}</span>
              </article>
            </div>
          </section>
        </div>
      </template>

      <!-- 单分类：封面网格 -->
      <template v-else-if="isGridView">
        <div class="featured-sections">
          <section
            v-for="col in gridColumns"
            :key="col.value"
            class="block-section"
          >
            <el-empty
              v-if="!resultsMap[col.value]?.length"
              description="暂无结果"
              :image-size="64"
            />
            <div v-else class="card-grid">
              <article
                v-for="item in resultsMap[col.value]"
                :key="`${item.type}-${item.id}`"
                class="grid-card"
              >
                <div class="grid-cover">
                  <img
                    v-if="showCoverImage(item.coverUrl)"
                    :src="item.coverUrl"
                    :alt="item.title"
                    loading="lazy"
                  />
                  <div v-else class="cover-placeholder">
                    <span class="placeholder-icon">{{ gridIcon(col.value) }}</span>
                  </div>
                </div>
                <h4 class="grid-title" v-html="highlightKeyword(item.title)" />
              </article>
            </div>
          </section>
        </div>
      </template>

      <!-- 其他单分类：列表 -->
      <template v-else>
        <el-empty v-if="!items.length" description="未找到相关内容，换个关键词试试" />
        <div v-else class="result-list">
          <el-card
            v-for="item in items"
            :key="`${item.type}-${item.id}`"
            class="result-card"
            shadow="hover"
          >
            <div class="result-meta">
              <el-tag size="small" type="info">{{ item.typeName }}</el-tag>
              <span v-if="item.extra" class="result-extra">{{ item.extra }}</span>
            </div>
            <h3 class="result-title" v-html="highlightKeyword(item.title)" />
            <p v-if="item.summary" class="result-summary">{{ item.summary }}</p>
          </el-card>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  padding: 32px 16px 48px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.agent-slot {
  margin-bottom: 20px;
  border: 1px dashed var(--color-primary);
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary-light) 0%, #fff 100%);
  min-height: 88px;
}

.agent-slot-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
}

.agent-slot-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.agent-slot-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.agent-slot-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.agent-slot-desc {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0 0 24px;
}

.all-sections,
.featured-sections {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.block-section {
  width: 100%;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
  color: var(--color-text);
}

.block-title.inline {
  margin-bottom: 0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px 14px;
}

.grid-card {
  cursor: pointer;
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
}

.placeholder-icon {
  font-size: 32px;
}

.grid-title {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.5;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.grid-title :deep(mark),
.panel-item-title :deep(mark),
.stack-title :deep(mark),
.enterprise-title :deep(mark),
.result-title :deep(mark) {
  background: none;
  color: var(--color-primary);
  font-weight: 600;
  padding: 0;
}

/* 资讯 + 活动 左右并排 */
.dual-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.identity-card {
  background: var(--color-white);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 16px 18px;
  min-height: 160px;
}

.identity-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.identity-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 16px;
  flex-shrink: 0;
}

.identity-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.panel-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel-item {
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f2f5;
}

.panel-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.panel-item-title {
  font-size: 13px;
  font-weight: 500;
  line-height: 1.5;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.panel-item-summary {
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 招聘 / 论坛 上下排列 */
.stack-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.stack-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 16px;
  flex-shrink: 0;
}

.stack-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stack-card {
  background: var(--color-white);
  border-radius: 8px;
  padding: 14px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stack-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 6px;
}

.stack-summary {
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.stack-extra {
  display: inline-block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-muted);
}

/* 企业：每行两个 */
.enterprise-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.enterprise-card {
  background: var(--color-white);
  border-radius: 8px;
  padding: 16px 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.enterprise-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.enterprise-summary {
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.enterprise-code {
  display: inline-block;
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 2px 8px;
  border-radius: 4px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-card {
  border-radius: 10px;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.result-extra {
  font-size: 12px;
  color: var(--color-text-muted);
}

.result-title {
  font-size: 17px;
  margin-bottom: 8px;
  color: var(--color-text);
}

.result-summary {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@media (max-width: 768px) {
  .dual-row {
    grid-template-columns: 1fr;
  }

  .enterprise-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px 10px;
  }
}
</style>
