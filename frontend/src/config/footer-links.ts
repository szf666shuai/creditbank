import { ROLE_ADMIN, ROLE_ENTERPRISE, ROLE_STUDENT } from '@/types/auth'

export interface FooterLink {
  label: string
  path: string
}

/** 按角色返回页脚快捷入口（仅保留各角色真正会用到的页面） */
export function getFooterLinksForRole(options: {
  isLoggedIn: boolean
  role?: number
}): FooterLink[] {
  if (!options.isLoggedIn) {
    return [
      { label: '课程资源', path: '/resources' },
      { label: '秩点商城', path: '/credit' },
      { label: '企业机构', path: '/enterprise' },
      { label: '机构入驻', path: '/register' },
    ]
  }

  const role = options.role ?? ROLE_STUDENT

  if (role === ROLE_ENTERPRISE) {
    return [
      { label: '企业工作台', path: '/profile/enterprise' },
      { label: '商品管理', path: '/profile/enterprise/products' },
      { label: '招聘管理', path: '/profile/enterprise/jobs' },
      { label: '加盟企业', path: '/enterprise' },
    ]
  }

  if (role === ROLE_ADMIN) {
    return [
      { label: '管理概览', path: '/profile/admin' },
      { label: '机构审核', path: '/profile/admin/organizations' },
      { label: '商品审核', path: '/profile/admin/products' },
      { label: '用户管理', path: '/profile/admin/users' },
    ]
  }

  return [
    { label: '课程资源', path: '/resources' },
    { label: '学习档案', path: '/archive' },
    { label: '秩点商城', path: '/credit' },
    { label: '个人中心', path: '/profile' },
  ]
}

export function getFooterNavLabel(role?: number, isLoggedIn = false): string {
  if (!isLoggedIn) return '访客快捷入口'
  if (role === ROLE_ENTERPRISE) return '企业快捷入口'
  if (role === ROLE_ADMIN) return '管理快捷入口'
  return '学员快捷入口'
}
