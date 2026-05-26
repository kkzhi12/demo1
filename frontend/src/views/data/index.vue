<template>
  <div class="data-page">
    <div class="page-card">
      <div class="page-title">📊 数据查看</div>
      <p class="hint">查看已爬取并存入数据库的银行股财报数据</p>

      <el-empty description="功能开发中，敬请期待..." />

      <div class="todo-list">
        <h4>计划功能：</h4>
        <ul>
          <li>✅ 按报告期查看所有银行股数据</li>
          <li>✅ 按股票查看历史季度数据趋势</li>
          <li>✅ 数据导出为Excel</li>
          <li>✅ 指标趋势图表（ECharts）</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.hint {
  color: #909399;
  font-size: 0.85rem;
  margin-bottom: 24px;
}

.todo-list {
  margin-top: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.todo-list h4 {
  margin-bottom: 8px;
  color: #606266;
}

.todo-list ul {
  list-style: none;
  padding: 0;
}

.todo-list li {
  padding: 4px 0;
  color: #909399;
}
</style>
