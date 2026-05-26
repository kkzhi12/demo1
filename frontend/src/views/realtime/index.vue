<template>
  <div class="realtime-page">
    <!-- 股票选择 -->
    <div class="page-card">
      <div class="page-title">⚡ 实时行情</div>
      <el-form :inline="true">
        <el-form-item label="搜索股票">
          <el-select
            v-model="selectedStocks"
            multiple
            filterable
            remote
            reserve-keyword
            :remote-method="searchStock"
            :loading="searching"
            placeholder="输入代码或名称搜索"
            style="width: 500px"
            no-data-text="输入关键字搜索股票"
          >
            <el-option
              v-for="stock in searchResults"
              :key="stock.stockCode"
              :label="`${stock.stockName} (${stock.stockCode})`"
              :value="stock.stockCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="subscribe" :disabled="connected && subscribed">
            {{ connected && subscribed ? '已订阅' : '开始订阅' }}
          </el-button>
          <el-button @click="unsubscribe" :disabled="!subscribed">停止</el-button>
        </el-form-item>
      </el-form>

      <!-- 已选股票标签 -->
      <div class="selected-tags" v-if="selectedStockDetails.length > 0">
        <span class="tag-label">已选({{ selectedStockDetails.length }})：</span>
        <el-tag
          v-for="stock in selectedStockDetails"
          :key="stock.stockCode"
          closable
          size="small"
          @close="removeStock(stock.stockCode)"
          class="stock-tag"
        >
          {{ stock.stockName }}
        </el-tag>
      </div>

      <div class="status-bar">
        <el-tag :type="connected ? 'success' : 'danger'" size="small">
          {{ connected ? '已连接' : '未连接' }}
        </el-tag>
        <el-tag type="info" size="small" v-if="lastUpdate">
          最后更新: {{ lastUpdate }}
        </el-tag>
        <el-tag type="info" size="small" v-if="stockData.length > 0">
          共 {{ stockData.length }} 只股票
        </el-tag>
      </div>
    </div>

    <!-- 实时数据表格 -->
    <div class="page-card" v-if="stockData.length > 0">
      <el-table
        :data="stockData"
        border
        size="small"
        style="width: 100%"
        :row-class-name="rowClassName"
      >
        <el-table-column prop="stockCode" label="代码" width="75" fixed />
        <el-table-column prop="stockName" label="名称" width="90" fixed />
        <el-table-column prop="price" label="最新价" width="80" align="right">
          <template #default="{ row }">
            <span :class="priceClass(row)">{{ row.price.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="changePercent" label="涨跌幅" width="85" align="right">
          <template #default="{ row }">
            <span :class="changeClass(row.changePercent)">
              {{ row.changePercent > 0 ? '+' : '' }}{{ row.changePercent.toFixed(2) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="changeAmount" label="涨跌额" width="80" align="right">
          <template #default="{ row }">
            <span :class="changeClass(row.changeAmount)">
              {{ row.changeAmount > 0 ? '+' : '' }}{{ row.changeAmount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="open" label="开盘" width="70" align="right">
          <template #default="{ row }">{{ row.open.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="high" label="最高" width="70" align="right">
          <template #default="{ row }">
            <span class="text-up">{{ row.high.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="low" label="最低" width="70" align="right">
          <template #default="{ row }">
            <span class="text-down">{{ row.low.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="prevClose" label="昨收" width="70" align="right">
          <template #default="{ row }">{{ row.prevClose.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="volume" label="成交量(手)" width="100" align="right">
          <template #default="{ row }">{{ formatVolume(row.volume) }}</template>
        </el-table-column>
        <el-table-column prop="turnover" label="成交额" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(row.turnover) }}</template>
        </el-table-column>
        <el-table-column prop="amplitude" label="振幅%" width="75" align="right">
          <template #default="{ row }">{{ row.amplitude.toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column prop="turnoverRate" label="换手率%" width="80" align="right">
          <template #default="{ row }">{{ row.turnoverRate.toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column prop="pe" label="PE" width="65" align="right">
          <template #default="{ row }">{{ row.pe.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="pb" label="PB" width="60" align="right">
          <template #default="{ row }">{{ row.pb.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="dividendYield" label="股息率%" width="80" align="right">
          <template #default="{ row }">{{ row.dividendYield.toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column prop="totalMarketCap" label="总市值" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(row.totalMarketCap) }}</template>
        </el-table-column>
        <el-table-column prop="changeYtd" label="年初至今%" width="95" align="right">
          <template #default="{ row }">
            <span :class="changeClass(row.changeYtd)">
              {{ row.changeYtd > 0 ? '+' : '' }}{{ row.changeYtd.toFixed(2) }}%
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { crawlerApi } from '@/api'
import request from '@/api'

const selectedStocks = ref([])
const searchResults = ref([])
const searching = ref(false)
const stockData = ref([])
const connected = ref(false)
const subscribed = ref(false)
const lastUpdate = ref('')

// 记录已选股票的详细信息（用于显示标签）
const stockDetailMap = ref({})
const selectedStockDetails = computed(() => {
  return selectedStocks.value
    .map(code => stockDetailMap.value[code])
    .filter(Boolean)
})

let ws = null
let searchTimer = null

onUnmounted(() => {
  closeWs()
})

// 远程搜索股票
function searchStock(keyword) {
  if (!keyword || keyword.length < 1) {
    searchResults.value = []
    return
  }

  // 防抖
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    searching.value = true
    try {
      const data = await request.get('/crawler/search-stock', { params: { keyword } })
      searchResults.value = data
      // 缓存搜索结果到详情map
      data.forEach(s => {
        stockDetailMap.value[s.stockCode] = s
      })
    } catch (e) {
      console.error('搜索失败', e)
    } finally {
      searching.value = false
    }
  }, 300)
}

// 快捷添加银行股
async function quickAddBanks() {
  try {
    const data = await crawlerApi.getSupportedBanks()
    const bankCodes = Object.keys(data)
    // 合并到已选列表（去重）
    const existing = new Set(selectedStocks.value)
    bankCodes.forEach(code => {
      if (!existing.has(code)) {
        selectedStocks.value.push(code)
      }
      stockDetailMap.value[code] = { stockCode: code, stockName: data[code] }
    })
    ElMessage.success(`已添加 ${bankCodes.length} 只银行股`)
  } catch (e) {
    ElMessage.error('获取银行股列表失败')
  }
}

// 移除单只股票
function removeStock(code) {
  selectedStocks.value = selectedStocks.value.filter(c => c !== code)
}

// 订阅
function subscribe() {
  if (selectedStocks.value.length === 0) {
    ElMessage.warning('请先搜索并选择要查看的股票')
    return
  }

  const wsUrl = `ws://${window.location.hostname}:8080/ws/stock-realtime`
  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    connected.value = true
    ws.send(JSON.stringify({
      action: 'subscribe',
      stockCodes: selectedStocks.value
    }))
    subscribed.value = true
    ElMessage.success('已连接，开始接收实时数据')
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.stocks) {
        stockData.value = data.stocks
        lastUpdate.value = new Date(data.timestamp).toLocaleTimeString()
      }
    } catch (e) {
      console.error('解析数据失败', e)
    }
  }

  ws.onclose = () => {
    connected.value = false
    subscribed.value = false
  }

  ws.onerror = () => {
    ElMessage.error('连接失败，请确认后端已启动')
    connected.value = false
    subscribed.value = false
  }
}

// 取消订阅
function unsubscribe() {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ action: 'unsubscribe' }))
  }
  closeWs()
  subscribed.value = false
  ElMessage.info('已停止接收实时数据')
}

