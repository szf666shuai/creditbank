<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { listLearningStatsApi, type LearningStatDaily } from '@/api/profile'
import { getErrorMessage, unwrapApi } from '@/utils/api'

const props = withDefaults(
  defineProps<{
    embedded?: boolean
  }>(),
  { embedded: false },
)

const loading = ref(false)
const loadError = ref<string | null>(null)
const stats = ref<LearningStatDaily[]>([])
const dateRange = ref<[string, string]>(defaultDateRange())

const lineChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()
let lineChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

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
  for (const item of stats.value) {
    totalMinutes += item.studyMinutes ?? 0
    totalCourses += item.coursesCompleted ?? 0
    totalCredit += Number(item.creditEarned ?? 0)
  }
  return {
    totalHours: (totalMinutes / 60).toFixed(1),
    totalCourses,
    totalCredit: totalCredit.toFixed(1),
    days: stats.value.length,
  }
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

function renderCharts() {
  if (!lineChartRef.value || !barChartRef.value) return
  if (!lineChart) lineChart = echarts.init(lineChartRef.value)
  if (!barChart) barChart = echarts.init(barChartRef.value)

  const dates = stats.value.map((item) => item.statDate)
  const studyMinutes = stats.value.map((item) => item.studyMinutes ?? 0)
  const coursesCompleted = stats.value.map((item) => item.coursesCompleted ?? 0)
  const creditEarned = stats.value.map((item) => Number(item.creditEarned ?? 0))

  lineChart.setOption({
    title: { text: '学习时长趋势', left: 'center', textStyle: { fontSize: 14, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 24, top: 48, bottom: 32 },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', name: '分钟' },
    series: [{ name: '学习时长', type: 'line', smooth: true, data: studyMinutes, areaStyle: { opacity: 0.15 }, itemStyle: { color: '#2094f3' } }],
  })

  barChart.setOption({
    title: { text: '完课与秩点', left: 'center', textStyle: { fontSize: 14, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['完成课程', '获得秩点'], top: 28 },
    grid: { left: 48, right: 24, top: 64, bottom: 32 },
    xAxis: { type: 'category', data: dates },
    yAxis: [{ type: 'value', name: '门', minInterval: 1 }, { type: 'value', name: '秩点', minInterval: 0.5 }],
    series: [
      { name: '完成课程', type: 'bar', data: coursesCompleted, itemStyle: { color: '#52c41a' }, barMaxWidth: 28 },
      { name: '获得秩点', type: 'bar', yAxisIndex: 1, data: creditEarned, itemStyle: { color: '#fa8c16' }, barMaxWidth: 28 },
    ],
  })
}

function handleResize() {
  lineChart?.resize()
  barChart?.resize()
}

function disposeCharts() {
  lineChart?.dispose()
  barChart?.dispose()
  lineChart = null
  barChart = null
}

watch(dateRange, fetchStats)

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
      <h2>学习进程</h2>
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
            <div class="page-summary-value">{{ summary.totalCourses }}</div>
            <div class="page-summary-label">完成课程（门）</div>
          </div>
          <div class="page-summary-card">
            <div class="page-summary-value">{{ summary.totalCredit }}</div>
            <div class="page-summary-label">获得秩点</div>
          </div>
          <div class="page-summary-card">
            <div class="page-summary-value">{{ summary.days }}</div>
            <div class="page-summary-label">统计天数</div>
          </div>
        </div>

        <el-empty v-if="stats.length === 0" class="page-empty" :image-size="64" description="暂无学习统计数据" />

        <div v-else class="charts-wrap">
          <div ref="lineChartRef" class="chart-box" />
          <div ref="barChartRef" class="chart-box" />
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
  border-bottom: 1px solid var(--color-border);
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
  color: var(--color-text);
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

.chart-box {
  width: 100%;
  height: 280px;
}

@media (max-width: 768px) {
  .stats-summary {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-box {
    height: 240px;
  }
}
</style>
