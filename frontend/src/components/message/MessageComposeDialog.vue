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
  color: rgba(125, 211, 252, 0.9);
  font-weight: 700;
}

.compose-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.35;
  color: #e0f2fe;
  font-weight: 700;
}

.compose-btn {
  --el-button-bg-color: rgba(56, 189, 248, 0.16);
  --el-button-border-color: rgba(56, 189, 248, 0.35);
  --el-button-text-color: #e0f2fe;
  --el-button-hover-bg-color: rgba(56, 189, 248, 0.28);
  --el-button-hover-border-color: rgba(56, 189, 248, 0.5);
  --el-button-hover-text-color: #f0f9ff;
}

.compose-btn--primary {
  --el-button-bg-color: rgba(37, 99, 235, 0.92);
  --el-button-border-color: rgba(37, 99, 235, 1);
  --el-button-text-color: #eff6ff;
  --el-button-hover-bg-color: rgba(29, 78, 216, 0.95);
  --el-button-hover-border-color: rgba(29, 78, 216, 1);
}

.compose-btn--ghost {
  --el-button-bg-color: transparent;
  --el-button-border-color: rgba(186, 230, 253, 0.28);
  --el-button-text-color: rgba(226, 232, 240, 0.88);
}
</style>

<style>
.compose-message-dialog.el-dialog {
  background:
    radial-gradient(ellipse at 12% 0%, rgba(56, 189, 248, 0.18), transparent 42%),
    rgba(8, 18, 36, 0.96);
  border: 1px solid rgba(125, 211, 252, 0.28);
  border-radius: 16px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(16px);
}

.compose-message-dialog .el-dialog__header {
  margin-right: 0;
  padding: 16px 20px 12px;
  border-bottom: 1px solid rgba(147, 197, 253, 0.14);
}

.compose-message-dialog .el-dialog__headerbtn .el-dialog__close {
  color: rgba(186, 230, 253, 0.78);
}

.compose-message-dialog .el-dialog__body {
  padding: 16px 20px 8px;
  color: #e2e8f0;
}

.compose-message-dialog .el-dialog__footer {
  padding: 12px 20px 18px;
  border-top: 1px solid rgba(147, 197, 253, 0.14);
}

.compose-message-dialog .el-form-item__label {
  color: rgba(186, 230, 253, 0.88) !important;
}

.compose-message-dialog .el-input__wrapper,
.compose-message-dialog .el-textarea__inner,
.compose-message-dialog .el-select__wrapper {
  background: rgba(8, 20, 40, 0.55) !important;
  box-shadow: 0 0 0 1px rgba(125, 211, 252, 0.28) inset !important;
  border-radius: 10px;
}

.compose-message-dialog .el-input__inner,
.compose-message-dialog .el-textarea__inner,
.compose-message-dialog .el-select__placeholder,
.compose-message-dialog .el-select__selected-item {
  color: #e0f2fe !important;
}

.compose-message-dialog .el-input__inner::placeholder,
.compose-message-dialog .el-textarea__inner::placeholder {
  color: rgba(147, 197, 253, 0.45) !important;
}

.compose-message-dialog .el-textarea__inner {
  box-shadow: none !important;
  border: 1px solid rgba(125, 211, 252, 0.28);
}

.compose-select-dropdown.el-select__popper,
.compose-select-dropdown {
  background: rgba(8, 18, 36, 0.96) !important;
  border: 1px solid rgba(125, 211, 252, 0.28) !important;
}

.compose-select-dropdown .el-select-dropdown__item {
  color: rgba(226, 232, 240, 0.9);
}

.compose-select-dropdown .el-select-dropdown__item.is-hovering,
.compose-select-dropdown .el-select-dropdown__item:hover {
  background: rgba(56, 189, 248, 0.16);
  color: #e0f2fe;
}
</style>
