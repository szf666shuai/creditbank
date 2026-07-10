import request from '@/utils/request'

export interface UserMessage {
  id: number
  fromUserId: number
  fromUserName?: string
  toUserId: number
  toUserName?: string
  title: string
  content: string
  msgType: number
  readStatus: number
  refType?: string
  refId?: number
  createTime?: string
}

export interface MessageRecipient {
  id: number
  username: string
  realName?: string
  displayName: string
  role: number
  roleName: string
}

export interface SendMessagePayload {
  receiverId: number
  title: string
  content: string
  msgType?: number
}

export type MessageBox = 'inbox' | 'outbox'

/** 1普通私信 2面试邀请 3活动邀请 4系统通知 */
export const MSG_TYPE_NORMAL = 1
export const MSG_TYPE_INTERVIEW = 2
export const MSG_TYPE_ACTIVITY = 3
export const MSG_TYPE_SYSTEM = 4

export type InboxCategory = 'all' | 'normal' | 'interview' | 'activity'

export const MSG_TYPE_LABEL: Record<number, string> = {
  [MSG_TYPE_NORMAL]: '私信',
  [MSG_TYPE_INTERVIEW]: '面试邀请',
  [MSG_TYPE_ACTIVITY]: '活动邀请',
  [MSG_TYPE_SYSTEM]: '系统通知',
}

export function messageTypeLabel(msgType?: number) {
  if (msgType == null) return '私信'
  return MSG_TYPE_LABEL[msgType] ?? '消息'
}

export function messageTypeTagType(msgType?: number) {
  if (msgType === MSG_TYPE_INTERVIEW) return 'warning'
  if (msgType === MSG_TYPE_ACTIVITY) return 'success'
  if (msgType === MSG_TYPE_SYSTEM) return 'info'
  return ''
}

/** 面试/活动邀请类消息（仅学员在消息中心展示） */
export function isInviteMessageType(msgType?: number) {
  return msgType === MSG_TYPE_INTERVIEW || msgType === MSG_TYPE_ACTIVITY
}

export function listMessagesApi(params?: { box?: MessageBox; isRead?: number }) {
  const query = new URLSearchParams()
  if (params?.box) query.set('box', params.box)
  if (params?.isRead !== undefined) query.set('isRead', String(params.isRead))
  const qs = query.toString()
  return request.get<UserMessage[]>(`/messages${qs ? `?${qs}` : ''}`)
}

export function listInboxApi(isRead?: number) {
  const query = isRead !== undefined ? `?isRead=${isRead}` : ''
  return request.get<UserMessage[]>(`/messages/inbox${query}`)
}

export function listOutboxApi() {
  return request.get<UserMessage[]>('/messages/outbox')
}

export function getMessageApi(id: number) {
  return request.get<UserMessage>(`/messages/${id}`)
}

export function sendMessageApi(data: SendMessagePayload) {
  return request.post<UserMessage>('/messages', data)
}

export function markMessageReadApi(id: number) {
  return request.post<void>(`/messages/${id}/read`)
}

export function getUnreadCountApi() {
  return request.get<number>('/messages/unread-count')
}

export function searchRecipientsApi(keyword: string) {
  return request.get<MessageRecipient[]>(
    `/messages/recipients?keyword=${encodeURIComponent(keyword)}`,
  )
}