function closeWs() {
  if (ws) {
    ws.close()
    ws = null
  }
  connected.value = false
}

// 格式化
function formatVolume(val) {
  if (val >= 10000) return (val / 10000).toFixed(1) + '万'
  return val.toString()
}

function formatMoney(val) {
  if (val >= 100000000) return (val / 100000000).toFixed(2) + '亿'
  if (val >= 10000) return (val / 10000).toFixed(1) + '万'
  return val.toString()
}

function changeClass(val) {
  if (val > 0) return 'text-up'
  if (val < 0) return 'text-down'
  return ''
}

function priceClass(row) {
  return changeClass(row.changePercent)
}

function rowClassName({ row }) {
  if (row.changePercent > 0) return 'row-up'
  if (row.changePercent < 0) return 'row-down'
  return ''
}
</script>

<style scoped>
.selected-tags {
  margin: 12px 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.tag-label {
  font-size: 0.85rem;
  color: #606266;
}

.stock-tag {
  margin: 0;
}

.status-bar {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.text-up {
  color: #f56c6c;
  font-weight: 500;
}

.text-down {
  color: #67c23a;
  font-weight: 500;
}

:deep(.row-up) {
  background-color: rgba(245, 108, 108, 0.03) !important;
}

:deep(.row-down) {
  background-color: rgba(103, 194, 58, 0.03) !important;
}
</style>
