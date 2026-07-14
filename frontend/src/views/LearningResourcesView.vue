<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Collection, Search, VideoPlay } from '@element-plus/icons-vue'
import {
  completeLearning,
  fetchLearningResources,
  fetchLearningTags,
  type LearningResource,
} from '@/api/learning'
import { useAuthStore } from '@/stores/auth'
import { BRAND_NAME } from '@/config/brand'
import UiIcon from '@/components/ui/UiIcon.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const completingId = ref<number | null>(null)
const resources = ref<LearningResource[]>([])
const tags = ref<string[]>([])
const keyword = ref('')
const activeTag = ref('')
const activeResourceGroup = ref('all')
const showAllCourses = ref(false)

const advantages = [
  {
    icon: 'collection',
    title: '学习资源立体化',
    desc: '视频、课件、弹幕互动与学习档案一体呈现，覆盖学、练、证全链路。',
  },
  {
    icon: 'edit',
    title: '进度可追踪可认证',
    desc: '按有效观看进度推进学习，达标后可完成课程并领取合格证与秩点奖励。',
  },
  {
    icon: 'link',
    title: '学习流程一体化',
    desc: '从资源发现、课程学习到档案沉淀无缝衔接，支撑终身学习与成果互认。',
  },
]

const quotes = [
  {
    text: `${BRAND_NAME}把优质课程沉淀为可追踪的学习资源，学员既能系统学习，也能把成果写入档案，真正打通「学—证—用」。`,
    author: '教学实践反馈',
    org: '联盟院校教师',
  },
  {
    text: '资源页把筛选、进度和领证入口收拢到一起，减少跳转成本，对混合式学习场景很友好。',
    author: '课程运营观察',
    org: '平台学员社群',
  },
  {
    text: '付费解锁与免费资源同屏展示，配合秩点激励，更容易形成持续学习习惯。',
    author: '学习路径设计',
    org: '职业教育顾问',
  },
]

const completedCount = computed(() =>
  resources.value.filter((item) => item.learningStatus === 1 || item.certNo).length,
)

const filteredResources = computed(() =>
  resources.value.filter((item) => {
    if (activeResourceGroup.value === 'purchased') return !!item.purchased
    if (activeResourceGroup.value === 'learned') return !!item.learned
    if (activeResourceGroup.value === 'free') return !item.paid
    if (activeResourceGroup.value === 'paid') return !!item.paid
    return true
  }),
)

const visibleResources = computed(() =>
  showAllCourses.value ? filteredResources.value : filteredResources.value.slice(0, 8),
)

const resourceGroups = computed(() => [
  { value: 'all', label: '全部课程', count: resources.value.length },
  { value: 'purchased', label: '已购买', count: resources.value.filter((item) => item.purchased).length },
  { value: 'learned', label: '已学习', count: resources.value.filter((item) => item.learned).length },
  { value: 'free', label: '免费课程', count: resources.value.filter((item) => !item.paid).length },
  { value: 'paid', label: '付费课程', count: resources.value.filter((item) => item.paid).length },
])

function tagList(item: LearningResource) {
  return item.tags ? item.tags.split(',').filter(Boolean).slice(0, 3) : []
}

function formatCredit(value?: number) {
  return Number(value || 0).toFixed(2)
}

function canComplete(item: LearningResource) {
  return !item.certNo && Number(item.progress || 0) >= 80
}

function showCoverImage(url?: string) {
  if (!url) return false
  return !/\.(pdf|zip|doc|docx|ppt|pptx|xlsx)(\?.*)?$/i.test(url)
}

