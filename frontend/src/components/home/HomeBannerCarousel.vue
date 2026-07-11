<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { homeBannerSlides, BANNER_AUTOPLAY_MS } from '@/config/home-banner'
import { getRoleTheme, type RoleThemeVariant } from '@/config/role-theme'

const props = withDefaults(
  defineProps<{
    /** 学员门户等场景使用更紧凑的轮播高度 */
    compact?: boolean
    /** 与角色星空主题适配的深色玻璃风格 */
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
  '--banner-accent-strong': theme.value.primaryDark,
  '--banner-text': theme.value.text,
  '--banner-text-muted': theme.value.textMuted,
  '--banner-overlay': theme.value.surfaceStrong,
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
    class="banner-carousel banner-carousel--themed"
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
              <div class="hero-stars" aria-hidden="true" />
              <div class="hero-pattern" aria-hidden="true" />
              <div class="hero-glow hero-glow--left" aria-hidden="true" />
              <div class="hero-glow hero-glow--right" aria-hidden="true" />
              <div class="hero-content">
                <span class="hero-eyebrow">学习门户</span>
                <h1 class="hero-title">{{ slide.title }}</h1>
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
.banner-carousel--themed {
  position: relative;
  width: 100%;
  height: clamp(200px, 26vw, 300px);
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid var(--banner-border);
  box-shadow: var(--banner-shadow);
  backdrop-filter: blur(8px);
}

.banner-carousel--themed.banner-carousel--compact {
  height: clamp(180px, 24vw, 280px);
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
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.06) 0%, transparent 50%),
    var(--banner-hero);
}

.hero-stars {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(1.2px 1.2px at 12% 24%, rgba(255, 255, 255, 0.55) 0%, transparent 100%),
    radial-gradient(1px 1px at 78% 18%, rgba(165, 243, 252, 0.7) 0%, transparent 100%),
    radial-gradient(1.4px 1.4px at 64% 72%, rgba(255, 255, 255, 0.45) 0%, transparent 100%),
    radial-gradient(1px 1px at 28% 68%, rgba(103, 232, 249, 0.55) 0%, transparent 100%),
    radial-gradient(1px 1px at 88% 58%, rgba(255, 255, 255, 0.35) 0%, transparent 100%),
    radial-gradient(1.2px 1.2px at 42% 38%, rgba(255, 255, 255, 0.4) 0%, transparent 100%);
  opacity: 0.85;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.18;
  background-image:
    linear-gradient(rgba(103, 232, 249, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(103, 232, 249, 0.08) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.5), transparent 90%);
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(64px);
  opacity: 0.42;
}

.hero-glow--left {
  width: 280px;
  height: 280px;
  top: -90px;
  left: -70px;
  background: var(--banner-accent);
}

.hero-glow--right {
  width: 240px;
  height: 240px;
  bottom: -70px;
  right: -50px;
  background: var(--banner-accent-strong);
}

.hero-content {
  position: relative;
  text-align: center;
  color: #fff;
  padding: 0 24px;
}

.hero-eyebrow {
  display: inline-block;
  margin-bottom: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: var(--banner-accent-strong);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(103, 232, 249, 0.24);
}

.hero-title {
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 700;
  letter-spacing: 4px;
  margin: 0 0 12px;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.35);
}

.hero-sub {
  margin: 0;
  font-size: clamp(14px, 2vw, 18px);
  color: var(--banner-text-muted);
  letter-spacing: 2px;
}

.image-slide {
  position: relative;
  width: 100%;
  height: 100%;
  background: #061525;
}

.slide-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
  user-select: none;
  filter: saturate(0.88) brightness(0.82);
}

.image-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(6, 22, 38, 0.72) 0%, rgba(6, 22, 38, 0.18) 42%, rgba(6, 22, 38, 0.55) 100%),
    linear-gradient(180deg, rgba(6, 22, 38, 0.08) 0%, rgba(6, 22, 38, 0.72) 100%);
  pointer-events: none;
}

.image-caption {
  position: absolute;
  left: 24px;
  right: 24px;
  bottom: 52px;
  margin: 0;
  font-size: clamp(16px, 2.4vw, 22px);
  font-weight: 600;
  line-height: 1.5;
  color: var(--banner-text);
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.45);
}

.banner-indicators {
  position: absolute;
  bottom: 18px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 10;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(6, 22, 38, 0.45);
  border: 1px solid rgba(103, 232, 249, 0.16);
  backdrop-filter: blur(6px);
}

.indicator {
  width: 24px;
  height: 4px;
  padding: 0;
  border: none;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.28);
  cursor: pointer;
  transition: width 0.3s ease, background 0.3s ease;
}

.indicator.active {
  width: 36px;
  background: var(--banner-accent-strong);
}

.indicator:hover {
  background: rgba(103, 232, 249, 0.55);
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
  .banner-carousel--themed {
    height: clamp(170px, 44vw, 240px);
  }

  .hero-title {
    letter-spacing: 2px;
  }

  .hero-sub {
    letter-spacing: 1px;
  }

  .image-caption {
    left: 16px;
    right: 16px;
    bottom: 46px;
    font-size: 15px;
  }

  .banner-indicators {
    bottom: 12px;
  }

  .indicator {
    width: 18px;
  }

  .indicator.active {
    width: 28px;
  }
}
</style>
