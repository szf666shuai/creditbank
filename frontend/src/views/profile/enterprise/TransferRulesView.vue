<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
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
import { listEnterpriseCoursesApi, type EnterpriseCourse } from '@/api/enterprise-course'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const rules = ref<CreditTransferRule[]>([])
const myCourses = ref<EnterpriseCourse[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  targetType: 1,
  targetCourseId: undefined as number | undefined,
  targetAchievementId: '',
  creditRatio: 1,
  description: '',
})

const enabledRules = computed(() => rules.value.filter((r) => r.status === 1))

const usedCourseIds = computed(() => {
  const set = new Set<number>()
  rules.value.forEach((r) => {
    if (r.status === 1 && r.targetType === 1 && r.targetCourseId) {
      if (editingId.value && r.id === editingId.value) return
      set.add(r.targetCourseId)
    }
  })
  return set
})

const courseOptions = computed(() =>
  myCourses.value
    .filter((c) => c.approvalStatus === 1 || c.status === 1)
    .map((c) => {
      const taken = usedCourseIds.value.has(c.id)
      return {
        label: taken
          ? `${c.title}（学分 ${Number(c.creditValue || 0).toFixed(1)} · 已有规则）`
          : `${c.title}（学分 ${Number(c.creditValue || 0).toFixed(1)}）`,
        value: c.id,
        disabled: taken,
      }
    }),
)

function resetForm() {
  form.targetType = 1
  form.targetCourseId = undefined
  form.targetAchievementId = ''
  form.creditRatio = 1
  form.description = ''
  editingId.value = null
}

async function fetchRules() {
  loading.value = true
  loadError.value = null
  try {
    const [ruleList, courseList] = await Promise.all([
      listRulesApi(),
      listEnterpriseCoursesApi(),
    ])
    rules.value = unwrapApi(ruleList)
    myCourses.value = unwrapApi(courseList)
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
  form.targetType = row.targetType || 1
  form.targetCourseId = row.targetCourseId || undefined
  form.targetAchievementId = row.targetAchievementId ? String(row.targetAchievementId) : ''
  form.creditRatio = Number(row.creditRatio || 1)
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
  if (form.targetType === 1 && !form.targetCourseId) {
    ElMessage.warning('请选择目标课程')
    return
  }
  if (!form.description.trim()) {
    ElMessage.warning('请填写规则说明，便于人工/AI 审核时对照')
    return
  }

  saving.value = true
  try {
    const payload: CreditTransferRulePayload = {
      targetType: form.targetType,
      targetCourseId: form.targetType === 1 ? form.targetCourseId : undefined,
      targetAchievementId:
        form.targetType === 2 && form.targetAchievementId
          ? Number(form.targetAchievementId)
          : undefined,
      creditRatio: form.creditRatio,
      description: form.description.trim(),
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
    return row.targetAchievementId ? `成果#${row.targetAchievementId}` : '学习成果 / 证书'
  }
  return row.targetCourseName || (row.targetCourseId ? `课程#${row.targetCourseId}` : '-')
}

function ratioText(ratio?: number) {
  const value = Number(ratio ?? 1)
  return `${(value * 100).toFixed(0)}%`
}

onMounted(fetchRules)
</script>

<template>
  <PageShell
    title="学分转换规则"
    description="配置本机构可接收的转入目标课程与换算比例。外部学分是否等价，由人工审核或 AI 初筛判断。"
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
      class="tip"
      title="本机构只需声明「接受转入到哪门课程、按何比例」。同一课程仅允许一条启用规则。学员提交后，在「学分转换申请」中人工审核，也可使用 AI 初筛辅助判断。"
    />

    <section v-if="enabledRules.length" class="rule-showcase">
      <h3 class="showcase-title">本机构接收规则一览（启用中 {{ enabledRules.length }} 条）</h3>
      <div class="rule-grid">
        <article v-for="row in enabledRules" :key="`card-${row.id}`" class="rule-card">
          <div class="rule-card__head">
            <el-tag size="small" type="success">启用</el-tag>
            <span class="ratio">{{ ratioText(row.creditRatio) }}</span>
          </div>
          <h4>{{ row.description || '未命名规则' }}</h4>
          <p>
            <strong>接收目标：</strong>{{ row.targetTypeName || '课程' }}
            · {{ targetDisplay(row) }}
          </p>
          <p class="muted">更新于 {{ formatTime(row.updateTime || row.createTime) }}</p>
          <div class="rule-card__actions">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </div>
        </article>
      </div>
    </section>

    <el-table :data="rules" border stripe class="rule-table">
      <el-table-column label="目标类型" width="120">
        <template #default="{ row }">{{ row.targetTypeName }}</template>
      </el-table-column>
      <el-table-column label="接收目标" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ targetDisplay(row) }}</template>
      </el-table-column>
      <el-table-column label="转换比例" width="100" align="center">
        <template #default="{ row }">{{ ratioText(row.creditRatio) }}</template>
      </el-table-column>
      <el-table-column prop="description" label="规则说明" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.statusName }}
          </el-tag>
        </template>
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
      width="560px"
      destroy-on-close
    >
      <el-form label-width="110px">
        <el-form-item label="目标类型">
          <el-radio-group v-model="form.targetType">
            <el-radio :value="1">本机构课程</el-radio>
            <el-radio :value="2">成果 / 证书</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.targetType === 1" label="目标课程" required>
          <el-select
            v-model="form.targetCourseId"
            filterable
            clearable
            placeholder="选择本机构课程"
            style="width: 100%"
          >
            <el-option
              v-for="opt in courseOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
              :disabled="opt.disabled"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-else label="目标成果ID">
          <el-input v-model="form.targetAchievementId" type="number" placeholder="可选，关联本机构成果" />
        </el-form-item>
        <el-form-item label="转换比例">
          <el-input-number v-model="form.creditRatio" :min="0.1" :max="2" :step="0.1" />
          <span class="ml-2">倍（1 = 等额转换）</span>
        </el-form-item>
        <el-form-item label="规则说明" required>
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="例如：接受外部同类专业课程转入本机构认证课，需覆盖核心知识点"
          />
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

.showcase-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
}

.rule-showcase {
  margin-bottom: 20px;
}

.rule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.rule-card {
  border: 1px solid var(--el-border-color);
  border-radius: 10px;
  padding: 14px 16px;
  background: var(--el-bg-color);
}

.rule-card h4 {
  margin: 8px 0 10px;
  font-size: 15px;
  line-height: 1.4;
}

.rule-card p {
  margin: 0 0 6px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.rule-card .muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.rule-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.rule-card__head .ratio {
  font-weight: 700;
  color: var(--el-color-primary);
}

.rule-card__actions {
  margin-top: 8px;
}

.rule-table {
  margin-top: 8px;
}
</style>
