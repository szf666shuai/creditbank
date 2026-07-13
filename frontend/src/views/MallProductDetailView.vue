<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Goods, ShoppingCart, Ticket, View } from '@element-plus/icons-vue'
import {
  createMallOrder,
  fetchMallOrder,
  fetchMallProduct,
  payMallOrder,
  type MallOrder,
  type MallProduct,
} from '@/api/mall'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const product = ref<MallProduct | null>(null)
const purchasedOrder = ref<MallOrder | null>(null)
const loading = ref(false)
const paying = ref(false)

const redemptionItem = computed(() =>
  purchasedOrder.value?.items.find((item) => item.productId === product.value?.id),
)

const deliveryText = computed(() => {
  if (!product.value) return ''
  if (product.value.productType === 3) return '平台课程自动开通 + 兑换码'
  if (product.value.productType === 2) return '支付后发放兑换码'
  return '订单记录核验'
})

function formatAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

async function loadProduct() {
  const productId = Number(route.params.productId)
  if (!productId) {
    ElMessage.error('商品参数无效')
    router.replace('/credit')
    return
  }
  loading.value = true
  try {
    const res = await fetchMallProduct(productId)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '加载商品详情失败')
    product.value = res.data
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载商品详情失败')
  } finally {
    loading.value = false
  }
}

async function redeemNow() {
  if (!product.value) return
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  paying.value = true
  try {
    const createRes = await createMallOrder({
      items: [{ productId: product.value.id, quantity: 1 }],
      payMethod: Number(product.value.priceMoney || 0) > 0 ? 3 : 1,
      remark: `商品详情页立即兑换：${product.value.name}`,
    })
    if (createRes.code !== 200 || !createRes.data) throw new Error(createRes.message || '创建订单失败')
    const payRes = await payMallOrder(createRes.data.id)
    if (payRes.code !== 200 || !payRes.data) throw new Error(payRes.message || '支付失败')
    const orderRes = await fetchMallOrder(createRes.data.id)
    if (orderRes.code !== 200 || !orderRes.data) throw new Error(orderRes.message || '读取订单失败')
    purchasedOrder.value = orderRes.data
    product.value.stock = Math.max(0, product.value.stock - 1)
    ElMessage.success('兑换成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '兑换失败')
  } finally {
    paying.value = false
  }
}

async function copyCode() {
  if (!redemptionItem.value?.redemptionCode) return
  await navigator.clipboard.writeText(redemptionItem.value.redemptionCode)
  ElMessage.success('兑换码已复制')
}

function viewCourse() {
  if (!product.value?.refCourseId) return
  router.push({ path: '/resources', query: { courseId: String(product.value.refCourseId) } })
}

onMounted(loadProduct)
</script>

<template>
  <div class="product-detail-page">
    <div class="page-glow" aria-hidden="true" />
    <div class="section-inner">
      <button type="button" class="back-link" @click="router.push('/credit')">
        <el-icon><ArrowLeft /></el-icon>
        返回秩点商城
      </button>

      <el-skeleton v-if="loading" class="detail-skeleton" :rows="8" animated />
      <el-empty v-else-if="!product" description="商品不存在或已下架" />

      <template v-else>
        <section class="product-detail-card">
          <div class="detail-cover">
            <img v-if="product.coverUrl" :src="product.coverUrl" :alt="product.name" />
            <div v-else class="detail-cover__fallback">
              <el-icon><Goods /></el-icon>
            </div>
            <span v-if="product.stock <= 0" class="stock-badge is-empty">暂无库存</span>
            <span v-else class="stock-badge">库存 {{ product.stock }}</span>
          </div>

          <div class="detail-content">
            <div class="detail-tags">
              <span class="chip">{{ product.productTypeName }}</span>
              <span class="chip chip--soft">{{ product.categoryName }}</span>
            </div>

            <h1>{{ product.name }}</h1>
            <p class="detail-desc">{{ product.description || '暂无商品介绍' }}</p>

            <div class="price-row">
              <div class="price-main">
                <span class="price-label">所需秩点</span>
                <strong>{{ formatAmount(product.priceCredit) }}</strong>
              </div>
              <div class="price-side">
                <span>模拟现金</span>
                <em>¥{{ formatAmount(product.priceMoney) }}</em>
              </div>
            </div>

            <dl class="meta-grid">
              <div>
                <dt>交付方式</dt>
                <dd>{{ deliveryText }}</dd>
              </div>
              <div>
                <dt>当前库存</dt>
                <dd>{{ product.stock }}</dd>
              </div>
            </dl>

            <div v-if="product.productType === 2 || product.productType === 3" class="tip-banner">
              <el-icon><Ticket /></el-icon>
              <p>
                兑换码用于订单核验、客服查询或外部合作方兑换；平台内课程支付成功后会自动开通，无需手动输入。
              </p>
            </div>

            <div class="detail-actions">
              <button
                type="button"
                class="redeem-btn"
                :disabled="paying || product.stock <= 0"
                @click="redeemNow"
              >
                <el-icon><ShoppingCart /></el-icon>
                {{ paying ? '兑换中…' : product.stock <= 0 ? '暂不可兑换' : '立即兑换' }}
              </button>
              <button type="button" class="ghost-btn" @click="router.push('/credit/orders')">
                查看订单
              </button>
            </div>
          </div>
        </section>

        <section v-if="purchasedOrder" class="success-panel">
          <div class="success-panel__head">
            <h2>兑换成功</h2>
            <p>订单已写入记录，可继续查看课程或复制兑换码</p>
          </div>
          <div class="result-actions">
            <button
              v-if="redemptionItem?.redemptionCode"
              type="button"
              class="ghost-btn"
              @click="copyCode"
            >
              复制兑换码 {{ redemptionItem.redemptionCode }}
            </button>
            <button
              v-if="product.productType === 3"
              type="button"
              class="redeem-btn"
              @click="viewCourse"
            >
              <el-icon><View /></el-icon>
              查看已购课程
            </button>
            <button type="button" class="ghost-btn" @click="router.push('/credit/orders')">
              查看订单记录
            </button>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<style scoped>
.product-detail-page {
  --mall-accent: #38bdf8;
  --mall-accent-deep: #0ea5e9;
  position: relative;
  min-height: calc(100vh - var(--header-height));
  padding: 28px 20px 60px;
  overflow: hidden;
}

.page-glow {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse at 12% 8%, rgba(56, 189, 248, 0.16), transparent 42%),
    radial-gradient(ellipse at 90% 20%, rgba(14, 165, 233, 0.1), transparent 40%);
}

