<!--
  报修详情视图
  
  功能说明：
  1. 展示报修单的详细信息
  2. 显示报修状态、处理进度
  3. 支持取消未处理的报修单
  
  路由：/student/repair/detail
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRepairDetail, cancelRepair } from '../../api/repair'
import { ElMessage, ElMessageBox } from 'element-plus'

// 获取路由实例和路由器实例
const route = useRoute()
const router = useRouter()

// 加载状态，用于显示loading效果
const loading = ref(false)
// 报修单详情数据
const repairDetail = ref(null)

// 报修单状态映射表：key为状态码，value为显示文本和标签类型
const statusMap = {
  0: { text: '已取消', type: 'info' },
  1: { text: '待分配', type: 'warning' },
  2: { text: '待接单', type: 'warning' },
  3: { text: '处理中', type: 'primary' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已拒绝', type: 'danger' }
}

// 组件挂载时获取报修单详情
onMounted(() => {
  fetchDetail()
})

/**
 * 获取报修单详情
 * 从路由参数中获取报修单ID，调用API获取详细信息
 */
const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getRepairDetail(route.params.id)
    repairDetail.value = res.data
  } catch (error) {
    console.error('获取详情失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 根据状态码获取状态信息
 * @param {number} status - 报修单状态码
 * @returns {object} 包含显示文本和标签类型的对象
 */
const getStatusInfo = (status) => {
  return statusMap[status] || { text: '未知', type: 'info' }
}

/**
 * 取消报修单
 * 弹出确认框，用户确认后调用取消接口
 */
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该报修单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelRepair(repairDetail.value.orderNo)
    ElMessage.success('取消成功')
    // 取消成功后重新获取详情以更新页面状态
    fetchDetail()
  } catch (error) {
    // 如果用户点击取消按钮，不显示错误信息
    if (error !== 'cancel') {
      console.error('取消失败:', error)
    }
  }
}

/**
 * 返回报修单列表页面
 */
const handleBack = () => {
  router.push('/student/repair/list')
}
</script>

<template>
  <div class="repair-detail">
    <!-- 报修单详情卡片 -->
    <el-card v-if="repairDetail">
      <template #header>
        <span>报修详情</span>
      </template>

      <!-- 使用描述列表展示报修单详细信息 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报修单号">
          {{ repairDetail.orderNo }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <!-- 根据状态码显示对应的标签 -->
          <el-tag :type="getStatusInfo(repairDetail.status).type">
            {{ getStatusInfo(repairDetail.status).text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="故障类型">
          {{ repairDetail.faultTypeName }}
        </el-descriptions-item>
        <el-descriptions-item label="故障地址">
          {{ repairDetail.buildingName }} {{ repairDetail.roomNumber }}
        </el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">
          {{ repairDetail.createTime }}
        </el-descriptions-item>

        <!-- 故障图片 -->
        <el-descriptions-item label="故障图片" :span="2">
          <template v-if="repairDetail.images && repairDetail.images.trim().length > 0 && repairDetail.images.trim() !== '无' && repairDetail.images.trim() !== 'null' && repairDetail.images.trim() !== 'undefined'">
            <div class="image-grid">
              <el-image
                v-for="(img, index) in repairDetail.images.split(',')"
                :key="index"
                :src="img"
                :preview-src-list="repairDetail.images.split(',')"
                class="repair-image"
              />
            </div>
          </template>
          <span v-else style="color: #999;">无</span>
        </el-descriptions-item>

        <!-- 如果有维修员信息，显示维修员姓名和电话 -->
        <template v-if="repairDetail.repairUserName">
          <el-descriptions-item label="维修员">
            {{ repairDetail.repairUserName }}
          </el-descriptions-item>
          <el-descriptions-item label="维修员电话">
            {{ repairDetail.repairUserPhone }}
          </el-descriptions-item>
        </template>

        <!-- 如果有接单时间，显示接单时间 -->
        <template v-if="repairDetail.acceptTime">
          <el-descriptions-item label="接单时间" :span="2">
            {{ repairDetail.acceptTime }}
          </el-descriptions-item>
        </template>

        <!-- 如果有完成时间，显示完成时间 -->
        <template v-if="repairDetail.completeTime">
          <el-descriptions-item label="完成时间" :span="2">
            {{ repairDetail.completeTime }}
          </el-descriptions-item>
        </template>

        <!-- 如果有维修结果，显示维修结果 -->
        <template v-if="repairDetail.repairResult">
          <el-descriptions-item label="维修结果" :span="2">
            {{ repairDetail.repairResult }}
          </el-descriptions-item>
        </template>

        <!-- 如果有拒单理由，显示拒单理由 -->
        <template v-if="repairDetail.rejectReason">
          <el-descriptions-item label="拒单理由" :span="2">
            {{ repairDetail.rejectReason }}
          </el-descriptions-item>
        </template>

        <!-- 如果已完成且有维修图片，显示维修图片 -->
        <template v-if="repairDetail.status === 4 && repairDetail.repairImages && repairDetail.repairImages.length > 0">
          <el-descriptions-item label="维修图片" :span="2">
            <div class="image-grid">
              <el-image
                v-for="(img, index) in repairDetail.repairImages.split(',')"
                :key="index"
                :src="img"
                :preview-src-list="repairDetail.repairImages.split(',')"
                class="repair-image"
              />
            </div>
          </el-descriptions-item>
        </template>
      </el-descriptions>

      <!-- 操作按钮组 -->
      <div class="button-group">
        <el-button @click="handleBack">返回列表</el-button>
        <!-- 只有在待分配或待接单状态下才显示取消按钮 -->
        <el-button
          type="danger"
          @click="handleCancel"
          v-if="repairDetail.status === 1 || repairDetail.status === 2"
        >
          取消报修
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
/* 报修详情容器样式 */
.repair-detail {
  width: 100%;
}

/* 按钮组样式：设置上边距和按钮间距 */
.button-group {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

/* 图片网格布局 */
.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* 维修图片样式 */
.repair-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}
</style>