import request from '@/utils/request'
import type { UserInfo } from '@/types/auth'

export interface ProfileDashboard {
  userInfo: UserInfo
  creditBalance: number
  totalEarned: number
  integrityScore: number | null
  unreadMessageCount: number
}

export function getProfileDashboardApi() {
  return request.get<ProfileDashboard>('/profile/dashboard')
}
