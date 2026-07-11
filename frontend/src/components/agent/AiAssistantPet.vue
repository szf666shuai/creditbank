<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { agentChatApi } from '@/api/agent'
import { generateLearningProfileApi } from '@/api/learning-profile'
import { useAuthStore } from '@/stores/auth'

import { BRAND_NAME } from '@/config/brand'

const STORAGE_KEY = 'singularis_ai_pet_pos'
const SIZE = 72
const PANEL_W = 380
const PANEL_H = 420

const greetings = [
  '你好呀，我是秩点助手～',
  '有什么学习问题尽管问我！',
  '想找课程、活动，还是秩点商城？',
  '双击我就能打开助手面板哦',
  '登录后可生成基于你学习情况的画像～',
]

const clickReplies = [
  '嘿嘿，被戳到啦～',
  '我在呢！有什么可以帮你？',
  '要不要搜一门 Java 课程？',
  '积分可以去商城兑换好礼哦',
  '诚信分也很重要，记得规范学习～',
  '按住我可以拖到任意位置',
  '双击我打开对话框吧～',
]

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)

const x = ref(0)
const y = ref(0)
const dragging = ref(false)
const panelOpen = ref(false)
const mood = ref<'idle' | 'happy' | 'think'>('idle')
const looking = ref(false)
const bubble = ref('')
const bubbleVisible = ref(false)
const chatInput = ref('')
const sending = ref(false)
const generatingProfile = ref(false)
type ChatMessage = {
  role: 'assistant' | 'user'
  text: string
  profile?: {
    targetJob: string
    summary: string
    skills: string[]
    strengths: string[]
    gaps: string[]
    suggestions: string[]
  }
}

const messages = ref<ChatMessage[]>([
  {
    role: 'assistant',
    text: `你好，我是${BRAND_NAME} AI 助手。登录后可基于你的课程进度与档案生成学习画像～`,
  },
])

const DOUBLE_CLICK_MS = 320

let dragOffsetX = 0
let dragOffsetY = 0
let movedDuringDrag = false
let lastClickAt = 0
let clickDelayTimer: ReturnType<typeof setTimeout> | null = null
let bubbleTimer: ReturnType<typeof setTimeout> | null = null
let idleTimer: ReturnType<typeof setInterval> | null = null
let moodTimer: ReturnType<typeof setTimeout> | null = null
let lookTimer: ReturnType<typeof setInterval> | null = null

const BUBBLE_W = 240

const petStyle = computed(() => ({
  left: `${x.value}px`,
  top: `${y.value}px`,
}))

const bubbleStyle = computed(() => ({
  left: `${Math.max(8, x.value - BUBBLE_W - 10)}px`,
  top: `${y.value + 4}px`,
  width: `${BUBBLE_W}px`,
}))

const panelStyle = computed(() => {
  const vw = typeof window !== 'undefined' ? window.innerWidth : 1200
  const vh = typeof window !== 'undefined' ? window.innerHeight : 800
  let left = x.value + SIZE - PANEL_W
  let top = y.value - PANEL_H - 12
  left = Math.min(Math.max(12, left), vw - PANEL_W - 12)
  if (top < 12) top = y.value + SIZE + 12
  top = Math.min(Math.max(12, top), vh - PANEL_H - 12)
  return { left: `${left}px`, top: `${top}px` }
})

function clampPosition(nx: number, ny: number) {
  const maxX = Math.max(8, window.innerWidth - SIZE - 8)
  const maxY = Math.max(8, window.innerHeight - SIZE - 8)
  x.value = Math.min(Math.max(8, nx), maxX)
  y.value = Math.min(Math.max(8, ny), maxY)
}

function defaultPosition() {
  clampPosition(window.innerWidth - SIZE - 20, Math.round(window.innerHeight * 0.4))
}

function loadPosition() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const pos = JSON.parse(raw) as { x: number; y: number }
      if (typeof pos.x === 'number' && typeof pos.y === 'number') {
        clampPosition(pos.x, pos.y)
        return
      }
    }
  } catch {
    // ignore
  }
  defaultPosition()
}

function savePosition() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ x: x.value, y: y.value }))
}

function showBubble(text: string, ms = 2600) {
  bubble.value = text
  bubbleVisible.value = true
  if (bubbleTimer) clearTimeout(bubbleTimer)
  bubbleTimer = setTimeout(() => {
    bubbleVisible.value = false
  }, ms)
}

