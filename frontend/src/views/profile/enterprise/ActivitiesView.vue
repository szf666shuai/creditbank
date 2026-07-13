<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  listMyActivitiesApi,
  createActivityApi,
  updateActivityApi,
  cancelActivityApi,
  type ActivityManageItem,
} from '@/api/enterprise-activities'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const activities = ref<ActivityManageItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  title: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: undefined as number | undefined,
  creditReward: undefined as number | undefined,
})

function resetForm() {
  form.title = ''
  form.description = ''
  form.location = ''
  form.startTime = ''
  form.endTime = ''
  form.maxParticipants = undefined
  form.creditReward = undefined
  editingId.value = null
}

function toInputTime(value?: string) {
  if (!value) return ''
  return value.slice(0, 16)
}

function toApiTime(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

function statusTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === 3) return 'info'
  return 'danger'
}

async function fetchActivities() {
  loading.value = true
  loadError.value = null
  try {
    activities.value = unwrapApi(await listMyActivitiesApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: ActivityManageItem) {
  editingId.value = row.id
  form.title = row.title
  form.description = row.description || ''
  form.location = row.location || ''
  form.startTime = toInputTime(row.startTime)
  form.endTime = toInputTime(row.endTime)
  form.maxParticipants = row.maxParticipants
  form.creditReward = row.creditReward
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('请填写活动名称')
    return
  }
  if (!form.startTime || !form.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title.trim(),
      description: form.description,
      location: form.location,
      startTime: toApiTime(form.startTime),
      endTime: toApiTime(form.endTime),
      maxParticipants: form.maxParticipants,
      creditReward: form.creditReward,
    }
    if (editingId.value) {
      unwrapApi(await updateActivityApi(editingId.value, payload))
    } else {
      unwrapApi(await createActivityApi(payload))
    }
    ElMessage.success(editingId.value ? '活动已更新' : '活动已发布')
    dialogVisible.value = false
    await fetchActivities()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function handleCancel(row: ActivityManageItem) {
  await ElMessageBox.confirm(`确定取消「${row.title}」吗？`, '取消活动', { type: 'warning' })
  try {
    unwrapApi(await cancelActivityApi(row.id))
    ElMessage.success('活动已取消')
    await fetchActivities()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  }
}

onMounted(fetchActivities)
</script>

<template>
  <PageShell
    title="活动管理"
    description="发布、编辑和取消本企业活动"
    :loading="loading"
    :error="loadError"
    @retry="fetchActivities"
  >
    <template #actions>
      <el-button type="primary" @click="openCreate">发布活动</el-button>
    </template>

    <el-table :data="activities" border stripe>
      <el-table-column prop="title" label="活动名称" min-width="160" />
      <el-table-column label="时间" min-width="220">
        <template #default="{ row }">
          {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="location" label="地点" width="140" />
      <el-table-column label="人数上限" width="100">
        <template #default="{ row }">{{ row.maxParticipants ?? '不限' }}</template>
      </el-table-column>
      <el-table-column label="奖励秩点" width="100">
        <template #default="{ row }">{{ row.creditReward ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status !== 0" link type="primary" @click="openEdit(row)">
            编辑
          </el-button>
          <el-button v-if="row.status !== 0" link type="danger" @click="handleCancel(row)">
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && activities.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无活动，点击发布活动开始"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑活动' : '发布活动'"
      width="640px"
      destroy-on-close
    >
      <el-form label-width="88px">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.title" placeholder="如：企业开放日" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="活动地点">
          <el-input v-model="form.location" placeholder="如：上海 / 线上" />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="form.maxParticipants" :min="1" :max="99999" />
        </el-form-item>
        <el-form-item label="奖励秩点">
          <el-input-number v-model="form.creditReward" :min="0" :max="9999" :precision="2" />
        </el-form-item>
        <el-form-item label="活动详情">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>
