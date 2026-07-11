<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { getRoleTheme, type RoleThemeVariant } from '@/config/role-theme'

const props = withDefaults(
  defineProps<{
    variant?: RoleThemeVariant
  }>(),
  {
    variant: 'default',
  },
)

interface Particle {
  x: number
  y: number
  baseX: number
  baseY: number
  size: number
  opacity: number
  speed: number
}

interface ExplosionParticle {
  x: number
  y: number
  vx: number
  vy: number
  life: number
  maxLife: number
  color: string
  size: number
}

const canvasRef = ref<HTMLCanvasElement | null>(null)

const PARTICLE_COUNT = 300
const SECTOR_COUNT = 28
const MOUSE_RADIUS = 260
const ATTRACT_STRENGTH = 0.028
const RETURN_STRENGTH = 0.018
const MOUSE_LERP = 0.06
const MAX_CHAIN = 6
const MIN_SEG = 24
const SLOW_SPEED = 3.5
const EFFECT_FADE_IN = 0.008
const EFFECT_FADE_OUT = 0.03
const MIN_EFFECT = 0.25

let ctx: CanvasRenderingContext2D | null = null
let particles: Particle[] = []
let explosions: ExplosionParticle[] = []
let rawMouse = { x: -9999, y: -9999, active: false }
let smoothMouse = { x: -9999, y: -9999 }
let lastRaw = { x: -9999, y: -9999 }
let effectStrength = 0
let animId = 0
let width = 0
let height = 0

function rgbaAlpha(color: string, alpha: number) {
  const match = color.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/)
  if (!match) return color
  return `rgba(${match[1]}, ${match[2]}, ${match[3]}, ${alpha})`
}

function currentTheme() {
  return getRoleTheme(props.variant)
}

function resize() {
  const canvas = canvasRef.value
  if (!canvas) return
  width = window.innerWidth
  height = window.innerHeight
  canvas.width = width
  canvas.height = height
  initParticles()
}

function initParticles() {
  particles = Array.from({ length: PARTICLE_COUNT }, () => {
    const x = Math.random() * width
    const y = Math.random() * height
    return {
      x,
      y,
      baseX: x,
      baseY: y,
      size: Math.random() * 2.5 + 1.2,
      opacity: Math.random() * 0.35 + 0.55,
      speed: Math.random() * 0.4 + 0.1,
    }
  })
}

function onMouseMove(e: MouseEvent) {
  rawMouse.x = e.clientX
  rawMouse.y = e.clientY
  rawMouse.active = true
}

function onMouseLeave() {
  rawMouse.active = false
}

function onClick(e: MouseEvent) {
  const colors = currentTheme().explosionColors
  const count = 50 + Math.floor(Math.random() * 30)
  for (let i = 0; i < count; i++) {
    const angle = (Math.PI * 2 * i) / count + Math.random() * 0.4
    const speed = Math.random() * 7 + 2
    explosions.push({
      x: e.clientX,
      y: e.clientY,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: 1,
      maxLife: Math.random() * 0.6 + 0.5,
      color: colors[Math.floor(Math.random() * colors.length)],
      size: Math.random() * 5 + 2,
    })
  }
}

function distToSmooth(x: number, y: number) {
  const dx = smoothMouse.x - x
  const dy = smoothMouse.y - y
  return Math.sqrt(dx * dx + dy * dy)
}

function updateMouseState() {
  if (!rawMouse.active) {
    effectStrength = Math.max(0, effectStrength - EFFECT_FADE_OUT * 2)
    return
  }

  if (smoothMouse.x < -9000) {
    smoothMouse.x = rawMouse.x
    smoothMouse.y = rawMouse.y
    lastRaw.x = rawMouse.x
    lastRaw.y = rawMouse.y
  }

  smoothMouse.x += (rawMouse.x - smoothMouse.x) * MOUSE_LERP
  smoothMouse.y += (rawMouse.y - smoothMouse.y) * MOUSE_LERP

  const speed = Math.hypot(rawMouse.x - lastRaw.x, rawMouse.y - lastRaw.y)
  lastRaw.x = rawMouse.x
  lastRaw.y = rawMouse.y

  if (speed < SLOW_SPEED) {
    effectStrength = Math.min(1, effectStrength + EFFECT_FADE_IN)
  } else {
    effectStrength = Math.max(0, effectStrength - EFFECT_FADE_OUT)
  }
}

