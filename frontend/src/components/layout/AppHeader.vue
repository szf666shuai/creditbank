<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowDown, ChatDotRound, Close, HomeFilled, Menu, Search } from '@element-plus/icons-vue'
import { useLayout } from '@/composables/useLayout'
import { useHeaderScroll } from '@/composables/useHeaderScroll'
import { useSearchSuggest } from '@/composables/useSearchSuggest'
import { useMessageStore } from '@/stores/message'
import { useAuthStore } from '@/stores/auth'
import { searchCategories, DEFAULT_SEARCH_CATEGORY } from '@/config/search-categories'
import { BRAND_NAME, BRAND_NAME_EN } from '@/config/brand'
import { getRoleTheme, resolveHeaderThemeVariant } from '@/config/role-theme'
import BrandLogo from '@/components/brand/BrandLogo.vue'
import type { SearchItem } from '@/api/search'

const {
  siteNav,
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
} = useLayout()

const route = useRoute()
const messageStore = useMessageStore()
const authStore = useAuthStore()
const unreadCount = computed(() => messageStore.unreadCount)
const hasUnread = computed(() => unreadCount.value > 0)

const isHomePage = computed(() => route.name === 'home')

const headerVariant = computed(() =>
  resolveHeaderThemeVariant({
    isHomePage: isHomePage.value,
    isLoggedIn: authStore.isLoggedIn,
    isStudent: authStore.isStudent,
    isEnterprise: authStore.isEnterprise,
    isAdmin: authStore.isAdmin,
  }),
)

const headerTheme = computed(() => getRoleTheme(headerVariant.value))

const headerStyle = computed(() => ({
  '--header-bg': headerTheme.value.headerBg,
  '--header-border': headerTheme.value.headerBorder,
  '--header-accent': headerTheme.value.primarySoft,
  '--header-accent-strong': headerTheme.value.primary,
  '--header-accent-hover': headerTheme.value.primaryDark,
  '--header-text': headerTheme.value.text,
  '--header-text-muted': headerTheme.value.textMuted,
  '--header-shadow': headerTheme.value.shadow,
}))

const { isHeaderVisible } = useHeaderScroll()
const mobileNavOpen = ref(false)

const mobileNavLinks = computed(() => {
  const links: { label: string; path: string }[] = [{ label: '首页', path: '/' }]
  for (const item of siteNav.value) {
    if (item.icon) continue
    if (item.path) links.push({ label: item.label, path: item.path })
    for (const child of item.children ?? []) {
      if (child.path) links.push({ label: child.label, path: child.path })
    }
  }
  return links
})

function toggleMobileNav() {
  mobileNavOpen.value = !mobileNavOpen.value
}

function closeMobileNav() {
  mobileNavOpen.value = false
}

function goMobile(path: string) {
  closeMobileNav()
  navigate(path)
}

function isMobileLinkActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path === path || route.path.startsWith(`${path}/`)
}

const {
  suggestions,
  showSuggest,
  loadingSuggest,
  activeIndex,
  hideSuggest,
  openSuggestIfAny,
  moveActive,
  pickActive,
} = useSearchSuggest(searchKeyword, searchCategory)

function applySuggestion(item: SearchItem) {
  searchKeyword.value = item.title
  hideSuggest()
  handleSearch()
}

function onSearchKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    moveActive(1)
    return
  }
  if (e.key === 'ArrowUp') {
    e.preventDefault()
    moveActive(-1)
    return
  }
  if (e.key === 'Escape') {
    hideSuggest()
    return
  }
  if (e.key === 'Enter') {
    const picked = pickActive()
    if (picked) {
      e.preventDefault()
      applySuggestion(picked)
      return
    }
    hideSuggest()
    handleSearch()
  }
}

function onSearchBlur() {
  // 延迟关闭，保证点击联想项能触发
  window.setTimeout(() => hideSuggest(), 150)
}

let unreadTimer: number | undefined

function startUnreadPolling() {
  stopUnreadPolling()
  messageStore.refreshUnreadCount()
  unreadTimer = window.setInterval(() => {
    if (authStore.isLoggedIn) {
      messageStore.refreshUnreadCount()
    }
  }, 30000)
}

