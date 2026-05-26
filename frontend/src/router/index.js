import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/analysis'
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: () => import('@/views/analysis/index.vue'),
    meta: { title: '股票分析' }
  },
  {
    path: '/crawler',
    name: 'Crawler',
    component: () => import('@/views/crawler/index.vue'),
    meta: { title: '数据爬取' }
  },
  {
    path: '/data',
    name: 'Data',
    component: () => import('@/views/data/index.vue'),
    meta: { title: '数据查看' }
  },
  {
    path: '/chart',
    name: 'Chart',
    component: () => import('@/views/chart/index.vue'),
    meta: { title: '图表趋势' }
  },
  {
    path: '/realtime',
    name: 'Realtime',
    component: () => import('@/views/realtime/index.vue'),
    meta: { title: '实时行情' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 股票价值分析` : '股票价值分析系统'
  next()
})

export default router
