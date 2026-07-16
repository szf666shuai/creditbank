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
  getOrgParticipationStatusApi,
  applyJobApi,
  registerActivityApi,
  type OrgListItem,
  type JobPostingItem,
  type ActivityItem,
} from '@/api/enterprise'
import { fetchOrgCourses, type LearningResource } from '@/api/learning'
import { listOrgTransferRulesApi, type CreditTransferRule } from '@/api/credit-transfer'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { listResumesApi, type UserResumeSummary } from '@/api/profile-resume'
import UiIcon from '@/components/ui/UiIcon.vue'
import { formatTime } from '@/utils/format'
import '@/styles/enterprise-form-dialog.css'

type ResumeAttachMode = 'default' | 'custom' | 'none'

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
const courses = ref<LearningResource[]>([])
const transferRules = ref<CreditTransferRule[]>([])
const jobsTotal = ref(0)
const activitiesTotal = ref(0)
const appliedJobIds = ref<number[]>([])
const registeredActivityIds = ref<number[]>([])

const applyDialogVisible = ref(false)
const applyingJob = ref<JobPostingItem | null>(null)
const coverMessage = ref('')
const applying = ref(false)
const resumeAttachMode = ref<ResumeAttachMode>('default')
const selectedResumeId = ref<number | null>(null)
const resumeOptions = ref<UserResumeSummary[]>([])
const resumesLoading = ref(false)

const canOperateAsStudent = computed(() => authStore.isLoggedIn && authStore.isStudent)

const typeIcons: Record<number, string> = {
  1: 'school',
  2: 'course',
  3: 'enterprise',
  4: 'admin',
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
    } else if (activeTab.value === 'courses') {
      courses.value = unwrapApi(await fetchOrgCourses(orgId.value))
    } else if (activeTab.value === 'transfer-rules') {
      transferRules.value = unwrapApi(await listOrgTransferRulesApi(orgId.value))
    }
    await fetchParticipationStatus()
  } catch (e) {
    tabError.value = getErrorMessage(e, '加载失败')
  } finally {
    tabLoading.value = false
  }
}

async function openApplyDialog(job: JobPostingItem) {
  if (!ensureStudentLogin()) return
  if (isJobApplied(job.id)) {
    ElMessage.info('您已投递该职位')
    return
  }
  applyingJob.value = job
  coverMessage.value = ''
  resumeAttachMode.value = 'default'
  selectedResumeId.value = null
  resumesLoading.value = true
  try {
    resumeOptions.value = unwrapApi(await listResumesApi())
    const defaultResume = resumeOptions.value.find((item) => item.isDefault === 1)
    if (!defaultResume && resumeOptions.value.length > 0) {
      resumeAttachMode.value = 'custom'
      selectedResumeId.value = resumeOptions.value[0].id
    }
  } catch {
    resumeOptions.value = []
  } finally {
    resumesLoading.value = false
  }
  applyDialogVisible.value = true
}