function stopUnreadPolling() {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = undefined
  }
}

watch(
  () => authStore.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) {
      startUnreadPolling()
    } else {
      stopUnreadPolling()
      messageStore.reset()
      searchKeyword.value = ''
      searchCategory.value = DEFAULT_SEARCH_CATEGORY
    }
  },
)

watch(
  () => route.fullPath,
  () => {
    closeMobileNav()
  },
)

function onGlobalKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') closeMobileNav()
}

onMounted(() => {
  if (isLoggedIn.value) {
    startUnreadPolling()
  }
  window.addEventListener('keydown', onGlobalKeydown)
})

onUnmounted(() => {
  stopUnreadPolling()
  window.removeEventListener('keydown', onGlobalKeydown)
})
</script>

<template>
  <header
    class="app-header is-themed"
    :style="headerStyle"
    :class="{
      'is-hidden': !isHeaderVisible && !mobileNavOpen,
      'is-logged-in': isLoggedIn,
    }"
  >
    <div class="header-inner">
      <!-- Logo -->
      <router-link to="/" class="logo" :aria-label="BRAND_NAME" @click="closeMobileNav">
        <div class="logo-icon">
          <BrandLogo :size="40" :variant="headerVariant" />
        </div>
        <div class="logo-text">
          <span class="logo-title">{{ BRAND_NAME }}</span>
          <span class="logo-sub">{{ BRAND_NAME_EN }}</span>
        </div>
      </router-link>

      <button
        type="button"
        class="mobile-nav-toggle"
        :aria-expanded="mobileNavOpen"
        aria-controls="mobile-nav-panel"
        :aria-label="mobileNavOpen ? '关闭导航' : '打开导航'"
        @click="toggleMobileNav"
      >
        <el-icon :size="20">
          <Close v-if="mobileNavOpen" />
          <Menu v-else />
        </el-icon>
      </button>

      <div class="header-main">
      <!-- 主导航 -->
      <nav class="main-nav">
        <template v-for="item in siteNav" :key="item.key">
          <!-- 首页：图标按钮 -->
          <router-link
            v-if="item.icon"
            to="/"
            class="nav-item nav-home"
            :class="{ active: isNavActive(item) }"
            title="首页"
          >
            <el-icon :size="18"><HomeFilled /></el-icon>
          </router-link>

          <!-- 带下拉的菜单：有 path 时可点击进入总览 -->
          <el-dropdown
            v-else
            trigger="hover"
            placement="bottom-start"
            popper-class="app-header-dropdown"
            :show-timeout="80"
            :hide-timeout="150"
          >
            <button
              type="button"
              class="nav-item nav-dropdown"
              :class="{ active: isNavActive(item), 'is-clickable': !!item.path }"
              @click.stop="item.path && navigate(item.path)"
            >
              {{ item.label }}
              <el-icon class="nav-arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-if="item.children.length">
                  <el-dropdown-item
                    v-for="child in item.children"
                    :key="child.path"
                    @click="navigate(child.path)"
                  >
                    {{ child.label }}
                  </el-dropdown-item>
                </template>
                <el-dropdown-item v-else disabled>
                  待组员补充页面
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </nav>

      <!-- 右侧：搜索 + 登录/个人中心 -->
      <div class="header-actions">
        <div class="search-box" :class="{ 'is-suggest-open': showSuggest }">
          <el-select
            v-model="searchCategory"
            class="search-category"
            size="small"
            :teleported="false"
          >
            <el-option
              v-for="cat in searchCategories"
              :key="cat.value"
              :label="cat.label"
              :value="cat.value"
            />
          </el-select>
          <span class="search-divider" aria-hidden="true" />
          <div class="search-input-wrap">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索关键词"
              autocomplete="off"
              @focus="openSuggestIfAny"
              @blur="onSearchBlur"
              @keydown="onSearchKeydown"
            />
            <div
              v-show="showSuggest"
              class="suggest-panel"
              role="listbox"
              aria-label="搜索联想"
            >
              <div v-if="loadingSuggest" class="suggest-loading">加载中…</div>
              <button
                v-for="(item, index) in suggestions"
                :key="`${item.type}-${item.id}`"
                type="button"
                class="suggest-item"
                :class="{ active: index === activeIndex }"
                role="option"
                @mousedown.prevent="applySuggestion(item)"
              >
                <span class="suggest-title">{{ item.title }}</span>
                <span class="suggest-type">{{ item.typeName }}</span>
              </button>
            </div>
          </div>
          <button class="search-btn" aria-label="搜索" @click="handleSearch">
            <el-icon><Search /></el-icon>
          </button>
        </div>

        <!-- 未登录 -->
        <div v-if="!isLoggedIn" class="auth-btns">
          <router-link to="/login" class="btn-login">登录</router-link>
          <router-link to="/register" class="btn-register">免费开始</router-link>
        </div>

        <!-- 已登录：消息 + 个人中心 -->
        <template v-else>
          <router-link
            to="/profile/messages"
            class="message-entry"
            :class="{ 'has-unread': hasUnread }"
            :title="hasUnread ? `消息中心（${unreadCount} 条未读）` : '消息中心'"
          >
            <el-icon :size="20"><ChatDotRound /></el-icon>
            <span v-if="hasUnread" class="message-dot" aria-hidden="true" />
          </router-link>

          <el-dropdown
            trigger="hover"
            placement="bottom-end"
            popper-class="app-header-dropdown"
          >
            <span class="nav-item profile-trigger">
              <span class="profile-name">{{ displayName }}</span>
              <span class="role-badge">{{ userRoleName }}</span>
              <el-icon class="nav-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="child in profileNav"
                  :key="child.path"
                  @click="navigate(child.path)"
                >
                  {{ child.label }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
      </div>
    </div>

    <div
      v-show="mobileNavOpen"
      class="mobile-nav-scrim"
      aria-hidden="true"
      @click="closeMobileNav"
    />
    <nav
      id="mobile-nav-panel"
      class="mobile-nav-panel"
      :class="{ 'is-open': mobileNavOpen }"
      :aria-hidden="!mobileNavOpen"
    >
      <p class="mobile-nav-label">站点导航</p>
      <button
        v-for="link in mobileNavLinks"
        :key="link.path + link.label"
        type="button"
        class="mobile-nav-link"
        :class="{ active: isMobileLinkActive(link.path) }"
        @click="goMobile(link.path)"
      >
        {{ link.label }}
      </button>
      <div v-if="!isLoggedIn" class="mobile-nav-auth">
        <router-link to="/login" class="nb-btn nb-btn--cream" @click="closeMobileNav">登录</router-link>
        <router-link to="/register" class="nb-btn nb-btn--primary" @click="closeMobileNav">免费开始</router-link>
      </div>
    </nav>
  </header>
</template>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 10px 16px 0;
  transform: translateY(0);
  transition: transform 0.32s ease;
}