function setMood(next: 'idle' | 'happy' | 'think', restoreMs?: number) {
  mood.value = next
  if (moodTimer) clearTimeout(moodTimer)
  if (restoreMs) {
    moodTimer = setTimeout(() => {
      mood.value = 'idle'
    }, restoreMs)
  }
}

function onPointerDown(e: PointerEvent) {
  if (e.button !== 0) return
  const el = e.currentTarget as HTMLElement
  el.setPointerCapture(e.pointerId)
  dragging.value = true
  movedDuringDrag = false
  dragOffsetX = e.clientX - x.value
  dragOffsetY = e.clientY - y.value
  if (moodTimer) clearTimeout(moodTimer)
  mood.value = 'idle'
}

function onPointerMove(e: PointerEvent) {
  if (!dragging.value) return
  const nx = e.clientX - dragOffsetX
  const ny = e.clientY - dragOffsetY
  if (Math.abs(nx - x.value) > 3 || Math.abs(ny - y.value) > 3) {
    movedDuringDrag = true
    lastClickAt = 0
    if (clickDelayTimer) {
      clearTimeout(clickDelayTimer)
      clickDelayTimer = null
    }
  }
  clampPosition(nx, ny)
}

function onPointerUp(e: PointerEvent) {
  if (!dragging.value) return
  const el = e.currentTarget as HTMLElement
  if (el.hasPointerCapture?.(e.pointerId)) {
    el.releasePointerCapture(e.pointerId)
  }
  dragging.value = false
  savePosition()
  if (!movedDuringDrag) onPetTap()
}

function onPetTap() {
  const now = Date.now()
  if (now - lastClickAt < DOUBLE_CLICK_MS) {
    lastClickAt = 0
    if (clickDelayTimer) {
      clearTimeout(clickDelayTimer)
      clickDelayTimer = null
    }
    openPanel()
    return
  }
  lastClickAt = now
  if (clickDelayTimer) clearTimeout(clickDelayTimer)
  clickDelayTimer = setTimeout(() => {
    clickDelayTimer = null
    handleClick()
  }, DOUBLE_CLICK_MS)
}

function handleClick() {
  setMood('happy', 900)
  showBubble(clickReplies[Math.floor(Math.random() * clickReplies.length)])
}

function openPanel() {
  if (panelOpen.value) return
  panelOpen.value = true
  setMood('think')
  showBubble('助手面板已打开～', 1800)
}

function togglePanel() {
  if (panelOpen.value) {
    panelOpen.value = false
    setMood('idle')
    return
  }
  openPanel()
}

async function sendMessage() {
  const text = chatInput.value.trim()
  if (!text || sending.value || generatingProfile.value) return
  messages.value.push({ role: 'user', text })
  chatInput.value = ''
  sending.value = true
  setMood('think')

  const history = messages.value
    .slice(0, -1)
    .filter((m) => m.text)
    .slice(-12)
    .map((m) => ({
      role: m.role,
      content: m.text,
    }))

  try {
    const res = await agentChatApi(text, history)
    if (res.code !== 200 || !res.data?.reply) {
      throw new Error(res.message || '助手暂时没有回复')
    }
    messages.value.push({ role: 'assistant', text: res.data.reply })
    setMood('happy', 800)
  } catch (err) {
    const tip = err instanceof Error ? err.message : '网络异常，请稍后再试'
    messages.value.push({
      role: 'assistant',
      text: `抱歉，刚才没接上：${tip}`,
    })
    setMood('idle')
  } finally {
    sending.value = false
  }
}

async function generateMyProfile() {
  if (!isLoggedIn.value) {
    messages.value.push({
      role: 'assistant',
      text: '生成学习画像需要先登录哦，请先登录后再试。',
    })
    return
  }
  if (generatingProfile.value || sending.value) return
  generatingProfile.value = true
  setMood('think')
  messages.value.push({
    role: 'user',
    text: '请根据我目前的学习情况生成学习画像',
  })
  try {
    const res = await generateLearningProfileApi()
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '生成失败')
    }
    const p = res.data
    messages.value.push({
      role: 'assistant',
      text: '已根据你的课程进度、档案与秩点情况生成画像。',
      profile: {
        targetJob: p.targetJob || '待明确',
        summary: p.summary || '暂无摘要',
        skills: p.profile?.skills?.slice(0, 6) || [],
        strengths: p.profile?.strengths?.slice(0, 4) || [],
        gaps: p.profile?.gaps?.slice(0, 4) || [],
        suggestions: p.profile?.suggestions?.slice(0, 3) || [],
      },
    })
    setMood('happy', 1000)
    showBubble('学习画像已生成～', 2200)
  } catch (err) {
    const tip = err instanceof Error ? err.message : '生成失败'
    messages.value.push({
      role: 'assistant',
      text: `画像生成失败：${tip}`,
    })
    setMood('idle')
  } finally {
    generatingProfile.value = false
  }
}

