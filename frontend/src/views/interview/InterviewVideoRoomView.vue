<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TRTC from 'trtc-sdk-v5'
import { ElMessage } from 'element-plus'
import {
  getEnterpriseInterviewRtcCredentialsApi,
  getProfileInterviewRtcCredentialsApi,
  type InterviewRtcCredentials,
} from '@/api/interview'
import { getErrorMessage, unwrapApi } from '@/utils/api'

const route = useRoute()
const router = useRouter()

const invitationId = Number(route.params.id)
const scope = (route.meta.scope as 'profile' | 'enterprise') ?? 'profile'

const loading = ref(true)
const joining = ref(false)
const joined = ref(false)
const error = ref<string | null>(null)
const credentials = ref<InterviewRtcCredentials | null>(null)
const cameraAvailable = ref(true)
const microphoneAvailable = ref(true)
const remoteVideoActive = ref(false)
const insecureHttpAccess = ref(false)
const retryingCamera = ref(false)

const localVideoRef = ref<HTMLDivElement | null>(null)
const remoteVideoRef = ref<HTMLDivElement | null>(null)

let trtc: ReturnType<typeof TRTC.create> | null = null

function formatMediaError(e: unknown, fallback: string): string {
  const raw = getErrorMessage(e, fallback)
  if (/NotReadableError|maybe in use|in use by another/i.test(raw)) {
    return '摄像头正被其他标签页或应用占用。可关闭占用方后重试，或先以仅音频模式继续面试。'
  }
  if (/NotAllowedError|Permission denied|permission/i.test(raw)) {
    return '浏览器未授权摄像头/麦克风，请在地址栏权限设置中允许访问后重试。'
  }
  if (/NotFoundError|no device/i.test(raw)) {
    return '未检测到摄像头或麦克风设备，请检查系统设置。'
  }
  return raw
}

async function startLocalMedia() {
  if (!trtc) return

  cameraAvailable.value = true
  microphoneAvailable.value = true
  insecureHttpAccess.value =
    !window.isSecureContext && !window.location.hostname.endsWith('localhost')

  if (!localVideoRef.value) {
    cameraAvailable.value = false
    ElMessage.warning('本地画面容器未就绪，请点击「重试开启摄像头」')
    return
  }

  try {
    await trtc.startLocalVideo({ view: localVideoRef.value })
  } catch (e) {
    cameraAvailable.value = false
    ElMessage.warning(formatMediaError(e, '无法开启摄像头，已以仅音频模式加入'))
  }

  try {
    await trtc.startLocalAudio()
  } catch (e) {
    microphoneAvailable.value = false
    ElMessage.warning(formatMediaError(e, '无法开启麦克风'))
  }
}

function bindRoomEvents(client: ReturnType<typeof TRTC.create>) {
  client.on(TRTC.EVENT.REMOTE_VIDEO_AVAILABLE, async ({ userId, streamType }) => {
    if (!trtc || !remoteVideoRef.value) return
    try {
      await trtc.startRemoteVideo({ userId, streamType, view: remoteVideoRef.value })
      remoteVideoActive.value = true
    } catch (e) {
      ElMessage.warning(getErrorMessage(e, '无法播放对方画面'))
    }
  })

  client.on(TRTC.EVENT.REMOTE_VIDEO_UNAVAILABLE, () => {
    remoteVideoActive.value = false
  })

  client.on(TRTC.EVENT.AUTOPLAY_FAILED, (event: { userId?: string; resume?: () => void }) => {
    ElMessage.info('浏览器限制自动播放，正在尝试恢复对方画面…')
    event.resume?.()
  })
}

async function fetchCredentials() {
  loading.value = true
  error.value = null
  try {
    const res =
      scope === 'enterprise'
        ? await getEnterpriseInterviewRtcCredentialsApi(invitationId)
        : await getProfileInterviewRtcCredentialsApi(invitationId)
    credentials.value = unwrapApi(res)
  } catch (e) {
    error.value = getErrorMessage(e, '获取视频凭证失败')
  } finally {
    loading.value = false
  }
}

async function joinRoom() {
  if (!credentials.value || joining.value || joined.value) return
  joining.value = true
  error.value = null
  try {
    await nextTick()
    trtc = TRTC.create()
    bindRoomEvents(trtc)
    await trtc.enterRoom({
      strRoomId: credentials.value.roomId,
      sdkAppId: Number(credentials.value.sdkAppId),
      userId: credentials.value.userId,
      userSig: credentials.value.userSig,
      scene: TRTC.TYPE.SCENE_RTC,
    })

    await startLocalMedia()
    joined.value = true
    ElMessage.success(
      cameraAvailable.value ? '已进入面试房间' : '已进入面试房间（仅音频，摄像头未开启）',
    )
  } catch (e) {
    error.value = formatMediaError(e, '进入房间失败，请检查摄像头/麦克风权限')
    await leaveRoom(false)
  } finally {
    joining.value = false
  }
}

async function retryLocalVideo() {
  if (!trtc || !localVideoRef.value || cameraAvailable.value) return
  retryingCamera.value = true
  try {
    await trtc.startLocalVideo({ view: localVideoRef.value })
    cameraAvailable.value = true
    ElMessage.success('摄像头已开启')
  } catch (e) {
    ElMessage.warning(formatMediaError(e, '仍无法开启摄像头'))
  } finally {
    retryingCamera.value = false
  }
}

