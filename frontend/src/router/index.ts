import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import HomeView from '@/views/HomeView.vue'
import PlaceholderView from '@/views/PlaceholderView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ProfileView from '@/views/ProfileView.vue'
import SearchView from '@/views/SearchView.vue'
import CreditMallView from '@/views/CreditMallView.vue'
import LearningResourcesView from '@/views/LearningResourcesView.vue'
import LearningArchiveView from '@/views/LearningArchiveView.vue'
import MallProductDetailView from '@/views/MallProductDetailView.vue'

const placeholder = (title: string) => ({
  component: PlaceholderView,
  meta: { title },
})

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', name: 'home', component: HomeView },
        { path: 'courses', ...placeholder('课程') },
        { path: 'credit', name: 'credit', component: CreditMallView },
        { path: 'credit/products/:productId', name: 'mall-product-detail', component: MallProductDetailView },
        { path: 'resources', name: 'resources', component: LearningResourcesView },
        { path: 'archive', name: 'archive', component: LearningArchiveView, meta: { requiresAuth: true } },
        { path: 'achievement', ...placeholder('学习成果') },
        { path: 'forum', ...placeholder('论坛') },
        { path: 'news', ...placeholder('资讯中心') },
        { path: 'enterprise', ...placeholder('企业中心') },
        { path: 'organization', ...placeholder('机构加盟') },
        { path: 'about', ...placeholder('关于我们') },
        { path: 'activity', ...placeholder('活动报名') },
        { path: 'job', ...placeholder('招聘求职') },
        { path: 'integrity', ...placeholder('诚信评定') },
        { path: 'partners', ...placeholder('合作单位') },
        { path: 'contact', ...placeholder('联系我们') },
        { path: 'search', name: 'search', component: SearchView },
        { path: 'login', name: 'login', component: LoginView, meta: { guest: true } },
        { path: 'register', name: 'register', component: RegisterView, meta: { guest: true } },
        { path: 'profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },
        { path: 'help/:type', ...placeholder('帮助中心') },
      ],
    },
  ],
  scrollBehavior(to) {
    if (to.hash) {
      return {
        el: to.hash,
        top: 96,
        behavior: 'smooth',
      }
    }
    return { top: 0 }
  },
})

router.beforeEach(async (to) => {
  const { useAuthStore } = await import('@/stores/auth')
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guest && authStore.isLoggedIn) {
    return { path: '/profile' }
  }
})

export default router
