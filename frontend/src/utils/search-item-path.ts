import type { RouteLocationRaw } from 'vue-router'
import type { SearchItem } from '@/api/search'

/** 搜索/首页条目 → 详情页路由（与首页 HomeContentSections 保持一致） */
export function searchItemPath(item: Pick<SearchItem, 'type' | 'id'>): RouteLocationRaw {
  switch (item.type) {
    case 'credit':
      return `/credit/products/${item.id}`
    case 'course':
      return item.id > 0 ? `/resources/${item.id}` : '/resources'
    case 'enterprise':
    case 'partner':
      return `/enterprise/${item.id}`
    case 'activity':
      return { path: '/news', query: { type: 'activity', id: String(item.id) } }
    case 'job':
      return { path: '/news', query: { type: 'job', id: String(item.id) } }
    case 'news':
      return { path: '/news', query: { type: 'policy', id: String(item.id) } }
    case 'forum':
      return { path: '/forum', query: { id: String(item.id) } }
    case 'resource':
      return '/resources'
    default:
      return { path: '/search' }
  }
}
