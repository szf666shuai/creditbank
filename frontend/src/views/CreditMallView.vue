<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete, Goods, Search } from '@element-plus/icons-vue'
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
import UiIcon from '@/components/ui/UiIcon.vue'

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
const cartDrawerVisible = ref(false)
const pendingOrder = ref<MallOrder | null>(null)
const paymentResultNo = ref('')
const bannerIndex = ref(0)
let bannerTimer: number | undefined

const categoryIcons = ['course', 'gift', 'collection', 'medal', 'goods', 'reading', 'activity', 'star', 'document', 'cart']

const totalCredit = computed(() =>
  cart.value.reduce((sum, line) => sum + Number(line.product.priceCredit || 0) * line.quantity, 0),
)
const totalMoney = computed(() =>
  cart.value.reduce((sum, line) => sum + Number(line.product.priceMoney || 0) * line.quantity, 0),
)
const cartCount = computed(() => cart.value.reduce((sum, line) => sum + line.quantity, 0))

const bannerSlides = computed(() => {
  const featured = products.value.filter((p) => p.coverUrl).slice(0, 4)
  if (featured.length) {
    return featured.map((p) => ({
      id: p.id,
      title: p.name,
      subtitle: `${formatAmount(p.priceCredit)} 秩点起兑`,
      coverUrl: p.coverUrl,
      productId: p.id,
    }))
  }
  return [
    {
      id: 0,
      title: '学有所得，兑有所值',
      subtitle: '用秩点兑换课程、用品与虚拟权益',
      coverUrl: '',
      productId: 0,
    },
  ]
})

const hotProducts = computed(() =>
  [...products.value]
    .sort((a, b) => Number(a.priceCredit) - Number(b.priceCredit))
    .slice(0, 6),
)

const currentBanner = computed(() => bannerSlides.value[bannerIndex.value] ?? bannerSlides.value[0])

function formatAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

function requireLogin() {
  if (authStore.isLoggedIn) return true
  router.push({ path: '/login', query: { redirect: '/credit' } })
  return false
}

function categoryIcon(index: number) {
  return categoryIcons[index % categoryIcons.length]
}

function startBannerTimer() {
  stopBannerTimer()
  bannerTimer = window.setInterval(() => {
    if (!bannerSlides.value.length) return
    bannerIndex.value = (bannerIndex.value + 1) % bannerSlides.value.length
  }, 4000)
}

function stopBannerTimer() {
  if (bannerTimer) {
    window.clearInterval(bannerTimer)
    bannerTimer = undefined
  }
}

function selectBanner(index: number) {
  bannerIndex.value = index
  startBannerTimer()
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
    bannerIndex.value = 0
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
  ElMessage.success('已加入兑换篮')
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
    cartDrawerVisible.value = false
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
  if (!productId) return
  router.push(`/credit/products/${productId}`)
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadProducts()])
  startBannerTimer()
})

onUnmounted(stopBannerTimer)
</script>

