<script setup lang="ts">
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import ParticleBackground from '@/components/effects/ParticleBackground.vue'
</script>

<template>
  <div class="main-layout">
    <ParticleBackground />
    <div class="layout-content">
      <AppHeader />
      <main class="main-content">
        <router-view />
      </main>
      <AppFooter />
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
</style>
