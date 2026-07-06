<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import { useLayout } from '@/composables/useLayout'

const { navItems, searchKeyword, locale, isActive, handleSearch, toggleLocale } = useLayout()
</script>

<template>
  <header class="app-header">
    <div class="header-inner">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="4" fill="white" fill-opacity="0.15"/>
            <path d="M10 28V12h4l6 10 6-10h4v16h-3.5V18l-5.5 9h-2l-5.5-9v10H10z" fill="white"/>
          </svg>
        </div>
        <div class="logo-text">
          <span class="logo-title">学分银行</span>
          <span class="logo-sub">creditbank.edu.cn</span>
        </div>
      </router-link>

      <!-- 主导航 -->
      <nav class="main-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <!-- 右侧操作区 -->
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

        <button class="lang-btn" @click="toggleLocale">
          {{ locale === 'zh' ? 'English' : '中文' }}
        </button>

        <div class="auth-btns">
          <router-link to="/login" class="btn-login">登录</router-link>
          <router-link to="/register" class="btn-register">注册</router-link>
        </div>
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
  gap: 24px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  text-decoration: none;
  background: var(--color-primary);
  padding: 8px 14px;
  border-radius: 4px;
  margin-right: 8px;
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

/* 导航 */
.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  overflow: hidden;
}

.nav-item {
  padding: 6px 12px;
  font-size: 14px;
  color: var(--color-text);
  text-decoration: none;
  white-space: nowrap;
  border-radius: 4px;
  transition: color 0.2s;
}

.nav-item:hover {
  color: var(--color-primary);
}

.nav-item.active {
  color: var(--color-primary);
  font-weight: 600;
}

/* 右侧操作 */
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

.lang-btn {
  padding: 5px 12px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-white);
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  white-space: nowrap;
  transition: border-color 0.2s, color 0.2s;
}

.lang-btn:hover {
  border-color: var(--color-primary);
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

@media (max-width: 1100px) {
  .main-nav {
    display: none;
  }

  .search-box {
    width: 120px;
  }
}
</style>