.section-inner {
  position: relative;
  z-index: 1;
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  padding: 0;
  border: 0;
  background: transparent;
  color: rgba(186, 230, 253, 0.88);
  font-size: 14px;
  cursor: pointer;
}

.back-link:hover {
  color: #e0f2fe;
}

.detail-skeleton {
  padding: 24px;
  border-radius: 16px;
  background: rgba(8, 20, 40, 0.45);
  border: 1px solid rgba(125, 211, 252, 0.18);
}

.product-detail-page :deep(.el-empty__description p) {
  color: rgba(186, 230, 253, 0.7);
}

.product-detail-card {
  display: grid;
  grid-template-columns: minmax(280px, 0.95fr) minmax(320px, 1.05fr);
  gap: 28px;
  padding: 24px;
  border-radius: 18px;
  background:
    radial-gradient(ellipse at 8% 0%, rgba(56, 189, 248, 0.14), transparent 42%),
    rgba(8, 18, 36, 0.72);
  border: 1px solid rgba(125, 211, 252, 0.24);
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(14px);
}

.detail-cover {
  position: relative;
  min-height: 360px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 14px;
  background: rgba(8, 24, 48, 0.65);
  border: 1px solid rgba(125, 211, 252, 0.18);
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-cover__fallback {
  display: grid;
  place-items: center;
  color: #38bdf8;
  font-size: 64px;
}

.stock-badge {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(8, 18, 36, 0.72);
  border: 1px solid rgba(56, 189, 248, 0.35);
  color: #bae6fd;
  font-size: 12px;
  font-weight: 650;
  backdrop-filter: blur(8px);
}

.stock-badge.is-empty {
  border-color: rgba(248, 113, 113, 0.45);
  color: #fecaca;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 11px;
  border-radius: 999px;
  background: rgba(14, 165, 233, 0.22);
  border: 1px solid rgba(56, 189, 248, 0.35);
  color: #bae6fd;
  font-size: 12px;
  font-weight: 650;
}

.chip--soft {
  background: rgba(148, 163, 184, 0.12);
  border-color: rgba(148, 163, 184, 0.28);
  color: rgba(226, 232, 240, 0.85);
}

.detail-content h1 {
  margin: 0;
  color: #e0f2fe;
  font-size: clamp(24px, 3vw, 30px);
  line-height: 1.3;
}

.detail-desc {
  margin: 0;
  color: rgba(186, 230, 253, 0.82);
  line-height: 1.8;
  white-space: pre-wrap;
}

.price-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 18px;
  padding: 16px 18px;
  border-radius: 14px;
  background: rgba(8, 24, 48, 0.55);
  border: 1px solid rgba(56, 189, 248, 0.22);
}

