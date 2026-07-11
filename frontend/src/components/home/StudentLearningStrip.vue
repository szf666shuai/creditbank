<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getProfileDashboardApi } from '@/api/profile-dashboard'
import { fetchLearningResources, type LearningResource } from '@/api/learning'
import { getErrorMessage, unwrapApi } from '@/utils/api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const creditBalance = ref(0)
const courses = ref<LearningResource[]>([])

const displayName = computed(
  () => authStore.userInfo?.realName || authStore.userInfo?.username || '学员',
)

const continueCourses = computed(() => {
  const inProgress = courses.value.filter(
    (item) => Number(item.progress || 0) > 0 && Number(item.progress || 0) < 100 && !item.certNo,
  )
  if (inProgress.length) {
    return inProgress
      .sort((a, b) => Number(b.progress || 0) - Number(a.progress || 0))
      .slice(0, 2)
  }
  return courses.value.slice(0, 2)
})

const learningHint = computed(() => {
  const active = courses.value.filter(
    (item) => Number(item.progress || 0) > 0 && Number(item.progress || 0) < 100,
  ).length
  if (active > 0) return `你有 ${active} 门课程正在学习中，坚持打卡可获得秩点奖励`
  return '选一门课程开始今日学习，完成后可打卡领取秩点'
})

function go(path: string) {
  router.push(path)
}

function openCourse(courseId: number) {
  router.push(`/resources/${courseId}`)
}

async function loadData() {
  loading.value = true
  try {
    const [dashboardRes, resourcesRes] = await Promise.all([
      getProfileDashboardApi(),
      fetchLearningResources(),
    ])
    const dashboard = unwrapApi(dashboardRes)
    creditBalance.value = Number(dashboard.creditBalance || 0)
    if (resourcesRes.code === 200 && resourcesRes.data) {
      courses.value = resourcesRes.data
    }
  } catch (e) {
    console.warn(getErrorMessage(e, '学习概览加载失败'))
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <section class="learning-strip">
    <div class="strip-head">
      <div>
        <p class="strip-eyebrow">我的学习</p>
        <h2>你好，{{ displayName }}</h2>
        <p class="strip-hint">{{ learningHint }}</p>
      </div>
      <div class="credit-pill">
        <span>秩点余额</span>
        <strong>{{ creditBalance.toFixed(2) }}</strong>
        <button type="button" class="text-link" @click="go('/profile/credit')">流水</button>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="3" animated />

    <div v-else class="course-row">
      <article
        v-for="course in continueCourses"
        :key="course.id"
        class="course-card"
        @click="openCourse(course.id)"
      >
        <div class="course-cover">
          <img v-if="course.coverUrl" :src="course.coverUrl" :alt="course.title" loading="lazy" />
          <span v-else class="cover-fallback">📚</span>
        </div>
        <div class="course-body">
          <h3>{{ course.title }}</h3>
          <p>{{ course.orgName || '平台课程' }}</p>
          <el-progress
            :percentage="course.progress || 0"
            :stroke-width="8"
            :status="Number(course.progress || 0) >= 80 ? 'success' : undefined"
          />
          <span class="progress-label">
            {{ Number(course.progress || 0) > 0 ? `已学 ${course.progress}%` : '点击开始学习' }}
          </span>
        </div>
      </article>

      <button type="button" class="more-card" @click="go('/resources')">
        <span>浏览全部课程</span>
        <strong>学习资源</strong>
      </button>
    </div>

    <div class="strip-actions">
      <el-button type="primary" plain @click="go('/profile/learning')">学习档案</el-button>
      <el-button plain @click="go('/archive')">学习成果</el-button>
      <el-button
        v-if="continueCourses.length"
        type="success"
        plain
        @click="openCourse(continueCourses[0].id)"
      >
        去学习打卡
      </el-button>
    </div>
  </section>
</template>

<style scoped>
.learning-strip {
  padding: 22px;
  border-radius: 18px;
  background: var(--role-surface, rgba(6, 22, 38, 0.58));
  border: 1px solid var(--role-border, rgba(0, 161, 214, 0.28));
  box-shadow: var(--role-shadow, 0 12px 40px rgba(0, 0, 0, 0.35));
  backdrop-filter: blur(12px);
}

.strip-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.strip-eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: var(--role-primary-soft, #22d3ee);
  text-transform: uppercase;
}

.strip-head h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: var(--role-text, #e8f8ff);
}

.strip-hint {
  margin: 0;
  color: var(--role-text-muted, #8ec8de);
  font-size: 13px;
  line-height: 1.6;
  max-width: 520px;
}

.credit-pill {
  flex-shrink: 0;
  display: grid;
  gap: 4px;
  padding: 14px 18px;
  border-radius: 14px;
  background: var(--role-surface-card, rgba(10, 36, 58, 0.68));
  border: 1px solid var(--role-border, rgba(0, 161, 214, 0.28));
  text-align: right;
}

.credit-pill span {
  font-size: 12px;
  color: var(--role-text-muted, #8ec8de);
}

.credit-pill strong {
  font-size: 28px;
  color: var(--role-primary-dark, #67e8f9);
  line-height: 1.1;
}

.text-link {
  border: none;
  background: transparent;
  color: var(--role-primary-soft, #22d3ee);
  font-size: 12px;
  cursor: pointer;
  justify-self: end;
}

.course-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.course-card {
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 12px;
  padding: 12px;
  border-radius: 14px;
  background: var(--role-surface-card, rgba(10, 36, 58, 0.68));
  border: 1px solid rgba(0, 161, 214, 0.16);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 161, 214, 0.18);
}

.course-cover {
  width: 88px;
  height: 66px;
  border-radius: 10px;
  overflow: hidden;
  background: rgba(8, 30, 52, 0.8);
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 28px;
}

.course-body h3 {
  margin: 0 0 4px;
  font-size: 14px;
  color: var(--role-text, #e8f8ff);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-body p {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--role-text-muted, #8ec8de);
}

.progress-label {
  display: block;
  margin-top: 6px;
  font-size: 11px;
  color: var(--role-primary-soft, #22d3ee);
}

.more-card {
  display: grid;
  align-content: center;
  justify-items: start;
  gap: 8px;
  padding: 16px;
  border-radius: 14px;
  border: 1px dashed rgba(0, 161, 214, 0.35);
  background: var(--role-surface-strong, rgba(8, 30, 52, 0.74));
  cursor: pointer;
  text-align: left;
}

.more-card span {
  font-size: 12px;
  color: var(--role-text-muted, #8ec8de);
}

.more-card strong {
  font-size: 18px;
  color: var(--role-primary-dark, #67e8f9);
}

.strip-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.strip-actions :deep(.el-button--primary.is-plain) {
  --el-button-text-color: var(--role-primary-soft, #22d3ee);
  --el-button-border-color: rgba(0, 161, 214, 0.45);
  --el-button-hover-text-color: var(--role-primary-dark, #67e8f9);
  --el-button-hover-border-color: #00a1d6;
  --el-button-hover-bg-color: rgba(0, 161, 214, 0.12);
}

@media (max-width: 900px) {
  .strip-head {
    flex-direction: column;
  }

  .credit-pill {
    width: 100%;
    text-align: left;
  }

  .course-row {
    grid-template-columns: 1fr;
  }
}
</style>
