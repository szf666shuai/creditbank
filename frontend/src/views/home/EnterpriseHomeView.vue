<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getEnterpriseDashboardApi, type EnterpriseDashboard } from '@/api/enterprise-dashboard'
import { listMyJobsApi, type JobManageItem } from '@/api/enterprise-jobs'
import { listMyActivitiesApi, type ActivityManageItem } from '@/api/enterprise-activities'
import { listMyApplicationsApi, type JobApplicationItem } from '@/api/interview'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const loadError = ref('')
const dashboard = ref<EnterpriseDashboard | null>(null)
const jobs = ref<JobManageItem[]>([])
const activities = ref<ActivityManageItem[]>([])
const applications = ref<JobApplicationItem[]>([])

const orgName = computed(() => dashboard.value?.orgName || authStore.userInfo?.orgName || '企业用户')

const openJobs = computed(() => jobs.value.filter((item) => item.status === 1).slice(0, 5))
const recentActivities = computed(() => activities.value.slice(0, 5))
const pendingApplications = computed(() =>
  applications.value.filter((item) => item.status === 0).slice(0, 5),
)

const overviewCards = computed(() => {
  const data = dashboard.value
  if (!data) return []
  return [
    { label: '在招职位', value: data.openJobCount, hint: '当前对外招聘中' },
    { label: '进行中活动', value: data.ongoingActivityCount, hint: '学员可参与' },
    { label: '待处理投递', value: data.pendingApplicationCount, hint: '等待企业回复' },
    { label: '待回复面试', value: data.pendingInterviewCount, hint: '面试安排待确认' },
  ]
})

function activityStatusTag(status: number) {
  if (status === 2) return 'success'
  if (status === 1) return 'warning'
  if (status === 3) return 'info'
  return 'danger'
}

function go(path: string) {
  router.push(path)
}

