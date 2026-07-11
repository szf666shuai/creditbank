<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete, Goods, ShoppingCart, Wallet } from '@element-plus/icons-vue'
import {
  createMallOrder,
  fetchMallCategories,
  fetchMallProducts,
  payMallOrder,
  type MallCategory,
  type MallOrder,
  type MallProduct,
} from '@/api/mall'
import { useAuthStore } from '@/stores/auth'

interface CartLine {
  product: MallProduct
  quantity: number
}

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const orderLoading = ref(false)
const categories = ref<MallCategory[]>([])
const products = ref<MallProduct[]>([])
const cart = ref<CartLine[]>([])
const activeCategoryId = ref<number | undefined>()
const keyword = ref('')
const remark = ref('')
const paymentVisible = ref(false)
const pendingOrder = ref<MallOrder | null>(null)
const paymentResultNo = ref('')

const totalCredit = computed(() =>
  cart.value.reduce((sum, line) => sum + Number(line.product.priceCredit || 0) * line.quantity, 0),
)
const totalMoney = computed(() =>
  cart.value.reduce((sum, line) => sum + Number(line.product.priceMoney || 0) * line.quantity, 0),
)
const cartCount = computed(() => cart.value.reduce((sum, line) => sum + line.quantity, 0))

function formatAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

function requireLogin() {
  if (authStore.isLoggedIn) return true
  router.push({ path: '/login', query: { redirect: '/credit' } })
  return false
}

function productTypeTag(type: number) {
  if (type === 1) return 'success'
  if (type === 2) return 'warning'
  if (type === 3) return 'primary'
  return 'info'
}

async function loadCategories() {
  const res = await fetchMallCategories()
  if (res.code === 200 && res.data) {
    categories.value = res.data
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await fetchMallProducts({
      categoryId: activeCategoryId.value,
      keyword: keyword.value.trim(),
    })
    if (res.code !== 200 || !res.data) throw new Error(res.message || '加载商品失败')
    products.value = res.data
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载商品失败')
  } finally {
    loading.value = false
  }
}

function selectCategory(categoryId?: number) {
  activeCategoryId.value = categoryId
  loadProducts()
}

function addToCart(product: MallProduct) {
  if (product.stock <= 0) {
    ElMessage.warning('商品库存不足')
    return
  }
  const line = cart.value.find((item) => item.product.id === product.id)
  if (line) {
    if (line.quantity >= product.stock) {
      ElMessage.warning('已达到可兑换库存')
      return
    }
    line.quantity += 1
  } else {
    cart.value.push({ product, quantity: 1 })
  }
}

