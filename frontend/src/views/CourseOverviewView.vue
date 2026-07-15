<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageShell from '@/components/common/PageShell.vue'
import {
  listEnterpriseCoursesApi,
  type EnterpriseCourse,
} from '@/api/enterprise-course'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const courses = ref<EnterpriseCourse[]>([])

function approvalTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

async function fetchCourses() {
  loading.value = true
  loadError.value = null
  try {
    courses.value = unwrapApi(await listEnterpriseCoursesApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchCourses)
</script>

<template>
  <PageShell
    title="课程概览"
    description="查看本机构已发布的课程及审核状态"
    :loading="loading"
    :error="loadError"
    @retry="fetchCourses"
  >
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="课程发布与审核在「企业工作台 → 课程管理」中操作，此处仅作展示。"
      class="tip"
    />

    <el-table :data="courses" border stripe>
      <el-table-column prop="title" label="课程名称" min-width="160" />
      <el-table-column label="标签" min-width="120">
        <template #default="{ row }">
          <el-tag v-for="tag in row.tags" :key="tag" size="small" class="tag-margin">{{ tag }}</el-tag>
          <span v-if="!row.tags || row.tags.length === 0" class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="秩点" width="80">
        <template #default="{ row }">{{ Number(row.creditValue || 0).toFixed(0) }}</template>
      </el-table-column>
      <el-table-column label="时长(分钟)" width="100">
        <template #default="{ row }">{{ row.duration }}</template>
      </el-table-column>
      <el-table-column prop="difficultyName" label="难度" width="80" />
      <el-table-column label="上架状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="approvalTagType(row.approvalStatus)" size="small">
            {{ row.approvalStatusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewRemark" label="审核备注" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.reviewRemark || '-' }}</template>
      </el-table-column>
      <el-table-column label="提交时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && courses.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无课程"
    />
  </PageShell>
</template>

<style scoped>
.tip {
  margin-bottom: 16px;
}

.tag-margin {
  margin-right: 4px;
  margin-bottom: 4px;
}

.text-muted {
  color: #909399;
}
</style>
