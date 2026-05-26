<template>
  <div class="crawler-page">
    <!-- 爬取控制 -->
    <div class="page-card">
      <div class="page-title">🕷️ 数据爬取</div>

      <el-form :inline="true" class="crawl-form">
        <el-form-item label="报告期">
          <el-select v-model="reportPeriod" placeholder="选择报告期" style="width: 140px">
            <el-option
              v-for="period in reportPeriods"
              :key="period"
              :label="period"
              :value="period"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="crawling" @click="crawlAllDynamic">
            批量爬取银行股
          </el-button>
          <el-button @click="fetchBankList" :loading="fetchingList">
            获取银行股列表
          </el-button>
          <el-button @click="fetchAllStockList" :loading="fetchingAll">
            获取全部A股列表
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 银行股列表 -->
    <div class="page-card" v-if="bankList.length > 0">
      <div class="page-title">🏦 银行板块股票列表（{{ bankList.length }}只）</div>
      <el-table :data="bankList" border size="small" max-height="400">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="股票代码" width="100" />
        <el-table-column prop="name" label="股票名称" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="crawlSingle(row.code)">
              爬取数据
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 爬取结果 -->
    <div class="page-card" v-if="crawlResults.length > 0">
      <div class="page-title">
        📋 爬取结果
        <el-tag type="success" size="small" class="result-tag">
          成功 {{ crawlResults.filter(r => r.success).length }}
        </el-tag>
        <el-tag type="danger" size="small" class="result-tag">
          失败 {{ crawlResults.filter(r => !r.success).length }}
        </el-tag>
      </div>
      <el-table :data="crawlResults" border size="small" max-height="500">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="stockCode" label="代码" width="80" />
        <el-table-column prop="stockName" label="名称" width="100" />
        <el-table-column prop="success" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">
              {{ row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="信息" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { crawlerApi } from '@/api'

const reportPeriod = ref('2025Q1')
const crawling = ref(false)
const fetchingList = ref(false)
const fetchingAll = ref(false)
const bankList = ref([])
const crawlResults = ref([])

// 生成报告期选项
const reportPeriods = []
for (let year = 2023; year <= 2026; year++) {
  for (let q = 1; q <= 4; q++) {
    if (year === 2026 && q > 1) break
    reportPeriods.push(`${year}Q${q}`)
  }
}

// 获取银行股列表
async function fetchBankList() {
  fetchingList.value = true
  try {
    const data = await crawlerApi.fetchBankStocks()
    bankList.value = Object.entries(data).map(([code, name]) => ({ code, name }))
    ElMessage.success(`获取到 ${bankList.value.length} 只银行股`)
  } catch (e) {
    ElMessage.error('获取失败: ' + e.message)
  } finally {
    fetchingList.value = false
  }
}

// 获取全部A股列表
async function fetchAllStockList() {
  fetchingAll.value = true
  try {
    const data = await crawlerApi.fetchAllStocks()
    const count = Object.keys(data).length
    ElMessage.success(`获取到 ${count} 只A股股票`)
  } catch (e) {
    ElMessage.error('获取失败: ' + e.message)
  } finally {
    fetchingAll.value = false
  }
}

// 批量爬取
async function crawlAllDynamic() {
  if (!reportPeriod.value) {
    ElMessage.warning('请选择报告期')
    return
  }
  crawling.value = true
  crawlResults.value = []
  try {
    const data = await crawlerApi.crawlAllBanksDynamic(reportPeriod.value)
    crawlResults.value = data
    const successCount = data.filter(r => r.success).length
    ElMessage.success(`爬取完成：成功${successCount}/${data.length}`)
  } catch (e) {
    ElMessage.error('爬取失败: ' + e.message)
  } finally {
    crawling.value = false
  }
}

// 单只爬取
async function crawlSingle(stockCode) {
  if (!reportPeriod.value) {
    ElMessage.warning('请先选择报告期')
    return
  }
  try {
    const result = await crawlerApi.crawlSingle(stockCode, reportPeriod.value)
    if (result.success) {
      ElMessage.success(`${result.stockName} 爬取成功`)
    } else {
      ElMessage.warning(`${result.stockName}: ${result.message}`)
    }
  } catch (e) {
    ElMessage.error('爬取失败: ' + e.message)
  }
}
</script>

<style scoped>
.crawl-form {
  margin-bottom: 16px;
}

.result-tag {
  margin-left: 8px;
}
</style>
