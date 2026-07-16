<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import CertificatePreview from '@/components/learning/CertificatePreview.vue'
import {
  listMyApplicationsApi,
  applyTransferApi,
  matchRulesApi,
  type CreditTransferRule,
  type CreditTransferApplication,
  type CreditTransferApplyPayload,
} from '@/api/credit-transfer'
import {
  listLearningArchivesApi,
  listLearningAchievementsApi,
  type LearningArchiveItem,
  type LearningAchievementItem,
} from '@/api/profile-learning'
import { fetchLearningCertificates, type LearningCertificate } from '@/api/learning'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const submitting = ref(false)
const matching = ref(false)

const applications = ref<CreditTransferApplication[]>([])
const archives = ref<LearningArchiveItem[]>([])
const achievements = ref<LearningAchievementItem[]>([])
const certificates = ref<LearningCertificate[]>([])
const matchedRules = ref<CreditTransferRule[]>([])

const selectedOrgId = ref<number | null>(null)
const selectedTargetKey = ref<string | null>(null)
const selectedRuleId = ref<number | null>(null)

const certDialogVisible = ref(false)
const previewCertificate = ref<LearningCertificate | null>(null)
const loadingCert = ref(false)

const applyDialogVisible = ref(false)
const sourceType = ref(1)
const selectedSourceId = ref<number | null>(null)
const selectedSourceTitle = ref('')

const form = reactive({
  applyReason: '',
})

const completedCourses = computed(() =>
  archives.value.filter(
    (a) => (a.archiveType === 1 || a.archiveTypeName?.includes('课程')) && a.status === 1,
  ),
)

const verifiedAchievements = computed(() =>
  achievements.value.filter((a) => a.verifyStatus === 1),
)

const courseCertMap = computed(() => {
  const map = new Map<number, number>()
  archives.value.forEach((a) => {
    if (a.courseId && a.certificateId) {
      map.set(a.courseId, a.certificateId)
    }
  })
  return map
})

const selectedRule = computed(() =>
  matchedRules.value.find((r) => r.id === selectedRuleId.value) || null,
)

const orgOptions = computed(() => {
  const map = new Map<number, string>()
  matchedRules.value.forEach((r) => {
    const id = r.targetOrgId || r.orgId
    if (!id) return
    map.set(id, r.orgName || r.targetOrgName || `机构#${id}`)
  })
  return [...map.entries()]
    .map(([value, label]) => ({ value, label }))
    .sort((a, b) => a.label.localeCompare(b.label, 'zh-CN'))
})

const targetOptions = computed(() => {
  if (!selectedOrgId.value) return [] as Array<{ value: string; label: string; ruleId: number }>
  const rules = matchedRules.value.filter(
    (r) => (r.targetOrgId || r.orgId) === selectedOrgId.value,
  )
  const options: Array<{ value: string; label: string; ruleId: number }> = []
  const seenCourses = new Set<number>()

  for (const rule of rules) {
    if (rule.targetType === 1 && rule.targetCourseId) {
      if (seenCourses.has(rule.targetCourseId)) continue
      seenCourses.add(rule.targetCourseId)
      options.push({
        value: `course:${rule.targetCourseId}`,
        label: rule.targetCourseName || `课程#${rule.targetCourseId}`,
        ruleId: rule.id,
      })
    } else if (rule.targetType === 2) {
      options.push({
        value: `rule:${rule.id}`,
        label: rule.targetAchievementTitle
          || (rule.targetAchievementId ? `成果#${rule.targetAchievementId}` : '学习成果 / 证书'),
        ruleId: rule.id,
      })
    }
  }
  return options
})

watch(selectedOrgId, () => {
  selectedTargetKey.value = null
  selectedRuleId.value = null
})

watch(selectedTargetKey, (key) => {
  if (!key) {
    selectedRuleId.value = null
    return
  }
  const opt = targetOptions.value.find((o) => o.value === key)
  selectedRuleId.value = opt?.ruleId ?? null
})

function blurLinkButton(e: Event) {
  const el = e.currentTarget
  if (el instanceof HTMLElement) {
    requestAnimationFrame(() => el.blur())
  }
}

function statusTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
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

function ruleTargetText(rule: CreditTransferRule) {
  if (rule.targetType === 2) {
    return rule.targetAchievementTitle || (rule.targetAchievementId ? `成果#${rule.targetAchievementId}` : '学习成果/证书')
  }
  return rule.targetCourseName || (rule.targetCourseId ? `课程#${rule.targetCourseId}` : '本机构课程学分')
}

