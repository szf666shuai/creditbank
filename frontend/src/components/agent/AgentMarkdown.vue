<script setup lang="ts">
import { computed } from 'vue'
import { renderLiteMarkdown } from '@/utils/render-lite-markdown'

const props = defineProps<{
  content: string
}>()

const html = computed(() => renderLiteMarkdown(props.content || ''))
</script>

<template>
  <div class="agent-md" v-html="html" />
</template>

<style scoped>
.agent-md {
  font-size: inherit;
  line-height: 1.7;
  color: inherit;
  word-break: break-word;
}

.agent-md :deep(.md-p) {
  margin: 0 0 0.65em;
}

.agent-md :deep(.md-p:last-child) {
  margin-bottom: 0;
}

.agent-md :deep(.md-h) {
  margin: 0.4em 0 0.5em;
  font-size: 1.02em;
  font-weight: 700;
  line-height: 1.45;
  color: inherit;
}

.agent-md :deep(.md-ul),
.agent-md :deep(.md-ol) {
  margin: 0.35em 0 0.7em;
  padding-left: 1.25em;
}

.agent-md :deep(.md-ul) {
  list-style: disc;
}

.agent-md :deep(.md-ol) {
  list-style: decimal;
}

.agent-md :deep(li) {
  margin: 0.25em 0;
  padding-left: 0.15em;
}

.agent-md :deep(li::marker) {
  color: var(--color-primary, #16a34a);
  font-weight: 600;
}

.agent-md :deep(strong) {
  font-weight: 700;
  color: var(--color-foreground, inherit);
}

.agent-md :deep(em) {
  font-style: italic;
}

.agent-md :deep(code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.92em;
  padding: 0.1em 0.35em;
  border-radius: 4px;
  background: color-mix(in srgb, var(--color-border-neutral, #ddd) 55%, transparent);
}
</style>
