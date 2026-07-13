<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/common/PageShell.vue'
import {
  APPLICATION_STATUS_OPTIONS,
  applicationStatusTagType,
  listMyJobApplicationsApi,
  type MyJobApplicationItem,
} from '@/api/profile-applications'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const loadError = ref<string | null>(null)
const applications = ref<MyJobApplicationItem[]>([])
const statusFilter = ref<number | undefined>(undefined)

async function fetchApplications() {
  loading.value = true
  loadError.value = null
  try {
    applications.value = unwrapApi(await listMyJobApplicationsApi(statusFilter.value))
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchApplications()
}

function handleReset() {
  statusFilter.value = undefined
  fetchApplications()
}

onMounted(fetchApplications)
</script>

<template>
  <PageShell
    title="投递记录"
    description="查看你的职位投递进度与处理状态"
    :loading="loading"
    :error="loadError"
    @retry="fetchApplications"
  >
    <template #actions>
      <el-button @click="router.push('/profile/interviews')">面试邀请</el-button>
    </template>

    <div class="page-toolbar">
      <el-select v-model="statusFilter" placeholder="投递状态" clearable style="width: 140px">
        <el-option
          v-for="item in APPLICATION_STATUS_OPTIONS.filter((o) => o.value !== undefined)"
          :key="String(item.value)"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="applications" border stripe>
      <el-table-column prop="jobTitle" label="应聘职位" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span :class="{ 'page-text-muted': row.jobUnavailable }">{{ row.jobTitle || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="orgName" label="企业" min-width="140" show-overflow-tooltip />
      <el-table-column prop="jobLocation" label="工作地点" width="120" show-overflow-tooltip />
      <el-table-column prop="salaryRange" label="薪资范围" width="120" show-overflow-tooltip />
      <el-table-column label="求职信" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">{{ row.coverMessage || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="applicationStatusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="投递时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="160">
        <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && applications.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无投递记录"
    />
  </PageShell>
</template>
