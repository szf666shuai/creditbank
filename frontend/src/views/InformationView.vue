<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, OfficeBuilding, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getInformationDetailApi,
  pageInformationApi,
  type InformationDetail,
  type InformationItem,
  type InformationType,
} from '@/api/information'
import { applyJobApi, getOrgParticipationStatusApi, registerActivityApi } from '@/api/enterprise'
import { useAuthStore } from '@/stores/auth'
import { BRAND_NAME, BRAND_SLOGAN } from '@/config/brand'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'
import UiIcon from '@/components/ui/UiIcon.vue'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const hubLoading = ref(false)
const loadError = ref<string | null>(null)
const activeType = ref<InformationType | null>(null)
const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<InformationItem[]>([])
const hotItems = ref<InformationItem[]>([])
const sectionHighlights = ref<Record<InformationType, InformationItem[]>>({
  job: [],
  activity: [],
  policy: [],
})
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<InformationDetail | null>(null)
const actionLoading = ref(false)
const coverMessage = ref('')
const appliedJobIds = ref<number[]>([])
const registeredActivityIds = ref<number[]>([])
const heroIndex = ref(0)
let heroTimer: number | undefined

const typeOrder: InformationType[] = ['job', 'activity', 'policy']

const typeConfig: Record<
  InformationType,
  { label: string; title: string; description: string; icon: string; accent: string }
> = {
  job: {
    label: '招聘信息',
    title: '招聘信息',
    description: '机构正式发布的岗位公告，可在详情中投递简历',
    icon: 'job',
    accent: '#2563eb',
  },
  activity: {
    label: '活动信息',
    title: '活动信息',
    description: '机构正式发布的活动日程，可在详情中报名参加',
    icon: 'activity',
    accent: '#3b82f6',
  },
  policy: {
    label: '政策资讯',
    title: '政策资讯',
    description: '平台与官方政策原文，与论坛讨论帖分开阅读',
    icon: 'news',
    accent: '#1d4ed8',
  },
}

const heroSlides = [
  {
    badge: '建立',
    title: '共建共享平台',
    subtitle: '的资源认证标准与交易机制',
  },
  {
    badge: '汇聚',
    title: BRAND_NAME,
    subtitle: '官方招聘 · 活动 · 政策一站发布',
  },
  {
    badge: '互通',
    title: '学习成果互认',
    subtitle: BRAND_SLOGAN,
  },
]

const isHub = computed(() => !activeType.value)

const currentTitle = computed(() =>
  activeType.value ? typeConfig[activeType.value].title : '资讯中心',
)

const currentDescription = computed(() =>
  activeType.value
    ? typeConfig[activeType.value].description
    : '机构与平台发布的正式信息总览',
)

const newsFeed = computed(() => hotItems.value.slice(0, 4))

const currentSlide = computed(() => heroSlides[heroIndex.value] ?? heroSlides[0])

const canApplyCurrentJob = computed(() => {
  return detail.value?.type === 'job' && !appliedJobIds.value.includes(detail.value.id)
})

const canRegisterCurrentActivity = computed(() => {
  return (
    detail.value?.type === 'activity'
    && !registeredActivityIds.value.includes(detail.value.id)
    && (detail.value.status === 1 || detail.value.status === 2)
  )
})

function dateParts(value?: string) {
  const raw = value ? String(value) : ''
  const date = raw.slice(0, 10)
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) {
    return { monthDay: '--', year: '' }
  }
  return {
    monthDay: date.slice(5),
    year: date.slice(0, 4),
  }
}

function itemTime(item: InformationItem) {
  return item.startTime || item.publishTime || item.createTime
}

function rankClass(index: number) {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return 'rank-n'
}

function startHeroTimer() {
  stopHeroTimer()
  heroTimer = window.setInterval(() => {
    heroIndex.value = (heroIndex.value + 1) % heroSlides.length
  }, 4500)
}

function stopHeroTimer() {
  if (heroTimer) {
    window.clearInterval(heroTimer)
    heroTimer = undefined
  }
}

function selectHero(index: number) {
  heroIndex.value = index
  startHeroTimer()
}

