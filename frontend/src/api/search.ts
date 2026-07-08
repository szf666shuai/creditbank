import request from '@/utils/request'

export interface SearchItem {
  type: string
  typeName: string
  id: number
  title: string
  summary?: string
  coverUrl?: string
  extra?: string
  createTime?: string
}

export interface SearchResponse {
  keyword: string
  type: string
  total: number
  items: SearchItem[]
}

export function searchApi(params: { q: string; type?: string; limit?: number }) {
  const query = new URLSearchParams({
    q: params.q,
    type: params.type ?? 'all',
    limit: String(params.limit ?? 20),
  })
  return request.get<SearchResponse>(`/search?${query.toString()}`)
}
