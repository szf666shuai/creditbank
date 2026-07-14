<script setup lang="ts">
import { computed, onMounted, ref, watch, type CSSProperties } from 'vue'
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
const boardHotPosts = ref<ForumPost[]>([])
const boardHotLoading = ref(false)
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
/** 正在回复的楼中楼目标；为空表示直接回帖 */
const replyTarget = ref<ForumReply | null>(null)

const replyPlaceholder = computed(() =>
  replyTarget.value
    ? `回复 @${replyTarget.value.authorName}`
    : '写下你的回复',
)

const boardQueryMap: Record<string, string> = {
  campus: '校园频道',
  market: '校园集市',
  jobs: '求职经验',
  policy: '政策解读',
}

const boardVisuals: Record<string, { key: string; icon: string; accent: string; blurb: string }> = {
  校园频道: {
    key: 'campus',
    icon: 'course',
    accent: '#fb923c',
    blurb: '课程讨论、学习打卡与经验分享',
  },
  校园集市: {
    key: 'market',
    icon: 'cart',
    accent: '#f59e0b',
    blurb: '二手转让、拼单互助与校园生活',
  },
  求职经验: {
    key: 'jobs',
    icon: 'job',
    accent: '#f97316',
    blurb: '面试复盘、简历建议与求职路线',
  },
  政策解读: {
    key: 'policy',
    icon: 'document',
    accent: '#ea580c',
    blurb: '学分认定、诚信规则与政策讨论',
  },
}

const isHub = computed(() => !selectedBoardId.value)

const activeBoard = computed(() =>
  boardList.value.find((item) => item.id === selectedBoardId.value) ?? null,
)

const activeBoardName = computed(() => activeBoard.value?.name ?? '全部板块')

const heroFeatured = computed(() => hotPosts.value[0] ?? null)

const heroHotList = computed(() => hotPosts.value.slice(0, 6))

const allBoardPostCount = computed(() =>
  boardList.value.reduce((sum, board) => sum + (board.postCount ?? 0), 0),
)

function boardMeta(name?: string) {
  return boardVisuals[name || ''] ?? {
    key: '',
    icon: 'forum',
    accent: '#fb923c',
    blurb: '学员自由发帖交流',
  }
}

function boardKeyOf(board?: ForumBoard | null) {
  if (!board) return ''
  return boardMeta(board.name).key
}

function boardAccentStyle(board: ForumBoard): CSSProperties {
  return {
    '--board-accent': boardMeta(board.name).accent,
  } as CSSProperties
}

function truncate(text?: string, max = 72) {
  if (!text) return ''
  const normalized = text.replace(/\s+/g, ' ').trim()
  return normalized.length > max ? `${normalized.slice(0, max)}…` : normalized
}

function heatValue(post: ForumPost) {
  return (post.likeCount || 0) + (post.replyCount || 0)
}

function boardBadgeName(post: ForumPost) {
  const boardName = post.boardName || activeBoard.value?.name || ''
  const badgeMap: Record<string, string> = {
    校园频道: '校',
    校园集市: '市',
    求职经验: '职',
    政策解读: '策',
  }
  return badgeMap[boardName] || boardName.charAt(0) || '帖'
}

function postBadge(post: ForumPost) {
  if (post.isTop) return '顶'
  if (heatValue(post) > 0 && boardHotPosts.value.some((item) => item.id === post.id)) return '热'
  return boardBadgeName(post)
}

function postBadgeClass(post: ForumPost) {
  if (post.isTop) return 'is-top'
  if (heatValue(post) > 0 && boardHotPosts.value.some((item) => item.id === post.id)) return 'is-hot'
  return `is-board-${boardMeta(post.boardName || activeBoard.value?.name).key || 'default'}`
}

function scorePost(post: ForumPost) {
  return heatValue(post)
}