.price-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-label {
  font-size: 12px;
  color: rgba(147, 197, 253, 0.72);
}

.price-main strong {
  font-size: 32px;
  line-height: 1;
  color: #38bdf8;
  font-weight: 760;
}

.price-side {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: rgba(186, 230, 253, 0.7);
  font-size: 12px;
}

.price-side em {
  font-style: normal;
  font-size: 16px;
  color: #e2e8f0;
  font-weight: 650;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 0;
}

.meta-grid div {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(8, 20, 40, 0.45);
  border: 1px solid rgba(125, 211, 252, 0.16);
}

.meta-grid dt {
  margin: 0 0 6px;
  font-size: 12px;
  color: rgba(147, 197, 253, 0.7);
}

.meta-grid dd {
  margin: 0;
  color: #e0f2fe;
  font-weight: 600;
  line-height: 1.5;
}

.tip-banner {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(14, 165, 233, 0.12);
  border: 1px solid rgba(56, 189, 248, 0.28);
  color: #bae6fd;
}

.tip-banner .el-icon {
  margin-top: 2px;
  flex-shrink: 0;
  color: #38bdf8;
}

.tip-banner p {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: rgba(186, 230, 253, 0.88);
}

.detail-actions,
.result-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.redeem-btn,
.ghost-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 18px;
  border-radius: 12px;
  border: 0;
  cursor: pointer;
  font-size: 14px;
  font-weight: 650;
  transition: filter 0.2s, background 0.2s, border-color 0.2s;
}

.redeem-btn {
  background: linear-gradient(135deg, #38bdf8, #0284c7);
  color: #f8fafc;
  box-shadow: 0 12px 28px rgba(14, 165, 233, 0.28);
}

.redeem-btn:hover:not(:disabled) {
  filter: brightness(1.06);
}

.redeem-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

.ghost-btn {
  background: transparent;
  color: #bae6fd;
  border: 1px solid rgba(125, 211, 252, 0.28);
}

.ghost-btn:hover {
  background: rgba(56, 189, 248, 0.12);
  border-color: rgba(56, 189, 248, 0.45);
}

.success-panel {
  margin-top: 18px;
  padding: 20px 22px;
  border-radius: 16px;
  background: rgba(8, 28, 24, 0.55);
  border: 1px solid rgba(52, 211, 153, 0.28);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.2);
}

.success-panel__head h2 {
  margin: 0 0 6px;
  color: #a7f3d0;
  font-size: 18px;
}

.success-panel__head p {
  margin: 0 0 14px;
  color: rgba(167, 243, 208, 0.78);
  font-size: 13px;
}

@media (max-width: 820px) {
  .product-detail-card {
    grid-template-columns: 1fr;
    padding: 18px;
  }

  .detail-cover {
    min-height: 240px;
  }

  .meta-grid {
    grid-template-columns: 1fr;
  }

  .price-main strong {
    font-size: 28px;
  }
}
</style>
