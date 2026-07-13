<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  deleteMyForumPostApi,
  deleteMyForumReplyApi,
  listMyForumPostsApi,
  listMyForumRepliesApi,
  type MyForumPostItem,
  type MyForumReplyItem,
} from '@/api/profile-posts'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const activeTab = ref<'posts' | 'replies'>('posts')
const posts = ref<MyForumPostItem[]>([])
const replies = ref<MyForumReplyItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

function postStatusType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  return 'info'
}

async function fetchPosts() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(await listMyForumPostsApi({ page: page.value, pageSize: pageSize.value }))
    posts.value = data.records
    total.value = data.total
    page.value = data.page
    pageSize.value = data.pageSize
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchReplies() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(await listMyForumRepliesApi({ page: page.value, pageSize: pageSize.value }))
    replies.value = data.records
    total.value = data.total
    page.value = data.page
    pageSize.value = data.pageSize
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchCurrentTab() {
  if (activeTab.value === 'posts') {
    await fetchPosts()
  } else {
    await fetchReplies()
  }
}

function handleTabChange() {
  page.value = 1
  fetchCurrentTab()
}

function handlePageChange() {
  fetchCurrentTab()
}

function handleSizeChange() {
  page.value = 1
  fetchCurrentTab()
}

async function handleDeletePost(row: MyForumPostItem) {
  await ElMessageBox.confirm(`确定删除帖子「${row.title}」吗？`, '删除帖子', { type: 'warning' })
  try {
    unwrapApi(await deleteMyForumPostApi(row.id))
    ElMessage.success('帖子已删除')
    await fetchPosts()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '删除失败'))
  }
}

async function handleDeleteReply(row: MyForumReplyItem) {
  await ElMessageBox.confirm('确定删除该回复吗？', '删除回复', { type: 'warning' })
  try {
    unwrapApi(await deleteMyForumReplyApi(row.id))
    ElMessage.success('回复已删除')
    await fetchReplies()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '删除失败'))
  }
}

watch(activeTab, handleTabChange)

onMounted(fetchPosts)
</script>

<template>
  <PageShell
    title="我的发言"
    description="管理你在论坛发布的帖子与回复"
    :loading="loading"
    :error="loadError"
    @retry="fetchCurrentTab"
  >
    <el-tabs v-model="activeTab" class="page-tabs">
      <el-tab-pane label="我的帖子" name="posts" />
      <el-tab-pane label="我的回复" name="replies" />
    </el-tabs>

    <el-table v-if="activeTab === 'posts'" :data="posts" border stripe>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="boardName" label="板块" width="120" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
      <el-table-column label="浏览/回复/点赞" width="130" align="center">
        <template #default="{ row }">
          {{ row.viewCount ?? 0 }} / {{ row.replyCount ?? 0 }} / {{ row.likeCount ?? 0 }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="postStatusType(row.status)" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="center">
        <template #default="{ row }">
          <el-button type="danger" link @click="handleDeletePost(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else :data="replies" border stripe>
      <el-table-column prop="postTitle" label="所属帖子" min-width="160" show-overflow-tooltip />
      <el-table-column prop="content" label="回复内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="点赞" width="70" align="center">
        <template #default="{ row }">{{ row.likeCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="回复时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="center">
        <template #default="{ row }">
          <el-button type="danger" link @click="handleDeleteReply(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && activeTab === 'posts' && posts.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无帖子"
    />
    <el-empty
      v-if="!loading && activeTab === 'replies' && replies.length === 0"
      class="page-empty"
      :image-size="80"
      description="暂无回复"
    />

    <div v-if="total > 0" class="page-pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </PageShell>
</template>
