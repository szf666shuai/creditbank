/** 个人中心左侧菜单项 */
export interface ProfileMenuItem {
  key: string
  label: string
  path: string
  /** UiIcon 语义名，见 @/config/ui-icons */
  icon: string
  /** 可见角色：0学员 1企业 2管理员 */
  roles: number[]
  /** 在此项前显示分隔线 */
  dividerBefore?: boolean
  /** 分组子菜单（侧栏可展开） */
  children?: ProfileMenuItem[]
}

/** 学员菜单 */
export const studentProfileMenu: ProfileMenuItem[] = [
  { key: 'dashboard', label: '个人概览', path: '/profile', icon: 'home', roles: [0] },
  {
    key: 'learning-group',
    label: '学习档案',
    path: '/profile/learning',
    icon: 'learning',
    roles: [0],
    children: [
      { key: 'learning', label: '档案与成果', path: '/profile/learning', icon: 'learning', roles: [0] },
      { key: 'resume', label: '我的简历', path: '/profile/resume', icon: 'resume', roles: [0] },
      { key: 'learning-profile', label: '学习画像', path: '/profile/learning-profile', icon: 'user', roles: [0] },
    ],
  },
  {
    key: 'credit-group',
    label: '我的秩点',
    path: '/profile/credit',
    icon: 'credit',
    roles: [0],
    children: [
      { key: 'credit', label: '秩点总览', path: '/profile/credit', icon: 'credit', roles: [0] },
      { key: 'credit-transfer', label: '学分转换', path: '/profile/credit-transfer', icon: 'exchange', roles: [0] },
    ],
  },
  { key: 'integrity', label: '诚信评定', path: '/profile/integrity', icon: 'integrity', roles: [0] },
  { key: 'applications', label: '投递记录', path: '/profile/applications', icon: 'applications', roles: [0] },
  { key: 'interviews', label: '面试邀请', path: '/profile/interviews', icon: 'interview', roles: [0] },
  { key: 'activities', label: '我的活动', path: '/profile/activities', icon: 'activity', roles: [0] },
  { key: 'messages', label: '消息中心', path: '/profile/messages', icon: 'message', roles: [0], dividerBefore: true },
]

/** 企业用户菜单 */
export const enterpriseProfileMenu: ProfileMenuItem[] = [
  { key: 'dashboard', label: '账号概览', path: '/profile', icon: 'home', roles: [1] },
  {
    key: 'ops-group',
    label: '企业运营',
    path: '/profile/enterprise',
    icon: 'enterprise',
    roles: [1],
    dividerBefore: true,
    children: [
      { key: 'enterprise', label: '企业工作台', path: '/profile/enterprise', icon: 'enterprise', roles: [1] },
      { key: 'org', label: '机构信息', path: '/profile/enterprise/org', icon: 'school', roles: [1] },
      { key: 'courses', label: '课程管理', path: '/profile/enterprise/courses', icon: 'course', roles: [1] },
      { key: 'transfer-rules', label: '转换规则', path: '/profile/enterprise/transfer-rules', icon: 'exchange', roles: [1] },
      { key: 'transfer-applications', label: '转换申请', path: '/profile/enterprise/transfer-applications', icon: 'file-application', roles: [1] },
    ],
  },
  {
    key: 'hr-group',
    label: '招聘活动',
    path: '/profile/enterprise/jobs',
    icon: 'job',
    roles: [1],
    children: [
      { key: 'jobs', label: '招聘管理', path: '/profile/enterprise/jobs', icon: 'job', roles: [1] },
      { key: 'activities', label: '活动管理', path: '/profile/enterprise/activities', icon: 'activity', roles: [1] },
      { key: 'applications', label: '投递管理', path: '/profile/enterprise/applications', icon: 'applications', roles: [1] },
      { key: 'interviews', label: '面试管理', path: '/profile/enterprise/interviews', icon: 'interview', roles: [1] },
      { key: 'activity-invitations', label: '定向邀请', path: '/profile/enterprise/activity-invitations', icon: 'invite', roles: [1] },
    ],
  },
  { key: 'materials', label: '企业资料', path: '/profile/enterprise/materials', icon: 'material', roles: [1] },
  { key: 'messages', label: '消息中心', path: '/profile/messages', icon: 'message', roles: [1], dividerBefore: true },
]

/** 管理员菜单 */
export const adminProfileMenu: ProfileMenuItem[] = [
  { key: 'admin', label: '管理概览', path: '/profile/admin', icon: 'admin', roles: [2] },
  {
    key: 'org-user-group',
    label: '组织用户',
    path: '/profile/admin/organizations',
    icon: 'school',
    roles: [2],
    children: [
      { key: 'organizations', label: '机构加盟', path: '/profile/admin/organizations', icon: 'school', roles: [2] },
      { key: 'users', label: '用户管理', path: '/profile/admin/users', icon: 'users', roles: [2] },
    ],
  },
  {
    key: 'review-group',
    label: '内容审核',
    path: '/profile/admin/courses',
    icon: 'course',
    roles: [2],
    children: [
      { key: 'courses', label: '课程审核', path: '/profile/admin/courses', icon: 'course', roles: [2] },
      { key: 'jobs', label: '招聘监管', path: '/profile/admin/jobs', icon: 'job', roles: [2] },
      { key: 'activities', label: '活动监管', path: '/profile/admin/activities', icon: 'activity', roles: [2] },
      { key: 'reports', label: '举报处理', path: '/profile/admin/reports', icon: 'report', roles: [2] },
    ],
  },
  {
    key: 'oversight-group',
    label: '平台监察',
    path: '/profile/admin/integrity',
    icon: 'integrity',
    roles: [2],
    children: [
      { key: 'integrity', label: '诚信监察', path: '/profile/admin/integrity', icon: 'integrity', roles: [2] },
      { key: 'credit', label: '秩点监察', path: '/profile/admin/credit', icon: 'credit', roles: [2] },
      { key: 'notifications', label: '系统通知', path: '/profile/admin/notifications', icon: 'notify', roles: [2] },
    ],
  },
  { key: 'messages', label: '消息中心', path: '/profile/messages', icon: 'message', roles: [2], dividerBefore: true },
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
  if (menuPath === '/profile/enterprise') {
    return currentPath === '/profile/enterprise'
  }
  return currentPath === menuPath || currentPath.startsWith(`${menuPath}/`)
}

export function isGroupActive(item: ProfileMenuItem, currentPath: string): boolean {
  if (item.children?.length) {
    return item.children.some((child) => isMenuActive(child.path, currentPath))
  }
  return isMenuActive(item.path, currentPath)
}
