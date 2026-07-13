/** 登录 token 存储键 */
export const AUTH_TOKEN_KEY = 'credit_bank_token'

/**
 * 开发环境用 sessionStorage（每个标签页独立），方便同浏览器双开测多角色；
 * 生产环境用 localStorage（同域标签页共享登录态）。
 */
export function getAuthStorage(): Storage {
  return import.meta.env.DEV ? sessionStorage : localStorage
}

export function readAuthToken(): string | null {
  return getAuthStorage().getItem(AUTH_TOKEN_KEY)
}

export function writeAuthToken(token: string): void {
  getAuthStorage().setItem(AUTH_TOKEN_KEY, token)
}

export function clearAuthToken(): void {
  getAuthStorage().removeItem(AUTH_TOKEN_KEY)
}