.app-header.is-themed {
  background: transparent;
  border: none;
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  box-shadow: none;
}

.app-header.is-hidden {
  transform: translateY(calc(-100% - 12px));
  pointer-events: none;
}

.header-inner {
  display: flex;
  align-items: center;
  max-width: var(--content-max-width);
  height: calc(var(--header-height) - 12px);
  margin: 0 auto;
  padding: 0 18px;
  gap: 14px;
  background: #fff;
  border: 2.5px solid var(--nb-ink);
  border-radius: 999px;
  box-shadow: var(--nb-shadow);
}

.app-header.is-themed .nav-item {
  color: var(--header-text);
  font-weight: 700;
}

.app-header.is-themed .nav-item:hover,
.app-header.is-themed .nav-dropdown:hover {
  color: var(--nb-ink);
  background: var(--nb-yellow);
}

.app-header.is-themed .nav-item.active {
  color: var(--nb-ink);
  background: #bbf7d0;
  font-weight: 800;
  border-radius: 999px;
}

.app-header.is-themed .search-box {
  background: var(--nb-cream);
  border: 2px solid var(--nb-ink);
  box-shadow: none;
}

.app-header.is-themed .search-box input {
  color: var(--header-text);
}

.app-header.is-themed .search-box input::placeholder {
  color: var(--header-text-muted);
}

