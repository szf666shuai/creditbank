<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  BRAND_EMAIL,
  BRAND_NAME,
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
  '--footer-accent-strong': footerTheme.value.primaryDark,
  '--footer-border': footerTheme.value.border,
  '--footer-text': footerTheme.value.textMuted,
  '--footer-text-strong': footerTheme.value.text,
  '--footer-surface': footerTheme.value.surfaceStrong,
  '--footer-bg-top': footerTheme.value.particleBg[0],
  '--footer-bg-mid': footerTheme.value.particleBg[1],
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
    <div class="footer-glow" aria-hidden="true" />

    <div class="footer-main">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="brand-logo">
            <BrandLogo :size="40" :variant="footerVariant" />
            <div>
              <div class="brand-name">{{ BRAND_NAME }}</div>
              <div class="brand-slogan">{{ BRAND_SLOGAN }}</div>
            </div>
          </div>
          <a class="brand-email" :href="`mailto:${BRAND_EMAIL}`">{{ BRAND_EMAIL }}</a>
        </div>

        <nav class="footer-nav" :aria-label="footerNavLabel">
          <router-link
            v-for="item in quickLinks"
            :key="item.path"
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

    <div class="footer-bottom">
      <div class="footer-bottom-inner">
        <p class="copyright">© 2026 {{ BRAND_NAME }}</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  position: relative;
  margin-top: auto;
  border-top: 1px solid var(--footer-border);
  background: linear-gradient(180deg, transparent 0%, var(--footer-bg-top) 18%, var(--footer-bg-mid) 100%);
}

.footer-glow {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: min(720px, 90%);
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    var(--footer-accent) 50%,
    transparent 100%
  );
  opacity: 0.65;
}

.footer-main {
  padding: 32px 16px 28px;
}

.footer-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.footer-brand {
  min-width: 0;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--footer-text-strong);
  letter-spacing: 2px;
  margin-bottom: 2px;
}

.brand-slogan {
  font-size: 12px;
  color: var(--footer-accent-strong);
  letter-spacing: 1px;
}

.brand-email {
  font-size: 13px;
  color: var(--footer-text);
  text-decoration: none;
  transition: color 0.2s;
}

.brand-email:hover {
  color: var(--footer-accent-strong);
}

.footer-nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 18px;
}

.footer-nav-link {
  font-size: 13px;
  color: var(--footer-text);
  text-decoration: none;
  transition: color 0.2s;
}

.footer-nav-link:hover {
  color: var(--footer-accent-strong);
}

.footer-nav-link.router-link-active {
  color: var(--footer-accent-strong);
}

.footer-reminder-btn {
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  font-family: inherit;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.22);
  padding: 14px 16px;
}

.footer-bottom-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.copyright {
  font-size: 12px;
  color: var(--footer-text);
  margin: 0;
}

@media (max-width: 768px) {
  .footer-inner {
    flex-direction: column;
    align-items: flex-start;
  }

  .footer-nav {
    gap: 8px 14px;
  }
}
</style>
