import request from '@/utils/request'

export interface ActivityManageItem {
  id: number
  orgId: number
  title: string
  description?: string
  location?: string
  startTime?: string
  endTime?: string
  maxParticipants?: number
  creditReward?: number
  status: number
  statusName: string
  createTime?: string
  updateTime?: string
}

export interface ActivitySavePayload {
  title: string
  description?: string
  location?: string
  startTime: string
  endTime: string
  maxParticipants?: number
  creditReward?: number
}

export function listMyActivitiesApi() {
  return request.get<ActivityManageItem[]>('/enterprise/my/activities')
}

export function getMyActivityApi(id: number) {
  return request.get<ActivityManageItem>(`/enterprise/my/activities/${id}`)
}

export function createActivityApi(data: ActivitySavePayload) {
  return request.post<ActivityManageItem>('/enterprise/my/activities', data)
}

export function updateActivityApi(id: number, data: ActivitySavePayload) {
  return request.put<ActivityManageItem>(`/enterprise/my/activities/${id}`, data)
}

export function cancelActivityApi(id: number) {
  return request.post<ActivityManageItem>(`/enterprise/my/activities/${id}/cancel`)
}