function removeCartLine(productId: number) {
  cart.value = cart.value.filter((line) => line.product.id !== productId)
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

async function submitOrder() {
  if (!requireLogin()) return
  if (!cart.value.length) {
    ElMessage.warning('请先选择商品')
    return
  }
  orderLoading.value = true
  try {
    const createRes = await createMallOrder({
      items: cart.value.map((line) => ({
        productId: line.product.id,
        quantity: line.quantity,
      })),
      payMethod: totalMoney.value > 0 ? 3 : 1,
      remark: remark.value.trim(),
    })
    if (createRes.code !== 200 || !createRes.data) {
      throw new Error(createRes.message || '创建订单失败')
    }
    pendingOrder.value = createRes.data
    paymentResultNo.value = ''
    paymentVisible.value = true
    cart.value = []
    remark.value = ''
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '提交订单失败')
  } finally {
    orderLoading.value = false
  }
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
    await loadProducts()
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

onMounted(async () => {
  await Promise.all([loadCategories(), loadProducts()])
})
</script>

<template>
  <div class="mall-page">
    <div class="section-inner">
      <nav class="mall-subnav">
        <router-link to="/credit" class="subnav-item is-active">商品兑换</router-link>
        <router-link to="/credit/orders" class="subnav-item">订单记录</router-link>
      </nav>

      <section class="mall-header">
        <div>
          <p class="eyebrow">Credit Mall</p>
          <h1>积分商城</h1>
          <p class="subtitle">商品按分类展示，支持实物、虚拟商品、课程资源和服务兑换，订单与支付记录会写入数据库。</p>
        </div>
        <div class="mall-header-actions">
          <div class="mall-stats">
            <div>
              <el-icon><Goods /></el-icon>
              <strong>{{ products.length }}</strong>
              <span>在售商品</span>
            </div>
            <div>
              <el-icon><ShoppingCart /></el-icon>
              <strong>{{ cartCount }}</strong>
              <span>购物车</span>
            </div>
          </div>
          <el-button type="primary" plain @click="router.push('/credit/orders')">
            <el-icon><Tickets /></el-icon>
            订单记录
          </el-button>
        </div>
      </section>

      <section class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索商品"
          clearable
          @keyup.enter="loadProducts"
          @clear="loadProducts"
        />
        <el-button type="primary" @click="loadProducts">搜索</el-button>
      </section>

      <div class="category-row">
        <el-check-tag :checked="!activeCategoryId" @click="selectCategory()">全部</el-check-tag>
        <el-check-tag
          v-for="category in categories"
          :key="category.id"
          :checked="activeCategoryId === category.id"
          @click="selectCategory(category.id)"
        >
          {{ category.name }}
        </el-check-tag>
      </div>

      <div class="mall-layout">
        <main>
          <el-skeleton v-if="loading" :rows="8" animated />
          <el-empty v-else-if="!products.length" description="暂无商品" />
          <div v-else class="product-grid">
            <article v-for="product in products" :key="product.id" class="product-card">
              <div class="product-cover">
                <img v-if="product.coverUrl" :src="product.coverUrl" :alt="product.name" loading="lazy" />
                <div v-else class="cover-placeholder">
                  <el-icon><Goods /></el-icon>
                </div>
              </div>
              <div class="product-body">
                <div class="product-tags">
                  <el-tag size="small" :type="productTypeTag(product.productType)">
                    {{ product.productTypeName }}
                  </el-tag>
                  <span>{{ product.categoryName }}</span>
                </div>
                <h2>{{ product.name }}</h2>
                <p>{{ product.description }}</p>
                <div class="product-footer">
                  <div class="price">
                    <strong>{{ formatAmount(product.priceCredit) }}</strong>
                    <span>秩点</span>
                    <em v-if="product.priceMoney > 0">+ ¥{{ formatAmount(product.priceMoney) }}</em>
                  </div>
                  <div class="product-actions">
                    <el-button @click="viewProductDetail(product.id)">详情</el-button>
                    <el-button type="primary" :icon="ShoppingCart" @click="addToCart(product)">加入</el-button>
                  </div>
                </div>
                <span class="stock">库存 {{ product.stock }}</span>
              </div>
            </article>
          </div>
        </main>

        <aside class="checkout-panel">
          <div class="panel-title">
            <el-icon><Wallet /></el-icon>
            <span>结算</span>
          </div>
          <el-empty v-if="!cart.length" description="购物车为空" :image-size="72" />
          <div v-else class="cart-list">
            <div v-for="line in cart" :key="line.product.id" class="cart-line">
              <div>
                <strong>{{ line.product.name }}</strong>
                <span>{{ formatAmount(line.product.priceCredit) }} 秩点 / 件</span>
              </div>
              <el-input-number
                v-model="line.quantity"
                :min="1"
                :max="line.product.stock"
                size="small"
                controls-position="right"
              />
              <el-button
                link
                type="danger"
                :icon="Delete"
                aria-label="移除商品"
                title="移除商品"
                @click="removeCartLine(line.product.id)"
              />
            </div>
          </div>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="2"
            maxlength="120"
            show-word-limit
            placeholder="订单备注，如收货信息或兑换说明"
          />
          <div class="total-line">
            <span>合计</span>
            <strong>{{ formatAmount(totalCredit) }} 秩点</strong>
          </div>
          <div v-if="totalMoney > 0" class="total-line muted">
            <span>模拟现金</span>
            <strong>¥{{ formatAmount(totalMoney) }}</strong>
          </div>
          <el-button
            type="primary"
            class="submit-btn"
            :loading="orderLoading"
            :disabled="!cart.length"
            @click="submitOrder"
          >
            创建订单并支付
          </el-button>
          <el-button class="orders-link-btn" @click="router.push('/credit/orders')">
            查看订单记录
          </el-button>
        </aside>
      </div>

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
  padding: 32px 20px 64px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  padding: 28px;
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

.mall-header,
.mall-layout,
.order-head,
.order-foot,
.order-item-line,
.product-footer,
.total-line {
  display: flex;
}

.mall-header-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.product-actions,
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
  color: #0b5cab;
  font: inherit;
  cursor: pointer;
}

