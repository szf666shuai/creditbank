<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import { listAdminActivitiesApi, updateAdminActivityStatusApi, type AdminActivity } from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const records = ref<AdminActivity[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const actingId = ref<number | null>(null)
const filters = reactive({ status: undefined as number | undefined, keyword: '' })

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(await listAdminActivitiesApi({ page: page.value, pageSize: pageSize.value, status: filters.status, keyword: filters.keyword || undefined }))
    records.value = data.records
    total.value = data.total
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row: AdminActivity) {
  const nextStatus = row.status === 1 ? 0 : 1
  const label = nextStatus === 1 ? '上架' : '下架'
  await ElMessageBox.confirm(`确定${label}活动「${row.title}」吗？`, '活动监管', { type: 'warning' })
  actingId.value = row.id
  try {
    unwrapApi(await updateAdminActivityStatusApi(row.id, nextStatus))
    ElMessage.success(`活动已${label}`)
    await fetchData()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  } finally {
    actingId.value = null
  }
}

onMounted(fetchData)
</script>

<template>
  <PageShell title="活动监管" description="查看全平台活动，必要时强制下架" :loading="loading" :error="loadError" @retry="fetchData">
    <div class="page-toolbar">
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="进行中" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-input v-model="filters.keyword" placeholder="活动名称" clearable style="width: 180px" />
      <el-button type="primary" @click="() => { page = 1; fetchData() }">查询</el-button>
    </div>
    <el-table :data="records" border stripe>
      <el-table-column prop="title" label="活动" min-width="160" show-overflow-tooltip />
      <el-table-column prop="orgName" label="企业" min-width="140" show-overflow-tooltip />
      <el-table-column prop="location" label="地点" width="120" show-overflow-tooltip />
      <el-table-column label="开始时间" width="170"><template #default="{ row }">{{ formatTime(row.startTime) }}</template></el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.statusName }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link :loading="actingId === row.id" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > 0" class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" layout="total, prev, pager, next" background @current-change="fetchData" />
    </div>
  </PageShell>
</template>
