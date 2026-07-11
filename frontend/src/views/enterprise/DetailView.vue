<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import { useAuthStore } from '@/stores/auth'
import {
  getJoinedOrgApi,
  listOrgJobsApi,
  listOrgActivitiesApi,
  listOrgMaterialsByOrgApi,
  getOrgParticipationStatusApi,
  applyJobApi,
  registerActivityApi,
  type OrgListItem,
  type JobPostingItem,
  type ActivityItem,
  type OrgMaterialItem,
} from '@/api/enterprise'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const orgId = computed(() => Number(route.params.id))
const loading = ref(false)
const loadError = ref<string | null>(null)
const tabLoading = ref(false)
const tabError = ref<string | null>(null)
const actionLoading = ref<number | null>(null)
const org = ref<OrgListItem | null>(null)
const activeTab = ref('jobs')

const jobs = ref<JobPostingItem[]>([])
const activities = ref<ActivityItem[]>([])
const materials = ref<OrgMaterialItem[]>([])
const jobsTotal = ref(0)
const activitiesTotal = ref(0)
const materialsTotal = ref(0)
const appliedJobIds = ref<number[]>([])
const registeredActivityIds = ref<number[]>([])

const applyDialogVisible = ref(false)
const applyingJob = ref<JobPostingItem | null>(null)
const coverMessage = ref('')
const applying = ref(false)

const canOperateAsStudent = computed(() => authStore.isLoggedIn && authStore.isStudent)

const typeIcons: Record<number, string> = {
  1: '🏫',
  2: '📚',
  3: '🏢',
  4: '🏛️',
}

function isJobApplied(jobId: number) {
  return appliedJobIds.value.includes(jobId)
}

function isActivityRegistered(activityId: number) {
  return registeredActivityIds.value.includes(activityId)
}

function canRegisterActivity(activity: ActivityItem) {
  return activity.status === 1 || activity.status === 2
}

function ensureStudentLogin() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  if (!authStore.isStudent) {
    ElMessage.warning('仅学员可投递简历或报名活动')
    return false
  }
  return true
}

async function fetchParticipationStatus() {
  if (!canOperateAsStudent.value || !orgId.value) {
    appliedJobIds.value = []
    registeredActivityIds.value = []
    return
  }
  try {
    const data = unwrapApi(await getOrgParticipationStatusApi(orgId.value))
    appliedJobIds.value = data.appliedJobIds ?? []
    registeredActivityIds.value = data.registeredActivityIds ?? []
  } catch {
    appliedJobIds.value = []
    registeredActivityIds.value = []
  }
}

async function fetchOrgDetail() {
  if (!orgId.value || Number.isNaN(orgId.value)) {
    loadError.value = '无效的企业 ID'
    return
  }

  loading.value = true
  loadError.value = null
  try {
    org.value = unwrapApi(await getJoinedOrgApi(orgId.value))
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchTabData() {
  if (!org.value) return

  tabLoading.value = true
  tabError.value = null
  try {
    if (activeTab.value === 'jobs') {
      const data = unwrapApi(await listOrgJobsApi(orgId.value))
      jobs.value = data.records
      jobsTotal.value = data.total
    } else if (activeTab.value === 'activities') {
      const data = unwrapApi(await listOrgActivitiesApi(orgId.value))
      activities.value = data.records
      activitiesTotal.value = data.total
    } else if (activeTab.value === 'materials') {
      const data = unwrapApi(await listOrgMaterialsByOrgApi(orgId.value))
      materials.value = data.records
      materialsTotal.value = data.total
    }
    await fetchParticipationStatus()
  } catch (e) {
    tabError.value = getErrorMessage(e, '加载失败')
  } finally {
    tabLoading.value = false
  }
}

function openApplyDialog(job: JobPostingItem) {
  if (!ensureStudentLogin()) return
  if (isJobApplied(job.id)) {
    ElMessage.info('您已投递该职位')
    return
  }
  applyingJob.value = job
  coverMessage.value = ''
  applyDialogVisible.value = true
}

async function handleApply() {
  if (!applyingJob.value) return
  applying.value = true
  try {
    unwrapApi(
      await applyJobApi(applyingJob.value.id, {
        coverMessage: coverMessage.value.trim() || undefined,
      }),
    )
    ElMessage.success('投递成功')
    applyDialogVisible.value = false
    if (!appliedJobIds.value.includes(applyingJob.value.id)) {
      appliedJobIds.value = [...appliedJobIds.value, applyingJob.value.id]
    }
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '投递失败'))
  } finally {
    applying.value = false
  }
}

