import request from '@/utils/request'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface IntegritySummary {
  score: number
  levelName: string
  updateTime?: string
}

export interface IntegrityRecordItem {
  id: number
  changeValue: number
  scoreAfter: number
  eventType: number
  eventTypeName: string
  reason: string
  refType?: string
  refId?: number
  createTime?: string
}

export const INTEGRITY_EVENT_OPTIONS = [
  { label: '全部类型', value: undefined },
  { label: '加分', value: 1 },
  { label: '扣分', value: 2 },
] as const

export interface IntegrityRecordQuery {
  page?: number
  pageSize?: number
  eventType?: number
  startDate?: string
  endDate?: string
}

export function getIntegritySummaryApi() {
  return request.get<IntegritySummary>('/profile/integrity/summary')
}

export function listIntegrityRecordsApi(params: IntegrityRecordQuery = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.eventType !== undefined) query.set('eventType', String(params.eventType))
  if (params.startDate) query.set('startDate', params.startDate)
  if (params.endDate) query.set('endDate', params.endDate)
  return request.get<PageResult<IntegrityRecordItem>>(`/profile/integrity/records?${query.toString()}`)
}

export function scoreLevelColor(score: number) {
  if (score >= 90) return '#52c41a'
  if (score >= 80) return '#2094f3'
  if (score >= 60) return '#fa8c16'
  return '#fa541c'
}
