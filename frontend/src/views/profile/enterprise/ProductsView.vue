<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/common/PageShell.vue'
import { fetchMallCategories, type MallCategory } from '@/api/mall'
import {
  createEnterpriseProductApi,
  listEnterpriseProductsApi,
  updateEnterpriseProductApi,
  type EnterpriseMallProduct,
} from '@/api/enterprise-mall'
import { getErrorMessage, unwrapApi } from '@/utils/api'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)
const products = ref<EnterpriseMallProduct[]>([])
const categories = ref<MallCategory[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const productTypeOptions = [
  { label: '实物商品', value: 1 },
  { label: '虚拟商品', value: 2 },
  { label: '课程兑换', value: 3 },
  { label: '服务权益', value: 4 },
]

const form = reactive({
  categoryId: undefined as number | undefined,
  name: '',
  description: '',
  coverUrl: '',
  productType: 1,
  priceCredit: 0,
  priceMoney: 0,
  stock: 99,
})

function approvalTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

function resetForm() {
  form.categoryId = categories.value[0]?.id
  form.name = ''
  form.description = ''
  form.coverUrl = ''
  form.productType = 1
  form.priceCredit = 0
  form.priceMoney = 0
  form.stock = 99
  editingId.value = null
}

async function fetchProducts() {
  loading.value = true
  loadError.value = null
  try {
    products.value = unwrapApi(await listEnterpriseProductsApi())
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    categories.value = unwrapApi(await fetchMallCategories())
    if (!form.categoryId && categories.value.length) {
      form.categoryId = categories.value[0].id
    }
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '分类加载失败'))
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: EnterpriseMallProduct) {
  if (row.approvalStatus === 1) {
    ElMessage.warning('已通过审核的商品需下架后再编辑')
    return
  }
  editingId.value = row.id
  form.categoryId = row.categoryId
  form.name = row.name
  form.description = row.description || ''
  form.coverUrl = row.coverUrl || ''
  form.productType = row.productType
  form.priceCredit = Number(row.priceCredit || 0)
  form.priceMoney = Number(row.priceMoney || 0)
  form.stock = row.stock
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.categoryId) {
    ElMessage.warning('请选择商品分类')
    return
  }
  if (!form.name.trim()) {
    ElMessage.warning('请填写商品名称')
    return
  }

  saving.value = true
  try {
    const payload = {
      categoryId: form.categoryId,
      name: form.name.trim(),
      description: form.description,
      coverUrl: form.coverUrl,
      productType: form.productType,
      priceCredit: form.priceCredit,
      priceMoney: form.priceMoney,
      stock: form.stock,
    }
    if (editingId.value) {
      unwrapApi(await updateEnterpriseProductApi(editingId.value, payload))
      ElMessage.success('商品已更新，等待平台审核')
    } else {
      unwrapApi(await createEnterpriseProductApi(payload))
      ElMessage.success('商品已提交，等待平台审核')
    }
    dialogVisible.value = false
    await fetchProducts()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await fetchCategories()
  await fetchProducts()
})
</script>

<template>
  <PageShell
    title="商城管理"
    description="发布秩点商城商品，提交后由平台管理员审核上架"
    :loading="loading"
    :error="loadError"
    @retry="fetchProducts"
  >
    <template #actions>
      <el-button type="primary" @click="openCreate">发布商品</el-button>
    </template>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="企业可挂商品到秩点商城，学员使用秩点兑换。商品需经平台审核通过后才会在商城展示。"
      class="tip"
    />

    <el-table :data="products" border stripe>
      <el-table-column prop="name" label="商品名称" min-width="160" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="productTypeName" label="类型" width="110" />
      <el-table-column label="秩点价" width="90">
        <template #default="{ row }">{{ Number(row.priceCredit || 0).toFixed(0) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :type="approvalTagType(row.approvalStatus)" size="small">
            {{ row.approvalStatusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewRemark" label="审核备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="提交时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && products.length === 0"
      class="page-empty"
      :image-size="80"
      description="尚未发布商品"
    >
      <el-button type="primary" @click="openCreate">发布第一件商品</el-button>
    </el-empty>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑商品' : '发布商品'"
      width="560px"
      destroy-on-close
    >
      <el-form label-width="96px">
        <el-form-item label="商品分类" required>
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称" required>
          <el-input v-model="form.name" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="商品类型" required>
          <el-select v-model="form.productType" style="width: 100%">
            <el-option
              v-for="item in productTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面链接">
          <el-input v-model="form.coverUrl" placeholder="可选，图片 URL" />
        </el-form-item>
        <el-form-item label="秩点价格" required>
          <el-input-number v-model="form.priceCredit" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="现金价格">
          <el-input-number v-model="form.priceMoney" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">提交审核</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<style scoped>
.tip {
  margin-bottom: 16px;
}
</style>