function compareHotPosts(a: ForumPost, b: ForumPost) {
  const heatDiff = scorePost(b) - scorePost(a)
  if (heatDiff !== 0) return heatDiff
  return new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime()
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
    loadError.value = getErrorMessage(e, '论坛加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchBoardHotPosts() {
  if (!selectedBoardId.value) {
    boardHotPosts.value = []
    return
  }
  boardHotLoading.value = true
  try {
    const data = unwrapApi(
      await pageForumPostsApi({
        page: 1,
        pageSize: 100,
        boardId: selectedBoardId.value,
      }),
    )
    boardHotPosts.value = [...data.records].sort(compareHotPosts).slice(0, 5)
  } catch (e) {
    boardHotPosts.value = []
    ElMessage.error(getErrorMessage(e, '近期热门加载失败'))
  } finally {
    boardHotLoading.value = false
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
    hotPosts.value = [...hotData.records].sort(compareHotPosts)

    const highlights: Record<number, ForumPost[]> = {}
    boardList.value.forEach((board, index) => {
      highlights[board.id] = unwrapApi(boardResults[index]).records
    })
    boardHighlights.value = highlights
  } catch (e) {
    loadError.value = getErrorMessage(e, '论坛加载失败')
  } finally {
    hubLoading.value = false
  }
}

async function openPost(post: ForumPost) {
  detailVisible.value = true
  detailLoading.value = true
  // 先展示列表里的数据，避免接口短暂失败时抽屉空白
  selectedPost.value = post
  replyList.value = []
  replyTarget.value = null
  replyContent.value = ''
  try {
    selectedPost.value = unwrapApi(await getForumPostApi(post.id))
    await fetchReplies(post.id)
  } catch (e) {
    const tip = getErrorMessage(e, '帖子详情加载失败')
    ElMessage.error(tip)
    // 帖子可能因后端重启重种数据导致 ID 失效，刷新列表
    if (tip.includes('不存在')) {
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
    ElMessage.error(getErrorMessage(e, '回复加载失败'))
  } finally {
    repliesLoading.value = false
  }
}

function ensureStudent() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return false
  }
  if (!authStore.isStudent) {
    ElMessage.warning('仅学员可参与论坛互动')
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
    ElMessage.warning('请选择板块')
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
    ElMessage.success('发布成功')
    postDialogVisible.value = false
    selectedBoardId.value = post.boardId
    page.value = 1
    await fetchBoards()
    await refreshCurrentView()
    openPost(post)
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '发布失败'))
  } finally {
    submittingPost.value = false
  }
}

async function submitReply() {
  if (!selectedPost.value || !ensureStudent()) return
  const content = replyContent.value.trim()
  if (content.length < 2) {
    ElMessage.warning('回复至少 2 个字符')
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
    ElMessage.success(replyTarget.value ? '楼中楼回复成功' : '回复成功')
    replyContent.value = ''
    replyTarget.value = null
    selectedPost.value.replyCount += 1
    await fetchReplies(selectedPost.value.id)
    await refreshCurrentView()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '回复失败'))
  } finally {
    submittingReply.value = false
  }
}

function startReplyTo(reply: ForumReply) {
  if (!ensureStudent()) return
  replyTarget.value = reply
  // 滚到编辑区并聚焦
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
    ElMessage.error(getErrorMessage(e, '点赞失败'))
  }
}

async function toggleReplyLike(reply: ForumReply) {
  if (!ensureStudent()) return
  try {
    const liked = unwrapApi(await toggleForumLikeApi(2, reply.id))
    reply.liked = liked
    reply.likeCount += liked ? 1 : -1
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '点赞失败'))
  }
}

