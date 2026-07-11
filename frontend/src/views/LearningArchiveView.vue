<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Collection, Medal, Tickets } from '@element-plus/icons-vue'
import {
  fetchLearningArchives,
  fetchLearningCertificates,
  type LearningArchive,
  type LearningCertificate,
} from '@/api/learning'
import CertificatePreview from '@/components/learning/CertificatePreview.vue'

const loading = ref(false)
const archives = ref<LearningArchive[]>([])
const certificates = ref<LearningCertificate[]>([])

function formatCredit(value?: number) {
  return Number(value || 0).toFixed(2)
}

async function loadData() {
  loading.value = true
  try {
    const [archiveRes, certRes] = await Promise.all([
      fetchLearningArchives(50),
      fetchLearningCertificates(50),
    ])
    if (archiveRes.code !== 200 || !archiveRes.data) throw new Error(archiveRes.message || '加载学习档案失败')
    if (certRes.code !== 200 || !certRes.data) throw new Error(certRes.message || '加载学习证书失败')
    archives.value = archiveRes.data
    certificates.value = certRes.data
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载学习档案失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="archive-page">
    <div class="section-inner">
      <section class="page-head">
        <div>
          <p class="eyebrow">Learning Profile</p>
          <h1>学习档案</h1>
          <p class="subtitle">这里记录完成的学习资源、学习证书、奖励秩点和区块链校验信息，个人中心也会同步体现。</p>
        </div>
        <div class="head-metrics">
          <div>
            <strong>{{ archives.length }}</strong>
            <span>档案记录</span>
          </div>
          <div>
            <strong>{{ certificates.length }}</strong>
            <span>学习证书</span>
          </div>
        </div>
      </section>

      <el-skeleton v-if="loading" :rows="8" animated />
      <div v-else class="archive-layout">
        <section>
          <div class="section-title">
            <el-icon><Collection /></el-icon>
            <h2>学习记录</h2>
          </div>
          <el-empty v-if="!archives.length" description="暂无学习档案" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in archives"
              :key="item.id"
              :timestamp="item.endDate || item.createTime"
              placement="top"
            >
              <article class="archive-card">
                <div class="archive-main">
                  <el-tag size="small">{{ item.archiveTypeName }}</el-tag>
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.description }}</p>
                </div>
                <div class="archive-meta">
                  <span>{{ item.statusName }}</span>
                  <strong>+{{ formatCredit(item.creditEarned) }} 秩点</strong>
                </div>
              </article>
            </el-timeline-item>
          </el-timeline>
        </section>

        <aside>
          <div class="section-title">
            <el-icon><Medal /></el-icon>
            <h2>学习证书</h2>
          </div>
          <el-empty v-if="!certificates.length" description="暂无证书" />
          <div v-else class="certificate-list">
            <article v-for="cert in certificates" :key="cert.id" class="certificate-card">
              <div class="cert-head">
                <el-icon><Tickets /></el-icon>
                <div>
                  <h3>{{ cert.title }}</h3>
                  <span>{{ cert.issuedAt }}</span>
                </div>
              </div>
              <CertificatePreview :certificate="cert" />
              <dl>
                <dt>证书编号</dt>
                <dd>{{ cert.certNo }}</dd>
                <dt>校验状态</dt>
                <dd>{{ cert.verifyStatusName }}</dd>
                <dt>链上哈希</dt>
                <dd class="hash">{{ cert.blockchainHash }}</dd>
              </dl>
            </article>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
.archive-page {
  padding: 32px 16px 56px;
}

.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 24px;
}

.eyebrow {
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}

h1 {
  margin: 6px 0 8px;
  color: var(--color-text);
  font-size: 30px;
}

.subtitle {
  max-width: 680px;
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.head-metrics {
  display: flex;
  gap: 10px;
}

.head-metrics div {
  min-width: 104px;
  padding: 12px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.head-metrics strong {
  display: block;
  color: var(--color-text);
  font-size: 22px;
}

.head-metrics span {
  color: var(--color-text-muted);
  font-size: 12px;
}

.archive-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 22px;
  align-items: flex-start;
}

.section-title,
.cert-head,
.archive-card {
  display: flex;
}

.section-title {
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: var(--color-text);
}

.section-title h2 {
  margin: 0;
  font-size: 20px;
}

.archive-card,
.certificate-card {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 16px;
}

.archive-card {
  justify-content: space-between;
  gap: 18px;
}

.archive-main h3 {
  margin: 8px 0;
  color: var(--color-text);
  font-size: 17px;
}

.archive-main p {
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.archive-meta {
  min-width: 112px;
  text-align: right;
}

.archive-meta span {
  display: block;
  color: var(--color-text-muted);
  font-size: 12px;
}

.archive-meta strong {
  display: block;
  margin-top: 8px;
  color: #0f8f68;
}

.certificate-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.cert-head {
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.cert-head .el-icon {
  font-size: 30px;
  color: #d9931b;
}

.cert-head h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 16px;
  line-height: 1.4;
}

.cert-head span {
  color: var(--color-text-muted);
  font-size: 12px;
}

dl {
  margin: 0;
}

dt {
  color: var(--color-text-muted);
  font-size: 12px;
  margin-top: 8px;
}

dd {
  margin: 2px 0 0;
  color: var(--color-text);
  font-size: 13px;
}

.hash {
  word-break: break-all;
  font-family: Consolas, monospace;
}

@media (max-width: 920px) {
  .page-head {
    flex-direction: column;
    align-items: stretch;
  }

  .archive-layout {
    grid-template-columns: 1fr;
  }
}
</style>
