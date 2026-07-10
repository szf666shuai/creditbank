import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/utils/request'
import { getErrorMessage, unwrapApi } from '@/utils/api'

export function useAsyncData() {
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function run<T>(
    fn: () => Promise<ApiResponse<T>>,
    options?: {
      fallback?: string
      toast?: boolean
      onSuccess?: (data: T) => void
    },
  ): Promise<T | null> {
    loading.value = true
    error.value = null
    try {
      const data = unwrapApi(await fn())
      options?.onSuccess?.(data)
      return data
    } catch (e) {
      const message = getErrorMessage(e, options?.fallback ?? '加载失败')
      error.value = message
      if (options?.toast) {
        ElMessage.error(message)
      }
      return null
    } finally {
      loading.value = false
    }
  }

  function clearError() {
    error.value = null
  }

  return { loading, error, run, clearError }
}
