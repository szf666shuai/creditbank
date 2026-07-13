<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, ChatLineRound, Pointer } from '@element-plus/icons-vue'
import { fetchCourseComments, postCourseComment, toggleCourseCommentLike, type CourseComment } from '@/api/learning'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  courseId: number
}>()

const authStore = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const likingCommentId = ref<number | null>(null)
const comments = ref<CourseComment[]>([])
const draft = ref('')
const replyTarget = ref<CourseComment | null>(null)

const totalCount = computed(() => {
  return comments.value.reduce((sum, item) => sum + 1 + (item.replies?.length || 0), 0)
})

async function loadComments() {
  loading.value = true
  try {
    const res = await fetchCourseComments(props.courseId)
    if (res.code === 200 && res.data) {
      comments.value = res.data
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '评论加载失败')
  } finally {
    loading.value = false
  }
}

function startReply(item: CourseComment) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('登录后即可参与讨论')
    return
  }
  replyTarget.value = item
}

function cancelReply() {
  replyTarget.value = null
}

async function submitComment() {
  const content = draft.value.trim()
  if (!content) return
  if (!authStore.isLoggedIn) {
    ElMessage.warning('登录后即可参与讨论')
    return
  }
  submitting.value = true
  const isReply = !!replyTarget.value
  try {
    const res = await postCourseComment(props.courseId, {
      content,
      parentId: replyTarget.value?.id,
    })
    if (res.code !== 200 || !res.data) throw new Error(res.message || '发表评论失败')
    const created = res.data
    if (isReply) {
      const parent = comments.value.find((item) => item.id === created.parentId)
      if (parent) {
        parent.replies = [...(parent.replies || []), created]
      } else {
        await loadComments()
      }
    } else {
      comments.value.unshift({ ...created, replies: created.replies || [] })
    }
    draft.value = ''
    replyTarget.value = null
    const reward = Number(created.creditReward || 0)
    ElMessage.success(
      reward > 0
        ? `${isReply ? '回复已发布' : '评论已发布'}，获得 ${reward.toFixed(2)} 秩点`
        : isReply
          ? '回复已发布'
          : '评论已发布',
    )
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '发表评论失败')
  } finally {
    submitting.value = false
  }
}

function findCommentById(commentId: number) {
  for (const item of comments.value) {
    if (item.id === commentId) return item
    const reply = item.replies?.find((child) => child.id === commentId)
    if (reply) return reply
  }
  return null
}

function applyLikeState(commentId: number, likeCount: number, liked: boolean) {
  const target = findCommentById(commentId)
  if (!target) return
  target.likeCount = likeCount
  target.liked = liked
}

async function toggleLike(item: CourseComment) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('登录后即可点赞')
    return
  }
  if (item.userId === authStore.userInfo?.id) {
    ElMessage.warning('不能给自己的评论点赞')
    return
  }
  likingCommentId.value = item.id
  try {
    const res = await toggleCourseCommentLike(props.courseId, item.id)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '点赞失败')
    applyLikeState(res.data.commentId, res.data.likeCount, res.data.liked)
    if (res.data.liked) {
      ElMessage.success('点赞成功')
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '点赞失败')
  } finally {
    likingCommentId.value = null
  }
}

function formatTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const now = Date.now()
  const diff = now - date.getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < hour) return `${Math.max(1, Math.floor(diff / minute))} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < day * 7) return `${Math.floor(diff / day)} 天前`
  return date.toLocaleDateString()
}

function avatarText(name: string) {
  return name?.trim()?.charAt(0) || '学'
}

watch(
  () => props.courseId,
  () => {
    replyTarget.value = null
    void loadComments()
  },
  { immediate: true },
)
</script>

<template>
  <div class="comment-panel">
    <div class="comment-head">
      <h3>
        <el-icon><ChatDotRound /></el-icon>
        课程讨论
      </h3>
      <span>{{ totalCount }} 条评论</span>
    </div>

    <div class="comment-editor">
      <div class="editor-avatar">{{ avatarText(authStore.displayName) }}</div>
      <div class="editor-main">
        <div v-if="replyTarget" class="reply-hint">
          回复 <strong>@{{ replyTarget.authorName }}</strong>
          <button type="button" @click="cancelReply">取消</button>
        </div>
        <el-input
          v-model="draft"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          :placeholder="replyTarget ? `回复 ${replyTarget.authorName}...` : '分享你的学习心得、疑问或笔记要点'"
        />
        <div class="editor-actions">
          <span class="editor-tip">发表评论/回复可获得秩点奖励，被点赞也有奖励</span>
          <el-button type="primary" :loading="submitting" @click="submitComment">
            {{ replyTarget ? '发表回复' : '发表评论' }}
          </el-button>
        </div>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="5" animated />
    <el-empty v-else-if="!comments.length" description="还没有评论，来做第一个分享者吧" />

    <div v-else class="comment-list">
      <article v-for="item in comments" :key="item.id" class="comment-thread">
        <div class="comment-item">
          <div class="comment-avatar">{{ avatarText(item.authorName) }}</div>
          <div class="comment-body">
            <div class="comment-meta">
              <strong>{{ item.authorName }}</strong>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
            <p>{{ item.content }}</p>
            <div class="comment-actions">
              <button
                type="button"
                class="like-btn"
                :class="{ active: item.liked }"
                :disabled="likingCommentId === item.id"
                @click="toggleLike(item)"
              >
                <el-icon><Pointer /></el-icon>
                {{ item.likeCount || 0 }}
              </button>
              <button type="button" class="reply-btn" @click="startReply(item)">
                <el-icon><ChatLineRound /></el-icon>
                回复
              </button>
            </div>
          </div>
        </div>

        <div v-if="item.replies?.length" class="reply-list">
          <div v-for="reply in item.replies" :key="reply.id" class="comment-item reply-item">
            <div class="comment-avatar small">{{ avatarText(reply.authorName) }}</div>
            <div class="comment-body">
              <div class="comment-meta">
                <strong>{{ reply.authorName }}</strong>
                <span v-if="reply.replyToAuthorName" class="reply-to">回复 @{{ reply.replyToAuthorName }}</span>
                <span>{{ formatTime(reply.createTime) }}</span>
              </div>
              <p>{{ reply.content }}</p>
              <div class="comment-actions">
                <button
                  type="button"
                  class="like-btn"
                  :class="{ active: reply.liked }"
                  :disabled="likingCommentId === reply.id"
                  @click="toggleLike(reply)"
                >
                  <el-icon><Pointer /></el-icon>
                  {{ reply.likeCount || 0 }}
                </button>
                <button type="button" class="reply-btn" @click="startReply(item)">
                  <el-icon><ChatLineRound /></el-icon>
                  回复
                </button>
              </div>
            </div>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.comment-panel {
  display: grid;
  gap: 18px;
}

.comment-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-head h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
}

.comment-head span {
  color: var(--color-text-muted);
  font-size: 13px;
}

.comment-editor {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: var(--nb-cream, #fff9f0);
  border: 2.5px solid var(--nb-ink, #1a202c);
  box-shadow: var(--nb-shadow-sm, 3px 3px 0 0 #1a202c);
}

.editor-avatar,
.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: var(--nb-green, #22c55e);
  border: 2px solid var(--nb-ink, #1a202c);
  color: #fff;
  font-weight: 800;
  box-shadow: 2px 2px 0 0 var(--nb-ink, #1a202c);
}

.comment-avatar.small {
  width: 32px;
  height: 32px;
  font-size: 13px;
}

.editor-main {
  display: grid;
  gap: 10px;
}

.reply-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.reply-hint button {
  border: none;
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
}

.editor-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.editor-tip {
  color: var(--color-text-muted);
  font-size: 12px;
}

.comment-list {
  display: grid;
  gap: 18px;
}

.comment-thread {
  padding-bottom: 18px;
  border-bottom: 1px solid var(--color-border);
}

.comment-item {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 12px;
}

.reply-list {
  margin-top: 14px;
  margin-left: 52px;
  display: grid;
  gap: 14px;
  padding-left: 14px;
  border-left: 2px solid #eef2f6;
}

.reply-item {
  grid-template-columns: 32px minmax(0, 1fr);
}

.comment-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 6px;
}

.comment-meta strong {
  color: var(--color-text);
}

.comment-meta span,
.reply-to {
  color: var(--color-text-muted);
  font-size: 12px;
}

.reply-to {
  color: #22c55e;
}

.comment-body p {
  margin: 0 0 8px;
  line-height: 1.75;
  color: var(--color-text-secondary);
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.like-btn,
.reply-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: 12px;
}

.like-btn:hover,
.like-btn.active {
  color: #fb7299;
}

.reply-btn:hover {
  color: var(--color-primary);
}
</style>
