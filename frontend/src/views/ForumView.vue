<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ChatDotRound, EditPen, Pointer, Search, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createForumPostApi,
  createForumReplyApi,
  getForumPostApi,
  listForumBoardsApi,
  pageForumPostsApi,
  pageForumRepliesApi,
  reportForumApi,
  toggleForumLikeApi,
  type ForumBoard,
  type ForumPost,
  type ForumReply,
} from '@/api/forum'
import { useAuthStore } from '@/stores/auth'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'
import UiIcon from '@/components/ui/UiIcon.vue'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const hubLoading = ref(false)
const loadError = ref<string | null>(null)
const boardList = ref<ForumBoard[]>([])
const postList = ref<ForumPost[]>([])
const hotPosts = ref<ForumPost[]>([])
const boardHighlights = ref<Record<number, ForumPost[]>>({})
const replyList = ref<ForumReply[]>([])
const selectedBoardId = ref<number | undefined>(undefined)
const selectedPost = ref<ForumPost | null>(null)
const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const detailLoading = ref(false)
const repliesLoading = ref(false)
const postDialogVisible = ref(false)
const submittingPost = ref(false)
const submittingReply = ref(false)
const newPost = ref({ boardId: undefined as number | undefined, title: '', content: '' })
const replyContent = ref('')
/** ??????????????????? */
const replyTarget = ref<ForumReply | null>(null)

const replyPlaceholder = computed(() =>
  replyTarget.value
    ? `?? @${replyTarget.value.authorName}`
    : '??????',
)

const boardQueryMap: Record<string, string> = {
  campus: '????',
  market: '????',
  jobs: '????',
  policy: '????',
}

const boardVisuals: Record<string, { key: string; icon: string; accent: string; blurb: string }> = {
  ????: {
    key: 'campus',
    icon: 'course',
    accent: '#22c55e',
    blurb: '??????????????',
  },
  ????: {
    key: 'market',
    icon: 'cart',
    accent: '#d97706',
    blurb: '??????????????',
  },
  ????: {
    key: 'jobs',
    icon: 'job',
    accent: '#0284c7',
    blurb: '??????????????',
  },
  ????: {
    key: 'policy',
    icon: 'document',
    accent: '#16a34a',
    blurb: '??????????????',
  },
}

const isHub = computed(() => !selectedBoardId.value)

const activeBoard = computed(() =>
  boardList.value.find((item) => item.id === selectedBoardId.value) ?? null,
)

const activeBoardName = computed(() => activeBoard.value?.name ?? '????')

const heroFeatured = computed(() => hotPosts.value[0] ?? null)

const heroHotList = computed(() => hotPosts.value.slice(0, 6))

const allBoardPostCount = computed(() =>
  boardList.value.reduce((sum, board) => sum + (board.postCount ?? 0), 0),
)

function boardMeta(name?: string) {
  return boardVisuals[name || ''] ?? {
    key: '',
    icon: 'forum',
    accent: '#22c55e',
    blurb: '????????',
  }
}

function boardKeyOf(board?: ForumBoard | null) {
  if (!board) return ''
  return boardMeta(board.name).key
}

function truncate(text?: string, max = 72) {
  if (!text) return ''
  const normalized = text.replace(/\s+/g, ' ').trim()
  return normalized.length > max ? `${normalized.slice(0, max)}?` : normalized
}

function scorePost(post: ForumPost) {
  return (post.isTop ? 1000 : 0) + post.likeCount * 3 + post.replyCount * 2 + post.viewCount
}

async function fetchBoards() {
  boardList.value = unwrapApi(await listForumBoardsApi())
  if (!newPost.value.boardId && boardList.value.length > 0) {
    newPost.value.boardId = boardList.value[0].id
  }
  applyBoardFromRoute()
}

async function fetchPosts() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(
      await pageForumPostsApi({
        page: page.value,
        pageSize: pageSize.value,
        boardId: selectedBoardId.value,
        keyword: keyword.value,
      }),
    )
    postList.value = data.records
    total.value = data.total
  } catch (e) {
    loadError.value = getErrorMessage(e, '??????')
  } finally {
    loading.value = false
  }
}

