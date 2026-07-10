import request from '@/utils/request'

export interface CreditChangeResult {
  userId: number
  amount: number
  balanceAfter: number
  transactionId: number
  message: string
}

export interface MallCategory {
  id: number
  name: string
  parentId: number
  sortOrder: number
}

export interface MallProduct {
  id: number
  categoryId: number
  categoryName: string
  name: string
  description?: string
  coverUrl?: string
  productType: number
  productTypeName: string
  refCourseId?: number
  priceCredit: number
  priceMoney: number
  stock: number
}

export interface MallOrderItem {
  id: number
  productId: number
  productName: string
  productType?: number
  refCourseId?: number
  quantity: number
  priceCredit: number
  priceMoney: number
  redemptionCode?: string
}

export interface MallOrder {
  id: number
  orderNo: string
  totalCredit: number
  totalMoney: number
  payMethod: number
  payMethodName: string
  payStatus: number
  payStatusName: string
  payTime?: string
  remark?: string
  createTime: string
  items: MallOrderItem[]
}

export interface PaymentResult {
  orderId: number
  orderNo: string
  payNo: string
  amountCredit: number
  amountMoney: number
  creditChange?: CreditChangeResult
}

export function fetchMallCategories() {
  return request.get<MallCategory[]>('/mall/categories')
}

export function fetchMallProducts(params?: { categoryId?: number; keyword?: string }) {
  const query = new URLSearchParams()
  if (params?.categoryId) query.set('categoryId', String(params.categoryId))
  if (params?.keyword) query.set('keyword', params.keyword)
  const suffix = query.toString() ? `?${query.toString()}` : ''
  return request.get<MallProduct[]>(`/mall/products${suffix}`)
}

export function fetchMallProduct(productId: number) {
  return request.get<MallProduct>(`/mall/products/${productId}`)
}

export function createMallOrder(payload: {
  items: Array<{ productId: number; quantity: number }>
  payMethod?: number
  remark?: string
}) {
  return request.post<MallOrder>('/mall/orders', payload)
}

export function fetchMallOrders(limit = 20) {
  return request.get<MallOrder[]>(`/mall/orders?limit=${limit}`)
}

export function fetchMallOrder(orderId: number) {
  return request.get<MallOrder>(`/mall/orders/${orderId}`)
}

export function payMallOrder(orderId: number) {
  return request.post<PaymentResult>(`/mall/orders/${orderId}/pay`)
}
