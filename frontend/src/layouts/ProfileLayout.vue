<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import {
  getProfileMenuByRole,
  isGroupActive,
  isMenuActive,
  type ProfileMenuItem,
} from '@/config/profile-menu'
import { ROLE_STUDENT, roleLabel } from '@/types/auth'
import UiIcon from '@/components/ui/UiIcon.vue'

const route = useRoute()
const authStore = useAuthStore()
const messageStore = useMessageStore()

const menuItems = computed(() => {
  const role = authStore.userInfo?.role ?? ROLE_STUDENT
  return getProfileMenuByRole(role)
})

const expandedKeys = ref<string[]>([])

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

function groupActive(item: ProfileMenuItem) {
  return isGroupActive(item, route.path)
}

function isExpanded(key: string) {
  return expandedKeys.value.includes(key)
}

function toggleGroup(item: ProfileMenuItem) {
  if (!item.children?.length) return
  if (isExpanded(item.key)) {
    expandedKeys.value = expandedKeys.value.filter((k) => k !== item.key)
  } else {
    expandedKeys.value = [...expandedKeys.value, item.key]
  }
}

function syncExpandedFromRoute() {
  const next = new Set(expandedKeys.value)
  for (const item of menuItems.value) {
    if (item.children?.length && isGroupActive(item, route.path)) {
      next.add(item.key)
    }
  }
  expandedKeys.value = [...next]
}

watch(() => route.path, syncExpandedFromRoute, { immediate: true })
watch(menuItems, syncExpandedFromRoute)

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

            <template v-if="item.children?.length">
              <button
                type="button"
                class="nav-item nav-item--group"
                :class="{ active: groupActive(item), open: isExpanded(item.key) }"
                @click="toggleGroup(item)"
              >
                <UiIcon class="nav-icon" :name="item.icon" :size="18" />
                <span class="nav-label">{{ item.label }}</span>
                <span class="nav-caret" aria-hidden="true">{{ isExpanded(item.key) ? '▾' : '▸' }}</span>
              </button>
              <div v-show="isExpanded(item.key)" class="nav-children">
                <router-link
                  v-for="child in item.children"
                  :key="child.key"
                  :to="child.path"
                  class="nav-item nav-item--child"
                  :class="{ active: menuActive(child.path) }"
                >
                  <span class="nav-label">{{ child.label }}</span>
                </router-link>
              </div>
            </template>

            <router-link
              v-else
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
  background: transparent;
  width: 100%;
  text-align: left;
  cursor: pointer;
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

.nav-item--group.open:not(.active) {
  background: color-mix(in srgb, var(--nb-cream, #fff9f0) 80%, white);
  border-color: color-mix(in srgb, var(--nb-ink, #1a202c) 35%, transparent);
  color: var(--nb-ink, #1a202c);
}

.nav-children {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 2px 0 6px 12px;
}

.nav-item--child {
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 600;
}

.nav-caret {
  margin-left: auto;
  font-size: 12px;
  opacity: 0.7;
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

.profile-main :deep(.page-card) {
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow, var(--shadow-md));
  color: var(--color-card-foreground);
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
    flex-direction: column;
  }

  .nav-divider {
    display: none;
  }
}
</style>
