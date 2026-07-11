<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import BackToTop from '@/components/layout/BackToTop.vue'
import AiAssistantPet from '@/components/agent/AiAssistantPet.vue'
import ParticleBackground from '@/components/effects/ParticleBackground.vue'

const route = useRoute()
const isHomePage = computed(() => route.name === 'home')
</script>

<template>
  <div class="main-layout">
    <ParticleBackground />
    <div class="layout-content">
      <AppHeader />
      <main class="main-content" :class="{ 'main-content--offset': !isHomePage }">
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
}

.main-content--offset {
  padding-top: var(--header-height);
}
</style>