async function handleApply() {
  if (!applyingJob.value) return
  if (resumeAttachMode.value === 'custom' && !selectedResumeId.value) {
    ElMessage.warning('请选择要投递的简历')
    return
  }
  applying.value = true
  try {
    const payload: {
      coverMessage?: string
      resumeId?: number
      attachResume?: boolean
    } = {
      coverMessage: coverMessage.value.trim() || undefined,
    }
    if (resumeAttachMode.value === 'none') {
      payload.attachResume = false
    } else if (resumeAttachMode.value === 'custom' && selectedResumeId.value) {
      payload.resumeId = selectedResumeId.value
    }
    unwrapApi(await applyJobApi(applyingJob.value.id, payload))
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
    courses.value = []
    transferRules.value = []
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
  <div class="org-detail-page">
    <PageShell :loading="loading" :error="loadError" plain @retry="fetchOrgDetail">
      <template #header>
        <button type="button" class="back-link" @click="router.push('/enterprise')">
          <span aria-hidden="true">←</span> 返回企业列表
        </button>
      </template>

      <div v-if="org" class="org-hero">
        <div class="org-hero__glow" aria-hidden="true" />
        <div class="org-hero__main">
          <div class="org-logo">
            <img v-if="org.logo" :src="org.logo" :alt="org.name" />
            <UiIcon
              v-else
              class="org-logo-fallback"
              :name="typeIcons[org.type] || 'enterprise'"
              :size="44"
            />
          </div>
          <div class="org-info">
            <p class="org-kicker">Partner Profile</p>
            <div class="org-title-row">
              <h1>{{ org.name }}</h1>
              <el-tag effect="dark" type="info">{{ org.typeName }}</el-tag>
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

      <el-tabs v-if="org" v-model="activeTab" class="detail-tabs">
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
              <article v-for="job in jobs" :key="job.id" class="tab-card job-card">
                <div class="tab-card-header">
                  <div class="tab-card-title">
                    <h3>{{ job.title }}</h3>
                    <div class="tab-meta">
                      <span v-if="job.salaryRange" class="meta-chip">💰 {{ job.salaryRange }}</span>
                      <span v-if="job.location" class="meta-chip">📍 {{ job.location }}</span>
                      <span v-if="job.eduRequirement" class="meta-chip">🎓 {{ job.eduRequirement }}</span>
                    </div>
                  </div>
                  <div class="tab-card-actions">
                    <el-tag size="small" type="success" effect="dark">{{ job.statusName }}</el-tag>
                    <el-tag v-if="canOperateAsStudent && isJobApplied(job.id)" size="small" type="info" effect="dark">
                      已投递
                    </el-tag>
                    <el-button
                      v-else-if="canOperateAsStudent"
                      type="primary"
                      round
                      @click="openApplyDialog(job)"
                    >
                      投递简历
                    </el-button>
                  </div>
                </div>
                <p class="tab-desc">{{ job.description || '暂无描述' }}</p>
                <p v-if="job.requirements" class="tab-sub">任职要求：{{ job.requirements }}</p>
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
                  <span v-if="activity.location">地点 {{ activity.location }}</span>
                  <span>时间 {{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</span>
                  <span v-if="activity.creditReward">⭐ 奖励 {{ activity.creditReward }} 秩点</span>
                </div>
              </article>
            </div>
            <p v-if="activitiesTotal > activities.length" class="tab-total">共 {{ activitiesTotal }} 条活动</p>
          </div>
        </el-tab-pane>

        <el-tab-pane label="课程概览" name="courses">
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
              v-if="!tabLoading && !tabError && courses.length === 0"
              class="page-empty"
              :image-size="80"
              description="暂无课程"
            />
            <div v-else-if="courses.length > 0" class="tab-list">
              <article v-for="course in courses" :key="course.id" class="tab-card">
                <div class="tab-card-header">
                  <h3>{{ course.title }}</h3>
                  <div class="tab-card-actions">
                    <el-tag v-if="course.creditValue" size="small" type="warning">
                      {{ course.creditValue }} 学分
                    </el-tag>
                    <el-button
                      type="primary"
                      size="small"
                      round
                      @click="router.push(`/resources/${course.id}`)"
                    >
                      开始学习
                    </el-button>
                  </div>
                </div>
                <p class="tab-desc">{{ course.description || '暂无描述' }}</p>
                <div class="tab-meta">
                  <span v-if="course.difficulty">难度：{{ course.difficulty }}</span>
                  <span v-if="course.durationMinutes">时长：{{ course.durationMinutes }}分钟</span>
                  <span v-if="course.tags">标签：{{ course.tags }}</span>
                </div>
              </article>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="学分转换规则" name="transfer-rules">
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
            <el-alert
              v-else
              type="info"
              :closable="false"
              show-icon
              class="tab-error"
              title="以下为该机构已启用的学分互认 / 转换规则。学员可在个人中心「学分转换」中选择规则并申请；是否承认由该机构审核。"
            />
            <el-empty
              v-if="!tabLoading && !tabError && transferRules.length === 0"
              class="page-empty"
              :image-size="80"
              description="该机构暂未公布转换规则"
            />
            <div v-else-if="transferRules.length > 0" class="rule-grid">
              <article v-for="rule in transferRules" :key="rule.id" class="rule-card">
                <div class="rule-card__head">
                  <el-tag size="small" type="success">启用</el-tag>
                  <strong>{{ Math.round(Number(rule.creditRatio || 1) * 100) }}%</strong>
                </div>
                <h3>{{ rule.description || '转换规则' }}</h3>
                <p>
                  <span>接收目标</span>{{ rule.targetTypeName || '课程' }} ·
                  {{ rule.targetCourseName || (rule.targetAchievementId ? `成果#${rule.targetAchievementId}` : '本机构学分') }}
                </p>
                <el-button
                  v-if="canOperateAsStudent"
                  type="primary"
                  link
                  @click="router.push('/profile/credit-transfer')"
                >
                  去申请转换 →
                </el-button>
              </article>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </PageShell>

    <el-dialog
      v-model="applyDialogVisible"
      title="投递简历"
      width="560px"
      destroy-on-close
      class="enterprise-form-dialog apply-dialog"
    >
      <template v-if="applyingJob">
        <div class="apply-job-banner">
          <span class="apply-job-label">应聘职位</span>
          <strong>{{ applyingJob.title }}</strong>
        </div>

        <div v-loading="resumesLoading" class="apply-section">
          <p class="apply-section-title">附带简历</p>
          <el-radio-group v-model="resumeAttachMode" class="resume-mode-group">
            <el-radio value="default">使用默认简历</el-radio>
            <el-radio value="custom" :disabled="resumeOptions.length === 0">选择其他简历</el-radio>
            <el-radio value="none">不附带简历</el-radio>
          </el-radio-group>
          <el-select
            v-if="resumeAttachMode === 'custom'"
            v-model="selectedResumeId"
            placeholder="请选择简历版本"
            style="width: 100%; margin-top: 12px"
          >
            <el-option
              v-for="item in resumeOptions"
              :key="item.id"
              :label="`${item.title}${item.isDefault === 1 ? '（默认）' : ''}`"
              :value="item.id"
            />
          </el-select>
          <p v-if="resumeAttachMode === 'none'" class="apply-tip">
            仅发送求职信，企业端将看不到简历附件。
          </p>
          <p v-else-if="resumeAttachMode === 'default'" class="apply-tip">
            将自动关联您在「我的简历」中设为默认的版本。
          </p>
          <router-link
            v-if="resumeOptions.length === 0 && !resumesLoading"
            to="/profile/resume/new"
            class="apply-resume-link"
          >
            还没有简历？去创建 →
          </router-link>
        </div>

        <div class="apply-section">
          <p class="apply-section-title">求职信（选填）</p>
          <el-input
            v-model="coverMessage"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="简要介绍您的优势与求职意向，将随投递一并发送"
          />
        </div>
      </template>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="handleApply">确认投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.org-detail-page {
  padding: 24px 16px 56px;
}

.org-detail-page :deep(.page-shell) {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: color-mix(in srgb, var(--color-primary) 16%, transparent);
  color: var(--color-primary);
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 20px;
  padding: 8px 14px;
  border-radius: 999px;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  transition: background 0.2s, color 0.2s;
}

.back-link:hover {
  background: color-mix(in srgb, var(--color-primary) 16%, transparent);
  color: var(--color-foreground);
}

.org-hero {
  position: relative;
  overflow: hidden;
  background: var(--color-card);
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  border-radius: 18px;
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: var(--nb-shadow, var(--shadow-md));
}

.org-hero__glow {
  position: absolute;
  inset: auto -80px -80px auto;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, color-mix(in srgb, var(--color-secondary) 28%, transparent), transparent 70%);
  pointer-events: none;
}

.org-hero__main {
  position: relative;
  display: flex;
  gap: 22px;
  margin-bottom: 22px;
}

.org-logo {
  width: 88px;
  height: 88px;
  border-radius: 18px;
  overflow: hidden;
  background: color-mix(in srgb, var(--color-primary) 16%, transparent);
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
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
  color: var(--color-primary);
}

.org-kicker {
  margin-bottom: 8px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-muted-foreground);
}

.org-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.org-title-row h1 {
  font-size: 28px;
  color: var(--color-foreground);
}

.org-intro {
  font-size: 15px;
  line-height: 1.75;
  color: var(--color-muted-foreground);
  max-width: 760px;
}

.contact-grid {
  position: relative;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-neutral);
}

.contact-item {
  padding: 12px 14px;
  border-radius: 12px;
  background: var(--color-background);
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  font-size: 14px;
  color: var(--color-foreground);
}

.contact-item .label {
  display: block;
  color: var(--color-muted-foreground);
  font-size: 12px;
  margin-bottom: 6px;
}

.contact-item a {
  color: var(--color-primary);
  text-decoration: none;
}

.contact-item a:hover {
  text-decoration: underline;
}

.detail-tabs {
  background: var(--color-card);
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  border-radius: 16px;
  padding: 8px 20px 24px;
  }

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.detail-tabs :deep(.el-tabs__item) {
  color: var(--color-muted-foreground);
  font-size: 15px;
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}

.detail-tabs :deep(.el-tabs__active-bar) {
  background: var(--color-primary);
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  background: var(--color-border-neutral);
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
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  border-radius: 14px;
  padding: 18px 20px;
  background: var(--color-background);
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
}

.tab-card:hover {
  border-color: color-mix(in srgb, var(--color-primary) 16%, transparent);
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.job-card {
  border-left: 3px solid color-mix(in srgb, var(--color-primary) 16%, transparent);
}

.tab-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 10px;
}

.tab-card-title h3 {
  font-size: 18px;
  color: var(--color-foreground);
  margin-bottom: 10px;
}

.tab-card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.tab-desc {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-muted-foreground);
  margin-bottom: 8px;
}

