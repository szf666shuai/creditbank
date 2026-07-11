<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { getProfileMenuByRole, isMenuActive } from '@/config/profile-menu'
import { ROLE_STUDENT, roleLabel } from '@/types/auth'

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
              <span class="nav-icon">{{ item.icon }}</span>
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
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px 12px;
  position: sticky;
  top: calc(var(--header-height) + 16px);
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px 16px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-primary-light);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.sidebar-meta {
  min-width: 0;
  flex: 1;
}

.sidebar-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
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
  background: var(--color-border);
  margin: 8px 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  text-decoration: none;
  color: var(--color-text-secondary);
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}

.nav-item:hover {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.nav-item.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
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
