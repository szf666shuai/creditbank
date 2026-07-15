<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/common/PageShell.vue'
import { useAuthStore } from '@/stores/auth'
import { roleLabel } from '@/types/auth'
import { getProfileDashboardApi, type ProfileDashboard } from '@/api/profile-dashboard'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { getDefaultHomePath } from '@/config/role-routes'
import UiIcon from '@/components/ui/UiIcon.vue'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const loadError = ref<string | null>(null)
const dashboard = ref<ProfileDashboard | null>(null)

const user = computed(() => dashboard.value?.userInfo || authStore.userInfo)
const isStudent = computed(() => authStore.isStudent)
const isEnterprise = computed(() => authStore.isEnterprise)
const isAdmin = computed(() => authStore.isAdmin)

const studentStatCards = computed(() => {
  const data = dashboard.value
  if (!data) return []
  return [
    {
      key: 'totalEarned',
      label: '累计获得秩点',
      value: data.totalEarned ?? 0,
      suffix: '分',
      icon: 'credit',
      color: '#22c55e',
      path: '/credit',
    },
    {
      key: 'messages',
      label: '未读消息',
      value: data.unreadMessageCount,
      suffix: '条',
      icon: 'message',
      color: '#0284c7',
      path: '/profile/messages',
    },
  ]
})

const enterpriseStatCards = computed(() => {
  const unread = dashboard.value?.unreadMessageCount ?? 0
  return [
    {
      key: 'workspace',
      label: '企业工作台',
      value: '进入',
      suffix: '',
      icon: 'enterprise',
      color: '#22c55e',
      path: '/profile/enterprise',
    },
    {
      key: 'enterprise-public',
      label: '企业主页',
      value: '查看',
      suffix: '',
      icon: 'view',
      color: '#059669',
      path: user.value?.orgId ? `/enterprise/${user.value.orgId}` : '/enterprise',
    },
    {
      key: 'messages',
      label: '未读消息',
      value: unread,
      suffix: '条',
      icon: 'message',
      color: '#0284c7',
      path: '/profile/messages',
    },
  ]
})

const studentQuickEntries = [
  { label: '我的简历', desc: '编辑个人简历', icon: 'resume', path: '/profile/resume', color: '#22c55e' },
  { label: '学习档案', desc: '学习记录与学分', icon: 'folder', path: '/profile/learning', color: '#059669' },
  { label: '学分转换', desc: '申请学分转换', icon: 'exchange', path: '/profile/credit-transfer', color: '#0f766e' },
  { label: '我的秩点', desc: '秩点与学习档案', icon: 'credit', path: '/credit', color: '#d97706' },
  { label: '资源商城', desc: '浏览机构课程', icon: 'course', path: '/resources', color: '#ea580c' },
  { label: '投递记录', desc: '求职投递历史', icon: 'applications', path: '/profile/applications', color: '#0284c7' },
  { label: '消息中心', desc: '查看私信通知', icon: 'message', path: '/profile/messages', color: '#0284c7' },
]

const enterpriseQuickEntries = [
  { label: '企业工作台', desc: '运营数据概览', icon: 'enterprise', path: '/profile/enterprise', color: '#22c55e' },
  { label: '课程管理', desc: '发布与管理课程', icon: 'course', path: '/profile/enterprise/courses', color: '#059669' },
  { label: '招聘管理', desc: '发布与管理职位', icon: 'job', path: '/profile/enterprise/jobs', color: '#0f766e' },
  { label: '活动管理', desc: '发布与管理活动', icon: 'activity', path: '/profile/enterprise/activities', color: '#d97706' },
  { label: '转换规则', desc: '设置学分转换规则', icon: 'exchange', path: '/profile/enterprise/transfer-rules', color: '#0284c7' },
  { label: '消息中心', desc: '查看私信通知', icon: 'message', path: '/profile/messages', color: '#0284c7' },
]

const statCards = computed(() =>
  isEnterprise.value ? enterpriseStatCards.value : studentStatCards.value,
)

const quickEntries = computed(() =>
  isEnterprise.value ? enterpriseQuickEntries : studentQuickEntries,
)

function go(path: string) {
  router.push(path)
}

async function loadDashboard() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(await getProfileDashboardApi())
    dashboard.value = data
    authStore.userInfo = data.userInfo
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isAdmin.value) {
    router.replace(getDefaultHomePath(2))
    return
  }
  loadDashboard()
})
</script>

<template>
  <PageShell v-if="!isAdmin" :loading="loading" :error="loadError" plain @retry="loadDashboard">
    <template #header>
      <div v-if="user" class="user-header">
        <div class="avatar">{{ user.realName?.charAt(0) || user.username.charAt(0) }}</div>
        <div class="user-meta">
          <div class="user-title-row">
            <h1>{{ user.realName || user.username }}</h1>
            <el-tag>{{ roleLabel(user.role) }}</el-tag>
          </div>
          <p class="user-sub">@{{ user.username }}</p>
          <p v-if="user.orgName" class="user-org">所属企业：{{ user.orgName }}</p>
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

    <el-card class="info-card">
      <template #header>
        <span>基本信息</span>
      </template>
      <el-descriptions v-if="user" :column="2" border>
        <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ user.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user.email || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="isStudent && dashboard" label="累计获得秩点">
          {{ dashboard.totalEarned }} 分
        </el-descriptions-item>
        <el-descriptions-item v-if="user.orgName" label="所属企业">
          {{ user.orgName }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <section class="page-quick-section">
      <h2>{{ isEnterprise ? '企业快捷入口' : '学习快捷入口' }}</h2>
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
  margin-bottom: 28px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  flex-shrink: 0;
  border: 1px solid var(--color-border);
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
  color: var(--color-foreground);
  font-family: var(--font-heading);
}

.user-sub {
  font-size: 14px;
  color: var(--color-muted-foreground);
  margin-bottom: 4px;
}

.user-org {
  font-size: 13px;
  color: var(--color-muted-foreground);
}

.info-card {
  margin-bottom: 28px;
  background: var(--color-card) !important;
  border: 1px solid var(--color-border-neutral) !important;
  color: var(--color-card-foreground);
  box-shadow: var(--shadow-sm);
}

.info-card :deep(.el-card__header) {
  border-bottom-color: var(--color-border-neutral);
  color: var(--color-foreground);
}
</style>
