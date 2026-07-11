<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import PageShell from '@/components/common/PageShell.vue'
import { CREDIT_TYPE_OPTIONS } from '@/api/profile-credit'
import { listAdminCreditTransactionsApi, type AdminCreditTransaction } from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const records = ref<AdminCreditTransaction[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const filters = reactive({ type: undefined as number | undefined, userId: undefined as number | undefined })

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(await listAdminCreditTransactionsApi({ page: page.value, pageSize: pageSize.value, type: filters.type, userId: filters.userId }))
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
  <PageShell title="秩点监察" description="全平台秩点流水记录（只读）" :loading="loading" :error="loadError" @retry="fetchData">
    <div class="page-toolbar">
      <el-select v-model="filters.type" placeholder="流水类型" clearable style="width: 120px">
        <el-option v-for="item in CREDIT_TYPE_OPTIONS.filter((o) => o.value !== undefined)" :key="String(item.value)" :label="item.label" :value="item.value" />
      </el-select>
      <el-input-number v-model="filters.userId" placeholder="用户ID" :min="1" controls-position="right" />
      <el-button type="primary" @click="() => { page = 1; fetchData() }">查询</el-button>
    </div>
    <el-table :data="records" border stripe>
      <el-table-column prop="userName" label="用户" width="100" />
      <el-table-column prop="userId" label="用户ID" width="80" align="center" />
      <el-table-column prop="typeName" label="类型" width="80" align="center" />
      <el-table-column prop="amount" label="金额" width="90" align="right" />
      <el-table-column prop="balanceAfter" label="余额" width="90" align="right" />
      <el-table-column prop="bizType" label="业务类型" width="120" show-overflow-tooltip />
      <el-table-column prop="source" label="来源说明" min-width="160" show-overflow-tooltip />
      <el-table-column label="时间" width="170"><template #default="{ row }">{{ formatTime(row.createTime) }}</template></el-table-column>
    </el-table>
    <div v-if="total > 0" class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" layout="total, prev, pager, next" background @current-change="fetchData" />
    </div>
  </PageShell>
</template>