async function reportTarget(targetType: 1 | 2, targetId: number) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请填写举报原因', '举报内容', {
      confirmButtonText: '提交举报',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: (value) => value.trim().length >= 2 || '至少输入 2 个字符',
    })
    unwrapApi(await reportForumApi(targetType, targetId, value))
    ElMessage.success('举报已提交')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(getErrorMessage(e, '举报失败'))
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
    await Promise.all([fetchPosts(), fetchBoardHotPosts()])
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
      <!-- 总览：大展示区 + 热门 + 各板块精选 -->
      <template v-if="isHub">
        <section class="hero-stage">
          <div class="hero-stage__main">
            <div class="hero-stage__glow" aria-hidden="true" />
            <p class="hero-kicker">Community Forum</p>
            <h1>学员论坛</h1>
            <p class="hero-lead">
              课程讨论、校园互助、求职经验与政策解读汇聚于此。先浏览热门，再进入各板块深入交流。
            </p>
            <div class="hero-stats">
              <div>
                <strong>{{ boardList.length }}</strong>
                <span>活跃板块</span>
              </div>
              <div>
                <strong>{{ allBoardPostCount }}</strong>
                <span>公开帖子</span>
              </div>
              <div>
                <strong>{{ hotPosts.length }}</strong>
                <span>热议内容</span>
              </div>
            </div>
            <div class="hero-actions">
              <el-button type="warning" size="large" :icon="EditPen" @click="openPostDialog">
                发布新帖
              </el-button>
              <el-button size="large" plain @click="boardList[0] && openBoard(boardList[0])">
                浏览校园频道
              </el-button>
            </div>

            <article
              v-if="heroFeatured"
              class="hero-feature"
              @click="openPost(heroFeatured)"
            >
              <span class="hero-feature__badge">本周热议</span>
              <h2>{{ heroFeatured.title }}</h2>
              <p>{{ truncate(heroFeatured.content, 110) }}</p>
              <div class="hero-feature__meta">
                <span>{{ heroFeatured.boardName }}</span>
                <span>{{ heroFeatured.authorName }}</span>
                <span>{{ heroFeatured.likeCount }} 赞 · {{ heroFeatured.replyCount }} 回复</span>
              </div>
            </article>
          </div>

          <aside class="hero-hot-panel">
            <div class="hero-hot-panel__head">
              <h3>热门帖子</h3>
              <span>按热度排序</span>
            </div>
            <el-empty
              v-if="!hubLoading && heroHotList.length === 0"
              :image-size="64"
              description="暂无热门帖子"
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
                  {{ post.boardName }} · {{ post.likeCount }} 赞 · {{ post.replyCount }} 回复
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
            <el-button link type="primary" @click="refreshCurrentView">重新加载</el-button>
          </template>
        </el-alert>

        <section class="board-showcase">
          <header class="section-head">
            <div>
              <p class="eyebrow">Boards</p>
              <h2>各板块精选</h2>
            </div>
            <p>可从导航栏直接进入对应板块，或点击「更多」查看全部。</p>
          </header>

          <div
            v-for="board in boardList"
            :key="board.id"
            class="board-block"
            :style="boardAccentStyle(board)"
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
                更多
              </el-button>
            </div>

            <el-empty
              v-if="!(boardHighlights[board.id] || []).length"
              :image-size="56"
              description="该板块暂无帖子"
            />
            <div v-else class="board-posts">
              <article
                v-for="post in boardHighlights[board.id]"
                :key="post.id"
                class="board-post-card"
                @click="openPost(post)"
              >
                <div class="board-post-card__top">
                  <el-tag v-if="post.isTop" size="small" type="danger" effect="dark">置顶</el-tag>
                  <h4>{{ post.title }}</h4>
                </div>
                <p>{{ truncate(post.content, 88) }}</p>
                <div class="board-post-card__meta">
                  <span>{{ post.authorName }}</span>
                  <span>{{ formatTime(post.createTime) }}</span>
                  <span>{{ post.likeCount }} 赞 · {{ post.replyCount }} 回复</span>
                </div>
              </article>
            </div>
          </div>
        </section>
      </template>

      <!-- 板块详情列表 -->
      <template v-else>
        <section class="board-detail-head">
          <button type="button" class="back-link" @click="goHub">
            <el-icon><ArrowLeft /></el-icon>
            返回论坛总览
          </button>
          <div class="board-detail-head__row">
            <div>
              <p class="eyebrow">Board</p>
              <h1>{{ activeBoardName }}</h1>
              <p class="hero-lead">
                {{ activeBoard?.description || boardMeta(activeBoard?.name).blurb }}
              </p>
            </div>
            <el-button type="warning" :icon="EditPen" @click="openPostDialog">发帖</el-button>
          </div>
        </section>

        <section class="board-list-panel">
          <div class="list-toolbar">
            <div class="glass-search glass-search--light">
              <el-input
                v-model="keyword"
                clearable
                placeholder="搜索本板块帖子"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button class="glass-search__btn" type="warning" @click="handleSearch">搜索</el-button>
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

          <section class="recent-hot" v-loading="boardHotLoading">
            <div class="recent-hot__head">
              <div>
                <p class="eyebrow">Hot</p>
                <h2>近期热门</h2>
              </div>
              <span>按点赞数 + 评论数降序</span>
            </div>
            <el-empty
              v-if="!boardHotLoading && boardHotPosts.length === 0"
              :image-size="56"
              description="暂无热门帖子"
            />
            <div v-else class="recent-hot__list">
              <button
                v-for="(post, index) in boardHotPosts"
                :key="post.id"
                type="button"
                class="recent-hot__item"
                @click="openPost(post)"
              >
                <span class="recent-hot__rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                <span class="recent-hot__content">
                  <strong>{{ post.title }}</strong>
                  <small>
                    {{ post.authorName }} · {{ post.likeCount }} 赞 · {{ post.replyCount }} 评论 · 热度 {{ heatValue(post) }}
                  </small>
                </span>
              </button>
            </div>
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
              <el-button link type="primary" @click="fetchPosts">重新加载</el-button>
            </template>
          </el-alert>

          <el-empty
            v-if="!loading && postList.length === 0"
            :image-size="80"
            description="暂无帖子"
          />

          <article
            v-for="post in postList"
            :key="post.id"
            class="post-row"
            @click="openPost(post)"
          >
            <div class="post-avatar" :class="postBadgeClass(post)">{{ postBadge(post) }}</div>
            <div class="post-main">
              <div class="post-title-line">
                <el-tag v-if="post.isTop" size="small" type="danger">置顶</el-tag>
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
              <el-button text :icon="Warning" @click="reportTarget(1, post.id)">举报</el-button>
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
          <p class="drawer-kicker">帖子详情</p>
          <h2 class="drawer-title">{{ selectedPost?.title || '帖子详情' }}</h2>
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
                点赞 {{ selectedPost.likeCount }}
              </el-button>
              <el-button
                class="detail-btn detail-btn--ghost"
                :icon="Warning"
                @click="reportTarget(1, selectedPost.id)"
              >
                举报
              </el-button>
            </div>
          </section>

          <section class="reply-editor">
            <div v-if="replyTarget" class="reply-target-bar">
              <span>回复 @{{ replyTarget.authorName }}</span>
              <button type="button" class="reply-target-cancel" @click="cancelReplyTarget">取消</button>
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
                {{ replyTarget ? '回复楼中楼' : '发表回复' }}
              </el-button>
            </div>
          </section>

          <section v-loading="repliesLoading" class="reply-list">
            <div class="reply-list__head">
              <h3>全部回复</h3>
              <span>{{ replyList.length }} 条</span>
            </div>
            <el-empty
              v-if="!repliesLoading && replyList.length === 0"
              :image-size="72"
              description="暂无回复，来抢沙发吧"
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
                  回复 @{{ reply.parentAuthorName || '用户' }}
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
                  回复
                </el-button>
                <el-button text class="reply-action" :icon="Warning" @click="reportTarget(2, reply.id)">
                  举报
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
      title="发布帖子"
      width="560px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="板块">
          <el-select v-model="newPost.boardId" class="full-width" placeholder="选择板块">
            <el-option
              v-for="board in boardList"
              :key="board.id"
              :label="board.name"
              :value="board.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="newPost.title" maxlength="100" show-word-limit placeholder="输入帖子标题" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input
            v-model="newPost.content"
            type="textarea"
            :rows="6"
            maxlength="5000"
            show-word-limit
            placeholder="分享内容、问题或集市信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingPost" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.forum-page {
  padding: 20px 16px 56px;
  background: transparent;
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
  min-height: 420px;
  margin-bottom: 28px;
}