function ruleOrgText(rule: CreditTransferRule) {
  return rule.orgName || rule.targetOrgName || `机构#${rule.orgId}`
}

function ratioText(ratio?: number) {
  return `${Math.round(Number(ratio ?? 1) * 100)}%`
}

function resetApplySelection() {
  selectedOrgId.value = null
  selectedTargetKey.value = null
  selectedRuleId.value = null
  form.applyReason = ''
}

async function ensureCertificates() {
  if (certificates.value.length) return certificates.value
  const res = await fetchLearningCertificates(100)
  certificates.value = unwrapApi(res)
  return certificates.value
}

async function openCertificateByCourseId(courseId: number) {
  const certId = courseCertMap.value.get(courseId)
  if (!certId) {
    ElMessage.warning('该课程暂无学习证书')
    return
  }
  loadingCert.value = true
  try {
    const list = await ensureCertificates()
    const cert = list.find((item) => item.id === certId)
    if (!cert) {
      ElMessage.error('未找到对应合格证，请刷新后重试')
      return
    }
    previewCertificate.value = cert
    certDialogVisible.value = true
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '加载证书失败'))
  } finally {
    loadingCert.value = false
  }
}

function openAchievementFile(fileUrl: string) {
  if (fileUrl) {
    window.open(fileUrl, '_blank')
  } else {
    ElMessage.warning('该成果暂无附件')
  }
}

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const [appRes, archiveRes, achievementRes] = await Promise.all([
      listMyApplicationsApi(),
      listLearningArchivesApi(),
      listLearningAchievementsApi(),
    ])
    applications.value = unwrapApi(appRes)
    archives.value = unwrapApi(archiveRes)
    achievements.value = unwrapApi(achievementRes)
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function openApplyForCourse(courseId: number, courseName: string) {
  if (!courseId) {
    ElMessage.warning('该档案未关联课程，无法申请转换')
    return
  }
  matching.value = true
  try {
    sourceType.value = 1
    selectedSourceId.value = courseId
    selectedSourceTitle.value = courseName
    resetApplySelection()
    matchedRules.value = unwrapApi(await matchRulesApi(1, courseId))
    applyDialogVisible.value = true
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '加载可转入机构失败'))
  } finally {
    matching.value = false
  }
}

async function openApplyForAchievement(achievementId: number, title: string) {
  matching.value = true
  try {
    sourceType.value = 2
    selectedSourceId.value = achievementId
    selectedSourceTitle.value = title
    resetApplySelection()
    matchedRules.value = unwrapApi(await matchRulesApi(2, undefined, achievementId))
    applyDialogVisible.value = true
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '加载可转入机构失败'))
  } finally {
    matching.value = false
  }
}

