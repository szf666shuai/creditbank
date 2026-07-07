import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import HomeView from '@/views/HomeView.vue'
import PlaceholderView from '@/views/PlaceholderView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ProfileView from '@/views/ProfileView.vue'

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
        { path: 'credit', ...placeholder('积分商城') },
        { path: 'resources', ...placeholder('学习资源') },
        { path: 'archive', ...placeholder('学习档案') },
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
        { path: 'search', ...placeholder('搜索结果') },
        { path: 'login', name: 'login', component: LoginView, meta: { guest: true } },
        { path: 'register', name: 'register', component: RegisterView, meta: { guest: true } },
        { path: 'profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },
        { path: 'help/:type', ...placeholder('帮助中心') },
      ],
    },
  ],
  scrollBehavior() {
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
