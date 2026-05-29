<!--
  学生报修列表视图
  
  功能说明：
  1. 展示学生自己的报修单列表
  2. 支持分页查看、状态筛选
  3. 支持取消未处理的报修单
  4. 支持查看报修详情
  
  路由：/student/repair/list
-->
<script setup>
// 导入Vue生命周期钩子，用于组件挂载时执行初始化操作
import { onMounted } from 'vue'
// 导入路由实例，用于页面跳转
import { useRouter } from 'vue-router'
// 导入报修相关的API接口方法
import { getRepairList, cancelRepair } from '../../api/repair'
// 导入Element Plus的消息提示和确认对话框组件
import { ElMessageBox, ElMessage } from 'element-plus'
// 导入状态徽章组件，用于显示报修单状态
import StatusBadge from '../../components/Common/StatusBadge.vue'
// 导入分页组件
import Pagination from '../../components/Common/Pagination.vue'
// 导入列表管理的组合式函数，封装了分页、加载等通用逻辑
import { useList } from '../../composables/useList'

// 获取路由实例
const router = useRouter()

// 使用useList组合式函数管理报修列表数据
// 解构出：列表数据、总数、分页配置、加载方法、分页变化处理方法
const {
  list: repairList,
  total,
  pagination,
  loadList,
  handlePageChange
} = useList(getRepairList)

// 组件挂载时，加载报修列表数据
onMounted(() => {
  loadList()
})

/**
 * 查看报修单详情
 * @param {Object} row - 当前行的报修单数据
 */
const handleViewDetail = (row) => {
  // 跳转到报修详情页，携带报修单ID
  router.push('/student/repair/detail/' + row.id)
}

/**
 * 编辑报修单
 * @param {Object} row - 当前行的报修单数据
 */
const handleEdit = (row) => {
  // 跳转到报修提交页面，携带编辑ID作为查询参数
  router.push('/student/repair/submit?editId=' + row.id)
}

/**
 * 取消报修单
 * @param {Object} row - 当前行的报修单数据
 */
const handleCancel = async (row) => {
  try {
    // 弹出确认对话框，用户确认后执行取消操作
    await ElMessageBox.confirm('确定要取消该报修单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用取消报修API，传入订单号
    await cancelRepair(row.orderNo)
    // 显示成功提示
    ElMessage.success('取消成功')
    // 重新加载列表数据，更新页面显示
    loadList()
  } catch (error) {
    // 如果用户点击取消按钮，error值为'cancel'，不进行处理
    // 其他错误由axios拦截器统一处理并显示错误提示
    if (error !== 'cancel') {
      // axios拦截器已统一处理错误提示
    }
  }
}

/**
 * 格式化日期字符串
 * @param {String} dateStr - 日期字符串
 * @returns {String} 格式化后的日期字符串，如果为空则返回'-'
 */
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr
}
</script>

<template>
  <!-- 报修列表容器 -->
  <div class="repair-list">
    <el-card>
      <!-- 卡片头部标题 -->
      <template #header>
        <span>我的报修</span>
      </template>

      <!-- 报修单表格 -->
      <el-table
        :data="repairList"
        stripe
        style="width: 100%"
      >
        <!-- 报修单号列 -->
        <el-table-column prop="orderNo" label="报修单号" width="180" />
        <!-- 故障类型列 -->
        <el-table-column prop="faultTypeName" label="故障类型" width="120" />
        <!-- 故障描述列，内容过长时显示省略号 -->
        <el-table-column prop="description" label="故障描述" show-overflow-tooltip />
        <!-- 故障图片列 -->
        <el-table-column label="故障图片" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.images && row.images.trim().length > 0 && row.images.trim() !== '无' && row.images.trim() !== 'null' && row.images.trim() !== 'undefined'"
              :src="row.images.split(',')[0]"
              :preview-src-list="row.images.split(',')"
              fit="cover"
              style="width: 80px; height: 60px; border-radius: 4px;"
            />
            <span v-else style="color: #999;">无</span>
          </template>
        </el-table-column>
        <!-- 状态列，使用StatusBadge组件显示 -->
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusBadge :status="row.status" size="small" />
          </template>
        </el-table-column>
        <!-- 提交时间列 -->
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <!-- 操作列，固定在右侧 -->
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <!-- 查看详情按钮 -->
            <el-button type="primary" link @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <!-- 编辑按钮，仅在待处理(1)或已派单(2)状态下显示 -->
            <el-button
              type="success"
              link
              @click="handleEdit(row)"
              v-if="row.status === 1 || row.status === 2"
            >
              编辑
            </el-button>
            <!-- 取消按钮，仅在待处理(1)或已派单(2)状态下显示 -->
            <el-button
              type="danger"
              link
              @click="handleCancel(row)"
              v-if="row.status === 1 || row.status === 2"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件，当有数据时显示 -->
      <Pagination
        v-if="total > 0"
        :total="total" 
        :page-num="pagination.pageNum"
        :page-size="pagination.pageSize"
        :show-sizes="true"
        @change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<style scoped>
/* 报修列表容器样式 */
.repair-list {
  padding: 20px;
}
</style>