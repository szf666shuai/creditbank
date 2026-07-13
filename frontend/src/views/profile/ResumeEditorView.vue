<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import PageShell from '@/components/common/PageShell.vue'
import {
  createEmptyResumeContent,
  createResumeApi,
  generateResumeAiApi,
  getResumeApi,
  updateResumeApi,
  type ResumeContent,
} from '@/api/profile-resume'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const aiGenerating = ref(false)
const aiDialogVisible = ref(false)
const aiForm = reactive({
  targetRole: '',
  extraHint: '',
})
const version = ref<number | undefined>()
const updateTime = ref<string | undefined>()
const isDefault = ref<number | undefined>()

const isCreate = computed(() => route.path.endsWith('/new'))
const resumeId = computed(() => (isCreate.value ? null : Number(route.params.id)))

const pageTitle = computed(() => (isCreate.value ? '新建简历' : '编辑简历'))

const form = reactive({
  title: '',
  fileUrl: '',
  content: createEmptyResumeContent() as ResumeContent,
})

async function loadResume() {
  if (isCreate.value) {
    form.title = ''
    form.fileUrl = ''
    form.content = createEmptyResumeContent()
    version.value = undefined
    updateTime.value = undefined
    isDefault.value = undefined
    return
  }

  if (!resumeId.value || Number.isNaN(resumeId.value)) {
    loadError.value = '无效的简历 ID'
    return
  }

  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(await getResumeApi(resumeId.value))
    form.title = data.title || ''
    form.fileUrl = data.fileUrl || ''
    form.content = { ...createEmptyResumeContent(), ...data.content }
    version.value = data.version
    updateTime.value = data.updateTime
    isDefault.value = data.isDefault
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!form.content.realName.trim()) {
    ElMessage.warning('请填写姓名')
    return
  }

  const payload = {
    title: form.title.trim() || '未命名简历',
    content: {
      realName: form.content.realName.trim(),
      phone: form.content.phone.trim(),
      email: form.content.email.trim(),
      education: form.content.education.trim(),
      workExperience: form.content.workExperience.trim(),
      skills: form.content.skills.trim(),
      selfIntro: form.content.selfIntro.trim(),
      projects: form.content.projects.trim(),
    },
    fileUrl: form.fileUrl.trim() || undefined,
  }

  saving.value = true
  try {
    if (isCreate.value) {
      const data = unwrapApi(await createResumeApi(payload))
      ElMessage.success('简历已创建')
      router.replace(`/profile/resume/${data.id}`)
    } else if (resumeId.value) {
      const data = unwrapApi(await updateResumeApi(resumeId.value, payload))
      ElMessage.success('简历已保存')
      version.value = data.version
      updateTime.value = data.updateTime
      isDefault.value = data.isDefault
    }
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/profile/resume')
}

function openAiDialog() {
  aiForm.targetRole = form.title.trim() || ''
  aiForm.extraHint = ''
  aiDialogVisible.value = true
}

async function handleAiGenerate() {
  if (!aiForm.targetRole.trim()) {
    ElMessage.warning('请填写目标岗位或方向')
    return
  }

  aiGenerating.value = true
  try {
    const content = unwrapApi(
      await generateResumeAiApi({
        targetRole: aiForm.targetRole.trim(),
        extraHint: aiForm.extraHint.trim() || undefined,
      }),
    )
    form.content = { ...createEmptyResumeContent(), ...content }
    if (!form.title.trim()) {
      form.title = `${aiForm.targetRole.trim()} · AI 简历`
    }
    aiDialogVisible.value = false
    ElMessage.success('AI 已生成简历内容，请核对后保存')
  } catch (e) {
    ElMessage.error(getErrorMessage(e, 'AI 生成失败'))
  } finally {
    aiGenerating.value = false
  }
}

onMounted(loadResume)

watch(
  () => route.fullPath,
  () => loadResume(),
)
</script>

