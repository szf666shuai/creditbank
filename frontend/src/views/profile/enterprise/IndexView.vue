<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/common/PageShell.vue'
import { useAuthStore } from '@/stores/auth'
import { getEnterpriseDashboardApi, type EnterpriseDashboard } from '@/api/enterprise-dashboard'
import { getErrorMessage, unwrapApi } from '@/utils/api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const loadError = ref<string | null>(null)
const dashboard = ref<EnterpriseDashboard | null>(null)

const orgName = computed(() => dashboard.value?.orgName || authStore.userInfo?.orgName || '企业工作台')

const statCards = computed(() => {
  const data = dashboard.value
  if (!data) return []
  return [
    { key: 'jobs', label: '在招职位', value: data.openJobCount, icon: '💼', color: '#2094f3', path: '/profile/enterprise/jobs' },
    { key: 'ongoing', label: '进行中活动', value: data.ongoingActivityCount, icon: '🎯', color: '#52c41a', path: '/profile/enterprise/activities' },
    { key: 'registering', label: '报名中活动', value: data.registeringActivityCount, icon: '📅', color: '#13c2c2', path: '/profile/enterprise/activities' },
    { key: 'applications', label: '待处理投递', value: data.pendingApplicationCount, icon: '📥', color: '#fa8c16', path: '/profile/enterprise/applications' },
    { key: 'interviews', label: '待回复面试', value: data.pendingInterviewCount, icon: '🤝', color: '#722ed1', path: '/profile/enterprise/interviews' },
    { key: 'materials', label: '企业资料', value: data.materialCount, icon: '📁', color: '#eb2f96', path: '/profile/enterprise/materials' },
  ]
})

const quickEntries = [
  { label: '发布职位', desc: '新增招聘岗位', icon: '➕', path: '/profile/enterprise/jobs', color: '#2094f3' },
  { label: '发布活动', desc: '创建企业活动', icon: '🎪', path: '/profile/enterprise/activities', color: '#52c41a' },
  { label: '投递管理', desc: '查看简历投递', icon: '📋', path: '/profile/enterprise/applications', color: '#fa8c16' },
  { label: '面试邀请', desc: '管理面试安排', icon: '💬', path: '/profile/enterprise/interviews', color: '#722ed1' },
  { label: '活动邀请', desc: '邀请学员参加活动', icon: '🎟️', path: '/profile/enterprise/activity-invitations', color: '#13c2c2' },
  { label: '企业资料', desc: '发布学习资料', icon: '📎', path: '/profile/enterprise/materials', color: '#eb2f96' },
  { label: '机构信息', desc: '维护企业简介', icon: '🏢', path: '/profile/enterprise/org', color: '#2f54eb' },
]

function go(path: string) {
  router.push(path)
}

function goOrgPage() {
  const orgId = dashboard.value?.orgId || authStore.userInfo?.orgId
  if (orgId) {
    router.push(`/enterprise/${orgId}`)
  }
}

async function loadDashboard() {
  loading.value = true
  loadError.value = null
  try {
    dashboard.value = unwrapApi(await getEnterpriseDashboardApi())
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
      <div class="workbench-header">
        <div>
          <h1>{{ orgName }}</h1>
          <p>企业工作台 · 一眼掌握招聘与活动运营情况</p>
        </div>
        <el-button type="primary" plain @click="goOrgPage">查看企业主页</el-button>
      </div>
    </template>

    <el-alert
      v-if="dashboard && dashboard.joinStatus !== 1"
      type="warning"
      :closable="false"
      show-icon
      class="org-alert"
      :title="`机构状态：${dashboard.joinStatusName || '待审核'}`"
      description="加盟审核通过前，暂不可发布职位、活动、资料及发送邀请。您可先完善机构信息，等待管理员审核。"
    />
    <el-alert
      v-else-if="dashboard && dashboard.writable === false"
      type="error"
      :closable="false"
      show-icon
      class="org-alert"
      title="机构已停用"
      description="机构已被管理员停用，暂不可执行发布类操作。"
    />

    <div class="page-stat-grid">
      <button
        v-for="card in statCards"
        :key="card.key"
        type="button"
        class="page-stat-card"
        @click="go(card.path)"
      >
        <div class="page-stat-icon" :style="{ background: card.color + '18', color: card.color }">
          {{ card.icon }}
        </div>
        <div class="stat-content">
          <div class="page-stat-value">{{ card.value }}</div>
          <div class="page-stat-label">{{ card.label }}</div>
        </div>
      </button>
    </div>

    <section class="page-quick-section">
      <h2>快捷入口</h2>
      <div class="page-quick-grid">
        <button
          v-for="item in quickEntries"
          :key="item.label"
          type="button"
          class="page-quick-card"
          @click="go(item.path)"
        >
          <span class="page-quick-icon" :style="{ background: item.color + '18', color: item.color }">
            {{ item.icon }}
          </span>
          <span class="page-quick-title">{{ item.label }}</span>
          <span class="page-quick-desc">{{ item.desc }}</span>
        </button>
      </div>
    </section>
  </PageShell>
</template>

<style scoped>
.workbench-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 28px;
}

.workbench-header h1 {
  font-size: 26px;
  color: var(--color-text);
  margin-bottom: 8px;
}

.workbench-header p {
  font-size: 14px;
  color: var(--color-text-muted);
}

.org-alert {
  margin-bottom: 20px;
}
</style>