async function fetchHubData() {
  hubLoading.value = true
  loadError.value = null
  try {
    const [hotRes, ...boardResults] = await Promise.all([
      pageForumPostsApi({ page: 1, pageSize: 20 }),
      ...boardList.value.map((board) =>
        pageForumPostsApi({ page: 1, pageSize: 4, boardId: board.id }),
      ),
    ])
    const hotData = unwrapApi(hotRes)
    hotPosts.value = [...hotData.records].sort((a, b) => scorePost(b) - scorePost(a))

    const highlights: Record<number, ForumPost[]> = {}
    boardList.value.forEach((board, index) => {
      highlights[board.id] = unwrapApi(boardResults[index]).records
    })
    boardHighlights.value = highlights
  } catch (e) {
    loadError.value = getErrorMessage(e, '??????')
  } finally {
    hubLoading.value = false
  }
}

async function openPost(post: ForumPost) {
  detailVisible.value = true
  detailLoading.value = true
  // ???????????????????????
  selectedPost.value = post
  replyList.value = []
  replyTarget.value = null
  replyContent.value = ''
  try {
    selectedPost.value = unwrapApi(await getForumPostApi(post.id))
    await fetchReplies(post.id)
  } catch (e) {
    const tip = getErrorMessage(e, '????????')
    ElMessage.error(tip)
    // ??????????????? ID ???????
    if (tip.includes('???')) {
      await refreshCurrentView()
    }
  } finally {
    detailLoading.value = false
  }
}

async function fetchReplies(postId: number) {
  repliesLoading.value = true
  try {
    const data = unwrapApi(await pageForumRepliesApi(postId, { page: 1, pageSize: 50 }))
    replyList.value = data.records
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '??????'))
  } finally {
    repliesLoading.value = false
  }
}

function ensureStudent() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('????')
    return false
  }
  if (!authStore.isStudent) {
    ElMessage.warning('??????????')
    return false
  }
  return true
}

function openPostDialog() {
  if (!ensureStudent()) return
  newPost.value = {
    boardId: selectedBoardId.value ?? boardList.value[0]?.id,
    title: '',
    content: '',
  }
  postDialogVisible.value = true
}

async function submitPost() {
  if (!newPost.value.boardId) {
    ElMessage.warning('?????')
    return
  }
  submittingPost.value = true
  try {
    const post = unwrapApi(
      await createForumPostApi({
        boardId: newPost.value.boardId,
        title: newPost.value.title,
        content: newPost.value.content,
      }),
    )
    ElMessage.success('????')
    postDialogVisible.value = false
    selectedBoardId.value = post.boardId
    page.value = 1
    await fetchBoards()
    await refreshCurrentView()
    openPost(post)
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '????'))
  } finally {
    submittingPost.value = false
  }
}

async function submitReply() {
  if (!selectedPost.value || !ensureStudent()) return
  const content = replyContent.value.trim()
  if (content.length < 2) {
    ElMessage.warning('???? 2 ???')
    return
  }
  submittingReply.value = true
  try {
    unwrapApi(
      await createForumReplyApi(selectedPost.value.id, {
        content,
        parentId: replyTarget.value?.id,
      }),
    )
    ElMessage.success(replyTarget.value ? '???????' : '????')
    replyContent.value = ''
    replyTarget.value = null
    selectedPost.value.replyCount += 1
    await fetchReplies(selectedPost.value.id)
    await refreshCurrentView()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '????'))
  } finally {
    submittingReply.value = false
  }
}

function startReplyTo(reply: ForumReply) {
  if (!ensureStudent()) return
  replyTarget.value = reply
  // ????????
  window.requestAnimationFrame(() => {
    const el = document.querySelector('.reply-editor textarea') as HTMLTextAreaElement | null
    el?.focus()
  })
}

function cancelReplyTarget() {
  replyTarget.value = null
}

function isNestedReply(reply: ForumReply) {
  return !!(reply.parentId && reply.parentId > 0)
}

async function togglePostLike(post: ForumPost) {
  if (!ensureStudent()) return
  try {
    const liked = unwrapApi(await toggleForumLikeApi(1, post.id))
    post.liked = liked
    post.likeCount += liked ? 1 : -1
    if (selectedPost.value?.id === post.id) {
      selectedPost.value.liked = liked
      selectedPost.value.likeCount = post.likeCount
    }
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '????'))
  }
}

async function toggleReplyLike(reply: ForumReply) {
  if (!ensureStudent()) return
  try {
    const liked = unwrapApi(await toggleForumLikeApi(2, reply.id))
    reply.liked = liked
    reply.likeCount += liked ? 1 : -1
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '????'))
  }
}

async function reportTarget(targetType: 1 | 2, targetId: number) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('????')
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('???????', '????', {
      confirmButtonText: '????',
      cancelButtonText: '??',
      inputType: 'textarea',
      inputValidator: (value) => value.trim().length >= 2 || '???? 2 ???',
    })
    unwrapApi(await reportForumApi(targetType, targetId, value))
    ElMessage.success('?????')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(getErrorMessage(e, '????'))
    }
  }
}

