import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { siteNav, profileNav } from '@/config/site-nav'

export function useLayout() {
  const route = useRoute()
  const router = useRouter()
  const searchKeyword = ref('')
  /** 登录状态，接入 JWT 后替换为 store 判断 */
  const isLoggedIn = ref(false)

  const activePath = computed(() => route.path)

  function isNavActive(item: { path?: string; key: string; children: { path: string }[] }) {
    if (item.path) {
      return item.path === '/' ? route.path === '/' : route.path.startsWith(item.path)
    }
    return item.children.some((c) => route.path.startsWith(c.path))
  }

  function handleSearch() {
    if (!searchKeyword.value.trim()) return
    router.push({ path: '/search', query: { q: searchKeyword.value.trim() } })
  }

  function navigate(path: string) {
    router.push(path)
  }

  return {
    siteNav,
    profileNav,
    searchKeyword,
    isLoggedIn,
    activePath,
    isNavActive,
    handleSearch,
    navigate,
  }
}
