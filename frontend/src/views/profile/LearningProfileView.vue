<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import UiIcon from '@/components/ui/UiIcon.vue'
import {
  generateLearningProfileApi,
  getLearningProfileApi,
  type LearningProfile,
} from '@/api/learning-profile'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const generating = ref(false)
const loadError = ref<string | null>(null)
const data = ref<LearningProfile | null>(null)

const hasProfile = computed(() => {
  const p = data.value
  if (!p) return false
  return !!(p.summary || p.targetJob || (p.profile && Object.keys(p.profile).length))
})

const skills = computed(() => asStringList(data.value?.profile?.skills))
const strengths = computed(() => asStringList(data.value?.profile?.strengths))
const gaps = computed(() => asStringList(data.value?.profile?.gaps))
const suggestions = computed(() => asStringList(data.value?.profile?.suggestions))
const stage = computed(() => {
  const value = data.value?.profile?.stage
  return typeof value === 'string' ? value : ''
})

function asStringList(value: unknown): string[] {
  if (!Array.isArray(value)) return []
  return value.filter((item): item is string => typeof item === 'string' && item.trim().length > 0)
}

async function fetchProfile() {
  loading.value = true
  loadError.value = null
  try {
    data.value = unwrapApi(await getLearningProfileApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载学习画像失败')
  } finally {
    loading.value = false
  }
}

async function handleGenerate() {
  if (generating.value) return
  generating.value = true
  try {
    data.value = unwrapApi(await generateLearningProfileApi())
    ElMessage.success('学习画像已更新')
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '生成失败'))
  } finally {
    generating.value = false
  }
}

onMounted(fetchProfile)
</script>

<template>
  <PageShell
    title="学习画像"
    description="基于课程进度、学习档案与秩点情况，由 AI 生成的个人学习画像"
    :loading="loading"
    :error="loadError"
    @retry="fetchProfile"
  >
    <template #actions>
      <el-button type="primary" :loading="generating" @click="handleGenerate">
        {{ hasProfile ? '重新生成画像' : '生成学习画像' }}
      </el-button>
    </template>

    <div v-if="!hasProfile" class="empty-wrap">
      <el-empty description="暂无学习画像，点击右上角生成">
        <el-button type="primary" :loading="generating" @click="handleGenerate">
          立即生成
        </el-button>
      </el-empty>
    </div>

    <div v-else class="portrait">
      <section class="portrait-hero">
        <div class="portrait-hero__icon">
          <UiIcon name="user" :size="28" />
        </div>
        <div class="portrait-hero__main">
          <div class="portrait-job">{{ data?.targetJob || '目标岗位待明确' }}</div>
          <p class="portrait-summary">{{ data?.summary || '暂无摘要' }}</p>
          <div class="portrait-meta">
            <span v-if="stage" class="portrait-chip">学习阶段 · {{ stage }}</span>
            <span v-if="data?.updateTime" class="portrait-time">
              更新于 {{ formatTime(data.updateTime) }}
            </span>
          </div>
        </div>
      </section>

      <div class="portrait-grid">
        <section v-if="skills.length" class="portrait-block">
          <h2>技能标签</h2>
          <div class="tag-list">
            <span v-for="(item, idx) in skills" :key="`skill-${idx}`" class="tag">{{ item }}</span>
          </div>
        </section>

        <section v-if="strengths.length" class="portrait-block">
          <h2>优势</h2>
          <ul class="item-list">
            <li v-for="(item, idx) in strengths" :key="`str-${idx}`">{{ item }}</li>
          </ul>
        </section>

        <section v-if="gaps.length" class="portrait-block">
          <h2>待补齐</h2>
          <ul class="item-list">
            <li v-for="(item, idx) in gaps" :key="`gap-${idx}`">{{ item }}</li>
          </ul>
        </section>

        <section v-if="suggestions.length" class="portrait-block portrait-block--wide">
          <h2>学习建议</h2>
          <ol class="item-list numbered">
            <li v-for="(item, idx) in suggestions" :key="`tip-${idx}`">{{ item }}</li>
          </ol>
        </section>
      </div>
    </div>
  </PageShell>
</template>

<style scoped>
.empty-wrap {
  padding: 48px 0;
}

.portrait {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.portrait-hero {
  display: flex;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.18), rgba(56, 189, 248, 0.08));
  border: 1px solid rgba(56, 189, 248, 0.28);
}

.portrait-hero__icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: rgba(24, 144, 255, 0.2);
  flex-shrink: 0;
}

.portrait-job {
  font-size: 20px;
  font-weight: 700;
  color: #e8f4ff;
  line-height: 1.3;
}

.portrait-summary {
  margin: 8px 0 0;
  color: rgba(226, 239, 255, 0.88);
  line-height: 1.7;
  font-size: 14px;
}

.portrait-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
}

.portrait-chip {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #9fd4ff;
  background: rgba(24, 144, 255, 0.18);
  border: 1px solid rgba(56, 189, 248, 0.28);
}

.portrait-time {
  font-size: 12px;
  color: rgba(186, 214, 240, 0.72);
}

.portrait-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.portrait-block {
  padding: 16px 18px;
  border-radius: 12px;
  background: rgba(10, 28, 52, 0.35);
  border: 1px solid rgba(125, 186, 255, 0.16);
}

.portrait-block--wide {
  grid-column: 1 / -1;
}

.portrait-block h2 {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 650;
  color: #d7ebff;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 5px 11px;
  border-radius: 999px;
  font-size: 13px;
  color: #b8dcff;
  background: rgba(24, 144, 255, 0.16);
  border: 1px solid rgba(56, 189, 248, 0.24);
}

.item-list {
  margin: 0;
  padding-left: 18px;
  color: rgba(220, 236, 255, 0.9);
  line-height: 1.75;
  font-size: 14px;
}

.item-list.numbered {
  padding-left: 20px;
}

@media (max-width: 768px) {
  .portrait-hero {
    flex-direction: column;
  }

  .portrait-grid {
    grid-template-columns: 1fr;
  }
}
</style>
