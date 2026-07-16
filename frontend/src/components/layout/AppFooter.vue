<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  BRAND_EMAIL,
  BRAND_NAME,
  BRAND_NAME_EN,
  BRAND_SLOGAN,
} from '@/config/brand'
import { getRoleTheme, resolveHeaderThemeVariant } from '@/config/role-theme'
import { getFooterLinksForRole, getFooterNavLabel } from '@/config/footer-links'
import BrandLogo from '@/components/brand/BrandLogo.vue'
import {
  disableGuestDailyReminder,
  enableGuestDailyReminder,
  isGuestDailyReminderEnabled,
} from '@/utils/guest-daily-reminder'

const route = useRoute()
const authStore = useAuthStore()

const footerVariant = computed(() =>
  resolveHeaderThemeVariant({
    isHomePage: route.name === 'home',
    isLoggedIn: authStore.isLoggedIn,
    isStudent: authStore.isStudent,
    isEnterprise: authStore.isEnterprise,
    isAdmin: authStore.isAdmin,
  }),
)

const footerTheme = computed(() => getRoleTheme(footerVariant.value))

const footerStyle = computed(() => ({
  '--footer-accent': footerTheme.value.primarySoft,
  '--footer-accent-strong': footerTheme.value.primary,
  '--footer-border': footerTheme.value.border,
  '--footer-text': footerTheme.value.textMuted,
  '--footer-text-strong': footerTheme.value.text,
  '--footer-surface': footerTheme.value.surfaceStrong,
}))

const quickLinks = computed(() =>
  getFooterLinksForRole({
    isLoggedIn: authStore.isLoggedIn,
    role: authStore.userInfo?.role,
  }),
)

const footerNavLabel = computed(() =>
  getFooterNavLabel(authStore.userInfo?.role, authStore.isLoggedIn),
)

const guestReminderEnabled = computed(() => isGuestDailyReminderEnabled())

const courseLinks = [
  { label: '课程资源', path: '/resources' },
  { label: '资讯动态', path: '/news' },
  { label: '学习社区', path: '/forum' },
]

const companyLinks = [
  { label: '企业机构', path: '/enterprise' },
  { label: '秩点商城', path: '/profile/credit' },
  { label: '机构入驻', path: '/register' },
]

const supportLinks = computed(() => {
  const base = [...quickLinks.value]
  if (!authStore.isLoggedIn) {
    return [
      { label: '登录', path: '/login' },
      { label: '免费注册', path: '/register' },
      ...base.filter((l) => !['/resources', '/credit', '/enterprise', '/register'].includes(l.path)),
    ]
  }
  return base
})

async function toggleGuestReminder() {
  try {
    if (guestReminderEnabled.value) {
      disableGuestDailyReminder()
      ElMessage.info('已关闭每日桌面提醒')
      return
    }
    await enableGuestDailyReminder()
    ElMessage.success('已开启每日桌面提醒：新开浏览器时会弹出系统通知')
  } catch (e) {
    ElMessage.warning(e instanceof Error ? e.message : '开启提醒失败')
  }
}
</script>

