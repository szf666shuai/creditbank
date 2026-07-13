<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check, Collection, Calendar, Lock } from '@element-plus/icons-vue'
import {
  completeLearning,
  fetchCourseDanmaku,
  fetchCourseEpisodes,
  fetchLearningCheckinStatus,
  fetchLearningResource,
  postCourseDanmaku,
  postLearningCheckin,
  purchaseCourse,
  reportLearningProgress,
  startLearning,
  type CourseDanmaku,
  type CourseEpisode,
  type LearningCertificate,
  type LearningCheckin,
  type LearningRecord,
  type LearningResource,
} from '@/api/learning'
import { useAuthStore } from '@/stores/auth'
import CourseVideoPlayer from '@/components/learning/CourseVideoPlayer.vue'
import CourseCommentPanel from '@/components/learning/CourseCommentPanel.vue'
import CoursewarePanel from '@/components/learning/CoursewarePanel.vue'
import CertificatePreview from '@/components/learning/CertificatePreview.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(true)
const actionLoading = ref(false)
const course = ref<LearningResource | null>(null)
const danmakuItems = ref<CourseDanmaku[]>([])
const danmakuEnabled = ref(true)
const activeTab = ref('intro')
const certificateVisible = ref(false)
const currentCertificate = ref<LearningCertificate | null>(null)
const videoPlayerRef = ref<InstanceType<typeof CourseVideoPlayer> | null>(null)

const episodes = ref<CourseEpisode[]>([])
const currentEpisodeId = ref<number | null>(null)
const checkinStatus = ref<LearningCheckin | null>(null)
const checkinLoading = ref(false)
const purchaseLoading = ref(false)

const pendingWatchedSeconds = ref(0)
const lastPlaybackTime = ref<number | null>(null)
const actualVideoDuration = ref(0)
const unlockedPositionSeconds = ref(0)
const positionDirty = ref(false)
const restoringSeek = ref(false)
let lastSeekWarningAt = 0
let progressReportPromise: Promise<void> | null = null

const courseId = computed(() => Number(route.params.courseId))

const needsPurchase = computed(
  () => !!course.value?.paid && !course.value?.purchased,
)

const coursePrice = computed(() => Number(course.value?.priceCredit || 0))

const hasEpisodes = computed(() => episodes.value.length > 0)

const currentEpisode = computed(() => {
  if (!hasEpisodes.value) return null
  return episodes.value.find((item) => item.id === currentEpisodeId.value) || episodes.value[0]
})

const currentVideoUrl = computed(() => {
  if (currentEpisode.value?.videoUrl) return currentEpisode.value.videoUrl
  return course.value?.videoUrl || ''
})

const tagList = computed(() =>
  course.value?.tags ? course.value.tags.split(',').filter(Boolean) : [],
)

const videoAttribution = computed(() => {
  const source = course.value?.videoUrl || ''
  const attributions: Record<string, { author: string; license: string; href?: string }> = {
    '/videos/java.mp4': { author: 'Nick Parlante', license: 'Attribution', href: 'https://commons.wikimedia.org/wiki/File:Pointer_Fun_with_Binky_(Java).ogv' },
    '/videos/cpp.mp4': { author: 'VolodyA! V Anarhist', license: 'Free Art License', href: 'https://commons.wikimedia.org/wiki/File:Programming_Start_with_C%2B%2B_in_Ubuntu.vorb.theo.ogv' },
    '/videos/python.mp4': { author: 'Alex The Analyst', license: 'CC BY 3.0', href: 'https://commons.wikimedia.org/wiki/File:Installing_Jupyter_Notebooks-Anaconda_-_Python_for_Beginners.webm' },
    '/videos/frontend.mp4': { author: 'Google Chrome Developers', license: 'CC BY 3.0', href: 'https://commons.wikimedia.org/wiki/File:Debugging_JavaScript_Chrome_DevTools_101.webm' },
    '/videos/database.mp4': { author: 'Sescott10', license: 'CC BY-SA 4.0', href: 'https://commons.wikimedia.org/wiki/File:MEC_-_MySQL_books_database_overview.webm' },
    '/videos/ai.mp4': { author: 'University of the Netherlands', license: 'CC BY-SA 4.0', href: 'https://commons.wikimedia.org/wiki/File:How_can_we_keep_robots_under_control.webm' },
    '/videos/blockchain.mp4': { author: 'Distribution Wissen SRF', license: 'CC BY-SA 4.0', href: 'https://commons.wikimedia.org/wiki/File:SRF_Wissen_-_Wie_funktioniert_eine_Blockchain%3F.webm' },
    '/videos/spring-boot.mp4': { author: '本项目', license: '项目自有内容' },
  }
  return attributions[source]
})

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
  const duration = actualVideoDuration.value > 0
    ? actualVideoDuration.value
    : Number(item.videoDurationSeconds || 0)
  return Math.ceil(duration * 0.8)
}

