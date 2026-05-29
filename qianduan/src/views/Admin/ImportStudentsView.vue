<!--
  学生导入视图
  
  功能说明：
  1. 批量导入学生数据
  2. 支持下载导入模板
  3. 显示导入结果（成功/失败数量）
  
  路由：/admin/import-students
-->
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { downloadStudentTemplate, importStudents } from '../../api/admin.js'

const file = ref(null)
const importResult = ref(null)
const loading = ref(false)

const handleFileChange = (event) => {
  file.value = event.target.files[0]
}

const handleDownloadTemplate = async () => {
  try {
    const response = await downloadStudentTemplate()
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '学生导入模板.xlsx'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

const handleImport = async () => {
  if (!file.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  loading.value = true
  try {
    const response = await importStudents(file.value)
    if (response.code === 200) {
      importResult.value = response.data
      ElMessage.success('导入完成')
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="import-students">
    <div class="page-header">
      <h2>批量导入学生</h2>
    </div>
    
    <div class="import-container">
      <div class="import-box">
        <h3>下载模板</h3>
        <p>请先下载Excel模板，按照模板格式填写学生信息后再进行导入。</p>
        <el-button type="primary" @click="handleDownloadTemplate">下载模板</el-button>
      </div>
      
      <div class="import-box">
        <h3>上传文件</h3>
        <p>选择已填写好的Excel文件进行批量导入。</p>
        <input
          type="file"
          accept=".xlsx,.xls"
          class="file-input"
          @change="handleFileChange"
        />
        <div v-if="file" class="file-info">
          已选择文件: {{ file.name }}
        </div>
        <el-button
          type="success"
          @click="handleImport"
          :disabled="!file"
        >
          开始导入
        </el-button>
      </div>
      
      <div v-if="importResult" class="result-box">
        <h3>导入结果</h3>
        <div class="result-item">
          <span class="label">总数:</span>
          <span class="value">{{ importResult.totalCount }}</span>
        </div>
        <div class="result-item">
          <span class="label success">成功:</span>
          <span class="value success">{{ importResult.successCount }}</span>
        </div>
        <div class="result-item">
          <span class="label error">失败:</span>
          <span class="value error">{{ importResult.failCount }}</span>
        </div>
        <div v-if="importResult.failMessages && importResult.failMessages.length > 0" class="fail-messages">
          <h4>失败详情:</h4>
          <ul>
            <li v-for="(msg, index) in importResult.failMessages" :key="index">{{ msg }}</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.import-students {
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

.import-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 600px;
}

.import-box {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.import-box h3 {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
}

.import-box p {
  color: #999;
  margin-bottom: 16px;
}

.file-input {
  margin-bottom: 12px;
}

.file-info {
  color: #666;
  margin-bottom: 12px;
}

.result-box {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.result-box h3 {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item .label {
  color: #666;
}

.result-item .label.success {
  color: #67c23a;
}

.result-item .label.error {
  color: #f56c6c;
}

.result-item .value {
  font-weight: bold;
  color: #333;
}

.result-item .value.success {
  color: #67c23a;
}

.result-item .value.error {
  color: #f56c6c;
}

.fail-messages {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.fail-messages h4 {
  font-size: 14px;
  color: #f56c6c;
  margin-bottom: 12px;
}

.fail-messages ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.fail-messages li {
  color: #666;
  padding: 4px 0;
  font-size: 14px;
}
</style>