.hero-stage__main {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  padding: 36px 36px 28px;
  color: #fff8f0;
  background:
    radial-gradient(ellipse at 18% 20%, rgba(251, 146, 60, 0.22), transparent 48%),
    radial-gradient(ellipse at 88% 10%, rgba(245, 158, 11, 0.12), transparent 42%),
    rgba(12, 18, 32, 0.28);
  border: 1px solid rgba(251, 146, 60, 0.28);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(14px);
}

.hero-stage__glow {
  position: absolute;
  inset: auto -10% -30% 35%;
  height: 60%;
  background: radial-gradient(circle, rgba(255, 237, 213, 0.22), transparent 70%);
  pointer-events: none;
}

.hero-kicker,
.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 237, 213, 0.82);
  font-weight: 700;
}

.hero-stage__main h1,
.board-detail-head h1 {
  margin: 0 0 12px;
  font-size: clamp(32px, 4vw, 44px);
  line-height: 1.15;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.35);
}

.hero-lead {
  margin: 0;
  max-width: 520px;
  line-height: 1.75;
  color: rgba(255, 247, 237, 0.88);
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
  font-size: 26px;
  line-height: 1.1;
}

.hero-stats span {
  font-size: 12px;
  color: rgba(255, 237, 213, 0.72);
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
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: transform 0.18s, background 0.18s;
}

