/** 个人中心左侧菜单项 */
export interface ProfileMenuItem {
  key: string
  label: string
  path: string
  icon: string
  /** 可见角色：0学员 1企业 2管理员 */
  roles: number[]
  /** 在此项前显示分隔线 */
  dividerBefore?: boolean
}

/** 学员菜单 */
export const studentProfileMenu: ProfileMenuItem[] = [
  { key: 'dashboard', label: '个人概览', path: '/profile', icon: '🏠', roles: [0] },
  { key: 'resume', label: '我的简历', path: '/profile/resume', icon: '📄', roles: [0] },
  { key: 'learning', label: '学习档案', path: '/profile/learning', icon: '📁', roles: [0] },
  { key: 'credit', label: '学分流水', path: '/profile/credit', icon: '💰', roles: [0] },
  { key: 'integrity', label: '诚信评定', path: '/profile/integrity', icon: '⭐', roles: [0] },
  { key: 'posts', label: '我的发言', path: '/profile/posts', icon: '📝', roles: [0] },
  { key: 'applications', label: '投递记录', path: '/profile/applications', icon: '📋', roles: [0] },
  { key: 'interviews', label: '面试邀请', path: '/profile/interviews', icon: '📅', roles: [0] },
  { key: 'activities', label: '我的活动', path: '/profile/activities', icon: '🎪', roles: [0] },
  { key: 'messages', label: '消息中心', path: '/profile/messages', icon: '💬', roles: [0], dividerBefore: true },
]

/** 企业用户菜单 */
export const enterpriseProfileMenu: ProfileMenuItem[] = [
  { key: 'dashboard', label: '账号概览', path: '/profile', icon: '🏠', roles: [1] },
  { key: 'enterprise', label: '企业工作台', path: '/profile/enterprise', icon: '🏢', roles: [1], dividerBefore: true },
  { key: 'org', label: '机构信息', path: '/profile/enterprise/org', icon: '🏛️', roles: [1] },
  { key: 'jobs', label: '招聘管理', path: '/profile/enterprise/jobs', icon: '💼', roles: [1] },
  { key: 'activities', label: '活动管理', path: '/profile/enterprise/activities', icon: '🎪', roles: [1] },
  { key: 'applications', label: '投递管理', path: '/profile/enterprise/applications', icon: '📥', roles: [1] },
  { key: 'interviews', label: '面试管理', path: '/profile/enterprise/interviews', icon: '🤝', roles: [1] },
  { key: 'activity-invitations', label: '定向邀请', path: '/profile/enterprise/activity-invitations', icon: '🎟️', roles: [1] },
  { key: 'materials', label: '企业资料', path: '/profile/enterprise/materials', icon: '📁', roles: [1] },
  { key: 'messages', label: '消息中心', path: '/profile/messages', icon: '💬', roles: [1], dividerBefore: true },
]

/** 管理员菜单 */
export const adminProfileMenu: ProfileMenuItem[] = [
  { key: 'admin', label: '管理概览', path: '/profile/admin', icon: '🛡️', roles: [2] },
  { key: 'organizations', label: '机构加盟', path: '/profile/admin/organizations', icon: '🏛️', roles: [2] },
  { key: 'users', label: '用户管理', path: '/profile/admin/users', icon: '👥', roles: [2] },
  { key: 'notifications', label: '系统通知', path: '/profile/admin/notifications', icon: '📢', roles: [2] },
  { key: 'reports', label: '举报处理', path: '/profile/admin/reports', icon: '🚩', roles: [2] },
  { key: 'integrity', label: '诚信监察', path: '/profile/admin/integrity', icon: '⭐', roles: [2] },
  { key: 'credit', label: '学分监察', path: '/profile/admin/credit', icon: '💰', roles: [2] },
  { key: 'jobs', label: '招聘监管', path: '/profile/admin/jobs', icon: '💼', roles: [2] },
  { key: 'activities', label: '活动监管', path: '/profile/admin/activities', icon: '🎪', roles: [2] },
  { key: 'messages', label: '消息中心', path: '/profile/messages', icon: '💬', roles: [2], dividerBefore: true },
]

export function getProfileMenuByRole(role: number): ProfileMenuItem[] {
  switch (role) {
    case 1:
      return enterpriseProfileMenu
    case 2:
      return adminProfileMenu
    default:
      return studentProfileMenu
  }
}

/** 判断路径是否匹配菜单项（含子路径高亮） */
export function isMenuActive(menuPath: string, currentPath: string): boolean {
  if (menuPath === '/profile') {
    return currentPath === '/profile'
  }
  if (menuPath === '/profile/admin') {
    return currentPath === '/profile/admin'
  }
  return currentPath === menuPath || currentPath.startsWith(`${menuPath}/`)
}
