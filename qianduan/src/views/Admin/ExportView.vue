<!--
  数据导出视图
  
  功能说明：
  1. 导出报修单数据为 Excel 文件
  2. 支持选择日期范围导出
  
  路由：/admin/export
-->
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { exportRepairOrders } from '../../api/admin.js'

const dateRange = ref({
  startDate: '2024-01-01',
  endDate: new Date().toISOString().split('T')[0]
})

const handleExport = async () => {
  try {
    const response = await exportRepairOrders(dateRange.value)
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `报修单数据_${dateRange.value.startDate}_${dateRange.value.endDate}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}
</script>

<template>
  <div class="export-view">
    <div class="page-header">
      <h2>数据导出</h2>
    </div>
    
    <el-card>
      <div class="export-content">
        <h3>导出报修单数据</h3>
        <p>选择时间范围，将该时间段内的报修单数据导出为Excel文件。</p>
        
        <div class="date-selector">
          <span class="label">时间范围:</span>
          <el-date-picker
            v-model="dateRange.startDate"
            type="date"
            placeholder="开始日期"
          />
          <span class="separator">至</span>
          <el-date-picker
            v-model="dateRange.endDate"
            type="date"
            placeholder="结束日期"
          />
        </div>
        
        <el-button
          type="primary"
          @click="handleExport"
          class="export-btn"
        >
          导出Excel
        </el-button>
        
        <div class="export-info">
          <h4>导出内容包括:</h4>
          <ul>
            <li>报修单编号</li>
            <li>报修人信息</li>
            <li>报修时间</li>
            <li>故障类型</li>
            <li>故障描述</li>
            <li>宿舍信息</li>
            <li>维修员信息</li>
            <li>处理状态</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.export-view {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.export-content {
  padding: 32px;
}

.export-content h3 {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
}

.export-content p {
  color: #999;
  margin-bottom: 24px;
}

.date-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.label {
  color: #666;
}

.separator {
  color: #999;
}

.export-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.export-info {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.export-info h4 {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.export-info ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.export-info li {
  color: #333;
  font-size: 14px;
  padding: 4px 0;
}
</style>