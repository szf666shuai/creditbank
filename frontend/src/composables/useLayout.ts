import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export interface NavItem {
  label: string
  path: string
}

export const navItems: NavItem[] = [
  { label: '首页', path: '/' },
  { label: '课程', path: '/courses' },
  { label: '积分商城', path: '/credit' },
  { label: '学习档案', path: '/archive' },
  { label: '学习成果', path: '/achievement' },
  { label: '论坛', path: '/forum' },
  { label: '机构加盟', path: '/organization' },
  { label: '关于我们', path: '/about' },
]

export function useLayout() {
  const route = useRoute()
  const router = useRouter()
  const searchKeyword = ref('')
  const locale = ref<'zh' | 'en'>('zh')

  const activePath = computed(() => route.path)

  function isActive(path: string) {
    if (path === '/') return route.path === '/'
    return route.path.startsWith(path)
  }

  function handleSearch() {
    if (!searchKeyword.value.trim()) return
    router.push({ path: '/search', query: { q: searchKeyword.value.trim() } })
  }

  function toggleLocale() {
    locale.value = locale.value === 'zh' ? 'en' : 'zh'
  }

  return { navItems, activePath, searchKeyword, locale, isActive, handleSearch, toggleLocale }
}
