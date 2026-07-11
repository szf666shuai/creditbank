import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import ProfileLayout from '@/layouts/ProfileLayout.vue'
import HomeView from '@/views/HomeView.vue'
import PlaceholderView from '@/views/PlaceholderView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import SearchView from '@/views/SearchView.vue'
import ForumView from '@/views/ForumView.vue'
import InformationView from '@/views/InformationView.vue'
import CreditMallView from '@/views/CreditMallView.vue'
import LearningResourcesView from '@/views/LearningResourcesView.vue'
import LearningArchiveView from '@/views/LearningArchiveView.vue'
import MallProductDetailView from '@/views/MallProductDetailView.vue'
import EnterpriseIndexView from '@/views/enterprise/IndexView.vue'
import EnterpriseDetailView from '@/views/enterprise/DetailView.vue'

import ProfileEnterpriseJobsView from '@/views/profile/enterprise/JobsView.vue'
import ProfileEnterpriseActivitiesView from '@/views/profile/enterprise/ActivitiesView.vue'
import ProfileEnterpriseApplicationsView from '@/views/profile/enterprise/ApplicationsView.vue'
import ProfileEnterpriseIndexView from '@/views/profile/enterprise/IndexView.vue'
import ProfileEnterpriseInterviewsView from '@/views/profile/enterprise/InterviewsView.vue'
import ProfileEnterpriseActivityInvitationsView from '@/views/profile/enterprise/ActivityInvitationsView.vue'
import ProfileEnterpriseOrgView from '@/views/profile/enterprise/OrgView.vue'
import ProfileEnterpriseMaterialsView from '@/views/profile/enterprise/MaterialsView.vue'
import ProfileIndexView from '@/views/profile/IndexView.vue'
import ProfileResumeView from '@/views/profile/ResumeView.vue'
import ProfileLearningView from '@/views/profile/LearningView.vue'
import ProfileCreditView from '@/views/profile/CreditView.vue'
import ProfileIntegrityView from '@/views/profile/IntegrityView.vue'
import ProfilePostsView from '@/views/profile/PostsView.vue'
import ProfileApplicationsView from '@/views/profile/ApplicationsView.vue'
import ProfileMessagesView from '@/views/profile/MessagesView.vue'
import ProfileMessageDetailView from '@/views/profile/MessageDetailView.vue'
import ProfileInterviewsView from '@/views/profile/InterviewsView.vue'
import ProfileActivitiesView from '@/views/profile/ActivitiesView.vue'
import ProfileResumeEditorView from '@/views/profile/ResumeEditorView.vue'
import ProfileAdminIndexView from '@/views/profile/admin/IndexView.vue'
import ProfileAdminOrganizationsView from '@/views/profile/admin/OrganizationsView.vue'
import ProfileAdminUsersView from '@/views/profile/admin/UsersView.vue'
import ProfileAdminNotificationsView from '@/views/profile/admin/NotificationsView.vue'
import ProfileAdminReportsView from '@/views/profile/admin/ReportsView.vue'
import ProfileAdminIntegrityView from '@/views/profile/admin/IntegrityView.vue'
import ProfileAdminCreditView from '@/views/profile/admin/CreditView.vue'
import ProfileAdminJobsView from '@/views/profile/admin/JobsView.vue'
import ProfileAdminActivitiesView from '@/views/profile/admin/ActivitiesView.vue'

import {
  canAccessPath,
  getDefaultHomePath,
} from '@/config/role-routes'
import { ROLE_STUDENT } from '@/types/auth'

const placeholder = (title: string) => ({
  component: PlaceholderView,
  meta: { title },
})

