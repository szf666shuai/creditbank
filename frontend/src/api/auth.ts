import request from '@/utils/request'
import type { LoginForm, LoginResponse, RegisterForm, UserInfo } from '@/types/auth'

export function loginApi(data: LoginForm) {
  return request.post<LoginResponse>('/auth/login', data)
}

export function registerApi(data: RegisterForm) {
  return request.post<LoginResponse>('/auth/register', data)
}

export function getMeApi() {
  return request.get<UserInfo>('/auth/me')
}
