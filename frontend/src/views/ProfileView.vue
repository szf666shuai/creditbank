<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Medal, Tickets, TrendCharts, Wallet } from '@element-plus/icons-vue'
import { fetchProfileSummary, type ProfileSummary } from '@/api/profile'
import { useAuthStore } from '@/stores/auth'
import { roleLabel } from '@/types/auth'
import CertificatePreview from '@/components/learning/CertificatePreview.vue'

const authStore = useAuthStore()

const user = computed(() => authStore.userInfo)
const isEnterprise = computed(() => authStore.isEnterprise)
const loading = ref(false)
const summary = ref<ProfileSummary | null>(null)

function formatAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

async function loadSummary() {
  loading.value = true
  try {
    const res = await fetchProfileSummary()
    if (res.code !== 200 || !res.data) throw new Error(res.message || '加载个人档案失败')
    summary.value = res.data
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载个人档案失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadSummary)
</script>

<template>
  <div class="profile-page">
    <div class="section-inner">
      <section v-if="user" class="profile-head">
        <div>
          <p class="eyebrow">Profile</p>
          <h1>个人中心</h1>
          <p class="subtitle">学习完成记录、证书和商城订单会同步沉淀到个人档案。</p>
        </div>
        <div class="identity-card">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
            <el-descriptions-item label="身份">
              <el-tag>{{ roleLabel(user.role) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="手机号">{{ user.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ user.email || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="user.orgName" label="所属企业">
              {{ user.orgName }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </section>

      <el-skeleton v-if="loading" :rows="8" animated />
      <template v-else-if="summary">
        <section class="metric-grid">
          <article class="metric-card">
            <el-icon><Wallet /></el-icon>
            <span>当前秩点</span>
            <strong>{{ formatAmount(summary.creditAccount.balance) }}</strong>
          </article>
          <article class="metric-card">
            <el-icon><TrendCharts /></el-icon>
            <span>诚信分</span>
            <strong>{{ summary.creditAccount.integrityScore }}</strong>
          </article>
          <article class="metric-card">
            <el-icon><Medal /></el-icon>
            <span>学习证书</span>
            <strong>{{ summary.certificates.length }}</strong>
          </article>
          <article class="metric-card">
            <el-icon><Tickets /></el-icon>
            <span>商城订单</span>
            <strong>{{ summary.orders.length }}</strong>
          </article>
        </section>

        <section class="content-grid">
          <article class="panel">
            <div class="panel-head">
              <h2>最近学习档案</h2>
              <el-button link type="primary" @click="$router.push('/archive')">查看全部</el-button>
            </div>
            <el-empty v-if="!summary.archives.length" description="暂无学习档案" />
            <div v-else class="archive-list">
              <div v-for="item in summary.archives" :key="item.id" class="archive-line">
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.description }}</span>
                </div>
                <el-tag size="small" type="success">+{{ formatAmount(item.creditEarned) }} 秩点</el-tag>
              </div>
            </div>
          </article>

          <article class="panel">
            <div class="panel-head">
              <h2>学习证书</h2>
              <el-button link type="primary" @click="$router.push('/resources')">继续学习</el-button>
            </div>
            <el-empty v-if="!summary.certificates.length" description="暂无学习证书" />
            <div v-else class="cert-list">
              <div v-for="cert in summary.certificates.slice(0, 3)" :key="cert.id" class="cert-line">
                <CertificatePreview :certificate="cert" class="profile-cert" />
                <div>
                  <strong>{{ cert.title }}</strong>
                  <span>编号：{{ cert.certNo }}</span>
                  <code>{{ cert.blockchainHash }}</code>
                </div>
              </div>
            </div>
          </article>

          <article class="panel wide">
            <div class="panel-head">
              <h2>商城订单</h2>
              <el-button link type="primary" @click="$router.push('/credit')">进入商城</el-button>
            </div>
            <el-empty v-if="!summary.orders.length" description="暂无订单记录" />
            <div v-else class="order-grid">
              <div v-for="order in summary.orders" :key="order.id" class="order-line">
                <div>
                  <strong>{{ order.orderNo }}</strong>
                  <span>{{ order.items.map((item) => `${item.productName} x ${item.quantity}`).join('、') }}</span>
                </div>
                <div class="order-price">
                  <strong>{{ formatAmount(order.totalCredit) }} 秩点</strong>
                  <el-tag size="small" :type="order.payStatus === 1 ? 'success' : 'warning'">
                    {{ order.payStatusName }}
                  </el-tag>
                </div>
              </div>
            </div>
          </article>
        </section>
      </template>

      <div class="quick-links">
        <el-button v-if="isEnterprise" type="primary" @click="$router.push('/enterprise')">
          进入企业中心
        </el-button>
        <el-button @click="$router.push('/archive')">学习档案</el-button>
        <el-button @click="$router.push('/resources')">学习资源</el-button>
        <el-button @click="$router.push('/credit')">秩点商城</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 32px 16px 48px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.profile-head {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
  margin-bottom: 20px;
}

.eyebrow {
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}

h1 {
  margin: 6px 0 8px;
  font-size: 30px;
  color: var(--color-text);
}

.subtitle {
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.identity-card,
.metric-card,
.panel {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.identity-card {
  padding: 16px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 18px;
}

.metric-card {
  padding: 16px;
}

.metric-card .el-icon {
  color: var(--color-primary);
  font-size: 24px;
}

.metric-card span {
  display: block;
  margin-top: 10px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.metric-card strong {
  display: block;
  margin-top: 4px;
  font-size: 24px;
  color: var(--color-text);
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.panel {
  padding: 16px;
}

.panel.wide {
  grid-column: 1 / -1;
}

.panel-head,
.archive-line,
.order-line,
.cert-line {
  display: flex;
}

.panel-head {
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-head h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 18px;
}

.archive-list,
.cert-list,
.order-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.archive-line,
.order-line {
  justify-content: space-between;
  gap: 16px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.archive-line strong,
.order-line strong,
.cert-line strong {
  display: block;
  color: var(--color-text);
  font-size: 14px;
}

.archive-line span,
.order-line span,
.cert-line span {
  display: block;
  margin-top: 4px;
  color: var(--color-text-muted);
  font-size: 12px;
  line-height: 1.5;
}

.cert-line {
  gap: 12px;
  align-items: flex-start;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.profile-cert {
  width: 180px;
  flex-shrink: 0;
}

.profile-cert :deep(.preview-actions) {
  display: none;
}

.cert-line code {
  display: block;
  margin-top: 6px;
  color: var(--color-text-muted);
  font-size: 11px;
  word-break: break-all;
  background: transparent;
  padding: 0;
}

.order-price {
  text-align: right;
  min-width: 116px;
}

.quick-links {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .profile-head,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .order-line,
  .archive-line {
    flex-direction: column;
  }

  .order-price {
    text-align: left;
  }
}
</style>
