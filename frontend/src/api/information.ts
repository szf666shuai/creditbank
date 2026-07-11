import request from '@/utils/request'
import type { PageResult } from '@/api/enterprise'

export type InformationType = 'job' | 'activity' | 'policy'

export interface InformationItem {
  type: InformationType
  id: number
  orgId?: number
  orgName?: string
  title: string
  summary?: string
  location?: string
  tag?: string
  status?: number
  statusName?: string
  startTime?: string
  publishTime?: string
  createTime?: string
}

export interface InformationDetail extends InformationItem {
  content?: string
  description?: string
  requirements?: string
  salaryRange?: string
  eduRequirement?: string
  maxParticipants?: number
  creditReward?: number
  source?: string
  author?: string
  coverUrl?: string
  endTime?: string
}

export function pageInformationApi(
  type: InformationType,
  params: { page?: number; pageSize?: number; keyword?: string } = {},
) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.keyword?.trim()) query.set('keyword', params.keyword.trim())
  return request.get<PageResult<InformationItem>>(`/information/${type}?${query.toString()}`)
}

export function getInformationDetailApi(type: InformationType, id: number) {
  return request.get<InformationDetail>(`/information/${type}/${id}`)
}
