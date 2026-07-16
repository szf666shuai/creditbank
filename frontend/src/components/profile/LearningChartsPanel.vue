<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { listLearningStatsApi, type LearningStatDaily } from '@/api/profile'
import type { LearningArchiveItem, LearningAchievementItem } from '@/api/profile-learning'
import { getErrorMessage, unwrapApi } from '@/utils/api'

const props = withDefaults(
  defineProps<{
    embedded?: boolean
    archives?: LearningArchiveItem[]
    achievements?: LearningAchievementItem[]
  }>(),
  { embedded: false, archives: () => [], achievements: () => [] },
)

const loading = ref(false)
const loadError = ref<string | null>(null)
const stats = ref<LearningStatDaily[]>([])
const dateRange = ref<[string, string]>(defaultDateRange())

const lineChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()
const radarChartRef = ref<HTMLDivElement>()
let lineChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let radarChart: echarts.ECharts | null = null

function defaultDateRange(): [string, string] {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 29)
  return [toDateStr(start), toDateStr(end)]
}

function toDateStr(d: Date) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const summary = computed(() => {
  let totalMinutes = 0
  let totalCourses = 0
  let totalCredit = 0
  let activeDays = 0
  for (const item of stats.value) {
    const minutes = item.studyMinutes ?? 0
    totalMinutes += minutes
    totalCourses += item.coursesCompleted ?? 0
    totalCredit += Number(item.creditEarned ?? 0)
    if (minutes > 0) activeDays++
  }
  return {
    totalHours: (totalMinutes / 60).toFixed(1),
    totalCourses,
    totalCredit: totalCredit.toFixed(1),
    days: stats.value.length,
    activeDays,
  }
})

const archiveSummary = computed(() => {
  const completed = props.archives.filter((item) => item.status === 1)
  const totalCredit = completed.reduce((sum, item) => sum + Number(item.creditEarned || 0), 0)
  const verified = props.achievements.filter((item) => item.verifyStatus === 1).length
  const redeemable = props.achievements.reduce((sum, item) => sum + Number(item.creditValue || 0), 0)
  return {
    completedCount: completed.length,
    totalCredit,
    achievementCount: props.achievements.length,
    verifiedCount: verified,
    redeemableCredit: redeemable,
  }
})

const radarDimensions = computed(() => {
  const archive = archiveSummary.value
  const values = [
    archive.completedCount,
    archive.totalCredit,
    archive.achievementCount,
    archive.verifiedCount,
    archive.redeemableCredit,
    summary.value.activeDays,
  ]
  const labels = ['完成课程', '档案秩点', '学习成果', '成果认证', '可兑秩点', '活跃天数']
  const indicators = labels.map((name, index) => ({
    name,
    max: Math.max(5, Math.ceil(values[index] * 1.25) || 5),
  }))
  return { indicators, values }
})

const topCourseCredits = computed(() => {
  return [...props.archives]
    .filter((item) => item.status === 1)
    .sort((a, b) => Number(b.creditEarned || 0) - Number(a.creditEarned || 0))
    .slice(0, 6)
})

async function fetchStats() {
  loading.value = true
  loadError.value = null
  try {
    const [startDate, endDate] = dateRange.value
    stats.value = unwrapApi(await listLearningStatsApi({ startDate, endDate }))
    await nextTick()
    renderCharts()
  } catch (e) {
    loadError.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
}

function renderLineChart() {
  if (!lineChartRef.value) return
  if (!lineChart) lineChart = echarts.init(lineChartRef.value)

  const dates = stats.value.map((item) => item.statDate)
  const studyMinutes = stats.value.map((item) => item.studyMinutes ?? 0)
  const axisLabel = { color: '#94a3b8' }
  const axisLine = { lineStyle: { color: 'rgba(125, 211, 252, 0.25)' } }
  const splitLine = { lineStyle: { color: 'rgba(125, 211, 252, 0.12)' } }

  lineChart.setOption({
    title: { text: '学习时长趋势', left: 'center', textStyle: { fontSize: 14, fontWeight: 600, color: '#1a202c' } },
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 24, top: 48, bottom: 32 },
    xAxis: { type: 'category', data: dates, boundaryGap: false, axisLabel, axisLine },
    yAxis: { type: 'value', name: '分钟', nameTextStyle: { color: '#94a3b8' }, axisLabel, splitLine },
    series: [
      {
        name: '学习时长',
        type: 'line',
        smooth: true,
        data: studyMinutes,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#38bdf8' },
      },
    ],
  })
}

