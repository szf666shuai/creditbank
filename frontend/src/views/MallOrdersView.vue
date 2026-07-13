<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DocumentCopy, Goods, Reading, Tickets, Wallet } from '@element-plus/icons-vue'
import {
  fetchMallOrders,
  payMallOrder,
  type MallOrder,
} from '@/api/mall'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const orderLoading = ref(false)
const listLoading = ref(false)
const orders = ref<MallOrder[]>([])
const paymentVisible = ref(false)
const pendingOrder = ref<MallOrder | null>(null)
const paymentResultNo = ref('')

function formatAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

function requireLogin() {
  if (authStore.isLoggedIn) return true
  router.push({ path: '/login', query: { redirect: '/credit/orders' } })
  return false
}

async function loadOrders() {
  if (!authStore.isLoggedIn) return
  listLoading.value = true
  try {
    const res = await fetchMallOrders(20)
    if (res.code === 200 && res.data) {
      orders.value = res.data
    }
  } finally {
    listLoading.value = false
  }
}

function viewProductDetail(productId: number) {
  router.push(`/credit/products/${productId}`)
}

function viewPurchasedCourse(courseId?: number) {
  if (!courseId) {
    ElMessage.warning('该商品暂未关联课程资源')
    return
  }
  router.push({ path: '/resources', query: { courseId: String(courseId) } })
}

function purchasedCourseItems(order?: MallOrder | null) {
  return order?.items.filter((item) => item.productType === 3 && item.refCourseId) || []
}

function openPay(order: MallOrder) {
  pendingOrder.value = order
  paymentResultNo.value = ''
  paymentVisible.value = true
}

async function payOrder(order: MallOrder) {
  if (!requireLogin()) return
  orderLoading.value = true
  try {
    const res = await payMallOrder(order.id)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '支付失败')
    paymentResultNo.value = res.data.payNo
    const balance = res.data.creditChange?.balanceAfter
    ElMessage.success(balance === undefined ? '支付成功' : `支付成功，剩余 ${formatAmount(balance)} 秩点`)
    await loadOrders()
    pendingOrder.value = orders.value.find((item) => item.id === order.id) || order
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '支付失败')
  } finally {
    orderLoading.value = false
  }
}

async function copyRedemptionCode(code?: string) {
  if (!code) return
  await navigator.clipboard.writeText(code)
  ElMessage.success('兑换码已复制')
}

onMounted(loadOrders)
</script>

