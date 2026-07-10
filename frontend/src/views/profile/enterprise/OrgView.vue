<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import { getMyOrgApi, updateMyOrgApi, type OrgProfile } from '@/api/enterprise-org'
import { getErrorMessage, unwrapApi } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)

const form = reactive({
  name: '',
  intro: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  website: '',
  logo: '',
})

const meta = ref<Pick<OrgProfile, 'id' | 'code' | 'typeName'> | null>(null)

async function loadOrg() {
  loading.value = true
  loadError.value = null
  try {
    const data = unwrapApi(await getMyOrgApi())
    meta.value = { id: data.id, code: data.code, typeName: data.typeName }
    form.name = data.name || ''
    form.intro = data.intro || ''
    form.contact = data.contact || ''
    form.phone = data.phone || ''
    form.email = data.email || ''
    form.address = data.address || ''
    form.website = data.website || ''
    form.logo = data.logo || ''
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!form.name.trim()) {
    ElMessage.warning('请填写机构名称')
    return
  }
  saving.value = true
  try {
    unwrapApi(await updateMyOrgApi({ ...form, name: form.name.trim() }))
    ElMessage.success('机构信息已保存')
    await loadOrg()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}

function goPublicPage() {
  if (meta.value?.id) {
    router.push(`/enterprise/${meta.value.id}`)
  }
}

onMounted(loadOrg)
</script>

<template>
  <PageShell
    title="机构信息"
    description="维护企业简介、联系方式等，将展示在加盟企业详情页"
    :loading="loading"
    :error="loadError"
    @retry="loadOrg"
  >
    <template #actions>
      <el-button v-if="meta?.id" @click="goPublicPage">预览企业主页</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>

    <el-descriptions v-if="meta" :column="2" border class="meta-bar">
      <el-descriptions-item label="机构编码">{{ meta.code }}</el-descriptions-item>
      <el-descriptions-item label="机构类型">{{ meta.typeName }}</el-descriptions-item>
    </el-descriptions>

    <el-form label-width="96px" class="org-form">
      <el-form-item label="机构名称" required>
        <el-input v-model="form.name" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="机构简介">
        <el-input v-model="form.intro" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="企业介绍、主营业务等" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-form-item label="联系人">
            <el-input v-model="form.contact" maxlength="50" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="联系电话">
            <el-input v-model="form.phone" maxlength="20" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="联系邮箱">
        <el-input v-model="form.email" maxlength="100" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="form.address" maxlength="200" />
      </el-form-item>
      <el-form-item label="官网">
        <el-input v-model="form.website" maxlength="200" placeholder="https://" />
      </el-form-item>
      <el-form-item label="Logo 链接">
        <el-input v-model="form.logo" maxlength="255" placeholder="图片 URL" />
      </el-form-item>
    </el-form>
  </PageShell>
</template>

<style scoped>
.meta-bar {
  margin-bottom: 20px;
}

.org-form {
  max-width: 720px;
}
</style>
