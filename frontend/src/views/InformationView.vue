<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
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
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

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

const typeOrder: InformationType[] = ['job', 'activity', 'policy']

const typeConfig: Record<
  InformationType,
  { label: string; title: string; description: string; icon: string; accent: string }
> = {
  job: {
    label: '招聘信息',
    title: '招聘信息',
    description: '机构正式发布的岗位公告，可在详情中投递简历',
    icon: '💼',
    accent: '#0d9488',
  },
  activity: {
    label: '活动信息',
    title: '活动信息',
    description: '机构正式发布的活动日程，可在详情中报名参加',
    icon: '🎯',
    accent: '#14b8a6',
  },
  policy: {
    label: '政策资讯',
    title: '政策资讯',
    description: '平台与官方政策原文，与论坛讨论帖分开阅读',
    icon: '📰',
    accent: '#0f766e',
  },
}

const isHub = computed(() => !activeType.value)

const currentTitle = computed(() =>
  activeType.value ? typeConfig[activeType.value].title : '资讯中心',
)

const currentDescription = computed(() =>
  activeType.value
    ? typeConfig[activeType.value].description
    : '机构与平台发布的正式信息总览',
)

const heroFeatured = computed(() => hotItems.value[0] ?? null)
const heroHotList = computed(() => hotItems.value.slice(0, 6))

const totalHighlightCount = computed(() =>
  typeOrder.reduce((sum, type) => sum + (sectionHighlights.value[type]?.length ?? 0), 0),
)

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

function truncate(text?: string, max = 88) {
  if (!text) return ''
  const normalized = text.replace(/\s+/g, ' ').trim()
  return normalized.length > max ? `${normalized.slice(0, max)}…` : normalized
}

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
})
</script>

