<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import QRCode from 'qrcode'
import { Download, Medal } from '@element-plus/icons-vue'
import type { LearningCertificate } from '@/api/learning'

const props = defineProps<{
  certificate: LearningCertificate
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const qrDataUrl = ref('')

const issuedDate = computed(() => {
  if (!props.certificate.issuedAt) return ''
  return props.certificate.issuedAt.slice(0, 10)
})

async function renderQr() {
  const qrContent = props.certificate.qrContent.startsWith('/')
    ? `${window.location.origin}${props.certificate.qrContent}`
    : props.certificate.qrContent
  qrDataUrl.value = await QRCode.toDataURL(qrContent, {
    width: 180,
    margin: 1,
    color: {
      dark: '#1f2937',
      light: '#ffffff',
    },
  })
  await nextTick()
  drawCertificate()
}

function drawCertificate() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  const width = 960
  const height = 640
  canvas.width = width
  canvas.height = height

  ctx.fillStyle = '#f8fbff'
  ctx.fillRect(0, 0, width, height)
  ctx.strokeStyle = '#2094f3'
  ctx.lineWidth = 8
  ctx.strokeRect(28, 28, width - 56, height - 56)
  ctx.strokeStyle = '#b9dcfb'
  ctx.lineWidth = 2
  ctx.strokeRect(52, 52, width - 104, height - 104)

  ctx.fillStyle = '#0f172a'
  ctx.font = '700 42px Microsoft YaHei, Arial'
  ctx.textAlign = 'center'
  ctx.fillText('学习证书', width / 2, 126)

  ctx.fillStyle = '#64748b'
  ctx.font = '18px Microsoft YaHei, Arial'
  ctx.fillText('Credit Bank Learning Certificate', width / 2, 158)

  ctx.fillStyle = '#111827'
  ctx.font = '700 30px Microsoft YaHei, Arial'
  wrapText(ctx, props.certificate.title, width / 2, 238, 720, 42)

  ctx.fillStyle = '#334155'
  ctx.font = '18px Microsoft YaHei, Arial'
  ctx.fillText(`证书编号：${props.certificate.certNo}`, width / 2, 328)
  ctx.fillText(`签发日期：${issuedDate.value}`, width / 2, 362)
  ctx.fillText(`校验状态：${props.certificate.verifyStatusName}`, width / 2, 396)

  ctx.textAlign = 'left'
  ctx.fillStyle = '#64748b'
  ctx.font = '15px Consolas, Microsoft YaHei, monospace'
  wrapText(ctx, `链上哈希：${props.certificate.blockchainHash}`, 110, 488, 560, 24)

  if (qrDataUrl.value) {
    const qr = new Image()
    qr.onload = () => {
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(710, 382, 180, 180)
      ctx.drawImage(qr, 710, 382, 180, 180)
      ctx.fillStyle = '#64748b'
      ctx.font = '14px Microsoft YaHei, Arial'
      ctx.textAlign = 'center'
      ctx.fillText('扫码校验证书', 800, 588)
    }
    qr.src = qrDataUrl.value
  }
}

function wrapText(
  ctx: CanvasRenderingContext2D,
  text: string,
  x: number,
  y: number,
  maxWidth: number,
  lineHeight: number,
) {
  let line = ''
  const chars = text.split('')
  for (const char of chars) {
    const testLine = line + char
    if (ctx.measureText(testLine).width > maxWidth && line) {
      ctx.fillText(line, x, y)
      line = char
      y += lineHeight
    } else {
      line = testLine
    }
  }
  ctx.fillText(line, x, y)
}

function downloadCertificate() {
  const canvas = canvasRef.value
  if (!canvas) return
  const link = document.createElement('a')
  link.download = `${props.certificate.certNo}.png`
  link.href = canvas.toDataURL('image/png')
  link.click()
}

watch(() => props.certificate, renderQr, { immediate: true, deep: true })
</script>

<template>
  <div class="certificate-preview">
    <canvas ref="canvasRef" aria-label="学习证书预览" />
    <div class="preview-actions">
      <el-button type="primary" :icon="Download" @click="downloadCertificate">
        下载证书图片
      </el-button>
      <el-tag type="success">
        <el-icon><Medal /></el-icon>
        {{ certificate.verifyStatusName }}
      </el-tag>
    </div>
  </div>
</template>

<style scoped>
.certificate-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

canvas {
  width: 100%;
  aspect-ratio: 3 / 2;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: #f8fbff;
}

.preview-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.preview-actions .el-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
