<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { ROLE_ENTERPRISE, ROLE_STUDENT } from '@/types/auth'
import { BRAND_FULL } from '@/config/brand'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  roleType: ROLE_STUDENT as number,
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  orgName: '',
  orgCode: '',
  orgIntro: '',
  orgContact: '',
  orgPhone: '',
})

async function handleRegister() {
  if (!form.username || !form.password || !form.realName) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  if (form.roleType === ROLE_ENTERPRISE && !form.orgName) {
    ElMessage.warning('请填写企业名称')
    return
  }

  loading.value = true
  try {
    await authStore.register({
      username: form.username,
      password: form.password,
      realName: form.realName,
      roleType: form.roleType,
      phone: form.phone || undefined,
      email: form.email || undefined,
      orgName: form.roleType === ROLE_ENTERPRISE ? form.orgName : undefined,
      orgCode: form.orgCode || undefined,
      orgIntro: form.orgIntro || undefined,
      orgContact: form.orgContact || undefined,
      orgPhone: form.orgPhone || undefined,
    })
    ElMessage.success('注册成功')
    router.push('/profile')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card wide">
      <h2 class="auth-title">注册账号</h2>
      <p class="auth-sub">{{ BRAND_FULL }}</p>

      <div class="role-select">
        <div
          class="role-item"
          :class="{ active: form.roleType === ROLE_STUDENT }"
          @click="form.roleType = ROLE_STUDENT"
        >
          <span class="role-icon">🎓</span>
          <span class="role-name">学员注册</span>
          <span class="role-desc">学习课程、获取秩点</span>
        </div>
        <div
          class="role-item"
          :class="{ active: form.roleType === ROLE_ENTERPRISE }"
          @click="form.roleType = ROLE_ENTERPRISE"
        >
          <span class="role-icon">🏢</span>
          <span class="role-name">企业注册</span>
          <span class="role-desc">发布招聘、活动与课程</span>
        </div>
      </div>

      <el-form label-position="top" @submit.prevent="handleRegister">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" required>
              <el-input v-model="form.username" placeholder="3-50个字符" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" required>
              <el-input v-model="form.realName" placeholder="您的姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" required>
              <el-input v-model="form.password" type="password" show-password placeholder="至少6位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" required>
              <el-input v-model="form.confirmPassword" type="password" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="form.roleType === ROLE_ENTERPRISE">
          <el-divider>企业信息</el-divider>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="企业名称" required>
                <el-input v-model="form.orgName" placeholder="企业全称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="企业编码">
                <el-input v-model="form.orgCode" placeholder="留空自动生成" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="企业简介">
                <el-input v-model="form.orgIntro" type="textarea" :rows="2" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="企业联系人">
                <el-input v-model="form.orgContact" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="企业电话">
                <el-input v-model="form.orgPhone" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">
          注册
        </el-button>
      </el-form>

      <div class="auth-footer">
        已有账号？
        <router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: calc(100vh - var(--header-height));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 36px 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.auth-card.wide {
  max-width: 640px;
}

.auth-title {
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 8px;
}

.auth-sub {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
  margin-bottom: 24px;
}

.role-select {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 24px;
}

.role-item {
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.role-item.active {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

.role-icon {
  font-size: 28px;
}

.role-name {
  font-weight: 600;
  font-size: 15px;
}

.role-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.auth-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.auth-footer a {
  color: var(--color-primary);
  text-decoration: none;
  margin-left: 4px;
}
</style>
