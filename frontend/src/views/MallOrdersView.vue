<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  fetchMallOrders,
  payMallOrder,
  type MallOrder,
} from '@/api/mall'
import { useAuthStore } from '@/stores/auth'
import PageShell from '@/components/common/PageShell.vue'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const orderLoading = ref(false)
const orders = ref<MallOrder[]>([])
const paymentVisible = ref(false)
const pendingOrder = ref<MallOrder | null>(null)
const paymentResultNo = ref('')

function formatAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

async function loadOrders() {
  if (!authStore.isLoggedIn) {
    orders.value = []
    return
  }
  loading.value = true
  try {
    const res = await fetchMallOrders(50)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '加载订单失败')
    orders.value = res.data
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载订单失败')
  } finally {
    loading.value = false
  }
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
  orderLoading.value = true
  try {
    const res = await payMallOrder(order.id)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '支付失败')
    paymentResultNo.value = res.data.payNo
    const balance = res.data.creditChange?.balanceAfter
    ElMessage.success(balance === undefined ? '支付成功' : `支付成功，剩余 ${formatAmount(balance)} 学分`)
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

function viewProductDetail(productId: number) {
  router.push(`/credit/products/${productId}`)
}

function openPay(order: MallOrder) {
  pendingOrder.value = order
  paymentResultNo.value = ''
  paymentVisible.value = true
}

onMounted(loadOrders)
</script>

<template>
  <div class="orders-page">
    <PageShell
      title="订单记录"
      description="查看积分商城兑换订单、支付状态与兑换码"
      :loading="loading"
    >
      <template #actions>
        <el-button type="primary" plain @click="router.push('/credit')">返回商城</el-button>
      </template>

      <el-alert
        v-if="!authStore.isLoggedIn"
        type="info"
        show-icon
        :closable="false"
        title="登录后可查看订单和支付记录"
      />

      <el-empty
        v-else-if="!orders.length"
        description="暂无订单"
        :image-size="80"
      >
        <el-button type="primary" @click="router.push('/credit')">去兑换商品</el-button>
      </el-empty>

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
            <strong>{{ formatAmount(order.totalCredit) }} 学分</strong>
            <el-button
              v-if="order.payStatus === 0"
              size="small"
              type="primary"
              :loading="orderLoading"
              @click="openPay(order)"
            >
              去支付
            </el-button>
          </div>
        </article>
      </div>
    </PageShell>

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
        </template>
        <template v-else>
          <div class="payment-order-no">订单号：{{ pendingOrder.orderNo }}</div>
          <div class="payment-items">
            <div v-for="item in pendingOrder.items" :key="item.id">
              <span>{{ item.productName }} × {{ item.quantity }}</span>
              <strong>{{ formatAmount(item.priceCredit * item.quantity) }} 学分</strong>
            </div>
          </div>
          <div class="payment-total">
            <span>应付合计</span>
            <strong>{{ formatAmount(pendingOrder.totalCredit) }} 学分</strong>
          </div>
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
</template>

<style scoped>
.orders-page {
  padding: 32px 20px 64px;
}

.orders-page :deep(.page-shell) {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.orders-page :deep(.page-card) {
  background: #fff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.orders-page :deep(.page-header__main h1) {
  color: #0f172a;
}

.orders-page :deep(.page-header__main p) {
  color: #64748b;
}

.order-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 18px;
}

.order-card {
  padding: 18px;
  border: 1px solid #e8eef5;
  border-radius: 12px;
  background: #fff;
}

.order-head,
.order-foot,
.order-item-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.order-head strong {
  display: block;
  color: #0f172a;
  margin-bottom: 4px;
}

.order-head span {
  color: #94a3b8;
  font-size: 12px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 14px 0;
}

.order-items span {
  display: inline-block;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 4px 8px;
  color: #475569;
  font-size: 12px;
}

.order-item-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.order-foot strong {
  color: #0f8f68;
  font-size: 16px;
}

.redemption-code {
  display: block;
  margin-top: 5px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #0b5cab;
  font: inherit;
  cursor: pointer;
}

.payment-sheet {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-order-no {
  color: #64748b;
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
  gap: 8px;
  justify-content: center;
}
</style>
