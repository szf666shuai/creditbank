<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { agentChatApi } from '@/api/agent'
import type { AgentChatTurn } from '@/api/agent'
import type { SearchItem } from '@/api/search'
import { searchCategoryLabel } from '@/config/search-categories'

const props = defineProps<{
  keyword: string
  searchType: string
  loading: boolean
  items: SearchItem[]
}>()

interface Msg {
  role: 'assistant' | 'user'
  text: string
}

const messages = ref<Msg[]>([])
const input = ref('')
const sending = ref(false)
const analyzing = ref(false)
const expanded = ref(false)
const errorTip = ref('')

let requestSeq = 0

const typeLabel = computed(() => searchCategoryLabel(props.searchType))

const resultFingerprint = computed(() =>
  props.items
    .slice(0, 12)
    .map((i) => `${i.type}:${i.id}:${i.title}`)
    .join('|'),
)

const quickPrompts = computed(() => {
  const base = [
    '帮我总结这些结果',
    '我该先看哪几条？',
  ]
  if (props.searchType === 'course' || props.searchType === 'all') {
    base.push('给我一条学习路径建议')
  }
  if (props.searchType === 'job') {
    base.push('这些岗位需要补什么技能？')
  }
  if (props.searchType === 'activity') {
    base.push('哪个活动更值得参加？')
  }
  return base
})

function buildContext() {
  const lines: string[] = [
    `场景：搜索结果页智能助手`,
    `关键词：${props.keyword.trim() || '（空）'}`,
    `分类：${typeLabel.value}（${props.searchType}）`,
    `结果条数：${props.items.length}`,
  ]
  if (props.items.length) {
    lines.push('结果摘要（仅可基于这些内容回答，勿编造）：')
    props.items.slice(0, 10).forEach((item, idx) => {
      const summary = item.summary ? ` | ${item.summary.slice(0, 60)}` : ''
      const extra = item.extra ? ` | ${item.extra}` : ''
      lines.push(`${idx + 1}. [${item.typeName}] ${item.title}${summary}${extra}`)
    })
  } else {
    lines.push('当前无匹配结果。')
  }
  return lines.join('\n')
}

async function callAgent(message: string, history: AgentChatTurn[], isAnalysis = false) {
  const seq = ++requestSeq
  if (isAnalysis) analyzing.value = true
  else sending.value = true
  errorTip.value = ''
  try {
    const res = await agentChatApi(message, history, buildContext())
    if (seq !== requestSeq) return null
    if (res.code !== 200 || !res.data?.reply) {
      throw new Error(res.message || '助手暂时没有回复')
    }
    return res.data.reply
  } catch (err) {
    if (seq !== requestSeq) return null
    errorTip.value = err instanceof Error ? err.message : '网络异常'
    return null
  } finally {
    if (seq === requestSeq) {
      analyzing.value = false
      sending.value = false
    }
  }
}

async function analyzeSearch() {
  if (!props.keyword.trim() || props.loading) return
  messages.value = []
  expanded.value = false
  const prompt =
    '请结合当前搜索结果，用简洁中文分三点回答：' +
    '1) 用户可能的搜索意图；' +
    '2) 最值得关注的 2-3 条结果（点名标题）；' +
    '3) 下一步学习或行动建议。' +
    '若没有结果，说明可能原因并给出换关键词建议。不要编造结果中没有的内容。'
  const reply = await callAgent(prompt, [], true)
  if (reply) {
    messages.value = [{ role: 'assistant', text: reply }]
  }
}

async function sendFollowUp(text?: string) {
  const content = (text ?? input.value).trim()
  if (!content || sending.value || analyzing.value) return
  messages.value.push({ role: 'user', text: content })
  input.value = ''
  expanded.value = true
  const history = messages.value
    .slice(0, -1)
    .slice(-10)
    .map((m) => ({ role: m.role, content: m.text }))
  const reply = await callAgent(content, history, false)
  if (reply) {
    messages.value.push({ role: 'assistant', text: reply })
  } else if (errorTip.value) {
    messages.value.push({
      role: 'assistant',
      text: `抱歉，刚才没接上：${errorTip.value}`,
    })
  }
}

watch(
  () => [props.keyword, props.searchType, props.loading, resultFingerprint.value] as const,
  ([, , loading]) => {
    if (loading) return
    if (!props.keyword.trim()) {
      messages.value = []
      errorTip.value = ''
      return
    }
    void analyzeSearch()
  },
  { immediate: true },
)
</script>

