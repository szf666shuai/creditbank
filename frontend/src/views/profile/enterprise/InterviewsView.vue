<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/common/PageShell.vue'
import { listSentInterviewsApi, INTERVIEW_MODE_VIDEO, type InterviewInvitationItem } from '@/api/interview'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const loadError = ref<string | null>(null)
const invitations = ref<InterviewInvitationItem[]>([])

function statusTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

function canJoinVideo(row: InterviewInvitationItem) {
  return row.canJoinVideo === true
}

function enterVideoRoom(row: InterviewInvitationItem) {
  router.push(`/profile/enterprise/interviews/${row.id}/video`)
}

async function fetchInvitations() {
  loading.value = true
  loadError.value = null
  try {
    invitations.value = unwrapApi(await listSentInterviewsApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchInvitations)
</script>

<template>
  <PageShell
    title="面试邀请"
    description="查看已发送的面试邀请及学员回复状态"
    :loading="loading"
    :error="loadError"
    @retry="fetchInvitations"
  >
    <template #actions>
      <el-button type="primary" @click="router.push('/profile/enterprise/applications')">投递管理</el-button>
    </template>

    <el-table :data="invitations" border stripe>
      <el-table-column prop="jobTitle" label="职位" min-width="140" />
      <el-table-column prop="toUserName" label="受邀学员" width="120" />
      <el-table-column label="面试时间" width="160">
        <template #default="{ row }">{{ formatTime(row.inviteTime) }}</template>
      </el-table-column>
      <el-table-column prop="location" label="地点/方式" min-width="140" show-overflow-tooltip />
      <el-table-column label="面试方式" width="100">
        <template #default="{ row }">{{ row.interviewModeName || '现场面试' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发送时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="canJoinVideo(row)" link type="primary" @click="enterVideoRoom(row)">
            进入面试
          </el-button>
          <span v-else-if="row.status === 1 && row.interviewMode === INTERVIEW_MODE_VIDEO" class="page-text-muted">
            {{ row.applicationStatus === 3 || row.applicationStatus === 4 ? row.applicationStatusName : '-' }}
          </span>
          <span v-else class="page-text-muted">-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && invitations.length === 0" class="page-empty" :image-size="80" description="暂无面试邀请" />
  </PageShell>
</template>
