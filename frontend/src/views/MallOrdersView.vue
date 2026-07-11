<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Tickets } from '@element-plus/icons-vue'
import {
  fetchMallOrders,
  payMallOrder,
  type MallOrder,
} from '@/api/mall'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const orderLoading = ref(false)
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
  const res = await fetchMallOrders(20)
  if (res.code === 200 && res.data) {
    orders.value = res.data
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
  <div class="mall-page">
    <div class="section-inner">
      <nav class="mall-subnav">
        <router-link to="/credit" class="subnav-item">商品兑换</router-link>
        <router-link to="/credit/orders" class="subnav-item is-active">订单记录</router-link>
      </nav>

      <section class="mall-header">
        <div>
          <p class="eyebrow">Credit Mall</p>
          <h1>订单记录</h1>
          <p class="subtitle">查看兑换订单、支付状态、兑换码与已购课程。</p>
        </div>
      </section>

      <section class="orders-section">
        <div class="section-title">
          <el-icon><Tickets /></el-icon>
          <h2>我的订单</h2>
        </div>
        <el-empty v-if="authStore.isLoggedIn && !orders.length" description="暂无订单" />
        <el-alert v-else-if="!authStore.isLoggedIn" type="info" show-icon :closable="false">
          登录后可查看订单和支付记录
        </el-alert>
        <div v-else class="order-list">
          <article v-for="order in orders" :key="order.id" class="order-card">
            <div class="order-head">
              <div>
                <strong>{{ order.orderNo }}</strong>
                <span>{{ order.createTime }}</span>
              </div>
              <el-tag :type="order.payStatus === 1 ? 'success' : 'warning'">
                {{ order.payStatusName }}
              </el-tag>
            </div>
            <div class="order-items">
              <div v-for="item in order.items" :key="item.id" class="order-item-line">
                <div>
                  <span>{{ item.productName }} x {{ item.quantity }}</span>
                  <button
                    v-if="item.redemptionCode"
                    type="button"
                    class="redemption-code"
                    title="点击复制兑换码"
                    @click="copyRedemptionCode(item.redemptionCode)"
                  >
                    兑换码：{{ item.redemptionCode }}
                  </button>
                </div>
                <div class="order-item-actions">
                  <el-button size="small" @click="viewProductDetail(item.productId)">商品详情</el-button>
                  <el-button
                    v-if="order.payStatus === 1 && item.productType === 3"
                    size="small"
                    type="primary"
                    plain
                    @click="viewPurchasedCourse(item.refCourseId)"
                  >
                    查看课程
                  </el-button>
                </div>
              </div>
            </div>
            <div class="order-foot">
              <strong>{{ formatAmount(order.totalCredit) }} 秩点</strong>
              <el-button
                v-if="order.payStatus === 0"
                size="small"
                type="primary"
                :loading="orderLoading"
                @click="pendingOrder = order; paymentResultNo = ''; paymentVisible = true"
              >
                去支付
              </el-button>
            </div>
          </article>
        </div>
      </section>

      <el-dialog v-model="paymentVisible" title="订单支付" width="520px">
        <div v-if="pendingOrder" class="payment-sheet">
          <template v-if="paymentResultNo">
            <el-result
              icon="success"
              title="支付成功"
              :sub-title="`支付流水号：${paymentResultNo}`"
            />
            <div v-if="purchasedCourseItems(pendingOrder).length" class="payment-course-actions">
              <el-button
                v-for="item in purchasedCourseItems(pendingOrder)"
                :key="item.id"
                type="primary"
                @click="viewPurchasedCourse(item.refCourseId)"
              >
                查看《{{ item.productName }}》
              </el-button>
            </div>
            <div class="payment-redemption-list">
              <button
                v-for="item in pendingOrder.items.filter((line) => line.redemptionCode)"
                :key="item.id"
                type="button"
                class="redemption-code"
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
            <el-alert
              title="本项目使用本地模拟支付，支付结果与流水记录写入本地数据库。"
              type="info"
              show-icon
              :closable="false"
            />
          </template>
        </div>
        <template #footer>
          <el-button @click="paymentVisible = false">{{ paymentResultNo ? '关闭' : '稍后支付' }}</el-button>
          <el-button
            v-if="pendingOrder && !paymentResultNo"
            type="primary"
            :loading="orderLoading"
            @click="payOrder(pendingOrder)"
          >
            确认支付
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped>
.mall-page {
  padding: 32px 16px 56px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.mall-subnav {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.subnav-item {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
  text-decoration: none;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  transition: all 0.2s;
}

.subnav-item:hover,
.subnav-item.is-active {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

.mall-header {
  margin-bottom: 22px;
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
  max-width: 680px;
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 14px;
}

.section-title h2 {
  font-size: 20px;
  margin: 0;
}

.order-head,
.order-foot,
.order-item-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.order-item-line {
  width: 100%;
}

.order-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.redemption-code {
  display: block;
  margin-top: 5px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-primary);
  font: inherit;
  cursor: pointer;
}

.order-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 14px;
}

.order-card {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 14px;
}

.order-head strong {
  display: block;
  font-size: 14px;
  color: var(--color-text);
}

.order-head span {
  display: block;
  margin-top: 3px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.order-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px 0;
}

.order-items span {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 4px 8px;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.payment-sheet {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-order-no {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.payment-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  background: #f8fafc;
  border-radius: 8px;
}

.payment-items div,
.payment-total {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.payment-total strong {
  color: #0f8f68;
  font-size: 20px;
}

.payment-course-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.payment-redemption-list {
  display: grid;
  gap: 8px;
  text-align: center;
}
</style>
