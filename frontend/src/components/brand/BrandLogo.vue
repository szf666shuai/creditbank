<script setup lang="ts">
import { computed } from 'vue'
import { getRoleTheme, type RoleThemeVariant } from '@/config/role-theme'

const props = withDefaults(
  defineProps<{
    size?: number
    showBackground?: boolean
    variant?: RoleThemeVariant
  }>(),
  {
    size: 40,
    showBackground: true,
    variant: 'default',
  },
)

const theme = computed(() => getRoleTheme(props.variant))
const gradientId = computed(() => `brandLogoBg-${props.variant}`)
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 40 40"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <defs>
      <linearGradient :id="gradientId" x1="4" y1="4" x2="36" y2="36" gradientUnits="userSpaceOnUse">
        <stop :stop-color="theme.logoBgFrom" />
        <stop offset="1" :stop-color="theme.logoBgTo" />
      </linearGradient>
    </defs>

    <rect
      v-if="showBackground"
      width="40"
      height="40"
      rx="8"
      :fill="`url(#${gradientId})`"
    />

    <path
      d="M20 7.5L21.55 12.35L26.6 12.35L22.52 15.32L24.07 20.17L20 17.2L15.93 20.17L17.48 15.32L13.4 12.35L18.45 12.35L20 7.5Z"
      :fill="theme.logoStar"
    />
    <circle cx="20" cy="13.8" r="1.2" fill="#fff8e7" opacity="0.9" />

    <path
      d="M11 22.5H29V31.5C29 32.33 28.33 33 27.5 33H12.5C11.67 33 11 32.33 11 31.5V22.5Z"
      fill="rgba(255,255,255,0.92)"
    />
    <path d="M20 22.5V33" :stroke="theme.logoAccent" stroke-width="1.2" />
    <path
      d="M13.5 26H18M22 26H26.5M13.5 29H18M22 29H25"
      stroke="#94a3b8"
      stroke-width="1"
      stroke-linecap="round"
    />
    <path
      d="M11 22.5L20 19.5L29 22.5"
      :stroke="theme.logoStar"
      stroke-width="1.2"
      stroke-linejoin="round"
    />
  </svg>
</template>
