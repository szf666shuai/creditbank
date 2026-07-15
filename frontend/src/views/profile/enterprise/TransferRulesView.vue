<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  listRulesApi,
  createRuleApi,
  updateRuleApi,
  deleteRuleApi,
  type CreditTransferRule,
  type CreditTransferRulePayload,
} from '@/api/credit-transfer'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const rules = ref<CreditTransferRule[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  sourceType: 1,
  sourceTags: '',
  targetType: 1,
  targetCourseId: '',
  targetAchievementId: '',
  targetOrgId: '',
  creditRatio: 1,
  description: '',
})

function resetForm() {
  form.sourceType = 1
  form.sourceTags = ''
  form.targetType = 1
  form.targetCourseId = ''
  form.targetAchievementId = ''
  form.targetOrgId = ''
  form.creditRatio = 1
  form.description = ''
  editingId.value = null
}

async function fetchRules() {
  loading.value = true
  loadError.value = null
  try {
    rules.value = unwrapApi(await listRulesApi())
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

function openEdit(row: CreditTransferRule) {
  editingId.value = row.id
  form.sourceType = row.sourceType || 1
  form.sourceTags = row.sourceTags || ''
  form.targetType = row.targetType || 1
  form.targetCourseId = row.targetCourseId ? String(row.targetCourseId) : ''
  form.targetAchievementId = row.targetAchievementId ? String(row.targetAchievementId) : ''
  form.targetOrgId = row.targetOrgId ? String(row.targetOrgId) : ''
  form.creditRatio = row.creditRatio || 1
  form.description = row.description || ''
  dialogVisible.value = true
}

async function handleDelete(row: CreditTransferRule) {
  if (!confirm(`确定删除转换规则「${row.description || '规则#' + row.id}」吗？`)) return
  try {
    await deleteRuleApi(row.id)
    ElMessage.success('规则已删除')
    await fetchRules()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '删除失败'))
  }
}

async function handleSubmit() {
  if (!form.sourceTags.trim()) {
    ElMessage.warning('请填写源标签')
    return
  }

  saving.value = true
  try {
    const payload: CreditTransferRulePayload = {
      sourceType: form.sourceType,
      sourceTags: form.sourceTags.trim(),
      targetType: form.targetType,
      targetCourseId: form.targetType === 1 && form.targetCourseId ? Number(form.targetCourseId) : undefined,
      targetAchievementId: form.targetType === 2 && form.targetAchievementId ? Number(form.targetAchievementId) : undefined,
      targetOrgId: form.targetOrgId ? Number(form.targetOrgId) : undefined,
      creditRatio: form.creditRatio,
      description: form.description,
      status: 1,
    }
    if (editingId.value) {
      unwrapApi(await updateRuleApi(editingId.value, payload))
      ElMessage.success('规则已更新')
    } else {
      unwrapApi(await createRuleApi(payload))
      ElMessage.success('规则已创建')
    }
    dialogVisible.value = false
    await fetchRules()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}

function targetDisplay(row: CreditTransferRule) {
  if (row.targetType === 2) {
    return row.targetAchievementId ? `成果#${row.targetAchievementId}` : '学习成果'
  }
  return row.targetCourseName || '-'
}

onMounted(fetchRules)
</script>

<template>
  <PageShell
    title="学分转换规则"
    description="设置本机构接受的其他机构课程或学习成果的转换规则，系统会根据标签进行匹配"
    :loading="loading"
    :error="loadError"
    @retry="fetchRules"
  >
    <template #actions>
      <el-button type="primary" @click="openCreate">新增规则</el-button>
    </template>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="当学员在其他机构完成课程或获得学习成果后，可以申请转换为本机构的等效学分。系统根据标签进行初步匹配，最终由本机构审核确认。"
      class="tip"
    />

    <el-table :data="rules" border stripe>
      <el-table-column label="源类型" width="100">
        <template #default="{ row }">{{ row.sourceTypeName }}</template>
      </el-table-column>
      <el-table-column prop="sourceTags" label="源匹配标签" min-width="160" show-overflow-tooltip />
      <el-table-column label="目标类型" width="100">
        <template #default="{ row }">{{ row.targetTypeName }}</template>
      </el-table-column>
      <el-table-column label="目标内容" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ targetDisplay(row) }}</template>
      </el-table-column>
      <el-table-column prop="targetOrgName" label="目标机构" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ row.targetOrgName || '本机构' }}</template>
      </el-table-column>
      <el-table-column label="转换比例" width="100">
        <template #default="{ row }">{{ (row.creditRatio || 1) * 100 }}%</template>
      </el-table-column>
      <el-table-column prop="description" label="规则说明" min-width="140" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatTime(row.updateTime || row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <div class="page-table-actions">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && rules.length === 0"
      class="page-empty"
      :image-size="80"
      description="尚未创建转换规则"
    >
      <el-button type="primary" @click="openCreate">创建第一条规则</el-button>
    </el-empty>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑转换规则' : '新增转换规则'"
      width="520px"
      destroy-on-close
    >
      <el-form label-width="100px">
        <el-form-item label="源类型">
          <el-radio-group v-model="form.sourceType">
            <el-radio :value="1">课程</el-radio>
            <el-radio :value="2">学习成果</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="源匹配标签" required>
          <el-input v-model="form.sourceTags" placeholder="多个标签用逗号分隔，如：编程,Java,后端" />
        </el-form-item>
        <el-form-item label="目标类型">
          <el-radio-group v-model="form.targetType">
            <el-radio :value="1">课程</el-radio>
            <el-radio :value="2">成果/证书</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.targetType === 1" label="目标课程ID">
          <el-input v-model="form.targetCourseId" type="number" placeholder="可选，关联本机构课程" />
        </el-form-item>
        <el-form-item v-if="form.targetType === 2" label="目标成果ID">
          <el-input v-model="form.targetAchievementId" type="number" placeholder="可选，关联本机构成果" />
        </el-form-item>
        <el-form-item label="目标机构ID">
          <el-input v-model="form.targetOrgId" type="number" placeholder="默认为本机构" />
        </el-form-item>
        <el-form-item label="转换比例">
          <el-input-number v-model="form.creditRatio" :min="0.1" :max="2" :step="0.1" />
          <span class="ml-2">倍（如 1 表示等额转换）</span>
        </el-form-item>
        <el-form-item label="规则说明">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<style scoped>
.tip {
  margin-bottom: 16px;
}

.ml-2 {
  margin-left: 8px;
}
</style>
