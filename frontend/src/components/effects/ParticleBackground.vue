<script setup lang="ts">
import { computed } from 'vue'
import { getRoleTheme, type RoleThemeVariant } from '@/config/role-theme'

const props = withDefaults(
  defineProps<{
    variant?: RoleThemeVariant
    /** When false, render nothing (used off-home). */
    enabled?: boolean
  }>(),
  {
    variant: 'default',
    enabled: true,
  },
)

const theme = computed(() => getRoleTheme(props.variant))

const ambienceStyle = computed(() => ({
  '--ambience-a': theme.value.particleBg[0],
  '--ambience-b': theme.value.particleBg[1],
  '--ambience-c': theme.value.particleBg[2],
  '--ambience-glow': theme.value.primarySoft,
}))
</script>

<template>
  <div
    v-if="enabled"
    class="soft-ambience"
    :style="ambienceStyle"
    aria-hidden="true"
  >
    <span class="soft-ambience__blob soft-ambience__blob--a" />
    <span class="soft-ambience__blob soft-ambience__blob--b" />
    <span class="soft-ambience__blob soft-ambience__blob--c" />
  </div>
</template>

<style scoped>
/**
 * Soft mint atmosphere — replaces sci-fi particle network (MASTER.md / student-home).
 * No canvas, no mouse trails, no explosions.
 */
.soft-ambience {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
  background: linear-gradient(
    165deg,
    var(--ambience-a) 0%,
    var(--ambience-b) 48%,
    var(--ambience-c) 100%
  );
}

.soft-ambience__blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(64px);
  opacity: 0.45;
}

.soft-ambience__blob--a {
  width: min(52vw, 520px);
  height: min(52vw, 520px);
  top: -12%;
  right: -8%;
  background: color-mix(in srgb, var(--ambience-glow) 55%, white);
}

.soft-ambience__blob--b {
  width: min(44vw, 440px);
  height: min(44vw, 440px);
  bottom: 8%;
  left: -10%;
  background: color-mix(in srgb, var(--ambience-glow) 35%, #fff9f0);
  opacity: 0.35;
}

.soft-ambience__blob--c {
  width: min(36vw, 360px);
  height: min(36vw, 360px);
  top: 42%;
  left: 38%;
  background: rgba(255, 255, 255, 0.7);
  opacity: 0.5;
  filter: blur(80px);
}

@media (prefers-reduced-motion: reduce) {
  .soft-ambience__blob {
    filter: blur(48px);
  }
}
</style>
