<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import PageShell from '@/components/common/PageShell.vue'
import {
  INTEGRITY_EVENT_OPTIONS,
  getIntegritySummaryApi,
  listIntegrityRecordsApi,
  scoreLevelColor,
  type IntegrityRecordItem,
  type IntegritySummary,
} from '@/api/profile-integrity'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const summary = ref<IntegritySummary | null>(null)
const records = ref<IntegrityRecordItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filters = reactive({
  eventType: undefined as number | undefined,
  dateRange: null as [string, string] | null,
})

const scoreColor = computed(() => (summary.value ? scoreLevelColor(summary.value.score) : '#2094f3'))

function changeClass(value: number) {
  if (value > 0) return 'change-plus'
  if (value < 0) return 'change-minus'
  return ''
}

function formatChange(value: number) {
  const prefix = value > 0 ? '+' : ''
  return `${prefix}${value}`
}

function eventTagType(eventType: number) {
  return eventType === 1 ? 'success' : 'danger'
}

async function fetchSummary() {
  try {
    summary.value = unwrapApi(await getIntegritySummaryApi())
  } catch {
    summary.value = null
  }
}

async function fetchRecords() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(
      await listIntegrityRecordsApi({
        page: page.value,
        pageSize: pageSize.value,
        eventType: filters.eventType,
        startDate: filters.dateRange?.[0],
        endDate: filters.dateRange?.[1],
      }),
    )
    records.value = data.records
    total.value = data.total
    page.value = data.page
    pageSize.value = data.pageSize
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchRecords()
}

function handleReset() {
  filters.eventType = undefined
  filters.dateRange = null
  page.value = 1
  fetchRecords()
}

function handlePageChange() {
  fetchRecords()
}

function handleSizeChange() {
  page.value = 1
  fetchRecords()
}

onMounted(async () => {
  await Promise.all([fetchSummary(), fetchRecords()])
})
</script>

<template>
  <PageShell
    title="诚信评定"
    description="查看当前诚信分及变动记录"
    :loading="loading"
    :error="loadError"
    @retry="fetchRecords"
  >
      <div v-if="summary" class="score-panel">
        <div class="score-ring" :style="{ '--score-color': scoreColor }">
          <div class="score-value">{{ summary.score }}</div>
          <div class="score-unit">分</div>
        </div>
        <div class="score-meta">
          <div class="level-badge" :style="{ color: scoreColor, borderColor: scoreColor }">
            {{ summary.levelName }}
          </div>
          <p class="score-desc">诚信分范围 0–100，反映学习与实践中的守信表现</p>
          <p v-if="summary.updateTime" class="update-time">
            最近更新：{{ formatTime(summary.updateTime) }}
          </p>
        </div>
      </div>

      <div class="page-toolbar">
        <el-select v-model="filters.eventType" placeholder="变动类型" clearable style="width: 140px">
          <el-option
            v-for="item in INTEGRITY_EVENT_OPTIONS.filter((o) => o.value !== undefined)"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 280px"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="records" border stripe>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="eventTagType(row.eventType)" size="small">{{ row.eventTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动分值" width="100" align="right">
          <template #default="{ row }">
            <span :class="changeClass(row.changeValue)">{{ formatChange(row.changeValue) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="变动后分数" width="110" align="right">
          <template #default="{ row }">
            {{ row.scoreAfter }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="260" show-overflow-tooltip />
        <el-table-column prop="refType" label="关联业务" width="120" show-overflow-tooltip />
      </el-table>

      <el-empty
        v-if="!loading && records.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无诚信变动记录"
      />

      <div v-if="total > 0" class="page-pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
  </PageShell>
</template>

<style scoped>
.score-panel {
  display: flex;
  align-items: center;
  gap: 28px;
  margin-bottom: 24px;
  padding: 20px;
  background: var(--nb-blue, #bee3f8);
  border: 2.5px solid var(--nb-ink, #1a202c);
  border-radius: 16px;
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
}

.score-ring {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 6px solid var(--score-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
  flex-shrink: 0;
  box-shadow: 3px 3px 0 0 var(--nb-ink, #1a202c);
}

.score-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--score-color);
  line-height: 1;
}

.score-unit {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.score-meta {
  flex: 1;
}

.level-badge {
  display: inline-block;
  padding: 4px 14px;
  border: 1px solid;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.score-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}

.update-time {
  font-size: 13px;
  color: var(--color-text-muted);
}

.change-plus {
  color: #52c41a;
  font-weight: 600;
}

.change-minus {
  color: #fa541c;
  font-weight: 600;
}

@media (max-width: 768px) {
  .score-panel {
    flex-direction: column;
    text-align: center;
  }
}
</style>
