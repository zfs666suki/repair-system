<!--
  报修单详情视图
  
  功能说明：
  1. 展示报修单的详细信息
  2. 支持接单、拒单、完成维修等操作（完成维修需填写维修结果和上传维修图片）
  3. 显示报修状态和处理进度
  
  路由：/repairman/orders/:id
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRepairDetail, acceptOrder, rejectOrder, completeOrder } from '../../api/repair'
import { ElMessage, ElMessageBox, ElDialog } from 'element-plus'
import ImageUploader from '../../components/Common/ImageUploader.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const repairDetail = ref(null)

// 完成维修弹窗相关
const showCompleteModal = ref(false)
const repairResult = ref('')
const repairImages = ref([])

const statusMap = {
  0: { text: '已取消', type: 'info' },
  1: { text: '待分配', type: 'warning' },
  2: { text: '待接单', type: 'warning' },
  3: { text: '处理中', type: 'primary' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已拒绝', type: 'danger' }
}

onMounted(() => {
  fetchDetail()
})

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

const getStatusInfo = (status) => {
  return statusMap[status] || { text: '未知', type: 'info' }
}

const handleAccept = async () => {
  try {
    await ElMessageBox.confirm('确定要接这个报修单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await acceptOrder({ orderId: repairDetail.value.id })
    if (response.code === 200) {
      ElMessage.success('接单成功')
      fetchDetail()
    } else {
      ElMessage.error(response.message || '接单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接单失败:', error)
    }
  }
}

const handleReject = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '拒绝原因不能为空'
    })
    const response = await rejectOrder({ orderId: repairDetail.value.id, reason: reason })
    if (response.code === 200) {
      ElMessage.success('拒单成功')
      fetchDetail()
    } else {
      ElMessage.error(response.message || '拒单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒单失败:', error)
    }
  }
}

const openCompleteModal = () => {
  repairResult.value = ''
  repairImages.value = []
  showCompleteModal.value = true
}

const handleComplete = async () => {
  if (!repairResult.value.trim()) {
    ElMessage.warning('请输入维修结果')
    return
  }
  if (repairImages.value.length === 0) {
    ElMessage.warning('请上传维修图片')
    return
  }
  
  try {
    const response = await completeOrder({ 
      orderId: repairDetail.value.id, 
      repairResult: repairResult.value,
      images: repairImages.value.join(',')
    })
    if (response.code === 200) {
      ElMessage.success('维修完成')
      showCompleteModal.value = false
      fetchDetail()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleBack = () => {
  router.back()
}
</script>

<template>
  <div class="repair-detail">
    <el-card v-if="repairDetail">
      <template #header>
        <span>报修详情</span>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="报修单号">
          {{ repairDetail.orderNo }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
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
        <el-descriptions-item label="故障描述">
          {{ repairDetail.description }}
        </el-descriptions-item>
        <el-descriptions-item label="故障图片" :span="2">
          <div v-if="repairDetail.images && repairDetail.images.length > 0" style="display: flex; gap: 10px; flex-wrap: wrap;">
            <el-image
              v-for="(img, index) in repairDetail.images.split(',')"
              :key="index"
              :src="img"
              :preview-src-list="repairDetail.images.split(',')"
              fit="cover"
              style="width: 150px; height: 100px; border-radius: 4px;"
            />
          </div>
          <span v-else style="color: #999;">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">
          {{ repairDetail.createTime }}
        </el-descriptions-item>

        <template v-if="repairDetail.studentName">
          <el-descriptions-item label="学生姓名">
            {{ repairDetail.studentName }}
          </el-descriptions-item>
          <el-descriptions-item label="学生电话">
            {{ repairDetail.studentPhone }}
          </el-descriptions-item>
        </template>

        <template v-if="repairDetail.repairUserName">
          <el-descriptions-item label="维修员">
            {{ repairDetail.repairUserName }}
          </el-descriptions-item>
          <el-descriptions-item label="维修员电话">
            {{ repairDetail.repairUserPhone }}
          </el-descriptions-item>
        </template>

        <template v-if="repairDetail.acceptTime">
          <el-descriptions-item label="接单时间" :span="2">
            {{ repairDetail.acceptTime }}
          </el-descriptions-item>
        </template>

        <template v-if="repairDetail.completeTime">
          <el-descriptions-item label="完成时间" :span="2">
            {{ repairDetail.completeTime }}
          </el-descriptions-item>
        </template>

        <template v-if="repairDetail.repairResult">
          <el-descriptions-item label="维修结果" :span="2">
            {{ repairDetail.repairResult }}
          </el-descriptions-item>
        </template>

        <template v-if="repairDetail.repairImages">
          <el-descriptions-item label="维修图片" :span="2">
            <div style="display: flex; gap: 10px; flex-wrap: wrap;">
              <el-image
                v-for="(img, index) in repairDetail.repairImages.split(',')"
                :key="index"
                :src="img"
                :preview-src-list="repairDetail.repairImages.split(',')"
                fit="cover"
                style="width: 150px; height: 100px; border-radius: 4px;"
              />
            </div>
          </el-descriptions-item>
        </template>

        <template v-if="repairDetail.rejectReason">
          <el-descriptions-item label="拒单理由" :span="2">
            {{ repairDetail.rejectReason }}
          </el-descriptions-item>
        </template>
      </el-descriptions>

      <div class="button-group">
        <el-button @click="handleBack">返回</el-button>
        <el-button
          type="primary"
          @click="handleAccept"
          v-if="repairDetail.status === 2"
        >
          接单
        </el-button>
        <el-button
          type="danger"
          @click="handleReject"
          v-if="repairDetail.status === 2"
        >
          拒单
        </el-button>
        <el-button
          type="success"
          @click="openCompleteModal"
          v-if="repairDetail.status === 3"
        >
          完成维修
        </el-button>
      </div>
    </el-card>

    <!-- 完成维修弹窗 -->
    <ElDialog title="完成维修" v-model="showCompleteModal" width="500px">
      <div class="complete-form">
        <el-form :model="repairResult" label-width="100px">
          <el-form-item label="维修结果">
            <el-input
              v-model="repairResult"
              type="textarea"
              :rows="4"
              placeholder="请输入维修结果描述"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="维修图片">
            <ImageUploader v-model="repairImages" :max-count="3" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showCompleteModal = false">取消</el-button>
        <el-button type="primary" @click="handleComplete">确认完成</el-button>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.repair-detail {
  width: 100%;
}

.button-group {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.complete-form {
  padding: 10px;
}
</style>