function goHub() {
  selectedBoardId.value = undefined
  page.value = 1
  keyword.value = ''
  router.push({ path: '/forum' })
}

function selectBoard(boardId?: number) {
  selectedBoardId.value = boardId
  page.value = 1
  keyword.value = ''
  syncBoardQuery(boardId)
}

function openBoard(board: ForumBoard) {
  selectBoard(board.id)
}

function handleSearch() {
  page.value = 1
  fetchPosts()
}

function applyBoardFromRoute() {
  const boardKey = typeof route.query.board === 'string' ? route.query.board : ''
  const boardName = boardQueryMap[boardKey]
  if (!boardName) {
    selectedBoardId.value = undefined
    return
  }
  const board = boardList.value.find((item) => item.name === boardName)
  selectedBoardId.value = board?.id
}

function syncBoardQuery(boardId?: number) {
  const board = boardList.value.find((item) => item.id === boardId)
  const boardKey = boardKeyOf(board)
  const nextQuery: Record<string, string> = {}
  if (boardKey) nextQuery.board = boardKey
  router.push({ path: '/forum', query: nextQuery })
}

async function refreshCurrentView() {
  if (isHub.value) {
    await fetchHubData()
  } else {
    await fetchPosts()
  }
}

async function openPostFromRoute() {
  const id = Number(route.query.id)
  if (!id) return
  try {
    const post = unwrapApi(await getForumPostApi(id))
    await openPost(post)
  } catch {
    /* ignore invalid deep link */
  }
}

watch(page, () => {
  if (!isHub.value) fetchPosts()
})

watch(
  () => [route.query.board, route.query.id],
  async () => {
    applyBoardFromRoute()
    page.value = 1
    await refreshCurrentView()
    await openPostFromRoute()
  },
)

onMounted(async () => {
  await authStore.initAuth()
  await fetchBoards()
  await refreshCurrentView()
  await openPostFromRoute()
})
</script>

