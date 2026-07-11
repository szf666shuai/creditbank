import request from '@/utils/request'

export interface EnterpriseMallProduct {
  id: number
  categoryId: number
  categoryName?: string
  name: string
  description?: string
  coverUrl?: string
  productType: number
  productTypeName: string
  refCourseId?: number
  priceCredit: number
  priceMoney: number
  stock: number
  status: number
  approvalStatus: number
  approvalStatusName: string
  reviewRemark?: string
  createTime?: string
}

export interface EnterpriseMallProductSavePayload {
  categoryId: number
  name: string
  description?: string
  coverUrl?: string
  productType: number
  refCourseId?: number
  priceCredit: number
  priceMoney?: number
  stock?: number
}

export function listEnterpriseProductsApi() {
  return request.get<EnterpriseMallProduct[]>('/enterprise/my/products')
}

export function createEnterpriseProductApi(data: EnterpriseMallProductSavePayload) {
  return request.post<EnterpriseMallProduct>('/enterprise/my/products', data)
}

export function updateEnterpriseProductApi(id: number, data: EnterpriseMallProductSavePayload) {
  return request.put<EnterpriseMallProduct>(`/enterprise/my/products/${id}`, data)
}
