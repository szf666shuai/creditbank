<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import { sendSystemNotificationApi } from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'

const sending = ref(false)

const form = reactive({
  title: '',
  content: '',
  scope: 'all' as 'all' | 'role' | 'user',
  targetRole: 0,
  targetUserId: undefined as number | undefined,
})

async function handleSend() {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('请填写通知标题和内容')
    return
  }
  if (form.scope === 'user' && !form.targetUserId) {
    ElMessage.warning('请填写目标用户 ID')
    return
  }
  sending.value = true
  try {
    const result = unwrapApi(
      await sendSystemNotificationApi({
        title: form.title.trim(),
        content: form.content.trim(),
        scope: form.scope,
        targetRole: form.scope === 'role' ? form.targetRole : undefined,
        targetUserId: form.scope === 'user' ? form.targetUserId : undefined,
      }),
    )
    ElMessage.success(`通知已发送，共 ${result.sentCount} 人`)
    form.title = ''
    form.content = ''
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '发送失败'))
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <PageShell title="系统通知" description="向全体或指定范围的用户发送平台通知（用户在消息中心查看）">
    <el-form label-width="100px" style="max-width: 640px">
      <el-form-item label="发送范围" required>
        <el-radio-group v-model="form.scope">
          <el-radio value="all">全体用户</el-radio>
          <el-radio value="role">按角色</el-radio>
          <el-radio value="user">指定用户</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.scope === 'role'" label="目标角色">
        <el-select v-model="form.targetRole" style="width: 200px">
          <el-option label="学员" :value="0" />
          <el-option label="企业用户" :value="1" />
          <el-option label="管理员" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.scope === 'user'" label="用户 ID">
        <el-input-number v-model="form.targetUserId" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="通知标题" required>
        <el-input v-model="form.title" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="通知内容" required>
        <el-input v-model="form.content" type="textarea" :rows="6" maxlength="1000" show-word-limit />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="sending" @click="handleSend">发送通知</el-button>
      </el-form-item>
    </el-form>
  </PageShell>
</template>
