<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  JOIN_STATUS_OPTIONS,
  listAdminOrganizationsApi,
  updateOrgJoinStatusApi,
  updateOrgStatusApi,
  type AdminOrganization,
} from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const route = useRoute()
const loading = ref(false)
const loadError = ref<string | null>(null)
const records = ref<AdminOrganization[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const actingId = ref<number | null>(null)

const filters = reactive({
  joinStatus: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: '',
})

function joinTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  return 'info'
}

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(
      await listAdminOrganizationsApi({
        page: page.value,
        pageSize: pageSize.value,
        joinStatus: filters.joinStatus,
        status: filters.status,
        keyword: filters.keyword || undefined,
      }),
    )
    records.value = data.records
    total.value = data.total
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  filters.joinStatus = undefined
  filters.status = undefined
  filters.keyword = ''
  page.value = 1
  fetchData()
}

async function handleJoinStatus(row: AdminOrganization, joinStatus: number, actionLabel: string) {
  await ElMessageBox.confirm(`确定将「${row.name}」标记为「${actionLabel}」吗？`, '机构审核', { type: 'warning' })
  actingId.value = row.id
  try {
    unwrapApi(await updateOrgJoinStatusApi(row.id, joinStatus))
    ElMessage.success('操作成功')
    await fetchData()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  } finally {
    actingId.value = null
  }
}

async function handleToggleStatus(row: AdminOrganization) {
  const nextStatus = row.status === 1 ? 0 : 1
  const label = nextStatus === 1 ? '启用' : '停用'
  await ElMessageBox.confirm(`确定${label}机构「${row.name}」吗？`, '机构状态', { type: 'warning' })
  actingId.value = row.id
  try {
    unwrapApi(await updateOrgStatusApi(row.id, nextStatus))
    ElMessage.success(`机构已${label}`)
    await fetchData()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  } finally {
    actingId.value = null
  }
}

onMounted(() => {
  const joinStatus = route.query.joinStatus
  if (joinStatus !== undefined && joinStatus !== '') {
    filters.joinStatus = Number(joinStatus)
  }
  fetchData()
})
</script>

<template>
  <PageShell
    title="机构加盟"
    description="审核加盟申请，管理机构的启用与停用"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <div class="page-toolbar">
      <el-select v-model="filters.joinStatus" placeholder="加盟状态" clearable style="width: 140px">
        <el-option
          v-for="item in JOIN_STATUS_OPTIONS.filter((o) => o.value !== undefined)"
          :key="String(item.value)"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select v-model="filters.status" placeholder="机构状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-input v-model="filters.keyword" placeholder="机构名称/编码" clearable style="width: 200px" />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="page-table-wrap">
      <el-table :data="records" border stripe class="profile-data-table">
        <el-table-column prop="name" label="机构名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="code" label="编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="typeName" label="类型" width="100" />
        <el-table-column prop="contact" label="联系人" width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="120" show-overflow-tooltip />
        <el-table-column label="加盟状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="joinTagType(row.joinStatus)" size="small">{{ row.joinStatusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="机构状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template #default="{ row }">
            <div class="page-table-actions">
              <template v-if="row.joinStatus === 0">
                <el-button type="success" link :loading="actingId === row.id" @click="handleJoinStatus(row, 1, '已加盟')">通过</el-button>
                <el-button type="danger" link :loading="actingId === row.id" @click="handleJoinStatus(row, 2, '已退出')">驳回</el-button>
              </template>
              <el-button v-if="row.status === 1" type="warning" link :loading="actingId === row.id" @click="handleToggleStatus(row)">停用</el-button>
              <el-button v-else type="primary" link :loading="actingId === row.id" @click="handleToggleStatus(row)">启用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty v-if="!loading && records.length === 0" class="page-empty" :image-size="80" description="暂无机构数据" />

    <div v-if="total > 0" class="page-pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="fetchData"
        @size-change="() => { page = 1; fetchData() }"
      />
    </div>
  </PageShell>
</template>