<template>
  <div class="search-agent" :class="{ expanded }">
    <div class="search-agent-head">
      <div class="search-agent-brand">
        <span class="search-agent-dot" />
        <div class="search-agent-titles">
          <span class="search-agent-title">智能搜索助手</span>
          <span class="search-agent-sub">
            基于「{{ keyword || '当前搜索' }}」· {{ typeLabel }}
          </span>
        </div>
      </div>
      <button
        v-if="messages.length"
        type="button"
        class="search-agent-toggle"
        @click="expanded = !expanded"
      >
        {{ expanded ? '收起对话' : '展开对话' }}
      </button>
    </div>

    <div v-if="!keyword.trim()" class="search-agent-idle">
      输入关键词搜索后，我会结合结果给出解读与学习建议。
    </div>

    <div v-else-if="analyzing && !messages.length" class="search-agent-idle">
      正在结合搜索结果分析…
    </div>

    <div v-else-if="errorTip && !messages.length" class="search-agent-idle error">
      {{ errorTip }}
      <button type="button" class="retry-btn" @click="analyzeSearch">重试</button>
    </div>

    <template v-else-if="messages.length">
      <div class="search-agent-insight">
        {{ messages[0]?.role === 'assistant' ? messages[0].text : '' }}
      </div>

      <div v-if="expanded && messages.length > 1" class="search-agent-thread">
        <div
          v-for="(msg, idx) in messages.slice(1)"
          :key="idx"
          class="thread-msg"
          :class="msg.role"
        >
          {{ msg.text }}
        </div>
      </div>

      <div class="search-agent-chips">
        <button
          v-for="prompt in quickPrompts"
          :key="prompt"
          type="button"
          class="chip"
          :disabled="sending || analyzing"
          @click="sendFollowUp(prompt)"
        >
          {{ prompt }}
        </button>
      </div>

      <div class="search-agent-input">
        <input
          v-model="input"
          type="text"
          placeholder="继续追问，例如：更适合零基础吗？"
          :disabled="sending || analyzing"
          @keyup.enter="sendFollowUp()"
        />
        <button
          type="button"
          class="send-btn"
          :disabled="sending || analyzing || !input.trim()"
          @click="sendFollowUp()"
        >
          {{ sending ? '…' : '发送' }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.search-agent {
  margin-bottom: 20px;
  border: 1px solid var(--color-border-neutral);
  border-radius: 14px;
  background: var(--color-card);
  box-shadow: var(--shadow-md);
  padding: 16px 18px 14px;
}

.search-agent-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.search-agent-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.search-agent-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-primary);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--color-primary) 18%, transparent);
  flex-shrink: 0;
}

.search-agent-titles {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.search-agent-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-foreground);
  font-family: var(--font-heading);
}

.search-agent-sub {
  font-size: 12px;
  color: var(--color-muted-foreground);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-agent-toggle {
  border: none;
  background: transparent;
  color: var(--color-primary);
  font-size: 12px;
  cursor: pointer;
  flex-shrink: 0;
  padding: 4px 0;
}

.search-agent-toggle:hover {
  color: var(--color-primary-dark);
}

.search-agent-idle {
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-muted-foreground);
}

.search-agent-idle.error {
  color: var(--color-destructive);
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.retry-btn {
  border: 1px solid var(--color-destructive);
  background: var(--color-card);
  color: var(--color-destructive);
  border-radius: 6px;
  font-size: 12px;
  padding: 2px 8px;
  cursor: pointer;
}

.search-agent-insight {
  font-size: 13px;
  line-height: 1.7;
  color: var(--color-foreground);
  white-space: pre-wrap;
  max-height: 160px;
  overflow-y: auto;
  margin-bottom: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: var(--color-background);
  border: 1px solid var(--color-border-neutral);
}

.search-agent.expanded .search-agent-insight {
  max-height: 220px;
}

.search-agent-thread {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
  max-height: 220px;
  overflow-y: auto;
}

.thread-msg {
  max-width: 92%;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.thread-msg.user {
  align-self: flex-end;
  background: var(--color-primary);
  color: var(--color-on-primary);
}

.thread-msg.assistant {
  align-self: flex-start;
  background: var(--color-background);
  border: 1px solid var(--color-border-neutral);
  color: var(--color-foreground);
}

.search-agent-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.chip {
  border: 1px solid var(--color-border);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  border-radius: 999px;
  font-size: 12px;
  padding: 4px 10px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.chip:hover:not(:disabled) {
  border-color: var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 14%, white);
}

.chip:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.search-agent-input {
  display: flex;
  gap: 8px;
}

.search-agent-input input {
  flex: 1;
  height: 34px;
  border: 1px solid var(--color-border-neutral);
  border-radius: 8px;
  padding: 0 10px;
  font-size: 13px;
  outline: none;
  background: var(--color-card);
  color: var(--color-foreground);
}

.search-agent-input input::placeholder {
  color: var(--color-muted-foreground);
}

.search-agent-input input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-primary) 16%, transparent);
}

.send-btn {
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 8px;
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