<template>
  <div class="mall-page">
    <div class="section-inner">
      <!-- 顶部搜索条：参考拼多多搜索主导航 -->
      <header class="mall-topbar">
        <div class="brand-mini">
          <strong>秩点商城</strong>
          <span>学有所得 · 兑有所值</span>
        </div>
        <div class="mall-search">
          <el-input
            v-model="keyword"
            clearable
            placeholder="搜索课程、用品、权益…"
            @keyup.enter="loadProducts"
            @clear="loadProducts"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <button type="button" class="mall-search__btn" @click="loadProducts">搜索</button>
        </div>
        <div class="mall-topbar__actions">
          <button type="button" class="top-action" @click="router.push('/credit/orders')">
            <UiIcon name="ticket" :size="18" color="#7dd3fc" />
            订单
          </button>
          <button type="button" class="top-action cart-btn" @click="cartDrawerVisible = true">
            <UiIcon name="cart" :size="18" color="#7dd3fc" />
            兑换篮
            <em v-if="cartCount">{{ cartCount > 99 ? '99+' : cartCount }}</em>
          </button>
        </div>
      </header>

      <!-- 分类侧栏 + Banner：参考拼多多首页双栏 -->
      <section class="mall-hero">
        <aside class="cat-panel">
          <button
            type="button"
            class="cat-panel__item"
            :class="{ active: !activeCategoryId }"
            @click="selectCategory()"
          >
            <UiIcon name="hot" :size="16" />
            全部商品
          </button>
          <button
            v-for="(category, index) in categories"
            :key="category.id"
            type="button"
            class="cat-panel__item"
            :class="{ active: activeCategoryId === category.id }"
            @click="selectCategory(category.id)"
          >
            <UiIcon :name="categoryIcon(index)" :size="16" />
            {{ category.name }}
          </button>
        </aside>

        <div class="banner-stage">
          <button
            type="button"
            class="banner-slide"
            @click="currentBanner.productId && viewProductDetail(currentBanner.productId)"
          >
            <img
              v-if="currentBanner.coverUrl"
              :src="currentBanner.coverUrl"
              :alt="currentBanner.title"
            />
            <div class="banner-slide__mask">
              <p class="banner-kicker">精彩兑换</p>
              <h2>{{ currentBanner.title }}</h2>
              <p>{{ currentBanner.subtitle }}</p>
            </div>
          </button>
          <div class="banner-dots">
            <button
              v-for="(slide, index) in bannerSlides"
              :key="slide.id"
              type="button"
              class="banner-dot"
              :class="{ active: bannerIndex === index }"
              @click="selectBanner(index)"
            />
          </div>
        </div>
      </section>

      <!-- 圆形分类入口 -->
      <section class="cat-icons">
        <button type="button" class="cat-icon" :class="{ active: !activeCategoryId }" @click="selectCategory()">
          <span class="cat-icon__circle"><UiIcon name="cart" :size="22" /></span>
          <em>全部</em>
        </button>
        <button
          v-for="(category, index) in categories"
          :key="`icon-${category.id}`"
          type="button"
          class="cat-icon"
          :class="{ active: activeCategoryId === category.id }"
          @click="selectCategory(category.id)"
        >
          <span class="cat-icon__circle"><UiIcon :name="categoryIcon(index)" :size="22" /></span>
          <em>{{ category.name }}</em>
        </button>
      </section>

      <!-- 特惠横滑：参考「精彩活动」 -->
      <section v-if="hotProducts.length" class="deal-section">
        <header class="deal-head">
          <div>
            <h3>精彩兑换</h3>
            <span>低价秩点 · 精选好物</span>
          </div>
          <button type="button" class="more-link" @click="selectCategory()">更多 &gt;</button>
        </header>
        <div class="deal-track">
          <article
            v-for="product in hotProducts"
            :key="`hot-${product.id}`"
            class="deal-card"
            @click="viewProductDetail(product.id)"
          >
            <div class="deal-card__cover">
              <img v-if="product.coverUrl" :src="product.coverUrl" :alt="product.name" loading="lazy" />
              <div v-else class="cover-fallback"><el-icon><Goods /></el-icon></div>
            </div>
            <strong class="deal-price">
              <small>秩点</small>{{ formatAmount(product.priceCredit) }}
            </strong>
            <p>{{ product.name }}</p>
            <button
              type="button"
              class="deal-add"
              @click.stop="addToCart(product)"
            >
              马上兑
            </button>
          </article>
        </div>
      </section>

      <!-- 商品货架网格 -->
      <section class="shelf-section">
        <header class="shelf-head">
          <h3>精选专题</h3>
          <span>{{ products.length }} 件在售</span>
        </header>

        <el-skeleton v-if="loading" :rows="8" animated />
        <el-empty v-else-if="!products.length" description="暂无商品" />
        <div v-else class="product-grid">
          <article v-for="product in products" :key="product.id" class="product-card">
            <button type="button" class="product-cover" @click="viewProductDetail(product.id)">
              <img v-if="product.coverUrl" :src="product.coverUrl" :alt="product.name" loading="lazy" />
              <div v-else class="cover-fallback"><el-icon><Goods /></el-icon></div>
              <span class="product-badge">{{ product.productTypeName || product.categoryName }}</span>
            </button>
            <div class="product-body">
              <h2 @click="viewProductDetail(product.id)">{{ product.name }}</h2>
              <p>{{ product.description || product.categoryName }}</p>
              <div class="product-foot">
                <div class="price">
                  <strong>{{ formatAmount(product.priceCredit) }}</strong>
                  <span>秩点</span>
                  <em v-if="product.priceMoney > 0">+¥{{ formatAmount(product.priceMoney) }}</em>
                </div>
                <button type="button" class="add-btn" :aria-label="`加入兑换篮 ${product.name}`" @click="addToCart(product)">
                  <UiIcon name="cart" :size="16" color="#031525" />
                </button>
              </div>
              <small class="stock">剩 {{ product.stock }} 件</small>
            </div>
          </article>
        </div>
      </section>
    </div>

    <!-- 悬浮兑换篮 -->
    <button type="button" class="float-cart" aria-label="打开兑换篮" @click="cartDrawerVisible = true">
      <span class="float-cart__icon">
        <UiIcon name="cart" :size="22" color="#031525" />
      </span>
      <span class="float-cart__label">兑换篮</span>
      <em v-if="cartCount">{{ cartCount > 99 ? '99+' : cartCount }}</em>
    </button>

    <el-drawer
      v-model="cartDrawerVisible"
      class="mall-cart-drawer"
      size="400px"
      direction="rtl"
      destroy-on-close
    >
      <template #header>
        <div class="drawer-head">
          <p class="drawer-kicker">Exchange Basket</p>
          <h2>
            兑换篮
            <span v-if="cartCount">（{{ cartCount }}）</span>
          </h2>
        </div>
      </template>
      <div class="cart-drawer">
        <div v-if="!cart.length" class="cart-empty">
          <UiIcon name="cart" :size="40" />
          <p>还没有商品</p>
          <button type="button" class="ghost-btn" @click="cartDrawerVisible = false">去逛逛</button>
        </div>
        <template v-else>
          <div class="cart-list">
            <div v-for="line in cart" :key="line.product.id" class="cart-line">
              <div class="cart-line__cover">
                <img
                  v-if="line.product.coverUrl"
                  :src="line.product.coverUrl"
                  :alt="line.product.name"
                />
                <UiIcon v-else name="goods" :size="22" />
              </div>
              <div class="cart-line__info">
                <strong>{{ line.product.name }}</strong>
                <span>{{ formatAmount(line.product.priceCredit) }} 秩点 / 件</span>
                <div class="cart-line__ctrl">
                  <el-input-number
                    v-model="line.quantity"
                    :min="1"
                    :max="line.product.stock"
                    size="small"
                    controls-position="right"
                  />
                  <button
                    type="button"
                    class="remove-btn"
                    aria-label="移除商品"
                    @click="removeCartLine(line.product.id)"
                  >
                    <el-icon><Delete /></el-icon>
                    移除
                  </button>
                </div>
              </div>
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
          <div class="cart-total">
            <div>
              <span>合计</span>
              <strong>{{ formatAmount(totalCredit) }} 秩点</strong>
            </div>
            <div v-if="totalMoney > 0" class="muted">
              <span>模拟现金</span>
              <strong>¥{{ formatAmount(totalMoney) }}</strong>
            </div>
          </div>
          <button
            type="button"
            class="checkout-btn"
            :disabled="orderLoading || !cart.length"
            @click="submitOrder"
          >
            <UiIcon name="wallet" :size="18" color="#031525" />
            {{ orderLoading ? '提交中…' : '去结算' }}
          </button>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="paymentVisible"
      class="mall-pay-dialog"
      width="520px"
      align-center
      destroy-on-close
    >
      <template #header>
        <div class="drawer-head">
          <p class="drawer-kicker">Payment</p>
          <h2>{{ paymentResultNo ? '支付结果' : '订单支付' }}</h2>
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
              class="checkout-btn compact"
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
          <div class="tip-banner">本项目使用本地模拟支付，支付结果与流水记录写入本地数据库。</div>
        </template>
      </div>
      <template #footer>
        <button type="button" class="ghost-btn" @click="paymentVisible = false">
          {{ paymentResultNo ? '关闭' : '稍后支付' }}
        </button>
        <button
          v-if="pendingOrder && !paymentResultNo"
          type="button"
          class="checkout-btn compact"
          :disabled="orderLoading"
          @click="payOrder(pendingOrder)"
        >
          {{ orderLoading ? '支付中…' : '确认支付' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.mall-page {
  --mall-accent: #38bdf8;
  --mall-accent-deep: #0ea5e9;
  --mall-accent-soft: rgba(56, 189, 248, 0.18);
  padding: 16px 16px 72px;
  background: transparent;
  min-height: calc(100vh - var(--header-height));
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.mall-topbar {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.brand-mini {
  display: grid;
  gap: 2px;
  color: #e0f2fe;
}

.brand-mini strong {
  font-size: 22px;
  letter-spacing: 0.02em;
  color: #f8fafc;
}

.brand-mini span {
  font-size: 12px;
  color: rgba(186, 230, 253, 0.7);
}

.mall-search {
  display: flex;
  align-items: stretch;
  max-width: 640px;
  width: 100%;
  margin: 0 auto;
  border: 1px solid rgba(56, 189, 248, 0.45);
  border-radius: 999px;
  overflow: hidden;
  background: rgba(8, 20, 40, 0.45);
  backdrop-filter: blur(10px);
}

.mall-search :deep(.el-input__wrapper) {
  box-shadow: none !important;
  border-radius: 0;
  background: transparent;
}

.mall-search :deep(.el-input__inner) {
  color: #e0f2fe;
}

.mall-search :deep(.el-input__inner::placeholder) {
  color: rgba(148, 163, 184, 0.75);
}

.mall-search :deep(.el-input__prefix) {
  color: #7dd3fc;
}

.mall-search__btn {
  border: none;
  min-width: 88px;
  padding: 0 18px;
  background: linear-gradient(135deg, #38bdf8, #0284c7);
  color: #031525;
  font-weight: 700;
  cursor: pointer;
}

.mall-search__btn:hover {
  background: linear-gradient(135deg, #7dd3fc, #0ea5e9);
}

.mall-topbar__actions {
  display: flex;
  gap: 8px;
}

.top-action {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(125, 211, 252, 0.28);
  background: rgba(8, 20, 40, 0.35);
  color: #e0f2fe;
  cursor: pointer;
}

.top-action em {
  position: absolute;
  top: -6px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--mall-accent);
  color: #031525;
  font-size: 11px;
  font-style: normal;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.mall-hero {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 12px;
  margin-bottom: 18px;
  min-height: 320px;
}

.cat-panel {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 8px;
  border-radius: 12px;
  background: rgba(8, 20, 40, 0.38);
  border: 1px solid rgba(125, 211, 252, 0.16);
  backdrop-filter: blur(12px);
}

.cat-panel__item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: rgba(226, 232, 240, 0.88);
  text-align: left;
  cursor: pointer;
  font-size: 14px;
}

.cat-panel__item:hover,
.cat-panel__item.active {
  background: var(--mall-accent-soft);
  color: #7dd3fc;
  font-weight: 600;
}

.banner-stage {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  min-height: 320px;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(56, 189, 248, 0.35), transparent 50%),
    linear-gradient(135deg, #0b1f3a 0%, #0c4a6e 55%, #075985 100%);
  border: 1px solid rgba(125, 211, 252, 0.22);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.28);
}

.banner-slide {
  width: 100%;
  height: 100%;
  min-height: 320px;
  border: none;
  padding: 0;
  cursor: pointer;
  position: relative;
  display: block;
  background: transparent;
  text-align: left;
}

.banner-slide img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-slide__mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 28px;
  background: linear-gradient(180deg, transparent 30%, rgba(3, 18, 40, 0.78));
  color: #f0f9ff;
}

.banner-kicker {
  margin: 0 0 8px;
  font-size: 13px;
  color: #7dd3fc;
  font-weight: 700;
}

.banner-slide__mask h2 {
  margin: 0 0 8px;
  font-size: clamp(24px, 3vw, 34px);
  line-height: 1.25;
}

.banner-slide__mask p {
  margin: 0;
  color: rgba(186, 230, 253, 0.9);
}

.banner-dots {
  position: absolute;
  left: 50%;
  bottom: 14px;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
  z-index: 2;
}

.banner-dot {
  width: 8px;
  height: 8px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.35);
  cursor: pointer;
  padding: 0;
}

.banner-dot.active {
  width: 22px;
  background: #7dd3fc;
}

.cat-icons {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
  gap: 10px;
  margin-bottom: 18px;
  padding: 6px 0;
  background: transparent;
}

.cat-icon {
  display: grid;
  justify-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
}

.cat-icon__circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 22px;
  background: rgba(8, 20, 40, 0.45);
  border: 1px solid rgba(125, 211, 252, 0.25);
  backdrop-filter: blur(8px);
}

.cat-icon.active .cat-icon__circle {
  background: linear-gradient(180deg, #38bdf8, #0284c7);
  border-color: transparent;
  box-shadow: 0 6px 14px rgba(14, 165, 233, 0.35);
}

.cat-icon.active .cat-icon__circle :deep(.ui-icon) {
  color: #031525 !important;
}

.cat-icon em {
  font-style: normal;
  font-size: 12px;
  color: rgba(186, 230, 253, 0.8);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cat-icon.active em {
  color: #7dd3fc;
  font-weight: 700;
}

.deal-section,
.shelf-section {
  margin-bottom: 20px;
}

.deal-section {
  padding: 14px 4px;
  background: transparent;
  border: none;
  box-shadow: none;
}

.deal-head,
.shelf-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  margin-bottom: 12px;
  gap: 12px;
}

.deal-head h3,
.shelf-head h3 {
  margin: 0;
  font-size: 20px;
  color: #e0f2fe;
}

.deal-head span,
.shelf-head span {
  color: rgba(148, 163, 184, 0.85);
  font-size: 13px;
}

.deal-head > div {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.more-link {
  border: none;
  background: transparent;
  color: #7dd3fc;
  cursor: pointer;
  font-size: 13px;
}

.deal-track {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(140px, 1fr);
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.deal-card {
  background: rgba(8, 20, 40, 0.42);
  border: 1px solid rgba(125, 211, 252, 0.16);
  border-radius: 10px;
  padding: 8px;
  cursor: pointer;
  backdrop-filter: blur(10px);
  transition: transform 0.15s, border-color 0.15s;
}

.deal-card:hover {
  transform: translateY(-2px);
  border-color: rgba(56, 189, 248, 0.45);
}

.deal-card__cover {
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.6);
  margin-bottom: 8px;
}

.deal-card__cover img,
.cover-fallback {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  display: grid;
  place-items: center;
  color: #64748b;
  font-size: 28px;
  background: linear-gradient(135deg, #0f172a, #1e3a5f);
}

.deal-price {
  display: block;
  color: #38bdf8;
  font-size: 18px;
  line-height: 1.2;
}

.deal-price small {
  font-size: 11px;
  margin-right: 2px;
  color: #7dd3fc;
}

.deal-card p {
  margin: 4px 0 8px;
  font-size: 13px;
  color: rgba(226, 232, 240, 0.9);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 36px;
}

.deal-add {
  width: 100%;
  border: none;
  border-radius: 999px;
  padding: 6px 0;
  background: linear-gradient(135deg, #38bdf8, #0284c7);
  color: #031525;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.deal-add:hover {
  filter: brightness(1.08);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.product-card {
  background: rgba(8, 20, 40, 0.42);
  border: 1px solid rgba(125, 211, 252, 0.14);
  border-radius: 10px;
  overflow: hidden;
  backdrop-filter: blur(10px);
  transition: transform 0.15s, border-color 0.15s;
}

.product-card:hover {
  transform: translateY(-3px);
  border-color: rgba(56, 189, 248, 0.4);
}

.product-cover {
  position: relative;
  display: block;
  width: 100%;
  aspect-ratio: 1;
  border: none;
  padding: 0;
  cursor: pointer;
  background: rgba(15, 23, 42, 0.7);
}

.product-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.product-badge {
  position: absolute;
  left: 8px;
  top: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(14, 165, 233, 0.9);
  color: #031525;
  font-size: 11px;
  font-weight: 600;
}

.product-body {
  padding: 10px 12px 12px;
}

.product-body h2 {
  margin: 0 0 6px;
  font-size: 14px;
  line-height: 1.4;
  color: #e2e8f0;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.product-body p {
  margin: 0 0 10px;
  font-size: 12px;
  color: rgba(148, 163, 184, 0.9);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.price {
  color: #38bdf8;
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.price strong {
  font-size: 20px;
  font-weight: 800;
}

.price span,
.price em {
  font-size: 12px;
  font-style: normal;
  color: #7dd3fc;
}

.price em {
  color: rgba(148, 163, 184, 0.85);
  margin-left: 2px;
}

.add-btn {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #38bdf8, #0284c7);
  color: #031525;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.add-btn :deep(.ui-icon),
.add-btn :deep(svg) {
  color: #031525;
}

.add-btn:hover {
  filter: brightness(1.1);
}

.stock {
  display: block;
  margin-top: 6px;
  color: rgba(148, 163, 184, 0.7);
  font-size: 11px;
}

.float-cart {
  position: fixed;
  right: 22px;
  bottom: 88px;
  z-index: 40;
  width: 72px;
  height: 72px;
  border: 1px solid rgba(125, 211, 252, 0.35);
  border-radius: 20px;
  background:
    radial-gradient(circle at 30% 20%, rgba(125, 211, 252, 0.35), transparent 55%),
    linear-gradient(160deg, rgba(8, 28, 48, 0.95), rgba(3, 21, 37, 0.98));
  color: #7dd3fc;
  box-shadow: 0 14px 32px rgba(2, 132, 199, 0.35);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  cursor: pointer;
  backdrop-filter: blur(12px);
}

.float-cart__icon {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: linear-gradient(135deg, #38bdf8, #0284c7);
  color: #031525;
}

.float-cart__icon :deep(.ui-icon),
.float-cart__icon :deep(svg) {
  color: #031525;
}

.float-cart__label {
  font-size: 11px;
  font-weight: 700;
  color: #bae6fd;
}

.float-cart em {
  position: absolute;
  top: 6px;
  right: 8px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background: #f43f5e;
  color: #fff;
  font-style: normal;
  font-weight: 700;
  font-size: 11px;
  display: grid;
  place-items: center;
  border: 1.5px solid rgba(3, 21, 37, 0.9);
}

.drawer-head h2 {
  margin: 0;
  font-size: 18px;
  color: #e0f2fe;
  font-weight: 700;
}

.drawer-head h2 span {
  color: #7dd3fc;
  font-weight: 650;
}

.drawer-kicker {
  margin: 0 0 4px;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(125, 211, 252, 0.9);
  font-weight: 700;
}

.cart-drawer {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
}

.cart-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: rgba(186, 230, 253, 0.72);
}

.cart-empty p {
  margin: 0;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  overflow: auto;
  padding-right: 2px;
}

.cart-line {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 12px;
  padding: 12px;
  border-radius: 14px;
  background: rgba(8, 24, 48, 0.5);
  border: 1px solid rgba(125, 211, 252, 0.16);
}

.cart-line__cover {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  display: grid;
  place-items: center;
  background: rgba(8, 18, 36, 0.7);
  border: 1px solid rgba(125, 211, 252, 0.18);
  color: #38bdf8;
}

.cart-line__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cart-line__info {
  min-width: 0;
}

.cart-line__info strong {
  display: block;
  font-size: 14px;
  color: #e0f2fe;
  line-height: 1.4;
}

.cart-line__info > span {
  display: block;
  margin-top: 4px;
  color: #7dd3fc;
  font-size: 12px;
}

.cart-line__ctrl {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 10px;
}

.remove-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 0;
  background: transparent;
  color: rgba(252, 165, 165, 0.9);
  cursor: pointer;
  font-size: 12px;
  padding: 0;
}

.remove-btn:hover {
  color: #fecaca;
}

.cart-total {
  display: grid;
  gap: 8px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(8, 24, 48, 0.55);
  border: 1px solid rgba(56, 189, 248, 0.22);
}

.cart-total > div {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: rgba(186, 230, 253, 0.78);
}

.cart-total strong {
  color: #38bdf8;
  font-size: 22px;
}

.cart-total .muted strong {
  font-size: 14px;
  color: #e2e8f0;
}

.checkout-btn,
.ghost-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 16px;
  border-radius: 12px;
  border: 0;
  cursor: pointer;
  font-size: 14px;
  font-weight: 650;
}

.checkout-btn {
  width: 100%;
  background: linear-gradient(135deg, #38bdf8, #0284c7);
  color: #031525;
  box-shadow: 0 12px 28px rgba(14, 165, 233, 0.28);
}

.checkout-btn.compact {
  width: auto;
}

.checkout-btn:hover:not(:disabled) {
  filter: brightness(1.06);
}

.checkout-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

.checkout-btn :deep(.ui-icon),
.checkout-btn :deep(svg) {
  color: #031525;
}

.ghost-btn {
  background: transparent;
  color: #bae6fd;
  border: 1px solid rgba(125, 211, 252, 0.28);
}

.ghost-btn:hover {
  background: rgba(56, 189, 248, 0.12);
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

.redemption-code {
  display: block;
  width: 100%;
  margin: 0;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px dashed rgba(56, 189, 248, 0.4);
  background: rgba(56, 189, 248, 0.1);
  color: #7dd3fc;
  font: inherit;
  font-size: 12px;
  cursor: pointer;
  text-align: left;
}

.redemption-code:hover {
  background: rgba(56, 189, 248, 0.18);
}

@media (max-width: 960px) {
  .mall-topbar {
    grid-template-columns: 1fr;
  }

  .mall-hero {
    grid-template-columns: 1fr;
  }

  .cat-panel {
    display: none;
  }

  .float-cart {
    right: 14px;
    bottom: 72px;
  }
}
</style>

<style>
.mall-cart-drawer.el-drawer {
  background:
    radial-gradient(ellipse at 80% 0%, rgba(56, 189, 248, 0.16), transparent 42%),
    rgba(8, 16, 28, 0.96);
  border-left: 1px solid rgba(125, 211, 252, 0.28);
  box-shadow: -18px 0 48px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(16px);
}

.mall-cart-drawer .el-drawer__header {
  margin-bottom: 12px;
  padding: 18px 20px 12px;
  border-bottom: 1px solid rgba(147, 197, 253, 0.14);
  color: #e0f2fe;
}

.mall-cart-drawer .el-drawer__close-btn,
.mall-cart-drawer .el-drawer__close-btn .el-icon {
  color: rgba(186, 230, 253, 0.78);
}

.mall-cart-drawer .el-drawer__body {
  padding: 16px 20px 24px;
  color: #e2e8f0;
}

.mall-cart-drawer .el-textarea__inner {
  background: rgba(8, 20, 40, 0.55);
  border: 1px solid rgba(125, 211, 252, 0.28);
  box-shadow: none;
  color: #e0f2fe;
  border-radius: 12px;
}

.mall-cart-drawer .el-textarea__inner::placeholder {
  color: rgba(147, 197, 253, 0.42);
}

.mall-cart-drawer .el-input__count {
  background: transparent;
  color: rgba(147, 197, 253, 0.5);
}

.mall-cart-drawer .el-input-number {
  width: 110px;
}

.mall-cart-drawer .el-input-number .el-input__wrapper {
  background: rgba(8, 20, 40, 0.55);
  box-shadow: 0 0 0 1px rgba(125, 211, 252, 0.28) inset;
}

.mall-cart-drawer .el-input-number .el-input__inner {
  color: #e0f2fe;
}

.mall-pay-dialog.el-dialog {
  background:
    radial-gradient(ellipse at 12% 0%, rgba(56, 189, 248, 0.16), transparent 42%),
    rgba(8, 18, 36, 0.96);
  border: 1px solid rgba(125, 211, 252, 0.28);
  border-radius: 16px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
}

.mall-pay-dialog .el-dialog__header {
  margin-right: 0;
  padding: 16px 20px 12px;
  border-bottom: 1px solid rgba(147, 197, 253, 0.14);
}

.mall-pay-dialog .el-dialog__headerbtn .el-dialog__close {
  color: rgba(186, 230, 253, 0.78);
}

.mall-pay-dialog .el-dialog__body {
  padding: 16px 20px;
  color: #e2e8f0;
}

.mall-pay-dialog .el-dialog__footer {
  padding: 12px 20px 18px;
  border-top: 1px solid rgba(147, 197, 253, 0.14);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
