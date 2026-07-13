<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import PageShell from '@/components/common/PageShell.vue'
import {
  CREDIT_TYPE_OPTIONS,
  getCreditSummaryApi,
  listCreditTransactionsApi,
  type CreditAccountSummary,
  type CreditTransactionItem,
} from '@/api/profile-credit'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const summary = ref<CreditAccountSummary | null>(null)
const transactions = ref<CreditTransactionItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filters = reactive({
  type: undefined as number | undefined,
  dateRange: null as [string, string] | null,
})

function typeTagType(type: number) {
  if (type === 1) return 'success'
  if (type === 4) return 'danger'
  if (type === 2) return 'warning'
  return 'primary'
}

function amountClass(amount: number) {
  if (amount > 0) return 'amount-plus'
  if (amount < 0) return 'amount-minus'
  return ''
}

function formatAmount(amount: number) {
  const prefix = amount > 0 ? '+' : ''
  return `${prefix}${amount}`
}

async function fetchSummary() {
  try {
    summary.value = unwrapApi(await getCreditSummaryApi())
  } catch {
    summary.value = null
  }
}

async function fetchTransactions() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(
      await listCreditTransactionsApi({
        page: page.value,
        pageSize: pageSize.value,
        type: filters.type,
        startDate: filters.dateRange?.[0],
        endDate: filters.dateRange?.[1],
      }),
    )
    transactions.value = data.records
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
  fetchTransactions()
}

function handleReset() {
  filters.type = undefined
  filters.dateRange = null
  page.value = 1
  fetchTransactions()
}

function handlePageChange() {
  fetchTransactions()
}

function handleSizeChange() {
  page.value = 1
  fetchTransactions()
}

onMounted(async () => {
  await Promise.all([fetchSummary(), fetchTransactions()])
})
</script>

<template>
  <PageShell
    title="秩点流水"
    description="查看秩点获取、转换、增长与消耗记录"
    :loading="loading"
    :error="loadError"
    @retry="fetchTransactions"
  >
    <div v-if="summary" class="page-summary-row">
      <div class="page-summary-card">
        <div class="page-summary-value">{{ summary.balance }}</div>
        <div class="page-summary-label">当前余额</div>
      </div>
      <div class="page-summary-card">
        <div class="page-summary-value is-success">{{ summary.totalEarned }}</div>
        <div class="page-summary-label">累计获取</div>
      </div>
      <div class="page-summary-card">
        <div class="page-summary-value is-danger">{{ summary.totalSpent }}</div>
        <div class="page-summary-label">累计消耗</div>
      </div>
    </div>

    <div class="page-toolbar">
        <el-select v-model="filters.type" placeholder="流水类型" clearable style="width: 140px">
          <el-option
            v-for="item in CREDIT_TYPE_OPTIONS.filter((o) => o.value !== undefined)"
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

      <el-table :data="transactions" border stripe>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动秩点" width="110" align="right">
          <template #default="{ row }">
            <span :class="amountClass(row.amount)">{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" width="110" align="right">
          <template #default="{ row }">
            {{ row.balanceAfter ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源/用途" min-width="220" show-overflow-tooltip />
        <el-table-column prop="bizType" label="业务类型" width="140" show-overflow-tooltip />
      </el-table>

      <el-empty
        v-if="!loading && transactions.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无秩点流水"
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
.amount-plus {
  color: #4ade80;
  font-weight: 700;
}

.amount-minus {
  color: #fb7185;
  font-weight: 700;
}
</style>
