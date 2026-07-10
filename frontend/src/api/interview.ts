import request from '@/utils/request'

export interface JobApplicationItem {
  id: number
  jobId: number
  jobTitle: string
  userId: number
  applicantName: string
  coverMessage?: string
  status: number
  statusName: string
  hasPendingInvite: boolean
  createTime?: string
}

export interface InterviewInvitationItem {
  id: number
  jobId: number
  jobTitle?: string
  orgId: number
  orgName?: string
  fromUserId: number
  fromUserName?: string
  toUserId: number
  toUserName?: string
  applicationId?: number
  messageId?: number
  status: number
  statusName: string
  inviteTime?: string
  location?: string
  createTime?: string
}

export interface SendInterviewInvitePayload {
  applicationId?: number
  jobId?: number
  toUserId?: number
  inviteTime: string
  location: string
  remark?: string
}

export function listMyApplicationsApi() {
  return request.get<JobApplicationItem[]>('/enterprise/my/applications')
}

export function listSentInterviewsApi() {
  return request.get<InterviewInvitationItem[]>('/enterprise/my/interviews')
}

export function sendInterviewInviteApi(data: SendInterviewInvitePayload) {
  return request.post<InterviewInvitationItem>('/enterprise/my/interviews', data)
}

export function listMyInterviewInvitesApi() {
  return request.get<InterviewInvitationItem[]>('/profile/interviews')
}

export function getMyInterviewInviteApi(id: number) {
  return request.get<InterviewInvitationItem>(`/profile/interviews/${id}`)
}

export function acceptInterviewInviteApi(id: number) {
  return request.post<InterviewInvitationItem>(`/profile/interviews/${id}/accept`)
}

export function rejectInterviewInviteApi(id: number) {
  return request.post<InterviewInvitationItem>(`/profile/interviews/${id}/reject`)
}
