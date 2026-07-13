<script setup lang="ts">
import UiIcon from '@/components/ui/UiIcon.vue'

defineProps<{
  title: string
  icon?: string
  moreTo?: string
}>()
</script>

<template>
  <div class="home-section-header">
    <div class="header-left">
      <UiIcon v-if="icon" class="header-icon" :name="icon" :size="24" />
      <h2 class="header-title">{{ title }}</h2>
    </div>
    <router-link v-if="moreTo" :to="moreTo" class="header-more">
      查看更多 &gt;
    </router-link>
  </div>
</template>

<style scoped>
.home-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  line-height: 1;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.header-more {
  font-size: 14px;
  color: var(--color-primary);
  text-decoration: none;
  white-space: nowrap;
}

.header-more:hover {
  opacity: 0.85;
}
</style>