.hero-feature:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.16);
}

.hero-feature__badge {
  display: inline-block;
  margin-bottom: 8px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(251, 191, 36, 0.25);
  border: 1px solid rgba(251, 191, 36, 0.45);
}

.hero-feature h2 {
  margin: 0 0 8px;
  font-size: 20px;
}

.hero-feature p,
.hero-feature__meta {
  margin: 0;
  color: rgba(255, 247, 237, 0.8);
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
  border-radius: 24px;
  padding: 22px 18px;
  background: rgba(12, 20, 36, 0.42);
  border: 1px solid rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(16px);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.22);
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
  color: #fff7ed;
  font-size: 18px;
}

.hero-hot-panel__head span {
  color: rgba(255, 237, 213, 0.55);
  font-size: 12px;
}

.hot-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
  padding: 12px 10px;
  border: none;
  border-radius: 12px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s;
}

.hot-item:hover {
  background: rgba(255, 255, 255, 0.08);
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
  color: rgba(255, 247, 237, 0.7);
  background: rgba(255, 255, 255, 0.08);
}

.hot-rank.top {
  color: #431407;
  background: linear-gradient(135deg, #fdba74, #fb923c);
}

.hot-body {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.hot-body strong {
  color: #fff7ed;
  font-size: 14px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-body small {
  color: rgba(255, 237, 213, 0.58);
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
  color: rgba(255, 247, 237, 0.78);
}

.section-head h2 {
  margin: 0;
  color: #fff7ed;
  font-size: 28px;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.35);
}

.section-head p {
  margin: 0;
  max-width: 360px;
  line-height: 1.6;
  font-size: 14px;
}

.board-block {
  --board-accent: #fb923c;
  padding: 20px 22px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(12px);
  box-shadow: 0 14px 36px rgba(0, 0, 0, 0.18);
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
  font-size: 24px;
  background: color-mix(in srgb, var(--board-accent) 28%, transparent);
  border: 1px solid color-mix(in srgb, var(--board-accent) 45%, transparent);
}

.board-block__title h3 {
  margin: 0 0 4px;
  color: #fff7ed;
  font-size: 20px;
}

.board-block__title p {
  margin: 0;
  color: rgba(255, 237, 213, 0.68);
  font-size: 13px;
}

.more-btn {
  color: #fdba74 !important;
  font-weight: 600;
}

.board-posts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.board-post-card {
  padding: 16px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.32);
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s, background 0.15s;
}

.board-post-card:hover {
  transform: translateY(-2px);
  background: rgba(15, 23, 42, 0.45);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.16);
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
  color: #fff7ed;
  line-height: 1.4;
}

