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
    class="compose-message-dialog"
    width="560px"
    destroy-on-close
    align-center
    @close="close"
  >
    <template #header>
      <div class="compose-head">
        <p class="compose-kicker">私信</p>
        <h2 class="compose-title">写私信</h2>
      </div>
    </template>
    <el-form label-position="top" class="compose-form">
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
          popper-class="compose-select-dropdown"
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
      <el-button class="compose-btn compose-btn--ghost" @click="close">取消</el-button>
      <el-button class="compose-btn compose-btn--primary" :loading="saving" @click="handleSubmit">
        发送
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.compose-head {
  min-width: 0;
  padding-right: 8px;
}

.compose-kicker {
  margin: 0 0 4px;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--nb-green-deep, #16a34a);
  font-weight: 800;
}

.compose-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.35;
  color: var(--nb-ink, #1a202c);
  font-weight: 900;
  font-family: var(--font-heading);
}

.compose-btn {
  --el-button-bg-color: #fff;
  --el-button-border-color: var(--nb-ink, #1a202c);
  --el-button-text-color: var(--nb-ink, #1a202c);
  --el-button-hover-bg-color: var(--nb-yellow, #fef08a);
  --el-button-hover-border-color: var(--nb-ink, #1a202c);
  --el-button-hover-text-color: var(--nb-ink, #1a202c);
}

.compose-btn--primary {
  --el-button-bg-color: var(--nb-green, #22c55e);
  --el-button-border-color: var(--nb-ink, #1a202c);
  --el-button-text-color: #fff;
  --el-button-hover-bg-color: var(--nb-green-deep, #16a34a);
  --el-button-hover-border-color: var(--nb-ink, #1a202c);
}

.compose-btn--ghost {
  --el-button-bg-color: #fff;
  --el-button-border-color: var(--nb-ink, #1a202c);
  --el-button-text-color: var(--nb-ink, #1a202c);
}
</style>

<style>
.compose-message-dialog.el-dialog {
  background: #fff;
  border: 2.5px solid var(--nb-ink, #1a202c);
  border-radius: 18px;
  box-shadow: var(--nb-shadow-lg, 6px 6px 0 0 #1a202c);
  backdrop-filter: none;
}

.compose-message-dialog .el-dialog__header {
  margin-right: 0;
  padding: 16px 20px 12px;
  border-bottom: 2px solid color-mix(in srgb, var(--nb-ink, #1a202c) 14%, transparent);
}

.compose-message-dialog .el-dialog__headerbtn .el-dialog__close {
  color: var(--color-muted-foreground, #64748b);
}

.compose-message-dialog .el-dialog__body {
  padding: 16px 20px 8px;
  color: var(--nb-ink, #1a202c);
}

.compose-message-dialog .el-dialog__footer {
  padding: 12px 20px 18px;
  border-top: 2px solid color-mix(in srgb, var(--nb-ink, #1a202c) 14%, transparent);
}

.compose-message-dialog .el-form-item__label {
  color: var(--nb-ink, #1a202c) !important;
  font-weight: 700;
}

.compose-message-dialog .el-input__wrapper,
.compose-message-dialog .el-textarea__inner,
.compose-message-dialog .el-select__wrapper {
  background: var(--nb-cream, #fff9f0) !important;
  box-shadow: none !important;
  border: 2px solid var(--nb-ink, #1a202c) !important;
  border-radius: 12px !important;
}

.compose-message-dialog .el-input__inner,
.compose-message-dialog .el-textarea__inner,
.compose-message-dialog .el-select__placeholder,
.compose-message-dialog .el-select__selected-item {
  color: var(--nb-ink, #1a202c) !important;
}

.compose-message-dialog .el-input__inner::placeholder,
.compose-message-dialog .el-textarea__inner::placeholder {
  color: var(--color-muted-foreground, #64748b) !important;
}

.compose-message-dialog .el-textarea__inner {
  box-shadow: none !important;
  border: 2px solid var(--nb-ink, #1a202c);
}

.compose-select-dropdown.el-select__popper,
.compose-select-dropdown {
  background: #fff !important;
  border: 2.5px solid var(--nb-ink, #1a202c) !important;
  box-shadow: var(--nb-shadow, 4px 4px 0 0 #1a202c) !important;
}

.compose-select-dropdown .el-select-dropdown__item {
  color: var(--nb-ink, #1a202c);
}

.compose-select-dropdown .el-select-dropdown__item.is-hovering,
.compose-select-dropdown .el-select-dropdown__item:hover {
  background: #bbf7d0;
  color: var(--nb-ink, #1a202c);
}
</style>
