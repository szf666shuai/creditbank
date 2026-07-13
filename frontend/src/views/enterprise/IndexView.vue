<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { OfficeBuilding, School, Reading, Search } from '@element-plus/icons-vue'
import {
  listJoinedOrgsApi,
  ORG_TYPE_OPTIONS,
  type OrgListItem,
} from '@/api/enterprise'
import { getErrorMessage, unwrapApi } from '@/utils/api'

const loading = ref(false)
const loadError = ref<string | null>(null)
const orgList = ref<OrgListItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const keyword = ref('')
const typeFilter = ref<number | undefined>(undefined)

const typeRail = computed(() =>
  ORG_TYPE_OPTIONS.map((item) => ({
    label: item.label,
    value: item.value,
  })),
)

const typeAccent: Record<number, string> = {
  1: '#38bdf8',
  2: '#34d399',
  3: '#a78bfa',
  4: '#fb923c',
}

function typeIcon(type: number) {
  if (type === 1) return School
  if (type === 2) return Reading
  return OfficeBuilding
}

async function fetchOrgs() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(
      await listJoinedOrgsApi({
        page: page.value,
        pageSize: pageSize.value,
        name: keyword.value,
        type: typeFilter.value,
      }),
    )
    orgList.value = data.records
    total.value = data.total
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function selectType(value?: number) {
  typeFilter.value = value
}

function handleSearch() {
  page.value = 1
  fetchOrgs()
}

function handlePageChange(value: number) {
  page.value = value
  fetchOrgs()
}

function handleSizeChange(value: number) {
  pageSize.value = value
  page.value = 1
  fetchOrgs()
}

watch(typeFilter, () => {
  page.value = 1
  fetchOrgs()
})

onMounted(fetchOrgs)
</script>

<template>
  <div class="enterprise-page" v-loading="loading">
    <section class="hero-banner">
      <div class="hero-banner__media" aria-hidden="true" />
      <div class="hero-banner__veil" aria-hidden="true" />
      <div class="hero-banner__content">
        <p class="hero-kicker">Enterprise Alliance</p>
        <h1>企业中心</h1>
        <p class="hero-copy">
          汇聚加盟机构资源，连接高校、培训机构与企业，共建学习成果互认、就业服务与活动协同通道。
        </p>
        <div class="hero-stats">
          <div>
            <strong>{{ total }}</strong>
            <span>加盟机构</span>
          </div>
          <div>
            <strong>{{ typeRail.length - 1 }}</strong>
            <span>机构类型</span>
          </div>
          <div>
            <strong>{{ orgList.length }}</strong>
            <span>本页展示</span>
          </div>
        </div>
      </div>
    </section>

    <div class="enterprise-inner">
      <header class="section-head">
        <div>
          <p class="eyebrow">Partners</p>
          <h2>加盟企业名录</h2>
        </div>
        <p>浏览机构主页，查看招聘、活动与公开资料。</p>
      </header>

      <div class="filter-bar">
        <div class="type-chips">
          <button
            v-for="item in typeRail"
            :key="String(item.value ?? 'all')"
            type="button"
            class="type-chip"
            :class="{ active: typeFilter === item.value }"
            @click="selectType(item.value)"
          >
            {{ item.label }}
          </button>
        </div>

        <div class="glass-search">
          <el-input
            v-model="keyword"
            placeholder="搜索机构名称"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button class="glass-search__btn" type="primary" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <el-alert
        v-if="loadError"
        :title="loadError"
        type="warning"
        show-icon
        :closable="false"
        class="load-alert"
      >
        <template #default>
          <el-button link type="primary" @click="fetchOrgs">重新加载</el-button>
        </template>
      </el-alert>

      <el-empty
        v-if="!loading && orgList.length === 0"
        description="暂无符合条件的加盟企业"
        :image-size="80"
      />

      <div v-else class="partner-grid">
        <router-link
          v-for="org in orgList"
          :key="org.id"
          :to="`/enterprise/${org.id}`"
          class="partner-card"
        >
          <div
            class="partner-logo"
            :style="{ color: typeAccent[org.type] || '#38bdf8' }"
          >
            <img v-if="org.logo" :src="org.logo" :alt="org.name" />
            <el-icon v-else :size="28"><component :is="typeIcon(org.type)" /></el-icon>
          </div>
          <div class="partner-body">
            <h3>{{ org.name }}</h3>
            <span class="partner-type">{{ org.typeName }}</span>
            <p>{{ org.intro || '暂无简介，点击进入机构主页了解更多。' }}</p>
          </div>
        </router-link>
      </div>

      <div v-if="total > 0" class="page-pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.enterprise-page {
  background: transparent;
  padding-bottom: 56px;
  min-height: calc(100vh - var(--header-height));
}

