<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Collection, Search } from '@element-plus/icons-vue'
import {
  completeLearning,
  fetchLearningResources,
  fetchLearningTags,
  reportLearningProgress,
  startLearning,
  type LearningCertificate,
  type LearningRecord,
  type LearningResource,
} from '@/api/learning'
import { useAuthStore } from '@/stores/auth'
import CertificatePreview from '@/components/learning/CertificatePreview.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const actionLoadingId = ref<number | null>(null)
const resources = ref<LearningResource[]>([])
const tags = ref<string[]>([])
const keyword = ref('')
const activeTag = ref('')
const activeResourceGroup = ref('all')
const certificateVisible = ref(false)
const currentCertificate = ref<LearningCertificate | null>(null)
const playerVisible = ref(false)
const currentCourse = ref<LearningResource | null>(null)
const videoPlayer = ref<HTMLVideoElement | null>(null)
const pendingWatchedSeconds = ref(0)
const lastPlaybackTime = ref<number | null>(null)
const actualVideoDuration = ref(0)
const unlockedPositionSeconds = ref(0)
const positionDirty = ref(false)
const restoringSeek = ref(false)
let lastSeekWarningAt = 0
let progressReportPromise: Promise<void> | null = null

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

const videoAttribution = computed(() => {
  const source = currentCourse.value?.videoUrl || ''
  const attributions: Record<string, { author: string; license: string; href?: string }> = {
    '/videos/java.mp4': {
      author: 'Nick Parlante',
      license: 'Attribution',
      href: 'https://commons.wikimedia.org/wiki/File:Pointer_Fun_with_Binky_(Java).ogv',
    },
    '/videos/cpp.mp4': {
      author: 'VolodyA! V Anarhist',
      license: 'Free Art License',
      href: 'https://commons.wikimedia.org/wiki/File:Programming_Start_with_C%2B%2B_in_Ubuntu.vorb.theo.ogv',
    },
    '/videos/python.mp4': {
      author: 'Alex The Analyst',
      license: 'CC BY 3.0',
      href: 'https://commons.wikimedia.org/wiki/File:Installing_Jupyter_Notebooks-Anaconda_-_Python_for_Beginners.webm',
    },
    '/videos/frontend.mp4': {
      author: 'Google Chrome Developers',
      license: 'CC BY 3.0',
      href: 'https://commons.wikimedia.org/wiki/File:Debugging_JavaScript_Chrome_DevTools_101.webm',
    },
    '/videos/database.mp4': {
      author: 'Sescott10',
      license: 'CC BY-SA 4.0',
      href: 'https://commons.wikimedia.org/wiki/File:MEC_-_MySQL_books_database_overview.webm',
    },
    '/videos/ai.mp4': {
      author: 'University of the Netherlands',
      license: 'CC BY-SA 4.0',
      href: 'https://commons.wikimedia.org/wiki/File:How_can_we_keep_robots_under_control.webm',
    },
    '/videos/blockchain.mp4': {
      author: 'Distribution Wissen SRF',
      license: 'CC BY-SA 4.0',
      href: 'https://commons.wikimedia.org/wiki/File:SRF_Wissen_-_Wie_funktioniert_eine_Blockchain%3F.webm',
    },
    '/videos/spring-boot.mp4': {
      author: '本项目',
      license: '项目自有内容',
    },
  }
  return attributions[source]
})

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
  const duration = item.id === currentCourse.value?.id && actualVideoDuration.value > 0
    ? actualVideoDuration.value
    : Number(item.videoDurationSeconds || 0)
  return Math.ceil(duration * 0.8)
}

function updateLiveProgress(currentTime: number) {
  if (!currentCourse.value) return
  const duration = actualVideoDuration.value || Number(currentCourse.value.videoDurationSeconds || 0)
  if (duration <= 0) return
  const livePosition = Math.min(duration, Math.max(unlockedPositionSeconds.value, currentTime))
  const liveProgress = Math.min(100, Math.floor(livePosition * 100 / duration))
  currentCourse.value.maxWatchedPositionSeconds = Math.floor(livePosition)
  currentCourse.value.lastPositionSeconds = Math.floor(currentTime)
  currentCourse.value.progress = liveProgress
  const resource = resources.value.find((item) => item.id === currentCourse.value?.id)
  if (resource) {
    resource.maxWatchedPositionSeconds = Math.floor(livePosition)
    resource.lastPositionSeconds = Math.floor(currentTime)
    resource.progress = liveProgress
  }
}