<template>
  <div class="mall-orders-page">
    <div class="page-glow" aria-hidden="true" />
    <div class="section-inner">
      <nav class="mall-subnav">
        <router-link to="/credit" class="subnav-item">商品兑换</router-link>
        <router-link to="/credit/orders" class="subnav-item is-active">订单记录</router-link>
      </nav>

      <section class="mall-header">
        <p class="eyebrow">Rank Point Mall</p>
        <h1>订单记录</h1>
        <p class="subtitle">查看兑换订单、支付状态、兑换码与已购课程。</p>
      </section>

      <section class="orders-section">
        <div class="section-title">
          <el-icon><Tickets /></el-icon>
          <h2>我的订单</h2>
          <span v-if="orders.length">共 {{ orders.length }} 笔</span>
        </div>

        <div v-if="!authStore.isLoggedIn" class="state-card">
          <p>登录后可查看订单和支付记录</p>
          <button type="button" class="btn-primary" @click="requireLogin()">去登录</button>
        </div>

        <div v-else-if="listLoading" class="state-card muted">正在加载订单…</div>

        <el-empty v-else-if="!orders.length" description="暂无订单，去商城兑换看看吧">
          <button type="button" class="btn-primary" @click="router.push('/credit')">去兑换</button>
        </el-empty>

        <div v-else class="order-list">
          <article v-for="order in orders" :key="order.id" class="order-card">
            <header class="order-head">
              <div class="order-id">
                <strong>{{ order.orderNo }}</strong>
                <time>{{ order.createTime }}</time>
              </div>
              <span
                class="status-chip"
                :class="order.payStatus === 1 ? 'is-paid' : 'is-pending'"
              >
                {{ order.payStatusName }}
              </span>
            </header>

            <div class="order-items">
              <div v-for="item in order.items" :key="item.id" class="order-item">
                <div class="order-item__main">
                  <div class="item-name">
                    <el-icon><Goods /></el-icon>
                    <span>{{ item.productName }}</span>
                    <em>× {{ item.quantity }}</em>
                  </div>
                  <button
                    v-if="item.redemptionCode"
                    type="button"
                    class="code-btn"
                    title="点击复制兑换码"
                    @click="copyRedemptionCode(item.redemptionCode)"
                  >
                    <el-icon><DocumentCopy /></el-icon>
                    兑换码 {{ item.redemptionCode }}
                  </button>
                </div>
                <div class="order-item__actions">
                  <button type="button" class="btn-ghost" @click="viewProductDetail(item.productId)">
                    商品详情
                  </button>
                  <button
                    v-if="order.payStatus === 1 && item.productType === 3"
                    type="button"
                    class="btn-ghost is-accent"
                    @click="viewPurchasedCourse(item.refCourseId)"
                  >
                    <el-icon><Reading /></el-icon>
                    查看课程
                  </button>
                </div>
              </div>
            </div>

            <footer class="order-foot">
              <div class="order-amount">
                <span>合计</span>
                <strong>{{ formatAmount(order.totalCredit) }}</strong>
                <em>秩点</em>
              </div>
              <button
                v-if="order.payStatus === 0"
                type="button"
                class="btn-primary"
                :disabled="orderLoading"
                @click="openPay(order)"
              >
                <el-icon><Wallet /></el-icon>
                去支付
              </button>
            </footer>
          </article>
        </div>
      </section>

      <el-dialog
        v-model="paymentVisible"
        class="orders-pay-dialog"
        width="520px"
        align-center
        destroy-on-close
      >
        <template #header>
          <div class="dialog-head">
            <p class="dialog-kicker">Payment</p>
            <h3>{{ paymentResultNo ? '支付结果' : '订单支付' }}</h3>
          </div>
        </template>
        <div v-if="pendingOrder" class="payment-sheet">
          <template v-if="paymentResultNo">
            <div class="pay-success">
              <h4>支付成功</h4>
              <p>支付流水号：{{ paymentResultNo }}</p>
            </div>
            <div v-if="purchasedCourseItems(pendingOrder).length" class="payment-course-actions">
              <button
                v-for="item in purchasedCourseItems(pendingOrder)"
                :key="item.id"
                type="button"
                class="btn-primary"
                @click="viewPurchasedCourse(item.refCourseId)"
              >
                查看《{{ item.productName }}》
              </button>
            </div>
            <div class="payment-redemption-list">
              <button
                v-for="item in pendingOrder.items.filter((line) => line.redemptionCode)"
                :key="item.id"
                type="button"
                class="code-btn block"
                @click="copyRedemptionCode(item.redemptionCode)"
              >
                {{ item.productName }}：{{ item.redemptionCode }}（点击复制）
              </button>
            </div>
          </template>
          <template v-else>
            <div class="payment-order-no">订单号：{{ pendingOrder.orderNo }}</div>
            <div class="payment-items">
              <div v-for="item in pendingOrder.items" :key="item.id">
                <span>{{ item.productName }} × {{ item.quantity }}</span>
                <strong>{{ formatAmount(item.priceCredit * item.quantity) }} 秩点</strong>
              </div>
            </div>
            <div class="payment-total">
              <span>应付合计</span>
              <strong>{{ formatAmount(pendingOrder.totalCredit) }} 秩点</strong>
            </div>
            <div class="tip-banner">本项目使用本地模拟支付，支付结果与流水记录写入本地数据库。</div>
          </template>
        </div>
        <template #footer>
          <button type="button" class="btn-ghost" @click="paymentVisible = false">
            {{ paymentResultNo ? '关闭' : '稍后支付' }}
          </button>
          <button
            v-if="pendingOrder && !paymentResultNo"
            type="button"
            class="btn-primary"
            :disabled="orderLoading"
            @click="payOrder(pendingOrder)"
          >
            {{ orderLoading ? '支付中…' : '确认支付' }}
          </button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped>