function requireLogin() {
  if (authStore.isLoggedIn) return true
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

function applyLearningRecord(record: LearningRecord) {
  if (!course.value) return
  course.value.progress = record.progress
  course.value.watchedSeconds = record.watchedSeconds
  course.value.maxWatchedPositionSeconds = record.maxWatchedPositionSeconds
  course.value.lastPositionSeconds = record.lastPositionSeconds
  course.value.learningStatus = record.status
  if (!hasEpisodes.value) {
    unlockedPositionSeconds.value = Number(record.maxWatchedPositionSeconds || 0)
  }
}

function applyEpisodeProgress(episode?: CourseEpisode | null) {
  if (!episode) return
  unlockedPositionSeconds.value = Number(episode.maxWatchedPositionSeconds || 0)
  lastPlaybackTime.value = null
  pendingWatchedSeconds.value = 0
  actualVideoDuration.value = 0
  positionDirty.value = false
}

async function loadCheckinStatus() {
  if (!authStore.isLoggedIn || needsPurchase.value) return
  const checkinRes = await fetchLearningCheckinStatus(courseId.value)
  if (checkinRes.code === 200 && checkinRes.data) {
    checkinStatus.value = checkinRes.data
  }
}

async function loadEpisodes() {
  if (needsPurchase.value) {
    episodes.value = []
    currentEpisodeId.value = null
    return
  }
  const res = await fetchCourseEpisodes(courseId.value)
  if (res.code !== 200 || !res.data?.length) {
    episodes.value = []
    currentEpisodeId.value = null
    return
  }
  episodes.value = res.data
  const firstIncomplete = res.data.find((item) => Number(item.progress || 0) < 80)
  currentEpisodeId.value = firstIncomplete?.id ?? res.data[0].id
  applyEpisodeProgress(currentEpisode.value)
}

async function switchEpisode(episodeId: number) {
  if (episodeId === currentEpisodeId.value) return
  await flushProgress()
  videoPlayerRef.value?.pause()
  currentEpisodeId.value = episodeId
  applyEpisodeProgress(currentEpisode.value)
}

async function handlePurchase() {
  if (!requireLogin()) return
  purchaseLoading.value = true
  try {
    const res = await purchaseCourse(courseId.value)
    if (res.code !== 200 || !res.data?.purchased) {
      throw new Error(res.message || '购买失败')
    }
    ElMessage.success(
      res.data.balanceAfter === undefined
        ? '购买成功，可以开始学习了'
        : `购买成功，剩余 ${Number(res.data.balanceAfter).toFixed(2)} 秩点`,
    )
    await loadCourse()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '购买失败')
  } finally {
    purchaseLoading.value = false
  }
}

async function handleCheckin() {
  if (!requireLogin()) return
  checkinLoading.value = true
  try {
    const res = await postLearningCheckin(courseId.value)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '打卡失败')
    checkinStatus.value = res.data
    const reward = Number(res.data.creditReward || 0)
    ElMessage.success(reward > 0 ? `打卡成功，获得 ${reward} 秩点` : res.data.message || '打卡成功')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '打卡失败')
  } finally {
    checkinLoading.value = false
  }
}

function updateLiveProgress(currentTime: number) {
  if (!course.value) return
  const duration = actualVideoDuration.value
    || Number(currentEpisode.value?.videoDurationSeconds || course.value.videoDurationSeconds || 0)
  if (duration <= 0) return
  const livePosition = Math.min(duration, Math.max(unlockedPositionSeconds.value, currentTime))
  if (hasEpisodes.value && currentEpisode.value) {
    currentEpisode.value.maxWatchedPositionSeconds = Math.floor(livePosition)
    currentEpisode.value.lastPositionSeconds = Math.floor(currentTime)
    currentEpisode.value.progress = Math.min(100, Math.floor(livePosition * 100 / duration))
    return
  }
  const liveProgress = Math.min(100, Math.floor(livePosition * 100 / duration))
  course.value.maxWatchedPositionSeconds = Math.floor(livePosition)
  course.value.lastPositionSeconds = Math.floor(currentTime)
  course.value.progress = liveProgress
}