function renderBarChart() {
  if (!barChartRef.value) return
  if (!barChart) barChart = echarts.init(barChartRef.value)

  const axisLabel = { color: '#94a3b8' }
  const axisLine = { lineStyle: { color: 'rgba(125, 211, 252, 0.25)' } }
  const splitLine = { lineStyle: { color: 'rgba(125, 211, 252, 0.12)' } }
  const topCourses = topCourseCredits.value

  if (topCourses.length > 0) {
    barChart.setOption({
      title: { text: '课程秩点分布', left: 'center', textStyle: { fontSize: 14, fontWeight: 600, color: '#1a202c' } },
      tooltip: { trigger: 'axis' },
      grid: { left: 48, right: 24, top: 48, bottom: 56 },
      xAxis: {
        type: 'category',
        data: topCourses.map((item) => item.title),
        axisLabel: { ...axisLabel, interval: 0, rotate: topCourses.length > 4 ? 24 : 0, formatter: (v: string) => (v.length > 8 ? `${v.slice(0, 8)}…` : v) },
        axisLine,
      },
      yAxis: { type: 'value', name: '秩点', nameTextStyle: { color: '#94a3b8' }, axisLabel, splitLine },
      series: [
        {
          name: '获得秩点',
          type: 'bar',
          data: topCourses.map((item) => Number(item.creditEarned || 0)),
          itemStyle: { color: '#4ade80' },
          barMaxWidth: 36,
        },
      ],
    })
    return
  }

  const dates = stats.value.map((item) => item.statDate)
  const coursesCompleted = stats.value.map((item) => item.coursesCompleted ?? 0)
  const creditEarned = stats.value.map((item) => Number(item.creditEarned ?? 0))

  barChart.setOption({
    title: { text: '完课与秩点', left: 'center', textStyle: { fontSize: 14, fontWeight: 600, color: '#1a202c' } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['完成课程', '获得秩点'], top: 28, textStyle: { color: '#64748b' } },
    grid: { left: 48, right: 24, top: 64, bottom: 32 },
    xAxis: { type: 'category', data: dates, axisLabel, axisLine },
    yAxis: [
      { type: 'value', name: '门', minInterval: 1, nameTextStyle: { color: '#94a3b8' }, axisLabel, splitLine },
      { type: 'value', name: '秩点', minInterval: 0.5, nameTextStyle: { color: '#94a3b8' }, axisLabel, splitLine },
    ],
    series: [
      { name: '完成课程', type: 'bar', data: coursesCompleted, itemStyle: { color: '#4ade80' }, barMaxWidth: 28 },
      { name: '获得秩点', type: 'bar', yAxisIndex: 1, data: creditEarned, itemStyle: { color: '#38bdf8' }, barMaxWidth: 28 },
    ],
  })
}

function renderRadarChart() {
  if (!radarChartRef.value) return
  if (!radarChart) radarChart = echarts.init(radarChartRef.value)

  const { indicators, values } = radarDimensions.value

  radarChart.setOption({
    title: { text: '多维学习雷达', left: 'center', textStyle: { fontSize: 14, fontWeight: 600, color: '#1a202c' } },
    tooltip: { trigger: 'item' },
    radar: {
      center: ['50%', '58%'],
      radius: '62%',
      indicator: indicators,
      axisName: { color: '#64748b', fontSize: 11 },
      splitArea: { areaStyle: { color: ['rgba(56, 189, 248, 0.04)', 'rgba(56, 189, 248, 0.08)'] } },
      splitLine: { lineStyle: { color: 'rgba(125, 211, 252, 0.2)' } },
      axisLine: { lineStyle: { color: 'rgba(125, 211, 252, 0.25)' } },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            name: '综合学习',
            value: values,
            areaStyle: { opacity: 0.28, color: '#38bdf8' },
            lineStyle: { color: '#0ea5e9', width: 2 },
            itemStyle: { color: '#0284c7' },
          },
        ],
      },
    ],
  })
}

function renderCharts() {
  renderLineChart()
  renderBarChart()
  renderRadarChart()
}

function handleResize() {
  lineChart?.resize()
  barChart?.resize()
  radarChart?.resize()
}

function disposeCharts() {
  lineChart?.dispose()
  barChart?.dispose()
  radarChart?.dispose()
  lineChart = null
  barChart = null
  radarChart = null
}

watch(dateRange, fetchStats)
watch(
  () => [props.archives, props.achievements],
  async () => {
    await nextTick()
    renderBarChart()
    renderRadarChart()
  },
  { deep: true },
)

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  await fetchStats()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})

defineExpose({ fetchStats })
</script>

<template>
  <section class="learning-charts-panel" :class="{ embedded }">
    <div v-if="embedded" class="panel-head">
      <h2>学习数据总览</h2>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始"
        end-placeholder="结束"
        value-format="YYYY-MM-DD"
        :clearable="false"
        size="small"
      />
    </div>

    <div v-loading="loading">
      <el-alert v-if="loadError" type="error" :title="loadError" show-icon :closable="false" class="panel-error">
        <el-button link type="primary" @click="fetchStats">重试</el-button>
      </el-alert>

      <template v-else>
        <div class="page-summary-row stats-summary">
          <div class="page-summary-card">
            <div class="page-summary-value">{{ summary.totalHours }}</div>
            <div class="page-summary-label">学习时长（小时）</div>
          </div>
          <div class="page-summary-card">
            <div class="page-summary-value">{{ archiveSummary.completedCount }}</div>
            <div class="page-summary-label">完成课程（门）</div>
          </div>
          <div class="page-summary-card">
            <div class="page-summary-value">{{ archiveSummary.totalCredit.toFixed(1) }}</div>
            <div class="page-summary-label">档案秩点</div>
          </div>
          <div class="page-summary-card">
            <div class="page-summary-value">{{ archiveSummary.achievementCount }}</div>
            <div class="page-summary-label">学习成果（项）</div>
          </div>
        </div>

        <el-empty
          v-if="stats.length === 0 && props.archives.length === 0"
          class="page-empty"
          :image-size="64"
          description="暂无学习统计数据，完成课程后将自动更新图表"
        />

        <div v-else class="charts-wrap">
          <div ref="lineChartRef" class="chart-box chart-box--wide" />
          <div class="charts-row">
            <div ref="barChartRef" class="chart-box" />
            <div ref="radarChartRef" class="chart-box" />
          </div>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.learning-charts-panel {
  margin-bottom: 24px;
}

.learning-charts-panel.embedded {
  padding-bottom: 8px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.panel-head h2 {
  font-size: 18px;
  color: #1a202c;
}

.panel-error {
  margin-bottom: 12px;
}

.stats-summary {
  grid-template-columns: repeat(4, 1fr);
}

.charts-wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.chart-box {
  width: 100%;
  height: 300px;
}

.chart-box--wide {
  height: 320px;
}

@media (max-width: 900px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-summary {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-box,
  .chart-box--wide {
    height: 260px;
  }
}
</style>
