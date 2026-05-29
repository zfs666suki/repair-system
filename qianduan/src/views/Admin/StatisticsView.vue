<!--
  统计分析视图
  
  功能说明：
  1. 展示报修数据统计图表
  2. 支持按日期、楼栋、故障类型、状态等维度统计
  3. 使用 ECharts 实现数据可视化
  
  路由：/admin/statistics
-->
<script setup>
// 导入 Vue 相关 API
import { ref, onMounted, watch, nextTick } from 'vue'
// 导入 ECharts 图表库
import * as echarts from 'echarts'
// 导入管理员相关的统计 API
import {
  statisticsByDate,        // 按日期统计
  statisticsByBuilding,    // 按楼栋统计
  statisticsByFaultType,   // 按故障类型统计
  statisticsByStatus,      // 按状态统计
  getAllRepairUserStatistics // 获取所有维修员统计数据
} from '../../api/admin.js'

// ==================== 响应式数据定义 ====================

// 日期范围选择器，默认从 2026-04-20 到今天
const dateRange = ref({
  startDate: '2026-04-20',
  endDate: new Date().toISOString().split('T')[0]
})

// 各类统计数据数组
const dateStatistics = ref([])        // 按日期统计数据
const buildingStatistics = ref([])    // 按楼栋统计数据
const faultTypeStatistics = ref([])   // 按故障类型统计数据
const statusStatistics = ref([])      // 按状态统计数据
const repairmanStatistics = ref([])   // 维修员工作量统计数据

// ==================== ECharts 实例管理 ====================

// 存储各个图表的 ECharts 实例，用于后续销毁和更新
let dateChart = null      // 日期趋势图实例
let buildingChart = null  // 楼栋分布图实例
let faultTypeChart = null // 故障类型图实例
let statusChart = null    // 状态分布图实例

// ==================== 工具函数 ====================

/**
 * 格式化日期对象为标准字符串格式 (YYYY-MM-DD)
 * @param {Date|string} date - 需要格式化的日期
 * @returns {string} 格式化后的日期字符串
 */
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// ==================== 数据加载函数 ====================

/**
 * 加载所有统计数据
 * 并行请求5个统计接口，获取不同维度的统计数据
 */
