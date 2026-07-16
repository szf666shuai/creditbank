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
    { label: '在招职位', value: data.openJobCount, hint: '当前对外招聘中', path: '/profile/enterprise/jobs' },
    { label: '进行中活动', value: data.ongoingActivityCount, hint: '学员可参与', path: '/profile/enterprise/activities' },
    { label: '待处理投递', value: data.pendingApplicationCount, hint: '等待企业回复', path: '/profile/enterprise/applications' },
    { label: '待审核转换', value: data.pendingTransferCount ?? 0, hint: '学分转入待处理', path: '/profile/enterprise/transfer-applications' },
    { label: '待回复面试', value: data.pendingInterviewCount, hint: '面试安排待确认', path: '/profile/enterprise/interviews' },
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
            <button
              v-for="item in overviewCards"
              :key="item.label"
              type="button"
              class="overview-card"
              @click="go(item.path)"
            >
              <span class="overview-label">{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <p>{{ item.hint }}</p>
              <span class="overview-go">进入 →</span>
            </button>
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
            <button type="button" class="cta-btn secondary" @click="go('/profile/enterprise/transfer-applications')">
              处理转换申请
            </button>
            <button type="button" class="cta-btn" @click="go('/profile/enterprise')">
              进入企业工作台
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.enterprise-home {
  --role-primary: #22c55e;
  --role-primary-dark: #16a34a;
  --role-primary-soft: #86efac;
  --role-surface: #ffffff;
  --role-surface-strong: #ffffff;
  --role-surface-card: #ffffff;
  --role-border: var(--nb-ink, #1a202c);
  --role-text: var(--nb-ink, #1a202c);
  --role-text-muted: #64748b;
  --role-text-on-hero: var(--nb-ink, #1a202c);
  --role-shadow: var(--nb-shadow-lg, 6px 6px 0 0 #1a202c);

  position: relative;
  min-height: calc(100vh - var(--header-height));
  padding-bottom: 48px;
  background: var(--nb-cream, #fff9f0);
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
  border-radius: 24px;
  color: var(--role-text-on-hero);
  background: #fff;
  border: 2.5px solid var(--nb-ink, #1a202c);
  box-shadow: var(--role-shadow);
  backdrop-filter: none;
}

.hero-inner::after {
  content: '';
  position: absolute;
  width: 160px;
  height: 160px;
  top: -50px;
  right: -24px;
  border-radius: 24px;
  background: var(--nb-blue, #bee3f8);
  border: 2.5px solid var(--nb-ink, #1a202c);
  opacity: 0.6;
  transform: rotate(12deg);
}

.hero-badge {
  display: inline-block;
  margin-bottom: 12px;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.06em;
  background: var(--nb-yellow, #fef08a);
  border: 2px solid var(--nb-ink, #1a202c);
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
  color: var(--nb-ink, #1a202c);
  backdrop-filter: none;
}

.hero-inner h1 {
  position: relative;
  margin: 0 0 10px;
  font-family: var(--font-heading);
  font-size: 32px;
  font-weight: 900;
  line-height: 1.25;
  color: var(--nb-ink, #1a202c);
}

.hero-desc {
  position: relative;
  margin: 0;
  max-width: 560px;
  line-height: 1.8;
  color: var(--role-text-muted);
  opacity: 1;
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
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.overview-card {
  padding: 18px;
  border-radius: 16px;
  background: #fff;
  border: 2.5px solid var(--nb-ink, #1a202c);
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
  backdrop-filter: none;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.overview-card:hover {
  background: #fff9f0;
  border-color: var(--nb-green-deep, #16a34a);
}

.overview-card:active {
  background: #dcfce7;
}

.overview-label {
  display: block;
  color: var(--role-text-muted);
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 8px;
}

.overview-card strong {
  font-size: 30px;
  font-family: var(--font-heading);
  font-weight: 900;
  color: var(--nb-ink, #1a202c);
}

.overview-card p {
  margin: 8px 0 0;
  color: var(--role-text-muted);
  font-size: 12px;
}

.overview-go {
  display: inline-block;
  margin-top: 10px;
  font-size: 12px;
  font-weight: 800;
  color: var(--nb-green-deep, #16a34a);
}

.feed-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.feed-panel {
  padding: 20px;
  border-radius: 16px;
  background: #fff;
  border: 2.5px solid var(--nb-ink, #1a202c);
  box-shadow: var(--nb-shadow, 4px 4px 0 0 #1a202c);
  backdrop-filter: none;
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
  border-bottom: 2px solid color-mix(in srgb, var(--nb-ink, #1a202c) 14%, transparent);
}

.panel-head h2 {
  margin: 0;
  font-size: 18px;
  color: var(--role-primary-dark);
}

.text-link {
  border: none;
  background: transparent;
  color: var(--nb-green-deep, #16a34a);
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
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
  background: #fff9f0;
  border: 2px solid var(--nb-ink, #1a202c);
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
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 24px;
}

.cta-btn {
  min-width: 200px;
  padding: 14px 28px;
  border-radius: 12px;
  border: 2.5px solid var(--nb-ink, #1a202c);
  background: var(--nb-green, #22c55e);
  color: #fff;
  font-size: 16px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: var(--nb-shadow, 4px 4px 0 0 #1a202c);
  transition: background 0.15s ease;
}

.cta-btn.secondary {
  background: #fff;
  color: var(--nb-ink, #1a202c);
}

.cta-btn.secondary:hover {
  background: #bbf7d0;
}

.cta-btn:hover {
  background: var(--nb-green-deep, #16a34a);
}

.cta-btn.secondary:hover {
  background: #bbf7d0;
}

.cta-btn:active {
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

@media (max-width: 1100px) {
  .overview-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
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
