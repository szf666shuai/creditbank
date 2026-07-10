<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  deleteResumeApi,
  listResumesApi,
  setDefaultResumeApi,
  type UserResumeSummary,
} from '@/api/profile-resume'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const loadError = ref<string | null>(null)
const actingId = ref<number | null>(null)
const resumes = ref<UserResumeSummary[]>([])

async function fetchResumes() {
  loading.value = true
  loadError.value = null
  try {
    resumes.value = unwrapApi(await listResumesApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function goCreate() {
  router.push('/profile/resume/new')
}

function goEdit(id: number) {
  router.push(`/profile/resume/${id}`)
}

async function handleSetDefault(row: UserResumeSummary) {
  if (row.isDefault === 1) return
  actingId.value = row.id
  try {
    unwrapApi(await setDefaultResumeApi(row.id))
    ElMessage.success('已设为默认投递简历')
    await fetchResumes()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  } finally {
    actingId.value = null
  }
}

async function handleDelete(row: UserResumeSummary) {
  await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '删除简历', { type: 'warning' })
  actingId.value = row.id
  try {
    await deleteResumeApi(row.id)
    ElMessage.success('简历已删除')
    await fetchResumes()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '删除失败'))
  } finally {
    actingId.value = null
  }
}

onMounted(fetchResumes)
</script>

<template>
  <PageShell
    title="我的简历"
    description="维护多份简历版本，针对不同企业与岗位使用不同版本投递"
    :loading="loading"
    :error="loadError"
    @retry="fetchResumes"
  >
    <div class="resume-grid">
      <button type="button" class="resume-card add-card" @click="goCreate">
        <span class="add-icon">+</span>
        <span class="add-title">添加简历</span>
        <span class="add-desc">为不同投递对象创建新版本</span>
      </button>

      <article
        v-for="item in resumes"
        :key="item.id"
        class="resume-card"
        @click="goEdit(item.id)"
      >
        <div class="card-head">
          <h3>{{ item.title }}</h3>
          <el-tag v-if="item.isDefault === 1" type="success" size="small">默认投递</el-tag>
        </div>
        <p class="card-name">{{ item.realName || '未填写姓名' }}</p>
        <p class="card-meta">
          <span>版本 v{{ item.version ?? 1 }}</span>
          <span>更新于 {{ formatTime(item.updateTime) }}</span>
        </p>
        <div class="card-actions" @click.stop>
          <el-button
            v-if="item.isDefault !== 1"
            link
            type="primary"
            :loading="actingId === item.id"
            @click="handleSetDefault(item)"
          >
            设为默认
          </el-button>
          <el-button link type="primary" @click="goEdit(item.id)">编辑</el-button>
          <el-button
            link
            type="danger"
            :disabled="resumes.length <= 1"
            :loading="actingId === item.id"
            @click="handleDelete(item)"
          >
            删除
          </el-button>
        </div>
      </article>
    </div>

    <el-empty
      v-if="!loading && resumes.length === 0"
      class="page-empty"
      :image-size="80"
      description="还没有简历，点击添加创建第一份"
    >
      <el-button type="primary" @click="goCreate">添加简历</el-button>
    </el-empty>
  </PageShell>
</template>

<style scoped>
.resume-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.resume-card {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 18px;
  background: var(--color-white);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.resume-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 4px 16px rgba(32, 148, 243, 0.08);
}

.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  border-style: dashed;
  background: #fafafa;
}

.add-card:hover {
  background: var(--color-primary-light);
}

.add-icon {
  font-size: 36px;
  line-height: 1;
  color: var(--color-primary);
  margin-bottom: 8px;
}

.add-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.add-desc {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.card-head h3 {
  font-size: 16px;
  color: var(--color-text);
  margin: 0;
}

.card-name {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 12px;
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
