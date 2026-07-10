<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import PageShell from '@/components/common/PageShell.vue'
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

const typeIcons: Record<number, string> = {
  1: '🏫',
  2: '📚',
  3: '🏢',
  4: '🏛️',
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

function handleSearch() {
  page.value = 1
  fetchOrgs()
}

function handleReset() {
  keyword.value = ''
  typeFilter.value = undefined
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
  <div class="enterprise-list-wrap">
    <PageShell
      title="加盟企业"
      description="浏览已加盟平台的高校、培训机构与企业"
      :loading="loading"
      :error="loadError"
      @retry="fetchOrgs"
    >
      <div class="page-toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索企业名称"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="typeFilter" placeholder="机构类型" clearable class="type-select">
          <el-option
            v-for="item in ORG_TYPE_OPTIONS.filter((o) => o.value !== undefined)"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <div class="card-grid">
        <el-empty
          v-if="!loading && orgList.length === 0"
          class="page-empty"
          :image-size="80"
          description="暂无符合条件的加盟企业"
        />

        <router-link
          v-for="org in orgList"
          :key="org.id"
          :to="`/enterprise/${org.id}`"
          class="org-card"
        >
          <div class="org-card-header">
            <div class="org-logo">
              <img v-if="org.logo" :src="org.logo" :alt="org.name" />
              <span v-else class="org-logo-fallback">{{ typeIcons[org.type] || '🏢' }}</span>
            </div>
            <div class="org-meta">
              <h3>{{ org.name }}</h3>
              <el-tag size="small" type="info">{{ org.typeName }}</el-tag>
            </div>
          </div>

          <p class="org-intro">{{ org.intro || '暂无简介' }}</p>

          <ul class="org-info">
            <li v-if="org.contact">
              <span class="label">联系人</span>
              <span>{{ org.contact }}</span>
            </li>
            <li v-if="org.phone">
              <span class="label">电话</span>
              <span>{{ org.phone }}</span>
            </li>
            <li v-if="org.address">
              <span class="label">地址</span>
              <span>{{ org.address }}</span>
            </li>
          </ul>
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
    </PageShell>
  </div>
</template>

<style scoped>
.enterprise-list-wrap {
  padding: 24px 16px 48px;
}

.enterprise-list-wrap :deep(.page-shell) {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.search-input {
  width: 280px;
}

.type-select {
  width: 160px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.org-card {
  display: block;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px;
  transition: box-shadow 0.2s, transform 0.2s;
  text-decoration: none;
  color: inherit;
}

.org-card:hover {
  box-shadow: 0 8px 24px rgba(32, 148, 243, 0.12);
  transform: translateY(-2px);
}

.org-card-header {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 12px;
}

.org-logo {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--color-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.org-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.org-logo-fallback {
  font-size: 24px;
}

.org-meta h3 {
  font-size: 16px;
  color: var(--color-text);
  margin-bottom: 6px;
}

.org-intro {
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-secondary);
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 62px;
}

.org-info {
  list-style: none;
  padding: 0;
  margin: 0;
  border-top: 1px solid var(--color-border);
  padding-top: 12px;
}

.org-info li {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}

.org-info li:last-child {
  margin-bottom: 0;
}

.org-info .label {
  color: var(--color-text-muted);
  min-width: 42px;
  flex-shrink: 0;
}
</style>
