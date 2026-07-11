<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Goods, ShoppingCart } from '@element-plus/icons-vue'
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
    <div class="section-inner">
      <el-button :icon="ArrowLeft" text @click="router.push('/credit')">返回积分商城</el-button>
      <el-skeleton v-if="loading" :rows="8" animated />
      <el-empty v-else-if="!product" description="商品不存在或已下架" />
      <template v-else>
        <section class="product-detail-card">
          <div class="detail-cover">
            <img v-if="product.coverUrl" :src="product.coverUrl" :alt="product.name" />
            <el-icon v-else><Goods /></el-icon>
          </div>
          <div class="detail-content">
            <div class="detail-tags">
              <el-tag>{{ product.productTypeName }}</el-tag>
              <el-tag type="info">{{ product.categoryName }}</el-tag>
            </div>
            <h1>{{ product.name }}</h1>
            <p>{{ product.description }}</p>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="所需秩点">{{ formatAmount(product.priceCredit) }}</el-descriptions-item>
              <el-descriptions-item label="模拟现金">¥{{ formatAmount(product.priceMoney) }}</el-descriptions-item>
              <el-descriptions-item label="当前库存">{{ product.stock }}</el-descriptions-item>
              <el-descriptions-item label="交付方式">
                {{ product.productType === 3 ? '平台课程自动开通 + 兑换码' : product.productType === 2 ? '支付后发放兑换码' : '订单记录核验' }}
              </el-descriptions-item>
            </el-descriptions>
            <el-alert
              v-if="product.productType === 2 || product.productType === 3"
              type="info"
              :closable="false"
              show-icon
              title="兑换码用于订单核验、客服查询或外部合作方兑换；平台内课程支付成功后会自动开通，无需手动输入。"
            />
            <div class="detail-actions">
              <el-button
                type="primary"
                size="large"
                :icon="ShoppingCart"
                :loading="paying"
                :disabled="product.stock <= 0"
                @click="redeemNow"
              >
                立即兑换
              </el-button>
            </div>
          </div>
        </section>

        <el-result v-if="purchasedOrder" icon="success" title="兑换成功" sub-title="订单已写入订单记录">
          <template #extra>
            <div class="result-actions">
              <el-button v-if="redemptionItem?.redemptionCode" @click="copyCode">
                复制兑换码 {{ redemptionItem.redemptionCode }}
              </el-button>
              <el-button v-if="product.productType === 3" type="primary" @click="viewCourse">
                查看已购课程
              </el-button>
              <el-button @click="router.push('/credit/orders')">查看订单记录</el-button>
            </div>
          </template>
        </el-result>
      </template>
    </div>
  </div>
</template>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  padding: 28px 20px 60px;
  background: var(--color-bg);
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.product-detail-card {
  display: grid;
  grid-template-columns: minmax(300px, 0.9fr) minmax(360px, 1.1fr);
  gap: 32px;
  margin-top: 18px;
  padding: 28px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--color-white);
}

.detail-cover {
  min-height: 360px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 10px;
  background: #eef2f6;
  color: var(--color-primary);
  font-size: 64px;
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-tags,
.detail-actions,
.result-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-content h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 30px;
}

.detail-content p {
  margin: 0;
  color: var(--color-text-secondary);
  line-height: 1.8;
}

@media (max-width: 820px) {
  .product-detail-card {
    grid-template-columns: 1fr;
  }

  .detail-cover {
    min-height: 240px;
  }
}
</style>
