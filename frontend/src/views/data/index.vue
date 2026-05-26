<template>
  <div class="data-page">
    <!-- 搜索条件 -->
    <div class="page-card">
      <div class="page-title">📊 数据查看</div>
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

        <!-- 盈利能力 -->
        <el-table-column label="盈利能力" align="center">
          <el-table-column prop="roe" label="ROE%" width="80" align="right">
            <template #default="{ row }">{{ formatNum(row.roe) }}</template>
          </el-table-column>
          <el-table-column prop="nim" label="净息差%" width="80" align="right">
            <template #default="{ row }">{{ formatNum(row.nim) }}</template>
          </el-table-column>
          <el-table-column prop="costIncomeRatio" label="成本收入比%" width="100" align="right">
            <template #default="{ row }">{{ formatNum(row.costIncomeRatio) }}</template>
          </el-table-column>
        </el-table-column>

        <!-- 资产质量 -->
        <el-table-column label="资产质量" align="center">
          <el-table-column prop="nplRatio" label="不良率%" width="80" align="right">
            <template #default="{ row }">{{ formatNum(row.nplRatio) }}</template>
          </el-table-column>
          <el-table-column prop="provisionCoverageRatio" label="拨备覆盖率%" width="105" align="right">
            <template #default="{ row }">{{ formatNum(row.provisionCoverageRatio) }}</template>
          </el-table-column>
          <el-table-column prop="specialMentionLoanRatio" label="关注类占比%" width="100" align="right">
            <template #default="{ row }">{{ formatNum(row.specialMentionLoanRatio) }}</template>
          </el-table-column>
        </el-table-column>

        <!-- 成长性 -->
        <el-table-column label="成长性" align="center">
          <el-table-column prop="revenueGrowthRate" label="营收增长%" width="95" align="right">
            <template #default="{ row }">
              <span :class="growthClass(row.revenueGrowthRate)">{{ formatNum(row.revenueGrowthRate) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="netProfitGrowthRate" label="净利润增长%" width="100" align="right">
            <template #default="{ row }">
              <span :class="growthClass(row.netProfitGrowthRate)">{{ formatNum(row.netProfitGrowthRate) }}</span>
            </template>
          </el-table-column>
        </el-table-column>

        <!-- 估值 -->
        <el-table-column label="估值" align="center">
          <el-table-column prop="pb" label="PB" width="65" align="right">
            <template #default="{ row }">{{ formatNum(row.pb) }}</template>
          </el-table-column>
          <el-table-column prop="pe" label="PE" width="65" align="right">
            <template #default="{ row }">{{ formatNum(row.pe) }}</template>
          </el-table-column>
          <el-table-column prop="dividendYield" label="股息率%" width="80" align="right">
            <template #default="{ row }">{{ formatNum(row.dividendYield) }}</template>
          </el-table-column>
        </el-table-column>

        <!-- 资本充足 -->
        <el-table-column label="资本充足" align="center">
          <el-table-column prop="coreCapitalAdequacyRatio" label="核心一级%" width="90" align="right">
            <template #default="{ row }">{{ formatNum(row.coreCapitalAdequacyRatio) }}</template>
          </el-table-column>
          <el-table-column prop="capitalAdequacyRatio" label="资本充足率%" width="100" align="right">
            <template #default="{ row }">{{ formatNum(row.capitalAdequacyRatio) }}</template>
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
import { dataApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

// 搜索条件
const searchForm = reactive({
  stockCode: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 15,
  total: 0
})

// 原始数据（不含展开的子行）
const rawData = ref([])

// 页面加载
onMounted(async () => {
  await handleSearch()
})

// 查询
async function handleSearch() {
  loading.value = true
  try {
    const data = await dataApi.pageQuery({
      stockCode: searchForm.stockCode,
      page: pagination.page,
      size: pagination.size
    })
    rawData.value = (data.content || []).map(row => ({
      ...row,
      rowKey: row.stockCode + '_' + row.reportPeriod,
      _expanded: false,
      _isChild: false
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
  pagination.page = 1
  handleSearch()
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
      const history = await dataApi.getHistory(row.stockCode)
      // 排除当前最新的那条，从新到旧排序
      const children = history
        .filter(h => h.reportPeriod !== row.reportPeriod)
        .sort((a, b) => b.reportPeriod.localeCompare(a.reportPeriod))
        .map(h => ({
          ...h,
          rowKey: h.stockCode + '_' + h.reportPeriod + '_child',
          _isChild: true,
          _parentCode: row.stockCode
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

// 查看图表趋势
function viewChart(row) {
  const routeData = router.resolve({
    path: '/chart',
    query: { stockCode: row.stockCode, stockName: row.stockName }
  })
  window.open(routeData.href, '_blank')
}

// 格式化数字
function formatNum(val) {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(2)
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
</style>
