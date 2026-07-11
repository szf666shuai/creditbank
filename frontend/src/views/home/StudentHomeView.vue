<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import HomeBannerCarousel from '@/components/home/HomeBannerCarousel.vue'
import StudentLearningStrip from '@/components/home/StudentLearningStrip.vue'
import StudentTodoStrip from '@/components/home/StudentTodoStrip.vue'
import HomeContentSections from '@/components/home/HomeContentSections.vue'
import { BRAND_NAME, BRAND_SLOGAN } from '@/config/brand'

const router = useRouter()
const authStore = useAuthStore()

const isStudentPortal = computed(() => authStore.isLoggedIn && authStore.isStudent)

function go(path: string) {
  router.push(path)
}

onMounted(async () => {
  if (authStore.isLoggedIn && !authStore.userInfo) {
    await authStore.fetchUserInfo()
  }
})
</script>

<template>
  <div class="student-home">
    <div class="student-home-content">
      <section class="banner-section">
        <HomeBannerCarousel compact />
      </section>

      <div class="portal-inner">
        <section v-if="!isStudentPortal" class="guest-welcome">
          <div class="guest-card">
            <span class="guest-badge">{{ BRAND_NAME }}</span>
            <h2>开启你的 {{ BRAND_NAME }} 学习之旅</h2>
            <p>登录后可查看学习进度、打卡记录、面试与活动待办。{{ BRAND_SLOGAN }}。</p>
            <div class="guest-actions">
              <el-button type="primary" size="large" @click="go('/login')">登录</el-button>
              <el-button size="large" plain @click="go('/register')">注册账号</el-button>
              <el-button size="large" text @click="go('/resources')">先看看课程</el-button>
            </div>
          </div>
        </section>

        <section v-else class="portal-stack">
          <StudentLearningStrip />
          <StudentTodoStrip />
        </section>
      </div>

      <HomeContentSections />
    </div>
  </div>
</template>

<style scoped>
.student-home {
  --role-primary: #00a1d6;
  --role-primary-dark: #67e8f9;
  --role-primary-soft: #22d3ee;
  --role-surface: rgba(6, 22, 38, 0.58);
  --role-surface-strong: rgba(8, 30, 52, 0.74);
  --role-surface-card: rgba(10, 36, 58, 0.68);
  --role-border: rgba(0, 161, 214, 0.28);
  --role-text: #e8f8ff;
  --role-text-muted: #8ec8de;
  --role-text-on-hero: #fff;
  --role-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);

  position: relative;
  padding-bottom: 48px;
  background: transparent;
  min-height: calc(100vh - var(--header-height));
}

.student-home-content {
  position: relative;
  z-index: 1;
}

.banner-section {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 16px;
}

.portal-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 20px 16px 0;
  display: grid;
  gap: 18px;
}

.guest-card {
  padding: 28px 32px;
  border-radius: 18px;
  color: var(--role-text-on-hero);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, transparent 55%),
    linear-gradient(120deg, #0c4a6e 0%, #0891b2 45%, #00a1d6 78%, #22d3ee 100%);
  box-shadow: var(--role-shadow);
  border: 1px solid rgba(103, 232, 249, 0.28);
  backdrop-filter: blur(12px);
}

.guest-badge {
  display: inline-block;
  margin-bottom: 10px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.24);
}

.guest-card h2 {
  margin: 0 0 10px;
  font-size: 28px;
}

.guest-card p {
  margin: 0 0 18px;
  max-width: 560px;
  line-height: 1.8;
  opacity: 0.92;
}

.guest-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.portal-stack {
  display: grid;
  gap: 16px;
}

.guest-actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, #0891b2, #00a1d6);
  border: none;
}

@media (max-width: 560px) {
  .guest-card {
    padding: 22px 20px;
  }

  .guest-card h2 {
    font-size: 22px;
  }
}
</style>
