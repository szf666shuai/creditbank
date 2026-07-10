<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { roleLabel } from '@/types/auth'
import {
  generateLearningProfileApi,
  getLearningProfileApi,
} from '@/api/learning-profile'
import type { LearningProfile } from '@/api/learning-profile'

const authStore = useAuthStore()

const user = computed(() => authStore.userInfo)
const isEnterprise = computed(() => authStore.isEnterprise)

const profile = ref<LearningProfile | null>(null)
const loadingProfile = ref(false)
const generating = ref(false)

const situation = computed(() => profile.value?.situation)
const detail = computed(() => profile.value?.profile || {})

async function loadProfile() {
  if (!authStore.isLoggedIn) return
  loadingProfile.value = true
  try {
    const res = await getLearningProfileApi()
    if (res.code === 200) {
      profile.value = res.data
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载学习画像失败')
  } finally {
    loadingProfile.value = false
  }
}

async function generateProfile() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  generating.value = true
  try {
    const res = await generateLearningProfileApi()
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '生成失败')
    }
    profile.value = res.data
    ElMessage.success('已根据你的学习情况生成画像')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '生成失败')
  } finally {
    generating.value = false
  }
}

function courseStatusText(status: unknown) {
  if (status === 1 || status === '1') return '已完成'
  if (status === 2 || status === '2') return '已退课'
  return '学习中'
}

onMounted(loadProfile)
</script>

<template>
  <div class="profile-page">
    <div class="section-inner">
      <el-card v-if="user">
        <template #header>
          <span>个人中心</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
          <el-descriptions-item label="身份">
            <el-tag>{{ roleLabel(user.role) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user.email || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="user.orgName" label="所属企业">
            {{ user.orgName }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="quick-links">
          <el-button v-if="isEnterprise" type="primary" @click="$router.push('/enterprise')">
            进入企业中心
          </el-button>
          <el-button @click="$router.push('/archive')">学习档案</el-button>
          <el-button @click="$router.push('/credit')">学分流水</el-button>
        </div>
      </el-card>

      <el-card class="learning-card" v-loading="loadingProfile">
        <template #header>
          <div class="learning-header">
            <span>AI 学习画像</span>
            <el-button type="primary" :loading="generating" @click="generateProfile">
              {{ profile?.summary ? '基于学习情况重新生成' : '基于学习情况生成画像' }}
            </el-button>
          </div>
        </template>

        <p class="learning-tip">
          画像将读取你当前的课程进度、学习档案、目标技能、学分与诚信分等数据，由 AI 汇总生成。
        </p>

        <div v-if="situation" class="situation-grid">
          <div class="stat">
            <span class="stat-label">学分余额</span>
            <span class="stat-value">{{ situation.creditBalance ?? 0 }}</span>
          </div>
          <div class="stat">
            <span class="stat-label">累计获得</span>
            <span class="stat-value">{{ situation.creditEarned ?? 0 }}</span>
          </div>
          <div class="stat">
            <span class="stat-label">诚信分</span>
            <span class="stat-value">{{ situation.integrityScore ?? '-' }}</span>
          </div>
          <div class="stat">
            <span class="stat-label">在学/完成课程</span>
            <span class="stat-value">{{ situation.courses?.length ?? 0 }}</span>
          </div>
        </div>

        <div v-if="situation?.courses?.length" class="block">
          <h4>课程学习情况</h4>
          <ul>
            <li v-for="(c, idx) in situation.courses" :key="idx">
              {{ c.courseTitle }} · 进度 {{ c.progress }}% · {{ courseStatusText(c.status) }}
              <span v-if="c.tags" class="muted">（{{ c.tags }}）</span>
            </li>
          </ul>
        </div>

        <div v-if="situation?.targetTags?.length" class="block">
          <h4>目标技能</h4>
          <div class="tags">
            <el-tag v-for="(t, idx) in situation.targetTags" :key="idx" size="small">
              {{ t.tagName }}
            </el-tag>
          </div>
        </div>

        <el-empty
          v-if="!profile?.summary"
          description="尚未生成画像，点击上方按钮，基于你的学习情况生成"
          :image-size="72"
        />

        <template v-else>
          <div class="summary-box">
            <div class="summary-meta">
              <el-tag v-if="profile.targetJob" type="success">心仪职位：{{ profile.targetJob }}</el-tag>
              <el-tag v-if="detail.stage" type="info">阶段：{{ detail.stage }}</el-tag>
              <span v-if="profile.updateTime" class="muted">更新于 {{ profile.updateTime }}</span>
            </div>
            <p class="summary-text">{{ profile.summary }}</p>
          </div>

          <div v-if="detail.skills?.length" class="block">
            <h4>技能标签</h4>
            <div class="tags">
              <el-tag v-for="(s, idx) in detail.skills" :key="idx" type="primary" effect="plain">
                {{ s }}
              </el-tag>
            </div>
          </div>

          <div class="two-col" v-if="detail.strengths?.length || detail.gaps?.length">
            <div class="block" v-if="detail.strengths?.length">
              <h4>优势</h4>
              <ul>
                <li v-for="(s, idx) in detail.strengths" :key="idx">{{ s }}</li>
              </ul>
            </div>
            <div class="block" v-if="detail.gaps?.length">
              <h4>待补齐</h4>
              <ul>
                <li v-for="(s, idx) in detail.gaps" :key="idx">{{ s }}</li>
              </ul>
            </div>
          </div>

          <div v-if="detail.suggestions?.length" class="block">
            <h4>下一步建议</h4>
            <ul>
              <li v-for="(s, idx) in detail.suggestions" :key="idx">{{ s }}</li>
            </ul>
          </div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 32px 16px 48px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quick-links {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.learning-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.learning-tip {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 16px;
  line-height: 1.6;
}

.situation-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.stat {
  background: #f7f9fc;
  border-radius: 10px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-muted);
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
}

.block {
  margin-bottom: 16px;
}

.block h4 {
  font-size: 14px;
  margin-bottom: 8px;
  color: var(--color-text);
}

.block ul {
  margin: 0;
  padding-left: 18px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  font-size: 13px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.summary-box {
  background: linear-gradient(135deg, #eef7ff 0%, #fff 80%);
  border: 1px solid #d6e9fb;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.summary-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}

.summary-text {
  font-size: 14px;
  line-height: 1.75;
  color: var(--color-text);
  margin: 0;
  white-space: pre-wrap;
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.muted {
  color: var(--color-text-muted);
  font-size: 12px;
}

@media (max-width: 768px) {
  .situation-grid,
  .two-col {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 520px) {
  .situation-grid,
  .two-col {
    grid-template-columns: 1fr;
  }
}
</style>
