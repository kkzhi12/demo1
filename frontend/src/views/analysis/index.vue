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

    <!-- 数据录入 -->
    <div class="page-card">
      <div class="page-title">📝 银行数据录入</div>
      <p class="hint">至少输入2家银行数据才能进行比较分析</p>

      <el-table :data="tableData" border size="small" max-height="500" class="data-table">
        <el-table-column label="操作" width="70" fixed>
          <template #default="{ $index }">
            <el-button type="danger" size="small" link @click="deleteRow($index)">删除</el-button>
          </template>
        </el-table-column>
        <el-table-column label="股票代码" width="110">
          <template #default="{ row }">
            <el-input v-model="row.stockCode" size="small" placeholder="如600036" />
          </template>
        </el-table-column>
        <el-table-column label="股票名称" width="110">
          <template #default="{ row }">
            <el-input v-model="row.stockName" size="small" placeholder="如招商银行" />
          </template>
        </el-table-column>
        <el-table-column label="当前股价" width="90">
          <template #default="{ row }">
            <el-input-number v-model="row.currentPrice" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>

        <!-- 盈利能力 -->
        <el-table-column label="ROE%" width="85">
          <template #default="{ row }">
            <el-input-number v-model="row.roe" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="净息差%" width="85">
          <template #default="{ row }">
            <el-input-number v-model="row.nim" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="成本收入比%" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.costIncomeRatio" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>

        <!-- 资产质量 -->
        <el-table-column label="不良率%" width="85">
          <template #default="{ row }">
            <el-input-number v-model="row.nplRatio" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="拨备覆盖率%" width="105">
          <template #default="{ row }">
            <el-input-number v-model="row.provisionCoverageRatio" size="small" :controls="false" :precision="1" />
          </template>
        </el-table-column>
        <el-table-column label="关注类占比%" width="105">
          <template #default="{ row }">
            <el-input-number v-model="row.specialMentionLoanRatio" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>

        <!-- 成长性 -->
        <el-table-column label="营收增长%" width="95">
          <template #default="{ row }">
            <el-input-number v-model="row.revenueGrowthRate" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="净利润增长%" width="105">
          <template #default="{ row }">
            <el-input-number v-model="row.netProfitGrowthRate" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="贷款增长%" width="95">
          <template #default="{ row }">
            <el-input-number v-model="row.loanGrowthRate" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>

        <!-- 估值 -->
        <el-table-column label="PB" width="75">
          <template #default="{ row }">
            <el-input-number v-model="row.pb" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="PE" width="75">
          <template #default="{ row }">
            <el-input-number v-model="row.pe" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="股息率%" width="85">
          <template #default="{ row }">
            <el-input-number v-model="row.dividendYield" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>

        <!-- 资本充足 -->
        <el-table-column label="核心一级%" width="95">
          <template #default="{ row }">
            <el-input-number v-model="row.coreCapitalAdequacyRatio" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="资本充足率%" width="105">
          <template #default="{ row }">
            <el-input-number v-model="row.capitalAdequacyRatio" size="small" :controls="false" :precision="2" />
          </template>
        </el-table-column>
      </el-table>

      <div class="table-actions">
        <el-button type="primary" @click="addRow">+ 添加银行</el-button>
        <el-button @click="loadSampleData">载入示例数据</el-button>
        <el-button type="danger" plain @click="clearAll">清空数据</el-button>
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
      <div class="page-title">📈 分析结果</div>

      <!-- Top3 卡片 -->
      <el-row :gutter="16" class="top-cards">
        <el-col :span="8" v-for="(item, index) in results.slice(0, 3)" :key="item.stockCode">
          <el-card shadow="hover" :class="'top-card top-' + (index + 1)">
            <div class="medal">{{ ['🥇', '🥈', '🥉'][index] }} 第{{ item.rank }}名</div>
            <div class="stock-name">{{ item.stockName }}</div>
            <div class="stock-code">{{ item.stockCode }}</div>
            <div class="total-score">综合得分: {{ item.totalScore }}</div>
            <div class="detail">股息率: {{ item.dividendYield || '-' }}%</div>
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { analysisApi } from '@/api'
import { SAMPLE_DATA } from './sampleData'

const loading = ref(false)
const results = ref([])

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

// 表格数据
const tableData = ref([createEmptyRow(), createEmptyRow()])

function createEmptyRow() {
  return {
    stockCode: null, stockName: null, currentPrice: null,
    roe: null, nim: null, costIncomeRatio: null,
    nplRatio: null, provisionCoverageRatio: null, specialMentionLoanRatio: null,
    revenueGrowthRate: null, netProfitGrowthRate: null, loanGrowthRate: null,
    pb: null, pe: null, dividendYield: null,
    coreCapitalAdequacyRatio: null, capitalAdequacyRatio: null
  }
}

function addRow() {
  tableData.value.push(createEmptyRow())
}

function deleteRow(index) {
  tableData.value.splice(index, 1)
}

function loadSampleData() {
  tableData.value = SAMPLE_DATA.map(item => ({ ...item }))
}

function clearAll() {
  tableData.value = []
  results.value = []
}

// 运行分析
async function runAnalysis() {
  const validData = tableData.value.filter(row => row.stockName)
  if (validData.length < 2) {
    ElMessage.warning('至少需要输入2家银行数据')
    return
  }

  const weightSum = Object.values(weights).reduce((a, b) => a + b, 0)
  if (Math.abs(weightSum - 1.0) > 0.01) {
    ElMessage.warning(`权重总和应为1.0，当前为${weightSum.toFixed(2)}`)
    return
  }

  loading.value = true
  try {
    const data = await analysisApi.analyzeJson({
      bankStocks: validData,
      weight: { ...weights }
    })
    results.value = data
    ElMessage.success('分析完成')
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

.table-actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
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

.data-table :deep(.el-input-number) {
  width: 100%;
}
</style>
