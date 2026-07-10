<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageShell from '@/components/common/PageShell.vue'
import LearningChartsPanel from '@/components/profile/LearningChartsPanel.vue'
import {
  listLearningArchivesApi,
  listLearningAchievementsApi,
  type LearningArchiveItem,
  type LearningAchievementItem,
} from '@/api/profile-learning'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatDate, formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const activeTab = ref<'archives' | 'achievements'>('archives')
const archives = ref<LearningArchiveItem[]>([])
const achievements = ref<LearningAchievementItem[]>([])

function archiveStatusType(status: number) {
  return status === 1 ? 'success' : 'warning'
}

function verifyStatusType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
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
    description="查看个人终身学习档案、成果记录及学习进程图表"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <LearningChartsPanel embedded />

    <el-tabs v-model="activeTab" class="page-tabs">
      <el-tab-pane label="学习档案" name="archives" />
      <el-tab-pane label="学习成果" name="achievements" />
    </el-tabs>

    <el-table v-if="activeTab === 'archives'" :data="archives" border stripe>
      <el-table-column prop="title" label="档案标题" min-width="180" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ row.archiveTypeName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类别" width="120" show-overflow-tooltip />
      <el-table-column label="起止日期" width="200">
        <template #default="{ row }">
          {{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}
        </template>
      </el-table-column>
      <el-table-column label="获得学分" width="100" align="right">
        <template #default="{ row }">{{ row.creditEarned ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="archiveStatusType(row.status)" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ row.description || '-' }}</template>
      </el-table-column>
    </el-table>

    <el-table v-else :data="achievements" border stripe>
      <el-table-column prop="title" label="成果名称" min-width="180" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ row.typeName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orgName" label="认证机构" width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.orgName || '-' }}</template>
      </el-table-column>
      <el-table-column label="可兑换学分" width="110" align="right">
        <template #default="{ row }">{{ row.creditValue ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="校验状态" width="100">
        <template #default="{ row }">
          <el-tag :type="verifyStatusType(row.verifyStatus)" size="small">{{ row.verifyStatusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="存证哈希" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ row.blockchainHash || '-' }}</template>
      </el-table-column>
      <el-table-column label="附件" width="80" align="center">
        <template #default="{ row }">
          <a v-if="row.fileUrl" :href="row.fileUrl" target="_blank" rel="noopener" class="page-link" @click.stop>
            查看
          </a>
          <span v-else class="page-text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="记录时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && activeTab === 'archives' && archives.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无学习档案"
    />
    <el-empty
      v-if="!loading && activeTab === 'achievements' && achievements.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无学习成果"
    />
  </PageShell>
</template>