async function loadDashboard() {
  loading.value = true
  loadError.value = ''
  try {
    const [dash, jobList, activityList, applicationList] = await Promise.all([
      getEnterpriseDashboardApi(),
      listMyJobsApi(),
      listMyActivitiesApi(),
      listMyApplicationsApi(),
    ])
    dashboard.value = unwrapApi(dash)
    jobs.value = unwrapApi(jobList)
    activities.value = unwrapApi(activityList)
    applications.value = unwrapApi(applicationList)
  } catch (e) {
    loadError.value = getErrorMessage(e, '主页数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <div class="role-home enterprise-home">
    <div class="role-home-content">
      <section class="hero-banner">
        <div class="hero-inner">
          <span class="hero-badge">企业运营门户</span>
          <h1>{{ orgName }}</h1>
          <p class="hero-desc">浏览招聘、活动与投递动态，掌握机构运营全貌。</p>
        </div>
      </section>

      <div class="section-inner">
        <el-skeleton v-if="loading" :rows="8" animated />
        <el-alert v-else-if="loadError" type="warning" :title="loadError" show-icon :closable="false" />

        <template v-else>
          <el-alert
            v-if="dashboard && dashboard.joinStatus !== 1"
            type="warning"
            show-icon
            :closable="false"
            title="机构尚未完成加盟审核，部分发布功能可能受限。"
            class="status-alert"
          />

          <section class="overview-grid">
            <article v-for="item in overviewCards" :key="item.label" class="overview-card">
              <span class="overview-label">{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <p>{{ item.hint }}</p>
            </article>
          </section>

          <div class="feed-grid">
            <section class="feed-panel">
              <div class="panel-head">
                <h2>在招职位</h2>
                <button type="button" class="text-link" @click="go('/profile/enterprise/jobs')">查看全部</button>
              </div>
              <div v-if="openJobs.length" class="feed-list">
                <article v-for="job in openJobs" :key="job.id" class="feed-item">
                  <div>
                    <strong>{{ job.title }}</strong>
                    <p>{{ job.location || '地点待定' }} · {{ job.salaryRange || '薪资面议' }}</p>
                  </div>
                  <el-tag size="small" type="success">{{ job.statusName }}</el-tag>
                </article>
              </div>
              <el-empty v-else :image-size="64" description="暂无在招职位" />
            </section>

            <section class="feed-panel">
              <div class="panel-head">
                <h2>近期活动</h2>
                <button type="button" class="text-link" @click="go('/profile/enterprise/activities')">查看全部</button>
              </div>
              <div v-if="recentActivities.length" class="feed-list">
                <article v-for="activity in recentActivities" :key="activity.id" class="feed-item">
                  <div>
                    <strong>{{ activity.title }}</strong>
                    <p>
                      {{ formatTime(activity.startTime) }} ~ {{ formatTime(activity.endTime) }}
                      · {{ activity.location || '线上/待定' }}
                    </p>
                  </div>
                  <el-tag size="small" :type="activityStatusTag(activity.status)">{{ activity.statusName }}</el-tag>
                </article>
              </div>
              <el-empty v-else :image-size="64" description="暂无活动" />
            </section>

            <section class="feed-panel full-width">
              <div class="panel-head">
                <h2>最新投递</h2>
                <button type="button" class="text-link" @click="go('/profile/enterprise/applications')">查看全部</button>
              </div>
              <div v-if="pendingApplications.length" class="feed-list">
                <article v-for="item in pendingApplications" :key="item.id" class="feed-item">
                  <div>
                    <strong>{{ item.applicantName }}</strong>
                    <p>投递「{{ item.jobTitle }}」 · {{ formatTime(item.createTime) }}</p>
                  </div>
                  <el-tag size="small" type="warning">{{ item.statusName }}</el-tag>
                </article>
              </div>
              <el-empty v-else :image-size="64" description="暂无待处理投递" />
            </section>
          </div>

          <div class="footer-cta">
            <el-button type="primary" size="large" @click="go('/profile/enterprise')">进入企业工作台</el-button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.enterprise-home {
  --role-primary: #2094f3;
  --role-primary-dark: #93c5fd;
  --role-primary-soft: #60a5fa;
  --role-surface: rgba(6, 18, 36, 0.58);
  --role-surface-strong: rgba(8, 26, 50, 0.74);
  --role-surface-card: rgba(10, 32, 58, 0.68);
  --role-border: rgba(32, 148, 243, 0.28);
  --role-text: #eff6ff;
  --role-text-muted: #93b8d8;
  --role-text-on-hero: #fff;
  --role-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);

  position: relative;
  min-height: calc(100vh - var(--header-height));
  padding-bottom: 48px;
  background: transparent;
}

.role-home-content {
  position: relative;
  z-index: 1;
}

.hero-banner {
  max-width: var(--content-max-width);
  margin: 0 auto 20px;
  padding: 0 16px;
}

.hero-inner {
  position: relative;
  overflow: hidden;
  padding: 32px 36px;
  border-radius: 20px;
  color: var(--role-text-on-hero);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, transparent 55%),
    linear-gradient(120deg, #0d4d8a 0%, #1565c0 42%, #2094f3 72%, #60a5fa 100%);
  box-shadow: var(--role-shadow);
  border: 1px solid rgba(147, 197, 253, 0.28);
  backdrop-filter: blur(12px);
}

.hero-inner::after {
  content: '';
  position: absolute;
  width: 220px;
  height: 220px;
  top: -80px;
  right: -40px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.22) 0%, transparent 68%);
}

.hero-badge {
  display: inline-block;
  margin-bottom: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 0.06em;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(4px);
}

.hero-inner h1 {
  position: relative;
  margin: 0 0 10px;
  font-size: 32px;
  line-height: 1.25;
}

.hero-desc {
  position: relative;
  margin: 0;
  max-width: 560px;
  line-height: 1.8;
  opacity: 0.92;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 16px;
}

.status-alert {
  margin-bottom: 16px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.overview-card {
  padding: 18px;
  border-radius: 16px;
  background: var(--role-surface-strong);
  border: 1px solid var(--role-border);
  box-shadow: var(--role-shadow);
  backdrop-filter: blur(12px);
}

.overview-label {
  display: block;
  color: var(--role-text-muted);
  font-size: 13px;
  margin-bottom: 8px;
}

.overview-card strong {
  font-size: 30px;
  color: var(--role-primary-dark);
}

.overview-card p {
  margin: 8px 0 0;
  color: var(--role-text-muted);
  font-size: 12px;
}

.feed-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.feed-panel {
  padding: 20px;
  border-radius: 16px;
  background: var(--role-surface);
  border: 1px solid var(--role-border);
  box-shadow: var(--role-shadow);
  backdrop-filter: blur(12px);
}

.feed-panel.full-width {
  grid-column: 1 / -1;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(32, 148, 243, 0.18);
}

.panel-head h2 {
  margin: 0;
  font-size: 18px;
  color: var(--role-primary-dark);
}

.text-link {
  border: none;
  background: transparent;
  color: var(--role-primary-soft);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
}

.feed-list {
  display: grid;
  gap: 10px;
}

.feed-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  padding: 12px 14px;
  border-radius: 12px;
  background: var(--role-surface-card);
  border: 1px solid rgba(32, 148, 243, 0.16);
}

.feed-item strong {
  display: block;
  margin-bottom: 4px;
  color: var(--role-text);
}

.feed-item p {
  margin: 0;
  color: var(--role-text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.footer-cta {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.footer-cta :deep(.el-button--primary) {
  background: linear-gradient(135deg, #1565c0, #2094f3);
  border: none;
  padding: 0 28px;
}

@media (max-width: 900px) {
  .overview-grid,
  .feed-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .hero-inner {
    padding: 24px 20px;
  }

  .hero-inner h1 {
    font-size: 26px;
  }
}
</style>
