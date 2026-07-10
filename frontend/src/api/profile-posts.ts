import request from '@/utils/request'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface MyForumPostItem {
  id: number
  boardId: number
  boardName?: string
  title: string
  content: string
  viewCount?: number
  replyCount?: number
  likeCount?: number
  status: number
  statusName: string
  createTime?: string
}

export interface MyForumReplyItem {
  id: number
  postId: number
  postTitle?: string
  parentId?: number
  content: string
  likeCount?: number
  status: number
  statusName: string
  createTime?: string
}

export interface PostsPageQuery {
  page?: number
  pageSize?: number
}

function buildPageQuery(params: PostsPageQuery = {}) {
  const query = new URLSearchParams()
  query.set('page', String(params.page ?? 1))
  query.set('pageSize', String(params.pageSize ?? 10))
  return query.toString()
}

export function listMyForumPostsApi(params: PostsPageQuery = {}) {
  return request.get<PageResult<MyForumPostItem>>(`/profile/posts?${buildPageQuery(params)}`)
}

export function listMyForumRepliesApi(params: PostsPageQuery = {}) {
  return request.get<PageResult<MyForumReplyItem>>(`/profile/posts/replies?${buildPageQuery(params)}`)
}

export function deleteMyForumPostApi(id: number) {
  return request.delete<void>(`/profile/posts/${id}`)
}

export function deleteMyForumReplyApi(id: number) {
  return request.delete<void>(`/profile/posts/replies/${id}`)
}
