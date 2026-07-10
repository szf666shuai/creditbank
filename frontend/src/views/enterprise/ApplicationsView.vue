<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  listMyApplicationsApi,
  sendInterviewInviteApi,
  type JobApplicationItem,
} from '@/api/interview'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const applications = ref<JobApplicationItem[]>([])
const dialogVisible = ref(false)
const currentApplication = ref<JobApplicationItem | null>(null)

const form = reactive({
  inviteTime: '',
  location: '',
  remark: '',
})

async function fetchApplications() {
  loading.value = true
  loadError.value = null
  try {
    applications.value = unwrapApi(await listMyApplicationsApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function openInvite(row: JobApplicationItem) {
  if (row.hasPendingInvite) {
    ElMessage.warning('该投递已有待回复的面试邀请')
    return
  }
  currentApplication.value = row
  form.inviteTime = ''
  form.location = ''
  form.remark = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!currentApplication.value) return
  if (!form.inviteTime) {
    ElMessage.warning('请选择面试时间')
    return
  }
  if (!form.location.trim()) {
    ElMessage.warning('请填写面试地点/方式')
    return
  }

  saving.value = true
  try {
    unwrapApi(
      await sendInterviewInviteApi({
        applicationId: currentApplication.value.id,
        inviteTime: form.inviteTime,
        location: form.location.trim(),
        remark: form.remark.trim() || undefined,
      }),
    )
    ElMessage.success('面试邀请已发送')
    dialogVisible.value = false
    await fetchApplications()
    router.push('/enterprise/interviews')
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '发送失败'))
  } finally {
    saving.value = false
  }
}

onMounted(fetchApplications)
</script>

<template>
  <div class="enterprise-page-wrap">
    <PageShell
      title="投递管理"
      description="查看学员投递记录，并向候选人发送面试邀请"
      :loading="loading"
      :error="loadError"
      @retry="fetchApplications"
    >
      <template #actions>
        <el-button @click="router.push('/enterprise/interviews')">面试邀请记录</el-button>
      </template>

      <el-table :data="applications" border stripe>
        <el-table-column prop="jobTitle" label="应聘职位" min-width="160" />
        <el-table-column prop="applicantName" label="学员" width="120" />
        <el-table-column label="求职信" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.coverMessage || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="投递时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="row.hasPendingInvite"
              @click="openInvite(row)"
            >
              {{ row.hasPendingInvite ? '已邀请' : '发面试邀请' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && applications.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无投递记录"
      />
    </PageShell>

    <el-dialog v-model="dialogVisible" title="发送面试邀请" width="520px" destroy-on-close>
      <el-form v-if="currentApplication" label-width="96px">
        <el-form-item label="学员">{{ currentApplication.applicantName }}</el-form-item>
        <el-form-item label="职位">{{ currentApplication.jobTitle }}</el-form-item>
        <el-form-item label="面试时间" required>
          <el-date-picker
            v-model="form.inviteTime"
            type="datetime"
            placeholder="选择面试时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地点/方式" required>
          <el-input v-model="form.location" placeholder="如：上海浦东 / 线上腾讯会议" />
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
