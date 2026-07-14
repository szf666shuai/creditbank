<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { roleLabel } from '@/types/auth'
import {
  getAdminDashboardStatsApi,
  listAdminOrganizationsApi,
  listAdminProductsApi,
  listAdminReportsApi,
  type AdminDashboardStats,
  type AdminMallProduct,
  type AdminOrganization,
  type AdminForumReport,
} from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const loadError = ref('')
const stats = ref<AdminDashboardStats | null>(null)
const pendingOrgs = ref<AdminOrganization[]>([])
const pendingProducts = ref<AdminMallProduct[]>([])
const pendingReports = ref<AdminForumReport[]>([])

const user = computed(() => authStore.userInfo)

const headlineStats = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { label: '平台用户', value: s.totalUsers },
    { label: '加盟机构', value: s.activeOrganizations },
    { label: '在招职位', value: s.activeJobs },
    { label: '进行中活动', value: s.activeActivities },
  ]
})

const todoCards = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { label: '待审核机构', value: s.pendingOrganizations, path: '/profile/admin/organizations?joinStatus=0' },
    { label: '待审核商品', value: pendingProducts.value.length, path: '/profile/admin/products' },
    { label: '待处理举报', value: s.pendingReports, path: '/profile/admin/reports?status=0' },
    { label: '未读消息', value: s.unreadMessages, path: '/profile/messages' },
  ]
})

const platformRows = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { label: '学员用户', value: s.studentCount },
    { label: '企业用户', value: s.enterpriseCount },
    { label: '管理员', value: s.adminCount },
    { label: '机构总数', value: s.totalOrganizations },
    { label: '职位总数', value: s.totalJobs },
    { label: '活动总数', value: s.totalActivities },
  ]
})

function go(path: string) {
  router.push(path)
}

