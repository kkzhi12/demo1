<template>
  <el-config-provider :locale="zhCn">
    <div class="app-container">
      <el-container>
        <el-header class="app-header">
          <div class="header-left">
            <h1>📈 股票价值分析系统</h1>
          </div>
          <el-menu
            :default-active="currentRoute"
            mode="horizontal"
            router
            :ellipsis="false"
            class="header-menu"
          >
            <el-menu-item index="/analysis">
              <el-icon><DataAnalysis /></el-icon>
              <span>股票分析</span>
            </el-menu-item>
            <el-menu-item index="/crawler">
              <el-icon><Download /></el-icon>
              <span>数据爬取</span>
            </el-menu-item>
            <el-menu-item index="/data">
              <el-icon><List /></el-icon>
              <span>数据查看</span>
            </el-menu-item>
            <el-menu-item index="/realtime">
              <el-icon><DataLine /></el-icon>
              <span>实时行情</span>
            </el-menu-item>
          </el-menu>
        </el-header>
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </div>
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataAnalysis, Download, List, DataLine } from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const route = useRoute()
const currentRoute = computed(() => route.path)
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: #f0f2f5;
}

.app-header {
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 0 24px;
  height: 60px;
}

.header-left h1 {
  font-size: 1.3rem;
  color: #303133;
  margin-right: 40px;
  white-space: nowrap;
}

.header-menu {
  border-bottom: none;
  flex: 1;
  overflow: visible;
}

.header-menu :deep(.el-menu--horizontal) {
  border-bottom: none;
}

.header-menu :deep(.el-sub-menu__icon-more) {
  display: none;
}

.app-main {
  padding: 24px;
  min-height: calc(100vh - 60px);
}
</style>
