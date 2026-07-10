<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  searchRecipientsApi,
  sendMessageApi,
  type MessageRecipient,
} from '@/api/message'
import { useMessageStore } from '@/stores/message'

const props = defineProps<{
  modelValue: boolean
  receiverId?: number
  receiverName?: string
  defaultTitle?: string
  defaultContent?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  sent: []
}>()

const messageStore = useMessageStore()
const saving = ref(false)
const searching = ref(false)
const recipients = ref<MessageRecipient[]>([])

const form = reactive({
  receiverId: undefined as number | undefined,
  title: '',
  content: '',
})

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) return
    form.receiverId = props.receiverId
    form.title = props.defaultTitle || ''
    form.content = props.defaultContent || ''
    if (props.receiverId && props.receiverName) {
      recipients.value = [
        {
          id: props.receiverId,
          username: '',
          displayName: props.receiverName,
          role: 0,
          roleName: '',
        },
      ]
    } else {
      recipients.value = []
    }
  },
)

async function searchRecipients(keyword: string) {
  if (!keyword.trim()) {
    recipients.value = props.receiverId && props.receiverName
      ? [{
          id: props.receiverId,
          username: '',
          displayName: props.receiverName,
          role: 0,
          roleName: '',
        }]
      : []
    return
  }
  searching.value = true
  try {
    const res = await searchRecipientsApi(keyword.trim())
    if (res.code === 200) {
      recipients.value = res.data
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '搜索用户失败')
  } finally {
    searching.value = false
  }
}

function close() {
  emit('update:modelValue', false)
}

async function handleSubmit() {
  if (!form.receiverId) {
    ElMessage.warning('请选择收件人')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请填写消息内容')
    return
  }

  saving.value = true
  try {
    const res = await sendMessageApi({
      receiverId: form.receiverId,
      title: form.title.trim() || '无标题',
      content: form.content.trim(),
    })
    if (res.code === 200) {
      ElMessage.success('发送成功')
      await messageStore.refreshUnreadCount()
      emit('sent')
      close()
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '发送失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="写私信"
    width="560px"
    destroy-on-close
    @close="close"
  >
    <el-form label-width="72px">
      <el-form-item label="收件人" required>
        <el-select
          v-model="form.receiverId"
          filterable
          remote
          reserve-keyword
          placeholder="输入用户名或姓名搜索"
          :remote-method="searchRecipients"
          :loading="searching"
          style="width: 100%"
        >
          <el-option
            v-for="user in recipients"
            :key="user.id"
            :label="`${user.displayName}（${user.username || user.roleName}）`"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="选填，默认「无标题」" maxlength="200" />
      </el-form-item>
      <el-form-item label="内容" required>
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          placeholder="请输入私信内容"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">发送</el-button>
    </template>
  </el-dialog>
</template>
