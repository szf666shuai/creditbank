<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  listMyActivityRegistrationsApi,
  type MyActivityRegistrationItem,
} from '@/api/profile-activity-registrations'
import { checkInActivityApi } from '@/api/activity-checkin'
import {
  listMyActivityInvitesApi,
  acceptActivityInviteApi,
  rejectActivityInviteApi,
  type ActivityInvitationItem,
} from '@/api/activity-invitation'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

type ActivityTab = 'registrations' | 'invitations'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const loadError = ref<string | null>(null)
const actingId = ref<number | null>(null)
const activeTab = ref<ActivityTab>('registrations')
const registrations = ref<MyActivityRegistrationItem[]>([])
const invitations = ref<ActivityInvitationItem[]>([])

function activityStatusTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === 3) return 'info'
  return 'danger'
}

function registrationStatusTagType(status: number) {
  if (status === 1) return 'success'
  return 'primary'
}

function inviteStatusTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

const pageDescription = computed(() =>
  activeTab.value === 'registrations'
    ? '在企业详情页主动报名的活动记录'
    : '企业定向发来的活动邀请，接受后自动完成报名',
)

function goBrowse() {
  router.push('/enterprise')
}

function goOrg(orgId: number) {
  router.push(`/enterprise/${orgId}`)
}

function syncTabFromRoute() {
  const tab = route.query.tab
  if (tab === 'invitations' || tab === 'registrations') {
    activeTab.value = tab
  }
}

async function handleCheckIn(row: MyActivityRegistrationItem) {
  actingId.value = row.id
  try {
    unwrapApi(await checkInActivityApi(row.activityId))
    ElMessage.success('签到成功')
    await fetchRegistrations()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '签到失败'))
  } finally {
    actingId.value = null
  }
}

function canCheckIn(row: MyActivityRegistrationItem) {
  return row.activityStatus === 2 && row.status === 0
}

async function fetchRegistrations() {
  registrations.value = unwrapApi(await listMyActivityRegistrationsApi())
}

async function fetchInvitations() {
  invitations.value = unwrapApi(await listMyActivityInvitesApi())
}

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    if (activeTab.value === 'registrations') {
      await fetchRegistrations()
    } else {
      await fetchInvitations()
    }
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleAccept(row: ActivityInvitationItem) {
  actingId.value = row.id
  try {
    unwrapApi(await acceptActivityInviteApi(row.id))
    ElMessage.success('已接受邀请并完成报名')
    await fetchInvitations()
    if (activeTab.value === 'invitations') {
      // refresh registrations count hint - user may switch tab
    }
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  } finally {
    actingId.value = null
  }
}

async function handleReject(row: ActivityInvitationItem) {
  await ElMessageBox.confirm('确定拒绝该活动邀请吗？', '拒绝邀请', { type: 'warning' })
  actingId.value = row.id
  try {
    unwrapApi(await rejectActivityInviteApi(row.id))
    ElMessage.success('已拒绝活动邀请')
    await fetchInvitations()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  } finally {
    actingId.value = null
  }
}

watch(activeTab, (tab) => {
  router.replace({ path: '/profile/activities', query: { tab } })
  fetchData()
})

watch(
  () => route.query.tab,
  () => syncTabFromRoute(),
)

onMounted(() => {
  syncTabFromRoute()
  fetchData()
})
</script>

<template>
  <PageShell
    title="我的活动"
    :description="pageDescription"
    :loading="loading"
    :error="loadError"
    @retry="fetchData"
  >
    <template #actions>
      <el-button type="primary" @click="goBrowse">去企业中心报名</el-button>
    </template>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="活动参与两种方式：① 企业详情页自行报名；② 接受企业定向邀请。"
      class="tip"
    />

    <el-tabs v-model="activeTab" class="page-tabs">
      <el-tab-pane label="活动报名" name="registrations" />
      <el-tab-pane label="活动邀请" name="invitations" />
    </el-tabs>

    <template v-if="activeTab === 'registrations'">
      <el-table :data="registrations" border stripe>
        <el-table-column prop="activityTitle" label="活动" min-width="160" />
        <el-table-column label="企业" width="140">
          <template #default="{ row }">
            <el-button v-if="row.orgId" link type="primary" @click="goOrg(row.orgId)">
              {{ row.orgName || `企业#${row.orgId}` }}
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" min-width="220">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="120" show-overflow-tooltip />
        <el-table-column label="活动状态" width="100">
          <template #default="{ row }">
            <el-tag :type="activityStatusTagType(row.activityStatus)" size="small">
              {{ row.activityStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名状态" width="100">
          <template #default="{ row }">
            <el-tag :type="registrationStatusTagType(row.status)" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canCheckIn(row)"
              link
              type="success"
              :loading="actingId === row.id"
              @click="handleCheckIn(row)"
            >
              签到
            </el-button>
            <span v-else-if="row.status === 1" class="page-text-muted">已签到</span>
            <span v-else class="page-text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && registrations.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无报名记录"
      >
        <el-button type="primary" @click="goBrowse">浏览加盟企业</el-button>
      </el-empty>
    </template>

    <template v-else>
      <el-table :data="invitations" border stripe>
        <el-table-column prop="orgName" label="企业" width="140" />
        <el-table-column prop="activityTitle" label="活动" min-width="160" />
        <el-table-column label="活动时间" min-width="220">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="inviteStatusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button
                link
                type="success"
                :loading="actingId === row.id"
                @click="handleAccept(row)"
              >
                接受
              </el-button>
              <el-button
                link
                type="danger"
                :loading="actingId === row.id"
                @click="handleReject(row)"
              >
                拒绝
              </el-button>
            </template>
            <span v-else class="page-text-muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && invitations.length === 0"
        class="page-empty"
        :image-size="80"
        description="暂无企业定向邀请"
      />
    </template>
  </PageShell>
</template>

<style scoped>
.tip {
  margin-bottom: 16px;
}
</style>