const loadStatistics = async () => {
  try {
    // 构建请求参数，格式化日期范围
    const params = {
      startDate: formatDate(dateRange.value.startDate),
      endDate: formatDate(dateRange.value.endDate)
    }
    
    // 并行发起5个统计请求，提高加载效率
    const [dateRes, buildingRes, faultTypeRes, statusRes, repairmanRes] = await Promise.all([
      statisticsByDate(params),           // 获取按日期统计数据
      statisticsByBuilding(params),       // 获取按楼栋统计数据
      statisticsByFaultType(params),      // 获取按故障类型统计数据
      statisticsByStatus(params),         // 获取按状态统计数据
      getAllRepairUserStatistics(params)  // 获取维修员统计数据
    ])
    
    // 处理各接口返回的数据，检查响应码是否为成功
    if (dateRes.code === 200) {
      dateStatistics.value = dateRes.data
    }
    if (buildingRes.code === 200) {
      buildingStatistics.value = buildingRes.data
    }
    if (faultTypeRes.code === 200) {
      faultTypeStatistics.value = faultTypeRes.data
    }
    if (statusRes.code === 200) {
      statusStatistics.value = statusRes.data
    }
    if (repairmanRes.code === 200) {
      repairmanStatistics.value = repairmanRes.data
    }
    
    // 等待 DOM 更新完成后渲染图表
    await nextTick()
    renderCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// ==================== 图表渲染函数 ====================

/**
 * 统一渲染所有图表
 */
const renderCharts = () => {
  renderDateChart()      // 渲染日期趋势图
  renderBuildingChart()  // 渲染楼栋分布图
  renderFaultTypeChart() // 渲染故障类型图
  renderStatusChart()    // 渲染状态分布图
}

/**
 * 渲染日期趋势柱状图
 * 展示选定时间范围内每天的报修数量变化趋势
 */
const renderDateChart = () => {
  // 获取图表容器 DOM 元素
  const chartDom = document.getElementById('dateChart')
  if (!chartDom) return
  
  // 如果已存在实例，先销毁避免内存泄漏
  if (dateChart) {
    dateChart.dispose()
  }
  
  // 创建新的 ECharts 实例
  dateChart = echarts.init(chartDom)
  
  // 提取日期标签（格式化为 M/D）和对应的报修数量
  const dates = dateStatistics.value.map(item => {
    const d = new Date(item.date)
    return `${d.getMonth() + 1}/${d.getDate()}`
  })
  const counts = dateStatistics.value.map(item => item.count)
  
  // 配置图表选项
  const option = {
    // 图表标题
    title: {
      text: '报修数量趋势',
      left: 'center', // 标题居中
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    // 提示框配置
    tooltip: {
      trigger: 'axis',// 漠标悬停时触发
    },
    // 图表网格配置 控制绘图区域与容器边缘间的间距
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true // 包含标签，当 X/Y 轴标签文字较长时，会自动扩大边距避免标签被裁剪 防止标签被裁剪
    },
    //x轴配置
    xAxis: {
      type: 'category',// 分类轴，用于显示日期标签
      data: dates,// 分类轴数据，即日期标签 如 ['04/20', '04/21', '04/22', ...]
      axisLabel: {
        fontSize: 12 // 标签字体大小
      }
    },
    //y轴配置
    yAxis: {
      type: 'value',// 数值轴，用于显示报修数量
      axisLabel: {
        fontSize: 12 // 标签字体大小
      }
    },
    // 数据系列配置
    series: [
      {
        name: '报修数量',
        type: 'bar',// 柱状图，用于显示报修数量
        data: counts,// 柱状图数据，即报修数量 如 [10, 15, 20, ...]
        itemStyle: {
          // 设置渐变色效果，从蓝色到绿色 天蓝色shiny blue
          color: 'skyblue',

        }
      }
    ]
  }
  
  // 应用配置到图表
  dateChart.setOption(option)
}

/**
 * 渲染楼栋分布饼图
 * 展示各楼栋的报修数量占比情况
 */
const renderBuildingChart = () => {
  const chartDom = document.getElementById('buildingChart')
  if (!chartDom) return
  
  if (buildingChart) {
    buildingChart.dispose()
  }
  
  buildingChart = echarts.init(chartDom)
  
  // 转换数据格式为 ECharts 所需的 {name, value} 结构
  const data = buildingStatistics.value.map(item => ({
    name: item.buildingName,
    value: item.count
  }))
  
  const option = {
    // 图表标题
    title: {
      text: '楼栋报修分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    // 提示框配置
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)' // 显示名称、数量和百分比
    },
    // 图例配置
    legend: {
      orient: 'horizontal',
      bottom: '5%',
      textStyle: {
        fontSize: 12
      }
    },
    // 数据系列配置
    series: [
      {
        name: '报修数量',
        type: 'pie',
        radius: ['40%', '70%'], // 环形图，内径40%，外径70%
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        // 标签配置
        label: {
          show: false,
          position: 'center'
        },
        // 高亮配置配置
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        // 标签线配置
        labelLine: {
          show: false
        },
        data: data,
        color: ['orange', 'green', 'blue', 'red', 'purple', 'black']
      }
    ]
  }
  
  buildingChart.setOption(option)
}

/**
 * 渲染故障类型分布柱状图
 * 展示各类故障类型的报修数量对比
 */
const renderFaultTypeChart = () => {
  const chartDom = document.getElementById('faultTypeChart')
  if (!chartDom) return
  
  if (faultTypeChart) {
    faultTypeChart.dispose()
  }
  
  faultTypeChart = echarts.init(chartDom)
  
  // 提取故障类型名称和对应数量
  const types = faultTypeStatistics.value.map(item => item.typeName)
  const counts = faultTypeStatistics.value.map(item => item.count)
  
  const option = {
    // 图表标题
    title: {
      text: '故障类型分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    // 提示框配置
    tooltip: {
      trigger: 'axis'
    },
    // 图表网格配置
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    // x轴配置
    xAxis: {
      type: 'category',
      data: types,
      axisLabel: {
        fontSize: 12,
        rotate: 30 // X轴标签旋转30度，避免文字重叠
      }
    },
    // y轴配置
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 12
      }
    },
    // 数据系列配置
    series: [
      {
        name: '报修数量',
        type: 'bar',
        data: counts,
        itemStyle: {
          // 设置渐变色效果，从红色到橙色
          color: 'orange',
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
  
  faultTypeChart.setOption(option)
}

/**
 * 渲染报修状态分布饼图
 * 展示不同状态下报修单的分布情况
 */
const renderStatusChart = () => {
  const chartDom = document.getElementById('statusChart')
  if (!chartDom) return
  
  if (statusChart) {
    statusChart.dispose()
  }
  
  statusChart = echarts.init(chartDom)
  
  // 状态码映射表：将数字状态码转换为中文描述
  const statusMap = { 0: '待分配', 1: '待接单', 2: '进行中', 3: '已完成', 4: '已取消', 5: '已拒单' }
  
  // 转换数据格式并映射状态名称
  const data = statusStatistics.value.map(item => ({
    name: statusMap[item.status] || '未知',
    value: item.count
  }))
  
  const option = {
    // 图表标题
    title: {
      text: '报修状态分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    // 提示框配置
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    // 图例配置
    legend: {
      orient: 'horizontal',
      bottom: '5%',
      textStyle: {
        fontSize: 12
      }
    },
    // 数据系列配置
    series: [
      {
        name: '状态分布',
        type: 'pie',
        radius: '55%', // 普通饼图，半径55%
        center: ['50%', '45%'], // 图表中心位置
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        color: ['orange', 'green', 'blue', 'red', 'purple', 'black']
      }
    ]
  }
  
  statusChart.setOption(option)
}

// ==================== 辅助函数 ====================

/**
 * 获取状态文本描述
 * @param {number} status - 状态码
 * @returns {string} 状态文本
 */
const getStatusText = (status) => {
  const map = { 0: '待分配', 1: '待接单', 2: '进行中', 3: '已完成', 4: '已取消', 5: '已拒单' }
  return map[status] || '未知'
}

/**
 * 窗口大小改变时重新调整所有图表尺寸
 */
const handleResize = () => {
  dateChart?.resize()
  buildingChart?.resize()
  faultTypeChart?.resize()
  statusChart?.resize()
}

// ==================== 生命周期和监听器 ====================

// 监听日期范围变化，自动重新加载统计数据
watch(dateRange, () => {
  loadStatistics()
}, { deep: true })

// 组件挂载时初始化数据和事件监听
onMounted(() => {
  loadStatistics()                          // 首次加载统计数据
  window.addEventListener('resize', handleResize) // 监听窗口大小变化
})
</script>

<template>
  <div class="statistics-view">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>统计分析</h2>
    </div>
    
    <!-- 日期筛选器 -->
    <div class="date-filter">
      <span class="filter-label">时间范围:</span>
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
    
    <!-- 图表区域：2x2网格布局 -->
    <div class="charts-grid">
      <!-- 按日期统计 - 柱状图 -->
      <div class="chart-card">
        <div id="dateChart" class="chart"></div>
      </div>
      
      <!-- 按楼栋统计 - 饼图 -->
      <div class="chart-card">
        <div id="buildingChart" class="chart"></div>
      </div>
      
      <!-- 按故障类型统计 - 柱状图 -->
      <div class="chart-card">
        <div id="faultTypeChart" class="chart"></div>
      </div>
      
      <!-- 按状态统计 - 饼图 -->
      <div class="chart-card">
        <div id="statusChart" class="chart"></div>
      </div>
    </div>
    
    <!-- 维修员工作量统计表 -->
    <div class="stat-section">
      <h3>维修员工作量统计</h3>
      <!--:data绑定数据源，显示维修员姓名、接单数、完成数和完成率-->
      <!--border添加边框--><!--:empty-text当 repairmanStatistics 为空数组时，表格中央显示的提示文字-->
      <el-table :data="repairmanStatistics" border class="data-table" :empty-text="'暂无数据'">
        <!--prop是数据绑定的属性，label是显示的文本-->
        <el-table-column prop="realName" label="维修员姓名" /> <!--prop="realName" 显示维修员姓名，而不是默认的ID --> 
        <el-table-column prop="orderCount" label="接单数" /> <!--prop="orderCount" 显示接单数，而不是默认的ID --> 
        <el-table-column prop="completedCount" label="完成数" /> <!--prop="completedCount" 显示完成数，而不是默认的ID --> 
        <el-table-column prop="completionRate" label="完成率"><!--prop="completionRate" 显示完成率，而不是默认的ID --> 
          <!--#default是默认的插槽，#default是插槽的名称-->
          <!--scope是作用域对象，包含当前行数据-->
              <!--scope.row 当前行数据对象-->
                 <!--scope.row.completionRate 当前行数据对象的 完成率属性 例如：0.8599-->
                <!--* 100 是将完成率乘以100，转换为百分比 例如：0.8599 * 100 = 85.99-->
                <!--.toFixed(1) 是将数字转换为字符串，并保留一位小数 例如：85.99.toFixed(1) = "85.9"-->
                <!--% 是百分号符号，用于表示百分比 例如："85.9%"-->
                 <!--{{ }} 插值表达式，将变量值插入到模板中-->
          <template #default="scope">
            <span>{{ (scope.row.completionRate * 100).toFixed(1) }}%</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
/* 主容器样式 */
.statistics-view {
  padding: 20px;
}

/* 页面标题样式 */
.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

/* 日期筛选器样式 */
.date-filter {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}

.filter-label {
  color: #666;
}

.separator {
  color: #999;
}

/* 图表网格布局：2列 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

/* 单个图表卡片样式 */
.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 图表容器固定高度 */
.chart {
  width: 100%;
  height: 280px;
}

/* 统计表格区域样式 */
.stat-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stat-section h3 {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

.data-table {
  background: #fff;
}

/* 响应式设计：小屏幕改为单列布局 */
@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart {
    height: 220px;
  }
}
</style>