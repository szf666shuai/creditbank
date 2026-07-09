<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { homeBannerSlides, BANNER_AUTOPLAY_MS } from '@/config/home-banner'

const router = useRouter()
const slides = homeBannerSlides
const currentIndex = ref(0)
const isPaused = ref(false)

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
              <div class="hero-pattern" />
              <div class="hero-glow hero-glow--left" />
              <div class="hero-glow hero-glow--right" />
              <div class="hero-content">
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
  /* 限制高度，避免占满整屏；超宽屏封顶后用 cover 铺满宽度 */
  height: clamp(240px, 38vw, 480px);
  overflow: hidden;
  background: linear-gradient(135deg, #2094f3 0%, #1a6fc4 50%, #0d4d8a 100%);
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

/* 统领图 */
.hero-slide {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2094f3 0%, #1a6fc4 45%, #0d4d8a 100%);
  overflow: hidden;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.35;
}

.hero-glow--left {
  width: 320px;
  height: 320px;
  top: -80px;
  left: -60px;
  background: #5eb8ff;
}

.hero-glow--right {
  width: 280px;
  height: 280px;
  bottom: -60px;
  right: -40px;
  background: #13c2c2;
}

.hero-content {
  position: relative;
  text-align: center;
  color: var(--color-white);
  padding: 0 24px;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  letter-spacing: 6px;
  margin-bottom: 16px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.25);
}

.hero-sub {
  font-size: 18px;
  opacity: 0.9;
  letter-spacing: 3px;
}

/* 图片轮播 — 容器与图片同比例，cover 铺满全宽 */
.image-slide {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #2094f3 0%, #1a6fc4 50%, #0d4d8a 100%);
}

.slide-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
  user-select: none;
}

/* 底部指示条 — 参考学银在线风格 */
.banner-indicators {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 10;
}

.indicator {
  width: 28px;
  height: 4px;
  padding: 0;
  border: none;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition: width 0.3s ease, background 0.3s ease;
}

.indicator.active {
  width: 40px;
  background: #fff;
}

.indicator:hover {
  background: rgba(255, 255, 255, 0.75);
}

/* 淡入淡出 */
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
    height: clamp(200px, 48vw, 300px);
  }

  .hero-title {
    font-size: 26px;
    letter-spacing: 3px;
  }

  .hero-sub {
    font-size: 14px;
    letter-spacing: 1px;
  }

  .banner-indicators {
    bottom: 16px;
  }

  .indicator {
    width: 20px;
  }

  .indicator.active {
    width: 32px;
  }
}
</style>
