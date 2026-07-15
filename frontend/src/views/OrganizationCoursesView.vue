<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, VideoPlay } from '@element-plus/icons-vue'
import PageShell from '@/components/common/PageShell.vue'
import {
  fetchOrgCourses,
  type LearningResource,
} from '@/api/learning'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const orgId = computed(() => Number(route.params.orgId))
const loading = ref(false)
const loadError = ref<string | null>(null)
const courses = ref<LearningResource[]>([])
const keyword = ref('')

const orgName = computed(() => {
  if (courses.value.length > 0 && courses.value[0].orgName) {
    return courses.value[0].orgName
  }
  return '机构课程'
})

function difficultyLabel(level?: number) {
  switch (level) {
    case 1: return '入门'
    case 2: return '初级'
    case 3: return '中级'
    case 4: return '高级'
    default: return '入门'
  }
}

function tagList(item: LearningResource) {
  return item.tags ? item.tags.split(',').filter(Boolean).slice(0, 3) : []
}

async function fetchCourses() {
  if (!orgId.value) return
  loading.value = true
  loadError.value = null
  try {
    courses.value = unwrapApi(await fetchOrgCourses(orgId.value, keyword.value))
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/resources')
}

function openCourse(id: number) {
  router.push(`/resources/${id}`)
}

onMounted(fetchCourses)
</script>

<template>
  <PageShell
    :title="orgName"
    description="机构课程列表"
    :loading="loading"
    :error="loadError"
    @retry="fetchCourses"
  >
    <template #actions>
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回机构列表</span>
      </el-button>
    </template>

    <div class="org-header">
      <h2>{{ orgName }}</h2>
      <p>共 {{ courses.length }} 门课程</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索课程名称..."
        clearable
        @clear="fetchCourses"
        @keyup.enter="fetchCourses"
      >
        <template #append>
          <el-button @click="fetchCourses">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div class="course-grid">
      <div
        v-for="item in courses"
        :key="item.id"
        class="course-card"
        @click="openCourse(item.id)"
      >
        <div class="course-cover">
          <img
            v-if="item.coverUrl"
            :src="item.coverUrl"
            :alt="item.title"
            loading="lazy"
          />
          <div v-else class="cover-fallback">
            <el-icon :size="40"><VideoPlay /></el-icon>
          </div>
          <span v-if="item.certNo" class="course-badge done">已完成</span>
          <span v-else-if="item.progress" class="course-badge progress">
            {{ item.progress }}%
          </span>
        </div>
        <div class="course-body">
          <h3>{{ item.title }}</h3>
          <p class="course-desc">{{ item.description }}</p>
          <div class="course-tags">
            <span v-for="tag in tagList(item)" :key="tag" class="tag">{{ tag }}</span>
          </div>
          <div class="course-meta">
            <span class="credit">{{ Number(item.creditValue || 0).toFixed(0) }} 秩点</span>
            <span class="diff">{{ difficultyLabel(item.difficulty) }}</span>
            <span class="dur">{{ item.durationMinutes || 0 }} 分钟</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty
      v-if="!loading && courses.length === 0"
      description="该机构暂无课程"
    />
  </PageShell>
</template>

<style scoped>
.org-header {
  margin-bottom: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #bfdbfe 0%, #93c5fd 100%);
  border: 2.5px solid var(--nb-ink);
  border-radius: 16px;
  box-shadow: var(--nb-shadow);
}

.org-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 900;
  color: var(--nb-ink);
}

.org-header p {
  margin: 0;
  font-size: 14px;
  color: var(--nb-ink);
  opacity: 0.7;
}

.search-bar {
  max-width: 480px;
  margin-bottom: 20px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}

.course-card {
  background: #fff;
  border: 2.5px solid var(--nb-ink);
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--nb-shadow-sm);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.course-card:hover {
  transform: translate(2px, 2px);
  box-shadow: 4px 4px 0 0 var(--nb-ink);
}

.course-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: var(--color-muted);
  overflow: hidden;
  border-bottom: 2px solid var(--nb-ink);
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #94a3b8;
}

.course-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  border: 2px solid var(--nb-ink);
}

.course-badge.done {
  background: #bbf7d0;
  color: var(--nb-ink);
}

.course-badge.progress {
  background: #fef3c7;
  color: var(--nb-ink);
}

.course-body {
  padding: 14px;
}

.course-body h3 {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 700;
  color: var(--nb-ink);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-desc {
  margin: 0 0 10px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-tags {
  margin-bottom: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag {
  font-size: 11px;
  padding: 2px 8px;
  background: #e0f2fe;
  border: 1px solid var(--nb-ink);
  border-radius: 999px;
  color: var(--nb-ink);
}

.course-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #64748b;
}

.credit {
  font-weight: 700;
  color: #16a34a;
}
</style>