function updateParticles() {
  const attractScale = effectStrength

  for (const p of particles) {
    p.baseX += Math.sin(p.baseY * 0.007) * p.speed * 0.3
    p.baseY += Math.cos(p.baseX * 0.007) * p.speed * 0.3
    if (p.baseX < 0) p.baseX = width
    if (p.baseX > width) p.baseX = 0
    if (p.baseY < 0) p.baseY = height
    if (p.baseY > height) p.baseY = 0

    if (rawMouse.active && attractScale > 0.05) {
      const dx = smoothMouse.x - p.x
      const dy = smoothMouse.y - p.y
      const dist = Math.sqrt(dx * dx + dy * dy)

      if (dist < MOUSE_RADIUS && dist > 1) {
        const force = (1 - dist / MOUSE_RADIUS) * ATTRACT_STRENGTH * attractScale
        p.x += dx * force
        p.y += dy * force
      } else {
        p.x += (p.baseX - p.x) * RETURN_STRENGTH
        p.y += (p.baseY - p.y) * RETURN_STRENGTH
      }
    } else {
      p.x += (p.baseX - p.x) * RETURN_STRENGTH
      p.y += (p.baseY - p.y) * RETURN_STRENGTH
    }
  }
}

function updateExplosions() {
  explosions = explosions.filter((ep) => {
    ep.x += ep.vx
    ep.y += ep.vy
    ep.vx *= 0.96
    ep.vy *= 0.96
    ep.vy += 0.05
    ep.life -= 0.016
    return ep.life > 0
  })
}

function drawParticlePolylines() {
  if (!ctx || effectStrength < MIN_EFFECT) return

  const theme = currentTheme()
  const alpha = effectStrength
  const sectorSize = (Math.PI * 2) / SECTOR_COUNT
  const buckets: Particle[][] = Array.from({ length: SECTOR_COUNT }, () => [])

  for (const p of particles) {
    const dist = distToSmooth(p.x, p.y)
    if (dist >= MOUSE_RADIUS * 0.85) continue

    const angle = Math.atan2(p.y - smoothMouse.y, p.x - smoothMouse.x)
    let idx = Math.floor((angle + Math.PI) / sectorSize)
    if (idx >= SECTOR_COUNT) idx = SECTOR_COUNT - 1
    buckets[idx].push(p)
  }

  for (const bucket of buckets) {
    if (bucket.length < 2) continue

    bucket.sort((a, b) => distToSmooth(a.x, a.y) - distToSmooth(b.x, b.y))

    const chain: { x: number; y: number }[] = [{ x: smoothMouse.x, y: smoothMouse.y }]
    for (const p of bucket) {
      const last = chain[chain.length - 1]
      const segDist = Math.hypot(p.x - last.x, p.y - last.y)
      if (segDist >= MIN_SEG && chain.length <= MAX_CHAIN) {
        chain.push({ x: p.x, y: p.y })
      }
    }

    if (chain.length < 3) continue

    const tail = chain[chain.length - 1]
    const gradient = ctx.createLinearGradient(
      smoothMouse.x, smoothMouse.y, tail.x, tail.y,
    )
    gradient.addColorStop(0, rgbaAlpha(theme.particleLine[0], 0.85 * alpha))
    gradient.addColorStop(0.5, rgbaAlpha(theme.particleLine[1], 0.45 * alpha))
    gradient.addColorStop(1, rgbaAlpha(theme.particleLine[2], 0.05 * alpha))

    ctx.beginPath()
    ctx.moveTo(chain[0].x, chain[0].y)
    for (let i = 1; i < chain.length; i++) {
      ctx.lineTo(chain[i].x, chain[i].y)
    }
    ctx.strokeStyle = gradient
    ctx.lineWidth = 1.2
    ctx.lineCap = 'round'
    ctx.lineJoin = 'round'
    ctx.stroke()
  }

  const glowColor = theme.particleGlow
  const glow = ctx.createRadialGradient(
    smoothMouse.x, smoothMouse.y, 0,
    smoothMouse.x, smoothMouse.y, 60,
  )
  glow.addColorStop(0, rgbaAlpha(glowColor, 0.25 * alpha))
  glow.addColorStop(1, rgbaAlpha(glowColor, 0))
  ctx.fillStyle = glow
  ctx.beginPath()
  ctx.arc(smoothMouse.x, smoothMouse.y, 60, 0, Math.PI * 2)
  ctx.fill()
}