async function loadResources() {
  loading.value = true
  try {
    const res = await fetchLearningResources({
      keyword: keyword.value.trim(),
      tag: activeTag.value,
    })
    if (res.code !== 200 || !res.data) throw new Error(res.message || '加载学习资源失败')
    resources.value = res.data
    showAllCourses.value = false
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载学习资源失败')
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  const res = await fetchLearningTags()
  if (res.code === 200 && res.data) {
    tags.value = res.data
  }
}

function openCourse(item: LearningResource) {
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: `/resources/${item.id}` } })
    return
  }
  if (!authStore.isStudent && !authStore.isAdmin) {
    ElMessage.warning('学习页仅学员或管理员可进入')
    return
  }
  router.push({ name: 'course-player', params: { courseId: item.id } })
}

async function handleComplete(item: LearningResource) {
  if (!canComplete(item)) {
    ElMessage.warning('需先在学习页有效观看至 80% 进度，才能完成课程并领取合格证')
    openCourse(item)
    return
  }
  try {
    await ElMessageBox.confirm(
      `《${item.title}》已达到合格观看时长，确认完成课程并生成合格证？`,
      '完成学习',
      { type: 'success', confirmButtonText: '确认完成', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  completingId.value = item.id
  try {
    const res = await completeLearning(item.id)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '完成学习失败')
    ElMessage.success(res.data.creditChange ? '已完成学习并发放奖励秩点' : '已完成学习并生成证书')
    await loadResources()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '完成学习失败')
  } finally {
    completingId.value = null
  }
}

function selectTag(tag: string) {
  activeTag.value = activeTag.value === tag ? '' : tag
  loadResources()
}

function selectGroup(value: string) {
  activeResourceGroup.value = value
  showAllCourses.value = false
}

onMounted(async () => {
  await Promise.all([loadTags(), loadResources()])
  const courseId = Number(route.query.courseId)
  if (courseId) {
    router.replace({ name: 'course-player', params: { courseId } })
  }
})
</script>

<template>
  <div class="resources-portal" v-loading="loading">
    <!-- 页头：与首页 CTA 连续的浅色教育站气质 -->
    <section class="page-hero">
      <div class="page-hero__inner">
        <p class="eyebrow">学习资源</p>
        <h1>探索优质课程</h1>
        <p class="page-hero__desc">
          由联盟机构与平台沉淀的在线课程，支持视频学习、进度追踪与成果认证。
        </p>
      </div>
    </section>

    <section class="intro-band">
      <div class="intro-inner">
        <div class="intro-card">
          <h2>什么是学习资源？</h2>
          <p>
            学习资源是由联盟机构与平台沉淀的优质在线课程，支持视频学习、课件查阅与互动讨论，
            可直接用于个人深度学习与成果认证。
          </p>
        </div>
        <div class="intro-card">
          <h2>学习资源有何意义？</h2>
          <p>
            让优质课程触达每一位学习者，促进教育资源共享与学习成果互认，
            推动终身学习公平与秩点激励闭环。
          </p>
        </div>
      </div>
    </section>

    <section class="portal-section">
      <header class="section-head">
        <p class="eyebrow">平台优势</p>
        <h2>学习资源的优势</h2>
      </header>
      <div class="advantage-grid">
        <article v-for="item in advantages" :key="item.title" class="advantage-card">
          <span class="advantage-icon"><UiIcon :name="item.icon" :size="28" /></span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </article>
      </div>
    </section>

    <!-- 课程目录：Popular Courses 气质 + 真实数据 -->
    <section class="portal-section catalog-section">
      <header class="section-head section-head--row">
        <div>
          <p class="eyebrow">课程目录</p>
          <h2>精选课程</h2>
          <p class="section-sub">按标签与学习状态筛选，进入课程页继续学习</p>
        </div>
        <div class="catalog-stats">
          <div>
            <strong>{{ resources.length }}</strong>
            <span>可学资源</span>
          </div>
          <div>
            <strong>{{ completedCount }}</strong>
            <span>已完成</span>
          </div>
        </div>
      </header>

      <div class="catalog-toolbar">
        <div class="glass-search">
          <el-input
            v-model="keyword"
            placeholder="搜索课程、技能或机构"
            clearable
            @keyup.enter="loadResources"
            @clear="loadResources"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button class="glass-search__btn" type="primary" @click="loadResources">搜索</el-button>
        </div>
      </div>

      <div class="chip-row">
        <button
          v-for="group in resourceGroups"
          :key="group.value"
          type="button"
          class="chip"
          :class="{ active: activeResourceGroup === group.value }"
          @click="selectGroup(group.value)"
        >
          {{ group.label }}
          <em>{{ group.count }}</em>
        </button>
      </div>

      <div v-if="tags.length" class="chip-row chip-row--tags">
        <button
          type="button"
          class="chip ghost"
          :class="{ active: !activeTag }"
          @click="selectTag('')"
        >
          全部标签
        </button>
        <button
          v-for="tag in tags"
          :key="tag"
          type="button"
          class="chip ghost"
          :class="{ active: activeTag === tag }"
          @click="selectTag(tag)"
        >
          {{ tag }}
        </button>
      </div>

      <el-empty
        v-if="!loading && !filteredResources.length"
        description="暂无符合条件的学习资源"
        :image-size="80"
      />

      <div v-else class="package-grid">
        <article
          v-for="item in visibleResources"
          :key="item.id"
          class="package-card"
          @click="openCourse(item)"
        >
          <div class="package-cover">
            <img
              v-if="showCoverImage(item.coverUrl)"
              :src="item.coverUrl"
              :alt="item.title"
              loading="lazy"
            />
            <div v-else class="cover-fallback"><UiIcon name="course" :size="36" /></div>
            <span v-if="item.paid && !item.purchased" class="package-badge paid">付费</span>
            <span v-else-if="item.certNo" class="package-badge done">已完成</span>
            <span v-else-if="item.progress" class="package-badge progress">{{ item.progress }}%</span>
          </div>
          <div class="package-body">
            <h3>{{ item.title }}</h3>
            <p class="package-org">{{ item.orgName || '平台课程' }}</p>
            <div v-if="tagList(item).length" class="package-tags">
              <span v-for="tag in tagList(item)" :key="tag">{{ tag }}</span>
            </div>
            <div class="package-meta">
              <span>奖励 {{ formatCredit(item.creditReward) }} 秩点</span>
              <span v-if="item.paid">{{ formatCredit(item.priceCredit) }} 秩点解锁</span>
              <span v-else>免费学习</span>
            </div>
            <div class="package-actions" @click.stop>
              <el-button type="primary" size="small" :icon="VideoPlay" @click="openCourse(item)">
                <template v-if="item.paid && !item.purchased">购买解锁</template>
                <template v-else-if="item.progress">继续学习</template>
                <template v-else>进入学习</template>
              </el-button>
              <el-button
                v-if="item.certNo"
                size="small"
                type="success"
                :icon="Check"
                disabled
              >
                已领证
              </el-button>
              <el-button
                v-else
                size="small"
                :icon="Collection"
                :loading="completingId === item.id"
                :disabled="!canComplete(item)"
                @click="handleComplete(item)"
              >
                {{ canComplete(item) ? '完成领证' : '满 80% 可领证' }}
              </el-button>
            </div>
          </div>
        </article>
      </div>

      <div v-if="filteredResources.length > 8" class="more-wrap">
        <el-button
          class="more-btn"
          size="large"
          @click="showAllCourses = !showAllCourses"
        >
          {{ showAllCourses ? '收起列表' : '查看更多课程' }}
        </el-button>
      </div>
    </section>

    <section class="portal-section quote-section">
      <header class="section-head">
        <p class="eyebrow">学习者声音</p>
        <h2>学习者与共建者反馈</h2>
      </header>
      <div class="quote-grid">
        <blockquote v-for="(item, index) in quotes" :key="index" class="quote-card">
          <p>{{ item.text }}</p>
          <footer>
            <strong>{{ item.author }}</strong>
            <span>{{ item.org }}</span>
          </footer>
        </blockquote>
      </div>
    </section>
  </div>
</template>

<style scoped>
.resources-portal {
  background: var(--nb-cream, var(--color-background));
  padding-bottom: 64px;
  min-height: calc(100vh - var(--header-height));
}

.page-hero {
  padding: 48px 16px 28px;
  background:
    radial-gradient(circle at 88% 18%, rgba(34, 197, 94, 0.12), transparent 28%),
    radial-gradient(circle at 12% 80%, rgba(190, 227, 248, 0.45), transparent 32%),
    var(--nb-cream, var(--color-background));
  border-bottom: 2.5px solid var(--nb-ink, var(--color-border-neutral));
}

.page-hero__inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.page-hero h1 {
  margin: 0 0 12px;
  font-family: var(--font-heading);
  font-size: clamp(1.75rem, 3.5vw, 2.5rem);
  color: var(--color-foreground);
  line-height: 1.2;
}

.page-hero__desc {
  margin: 0;
  max-width: 40rem;
  font-size: 1.05rem;
  line-height: 1.7;
  color: var(--color-text-secondary);
}

.intro-band {
  padding: 28px 16px 8px;
}

.intro-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.intro-card {
  padding: 24px 26px;
  border-radius: var(--radius-lg);
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow-sm, var(--shadow-sm));
  color: var(--color-foreground);
}

