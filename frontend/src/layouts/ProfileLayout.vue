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
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  border-radius: 18px;
  padding: 20px 12px;
  position: sticky;
  top: calc(var(--header-height) + 16px);
  box-shadow: var(--nb-shadow, var(--shadow-md));
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px 16px;
  margin-bottom: 8px;
  border-bottom: 2px solid color-mix(in srgb, var(--nb-ink, #1a202c) 14%, transparent);
}

.sidebar-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--nb-pink, var(--color-primary-light));
  color: var(--nb-ink, var(--color-primary-dark));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  flex-shrink: 0;
  border: 2.5px solid var(--nb-ink, var(--color-border));
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.sidebar-meta {
  min-width: 0;
  flex: 1;
}

.sidebar-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-foreground);
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
  background: var(--color-border-neutral);
  margin: 8px 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  text-decoration: none;
  color: var(--color-muted-foreground);
  font-size: 14px;
  font-weight: 700;
  border: 2px solid transparent;
  transition: background 0.12s ease, color 0.12s ease, border-color 0.12s ease;
}

.nav-item:hover {
  background: var(--nb-yellow, #fef08a);
  color: var(--nb-ink, #1a202c);
  border-color: var(--nb-ink, #1a202c);
}

.nav-item.active {
  background: #bbf7d0;
  color: var(--nb-ink, #1a202c);
  font-weight: 800;
  border-color: var(--nb-ink, #1a202c);
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.nav-item.active :deep(.ui-icon) {
  color: var(--nb-ink, #1a202c);
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

/* 与公开页统一：Neo-brutal 白卡片 */
.profile-main :deep(.page-card) {
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow, var(--shadow-md));
  color: var(--color-card-foreground);
}

.profile-main :deep(.page-card--plain) {
  background: transparent;
  border: none;
  box-shadow: none;
}

.profile-main :deep(.page-header__main h1),
.profile-main :deep(.page-stat-value),
.profile-main :deep(.page-quick-title),
.profile-main :deep(.page-quick-section h2) {
  color: var(--color-foreground);
}

.profile-main :deep(.page-header__main p),
.profile-main :deep(.page-stat-label),
.profile-main :deep(.page-stat-suffix),
.profile-main :deep(.page-quick-desc),
.profile-main :deep(.page-text-muted),
.profile-main :deep(.page-summary-label) {
  color: var(--color-muted-foreground);
}

.profile-main :deep(.page-stat-card),
.profile-main :deep(.page-quick-card) {
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  color: var(--color-card-foreground);
  box-shadow: var(--nb-shadow-sm, var(--shadow-sm));
}

.profile-main :deep(.page-stat-card:hover),
.profile-main :deep(.page-quick-card:hover) {
  border-color: var(--nb-ink, var(--color-border));
  transform: translate(2px, 2px);
  box-shadow: 1px 1px 0 0 var(--nb-ink, #1a202c);
}

.profile-main :deep(.page-summary-card) {
  background: var(--nb-blue, var(--color-primary-light));
  border: 2.5px solid var(--nb-ink, var(--color-border));
}

.profile-main :deep(.page-summary-value) {
  color: var(--nb-green-deep, var(--color-primary-dark));
}

.profile-main :deep(.el-card) {
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  color: var(--color-card-foreground);
  box-shadow: var(--nb-shadow-sm, var(--shadow-sm));
}

.profile-main :deep(.el-card__header) {
  border-bottom-color: var(--color-border-neutral);
  color: var(--color-foreground);
}

/* 表格/表单 — Neo-brutal（细节由 neubrutal-crud.css 接管） */
.profile-main :deep(.el-table) {
  --el-table-bg-color: #fff;
  --el-table-tr-bg-color: #fff;
  --el-table-header-bg-color: var(--nb-cream, #fff9f0);
  --el-table-row-hover-bg-color: #bbf7d0;
  --el-table-current-row-bg-color: #dcfce7;
  --el-table-text-color: var(--nb-ink, #1a202c);
  --el-table-header-text-color: var(--nb-ink, #1a202c);
  --el-table-border-color: var(--nb-ink, #1a202c);
  --el-fill-color-lighter: var(--nb-cream, #fff9f0);
  --el-fill-color-light: var(--nb-cream, #fff9f0);
  --el-bg-color: #fff;
  background: #fff;
  color: var(--nb-ink, #1a202c);
}

.profile-main :deep(.el-table th.el-table__cell) {
  background: var(--nb-cream, #fff9f0) !important;
  color: var(--nb-ink, #1a202c) !important;
  font-weight: 800;
}

.profile-main :deep(.el-table td.el-table__cell) {
  background: transparent !important;
  color: var(--nb-ink, #1a202c);
}

.profile-main :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: var(--nb-cream, #fff9f0) !important;
}

.profile-main :deep(.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell) {
  background: #bbf7d0 !important;
  color: var(--nb-ink, #1a202c) !important;
}

.profile-main :deep(.el-table__body tr.current-row > td.el-table__cell) {
  background: #dcfce7 !important;
}

.profile-main :deep(.el-table__fixed),
.profile-main :deep(.el-table__fixed-right) {
  background: #fff;
}

.profile-main :deep(.el-table__fixed-right-patch),
.profile-main :deep(.el-table__fixed-header-wrapper th.el-table__cell),
.profile-main :deep(.el-table__fixed-body-wrapper td.el-table__cell) {
  background: #fff !important;
}

.profile-main :deep(.el-table .cell) {
  color: inherit;
}

.profile-main :deep(.el-tabs__item) {
  color: var(--color-muted-foreground);
  font-weight: 700;
}

.profile-main :deep(.el-tabs__item.is-active),
.profile-main :deep(.el-tabs__item:hover) {
  color: var(--nb-ink, #1a202c);
}

.profile-main :deep(.el-tabs__nav-wrap::after) {
  background-color: color-mix(in srgb, var(--nb-ink, #1a202c) 18%, transparent);
}

.profile-main :deep(.el-tabs__active-bar) {
  background-color: var(--nb-green, #22c55e);
  height: 3px;
}

.profile-main :deep(.page-toolbar) {
  color: var(--color-foreground);
  gap: 10px;
}

.profile-main :deep(.el-descriptions) {
  --el-descriptions-table-border: var(--nb-ink, #1a202c);
  --el-text-color-primary: var(--nb-ink, #1a202c);
  --el-fill-color-blank: #fff;
  --el-fill-color-light: var(--nb-cream, #fff9f0);
  --el-text-color-regular: var(--color-muted-foreground);
}

.profile-main :deep(.el-input__wrapper),
.profile-main :deep(.el-textarea__inner),
.profile-main :deep(.el-select__wrapper),
.profile-main :deep(.el-date-editor.el-input__wrapper) {
  background: #fff !important;
  box-shadow: none !important;
  border: 2px solid var(--nb-ink, #1a202c) !important;
  border-radius: 12px !important;
}

.profile-main :deep(.el-input__wrapper.is-focus),
.profile-main :deep(.el-select__wrapper.is-focused) {
  box-shadow: 3px 3px 0 0 var(--nb-ink, #1a202c) !important;
}

.profile-main :deep(.el-input__inner),
.profile-main :deep(.el-textarea__inner),
.profile-main :deep(.el-select__placeholder),
.profile-main :deep(.el-select__selected-item),
.profile-main :deep(.el-range-input),
.profile-main :deep(.el-range-separator) {
  color: var(--color-foreground) !important;
}

.profile-main :deep(.el-input__inner::placeholder),
.profile-main :deep(.el-textarea__inner::placeholder),
.profile-main :deep(.el-select__placeholder) {
  color: var(--color-muted-foreground) !important;
}

.profile-main :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: var(--color-card);
  --el-pagination-button-color: var(--color-foreground);
  --el-pagination-hover-color: var(--color-primary);
  --el-text-color-regular: var(--color-muted-foreground);
  color: var(--color-muted-foreground);
}

.profile-main :deep(.el-empty__description p) {
  color: var(--color-muted-foreground);
}

.profile-main :deep(.el-loading-mask) {
  background-color: color-mix(in srgb, var(--color-background) 70%, white);
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
