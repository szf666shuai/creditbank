<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { OfficeBuilding, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
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
const loadError = ref<string | null>(null)
const activeType = ref<InformationType>('job')
const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<InformationItem[]>([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<InformationDetail | null>(null)
const actionLoading = ref(false)
const coverMessage = ref('')
const appliedJobIds = ref<number[]>([])
const registeredActivityIds = ref<number[]>([])

const typeConfig: Record<InformationType, { label: string; title: string; description: string }> = {
  job: {
    label: '招聘信息',
    title: '招聘信息',
    description: '机构正式发布的岗位公告，可在详情中投递简历',
  },
  activity: {
    label: '活动信息',
    title: '活动信息',
    description: '机构正式发布的活动日程，可在详情中报名参加',
  },
  policy: {
    label: '政策资讯',
    title: '政策资讯',
    description: '平台与官方政策原文，与论坛讨论帖分开阅读',
  },
}

const currentTitle = computed(() => typeConfig[activeType.value].title)
const currentDescription = computed(() => typeConfig[activeType.value].description)
const canApplyCurrentJob = computed(() => {
  return detail.value?.type === 'job' && !appliedJobIds.value.includes(detail.value.id)
})
const canRegisterCurrentActivity = computed(() => {
  return detail.value?.type === 'activity'
    && !registeredActivityIds.value.includes(detail.value.id)
    && (detail.value.status === 1 || detail.value.status === 2)
})

async function fetchList() {
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

async function openDetail(item: InformationItem) {
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

function changeType() {
  page.value = 1
  syncTypeQuery()
  fetchList()
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

function applyTypeFromRoute() {
  const type = route.query.type
  if (type === 'job' || type === 'activity' || type === 'policy') {
    activeType.value = type
  }
}

function syncTypeQuery() {
  const nextQuery: Record<string, string> = {
    ...Object.fromEntries(
      Object.entries(route.query)
        .filter(([, v]) => typeof v === 'string')
        .map(([k, v]) => [k, String(v)]),
    ),
    type: activeType.value,
  }
  if (route.query.type === activeType.value && !route.query.id) return
  // 切换类型时去掉旧 id，避免串到错误详情
  delete nextQuery.id
  router.replace({ query: nextQuery })
}

async function openDetailFromRoute() {
  const idRaw = route.query.id
  const id = typeof idRaw === 'string' ? Number(idRaw) : NaN
  if (!Number.isFinite(id) || id <= 0) return
  await openDetail({
    type: activeType.value,
    id,
    title: '',
    status: 0,
  })
}

watch(activeType, changeType)
watch(page, fetchList)

watch(
  () => route.query.type,
  () => {
    const previous = activeType.value
    applyTypeFromRoute()
    if (activeType.value === previous) {
      page.value = 1
      fetchList()
    }
  },
)

watch(
  () => [route.query.type, route.query.id] as const,
  async () => {
    applyTypeFromRoute()
    await openDetailFromRoute()
  },
)

onMounted(async () => {
  await authStore.initAuth()
  applyTypeFromRoute()
  await fetchList()
  await openDetailFromRoute()
})
</script>

<template>
  <div class="information-wrap">
    <PageShell
      :title="currentTitle"
      :description="currentDescription"
      :loading="loading"
      :error="loadError"
      @retry="fetchList"
    >
      <div class="official-strip">
        <span class="official-badge">官方发布</span>
        <p>这里是机构与平台发布的正式信息；经验分享与讨论请前往「论坛」。</p>
      </div>

      <el-tabs v-model="activeType" class="page-tabs info-tabs">
        <el-tab-pane
          v-for="(config, type) in typeConfig"
          :key="type"
          :label="config.label"
          :name="type"
        />
      </el-tabs>

      <div class="page-toolbar">
        <el-input
          v-model="keyword"
          class="info-search"
          clearable
          :placeholder="`搜索${typeConfig[activeType].label}`"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-empty
        v-if="!loading && list.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无资讯"
      />

      <div class="info-list" :class="`info-list--${activeType}`">
        <article
          v-for="item in list"
          :key="`${item.type}-${item.id}`"
          class="info-card"
          :class="`info-card--${item.type}`"
          @click="openDetail(item)"
        >
          <div class="info-date">
            <strong>{{ dateParts(item.startTime || item.publishTime || item.createTime).monthDay }}</strong>
            <small>{{ dateParts(item.startTime || item.publishTime || item.createTime).year }}</small>
          </div>
          <div class="info-main">
            <div class="info-title-line">
              <h3>{{ item.title }}</h3>
              <el-tag size="small" effect="plain" :type="item.type === 'policy' ? 'info' : 'success'">
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
    </PageShell>

    <el-dialog v-model="detailVisible" width="720px" destroy-on-close>
      <template #header>
        <span>{{ detail?.title || '资讯详情' }}</span>
      </template>
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detail">
          <div class="detail-tags">
            <el-tag>{{ typeConfig[detail.type].label }}</el-tag>
            <el-tag v-if="detail.statusName" type="success">{{ detail.statusName }}</el-tag>
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
                <dt>学分奖励</dt>
                <dd>{{ detail.creditReward }} 学分</dd>
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
        <el-button v-else-if="detail?.type === 'activity' && registeredActivityIds.includes(detail.id)" type="success" disabled>
          已报名
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.information-wrap {
  padding: 24px 16px 48px;
}

.information-wrap :deep(.page-shell) {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.information-wrap :deep(.page-card) {
  border-radius: 16px;
  overflow: hidden;
  border: none;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
  padding-top: 0;
}

.information-wrap :deep(.page-header) {
  margin: 0 -24px 0;
  padding: 22px 24px 18px;
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  border-bottom: none;
}

.information-wrap :deep(.page-header__main h1),
.information-wrap :deep(.page-header__main p) {
  color: #fff;
}

.information-wrap :deep(.page-header__main p) {
  opacity: 0.9;
}

.official-strip {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
  padding: 12px 14px;
  background: #ecfdf8;
  border: 1px solid #99f6e4;
  border-radius: 10px;
}

.official-badge {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 700;
  color: #0f766e;
  letter-spacing: 0.04em;
}

.official-strip p {
  margin: 0;
  font-size: 13px;
  color: #0f766e;
  line-height: 1.5;
}

.info-search {
  width: 320px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  border-top: 1px solid #ccfbf1;
}

.info-card {
  display: flex;
  gap: 18px;
  background: transparent;
  border: none;
  border-bottom: 1px solid #e7f6f3;
  border-radius: 0;
  padding: 18px 4px;
  cursor: pointer;
  transition: background 0.15s;
}

.info-card:hover {
  background: #f0fdfa;
  box-shadow: none;
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
  color: #5eead4;
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

@media (max-width: 768px) {
  .info-card,
  .info-title-line {
    flex-direction: column;
  }

  .info-search {
    width: 100%;
  }
}
</style>