.tab-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: var(--color-primary-dark);
  background: var(--color-primary-light);
  border: 1px solid var(--color-border);
}

.tab-sub {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--color-border-neutral);
  font-size: 13px;
  color: var(--color-muted-foreground);
  line-height: 1.6;
}

.tab-total {
  margin-top: 14px;
  font-size: 12px;
  color: var(--color-muted-foreground);
  text-align: right;
}

.apply-job-banner {
  padding: 14px 16px;
  margin-bottom: 18px;
  border-radius: 12px;
  background: color-mix(in srgb, var(--color-primary) 16%, transparent);
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
}

.apply-job-label {
  display: block;
  font-size: 12px;
  color: var(--color-muted-foreground);
  margin-bottom: 6px;
}

.apply-job-banner strong {
  color: var(--color-foreground);
  font-size: 16px;
}

.apply-section {
  margin-bottom: 18px;
}

.apply-section-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-muted-foreground);
}

.resume-mode-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.apply-tip {
  margin-top: 10px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--color-muted-foreground);
}

.apply-resume-link {
  display: inline-block;
  margin-top: 10px;
  font-size: 13px;
  color: var(--color-primary);
  text-decoration: none;
}

.apply-resume-link:hover {
  text-decoration: underline;
}

.rule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.rule-card {
  border: 1.5px solid var(--color-border-neutral);
  border-radius: 12px;
  padding: 14px 16px;
  background: #fff;
}

.rule-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.rule-card h3 {
  margin: 0 0 10px;
  font-size: 15px;
  line-height: 1.4;
}

.rule-card p {
  margin: 0 0 6px;
  font-size: 13px;
  color: var(--color-muted-foreground);
}

.rule-card p span {
  display: inline-block;
  min-width: 2.5em;
  margin-right: 6px;
  color: var(--color-foreground);
  font-weight: 700;
}

@media (max-width: 768px) {
  .org-hero__main {
    flex-direction: column;
  }

  .tab-card-header {
    flex-direction: column;
  }

  .tab-card-actions {
    justify-content: flex-start;
  }
}
</style>
