<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import StudentHomeView from '@/views/home/StudentHomeView.vue'
import EnterpriseHomeView from '@/views/home/EnterpriseHomeView.vue'
import AdminHomeView from '@/views/home/AdminHomeView.vue'

const authStore = useAuthStore()

const homeComponent = computed(() => {
  if (authStore.isEnterprise) return EnterpriseHomeView
  if (authStore.isAdmin) return AdminHomeView
  return StudentHomeView
})

onMounted(async () => {
  if (authStore.isLoggedIn && !authStore.userInfo) {
    await authStore.fetchUserInfo()
  }
})
</script>

<template>
  <component :is="homeComponent" />
</template>
