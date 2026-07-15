import request from '@/utils/request'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface AdminDashboardStats {
  totalUsers: number
  studentCount: number
  enterpriseCount: number
  adminCount: number
  totalOrganizations: number
  pendingOrganizations: number
  activeOrganizations: number
  pendingReports: number
  totalJobs: number
  activeJobs: number
  totalActivities: number
  activeActivities: number
  unreadMessages: number
}

export interface AdminOrganization {
  id: number
  name: string
  code: string
  type: number
  typeName: string
  contact?: string
  phone?: string
  email?: string
  joinStatus: number
  joinStatusName: string
  status: number
  statusName: string
  createTime?: string
}

export interface AdminUser {
  id: number
  username: string
  realName?: string
  phone?: string
  email?: string
  role: number
  roleName: string
  orgId?: number
  orgName?: string
  status: number
  statusName: string
  createTime?: string
}

export interface AdminForumReport {
  id: number
  userId: number
  reporterName?: string
  targetType: number
  targetTypeName: string
  targetId: number
  targetSummary?: string
  reason: string
  status: number
  statusName: string
  handleRemark?: string
  createTime?: string
}

export interface AdminIntegrityRecord {
  id: number
  userId: number
  userName?: string
  changeValue: number
  scoreAfter: number
  eventType: number
  eventTypeName: string
  reason: string
  refType?: string
  refId?: number
  createTime?: string
}

export interface AdminCreditTransaction {
  id: number
  userId: number
  userName?: string
  type: number
  typeName: string
  amount: number
  balanceAfter?: number
  bizType?: string
  source?: string
  createTime?: string
}

export interface AdminJob {
  id: number
  orgId: number
  orgName?: string
  title: string
  location?: string
  salaryRange?: string
  status: number
  statusName: string
  viewCount?: number
  createTime?: string
}

export interface AdminActivity {
  id: number
  orgId: number
  orgName?: string
  title: string
  location?: string
  startTime?: string
  endTime?: string
  status: number
  statusName: string
  createTime?: string
}

export const JOIN_STATUS_OPTIONS = [
  { label: '全部加盟状态', value: undefined },
  { label: '待审核', value: 0 },
  { label: '已加盟', value: 1 },
  { label: '已退出', value: 2 },
] as const

export const USER_ROLE_OPTIONS = [
  { label: '全部角色', value: undefined },
  { label: '学员', value: 0 },
  { label: '企业用户', value: 1 },
  { label: '管理员', value: 2 },
] as const

export const USER_STATUS_OPTIONS = [
  { label: '全部状态', value: undefined },
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 },
] as const

export const REPORT_STATUS_OPTIONS = [
  { label: '全部状态', value: undefined },
  { label: '待处理', value: 0 },
  { label: '已处理', value: 1 },
  { label: '已驳回', value: 2 },
] as const

export function getAdminDashboardStatsApi() {
  return request.get<AdminDashboardStats>('/admin/dashboard/stats')
}

export function listAdminOrganizationsApi(params: {
  page?: number
  pageSize?: number
  joinStatus?: number
  status?: number
  keyword?: string
} = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.joinStatus !== undefined) query.set('joinStatus', String(params.joinStatus))
  if (params.status !== undefined) query.set('status', String(params.status))
  if (params.keyword) query.set('keyword', params.keyword)
  return request.get<PageResult<AdminOrganization>>(`/admin/organizations?${query.toString()}`)
}

export function updateOrgJoinStatusApi(id: number, joinStatus: number) {
  return request.patch<AdminOrganization>(`/admin/organizations/${id}/join-status`, { joinStatus })
}

export function updateOrgStatusApi(id: number, status: number) {
  return request.patch<AdminOrganization>(`/admin/organizations/${id}/status`, { status })
}

export function listAdminUsersApi(params: {
  page?: number
  pageSize?: number
  role?: number
  status?: number
  keyword?: string
} = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.role !== undefined) query.set('role', String(params.role))
  if (params.status !== undefined) query.set('status', String(params.status))
  if (params.keyword) query.set('keyword', params.keyword)
  return request.get<PageResult<AdminUser>>(`/admin/users?${query.toString()}`)
}

