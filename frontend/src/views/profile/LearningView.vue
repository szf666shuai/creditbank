<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
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
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatDate, formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const archives = ref<LearningArchiveItem[]>([])
const achievements = ref<LearningAchievementItem[]>([])
const certificates = ref<LearningCertificate[]>([])

const certDialogVisible = ref(false)
const previewCertificate = ref<LearningCertificate | null>(null)
const loadingCert = ref(false)

/** 课程学习档案 */
const courseArchives = computed(() =>
  archives.value.filter((a) => a.archiveType === 1 || a.archiveTypeName?.includes('课程')),
)

function archiveStatusType(status: number) {
  return status === 1 ? 'success' : 'warning'
}

function verifyStatusType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

function hasAttachment(row: LearningAchievementItem) {
  return !!row.certificateId || isExternalUrl(row.fileUrl)
}

function isExternalUrl(url?: string) {
  return !!url && /^https?:\/\//i.test(url)
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
  loadingCert.value = true
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
  } finally {
    loadingCert.value = false
  }
}

async function openAchievementAttachment(row: LearningAchievementItem) {
  if (isExternalUrl(row.fileUrl)) {
    window.open(row.fileUrl, '_blank', 'noopener,noreferrer')
    return
  }
  if (!row.certificateId) {
    ElMessage.warning('暂无可预览的附件')
    return
  }
  loadingCert.value = true
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
  } finally {
    loadingCert.value = false
  }
}

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const [archiveData, achievementData] = await Promise.all([
      unwrapApi(await listLearningArchivesApi()),
      unwrapApi(await listLearningAchievementsApi()),
    ])
    archives.value = archiveData
    achievements.value = achievementData
    certificates.value = []
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
    title="学习档案与成果"
    description="查看个人终身学习档案、课程学分及学习成果"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <!-- 学习档案：课程+学分表格 -->
    <section class="page-section">
      <h2>学习档案</h2>
      <el-table :data="courseArchives" border stripe>
        <el-table-column prop="title" label="课程名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="获得秩点" width="100" align="right">
          <template #default="{ row }">
            <span class="credit-positive">+{{ Number(row.creditEarned || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="archiveStatusType(row.status)" size="small">{{ row.statusName }}</el-tag>
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
              :loading="loadingCert"
              @click="openCertificate(row)"
            >
              查看证书
            </el-button>
            <span v-else class="page-text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!loading && courseArchives.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无课程学习档案"
      />
    </section>

    <!-- 其他学习成果 -->
    <section class="page-section">
      <h2>其他学习成果</h2>
      <el-table :data="achievements" border stripe>
        <el-table-column prop="title" label="成果名称" min-width="180" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orgName" label="认证机构" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.orgName || '-' }}</template>
        </el-table-column>
        <el-table-column label="可兑换秩点" width="110" align="right">
          <template #default="{ row }">{{ row.creditValue ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="校验状态" width="100">
          <template #default="{ row }">
            <el-tag :type="verifyStatusType(row.verifyStatus)" size="small">{{ row.verifyStatusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学习成果" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="hasAttachment(row)"
              link
              type="primary"
              :loading="loadingCert"
              @click.stop="openAchievementAttachment(row)"
            >
              查看
            </el-button>
            <span v-else class="page-text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="记录时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!loading && achievements.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无其他学习成果"
      />
    </section>
  </PageShell>

  <el-dialog
    v-model="certDialogVisible"
    title="合格证预览"
    width="860px"
    destroy-on-close
    append-to-body
    class="cert-dialog"
  >
    <CertificatePreview v-if="previewCertificate" :certificate="previewCertificate" />
  </el-dialog>
</template>

<style scoped>
.page-section {
  margin-bottom: 28px;
}

.page-section h2 {
  font-size: 16px;
  font-weight: 700;
  color: var(--nb-ink, var(--color-foreground));
  margin: 0 0 12px;
}

.credit-positive {
  color: #16a34a;
  font-weight: 600;
}
</style>