async function loadCourse() {
  loading.value = true
  try {
    const res = await fetchLearningResource(courseId.value)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '课程不存在')
    course.value = res.data
    await loadEpisodes()
    const danmakuRes = await fetchCourseDanmaku(courseId.value)
    if (danmakuRes.code === 200 && danmakuRes.data) {
      danmakuItems.value = danmakuRes.data
    }
    if (authStore.isLoggedIn && !needsPurchase.value) {
      const startRes = await startLearning(courseId.value)
      if (startRes.code === 200 && startRes.data) {
        applyLearningRecord(startRes.data)
      }
      await loadCheckinStatus()
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '课程加载失败')
    router.replace('/resources')
  } finally {
    loading.value = false
  }
}

function handleVideoLoaded() {
  const player = videoPlayerRef.value?.getPlayer()
  if (!player || !course.value) return
  actualVideoDuration.value = Number.isFinite(player.duration)
    ? player.duration
    : Number(currentEpisode.value?.videoDurationSeconds || course.value.videoDurationSeconds || 0)
  const savedPosition = hasEpisodes.value
    ? Number(currentEpisode.value?.lastPositionSeconds || 0)
    : Number(course.value.lastPositionSeconds || 0)
  unlockedPositionSeconds.value = Math.min(
    hasEpisodes.value
      ? Number(currentEpisode.value?.maxWatchedPositionSeconds || 0)
      : Number(course.value.maxWatchedPositionSeconds || 0),
    player.duration,
  )
  const maximumPosition = Math.max(0, player.duration - 1)
  player.currentTime = Math.min(savedPosition, unlockedPositionSeconds.value, maximumPosition)
}

function handleVideoPlay() {
  const player = videoPlayerRef.value?.getPlayer()
  lastPlaybackTime.value = player?.currentTime ?? null
}

