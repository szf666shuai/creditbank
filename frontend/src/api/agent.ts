import request from '@/utils/request'

export interface AgentChatTurn {
  role: 'user' | 'assistant'
  content: string
}

export interface AgentChatResult {
  reply: string
  model: string
}

export function agentChatApi(
  message: string,
  history: AgentChatTurn[] = [],
  context?: string,
) {
  return request.post<AgentChatResult>(
    '/agent/chat',
    { message, history, context },
    { timeout: 60000 },
  )
}
