<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { getProfileMenuByRole, isMenuActive } from '@/config/profile-menu'
import { ROLE_STUDENT, roleLabel } from '@/types/auth'
import UiIcon from '@/components/ui/UiIcon.vue'

const route = useRoute()
const authStore = useAuthStore()
const messageStore = useMessageStore()

const menuItems = computed(() => {
  const role = authStore.userInfo?.role ?? ROLE_STUDENT
  return getProfileMenuByRole(role)
})

const displayName = computed(
  () => authStore.userInfo?.realName || authStore.userInfo?.username || '用户',
)

const roleName = computed(() => {
  const role = authStore.userInfo?.role ?? ROLE_STUDENT
  return roleLabel(role)
})

function menuActive(path: string) {
  return isMenuActive(path, route.path)
}

onMounted(() => {
  messageStore.refreshUnreadCount()
})
</script>

<template>
  <div class="profile-layout">
    <div class="profile-container">
      <aside class="profile-sidebar">
        <div class="sidebar-user">
          <div class="sidebar-avatar">{{ displayName.charAt(0) }}</div>
          <div class="sidebar-meta">
            <div class="sidebar-name">{{ displayName }}</div>
            <el-tag size="small" type="info">{{ roleName }}</el-tag>
          </div>
        </div>

        <nav class="sidebar-nav">
          <template v-for="item in menuItems" :key="item.key">
            <div v-if="item.dividerBefore" class="nav-divider" />
            <router-link
              :to="item.path"
              class="nav-item"
              :class="{ active: menuActive(item.path) }"
            >
              <UiIcon class="nav-icon" :name="item.icon" :size="18" />
              <span class="nav-label">{{ item.label }}</span>
              <el-badge
                v-if="item.key === 'messages' && messageStore.unreadCount > 0"
                :value="messageStore.unreadCount"
                :max="99"
                class="nav-badge"
              />
            </router-link>
          </template>
        </nav>
      </aside>

      <main class="profile-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.profile-layout {
  padding: 16px 16px 40px;
  min-height: calc(100vh - var(--header-height) - 120px);
  background: transparent;
}

.profile-container {
  max-width: min(1280px, 100%);
  margin: 0 auto;
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.profile-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: rgba(8, 20, 40, 0.42);
  border: 1px solid rgba(125, 211, 252, 0.18);
  border-radius: 12px;
  padding: 20px 12px;
  position: sticky;
  top: calc(var(--header-height) + 16px);
  backdrop-filter: blur(14px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.18);
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px 16px;
  margin-bottom: 8px;
  border-bottom: 1px solid rgba(125, 211, 252, 0.14);
}

.sidebar-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(56, 189, 248, 0.2);
  color: #7dd3fc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
  border: 1px solid rgba(125, 211, 252, 0.28);
}

.sidebar-meta {
  min-width: 0;
  flex: 1;
}

.sidebar-name {
  font-size: 14px;
  font-weight: 600;
  color: #e0f2fe;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-divider {
  height: 1px;
  background: rgba(125, 211, 252, 0.14);
  margin: 8px 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  text-decoration: none;
  color: rgba(186, 230, 253, 0.78);
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}

.nav-item:hover {
  background: rgba(56, 189, 248, 0.14);
  color: #7dd3fc;
}

.nav-item.active {
  background: rgba(56, 189, 248, 0.2);
  color: #e0f2fe;
  font-weight: 600;
}

.nav-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.nav-label {
  flex: 1;
}

.nav-badge :deep(.el-badge__content) {
  position: static;
  transform: none;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

/* 个人中心内容区：去掉大片白底，统一半透明冷色 */
.profile-main :deep(.page-card) {
  background: rgba(8, 20, 40, 0.42);
  border: 1px solid rgba(125, 211, 252, 0.16);
  backdrop-filter: blur(12px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.16);
  color: #e2e8f0;
}

.profile-main :deep(.page-card--plain) {
  background: transparent;
  border: none;
  box-shadow: none;
  backdrop-filter: none;
}

.profile-main :deep(.page-header__main h1),
.profile-main :deep(.page-stat-value),
.profile-main :deep(.page-quick-title),
.profile-main :deep(.page-quick-section h2) {
  color: #e0f2fe;
}

.profile-main :deep(.page-header__main p),
.profile-main :deep(.page-stat-label),
.profile-main :deep(.page-stat-suffix),
.profile-main :deep(.page-quick-desc),
.profile-main :deep(.page-text-muted),
.profile-main :deep(.page-summary-label) {
  color: rgba(186, 230, 253, 0.82);
}

.profile-main :deep(.page-stat-card),
.profile-main :deep(.page-quick-card) {
  background: rgba(8, 20, 40, 0.38);
  border: 1px solid rgba(125, 211, 252, 0.14);
  color: #e2e8f0;
  backdrop-filter: blur(10px);
}

.profile-main :deep(.page-stat-card:hover),
.profile-main :deep(.page-quick-card:hover) {
  border-color: rgba(56, 189, 248, 0.4);
  box-shadow: 0 10px 24px rgba(14, 165, 233, 0.12);
}

.profile-main :deep(.page-summary-card) {
  background: rgba(56, 189, 248, 0.12);
  border: 1px solid rgba(125, 211, 252, 0.16);
}

.profile-main :deep(.page-summary-value) {
  color: #7dd3fc;
}

.profile-main :deep(.el-card) {
  background: rgba(8, 20, 40, 0.42);
  border: 1px solid rgba(125, 211, 252, 0.16);
  color: #e2e8f0;
  backdrop-filter: blur(10px);
}

.profile-main :deep(.el-card__header) {
  border-bottom-color: rgba(125, 211, 252, 0.12);
  color: #e0f2fe;
}

/* 表格：修复斑马纹/悬停白底导致浅色字看不清 */
.profile-main :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(14, 32, 58, 0.72);
  --el-table-row-hover-bg-color: rgba(56, 189, 248, 0.18);
  --el-table-current-row-bg-color: rgba(56, 189, 248, 0.16);
  --el-table-text-color: #eef6ff;
  --el-table-header-text-color: #dbeafe;
  --el-table-border-color: rgba(125, 211, 252, 0.16);
  --el-fill-color-lighter: rgba(56, 189, 248, 0.08);
  --el-fill-color-light: rgba(56, 189, 248, 0.12);
  --el-bg-color: transparent;
  background: transparent;
  color: #eef6ff;
}

