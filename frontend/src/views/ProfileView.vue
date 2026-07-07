<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { roleLabel } from '@/types/auth'

const authStore = useAuthStore()

const user = computed(() => authStore.userInfo)
const isEnterprise = computed(() => authStore.isEnterprise)
</script>

<template>
  <div class="profile-page">
    <div class="section-inner">
      <el-card v-if="user">
        <template #header>
          <span>个人中心</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
          <el-descriptions-item label="身份">
            <el-tag>{{ roleLabel(user.role) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user.email || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="user.orgName" label="所属企业">
            {{ user.orgName }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="quick-links">
          <el-button v-if="isEnterprise" type="primary" @click="$router.push('/enterprise')">
            进入企业中心
          </el-button>
          <el-button @click="$router.push('/archive')">学习档案</el-button>
          <el-button @click="$router.push('/credit')">学分流水</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 32px 16px 48px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.quick-links {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
