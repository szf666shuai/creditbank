<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Download, Link, View } from '@element-plus/icons-vue'
import { fetchCourseMaterials, type CourseMaterial } from '@/api/learning'

const props = defineProps<{
  courseId: number
}>()

const loading = ref(false)
const materials = ref<CourseMaterial[]>([])

const typeMeta: Record<string, { label: string; color: string }> = {
  pdf: { label: 'PDF', color: '#ff6b6b' },
  ppt: { label: 'PPT', color: '#ff9f43' },
  zip: { label: 'ZIP', color: '#54a0ff' },
  code: { label: 'MD', color: '#10ac84' },
  link: { label: 'SVG', color: '#5f27cd' },
}

function isExternalUrl(url: string) {
  return /^https?:\/\//i.test(url)
}

function fileNameFromUrl(url: string, fallback: string) {
  const segment = url.split('/').filter(Boolean).pop()
  return segment || fallback
}

async function loadMaterials() {
  loading.value = true
  try {
    const res = await fetchCourseMaterials(props.courseId)
    if (res.code === 200 && res.data) {
      materials.value = res.data
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '课件加载失败')
  } finally {
    loading.value = false
  }
}

function previewMaterial(item: CourseMaterial) {
  if (!item.fileUrl) return
  window.open(item.fileUrl, '_blank', 'noopener,noreferrer')
}

function downloadMaterial(item: CourseMaterial) {
  if (!item.fileUrl) return
  if (isExternalUrl(item.fileUrl)) {
    window.open(item.fileUrl, '_blank', 'noopener,noreferrer')
    return
  }
  const anchor = document.createElement('a')
  anchor.href = item.fileUrl
  anchor.download = fileNameFromUrl(item.fileUrl, item.title)
  anchor.rel = 'noopener'
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
}

watch(
  () => props.courseId,
  () => {
    void loadMaterials()
  },
  { immediate: true },
)
</script>

<template>
  <div class="courseware-panel">
    <div class="courseware-head">
      <h3>
        <el-icon><Document /></el-icon>
        课件资料
      </h3>
      <span>支持在线预览与真实文件下载，配合视频巩固学习</span>
    </div>

    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="!materials.length" description="本课程暂未上传课件" />

    <div v-else class="material-grid">
      <article
        v-for="item in materials"
        :key="item.id"
        class="material-card"
      >
        <div
          class="material-icon"
          :style="{ background: `${typeMeta[item.fileType]?.color || '#22c55e'}18`, color: typeMeta[item.fileType]?.color || '#22c55e' }"
        >
          <el-icon><Document v-if="item.fileType !== 'link'" /><Link v-else /></el-icon>
        </div>
        <div class="material-body">
          <strong>{{ item.title }}</strong>
          <span>{{ typeMeta[item.fileType]?.label || '文件' }}</span>
        </div>
        <div class="material-actions">
          <button type="button" class="action-btn" @click="previewMaterial(item)">
            <el-icon><View /></el-icon>
            预览
          </button>
          <button type="button" class="action-btn primary" @click="downloadMaterial(item)">
            <el-icon><Download /></el-icon>
            下载
          </button>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.courseware-panel {
  display: grid;
  gap: 16px;
}

.courseware-head h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 4px;
  font-size: 18px;
}

.courseware-head span {
  color: var(--color-text-muted);
  font-size: 13px;
}

.material-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.material-card {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  padding: 14px 16px;
  border: 2.5px solid var(--nb-ink, #1a202c);
  border-radius: 14px;
  background: #fff;
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.material-card:hover {
  border-color: var(--nb-ink, #1a202c);
  transform: translate(2px, 2px);
  box-shadow: 1px 1px 0 0 var(--nb-ink, #1a202c);
}

.material-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 20px;
}

.material-body {
  display: grid;
  gap: 4px;
}

.material-body strong {
  color: var(--color-text);
  font-size: 14px;
}

.material-body span {
  color: var(--color-text-muted);
  font-size: 12px;
}

.material-actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 2px solid var(--nb-ink, #1a202c);
  background: #fff;
  color: var(--color-text-secondary);
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.action-btn.primary {
  border-color: var(--nb-ink, #1a202c);
  background: var(--nb-green, #22c55e);
  color: #fff;
}

.action-btn:hover {
  opacity: 1;
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0 0 var(--nb-ink, #1a202c);
}
</style>
