<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, VideoPlay } from '@element-plus/icons-vue'
import PageShell from '@/components/common/PageShell.vue'
import { fetchLearningResources, type LearningResource } from '@/api/learning'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const tagName = computed(() => decodeURIComponent(String(route.params.tag || '')))
const loading = ref(false)
const loadError = ref<string | null>(null)
const courses = ref<LearningResource[]>([])

function difficultyLabel(level?: number) {
  switch (level) {
    case 1: return '入门'
    case 2: return '初级'
    case 3: return '中级'
    case 4: return '高级'
    default: return '入门'
  }
}

async function fetchCourses() {
  if (!tagName.value) return
  loading.value = true
  loadError.value = null
  try {
    courses.value = unwrapApi(await fetchLearningResources({ tag: tagName.value }))
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function openCourse(course: LearningResource) {
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: `/resources/${course.id}` } })
    return
  }
  router.push(`/resources/${course.id}`)
}

watch(tagName, fetchCourses)
onMounted(fetchCourses)
</script>

<template>
  <PageShell
    :title="`${tagName} · 微专业轨道`"
    :description="`汇聚「${tagName}」相关课程，按标签进入学习`"
    :loading="loading"
    :error="loadError"
    @retry="fetchCourses"
  >
    <template #header>
      <el-button :icon="ArrowLeft" @click="router.push('/')">返回首页</el-button>
    </template>

    <el-empty
      v-if="!loading && courses.length === 0"
      description="该微专业下暂无课程"
      :image-size="80"
    />

    <div v-else class="course-grid">
      <article
        v-for="course in courses"
        :key="course.id"
        class="course-card"
        @click="openCourse(course)"
      >
        <div class="cover">
          <img v-if="course.coverUrl" :src="course.coverUrl" :alt="course.title" />
          <div v-else class="cover-fallback"><el-icon :size="28"><VideoPlay /></el-icon></div>
        </div>
        <div class="body">
          <h3>{{ course.title }}</h3>
          <p>{{ course.description || '暂无简介' }}</p>
          <div class="meta">
            <span v-if="course.orgName">{{ course.orgName }}</span>
            <span>{{ difficultyLabel(course.difficulty) }}</span>
            <span v-if="course.creditReward">{{ course.creditReward }} 秩点</span>
          </div>
        </div>
      </article>
    </div>
  </PageShell>
</template>

<style scoped>
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.course-card {
  display: flex;
  flex-direction: column;
  border: 2px solid var(--nb-ink, #1a202c);
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 3px 3px 0 0 var(--nb-ink, #1a202c);
}

.cover {
  height: 140px;
  background: #f3f4f6;
}

.cover img,
.cover-fallback {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.body {
  padding: 12px 14px 14px;
}

.body h3 {
  margin: 0 0 6px;
  font-size: 15px;
  line-height: 1.4;
}

.body p {
  margin: 0 0 10px;
  font-size: 13px;
  color: #6b7280;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #4b5563;
}
</style>
