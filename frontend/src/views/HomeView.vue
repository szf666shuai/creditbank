<script setup lang="ts">
import { identityRecommends } from '@/config/home-recommendations'
import HomeBannerCarousel from '@/components/home/HomeBannerCarousel.vue'
import HomeContentSections from '@/components/home/HomeContentSections.vue'

const quickEntries = [
  { icon: '💰', label: '积分商城', path: '/credit', color: '#2094f3' },
  { icon: '📁', label: '学习档案', path: '/archive', color: '#52c41a' },
  { icon: '🏆', label: '学习成果', path: '/achievement', color: '#faad14' },
  { icon: '💬', label: '学习论坛', path: '/forum', color: '#722ed1' },
  { icon: '📅', label: '活动报名', path: '/activity', color: '#13c2c2' },
  { icon: '💼', label: '招聘求职', path: '/job', color: '#eb2f96' },
  { icon: '🔗', label: '区块链校验', path: '/achievement', color: '#2f54eb' },
  { icon: '⭐', label: '诚信评定', path: '/integrity', color: '#fa8c16' },
]
</script>

<template>
  <div class="home-page">
    <HomeBannerCarousel />

    <!-- 快捷入口 + 身份推荐 -->
    <section class="feature-section">
      <div class="section-inner">
        <div class="feature-row">
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

    <!-- 精品课程 / 热卖商品 / 热门活动 / 微专业 / 最新热点 / 招聘 / 论坛 / 合作单位 -->
    <HomeContentSections />
  </div>
</template>

<style scoped>
.home-page {
  padding-bottom: 40px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 16px;
}

.feature-section {
  margin-top: 24px;
  position: relative;
  z-index: 1;
}

.feature-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: stretch;
}

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

@media (max-width: 900px) {
  .feature-row {
    grid-template-columns: 1fr;
  }

  .identity-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
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