.intro-card h2 {
  margin: 0 0 10px;
  font-family: var(--font-heading);
  font-size: 1.2rem;
  color: var(--color-foreground);
}

.intro-card p {
  margin: 0;
  line-height: 1.8;
  color: var(--color-muted-foreground);
  font-size: 0.95rem;
}

.portal-section {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 32px 16px 8px;
}

.section-head {
  text-align: center;
  margin-bottom: 24px;
}

.section-head--row {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  text-align: left;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
  font-weight: 700;
}

.section-head h2 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: clamp(1.5rem, 2.5vw, 1.875rem);
  color: var(--color-foreground);
  text-shadow: none;
}

.section-sub {
  margin: 8px 0 0;
  font-size: 0.9rem;
  color: var(--color-muted-foreground);
  line-height: 1.5;
}

.catalog-stats {
  display: flex;
  gap: 12px;
}

.catalog-stats div {
  min-width: 88px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  background: var(--color-primary-light);
  border: 1px solid var(--color-border);
  text-align: center;
}

.catalog-stats strong {
  display: block;
  font-size: 1.35rem;
  color: var(--color-primary-dark);
}

.catalog-stats span {
  font-size: 12px;
  color: var(--color-muted-foreground);
}

.advantage-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.advantage-card {
  padding: 24px 22px;
  border-radius: var(--radius-lg);
  background: var(--color-card);
  border: 1px solid var(--color-border-neutral);
  box-shadow: var(--shadow-sm);
  color: var(--color-foreground);
}

