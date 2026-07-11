<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound, EditPen, Pointer, Search, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
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
const loadError = ref<string | null>(null)
const boardList = ref<ForumBoard[]>([])
const postList = ref<ForumPost[]>([])
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

const activeBoardName = computed(() => {
  if (!selectedBoardId.value) return '全部板块'
  return boardList.value.find((item) => item.id === selectedBoardId.value)?.name ?? '当前板块'
})

const allBoardPostCount = computed(() => {
  return boardList.value.reduce((sum, board) => sum + (board.postCount ?? 0), 0)
})

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
    await fetchPosts()
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
    await fetchPosts()
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

function selectBoard(boardId?: number) {
  selectedBoardId.value = boardId
  page.value = 1
  syncBoardQuery(boardId)
  fetchPosts()
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
  const boardKey = Object.entries(boardQueryMap).find(([, name]) => name === board?.name)?.[0]
  const nextQuery = { ...route.query }
  if (boardKey) {
    nextQuery.board = boardKey
  } else {
    delete nextQuery.board
  }
  router.replace({ query: nextQuery })
}

watch(page, fetchPosts)

watch(
  () => route.query.board,
  () => {
    applyBoardFromRoute()
    page.value = 1
    fetchPosts()
  },
)

onMounted(async () => {
  await authStore.initAuth()
  await fetchBoards()
  await fetchPosts()
})
</script>

<template>
  <div class="forum-wrap">
    <PageShell
      plain
      title="论坛"
      description="学员自由发帖交流。官方招聘、活动与政策请前往资讯中心。"
      :loading="loading"
      :error="loadError"
      @retry="fetchPosts"
    >
      <template #actions>
        <el-button type="warning" :icon="EditPen" @click="openPostDialog">发帖</el-button>
      </template>

      <div class="forum-layout">
        <aside class="board-panel">
          <button
            type="button"
            class="board-item"
            :class="{ active: !selectedBoardId }"
            @click="selectBoard(undefined)"
          >
            <span>全部板块</span>
            <strong>{{ allBoardPostCount }}</strong>
          </button>
          <button
            v-for="board in boardList"
            :key="board.id"
            type="button"
            class="board-item"
            :class="{ active: selectedBoardId === board.id }"
            @click="selectBoard(board.id)"
          >
            <span>{{ board.name }}</span>
            <strong>{{ board.postCount }}</strong>
            <small>{{ board.description }}</small>
          </button>
        </aside>

        <section class="post-panel">
          <div class="page-toolbar">
            <el-input
              v-model="keyword"
              class="forum-search"
              clearable
              placeholder="搜索帖子"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button @click="handleSearch">搜索</el-button>
            <span class="active-board">{{ activeBoardName }}</span>
          </div>

          <el-empty
            v-if="!loading && postList.length === 0"
            class="page-empty"
            :image-size="80"
            description="暂无帖子"
          />

          <article v-for="post in postList" :key="post.id" class="post-row" @click="openPost(post)">
            <div class="post-avatar">{{ (post.authorName || '匿').charAt(0) }}</div>
            <div class="post-main">
              <div class="post-title-line">
                <el-tag v-if="post.isTop" size="small" type="danger">置顶</el-tag>
                <h3>{{ post.title }}</h3>
              </div>
              <p>{{ post.content }}</p>
              <div class="post-meta">
                <span class="board-chip">{{ post.boardName }}</span>
                <span>{{ post.authorName }}</span>
                <span>{{ formatTime(post.createTime) }}</span>
              </div>
            </div>
            <div class="post-actions" @click.stop>
              <el-button text :type="post.liked ? 'primary' : 'default'" :icon="Pointer" @click="togglePostLike(post)">
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
      </div>
    </PageShell>

    <el-drawer v-model="detailVisible" size="52%" destroy-on-close>
      <template #header>
        <span>{{ selectedPost?.title || '帖子详情' }}</span>
      </template>
      <div v-loading="detailLoading" class="post-detail">
        <template v-if="selectedPost">
          <div class="detail-meta">
            <el-tag type="info">{{ selectedPost.boardName }}</el-tag>
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
            <el-empty v-if="!repliesLoading && replyList.length === 0" :image-size="72" description="暂无回复" />
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
            <el-option v-for="board in boardList" :key="board.id" :label="board.name" :value="board.id" />
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
.forum-wrap {
  padding: 24px 16px 48px;
}

.forum-wrap :deep(.page-shell) {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.forum-wrap :deep(.page-header__main h1) {
  color: #f5f8ff;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.35);
}

.forum-wrap :deep(.page-header__main p) {
  color: rgba(245, 248, 255, 0.78);
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.3);
}

.forum-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 16px;
}

.board-panel,
.post-panel {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
}

.board-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-self: start;
  border-top: 4px solid #fa8c16;
}

.board-item {
  border: none;
  border-radius: 10px;
  background: transparent;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 4px 8px;
}

.board-item:hover,
.board-item.active {
  background: #fff7e8;
}

.board-item span,
.board-item strong {
  color: var(--color-text);
}

.board-item.active span,
.board-item.active strong {
  color: #d46b08;
}

.board-item small {
  grid-column: 1 / -1;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.forum-search {
  width: 280px;
}

.active-board {
  color: #d46b08;
  font-size: 13px;
  font-weight: 600;
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

.board-chip {
  color: #d46b08;
  background: #fff7e8;
  padding: 1px 8px;
  border-radius: 999px;
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

@media (max-width: 900px) {
  .forum-layout {
    grid-template-columns: 1fr;
  }

  .post-row {
    flex-direction: column;
  }

  .post-actions {
    justify-content: flex-start;
  }
}
</style>