.board-post-card p {
  margin: 0;
  color: rgba(255, 237, 213, 0.72);
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
  color: rgba(255, 237, 213, 0.5);
  font-size: 12px;
}

.board-detail-head {
  margin-bottom: 18px;
  color: #fff7ed;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
  border: none;
  background: transparent;
  color: #fdba74;
  cursor: pointer;
  font-size: 14px;
}

.board-detail-head__row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
}

.board-list-panel {
  padding: 18px;
  border-radius: 20px;
  background: rgba(12, 20, 36, 0.38);
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.18);
}

.list-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
}

.recent-hot {
  margin-bottom: 16px;
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(251, 146, 60, 0.18);
}

.recent-hot__head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.recent-hot__head .eyebrow {
  margin-bottom: 4px;
}

.recent-hot__head h2 {
  margin: 0;
  color: #fff7ed;
  font-size: 18px;
}

.recent-hot__head span {
  color: rgba(255, 237, 213, 0.56);
  font-size: 12px;
  white-space: nowrap;
}

.recent-hot__list {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.recent-hot__item {
  min-width: 0;
  min-height: 86px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 237, 213, 0.12);
  background: rgba(15, 23, 42, 0.24);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s, transform 0.15s;
}

.recent-hot__item:hover {
  border-color: rgba(251, 146, 60, 0.42);
  background: rgba(15, 23, 42, 0.38);
  transform: translateY(-1px);
}

.recent-hot__rank {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  color: rgba(255, 237, 213, 0.72);
  background: rgba(255, 255, 255, 0.08);
  font-size: 12px;
  font-weight: 700;
}

.recent-hot__rank.top {
  color: #431407;
  background: linear-gradient(135deg, #fed7aa, #fb923c);
}

.recent-hot__content {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.recent-hot__content strong {
  color: #fff7ed;
  font-size: 14px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recent-hot__content small {
  color: rgba(255, 237, 213, 0.58);
  font-size: 12px;
  line-height: 1.45;
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
  border: 1px solid rgba(251, 146, 60, 0.35);
  background: rgba(15, 23, 42, 0.35);
  color: #fdba74;
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 13px;
}

.board-tab.active {
  background: rgba(234, 88, 12, 0.85);
  border-color: rgba(251, 146, 60, 0.7);
  color: #fff;
}

.post-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 10px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.06);
  transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
}

.post-row:hover {
  border-color: rgba(251, 146, 60, 0.45);
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.16);
}

.post-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: rgba(251, 146, 60, 0.2);
  color: #fdba74;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
  border: 1px solid rgba(251, 146, 60, 0.28);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.post-avatar.is-top {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.9), rgba(249, 115, 22, 0.88));
  color: #fff7ed;
  border-color: rgba(254, 202, 202, 0.55);
}

.post-avatar.is-hot {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.92), rgba(251, 146, 60, 0.9));
  color: #431407;
  border-color: rgba(254, 215, 170, 0.65);
}

.post-avatar.is-board-campus {
  background: rgba(59, 130, 246, 0.2);
  color: #93c5fd;
  border-color: rgba(147, 197, 253, 0.36);
}

.post-avatar.is-board-market {
  background: rgba(16, 185, 129, 0.2);
  color: #86efac;
  border-color: rgba(134, 239, 172, 0.34);
}

.post-avatar.is-board-jobs {
  background: rgba(168, 85, 247, 0.2);
  color: #d8b4fe;
  border-color: rgba(216, 180, 254, 0.36);
}

