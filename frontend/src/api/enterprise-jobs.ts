import request from '@/utils/request'

export interface SkillTag {
  id: number
  name: string
}

export interface JobManageItem {
  id: number
  orgId: number
  title: string
  description?: string
  requirements?: string
  salaryRange?: string
  location?: string
  eduRequirement?: string
  status: number
  statusName: string
  createTime?: string
  updateTime?: string
  tags: SkillTag[]
}

export interface JobSavePayload {
  title: string
  description?: string
  requirements?: string
  salaryRange?: string
  location?: string
  eduRequirement?: string
  tagIds?: number[]
}

export function listSkillTagsApi() {
  return request.get<SkillTag[]>('/enterprise/tags')
}

export function listMyJobsApi() {
  return request.get<JobManageItem[]>('/enterprise/my/jobs')
}

export function getMyJobApi(id: number) {
  return request.get<JobManageItem>(`/enterprise/my/jobs/${id}`)
}

export function createJobApi(data: JobSavePayload) {
  return request.post<JobManageItem>('/enterprise/my/jobs', data)
}

export function updateJobApi(id: number, data: JobSavePayload) {
  return request.put<JobManageItem>(`/enterprise/my/jobs/${id}`, data)
}

export function offlineJobApi(id: number) {
  return request.post<JobManageItem>(`/enterprise/my/jobs/${id}/offline`)
}

export function onlineJobApi(id: number) {
  return request.post<JobManageItem>(`/enterprise/my/jobs/${id}/online`)
}
