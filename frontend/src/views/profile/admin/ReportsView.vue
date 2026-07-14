<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  REPORT_STATUS_OPTIONS,
  handleAdminReportApi,
  listAdminReportsApi,
  type AdminForumReport,
} from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const route = useRoute()
const loading = ref(false)
const loadError = ref<string | null>(null)
const records = ref<AdminForumReport[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const submitting = ref(false)
const dialogVisible = ref(false)
const currentReport = ref<AdminForumReport | null>(null)

const filters = reactive({ status: undefined as number | undefined })

const handleForm = reactive({
  status: 1 as 1 | 2,
  hideTarget: true,
  handleRemark: '',
})

const handlePresets = [
  { label: '确认违规，隐藏内容', status: 1 as const, hideTarget: true, remark: '经核实存在违规内容，已隐藏，并对作者扣减诚信分' },
  { label: '确认违规，仅标记', status: 1 as const, hideTarget: false, remark: '经核实存在违规，已记录处理，并对作者扣减诚信分' },
  { label: '举报不成立', status: 2 as const, hideTarget: false, remark: '经核查，举报理由不成立' },
  { label: '内容正常，驳回', status: 2 as const, hideTarget: false, remark: '内容符合社区规范，驳回举报' },
]

function openHandleDialog(row: AdminForumReport) {
  currentReport.value = row
  handleForm.status = 1
  handleForm.hideTarget = true
  handleForm.handleRemark = ''
  dialogVisible.value = true
}

function applyPreset(preset: (typeof handlePresets)[number]) {
  handleForm.status = preset.status
  handleForm.hideTarget = preset.hideTarget
  handleForm.handleRemark = preset.remark
}

async function submitHandle() {
  if (!currentReport.value) return
  if (!handleForm.handleRemark.trim()) {
    ElMessage.warning('请填写处理说明')
    return
  }
  submitting.value = true
  try {
    unwrapApi(
      await handleAdminReportApi(currentReport.value.id, {
        status: handleForm.status,
        hideTarget: handleForm.status === 1 ? handleForm.hideTarget : false,
        handleRemark: handleForm.handleRemark.trim(),
      }),
    )
    ElMessage.success(
      handleForm.status === 1 ? '举报已处理，已对违规内容作者扣减诚信分' : '举报已驳回',
    )
    dialogVisible.value = false
    await fetchData()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '处理失败'))
  } finally {
    submitting.value = false
  }
}

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(
      await listAdminReportsApi({ page: page.value, pageSize: pageSize.value, status: filters.status }),
    )
    records.value = data.records
    total.value = data.total
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (route.query.status !== undefined && route.query.status !== '') {
    filters.status = Number(route.query.status)
  }
  fetchData()
})
</script>

<template>
  <PageShell
    title="举报处理"
    description="审核论坛内容举报：确认违规将扣减作者诚信分（-5），并可选择隐藏内容"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <div class="page-toolbar">
      <el-select v-model="filters.status" placeholder="处理状态" clearable style="width: 140px">
        <el-option
          v-for="item in REPORT_STATUS_OPTIONS.filter((o) => o.value !== undefined)"
          :key="String(item.value)"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button type="primary" @click="() => { page = 1; fetchData() }">查询</el-button>
    </div>

    <el-table :data="records" border stripe>
      <el-table-column prop="reporterName" label="举报人" width="100" />
      <el-table-column prop="targetTypeName" label="类型" width="80" align="center" />
      <el-table-column prop="targetSummary" label="被举报内容" min-width="180" show-overflow-tooltip />
      <el-table-column prop="reason" label="举报原因" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleRemark" label="处理说明" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span :class="{ muted: !row.handleRemark }">{{ row.handleRemark || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="举报时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" link @click="openHandleDialog(row)">处理</el-button>
          <span v-else class="muted">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && records.length === 0" class="page-empty" :image-size="80" description="暂无举报记录" />

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

  <el-dialog v-model="dialogVisible" title="处理举报" width="560px" destroy-on-close>
    <div v-if="currentReport" class="report-preview">
      <p><strong>举报类型：</strong>{{ currentReport.targetTypeName }}</p>
      <p><strong>被举报内容：</strong>{{ currentReport.targetSummary }}</p>
      <p><strong>举报原因：</strong>{{ currentReport.reason }}</p>
    </div>

    <div class="preset-section">
      <div class="preset-label">快捷处理</div>
      <div class="preset-list">
        <el-button
          v-for="item in handlePresets"
          :key="item.label"
          size="small"
          @click="applyPreset(item)"
        >
          {{ item.label }}
        </el-button>
      </div>
    </div>

    <el-form label-width="100px" class="handle-form">
      <el-form-item label="处理结果" required>
        <el-radio-group v-model="handleForm.status">
          <el-radio :value="1">确认违规（已处理，扣作者诚信分 5 分）</el-radio>
          <el-radio :value="2">驳回举报</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="handleForm.status === 1" label="内容处置">
        <el-checkbox v-model="handleForm.hideTarget">同时隐藏被举报的帖子/回复</el-checkbox>
      </el-form-item>
      <el-form-item label="处理说明" required>
        <el-input
          v-model="handleForm.handleRemark"
          type="textarea"
          :rows="4"
          maxlength="255"
          show-word-limit
          placeholder="请填写处理依据或说明，将记录在案"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitHandle">提交处理</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.muted {
  color: var(--color-text-muted);
  font-size: 13px;
}

.report-preview {
  margin-bottom: 16px;
  padding: 12px 14px;
  background: var(--color-primary-light);
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.7;
}

.report-preview p {
  margin: 0 0 6px;
}

.preset-section {
  margin-bottom: 16px;
}

.preset-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.preset-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.handle-form {
  margin-top: 4px;
}
</style>