.post-avatar.is-board-policy {
  background: rgba(14, 165, 233, 0.2);
  color: #7dd3fc;
  border-color: rgba(125, 211, 252, 0.36);
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
  color: #fff7ed;
}

.post-main p {
  color: rgba(255, 237, 213, 0.72);
  line-height: 1.55;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
  color: rgba(255, 237, 213, 0.5);
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
  color: rgba(255, 237, 213, 0.55);
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
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(253, 186, 116, 0.88);
  font-weight: 700;
}

.drawer-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.4;
  color: #fff7ed;
  font-weight: 700;
}

.detail-post,
.reply-editor,
.reply-list {
  padding: 16px;
  border-radius: 14px;
  background: rgba(12, 18, 32, 0.35);
  border: 1px solid rgba(251, 146, 60, 0.2);
}

.detail-meta,
.reply-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  color: rgba(255, 237, 213, 0.72);
  font-size: 12px;
}

.detail-board {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(251, 146, 60, 0.2);
  border: 1px solid rgba(251, 146, 60, 0.35);
  color: #fdba74;
  font-weight: 600;
}

.detail-content {
  color: rgba(255, 247, 237, 0.92);
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
  --el-button-bg-color: rgba(251, 146, 60, 0.16);
  --el-button-border-color: rgba(251, 146, 60, 0.34);
  --el-button-text-color: #ffedd5;
  --el-button-hover-bg-color: rgba(251, 146, 60, 0.28);
  --el-button-hover-border-color: rgba(251, 146, 60, 0.5);
  --el-button-hover-text-color: #fff7ed;
}

.detail-btn.is-liked,
.detail-btn--primary {
  --el-button-bg-color: rgba(249, 115, 22, 0.88);
  --el-button-border-color: rgba(249, 115, 22, 0.95);
  --el-button-text-color: #fff7ed;
  --el-button-hover-bg-color: rgba(234, 88, 12, 0.95);
  --el-button-hover-border-color: rgba(234, 88, 12, 1);
  --el-button-hover-text-color: #fff;
}

.detail-btn--ghost {
  --el-button-bg-color: transparent;
  --el-button-border-color: rgba(255, 237, 213, 0.22);
  --el-button-text-color: rgba(255, 237, 213, 0.82);
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
  background: rgba(251, 146, 60, 0.14);
  border: 1px solid rgba(251, 146, 60, 0.28);
  color: #fdba74;
  font-size: 13px;
}

.reply-target-cancel {
  border: 0;
  background: transparent;
  color: rgba(255, 237, 213, 0.72);
  cursor: pointer;
  font-size: 12px;
}

.reply-target-cancel:hover {
  color: #fff7ed;
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
  color: #fff7ed;
}

.reply-list__head span {
  font-size: 12px;
  color: rgba(255, 237, 213, 0.55);
}

.reply-card {
  border-top: 1px solid rgba(255, 237, 213, 0.12);
  padding: 14px 0;
}

.reply-card:first-of-type {
  border-top: 0;
  padding-top: 8px;
}

.reply-card--nested {
  margin-left: 12px;
  padding-left: 12px;
  border-left: 2px solid rgba(251, 146, 60, 0.4);
}

.reply-header strong {
  color: #ffedd5;
  font-size: 13px;
}

.reply-card p {
  margin: 8px 0 10px;
  color: rgba(255, 247, 237, 0.88);
  line-height: 1.7;
  white-space: pre-wrap;
}

.reply-to {
  color: #fb923c;
}

.reply-action {
  color: rgba(255, 237, 213, 0.62) !important;
}

.reply-action:hover,
.reply-action.is-liked {
  color: #fb923c !important;
}

.full-width {
  width: 100%;
}