function handleVideoSeeking(currentTime: number) {
  const player = videoPlayerRef.value?.getPlayer()
  if (!player) return
  if (restoringSeek.value) {
    restoringSeek.value = false
    return
  }
  const maximumAllowedPosition = Math.min(
    unlockedPositionSeconds.value,
    Math.max(0, player.duration - 0.1),
  )
  if (currentTime > maximumAllowedPosition + 0.5) {
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

function handleVideoTimeUpdate(currentTime: number) {
  const player = videoPlayerRef.value?.getPlayer()
  if (!player || player.paused) return
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
  if (!course.value) return
  const player = videoPlayerRef.value?.getPlayer()
  if (!player) return
  const watchedDeltaSeconds = Math.min(30, Math.floor(pendingWatchedSeconds.value))
  if (watchedDeltaSeconds <= 0 && !positionDirty.value) return
  const currentTimeSeconds = Math.min(
    Math.floor(player.currentTime),
    Math.floor(unlockedPositionSeconds.value),
  )
  let reportSucceeded = false
  pendingWatchedSeconds.value -= watchedDeltaSeconds
  positionDirty.value = false
  progressReportPromise = (async () => {
    try {
      const res = await reportLearningProgress(course.value!.id, {
        watchedDeltaSeconds,
        currentTimeSeconds,
        episodeId: hasEpisodes.value ? currentEpisodeId.value ?? undefined : undefined,
      })
      if (res.code !== 200 || !res.data) throw new Error(res.message || '学习进度保存失败')
      applyLearningRecord(res.data)
      if (hasEpisodes.value && currentEpisode.value) {
        const episode = episodes.value.find((item) => item.id === currentEpisodeId.value)
        if (episode) {
          episode.progress = currentEpisode.value.progress
          episode.maxWatchedPositionSeconds = currentEpisode.value.maxWatchedPositionSeconds
          episode.lastPositionSeconds = currentEpisode.value.lastPositionSeconds
        }
      }
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

async function handleSendDanmaku(payload: { content: string; color: string }) {
  if (!requireLogin()) return
  const player = videoPlayerRef.value?.getPlayer()
  const videoTimeSeconds = Number((player?.currentTime || 0).toFixed(2))
  try {
    const res = await postCourseDanmaku(courseId.value, {
      content: payload.content,
      videoTimeSeconds,
      color: payload.color,
    })
    if (res.code !== 200 || !res.data) throw new Error(res.message || '弹幕发送失败')
    danmakuItems.value = [...danmakuItems.value, res.data]
    ElMessage.success('弹幕已发送')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '弹幕发送失败')
  }
}

async function handleComplete() {
  if (!course.value || !requireLogin()) return
  actionLoading.value = true
  try {
    await flushProgress()
    if (Number(course.value.progress || 0) < 80) {
      throw new Error('课程观看进度达到 80% 后才能颁发合格证')
    }
    const confirmed = await ElMessageBox.confirm(
      `《${course.value.title}》已达到合格观看时长，确认完成课程并生成合格证？`,
      '完成学习',
      { type: 'success', confirmButtonText: '颁发合格证', cancelButtonText: '取消' },
    )
    if (confirmed !== 'confirm') return
    const res = await completeLearning(course.value.id)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '完成学习失败')
    currentCertificate.value = res.data.certificate
    certificateVisible.value = true
    ElMessage.success(res.data.creditChange ? '已完成学习并发放奖励秩点' : '已完成学习并生成证书')
    await loadCourse()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e instanceof Error ? e.message : '完成学习失败')
    }
  } finally {
    actionLoading.value = false
  }
}

async function leavePage() {
  videoPlayerRef.value?.pause()
  await flushProgress()
  router.push('/resources')
}

onMounted(() => {
  if (!courseId.value) {
    router.replace('/resources')
    return
  }
  void loadCourse()
})
</script>

<template>
  <div class="course-player-page">
    <div class="section-inner">
      <button type="button" class="back-link" @click="leavePage">
        <el-icon><ArrowLeft /></el-icon>
        返回学习资源
      </button>

      <el-skeleton v-if="loading" :rows="10" animated />

      <template v-else-if="course">
        <header class="player-header">
          <div>
            <p class="eyebrow">专注学习 · 轻松自由</p>
            <h1>{{ course.title }}</h1>
            <div class="header-meta">
              <span>{{ course.orgName || '平台资源' }}</span>
              <span>{{ course.durationHours || 0 }} 学时</span>
              <span>奖励 {{ formatCredit(course.creditReward) }} 秩点</span>
            </div>
          </div>
          <div class="header-tags">
            <el-tag v-for="tag in tagList" :key="tag" effect="plain">{{ tag }}</el-tag>
          </div>
        </header>

        <div class="player-layout">
          <main class="player-main">
            <div v-if="needsPurchase" class="purchase-paywall">
              <div class="paywall-inner">
                <el-icon class="paywall-icon"><Lock /></el-icon>
                <h2>付费课程需购买后观看</h2>
                <p>《{{ course.title }}》为付费课程，支付 {{ formatCredit(coursePrice) }} 秩点即可解锁全部视频与学习内容。</p>
                <div class="paywall-actions">
                  <el-button
                    type="primary"
                    size="large"
                    :loading="purchaseLoading"
                    @click="handlePurchase"
                  >
                    {{ authStore.isLoggedIn ? `支付 ${formatCredit(coursePrice)} 秩点解锁` : '登录后购买' }}
                  </el-button>
                  <el-button size="large" plain @click="router.push('/mall')">去秩点商城充值</el-button>
                </div>
              </div>
            </div>
            <CourseVideoPlayer
              v-else-if="currentVideoUrl"
              ref="videoPlayerRef"
              :key="currentEpisodeId || 'single'"
              :video-url="currentVideoUrl"
              :danmaku-items="danmakuItems"
              :danmaku-enabled="danmakuEnabled"
              :unlocked-position-seconds="unlockedPositionSeconds"
              @loadedmetadata="handleVideoLoaded"
              @play="handleVideoPlay"
              @timeupdate="handleVideoTimeUpdate"
              @seeking="handleVideoSeeking"
              @pause="flushProgress"
              @ended="flushProgress"
              @update:danmaku-enabled="danmakuEnabled = $event"
              @send-danmaku="handleSendDanmaku"
            />
            <el-empty v-else description="该课程暂未配置视频" />

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

            <section class="interaction-card">
              <el-tabs v-model="activeTab" class="player-tabs">
                <el-tab-pane label="简介" name="intro">
                  <div class="intro-panel">
                    <p>{{ course.description }}</p>
                    <div class="intro-highlights">
                      <div>
                        <span>学习模式</span>
                        <strong>视频 + 弹幕 + 讨论</strong>
                      </div>
                      <div>
                        <span>合格标准</span>
                        <strong>连续观看至 {{ formatDuration(requiredWatchSeconds(course)) }}</strong>
                      </div>
                      <div>
                        <span>当前进度</span>
                        <strong>{{ course.progress || 0 }}%</strong>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
                <el-tab-pane v-if="!needsPurchase" label="评论" name="comments">
                  <CourseCommentPanel :course-id="course.id" />
                </el-tab-pane>
                <el-tab-pane v-if="!needsPurchase" label="课件" name="courseware">
                  <CoursewarePanel :course-id="course.id" />
                </el-tab-pane>
              </el-tabs>
            </section>
          </main>

          <aside class="player-sidebar">
            <div v-if="hasEpisodes" class="sidebar-card episode-card">
              <div class="sidebar-title">课程分集</div>
              <button
                v-for="(episode, index) in episodes"
                :key="episode.id"
                type="button"
                class="episode-item"
                :class="{ active: episode.id === currentEpisodeId }"
                @click="switchEpisode(episode.id)"
              >
                <span class="episode-index">第 {{ index + 1 }} 集</span>
                <strong>{{ episode.title }}</strong>
                <el-progress :percentage="episode.progress || 0" :stroke-width="6" :show-text="false" />
              </button>
            </div>

            <div class="sidebar-card">
              <div class="sidebar-title">
                <el-icon><Collection /></el-icon>
                {{ needsPurchase ? '课程解锁' : '学习进度' }}
              </div>
              <template v-if="needsPurchase">
                <el-alert
                  type="warning"
                  :closable="false"
                  show-icon
                  :title="`需支付 ${formatCredit(coursePrice)} 秩点解锁本课程`"
                />
                <p class="sidebar-tip">
                  购买后可观看全部视频、记录学习进度、打卡领奖并申请合格证。
                </p>
                <el-button
                  type="primary"
                  class="complete-btn"
                  :loading="purchaseLoading"
                  @click="handlePurchase"
                >
                  {{ authStore.isLoggedIn ? '立即购买解锁' : '登录后购买' }}
                </el-button>
              </template>
              <template v-else>
              <el-progress
                :percentage="course.progress || 0"
                :status="Number(course.progress || 0) >= 80 ? 'success' : undefined"
                :stroke-width="12"
              />
              <p class="sidebar-tip">
                <template v-if="hasEpisodes && currentEpisode">
                  当前：{{ currentEpisode.title }} ·
                  时长 {{ formatDuration(actualVideoDuration || currentEpisode.videoDurationSeconds) }}
                </template>
                <template v-else>
                  本视频时长 {{ formatDuration(actualVideoDuration || course.videoDurationSeconds) }}，
                  需从开头连续观看至 {{ formatDuration(requiredWatchSeconds(course)) }}。
                </template>
              </p>
              <div class="checkin-box">
                <div class="checkin-meta">
                  <el-icon><Calendar /></el-icon>
                  <span>
                    连续打卡 {{ checkinStatus?.streakDays || 0 }} 天
                    · {{ checkinStatus?.checkedInToday ? '今日已打卡' : '今日未打卡' }}
                  </span>
                </div>
                <el-button
                  type="success"
                  plain
                  :disabled="!!checkinStatus?.checkedInToday"
                  :loading="checkinLoading"
                  @click="handleCheckin"
                >
                  {{ checkinStatus?.checkedInToday ? '今日已打卡' : '学习打卡' }}
                </el-button>
              </div>
              <el-alert
                v-if="Number(course.progress || 0) < 80"
                type="info"
                :closable="false"
                show-icon
                title="纯净学习空间：无广告、无无关推荐，专注课程本身。"
              />
              <el-alert
                v-else
                type="success"
                :closable="false"
                show-icon
                title="已达到课程合格观看时长，可以申请颁发合格证。"
              />
              <el-button
                type="primary"
                class="complete-btn"
                :disabled="!!course.certNo || Number(course.progress || 0) < 80"
                :loading="actionLoading"
                :icon="Check"
                @click="handleComplete"
              >
                {{ course.certNo ? '已颁发合格证' : '完成课程并颁发合格证' }}
              </el-button>
              </template>
            </div>
          </aside>
        </div>
      </template>
    </div>

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
.course-player-page {
  padding: 24px 16px 56px;
  background: transparent;
  min-height: calc(100vh - var(--header-height));
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
  margin-bottom: 18px;
  font-size: 14px;
}

.back-link:hover {
  color: var(--color-primary-dark);
}

.player-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
  padding: 18px 20px;
  border-radius: 16px;
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow, var(--shadow-sm));
}

