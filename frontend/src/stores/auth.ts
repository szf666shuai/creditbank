import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMeApi, loginApi, registerApi } from '@/api/auth'
import type { LoginForm, RegisterForm, UserInfo } from '@/types/auth'
import { ROLE_ADMIN, ROLE_ENTERPRISE, ROLE_STUDENT } from '@/types/auth'
import { clearAuthToken, readAuthToken, writeAuthToken } from '@/utils/auth-storage'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(readAuthToken())
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isStudent = computed(() => userInfo.value?.role === ROLE_STUDENT)
  const isEnterprise = computed(() => userInfo.value?.role === ROLE_ENTERPRISE)
  const isAdmin = computed(() => userInfo.value?.role === ROLE_ADMIN)
  const displayName = computed(
    () => userInfo.value?.realName || userInfo.value?.username || '用户',
  )

  function setToken(newToken: string) {
    token.value = newToken
    writeAuthToken(newToken)
  }

  function clearAuth() {
    token.value = null
    userInfo.value = null
    clearAuthToken()
  }

  async function login(form: LoginForm) {
    const res = await loginApi(form)
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '登录失败')
    }
    setToken(res.data.token)
    userInfo.value = res.data.userInfo
    return res.data
  }

  async function register(form: RegisterForm) {
    const res = await registerApi(form)
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '注册失败')
    }
    setToken(res.data.token)
    userInfo.value = res.data.userInfo
    return res.data
  }

  async function fetchUserInfo() {
    if (!token.value) return null
    const res = await getMeApi()
    if (res.code === 200 && res.data) {
      userInfo.value = res.data
      return res.data
    }
    clearAuth()
    return null
  }

  async function initAuth() {
    if (!token.value) return
    try {
      await fetchUserInfo()
    } catch {
      clearAuth()
    }
  }

  function logout() {
    clearAuth()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isStudent,
    isEnterprise,
    isAdmin,
    displayName,
    login,
    register,
    fetchUserInfo,
    initAuth,
    logout,
  }
})