async function handleRegister(activity: ActivityItem) {
  if (!ensureStudentLogin()) return
  if (isActivityRegistered(activity.id)) {
    ElMessage.info('您已报名该活动')
    return
  }
  if (!canRegisterActivity(activity)) {
    ElMessage.warning('活动当前不可报名')
    return
  }

  await ElMessageBox.confirm(`确定报名参加「${activity.title}」吗？`, '报名活动', {
    type: 'info',
    confirmButtonText: '确认报名',
    cancelButtonText: '取消',
  })

  actionLoading.value = activity.id
  try {
    unwrapApi(await registerActivityApi(activity.id))
    ElMessage.success('报名成功')
    if (!registeredActivityIds.value.includes(activity.id)) {
      registeredActivityIds.value = [...registeredActivityIds.value, activity.id]
    }
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '报名失败'))
  } finally {
    actionLoading.value = null
  }
}

watch(activeTab, fetchTabData)

watch(
  () => authStore.isLoggedIn,
  () => {
    fetchParticipationStatus()
  },
)

watch(
  () => route.params.id,
  async () => {
    activeTab.value = 'jobs'
    jobs.value = []
    activities.value = []
    materials.value = []
    await fetchOrgDetail()
    await fetchTabData()
  },
)

onMounted(async () => {
  await authStore.initAuth()
  await fetchOrgDetail()
  await fetchTabData()
})
</script>