.eyebrow {
  margin: 0 0 6px;
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.player-header h1 {
  margin: 0 0 10px;
  font-size: clamp(22px, 3vw, 28px);
  line-height: 1.3;
  color: var(--color-foreground);
  font-family: var(--font-heading);
  text-shadow: none;
}

.header-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: var(--color-muted-foreground);
  font-size: 13px;
}

.header-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-content: flex-start;
}

.player-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 18px;
  align-items: start;
}

.player-main {
  display: grid;
  gap: 16px;
  min-width: 0;
}

.video-attribution {
  color: var(--color-muted-foreground);
  font-size: 12px;
}

.video-attribution a {
  margin-left: 6px;
  color: var(--color-primary);
}

.interaction-card {
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  border-radius: 16px;
  padding: 8px 18px 18px;
  box-shadow: var(--nb-shadow, var(--shadow-md));
}

.player-tabs :deep(.el-tabs__item) {
  color: var(--color-muted-foreground);
}

.player-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}

.player-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
}

.player-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: var(--color-border-neutral);
}

.intro-panel p {
  line-height: 1.8;
  color: var(--color-muted-foreground);
}

.intro-highlights {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.intro-highlights div {
  padding: 14px;
  border-radius: 12px;
  background: var(--nb-cream, var(--color-background));
  border: 2px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.intro-highlights span {
  display: block;
  color: var(--color-muted-foreground);
  font-size: 12px;
  margin-bottom: 6px;
}

.intro-highlights strong {
  color: var(--color-foreground);
  font-size: 14px;
}

.player-sidebar {
  position: sticky;
  top: 88px;
  display: grid;
  gap: 12px;
}

.sidebar-card {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 14px;
  background: var(--color-card);
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow, var(--shadow-md));
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-foreground);
  font-family: var(--font-heading);
}

