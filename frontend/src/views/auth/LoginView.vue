<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await authStore.login(form)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-title">登录学分银行</h2>
      <p class="auth-sub">终身学习学分银行平台</p>

      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>

      <div class="auth-footer">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </div>

      <div class="auth-tip">
        <p>测试账号（需数据库已初始化）：</p>
        <p>学员 student1 / admin123</p>
        <p>企业 enterprise1 / admin123</p>
        <p>管理员 admin / admin123</p>
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

.auth-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
  text-align: center;
}

.auth-sub {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
  margin-bottom: 28px;
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

.auth-tip {
  margin-top: 24px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.8;
}

.auth-tip p {
  margin: 0;
}
</style>
