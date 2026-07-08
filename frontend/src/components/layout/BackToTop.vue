<script setup lang="ts">
import { Top } from '@element-plus/icons-vue'
import { useHeaderScroll } from '@/composables/useHeaderScroll'

const { showBackToTop, scrollToTop } = useHeaderScroll()
</script>

<template>
  <Transition name="back-top-fade">
    <button
      v-show="showBackToTop"
      type="button"
      class="back-to-top"
      aria-label="回到顶部"
      title="回到顶部"
      @click="scrollToTop"
    >
      <el-icon :size="22"><Top /></el-icon>
    </button>
  </Transition>
</template>

<style scoped>
.back-to-top {
  position: fixed;
  right: 24px;
  bottom: 32px;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-white);
  box-shadow: 0 4px 16px rgba(32, 148, 243, 0.45);
  cursor: pointer;
  transition: background 0.2s, transform 0.2s, box-shadow 0.2s;
}

.back-to-top:hover {
  background: var(--color-primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(32, 148, 243, 0.55);
}

.back-to-top-text {
  font-size: 10px;
  line-height: 1;
  letter-spacing: 1px;
}

.back-top-fade-enter-active,
.back-top-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.back-top-fade-enter-from,
.back-top-fade-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

@media (max-width: 768px) {
  .back-to-top {
    right: 16px;
    bottom: 24px;
    width: 44px;
    height: 44px;
  }
}
</style>
