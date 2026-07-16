<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import CertificatePreview from '@/components/learning/CertificatePreview.vue'
import {
  listLearningArchivesApi,
  listLearningAchievementsApi,
  type LearningArchiveItem,
  type LearningAchievementItem,
} from '@/api/profile-learning'
import { fetchLearningCertificates, type LearningCertificate } from '@/api/learning'
import {
  listMyApplicationsApi,
  type CreditTransferApplication,
} from '@/api/credit-transfer'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const archives = ref<LearningArchiveItem[]>([])
const achievements = ref<LearningAchievementItem[]>([])
const applications = ref<CreditTransferApplication[]>([])
const certificates = ref<LearningCertificate[]>([])

const certDialogVisible = ref(false)
const previewCertificate = ref<LearningCertificate | null>(null)

const courseArchives = computed(() =>
  archives.value.filter((a) => a.archiveType === 1 || a.archiveTypeName?.includes('课程')),
)

const totalCredits = computed(() =>
  courseArchives.value
    .reduce((sum, a) => sum + Number(a.creditEarned || 0), 0)
    .toFixed(2),
)

const completedCourses = computed(() => courseArchives.value.length)

const transferPending = computed(() =>
  applications.value.filter((a) => a.status === 0).length,
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

function archiveStatusType(status: number) {
  return status === 1 ? 'success' : 'warning'
}

function transferStatusType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

function transferSourceDisplay(row: CreditTransferApplication) {
  if (row.sourceType === 2 && row.sourceAchievementTitle) {
    return row.sourceAchievementTitle
  }
  return row.sourceCourseName || '-'
}

function transferTargetDisplay(row: CreditTransferApplication) {
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

async function openCertificate(row: LearningArchiveItem) {
  if (!row.certificateId) {
    ElMessage.warning('该课程暂无学习证书')
    return
  }
  try {
    const list = await ensureCertificates()
    const cert = list.find((item) => item.id === row.certificateId)
    if (!cert) {
      ElMessage.error('未找到对应合格证，请刷新后重试')
      return
    }
    previewCertificate.value = cert
    certDialogVisible.value = true
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '加载证书失败'))
  }
}

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const [archiveRes, appRes, achievementRes] = await Promise.all([
      listLearningArchivesApi(),
      listMyApplicationsApi(),
      listLearningAchievementsApi(),
    ])
    archives.value = unwrapApi(archiveRes)
    applications.value = unwrapApi(appRes)
    achievements.value = unwrapApi(achievementRes)
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <PageShell
    title="秩点总览"
    description="学习档案与学分转换"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <section class="overview-row">
      <div class="overview-card">
        <div class="card-label">累计获得秩点</div>
        <div class="card-value">{{ totalCredits }}</div>
      </div>
      <div class="overview-card">
        <div class="card-label">已完成课程</div>
        <div class="card-value">{{ completedCourses }}</div>
      </div>
      <div class="overview-card">
        <div class="card-label">转换申请中</div>
        <div class="card-value">{{ transferPending }}</div>
      </div>
    </section>

    <section class="archive-section">
      <h3>学习档案</h3>
      <el-table :data="courseArchives" border stripe>
        <el-table-column prop="title" label="课程名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="获得秩点" width="100" align="right">
          <template #default="{ row }">
            <span class="credit-positive">+{{ Number(row.creditEarned || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="archiveStatusType(row.status)" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="获得时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="学习成果" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.certificateId"
              link
              type="primary"
              @click="openCertificate(row)"
            >
              查看证书
            </el-button>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!loading && courseArchives.length === 0"
        :image-size="80"
        description="暂无课程学习档案"
      />
    </section>

    <section class="archive-section">
      <h3>其他学习成果</h3>
      <el-table :data="achievements" border stripe>
        <el-table-column prop="title" label="成果名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="typeName" label="类型" width="100" />
        <el-table-column label="对应秩点" width="100" align="right">
          <template #default="{ row }">
            <span class="credit-positive">+{{ Number(row.creditValue || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orgName" label="颁发机构" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.orgName || '-' }}</template>
        </el-table-column>
        <el-table-column label="校验状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.verifyStatus === 1 ? 'success' : row.verifyStatus === 2 ? 'danger' : 'warning'" size="small">
              {{ row.verifyStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="获得时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!loading && achievements.length === 0"
        :image-size="80"
        description="暂无其他学习成果"
      />
    </section>

    <section class="archive-section">
      <h3>学分转换</h3>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="完成课程学习或获得学习成果后，可申请转换为其他机构的等效课程学分或成果。"
        class="tip"
      />
      <el-table :data="applications" border stripe>
        <el-table-column label="源" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <div>
              <div>{{ transferSourceDisplay(row) }}</div>
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
              <div>{{ transferTargetDisplay(row) }}</div>
              <el-tag size="small" type="warning" style="margin-top: 4px">{{ row.targetTypeName }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="targetOrgName" label="目标机构" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.targetOrgName || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="transferStatusType(row.status)" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实际转换学分" width="110" align="right">
          <template #default="{ row }">
            <span v-if="row.actualCredit">{{ Number(row.actualCredit).toFixed(2) }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ formatTime(row.applyTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!loading && applications.length === 0"
        :image-size="80"
        description="暂无转换申请"
      />
    </section>

    <el-dialog
      v-model="certDialogVisible"
      title="学习证书"
      width="520px"
      destroy-on-close
    >
      <CertificatePreview v-if="previewCertificate" :certificate="previewCertificate" />
    </el-dialog>
  </PageShell>
</template>

<style scoped>
.overview-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 24px;
}

.overview-card {
  background: linear-gradient(135deg, #bbf7d0 0%, #86efac 100%);
  border: 2.5px solid var(--nb-ink);
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--nb-shadow);
}

.overview-card:nth-child(2) {
  background: linear-gradient(135deg, #bfdbfe 0%, #93c5fd 100%);
}

.overview-card:nth-child(3) {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.card-label {
  font-size: 13px;
  color: var(--nb-ink);
  font-weight: 600;
  opacity: 0.8;
}

.card-value {
  font-size: 32px;
  font-weight: 900;
  color: var(--nb-ink);
  margin: 8px 0 0;
  line-height: 1.1;
}

.archive-section {
  margin-bottom: 28px;
}

.archive-section h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--nb-ink);
  margin: 0 0 12px;
}

.credit-positive {
  color: #16a34a;
  font-weight: 600;
}

.text-muted {
  color: #909399;
}

.tip {
  margin-bottom: 16px;
}

@media (max-width: 900px) {
  .overview-row {
    grid-template-columns: 1fr;
  }
}
</style>