async function fetchList() {
  if (!activeType.value) return
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(
      await pageInformationApi(activeType.value, {
        page: page.value,
        pageSize: pageSize.value,
        keyword: keyword.value,
      }),
    )
    list.value = data.records
    total.value = data.total
  } catch (e) {
    loadError.value = getErrorMessage(e, '资讯加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchHubData() {
  hubLoading.value = true
  loadError.value = null
  try {
    const results = await Promise.all(
      typeOrder.map((type) => pageInformationApi(type, { page: 1, pageSize: 6 })),
    )
    const highlights = { ...sectionHighlights.value }
    const merged: InformationItem[] = []
    typeOrder.forEach((type, index) => {
      const records = unwrapApi(results[index]).records
      highlights[type] = records.slice(0, 4)
      merged.push(...records)
    })
    sectionHighlights.value = highlights
    hotItems.value = [...merged]
      .sort((a, b) => String(itemTime(b)).localeCompare(String(itemTime(a))))
      .slice(0, 8)
  } catch (e) {
    loadError.value = getErrorMessage(e, '资讯加载失败')
  } finally {
    hubLoading.value = false
  }
}

async function openDetail(item: Pick<InformationItem, 'type' | 'id'> & Partial<InformationItem>) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  coverMessage.value = ''
  try {
    detail.value = unwrapApi(await getInformationDetailApi(item.type, item.id))
    await fetchParticipationForDetail()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '详情加载失败'))
  } finally {
    detailLoading.value = false
  }
}

async function fetchParticipationForDetail() {
  if (!authStore.isLoggedIn || !authStore.isStudent || !detail.value?.orgId) {
    appliedJobIds.value = []
    registeredActivityIds.value = []
    return
  }
  try {
    const data = unwrapApi(await getOrgParticipationStatusApi(detail.value.orgId))
    appliedJobIds.value = data.appliedJobIds ?? []
    registeredActivityIds.value = data.registeredActivityIds ?? []
  } catch {
    appliedJobIds.value = []
    registeredActivityIds.value = []
  }
}

function ensureStudent() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return false
  }
  if (!authStore.isStudent) {
    ElMessage.warning('仅学员可进行该操作')
    return false
  }
  return true
}

async function handleApply() {
  if (!detail.value || !ensureStudent()) return
  actionLoading.value = true
  try {
    unwrapApi(await applyJobApi(detail.value.id, { coverMessage: coverMessage.value.trim() || undefined }))
    appliedJobIds.value = [...appliedJobIds.value, detail.value.id]
    ElMessage.success('简历已发送')
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '投递失败'))
  } finally {
    actionLoading.value = false
  }
}