.advantage-icon {
  display: inline-grid;
  place-items: center;
  width: 48px;
  height: 48px;
  margin-bottom: 14px;
  border-radius: 12px;
  color: var(--color-primary-dark);
  background: var(--color-primary-light);
}

.advantage-card h3 {
  margin: 0 0 10px;
  font-family: var(--font-heading);
  font-size: 1.1rem;
  color: var(--color-foreground);
}

.advantage-card p {
  margin: 0;
  line-height: 1.7;
  font-size: 0.9rem;
  color: var(--color-muted-foreground);
}

.catalog-section {
  padding-top: 40px;
}

.catalog-toolbar {
  margin-bottom: 16px;
}

.catalog-toolbar .glass-search {
  width: min(100%, 560px);
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.chip-row--tags {
  margin-bottom: 22px;
}

.chip {
  border: 2px solid var(--nb-ink, var(--color-border-neutral));
  background: #fff;
  color: var(--color-foreground);
  border-radius: 999px;
  padding: 7px 14px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
  transition: transform 0.12s ease, box-shadow 0.12s ease, background 0.12s ease;
}

.chip em {
  font-style: normal;
  opacity: 0.7;
  font-size: 12px;
}

.chip.ghost {
  background: transparent;
}

.chip:hover {
  border-color: var(--nb-ink, var(--color-border));
  background: var(--nb-yellow, var(--color-primary-light));
  color: var(--nb-ink, var(--color-primary-dark));
}

.chip.active {
  border-color: var(--nb-ink, var(--color-primary));
  color: var(--nb-ink, var(--color-primary-dark));
  background: #bbf7d0;
  font-weight: 800;
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.package-card {
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow, var(--shadow-md));
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.package-card:hover {
  transform: translate(3px, 3px);
  border-color: var(--nb-ink, var(--color-border));
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.package-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  background: var(--color-muted);
}

.package-cover img,
.cover-fallback {
  width: 100%;
  height: 100%;
}

.package-cover img {
  display: block;
  object-fit: cover;
}

.cover-fallback {
  display: grid;
  place-items: center;
  color: var(--color-on-primary);
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
}

.package-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-primary-dark);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid var(--color-border);
}

