<template>
  <div class="analysis-page">
    <!-- 权重配置 -->
    <div class="page-card">
      <div class="page-title">📊 模型权重配置</div>
      <p class="hint">五大维度权重总和应为 1.0，可根据投资策略调整</p>

      <el-row :gutter="20" class="weight-row">
        <el-col :span="4" v-for="item in weightItems" :key="item.key">
          <div class="weight-item">
            <span class="weight-label">{{ item.label }}</span>
            <el-input-number
              v-model="weights[item.key]"
              :min="0"
              :max="1"
              :step="0.05"
              :precision="2"
              size="small"
            />
          </div>
        </el-col>
      </el-row>

      <div class="strategy-buttons">
        <el-button @click="applyStrategy('default')">默认均衡</el-button>
        <el-button type="warning" @click="applyStrategy('dividend')">高股息策略</el-button>
        <el-button type="primary" @click="applyStrategy('growth')">成长性策略</el-button>
        <el-button type="success" @click="applyStrategy('safety')">安全性策略</el-button>
      </div>
    </div>

    <!-- 股票选择 -->
    <div class="page-card">
      <div class="page-title">📝 选择分析对象</div>
      <p class="hint">选择报告期和要对比的银行股（至少2只），点击分析即可得出排名</p>

      <el-form :inline="true" class="select-form">
        <el-form-item label="报告期">
          <el-select
            v-model="selectedPeriod"
            placeholder="选择报告期"
            style="width: 140px"
            @change="onPeriodChange"
          >
            <el-option
              v-for="period in reportPeriods"
              :key="period"
              :label="period"
              :value="period"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="添加股票">
          <el-select
            v-model="selectedStocks"
            multiple
            filterable
            placeholder="搜索并选择股票"
            style="width: 500px"
            :loading="stocksLoading"
          >
            <el-option
              v-for="stock in availableStocks"
              :key="stock.stockCode"
              :label="`${stock.stockName} (${stock.stockCode})`"
              :value="stock.stockCode"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" plain @click="selectAll">全选</el-button>
          <el-button plain @click="selectedStocks = []">清空</el-button>
        </el-form-item>
      </el-form>

      <div class="selected-info" v-if="selectedStocks.length > 0">
        <el-tag type="info" size="small">已选择 {{ selectedStocks.length }} 只股票</el-tag>
      </div>
    </div>

    <!-- 分析按钮 -->
    <div class="analyze-action">
      <el-button type="primary" size="large" :loading="loading" @click="runAnalysis">
        🚀 开始分析
      </el-button>
    </div>

    <!-- 结果展示 -->
    <div class="page-card" v-if="results.length > 0">
      <div class="page-title">📈 分析结果（{{ selectedPeriod }}）</div>

      <!-- Top3 卡片 -->
      <el-row :gutter="16" class="top-cards">
        <el-col :span="8" v-for="(item, index) in results.slice(0, 3)" :key="item.stockCode">
          <el-card shadow="hover" :class="'top-card top-' + (index + 1)">
            <div class="medal">{{ ['🥇', '🥈', '🥉'][index] }} 第{{ item.rank }}名</div>
            <div class="stock-name">{{ item.stockName }}</div>
            <div class="stock-code">{{ item.stockCode }}</div>
            <div class="total-score">综合得分: {{ item.totalScore }}</div>
            <div class="detail">ROE: {{ item.roe || '-' }}% | 股息率: {{ item.dividendYield || '-' }}%</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细结果表格 -->
      <el-table :data="results" border stripe size="small" class="result-table">
        <el-table-column prop="rank" label="排名" width="60" align="center" />
        <el-table-column prop="stockName" label="名称" width="100" />
        <el-table-column prop="stockCode" label="代码" width="80" />
        <el-table-column prop="totalScore" label="综合得分" width="90" align="center">
          <template #default="{ row }">
            <span class="score-highlight">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="profitabilityScore" label="盈利能力" width="85" align="center" />
        <el-table-column prop="assetQualityScore" label="资产质量" width="85" align="center" />
        <el-table-column prop="growthScore" label="成长性" width="75" align="center" />
        <el-table-column prop="valuationScore" label="估值" width="65" align="center" />
        <el-table-column prop="capitalScore" label="资本充足" width="85" align="center" />
        <el-table-column prop="roe" label="ROE%" width="70" align="center" />
        <el-table-column prop="pb" label="PB" width="60" align="center" />
        <el-table-column prop="dividendYield" label="股息率%" width="80" align="center" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { analysisApi, dataApi } from '@/api'
import request from '@/api'

const loading = ref(false)
const stocksLoading = ref(false)
const results = ref([])
const reportPeriods = ref([])
const availableStocks = ref([])
const selectedPeriod = ref('')
const selectedStocks = ref([])