<template>
  <PageShell
    :title="pageTitle"
    description="填写结构化简历内容，投递时将使用「默认投递」版本（可在列表页设置）"
    :loading="loading"
    :error="loadError"
    @retry="loadResume"
  >
    <template #header>
      <div class="editor-header">
        <el-button link type="primary" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回简历列表
        </el-button>
        <div class="header-actions">
          <el-button type="primary" plain @click="openAiDialog">AI 生成</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </div>
      </div>
    </template>

    <div v-if="version" class="meta-bar">
      <span>版本 v{{ version }}</span>
      <span v-if="isDefault === 1">默认投递简历</span>
      <span>最近更新：{{ formatTime(updateTime) }}</span>
    </div>

    <el-form label-width="96px" class="resume-form">
      <el-form-item label="简历名称">
        <el-input
          v-model="form.title"
          maxlength="100"
          show-word-limit
          placeholder="例如：Java 后端方向 / 某企业定制版"
        />
      </el-form-item>

      <div class="form-section-title">基本信息</div>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-form-item label="姓名" required>
            <el-input v-model="form.content.realName" maxlength="50" placeholder="真实姓名" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="手机号">
            <el-input v-model="form.content.phone" maxlength="20" placeholder="联系电话" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="邮箱">
        <el-input v-model="form.content.email" maxlength="100" placeholder="联系邮箱" />
      </el-form-item>

      <div class="form-section-title">教育背景</div>
      <el-form-item label="教育经历">
        <el-input
          v-model="form.content.education"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="学历、专业、毕业院校等"
        />
      </el-form-item>

      <div class="form-section-title">工作与项目</div>
      <el-form-item label="工作经历">
        <el-input
          v-model="form.content.workExperience"
          type="textarea"
          :rows="5"
          maxlength="2000"
          show-word-limit
          placeholder="公司、岗位、时间与主要工作内容"
        />
      </el-form-item>
      <el-form-item label="项目经历">
        <el-input
          v-model="form.content.projects"
          type="textarea"
          :rows="4"
          maxlength="2000"
          show-word-limit
          placeholder="项目名称、技术栈与个人贡献"
        />
      </el-form-item>

      <div class="form-section-title">技能与简介</div>
      <el-form-item label="技能标签">
        <el-input
          v-model="form.content.skills"
          maxlength="300"
          show-word-limit
          placeholder="Java, Spring Boot, Vue3 等，逗号分隔"
        />
      </el-form-item>
      <el-form-item label="自我评价">
        <el-input
          v-model="form.content.selfIntro"
          type="textarea"
          :rows="4"
          maxlength="1000"
          show-word-limit
          placeholder="简要介绍个人优势与求职意向"
        />
      </el-form-item>

      <div class="form-section-title">附件（可选）</div>
      <el-form-item label="附件链接">
        <el-input v-model="form.fileUrl" maxlength="255" placeholder="简历 PDF/Word 文件 URL（可选）" />
      </el-form-item>
    </el-form>
  </PageShell>

  <el-dialog v-model="aiDialogVisible" title="AI 生成简历" width="480px" destroy-on-close>
    <el-form label-width="96px">
      <el-form-item label="目标岗位" required>
        <el-input
          v-model="aiForm.targetRole"
          maxlength="80"
          placeholder="如：Java 后端开发、数据分析实习生"
        />
      </el-form-item>
      <el-form-item label="补充说明">
        <el-input
          v-model="aiForm.extraHint"
          type="textarea"
          :rows="3"
          maxlength="300"
          show-word-limit
          placeholder="如：突出 Spring Boot 项目、应届生、希望强调活动经历等"
        />
      </el-form-item>
      <p class="ai-tip">将结合你的账号信息、学习档案与成果生成草稿，生成后请自行核对再保存。</p>
    </el-form>
    <template #footer>
      <el-button @click="aiDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="aiGenerating" @click="handleAiGenerate">开始生成</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.meta-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 20px;
  padding: 10px 14px;
  background: var(--color-primary-light);
  border-radius: 8px;
}

.form-section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 8px 0 16px;
  padding-left: 10px;
  border-left: 3px solid var(--color-primary);
}

.resume-form {
  max-width: 900px;
}

.ai-tip {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.6;
}
</style>
