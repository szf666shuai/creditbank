<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '页面建设中')
</script>

<template>
  <div class="placeholder">
    <div class="placeholder-inner">
      <div class="icon">🚧</div>
      <h2>{{ title }}</h2>
      <p>该模块正在开发中，敬请期待</p>
      <router-link to="/" class="back-link">返回首页</router-link>
    </div>
  </div>
</template>

<style scoped>
.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 60px 20px;
}

.placeholder-inner {
  text-align: center;
}

.icon {
  font-size: 48px;
  margin-bottom: 16px;
}

h2 {
  font-size: 22px;
  color: var(--color-text);
  margin-bottom: 8px;
}

p {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-bottom: 24px;
}

.back-link {
  display: inline-block;
  padding: 8px 24px;
  background: var(--color-primary);
  color: var(--color-white);
  text-decoration: none;
  border-radius: 20px;
  font-size: 14px;
  transition: background 0.2s;
}

.back-link:hover {
  background: var(--color-primary-dark);
}
</style>
