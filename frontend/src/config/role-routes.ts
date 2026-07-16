import { ROLE_ADMIN, ROLE_ENTERPRISE, ROLE_STUDENT } from '@/types/auth'

/** 登录后各角色默认落地页 */
export function getDefaultHomePath(role: number): string {
  switch (role) {
    case ROLE_ENTERPRISE:
      return '/profile/enterprise'
    case ROLE_ADMIN:
      return '/profile/admin'
    default:
      return '/profile'
  }
}

/** 仅学员可访问的个人中心路径前缀 */
export const STUDENT_PROFILE_PATHS = [
  '/profile/resume',
  '/profile/learning',
  '/profile/credit',
  '/profile/integrity',
  '/profile/posts',
  '/profile/applications',
  '/profile/interviews',
  '/profile/activities',
] as const

/** 仅学员可访问的学习课程相关路径 */
export const STUDENT_LEARNING_PATHS = [
  '/resources',
  '/archive',
] as const

export function isStudentOnlyPath(path: string): boolean {
  return STUDENT_PROFILE_PATHS.some((p) => path === p || path.startsWith(`${p}/`))
}

export function isStudentLearningPath(path: string): boolean {
  return STUDENT_LEARNING_PATHS.some((p) => path === p || path.startsWith(`${p}/`))
}

export function isEnterpriseOnlyPath(path: string): boolean {
  return path === '/profile/enterprise' || path.startsWith('/profile/enterprise/')
}

export function isAdminOnlyPath(path: string): boolean {
  return path === '/profile/admin' || path.startsWith('/profile/admin/')
}

/** 企业加盟列表页（企业用户不可进入「企业中心」） */
export function isEnterprisePortalPath(path: string): boolean {
  return path === '/enterprise'
}

export function canAccessPath(role: number, path: string): boolean {
  if (role === ROLE_ENTERPRISE && isEnterprisePortalPath(path)) {
    return false
  }
  if (isStudentLearningPath(path)) {
    return role === ROLE_STUDENT || role === ROLE_ADMIN
  }
  if (isStudentOnlyPath(path)) return role === ROLE_STUDENT
  if (isEnterpriseOnlyPath(path)) return role === ROLE_ENTERPRISE
  if (isAdminOnlyPath(path)) return role === ROLE_ADMIN
  return true
}
