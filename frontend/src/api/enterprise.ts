import request from '@/utils/request'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

/** 机构类型：1高校 2培训机构 3企业 4其他 */
export const ORG_TYPE_OPTIONS = [
  { label: '全部类型', value: undefined },
  { label: '高校', value: 1 },
  { label: '培训机构', value: 2 },
  { label: '企业', value: 3 },
  { label: '其他', value: 4 },
] as const

export const ORG_TYPE_LABEL: Record<number, string> = {
  1: '高校',
  2: '培训机构',
  3: '企业',
  4: '其他',
}

/** 加盟机构列表项 / 详情 */
export interface OrgListItem {
  id: number
  name: string
  code: string
  type: number
  typeName: string
  logo?: string
  intro?: string
  contact?: string
  phone?: string
  email?: string
  address?: string
  website?: string
}

/** 招聘职位 */
export interface JobPostingItem {
  id: number
  title: string
  description?: string
  requirements?: string
  salaryRange?: string
  location?: string
  eduRequirement?: string
  status: number
  statusName: string
  createTime?: string
}

/** 活动 */
export interface ActivityItem {
  id: number
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
}

/** 企业资料（公开） */
export interface OrgMaterialItem {
  id: number
  title: string
  description?: string
  fileUrl?: string
  materialType: number
  materialTypeName: string
  createTime?: string
}

/** 机构信息（详情/编辑） */
export interface OrgInfo {
  id: number
  name: string
  code: string
  type: number
  logo?: string
  intro?: string
  contact?: string
  phone?: string
  email?: string
  address?: string
  website?: string
}

/** 企业资料 */
export interface OrgMaterial {
  id: number
  orgId: number
  title: string
  fileUrl: string
  fileType?: string
  createTime?: string
}

/** 简历投递 */
export interface JobApplication {
  id: number
  jobId: number
  userId: number
  status: number
  createTime?: string
}

/** 面试邀请 */
export interface InterviewInvitation {
  id: number
  applicationId: number
  orgId: number
  userId: number
  status: number
  interviewTime?: string
}

/** 活动邀请 */
export interface ActivityInvitation {
  id: number
  activityId: number
  orgId: number
  userId: number
  status: number
}

export interface OrgParticipationStatus {
  appliedJobIds: number[]
  registeredActivityIds: number[]
}

export interface JobApplyResult {
  id: number
  jobId: number
  status: number
  statusName: string
  createTime?: string
}

export interface ActivityRegisterResult {
  id: number
  activityId: number
  status: number
  statusName: string
  createTime?: string
}

export interface ApplyJobPayload {
  coverMessage?: string
}

export interface OrgListQuery {
  page?: number
  pageSize?: number
  name?: string
  type?: number
}

export function listJoinedOrgsApi(params: OrgListQuery = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 12))
  if (params.name?.trim()) query.set('name', params.name.trim())
  if (params.type !== undefined) query.set('type', String(params.type))
  return request.get<PageResult<OrgListItem>>(`/enterprise/orgs?${query.toString()}`)
}

export function getJoinedOrgApi(id: number) {
  return request.get<OrgListItem>(`/enterprise/orgs/${id}`)
}

export function listOrgJobsApi(id: number, params: { page?: number; pageSize?: number } = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  return request.get<PageResult<JobPostingItem>>(`/enterprise/orgs/${id}/jobs?${query.toString()}`)
}

export function listOrgActivitiesApi(id: number, params: { page?: number; pageSize?: number } = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  return request.get<PageResult<ActivityItem>>(`/enterprise/orgs/${id}/activities?${query.toString()}`)
}

export function listOrgMaterialsByOrgApi(id: number, params: { page?: number; pageSize?: number } = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  return request.get<PageResult<OrgMaterialItem>>(`/enterprise/orgs/${id}/materials?${query.toString()}`)
}

export function getOrgParticipationStatusApi(orgId: number) {
  return request.get<OrgParticipationStatus>(`/enterprise/my/participation?orgId=${orgId}`)
}

export function applyJobApi(jobId: number, data?: ApplyJobPayload) {
  return request.post<JobApplyResult>(`/enterprise/jobs/${jobId}/apply`, data ?? {})
}

export function registerActivityApi(activityId: number) {
  return request.post<ActivityRegisterResult>(`/enterprise/activities/${activityId}/register`)
}

export function getOrgInfoApi() {
  return request.get<OrgInfo>('/enterprise/org')
}

export function updateOrgInfoApi(data: Partial<OrgInfo>) {
  return request.post<OrgInfo>('/enterprise/org', data)
}

export function listOrgMaterialsApi() {
  return request.get<OrgMaterial[]>('/enterprise/materials')
}

export function createOrgMaterialApi(data: Pick<OrgMaterial, 'title' | 'fileUrl' | 'fileType'>) {
  return request.post<OrgMaterial>('/enterprise/materials', data)
}

export function listJobApplicationsApi() {
  return request.get<JobApplication[]>('/enterprise/applications')
}

export function listInterviewInvitationsApi() {
  return request.get<InterviewInvitation[]>('/enterprise/interviews')
}

export function listActivityInvitationsApi() {
  return request.get<ActivityInvitation[]>('/enterprise/activity-invitations')
}
