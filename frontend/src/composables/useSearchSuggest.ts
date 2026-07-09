import { onBeforeUnmount, ref, watch } from 'vue'
import { searchSuggestApi, type SearchItem } from '@/api/search'

const DEBOUNCE_MS = 280
const MIN_CHARS = 1

export function useSearchSuggest(
  keyword: { value: string },
  category: { value: string },
) {
  const suggestions = ref<SearchItem[]>([])
  const showSuggest = ref(false)
  const loadingSuggest = ref(false)
  const activeIndex = ref(-1)

  let timer: ReturnType<typeof setTimeout> | null = null
  let requestSeq = 0

  function clearTimer() {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  async function fetchSuggest(q: string, type: string) {
    const seq = ++requestSeq
    loadingSuggest.value = true
    try {
      const res = await searchSuggestApi({ q, type, limit: 8 })
      if (seq !== requestSeq) return
      if (res.code === 200 && res.data) {
        suggestions.value = res.data.suggestions ?? []
        showSuggest.value = suggestions.value.length > 0
        activeIndex.value = -1
      } else {
        suggestions.value = []
        showSuggest.value = false
      }
    } catch {
      if (seq !== requestSeq) return
      suggestions.value = []
      showSuggest.value = false
    } finally {
      if (seq === requestSeq) loadingSuggest.value = false
    }
  }

  function scheduleSuggest() {
    clearTimer()
    const q = keyword.value.trim()
    if (q.length < MIN_CHARS) {
      suggestions.value = []
      showSuggest.value = false
      activeIndex.value = -1
      return
    }
    timer = setTimeout(() => {
      fetchSuggest(q, category.value)
    }, DEBOUNCE_MS)
  }

  function hideSuggest() {
    showSuggest.value = false
    activeIndex.value = -1
  }

  function openSuggestIfAny() {
    if (suggestions.value.length > 0 && keyword.value.trim().length >= MIN_CHARS) {
      showSuggest.value = true
    }
  }

  function moveActive(delta: number) {
    if (!showSuggest.value || suggestions.value.length === 0) return
    const len = suggestions.value.length
    activeIndex.value = (activeIndex.value + delta + len) % len
  }

  function pickActive(): SearchItem | null {
    if (activeIndex.value < 0 || activeIndex.value >= suggestions.value.length) {
      return null
    }
    return suggestions.value[activeIndex.value]
  }

  watch(
    () => [keyword.value, category.value] as const,
    () => {
      scheduleSuggest()
    },
  )

  onBeforeUnmount(clearTimer)

  return {
    suggestions,
    showSuggest,
    loadingSuggest,
    activeIndex,
    hideSuggest,
    openSuggestIfAny,
    moveActive,
    pickActive,
    scheduleSuggest,
  }
}