<template>
  <div class="forum-page" v-loading="hubLoading || loading">
    <div class="forum-inner">
      <!-- ??????? + ?? + ????? -->
      <template v-if="isHub">
        <section class="hero-stage">
          <div class="hero-stage__main">
            <div class="hero-stage__glow" aria-hidden="true" />
            <p class="hero-kicker">????</p>
            <h1>????</h1>
            <p class="hero-lead">
              ?????????????????????????????????????????
            </p>
            <div class="hero-stats">
              <div>
                <strong>{{ boardList.length }}</strong>
                <span>????</span>
              </div>
              <div>
                <strong>{{ allBoardPostCount }}</strong>
                <span>????</span>
              </div>
              <div>
                <strong>{{ hotPosts.length }}</strong>
                <span>????</span>
              </div>
            </div>
            <div class="hero-actions">
              <el-button type="primary" size="large" :icon="EditPen" @click="openPostDialog">
                ????
              </el-button>
              <el-button size="large" plain @click="boardList[0] && openBoard(boardList[0])">
                ??????
              </el-button>
            </div>

            <article
              v-if="heroFeatured"
              class="hero-feature"
              @click="openPost(heroFeatured)"
            >
              <span class="hero-feature__badge">????</span>
              <h2>{{ heroFeatured.title }}</h2>
              <p>{{ truncate(heroFeatured.content, 110) }}</p>
              <div class="hero-feature__meta">
                <span>{{ heroFeatured.boardName }}</span>
                <span>{{ heroFeatured.authorName }}</span>
                <span>{{ heroFeatured.likeCount }} ? ? {{ heroFeatured.replyCount }} ??</span>
              </div>
            </article>
          </div>

          <aside class="hero-hot-panel">
            <div class="hero-hot-panel__head">
              <h3>????</h3>
              <span>?????</span>
            </div>
            <el-empty
              v-if="!hubLoading && heroHotList.length === 0"
              :image-size="64"
              description="??????"
            />
            <button
              v-for="(post, index) in heroHotList"
              :key="post.id"
              type="button"
              class="hot-item"
              @click="openPost(post)"
            >
              <span class="hot-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="hot-body">
                <strong>{{ post.title }}</strong>
                <small>
                  {{ post.boardName }} ? {{ post.likeCount }} ? ? {{ post.replyCount }} ??
                </small>
              </span>
            </button>
          </aside>
        </section>

        <el-alert
          v-if="loadError"
          :title="loadError"
          type="warning"
          show-icon
          :closable="false"
          class="forum-alert"
        >
          <template #default>
            <el-button link type="primary" @click="refreshCurrentView">????</el-button>
          </template>
        </el-alert>

        <section class="board-showcase">
          <header class="section-head">
            <div>
              <p class="eyebrow">????</p>
              <h2>?????</h2>
            </div>
            <p>??????????????????????????</p>
          </header>

          <div
            v-for="board in boardList"
            :key="board.id"
            class="board-block"
            :style="{ '--board-accent': boardMeta(board.name).accent }"
          >
            <div class="board-block__head">
              <div class="board-block__title">
                <span class="board-icon"><UiIcon :name="boardMeta(board.name).icon" :size="22" /></span>
                <div>
                  <h3>{{ board.name }}</h3>
                  <p>{{ board.description || boardMeta(board.name).blurb }}</p>
                </div>
              </div>
              <el-button class="more-btn" text @click="openBoard(board)">
                ??
              </el-button>
            </div>

            <el-empty
              v-if="!(boardHighlights[board.id] || []).length"
              :image-size="56"
              description="???????"
            />
            <div v-else class="board-posts">
              <article
                v-for="post in boardHighlights[board.id]"
                :key="post.id"
                class="board-post-card"
                @click="openPost(post)"
              >
                <div class="board-post-card__top">
                  <el-tag v-if="post.isTop" size="small" type="danger" effect="dark">??</el-tag>
                  <h4>{{ post.title }}</h4>
                </div>
                <p>{{ truncate(post.content, 88) }}</p>
                <div class="board-post-card__meta">
                  <span>{{ post.authorName }}</span>
                  <span>{{ formatTime(post.createTime) }}</span>
                  <span>{{ post.likeCount }} ? ? {{ post.replyCount }} ??</span>
                </div>
              </article>
            </div>
          </div>
        </section>
      </template>

      <!-- ?????? -->
      <template v-else>
        <section class="board-detail-head">
          <button type="button" class="back-link" @click="goHub">
            <el-icon><ArrowLeft /></el-icon>
            ??????
          </button>
          <div class="board-detail-head__row">
            <div>
              <p class="eyebrow">??</p>
              <h1>{{ activeBoardName }}</h1>
              <p class="hero-lead">
                {{ activeBoard?.description || boardMeta(activeBoard?.name).blurb }}
              </p>
            </div>
            <el-button type="primary" :icon="EditPen" @click="openPostDialog">??</el-button>
          </div>
        </section>

        <section class="board-list-panel">
          <div class="list-toolbar">
            <div class="glass-search glass-search--light">
              <el-input
                v-model="keyword"
                clearable
                placeholder="???????"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button class="glass-search__btn" type="primary" @click="handleSearch">??</el-button>
            </div>
            <div class="board-tabs">
              <button
                v-for="board in boardList"
                :key="board.id"
                type="button"
                class="board-tab"
                :class="{ active: selectedBoardId === board.id }"
                @click="selectBoard(board.id)"
              >
                {{ board.name }}
              </button>
            </div>
          </div>

          <el-alert
            v-if="loadError"
            :title="loadError"
            type="warning"
            show-icon
            :closable="false"
            class="forum-alert"
          >
            <template #default>
              <el-button link type="primary" @click="fetchPosts">????</el-button>
            </template>
          </el-alert>

          <el-empty
            v-if="!loading && postList.length === 0"
            :image-size="80"
            description="????"
          />

          <article
            v-for="post in postList"
            :key="post.id"
            class="post-row"
            @click="openPost(post)"
          >
            <div class="post-avatar">{{ (post.authorName || '?').charAt(0) }}</div>
            <div class="post-main">
              <div class="post-title-line">
                <el-tag v-if="post.isTop" size="small" type="danger">??</el-tag>
                <h3>{{ post.title }}</h3>
              </div>
              <p>{{ truncate(post.content, 140) }}</p>
              <div class="post-meta">
                <span>{{ post.authorName }}</span>
                <span>{{ formatTime(post.createTime) }}</span>
              </div>
            </div>
            <div class="post-actions" @click.stop>
              <el-button
                text
                :type="post.liked ? 'primary' : 'default'"
                :icon="Pointer"
                @click="togglePostLike(post)"
              >
                {{ post.likeCount }}
              </el-button>
              <span class="reply-count">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.replyCount }}
              </span>
              <el-button text :icon="Warning" @click="reportTarget(1, post.id)">??</el-button>
            </div>
          </article>

          <div v-if="total > 0" class="page-pagination">
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="total"
              layout="total, prev, pager, next"
              background
            />
          </div>
        </section>
      </template>
    </div>

    <el-drawer
      v-model="detailVisible"
      class="forum-detail-drawer"
      size="540px"
      destroy-on-close
    >
      <template #header>
        <div class="drawer-head">
          <p class="drawer-kicker">????</p>
          <h2 class="drawer-title">{{ selectedPost?.title || '????' }}</h2>
        </div>
      </template>
      <div v-loading="detailLoading" class="post-detail">
        <template v-if="selectedPost">
          <section class="detail-post">
            <div class="detail-meta">
              <span class="detail-board">{{ selectedPost.boardName }}</span>
              <span>{{ selectedPost.authorName }}</span>
              <span>{{ formatTime(selectedPost.createTime) }}</span>
            </div>
            <p class="detail-content">{{ selectedPost.content }}</p>
            <div class="detail-actions">
              <el-button
                class="detail-btn"
                :class="{ 'is-liked': selectedPost.liked }"
                :icon="Pointer"
                @click="togglePostLike(selectedPost)"
              >
                ?? {{ selectedPost.likeCount }}
              </el-button>
              <el-button
                class="detail-btn detail-btn--ghost"
                :icon="Warning"
                @click="reportTarget(1, selectedPost.id)"
              >
                ??
              </el-button>
            </div>
          </section>

          <section class="reply-editor">
            <div v-if="replyTarget" class="reply-target-bar">
              <span>?? @{{ replyTarget.authorName }}</span>
              <button type="button" class="reply-target-cancel" @click="cancelReplyTarget">??</button>
            </div>
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="3"
              maxlength="1000"
              show-word-limit
              :placeholder="replyPlaceholder"
            />
            <div class="reply-editor__footer">
              <el-button
                class="detail-btn detail-btn--primary"
                :loading="submittingReply"
                @click="submitReply"
              >
                {{ replyTarget ? '?????' : '????' }}
              </el-button>
            </div>
          </section>

          <section v-loading="repliesLoading" class="reply-list">
            <div class="reply-list__head">
              <h3>????</h3>
              <span>{{ replyList.length }} ?</span>
            </div>
            <el-empty
              v-if="!repliesLoading && replyList.length === 0"
              :image-size="72"
              description="??????????"
            />
            <article
              v-for="reply in replyList"
              :key="reply.id"
              class="reply-card"
              :class="{ 'reply-card--nested': isNestedReply(reply) }"
            >
              <div class="reply-header">
                <strong>{{ reply.authorName }}</strong>
                <span v-if="isNestedReply(reply)" class="reply-to">
                  ?? @{{ reply.parentAuthorName || '??' }}
                </span>
                <span>{{ formatTime(reply.createTime) }}</span>
              </div>
              <p>{{ reply.content }}</p>
              <div class="reply-actions">
                <el-button
                  text
                  class="reply-action"
                  :class="{ 'is-liked': reply.liked }"
                  :icon="Pointer"
                  @click="toggleReplyLike(reply)"
                >
                  {{ reply.likeCount }}
                </el-button>
                <el-button text class="reply-action" :icon="ChatDotRound" @click="startReplyTo(reply)">
                  ??
                </el-button>
                <el-button text class="reply-action" :icon="Warning" @click="reportTarget(2, reply.id)">
                  ??
                </el-button>
              </div>
            </article>
          </section>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="postDialogVisible"
      class="forum-post-dialog"
      title="????"
      width="560px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="??">
          <el-select v-model="newPost.boardId" class="full-width" placeholder="????">
            <el-option
              v-for="board in boardList"
              :key="board.id"
              :label="board.name"
              :value="board.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="??">
          <el-input v-model="newPost.title" maxlength="100" show-word-limit placeholder="??????" />
        </el-form-item>
        <el-form-item label="??">
          <el-input
            v-model="newPost.content"
            type="textarea"
            :rows="6"
            maxlength="5000"
            show-word-limit
            placeholder="????????????"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="postDialogVisible = false">??</el-button>
        <el-button type="primary" :loading="submittingPost" @click="submitPost">??</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.forum-page {
  padding: 20px 16px 56px;
  background: var(--color-background);
  min-height: calc(100vh - var(--header-height));
}

