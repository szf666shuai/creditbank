<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  CaretRight,
  FullScreen,
  Mute,
} from '@element-plus/icons-vue'
import type { CourseDanmaku } from '@/api/learning'
import { DANMAKU_COLORS, type DanmakuColor } from '@/config/danmaku'
import DanmakuLayer from '@/components/learning/DanmakuLayer.vue'

const props = defineProps<{
  videoUrl: string
  danmakuItems: CourseDanmaku[]
  danmakuEnabled: boolean
  unlockedPositionSeconds: number
}>()

const emit = defineEmits<{
  loadedmetadata: []
  play: []
  pause: []
  timeupdate: [currentTime: number]
  seeking: [currentTime: number]
  ended: []
  'update:danmakuEnabled': [value: boolean]
  sendDanmaku: [payload: { content: string; color: string }]
}>()

const videoRef = ref<HTMLVideoElement | null>(null)
const playerShell = ref<HTMLElement | null>(null)
const playing = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(0.8)
const muted = ref(false)
const playbackRate = ref(1)
const danmakuInput = ref('')
const selectedDanmakuColor = ref<DanmakuColor>(DANMAKU_COLORS[0].value)
const controlsVisible = ref(true)
const isFullscreen = ref(false)
let hideControlsTimer: number | null = null

const speedOptions = [0.5, 0.75, 1, 1.25, 1.5, 2]

const progressPercent = computed(() =>
  duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0,
)

function formatClock(seconds: number) {
  const safe = Math.max(0, Math.floor(seconds))
  const minutes = Math.floor(safe / 60)
  const remaining = safe % 60
  return `${minutes}:${remaining.toString().padStart(2, '0')}`
}

function showControls() {
  controlsVisible.value = true
  if (hideControlsTimer) window.clearTimeout(hideControlsTimer)
  if (playing.value) {
    hideControlsTimer = window.setTimeout(() => {
      controlsVisible.value = false
    }, 2800)
  }
}

function togglePlay() {
  const player = videoRef.value
  if (!player) return
  if (player.paused) {
    void player.play()
  } else {
    player.pause()
  }
}

function handleLoadedMetadata() {
  const player = videoRef.value
  if (!player) return
  duration.value = Number.isFinite(player.duration) ? player.duration : 0
  player.volume = volume.value
  player.playbackRate = playbackRate.value
  emit('loadedmetadata')
}

function handleTimeUpdate() {
  const player = videoRef.value
  if (!player) return
  currentTime.value = player.currentTime
  emit('timeupdate', player.currentTime)
}

function handleSeekInput(value: number) {
  const player = videoRef.value
  if (!player || duration.value <= 0) return
  const nextTime = (value / 100) * duration.value
  emit('seeking', nextTime)
  player.currentTime = nextTime
  currentTime.value = player.currentTime
}

function changeVolume(value: number) {
  volume.value = value
  muted.value = value === 0
  if (videoRef.value) {
    videoRef.value.volume = value
    videoRef.value.muted = value === 0
  }
}

function toggleMute() {
  muted.value = !muted.value
  if (videoRef.value) {
    videoRef.value.muted = muted.value
    if (!muted.value && volume.value === 0) {
      volume.value = 0.6
      videoRef.value.volume = 0.6
    }
  }
}

function changeSpeed(rate: number) {
  playbackRate.value = rate
  if (videoRef.value) {
    videoRef.value.playbackRate = rate
  }
}

function toggleDanmaku() {
  emit('update:danmakuEnabled', !props.danmakuEnabled)
}

function submitDanmaku() {
  const content = danmakuInput.value.trim()
  if (!content) return
  emit('sendDanmaku', { content, color: selectedDanmakuColor.value })
  danmakuInput.value = ''
}

function toggleFullscreen() {
  const shell = playerShell.value
  if (!shell) return
  if (!document.fullscreenElement) {
    void shell.requestFullscreen()
  } else {
    void document.exitFullscreen()
  }
}

function handleFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}

function pause() {
  videoRef.value?.pause()
}

function getPlayer() {
  return videoRef.value
}

defineExpose({ pause, getPlayer })

watch(
  () => props.videoUrl,
  () => {
    playing.value = false
    currentTime.value = 0
    duration.value = 0
  },
)

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  if (hideControlsTimer) window.clearTimeout(hideControlsTimer)
})
</script>

