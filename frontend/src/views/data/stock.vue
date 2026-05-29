<template>
  <div class="data-page">
    <!-- 搜索条件 -->
    <div class="page-card">
      <div class="page-title">📊 股票数据查看</div>
      <el-form :inline="true" class="search-form">
        <el-form-item label="股票代码/名称">
          <el-input
            v-model="searchForm.stockCode"
            placeholder="输入代码或名称模糊搜索"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="营收同比 ≥">
          <el-input
            v-model="searchForm.minRevenueYoy"
            placeholder="如 15"
            style="width: 100px"
            @keyup.enter="handleSearch"
            @input="onNumberInput('minRevenueYoy')"
          >
            <template #suffix>%</template>
          </el-input>
        </el-form-item>
        <el-form-item label="净利润同比 ≥">
          <el-input
            v-model="searchForm.minNetProfitYoy"
            placeholder="如 20"
            style="width: 100px"
            @keyup.enter="handleSearch"
            @input="onNumberInput('minNetProfitYoy')"
          >
            <template #suffix>%</template>
          </el-input>
        </el-form-item>
        <el-form-item label="ROE ≥">
          <el-input
            v-model="searchForm.minRoe"
            placeholder="如 10"
            style="width: 100px"
            @keyup.enter="handleSearch"
            @input="onNumberInput('minRoe')"
          >
            <template #suffix>%</template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="page-card">
      <el-table
        :data="tableData"
        border
        stripe
        size="small"
        v-loading="loading"
        style="width: 100%"
        row-key="rowKey"
        :row-class-name="rowClassName"
      >
        <!-- 展开列 -->
        <el-table-column width="40" fixed align="center">
          <template #default="{ row }">
            <span
              v-if="!row._isChild"
              class="expand-btn"
              @click="toggleExpand(row)"
            >
              <el-icon :class="{ 'is-expanded': row._expanded }"><ArrowRight /></el-icon>
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="stockCode" label="股票代码" width="90" fixed />
        <el-table-column prop="stockName" label="股票名称" width="100" fixed />
        <el-table-column prop="reportPeriod" label="报告期" width="85" align="center" />

        <!-- 每股指标 -->
        <el-table-column label="每股指标" align="center">
          <el-table-column prop="epsJb" label="基本每股收益" width="110" align="right">
            <template #default="{ row }">{{ formatNum(row.epsJb) }}</template>
          </el-table-column>
          <el-table-column prop="epsKc" label="扣非每股收益" width="110" align="right">
            <template #default="{ row }">{{ formatNum(row.epsKc) }}</template>
          </el-table-column>
          <el-table-column prop="bps" label="每股净资产" width="100" align="right">
            <template #default="{ row }">{{ formatNum(row.bps) }}</template>
          </el-table-column>
        </el-table-column>

        <!-- 营收利润 -->
        <el-table-column label="营收利润" align="center">
          <el-table-column prop="totalRevenue" label="营业总收入" width="120" align="right">
            <template #default="{ row }">{{ formatBigNum(row.totalRevenue) }}</template>
          </el-table-column>
          <el-table-column prop="netProfit" label="归母净利润" width="120" align="right">
            <template #default="{ row }">{{ formatBigNum(row.netProfit) }}</template>
          </el-table-column>
          <el-table-column prop="netProfitKc" label="扣非净利润" width="120" align="right">
            <template #default="{ row }">{{ formatBigNum(row.netProfitKc) }}</template>
          </el-table-column>
        </el-table-column>

        <!-- 增长率 -->
        <el-table-column label="增长率" align="center">
          <el-table-column prop="revenueYoy" label="营收同比%" width="95" align="right">
            <template #default="{ row }">
              <span :class="growthClass(row.revenueYoy)">{{ formatNum(row.revenueYoy) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="netProfitYoy" label="净利润同比%" width="100" align="right">
            <template #default="{ row }">
              <span :class="growthClass(row.netProfitYoy)">{{ formatNum(row.netProfitYoy) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="netProfitKcYoy" label="扣非同比%" width="95" align="right">
            <template #default="{ row }">
              <span :class="growthClass(row.netProfitKcYoy)">{{ formatNum(row.netProfitKcYoy) }}</span>
            </template>
          </el-table-column>
        </el-table-column>

        <!-- 盈利能力 -->
        <el-table-column label="盈利能力" align="center">
          <el-table-column prop="roe" label="ROE%" width="80" align="right">
            <template #default="{ row }">{{ formatNum(row.roe) }}</template>
          </el-table-column>
          <el-table-column prop="roa" label="ROA%" width="80" align="right">
            <template #default="{ row }">{{ formatNum(row.roa) }}</template>
          </el-table-column>
          <el-table-column prop="grossProfitMargin" label="毛利率%" width="85" align="right">
            <template #default="{ row }">{{ formatNum(row.grossProfitMargin) }}</template>
          </el-table-column>
          <el-table-column prop="netProfitMargin" label="净利率%" width="85" align="right">
            <template #default="{ row }">{{ formatNum(row.netProfitMargin) }}</template>
          </el-table-column>
        </el-table-column>

        <!-- 偿债能力 -->
        <el-table-column label="偿债能力" align="center">
          <el-table-column prop="currentRatio" label="流动比率" width="85" align="right">
            <template #default="{ row }">{{ formatNum(row.currentRatio) }}</template>
          </el-table-column>
          <el-table-column prop="debtRatio" label="资产负债率%" width="105" align="right">
            <template #default="{ row }">{{ formatNum(row.debtRatio) }}</template>
          </el-table-column>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="!row._isChild"
              type="primary"
              size="small"
              link
              @click="viewChart(row)"
            >
              图表趋势
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 15, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { stockFinanceApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

// 搜索条件
const searchForm = reactive({
  stockCode: '',
  minRevenueYoy: '',
  minNetProfitYoy: '',
  minRoe: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 15,
  total: 0
})

// 原始数据（不含展开的子行）
const rawData = ref([])

// 限制只能输入正数（含小数）
function onNumberInput(field) {
  // 移除非数字和小数点的字符
  let val = searchForm[field].replace(/[^\d.]/g, '')
  // 确保只有一个小数点
  const dotIndex = val.indexOf('.')
  if (dotIndex !== -1) {
    val = val.slice(0, dotIndex + 1) + val.slice(dotIndex + 1).replace(/\./g, '')
  }
  // 不允许以小数点开头
  if (val.startsWith('.')) {
    val = ''
  }
  searchForm[field] = val
}

// 页面加载
onMounted(async () => {
  await handleSearch()
})

// 查询
async function handleSearch() {
  loading.value = true
  try {
    const params = {
      stockCode: searchForm.stockCode,
      page: pagination.page,
      size: pagination.size
    }
    // 只有填了值才传筛选参数
    if (searchForm.minRevenueYoy) {
      params.minRevenueYoy = searchForm.minRevenueYoy
    }
    if (searchForm.minNetProfitYoy) {
      params.minNetProfitYoy = searchForm.minNetProfitYoy
    }
    if (searchForm.minRoe) {
      params.minRoe = searchForm.minRoe
    }

    const data = await stockFinanceApi.pageQuery(params)
    rawData.value = (data.content || []).map(row => ({
      ...row,
      rowKey: row.stockCode + '_' + row.reportPeriod,
      _expanded: false,
      _isChild: false,
      _dimmed: false
    }))
    tableData.value = [...rawData.value]
    pagination.total = data.totalElements || 0
  } catch (e) {
    ElMessage.error('查询失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

// 重置
function handleReset() {
  searchForm.stockCode = ''
  searchForm.minRevenueYoy = ''
  searchForm.minNetProfitYoy = ''
  searchForm.minRoe = ''
  pagination.page = 1
  handleSearch()
}

// 判断子行是否满足筛选条件
function isChildMatchFilter(childRow) {
  const minRevYoy = searchForm.minRevenueYoy ? Number(searchForm.minRevenueYoy) : null
  const minNpYoy = searchForm.minNetProfitYoy ? Number(searchForm.minNetProfitYoy) : null
  const minRoe = searchForm.minRoe ? Number(searchForm.minRoe) : null

  // 如果没有设置任何筛选条件，不置灰
  if (minRevYoy === null && minNpYoy === null && minRoe === null) {
    return true
  }

  // 检查每个条件
  if (minRevYoy !== null) {
    const val = childRow.revenueYoy !== null && childRow.revenueYoy !== undefined ? Number(childRow.revenueYoy) : null
    if (val === null || val < minRevYoy) return false
  }
  if (minNpYoy !== null) {
    const val = childRow.netProfitYoy !== null && childRow.netProfitYoy !== undefined ? Number(childRow.netProfitYoy) : null
    if (val === null || val < minNpYoy) return false
  }
  if (minRoe !== null) {
    const val = childRow.roe !== null && childRow.roe !== undefined ? Number(childRow.roe) : null
    if (val === null || val < minRoe) return false
  }
  return true
}

// 展开/收起历史数据
async function toggleExpand(row) {
  if (row._expanded) {
    // 收起：移除子行
    row._expanded = false
    tableData.value = tableData.value.filter(r => r._parentCode !== row.stockCode)
  } else {
    // 展开：加载历史数据
    row._expanded = true
    try {
      const history = await stockFinanceApi.getHistory(row.stockCode)
      // 排除当前最新的那条，从新到旧排序
      const children = history
        .filter(h => h.reportPeriod !== row.reportPeriod)
        .sort((a, b) => b.reportPeriod.localeCompare(a.reportPeriod))
        .map(h => ({
          ...h,
          rowKey: h.stockCode + '_' + h.reportPeriod + '_child',
          _isChild: true,
          _parentCode: row.stockCode,
          _dimmed: !isChildMatchFilter(h)
        }))

      // 插入到当前行后面
      const index = tableData.value.findIndex(r => r.rowKey === row.rowKey)
      tableData.value.splice(index + 1, 0, ...children)
    } catch (e) {
      ElMessage.error('获取历史数据失败')
      row._expanded = false
    }
  }
}

// 行样式：不符合条件的子行置灰
function rowClassName({ row }) {
  if (row._dimmed) {
    return 'row-dimmed'
  }
  return ''
}

// 查看图表趋势
function viewChart(row) {
  const routeData = router.resolve({
    path: '/chart',
    query: { stockCode: row.stockCode, stockName: row.stockName }
  })
  window.open(routeData.href, '_blank')
}

// 格式化数字（保留2位小数）
function formatNum(val) {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(2)
}

// 格式化大数字（亿/万）
function formatBigNum(val) {
  if (val === null || val === undefined) return '-'
  const num = Number(val)
  if (Math.abs(num) >= 100000000) {
    return (num / 100000000).toFixed(2) + '亿'
  } else if (Math.abs(num) >= 10000) {
    return (num / 10000).toFixed(2) + '万'
  }
  return num.toFixed(2)
}

// 增长率颜色
function growthClass(val) {
  if (val === null || val === undefined) return ''
  return Number(val) >= 0 ? 'text-up' : 'text-down'
}
</script>

<style scoped>
.search-form {
  margin-bottom: 0;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.text-up {
  color: #f56c6c;
}

.text-down {
  color: #67c23a;
}

.expand-btn {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  transition: transform 0.2s;
}

.expand-btn .el-icon {
  transition: transform 0.2s;
  color: #409eff;
  font-size: 14px;
}

.expand-btn .el-icon.is-expanded {
  transform: rotate(90deg);
}

/* 不符合筛选条件的子行置灰 */
:deep(.row-dimmed) {
  color: #c0c4cc !important;
  background-color: #f9f9f9 !important;
}

:deep(.row-dimmed) td {
  color: #c0c4cc !important;
}

:deep(.row-dimmed .text-up),
:deep(.row-dimmed .text-down) {
  color: #c0c4cc !important;
}
</style>