function onResize() {
  clampPosition(x.value, y.value)
  savePosition()
}

onMounted(() => {
  loadPosition()
  showBubble(greetings[Math.floor(Math.random() * greetings.length)], 3200)
  idleTimer = setInterval(() => {
    if (dragging.value || panelOpen.value || bubbleVisible.value) return
    if (Math.random() > 0.5) return
    showBubble(greetings[Math.floor(Math.random() * greetings.length)], 2200)
  }, 18000)
  lookTimer = setInterval(() => {
    if (dragging.value || mood.value !== 'idle') return
    looking.value = true
    window.setTimeout(() => {
      looking.value = false
    }, 700)
  }, 5200)
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  if (bubbleTimer) clearTimeout(bubbleTimer)
  if (idleTimer) clearInterval(idleTimer)
  if (moodTimer) clearTimeout(moodTimer)
  if (lookTimer) clearInterval(lookTimer)
  if (clickDelayTimer) clearTimeout(clickDelayTimer)
  window.removeEventListener('resize', onResize)
})
</script>

<template>
  <div class="ai-pet-root" aria-live="polite">
    <Transition name="ai-panel">
      <div v-if="panelOpen" class="ai-panel" :style="panelStyle">
        <div class="ai-panel-header">
          <div class="ai-panel-title">
            <span class="ai-panel-dot" />
            秩点 AI 助手
          </div>
          <button type="button" class="ai-panel-close" aria-label="关闭" @click="panelOpen = false">
            ×
          </button>
        </div>
        <div class="ai-panel-body">
          <div
            v-for="(msg, idx) in messages"
            :key="idx"
            class="ai-msg"
            :class="[msg.role, { 'has-profile': !!msg.profile }]"
          >
            <p class="ai-msg-text">{{ msg.text }}</p>
            <div v-if="msg.profile" class="ai-profile-card">
              <div class="ai-profile-row">
                <span class="ai-profile-label">心仪职位</span>
                <span class="ai-profile-job">{{ msg.profile.targetJob }}</span>
              </div>
              <div class="ai-profile-row">
                <span class="ai-profile-label">摘要</span>
                <p class="ai-profile-summary">{{ msg.profile.summary }}</p>
              </div>
              <div v-if="msg.profile.skills.length" class="ai-profile-row">
                <span class="ai-profile-label">技能</span>
                <div class="ai-profile-tags">
                  <span
                    v-for="(skill, sIdx) in msg.profile.skills"
                    :key="`skill-${sIdx}`"
                    class="ai-profile-tag"
                  >
                    {{ skill }}
                  </span>
                </div>
              </div>
              <div v-if="msg.profile.strengths.length" class="ai-profile-row">
                <span class="ai-profile-label">优势</span>
                <ul class="ai-profile-list">
                  <li v-for="(item, i) in msg.profile.strengths" :key="`str-${i}`">
                    {{ item }}
                  </li>
                </ul>
              </div>
              <div v-if="msg.profile.gaps.length" class="ai-profile-row">
                <span class="ai-profile-label">待补齐</span>
                <ul class="ai-profile-list">
                  <li v-for="(item, i) in msg.profile.gaps" :key="`gap-${i}`">
                    {{ item }}
                  </li>
                </ul>
              </div>
              <div v-if="msg.profile.suggestions.length" class="ai-profile-row">
                <span class="ai-profile-label">建议</span>
                <ol class="ai-profile-list numbered">
                  <li v-for="(item, i) in msg.profile.suggestions" :key="`tip-${i}`">
                    {{ item }}
                  </li>
                </ol>
              </div>
              <p class="ai-profile-foot">也可在「个人中心」查看完整画像</p>
            </div>
          </div>
        </div>
        <div class="ai-panel-actions">
          <button
            type="button"
            class="ai-action"
            :disabled="sending || generatingProfile"
            @click="generateMyProfile"
          >
            {{ generatingProfile ? '正在分析学习情况…' : '生成我的学习画像' }}
          </button>
        </div>
        <div class="ai-panel-footer">
          <input
            v-model="chatInput"
            type="text"
            placeholder="输入问题…"
            :disabled="sending || generatingProfile"
            @keyup.enter="sendMessage"
          />
          <button
            type="button"
            class="ai-send"
            :disabled="sending || generatingProfile"
            @click="sendMessage"
          >
            {{ sending ? '…' : '发送' }}
          </button>
        </div>
      </div>
    </Transition>

    <Transition name="ai-bubble">
      <div v-if="bubbleVisible" class="ai-bubble" :style="bubbleStyle">{{ bubble }}</div>
    </Transition>

    <div
      class="ai-pet"
      :class="[{ dragging, looking }, mood]"
      :style="petStyle"
      role="button"
      tabindex="0"
      aria-label="AI 助手桌宠，可拖动，单击互动，双击打开面板"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointercancel="onPointerUp"
      @keydown.enter.prevent="handleClick"
      @keydown.space.prevent="handleClick"
    >
      <div class="ai-avatar">
        <div class="ai-face">
          <span class="eye left" />
          <span class="eye right" />
          <span class="mouth" />
        </div>
        <div class="ai-ring" />
      </div>

      <button
        type="button"
        class="ai-open-btn"
        title="打开助手"
        @pointerdown.stop
        @click.stop="togglePanel"
      >
        AI
      </button>
    </div>
  </div>