@media (max-width: 960px) {
  .hero-stage,
  .board-posts {
    grid-template-columns: 1fr;
  }

  .recent-hot__list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
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

@media (max-width: 640px) {
  .recent-hot__head {
    flex-direction: column;
    align-items: flex-start;
  }

  .recent-hot__head span {
    white-space: normal;
  }

  .recent-hot__list {
    grid-template-columns: 1fr;
  }
}
</style>

<style>
/* Drawer / Dialog teleport 到 body，需非 scoped */
.forum-detail-drawer.el-drawer {
  background:
    radial-gradient(ellipse at 12% 0%, rgba(251, 146, 60, 0.18), transparent 42%),
    radial-gradient(ellipse at 100% 20%, rgba(245, 158, 11, 0.1), transparent 45%),
    rgba(10, 16, 28, 0.92);
  border-left: 1px solid rgba(251, 146, 60, 0.28);
  box-shadow: -18px 0 48px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(18px);
}

.forum-detail-drawer .el-drawer__header {
  margin-bottom: 12px;
  padding: 18px 20px 12px;
  border-bottom: 1px solid rgba(255, 237, 213, 0.12);
  color: #fff7ed;
}

.forum-detail-drawer .el-drawer__close-btn,
.forum-detail-drawer .el-drawer__close-btn .el-icon {
  color: rgba(255, 237, 213, 0.78);
}

.forum-detail-drawer .el-drawer__close-btn:hover,
.forum-detail-drawer .el-drawer__close-btn:hover .el-icon {
  color: #fff7ed;
}

.forum-detail-drawer .el-drawer__body {
  padding: 16px 20px 24px;
  color: #ffedd5;
}

.forum-detail-drawer .el-textarea__inner {
  background: rgba(8, 14, 26, 0.55);
  border: 1px solid rgba(251, 146, 60, 0.28);
  box-shadow: none;
  color: #fff7ed;
  border-radius: 12px;
}

.forum-detail-drawer .el-textarea__inner:hover,
.forum-detail-drawer .el-textarea__inner:focus {
  border-color: rgba(251, 146, 60, 0.5);
}

.forum-detail-drawer .el-textarea__inner::placeholder {
  color: rgba(255, 237, 213, 0.42);
}

.forum-detail-drawer .el-input__count {
  background: transparent;
  color: rgba(255, 237, 213, 0.45);
}

.forum-detail-drawer .el-loading-mask {
  background: rgba(8, 14, 26, 0.45);
}

.forum-detail-drawer .el-empty__description p {
  color: rgba(255, 237, 213, 0.55);
}

.forum-post-dialog.el-dialog {
  background:
    radial-gradient(ellipse at 10% 0%, rgba(251, 146, 60, 0.16), transparent 40%),
    rgba(12, 18, 32, 0.94);
  border: 1px solid rgba(251, 146, 60, 0.28);
  border-radius: 16px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.4);
}

.forum-post-dialog .el-dialog__header {
  border-bottom: 1px solid rgba(255, 237, 213, 0.12);
  margin-right: 0;
  padding: 16px 20px;
}

.forum-post-dialog .el-dialog__title,
.forum-post-dialog .el-form-item__label {
  color: #fff7ed;
}

.forum-post-dialog .el-dialog__headerbtn .el-dialog__close {
  color: rgba(255, 237, 213, 0.75);
}

.forum-post-dialog .el-dialog__body {
  padding: 18px 20px;
  color: #ffedd5;
}

.forum-post-dialog .el-dialog__footer {
  border-top: 1px solid rgba(255, 237, 213, 0.12);
  padding: 14px 20px;
}

.forum-post-dialog .el-input__wrapper,
.forum-post-dialog .el-textarea__inner,
.forum-post-dialog .el-select__wrapper {
  background: rgba(8, 14, 26, 0.55);
  box-shadow: 0 0 0 1px rgba(251, 146, 60, 0.28) inset;
}

.forum-post-dialog .el-input__inner,
.forum-post-dialog .el-textarea__inner,
.forum-post-dialog .el-select__placeholder,
.forum-post-dialog .el-select__selected-item {
  color: #fff7ed;
}

.forum-post-dialog .el-input__inner::placeholder,
.forum-post-dialog .el-textarea__inner::placeholder {
  color: rgba(255, 237, 213, 0.42);
}
</style>
