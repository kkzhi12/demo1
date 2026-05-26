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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 银行股分析` : '银行股价值分析系统'
  next()
})

export default router
