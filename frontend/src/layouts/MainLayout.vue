<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import BackToTop from '@/components/layout/BackToTop.vue'
import AiAssistantPet from '@/components/agent/AiAssistantPet.vue'
import {
  initGuestDailyReminder,
  registerGuestReminderServiceWorker,
} from '@/utils/guest-daily-reminder'

const authStore = useAuthStore()

onMounted(() => {
  void registerGuestReminderServiceWorker()
  initGuestDailyReminder(authStore.isLoggedIn)
})
</script>

<template>
  <div class="main-layout">
    <div class="layout-content">
      <AppHeader />
      <main class="main-content">
        <router-view />
      </main>
      <AppFooter />
      <BackToTop />
      <AiAssistantPet />
    </div>
  </div>
</template>

<style scoped>
.main-layout {
  position: relative;
  min-height: 100vh;
  background: var(--nb-cream, var(--color-background));
}

.layout-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  width: 100%;
  padding-top: calc(var(--header-height) + var(--header-gap));
}
</style>
