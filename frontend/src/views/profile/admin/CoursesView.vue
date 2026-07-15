<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  COURSE_APPROVAL_OPTIONS,
  listAdminCoursesApi,
  reviewAdminCourseApi,
  type AdminCourse,
} from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const actingId = ref<number | null>(null)
const records = ref<AdminCourse[]>([])
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
      await listAdminCoursesApi({
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

async function handleReview(row: AdminCourse, approvalStatus: number, actionLabel: string) {
  let reviewRemark: string | undefined
  if (approvalStatus === 2) {
    const result = await ElMessageBox.prompt('请填写驳回原因（可选）', `驳回「${row.title}」`, {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：课程描述不完整',
    }).catch(() => null)
    if (!result) return
    reviewRemark = result.value || undefined
  } else {
    await ElMessageBox.confirm(`确定通过「${row.title}」的审核吗？`, '课程审核', { type: 'success' })
  }

  actingId.value = row.id
  try {
    unwrapApi(await reviewAdminCourseApi(row.id, { approvalStatus, reviewRemark }))
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
    title="课程审核"
    description="审核企业提交的学习课程，通过后学员可见并可学习"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="审核状态">
        <el-select v-model="filters.approvalStatus" clearable placeholder="全部" style="width: 140px">
          <el-option
            v-for="item in COURSE_APPROVAL_OPTIONS"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="filters.keyword" placeholder="课程名称" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="page-table-wrap">
      <el-table :data="records" border stripe class="profile-data-table">
        <el-table-column prop="title" label="课程名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="orgName" label="发布机构" min-width="140" show-overflow-tooltip />
        <el-table-column label="学分值" width="90" align="right">
          <template #default="{ row }">{{ Number(row.creditValue || 0).toFixed(0) }}</template>
        </el-table-column>
        <el-table-column label="时长(分钟)" width="100" align="center">
          <template #default="{ row }">{{ row.duration }}</template>
        </el-table-column>
        <el-table-column prop="difficultyName" label="难度" width="80" />
        <el-table-column label="审核状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="approvalTagType(row.approvalStatus)" size="small">
              {{ row.approvalStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewRemark" label="审核备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="提交时间" width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <div v-if="row.approvalStatus === 0" class="page-table-actions">
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
            </div>
            <span v-else class="page-text-muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="page-pagination">
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
</style>