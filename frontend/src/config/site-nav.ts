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
      // 积分模块负责人补充，例如：
      // { label: '积分查询', path: '/credit' },
      // { label: '积分兑换', path: '/credit/exchange' },
    ],
  },
  {
    key: 'resources',
    label: '学习资源',
    children: [
      // 学习资源负责人补充
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

/** 登录后「个人中心」下拉 — 待补充 */
export const profileNav: NavChild[] = [
  // { label: '个人资料', path: '/profile' },
  // { label: '学习档案', path: '/archive' },
  // { label: '退出登录', path: '/logout' },
]
