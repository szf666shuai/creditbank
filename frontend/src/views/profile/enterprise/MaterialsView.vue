<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  MATERIAL_TYPE_OPTIONS,
  createMaterialApi,
  listMyMaterialsApi,
  offlineMaterialApi,
  onlineMaterialApi,
  updateMaterialApi,
  type MaterialManageItem,
} from '@/api/enterprise-materials'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const materials = ref<MaterialManageItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  title: '',
  description: '',
  fileUrl: '',
  materialType: 1,
})

function resetForm() {
  form.title = ''
  form.description = ''
  form.fileUrl = ''
  form.materialType = 1
  editingId.value = null
}

async function fetchMaterials() {
  loading.value = true
  loadError.value = null
  try {
    materials.value = unwrapApi(await listMyMaterialsApi())
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

function openEdit(row: MaterialManageItem) {
  editingId.value = row.id
  form.title = row.title
  form.description = row.description || ''
  form.fileUrl = row.fileUrl
  form.materialType = row.materialType
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('请填写资料标题')
    return
  }
  if (!form.fileUrl.trim()) {
    ElMessage.warning('请填写文件链接')
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title.trim(),
      description: form.description.trim() || undefined,
      fileUrl: form.fileUrl.trim(),
      materialType: form.materialType,
    }
    if (editingId.value) {
      unwrapApi(await updateMaterialApi(editingId.value, payload))
      ElMessage.success('资料已更新')
    } else {
      unwrapApi(await createMaterialApi(payload))
      ElMessage.success('资料已发布')
    }
    dialogVisible.value = false
    await fetchMaterials()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: MaterialManageItem) {
  const offline = row.status === 1
  const action = offline ? '下架' : '重新发布'
  await ElMessageBox.confirm(`确定${action}「${row.title}」吗？`, `${action}资料`, { type: 'warning' })
  try {
    if (offline) {
      unwrapApi(await offlineMaterialApi(row.id))
    } else {
      unwrapApi(await onlineMaterialApi(row.id))
    }
    ElMessage.success(`已${action}`)
    await fetchMaterials()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, `${action}失败`))
  }
}

onMounted(fetchMaterials)
</script>

<template>
  <PageShell
    title="企业资料"
    description="发布学习资料（文档/视频链接），学员可在企业详情页查看"
    :loading="loading"
    :error="loadError"
    @retry="fetchMaterials"
  >
    <template #actions>
      <el-button type="primary" @click="openCreate">发布资料</el-button>
    </template>

    <el-table :data="materials" border stripe>
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ row.materialTypeName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件链接" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <a v-if="row.fileUrl" :href="row.fileUrl" target="_blank" rel="noopener" class="page-link">{{ row.fileUrl }}</a>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '发布' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && materials.length === 0" class="page-empty" :image-size="80" description="暂无企业资料" />
  </PageShell>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑资料' : '发布资料'" width="560px" destroy-on-close @closed="resetForm">
    <el-form label-width="88px">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="类型" required>
        <el-select v-model="form.materialType" style="width: 100%">
          <el-option v-for="item in MATERIAL_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="文件链接" required>
        <el-input v-model="form.fileUrl" maxlength="255" placeholder="PDF/视频等可访问 URL" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="2000" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>
