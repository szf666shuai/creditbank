import request from '@/utils/request'

export interface LearningArchiveItem {
  id: number
  title: string
  archiveType: number
  archiveTypeName: string
  courseId?: number
  certificateId?: number
  category?: string
  description?: string
  startDate?: string
  endDate?: string
  creditEarned?: number
  status: number
  statusName: string
  createTime?: string
}

export interface LearningAchievementItem {
  id: number
  title: string
  type: number
  typeName: string
  orgId?: number
  orgName?: string
  certificateId?: number
  creditValue?: number
  fileUrl?: string
  verifyStatus: number
  verifyStatusName: string
  blockchainHash?: string
  createTime?: string
}

export function listLearningArchivesApi() {
  return request.get<LearningArchiveItem[]>('/profile/learning/archives')
}

export function listLearningAchievementsApi() {
  return request.get<LearningAchievementItem[]>('/profile/learning/achievements')
}