<template>
  <footer class="app-footer" :style="footerStyle">
    <div class="footer-main">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="brand-logo">
            <div class="brand-logo-mark">
              <BrandLogo :size="36" :variant="footerVariant" />
            </div>
            <div>
              <div class="brand-name">{{ BRAND_NAME }}</div>
              <div class="brand-en">{{ BRAND_NAME_EN }}</div>
            </div>
          </div>
          <p class="brand-slogan">{{ BRAND_SLOGAN }}</p>
          <a class="brand-email" :href="`mailto:${BRAND_EMAIL}`">{{ BRAND_EMAIL }}</a>
          <div class="social-row" aria-hidden="true">
            <span class="social-box">学</span>
            <span class="social-box">档</span>
            <span class="social-box">证</span>
            <span class="social-box">商</span>
          </div>
        </div>

        <div class="footer-cols">
          <nav class="footer-col" aria-label="课程">
            <h4 class="col-title">课程</h4>
            <router-link
              v-for="item in courseLinks"
              :key="item.path"
              :to="item.path"
              class="footer-nav-link"
            >
              {{ item.label }}
            </router-link>
          </nav>

          <nav class="footer-col" aria-label="机构与服务">
            <h4 class="col-title">服务</h4>
            <router-link
              v-for="item in companyLinks"
              :key="item.path"
              :to="item.path"
              class="footer-nav-link"
            >
              {{ item.label }}
            </router-link>
          </nav>

          <nav class="footer-col" :aria-label="footerNavLabel">
            <h4 class="col-title">快捷入口</h4>
            <router-link
              v-for="item in supportLinks"
              :key="item.path + item.label"
              :to="item.path"
              class="footer-nav-link"
            >
              {{ item.label }}
            </router-link>
            <button
              v-if="!authStore.isLoggedIn"
              type="button"
              class="footer-nav-link footer-reminder-btn"
              @click="toggleGuestReminder"
            >
              {{ guestReminderEnabled ? '关闭每日提醒' : '开启每日学习提醒' }}
            </button>
          </nav>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="footer-bottom-inner">
        <p class="copyright">© 2026 {{ BRAND_NAME }} · {{ BRAND_NAME_EN }}</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  position: relative;
  margin-top: auto;
  border-top: 2.5px solid var(--nb-ink);
  background: var(--nb-cream);
}

.footer-main {
  padding: 48px 20px 36px;
}

.footer-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(220px, 1.2fr) 2fr;
  gap: 40px 48px;
  align-items: start;
}

.footer-brand {
  min-width: 0;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.brand-logo-mark {
  width: 44px;
  height: 44px;
  border: 2.5px solid var(--nb-ink);
  border-radius: 14px;
  overflow: hidden;
  background: var(--nb-pink);
  box-shadow: var(--nb-shadow-sm);
  display: grid;
  place-items: center;
}

.brand-name {
  font-family: var(--font-heading);
  font-size: 1.15rem;
  font-weight: 900;
  color: var(--nb-ink);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.brand-en {
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--footer-text);
  margin-top: 2px;
}

.brand-slogan {
  font-size: 0.875rem;
  color: var(--footer-text);
  line-height: 1.55;
  margin: 0 0 12px;
  max-width: 280px;
}

.brand-email {
  display: inline-block;
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--nb-ink);
  text-decoration: none;
  margin-bottom: 16px;
}

.brand-email:hover {
  color: var(--nb-green-deep);
}

.social-row {
  display: flex;
  gap: 10px;
}

.social-box {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  background: #fff;
  border: 2.5px solid var(--nb-ink);
  border-radius: 12px;
  box-shadow: var(--nb-shadow-sm);
  font-family: var(--font-heading);
  font-weight: 900;
  font-size: 0.8rem;
  color: var(--nb-ink);
}

.footer-cols {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.col-title {
  margin: 0 0 4px;
  font-family: var(--font-heading);
  font-size: 0.95rem;
  font-weight: 900;
  color: var(--nb-ink);
}

.footer-nav-link {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--footer-text);
  text-decoration: none;
  transition: color 0.15s ease;
  cursor: pointer;
  width: fit-content;
}

.footer-nav-link:hover {
  color: var(--nb-green-deep);
}

.footer-nav-link.router-link-active {
  color: var(--nb-ink);
  font-weight: 800;
}

.footer-reminder-btn {
  border: none;
  background: transparent;
  padding: 0;
  text-align: left;
  font: inherit;
}

.footer-bottom {
  border-top: 2px solid color-mix(in srgb, var(--nb-ink) 18%, transparent);
  padding: 16px 20px;
}

.footer-bottom-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.copyright {
  margin: 0;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--footer-text);
}

@media (max-width: 860px) {
  .footer-inner {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .footer-cols {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 520px) {
  .footer-cols {
    grid-template-columns: 1fr;
  }
}
</style>
