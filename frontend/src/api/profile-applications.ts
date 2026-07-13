import request from '@/utils/request'

export interface MyJobApplicationItem {
  id: number
  jobId: number
  jobTitle?: string
  orgId?: number
  orgName?: string
  jobLocation?: string
  salaryRange?: string
  coverMessage?: string
  status: number
  statusName: string
  jobUnavailable?: boolean
  createTime?: string
  updateTime?: string
}

export const APPLICATION_STATUS_OPTIONS = [
  { label: '全部状态', value: undefined },
  { label: '已投递', value: 0 },
  { label: '已查看', value: 1 },
  { label: '面试中', value: 2 },
  { label: '录用', value: 3 },
  { label: '已拒绝', value: 4 },
  { label: '面试取消', value: 5 },
] as const

export function listMyJobApplicationsApi(status?: number) {
  const query = status !== undefined ? `?status=${status}` : ''
  return request.get<MyJobApplicationItem[]>(`/profile/applications${query}`)
}

export function applicationStatusTagType(status: number) {
  if (status === 3) return 'success'
  if (status === 4) return 'danger'
  if (status === 5) return 'info'
  if (status === 2) return 'warning'
  if (status === 1) return 'primary'
  return 'info'
}
