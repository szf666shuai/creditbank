import request from '@/utils/request'

export interface MyActivityRegistrationItem {
  id: number
  activityId: number
  activityTitle: string
  orgId: number
  orgName?: string
  startTime?: string
  endTime?: string
  location?: string
  creditReward?: number
  activityStatus: number
  activityStatusName: string
  status: number
  statusName: string
  createTime?: string
}

export function listMyActivityRegistrationsApi() {
  return request.get<MyActivityRegistrationItem[]>('/profile/activity-registrations')
}
