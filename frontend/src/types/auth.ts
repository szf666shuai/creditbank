export interface UserInfo {
  id: number
  username: string
  realName: string
  phone?: string
  email?: string
  avatar?: string
  role: number
  roleName: string
  orgId?: number
  orgName?: string
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  realName: string
  roleType: number
  phone?: string
  email?: string
  orgName?: string
  orgCode?: string
  orgIntro?: string
  orgContact?: string
  orgPhone?: string
}

/** 0学员 1企业 2管理员 */
export const ROLE_STUDENT = 0
export const ROLE_ENTERPRISE = 1
export const ROLE_ADMIN = 2

export function roleLabel(role: number) {
  switch (role) {
    case ROLE_STUDENT:
      return '学员'
    case ROLE_ENTERPRISE:
      return '企业用户'
    case ROLE_ADMIN:
      return '系统管理员'
    default:
      return '用户'
  }
}
