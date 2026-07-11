<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Collection, Search } from '@element-plus/icons-vue'
import {
  fetchLearningResources,
  fetchLearningTags,
  type LearningResource,
} from '@/api/learning'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const resources = ref<LearningResource[]>([])
const tags = ref<string[]>([])
const keyword = ref('')
const activeTag = ref('')
const activeResourceGroup = ref('all')

const completedCount = computed(() =>
  resources.value.filter((item) => item.learningStatus === 1 || item.certNo).length,
)

const filteredResources = computed(() => resources.value.filter((item) => {
  if (activeResourceGroup.value === 'purchased') return !!item.purchased
  if (activeResourceGroup.value === 'learned') return !!item.learned
  if (activeResourceGroup.value === 'free') return !item.paid
  if (activeResourceGroup.value === 'paid') return !!item.paid
  return true
}))

const resourceGroups = computed(() => [
  { value: 'all', label: '全部课程', count: resources.value.length },
  { value: 'purchased', label: '已购买', count: resources.value.filter((item) => item.purchased).length },
  { value: 'learned', label: '已学习', count: resources.value.filter((item) => item.learned).length },
  { value: 'free', label: '免费课程', count: resources.value.filter((item) => !item.paid).length },
  { value: 'paid', label: '付费课程', count: resources.value.filter((item) => item.paid).length },
])

function tagList(item: LearningResource) {
  return item.tags ? item.tags.split(',').filter(Boolean) : []
}

function formatCredit(value?: number) {
  return Number(value || 0).toFixed(2)
}

function formatDuration(totalSeconds?: number) {
  const seconds = Math.max(0, Math.ceil(Number(totalSeconds || 0)))
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return minutes ? `${minutes} 分 ${remainingSeconds} 秒` : `${remainingSeconds} 秒`
}

function requiredWatchSeconds(item?: LearningResource | null) {
  if (!item) return 0
  return Math.ceil(Number(item.videoDurationSeconds || 0) * 0.8)
}