.app-header.is-themed .search-category :deep(.el-select__selected-item) {
  color: var(--header-text);
}

.app-header.is-themed .search-category :deep(.el-select__caret),
.app-header.is-themed .search-btn {
  color: var(--header-text-muted);
}

.app-header.is-themed .search-btn:hover {
  color: var(--nb-green-deep);
}

.app-header.is-themed .search-divider {
  background: var(--nb-ink);
  opacity: 0.2;
}

.app-header.is-themed .profile-trigger {
  color: var(--nb-ink);
  border: 2px solid var(--nb-ink);
  background: #fff;
  box-shadow: var(--nb-shadow-sm);
}

.app-header.is-themed .profile-trigger:hover {
  background: var(--nb-blue);
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0 0 var(--nb-ink);
}

.app-header.is-themed .role-badge {
  background: #bbf7d0;
  color: var(--nb-ink);
  border: 1.5px solid var(--nb-ink);
}

.app-header.is-themed .message-entry {
  color: var(--nb-ink);
  border: 2px solid var(--nb-ink);
  background: #fff;
  box-shadow: var(--nb-shadow-sm);
}

.app-header.is-themed .message-entry:hover,
.app-header.is-themed .message-entry.has-unread {
  background: var(--nb-pink);
  color: var(--nb-ink);
}

.app-header.is-themed .auth-btns {
  background: transparent;
  gap: 10px;
  padding: 0;
  border: none;
  box-shadow: none;
}

.app-header.is-themed .btn-login {
  color: var(--nb-ink);
  font-weight: 800;
  text-decoration: none;
  padding: 8px 4px;
}

.app-header.is-themed .btn-login:hover {
  color: var(--nb-green-deep);
}

