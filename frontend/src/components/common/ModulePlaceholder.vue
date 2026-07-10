<script setup lang="ts">
import PageShell from '@/components/common/PageShell.vue'

defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <PageShell :title="title" :description="description" plain>
    <div class="module-placeholder">
      <el-empty :image-size="96" description="该模块正在开发中，敬请期待">
        <router-link to="/" class="placeholder-link">返回首页</router-link>
      </el-empty>
    </div>
  </PageShell>
</template>

<style scoped>
.module-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
}

.placeholder-link {
  display: inline-block;
  padding: 8px 24px;
  background: var(--color-primary);
  color: var(--color-white);
  text-decoration: none;
  border-radius: 20px;
  font-size: 14px;
  transition: background 0.2s;
}

.placeholder-link:hover {
  background: var(--color-primary-dark);
}
</style>