.forum-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.hero-stage {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(280px, 0.9fr);
  gap: 18px;
  min-height: 380px;
  margin-bottom: 28px;
}

.hero-stage__main {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-xl);
  padding: 36px 36px 28px;
  color: var(--color-foreground);
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow-lg, var(--shadow-md));
}

.hero-stage__glow {
  position: absolute;
  inset: auto -10% -30% 35%;
  height: 60%;
  background: radial-gradient(circle, rgba(34, 197, 94, 0.16), transparent 70%);
  pointer-events: none;
}

.hero-kicker,
.eyebrow {
  margin: 0 0 8px;
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
  font-weight: 700;
}

.hero-stage__main h1,
.board-detail-head h1 {
  margin: 0 0 12px;
  font-family: var(--font-heading);
  font-size: clamp(1.75rem, 4vw, 2.5rem);
  line-height: 1.15;
  color: var(--color-foreground);
  text-shadow: none;
}

.hero-lead {
  margin: 0;
  max-width: 520px;
  line-height: 1.75;
  color: var(--color-text-secondary);
}

.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin: 22px 0 18px;
}

.hero-stats div {
  min-width: 88px;
}

.hero-stats strong {
  display: block;
  font-size: 1.5rem;
  line-height: 1.1;
  color: var(--color-primary-dark);
}