<template>
  <div
    ref="playerShell"
    class="course-player"
    :class="{ 'is-fullscreen': isFullscreen }"
    @mousemove="showControls"
    @mouseleave="controlsVisible = !playing"
  >
    <video
      ref="videoRef"
      class="course-player__video"
      :src="videoUrl"
      preload="metadata"
      playsinline
      @loadedmetadata="handleLoadedMetadata"
      @play="playing = true; emit('play'); showControls()"
      @pause="playing = false; emit('pause'); controlsVisible = true"
      @timeupdate="handleTimeUpdate"
      @seeking="emit('seeking', videoRef?.currentTime || 0)"
      @ended="emit('ended')"
      @click="togglePlay"
    />

    <DanmakuLayer
      :items="danmakuItems"
      :current-time="currentTime"
      :enabled="danmakuEnabled"
      :playing="playing"
    />

    <div class="course-player__center-action" :class="{ hidden: playing && !controlsVisible }">
      <button type="button" class="center-btn" @click.stop="togglePlay">
        <el-icon v-if="!playing" :size="42"><CaretRight /></el-icon>
        <span v-else class="pause-glyph" />
      </button>
    </div>

    <div class="course-player__controls" :class="{ hidden: !controlsVisible }">
      <div class="progress-row">
        <input
          class="progress-slider"
          type="range"
          min="0"
          max="100"
          step="0.1"
          :value="progressPercent"
          @input="handleSeekInput(Number(($event.target as HTMLInputElement).value))"
        >
      </div>

      <div class="toolbar-row">
        <div class="toolbar-left">
          <button type="button" class="icon-btn" @click="togglePlay">
            <el-icon v-if="!playing"><CaretRight /></el-icon>
            <span v-else class="pause-glyph small" />
          </button>
          <span class="time-text">{{ formatClock(currentTime) }} / {{ formatClock(duration) }}</span>
        </div>

        <div class="toolbar-right">
          <button
            type="button"
            class="text-btn"
            :class="{ active: danmakuEnabled }"
            @click="toggleDanmaku"
          >
            弹
          </button>

          <el-dropdown trigger="click" @command="changeSpeed">
            <button type="button" class="text-btn">{{ playbackRate === 1 ? '倍速' : `${playbackRate}x` }}</button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="rate in speedOptions"
                  :key="rate"
                  :command="rate"
                  :class="{ 'is-active': playbackRate === rate }"
                >
                  {{ rate }}x
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <button type="button" class="icon-btn" @click="toggleMute">
            <el-icon><Mute /></el-icon>
          </button>
          <input
            class="volume-slider"
            type="range"
            min="0"
            max="1"
            step="0.05"
            :value="muted ? 0 : volume"
            @input="changeVolume(Number(($event.target as HTMLInputElement).value))"
          >

          <button type="button" class="icon-btn" @click="toggleFullscreen">
            <el-icon><FullScreen /></el-icon>
          </button>
        </div>
      </div>

      <div class="danmaku-row">
        <div class="danmaku-colors" title="弹幕颜色">
          <button
            v-for="item in DANMAKU_COLORS"
            :key="item.value"
            type="button"
            class="color-dot"
            :class="{ active: selectedDanmakuColor === item.value }"
            :style="{ background: item.value }"
            :title="item.label"
            @click="selectedDanmakuColor = item.value"
          />
        </div>
        <input
          v-model="danmakuInput"
          class="danmaku-input"
          maxlength="100"
          placeholder="发个弹幕，和同学一起学习吧"
          @keyup.enter="submitDanmaku"
        >
        <button type="button" class="danmaku-send" @click="submitDanmaku">发送</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.course-player {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #0f1117;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(15, 17, 23, 0.18);
}

.course-player.is-fullscreen {
  border-radius: 0;
}

.course-player__video {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}

.course-player__center-action,
.course-player__controls {
  position: absolute;
  left: 0;
  right: 0;
  transition: opacity 0.25s ease;
}

.course-player__center-action {
  inset: 0;
  display: grid;
  place-items: center;
  pointer-events: none;
  z-index: 3;
}

.course-player__center-action.hidden,
.course-player__controls.hidden {
  opacity: 0;
  pointer-events: none;
}

.center-btn {
  pointer-events: auto;
  width: 72px;
  height: 72px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  cursor: pointer;
  display: grid;
  place-items: center;
}

.pause-glyph {
  display: inline-block;
  width: 16px;
  height: 18px;
  border-left: 5px solid #fff;
  border-right: 5px solid #fff;
}

.pause-glyph.small {
  width: 10px;
  height: 12px;
  border-left-width: 3px;
  border-right-width: 3px;
}

.course-player__controls {
  bottom: 0;
  z-index: 4;
  padding: 10px 14px 12px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.78) 100%);
}

.progress-row {
  margin-bottom: 8px;
}

.progress-slider,
.volume-slider {
  width: 100%;
  accent-color: #00a1d6;
}

.volume-slider {
  width: 72px;
}

.toolbar-row,
.toolbar-left,
.toolbar-right,
.danmaku-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-row {
  justify-content: space-between;
}

.toolbar-right {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.icon-btn,
.text-btn,
.danmaku-send {
  border: none;
  background: transparent;
  color: #fff;
  cursor: pointer;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
}

.icon-btn:hover,
.text-btn:hover,
.danmaku-send:hover {
  background: rgba(255, 255, 255, 0.12);
}

.text-btn {
  min-width: 42px;
  height: 32px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

.text-btn.active {
  color: #00a1d6;
}

.time-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.88);
  min-width: 92px;
}

.danmaku-row {
  margin-top: 10px;
}

.danmaku-colors {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.color-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.35);
  cursor: pointer;
  padding: 0;
}

.color-dot.active {
  border-color: #fff;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.35);
}

.danmaku-input {
  flex: 1;
  height: 34px;
  border: none;
  border-radius: 17px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  outline: none;
}

.danmaku-input::placeholder {
  color: rgba(255, 255, 255, 0.55);
}

.danmaku-send {
  min-width: 58px;
  height: 34px;
  border-radius: 17px;
  background: #00a1d6;
  font-size: 13px;
  font-weight: 600;
}

:deep(.el-dropdown-menu__item.is-active) {
  color: #00a1d6;
  font-weight: 600;
}
</style>