.app-header.is-themed .btn-register {
  color: #fff;
  background: var(--nb-green);
  border: 2.5px solid var(--nb-ink);
  border-radius: 999px;
  padding: 8px 16px;
  font-weight: 800;
  text-decoration: none;
  box-shadow: var(--nb-shadow-sm);
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.app-header.is-themed .btn-register:hover {
  background: var(--nb-green-deep);
  transform: translate(2px, 2px);
  box-shadow: 1px 1px 0 0 var(--nb-ink);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  text-decoration: none;
  padding: 2px;
  border-radius: 12px;
  cursor: pointer;
}

.logo:hover {
  opacity: 0.95;
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border: 2.5px solid var(--nb-ink);
  border-radius: 12px;
  overflow: hidden;
  background: var(--nb-pink);
  box-shadow: 2px 2px 0 0 var(--nb-ink);
}

.logo-icon :deep(svg),
.logo-icon svg {
  width: 100%;
  height: 100%;
  display: block;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
  gap: 1px;
}

.logo-title {
  font-family: var(--font-heading);
  font-size: 1.05rem;
  font-weight: 900;
  color: var(--nb-ink);
  letter-spacing: -0.02em;
}

.logo-sub {
  font-size: 0.65rem;
  color: var(--header-text-muted);
  font-weight: 600;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
  min-width: 0;
  flex: 1;
  justify-content: flex-end;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 2px;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
}

.main-nav::-webkit-scrollbar {
  display: none;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 6px 14px;
  font-size: 14px;
  color: var(--color-text);
  text-decoration: none;
  white-space: nowrap;
  border-radius: 999px;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease;
  outline: none;
  border: none;
  background: transparent;
  font: inherit;
}

.app-header.is-logged-in .main-nav {
  gap: 2px;
}

.app-header.is-logged-in .nav-item {
  padding: 6px 11px;
  font-size: 14px;
}

.app-header.is-logged-in .nav-home {
  padding: 6px 9px;
}

.nav-item:hover,
.nav-dropdown:hover {
  color: var(--color-primary);
}

.nav-item.active {
  color: var(--color-primary);
  font-weight: 600;
}

.nav-home {
  padding: 6px 10px;
}

.nav-arrow {
  font-size: 12px;
  transition: transform 0.2s ease;
}

.nav-dropdown:hover .nav-arrow {
  transform: rotate(180deg);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.app-header.is-logged-in .search-box {
  width: 240px;
}

.app-header.is-logged-in .search-category {
  width: 88px;
}

.search-box {
  display: flex;
  align-items: center;
  background: var(--color-muted);
  border: 1px solid var(--color-border-neutral);
  border-radius: 999px;
  padding: 0 4px 0 2px;
  height: 36px;
  width: 260px;
  flex-shrink: 0;
}

.search-category {
  width: 92px;
  flex-shrink: 0;
}

.search-category :deep(.el-select__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
  padding: 0 6px 0 10px;
  min-height: 28px;
}

.search-category :deep(.el-select__selected-item) {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.search-category :deep(.el-select__caret) {
  font-size: 12px;
  color: var(--color-text-muted);
}

.search-divider {
  width: 1px;
  height: 18px;
  background: var(--color-border-neutral);
  flex-shrink: 0;
}

.search-input-wrap {
  position: relative;
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  align-items: center;
}

.search-box input {
  flex: 1;
  width: 100%;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: var(--color-text);
  min-width: 0;
  height: 100%;
}

.search-box input::placeholder {
  color: var(--color-text-muted);
}

.suggest-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: -96px;
  right: -36px;
  z-index: 1200;
  max-height: 320px;
  overflow-y: auto;
  background: #fff;
  border: 2.5px solid var(--nb-ink);
  border-radius: var(--radius-md);
  box-shadow: var(--nb-shadow);
  padding: 6px 0;
}

.suggest-loading {
  padding: 10px 14px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.suggest-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 14px;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s ease;
}

.suggest-item:hover,
.suggest-item.active {
  background: var(--color-primary-light);
}

.suggest-title {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggest-type {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 2px 8px;
  border-radius: 10px;
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  border-radius: 50%;
  transition: color 0.2s ease;
}

.search-btn:hover {
  color: var(--color-primary);
}

.auth-btns {
  display: flex;
  align-items: center;
  gap: 10px;
  height: auto;
  background: transparent;
}

.btn-login,
.btn-register {
  font-size: 13px;
  text-decoration: none;
  white-space: nowrap;
  cursor: pointer;
}

.btn-login {
  color: var(--nb-ink);
  font-weight: 800;
  padding: 8px 4px;
}

.btn-login:hover {
  color: var(--nb-green-deep);
}

.btn-register {
  color: #fff;
  background: var(--nb-green);
  border: 2.5px solid var(--nb-ink);
  border-radius: 999px;
  padding: 8px 16px;
  font-weight: 800;
  box-shadow: var(--nb-shadow-sm);
  transition: transform 0.12s ease, box-shadow 0.12s ease, background 0.12s ease;
}

.btn-register:hover {
  background: var(--nb-green-deep);
  transform: translate(2px, 2px);
  box-shadow: 1px 1px 0 0 var(--nb-ink);
}

.profile-trigger {
  padding: 6px 14px;
  border: 1px solid var(--color-primary);
  border-radius: 999px;
  color: var(--color-primary);
  font-weight: 500;
  flex-shrink: 0;
  white-space: nowrap;
  cursor: pointer;
}

.profile-name {
  white-space: nowrap;
}

.role-badge {
  font-size: 11px;
  padding: 1px 6px;
  background: var(--color-primary-light);
  border-radius: 10px;
  color: var(--color-primary);
  font-weight: 400;
}

.profile-trigger:hover {
  background: var(--color-primary-light);
}

.message-entry {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: background 0.2s ease, color 0.2s ease;
  overflow: visible;
  flex-shrink: 0;
  cursor: pointer;
}

.message-entry:hover {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.message-entry.has-unread {
  color: var(--color-primary);
}

.message-dot {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-destructive);
  border: 1.5px solid var(--color-card);
  pointer-events: none;
}

@media (max-width: 1280px) {
  .header-main {
    gap: 10px;
  }

  .app-header.is-logged-in .nav-item {
    padding: 6px 9px;
    font-size: 13px;
  }

  .app-header.is-logged-in .search-box {
    width: 210px;
  }
}

@media (max-width: 1100px) {
  .header-main {
    gap: 8px;
  }

  .main-nav {
    display: none;
  }

  .mobile-nav-toggle {
    display: inline-grid;
  }

  .search-box {
    width: 210px;
  }

  .search-category {
    width: 76px;
  }

  .logo-sub {
    display: none;
  }
}

@media (max-width: 720px) {
  .header-inner {
    padding: 0 12px;
    gap: 8px;
  }

  .search-box {
    width: 148px;
  }

  .search-category {
    display: none;
  }

  .search-divider {
    display: none;
  }

  .profile-name {
    display: none;
  }

  .auth-btns .btn-login {
    display: none;
  }
}

.mobile-nav-toggle {
  display: none;
  place-items: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border: 2.5px solid var(--nb-ink);
  border-radius: 12px;
  background: var(--nb-yellow);
  color: var(--nb-ink);
  box-shadow: var(--nb-shadow-sm);
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.mobile-nav-toggle:hover {
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0 0 var(--nb-ink);
}

.mobile-nav-scrim {
  position: fixed;
  inset: 0;
  z-index: 998;
  background: rgba(26, 32, 44, 0.28);
}

.mobile-nav-panel {
  position: fixed;
  top: calc(var(--header-height) + 8px);
  left: 16px;
  right: 16px;
  z-index: 999;
  max-height: min(70vh, 520px);
  overflow-y: auto;
  padding: 16px;
  background: #fff;
  border: 2.5px solid var(--nb-ink);
  border-radius: 20px;
  box-shadow: var(--nb-shadow-lg);
  display: none;
  flex-direction: column;
  gap: 6px;
}

.mobile-nav-panel.is-open {
  display: flex;
}

.mobile-nav-label {
  margin: 0 0 8px;
  font-family: var(--font-heading);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--header-text-muted);
}

.mobile-nav-link {
  display: block;
  width: 100%;
  text-align: left;
  padding: 12px 14px;
  border: 2px solid transparent;
  border-radius: 12px;
  background: transparent;
  font-family: var(--font-heading);
  font-size: 0.95rem;
  font-weight: 800;
  color: var(--nb-ink);
  cursor: pointer;
}

.mobile-nav-link:hover,
.mobile-nav-link.active {
  background: #bbf7d0;
  border-color: var(--nb-ink);
}

.mobile-nav-auth {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 2px solid color-mix(in srgb, var(--nb-ink) 16%, transparent);
}

.mobile-nav-auth .nb-btn {
  min-height: 42px;
  font-size: 0.875rem;
}
</style>

<style>
/* 导航 / 个人中心下拉：Neo-brutal */
.app-header-dropdown.el-popper,
.app-header-dropdown {
  --el-dropdown-menuItem-hover-fill: #bbf7d0;
  --el-dropdown-menuItem-hover-color: var(--nb-ink);
  border: 2.5px solid var(--nb-ink) !important;
  border-radius: 14px !important;
  background: #fff !important;
  box-shadow: var(--nb-shadow) !important;
  overflow: hidden;
}

.app-header-dropdown .el-dropdown-menu {
  background: transparent;
  border: none;
  padding: 6px;
  box-shadow: none;
}

.app-header-dropdown .el-dropdown-menu__item {
  color: var(--color-foreground);
  border-radius: 8px;
  margin: 2px 0;
  line-height: 1.4;
  padding: 10px 14px;
}

.app-header-dropdown .el-dropdown-menu__item:not(.is-disabled):hover,
.app-header-dropdown .el-dropdown-menu__item:focus {
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}

.app-header-dropdown .el-dropdown-menu__item.is-disabled {
  color: var(--color-muted-foreground);
}

.app-header-dropdown .el-dropdown-menu__item--divided {
  border-top-color: var(--color-border-neutral);
}

.app-header-dropdown .el-dropdown-menu__item--divided::before {
  background: transparent;
}

.app-header-dropdown .el-popper__arrow::before {
  background: var(--color-card) !important;
  border: 1px solid var(--color-border-neutral) !important;
}
</style>