async function handleApply() {
  if (!selectedSourceId.value) {
    ElMessage.warning('请选择源课程或成果')
    return
  }
  if (!selectedOrgId.value) {
    ElMessage.warning('请选择要转入的机构')
    return
  }
  if (!selectedTargetKey.value) {
    ElMessage.warning('请选择要转入的课程')
    return
  }
  const rule = selectedRule.value
  if (!rule) {
    ElMessage.warning('未找到对应转换规则')
    return
  }
  const targetOrgId = rule.targetOrgId || rule.orgId
  if (!targetOrgId) {
    ElMessage.warning('该规则缺少目标机构，请联系机构管理员')
    return
  }
  if (!form.applyReason.trim()) {
    ElMessage.warning('请填写转入理由')
    return
  }

  submitting.value = true
  try {
    const payload: CreditTransferApplyPayload = {
      sourceType: sourceType.value,
      sourceCourseId: sourceType.value === 1 ? selectedSourceId.value : undefined,
      sourceAchievementId: sourceType.value === 2 ? selectedSourceId.value : undefined,
      targetType: rule.targetType || 1,
      targetCourseId: rule.targetType === 1 ? rule.targetCourseId : undefined,
      targetAchievementId: rule.targetType === 2 ? rule.targetAchievementId : undefined,
      targetOrgId,
      applyReason: form.applyReason.trim(),
    }
    unwrapApi(await applyTransferApi(payload))
    ElMessage.success('申请已提交，等待目标机构审核')
    applyDialogVisible.value = false
    await fetchData()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '提交失败'))
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <PageShell
    title="学分转换"
    description="选择已完成的课程或成果，再选择目标机构与转入课程提交申请；对方机构将人工审核（可辅以 AI 初筛）"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip"
      title="各机构只公布可接收的目标课程。先选转入机构，再选该机构课程；系统会自动带出该课程唯一的转换规则。"
    />

    <section class="page-section">
      <h2>我的转换申请</h2>
      <el-table :data="applications" border stripe>
        <el-table-column label="源" min-width="140" show-overflow-tooltip>
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
        <el-table-column label="目标" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <div>
              <div>{{ targetDisplay(row) }}</div>
              <el-tag size="small" type="warning" style="margin-top: 4px">{{ row.targetTypeName }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="targetOrgName" label="目标机构" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.targetOrgName || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实际转换学分" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.actualCredit">{{ Number(row.actualCredit).toFixed(2) }}</span>
            <span v-else class="page-text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ formatTime(row.applyTime) }}</template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && applications.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无转换申请"
      />
    </section>

    <section class="page-section">
      <h2>可申请转换的内容</h2>
      <el-tabs v-model="sourceType">
        <el-tab-pane label="课程学分" :name="1">
          <el-alert
            type="warning"
            :closable="false"
            title="请选择已完成的课程发起申请，再选择目标机构与转入课程。"
            class="mb-3"
          />
          <el-table :data="completedCourses" border stripe>
            <el-table-column prop="title" label="课程名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="获得学分" width="100" align="right">
              <template #default="{ row }">{{ Number(row.creditEarned || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="获得时间" width="160">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="学习成果" width="100" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.certificateId"
                  link
                  type="primary"
                  @click="(e: MouseEvent) => { openCertificateByCourseId(row.courseId || 0); blurLinkButton(e) }"
                >
                  查看证书
                </el-button>
                <span v-else class="page-text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  :loading="matching"
                  @click="(e: MouseEvent) => { openApplyForCourse(row.courseId || 0, row.title); blurLinkButton(e) }"
                >
                  申请转换
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!loading && completedCourses.length === 0"
            class="page-empty"
            :image-size="80"
            description="暂无可转换的课程"
          />
        </el-tab-pane>

        <el-tab-pane label="学习成果" :name="2">
          <el-table :data="verifiedAchievements" border stripe>
            <el-table-column prop="title" label="成果名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="typeName" label="类型" width="100" />
            <el-table-column label="对应学分" width="100" align="right">
              <template #default="{ row }">{{ Number(row.creditValue || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="orgName" label="颁发机构" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.orgName || '-' }}</template>
            </el-table-column>
            <el-table-column label="附件" width="100" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.fileUrl"
                  link
                  type="primary"
                  @click="(e: MouseEvent) => { openAchievementFile(row.fileUrl); blurLinkButton(e) }"
                >
                  查看附件
                </el-button>
                <span v-else class="page-text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  :loading="matching"
                  @click="(e: MouseEvent) => { openApplyForAchievement(row.id, row.title); blurLinkButton(e) }"
                >
                  申请转换
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!loading && verifiedAchievements.length === 0"
            class="page-empty"
            :image-size="80"
            description="暂无可转换的学习成果"
          />
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog
      v-model="certDialogVisible"
      title="学习证书"
      width="860px"
      destroy-on-close
      append-to-body
    >
      <CertificatePreview v-if="previewCertificate" :certificate="previewCertificate" />
    </el-dialog>

    <el-dialog
      v-model="applyDialogVisible"
      title="申请学分转换"
      width="560px"
      destroy-on-close
      class="apply-dialog"
    >
      <div class="source-banner">
        <span class="source-banner__label">申请转入来源</span>
        <strong class="source-banner__title">{{ selectedSourceTitle }}</strong>
      </div>

      <template v-if="matchedRules.length > 0">
        <el-form label-position="top" class="apply-form">
          <el-form-item label="转入机构" required>
            <el-select
              v-model="selectedOrgId"
              filterable
              clearable
              placeholder="请选择要转入的机构"
              style="width: 100%"
            >
              <el-option
                v-for="opt in orgOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="转入课程" required>
            <el-select
              v-model="selectedTargetKey"
              filterable
              clearable
              :disabled="!selectedOrgId"
              :placeholder="selectedOrgId ? '请选择该机构可转入的课程' : '请先选择机构'"
              style="width: 100%"
            >
              <el-option
                v-for="opt in targetOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <div v-if="selectedRule" class="rule-panel">
          <div class="rule-panel__head">
            <span class="rule-panel__badge">已匹配规则</span>
            <span class="rule-panel__ratio">{{ ratioText(selectedRule.creditRatio) }}</span>
          </div>

          <dl class="rule-panel__meta">
            <div>
              <dt>接收机构</dt>
              <dd>{{ ruleOrgText(selectedRule) }}</dd>
            </div>
            <div>
              <dt>转入目标</dt>
              <dd>{{ ruleTargetText(selectedRule) }}</dd>
            </div>
            <div>
              <dt>目标类型</dt>
              <dd>{{ selectedRule.targetTypeName || (selectedRule.targetType === 2 ? '成果/证书' : '课程') }}</dd>
            </div>
            <div>
              <dt>换算说明</dt>
              <dd>{{ Number(selectedRule.creditRatio ?? 1) === 1 ? '等额转换' : `${ratioText(selectedRule.creditRatio)} 折算` }}</dd>
            </div>
          </dl>

          <div class="rule-panel__desc">
            <div class="rule-panel__desc-label">
              规则说明
              <span>审核与 AI 初筛主要依据</span>
            </div>
            <p>{{ selectedRule.description || '该机构未填写规则说明' }}</p>
          </div>
        </div>

        <div v-else-if="selectedOrgId && selectedTargetKey" class="rule-empty">
          未找到该课程对应的转换规则
        </div>
        <div v-else class="rule-placeholder">
          选择机构与课程后，将自动显示该课程唯一的转换规则
        </div>

        <el-form label-position="top" class="reason-form">
          <el-form-item label="转入理由" required>
            <el-input
              v-model="form.applyReason"
              type="textarea"
              :rows="3"
              maxlength="300"
              show-word-limit
              placeholder="请说明为何申请转入该课程，便于机构审核与 AI 初筛"
            />
          </el-form-item>
        </el-form>
      </template>

      <el-empty
        v-else
        :image-size="72"
        description="暂无其他机构可接收的转换规则"
      >
        <p class="empty-hint">
          常见原因：课程与规则同属一个机构（不能转给自己），或其他机构尚未公布接收规则。
        </p>
      </el-empty>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="!selectedRule || !form.applyReason.trim()"
          @click="handleApply"
        >
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<style scoped>
.tip {
  margin-bottom: 16px;
}

.page-section {
  margin-bottom: 24px;
}

.page-section h2 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.mb-3 {
  margin-bottom: 12px;
}

.source-banner {
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1.5px solid #d1d5db;
  background: linear-gradient(135deg, #f8fafc 0%, #f0fdf4 100%);
}

.source-banner__label {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 6px;
}

.source-banner__title {
  display: block;
  font-size: 16px;
  line-height: 1.4;
  color: #111827;
}

.apply-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.apply-form :deep(.el-form-item__label) {
  font-weight: 700;
  color: #111827;
}

.rule-panel {
  margin: 4px 0 16px;
  padding: 16px 18px;
  border-radius: 14px;
  border: 2px solid #1a202c;
  background: #fff;
  box-shadow: 3px 3px 0 0 #1a202c;
}

.rule-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.rule-panel__badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  color: #166534;
  background: #bbf7d0;
  border: 1.5px solid #1a202c;
}

.rule-panel__ratio {
  font-size: 22px;
  font-weight: 900;
  color: #16a34a;
  letter-spacing: -0.02em;
}

.rule-panel__meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 14px;
  margin: 0 0 14px;
}

