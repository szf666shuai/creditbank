<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import type { CourseDanmaku } from '@/api/learning'

interface ActiveDanmaku {
  key: string
  content: string
  color: string
  track: number
}

const props = defineProps<{
  items: CourseDanmaku[]
  currentTime: number
  enabled: boolean
  playing: boolean
}>()

const shownKeys = ref(new Set<string>())
const activeItems = ref<ActiveDanmaku[]>([])
const trackCount = 5

const sortedItems = computed(() =>
  [...props.items].sort((a, b) => a.videoTimeSeconds - b.videoTimeSeconds),
)

function pickTrack(seed: string) {
  let hash = 0
  for (let i = 0; i < seed.length; i += 1) {
    hash = (hash + seed.charCodeAt(i) * (i + 1)) % trackCount
  }
  return hash
}

function spawnDanmaku(item: CourseDanmaku) {
  const key = `${item.id}-${Math.floor(item.videoTimeSeconds)}`
  if (shownKeys.value.has(key)) return
  shownKeys.value.add(key)
  const entry: ActiveDanmaku = {
    key,
    content: item.content,
    color: item.color || '#ffffff',
    track: pickTrack(key),
  }
  activeItems.value.push(entry)
  window.setTimeout(() => {
    activeItems.value = activeItems.value.filter((active) => active.key !== key)
  }, 9000)
}

watch(
  () => props.currentTime,
  (time) => {
    if (!props.enabled || !props.playing) return
    sortedItems.value.forEach((item) => {
      const delta = time - item.videoTimeSeconds
      if (delta >= 0 && delta < 0.35) {
        spawnDanmaku(item)
      }
    })
  },
)

watch(
  () => props.enabled,
  (enabled) => {
    if (!enabled) {
      activeItems.value = []
    }
  },
)

watch(
  () => props.items,
  () => {
    shownKeys.value.clear()
    activeItems.value = []
  },
)

onBeforeUnmount(() => {
  activeItems.value = []
  shownKeys.value.clear()
})
</script>

<template>
  <div v-if="enabled" class="danmaku-layer" aria-hidden="true">
    <span
      v-for="item in activeItems"
      :key="item.key"
      class="danmaku-item"
      :style="{
        color: item.color,
        top: `${12 + item.track * 28}px`,
        animationDuration: '9s',
      }"
    >
      {{ item.content }}
    </span>
  </div>
</template>

<style scoped>
.danmaku-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 2;
}

.danmaku-item {
  position: absolute;
  right: 0;
  transform: translateX(100%);
  white-space: nowrap;
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.85), 0 0 6px rgba(0, 0, 0, 0.45);
  animation-name: danmaku-fly;
  animation-timing-function: linear;
  animation-fill-mode: forwards;
}

@keyframes danmaku-fly {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(calc(-100vw - 100%));
  }
}
</style>
