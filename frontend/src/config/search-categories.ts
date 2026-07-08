export interface SearchCategory {
  value: string
  label: string
}

/** 顶部搜索分类 — 顺序与「全部」结果页分区一致 */
export const searchCategories: SearchCategory[] = [
  { value: 'all', label: '全部' },
  { value: 'resource', label: '学习资源' },
  { value: 'course', label: '课程' },
  { value: 'credit', label: '积分商品' },
  { value: 'news', label: '资讯' },
  { value: 'activity', label: '活动' },
  { value: 'job', label: '招聘' },
  { value: 'forum', label: '论坛' },
  { value: 'enterprise', label: '企业' },
]

/** 搜索结果页 — 封面网格展示的分类 */
export const gridSearchTypes: SearchCategory[] = [
  { value: 'resource', label: '学习资源' },
  { value: 'course', label: '课程' },
  { value: 'credit', label: '积分商品' },
]

/** @deprecated 使用 gridSearchTypes */
export const featuredSearchTypes = gridSearchTypes

/** 全部搜索时各分类展示配置 */
export const allSearchSections = {
  grid: gridSearchTypes,
  dual: [
    { value: 'news', label: '资讯', icon: '📰', color: '#2094f3' },
    { value: 'activity', label: '活动', icon: '📅', color: '#13c2c2' },
  ] as const,
  stack: [
    { value: 'job', label: '招聘', icon: '💼', color: '#eb2f96' },
    { value: 'forum', label: '论坛', icon: '💬', color: '#722ed1' },
  ] as const,
  enterprise: { value: 'enterprise', label: '企业', icon: '🏢', color: '#52c41a' },
}

export const ALL_SEARCH_TYPE_VALUES = [
  'resource', 'course', 'credit', 'news', 'activity', 'job', 'forum', 'enterprise',
] as const

export const DEFAULT_SEARCH_CATEGORY = 'all'

export function searchCategoryLabel(value: string) {
  return searchCategories.find((c) => c.value === value)?.label ?? '全部'
}