.sidebar-tip {
  margin: 0;
  color: var(--color-muted-foreground);
  font-size: 13px;
  line-height: 1.7;
}

.complete-btn {
  width: 100%;
}

.episode-card {
  margin-bottom: 0;
}

.episode-item {
  display: grid;
  gap: 6px;
  width: 100%;
  padding: 12px;
  border: 2px solid var(--nb-ink, var(--color-border-neutral));
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
  transition: transform 0.12s ease, box-shadow 0.12s ease, background 0.12s ease;
}

.episode-item + .episode-item {
  margin-top: 8px;
}

.episode-item:hover {
  border-color: var(--nb-ink, var(--color-border));
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0 0 var(--nb-ink, #1a202c);
}

.episode-item.active {
  border-color: var(--nb-ink, var(--color-primary));
  background: #bbf7d0;
}

.episode-index {
  color: var(--color-muted-foreground);
  font-size: 12px;
}

.episode-item strong {
  font-size: 14px;
  color: var(--color-foreground);
}

.checkin-box {
  display: grid;
  gap: 10px;
  padding: 12px;
  border-radius: 12px;
  background: var(--color-background);
  border: 1px solid var(--color-border-neutral);
}

.checkin-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-muted-foreground);
  font-size: 13px;
}

.purchase-paywall {
  aspect-ratio: 16 / 9;
  border-radius: 14px;
  background: var(--hero-gradient);
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow, var(--shadow-md));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
}

.paywall-inner {
  max-width: 480px;
  text-align: center;
  color: var(--color-foreground);
}

.paywall-icon {
  font-size: 48px;
  color: var(--color-accent);
  margin-bottom: 16px;
}

.paywall-inner h2 {
  margin: 0 0 12px;
  font-size: 22px;
  font-family: var(--font-heading);
  color: var(--color-foreground);
}

.paywall-inner p {
  margin: 0 0 24px;
  color: var(--color-muted-foreground);
  line-height: 1.7;
  font-size: 14px;
}

.paywall-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.certificate-box {
  text-align: center;
}

.hash {
  margin-top: 12px;
  font-size: 12px;
  color: var(--color-muted-foreground);
  word-break: break-all;
}

@media (max-width: 960px) {
  .player-layout,
  .intro-highlights {
    grid-template-columns: 1fr;
  }

  .player-sidebar {
    position: static;
  }

  .player-header {
    flex-direction: column;
  }
}
</style>
