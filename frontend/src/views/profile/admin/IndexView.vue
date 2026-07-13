<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/common/PageShell.vue'
import { useAuthStore } from '@/stores/auth'
import { roleLabel } from '@/types/auth'
import { getAdminDashboardStatsApi, type AdminDashboardStats } from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import UiIcon from '@/components/ui/UiIcon.vue'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const loadError = ref<string | null>(null)
const stats = ref<AdminDashboardStats | null>(null)

const user = computed(() => authStore.userInfo)

const statCards = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { key: 'users', label: '平台用户', value: s.totalUsers, suffix: '人', icon: 'users', color: '#52c41a', path: '/profile/admin/users' },
    { key: 'orgs', label: '待审核机构', value: s.pendingOrganizations, suffix: '家', icon: 'school', color: '#2094f3', path: '/profile/admin/organizations?joinStatus=0' },
    { key: 'reports', label: '待处理举报', value: s.pendingReports, suffix: '条', icon: 'report', color: '#fa541c', path: '/profile/admin/reports?status=0' },
    { key: 'messages', label: '未读消息', value: s.unreadMessages, suffix: '条', icon: 'message', color: '#722ed1', path: '/profile/messages' },
  ]
})

const quickEntries = [
  { label: '机构加盟', desc: '审核与管理加盟机构', icon: 'school', path: '/profile/admin/organizations', color: '#2094f3' },
  { label: '用户管理', desc: '账号启停与企业用户创建', icon: 'users', path: '/profile/admin/users', color: '#52c41a' },
  { label: '系统通知', desc: '向平台用户发送通知', icon: 'notify', path: '/profile/admin/notifications', color: '#722ed1' },
  { label: '举报处理', desc: '论坛内容举报审核', icon: 'report', path: '/profile/admin/reports', color: '#fa541c' },
  { label: '诚信监察', desc: '全平台诚信变动记录', icon: 'integrity', path: '/profile/admin/integrity', color: '#fa8c16' },
  { label: '秩点监察', desc: '全平台秩点流水', icon: 'credit', path: '/profile/admin/credit', color: '#13c2c2' },
  { label: '招聘监管', desc: '职位上架与下架', icon: 'job', path: '/profile/admin/jobs', color: '#2f54eb' },
  { label: '活动监管', desc: '活动上架与下架', icon: 'activity', path: '/profile/admin/activities', color: '#eb2f96' },
  { label: '消息中心', desc: '查看平台私信', icon: 'message', path: '/profile/messages', color: '#595959' },
]

const overviewRows = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { label: '学员用户', value: s.studentCount },
    { label: '企业用户', value: s.enterpriseCount },
    { label: '管理员', value: s.adminCount },
    { label: '加盟机构', value: s.activeOrganizations },
    { label: '在招职位', value: s.activeJobs },
    { label: '进行中活动', value: s.activeActivities },
  ]
})

function go(path: string) {
  router.push(path)
}

async function loadDashboard() {
  loading.value = true
  loadError.value = null
  try {
    stats.value = unwrapApi(await getAdminDashboardStatsApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <PageShell :loading="loading" :error="loadError" plain @retry="loadDashboard">
    <template #header>
      <div v-if="user" class="user-header">
        <div class="avatar">{{ user.realName?.charAt(0) || user.username.charAt(0) }}</div>
        <div class="user-meta">
          <div class="user-title-row">
            <h1>系统管理台</h1>
            <el-tag type="danger">{{ roleLabel(user.role) }}</el-tag>
          </div>
          <p class="user-sub">{{ user.realName || user.username }} · 平台治理与审核入口</p>
        </div>
      </div>
    </template>

    <div class="page-stat-grid">
      <button
        v-for="card in statCards"
        :key="card.key"
        type="button"
        class="page-stat-card"
        @click="go(card.path)"
      >
        <div class="page-stat-icon" :style="{ background: card.color + '18', color: card.color }">
          <UiIcon :name="card.icon" :size="22" :color="card.color" />
        </div>
        <div class="stat-content">
          <div class="page-stat-value">
            {{ card.value }}<span v-if="card.suffix" class="page-stat-suffix">{{ card.suffix }}</span>
          </div>
          <div class="page-stat-label">{{ card.label }}</div>
        </div>
      </button>
    </div>

    <section v-if="overviewRows.length" class="overview-panel">
      <h2>平台概览</h2>
      <div class="overview-grid">
        <div v-for="item in overviewRows" :key="item.label" class="overview-item">
          <span class="overview-value">{{ item.value }}</span>
          <span class="overview-label">{{ item.label }}</span>
        </div>
      </div>
    </section>

    <section class="page-quick-section">
      <h2>管理入口</h2>
      <div class="page-quick-grid">
        <button
          v-for="item in quickEntries"
          :key="item.label"
          type="button"
          class="page-quick-card"
          @click="go(item.path)"
        >
          <span class="page-quick-icon" :style="{ background: item.color + '18', color: item.color }">
            <UiIcon :name="item.icon" :size="22" :color="item.color" />
          </span>
          <span class="page-quick-title">{{ item.label }}</span>
          <span class="page-quick-desc">{{ item.desc }}</span>
        </button>
      </div>
    </section>
  </PageShell>
</template>

<style scoped>
.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: var(--nb-pink, #fecdd3);
  color: var(--nb-ink, #1a202c);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 900;
  flex-shrink: 0;
  border: 2.5px solid var(--nb-ink, #1a202c);
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
}

.user-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.user-title-row h1 {
  font-size: 26px;
  color: #1a202c;
}

.user-sub {
  font-size: 14px;
  color: rgba(148, 163, 184, 0.9);
}

.overview-panel {
  margin-bottom: 28px;
}

.overview-panel h2 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #1a202c;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.overview-item {
  padding: 16px;
  background: #fff;
  border: 2.5px solid var(--nb-ink, #1a202c);
  border-radius: 14px;
  text-align: center;
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
  backdrop-filter: none;
}

.overview-value {
  display: block;
  font-size: 24px;
  font-weight: 900;
  font-family: var(--font-heading);
  color: var(--nb-green-deep, #16a34a);
}

.overview-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-muted-foreground);
}
</style>
