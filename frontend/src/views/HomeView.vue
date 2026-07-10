<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { identityRecommends } from '@/config/home-recommendations'

const quickEntries = [
  { icon: '💰', label: '积分商城', path: '/credit', color: '#2094f3' },
  { icon: '📁', label: '学习档案', path: '/profile/learning', color: '#52c41a' },
  { icon: '🏆', label: '学习成果', path: '/achievement', color: '#faad14' },
  { icon: '💬', label: '学习论坛', path: '/forum', color: '#722ed1' },
  { icon: '📅', label: '活动报名', path: '/enterprise', color: '#13c2c2' },
  { icon: '💼', label: '招聘求职', path: '/enterprise', color: '#eb2f96' },
  { icon: '🔗', label: '区块链校验', path: '/achievement', color: '#2f54eb' },
  { icon: '⭐', label: '诚信评定', path: '/profile/integrity', color: '#fa8c16' },
]

interface HealthData {
  status: string
  application: string
  version: string
}

const health = ref<HealthData | null>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const res = await request.get<HealthData>('/health')
    if (res.code === 200) {
      health.value = res.data
    }
  } catch {
    error.value = '后端服务未启动'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home-page">
    <!-- Banner -->
    <section class="banner">
      <div class="banner-content">
        <h1 class="banner-title">终身学习 · 学分银行</h1>
        <p class="banner-sub">存取学习成果，兑换成长价值</p>
      </div>
      <div class="banner-dots">
        <span class="dot active" />
        <span class="dot" />
        <span class="dot" />
      </div>
    </section>

    <!-- 快捷入口 + 身份推荐 -->
    <section class="feature-section">
      <div class="section-inner">
        <div class="feature-row">
          <!-- 左侧：快捷入口 -->
          <div class="feature-left">
            <div class="quick-grid">
              <router-link
                v-for="item in quickEntries"
                :key="item.label"
                :to="item.path"
                class="quick-item"
              >
                <span
                  class="quick-icon"
                  :style="{ background: item.color + '18', color: item.color }"
                >
                  {{ item.icon }}
                </span>
                <span class="quick-label">{{ item.label }}</span>
              </router-link>
            </div>
          </div>

          <!-- 右侧：身份分类推荐 -->
          <div class="feature-right">
            <div class="identity-grid">
              <div
                v-for="item in identityRecommends"
                :key="item.title"
                class="identity-card"
              >
                <div class="identity-header">
                  <span
                    class="identity-icon"
                    :style="{ background: item.color + '18', color: item.color }"
                  >
                    {{ item.icon }}
                  </span>
                  <span class="identity-title">{{ item.title }}</span>
                </div>
                <div class="identity-tags">
                  <router-link
                    v-for="tag in item.tags"
                    :key="tag.label"
                    :to="tag.path"
                    class="identity-tag"
                  >
                    {{ tag.label }}
                  </router-link>
                  <router-link :to="item.morePath" class="identity-more">
                    查看更多 &gt;
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 系统状态（开发阶段） -->
    <section class="status-section">
      <div class="section-inner">
        <div class="section-header">
          <span class="section-icon">📡</span>
          <h2 class="section-title">系统状态</h2>
        </div>
        <div class="status-card" v-loading="loading">
          <el-alert
            v-if="error"
            :title="error"
            type="warning"
            show-icon
            :closable="false"
          />
          <div v-else-if="health" class="status-info">
            <span>应用：{{ health.application }}</span>
            <span>版本：{{ health.version }}</span>
            <el-tag type="success" size="small">{{ health.status }}</el-tag>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  padding-bottom: 40px;
}

/* Banner */
.banner {
  position: relative;
  height: 320px;
  background: linear-gradient(135deg, #2094f3 0%, #1a6fc4 50%, #0d4d8a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.banner-content {
  position: relative;
  text-align: center;
  color: var(--color-white);
}

.banner-title {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 4px;
  margin-bottom: 12px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.banner-sub {
  font-size: 16px;
  opacity: 0.85;
  letter-spacing: 2px;
}

.banner-dots {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.dot {
  width: 24px;
  height: 3px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 2px;
  cursor: pointer;
}

.dot.active {
  background: var(--color-white);
  width: 32px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 16px;
}

/* 左右分栏 */
.feature-section {
  margin-top: -40px;
  position: relative;
  z-index: 1;
}

.feature-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: stretch;
}

/* 左侧快捷入口 */
.feature-left {
  min-width: 0;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  background: var(--color-white);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 20px 12px;
  height: 100%;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 4px;
  text-decoration: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.quick-item:hover {
  background: var(--color-primary-light);
}

.quick-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  font-size: 22px;
}

.quick-label {
  font-size: 12px;
  color: var(--color-text);
  text-align: center;
  line-height: 1.3;
}

/* 右侧身份推荐 */
.feature-right {
  min-width: 0;
}

.identity-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  height: 100%;
}

.identity-card {
  background: var(--color-white);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.identity-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.identity-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 16px;
  flex-shrink: 0;
}

.identity-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.identity-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px 12px;
  line-height: 1.8;
}

.identity-tag {
  font-size: 13px;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color 0.2s;
  white-space: nowrap;
}

.identity-tag:hover {
  color: var(--color-primary);
}

.identity-more {
  font-size: 13px;
  color: var(--color-primary);
  text-decoration: none;
  white-space: nowrap;
  margin-left: auto;
}

.identity-more:hover {
  opacity: 0.8;
}

/* 状态区 */
.status-section {
  margin-top: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-icon {
  font-size: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

.status-card {
  background: var(--color-white);
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  min-height: 60px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

@media (max-width: 900px) {
  .feature-row {
    grid-template-columns: 1fr;
  }

  .identity-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .banner-title {
    font-size: 24px;
  }

  .quick-grid {
    grid-template-columns: repeat(4, 1fr);
    padding: 16px 8px;
  }
}

@media (max-width: 480px) {
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .identity-grid {
    grid-template-columns: 1fr;
  }
}
</style>
