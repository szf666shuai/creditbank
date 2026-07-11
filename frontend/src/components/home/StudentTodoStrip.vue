<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listMyInterviewInvitesApi, type InterviewInvitationItem } from '@/api/interview'
import {
  listMyActivityRegistrationsApi,
  type MyActivityRegistrationItem,
} from '@/api/profile-activity-registrations'
import { useMessageStore } from '@/stores/message'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const router = useRouter()
const messageStore = useMessageStore()

const loading = ref(true)
const interviews = ref<InterviewInvitationItem[]>([])
const activities = ref<MyActivityRegistrationItem[]>([])

const pendingInterviews = computed(() =>
  interviews.value.filter((item) => item.status === 0).slice(0, 3),
)

const checkinActivities = computed(() =>
  activities.value
    .filter((item) => item.activityStatus === 2 && item.status === 0)
    .slice(0, 3),
)

const unreadCount = computed(() => messageStore.unreadCount)

const hasTodos = computed(
  () =>
    pendingInterviews.value.length > 0
    || checkinActivities.value.length > 0
    || unreadCount.value > 0,
)

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  try {
    const [interviewRes, activityRes] = await Promise.all([
      listMyInterviewInvitesApi(),
      listMyActivityRegistrationsApi(),
      messageStore.refreshUnreadCount(),
    ])
    interviews.value = unwrapApi(interviewRes)
    activities.value = unwrapApi(activityRes)
  } catch (e) {
    console.warn(getErrorMessage(e, '待办加载失败'))
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <section v-if="loading || hasTodos" class="todo-strip">
    <div class="strip-head">
      <h2>待办提醒</h2>
      <span>需要留意的学习相关事项</span>
    </div>

    <el-skeleton v-if="loading" :rows="2" animated />

    <div v-else class="todo-grid">
      <button
        v-for="item in pendingInterviews"
        :key="`interview-${item.id}`"
        type="button"
        class="todo-card todo-card--interview"
        @click="go('/profile/interviews')"
      >
        <span class="todo-tag">面试邀请</span>
        <strong>{{ item.orgName || '企业' }} · {{ item.jobTitle || '岗位待定' }}</strong>
        <p>{{ formatTime(item.inviteTime) }} · {{ item.location || '地点待定' }}</p>
      </button>

      <button
        v-for="item in checkinActivities"
        :key="`activity-${item.id}`"
        type="button"
        class="todo-card todo-card--activity"
        @click="go('/profile/activities')"
      >
        <span class="todo-tag">活动签到</span>
        <strong>{{ item.activityTitle }}</strong>
        <p>{{ item.orgName || '企业活动' }} · 进行中可签到</p>
      </button>

      <button
        v-if="unreadCount > 0"
        type="button"
        class="todo-card todo-card--message"
        @click="go('/profile/messages')"
      >
        <span class="todo-tag">未读消息</span>
        <strong>{{ unreadCount }} 条消息待查看</strong>
        <p>面试回复、系统通知与活动消息</p>
      </button>
    </div>
  </section>
</template>

<style scoped>
.todo-strip {
  padding: 20px 22px;
  border-radius: 18px;
  background: var(--role-surface, rgba(6, 22, 38, 0.58));
  border: 1px solid var(--role-border, rgba(0, 161, 214, 0.28));
  box-shadow: var(--role-shadow, 0 12px 40px rgba(0, 0, 0, 0.35));
  backdrop-filter: blur(12px);
}

.strip-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 14px;
}

.strip-head h2 {
  margin: 0;
  font-size: 18px;
  color: var(--role-primary-dark, #67e8f9);
}

.strip-head span {
  font-size: 13px;
  color: var(--role-text-muted, #8ec8de);
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.todo-card {
  display: grid;
  gap: 6px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid transparent;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  background: var(--role-surface-card, rgba(10, 36, 58, 0.68));
}

.todo-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
}

.todo-card--interview {
  border-color: rgba(245, 158, 11, 0.28);
}

.todo-card--activity {
  border-color: rgba(34, 197, 94, 0.28);
}

.todo-card--activity .todo-tag {
  color: #4ade80;
}

.todo-card--message {
  border-color: rgba(124, 58, 237, 0.28);
}

.todo-card--message .todo-tag {
  color: #c4b5fd;
}

.todo-tag {
  font-size: 12px;
  color: #fbbf24;
  font-weight: 600;
}

.todo-card strong {
  font-size: 14px;
  color: var(--role-text, #e8f8ff);
  line-height: 1.4;
}

.todo-card p {
  margin: 0;
  font-size: 12px;
  color: var(--role-text-muted, #8ec8de);
  line-height: 1.5;
}

@media (max-width: 900px) {
  .todo-grid {
    grid-template-columns: 1fr;
  }
}
</style>
