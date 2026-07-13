<script setup lang="ts">
import { computed } from 'vue'
import { resolveUiIcon, UI_ICON_COLOR } from '@/config/ui-icons'

const props = withDefaults(
  defineProps<{
    name?: string
    size?: number | string
    color?: string
  }>(),
  {
    name: 'collection',
    size: 22,
    color: UI_ICON_COLOR,
  },
)

const icon = computed(() => resolveUiIcon(props.name))
</script>

<template>
  <el-icon class="ui-icon" :size="size" :color="color">
    <component :is="icon" />
  </el-icon>
</template>

<style scoped>
.ui-icon {
  flex-shrink: 0;
  vertical-align: middle;
}
</style>
