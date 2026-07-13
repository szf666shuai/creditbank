import request from '@/utils/request'

export interface ActivityCheckinResult {
  registrationId: number
  activityId: number
  status: number
  statusName: string
  checkInTime?: string
  message?: string
  creditGranted?: boolean
  creditReward?: number
  creditMessage?: string
}

export function checkInActivityApi(activityId: number) {
  return request.post<ActivityCheckinResult>(`/enterprise/activities/${activityId}/check-in`)
}