.hero-stats span {
  font-size: 12px;
  color: var(--color-muted-foreground);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 22px;
}

.hero-feature {
  position: relative;
  z-index: 1;
  padding: 18px 20px;
  border-radius: var(--radius-lg);
  background: var(--nb-cream, #fff9f0);
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow-sm, var(--shadow-sm));
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.hero-feature:hover {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.hero-feature__badge {
  display: inline-block;
  margin-bottom: 8px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: var(--color-primary-light);
  border: 1px solid var(--color-border);
  color: var(--color-primary-dark);
}

.hero-feature h2 {
  margin: 0 0 8px;
  font-family: var(--font-heading);
  font-size: 1.2rem;
  color: var(--color-foreground);
}

.hero-feature p,
.hero-feature__meta {
  margin: 0;
  color: var(--color-muted-foreground);
  line-height: 1.6;
}

.hero-feature__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  font-size: 12px;
}

.hero-hot-panel {
  border-radius: var(--radius-xl);
  padding: 22px 18px;
  background: #fff;
  border: 2.5px solid var(--nb-ink, var(--color-border-neutral));
  box-shadow: var(--nb-shadow, var(--shadow-md));
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hero-hot-panel__head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 8px;
  padding: 0 6px;
}

.hero-hot-panel__head h3 {
  margin: 0;
  font-family: var(--font-heading);
  color: var(--color-foreground);
  font-size: 1.125rem;
}

.hero-hot-panel__head span {
  color: var(--color-muted-foreground);
  font-size: 12px;
}

.hot-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
  padding: 12px 10px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease;
  font: inherit;
}

.hot-item:hover {
  background: var(--color-primary-light);
}

.hot-rank {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 700;
  color: var(--color-muted-foreground);
  background: var(--color-muted);
}

.hot-rank.top {
  color: var(--color-on-primary);
  background: var(--color-primary);
}

.hot-body {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.hot-body strong {
  color: var(--color-foreground);
  font-size: 14px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-body small {
  color: var(--color-muted-foreground);
  font-size: 12px;
}

.forum-alert {
  margin-bottom: 18px;
}

.board-showcase {
  display: grid;
  gap: 18px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
  color: var(--color-muted-foreground);
}

.section-head h2 {
  margin: 0;
  font-family: var(--font-heading);
  color: var(--color-foreground);
  font-size: 1.75rem;
  text-shadow: none;
}

.section-head p {
  margin: 0;
  max-width: 360px;
  line-height: 1.6;
  font-size: 14px;
}

.board-block {
  padding: 20px 22px;
  border-radius: var(--radius-lg);
  background: var(--color-card);
  border: 1px solid var(--color-border-neutral);
  box-shadow: var(--shadow-md);
}

.board-block__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.board-block__title {
  display: flex;
  gap: 14px;
  align-items: center;
  min-width: 0;
}

.board-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: var(--board-accent, var(--color-primary));
  background: color-mix(in srgb, var(--board-accent, var(--color-primary)) 16%, white);
  border: 1px solid color-mix(in srgb, var(--board-accent, var(--color-primary)) 28%, transparent);
}

.board-block__title h3 {
  margin: 0 0 4px;
  font-family: var(--font-heading);
  color: var(--color-foreground);
  font-size: 1.2rem;
}

.board-block__title p {
  margin: 0;
  color: var(--color-muted-foreground);
  font-size: 13px;
}

.more-btn {
  color: var(--color-primary) !important;
  font-weight: 600;
}

.board-posts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.board-post-card {
  padding: 16px;
  border-radius: var(--radius-md);
  background: var(--color-background);
  border: 1px solid var(--color-border-neutral);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
}

.board-post-card:hover {
  transform: translateY(-2px);
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
}

.board-post-card__top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.board-post-card h4 {
  margin: 0;
  font-size: 15px;
  color: var(--color-foreground);
  line-height: 1.4;
}

.board-post-card p {
  margin: 0;
  color: var(--color-muted-foreground);
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

.board-post-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
  color: var(--color-muted-foreground);
  font-size: 12px;
}

.board-detail-head {
  margin-bottom: 18px;
  color: var(--color-foreground);
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
  border: none;
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.board-detail-head__row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
}

.board-list-panel {
  padding: 18px;
  border-radius: var(--radius-lg);
  background: var(--color-card);
  border: 1px solid var(--color-border-neutral);
  box-shadow: var(--shadow-md);
}

.list-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
}

