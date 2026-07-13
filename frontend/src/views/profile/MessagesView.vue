<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import PageShell from '@/components/common/PageShell.vue'
import MessageComposeDialog from '@/components/message/MessageComposeDialog.vue'
import {
  listInboxApi,
  listOutboxApi,
  type UserMessage,
} from '@/api/message'
import { useMessageStore } from '@/stores/message'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const messageStore = useMessageStore()

const loading = ref(false)
const loadError = ref<string | null>(null)
const activeTab = ref<'inbox' | 'outbox'>('inbox')
const messages = ref<UserMessage[]>([])
const composeVisible = ref(false)
const composeReceiverId = ref<number>()
const composeReceiverName = ref<string>()
const composeTitle = ref<string>()

function peerLabel(row: UserMessage) {
  if (activeTab.value === 'inbox') {
    return row.fromUserName || `用户#${row.fromUserId}`
  }
  return row.toUserName || `用户#${row.toUserId}`
}

async function fetchMessages() {
  loading.value = true
  loadError.value = null
  try {
    messages.value = unwrapApi(
      activeTab.value === 'inbox' ? await listInboxApi() : await listOutboxApi(),
    )
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function openCompose() {
  composeReceiverId.value = undefined
  composeReceiverName.value = undefined
  composeTitle.value = undefined
  composeVisible.value = true
}

function openDetail(row: UserMessage) {
  router.push(`/profile/messages/${row.id}`)
}

function applyRouteCompose() {
  const replyTo = route.query.replyTo
  if (replyTo) {
    composeReceiverId.value = Number(replyTo)
    composeReceiverName.value = (route.query.replyName as string) || undefined
    composeTitle.value = (route.query.title as string) || undefined
    composeVisible.value = true
    router.replace({ path: '/profile/messages' })
  } else if (route.query.compose === '1') {
    composeVisible.value = true
    router.replace({ path: '/profile/messages' })
  }
}

watch(activeTab, fetchMessages)

onMounted(async () => {
  applyRouteCompose()
  await fetchMessages()
  await messageStore.refreshUnreadCount()
})
</script>

<template>
  <PageShell
    title="消息中心"
    description="查看与发送私信；面试/活动邀请请在对应专属页面处理"
    :loading="loading"
    :error="loadError"
    @retry="fetchMessages"
  >
    <template #actions>
      <el-button type="primary" @click="openCompose">写私信</el-button>
    </template>

    <el-tabs v-model="activeTab" class="page-tabs">
      <el-tab-pane label="收件箱" name="inbox" />
      <el-tab-pane label="发件箱" name="outbox" />
    </el-tabs>

    <el-table :data="messages" border stripe class="message-table" @row-click="openDetail">
      <el-table-column v-if="activeTab === 'inbox'" label="阅读" width="72" align="center">
        <template #default="{ row }">
          <el-tag :type="row.readStatus === 1 ? 'info' : 'danger'" size="small">
            {{ row.readStatus === 1 ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="activeTab === 'inbox' ? '发件人' : '收件人'" width="120">
        <template #default="{ row }">
          <span :class="{ unread: activeTab === 'inbox' && row.readStatus === 0 }">
            {{ peerLabel(row) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160">
        <template #default="{ row }">
          <span :class="{ unread: activeTab === 'inbox' && row.readStatus === 0 }">
            {{ row.title || '无标题' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="内容摘要" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ row.content }}</template>
      </el-table-column>
      <el-table-column label="时间" width="150">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="openDetail(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && messages.length === 0"
      class="page-empty"
      :image-size="80"
      :description="activeTab === 'inbox' ? '收件箱暂无私信' : '发件箱暂无私信'"
    />
  </PageShell>

  <MessageComposeDialog
    v-model="composeVisible"
    :receiver-id="composeReceiverId"
    :receiver-name="composeReceiverName"
    :default-title="composeTitle"
    @sent="fetchMessages"
  />
</template>

<style scoped>
.message-table :deep(.el-table__row) {
  cursor: pointer;
}

.unread {
  font-weight: 600;
  color: #1a202c;
}
</style>