.profile-main :deep(.el-table th.el-table__cell) {
  background: rgba(14, 32, 58, 0.72) !important;
  color: #dbeafe !important;
  font-weight: 600;
}

.profile-main :deep(.el-table td.el-table__cell) {
  background: transparent !important;
  color: #eef6ff;
}

.profile-main :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: rgba(56, 189, 248, 0.1) !important;
}

.profile-main :deep(.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell) {
  background: rgba(56, 189, 248, 0.2) !important;
  color: #f8fbff !important;
}

.profile-main :deep(.el-table__body tr.current-row > td.el-table__cell) {
  background: rgba(56, 189, 248, 0.18) !important;
}

.profile-main :deep(.el-table__fixed),
.profile-main :deep(.el-table__fixed-right) {
  background: transparent;
}

.profile-main :deep(.el-table__fixed-right-patch),
.profile-main :deep(.el-table__fixed-header-wrapper th.el-table__cell),
.profile-main :deep(.el-table__fixed-body-wrapper td.el-table__cell) {
  background: rgba(10, 24, 48, 0.92) !important;
}

.profile-main :deep(.el-table .cell) {
  color: inherit;
}

.profile-main :deep(.el-table .el-tag) {
  border-color: transparent;
}

.profile-main :deep(.el-table .el-tag--info) {
  --el-tag-bg-color: rgba(148, 163, 184, 0.22);
  --el-tag-text-color: #e2e8f0;
  --el-tag-border-color: rgba(148, 163, 184, 0.28);
}

.profile-main :deep(.el-tabs__item) {
  color: rgba(186, 230, 253, 0.72);
}

.profile-main :deep(.el-tabs__item.is-active),
.profile-main :deep(.el-tabs__item:hover) {
  color: #7dd3fc;
}

.profile-main :deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(125, 211, 252, 0.16);
}

.profile-main :deep(.el-tabs__active-bar) {
  background-color: #38bdf8;
}

.profile-main :deep(.page-toolbar) {
  color: #e2e8f0;
}

.profile-main :deep(.page-toolbar .el-button.is-plain),
.profile-main :deep(.page-toolbar .el-button--default) {
  --el-button-bg-color: rgba(8, 20, 40, 0.55);
  --el-button-border-color: rgba(125, 211, 252, 0.3);
  --el-button-text-color: #e0f2fe;
  --el-button-hover-bg-color: rgba(56, 189, 248, 0.18);
  --el-button-hover-border-color: rgba(56, 189, 248, 0.5);
  --el-button-hover-text-color: #f0f9ff;
}

.profile-main :deep(.el-descriptions) {
  --el-descriptions-table-border: rgba(125, 211, 252, 0.14);
  --el-text-color-primary: #e2e8f0;
  --el-fill-color-blank: rgba(8, 20, 40, 0.35);
  --el-fill-color-light: rgba(15, 23, 42, 0.45);
  --el-text-color-regular: #cbd5e1;
}

.profile-main :deep(.el-input__wrapper),
.profile-main :deep(.el-textarea__inner),
.profile-main :deep(.el-select__wrapper),
.profile-main :deep(.el-date-editor.el-input__wrapper) {
  background: rgba(8, 20, 40, 0.55) !important;
  box-shadow: 0 0 0 1px rgba(125, 211, 252, 0.28) inset !important;
}

.profile-main :deep(.el-input__inner),
.profile-main :deep(.el-textarea__inner),
.profile-main :deep(.el-select__placeholder),
.profile-main :deep(.el-select__selected-item),
.profile-main :deep(.el-range-input),
.profile-main :deep(.el-range-separator) {
  color: #e2e8f0 !important;
}

.profile-main :deep(.el-input__inner::placeholder),
.profile-main :deep(.el-textarea__inner::placeholder),
.profile-main :deep(.el-select__placeholder) {
  color: rgba(148, 163, 184, 0.85) !important;
}

.profile-main :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: rgba(8, 20, 40, 0.45);
  --el-pagination-button-color: #e0f2fe;
  --el-pagination-hover-color: #7dd3fc;
  --el-text-color-regular: #cbd5e1;
  color: #cbd5e1;
}

.profile-main :deep(.el-empty__description p) {
  color: rgba(186, 230, 253, 0.75);
}

.profile-main :deep(.el-loading-mask) {
  background-color: rgba(8, 20, 40, 0.45);
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
    position: static;
    padding: 16px 12px;
  }

  .sidebar-nav {
    flex-direction: row;
    flex-wrap: nowrap;
    overflow-x: auto;
    gap: 6px;
    padding-bottom: 4px;
    -webkit-overflow-scrolling: touch;
  }

  .nav-item {
    flex-shrink: 0;
    white-space: nowrap;
  }

  .nav-divider {
    display: none;
  }
}
</style>
