import request from '@/utils/request'

export interface LearningSituation {
  userId: number
  username?: string
  realName?: string
  role?: number
  creditBalance?: number
  creditEarned?: number
  creditSpent?: number
  integrityScore?: number
  courses?: Record<string, unknown>[]
  archives?: Record<string, unknown>[]
  achievements?: Record<string, unknown>[]
  targetTags?: Record<string, unknown>[]
  forumPosts?: Record<string, unknown>[]
  recentCredits?: Record<string, unknown>[]
}

export interface LearningProfile {
  userId: number
  targetJob?: string
  summary?: string
  profile?: {
    stage?: string
    skills?: string[]
    strengths?: string[]
    gaps?: string[]
    suggestions?: string[]
    [key: string]: unknown
  }
  updateTime?: string
  situation?: LearningSituation
}

export function getLearningProfileApi() {
  return request.get<LearningProfile>('/agent/profile')
}

export function getLearningSituationApi() {
  return request.get<LearningSituation>('/agent/profile/situation')
}

export function generateLearningProfileApi() {
  return request.post<LearningProfile>('/agent/profile/generate', undefined, {
    timeout: 90000,
  })
}
