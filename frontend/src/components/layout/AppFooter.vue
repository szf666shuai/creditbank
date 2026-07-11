<script setup lang="ts">
const footerLinks = {
  platform: {
    title: '平台服务',
    items: [
      { label: '课程学习', path: '/courses' },
      { label: '积分商城', path: '/credit' },
      { label: '学习档案', path: '/archive' },
      { label: '学习成果', path: '/achievement' },
    ],
  },
  community: {
    title: '交流互动',
    items: [
      { label: '学习论坛', path: '/forum' },
      { label: '活动报名', path: '/news?type=activity' },
      { label: '招聘求职', path: '/news?type=job' },
      { label: '诚信评定', path: '/integrity' },
    ],
  },
  about: {
    title: '关于平台',
    items: [
      { label: '平台介绍', path: '/about' },
      { label: '机构加盟', path: '/organization' },
      { label: '合作单位', path: '/partners' },
      { label: '联系我们', path: '/contact' },
    ],
  },
  help: {
    title: '帮助中心',
    items: [
      { label: '使用指南', path: '/help/guide' },
      { label: '常见问题', path: '/help/faq' },
      { label: '隐私政策', path: '/help/privacy' },
      { label: '服务条款', path: '/help/terms' },
    ],
  },
}

const contactInfo = {
  phone: '400-888-0000',
  email: 'service@creditbank.edu.cn',
  address: '中国 · 终身学习学分银行平台',
}
</script>

<template>
  <footer class="app-footer">
    <div class="footer-main">
      <div class="footer-inner">
        <!-- 品牌区 -->
        <div class="footer-brand">
          <div class="brand-logo">
            <span class="brand-icon">CB</span>
            <div>
              <div class="brand-name">终身学习学分银行平台</div>
              <div class="brand-slogan">存取学习成果 · 兑换成长价值</div>
            </div>
          </div>
          <p class="brand-desc">
            汇聚机构加盟资源，运用积分管理手段，提升学员终身学习积极性，构建学习型社会支撑体系。
          </p>
          <div class="contact-list">
            <p>客服热线：{{ contactInfo.phone }}</p>
            <p>电子邮箱：{{ contactInfo.email }}</p>
            <p>{{ contactInfo.address }}</p>
          </div>
        </div>

        <!-- 链接列 -->
        <div
          v-for="(section, key) in footerLinks"
          :key="key"
          class="footer-col"
        >
          <h4 class="col-title">{{ section.title }}</h4>
          <ul class="col-links">
            <li v-for="item in section.items" :key="item.path">
              <router-link :to="item.path">{{ item.label }}</router-link>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 底部版权栏 -->
    <div class="footer-bottom">
      <div class="footer-bottom-inner">
        <p class="copyright">
          © 2026 终身学习学分银行平台 版权所有
          <span class="divider">|</span>
          京ICP备XXXXXXXX号
        </p>
        <div class="bottom-links">
          <router-link to="/help/privacy">隐私政策</router-link>
          <router-link to="/help/terms">服务条款</router-link>
          <router-link to="/about">关于我们</router-link>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  margin-top: auto;
}

.footer-main {
  background: var(--footer-bg);
  padding: 48px 16px 40px;
}

.footer-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  gap: 32px;
}

.footer-brand {
  padding-right: 24px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: var(--color-primary);
  border-radius: 8px;
  color: var(--color-white);
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.brand-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-white);
  margin-bottom: 4px;
}

.brand-slogan {
  font-size: 12px;
  color: var(--footer-text);
}

.brand-desc {
  font-size: 13px;
  color: var(--footer-text);
  line-height: 1.8;
  margin-bottom: 16px;
}

.contact-list p {
  font-size: 13px;
  color: var(--footer-text);
  line-height: 2;
  margin: 0;
}

.footer-col {
  min-width: 0;
}

.col-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-white);
  margin-bottom: 16px;
}

.col-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.col-links li {
  margin-bottom: 10px;
}

.col-links a {
  font-size: 13px;
  color: var(--footer-text);
  text-decoration: none;
  transition: color 0.2s;
}

.col-links a:hover {
  color: var(--color-primary);
}

.footer-bottom {
  background: #1a252f;
  padding: 16px;
}

.footer-bottom-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.copyright {
  font-size: 12px;
  color: var(--footer-text);
  margin: 0;
}

.divider {
  margin: 0 8px;
  opacity: 0.4;
}

.bottom-links {
  display: flex;
  gap: 20px;
}

.bottom-links a {
  font-size: 12px;
  color: var(--footer-text);
  text-decoration: none;
  transition: color 0.2s;
}

.bottom-links a:hover {
  color: var(--color-primary);
}

@media (max-width: 900px) {
  .footer-inner {
    grid-template-columns: 1fr 1fr;
  }

  .footer-brand {
    grid-column: 1 / -1;
  }
}

@media (max-width: 480px) {
  .footer-inner {
    grid-template-columns: 1fr;
  }
}
</style>
