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

const boardQueryMap: Record<string, string> = {
  campus: '校园频道',
  market: '校园集市',
  jobs: '求职经验',
  policy: '政策解读',
}

const boardVisuals: Record<string, { key: string; icon: string; accent: string; blurb: string }> = {
  校园频道: {
    key: 'campus',
    icon: '📚',
    accent: '#fb923c',
    blurb: '课程讨论、学习打卡与经验分享',
  },
  校园集市: {
    key: 'market',
    icon: '🛒',
    accent: '#f59e0b',
    blurb: '二手转让、拼单互助与校园生活',
  },
  求职经验: {
    key: 'jobs',
    icon: '💼',
    accent: '#f97316',
    blurb: '面试复盘、简历建议与求职路线',
  },
  政策解读: {
    key: 'policy',
    icon: '📜',
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
    icon: '💬',
    accent: '#fb923c',
    blurb: '学员自由发帖交流',
  }
}

function boardKeyOf(board?: ForumBoard | null) {
  if (!board) return ''
  return boardMeta(board.name).key
}

function truncate(text?: string, max = 72) {
  if (!text) return ''
  const normalized = text.replace(/\s+/g, ' ').trim()
  return normalized.length > max ? `${normalized.slice(0, max)}…` : normalized
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
    loadError.value = getErrorMessage(e, '论坛加载失败')
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
    loadError.value = getErrorMessage(e, '论坛加载失败')
  } finally {
    hubLoading.value = false
  }
}

async function openPost(post: ForumPost) {
  detailVisible.value = true
  detailLoading.value = true
  selectedPost.value = null
  try {
    selectedPost.value = unwrapApi(await getForumPostApi(post.id))
    await fetchReplies(post.id)
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '帖子详情加载失败'))
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
  submittingReply.value = true
  try {
    unwrapApi(await createForumReplyApi(selectedPost.value.id, { content: replyContent.value }))
    ElMessage.success('回复成功')
    replyContent.value = ''
    selectedPost.value.replyCount += 1
    await fetchReplies(selectedPost.value.id)
    await refreshCurrentView()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '回复失败'))
  } finally {
    submittingReply.value = false
  }
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
            :style="{ '--board-accent': boardMeta(board.name).accent }"
          >
            <div class="board-block__head">
              <div class="board-block__title">
                <span class="board-icon">{{ boardMeta(board.name).icon }}</span>
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
              <p class="eyebrow">{{ boardMeta(activeBoard?.name).icon }} Board</p>
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
            <div class="post-avatar">{{ (post.authorName || '匿').charAt(0) }}</div>
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

    <el-drawer v-model="detailVisible" size="52%" destroy-on-close>
      <template #header>
        <span>{{ selectedPost?.title || '帖子详情' }}</span>
      </template>
      <div v-loading="detailLoading" class="post-detail">
        <template v-if="selectedPost">
          <div class="detail-meta">
            <el-tag type="warning">{{ selectedPost.boardName }}</el-tag>
            <span>{{ selectedPost.authorName }}</span>
            <span>{{ formatTime(selectedPost.createTime) }}</span>
          </div>
          <p class="detail-content">{{ selectedPost.content }}</p>
          <div class="detail-actions">
            <el-button
              :type="selectedPost.liked ? 'primary' : 'default'"
              :icon="Pointer"
              @click="togglePostLike(selectedPost)"
            >
              点赞 {{ selectedPost.likeCount }}
            </el-button>
            <el-button :icon="Warning" @click="reportTarget(1, selectedPost.id)">举报</el-button>
          </div>

          <div class="reply-editor">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="3"
              maxlength="1000"
              show-word-limit
              placeholder="写下你的回复"
            />
            <el-button type="primary" :loading="submittingReply" @click="submitReply">回复</el-button>
          </div>

          <div v-loading="repliesLoading" class="reply-list">
            <h3>全部回复</h3>
            <el-empty
              v-if="!repliesLoading && replyList.length === 0"
              :image-size="72"
              description="暂无回复"
            />
            <article v-for="reply in replyList" :key="reply.id" class="reply-card">
              <div class="reply-header">
                <strong>{{ reply.authorName }}</strong>
                <span>{{ formatTime(reply.createTime) }}</span>
              </div>
              <p>{{ reply.content }}</p>
              <div class="reply-actions">
                <el-button
                  text
                  :type="reply.liked ? 'primary' : 'default'"
                  :icon="Pointer"
                  @click="toggleReplyLike(reply)"
                >
                  {{ reply.likeCount }}
                </el-button>
                <el-button text :icon="Warning" @click="reportTarget(2, reply.id)">举报</el-button>
              </div>
            </article>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="postDialogVisible" title="发布帖子" width="560px" destroy-on-close>
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
    radial-gradient(ellipse at 18% 20%, rgba(251, 146, 60, 0.45), transparent 48%),
    radial-gradient(ellipse at 88% 10%, rgba(245, 158, 11, 0.28), transparent 42%),
    linear-gradient(135deg, rgba(67, 20, 7, 0.88) 0%, rgba(154, 52, 18, 0.82) 48%, rgba(234, 88, 12, 0.78) 100%);
  border: 1px solid rgba(251, 146, 60, 0.35);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.28);
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
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.35);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}

.board-post-card:hover {
  transform: translateY(-2px);
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
  color: #1c1917;
  line-height: 1.4;
}

.board-post-card p {
  margin: 0;
  color: #57534e;
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
  color: #a8a29e;
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
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.18);
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
  border: 1px solid #fed7aa;
  background: #fff7ed;
  color: #9a3412;
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 13px;
}

.board-tab.active {
  background: #ea580c;
  border-color: #ea580c;
  color: #fff;
}

.post-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  border: 1px solid #f0e6d8;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 10px;
  cursor: pointer;
  background: #fffdf9;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.post-row:hover {
  border-color: #fa8c16;
  box-shadow: 0 8px 20px rgba(250, 140, 22, 0.12);
}

.post-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #fff7e8;
  color: #d46b08;
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
  color: var(--color-text);
}

.post-main p {
  color: var(--color-text-secondary);
  line-height: 1.55;
}

.post-meta,
.detail-meta,
.reply-header {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.post-actions,
.detail-actions,
.reply-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.reply-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-muted);
  font-size: 13px;
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.post-detail {
  min-height: 240px;
}

.detail-content {
  color: var(--color-text);
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 18px 0;
}

.reply-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 22px 0;
}

.reply-editor .el-button {
  align-self: flex-end;
}

.reply-list h3 {
  font-size: 16px;
  margin-bottom: 12px;
}

.reply-card {
  border-top: 1px solid var(--color-border);
  padding: 14px 0;
}

.reply-card p {
  margin-top: 8px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  white-space: pre-wrap;
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
