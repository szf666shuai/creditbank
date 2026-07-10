<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import { listMyActivitiesApi, type ActivityManageItem } from '@/api/enterprise-activities'
import { searchRecipientsApi, type MessageRecipient } from '@/api/message'
import {
  listSentActivityInvitesApi,
  sendActivityInviteApi,
  type ActivityInvitationItem,
} from '@/api/activity-invitation'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const searching = ref(false)
const dialogVisible = ref(false)
const activities = ref<ActivityManageItem[]>([])
const invitations = ref<ActivityInvitationItem[]>([])
const recipients = ref<MessageRecipient[]>([])

const form = reactive({
  activityId: undefined as number | undefined,
  toUserId: undefined as number | undefined,
  remark: '',
})

const invitableActivities = computed(() =>
  activities.value.filter((item) => item.status === 1 || item.status === 2),
)

function statusTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const [activityData, inviteData] = await Promise.all([
      unwrapApi(await listMyActivitiesApi()),
      unwrapApi(await listSentActivityInvitesApi()),
    ])
    activities.value = activityData
    invitations.value = inviteData
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function openInvite() {
  form.activityId = invitableActivities.value[0]?.id
  form.toUserId = undefined
  form.remark = ''
  recipients.value = []
  dialogVisible.value = true
}

async function searchRecipients(keyword: string) {
  if (!keyword.trim()) {
    recipients.value = []
    return
  }
  searching.value = true
  try {
    recipients.value = unwrapApi(await searchRecipientsApi(keyword.trim()))
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '搜索用户失败'))
  } finally {
    searching.value = false
  }
}

async function handleSubmit() {
  if (!form.activityId) {
    ElMessage.warning('请选择活动')
    return
  }
  if (!form.toUserId) {
    ElMessage.warning('请选择受邀学员')
    return
  }

  saving.value = true
  try {
    unwrapApi(
      await sendActivityInviteApi({
        activityId: form.activityId,
        toUserId: form.toUserId,
        remark: form.remark.trim() || undefined,
      }),
    )
    ElMessage.success('活动邀请已发送')
    dialogVisible.value = false
    await fetchData()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '发送失败'))
  } finally {
    saving.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="enterprise-page-wrap">
    <PageShell
      title="活动邀请"
      description="向学员发送活动参与邀请，学员接受后将自动完成报名"
      :loading="loading"
      :error="loadError"
      @retry="fetchData"
    >
      <template #actions>
        <el-button type="primary" :disabled="invitableActivities.length === 0" @click="openInvite">
          发送活动邀请
        </el-button>
      </template>

      <el-table :data="invitations" border stripe>
        <el-table-column prop="activityTitle" label="活动" min-width="160" />
        <el-table-column prop="toUserName" label="受邀学员" width="120" />
        <el-table-column label="活动时间" min-width="220">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="140" show-overflow-tooltip />
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
        description="暂无活动邀请"
      />
    </PageShell>

    <el-dialog v-model="dialogVisible" title="发送活动邀请" width="560px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="活动" required>
          <el-select v-model="form.activityId" placeholder="选择活动" style="width: 100%">
            <el-option
              v-for="item in invitableActivities"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="受邀学员" required>
          <el-select
            v-model="form.toUserId"
            filterable
            remote
            reserve-keyword
            placeholder="输入用户名或姓名搜索"
            :remote-method="searchRecipients"
            :loading="searching"
            style="width: 100%"
          >
            <el-option
              v-for="user in recipients"
              :key="user.id"
              :label="`${user.displayName}（${user.username}）`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">发送邀请</el-button>
      </template>
    </el-dialog>
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
