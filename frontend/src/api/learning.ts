import request from '@/utils/request'
import type { CreditChangeResult } from '@/api/mall'

export interface LearningResource {
  id: number
  title: string
  description?: string
  coverUrl?: string
  videoUrl?: string
  videoDurationSeconds?: number
  priceCredit?: number
  priceMoney?: number
  durationHours?: number
  creditReward?: number
  orgName?: string
  tags?: string
  progress?: number
  watchedSeconds?: number
  maxWatchedPositionSeconds?: number
  lastPositionSeconds?: number
  learningStatus?: number
  purchased?: boolean
  paid?: boolean
  learned?: boolean
  purchaseProductId?: number
  certificateId?: number
  certNo?: string
}

export interface LearningCertificate {
  id: number
  certNo: string
  userId: number
  courseId: number
  title: string
  qrContent: string
  qrImageUrl: string
  blockchainHash: string
  verifyStatus: number
  verifyStatusName: string
  issuedAt: string
}

export interface LearningArchive {
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
  createTime: string
}

export interface LearningCompletionResult {
  userCourseId: number
  certificate: LearningCertificate
  archive: LearningArchive
  creditChange?: CreditChangeResult
}

export interface LearningRecord {
  id: number
  userId: number
  courseId: number
  progress: number
  watchedSeconds: number
  maxWatchedPositionSeconds: number
  lastPositionSeconds: number
  status: number
}

export function fetchLearningResources(params?: { keyword?: string; tag?: string }) {
  const query = new URLSearchParams()
  if (params?.keyword) query.set('keyword', params.keyword)
  if (params?.tag) query.set('tag', params.tag)
  const suffix = query.toString() ? `?${query.toString()}` : ''
  return request.get<LearningResource[]>(`/learning/resources${suffix}`)
}

export function fetchLearningTags() {
  return request.get<string[]>('/learning/tags')
}

export function startLearning(courseId: number) {
  return request.post<LearningRecord>(`/learning/resources/${courseId}/start`)
}

export function reportLearningProgress(
  courseId: number,
  payload: { watchedDeltaSeconds: number; currentTimeSeconds: number },
) {
  return request.post<LearningRecord>(`/learning/resources/${courseId}/progress`, payload)
}

export function completeLearning(courseId: number) {
  return request.post<LearningCompletionResult>(`/learning/resources/${courseId}/complete`)
}

export function fetchLearningArchives(limit = 20) {
  return request.get<LearningArchive[]>(`/learning/archives?limit=${limit}`)
}

export function fetchLearningCertificates(limit = 20) {
  return request.get<LearningCertificate[]>(`/learning/certificates?limit=${limit}`)
}
