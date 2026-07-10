export interface RecommendTag {
  label: string
  path: string
}

export interface IdentityRecommend {
  title: string
  icon: string
  color: string
  tags: RecommendTag[]
  morePath: string
}

/** 首页右侧「身份分类推荐」— 组员可在此追加标签 */
export const identityRecommends: IdentityRecommend[] = [
  {
    title: '在校学员',
    icon: '🎓',
    color: '#2094f3',
    tags: [
      { label: '积分查询', path: '/profile/credit' },
      { label: '学习档案', path: '/profile/learning' },
      { label: '成果认证', path: '/achievement' },
      { label: '活动报名', path: '/enterprise' },
    ],
    morePath: '/profile',
  },
  {
    title: '在职学员',
    icon: '💼',
    color: '#722ed1',
    tags: [
      { label: '微专业', path: '/courses' },
      { label: '技能提升', path: '/courses' },
      { label: '证书兑换', path: '/credit' },
      { label: '招聘求职', path: '/enterprise' },
    ],
    morePath: '/enterprise',
  },
  {
    title: '加盟机构',
    icon: '🏫',
    color: '#13c2c2',
    tags: [
      { label: '机构入驻', path: '/register' },
      { label: '成果审核', path: '/achievement' },
      { label: '课程发布', path: '/courses' },
      { label: '数据统计', path: '/profile/enterprise' },
    ],
    morePath: '/register',
  },
  {
    title: '社会公众',
    icon: '🌐',
    color: '#fa8c16',
    tags: [
      { label: '公开课程', path: '/courses' },
      { label: '学习论坛', path: '/forum' },
      { label: '诚信查询', path: '/profile/integrity' },
      { label: '关于平台', path: '/about' },
    ],
    morePath: '/about',
  },
]