<template>
  <div class="detail-page-wrap">
    <PageShell :loading="loading" :error="loadError" @retry="fetchOrgDetail">
      <template #header>
        <button type="button" class="back-btn" @click="router.push('/enterprise')">
          ← 返回企业列表
        </button>
      </template>

      <div v-if="org" class="org-header">
        <div class="org-header-main">
          <div class="org-logo">
            <img v-if="org.logo" :src="org.logo" :alt="org.name" />
            <span v-else class="org-logo-fallback">{{ typeIcons[org.type] || '🏢' }}</span>
          </div>
          <div class="org-info">
            <div class="org-title-row">
              <h1>{{ org.name }}</h1>
              <el-tag type="info">{{ org.typeName }}</el-tag>
            </div>
            <p class="org-intro">{{ org.intro || '暂无简介' }}</p>
          </div>
        </div>

        <div class="contact-grid">
          <div v-if="org.contact" class="contact-item">
            <span class="label">联系人</span>
            <span>{{ org.contact }}</span>
          </div>
          <div v-if="org.phone" class="contact-item">
            <span class="label">电话</span>
            <span>{{ org.phone }}</span>
          </div>
          <div v-if="org.email" class="contact-item">
            <span class="label">邮箱</span>
            <span>{{ org.email }}</span>
          </div>
          <div v-if="org.address" class="contact-item">
            <span class="label">地址</span>
            <span>{{ org.address }}</span>
          </div>
          <div v-if="org.website" class="contact-item">
            <span class="label">官网</span>
            <a :href="org.website" target="_blank" rel="noopener noreferrer">{{ org.website }}</a>
          </div>
        </div>
      </div>

      <el-tabs v-if="org" v-model="activeTab" class="page-tabs detail-tabs">
        <el-tab-pane label="招聘" name="jobs">
          <div v-loading="tabLoading">
            <el-alert
              v-if="tabError"
              :title="tabError"
              type="error"
              show-icon
              :closable="false"
              class="tab-error"
            >
              <el-button link type="primary" @click="fetchTabData">点击重试</el-button>
            </el-alert>
            <el-empty
              v-if="!tabLoading && !tabError && jobs.length === 0"
              class="page-empty"
              :image-size="80"
              description="暂无招聘信息"
            />
            <div v-else-if="jobs.length > 0" class="tab-list">
              <article v-for="job in jobs" :key="job.id" class="tab-card">
                <div class="tab-card-header">
                  <h3>{{ job.title }}</h3>
                  <div class="tab-card-actions">
                    <el-tag size="small" type="success">{{ job.statusName }}</el-tag>
                    <el-tag v-if="canOperateAsStudent && isJobApplied(job.id)" size="small" type="info">
                      已投递
                    </el-tag>
                    <el-button
                      v-else-if="canOperateAsStudent"
                      type="primary"
                      size="small"
                      @click="openApplyDialog(job)"
                    >
                      投递简历
                    </el-button>
                  </div>
                </div>
                <p class="tab-desc">{{ job.description || '暂无描述' }}</p>
                <div class="tab-meta">
                  <span v-if="job.salaryRange">💰 {{ job.salaryRange }}</span>
                  <span v-if="job.location">📍 {{ job.location }}</span>
                  <span v-if="job.eduRequirement">🎓 {{ job.eduRequirement }}</span>
                </div>
                <p v-if="job.requirements" class="tab-sub">要求：{{ job.requirements }}</p>
              </article>
            </div>
            <p v-if="jobsTotal > jobs.length" class="tab-total">共 {{ jobsTotal }} 条招聘</p>
          </div>
        </el-tab-pane>

        <el-tab-pane label="活动" name="activities">
          <div v-loading="tabLoading">
            <el-alert
              v-if="tabError"
              :title="tabError"
              type="error"
              show-icon
              :closable="false"
              class="tab-error"
            >
              <el-button link type="primary" @click="fetchTabData">点击重试</el-button>
            </el-alert>
            <el-empty
              v-if="!tabLoading && !tabError && activities.length === 0"
              class="page-empty"
              :image-size="80"
              description="暂无活动信息"
            />
            <div v-else-if="activities.length > 0" class="tab-list">
              <article v-for="activity in activities" :key="activity.id" class="tab-card">
                <div class="tab-card-header">
                  <h3>{{ activity.title }}</h3>
                  <div class="tab-card-actions">
                    <el-tag size="small">{{ activity.statusName }}</el-tag>
                    <el-tag
                      v-if="canOperateAsStudent && isActivityRegistered(activity.id)"
                      size="small"
                      type="success"
                    >
                      已报名
                    </el-tag>
                    <el-button
                      v-else-if="canOperateAsStudent && canRegisterActivity(activity)"
                      type="primary"
                      size="small"
                      :loading="actionLoading === activity.id"
                      @click="handleRegister(activity)"
                    >
                      报名活动
                    </el-button>
                  </div>
                </div>
                <p class="tab-desc">{{ activity.description || '暂无描述' }}</p>
                <div class="tab-meta">
                  <span v-if="activity.location">📍 {{ activity.location }}</span>
                  <span>🕐 {{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</span>
                  <span v-if="activity.creditReward">⭐ 奖励 {{ activity.creditReward }} 秩点</span>
                </div>
              </article>
            </div>
            <p v-if="activitiesTotal > activities.length" class="tab-total">共 {{ activitiesTotal }} 条活动</p>
          </div>
        </el-tab-pane>

        <el-tab-pane label="企业资料" name="materials">
          <div v-loading="tabLoading">
            <el-alert
              v-if="tabError"
              :title="tabError"
              type="error"
              show-icon
              :closable="false"
              class="tab-error"
            >
              <el-button link type="primary" @click="fetchTabData">点击重试</el-button>
            </el-alert>
            <el-empty
              v-if="!tabLoading && !tabError && materials.length === 0"
              class="page-empty"
              :image-size="80"
              description="暂无企业资料"
            />
            <div v-else-if="materials.length > 0" class="tab-list">
              <article v-for="material in materials" :key="material.id" class="tab-card">
                <div class="tab-card-header">
                  <h3>{{ material.title }}</h3>
                  <el-tag size="small" type="info">{{ material.materialTypeName }}</el-tag>
                </div>
                <p class="tab-desc">{{ material.description || '暂无描述' }}</p>
                <div class="tab-meta">
                  <span>📅 {{ formatTime(material.createTime) }}</span>
                  <a
                    v-if="material.fileUrl"
                    :href="material.fileUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="page-link"
                  >
                    查看资料 →
                  </a>
                </div>
              </article>
            </div>
            <p v-if="materialsTotal > materials.length" class="tab-total">共 {{ materialsTotal }} 份资料</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </PageShell>

    <el-dialog v-model="applyDialogVisible" title="投递简历" width="520px" destroy-on-close>
      <template v-if="applyingJob">
        <p class="apply-job-title">应聘职位：{{ applyingJob.title }}</p>
        <el-input
          v-model="coverMessage"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="选填求职信，将随简历一并发送给企业"
        />
        <p class="apply-tip">系统将自动关联您的默认简历</p>
      </template>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="handleApply">确认投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-page-wrap {
  padding: 32px 16px 48px;
}

.detail-page-wrap :deep(.page-shell) {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.back-btn {
  border: none;
  background: none;
  color: var(--color-primary);
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 20px;
  padding: 0;
}

.back-btn:hover {
  color: var(--color-primary-dark);
}

.org-header {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.org-header-main {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.org-logo {
  width: 72px;
  height: 72px;
  border-radius: 14px;
  overflow: hidden;
  background: var(--color-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.org-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.org-logo-fallback {
  font-size: 32px;
}

.org-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.org-title-row h1 {
  font-size: 24px;
  color: var(--color-text);
}

.org-intro {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text-secondary);
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.contact-item {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.contact-item .label {
  display: block;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}

.contact-item a {
  color: var(--color-primary);
  text-decoration: none;
}

.contact-item a:hover {
  text-decoration: underline;
}

.detail-tabs {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 8px 20px 20px;
}

.tab-error {
  margin-bottom: 16px;
}

.tab-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tab-card {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 16px;
}

.tab-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.tab-card-header h3 {
  font-size: 16px;
  color: var(--color-text);
}

.tab-card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.tab-desc {
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-secondary);
  margin-bottom: 10px;
}

.tab-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.tab-sub {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.tab-total {
  margin-top: 12px;
  font-size: 12px;
  color: var(--color-text-muted);
  text-align: right;
}

.apply-job-title {
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.apply-tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