.forum-search {
  width: 260px;
}

.board-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-left: auto;
}

.board-tab {
  border: 1px solid var(--color-border-neutral);
  background: var(--color-background);
  color: var(--color-foreground);
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.board-tab.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-on-primary);
  font-weight: 600;
}

.post-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  border: 1px solid var(--color-border-neutral);
  border-radius: var(--radius-md);
  padding: 14px;
  margin-bottom: 10px;
  cursor: pointer;
  background: var(--color-background);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.post-row:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-sm);
}

.post-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}

.post-main {
  min-width: 0;
  flex: 1;
}

.post-title-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.post-title-line h3 {
  font-size: 16px;
  color: var(--color-foreground);
  margin: 0;
}

.post-main p {
  color: var(--color-muted-foreground);
  line-height: 1.55;
  margin: 0;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
  color: var(--color-muted-foreground);
  font-size: 12px;
}

.post-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.reply-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-muted-foreground);
  font-size: 13px;
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.post-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 240px;
  padding-bottom: 12px;
}

.drawer-head {
  min-width: 0;
  padding-right: 12px;
}

.drawer-kicker {
  margin: 0 0 4px;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
  font-weight: 700;
}

.drawer-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.4;
  color: var(--color-foreground);
  font-weight: 700;
  font-family: var(--font-heading);
}

.detail-post,
.reply-editor,
.reply-list {
  padding: 16px;
  border-radius: var(--radius-md);
  background: var(--color-background);
  border: 1px solid var(--color-border-neutral);
}

.detail-meta,
.reply-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  color: var(--color-muted-foreground);
  font-size: 12px;
}

.detail-board {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--color-primary-light);
  border: 1px solid var(--color-border);
  color: var(--color-primary-dark);
  font-weight: 600;
}

.detail-content {
  color: var(--color-foreground);
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 14px 0 16px;
  font-size: 15px;
}

.detail-actions,
.reply-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-btn {
  --el-button-bg-color: var(--color-primary-light);
  --el-button-border-color: var(--color-border);
  --el-button-text-color: var(--color-primary-dark);
  --el-button-hover-bg-color: #b2f5ea;
  --el-button-hover-border-color: var(--color-primary);
  --el-button-hover-text-color: var(--color-primary-dark);
}

.detail-btn.is-liked,
.detail-btn--primary {
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-text-color: var(--color-on-primary);
  --el-button-hover-bg-color: var(--color-primary-dark);
  --el-button-hover-border-color: var(--color-primary-dark);
  --el-button-hover-text-color: var(--color-on-primary);
}

.detail-btn--ghost {
  --el-button-bg-color: transparent;
  --el-button-border-color: var(--color-border-neutral);
  --el-button-text-color: var(--color-muted-foreground);
}

.reply-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reply-editor__footer {
  display: flex;
  justify-content: flex-end;
}

.reply-target-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 10px;
  background: var(--color-primary-light);
  border: 1px solid var(--color-border);
  color: var(--color-primary-dark);
  font-size: 13px;
}

.reply-target-cancel {
  border: 0;
  background: transparent;
  color: var(--color-muted-foreground);
  cursor: pointer;
  font-size: 12px;
}

.reply-target-cancel:hover {
  color: var(--color-primary-dark);
}

.reply-list__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.reply-list__head h3 {
  margin: 0;
  font-size: 16px;
  color: var(--color-foreground);
}

.reply-list__head span {
  font-size: 12px;
  color: var(--color-muted-foreground);
}

