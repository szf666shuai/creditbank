<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  createEnterpriseCourseApi,
  listEnterpriseCoursesApi,
  updateEnterpriseCourseApi,
  deleteEnterpriseCourseApi,
  type EnterpriseCourse,
} from '@/api/enterprise-course'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const courses = ref<EnterpriseCourse[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const difficultyOptions = [
  { label: '入门', value: 1 },
  { label: '初级', value: 2 },
  { label: '中级', value: 3 },
  { label: '高级', value: 4 },
]

const form = reactive({
  title: '',
  description: '',
  coverUrl: '',
  tags: '' as string,
  creditValue: 1,
  duration: 60,
  difficulty: 1,
})

function approvalTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.coverUrl = ''
  form.tags = ''
  form.creditValue = 1
  form.duration = 60
  form.difficulty = 1
  editingId.value = null
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

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: EnterpriseCourse) {
  if (row.approvalStatus === 1) {
    ElMessage.warning('已通过审核的课程需下架后再编辑')
    return
  }
  editingId.value = row.id
  form.title = row.title
  form.description = row.description || ''
  form.coverUrl = row.coverUrl || ''
  form.tags = row.tags?.join(',') || ''
  form.creditValue = Number(row.creditValue || 1)
  form.duration = Number(row.duration || 60)
  form.difficulty = row.difficulty || 1
  dialogVisible.value = true
}

async function handleDelete(row: EnterpriseCourse) {
  if (row.approvalStatus === 1) {
    ElMessage.warning('已通过审核的课程需先下架')
    return
  }
  if (!confirm(`确定删除课程「${row.title}」吗？`)) return
  try {
    await deleteEnterpriseCourseApi(row.id)
    ElMessage.success('课程已删除')
    await fetchCourses()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '删除失败'))
  }
}

async function handleSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('请填写课程名称')
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title.trim(),
      description: form.description,
      coverUrl: form.coverUrl,
      tags: form.tags.trim() ? form.tags.split(',').map(t => t.trim()).filter(Boolean) : undefined,
      creditValue: form.creditValue,
      duration: form.duration,
      difficulty: form.difficulty,
    }
    if (editingId.value) {
      unwrapApi(await updateEnterpriseCourseApi(editingId.value, payload))
      ElMessage.success('课程已更新，等待平台审核')
    } else {
      unwrapApi(await createEnterpriseCourseApi(payload))
      ElMessage.success('课程已提交，等待平台审核')
    }
    dialogVisible.value = false
    await fetchCourses()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}

onMounted(fetchCourses)
</script>

<template>
  <PageShell
    title="课程管理"
    description="发布学习课程，提交后由平台管理员审核上架"
    :loading="loading"
    :error="loadError"
    @retry="fetchCourses"
  >
    <template #actions>
      <el-button type="primary" @click="openCreate">发布课程</el-button>
    </template>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="企业可发布学习课程，学员完成课程后可获得相应秩点。课程需经平台审核通过后才会在学习资源中展示。"
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
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :type="approvalTagType(row.approvalStatus)" size="small">
            {{ row.approvalStatusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewRemark" label="审核备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="提交时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <div class="page-table-actions">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && courses.length === 0"
      class="page-empty"
      :image-size="80"
      description="尚未发布课程"
    >
      <el-button type="primary" @click="openCreate">发布第一门课程</el-button>
    </el-empty>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑课程' : '发布课程'"
      width="560px"
      destroy-on-close
    >
      <el-form label-width="96px">
        <el-form-item label="课程名称" required>
          <el-input v-model="form.title" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面链接">
          <el-input v-model="form.coverUrl" placeholder="可选，图片 URL" />
        </el-form-item>
        <el-form-item label="课程标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="学分值" required>
          <el-input-number v-model="form.creditValue" :min="1" :step="1" />
        </el-form-item>
        <el-form-item label="课程时长(分钟)" required>
          <el-input-number v-model="form.duration" :min="1" :step="10" />
        </el-form-item>
        <el-form-item label="难度等级" required>
          <el-select v-model="form.difficulty" style="width: 100%">
            <el-option
              v-for="item in difficultyOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">提交审核</el-button>
      </template>
    </el-dialog>
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