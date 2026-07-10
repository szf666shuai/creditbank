import request from '@/utils/request'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface CreditAccountSummary {
  balance: number
  totalEarned: number
  totalSpent: number
}

export interface CreditTransactionItem {
  id: number
  type: number
  typeName: string
  amount: number
  balanceAfter?: number
  bizType?: string
  source?: string
  refType?: string
  refId?: number
  createTime?: string
}

export const CREDIT_TYPE_OPTIONS = [
  { label: '全部类型', value: undefined },
  { label: '获取', value: 1 },
  { label: '转换', value: 2 },
  { label: '增长', value: 3 },
  { label: '消耗', value: 4 },
] as const

export interface CreditTransactionQuery {
  page?: number
  pageSize?: number
  type?: number
  startDate?: string
  endDate?: string
}

export function getCreditSummaryApi() {
  return request.get<CreditAccountSummary>('/profile/credit/summary')
}

export function listCreditTransactionsApi(params: CreditTransactionQuery = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.type !== undefined) query.set('type', String(params.type))
  if (params.startDate) query.set('startDate', params.startDate)
  if (params.endDate) query.set('endDate', params.endDate)
  return request.get<PageResult<CreditTransactionItem>>(`/profile/credit/transactions?${query.toString()}`)
}
