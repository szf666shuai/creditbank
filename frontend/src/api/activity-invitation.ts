import request from '@/utils/request'

export interface ActivityInvitationItem {
  id: number
  activityId: number
  activityTitle?: string
  orgId?: number
  orgName?: string
  location?: string
  startTime?: string
  endTime?: string
  creditReward?: number
  fromUserId: number
  fromUserName?: string
  toUserId: number
  toUserName?: string
  messageId?: number
  status: number
  statusName: string
  createTime?: string
}

export interface SendActivityInvitePayload {
  activityId: number
  toUserId: number
  remark?: string
}

export function listSentActivityInvitesApi() {
  return request.get<ActivityInvitationItem[]>('/enterprise/my/activity-invitations')
}

export function sendActivityInviteApi(data: SendActivityInvitePayload) {
  return request.post<ActivityInvitationItem>('/enterprise/my/activity-invitations', data)
}

export function listMyActivityInvitesApi() {
  return request.get<ActivityInvitationItem[]>('/profile/activity-invitations')
}

export function getMyActivityInviteApi(id: number) {
  return request.get<ActivityInvitationItem>(`/profile/activity-invitations/${id}`)
}

export function acceptActivityInviteApi(id: number) {
  return request.post<ActivityInvitationItem>(`/profile/activity-invitations/${id}/accept`)
}

export function rejectActivityInviteApi(id: number) {
  return request.post<ActivityInvitationItem>(`/profile/activity-invitations/${id}/reject`)
}
