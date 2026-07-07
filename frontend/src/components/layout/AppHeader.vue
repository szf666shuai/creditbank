<script setup lang="ts">
import { ArrowDown, HomeFilled, Search } from '@element-plus/icons-vue'
import { useLayout } from '@/composables/useLayout'

const {
  siteNav,
  profileNav,
  searchKeyword,
  isLoggedIn,
  displayName,
  userRoleName,
  isNavActive,
  handleSearch,
  navigate,
  logout,
} = useLayout()
</script>

<template>
  <header class="app-header">
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
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" aria-label="搜索" @click="handleSearch">
            <el-icon><Search /></el-icon>
          </button>
        </div>

        <!-- 未登录 -->
        <div v-if="!isLoggedIn" class="auth-btns">
          <router-link to="/login" class="btn-login">登录</router-link>
          <router-link to="/register" class="btn-register">注册</router-link>
        </div>

        <!-- 已登录：个人中心下拉 -->
        <el-dropdown
          v-else
          trigger="hover"
          placement="bottom-end"
        >
          <span class="nav-item profile-trigger">
            {{ displayName }}
            <span class="role-badge">{{ userRoleName }}</span>
            <el-icon class="nav-arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="navigate('/profile')">
                个人中心
              </el-dropdown-item>
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
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: var(--color-white);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-inner {
  display: flex;
  align-items: center;
  max-width: var(--content-max-width);
  height: var(--header-height);
  margin: 0 auto;
  padding: 0 16px;
  gap: 20px;
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

.main-nav {
  display: flex;
  align-items: center;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
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
  gap: 12px;
  flex-shrink: 0;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f0f2f5;
  border-radius: 20px;
  padding: 0 4px 0 14px;
  height: 34px;
  width: 160px;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: var(--color-text);
  min-width: 0;
}

.search-box input::placeholder {
  color: var(--color-text-muted);
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
  padding: 6px 16px;
  border: 1px solid var(--color-primary);
  border-radius: 20px;
  color: var(--color-primary);
  font-weight: 500;
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

@media (max-width: 1100px) {
  .main-nav {
    display: none;
  }

  .search-box {
    width: 120px;
  }
}
</style>