.payment-redemption-list {
  display: grid;
  gap: 8px;
  text-align: center;
}

.mall-header {
  justify-content: space-between;
  gap: 24px;
  align-items: flex-end;
  margin-bottom: 22px;
  padding-bottom: 18px;
  border-bottom: 1px solid #eef2f7;
}

.eyebrow {
  color: #0b5cab;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}

h1 {
  margin: 6px 0 8px;
  font-size: 30px;
  color: #0f172a;
  font-weight: 700;
}

.subtitle {
  max-width: 680px;
  color: #64748b;
  line-height: 1.7;
}

.mall-stats {
  display: flex;
  gap: 10px;
}

.mall-stats div {
  min-width: 104px;
  padding: 12px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.mall-stats strong {
  display: block;
  font-size: 22px;
  color: var(--color-text);
}

.mall-stats span,
.stock {
  color: var(--color-text-muted);
  font-size: 12px;
}

.filters {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  margin-bottom: 14px;
}

.category-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.mall-layout {
  align-items: flex-start;
  gap: 20px;
}

.mall-layout main {
  flex: 1;
  min-width: 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.product-card,
.checkout-panel,
.order-card {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.product-card {
  overflow: hidden;
}

.product-cover {
  aspect-ratio: 16 / 10;
  background: #eef2f6;
}

.product-cover img,
.cover-placeholder {
  width: 100%;
  height: 100%;
}

.product-cover img {
  display: block;
  object-fit: cover;
}

.cover-placeholder {
  display: grid;
  place-items: center;
  color: var(--color-primary);
  font-size: 34px;
}

.product-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.product-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.product-body h2 {
  font-size: 17px;
  line-height: 1.45;
  margin: 0;
  color: var(--color-text);
}

.product-body p {
  min-height: 44px;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-footer,
.order-head,
.order-foot,
.order-item-line,
.total-line {
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.order-item-line {
  width: 100%;
}

.price strong {
  color: #0f8f68;
  font-size: 20px;
}

.price span,
.price em {
  color: var(--color-text-muted);
  font-size: 12px;
  font-style: normal;
  margin-left: 4px;
}

.checkout-panel {
  position: sticky;
  top: calc(var(--header-height) + 18px);
  width: 340px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.panel-title,
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: var(--color-text);
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-line {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 92px auto;
  align-items: center;
  gap: 8px;
}

.cart-line strong,
.order-head strong {
  display: block;
  font-size: 14px;
  color: var(--color-text);
}

.cart-line span,
.order-head span {
  display: block;
  margin-top: 3px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.total-line strong {
  font-size: 20px;
  color: #0f8f68;
}

.total-line.muted strong {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.submit-btn,
.orders-link-btn {
  width: 100%;
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

@media (max-width: 920px) {
  .mall-header,
  .mall-layout {
    flex-direction: column;
  }

  .checkout-panel {
    position: static;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .filters {
    grid-template-columns: 1fr;
  }
}
</style>
