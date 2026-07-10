<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  listSkillTagsApi,
  listMyJobsApi,
  createJobApi,
  updateJobApi,
  offlineJobApi,
  onlineJobApi,
  type JobManageItem,
  type SkillTag,
} from '@/api/enterprise-jobs'
import { getErrorMessage, unwrapApi } from '@/utils/api'

const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const jobs = ref<JobManageItem[]>([])
const skillTags = ref<SkillTag[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  title: '',
  description: '',
  requirements: '',
  salaryRange: '',
  location: '',
  eduRequirement: '',
  tagIds: [] as number[],
})

function resetForm() {
  form.title = ''
  form.description = ''
  form.requirements = ''
  form.salaryRange = ''
  form.location = ''
  form.eduRequirement = ''
  form.tagIds = []
  editingId.value = null
}

async function fetchJobs() {
  loading.value = true
  loadError.value = null
  try {
    jobs.value = unwrapApi(await listMyJobsApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchTags() {
  try {
    skillTags.value = unwrapApi(await listSkillTagsApi())
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '标签加载失败'))
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: JobManageItem) {
  editingId.value = row.id
  form.title = row.title
  form.description = row.description || ''
  form.requirements = row.requirements || ''
  form.salaryRange = row.salaryRange || ''
  form.location = row.location || ''
  form.eduRequirement = row.eduRequirement || ''
  form.tagIds = row.tags.map((t) => t.id)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('请填写职位名称')
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title.trim(),
      description: form.description,
      requirements: form.requirements,
      salaryRange: form.salaryRange,
      location: form.location,
      eduRequirement: form.eduRequirement,
      tagIds: form.tagIds,
    }
    if (editingId.value) {
      unwrapApi(await updateJobApi(editingId.value, payload))
    } else {
      unwrapApi(await createJobApi(payload))
    }
    ElMessage.success(editingId.value ? '职位已更新' : '职位已发布')
    dialogVisible.value = false
    await fetchJobs()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function handleOffline(row: JobManageItem) {
  await ElMessageBox.confirm(`确定下架「${row.title}」吗？`, '下架职位', { type: 'warning' })
  try {
    unwrapApi(await offlineJobApi(row.id))
    ElMessage.success('已下架')
    await fetchJobs()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  }
}

async function handleOnline(row: JobManageItem) {
  try {
    unwrapApi(await onlineJobApi(row.id))
    ElMessage.success('已重新上架')
    await fetchJobs()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  }
}

onMounted(async () => {
  await Promise.all([fetchTags(), fetchJobs()])
})
</script>

<template>
  <PageShell
    title="招聘管理"
    description="发布、编辑和下架本企业招聘职位"
    :loading="loading"
    :error="loadError"
    @retry="fetchJobs"
  >
    <template #actions>
      <el-button type="primary" @click="openCreate">发布职位</el-button>
    </template>

    <el-table :data="jobs" border stripe>
      <el-table-column prop="title" label="职位名称" min-width="160" />
      <el-table-column label="技能标签" min-width="180">
        <template #default="{ row }">
          <el-tag v-for="tag in row.tags" :key="tag.id" size="small" class="tag-item">
            {{ tag.name }}
          </el-tag>
          <span v-if="row.tags.length === 0" class="page-text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="salaryRange" label="薪资" width="120" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="handleOffline(row)">
            下架
          </el-button>
          <el-button v-else link type="success" @click="handleOnline(row)">上架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && jobs.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无职位，点击发布职位开始招聘"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑职位' : '发布职位'"
      width="640px"
      destroy-on-close
    >
      <el-form label-width="88px">
        <el-form-item label="职位名称" required>
          <el-input v-model="form.title" placeholder="如：Java 开发工程师" />
        </el-form-item>
        <el-form-item label="技能标签">
          <el-select v-model="form.tagIds" multiple placeholder="选择技能标签" style="width: 100%">
            <el-option v-for="tag in skillTags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资范围">
          <el-input v-model="form.salaryRange" placeholder="如：15k-25k" />
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input v-model="form.location" placeholder="如：上海" />
        </el-form-item>
        <el-form-item label="学历要求">
          <el-input v-model="form.eduRequirement" placeholder="如：本科" />
        </el-form-item>
        <el-form-item label="职位描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input v-model="form.requirements" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<style scoped>
.tag-item {
  margin-right: 6px;
  margin-bottom: 4px;
}
</style>
