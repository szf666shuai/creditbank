<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, OfficeBuilding } from '@element-plus/icons-vue'
import { listJoinedOrgsApi, type OrgListItem } from '@/api/enterprise'
import { BRAND_NAME } from '@/config/brand'
import UiIcon from '@/components/ui/UiIcon.vue'

const router = useRouter()

const loading = ref(false)
const orgs = ref<OrgListItem[]>([])
const keyword = ref('')
const activeType = ref<number | null>(null)

const typeOptions = [
  { label: '全部', value: null },
  { label: '企业', value: 1 },
  { label: '高校', value: 2 },
  { label: '培训机构', value: 3 },
]

function typeLabel(type?: number) {
  switch (type) {
    case 1: return '企业'
    case 2: return '高校'
    case 3: return '培训机构'
    default: return '机构'
  }
}

async function loadOrgs() {
  loading.value = true
  try {
    const res = await listJoinedOrgsApi({
      page: 1,
      pageSize: 50,
      name: keyword.value.trim(),
      type: activeType.value ?? undefined,
    })
    if (res.code === 200 && res.data?.records) {
      orgs.value = res.data.records
    }
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function gotoOrgCourses(orgId: number) {
  router.push(`/resources/org/${orgId}`)
}

function selectType(val: number | null) {
  activeType.value = val
  loadOrgs()
}

onMounted(loadOrgs)
</script>

<template>
  <div class="resources-portal" v-loading="loading">
    <section class="page-hero">
      <div class="page-hero__inner">
        <p class="eyebrow">资源商城</p>
        <h1>探索机构课程</h1>
        <p class="page-hero__desc">
          汇聚联盟高校、企业与培训机构的优质课程资源，选择心仪机构开始学习之旅。
        </p>
      </div>
    </section>

    <section class="intro-band">
      <div class="intro-inner">
        <div class="intro-card">
          <h2>为什么选择资源商城？</h2>
          <p>
            精选联盟机构优质课程，每门课程均经过平台审核，
            学习成果可计入学习档案并兑换秩点奖励。
          </p>
        </div>
        <div class="intro-card">
          <h2>机构资源有哪些？</h2>
          <p>
            涵盖企业内训、高校公开课、职业技能培训等多种类型，
            满足学历提升、职业发展与兴趣学习多元化需求。
          </p>
        </div>
      </div>
    </section>

    <section class="portal-section">
      <header class="section-head">
        <p class="eyebrow">机构列表</p>
        <h2>入驻机构</h2>
        <p class="section-sub">选择机构查看其提供的课程资源</p>
      </header>

      <div class="catalog-toolbar">
        <div class="glass-search">
          <el-input
            v-model="keyword"
            placeholder="搜索机构名称..."
            clearable
            @keyup.enter="loadOrgs"
            @clear="loadOrgs"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button class="glass-search__btn" type="primary" @click="loadOrgs">搜索</el-button>
        </div>
      </div>

      <div class="chip-row">
        <button
          v-for="opt in typeOptions"
          :key="opt.label"
          type="button"
          class="chip"
          :class="{ active: activeType === opt.value }"
          @click="selectType(opt.value)"
        >
          {{ opt.label }}
        </button>
      </div>

      <el-empty
        v-if="!loading && orgs.length === 0"
        description="暂无符合条件的机构"
        :image-size="80"
      />

      <div v-else class="org-grid">
        <article
          v-for="org in orgs"
          :key="org.id"
          class="org-card"
          @click="gotoOrgCourses(org.id)"
        >
          <div class="org-cover">
            <img
              v-if="org.logo"
              :src="org.logo"
              :alt="org.name"
              loading="lazy"
            />
            <div v-else class="cover-fallback">
              <el-icon :size="36"><OfficeBuilding /></el-icon>
            </div>
            <span class="org-type-badge">{{ typeLabel(org.type) }}</span>
          </div>
          <div class="org-body">
            <h3>{{ org.name }}</h3>
            <p class="org-desc">{{ org.intro || '暂无介绍' }}</p>
            <div class="org-meta">
              <span v-if="org.email" class="meta-item">
                <el-icon><UiIcon name="mail" :size="14" /></el-icon>
                <span>有合作</span>
              </span>
              <span class="meta-item">
                <el-icon><UiIcon name="star" :size="14" /></el-icon>
                <span>精选机构</span>
              </span>
            </div>
            <div class="org-actions">
              <el-button type="primary" @click.stop="gotoOrgCourses(org.id)">
                查看课程
              </el-button>
            </div>
          </div>
        </article>
      </div>
    </section>

    <section class="portal-section quote-section">
      <header class="section-head">
        <p class="eyebrow">机构声音</p>
        <h2>共建机构反馈</h2>
      </header>
      <div class="quote-grid">
        <blockquote class="quote-card">
          <p>{{ BRAND_NAME }}的资源商城帮助我们的课程触达更多学员，学习数据与秩点体系让教学效果可量化。</p>
          <footer>
            <strong>合作院校代表</strong>
            <span>高校联盟成员</span>
          </footer>
        </blockquote>
        <blockquote class="quote-card">
          <p>企业内训课程上架后，学员参与度明显提升，证书与秩点激励形成了良好的学习闭环。</p>
          <footer>
            <strong>企业培训负责人</strong>
            <span>合作企业 HRD</span>
          </footer>
        </blockquote>
        <blockquote class="quote-card">
          <p>平台的课程审核与档案体系，让培训机构的专业课程获得了更广泛的信任与认可。</p>
          <footer>
            <strong>培训机构创始人</strong>
            <span>职业教育伙伴</span>
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
    radial-gradient(circle at 88% 18%, rgba(59, 130, 246, 0.15), transparent 28%),
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
}

.section-sub {
  margin: 8px 0 0;
  font-size: 0.9rem;
  color: var(--color-muted-foreground);
  line-height: 1.5;
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

.chip:hover {
  border-color: var(--nb-ink, var(--color-border));
  background: var(--nb-yellow, var(--color-primary-light));
  color: var(--nb-ink, var(--color-primary-dark));
}

.chip.active {
  border-color: var(--nb-ink, var(--color-primary));
  color: var(--nb-ink, var(--color-primary-dark));
  background: #bfdbfe;
  font-weight: 800;
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.org-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.org-card {
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

.org-card:hover {
  transform: translate(3px, 3px);
  border-color: var(--nb-ink, var(--color-border));
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.org-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  background: linear-gradient(135deg, #bfdbfe 0%, #93c5fd 100%);
}

.org-cover img {
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
  color: var(--nb-ink, #1e40af);
}

.org-type-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid var(--nb-ink, #1a202c);
  color: var(--nb-ink, #1a202c);
}

.org-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  color: var(--color-foreground);
}

.org-body h3 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: 1.05rem;
  line-height: 1.4;
  color: var(--color-foreground);
}

.org-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-muted-foreground);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8em;
}

.org-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: var(--color-muted-foreground);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.org-actions {
  margin-top: auto;
  padding-top: 4px;
}

.org-actions :deep(.el-button) {
  width: 100%;
  font-weight: 800 !important;
  border-radius: 10px !important;
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
  .org-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .intro-inner,
  .quote-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .org-grid {
    grid-template-columns: 1fr;
  }

  .page-hero {
    padding: 36px 16px 24px;
  }
}
</style>
