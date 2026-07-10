import request from '@/utils/request'

export interface OrgProfile {
  id: number
  name: string
  code: string
  type: number
  typeName: string
  logo?: string
  intro?: string
  contact?: string
  phone?: string
  email?: string
  address?: string
  website?: string
}

export interface UpdateOrgPayload {
  name: string
  intro?: string
  contact?: string
  phone?: string
  email?: string
  address?: string
  website?: string
  logo?: string
}

export function getMyOrgApi() {
  return request.get<OrgProfile>('/enterprise/my/org')
}

export function updateMyOrgApi(data: UpdateOrgPayload) {
  return request.put<OrgProfile>('/enterprise/my/org', data)
}
