import type { ApiResponse } from '@/utils/request'

export function unwrapApi<T>(res: ApiResponse<T>): T {
  if (res.code !== 200) {
    throw new Error(res.message || '请求失败')
  }
  return res.data
}

export function getErrorMessage(error: unknown, fallback = '操作失败'): string {
  return error instanceof Error ? error.message : fallback
}