async function handleRegister() {
  if (!detail.value || !ensureStudent()) return
  await ElMessageBox.confirm(`确定报名参加「${detail.value.title}」吗？`, '参加活动', {
    type: 'info',
    confirmButtonText: '确认参加',
    cancelButtonText: '取消',
  })
  actionLoading.value = true
  try {
    unwrapApi(await registerActivityApi(detail.value.id))
    registeredActivityIds.value = [...registeredActivityIds.value, detail.value.id]
    ElMessage.success('报名成功')
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '报名失败'))
  } finally {
    actionLoading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function goHub() {
  activeType.value = null
  page.value = 1
  keyword.value = ''
  router.push({ path: '/news' })
}

function openType(type: InformationType) {
  activeType.value = type
  page.value = 1
  keyword.value = ''
  router.push({ path: '/news', query: { type } })
}

function applyTypeFromRoute() {
  const type = route.query.type
  if (type === 'job' || type === 'activity' || type === 'policy') {
    activeType.value = type
  } else {
    activeType.value = null
  }
}

async function refreshCurrentView() {
  if (isHub.value) {
    await fetchHubData()
  } else {
    await fetchList()
  }
}

async function openDetailFromRoute() {
  const idRaw = route.query.id
  const id = typeof idRaw === 'string' ? Number(idRaw) : NaN
  if (!Number.isFinite(id) || id <= 0 || !activeType.value) return
  await openDetail({ type: activeType.value, id, title: '', status: 0 })
}

watch(page, () => {
  if (!isHub.value) fetchList()
})

watch(
  () => [route.query.type, route.query.id] as const,
  async () => {
    applyTypeFromRoute()
    page.value = 1
    await refreshCurrentView()
    await openDetailFromRoute()
  },
)

onMounted(async () => {
  await authStore.initAuth()
  applyTypeFromRoute()
  await refreshCurrentView()
  await openDetailFromRoute()
  startHeroTimer()
})

onUnmounted(stopHeroTimer)
</script>

<template>
  <div class="news-page" v-loading="hubLoading || loading">
    <!-- 总览：科技蓝 Banner + 资讯动态 -->
    <template v-if="isHub">
      <section class="tech-banner">
        <div class="tech-banner__grid" aria-hidden="true" />
        <div class="tech-banner__orbs" aria-hidden="true" />

        <div class="tech-banner__inner">
          <div class="banner-left">
            <div class="banner-slogan">
              <span class="slogan-badge">{{ currentSlide.badge }}</span>
              <div class="slogan-copy">
                <h1>{{ currentSlide.title }}</h1>
                <p>{{ currentSlide.subtitle }}</p>
              </div>
            </div>
            <div class="banner-dots" role="tablist" aria-label="资讯标语轮播">
              <button
                v-for="(_, index) in heroSlides"
                :key="index"
                type="button"
                class="banner-dot"
                :class="{ active: heroIndex === index }"
                :aria-selected="heroIndex === index"
                @click="selectHero(index)"
              />
            </div>
          </div>

          <aside class="news-panel">
            <div class="news-panel__mark" aria-hidden="true" />
            <div class="news-panel__head">
              <h2>资讯动态</h2>
              <button type="button" class="more-link" @click="openType('policy')">
                更多 &gt;
              </button>
            </div>
            <el-empty
              v-if="!hubLoading && newsFeed.length === 0"
              :image-size="56"
              description="暂无资讯"
            />
            <ul v-else class="news-feed">
              <li v-for="(item, index) in newsFeed" :key="`${item.type}-${item.id}`">
                <button type="button" class="news-feed__item" @click="openDetail(item)">
                  <span class="rank" :class="rankClass(index)">{{ index + 1 }}</span>
                  <span class="feed-title">{{ item.title }}</span>
                </button>
              </li>
            </ul>
          </aside>
        </div>
      </section>

      <div class="news-body">
        <el-alert
          v-if="loadError"
          :title="loadError"
          type="warning"
          show-icon
          :closable="false"
          class="news-alert"
        >
          <template #default>
            <el-button link type="primary" @click="refreshCurrentView">重新加载</el-button>
          </template>
        </el-alert>

        <section class="channel-nav">
          <button
            v-for="type in typeOrder"
            :key="type"
            type="button"
            class="channel-pill"
            @click="openType(type)"
          >
            <UiIcon :name="typeConfig[type].icon" :size="18" />
            {{ typeConfig[type].label }}
          </button>
        </section>

        <section class="section-showcase">
          <header class="section-head">
            <div>
              <p class="eyebrow">Channels</p>
              <h2>栏目精选</h2>
            </div>
            <p>点击条目查看详情，或进入栏目浏览全部官方发布信息。</p>
          </header>

          <div
            v-for="type in typeOrder"
            :key="type"
            class="type-block"
            :style="{ '--type-accent': typeConfig[type].accent }"
          >
            <div class="type-block__head">
              <div class="type-block__title">
                <span class="type-icon"><UiIcon :name="typeConfig[type].icon" :size="22" /></span>
                <div>
                  <h3>{{ typeConfig[type].title }}</h3>
                  <p>{{ typeConfig[type].description }}</p>
                </div>
              </div>
              <el-button class="more-btn" text @click="openType(type)">更多</el-button>
            </div>

            <el-empty
              v-if="!sectionHighlights[type].length"
              :image-size="56"
              description="该栏目暂无内容"
            />
            <div v-else class="type-list">
              <button
                v-for="(item, index) in sectionHighlights[type]"
                :key="`${item.type}-${item.id}`"
                type="button"
                class="type-row"
                @click="openDetail(item)"
              >
                <span class="type-row__rank" :class="rankClass(index)">{{ index + 1 }}</span>
                <span class="type-row__main">
                  <strong>{{ item.title }}</strong>
                  <small>
                    {{ item.orgName || typeConfig[type].label }}
                    · {{ formatTime(itemTime(item)) }}
                  </small>
                </span>
              </button>
            </div>
          </div>
        </section>
      </div>
    </template>

    <!-- 栏目列表 -->
    <div v-else class="news-body list-mode">
      <section class="list-head">
        <button type="button" class="back-link" @click="goHub">
          <el-icon><ArrowLeft /></el-icon>
          返回资讯总览
        </button>
        <div class="list-head__row">
          <div>
            <p class="eyebrow">Channel</p>
            <h1>{{ currentTitle }}</h1>
            <p class="list-desc">{{ currentDescription }}</p>
          </div>
          <div class="type-tabs">
            <button
              v-for="type in typeOrder"
              :key="type"
              type="button"
              class="type-tab"
              :class="{ active: activeType === type }"
              @click="openType(type)"
            >
              {{ typeConfig[type].label }}
            </button>
          </div>
        </div>
      </section>

      <section class="list-panel">
        <div class="list-toolbar">
          <div class="glass-search glass-search--light">
            <el-input
              v-model="keyword"
              clearable
              :placeholder="`搜索${typeConfig[activeType!].label}`"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button class="glass-search__btn" type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>

        <el-alert
          v-if="loadError"
          :title="loadError"
          type="warning"
          show-icon
          :closable="false"
          class="news-alert"
        >
          <template #default>
            <el-button link type="primary" @click="fetchList">重新加载</el-button>
          </template>
        </el-alert>

        <el-empty v-if="!loading && list.length === 0" :image-size="80" description="暂无资讯" />

        <div class="info-list">
          <article
            v-for="item in list"
            :key="`${item.type}-${item.id}`"
            class="info-card"
            @click="openDetail(item)"
          >
            <div class="info-date">
              <strong>{{ dateParts(itemTime(item)).monthDay }}</strong>
              <small>{{ dateParts(itemTime(item)).year }}</small>
            </div>
            <div class="info-main">
              <div class="info-title-line">
                <h3>{{ item.title }}</h3>
                <el-tag size="small" effect="plain" type="primary">
                  {{ item.statusName || typeConfig[item.type].label }}
                </el-tag>
              </div>
              <p>{{ item.summary || '暂无摘要' }}</p>
              <div class="info-meta">
                <span v-if="item.orgName">
                  <el-icon><OfficeBuilding /></el-icon>
                  {{ item.orgName }}
                </span>
                <span v-if="item.location">{{ item.location }}</span>
                <span v-if="item.tag">{{ item.tag }}</span>
              </div>
            </div>
          </article>
        </div>

        <div v-if="total > 0" class="page-pagination">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            background
          />
        </div>
      </section>
    </div>

    <el-dialog
      v-model="detailVisible"
      class="news-detail-dialog"
      width="720px"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <p class="dialog-kicker">资讯详情</p>
          <h2 class="dialog-title">{{ detail?.title || '资讯详情' }}</h2>
        </div>
      </template>
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detail">
          <div class="detail-tags">
            <span class="detail-chip detail-chip--primary">{{ typeConfig[detail.type].label }}</span>
            <span v-if="detail.statusName" class="detail-chip">{{ detail.statusName }}</span>
            <span v-if="detail.orgName">{{ detail.orgName }}</span>
            <span>{{ formatTime(detail.startTime || detail.publishTime || detail.createTime) }}</span>
          </div>

          <img v-if="detail.coverUrl" :src="detail.coverUrl" :alt="detail.title" class="cover" />

          <section v-if="detail.type === 'job'" class="detail-section">
            <p>{{ detail.description || '暂无职位描述' }}</p>
            <dl class="detail-grid">
              <div v-if="detail.salaryRange">
                <dt>薪资范围</dt>
                <dd>{{ detail.salaryRange }}</dd>
              </div>
              <div v-if="detail.location">
                <dt>工作地点</dt>
                <dd>{{ detail.location }}</dd>
              </div>
              <div v-if="detail.eduRequirement">
                <dt>学历要求</dt>
                <dd>{{ detail.eduRequirement }}</dd>
              </div>
            </dl>
            <h3>任职要求</h3>
            <p>{{ detail.requirements || '暂无要求' }}</p>
            <el-input
              v-model="coverMessage"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="选填求职信，将随简历发送给企业"
            />
          </section>

          <section v-else-if="detail.type === 'activity'" class="detail-section">
            <p>{{ detail.description || '暂无活动详情' }}</p>
            <dl class="detail-grid">
              <div v-if="detail.location">
                <dt>活动地点</dt>
                <dd>{{ detail.location }}</dd>
              </div>
              <div>
                <dt>活动时间</dt>
                <dd>{{ formatTime(detail.startTime) }} - {{ formatTime(detail.endTime) }}</dd>
              </div>
              <div v-if="detail.maxParticipants">
                <dt>人数上限</dt>
                <dd>{{ detail.maxParticipants }} 人</dd>
              </div>
              <div v-if="detail.creditReward">
                <dt>秩点奖励</dt>
                <dd>{{ detail.creditReward }} 秩点</dd>
              </div>
            </dl>
          </section>

          <section v-else class="detail-section policy-content">
            <p>{{ detail.content || '暂无正文' }}</p>
            <div class="policy-source">
              <span v-if="detail.source">来源：{{ detail.source }}</span>
              <span v-if="detail.author">发布：{{ detail.author }}</span>
            </div>
          </section>
        </template>
      </div>
      <template #footer>
        <el-button class="news-btn news-btn--ghost" @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detail?.type === 'job' && canApplyCurrentJob"
          class="news-btn news-btn--primary"
          :loading="actionLoading"
          @click="handleApply"
        >
          发送简历
        </el-button>
        <el-button v-else-if="detail?.type === 'job'" class="news-btn news-btn--done" disabled>
          已投递
        </el-button>
        <el-button
          v-if="detail?.type === 'activity' && canRegisterCurrentActivity"
          class="news-btn news-btn--primary"
          :loading="actionLoading"
          @click="handleRegister"
        >
          参加活动
        </el-button>
        <el-button
          v-else-if="detail?.type === 'activity' && registeredActivityIds.includes(detail.id)"
          class="news-btn news-btn--done"
          disabled
        >
          已报名
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.news-page {
  background: transparent;
  min-height: calc(100vh - var(--header-height));
  padding-bottom: 48px;
  color: var(--nb-ink, #1a202c);
}

.tech-banner {
  position: relative;
  overflow: hidden;
  min-height: 360px;
  margin-bottom: 28px;
  background:
    radial-gradient(circle at 12% 18%, rgba(190, 227, 248, 0.75), transparent 42%),
    radial-gradient(circle at 88% 12%, rgba(253, 164, 175, 0.35), transparent 40%),
    linear-gradient(135deg, #ffffff 0%, #eff6ff 48%, #fff7ed 100%);
  border-bottom: 2px solid var(--nb-ink, #1a202c);
}

.tech-banner__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(26, 32, 44, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(26, 32, 44, 0.05) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.55), transparent 90%);
  pointer-events: none;
}

.tech-banner__orbs {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 8% 82%, rgba(34, 197, 94, 0.18) 0 2px, transparent 3px),
    radial-gradient(circle at 18% 70%, rgba(37, 99, 235, 0.16) 0 1.5px, transparent 2.5px),
    radial-gradient(circle at 78% 78%, rgba(251, 146, 60, 0.18) 0 2px, transparent 3px),
    radial-gradient(circle at 90% 60%, rgba(124, 58, 237, 0.12) 0 1px, transparent 2px);
  pointer-events: none;
}

.tech-banner__inner {
  position: relative;
  z-index: 1;
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 48px 16px;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(300px, 420px);
  gap: 28px;
  align-items: center;
  min-height: 360px;
}

.banner-left {
  color: var(--nb-ink, #1a202c);
  min-height: 220px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 28px;
}

.banner-slogan {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.slogan-badge {
  flex-shrink: 0;
  width: 42px;
  writing-mode: vertical-rl;
  text-orientation: upright;
  letter-spacing: 0.2em;
  display: grid;
  place-items: center;
  padding: 10px 0;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 800;
  color: #ffffff;
  background: #2563eb;
  border: 2px solid var(--nb-ink, #1a202c);
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
}

.slogan-copy h1 {
  margin: 0 0 10px;
  font-size: clamp(34px, 4.5vw, 48px);
  line-height: 1.2;
  font-weight: 900;
  font-family: var(--font-heading);
  color: var(--nb-ink, #1a202c);
}

.slogan-copy p {
  margin: 0;
  font-size: clamp(18px, 2vw, 24px);
  line-height: 1.5;
  color: var(--color-text-secondary, #475569);
  font-weight: 600;
}

.banner-dots {
  display: flex;
  gap: 8px;
  align-items: center;
}

.banner-dot {
  width: 8px;
  height: 8px;
  border: 2px solid var(--nb-ink, #1a202c);
  border-radius: 999px;
  background: #cbd5e1;
  cursor: pointer;
  padding: 0;
  transition: width 0.2s, background 0.2s;
}

.banner-dot.active {
  width: 28px;
  background: #2563eb;
}

.news-panel {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg, 18px);
  padding: 18px 18px 12px;
  background: #ffffff;
  border: 2px solid var(--nb-ink, #1a202c);
  box-shadow: var(--nb-shadow-lg, 6px 6px 0 0 #1a202c);
  min-height: 260px;
}

.news-panel__mark {
  position: absolute;
  right: -20px;
  bottom: -30px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  border: 2px solid rgba(37, 99, 235, 0.18);
  background: radial-gradient(circle at center, rgba(190, 227, 248, 0.55), transparent 62%);
  pointer-events: none;
}

.news-panel__mark::after {
  content: '';
  position: absolute;
  inset: 18%;
  border-radius: 50%;
  border: 2px solid rgba(37, 99, 235, 0.22);
  opacity: 0.9;
}

.news-panel__head {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.news-panel__head h2 {
  margin: 0;
  color: var(--nb-ink, #1a202c);
  font-size: 20px;
  font-weight: 800;
}

.more-link {
  border: none;
  background: transparent;
  color: #1d4ed8;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
}

.more-link:hover {
  color: #1e3a8a;
}

.news-feed {
  position: relative;
  z-index: 1;
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 4px;
}

.news-feed__item {
  width: 100%;
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  padding: 10px 4px;
  border: none;
  background: transparent;
  text-align: left;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.15s;
}

.news-feed__item:hover {
  background: #eff6ff;
}

.rank {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 800;
  color: #fff;
  border: 2px solid var(--nb-ink, #1a202c);
}

.rank-1 { background: #ef4444; }
.rank-2 { background: #f97316; }
.rank-3 { background: #3b82f6; }
.rank-n { background: #94a3b8; }

.feed-title {
  color: var(--nb-ink, #1a202c);
  font-size: 14px;
  line-height: 1.4;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.news-body {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 16px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.14em;
}

.news-alert {
  margin-bottom: 16px;
}

.channel-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 22px;
}

.channel-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 999px;
  border: 2px solid var(--nb-ink, #1a202c);
  background: #ffffff;
  color: var(--nb-ink, #1a202c);
  cursor: pointer;
  font-weight: 700;
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
}

.channel-pill:hover {
  background: #dbeafe;
  transform: translate(-1px, -1px);
}

.section-showcase {
  display: grid;
  gap: 16px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
  color: var(--color-text-secondary, #475569);
  margin-bottom: 4px;
}

.section-head h2 {
  margin: 0;
  color: var(--nb-ink, #1a202c);
  font-size: 28px;
  font-weight: 900;
  font-family: var(--font-heading);
}

.section-head p {
  margin: 0;
  color: var(--color-text-muted, #64748b);
}

.type-block {
  padding: 18px 20px;
  border-radius: var(--radius-lg, 18px);
  background: #ffffff;
  border: 2px solid var(--nb-ink, #1a202c);
  box-shadow: var(--nb-shadow, 4px 4px 0 0 #1a202c);
}

.type-block__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.type-block__title {
  display: flex;
  gap: 12px;
  align-items: center;
}

.type-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 22px;
  background: color-mix(in srgb, var(--type-accent) 22%, #ffffff);
  border: 2px solid var(--nb-ink, #1a202c);
}

.type-block__title h3 {
  margin: 0 0 4px;
  color: var(--nb-ink, #1a202c);
  font-size: 18px;
  font-weight: 800;
}

.type-block__title p {
  margin: 0;
  color: var(--color-text-muted, #64748b);
  font-size: 13px;
}

.more-btn {
  color: #1d4ed8 !important;
  font-weight: 700;
}

.type-list {
  display: grid;
  gap: 4px;
}

.type-row {
  display: grid;
  grid-template-columns: 26px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  width: 100%;
  padding: 12px 8px;
  border: none;
  border-radius: 8px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s;
}

.type-row:hover {
  background: #eff6ff;
}

.type-row__rank {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 800;
  color: #fff;
  border: 2px solid var(--nb-ink, #1a202c);
  background: color-mix(in srgb, var(--type-accent) 85%, #1a202c);
}

.type-row__main {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.type-row__main strong {
  color: var(--nb-ink, #1a202c);
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.type-row__main small {
  color: var(--color-text-muted, #64748b);
  font-size: 12px;
}

.list-mode {
  padding-top: 20px;
}

.list-head {
  margin-bottom: 16px;
  color: var(--nb-ink, #1a202c);
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  border: none;
  background: transparent;
  color: #1d4ed8;
  cursor: pointer;
  font-weight: 700;
}

.list-head__row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
}

.list-head h1 {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 900;
}

.list-desc {
  margin: 0;
  color: var(--color-text-secondary, #475569);
  line-height: 1.6;
}

.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.type-tab {
  border: 2px solid var(--nb-ink, #1a202c);
  background: #ffffff;
  color: var(--nb-ink, #1a202c);
  border-radius: 8px;
  padding: 7px 12px;
  cursor: pointer;
  font-weight: 700;
}

.type-tab.active {
  background: #2563eb;
  border-color: var(--nb-ink, #1a202c);
  color: #fff;
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
}

.list-panel {
  padding: 18px;
  border-radius: var(--radius-lg, 18px);
  background: #ffffff;
  border: 2px solid var(--nb-ink, #1a202c);
  box-shadow: var(--nb-shadow, 4px 4px 0 0 #1a202c);
}

.list-toolbar {
  margin-bottom: 12px;
}

.info-list {
  display: flex;
  flex-direction: column;
}

.info-card {
  display: flex;
  gap: 18px;
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 4px;
  cursor: pointer;
}

.info-card:hover {
  background: #eff6ff;
}

.info-date {
  width: 58px;
  flex-shrink: 0;
  text-align: center;
  padding: 6px 0;
  border-radius: 8px;
  background: #dbeafe;
  color: #1e3a8a;
  border: 2px solid var(--nb-ink, #1a202c);
}

.info-date strong {
  display: block;
  font-size: 16px;
}

.info-date small {
  font-size: 12px;
  color: #1d4ed8;
}

.info-main {
  min-width: 0;
  flex: 1;
}

.info-title-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.info-title-line h3 {
  margin: 0;
  font-size: 16px;
  color: var(--nb-ink, #1a202c);
}

.info-main p {
  margin: 0;
  color: var(--color-text-secondary, #475569);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.info-meta,
.detail-tags,
.policy-source {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  color: var(--color-text-muted, #64748b);
  font-size: 12px;
  margin-top: 10px;
}

.info-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.dialog-head {
  min-width: 0;
  padding-right: 8px;
}

.dialog-kicker {
  margin: 0 0 4px;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #1d4ed8;
  font-weight: 800;
}

.dialog-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.4;
  color: var(--nb-ink, #1a202c);
  font-weight: 800;
}

.detail-panel {
  min-height: 200px;
}

.detail-chip {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  border: 2px solid var(--nb-ink, #1a202c);
  background: #e0f2fe;
  color: #075985;
  font-weight: 700;
}

.detail-chip--primary {
  border-color: var(--nb-ink, #1a202c);
  background: #dbeafe;
  color: #1e3a8a;
}

.cover {
  width: 100%;
  max-height: 260px;
  object-fit: cover;
  border-radius: 12px;
  margin: 16px 0;
  border: 2px solid var(--nb-ink, #1a202c);
}

.detail-section {
  margin-top: 8px;
}

.detail-section p {
  color: var(--color-text-secondary, #475569);
  line-height: 1.8;
  white-space: pre-wrap;
  margin-bottom: 14px;
}

.detail-section h3 {
  font-size: 15px;
  color: var(--nb-ink, #1a202c);
  margin: 16px 0 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  margin: 14px 0;
}

.detail-grid div {
  border: 2px solid var(--nb-ink, #1a202c);
  border-radius: 12px;
  padding: 12px;
  background: #fff9f0;
}

.detail-grid dt {
  color: var(--color-text-muted, #64748b);
  font-size: 12px;
  margin-bottom: 6px;
}

.detail-grid dd {
  color: var(--nb-ink, #1a202c);
  margin: 0;
  font-weight: 700;
}

.policy-content p {
  white-space: pre-wrap;
}

.news-btn {
  --el-button-bg-color: #eff6ff;
  --el-button-border-color: #1a202c;
  --el-button-text-color: #1e3a8a;
  --el-button-hover-bg-color: #dbeafe;
  --el-button-hover-border-color: #1a202c;
  --el-button-hover-text-color: #1e3a8a;
}

.news-btn--primary {
  --el-button-bg-color: #2563eb;
  --el-button-border-color: #1a202c;
  --el-button-text-color: #ffffff;
  --el-button-hover-bg-color: #1d4ed8;
  --el-button-hover-border-color: #1a202c;
}

.news-btn--ghost {
  --el-button-bg-color: #ffffff;
  --el-button-border-color: #1a202c;
  --el-button-text-color: #475569;
}

.news-btn--done {
  --el-button-bg-color: #dcfce7;
  --el-button-border-color: #1a202c;
  --el-button-text-color: #166534;
  --el-button-disabled-bg-color: #dcfce7;
  --el-button-disabled-border-color: #1a202c;
  --el-button-disabled-text-color: #166534;
}

@media (max-width: 960px) {
  .tech-banner__inner,
  .list-head__row,
  .section-head {
    flex-direction: column;
    align-items: stretch;
  }

  .tech-banner__inner {
    display: flex;
  }

  .news-panel {
    width: 100%;
  }
}
</style>

<style>
.news-detail-dialog.el-dialog {
  background: #ffffff;
  border: 2px solid var(--nb-ink, #1a202c);
  border-radius: 16px;
  box-shadow: var(--nb-shadow-lg, 6px 6px 0 0 #1a202c);
}

.news-detail-dialog .el-dialog__header {
  margin-right: 0;
  padding: 16px 20px 12px;
  border-bottom: 2px solid var(--nb-ink, #1a202c);
}

.news-detail-dialog .el-dialog__headerbtn .el-dialog__close {
  color: var(--color-text-muted, #64748b);
}

.news-detail-dialog .el-dialog__body {
  padding: 16px 20px;
  color: var(--nb-ink, #1a202c);
}

.news-detail-dialog .el-dialog__footer {
  padding: 12px 20px 18px;
  border-top: 2px solid var(--nb-ink, #1a202c);
}

.news-detail-dialog .el-textarea__inner {
  background: #fff9f0;
  border: 2px solid var(--nb-ink, #1a202c);
  box-shadow: none;
  color: var(--nb-ink, #1a202c);
  border-radius: 12px;
}

.news-detail-dialog .el-textarea__inner:hover,
.news-detail-dialog .el-textarea__inner:focus {
  border-color: #2563eb;
}

.news-detail-dialog .el-textarea__inner::placeholder {
  color: #94a3b8;
}

.news-detail-dialog .el-input__count {
  background: transparent;
  color: #94a3b8;
}

.news-detail-dialog .el-loading-mask {
  background: rgba(255, 249, 240, 0.7);
}
</style>