.hero-banner {
  position: relative;
  min-height: min(46vh, 420px);
  display: grid;
  place-items: center;
  overflow: hidden;
  margin-bottom: 28px;
}

.hero-banner__media {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(120deg, rgba(15, 23, 42, 0.2), transparent 40%),
    url('https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=1800&q=80')
      center / cover no-repeat;
  transform: scale(1.04);
  filter: saturate(0.85) contrast(1.05);
}

.hero-banner__veil {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(8, 15, 30, 0.35) 0%, rgba(8, 15, 30, 0.72) 100%),
    radial-gradient(ellipse at 50% 20%, rgba(56, 189, 248, 0.18), transparent 55%);
}

.hero-banner__content {
  position: relative;
  z-index: 1;
  max-width: 820px;
  padding: 48px 24px;
  text-align: center;
  color: #f8fafc;
}

.hero-kicker,
.eyebrow {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(186, 230, 253, 0.88);
  font-weight: 700;
}

.hero-banner__content h1 {
  margin: 0 0 14px;
  font-size: clamp(34px, 5vw, 52px);
  line-height: 1.15;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.35);
}

.hero-copy {
  margin: 0 auto;
  max-width: 640px;
  line-height: 1.85;
  font-size: 16px;
  color: rgba(241, 245, 249, 0.9);
  text-shadow: 0 1px 10px rgba(0, 0, 0, 0.35);
}

.hero-stats {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 28px;
  margin-top: 28px;
}

.hero-stats strong {
  display: block;
  font-size: 28px;
  line-height: 1.1;
}

.hero-stats span {
  font-size: 12px;
  color: rgba(186, 230, 253, 0.75);
}

.enterprise-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 16px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
  margin-bottom: 18px;
  color: rgba(226, 232, 240, 0.78);
}

.section-head h2 {
  margin: 0;
  color: #f8fafc;
  font-size: 28px;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.35);
}

.section-head p {
  margin: 0;
  max-width: 320px;
  line-height: 1.6;
  font-size: 14px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  margin-bottom: 20px;
}

.type-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.type-chip {
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: rgba(15, 23, 42, 0.35);
  color: rgba(226, 232, 240, 0.82);
  border-radius: 999px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 13px;
  backdrop-filter: blur(8px);
}

.type-chip:hover,
.type-chip.active {
  border-color: rgba(56, 189, 248, 0.55);
  color: #e0f2fe;
  background: rgba(14, 165, 233, 0.22);
  font-weight: 600;
}

.load-alert {
  margin-bottom: 16px;
}

.partner-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  min-height: 180px;
}

.partner-card {
  display: flex;
  gap: 14px;
  align-items: center;
  min-height: 108px;
  padding: 16px 18px;
  border-radius: 14px;
  text-decoration: none;
  color: inherit;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(12px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.16);
  transition: transform 0.18s, background 0.18s, border-color 0.18s;
}

.partner-card:hover {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.16);
  border-color: rgba(125, 211, 252, 0.4);
}

.partner-logo {
  width: 58px;
  height: 58px;
  border-radius: 14px;
  overflow: hidden;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  background: rgba(15, 23, 42, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.partner-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.partner-body {
  min-width: 0;
}

.partner-body h3 {
  margin: 0 0 4px;
  font-size: 15px;
  color: #f8fafc;
  line-height: 1.35;
}

.partner-type {
  display: inline-block;
  margin-bottom: 6px;
  font-size: 12px;
  color: #7dd3fc;
}

.partner-body p {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
  color: rgba(226, 232, 240, 0.68);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.page-pagination {
  margin-top: 22px;
}

.page-pagination :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.08);
  --el-pagination-text-color: #e2e8f0;
  justify-content: center;
}

@media (max-width: 960px) {
  .partner-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .section-head,
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
}

@media (max-width: 640px) {
  .partner-grid {
    grid-template-columns: 1fr;
  }
}
</style>
