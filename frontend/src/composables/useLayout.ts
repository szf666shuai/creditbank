import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { profileNavByRole, getSiteNavForRole } from '@/config/site-nav'
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
  const visibleSiteNav = computed(() => getSiteNavForRole(authStore.userInfo?.role))

  function isNavActive(item: { path?: string; key: string; children: { path: string }[] }) {
    if (item.path) {
      return item.path === '/' ? route.path === '/' : route.path.startsWith(item.path)
    }
    return item.children.some((c) => route.path.startsWith(c.path))
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

  function navigate(path: string) {
    router.push(path)
  }

  function logout() {
    authStore.logout()
    router.push('/login')
  }

  return {
    siteNav: visibleSiteNav,
    profileNav,
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