</template>

<style scoped>
.ai-pet-root {
  position: fixed;
  inset: 0;
  z-index: 1100;
  pointer-events: none;
}

.ai-pet {
  position: fixed;
  width: 72px;
  height: 72px;
  pointer-events: auto;
  cursor: grab;
  user-select: none;
  touch-action: none;
  z-index: 1101;
  overflow: visible;
}

.ai-pet.dragging {
  cursor: grabbing;
  animation: none;
}

.ai-pet.dragging .ai-avatar {
  transform: scale(1.04, 0.9);
  transition: transform 0.12s ease;
}

.ai-pet:not(.dragging) {
  animation: pet-float 3.2s ease-in-out infinite;
}

.ai-avatar {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(160deg, #4db3ff 0%, #2094f3 55%, #1a7fd4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(32, 148, 243, 0.4);
  overflow: hidden;
  transition: transform 0.2s ease;
}

.ai-ring {
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.35);
  pointer-events: none;
}

.ai-face {
  position: relative;
  width: 36px;
  height: 28px;
  transition: transform 0.35s ease;
}

.ai-pet.looking .ai-face {
  transform: translateX(3px);
}

.eye {
  position: absolute;
  top: 4px;
  width: 7px;
  height: 9px;
  background: #fff;
  border-radius: 50%;
  transform-origin: center;
  animation: blink 2.2s infinite;
  transition: height 0.12s ease, top 0.12s ease, border-radius 0.12s ease, width 0.12s ease;
}

.eye.left {
  left: 4px;
  animation-delay: 0s;
}

.eye.right {
  right: 4px;
  animation-delay: 0.08s;
}

.mouth {
  position: absolute;
  left: 50%;
  bottom: 2px;
  width: 12px;
  height: 6px;
  margin-left: -6px;
  border: 2px solid rgba(255, 255, 255, 0.95);
  border-top: none;
  border-radius: 0 0 10px 10px;
  transition: width 0.15s ease, height 0.15s ease, margin-left 0.15s ease;
}

.ai-pet.happy .mouth {
  width: 16px;
  margin-left: -8px;
  height: 8px;
}

.ai-pet.dragging .eye {
  height: 2px;
  top: 9px;
  width: 9px;
  border-radius: 2px;
  animation: none;
}

.ai-pet.dragging .eye.left {
  left: 3px;
}

.ai-pet.dragging .eye.right {
  right: 3px;
}

.ai-pet.dragging .mouth {
  width: 8px;
  height: 4px;
  margin-left: -4px;
  border-radius: 0 0 6px 6px;
}

.ai-pet.think .eye {
  height: 3px;
  top: 8px;
  border-radius: 2px;
  animation: none;
}

.ai-pet.happy .ai-avatar {
  transform: scale(1.06);
}

.ai-pet.happy .eye {
  animation: blink-fast 1.4s infinite;
}

.ai-open-btn {
  position: absolute;
  right: -4px;
  bottom: -2px;
  width: 24px;
  height: 24px;
  border: 2px solid #fff;
  border-radius: 50%;
  background: #0d4d8a;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  cursor: pointer;
  line-height: 1;
  padding: 0;
}

.ai-bubble {
  position: fixed;
  z-index: 1102;
  padding: 10px 14px;
  background: #fff;
  color: var(--color-text);
  font-size: 13px;
  line-height: 1.55;
  border-radius: 12px 12px 4px 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.14);
  white-space: normal;
  word-break: keep-all;
  overflow-wrap: break-word;
  pointer-events: none;
  box-sizing: border-box;
}