const authRoute = { requiresAuth: true as const }
const studentRoute = { requiresAuth: true as const, requiresStudent: true as const }
const enterpriseRoute = { requiresAuth: true as const, requiresEnterprise: true as const }
const adminRoute = { requiresAuth: true as const, requiresAdmin: true as const }

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
        { path: 'forum', name: 'forum', component: ForumView, meta: { title: '论坛' } },
        { path: 'news', name: 'news', component: InformationView, meta: { title: '资讯中心' } },
        { path: 'organization', redirect: '/register' },
        { path: 'about', ...placeholder('关于我们') },
        { path: 'activity', ...placeholder('活动报名') },
        { path: 'job', ...placeholder('招聘求职') },
        { path: 'integrity', ...placeholder('诚信评定') },
        { path: 'partners', ...placeholder('合作单位') },
        { path: 'contact', ...placeholder('联系我们') },
        { path: 'search', name: 'search', component: SearchView },
        { path: 'login', name: 'login', component: LoginView, meta: { guest: true } },
        { path: 'register', name: 'register', component: RegisterView, meta: { guest: true } },

        // 企业中心（公开浏览）
        {
          path: 'enterprise',
          name: 'enterprise',
          component: EnterpriseIndexView,
          meta: { title: '加盟企业' },
        },
        {
          path: 'enterprise/org',
          redirect: '/profile/enterprise/org',
        },
        {
          path: 'enterprise/materials',
          redirect: '/profile/enterprise/materials',
        },
        {
          path: 'enterprise/applications',
          redirect: '/profile/enterprise/applications',
        },
        {
          path: 'enterprise/interviews',
          redirect: '/profile/enterprise/interviews',
        },
        {
          path: 'enterprise/activity-invitations',
          redirect: '/profile/enterprise/activity-invitations',
        },
        {
          path: 'enterprise/:id',
          name: 'enterprise-detail',
          component: EnterpriseDetailView,
          meta: { title: '企业详情' },
        },

        // 个人中心（左侧菜单布局）
        {
          path: 'profile',
          component: ProfileLayout,
          meta: { ...authRoute },
          children: [
            {
              path: '',
              name: 'profile',
              component: ProfileIndexView,
              meta: { title: '个人概览' },
            },
            {
              path: 'resume',
              name: 'profile-resume',
              component: ProfileResumeView,
              meta: { title: '我的简历', ...studentRoute },
            },
            {
              path: 'resume/new',
              name: 'profile-resume-new',
              component: ProfileResumeEditorView,
              meta: { title: '新建简历', ...studentRoute },
            },
            {
              path: 'resume/:id',
              name: 'profile-resume-edit',
              component: ProfileResumeEditorView,
              meta: { title: '编辑简历', ...studentRoute },
            },
            {
              path: 'learning-stats',
              redirect: '/profile/learning',
            },
            {
              path: 'learning',
              name: 'profile-learning',
              component: ProfileLearningView,
              meta: { title: '学习档案', ...studentRoute },
            },
            {
              path: 'credit',
              name: 'profile-credit',
              component: ProfileCreditView,
              meta: { title: '学分流水', ...studentRoute },
            },
            {
              path: 'integrity',
              name: 'profile-integrity',
              component: ProfileIntegrityView,
              meta: { title: '诚信评定', ...studentRoute },
            },
            {
              path: 'posts',
              name: 'profile-posts',
              component: ProfilePostsView,
              meta: { title: '我的发言', ...studentRoute },
            },
            {
              path: 'applications',
              name: 'profile-applications',
              component: ProfileApplicationsView,
              meta: { title: '投递记录', ...studentRoute },
            },
            {
              path: 'messages',
              name: 'profile-messages',
              component: ProfileMessagesView,
              meta: { title: '消息中心' },
            },
            {
              path: 'messages/:id',
              name: 'profile-message-detail',
              component: ProfileMessageDetailView,
              meta: { title: '消息详情' },
            },
            {
              path: 'interviews',
              name: 'profile-interviews',
              component: ProfileInterviewsView,
              meta: { title: '面试邀请', ...studentRoute },
            },
            {
              path: 'activities',
              name: 'profile-activities',
              component: ProfileActivitiesView,
              meta: { title: '我的活动', ...studentRoute },
            },
            {
              path: 'activity-registrations',
              redirect: { path: '/profile/activities', query: { tab: 'registrations' } },
            },
            {
              path: 'activity-invitations',
              redirect: { path: '/profile/activities', query: { tab: 'invitations' } },
            },
            {
              path: 'admin',
              name: 'profile-admin',
              component: ProfileAdminIndexView,
              meta: { title: '管理概览', ...adminRoute },
            },
            {
              path: 'admin/organizations',
              name: 'profile-admin-organizations',
              component: ProfileAdminOrganizationsView,
              meta: { title: '机构加盟', ...adminRoute },
            },
            {
              path: 'admin/users',
              name: 'profile-admin-users',
              component: ProfileAdminUsersView,
              meta: { title: '用户管理', ...adminRoute },
            },
            {
              path: 'admin/notifications',
              name: 'profile-admin-notifications',
              component: ProfileAdminNotificationsView,
              meta: { title: '系统通知', ...adminRoute },
            },
            {
              path: 'admin/reports',
              name: 'profile-admin-reports',
              component: ProfileAdminReportsView,
              meta: { title: '举报处理', ...adminRoute },
            },
            {
              path: 'admin/integrity',
              name: 'profile-admin-integrity',
              component: ProfileAdminIntegrityView,
              meta: { title: '诚信监察', ...adminRoute },
            },
            {
              path: 'admin/credit',
              name: 'profile-admin-credit',
              component: ProfileAdminCreditView,
              meta: { title: '学分监察', ...adminRoute },
            },
            {
              path: 'admin/jobs',
              name: 'profile-admin-jobs',
              component: ProfileAdminJobsView,
              meta: { title: '招聘监管', ...adminRoute },
            },
            {
              path: 'admin/activities',
              name: 'profile-admin-activities',
              component: ProfileAdminActivitiesView,
              meta: { title: '活动监管', ...adminRoute },
            },
            {
              path: 'enterprise',
              name: 'profile-enterprise',
              component: ProfileEnterpriseIndexView,
              meta: { title: '企业工作台', ...enterpriseRoute },
            },
            {
              path: 'enterprise/jobs',
              name: 'profile-enterprise-jobs',
              component: ProfileEnterpriseJobsView,
              meta: { title: '招聘管理', ...enterpriseRoute },
            },
            {
              path: 'enterprise/activities',
              name: 'profile-enterprise-activities',
              component: ProfileEnterpriseActivitiesView,
              meta: { title: '活动管理', ...enterpriseRoute },
            },
            {
              path: 'enterprise/applications',
              name: 'profile-enterprise-applications',
              component: ProfileEnterpriseApplicationsView,
              meta: { title: '投递管理', ...enterpriseRoute },
            },
            {
              path: 'enterprise/interviews',
              name: 'profile-enterprise-interviews',
              component: ProfileEnterpriseInterviewsView,
              meta: { title: '面试邀请', ...enterpriseRoute },
            },
            {
              path: 'enterprise/activity-invitations',
              name: 'profile-enterprise-activity-invitations',
              component: ProfileEnterpriseActivityInvitationsView,
              meta: { title: '活动邀请', ...enterpriseRoute },
            },
            {
              path: 'enterprise/org',
              name: 'profile-enterprise-org',
              component: ProfileEnterpriseOrgView,
              meta: { title: '机构信息', ...enterpriseRoute },
            },
            {
              path: 'enterprise/materials',
              name: 'profile-enterprise-materials',
              component: ProfileEnterpriseMaterialsView,
              meta: { title: '企业资料', ...enterpriseRoute },
            },
          ],
        },

        // 兼容旧消息中心路径
        { path: 'messages', redirect: '/profile/messages' },
        { path: 'messages/:id', redirect: (to) => `/profile/messages/${to.params.id}` },

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

  if (to.meta.requiresAuth || to.meta.guest) {
    if (authStore.isLoggedIn && !authStore.userInfo) {
      await authStore.fetchUserInfo()
    }
  }

  const role = authStore.userInfo?.role ?? ROLE_STUDENT

  if (to.meta.requiresAuth && authStore.isLoggedIn && !canAccessPath(role, to.path)) {
    return { path: getDefaultHomePath(role) }
  }

  if (to.meta.requiresStudent && !authStore.isStudent) {
    return { path: getDefaultHomePath(role) }
  }
  if (to.meta.requiresEnterprise && !authStore.isEnterprise) {
    return { path: getDefaultHomePath(role) }
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { path: getDefaultHomePath(role) }
  }

  if (to.meta.guest && authStore.isLoggedIn) {
    return { path: getDefaultHomePath(role) }
  }
})

export default router