function requireLogin() {
  if (authStore.isLoggedIn) return true
  router.push({ path: '/login', query: { redirect: '/resources' } })
  return false
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

function applyLearningRecord(courseId: number, record: LearningRecord) {
  const resource = resources.value.find((item) => item.id === courseId)
  if (resource) {
    resource.progress = record.progress
    resource.watchedSeconds = record.watchedSeconds
    resource.maxWatchedPositionSeconds = record.maxWatchedPositionSeconds
    resource.lastPositionSeconds = record.lastPositionSeconds
    resource.learningStatus = record.status
  }
  if (currentCourse.value?.id === courseId) {
    currentCourse.value.progress = record.progress
    currentCourse.value.watchedSeconds = record.watchedSeconds
    currentCourse.value.maxWatchedPositionSeconds = record.maxWatchedPositionSeconds
    currentCourse.value.lastPositionSeconds = record.lastPositionSeconds
    currentCourse.value.learningStatus = record.status
    unlockedPositionSeconds.value = Number(record.maxWatchedPositionSeconds || 0)
  }
}

async function handleWatch(item: LearningResource) {
  if (!requireLogin()) return
  actionLoadingId.value = item.id
  try {
    const res = await startLearning(item.id)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '开始学习失败')
    applyLearningRecord(item.id, res.data)
    currentCourse.value = { ...item, ...resources.value.find((resource) => resource.id === item.id) }
    pendingWatchedSeconds.value = 0
    lastPlaybackTime.value = null
    actualVideoDuration.value = 0
    unlockedPositionSeconds.value = Number(currentCourse.value.maxWatchedPositionSeconds || 0)
    positionDirty.value = false
    restoringSeek.value = false
    playerVisible.value = true
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '开始学习失败')
  } finally {
    actionLoadingId.value = null
  }
}

function handleVideoLoaded() {
  if (!videoPlayer.value || !currentCourse.value) return
  actualVideoDuration.value = Number.isFinite(videoPlayer.value.duration)
    ? videoPlayer.value.duration
    : Number(currentCourse.value.videoDurationSeconds || 0)
  const savedPosition = Number(currentCourse.value.lastPositionSeconds || 0)
  unlockedPositionSeconds.value = Math.min(
    Number(currentCourse.value.maxWatchedPositionSeconds || 0),
    videoPlayer.value.duration,
  )
  const maximumPosition = Math.max(0, videoPlayer.value.duration - 1)
  videoPlayer.value.currentTime = Math.min(savedPosition, unlockedPositionSeconds.value, maximumPosition)
}

function handleVideoPlay() {
  lastPlaybackTime.value = videoPlayer.value?.currentTime ?? null
}

function handleVideoSeeking() {
  const player = videoPlayer.value
  if (!player) return
  if (restoringSeek.value) {
    restoringSeek.value = false
    return
  }
  const maximumAllowedPosition = Math.min(
    unlockedPositionSeconds.value,
    Math.max(0, player.duration - 0.1),
  )
  if (player.currentTime > maximumAllowedPosition + 0.5) {
    restoringSeek.value = true
    player.currentTime = maximumAllowedPosition
    lastPlaybackTime.value = maximumAllowedPosition
    const now = Date.now()
    if (now - lastSeekWarningAt > 1500) {
      ElMessage.warning('未观看区域暂不可跳转，请按顺序完成学习')
      lastSeekWarningAt = now
    }
    return
  }
  lastPlaybackTime.value = null
  positionDirty.value = true
}

function handleVideoTimeUpdate() {
  const player = videoPlayer.value
  if (!player || player.paused) return
  const currentTime = player.currentTime
  if (lastPlaybackTime.value != null) {
    const delta = currentTime - lastPlaybackTime.value
    if (delta > 0 && delta <= 3) {
      pendingWatchedSeconds.value += delta
      unlockedPositionSeconds.value = Math.max(unlockedPositionSeconds.value, currentTime)
      positionDirty.value = true
      updateLiveProgress(currentTime)
    }
  }
  lastPlaybackTime.value = currentTime
  if (pendingWatchedSeconds.value >= 10) {
    void flushProgress()
  }
}

