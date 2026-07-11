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

/** 顶部主导航配置（学员 / 访客默认） */
export const siteNav: NavItem[] = [
  {
    key: 'home',
    label: '首页',
    path: '/',
    icon: true,
    children: [],
  },
  {
    key: 'credit',
    label: '积分商城',
    children: [
      { label: '商品兑换', path: '/credit' },
      { label: '订单记录', path: '/credit/orders' },
    ],
  },
  {
    key: 'resources',
    label: '学习资源',
    children: [
      { label: '资源列表', path: '/resources' },
      { label: '学习档案', path: '/archive' },
    ],
  },
  {
    key: 'forum',
    label: '论坛',
    path: '/forum',
    children: [
      { label: '校园频道', path: '/forum?board=campus' },
      { label: '校园集市', path: '/forum?board=market' },
      { label: '求职经验', path: '/forum?board=jobs' },
      { label: '政策讨论', path: '/forum?board=policy' },
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
    children: [
      { label: '加盟企业', path: '/enterprise' },
    ],
  },
]

/** 按角色返回顶部导航 */
export function getSiteNavForRole(role?: number): NavItem[] {
  if (role === 1) {
    return siteNav
      .filter((item) => item.key !== 'resources')
      .map((item) => {
        if (item.key === 'credit') {
          return {
            ...item,
            label: '商城管理',
            children: [{ label: '商品管理', path: '/profile/enterprise/products' }],
          }
        }
        if (item.key !== 'enterprise') return item
        return {
          ...item,
          label: '企业运营',
          children: [
            { label: '企业工作台', path: '/profile/enterprise' },
            { label: '加盟企业', path: '/enterprise' },
          ],
        }
      })
  }

  if (role === 2) {
    return siteNav
      .filter((item) => item.key !== 'resources' && item.key !== 'credit')
      .map((item) => {
        if (item.key !== 'enterprise') return item
        return {
          ...item,
          label: '平台监管',
          children: [
            { label: '管理概览', path: '/profile/admin' },
            { label: '机构加盟', path: '/profile/admin/organizations' },
          ],
        }
      })
  }

  return siteNav
}

/** 登录后「个人中心」下拉 — 按角色扩展 */
export const profileNavByRole: Record<number, NavChild[]> = {
  /** 学员 */
  0: [
    { label: '个人中心', path: '/profile' },
    { label: '我的简历', path: '/profile/resume' },
    { label: '学习档案', path: '/profile/learning' },
    { label: '秩点流水', path: '/profile/credit' },
    { label: '诚信评定', path: '/profile/integrity' },
    { label: '消息中心', path: '/profile/messages' },
  ],
  /** 企业用户 */
  1: [
    { label: '账号概览', path: '/profile' },
    { label: '企业工作台', path: '/profile/enterprise' },
    { label: '商城管理', path: '/profile/enterprise/products' },
    { label: '加盟企业', path: '/enterprise' },
    { label: '消息中心', path: '/profile/messages' },
  ],
  /** 管理员 */
  2: [
    { label: '管理概览', path: '/profile/admin' },
    { label: '商品审核', path: '/profile/admin/products' },
    { label: '机构加盟', path: '/profile/admin/organizations' },
    { label: '用户管理', path: '/profile/admin/users' },
    { label: '消息中心', path: '/profile/messages' },
  ],
}

/** @deprecated 使用 profileNavByRole */
export const profileNav: NavChild[] = []
