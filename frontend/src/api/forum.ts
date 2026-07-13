import request from '@/utils/request'
import type { PageResult } from '@/api/enterprise'

export interface ForumBoard {
  id: number
  name: string
  description?: string
  icon?: string
  sortOrder?: number
  postCount: number
}

export interface ForumPost {
  id: number
  boardId: number
  boardName?: string
  userId: number
  authorName: string
  title: string
  content: string
  viewCount: number
  replyCount: number
  likeCount: number
  isTop?: number
  status: number
  statusName: string
  liked: boolean
  createTime?: string
  updateTime?: string
}

export interface ForumReply {
  id: number
  postId: number
  userId: number
  authorName: string
  parentId?: number
  content: string
  likeCount: number
  status: number
  statusName: string
  liked: boolean
  createTime?: string
}

export interface CreateForumPostPayload {
  boardId: number
  title: string
  content: string
}

export interface CreateForumReplyPayload {
  parentId?: number
  content: string
}

export function listForumBoardsApi() {
  return request.get<ForumBoard[]>('/forum/boards')
}

export function pageForumPostsApi(params: {
  page?: number
  pageSize?: number
  boardId?: number
  keyword?: string
} = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  if (params.boardId) query.set('boardId', String(params.boardId))
  if (params.keyword?.trim()) query.set('keyword', params.keyword.trim())
  return request.get<PageResult<ForumPost>>(`/forum/posts?${query.toString()}`)
}

export function getForumPostApi(id: number) {
  return request.get<ForumPost>(`/forum/posts/${id}`)
}

export function createForumPostApi(data: CreateForumPostPayload) {
  return request.post<ForumPost>('/forum/posts', data)
}

export function pageForumRepliesApi(postId: number, params: { page?: number; pageSize?: number } = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 20))
  return request.get<PageResult<ForumReply>>(`/forum/posts/${postId}/replies?${query.toString()}`)
}

export function createForumReplyApi(postId: number, data: CreateForumReplyPayload) {
  return request.post<ForumReply>(`/forum/posts/${postId}/replies`, data)
}

export function toggleForumLikeApi(targetType: 1 | 2, targetId: number) {
  return request.post<boolean>(`/forum/likes/${targetType}/${targetId}`)
}

export function reportForumApi(targetType: 1 | 2, targetId: number, reason: string) {
  return request.post<void>(`/forum/reports/${targetType}/${targetId}`, { reason })
}
