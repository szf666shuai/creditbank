/** 导航下拉子项 — 组员在对应模块的 children 中补充 */
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
  /** 下拉子页面，留空数组表示待组员补充 */
  children: NavChild[]
}

/** 顶部主导航配置 */
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
      { label: '商品兑换', path: '/credit#mall-products' },
      { label: '订单记录', path: '/credit#mall-orders' },
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
    children: [
      // 论坛负责人补充
    ],
  },
  {
    key: 'news',
    label: '资讯中心',
    children: [
      // 资讯负责人补充
    ],
  },
  {
    key: 'enterprise',
    label: '企业中心',
    children: [
      // 企业中心负责人补充
    ],
  },
]

/** 登录后「个人中心」下拉 — 按角色扩展 */
export const profileNavByRole: Record<number, NavChild[]> = {
  /** 学员 */
  0: [
    { label: '学习档案', path: '/archive' },
    { label: '积分商城', path: '/credit' },
  ],
  /** 企业用户 */
  1: [
    { label: '企业中心', path: '/enterprise' },
    { label: '活动报名', path: '/activity' },
    { label: '招聘求职', path: '/job' },
  ],
  /** 管理员 */
  2: [
    { label: '机构加盟', path: '/organization' },
  ],
}

/** @deprecated 使用 profileNavByRole，保留空数组兼容旧引用 */
export const profileNav: NavChild[] = []