async function flushProgress() {
  if (progressReportPromise) {
    await progressReportPromise
    return flushProgress()
  }
  if (!currentCourse.value || !videoPlayer.value) return
  const watchedDeltaSeconds = Math.min(30, Math.floor(pendingWatchedSeconds.value))
  if (watchedDeltaSeconds <= 0 && !positionDirty.value) return
  const courseId = currentCourse.value.id
  const currentTimeSeconds = Math.min(
    Math.floor(videoPlayer.value.currentTime),
    Math.floor(unlockedPositionSeconds.value),
  )
  let reportSucceeded = false
  pendingWatchedSeconds.value -= watchedDeltaSeconds
  positionDirty.value = false
  progressReportPromise = (async () => {
    try {
      const res = await reportLearningProgress(courseId, {
        watchedDeltaSeconds,
        currentTimeSeconds,
      })
      if (res.code !== 200 || !res.data) throw new Error(res.message || '学习进度保存失败')
      applyLearningRecord(courseId, res.data)
      reportSucceeded = true
    } catch (e) {
      pendingWatchedSeconds.value += watchedDeltaSeconds
      positionDirty.value = true
      ElMessage.error(e instanceof Error ? e.message : '学习进度保存失败')
    }
  })()
  await progressReportPromise
  progressReportPromise = null
  if (reportSucceeded && Math.floor(pendingWatchedSeconds.value) > 0) {
    await flushProgress()
  }
}

async function closePlayer(done: () => void) {
  videoPlayer.value?.pause()
  await flushProgress()
  await loadResources()
  done()
}

async function closePlayerFromFooter() {
  videoPlayer.value?.pause()
  await flushProgress()
  await loadResources()
  playerVisible.value = false
}

async function handleComplete(item: LearningResource) {
  if (!requireLogin()) return
  actionLoadingId.value = item.id
  try {
    if (currentCourse.value?.id === item.id) {
      await flushProgress()
    }
    const effectiveProgress = Math.max(
      Number(item.progress || 0),
      currentCourse.value?.id === item.id ? Number(currentCourse.value.progress || 0) : 0,
    )
    if (effectiveProgress < 80) {
      throw new Error('课程观看进度达到 80% 后才能颁发合格证')
    }
    const confirmed = await ElMessageBox.confirm(
      `《${item.title}》已达到合格观看时长，确认完成课程并生成合格证？`,
      '完成学习',
      { type: 'success', confirmButtonText: '颁发合格证', cancelButtonText: '取消' },
    )
    if (confirmed !== 'confirm') return
    const res = await completeLearning(item.id)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '完成学习失败')
    currentCertificate.value = res.data.certificate
    playerVisible.value = false
    certificateVisible.value = true
    ElMessage.success(res.data.creditChange ? '已完成学习并发放奖励学分' : '已完成学习并生成证书')
    await loadResources()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e instanceof Error ? e.message : '完成学习失败')
    }
  } finally {
    actionLoadingId.value = null
  }
}

function selectTag(tag: string) {
  activeTag.value = activeTag.value === tag ? '' : tag
  loadResources()
}

