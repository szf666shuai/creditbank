/** 导航下拉子项 */
export interface NavChild {
  label: string
  path: string
}

export interface NavItem {
  key: string
  label: string
  /** 无下拉时直接跳转（如首页） */
  path?: string
  /** 是否为首页图标按钮 */
  icon?: boolean
  /** 下拉子页面 */
  children: NavChild[]
}

/** 顶部主导航配置（学员 / 访客默认）——不含「我的秩点」 */
export const siteNav: NavItem[] = [
  {
    key: 'home',
    label: '首页',
    path: '/',
    icon: true,
    children: [],
  },
  {
    key: 'resources',
    label: '资源商城',
    path: '/resources',
    children: [],
  },
  {
    key: 'forum',
    label: '论坛',
    path: '/forum',
    children: [
      { label: '校园频道', path: '/forum?board=campus' },
      { label: '校园集市', path: '/forum?board=market' },
      { label: '求职经验', path: '/forum?board=jobs' },
      { label: '政策解读', path: '/forum?board=policy' },
    ],
  },
  {
    key: 'news',
    label: '资讯中心',
    path: '/news',
    children: [
      { label: '招聘信息', path: '/news?type=job' },
      { label: '活动信息', path: '/news?type=activity' },
      { label: '政策资讯', path: '/news?type=policy' },
    ],
  },
  {
    key: 'enterprise',
    label: '企业中心',
    path: '/enterprise',
    children: [],
  },
]

/** 按角色返回顶部导航 */
export function getSiteNavForRole(role?: number): NavItem[] {
  if (role === 1) {
    return [
      { key: 'home', label: '首页', path: '/', icon: true, children: [] },
      {
        key: 'courses',
        label: '课程概览',
        path: '/courses-overview',
        children: [],
      },
      {
        key: 'enterprise',
        label: '企业运营',
        children: [
          { label: '企业工作台', path: '/profile/enterprise' },
          { label: '学分转换规则', path: '/profile/enterprise/transfer-rules' },
          { label: '加盟企业', path: '/enterprise' },
        ],
      },
      {
        key: 'news',
        label: '资讯中心',
        path: '/news',
        children: [
          { label: '招聘信息', path: '/news?type=job' },
          { label: '活动信息', path: '/news?type=activity' },
        ],
      },
    ]
  }

  if (role === 2) {
    return [
      { key: 'home', label: '首页', path: '/', icon: true, children: [] },
      {
        key: 'admin',
        label: '平台监管',
        path: '/profile/admin',
        children: [
          { label: '管理概览', path: '/profile/admin' },
          { label: '课程审核', path: '/profile/admin/courses' },
          { label: '机构加盟', path: '/profile/admin/organizations' },
        ],
      },
      {
        key: 'enterprise',
        label: '企业中心',
        path: '/enterprise',
        children: [],
      },
      {
        key: 'news',
        label: '资讯中心',
        path: '/news',
        children: [
          { label: '招聘信息', path: '/news?type=job' },
          { label: '活动信息', path: '/news?type=activity' },
          { label: '政策资讯', path: '/news?type=policy' },
        ],
      },
    ]
  }

  return siteNav
}

/** 登录后「个人中心」下拉 — 仅常用入口 */
export const profileNavByRole: Record<number, NavChild[]> = {
  /** 学员 */
  0: [
    { label: '个人概览', path: '/profile' },
    { label: '学习档案', path: '/profile/learning' },
    { label: '我的秩点', path: '/profile/credit' },
    { label: '消息中心', path: '/profile/messages' },
  ],
  /** 企业用户 */
  1: [
    { label: '账号概览', path: '/profile' },
    { label: '企业工作台', path: '/profile/enterprise' },
    { label: '课程概览', path: '/courses-overview' },
    { label: '转换规则', path: '/profile/enterprise/transfer-rules' },
    { label: '消息中心', path: '/profile/messages' },
  ],
  /** 管理员 */
  2: [
    { label: '管理概览', path: '/profile/admin' },
    { label: '课程审核', path: '/profile/admin/courses' },
    { label: '机构加盟', path: '/profile/admin/organizations' },
    { label: '消息中心', path: '/profile/messages' },
  ],
}

/** 个人中心首页路径（点击头像名称直接进入） */
export function getProfileHomePath(role?: number): string {
  if (role === 1) return '/profile/enterprise'
  if (role === 2) return '/profile/admin'
  return '/profile'
}

/** @deprecated 使用 profileNavByRole */
export const profileNav: NavChild[] = []