.mall-orders-page {
  --mall-accent: #38bdf8;
  position: relative;
  padding: 24px 16px 56px;
  min-height: calc(100vh - var(--header-height));
  overflow: hidden;
}

.page-glow {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse at 14% 0%, rgba(56, 189, 248, 0.14), transparent 40%),
    radial-gradient(ellipse at 90% 18%, rgba(14, 165, 233, 0.1), transparent 42%);
}

.section-inner {
  position: relative;
  z-index: 1;
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.mall-subnav {
  display: flex;
  gap: 8px;
  margin-bottom: 18px;
}

.subnav-item {
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 14px;
  color: rgba(226, 232, 240, 0.78);
  text-decoration: none;
  background: rgba(15, 23, 42, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.25);
  transition: all 0.2s;
  backdrop-filter: blur(8px);
}

.subnav-item:hover,
.subnav-item.is-active {
  color: #e0f2fe;
  border-color: rgba(56, 189, 248, 0.5);
  background: rgba(14, 165, 233, 0.22);
}

.mall-header {
  margin-bottom: 22px;
  color: #f8fafc;
}

.eyebrow {
  margin: 0;
  color: #7dd3fc;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

h1 {
  margin: 6px 0 8px;
  font-size: clamp(28px, 4vw, 34px);
  color: #f8fafc;
}

.subtitle {
  margin: 0;
  max-width: 680px;
  color: rgba(186, 230, 253, 0.78);
  line-height: 1.7;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e2e8f0;
  margin-bottom: 14px;
}

.section-title h2 {
  font-size: 20px;
  margin: 0;
}

.section-title span {
  margin-left: auto;
  font-size: 12px;
  color: rgba(147, 197, 253, 0.7);
}

.state-card {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px;
  border-radius: 14px;
  background: rgba(8, 20, 40, 0.5);
  border: 1px solid rgba(125, 211, 252, 0.2);
  color: rgba(186, 230, 253, 0.88);
}

.state-card.muted {
  justify-content: center;
  color: rgba(147, 197, 253, 0.7);
}

.mall-orders-page :deep(.el-empty__description p) {
  color: rgba(186, 230, 253, 0.7);
}

.order-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}

.order-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  border-radius: 16px;
  background:
    radial-gradient(ellipse at 0% 0%, rgba(56, 189, 248, 0.1), transparent 46%),
    rgba(8, 18, 36, 0.68);
  border: 1px solid rgba(125, 211, 252, 0.22);
  box-shadow: 0 14px 32px rgba(0, 0, 0, 0.22);
  backdrop-filter: blur(12px);
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.order-id strong {
  display: block;
  font-size: 14px;
  color: #e0f2fe;
  word-break: break-all;
  line-height: 1.4;
}

.order-id time {
  display: block;
  margin-top: 6px;
  color: rgba(147, 197, 253, 0.68);
  font-size: 12px;
}

.status-chip {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 650;
  border: 1px solid transparent;
}

.status-chip.is-paid {
  color: #86efac;
  background: rgba(34, 197, 94, 0.16);
  border-color: rgba(34, 197, 94, 0.35);
}

.status-chip.is-pending {
  color: #fde68a;
  background: rgba(245, 158, 11, 0.16);
  border-color: rgba(245, 158, 11, 0.35);
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(8, 24, 48, 0.45);
  border: 1px solid rgba(125, 211, 252, 0.14);
}

.item-name {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #e2e8f0;
  font-size: 14px;
  font-weight: 600;
}

.item-name .el-icon {
  color: #38bdf8;
}

.item-name em {
  font-style: normal;
  color: rgba(147, 197, 253, 0.7);
  font-weight: 500;
}

.code-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px dashed rgba(56, 189, 248, 0.4);
  background: rgba(56, 189, 248, 0.1);
  color: #7dd3fc;
  font: inherit;
  font-size: 12px;
  cursor: pointer;
  text-align: left;
}

.code-btn:hover {
  background: rgba(56, 189, 248, 0.18);
}

