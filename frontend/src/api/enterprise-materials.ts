import request from '@/utils/request'

export interface MaterialManageItem {
  id: number
  title: string
  description?: string
  fileUrl: string
  materialType: number
  materialTypeName: string
  status: number
  statusName: string
  createTime: string
}

export interface MaterialSavePayload {
  title: string
  description?: string
  fileUrl: string
  materialType: number
}

export const MATERIAL_TYPE_OPTIONS = [
  { label: '文档', value: 1 },
  { label: '视频', value: 2 },
  { label: '其他', value: 3 },
]

export function listMyMaterialsApi() {
  return request.get<MaterialManageItem[]>('/enterprise/my/materials')
}

export function createMaterialApi(data: MaterialSavePayload) {
  return request.post<MaterialManageItem>('/enterprise/my/materials', data)
}

export function updateMaterialApi(id: number, data: MaterialSavePayload) {
  return request.put<MaterialManageItem>(`/enterprise/my/materials/${id}`, data)
}

export function offlineMaterialApi(id: number) {
  return request.post<void>(`/enterprise/my/materials/${id}/offline`)
}

export function onlineMaterialApi(id: number) {
  return request.post<void>(`/enterprise/my/materials/${id}/online`)
}
