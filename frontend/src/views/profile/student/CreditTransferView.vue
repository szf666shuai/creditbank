<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
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

const applications = ref<CreditTransferApplication[]>([])
const archives = ref<LearningArchiveItem[]>([])
const achievements = ref<LearningAchievementItem[]>([])
const certificates = ref<LearningCertificate[]>([])
const matchedRules = ref<CreditTransferRule[]>([])

const certDialogVisible = ref(false)
const previewCertificate = ref<LearningCertificate | null>(null)
const loadingCert = ref(false)

const applyDialogVisible = ref(false)
const sourceType = ref(1)
const selectedSourceId = ref<number | null>(null)
const selectedSourceTitle = ref('')

const form = reactive({
  targetType: 1,
  targetCourseId: '',
  targetAchievementId: '',
  targetOrgId: '',
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

async function handleMatchRulesForCourse(courseId: number, courseName: string) {
  try {
    sourceType.value = 1
    selectedSourceId.value = courseId
    selectedSourceTitle.value = courseName
    matchedRules.value = unwrapApi(await matchRulesApi(1, courseId))
    form.targetType = 1
    form.targetCourseId = ''
    form.targetAchievementId = ''
    form.targetOrgId = ''
    form.applyReason = ''
    applyDialogVisible.value = true
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '匹配失败'))
  }
}

async function handleMatchRulesForAchievement(achievementId: number, title: string) {
  try {
    sourceType.value = 2
    selectedSourceId.value = achievementId
    selectedSourceTitle.value = title
    matchedRules.value = unwrapApi(await matchRulesApi(2, undefined, achievementId))
    form.targetType = 1
    form.targetCourseId = ''
    form.targetAchievementId = ''
    form.targetOrgId = ''
    form.applyReason = ''
    applyDialogVisible.value = true
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '匹配失败'))
  }
}

function selectRule(rule: CreditTransferRule) {
  form.targetType = rule.targetType || 1
  form.targetCourseId = rule.targetCourseId ? String(rule.targetCourseId) : ''
  form.targetAchievementId = rule.targetAchievementId ? String(rule.targetAchievementId) : ''
  form.targetOrgId = rule.targetOrgId ? String(rule.targetOrgId) : ''
}

async function handleApply() {
  if (!selectedSourceId.value) {
    ElMessage.warning('请选择源')
    return
  }
  if (!form.targetOrgId) {
    ElMessage.warning('请选择目标机构')
    return
  }
  submitting.value = true
  try {
    const payload: CreditTransferApplyPayload = {
      sourceType: sourceType.value,
      sourceCourseId: sourceType.value === 1 ? selectedSourceId.value : undefined,
      sourceAchievementId: sourceType.value === 2 ? selectedSourceId.value : undefined,
      targetType: form.targetType,
      targetCourseId: form.targetType === 1 && form.targetCourseId ? Number(form.targetCourseId) : undefined,
      targetAchievementId: form.targetType === 2 && form.targetAchievementId ? Number(form.targetAchievementId) : undefined,
      targetOrgId: form.targetOrgId ? Number(form.targetOrgId) : undefined,
      applyReason: form.applyReason || undefined,
    }
    unwrapApi(await applyTransferApi(payload))
    ElMessage.success('申请已提交，等待机构审核')
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
    description="将您已有的课程学分或学习成果，申请转换为目标机构的等效课程学分或成果"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="学分转换说明：您已完成的课程或已通过校验的学习成果，均可申请转换为其他机构的等效课程学分或成果。系统会根据标签进行初步匹配，最终由目标机构审核确认。"
      class="tip"
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
        <el-table-column label="学习成果" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.sourceType === 1 && courseCertMap.has(row.sourceCourseId!)"
              link
              type="primary"
              :loading="loadingCert"
              @click="openCertificateByCourseId(row.sourceCourseId!)"
            >
              查看证书
            </el-button>
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
            title="以下为已完成的课程，您可申请将其学分转换到其他机构。"
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
                  @click="openCertificateByCourseId(row.courseId || 0)"
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
                  @click="handleMatchRulesForCourse(row.courseId || 0, row.title)"
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
          <el-alert
            type="warning"
            :closable="false"
            title="以下为已通过校验的学习成果，您可申请将其转换为其他机构的课程或成果。"
            class="mb-3"
          />
          <el-table :data="verifiedAchievements" border stripe>
            <el-table-column prop="title" label="成果名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="typeName" label="类型" width="100" />
            <el-table-column label="对应学分" width="100" align="right">
              <template #default="{ row }">{{ Number(row.creditValue || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="orgName" label="颁发机构" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.orgName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="获得时间" width="160">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="附件" width="100" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.fileUrl"
                  link
                  type="primary"
                  @click="openAchievementFile(row.fileUrl)"
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
                  @click="handleMatchRulesForAchievement(row.id, row.title)"
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
    >
      <div class="mb-4">
        <span class="text-muted">源类型：</span>
        <el-tag size="small">{{ sourceType === 1 ? '课程' : '学习成果' }}</el-tag>
      </div>
      <div class="mb-4">
        <span class="text-muted">源名称：</span>
        <strong>{{ selectedSourceTitle }}</strong>
      </div>

      <div v-if="matchedRules.length > 0" class="mb-4">
        <h4>匹配到以下转换规则：</h4>
        <el-table :data="matchedRules" border size="small">
          <el-table-column prop="orgName" label="目标机构" min-width="100" />
          <el-table-column label="目标类型" width="80">
            <template #default="{ row }">{{ row.targetTypeName }}</template>
          </el-table-column>
          <el-table-column prop="targetCourseName" label="目标内容" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.targetType === 2 ? (row.targetAchievementTitle || '学习成果') : (row.targetCourseName || '-') }}
            </template>
          </el-table-column>
          <el-table-column prop="sourceTags" label="匹配标签" min-width="100" />
          <el-table-column label="操作" width="70">
            <template #default="{ row }">
              <el-button link type="primary" @click="selectRule(row)">选择</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-form label-width="100px">
        <el-form-item label="目标类型">
          <el-radio-group v-model="form.targetType">
            <el-radio :value="1">课程</el-radio>
            <el-radio :value="2">成果/证书</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.targetType === 1" label="目标课程ID">
          <el-input v-model="form.targetCourseId" type="number" placeholder="选择规则后自动填充" />
        </el-form-item>
        <el-form-item v-if="form.targetType === 2" label="目标成果ID">
          <el-input v-model="form.targetAchievementId" type="number" placeholder="选择规则后自动填充" />
        </el-form-item>
        <el-form-item label="目标机构ID">
          <el-input v-model="form.targetOrgId" type="number" placeholder="选择规则后自动填充" />
        </el-form-item>
        <el-form-item label="申请理由">
          <el-input v-model="form.applyReason" type="textarea" :rows="2" placeholder="说明申请转换的原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApply">提交申请</el-button>
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

.text-muted {
  color: #909399;
}

.mb-3 {
  margin-bottom: 12px;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