.code-btn.block {
  display: flex;
  width: 100%;
  margin-top: 0;
}

.order-item__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-top: 4px;
  border-top: 1px solid rgba(125, 211, 252, 0.12);
}

.order-amount {
  display: flex;
  align-items: baseline;
  gap: 6px;
  color: rgba(147, 197, 253, 0.72);
  font-size: 12px;
}

.order-amount strong {
  color: #38bdf8;
  font-size: 22px;
  font-weight: 760;
}

.order-amount em {
  font-style: normal;
  color: rgba(186, 230, 253, 0.8);
}

.btn-primary,
.btn-ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 10px;
  border: 0;
  cursor: pointer;
  font-size: 13px;
  font-weight: 650;
  transition: filter 0.2s, background 0.2s, border-color 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #38bdf8, #0284c7);
  color: #f8fafc;
  box-shadow: 0 10px 22px rgba(14, 165, 233, 0.25);
}

.btn-primary:hover:not(:disabled) {
  filter: brightness(1.06);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-ghost {
  background: transparent;
  color: #bae6fd;
  border: 1px solid rgba(125, 211, 252, 0.28);
}

.btn-ghost:hover,
.btn-ghost.is-accent {
  background: rgba(56, 189, 248, 0.12);
  border-color: rgba(56, 189, 248, 0.42);
}

.dialog-head h3 {
  margin: 0;
  color: #e0f2fe;
  font-size: 18px;
}

.dialog-kicker {
  margin: 0 0 4px;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(125, 211, 252, 0.9);
  font-weight: 700;
}

.payment-sheet {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.payment-order-no {
  color: rgba(186, 230, 253, 0.75);
  font-size: 13px;
}

.payment-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  background: rgba(8, 24, 48, 0.55);
  border-radius: 12px;
  border: 1px solid rgba(125, 211, 252, 0.16);
}

.payment-items div,
.payment-total {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  color: rgba(226, 232, 240, 0.9);
}

.payment-total {
  align-items: baseline;
}

.payment-total strong {
  color: #38bdf8;
  font-size: 20px;
}

.tip-banner {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(14, 165, 233, 0.12);
  border: 1px solid rgba(56, 189, 248, 0.28);
  color: rgba(186, 230, 253, 0.88);
  font-size: 13px;
  line-height: 1.6;
}

.pay-success {
  padding: 18px;
  border-radius: 14px;
  text-align: center;
  background: rgba(8, 28, 24, 0.55);
  border: 1px solid rgba(52, 211, 153, 0.28);
}

.pay-success h4 {
  margin: 0 0 6px;
  color: #a7f3d0;
  font-size: 18px;
}

.pay-success p {
  margin: 0;
  color: rgba(167, 243, 208, 0.78);
  font-size: 13px;
  word-break: break-all;
}

.payment-course-actions,
.payment-redemption-list {
  display: grid;
  gap: 8px;
}

@media (max-width: 720px) {
  .order-list {
    grid-template-columns: 1fr;
  }

  .order-foot {
    flex-direction: column;
    align-items: stretch;
  }

  .btn-primary,
  .btn-ghost {
    width: 100%;
  }
}
</style>

<style>
.orders-pay-dialog.el-dialog {
  background:
    radial-gradient(ellipse at 12% 0%, rgba(56, 189, 248, 0.16), transparent 42%),
    rgba(8, 18, 36, 0.96);
  border: 1px solid rgba(125, 211, 252, 0.28);
  border-radius: 16px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
}

.orders-pay-dialog .el-dialog__header {
  margin-right: 0;
  padding: 16px 20px 12px;
  border-bottom: 1px solid rgba(147, 197, 253, 0.14);
}

.orders-pay-dialog .el-dialog__headerbtn .el-dialog__close {
  color: rgba(186, 230, 253, 0.78);
}

.orders-pay-dialog .el-dialog__body {
  padding: 16px 20px;
  color: #e2e8f0;
}

.orders-pay-dialog .el-dialog__footer {
  padding: 12px 20px 18px;
  border-top: 1px solid rgba(147, 197, 253, 0.14);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
