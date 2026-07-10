<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/common/PageShell.vue'
import { listSentInterviewsApi, type InterviewInvitationItem } from '@/api/interview'
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
  <div class="enterprise-page-wrap">
    <PageShell
      title="面试邀请"
      description="查看已发送的面试邀请及学员回复状态"
      :loading="loading"
      :error="loadError"
      @retry="fetchInvitations"
    >
      <template #actions>
        <el-button type="primary" @click="router.push('/enterprise/applications')">
          投递管理
        </el-button>
      </template>

      <el-table :data="invitations" border stripe>
        <el-table-column prop="jobTitle" label="职位" min-width="140" />
        <el-table-column prop="toUserName" label="受邀学员" width="120" />
        <el-table-column label="面试时间" width="160">
          <template #default="{ row }">{{ formatTime(row.inviteTime) }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点/方式" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发送时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && invitations.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无面试邀请"
      />
    </PageShell>
  </div>
</template>

<style scoped>
.enterprise-page-wrap {
  padding: 24px 16px 48px;
}

.enterprise-page-wrap :deep(.page-shell) {
  max-width: var(--content-max-width);
  margin: 0 auto;
}
</style>