// 权重配置
const weights = reactive({
  profitabilityWeight: 0.25,
  assetQualityWeight: 0.30,
  growthWeight: 0.20,
  valuationWeight: 0.15,
  capitalWeight: 0.10
})

const weightItems = [
  { key: 'profitabilityWeight', label: '盈利能力' },
  { key: 'assetQualityWeight', label: '资产质量' },
  { key: 'growthWeight', label: '成长性' },
  { key: 'valuationWeight', label: '估值/安全边际' },
  { key: 'capitalWeight', label: '资本充足性' },
]

const STRATEGIES = {
  default: { profitabilityWeight: 0.25, assetQualityWeight: 0.30, growthWeight: 0.20, valuationWeight: 0.15, capitalWeight: 0.10 },
  dividend: { profitabilityWeight: 0.20, assetQualityWeight: 0.25, growthWeight: 0.10, valuationWeight: 0.35, capitalWeight: 0.10 },
  growth: { profitabilityWeight: 0.25, assetQualityWeight: 0.20, growthWeight: 0.35, valuationWeight: 0.10, capitalWeight: 0.10 },
  safety: { profitabilityWeight: 0.20, assetQualityWeight: 0.35, growthWeight: 0.15, valuationWeight: 0.15, capitalWeight: 0.15 },
}

function applyStrategy(name) {
  Object.assign(weights, STRATEGIES[name])
}

// 页面加载
onMounted(async () => {
  await loadReportPeriods()
})

// 加载报告期列表
async function loadReportPeriods() {
  try {
    const data = await dataApi.getReportPeriods()
    reportPeriods.value = data
    // 默认选最新的报告期
    if (data.length > 0) {
      selectedPeriod.value = data[0]
      await onPeriodChange()
    }
  } catch (e) {
    console.error('获取报告期失败', e)
  }
}

// 报告期变化时，加载该报告期可用的股票列表
async function onPeriodChange() {
  if (!selectedPeriod.value) return
  stocksLoading.value = true
  try {
    const data = await request.get('/bank-stock/available-stocks', {
      params: { reportPeriod: selectedPeriod.value }
    })
    availableStocks.value = data
    // 清空之前的选择
    selectedStocks.value = []
  } catch (e) {
    console.error('获取股票列表失败', e)
  } finally {
    stocksLoading.value = false
  }
}

// 全选
function selectAll() {
  selectedStocks.value = availableStocks.value.map(s => s.stockCode)
}

// 运行分析
async function runAnalysis() {
  if (selectedStocks.value.length < 2) {
    ElMessage.warning('至少需要选择2只股票进行对比分析')
    return
  }

  if (!selectedPeriod.value) {
    ElMessage.warning('请选择报告期')
    return
  }

  const weightSum = Object.values(weights).reduce((a, b) => a + b, 0)
  if (Math.abs(weightSum - 1.0) > 0.01) {
    ElMessage.warning(`权重总和应为1.0，当前为${weightSum.toFixed(2)}`)
    return
  }

  loading.value = true
  try {
    const data = await request.post('/bank-stock/analyze-db', {
      stockCodes: selectedStocks.value,
      reportPeriod: selectedPeriod.value,
      weight: { ...weights }
    })
    results.value = data
    if (data.length === 0) {
      ElMessage.warning('所选股票在该报告期无有效数据')
    } else {
      ElMessage.success('分析完成')
    }
  } catch (e) {
    ElMessage.error('分析失败: ' + e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.hint {
  color: #909399;
  font-size: 0.85rem;
  margin-bottom: 16px;
}

.weight-row {
  margin-bottom: 16px;
}

.weight-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.weight-label {
  font-size: 0.85rem;
  color: #606266;
}

.strategy-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.select-form {
  margin-bottom: 12px;
}

.selected-info {
  margin-top: 8px;
}

.analyze-action {
  text-align: center;
  margin: 20px 0;
}

.top-cards {
  margin-bottom: 20px;
}

.top-card {
  text-align: center;
  padding: 8px;
}

.top-1 { border-top: 3px solid #ffd700; }
.top-2 { border-top: 3px solid #c0c0c0; }
.top-3 { border-top: 3px solid #cd7f32; }

.medal {
  font-size: 1.2rem;
  font-weight: bold;
}

.stock-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 4px 0;
}

.stock-code {
  color: #909399;
  font-size: 0.85rem;
}

.total-score {
  color: #409eff;
  font-weight: bold;
  margin-top: 4px;
}

.detail {
  color: #909399;
  font-size: 0.85rem;
}

.score-highlight {
  color: #409eff;
  font-weight: bold;
}
</style>