function drawBackground() {
  if (!ctx) return
  const theme = currentTheme()
  const grad = ctx.createLinearGradient(0, 0, width, height)
  grad.addColorStop(0, theme.particleBg[0])
  grad.addColorStop(0.5, theme.particleBg[1])
  grad.addColorStop(1, theme.particleBg[2])
  ctx.fillStyle = grad
  ctx.fillRect(0, 0, width, height)
}

function drawParticles() {
  if (!ctx) return
  const theme = currentTheme()

  for (const p of particles) {
    const dist = rawMouse.active ? distToSmooth(p.x, p.y) : MOUSE_RADIUS
    const attracted = rawMouse.active && dist < MOUSE_RADIUS && effectStrength > 0.1
    const boost = attracted
      ? (1 - dist / MOUSE_RADIUS) * 0.5 * effectStrength
      : 0

    ctx.beginPath()
    ctx.arc(p.x, p.y, p.size + boost, 0, Math.PI * 2)

    if (attracted) {
      ctx.fillStyle = rgbaAlpha(theme.particleDotActive, Math.min(p.opacity * 0.6 + boost, 0.95))
      ctx.shadowColor = theme.particleGlow
      ctx.shadowBlur = 6 * effectStrength
    } else {
      ctx.fillStyle = rgbaAlpha(theme.particleDot, p.opacity * 0.75)
      ctx.shadowBlur = 0
    }
    ctx.fill()
    ctx.shadowBlur = 0
  }
}

function drawExplosions() {
  if (!ctx) return
  for (const ep of explosions) {
    const alpha = ep.life / ep.maxLife
    ctx.beginPath()
    ctx.arc(ep.x, ep.y, ep.size * alpha, 0, Math.PI * 2)
    ctx.fillStyle = ep.color
    ctx.globalAlpha = alpha
    ctx.shadowColor = ep.color
    ctx.shadowBlur = 10
    ctx.fill()
    ctx.shadowBlur = 0
    ctx.globalAlpha = 1
  }
}

function draw() {
  if (!ctx) return
  drawBackground()
  drawParticlePolylines()
  drawParticles()
  drawExplosions()
}

function animate() {
  updateMouseState()
  updateParticles()
  updateExplosions()
  draw()
  animId = requestAnimationFrame(animate)
}

watch(
  () => props.variant,
  () => {
    explosions = []
  },
)

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  resize()
  animate()

  window.addEventListener('resize', resize)
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseleave', onMouseLeave)
  window.addEventListener('click', onClick)
})

onUnmounted(() => {
  cancelAnimationFrame(animId)
  window.removeEventListener('resize', resize)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseleave', onMouseLeave)
  window.removeEventListener('click', onClick)
})
</script>

<template>
  <canvas ref="canvasRef" class="particle-canvas" aria-hidden="true" />
</template>

<style scoped>
.particle-canvas {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}
</style>
