import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCountApi } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)

  async function refreshUnreadCount() {
    try {
      const res = await getUnreadCountApi()
      if (res.code === 200) {
        unreadCount.value = res.data ?? 0
      }
    } catch {
      unreadCount.value = 0
    }
  }

  function reset() {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    refreshUnreadCount,
    reset,
  }
})
