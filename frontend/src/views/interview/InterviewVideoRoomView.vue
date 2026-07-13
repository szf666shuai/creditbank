<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
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

const localVideoRef = ref<HTMLDivElement | null>(null)
const remoteVideoRef = ref<HTMLDivElement | null>(null)

let trtc: ReturnType<typeof TRTC.create> | null = null

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
    trtc = TRTC.create()
    await trtc.enterRoom({
      strRoomId: credentials.value.roomId,
      sdkAppId: Number(credentials.value.sdkAppId),
      userId: credentials.value.userId,
      userSig: credentials.value.userSig,
      scene: TRTC.TYPE.SCENE_RTC,
    })

    trtc.on(TRTC.EVENT.REMOTE_VIDEO_AVAILABLE, async ({ userId, streamType }) => {
      if (!trtc || !remoteVideoRef.value) return
      await trtc.startRemoteVideo({ userId, streamType, view: remoteVideoRef.value })
    })

    if (localVideoRef.value) {
      await trtc.startLocalVideo({ view: localVideoRef.value })
    }
    await trtc.startLocalAudio()
    joined.value = true
    ElMessage.success('已进入面试房间')
  } catch (e) {
    error.value = getErrorMessage(e, '进入房间失败，请检查摄像头/麦克风权限')
    await leaveRoom(false)
  } finally {
    joining.value = false
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

    <div v-if="loading" class="video-room__loading">正在准备视频面试…</div>

    <div v-else class="video-room__stage">
      <div class="video-room__remote-wrap">
        <div ref="remoteVideoRef" class="video-room__remote" />
        <span v-if="joined" class="video-room__label">对方画面</span>
        <span v-else class="video-room__placeholder">等待进入或连接中…</span>
      </div>
      <div class="video-room__local-wrap">
        <div ref="localVideoRef" class="video-room__local" />
        <span class="video-room__label">我的画面</span>
      </div>
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
  display: grid;
  grid-template-columns: 1fr 240px;
  gap: 16px;
}

.video-room__remote-wrap,
.video-room__local-wrap {
  position: relative;
  background: #111;
  border-radius: 12px;
  overflow: hidden;
  min-height: 360px;
}

.video-room__local-wrap {
  min-height: 200px;
}

.video-room__remote,
.video-room__local {
  width: 100%;
  height: 100%;
  min-height: inherit;
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
}

.video-room__placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
}

.video-room__retry {
  margin-top: 16px;
  text-align: center;
}

@media (max-width: 768px) {
  .video-room__stage {
    grid-template-columns: 1fr;
  }

  .video-room__local-wrap {
    min-height: 160px;
  }
}
</style>
