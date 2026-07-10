/** 格式化日期时间：2024-01-01T10:00:00 -> 2024-01-01 10:00 */
export function formatTime(value?: string | null, length = 16): string {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, length)
}

/** 格式化日期：2024-01-01 */
export function formatDate(value?: string | null): string {
  if (!value) return '-'
  return value.slice(0, 10)
}