async function loadHomeData() {
  loading.value = true
  loadError.value = ''
  try {
    const [statsRes, orgRes, productRes, reportRes] = await Promise.all([
      getAdminDashboardStatsApi(),
      listAdminOrganizationsApi({ page: 1, pageSize: 5, joinStatus: 0 }),
      listAdminProductsApi({ page: 1, pageSize: 5, approvalStatus: 0 }),
      listAdminReportsApi({ page: 1, pageSize: 5, status: 0 }),
    ])
    stats.value = unwrapApi(statsRes)
    pendingOrgs.value = unwrapApi(orgRes).records
    pendingProducts.value = unwrapApi(productRes).records
    pendingReports.value = unwrapApi(reportRes).records
  } catch (e) {
    loadError.value = getErrorMessage(e, '平台数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadHomeData)
</script>

<template>
  <div class="role-home admin-home">
    <div class="role-home-content">
      <section class="hero-banner">
        <div class="hero-inner">
          <span class="hero-badge">平台监管门户</span>
          <h1>系统管理主页</h1>
          <p v-if="user" class="hero-desc">
            欢迎，{{ user.realName || user.username }}（{{ roleLabel(user.role) }}）。
            汇总平台运行态势与待办事项，便于快速浏览与跟进。
          </p>
        </div>
      </section>

      <div class="section-inner">
        <el-skeleton v-if="loading" :rows="8" animated />
        <el-alert v-else-if="loadError" type="warning" :title="loadError" show-icon :closable="false" />

        <template v-else>
          <section class="headline-grid">
            <article v-for="item in headlineStats" :key="item.label" class="headline-card">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </article>
          </section>

          <section class="todo-panel">
            <div class="panel-head">
              <h2>待办关注</h2>
              <span>需要优先处理的事项</span>
            </div>
            <div class="todo-grid">
              <button
                v-for="item in todoCards"
                :key="item.label"
                type="button"
                class="todo-card"
                @click="go(item.path)"
              >
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </button>
            </div>
          </section>

          <div class="feed-grid">
            <section class="feed-panel">
              <div class="panel-head">
                <h2>待审核机构</h2>
                <button type="button" class="text-link" @click="go('/profile/admin/organizations?joinStatus=0')">
                  查看全部
                </button>
              </div>
              <div v-if="pendingOrgs.length" class="feed-list">
                <article v-for="org in pendingOrgs" :key="org.id" class="feed-item">
                  <div>
                    <strong>{{ org.name }}</strong>
                    <p>{{ org.contact || '联系人未填' }} · {{ formatTime(org.createTime) }}</p>
                  </div>
                  <el-tag size="small" type="warning">{{ org.joinStatusName }}</el-tag>
                </article>
              </div>
              <el-empty v-else :image-size="64" description="暂无待审核机构" />
            </section>

            <section class="feed-panel">
              <div class="panel-head">
                <h2>待审核商品</h2>
                <button type="button" class="text-link" @click="go('/profile/admin/products')">查看全部</button>
              </div>
              <div v-if="pendingProducts.length" class="feed-list">
                <article v-for="product in pendingProducts" :key="product.id" class="feed-item">
                  <div>
                    <strong>{{ product.name }}</strong>
                    <p>{{ product.orgName || '未知企业' }} · {{ product.productTypeName }}</p>
                  </div>
                  <el-tag size="small" type="warning">{{ product.approvalStatusName }}</el-tag>
                </article>
              </div>
              <el-empty v-else :image-size="64" description="暂无待审核商品" />
            </section>

            <section class="feed-panel full-width">
              <div class="panel-head">
                <h2>待处理举报</h2>
                <button type="button" class="text-link" @click="go('/profile/admin/reports?status=0')">查看全部</button>
              </div>
              <div v-if="pendingReports.length" class="feed-list">
                <article v-for="report in pendingReports" :key="report.id" class="feed-item">
                  <div>
                    <strong>{{ report.targetTypeName }} #{{ report.targetId }}</strong>
                    <p>{{ report.reporterName || '匿名用户' }}：{{ report.reason }}</p>
                  </div>
                  <el-tag size="small" type="danger">{{ report.statusName }}</el-tag>
                </article>
              </div>
              <el-empty v-else :image-size="64" description="暂无待处理举报" />
            </section>
          </div>

          <section class="platform-panel">
            <div class="panel-head">
              <h2>平台运行概览</h2>
            </div>
            <div class="platform-grid">
              <div v-for="row in platformRows" :key="row.label" class="platform-item">
                <span>{{ row.label }}</span>
                <strong>{{ row.value }}</strong>
              </div>
            </div>
          </section>

          <div class="footer-cta">
            <button type="button" class="cta-btn" @click="go('/profile/admin')">
              进入管理后台
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-home {
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
  box-shadow: var(--role-shadow);
  border: 2.5px solid var(--nb-ink, #1a202c);
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
  background: var(--nb-purple, #ddd6fe);
  border: 2.5px solid var(--nb-ink, #1a202c);
  opacity: 0.65;
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
  background: var(--nb-pink, #fecdd3);
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
  color: var(--nb-ink, #1a202c);
}

.hero-desc {
  position: relative;
  margin: 0;
  max-width: 640px;
  line-height: 1.8;
  color: var(--role-text-muted);
  opacity: 1;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 16px;
}

.headline-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.headline-card {
  padding: 18px;
  border-radius: 16px;
  background: #fff;
  border: 2.5px solid var(--nb-ink, #1a202c);
  box-shadow: var(--role-shadow);
  backdrop-filter: none;
}

.headline-card span {
  display: block;
  color: var(--role-text-muted);
  font-size: 13px;
  margin-bottom: 8px;
}

.headline-card strong {
  font-size: 30px;
  color: var(--role-primary-dark);
}

.todo-panel,
.feed-panel,
.platform-panel {
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 16px;
  background: #fff;
  border: 2.5px solid var(--nb-ink, #1a202c);
  box-shadow: var(--role-shadow);
  backdrop-filter: none;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 2px solid color-mix(in srgb, var(--nb-ink, #1a202c) 14%, transparent);
}

.panel-head h2 {
  margin: 0;
  font-size: 18px;
  color: var(--role-primary-dark);
}

.panel-head span {
  color: var(--role-text-muted);
  font-size: 13px;
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.todo-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 16px 18px;
  border-radius: 14px;
  text-align: left;
  cursor: pointer;
  background: #fff9f0;
  border: 2.5px solid var(--nb-ink, #1a202c);
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
  transition: background 0.15s ease, border-color 0.15s ease;
}

.todo-card:hover {
  background: #ffffff;
  border-color: var(--nb-green-deep, #16a34a);
}

.todo-card:active {
  background: #dcfce7;
}

.todo-card span {
  display: block;
  color: var(--role-text-muted);
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 0;
}

.todo-card strong {
  font-size: 28px;
  font-weight: 900;
  font-family: var(--font-heading);
  color: var(--nb-ink, #1a202c);
  line-height: 1.1;
}

.todo-card::after {
  content: '进入 →';
  margin-top: 4px;
  font-size: 12px;
  font-weight: 800;
  color: var(--nb-green-deep, #16a34a);
}

.feed-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.feed-panel.full-width {
  grid-column: 1 / -1;
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

.platform-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.platform-item {
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff9f0;
  border: 2px solid var(--nb-ink, #1a202c);
}

.platform-item span {
  display: block;
  color: var(--role-text-muted);
  font-size: 12px;
  margin-bottom: 6px;
}

.platform-item strong {
  font-size: 22px;
  color: var(--role-primary-dark);
}

.footer-cta {
  display: flex;
  justify-content: center;
  margin-top: 8px;
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

.cta-btn:hover {
  background: var(--nb-green-deep, #16a34a);
}

.cta-btn:active {
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

@media (max-width: 900px) {
  .headline-grid,
  .todo-grid,
  .feed-grid,
  .platform-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .headline-grid,
  .todo-grid,
  .feed-grid,
  .platform-grid {
    grid-template-columns: 1fr;
  }

  .hero-inner {
    padding: 24px 20px;
  }

  .hero-inner h1 {
    font-size: 26px;
  }
}
</style>
