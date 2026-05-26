<template>
  <div class="chart-page">
    <!-- 主图：ROE、营收增长率、净利润增长率 -->
    <div class="page-card">
      <div class="page-title">
        <el-button class="back-btn" link @click="goBack">
          <el-icon :size="18"><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <span>📈 {{ stockName }}（{{ stockCode }}）盈利与成长趋势</span>
      </div>
      <div ref="mainChartRef" class="chart-main" v-loading="loading"></div>
    </div>

    <!-- 小图区域 -->
    <div class="small-charts-grid">
      <div class="page-card small-chart-card">
        <div class="page-title">净息差</div>
        <div ref="nimChartRef" class="chart-small"></div>
      </div>
      <div class="page-card small-chart-card">
        <div class="page-title">拨备覆盖率</div>
        <div ref="provisionChartRef" class="chart-small"></div>
      </div>
      <div class="page-card small-chart-card">
        <div class="page-title">不良贷款率</div>
        <div ref="nplChartRef" class="chart-small"></div>
      </div>
      <div class="page-card small-chart-card">
        <div class="page-title">核心一级资本充足率</div>
        <div ref="capitalChartRef" class="chart-small"></div>
      </div>
    </div>

    <!-- 数据明细表格 -->
    <div class="page-card">
      <div class="page-title">📋 数据明细</div>
      <el-table :data="historyData" border stripe size="small">
        <el-table-column prop="reportPeriod" label="报告期" width="85" align="center" />
        <el-table-column prop="roe" label="ROE%" width="80" align="right">
          <template #default="{ row }">{{ formatNum(row.roe) }}</template>
        </el-table-column>
        <el-table-column prop="nim" label="净息差%" width="80" align="right">
          <template #default="{ row }">{{ formatNum(row.nim) }}</template>
        </el-table-column>
        <el-table-column prop="revenueGrowthRate" label="营收增长%" width="100" align="right">
          <template #default="{ row }">
            <span :class="growthClass(row.revenueGrowthRate)">{{ formatNum(row.revenueGrowthRate) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="netProfitGrowthRate" label="净利润增长%" width="110" align="right">
          <template #default="{ row }">
            <span :class="growthClass(row.netProfitGrowthRate)">{{ formatNum(row.netProfitGrowthRate) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="provisionCoverageRatio" label="拨备覆盖率%" width="110" align="right">
          <template #default="{ row }">{{ formatNum(row.provisionCoverageRatio) }}</template>
        </el-table-column>
        <el-table-column prop="nplRatio" label="不良率%" width="80" align="right">
          <template #default="{ row }">{{ formatNum(row.nplRatio) }}</template>
        </el-table-column>
        <el-table-column prop="coreCapitalAdequacyRatio" label="核心一级%" width="100" align="right">
          <template #default="{ row }">{{ formatNum(row.coreCapitalAdequacyRatio) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ArrowLeft } from '@element-plus/icons-vue'
import { dataApi } from '@/api'

const route = useRoute()
const router = useRouter()
const stockCode = route.query.stockCode || ''
const stockName = route.query.stockName || ''

const loading = ref(false)
const mainChartRef = ref(null)
const nimChartRef = ref(null)
const provisionChartRef = ref(null)
const nplChartRef = ref(null)
const capitalChartRef = ref(null)
const historyData = ref([])

const charts = []

function goBack() {
  // 如果是新tab打开的，关闭当前tab；否则路由跳转
  if (window.history.length <= 1) {
    window.close()
  } else {
    router.push('/data')
  }
}

onMounted(async () => {
  await loadData()
})

onUnmounted(() => {
  charts.forEach(c => c.dispose())
  window.removeEventListener('resize', handleResize)
})

function handleResize() {
  charts.forEach(c => c.resize())
}

async function loadData() {
  if (!stockCode) return
  loading.value = true
  try {
    const data = await dataApi.getHistory(stockCode)
    historyData.value = data.sort((a, b) => a.reportPeriod.localeCompare(b.reportPeriod))
    await nextTick()
    renderAllCharts()
    window.addEventListener('resize', handleResize)
  } catch (e) {
    console.error('获取数据失败', e)
  } finally {
    loading.value = false
  }
}

function renderAllCharts() {
  if (historyData.value.length === 0) return

  const periods = historyData.value.map(d => d.reportPeriod)

  // 主图：ROE + 营收增长率 + 净利润增长率
  renderMainChart(periods)

  // 小图
  renderSmallChart(nimChartRef.value, periods, '净息差(%)', historyData.value.map(d => d.nim), '#00BCD4')
  renderSmallChart(provisionChartRef.value, periods, '拨备覆盖率(%)', historyData.value.map(d => d.provisionCoverageRatio), '#F56C6C')
  renderSmallChart(nplChartRef.value, periods, '不良贷款率(%)', historyData.value.map(d => d.nplRatio), '#909399')
  renderSmallChart(capitalChartRef.value, periods, '核心一级资本充足率(%)', historyData.value.map(d => d.coreCapitalAdequacyRatio), '#9B59B6')
}

function renderMainChart(periods) {
  const chart = echarts.init(mainChartRef.value)
  charts.push(chart)

  const option = {
    title: {
      text: `${stockName} 盈利能力与成长性`,
      left: 'center',
      textStyle: { fontSize: 15 }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params) {
        let html = `<strong>${params[0].axisValue}</strong><br/>`
        params.forEach(p => {
          const val = p.value !== null && p.value !== undefined ? Number(p.value).toFixed(2) + '%' : '-'
          html += `${p.marker} ${p.seriesName}: ${val}<br/>`
        })
        return html
      }
    },
    legend: {
      data: ['ROE', '营收增长率', '净利润增长率'],
      top: 30
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '12%',
      top: '18%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: periods,
      axisLabel: { rotate: 30 }
    },
    yAxis: {
      type: 'value',
      name: '%',
      axisLabel: { formatter: '{value}%' }
    },
    series: [
      {
        name: 'ROE',
        type: 'line',
        data: historyData.value.map(d => d.roe),
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { width: 2.5 },
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '营收增长率',
        type: 'line',
        data: historyData.value.map(d => d.revenueGrowthRate),
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { width: 2.5 },
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '净利润增长率',
        type: 'line',
        data: historyData.value.map(d => d.netProfitGrowthRate),
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { width: 2.5 },
        itemStyle: { color: '#E6A23C' }
      }
    ]
  }

  chart.setOption(option)
}

function renderSmallChart(el, periods, name, data, color) {
  if (!el) return
  const chart = echarts.init(el)
  charts.push(chart)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function (params) {
        const p = params[0]
        const val = p.value !== null && p.value !== undefined ? Number(p.value).toFixed(2) + '%' : '-'
        return `<strong>${p.axisValue}</strong><br/>${p.marker} ${name}: ${val}`
      }
    },
    grid: {
      left: '12%',
      right: '8%',
      bottom: '18%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: periods,
      axisLabel: { rotate: 30, fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: '{value}%', fontSize: 10 }
    },
    series: [
      {
        name: name,
        type: 'line',
        data: data,
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { width: 2, color: color },
        itemStyle: { color: color },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: color + '40' },
            { offset: 1, color: color + '05' }
          ])
        }
      }
    ]
  }

  chart.setOption(option)
}

function formatNum(val) {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(2)
}

function growthClass(val) {
  if (val === null || val === undefined) return ''
  return Number(val) >= 0 ? 'text-up' : 'text-down'
}
</script>

<style scoped>
.chart-main {
  width: 100%;
  height: 400px;
}

.back-btn {
  margin-right: 12px;
  font-size: 0.9rem;
  color: #409eff;
}

.small-charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.small-chart-card {
  margin-bottom: 0;
}

.chart-small {
  width: 100%;
  height: 250px;
}

.text-up {
  color: #f56c6c;
}

.text-down {
  color: #67c23a;
}

@media (max-width: 768px) {
  .small-charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
