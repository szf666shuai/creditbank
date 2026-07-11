import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const TOP_THRESHOLD = 12
const SCROLL_DELTA = 8
const BACK_TO_TOP_THRESHOLD = 320

const isAtTop = ref(true)
const isHeaderVisible = ref(true)
const scrollY = ref(0)

let lastScrollY = 0
let listenerCount = 0

function updateScrollState() {
  const y = window.scrollY
  scrollY.value = y
  isAtTop.value = y <= TOP_THRESHOLD

  if (y <= TOP_THRESHOLD) {
    isHeaderVisible.value = true
  } else if (y - lastScrollY > SCROLL_DELTA) {
    isHeaderVisible.value = false
  } else if (lastScrollY - y > SCROLL_DELTA) {
    isHeaderVisible.value = true
  }

  lastScrollY = y
}

function resetScrollState() {
  lastScrollY = window.scrollY
  scrollY.value = lastScrollY
  isAtTop.value = lastScrollY <= TOP_THRESHOLD
  isHeaderVisible.value = true
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function mountScrollListener() {
  listenerCount++
  if (listenerCount === 1) {
    window.addEventListener('scroll', updateScrollState, { passive: true })
    updateScrollState()
  }
}

function unmountScrollListener() {
  listenerCount--
  if (listenerCount === 0) {
    window.removeEventListener('scroll', updateScrollState)
  }
}

export function useHeaderScroll() {
  const route = useRoute()

  const isHomePage = computed(() => route.name === 'home')
  /** 导航条保持独立实色区域，内容不叠在导航下方 */
  const isTransparent = computed(() => false)
  const showBackToTop = computed(() => scrollY.value > BACK_TO_TOP_THRESHOLD)

  onMounted(mountScrollListener)
  onUnmounted(unmountScrollListener)

  watch(
    () => route.fullPath,
    () => {
      requestAnimationFrame(resetScrollState)
    },
  )

  return {
    isAtTop,
    isHeaderVisible,
    isTransparent,
    isHomePage,
    showBackToTop,
    scrollToTop,
  }
}