export function updateUserStatusApi(id: number, status: number) {
  return request.patch<AdminUser>(`/admin/users/${id}/status`, { status })
}

export function createEnterpriseUserApi(data: {
  username: string
  password: string
  realName: string
  orgId: number
  phone?: string
  email?: string
}) {
  return request.post<AdminUser>('/admin/users/enterprise', data)
}

export function sendSystemNotificationApi(data: {
  title: string
  content: string
  scope: 'all' | 'role' | 'user'
  targetRole?: number
  targetUserId?: number
}) {
  return request.post<{ sentCount: number }>('/admin/notifications', data)
}

export function listAdminReportsApi(params: { page?: number; pageSize?: number; status?: number } = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.status !== undefined) query.set('status', String(params.status))
  return request.get<PageResult<AdminForumReport>>(`/admin/reports?${query.toString()}`)
}

export function handleAdminReportApi(
  id: number,
  data: { status: number; handleRemark?: string; hideTarget?: boolean },
) {
  return request.post<AdminForumReport>(`/admin/reports/${id}/handle`, data)
}

export function listAdminIntegrityRecordsApi(params: {
  page?: number
  pageSize?: number
  eventType?: number
  userId?: number
} = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.eventType !== undefined) query.set('eventType', String(params.eventType))
  if (params.userId !== undefined) query.set('userId', String(params.userId))
  return request.get<PageResult<AdminIntegrityRecord>>(`/admin/oversight/integrity-records?${query.toString()}`)
}

export function listAdminCreditTransactionsApi(params: {
  page?: number
  pageSize?: number
  type?: number
  userId?: number
} = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.type !== undefined) query.set('type', String(params.type))
  if (params.userId !== undefined) query.set('userId', String(params.userId))
  return request.get<PageResult<AdminCreditTransaction>>(`/admin/oversight/credit-transactions?${query.toString()}`)
}

export function listAdminJobsApi(params: {
  page?: number
  pageSize?: number
  status?: number
  keyword?: string
} = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.status !== undefined) query.set('status', String(params.status))
  if (params.keyword) query.set('keyword', params.keyword)
  return request.get<PageResult<AdminJob>>(`/admin/oversight/jobs?${query.toString()}`)
}

export function updateAdminJobStatusApi(id: number, status: number) {
  return request.patch<AdminJob>(`/admin/oversight/jobs/${id}/status`, { status })
}

export function listAdminActivitiesApi(params: {
  page?: number
  pageSize?: number
  status?: number
  keyword?: string
} = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.status !== undefined) query.set('status', String(params.status))
  if (params.keyword) query.set('keyword', params.keyword)
  return request.get<PageResult<AdminActivity>>(`/admin/oversight/activities?${query.toString()}`)
}

export function updateAdminActivityStatusApi(id: number, status: number) {
  return request.patch<AdminActivity>(`/admin/oversight/activities/${id}/status`, { status })
}

export interface AdminCourse {
  id: number
  orgId: number
  orgName?: string
  title: string
  description?: string
  coverUrl?: string
  tags?: string[]
  creditValue: number
  duration: number
  difficulty: number
  difficultyName?: string
  status: number
  approvalStatus: number
  approvalStatusName: string
  reviewRemark?: string
  createTime?: string
}

export const COURSE_APPROVAL_OPTIONS = [
  { label: '全部审核状态', value: undefined },
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 2 },
] as const

export function listAdminCoursesApi(params: {
  page?: number
  pageSize?: number
  approvalStatus?: number
  keyword?: string
} = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.approvalStatus !== undefined) query.set('approvalStatus', String(params.approvalStatus))
  if (params.keyword) query.set('keyword', params.keyword)
  return request.get<PageResult<AdminCourse>>(`/admin/oversight/courses?${query.toString()}`)
}

export function reviewAdminCourseApi(
  id: number,
  data: { approvalStatus: number; reviewRemark?: string },
) {
  return request.patch<AdminCourse>(`/admin/oversight/courses/${id}/approval`, data)
}
