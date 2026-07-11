<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ArrowDown, ChatDotRound, HomeFilled, Search } from '@element-plus/icons-vue'
import { useLayout } from '@/composables/useLayout'
import { useHeaderScroll } from '@/composables/useHeaderScroll'
import { useSearchSuggest } from '@/composables/useSearchSuggest'
import { useMessageStore } from '@/stores/message'
import { searchCategories } from '@/config/search-categories'
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

const messageStore = useMessageStore()
const unreadCount = computed(() => messageStore.unreadCount)

const { isHeaderVisible, isTransparent } = useHeaderScroll()

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

onMounted(() => {
  if (isLoggedIn.value) {
    messageStore.refreshUnreadCount()
  }
})
</script>

<template>
  <header
    class="app-header"
    :class="{
      'is-hidden': !isHeaderVisible,
      'is-transparent': isTransparent,
      'is-logged-in': isLoggedIn,
    }"
  >
    <div class="header-inner">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="4" fill="white" fill-opacity="0.15" />
            <path
              d="M10 28V12h4l6 10 6-10h4v16h-3.5V18l-5.5 9h-2l-5.5-9v10H10z"
              fill="white"
            />
          </svg>
        </div>
        <div class="logo-text">
          <span class="logo-title">学分银行</span>
          <span class="logo-sub">creditbank.edu.cn</span>
        </div>
      </router-link>

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

          <!-- 带下拉的菜单 -->
          <el-dropdown
            v-else
            trigger="hover"
            placement="bottom-start"
            :show-timeout="80"
            :hide-timeout="150"
          >
            <span
              class="nav-item nav-dropdown"
              :class="{ active: isNavActive(item) }"
            >
              {{ item.label }}
              <el-icon class="nav-arrow"><ArrowDown /></el-icon>
            </span>
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
          <router-link to="/register" class="btn-register">注册</router-link>
        </div>

        <!-- 已登录：消息 + 个人中心 -->
        <template v-else>
          <router-link to="/profile/messages" class="message-entry" title="消息中心">
            <el-badge :value="unreadCount" :hidden="unreadCount <= 0" :max="99">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </el-badge>
          </router-link>

          <el-dropdown
            trigger="hover"
            placement="bottom-end"
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
  </header>
</template>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: var(--color-white);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  transform: translateY(0);
  transition:
    transform 0.32s ease,
    background 0.3s ease,
    box-shadow 0.3s ease;
}

.app-header.is-hidden {
  transform: translateY(-100%);
  pointer-events: none;
}

.app-header.is-transparent {
  background: transparent;
  box-shadow: none;
}

.app-header.is-transparent .nav-item {
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.25);
}

.app-header.is-transparent .nav-item:hover,
.app-header.is-transparent .nav-dropdown:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
}

.app-header.is-transparent .nav-item.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.22);
  border-radius: 20px;
}

.app-header.is-transparent .search-box {
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(6px);
}

.app-header.is-transparent .search-box input {
  color: #fff;
}

.app-header.is-transparent .search-box input::placeholder {
  color: rgba(255, 255, 255, 0.72);
}

.app-header.is-transparent .search-category :deep(.el-select__selected-item) {
  color: rgba(255, 255, 255, 0.92);
}

.app-header.is-transparent .search-category :deep(.el-select__caret),
.app-header.is-transparent .search-btn {
  color: rgba(255, 255, 255, 0.85);
}

.app-header.is-transparent .search-divider {
  background: rgba(255, 255, 255, 0.4);
}

.app-header.is-transparent .profile-trigger {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.55);
  background: rgba(255, 255, 255, 0.12);
}

.app-header.is-transparent .profile-trigger:hover {
  background: rgba(255, 255, 255, 0.22);
}

.app-header.is-transparent .role-badge {
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.header-inner {
  display: flex;
  align-items: center;
  max-width: var(--content-max-width);
  height: var(--header-height);
  margin: 0 auto;
  padding: 0 16px;
  gap: 16px;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: auto;
  min-width: 0;
  flex: 1;
  justify-content: flex-end;
}

.app-header.is-logged-in .header-main {
  gap: 12px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  text-decoration: none;
  background: var(--color-primary);
  padding: 8px 14px;
  border-radius: 4px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.logo-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-white);
  letter-spacing: 1px;
}

.logo-sub {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.75);
}

.app-header.is-logged-in .logo {
  padding: 7px 12px;
}

.app-header.is-logged-in .logo-sub {
  display: block;
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
  border-radius: 4px;
  cursor: pointer;
  transition: color 0.2s, background 0.2s;
  outline: none;
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
  transition: transform 0.2s;
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
  background: #f0f2f5;
  border-radius: 20px;
  padding: 0 4px 0 2px;
  height: 34px;
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
  background: #dcdfe6;
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
  border-radius: 10px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.14);
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
  transition: background 0.15s;
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
  background: rgba(32, 148, 243, 0.1);
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
  transition: color 0.2s;
}

.search-btn:hover {
  color: var(--color-primary);
}

.auth-btns {
  display: flex;
  align-items: center;
  background: var(--color-primary);
  border-radius: 20px;
  overflow: hidden;
  height: 34px;
}

.btn-login,
.btn-register {
  padding: 0 16px;
  font-size: 13px;
  text-decoration: none;
  line-height: 34px;
  white-space: nowrap;
  transition: background 0.2s;
}

.btn-login {
  color: var(--color-white);
}

.btn-login:hover {
  background: var(--color-primary-dark);
}

.btn-register {
  color: var(--color-primary);
  background: var(--color-white);
  border-radius: 0 20px 20px 0;
}

.btn-register:hover {
  background: var(--color-primary-light);
}

.profile-trigger {
  padding: 6px 14px;
  border: 1px solid var(--color-primary);
  border-radius: 20px;
  color: var(--color-primary);
  font-weight: 500;
  flex-shrink: 0;
  white-space: nowrap;
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
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
}

.message-entry:hover {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.app-header.is-transparent .message-entry {
  color: rgba(255, 255, 255, 0.9);
}

.app-header.is-transparent .message-entry:hover {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
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

  .search-box {
    width: 210px;
  }

  .search-category {
    width: 76px;
  }
}
</style>
