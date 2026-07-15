import request from '@/utils/request'

export interface ActivityCheckinResult {
  registrationId: number
  activityId: number
  status: number
  statusName: string
  checkInTime?: string
  message?: string
  integrityGranted?: boolean
  integrityReward?: number
  integrityMessage?: string
}

export function checkInActivityApi(activityId: number) {
  return request.post<ActivityCheckinResult>(`/enterprise/activities/${activityId}/check-in`)
}
