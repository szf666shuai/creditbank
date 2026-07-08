<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchApi } from '@/api/search'
import type { SearchItem } from '@/api/search'
import { searchCategories, searchCategoryLabel } from '@/config/search-categories'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const items = ref<SearchItem[]>([])
const total = ref(0)

const keyword = computed(() => (route.query.q as string) || '')
const searchType = computed(() => (route.query.type as string) || 'all')
const typeLabel = computed(() => searchCategoryLabel(searchType.value))

async function fetchResults() {
  if (!keyword.value.trim()) return
  loading.value = true
  try {
    const res = await searchApi({ q: keyword.value.trim(), type: searchType.value })
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '搜索失败')
    }
    items.value = res.data.items
    total.value = res.data.total
  } catch (e) {
    items.value = []
    total.value = 0
    ElMessage.error(e instanceof Error ? e.message : '搜索失败')
  } finally {
    loading.value = false
  }
}

function changeType(type: string) {
  router.push({ path: '/search', query: { q: keyword.value, type } })
}

onMounted(fetchResults)
watch(() => [route.query.q, route.query.type], fetchResults)
</script>

<template>
  <div class="search-page">
    <div class="section-inner">
      <div class="search-header">
        <h2>搜索结果</h2>
        <p v-if="keyword">
          关键词「<strong>{{ keyword }}</strong>」· 分类「{{ typeLabel }}」· 共 {{ total }} 条
        </p>
      </div>

      <div class="type-tabs">
        <el-check-tag
          v-for="cat in searchCategories"
          :key="cat.value"
          :checked="searchType === cat.value"
          @click="changeType(cat.value)"
        >
          {{ cat.label }}
        </el-check-tag>
      </div>

      <el-skeleton v-if="loading" :rows="6" animated />

      <el-empty v-else-if="!items.length" description="未找到相关内容，换个关键词试试" />

      <div v-else class="result-list">
        <el-card v-for="item in items" :key="`${item.type}-${item.id}`" class="result-card" shadow="hover">
          <div class="result-meta">
            <el-tag size="small" type="info">{{ item.typeName }}</el-tag>
            <span v-if="item.extra" class="result-extra">{{ item.extra }}</span>
          </div>
          <h3 class="result-title">{{ item.title }}</h3>
          <p v-if="item.summary" class="result-summary">{{ item.summary }}</p>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  padding: 32px 16px 48px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.search-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.search-header p {
  color: var(--color-text-muted);
  font-size: 14px;
}

.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 20px 0 24px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-card {
  border-radius: 10px;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.result-extra {
  font-size: 12px;
  color: var(--color-text-muted);
}

.result-title {
  font-size: 17px;
  margin-bottom: 8px;
  color: var(--color-text);
}

.result-summary {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
