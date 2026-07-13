<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { homeBannerSlides, BANNER_AUTOPLAY_MS } from '@/config/home-banner'
import { getRoleTheme, type RoleThemeVariant } from '@/config/role-theme'

const props = withDefaults(
  defineProps<{
    compact?: boolean
    variant?: RoleThemeVariant
  }>(),
  {
    variant: 'student',
  },
)

const router = useRouter()
const slides = homeBannerSlides
const currentIndex = ref(0)
const isPaused = ref(false)

const theme = computed(() => getRoleTheme(props.variant))

const carouselStyle = computed(() => ({
  '--banner-border': theme.value.border,
  '--banner-shadow': theme.value.shadow,
  '--banner-accent': theme.value.primarySoft,
  '--banner-accent-strong': theme.value.primary,
  '--banner-text': theme.value.text,
  '--banner-text-muted': theme.value.textMuted,
  '--banner-hero': theme.value.heroGradient,
}))

let timer: ReturnType<typeof setInterval> | null = null

function goTo(index: number) {
  currentIndex.value = (index + slides.length) % slides.length
}

function next() {
  goTo(currentIndex.value + 1)
}

function startAutoplay() {
  stopAutoplay()
  timer = setInterval(() => {
    if (!isPaused.value) next()
  }, BANNER_AUTOPLAY_MS)
}

function stopAutoplay() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function onSlideClick(slide: (typeof slides)[number]) {
  if (slide.link) router.push(slide.link)
}

onMounted(startAutoplay)
onUnmounted(stopAutoplay)
</script>

<template>
  <section
    class="banner-carousel"
    :class="{ 'banner-carousel--compact': compact }"
    :style="carouselStyle"
    @mouseenter="isPaused = true"
    @mouseleave="isPaused = false"
  >
    <div class="banner-track">
      <TransitionGroup name="banner-fade">
        <div
          v-for="(slide, index) in slides"
          v-show="index === currentIndex"
          :key="slide.id"
          class="banner-slide"
          :class="{ 'is-clickable': !!slide.link }"
          @click="onSlideClick(slide)"
        >
          <template v-if="slide.type === 'hero'">
            <div class="hero-slide">
              <div class="hero-content">
                <h2 class="hero-title">{{ slide.title }}</h2>
                <p class="hero-sub">{{ slide.subtitle }}</p>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="image-slide">
              <img
                class="slide-image"
                :src="slide.image"
                :alt="slide.alt ?? ''"
                draggable="false"
              />
              <div class="image-overlay" aria-hidden="true" />
              <p v-if="slide.alt" class="image-caption">{{ slide.alt }}</p>
            </div>
          </template>
        </div>
      </TransitionGroup>
    </div>

    <div class="banner-indicators">
      <button
        v-for="(slide, index) in slides"
        :key="slide.id"
        type="button"
        class="indicator"
        :class="{ active: index === currentIndex }"
        :aria-label="`切换到第 ${index + 1} 张：${slide.title ?? slide.alt}`"
        @click.stop="goTo(index)"
      />
    </div>
  </section>
</template>

<style scoped>
.banner-carousel {
  position: relative;
  width: 100%;
  height: clamp(200px, 26vw, 300px);
  overflow: hidden;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-neutral);
  box-shadow: var(--banner-shadow, var(--shadow-md));
  background: var(--color-card);
}

.banner-carousel--compact {
  height: clamp(180px, 24vw, 260px);
}

.banner-track {
  position: relative;
  width: 100%;
  height: 100%;
}

.banner-slide {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.banner-slide.is-clickable {
  cursor: pointer;
}

.hero-slide {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--banner-hero, var(--hero-gradient));
}

.hero-content {
  position: relative;
  text-align: center;
  padding: 0 24px;
}

.hero-title {
  font-family: var(--font-heading);
  font-size: clamp(1.5rem, 3.5vw, 2.25rem);
  font-weight: 700;
  letter-spacing: 0.06em;
  margin: 0 0 10px;
  color: var(--color-foreground);
}

.hero-sub {
  margin: 0;
  font-size: clamp(0.9rem, 2vw, 1.1rem);
  color: var(--color-muted-foreground);
}

.image-slide {
  position: relative;
  width: 100%;
  height: 100%;
  background: var(--color-muted);
}

.slide-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
  user-select: none;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(19, 78, 74, 0.05) 0%,
    rgba(19, 78, 74, 0.55) 100%
  );
  pointer-events: none;
}

.image-caption {
  position: absolute;
  left: 24px;
  right: 24px;
  bottom: 48px;
  margin: 0;
  font-family: var(--font-heading);
  font-size: clamp(1rem, 2.2vw, 1.25rem);
  font-weight: 600;
  line-height: 1.5;
  color: #fff;
}

.banner-indicators {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 10;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid var(--color-border-neutral);
  box-shadow: var(--shadow-sm);
}

.indicator {
  width: 22px;
  height: 4px;
  padding: 0;
  border: none;
  border-radius: 2px;
  background: rgba(13, 148, 136, 0.25);
  cursor: pointer;
  transition: width 0.3s ease, background 0.3s ease;
}

.indicator.active {
  width: 34px;
  background: var(--banner-accent-strong, var(--color-primary));
}

.indicator:hover {
  background: var(--color-secondary);
}

.banner-fade-enter-active,
.banner-fade-leave-active {
  transition: opacity 0.6s ease;
}

.banner-fade-enter-from,
.banner-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .banner-carousel {
    height: clamp(170px, 44vw, 240px);
  }

  .image-caption {
    left: 16px;
    right: 16px;
    bottom: 44px;
    font-size: 0.95rem;
  }

  .banner-indicators {
    bottom: 12px;
  }
}
</style>
