import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import type { MallProduct } from '@/api/mall'

export interface MallCartLine {
  product: MallProduct
  quantity: number
}

const STORAGE_KEY = 'credit_bank_mall_cart'

function loadCart(): MallCartLine[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw) as MallCartLine[]
    if (!Array.isArray(parsed)) return []
    return parsed.filter(
      (line) =>
        line?.product?.id != null &&
        typeof line.quantity === 'number' &&
        line.quantity > 0,
    )
  } catch {
    return []
  }
}

export const useMallCartStore = defineStore('mallCart', () => {
  const cart = ref<MallCartLine[]>(loadCart())

  const cartCount = computed(() =>
    cart.value.reduce((sum, line) => sum + line.quantity, 0),
  )
  const totalCredit = computed(() =>
    cart.value.reduce(
      (sum, line) => sum + Number(line.product.priceCredit || 0) * line.quantity,
      0,
    ),
  )
  const totalMoney = computed(() =>
    cart.value.reduce(
      (sum, line) => sum + Number(line.product.priceMoney || 0) * line.quantity,
      0,
    ),
  )

  watch(
    cart,
    (value) => {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
    },
    { deep: true },
  )

  function addToCart(product: MallProduct): { ok: boolean; message?: string } {
    if (product.stock <= 0) {
      return { ok: false, message: '商品库存不足' }
    }
    const line = cart.value.find((item) => item.product.id === product.id)
    if (line) {
      if (line.quantity >= product.stock) {
        return { ok: false, message: '已达到可兑换库存' }
      }
      line.quantity += 1
      // 刷新快照中的商品字段（价格等可能变化）
      line.product = product
    } else {
      cart.value.push({ product, quantity: 1 })
    }
    return { ok: true }
  }

  function removeCartLine(productId: number) {
    cart.value = cart.value.filter((line) => line.product.id !== productId)
  }

  function clearCart() {
    cart.value = []
  }

  return {
    cart,
    cartCount,
    totalCredit,
    totalMoney,
    addToCart,
    removeCartLine,
    clearCart,
  }
})