onMounted(async () => {
  await Promise.all([loadTags(), loadResources()])
  const courseId = Number(route.query.courseId)
  const purchasedCourse = resources.value.find((item) => item.id === courseId)
  if (purchasedCourse) {
    await handleWatch(purchasedCourse)
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
          <p class="subtitle">按 Java、C++ 等技能标签筛选资源，完成后自动生成带唯一编号和二维码的学习证书。</p>
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
          <div class="cover">
            <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.title" loading="lazy" />
            <div v-else class="cover-fallback">
              <el-icon><Collection /></el-icon>
            </div>
            <el-tag v-if="item.certNo" class="status-tag" type="success">已发证</el-tag>
            <el-tag v-else-if="item.purchased" class="status-tag" type="success">已购买</el-tag>
            <el-tag v-else-if="item.paid" class="status-tag" type="warning">付费</el-tag>
            <el-tag v-else class="status-tag" type="info">免费</el-tag>
          </div>
          <div class="card-body">
            <div class="card-meta">
              <span>{{ item.orgName || '平台资源' }}</span>
              <span>{{ item.durationHours || 0 }} 学时</span>
            </div>
            <h2>{{ item.title }}</h2>
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
                <strong>{{ formatCredit(item.creditReward) }} 学分</strong>
              </div>
              <div class="actions">
                <el-button
                  v-if="!item.certNo"
                  :loading="actionLoadingId === item.id"
                  :icon="Collection"
                  @click="handleWatch(item)"
                >
                  {{ item.progress ? '继续学习' : '开始学习' }}
                </el-button>
                <el-button
                  type="primary"
                  :disabled="!!item.certNo || Number(item.progress || 0) < 80"
                  :loading="actionLoadingId === item.id"
                  :icon="Check"
                  @click="handleComplete(item)"
                >
                  {{ item.certNo ? '已完成' : '颁发合格证' }}
                </el-button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </div>

    <el-dialog
      v-model="playerVisible"
      :title="currentCourse ? `正在学习：${currentCourse.title}` : '课程学习'"
      width="860px"
      destroy-on-close
      :before-close="closePlayer"
    >
      <div v-if="currentCourse" class="video-learning-panel">
        <video
          ref="videoPlayer"
          class="course-video"
          :src="currentCourse.videoUrl"
          controls
          controlslist="nodownload"
          preload="metadata"
          @loadedmetadata="handleVideoLoaded"
          @play="handleVideoPlay"
          @timeupdate="handleVideoTimeUpdate"
          @seeking="handleVideoSeeking"
          @pause="flushProgress"
          @ended="flushProgress"
        >
          当前浏览器不支持 HTML5 视频播放。
        </video>
        <div v-if="videoAttribution" class="video-attribution">
          视频来源：{{ videoAttribution.author }} · {{ videoAttribution.license }}
          <a
            v-if="videoAttribution.href"
            :href="videoAttribution.href"
            target="_blank"
            rel="noopener noreferrer"
          >
            查看原始文件与许可
          </a>
        </div>
        <div class="player-progress">
          <div>
            <span>连续观看进度</span>
            <strong>{{ currentCourse.progress || 0 }}%</strong>
          </div>
          <el-progress
            :percentage="currentCourse.progress || 0"
            :status="Number(currentCourse.progress || 0) >= 80 ? 'success' : undefined"
            :stroke-width="10"
          />
          <el-alert
            v-if="Number(currentCourse.progress || 0) < 80"
            type="info"
            :closable="false"
            show-icon
            :title="`本视频时长 ${formatDuration(actualVideoDuration || currentCourse.videoDurationSeconds)}，需从开头连续观看至 ${formatDuration(requiredWatchSeconds(currentCourse))}。只能在已观看的 ${formatDuration(unlockedPositionSeconds)} 内拖动。`"
          />
          <el-alert
            v-else
            type="success"
            :closable="false"
            show-icon
            title="已达到课程合格观看时长，可以申请颁发合格证。"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="closePlayerFromFooter">稍后继续</el-button>
        <el-button
          type="primary"
          :disabled="!currentCourse || Number(currentCourse.progress || 0) < 80 || !!currentCourse.certNo"
          :loading="currentCourse ? actionLoadingId === currentCourse.id : false"
          @click="currentCourse && handleComplete(currentCourse)"
        >
          {{ currentCourse?.certNo ? '已颁发合格证' : '完成课程并颁发合格证' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="certificateVisible" title="学习证书" width="720px">
      <div v-if="currentCertificate" class="certificate-box">
        <CertificatePreview :certificate="currentCertificate" />
        <p class="hash">链上哈希：{{ currentCertificate.blockchainHash }}</p>
      </div>
      <template #footer>
        <el-button @click="certificateVisible = false">关闭</el-button>
        <el-button type="primary" @click="router.push('/archive')">查看个人档案</el-button>
      </template>
    </el-dialog>
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

.learning-progress > div,
.player-progress > div {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.learning-progress strong,
.player-progress strong {
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

.certificate-box {
  text-align: center;
}

.video-learning-panel {
  display: grid;
  gap: 18px;
}

.course-video {
  display: block;
  width: 100%;
  max-height: 520px;
  background: #0b1020;
  border-radius: 10px;
}

.player-progress {
  display: grid;
  gap: 10px;
}

.video-attribution {
  margin-top: 8px;
  color: var(--color-text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.video-attribution a {
  margin-left: 6px;
  color: var(--color-primary);
}

.hash {
  margin-top: 12px;
  font-size: 12px;
  color: var(--color-text-muted);
  word-break: break-all;
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
