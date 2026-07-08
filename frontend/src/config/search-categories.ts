export interface SearchCategory {
  value: string
  label: string
}

/** 顶部搜索分类 — 可按模块扩展 */
export const searchCategories: SearchCategory[] = [
  { value: 'all', label: '全部' },
  { value: 'course', label: '课程' },
  { value: 'resource', label: '学习资源' },
  { value: 'forum', label: '论坛' },
  { value: 'news', label: '资讯' },
  { value: 'activity', label: '活动' },
  { value: 'job', label: '招聘' },
  { value: 'enterprise', label: '企业' },
  { value: 'credit', label: '积分商品' },
]

export const DEFAULT_SEARCH_CATEGORY = 'all'

export function searchCategoryLabel(value: string) {
  return searchCategories.find((c) => c.value === value)?.label ?? '全部'
}