<template>
  <div class="news-page" v-loading="hubLoading || loading">
    <div class="news-inner">
      <!-- 总览 -->
      <template v-if="isHub">
        <section class="hero-stage">
          <div class="hero-stage__main">
            <div class="hero-stage__grid" aria-hidden="true" />
            <p class="hero-kicker">Official Bulletin</p>
            <h1>资讯中心</h1>
            <p class="hero-lead">
              招聘、活动与政策由机构及平台正式发布。这里是权威信息入口；经验讨论请前往论坛。
            </p>
            <div class="hero-stats">
              <div>
                <strong>{{ typeOrder.length }}</strong>
                <span>资讯栏目</span>
              </div>
              <div>
                <strong>{{ totalHighlightCount }}</strong>
                <span>近期条目</span>
              </div>
              <div>
                <strong>{{ hotItems.length }}</strong>
                <span>热门速览</span>
              </div>
            </div>
            <div class="hero-actions">
              <el-button type="primary" size="large" @click="openType('job')">查看招聘</el-button>
              <el-button size="large" plain @click="openType('activity')">浏览活动</el-button>
              <el-button size="large" text @click="openType('policy')">政策原文</el-button>
            </div>

            <article
              v-if="heroFeatured"
              class="hero-feature"
              @click="openDetail(heroFeatured)"
            >
              <span class="hero-feature__badge">
                {{ typeConfig[heroFeatured.type].label }}
              </span>
              <h2>{{ heroFeatured.title }}</h2>
              <p>{{ truncate(heroFeatured.summary || '点击查看完整内容', 110) }}</p>
              <div class="hero-feature__meta">
                <span v-if="heroFeatured.orgName">{{ heroFeatured.orgName }}</span>
                <span>{{ formatTime(itemTime(heroFeatured)) }}</span>
              </div>
            </article>
          </div>

          <aside class="hero-hot-panel">
            <div class="hero-hot-panel__head">
              <h3>最新速览</h3>
              <span>官方发布</span>
            </div>
            <el-empty
              v-if="!hubLoading && heroHotList.length === 0"
              :image-size="64"
              description="暂无资讯"
            />
            <button
              v-for="(item, index) in heroHotList"
              :key="`${item.type}-${item.id}`"
              type="button"
              class="hot-item"
              @click="openDetail(item)"
            >
              <span class="hot-date">
                <strong>{{ dateParts(itemTime(item)).monthDay }}</strong>
                <small>{{ dateParts(itemTime(item)).year }}</small>
              </span>
              <span class="hot-body">
                <em>{{ typeConfig[item.type].label }}</em>
                <strong>{{ item.title }}</strong>
                <small>{{ item.orgName || item.tag || item.location || '平台发布' }}</small>
              </span>
              <span class="hot-index">{{ String(index + 1).padStart(2, '0') }}</span>
            </button>
          </aside>
        </section>

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

        <section class="section-showcase">
          <header class="section-head">
            <div>
              <p class="eyebrow">Channels</p>
              <h2>栏目精选</h2>
            </div>
            <p>点击「更多」进入栏目列表；也可从导航栏下拉直接跳转。</p>
          </header>

          <div
            v-for="type in typeOrder"
            :key="type"
            class="type-block"
            :style="{ '--type-accent': typeConfig[type].accent }"
          >
            <div class="type-block__head">
              <div class="type-block__title">
                <span class="type-icon">{{ typeConfig[type].icon }}</span>
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
            <div v-else class="type-cards" :class="`type-cards--${type}`">
              <article
                v-for="item in sectionHighlights[type]"
                :key="`${item.type}-${item.id}`"
                class="type-card"
                @click="openDetail(item)"
              >
                <div class="type-card__date">
                  <strong>{{ dateParts(itemTime(item)).monthDay }}</strong>
                  <small>{{ dateParts(itemTime(item)).year }}</small>
                </div>
                <div class="type-card__body">
                  <h4>{{ item.title }}</h4>
                  <p>{{ truncate(item.summary || '暂无摘要', 72) }}</p>
                  <div class="type-card__meta">
                    <span v-if="item.orgName">{{ item.orgName }}</span>
                    <span v-if="item.location">{{ item.location }}</span>
                    <span v-if="item.statusName">{{ item.statusName }}</span>
                  </div>
                </div>
              </article>
            </div>
          </div>
        </section>
      </template>

      <!-- 栏目列表 -->
      <template v-else>
        <section class="list-head">
          <button type="button" class="back-link" @click="goHub">
            <el-icon><ArrowLeft /></el-icon>
            返回资讯总览
          </button>
          <div class="list-head__row">
            <div>
              <p class="eyebrow">{{ typeConfig[activeType!].icon }} Channel</p>
              <h1>{{ currentTitle }}</h1>
              <p class="hero-lead">{{ currentDescription }}</p>
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

          <el-empty
            v-if="!loading && list.length === 0"
            :image-size="80"
            description="暂无资讯"
          />

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
                  <el-tag size="small" effect="plain" type="success">
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
      </template>
    </div>

    <el-dialog v-model="detailVisible" width="720px" destroy-on-close>
      <template #header>
        <span>{{ detail?.title || '资讯详情' }}</span>
      </template>
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detail">
          <div class="detail-tags">
            <el-tag type="success">{{ typeConfig[detail.type].label }}</el-tag>
            <el-tag v-if="detail.statusName" type="info">{{ detail.statusName }}</el-tag>
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
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detail?.type === 'job' && canApplyCurrentJob"
          type="primary"
          :loading="actionLoading"
          @click="handleApply"
        >
          发送简历
        </el-button>
        <el-button v-else-if="detail?.type === 'job'" type="success" disabled>已投递</el-button>
        <el-button
          v-if="detail?.type === 'activity' && canRegisterCurrentActivity"
          type="primary"
          :loading="actionLoading"
          @click="handleRegister"
        >
          参加活动
        </el-button>
        <el-button
          v-else-if="detail?.type === 'activity' && registeredActivityIds.includes(detail.id)"
          type="success"
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
  padding: 20px 16px 56px;
  background: transparent;
  min-height: calc(100vh - var(--header-height));
}

.news-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.hero-stage {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.95fr);
  gap: 18px;
  min-height: 420px;
  margin-bottom: 28px;
}

