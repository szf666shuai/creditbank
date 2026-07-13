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

export interface CourseComment {
  id: number
  courseId: number
  userId: number
  parentId?: number | null
  authorName: string
  replyToAuthorName?: string
  avatar?: string
  content: string
  likeCount: number
  liked?: boolean
  creditReward?: number
  createTime: string
  replies?: CourseComment[]
}

export interface CourseCommentLikeResult {
  commentId: number
  likeCount: number
  liked: boolean
}

export interface CourseDanmaku {
  id: number
  courseId: number
  userId: number
  authorName: string
  content: string
  videoTimeSeconds: number
  color: string
  createTime: string
}

export interface CourseMaterial {
  id: number
  courseId: number
  title: string
  fileType: string
  fileUrl: string
  sortOrder: number
}

export function fetchLearningResource(courseId: number) {
  return request.get<LearningResource>(`/learning/resources/${courseId}`)
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

export interface CourseEpisode {
  id: number
  courseId: number
  title: string
  videoUrl?: string
  videoDurationSeconds?: number
  sortOrder?: number
  progress?: number
  maxWatchedPositionSeconds?: number
  lastPositionSeconds?: number
}

export interface LearningReminder {
  courseId: number
  enabled: boolean
  intervalMinutes: number
}

export interface LearningCheckin {
  courseId: number
  checkedInToday: boolean
  streakDays: number
  creditReward?: number
  message?: string
}

export function fetchCourseEpisodes(courseId: number) {
  return request.get<CourseEpisode[]>(`/learning/resources/${courseId}/episodes`)
}

export function fetchLearningReminder(courseId: number) {
  return request.get<LearningReminder>(`/learning/resources/${courseId}/reminder`)
}

export function saveLearningReminder(
  courseId: number,
  payload: { enabled: boolean; intervalMinutes: number },
) {
  return request.post<LearningReminder>(`/learning/resources/${courseId}/reminder`, payload)
}

export function fetchLearningCheckinStatus(courseId: number) {
  return request.get<LearningCheckin>(`/learning/resources/${courseId}/checkin/status`)
}

export function postLearningCheckin(courseId: number) {
  return request.post<LearningCheckin>(`/learning/resources/${courseId}/checkin`)
}

export function purchaseCourse(courseId: number) {
  return request.post<{
    courseId: number
    paidCredit: number
    balanceAfter?: number
    purchased: boolean
  }>(`/learning/resources/${courseId}/purchase`)
}

export function reportLearningProgress(
  courseId: number,
  payload: { watchedDeltaSeconds: number; currentTimeSeconds: number; episodeId?: number },
) {
  return request.post<LearningRecord>(`/learning/resources/${courseId}/progress`, payload)
}

export function completeLearning(courseId: number) {
  return request.post<LearningCompletionResult>(`/learning/resources/${courseId}/complete`)
}

export function fetchCourseComments(courseId: number, limit = 50) {
  return request.get<CourseComment[]>(`/learning/resources/${courseId}/comments?limit=${limit}`)
}

export function postCourseComment(courseId: number, payload: { content: string; parentId?: number }) {
  return request.post<CourseComment>(`/learning/resources/${courseId}/comments`, payload)
}

export function toggleCourseCommentLike(courseId: number, commentId: number) {
  return request.post<CourseCommentLikeResult>(
    `/learning/resources/${courseId}/comments/${commentId}/like`,
  )
}

export function fetchCourseDanmaku(courseId: number) {
  return request.get<CourseDanmaku[]>(`/learning/resources/${courseId}/danmaku`)
}

export function postCourseDanmaku(
  courseId: number,
  payload: { content: string; videoTimeSeconds: number; color?: string },
) {
  return request.post<CourseDanmaku>(`/learning/resources/${courseId}/danmaku`, payload)
}

export function fetchCourseMaterials(courseId: number) {
  return request.get<CourseMaterial[]>(`/learning/resources/${courseId}/materials`)
}

export function fetchLearningArchives(limit = 20) {
  return request.get<LearningArchive[]>(`/learning/archives?limit=${limit}`)
}

export function fetchLearningCertificates(limit = 20) {
  return request.get<LearningCertificate[]>(`/learning/certificates?limit=${limit}`)
}
