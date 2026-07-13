<script setup lang="ts">
import UiIcon from '@/components/ui/UiIcon.vue'

withDefaults(
  defineProps<{
    title: string
    icon?: string
    badge?: string
    moreTo?: string
    moreLabel?: string
  }>(),
  {
    moreLabel: '查看更多',
  },
)
</script>

<template>
  <div class="home-section-header">
    <div class="header-left">
      <span v-if="badge" class="header-badge">{{ badge }}</span>
      <div class="header-title-row">
        <span v-if="icon" class="header-icon-box">
          <UiIcon :name="icon" :size="18" />
        </span>
        <h2 class="header-title">{{ title }}</h2>
      </div>
    </div>
    <router-link v-if="moreTo" :to="moreTo" class="header-more">
      {{ moreLabel }} →
    </router-link>
  </div>
</template>

<style scoped>
.home-section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.header-left {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  border: 2px solid var(--nb-ink);
  border-radius: 999px;
  background: var(--nb-blue);
  box-shadow: var(--nb-shadow-sm);
  font-family: var(--font-heading);
  font-size: 12px;
  font-weight: 800;
  color: var(--nb-ink);
}

.header-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon-box {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border: 2px solid var(--nb-ink);
  border-radius: 12px;
  background: var(--nb-pink);
  color: var(--nb-ink);
  box-shadow: 2px 2px 0 0 var(--nb-ink);
  flex-shrink: 0;
}

.header-title {
  font-family: var(--font-heading);
  font-size: clamp(1.35rem, 2.4vw, 1.75rem);
  font-weight: 900;
  color: var(--nb-ink);
  margin: 0;
  letter-spacing: -0.02em;
}

.header-more {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border: 2px solid var(--nb-ink);
  border-radius: 999px;
  background: var(--nb-blue);
  box-shadow: var(--nb-shadow-sm);
  font-family: var(--font-heading);
  font-size: 0.8125rem;
  font-weight: 800;
  color: var(--nb-ink);
  text-decoration: none;
  white-space: nowrap;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.header-more:hover {
  transform: translate(2px, 2px);
  box-shadow: 1px 1px 0 0 var(--nb-ink);
  color: var(--nb-ink);
}
</style>
