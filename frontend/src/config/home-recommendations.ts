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
      { label: '积分查询', path: '/credit' },
      { label: '学习档案', path: '/profile/learning' },
      { label: '成果认证', path: '/profile/learning' },
      { label: '活动报名', path: '/news?type=activity' },
    ],
    morePath: '/profile/learning',
  },
  {
    title: '在职学员',
    icon: '💼',
    color: '#722ed1',
    tags: [
      { label: '微专业', path: '/resources' },
      { label: '技能提升', path: '/resources' },
      { label: '证书兑换', path: '/credit' },
      { label: '招聘求职', path: '/news?type=job' },
    ],
    morePath: '/news?type=job',
  },
  {
    title: '加盟机构',
    icon: '🏫',
    color: '#13c2c2',
    tags: [
      { label: '机构入驻', path: '/register' },
      { label: '加盟企业', path: '/enterprise' },
      { label: '课程发布', path: '/resources' },
      { label: '企业工作台', path: '/profile/enterprise' },
    ],
    morePath: '/enterprise',
  },
  {
    title: '社会公众',
    icon: '🌐',
    color: '#fa8c16',
    tags: [
      { label: '公开课程', path: '/resources' },
      { label: '学习论坛', path: '/forum' },
      { label: '诚信查询', path: '/profile/integrity' },
      { label: '关于平台', path: '/about' },
    ],
    morePath: '/about',
  },
]