async function loadResources() {
  loading.value = true
  try {
    const res = await fetchLearningResources({
      keyword: keyword.value.trim(),
      tag: activeTag.value,
    })
    if (res.code !== 200 || !res.data) throw new Error(res.message || '加载学习资源失败')
    resources.value = res.data
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载学习资源失败')
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  const res = await fetchLearningTags()
  if (res.code === 200 && res.data) {
    tags.value = res.data
  }
}

function openCourse(item: LearningResource) {
  router.push({ name: 'course-player', params: { courseId: item.id } })
}

function selectTag(tag: string) {
  activeTag.value = activeTag.value === tag ? '' : tag
  loadResources()
}

onMounted(async () => {
  await Promise.all([loadTags(), loadResources()])
  const courseId = Number(route.query.courseId)
  if (courseId) {
    router.replace({ name: 'course-player', params: { courseId } })
  }
})
</script>

<template>
  <div class="resources-page">
    <div class="section-inner">
      <section class="toolbar">
        <div>
          <p class="eyebrow">Learning Resources</p>
          <h1>学习资源</h1>
          <p class="subtitle">
            像 B 站一样轻松看视频、发弹幕、聊心得，但没有广告和无关干扰，只专注学习本身。
          </p>
        </div>
        <div class="summary-strip">
          <div>
            <strong>{{ resources.length }}</strong>
            <span>可学资源</span>
          </div>
          <div>
            <strong>{{ completedCount }}</strong>
            <span>已完成</span>
          </div>
        </div>
      </section>

      <section class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索课程、技能或机构"
          clearable
          @keyup.enter="loadResources"
          @clear="loadResources"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Search" @click="loadResources">搜索</el-button>
      </section>

      <div class="tag-row">
        <el-check-tag :checked="!activeTag" @click="selectTag('')">全部</el-check-tag>
        <el-check-tag
          v-for="tag in tags"
          :key="tag"
          :checked="activeTag === tag"
          @click="selectTag(tag)"
        >
          {{ tag }}
        </el-check-tag>
      </div>

      <div class="resource-group-row">
        <el-check-tag
          v-for="group in resourceGroups"
          :key="group.value"
          :checked="activeResourceGroup === group.value"
          @click="activeResourceGroup = group.value"
        >
          {{ group.label }}（{{ group.count }}）
        </el-check-tag>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />
      <el-empty v-else-if="!filteredResources.length" description="该分类下暂无学习资源" />
      <div v-else class="resource-grid">
        <article v-for="item in filteredResources" :key="item.id" class="resource-card">
          <div class="cover" @click="openCourse(item)">
            <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.title" loading="lazy" />
            <div v-else class="cover-fallback">
              <el-icon><Collection /></el-icon>
            </div>
            <el-tag v-if="item.certNo" class="status-tag" type="success">已发证</el-tag>
            <el-tag v-else-if="item.paid && !item.purchased" class="status-tag" type="warning">
              {{ formatCredit(item.priceCredit) }} 秩点
            </el-tag>
            <el-tag v-else-if="item.purchased" class="status-tag" type="success">已购买</el-tag>
            <el-tag v-else-if="item.paid" class="status-tag" type="warning">付费</el-tag>
            <el-tag v-else class="status-tag" type="info">免费</el-tag>
          </div>
          <div class="card-body">
            <div class="card-meta">
              <span>{{ item.orgName || '平台资源' }}</span>
              <span>{{ item.durationHours || 0 }} 学时</span>
            </div>
            <h2 @click="openCourse(item)">{{ item.title }}</h2>
            <p class="desc">{{ item.description }}</p>
            <div class="skill-tags">
              <el-tag v-for="tag in tagList(item)" :key="tag" size="small" effect="plain">
                {{ tag }}
              </el-tag>
            </div>
            <div class="learning-progress">
              <div>
                <span>视频学习进度</span>
                <strong>{{ item.progress || 0 }}%</strong>
              </div>
              <el-progress
                :percentage="item.progress || 0"
                :status="item.certNo ? 'success' : undefined"
                :stroke-width="8"
              />
              <small>
                需有效观看 {{ formatDuration(requiredWatchSeconds(item)) }}（视频总长的 80%）
              </small>
            </div>
            <div class="card-footer">
              <div class="reward">
                <span>奖励</span>
                <strong>{{ formatCredit(item.creditReward) }} 秩点</strong>
              </div>
              <div class="actions">
                <el-button
                  v-if="!item.certNo"
                  :icon="Collection"
                  @click="openCourse(item)"
                >
                  <template v-if="item.paid && !item.purchased">
                    购买解锁 · {{ formatCredit(item.priceCredit) }} 秩点
                  </template>
                  <template v-else>
                    {{ item.progress ? '继续学习' : '开始学习' }}
                  </template>
                </el-button>
                <el-button
                  type="primary"
                  :disabled="!!item.certNo || Number(item.progress || 0) < 80"
                  :icon="Check"
                  @click="openCourse(item)"
                >
                  {{ item.certNo ? '已完成' : '进入学习页' }}
                </el-button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>

<style scoped>
.resources-page {
  padding: 32px 16px 56px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-end;
  margin-bottom: 22px;
}

.eyebrow {
  font-size: 12px;
  letter-spacing: 0;
  color: var(--color-primary);
  font-weight: 700;
  text-transform: uppercase;
}

h1 {
  margin: 6px 0 8px;
  font-size: 30px;
  line-height: 1.2;
  color: var(--color-text);
}

.subtitle {
  color: var(--color-text-secondary);
  line-height: 1.7;
  max-width: 620px;
}

.summary-strip {
  display: flex;
  gap: 10px;
}

.summary-strip div {
  min-width: 96px;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
}

.summary-strip strong {
  display: block;
  font-size: 22px;
  color: var(--color-text);
}

.summary-strip span {
  font-size: 12px;
  color: var(--color-text-muted);
}

.filters {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  margin-bottom: 14px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 22px;
}

.resource-group-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.resource-card {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 420px;
}

.cover {
  position: relative;
  aspect-ratio: 16 / 9;
  background: #eef2f6;
  cursor: pointer;
}

.cover img,
.cover-fallback {
  width: 100%;
  height: 100%;
}

.cover img {
  display: block;
  object-fit: cover;
}

.cover-fallback {
  display: grid;
  place-items: center;
  color: var(--color-primary);
  font-size: 34px;
}

.status-tag {
  position: absolute;
  right: 10px;
  top: 10px;
}

.card-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.resource-card h2 {
  font-size: 18px;
  line-height: 1.45;
  color: var(--color-text);
  margin: 0;
  cursor: pointer;
}

.desc {
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.skill-tags {
  min-height: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.learning-progress {
  display: grid;
  gap: 6px;
}

.learning-progress > div {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.learning-progress strong {
  color: var(--color-primary);
}

.learning-progress small {
  color: var(--color-text-muted);
}

.card-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.reward span {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
}

.reward strong {
  color: #0f8f68;
  font-size: 16px;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .toolbar,
  .card-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .summary-strip {
    width: 100%;
  }
}
</style>
