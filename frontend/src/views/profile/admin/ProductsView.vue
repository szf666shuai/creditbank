<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  PRODUCT_APPROVAL_OPTIONS,
  listAdminProductsApi,
  reviewAdminProductApi,
  type AdminMallProduct,
} from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const actingId = ref<number | null>(null)
const records = ref<AdminMallProduct[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filters = reactive({
  approvalStatus: 0 as number | undefined,
  keyword: '',
})

function approvalTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(
      await listAdminProductsApi({
        page: page.value,
        pageSize: pageSize.value,
        approvalStatus: filters.approvalStatus,
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
  filters.approvalStatus = 0
  filters.keyword = ''
  page.value = 1
  fetchData()
}

async function handleReview(row: AdminMallProduct, approvalStatus: number, actionLabel: string) {
  let reviewRemark: string | undefined
  if (approvalStatus === 2) {
    const result = await ElMessageBox.prompt('请填写驳回原因（可选）', `驳回「${row.name}」`, {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：商品描述不完整',
    }).catch(() => null)
    if (!result) return
    reviewRemark = result.value || undefined
  } else {
    await ElMessageBox.confirm(`确定通过「${row.name}」的审核吗？`, '商品审核', { type: 'success' })
  }

  actingId.value = row.id
  try {
    unwrapApi(await reviewAdminProductApi(row.id, { approvalStatus, reviewRemark }))
    ElMessage.success(`${actionLabel}成功`)
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
  <PageShell
    title="商品审核"
    description="审核企业提交的积分商城商品，通过后学员可见并可兑换"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="审核状态">
        <el-select v-model="filters.approvalStatus" clearable placeholder="全部" style="width: 140px">
          <el-option
            v-for="item in PRODUCT_APPROVAL_OPTIONS"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="filters.keyword" placeholder="商品名称" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="records" border stripe>
      <el-table-column prop="name" label="商品名称" min-width="150" />
      <el-table-column prop="orgName" label="发布企业" width="140" />
      <el-table-column prop="productTypeName" label="类型" width="110" />
      <el-table-column label="秩点价" width="90">
        <template #default="{ row }">{{ Number(row.priceCredit || 0).toFixed(0) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :type="approvalTagType(row.approvalStatus)" size="small">
            {{ row.approvalStatusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewRemark" label="审核备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="提交时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <template v-if="row.approvalStatus === 0">
            <el-button
              link
              type="success"
              :loading="actingId === row.id"
              @click="handleReview(row, 1, '通过审核')"
            >
              通过
            </el-button>
            <el-button
              link
              type="danger"
              :loading="actingId === row.id"
              @click="handleReview(row, 2, '驳回')"
            >
              驳回
            </el-button>
          </template>
          <span v-else class="page-text-muted">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </div>
  </PageShell>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
