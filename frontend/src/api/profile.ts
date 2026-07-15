import request from '@/utils/request'
import type { LearningArchive, LearningCertificate } from '@/api/learning'

export interface CreditAccount {
  userId: number
  totalEarned: number
  integrityScore: number
  integrityLevel: string
  earnMultiplier: number
}

export interface ProfileSummary {
  creditAccount: CreditAccount
  archives: LearningArchive[]
  certificates: LearningCertificate[]
}

/** 学习画像（个人中心统计卡片） */
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

export function fetchProfileSummary() {
  return request.get<ProfileSummary>('/profile/summary')
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
