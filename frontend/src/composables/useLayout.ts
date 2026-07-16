import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { profileNavByRole, getSiteNavForRole, getProfileHomePath } from '@/config/site-nav'
import { DEFAULT_SEARCH_CATEGORY } from '@/config/search-categories'
import { useAuthStore } from '@/stores/auth'
import { roleLabel } from '@/types/auth'

export function useLayout() {
  const route = useRoute()
  const router = useRouter()
  const authStore = useAuthStore()
  const searchKeyword = ref('')
  const searchCategory = ref(DEFAULT_SEARCH_CATEGORY)

  const isLoggedIn = computed(() => authStore.isLoggedIn)
  const displayName = computed(() => authStore.displayName)
  const userRoleName = computed(() =>
    authStore.userInfo ? roleLabel(authStore.userInfo.role) : '',
  )
  const profileNav = computed(() => {
    const role = authStore.userInfo?.role
    return role != null ? profileNavByRole[role] ?? [] : []
  })
  const profileHomePath = computed(() => getProfileHomePath(authStore.userInfo?.role))
  const visibleSiteNav = computed(() => getSiteNavForRole(authStore.userInfo?.role))

  function navigate(path: string) {
    const [pathname, search = ''] = path.split('?')
    if (!search) {
      router.push({ path: pathname })
      return
    }
    router.push({
      path: pathname,
      query: Object.fromEntries(new URLSearchParams(search)),
    })
  }

  function isNavActive(item: { path?: string; key: string; children: { path: string }[] }) {
    if (item.path) {
      if (item.path === '/') return route.path === '/'
      return route.path === item.path || route.path.startsWith(`${item.path}/`)
    }
    return item.children.some((c) => {
      const childPath = c.path.split('?')[0]
      return route.path === childPath || route.path.startsWith(`${childPath}/`)
    })
  }

  function handleSearch() {
    if (!searchKeyword.value.trim()) return
    router.push({
      path: '/search',
      query: {
        q: searchKeyword.value.trim(),
        type: searchCategory.value,
      },
    })
  }

  function logout() {
    searchKeyword.value = ''
    searchCategory.value = DEFAULT_SEARCH_CATEGORY
    authStore.logout()
    router.push('/login')
  }

  return {
    siteNav: visibleSiteNav,
    profileNav,
    profileHomePath,
    searchKeyword,
    searchCategory,
    isLoggedIn,
    displayName,
    userRoleName,
    isNavActive,
    handleSearch,
    navigate,
    logout,
  }
}