async function leaveRoom(showMessage = true) {
  if (trtc) {
    try {
      await trtc.exitRoom()
      trtc.destroy()
    } catch {
      // ignore cleanup errors
    }
    trtc = null
  }
  joined.value = false
  cameraAvailable.value = true
  microphoneAvailable.value = true
  remoteVideoActive.value = false
  if (showMessage) {
    ElMessage.info('已离开面试房间')
  }
}

function goBack() {
  if (scope === 'enterprise') {
    router.push('/profile/enterprise/interviews')
  } else {
    router.push('/profile/interviews')
  }
}

onMounted(async () => {
  if (!Number.isFinite(invitationId) || invitationId <= 0) {
    error.value = '无效的面试邀请'
    loading.value = false
    return
  }
  await fetchCredentials()
  if (credentials.value) {
    await nextTick()
    await joinRoom()
  }
})

onBeforeUnmount(() => {
  void leaveRoom(false)
})
</script>

<template>
  <div class="video-room">
    <header class="video-room__header">
      <div>
        <h1>视频面试</h1>
        <p v-if="credentials" class="video-room__meta">
          房间 {{ credentials.roomId }} · 邀请 #{{ credentials.invitationId }}
        </p>
      </div>
      <div class="video-room__actions">
        <el-button v-if="joined" type="danger" @click="leaveRoom()">离开房间</el-button>
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </header>

    <el-alert v-if="error" type="error" :title="error" show-icon :closable="false" class="video-room__alert" />
    <el-alert
      v-else-if="joined && insecureHttpAccess && !cameraAvailable"
      type="warning"
      title="HTTP 访问无法开启本机摄像头"
      description="通过 http://IP 访问时，浏览器会禁止摄像头/麦克风。请改用 https://你的IP:5173 重新打开（首次需信任自签证书）。你仍可能看到对方画面，但对方看不到你。"
      show-icon
      :closable="false"
      class="video-room__alert"
    />
    <el-alert
      v-else-if="joined && !cameraAvailable"
      type="warning"
      title="本机摄像头未开启"
      description="常见原因：被其他应用占用或浏览器未授权。可先以音频参与；若本机已开启但对方画面仍黑，说明对方未成功推流。"
      show-icon
      :closable="false"
      class="video-room__alert"
    />

    <div v-if="loading" class="video-room__loading">正在准备视频面试…</div>

    <div v-else class="video-room__stage">
      <div class="video-room__local-wrap">
        <div ref="localVideoRef" class="video-room__local" />
        <span v-if="joined && !cameraAvailable" class="video-room__placeholder video-room__placeholder--local">
          摄像头未开启
        </span>
        <span class="video-room__label">我的画面</span>
      </div>
      <div class="video-room__remote-wrap">
        <div ref="remoteVideoRef" class="video-room__remote" />
        <span v-if="joined" class="video-room__label">对方画面</span>
        <span
          v-if="joined && !remoteVideoActive"
          class="video-room__placeholder"
        >
          对方未开启摄像头
        </span>
        <span v-else-if="!joined" class="video-room__placeholder">等待对方…</span>
      </div>
    </div>

    <div v-if="joined && !cameraAvailable" class="video-room__retry">
      <el-button type="primary" :loading="retryingCamera" @click="retryLocalVideo">重试开启摄像头</el-button>
    </div>

    <div v-if="!loading && !joined && !error" class="video-room__retry">
      <el-button type="primary" :loading="joining" @click="joinRoom">重新进入</el-button>
    </div>
  </div>
</template>

<style scoped>
.video-room {
  min-height: calc(100vh - 120px);
  padding: 24px 16px 48px;
  max-width: var(--content-max-width, 1100px);
  margin: 0 auto;
}

.video-room__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.video-room__header h1 {
  margin: 0 0 4px;
  font-size: 1.35rem;
}

.video-room__meta {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
}

.video-room__actions {
  display: flex;
  gap: 8px;
}

.video-room__alert {
  margin-bottom: 16px;
}

.video-room__loading {
  padding: 48px;
  text-align: center;
  color: var(--el-text-color-secondary);
}

.video-room__stage {
  position: relative;
  width: 100%;
  min-height: min(72vh, 640px);
  border-radius: 14px;
  overflow: hidden;
  background: #0a0f18;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.28);
}

.video-room__local-wrap {
  position: relative;
  width: 100%;
  min-height: min(72vh, 640px);
  background: #111;
}

.video-room__remote-wrap {
  position: absolute;
  right: 16px;
  bottom: 16px;
  width: min(32vw, 300px);
  min-width: 200px;
  aspect-ratio: 16 / 10;
  z-index: 2;
  background: #111;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(125, 211, 252, 0.35);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.45);
}

.video-room__remote,
.video-room__local {
  width: 100%;
  height: 100%;
  min-height: inherit;
}

.video-room__local {
  min-height: min(72vh, 640px);
}

.video-room__remote {
  min-height: 100%;
}

.video-room__label {
  position: absolute;
  left: 12px;
  bottom: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  z-index: 1;
}

.video-room__remote-wrap .video-room__label {
  left: 8px;
  bottom: 8px;
  font-size: 11px;
}

.video-room__placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
  text-align: center;
  padding: 12px;
}

.video-room__placeholder--local {
  z-index: 1;
  background: rgba(0, 0, 0, 0.45);
}

.video-room__retry {
  margin-top: 16px;
  text-align: center;
}

@media (max-width: 768px) {
  .video-room__stage,
  .video-room__local-wrap,
  .video-room__local {
    min-height: 52vh;
  }

  .video-room__remote-wrap {
    width: 42vw;
    min-width: 140px;
    right: 10px;
    bottom: 10px;
  }
}
</style>