.reply-card {
  border-top: 1px solid var(--color-border-neutral);
  padding: 14px 0;
}

.reply-card:first-of-type {
  border-top: 0;
  padding-top: 8px;
}

.reply-card--nested {
  margin-left: 12px;
  padding-left: 12px;
  border-left: 2px solid var(--color-border);
}

.reply-header strong {
  color: var(--color-foreground);
  font-size: 13px;
}

.reply-card p {
  margin: 8px 0 10px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  white-space: pre-wrap;
}

.reply-to {
  color: var(--color-primary);
}

.reply-action {
  color: var(--color-muted-foreground) !important;
}

.reply-action:hover,
.reply-action.is-liked {
  color: var(--color-primary) !important;
}

.full-width {
  width: 100%;
}

@media (max-width: 960px) {
  .hero-stage,
  .board-posts {
    grid-template-columns: 1fr;
  }

  .section-head,
  .board-detail-head__row {
    flex-direction: column;
    align-items: flex-start;
  }

  .board-tabs {
    margin-left: 0;
    width: 100%;
  }

  .post-row {
    flex-direction: column;
  }
}
</style>

<style>
/* Drawer / Dialog teleport ? body??? scoped */
.forum-detail-drawer.el-drawer {
  background: var(--color-card);
  border-left: 1px solid var(--color-border-neutral);
  box-shadow: var(--shadow-lg);
}

.forum-detail-drawer .el-drawer__header {
  margin-bottom: 12px;
  padding: 18px 20px 12px;
  border-bottom: 1px solid var(--color-border-neutral);
  color: var(--color-foreground);
}

.forum-detail-drawer .el-drawer__close-btn,
.forum-detail-drawer .el-drawer__close-btn .el-icon {
  color: var(--color-muted-foreground);
}

.forum-detail-drawer .el-drawer__close-btn:hover,
.forum-detail-drawer .el-drawer__close-btn:hover .el-icon {
  color: var(--color-primary);
}

.forum-detail-drawer .el-drawer__body {
  padding: 16px 20px 24px;
  color: var(--color-foreground);
  background: var(--color-card);
}

.forum-detail-drawer .el-textarea__inner {
  background: var(--color-background);
  border: 1px solid var(--color-border-neutral);
  box-shadow: none;
  color: var(--color-foreground);
  border-radius: 12px;
}

.forum-detail-drawer .el-textarea__inner:hover,
.forum-detail-drawer .el-textarea__inner:focus {
  border-color: var(--color-primary);
}

.forum-detail-drawer .el-textarea__inner::placeholder {
  color: var(--color-muted-foreground);
}

.forum-detail-drawer .el-input__count {
  background: transparent;
  color: var(--color-muted-foreground);
}

.forum-detail-drawer .el-loading-mask {
  background: rgba(240, 253, 250, 0.55);
}

.forum-detail-drawer .el-empty__description p {
  color: var(--color-muted-foreground);
}

.forum-post-dialog.el-dialog {
  background: var(--color-card);
  border: 1px solid var(--color-border-neutral);
  border-radius: 16px;
  box-shadow: var(--shadow-lg);
}

.forum-post-dialog .el-dialog__header {
  border-bottom: 1px solid var(--color-border-neutral);
  margin-right: 0;
  padding: 16px 20px;
}

.forum-post-dialog .el-dialog__title,
.forum-post-dialog .el-form-item__label {
  color: var(--color-foreground);
}

.forum-post-dialog .el-dialog__headerbtn .el-dialog__close {
  color: var(--color-muted-foreground);
}

.forum-post-dialog .el-dialog__body {
  padding: 18px 20px;
  color: var(--color-foreground);
}

.forum-post-dialog .el-dialog__footer {
  border-top: 1px solid var(--color-border-neutral);
  padding: 14px 20px;
}

.forum-post-dialog .el-input__wrapper,
.forum-post-dialog .el-textarea__inner,
.forum-post-dialog .el-select__wrapper {
  background: var(--color-background);
  box-shadow: 0 0 0 1px var(--color-border-neutral) inset;
}

.forum-post-dialog .el-input__inner,
.forum-post-dialog .el-textarea__inner,
.forum-post-dialog .el-select__placeholder,
.forum-post-dialog .el-select__selected-item {
  color: var(--color-foreground);
}

.forum-post-dialog .el-input__inner::placeholder,
.forum-post-dialog .el-textarea__inner::placeholder {
  color: var(--color-muted-foreground);
}
</style>
