import request from '@/utils/request'

export interface CreditTransferRule {
  id: number
  orgId: number
  orgName?: string
  sourceType: number
  sourceTypeName?: string
  sourceCourseId?: number
  sourceCourseName?: string
  sourceTags?: string
  targetType: number
  targetTypeName?: string
  targetCourseId?: number
  targetCourseName?: string
  targetCertificateId?: number
  targetAchievementId?: number
  targetOrgId?: number
  creditRatio?: number
  description?: string
  status: number
  statusName?: string
  createTime?: string
  updateTime?: string
}

export interface CreditTransferRulePayload {
  sourceType?: number
  sourceCourseId?: number
  sourceTags?: string
  targetType?: number
  targetCourseId?: number
  targetCertificateId?: number
  targetAchievementId?: number
  targetOrgId?: number
  creditRatio?: number
  description?: string
  status?: number
}

export interface CreditTransferApplication {
  id: number
  userId: number
  userName?: string
  sourceType: number
  sourceTypeName?: string
  sourceCourseId?: number
  sourceCourseName?: string
  sourceAchievementId?: number
  sourceAchievementTitle?: string
  sourceOrgId?: number
  sourceOrgName?: string
  sourceCredit: number
  targetType: number
  targetTypeName?: string
  targetCourseId?: number
  targetCourseName?: string
  targetCertificateId?: number
  targetAchievementId?: number
  targetAchievementTitle?: string
  targetOrgId?: number
  targetOrgName?: string
  applyReason?: string
  status: number
  statusName?: string
  reviewerId?: number
  reviewerName?: string
  reviewComment?: string
  actualCredit?: number
  applyTime?: string
  reviewTime?: string
}

export interface CreditTransferApplyPayload {
  sourceType: number
  sourceCourseId?: number
  sourceAchievementId?: number
  targetType?: number
  targetCourseId?: number
  targetCertificateId?: number
  targetAchievementId?: number
  targetOrgId?: number
  applyReason?: string
}

export function listRulesApi() {
  return request.get<CreditTransferRule[]>('/credit-transfer/rules')
}

export function getRuleApi(ruleId: number) {
  return request.get<CreditTransferRule>(`/credit-transfer/rules/${ruleId}`)
}

export function createRuleApi(data: CreditTransferRulePayload) {
  return request.post<CreditTransferRule>('/credit-transfer/rules', data)
}

export function updateRuleApi(ruleId: number, data: CreditTransferRulePayload) {
  return request.put<CreditTransferRule>(`/credit-transfer/rules/${ruleId}`, data)
}

export function deleteRuleApi(ruleId: number) {
  return request.delete(`/credit-transfer/rules/${ruleId}`)
}

export function applyTransferApi(data: CreditTransferApplyPayload) {
  return request.post<CreditTransferApplication>('/credit-transfer/apply', data)
}

export function listApplicationsApi(status?: number) {
  const params: Record<string, string> = {}
  if (status !== undefined) params.status = String(status)
  return request.get<CreditTransferApplication[]>(`/credit-transfer/applications?${new URLSearchParams(params).toString()}`)
}

export function listMyApplicationsApi() {
  return request.get<CreditTransferApplication[]>('/credit-transfer/my-applications')
}

export function reviewApplicationApi(applicationId: number, status: number, comment?: string) {
  const params: Record<string, string> = { status: String(status) }
  if (comment) params.comment = comment
  return request.put<CreditTransferApplication>(`/credit-transfer/applications/${applicationId}/review?${new URLSearchParams(params).toString()}`)
}

export function matchRulesApi(sourceType: number, sourceCourseId?: number, sourceAchievementId?: number) {
  const params: Record<string, string> = { sourceType: String(sourceType) }
  if (sourceCourseId) params.sourceCourseId = String(sourceCourseId)
  if (sourceAchievementId) params.sourceAchievementId = String(sourceAchievementId)
  return request.get<CreditTransferRule[]>(`/credit-transfer/match-rules?${new URLSearchParams(params).toString()}`)
}
