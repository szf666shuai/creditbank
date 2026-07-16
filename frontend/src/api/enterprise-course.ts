import request from '@/utils/request'

export interface EnterpriseCourse {
  id: number
  title: string
  description?: string
  coverUrl?: string
  /** 后端存逗号分隔字符串；兼容偶发数组 */
  tags?: string | string[]
  creditValue: number
  creditReward?: number
  duration: number
  durationMinutes?: number
  difficulty: number
  difficultyName?: string
  status: number
  statusName?: string
  approvalStatus: number
  approvalStatusName?: string
  reviewRemark?: string
  createTime?: string
  updateTime?: string
}

export interface EnterpriseCourseSavePayload {
  title: string
  description?: string
  coverUrl?: string
  tags?: string
  creditValue: number
  duration: number
  difficulty: number
}

export function listEnterpriseCoursesApi() {
  return request.get<EnterpriseCourse[]>('/enterprise/my/courses')
}

export function createEnterpriseCourseApi(data: EnterpriseCourseSavePayload) {
  return request.post<EnterpriseCourse>('/enterprise/my/courses', data)
}

export function updateEnterpriseCourseApi(id: number, data: EnterpriseCourseSavePayload) {
  return request.put<EnterpriseCourse>(`/enterprise/my/courses/${id}`, data)
}

export function deleteEnterpriseCourseApi(id: number) {
  return request.delete(`/enterprise/my/courses/${id}`)
}