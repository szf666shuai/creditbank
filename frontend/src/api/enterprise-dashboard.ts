import request from '@/utils/request'

export interface EnterpriseDashboard {
  orgId: number
  orgName?: string
  joinStatus?: number
  joinStatusName?: string
  writable?: boolean
  openJobCount: number
  ongoingActivityCount: number
  registeringActivityCount: number
  pendingApplicationCount: number
  pendingInterviewCount: number
  pendingTransferCount: number
  materialCount: number
}

export function getEnterpriseDashboardApi() {
  return request.get<EnterpriseDashboard>('/enterprise/my/dashboard')
}