.hero-stage__main {
  position: relative;
  overflow: hidden;
  border-radius: 8px 28px 8px 28px;
  padding: 36px 36px 28px;
  color: #ecfeff;
  background:
    linear-gradient(160deg, rgba(15, 118, 110, 0.92) 0%, rgba(13, 148, 136, 0.86) 42%, rgba(45, 212, 191, 0.72) 100%);
  border: 1px solid rgba(94, 234, 212, 0.35);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.28);
}

.hero-stage__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.55), transparent 78%);
  pointer-events: none;
}

.hero-kicker,
.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(204, 251, 241, 0.86);
  font-weight: 700;
}

.hero-stage__main h1,
.list-head h1 {
  margin: 0 0 12px;
  font-size: clamp(32px, 4vw, 44px);
  line-height: 1.15;
  letter-spacing: 0.02em;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.28);
}

.hero-lead {
  margin: 0;
  max-width: 540px;
  line-height: 1.75;
  color: rgba(236, 254, 255, 0.88);
}

.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin: 22px 0 18px;
}

.hero-stats strong {
  display: block;
  font-size: 26px;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.hero-stats span {
  font-size: 12px;
  color: rgba(204, 251, 241, 0.72);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 22px;
}

.hero-feature {
  position: relative;
  z-index: 1;
  padding: 18px 20px;
  border-radius: 4px 16px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: transform 0.18s, background 0.18s;
}

.hero-feature:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.18);
}

.hero-feature__badge {
  display: inline-block;
  margin-bottom: 8px;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  background: rgba(45, 212, 191, 0.28);
  border: 1px solid rgba(153, 246, 228, 0.45);
}

.hero-feature h2 {
  margin: 0 0 8px;
  font-size: 20px;
}

.hero-feature p,
.hero-feature__meta {
  margin: 0;
  color: rgba(236, 254, 255, 0.8);
  line-height: 1.6;
}

.hero-feature__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  font-size: 12px;
}

.hero-hot-panel {
  border-radius: 28px 8px 28px 8px;
  padding: 22px 16px;
  background: rgba(8, 28, 36, 0.45);
  border: 1px solid rgba(153, 246, 228, 0.18);
  backdrop-filter: blur(16px);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.22);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hero-hot-panel__head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 8px;
  padding: 0 8px;
}

.hero-hot-panel__head h3 {
  margin: 0;
  color: #ecfeff;
  font-size: 18px;
}

.hero-hot-panel__head span {
  color: rgba(153, 246, 228, 0.58);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.hot-item {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: start;
  width: 100%;
  padding: 12px 10px;
  border: none;
  border-radius: 10px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s;
}

.hot-item:hover {
  background: rgba(45, 212, 191, 0.1);
}

.hot-date {
  text-align: center;
  color: #99f6e4;
}

.hot-date strong {
  display: block;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

.hot-date small {
  font-size: 11px;
  opacity: 0.7;
}

.hot-body {
  min-width: 0;
  display: grid;
  gap: 3px;
}

.hot-body em {
  font-style: normal;
  font-size: 11px;
  color: #5eead4;
  letter-spacing: 0.06em;
}

.hot-body strong {
  color: #ecfeff;
  font-size: 14px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-body small {
  color: rgba(204, 251, 241, 0.55);
  font-size: 12px;
}

.hot-index {
  color: rgba(153, 246, 228, 0.35);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
  padding-top: 2px;
}

.news-alert {
  margin-bottom: 18px;
}

.section-showcase {
  display: grid;
  gap: 18px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
  color: rgba(204, 251, 241, 0.78);
}

.section-head h2 {
  margin: 0;
  color: #ecfeff;
  font-size: 28px;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.35);
}

.section-head p {
  margin: 0;
  max-width: 360px;
  line-height: 1.6;
  font-size: 14px;
}

.type-block {
  padding: 20px 22px;
  border-radius: 6px 20px;
  background: rgba(8, 40, 44, 0.42);
  border: 1px solid rgba(153, 246, 228, 0.16);
  backdrop-filter: blur(12px);
  box-shadow: 0 14px 36px rgba(0, 0, 0, 0.18);
}

.type-block__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.type-block__title {
  display: flex;
  gap: 14px;
  align-items: center;
  min-width: 0;
}

.type-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  font-size: 24px;
  background: color-mix(in srgb, var(--type-accent) 28%, transparent);
  border: 1px solid color-mix(in srgb, var(--type-accent) 45%, transparent);
}

.type-block__title h3 {
  margin: 0 0 4px;
  color: #ecfeff;
  font-size: 20px;
}

.type-block__title p {
  margin: 0;
  color: rgba(204, 251, 241, 0.68);
  font-size: 13px;
}

.more-btn {
  color: #5eead4 !important;
  font-weight: 600;
}

.type-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.type-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  border-radius: 4px 14px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(153, 246, 228, 0.35);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}

