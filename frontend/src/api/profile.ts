import request from '@/utils/request'
import type { MallOrder } from '@/api/mall'
import type { LearningArchive, LearningCertificate } from '@/api/learning'

export interface CreditAccount {
  userId: number
  balance: number
  totalEarned: number
  totalSpent: number
  integrityScore: number
  integrityLevel: string
  earnMultiplier: number
  canSpend: boolean
}

export interface ProfileSummary {
  creditAccount: CreditAccount
  archives: LearningArchive[]
  certificates: LearningCertificate[]
  orders: MallOrder[]
}

export function fetchProfileSummary() {
  return request.get<ProfileSummary>('/profile/summary')
}