.rule-panel__meta dt {
  margin: 0 0 2px;
  font-size: 11px;
  font-weight: 700;
  color: #6b7280;
  text-transform: none;
}

.rule-panel__meta dd {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: #111827;
  line-height: 1.4;
  word-break: break-word;
}

.rule-panel__desc {
  padding: 12px 14px;
  border-radius: 10px;
  background: #f0fdf4;
  border: 1.5px solid #86efac;
}

.rule-panel__desc-label {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 800;
  color: #166534;
}

.rule-panel__desc-label span {
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
}

.rule-panel__desc p {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.55;
  color: #111827;
  white-space: pre-wrap;
  word-break: break-word;
}

.rule-placeholder,
.rule-empty {
  margin: 0 0 16px;
  padding: 18px 16px;
  text-align: center;
  font-size: 13px;
  color: #6b7280;
  border: 1.5px dashed #d1d5db;
  border-radius: 12px;
  background: #fafafa;
}

.rule-empty {
  color: #b45309;
  border-color: #fcd34d;
  background: #fffbeb;
}

.reason-form :deep(.el-form-item__label) {
  font-weight: 700;
}

.empty-hint {
  margin: 8px auto 0;
  max-width: 420px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

@media (max-width: 520px) {
  .rule-panel__meta {
    grid-template-columns: 1fr;
  }
}
</style>