.type-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.16);
}

.type-card__date {
  width: 52px;
  flex-shrink: 0;
  text-align: center;
  padding: 8px 0;
  border-radius: 8px;
  background: #ecfdf8;
  color: #0f766e;
}

.type-card__date strong {
  display: block;
  font-size: 14px;
}

.type-card__date small {
  font-size: 11px;
  color: #14b8a6;
}

.type-card__body {
  min-width: 0;
}

.type-card h4 {
  margin: 0 0 6px;
  font-size: 15px;
  color: #134e4a;
  line-height: 1.4;
}

.type-card p {
  margin: 0;
  color: #5b716e;
  font-size: 13px;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.type-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
  color: #94a9a6;
  font-size: 12px;
}

.list-head {
  margin-bottom: 18px;
  color: #ecfeff;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
  border: none;
  background: transparent;
  color: #5eead4;
  cursor: pointer;
  font-size: 14px;
}

.list-head__row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
}

.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.type-tab {
  border: 1px solid rgba(94, 234, 212, 0.35);
  background: rgba(8, 40, 44, 0.45);
  color: #99f6e4;
  border-radius: 4px;
  padding: 7px 12px;
  cursor: pointer;
  font-size: 13px;
}

.type-tab.active {
  background: #0d9488;
  border-color: #0d9488;
  color: #fff;
}

.list-panel {
  padding: 18px;
  border-radius: 6px 20px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(153, 246, 228, 0.28);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.18);
}

.list-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.info-search {
  width: 280px;
}

.info-list {
  display: flex;
  flex-direction: column;
}

.info-card {
  display: flex;
  gap: 18px;
  border-bottom: 1px solid #e7f6f3;
  padding: 16px 4px;
  cursor: pointer;
  transition: background 0.15s;
}

.info-card:hover {
  background: #f0fdfa;
}

.info-date {
  width: 58px;
  flex-shrink: 0;
  text-align: center;
  padding: 6px 0;
  border-radius: 8px;
  background: #ecfdf8;
}

.info-date strong {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: #0f766e;
}

.info-date small {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #14b8a6;
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
  color: var(--color-text);
  font-size: 16px;
  font-weight: 600;
}

.info-main p {
  color: var(--color-text-secondary);
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
  color: var(--color-text-muted);
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

.detail-panel {
  min-height: 240px;
}

.cover {
  width: 100%;
  max-height: 260px;
  object-fit: cover;
  border-radius: 10px;
  margin: 16px 0;
}

.detail-section {
  margin-top: 16px;
}

.detail-section p {
  color: var(--color-text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
  margin-bottom: 14px;
}

.detail-section h3 {
  font-size: 15px;
  color: var(--color-text);
  margin: 16px 0 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  margin: 14px 0;
}

.detail-grid div {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 12px;
}

.detail-grid dt {
  color: var(--color-text-muted);
  font-size: 12px;
  margin-bottom: 6px;
}

.detail-grid dd {
  color: var(--color-text);
  margin: 0;
}

.policy-content p {
  white-space: pre-wrap;
}

@media (max-width: 960px) {
  .hero-stage,
  .type-cards {
    grid-template-columns: 1fr;
  }

  .section-head,
  .list-head__row,
  .info-card,
  .info-title-line {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-search {
    width: 100%;
  }
}
</style>
