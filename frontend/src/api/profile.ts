import request from '@/utils/request'

/** 学习画像 */
export interface UserLearningProfile {
  userId: number
  totalHours: number
  completedCourses: number
  certificates: number
}

/** 每日学习统计 */
export interface LearningStatDaily {
  statDate: string
  studyMinutes: number
  coursesCompleted: number
  creditEarned?: number
}

export function getLearningProfileApi() {
  return request.get<UserLearningProfile>('/profile/learning-profile')
}

export function listLearningStatsApi(params?: { startDate?: string; endDate?: string }) {
  const query = new URLSearchParams()
  if (params?.startDate) query.set('startDate', params.startDate)
  if (params?.endDate) query.set('endDate', params.endDate)
  const qs = query.toString()
  return request.get<LearningStatDaily[]>(`/profile/learning-stats${qs ? `?${qs}` : ''}`)
}
