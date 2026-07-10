<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
  loading?: boolean
  error?: string | null
  plain?: boolean
}>()

defineEmits<{
  retry: []
}>()
</script>

<template>
  <div class="page-shell">
    <div class="page-card" :class="{ 'page-card--plain': plain }">
      <slot name="header">
        <div v-if="title" class="page-header">
          <div class="page-header__main">
            <h1>{{ title }}</h1>
            <p v-if="description">{{ description }}</p>
          </div>
          <div v-if="$slots.actions" class="page-header__actions">
            <slot name="actions" />
          </div>
        </div>
      </slot>

      <div v-if="error" class="page-error">
        <el-alert type="error" :title="error" show-icon :closable="false">
          <el-button link type="primary" @click="$emit('retry')">点击重试</el-button>
        </el-alert>
      </div>

      <div
        v-loading="loading"
        element-loading-text="加载中..."
        class="page-body"
      >
        <slot v-if="!error" />
      </div>
    </div>
  </div>
</template>