.package-badge.paid {
  background: #fff7ed;
  color: #9a3412;
  border-color: rgba(217, 119, 6, 0.35);
}

.package-badge.done {
  background: #ecfdf5;
  color: #065f46;
  border-color: rgba(16, 185, 129, 0.35);
}

.package-badge.progress {
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}

.package-body {
  padding: 14px 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  color: var(--color-foreground);
}

.package-body h3 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: 1rem;
  line-height: 1.4;
  color: var(--color-foreground);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8em;
}

.package-org {
  margin: 0;
  font-size: 12px;
  color: var(--color-primary);
  font-weight: 600;
}

.package-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 22px;
}

.package-tags span {
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  background: var(--color-primary-light);
  border: 1px solid var(--color-border);
  color: var(--color-primary-dark);
}

.package-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: var(--color-muted-foreground);
}

.package-actions {
  margin-top: auto;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 6px;
}

.package-actions :deep(.el-button) {
  flex: 1 1 auto;
  min-height: 36px;
  border-radius: 10px !important;
  font-weight: 800 !important;
}

.package-actions :deep(.el-button--default) {
  background: #fff !important;
  color: var(--nb-ink, #1a202c) !important;
  border: 2px solid var(--nb-ink, #1a202c) !important;
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.package-actions :deep(.el-button--default.is-disabled) {
  background: #f5efe6 !important;
  color: #64748b !important;
  opacity: 1;
  box-shadow: none;
}

.more-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

.more-btn {
  min-width: 180px;
  min-height: 44px;
  border-radius: 999px !important;
  background: var(--nb-green, #22c55e) !important;
  border: 2.5px solid var(--nb-ink, #1a202c) !important;
  color: #fff !important;
  font-weight: 800 !important;
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c) !important;
}

.more-btn:hover {
  background: var(--nb-green-deep, #16a34a) !important;
  color: #fff !important;
}

.quote-section {
  padding-top: 48px;
}

.quote-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.quote-card {
  margin: 0;
  padding: 22px 20px;
  border-radius: var(--radius-lg);
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow-sm, var(--shadow-sm));
  color: var(--color-foreground);
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 100%;
  box-sizing: border-box;
}

.quote-card p {
  margin: 0;
  line-height: 1.75;
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  flex: 1 1 auto;
}

.quote-card footer {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: auto;
  padding-top: 16px;
}

.quote-card strong {
  color: var(--color-primary-dark);
  font-size: 13px;
}

.quote-card span {
  font-size: 12px;
  color: var(--color-muted-foreground);
}

@media (max-width: 1100px) {
  .package-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .intro-inner,
  .advantage-grid,
  .quote-grid {
    grid-template-columns: 1fr;
  }

  .package-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .section-head--row {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 560px) {
  .package-grid {
    grid-template-columns: 1fr;
  }

  .page-hero {
    padding: 36px 16px 24px;
  }
}
</style>
