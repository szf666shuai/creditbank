<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import PageShell from '@/components/common/PageShell.vue'
import { INTEGRITY_EVENT_OPTIONS } from '@/api/profile-integrity'
import { listAdminIntegrityRecordsApi, type AdminIntegrityRecord } from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const records = ref<AdminIntegrityRecord[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const filters = reactive({ eventType: undefined as number | undefined, userId: undefined as number | undefined })

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(await listAdminIntegrityRecordsApi({ page: page.value, pageSize: pageSize.value, eventType: filters.eventType, userId: filters.userId }))
    records.value = data.records
    total.value = data.total
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <PageShell title="诚信监察" description="全平台诚信分变动记录（只读）" :loading="loading" :error="loadError" @retry="fetchData">
    <div class="page-toolbar">
      <el-select v-model="filters.eventType" placeholder="变动类型" clearable style="width: 120px">
        <el-option v-for="item in INTEGRITY_EVENT_OPTIONS.filter((o) => o.value !== undefined)" :key="String(item.value)" :label="item.label" :value="item.value" />
      </el-select>
      <el-input-number v-model="filters.userId" placeholder="用户ID" :min="1" controls-position="right" />
      <el-button type="primary" @click="() => { page = 1; fetchData() }">查询</el-button>
    </div>
    <el-table :data="records" border stripe>
      <el-table-column prop="userName" label="用户" width="100" />
      <el-table-column prop="userId" label="用户ID" width="80" align="center" />
      <el-table-column label="类型" width="80" align="center">
        <template #default="{ row }"><el-tag :type="row.eventType === 1 ? 'success' : 'danger'" size="small">{{ row.eventTypeName }}</el-tag></template>
      </el-table-column>
      <el-table-column label="变动" width="80" align="right">
        <template #default="{ row }"><span :class="row.changeValue > 0 ? 'plus' : 'minus'">{{ row.changeValue > 0 ? '+' : '' }}{{ row.changeValue }}</span></template>
      </el-table-column>
      <el-table-column prop="scoreAfter" label="变动后" width="80" align="right" />
      <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
      <el-table-column label="时间" width="170"><template #default="{ row }">{{ formatTime(row.createTime) }}</template></el-table-column>
    </el-table>
    <div v-if="total > 0" class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" layout="total, prev, pager, next" background @current-change="fetchData" />
    </div>
  </PageShell>
</template>

<style scoped>
.plus { color: #52c41a; font-weight: 600; }
.minus { color: #fa541c; font-weight: 600; }
</style>
