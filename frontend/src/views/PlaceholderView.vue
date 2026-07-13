<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import UiIcon from '@/components/ui/UiIcon.vue'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '页面建设中')
</script>

<template>
  <div class="placeholder">
    <div class="placeholder-inner">
      <UiIcon class="icon" name="collection" :size="48" />
      <h2>{{ title }}</h2>
      <p>该模块正在开发中，敬请期待</p>
      <router-link to="/" class="back-link">返回首页</router-link>
    </div>
  </div>
</template>

<style scoped>
.placeholder {
  min-height: calc(100vh - var(--header-height));
  display: grid;
  place-items: center;
  padding: 40px 16px;
}

.placeholder-inner {
  text-align: center;
  color: #e2e8f0;
}

.icon {
  margin-bottom: 16px;
}

.placeholder-inner h2 {
  margin: 0 0 8px;
  font-size: 24px;
}

.placeholder-inner p {
  margin: 0 0 20px;
  color: rgba(186, 230, 253, 0.75);
}

.back-link {
  color: #38bdf8;
  text-decoration: none;
}

.back-link:hover {
  text-decoration: underline;
}
</style>
