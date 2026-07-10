<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import PageShell from '@/components/common/PageShell.vue'
import MessageComposeDialog from '@/components/message/MessageComposeDialog.vue'
import { getMessageApi, type UserMessage } from '@/api/message'
import { useMessageStore } from '@/stores/message'
import { useAuthStore } from '@/stores/auth'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const messageStore = useMessageStore()
const authStore = useAuthStore()

const loading = ref(false)
const loadError = ref<string | null>(null)
const message = ref<UserMessage | null>(null)
const composeVisible = ref(false)

const messageId = computed(() => Number(route.params.id))
const currentUserId = computed(() => authStore.userInfo?.id)
const isInbox = computed(
  () => message.value != null && message.value.toUserId === currentUserId.value,
)
const replyTarget = computed(() => {
  if (!message.value || isInbox.value) {
    return {
      id: message.value?.fromUserId,
      name: message.value?.fromUserName,
    }
  }
  return {
    id: message.value.toUserId,
    name: message.value.toUserName,
  }
})

async function fetchMessage() {
  loading.value = true
  loadError.value = null
  try {
    message.value = unwrapApi(await getMessageApi(messageId.value))
    await messageStore.refreshUnreadCount()
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/profile/messages')
}

function openReply() {
  composeVisible.value = true
}

function replyTitle() {
  const title = message.value?.title || '无标题'
  return title.startsWith('Re:') ? title : `Re: ${title}`
}

onMounted(fetchMessage)
</script>

<template>
  <PageShell :loading="loading" :error="loadError" @retry="fetchMessage">
    <template #header>
      <div class="detail-header">
        <el-button link type="primary" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回消息列表
        </el-button>
        <el-button v-if="message" type="primary" @click="openReply">回复</el-button>
      </div>
    </template>

    <template v-if="message">
      <h1 class="detail-title">{{ message.title || '无标题' }}</h1>

      <div class="detail-meta">
        <span>发件人：{{ message.fromUserName || `用户#${message.fromUserId}` }}</span>
        <span>收件人：{{ message.toUserName || `用户#${message.toUserId}` }}</span>
        <span>时间：{{ formatTime(message.createTime) }}</span>
        <el-tag v-if="isInbox" :type="message.readStatus === 1 ? 'info' : 'danger'" size="small">
          {{ message.readStatus === 1 ? '已读' : '未读' }}
        </el-tag>
      </div>

      <div class="detail-content">{{ message.content }}</div>
    </template>
  </PageShell>

  <MessageComposeDialog
    v-if="message"
    v-model="composeVisible"
    :receiver-id="replyTarget.id"
    :receiver-name="replyTarget.name"
    :default-title="replyTitle()"
    @sent="goBack"
  />
</template>

<style scoped>
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
}

.detail-title {
  font-size: 22px;
  margin-bottom: 16px;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
  color: var(--color-text);
}
</style>
