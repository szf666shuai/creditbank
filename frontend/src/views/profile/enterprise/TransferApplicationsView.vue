<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  listApplicationsApi,
  reviewApplicationApi,
  type CreditTransferApplication,
} from '@/api/credit-transfer'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const actingId = ref<number | null>(null)
const applications = ref<CreditTransferApplication[]>([])

const filters = reactive({
  status: 0 as number | undefined,
})

function statusTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

function suggestionLabel(suggestion?: string) {
  if (suggestion === 'approve') return '建议通过'
  if (suggestion === 'reject') return '建议驳回'
  if (suggestion === 'uncertain') return '存疑'
  return ''
}

function suggestionTagType(suggestion?: string) {
  if (suggestion === 'approve') return 'success'
  if (suggestion === 'reject') return 'danger'
  return 'warning'
}

function sourceDisplay(row: CreditTransferApplication) {
  if (row.sourceType === 2 && row.sourceAchievementTitle) {
    return row.sourceAchievementTitle
  }
  return row.sourceCourseName || '-'
}

function targetDisplay(row: CreditTransferApplication) {
  if (row.targetType === 2) {
    return row.targetAchievementTitle || '学习成果'
  }
  return row.targetCourseName || '-'
}

async function fetchApplications() {
  loading.value = true
  loadError.value = null
  try {
    applications.value = unwrapApi(await listApplicationsApi(filters.status))
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  fetchApplications()
}

async function handleReview(row: CreditTransferApplication, status: number) {
  if (status === 2) {
    const result = await ElMessageBox.prompt('请填写驳回原因（可选）', `驳回申请`, {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
    }).catch(() => null)
    if (!result) return
    const comment = result.value || undefined
    actingId.value = row.id
    try {
      unwrapApi(await reviewApplicationApi(row.id, status, comment))
      ElMessage.success('已驳回')
      await fetchApplications()
    } catch (e) {
      ElMessage.error(getErrorMessage(e, '操作失败'))
    } finally {
      actingId.value = null
    }
  } else {
    await ElMessageBox.confirm(`确定通过「${row.userName}」的转换申请吗？`, '审核确认', { type: 'success' })
    actingId.value = row.id
    try {
      unwrapApi(await reviewApplicationApi(row.id, status))
      ElMessage.success('已通过')
      await fetchApplications()
    } catch (e) {
      ElMessage.error(getErrorMessage(e, '操作失败'))
    } finally {
      actingId.value = null
    }
  }
}

onMounted(fetchApplications)
</script>

<template>
  <PageShell
    title="学分转换申请"
    description="审核学员提交的外部学分转入申请。学员提交时系统已自动完成 AI 初筛，可直接对照建议进行人工复核。"
    :loading="loading"
    :error="loadError"
    @retry="fetchApplications"
  >
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip"
      title="AI 初筛在学员提交申请时自动生成，仅供参考；最终以人工审核为准。"
    />

    <el-form :inline="true" class="filter-form" @submit.prevent="handleFilter">
      <el-form-item label="审核状态">
        <el-select v-model="filters.status" clearable placeholder="全部" style="width: 140px">
          <el-option :label="status === 0 ? '待审核' : status === 1 ? '已通过' : '已驳回'" :value="status" v-for="status in [0, 1, 2]" :key="status" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleFilter">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="applications" border stripe>
      <el-table-column prop="userName" label="申请人" min-width="100" />
      <el-table-column label="源内容" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          <div>
            <div>{{ sourceDisplay(row) }}</div>
            <el-tag size="small" type="info" style="margin-top: 4px">{{ row.sourceTypeName }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="sourceOrgName" label="来源机构" min-width="120" show-overflow-tooltip />
      <el-table-column label="源学分" width="80" align="right">
        <template #default="{ row }">{{ Number(row.sourceCredit || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="目标内容" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          <div>
            <div>{{ targetDisplay(row) }}</div>
            <el-tag size="small" type="warning" style="margin-top: 4px">{{ row.targetTypeName }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="applyReason" label="申请理由" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.applyReason || '-' }}</template>
      </el-table-column>
      <el-table-column label="AI 初筛" min-width="220">
        <template #default="{ row }">
          <div v-if="row.aiSuggestion" class="ai-hint">
            <el-tag :type="suggestionTagType(row.aiSuggestion)" size="small" effect="plain">
              {{ suggestionLabel(row.aiSuggestion) }}
            </el-tag>
            <p>{{ row.aiReason || '-' }}</p>
          </div>
          <span v-else class="page-text-muted">未生成</span>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审核备注" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ row.reviewComment || '-' }}</template>
      </el-table-column>
      <el-table-column label="申请时间" width="160">
        <template #default="{ row }">{{ formatTime(row.applyTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center">
        <template #default="{ row }">
          <div v-if="row.status === 0" class="page-table-actions">
            <el-button
              link
              type="success"
              :loading="actingId === row.id"
              @click="handleReview(row, 1)"
            >
              通过
            </el-button>
            <el-button
              link
              type="danger"
              :loading="actingId === row.id"
              @click="handleReview(row, 2)"
            >
              驳回
            </el-button>
          </div>
          <span v-else class="page-text-muted">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && applications.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无转换申请"
    />
  </PageShell>
</template>

<style scoped>
.tip {
  margin-bottom: 16px;
}

.filter-form {
  margin-bottom: 16px;
}

.ai-hint {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ai-hint p {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  white-space: normal;
}
</style>
