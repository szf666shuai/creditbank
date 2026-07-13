<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  listMyInterviewInvitesApi,
  acceptInterviewInviteApi,
  rejectInterviewInviteApi,
  type InterviewInvitationItem,
} from '@/api/interview'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const loadError = ref<string | null>(null)
const actingId = ref<number | null>(null)
const invitations = ref<InterviewInvitationItem[]>([])

function statusTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

async function fetchInvitations() {
  loading.value = true
  loadError.value = null
  try {
    invitations.value = unwrapApi(await listMyInterviewInvitesApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function canJoinVideo(row: InterviewInvitationItem) {
  return row.canJoinVideo === true
}

function enterVideoRoom(row: InterviewInvitationItem) {
  router.push(`/profile/interviews/${row.id}/video`)
}

async function handleAccept(row: InterviewInvitationItem) {
  actingId.value = row.id
  try {
    unwrapApi(await acceptInterviewInviteApi(row.id))
    ElMessage.success('已接受面试邀请')
    await fetchInvitations()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  } finally {
    actingId.value = null
  }
}

async function handleReject(row: InterviewInvitationItem) {
  await ElMessageBox.confirm('确定拒绝该面试邀请吗？', '拒绝邀请', { type: 'warning' })
  actingId.value = row.id
  try {
    unwrapApi(await rejectInterviewInviteApi(row.id))
    ElMessage.success('已拒绝面试邀请')
    await fetchInvitations()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  } finally {
    actingId.value = null
  }
}

onMounted(fetchInvitations)
</script>

<template>
  <PageShell
    title="我的面试邀请"
    description="查看企业发来的面试邀请并回复（请在专属页面处理，不在消息中心展示）"
    :loading="loading"
    :error="loadError"
    @retry="fetchInvitations"
  >
    <el-table :data="invitations" border stripe>
      <el-table-column prop="orgName" label="企业" width="140" />
      <el-table-column prop="jobTitle" label="职位" min-width="140" />
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
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button
              link
              type="success"
              :loading="actingId === row.id"
              @click="handleAccept(row)"
            >
              接受
            </el-button>
            <el-button
              link
              type="danger"
              :loading="actingId === row.id"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
          </template>
          <template v-else-if="canJoinVideo(row)">
            <el-button link type="primary" @click="enterVideoRoom(row)">进入面试</el-button>
          </template>
          <span v-else-if="row.status !== 0" class="page-text-muted">
            {{ row.applicationStatus === 3 || row.applicationStatus === 4 ? row.applicationStatusName : '已处理' }}
          </span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && invitations.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无面试邀请"
    />
  </PageShell>
</template>
