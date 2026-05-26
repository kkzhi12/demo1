import axios from 'axios'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 60000
})

// 响应拦截器
request.interceptors.response.use(
  response => response.data,
  error => {
    console.error('请求失败:', error.message)
    return Promise.reject(error)
  }
)

export default request

/**
 * 银行股分析相关接口
 */
export const analysisApi = {
  // 通过JSON数据分析
  analyzeJson: (data) => request.post('/bank-stock/analyze-json', data),
  // 获取默认权重
  getDefaultWeight: () => request.get('/bank-stock/default-weight'),
  // 使用示例数据运行
  runDemo: () => request.get('/bank-stock/demo/run'),
  // 高股息策略
  dividendStrategy: () => request.get('/bank-stock/demo/strategy/dividend'),
  // 成长性策略
  growthStrategy: () => request.get('/bank-stock/demo/strategy/growth'),
  // 安全性策略
  safetyStrategy: () => request.get('/bank-stock/demo/strategy/safety'),
}

/**
 * 数据查看相关接口
 */
export const dataApi = {
  // 分页查询
  pageQuery: (params) => request.get('/bank-data/page', { params }),
  // 获取所有报告期
  getReportPeriods: () => request.get('/bank-data/report-periods'),
  // 获取最新报告期
  getLatestPeriod: () => request.get('/bank-data/latest-period'),
  // 查询股票历史数据
  getHistory: (stockCode) => request.get(`/bank-data/history/${stockCode}`),
}

/**
 * 爬虫相关接口
 */
export const crawlerApi = {
  // 获取所有A股代码
  fetchAllStocks: () => request.get('/crawler/fetch-all-stocks'),
  // 获取银行板块股票
  fetchBankStocks: () => request.get('/crawler/fetch-bank-stocks'),
  // 获取静态银行列表
  getSupportedBanks: () => request.get('/crawler/supported-banks'),
  // 爬取单只股票
  crawlSingle: (stockCode, reportPeriod) =>
    request.get('/crawler/single', { params: { stockCode, reportPeriod } }),
  // 批量爬取（静态列表）
  crawlAllBanks: (reportPeriod) =>
    request.get('/crawler/all-banks', { params: { reportPeriod } }),
  // 批量爬取（动态列表）
  crawlAllBanksDynamic: (reportPeriod) =>
    request.get('/crawler/all-banks-dynamic', { params: { reportPeriod } }),
}