.ai-bubble::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 14px;
  border: 6px solid transparent;
  border-left-color: #fff;
  border-right: 0;
}

.ai-panel {
  position: fixed;
  width: 380px;
  height: 420px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  pointer-events: auto;
  z-index: 1102;
}

.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: #2094f3;
  color: #fff;
}

.ai-panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
}

.ai-panel-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #7dffb3;
}

.ai-panel-close {
  border: none;
  background: transparent;
  color: #fff;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  padding: 0 4px;
}

.ai-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f7f9fc;
}

.ai-msg {
  max-width: 92%;
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  letter-spacing: 0.02em;
}

.ai-msg.has-profile {
  max-width: 100%;
  width: 100%;
  padding: 12px;
}

.ai-msg-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-msg.assistant {
  align-self: flex-start;
  background: #fff;
  color: var(--color-text);
  border: 1px solid #e8eef5;
}

.ai-msg.user {
  align-self: flex-end;
  background: #2094f3;
  color: #fff;
}

.ai-profile-card {
  margin-top: 10px;
  padding: 12px;
  border-radius: 10px;
  background: linear-gradient(180deg, #f4f9ff 0%, #f8fbff 100%);
  border: 1px solid #dceaf8;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-profile-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ai-profile-label {
  font-size: 12px;
  font-weight: 600;
  color: #5a7a9a;
  letter-spacing: 0.04em;
}

.ai-profile-job {
  display: inline-flex;
  align-self: flex-start;
  padding: 4px 10px;
  border-radius: 999px;
  background: #2094f3;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
}

.ai-profile-summary {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #334155;
}

.ai-profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ai-profile-tag {
  padding: 3px 8px;
  border-radius: 6px;
  background: #fff;
  border: 1px solid #cfe3f7;
  color: #1a7fd4;
  font-size: 12px;
  line-height: 1.4;
}

.ai-profile-list {
  margin: 0;
  padding-left: 1.1em;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  line-height: 1.55;
  color: #334155;
}

.ai-profile-list.numbered {
  padding-left: 1.25em;
}

.ai-profile-foot {
  margin: 2px 0 0;
  padding-top: 10px;
  border-top: 1px dashed #d5e4f3;
  font-size: 12px;
  color: #7a90a8;
  line-height: 1.4;
}

.ai-panel-footer {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #eef2f6;
  background: #fff;
}

.ai-panel-actions {
  padding: 0 16px 10px;
  background: #f7f9fc;
  border-top: 1px solid #eef2f6;
}

.ai-action {
  width: 100%;
  height: 34px;
  border: 1px solid rgba(32, 148, 243, 0.35);
  border-radius: 8px;
  background: #fff;
  color: #1a7fd4;
  font-size: 13px;
  cursor: pointer;
}

.ai-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ai-panel-footer input {
  flex: 1;
  height: 38px;
  border: 1px solid #dce3ea;
  border-radius: 8px;
  padding: 0 12px;
  outline: none;
  font-size: 14px;
}

.ai-panel-footer input:focus {
  border-color: #2094f3;
}

.ai-send {
  height: 38px;
  padding: 0 16px;
  border: none;
  border-radius: 8px;
  background: #2094f3;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.ai-send:hover {
  background: #1a7fd4;
}

.ai-send:disabled,
.ai-panel-footer input:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

@keyframes pet-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}

@keyframes blink {
  0%,
  78%,
  100% {
    transform: scaleY(1);
  }
  82%,
  88% {
    transform: scaleY(0.08);
  }
  85% {
    transform: scaleY(1);
  }
  91% {
    transform: scaleY(0.08);
  }
  94% {
    transform: scaleY(1);
  }
}

@keyframes blink-fast {
  0%,
  70%,
  100% {
    transform: scaleY(1);
  }
  75%,
  85% {
    transform: scaleY(0.08);
  }
  80% {
    transform: scaleY(1);
  }
}

.ai-bubble-enter-active,
.ai-bubble-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.ai-bubble-enter-from,
.ai-bubble-leave-to {
  opacity: 0;
  transform: translateX(8px);
}

.ai-panel-enter-active,
.ai-panel-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.ai-panel-enter-from,
.ai-panel-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.96);
}

@media (max-width: 640px) {
  .ai-bubble {
    font-size: 12px;
  }

  .ai-panel {
    width: min(380px, calc(100vw - 24px));
    height: min(420px, calc(100vh - 100px));
  }
}
</style>
