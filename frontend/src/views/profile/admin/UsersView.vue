<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import {
  USER_ROLE_OPTIONS,
  USER_STATUS_OPTIONS,
  createEnterpriseUserApi,
  listAdminOrganizationsApi,
  listAdminUsersApi,
  updateUserStatusApi,
  type AdminOrganization,
  type AdminUser,
} from '@/api/admin'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const records = ref<AdminUser[]>([])
const orgOptions = ref<AdminOrganization[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const actingId = ref<number | null>(null)
const dialogVisible = ref(false)
const creating = ref(false)

const filters = reactive({
  role: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: '',
})

const createForm = reactive({
  username: '',
  password: '',
  realName: '',
  orgId: undefined as number | undefined,
  phone: '',
  email: '',
})

async function fetchData() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(
      await listAdminUsersApi({
        page: page.value,
        pageSize: pageSize.value,
        role: filters.role,
        status: filters.status,
        keyword: filters.keyword || undefined,
      }),
    )
    records.value = data.records
    total.value = data.total
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadOrgOptions() {
  try {
    const data = unwrapApi(await listAdminOrganizationsApi({ page: 1, pageSize: 100, joinStatus: 1, status: 1 }))
    orgOptions.value = data.records
  } catch {
    orgOptions.value = []
  }
}

function openCreateDialog() {
  createForm.username = ''
  createForm.password = ''
  createForm.realName = ''
  createForm.orgId = undefined
  createForm.phone = ''
  createForm.email = ''
  dialogVisible.value = true
}

async function handleCreate() {
  if (!createForm.username.trim() || !createForm.password.trim() || !createForm.realName.trim() || !createForm.orgId) {
    ElMessage.warning('请填写完整的企业用户信息')
    return
  }
  creating.value = true
  try {
    unwrapApi(await createEnterpriseUserApi({ ...createForm, orgId: createForm.orgId }))
    ElMessage.success('企业用户已创建')
    dialogVisible.value = false
    await fetchData()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '创建失败'))
  } finally {
    creating.value = false
  }
}

async function handleToggleStatus(row: AdminUser) {
  if (row.role === 2) return
  const nextStatus = row.status === 1 ? 0 : 1
  const label = nextStatus === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确定${label}用户「${row.username}」吗？`, '用户状态', { type: 'warning' })
  actingId.value = row.id
  try {
    unwrapApi(await updateUserStatusApi(row.id, nextStatus))
    ElMessage.success(`用户已${label}`)
    await fetchData()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '操作失败'))
  } finally {
    actingId.value = null
  }
}

onMounted(async () => {
  await Promise.all([fetchData(), loadOrgOptions()])
})
</script>

<template>
  <PageShell title="用户管理" description="查看平台用户，启停账号，创建企业用户" :loading="loading" :error="loadError" @retry="fetchData">
    <div class="page-toolbar">
      <el-select v-model="filters.role" placeholder="角色" clearable style="width: 120px">
        <el-option v-for="item in USER_ROLE_OPTIONS.filter((o) => o.value !== undefined)" :key="String(item.value)" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px">
        <el-option v-for="item in USER_STATUS_OPTIONS.filter((o) => o.value !== undefined)" :key="String(item.value)" :label="item.label" :value="item.value" />
      </el-select>
      <el-input v-model="filters.keyword" placeholder="用户名/姓名/手机" clearable style="width: 200px" />
      <el-button type="primary" @click="() => { page = 1; fetchData() }">查询</el-button>
      <el-button type="success" @click="openCreateDialog">创建企业用户</el-button>
    </div>

    <el-table :data="records" border stripe>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" show-overflow-tooltip />
      <el-table-column prop="roleName" label="角色" width="100" />
      <el-table-column prop="orgName" label="所属机构" min-width="140" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机" width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.role !== 2" type="primary" link :loading="actingId === row.id" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > 0" class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" background @current-change="fetchData" @size-change="() => { page = 1; fetchData() }" />
    </div>
  </PageShell>

  <el-dialog v-model="dialogVisible" title="创建企业用户" width="520px" destroy-on-close>
    <el-form label-width="90px">
      <el-form-item label="用户名" required><el-input v-model="createForm.username" /></el-form-item>
      <el-form-item label="密码" required><el-input v-model="createForm.password" type="password" show-password /></el-form-item>
      <el-form-item label="姓名" required><el-input v-model="createForm.realName" /></el-form-item>
      <el-form-item label="所属机构" required>
        <el-select v-model="createForm.orgId" placeholder="选择已加盟机构" style="width: 100%">
          <el-option v-for="org in orgOptions" :key="org.id" :label="org.name" :value="org.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="手机"><el-input v-model="createForm.phone" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="createForm.email" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
    </template>
  </el-dialog>
</template>
