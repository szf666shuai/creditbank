<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/common/PageShell.vue'
import {
  listEnterpriseCoursesApi,
  type EnterpriseCourse,
} from '@/api/enterprise-course'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const loadError = ref<string | null>(null)
const courses = ref<EnterpriseCourse[]>([])

function parseTags(tags?: string | string[] | null): string[] {
  if (!tags) return []
  if (Array.isArray(tags)) return tags.map(String).map((t) => t.trim()).filter(Boolean)
  return String(tags)
    .split(/[,，]/)
    .map((t) => t.trim())
    .filter(Boolean)
}

const publishedCount = computed(() => courses.value.filter((c) => c.status === 1 && c.approvalStatus === 1).length)

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
    <template #actions>
      <el-button @click="router.push('/profile/enterprise/transfer-rules')">学分转换规则</el-button>
      <el-button type="primary" @click="router.push('/profile/enterprise/courses')">课程管理</el-button>
    </template>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip"
      :title="`当前共 ${courses.length} 门课程，其中 ${publishedCount} 门已上架并通过审核。发布与编辑请前往「课程管理」，学员跨机构转换规则请查看「学分转换规则」。`"
    />

    <el-table :data="courses" border stripe>
      <el-table-column prop="title" label="课程名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="标签" min-width="140">
        <template #default="{ row }">
          <el-tag
            v-for="tag in parseTags(row.tags)"
            :key="tag"
            size="small"
            class="tag-margin"
          >{{ tag }}</el-tag>
          <span v-if="parseTags(row.tags).length === 0" class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="学分" width="80" align="center">
        <template #default="{ row }">{{ Number(row.creditValue || 0).toFixed(1) }}</template>
      </el-table-column>
      <el-table-column label="完成秩点" width="100" align="center">
        <template #default="{ row }">{{ Number(row.creditReward ?? row.creditValue ?? 0).toFixed(0) }}</template>
      </el-table-column>
      <el-table-column label="时长(分钟)" width="100" align="center">
        <template #default="{ row }">{{ row.duration ?? row.durationMinutes ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="difficultyName" label="难度" width="80" align="center" />
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
      <el-table-column prop="reviewRemark" label="审核备注" min-width="120" show-overflow-tooltip>
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
      description="暂无课程，请先在课程管理中发布"
    >
      <el-button type="primary" @click="router.push('/profile/enterprise/courses')">去发布课程</el-button>
    </el-empty>
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
