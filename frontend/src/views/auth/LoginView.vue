<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { BRAND_FULL, BRAND_NAME, BRAND_SLOGAN } from '@/config/brand'
import BrandLogo from '@/components/brand/BrandLogo.vue'
import '@/styles/auth-page.css'

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
    <div class="auth-page__glow" aria-hidden="true" />
    <div class="auth-card">
      <div class="auth-brand">
        <BrandLogo :size="56" variant="student" />
        <h1 class="auth-brand__name">{{ BRAND_NAME }}</h1>
        <p class="auth-brand__slogan">{{ BRAND_SLOGAN }}</p>
      </div>

      <h2 class="auth-title">欢迎回来</h2>
      <p class="auth-sub">{{ BRAND_FULL }}</p>

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
        <el-button class="submit-btn" size="large" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>

      <div class="auth-footer">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </div>

      <div class="auth-tip">
        <p><strong>测试账号</strong>（需数据库已初始化）</p>
        <p>学员 student1 / admin123</p>
        <p>企业 enterprise1 / admin123</p>
        <p>管理员 admin / admin123</p>
      </div>
    </div>
  </div>
</template>
