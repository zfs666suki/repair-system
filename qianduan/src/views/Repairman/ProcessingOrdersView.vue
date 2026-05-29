<!--
  进行中订单视图
  
  功能说明：
  1. 展示维修员正在处理的报修单列表
  2. 支持完成维修操作（需填写维修结果和上传维修图片）
  3. 支持分页查看
  
  路由：/repairman/orders/processing
-->
<script setup>
import { ref, onMounted } from 'vue'
import { getRepairList, completeOrder, rejectOrder } from '../../api/repair'
import { ElMessage, ElDialog, ElMessageBox } from 'element-plus'
import Pagination from '../../components/Common/Pagination.vue'
import ImageUploader from '../../components/Common/ImageUploader.vue'
import { useList } from '../../composables/useList'

const {
  list: orders,
  total,
  pagination,
  loading,
  loadList,
  handlePageChange
} = useList(getRepairList)

// 完成维修弹窗相关
const showCompleteModal = ref(false)
const currentOrder = ref(null)
const repairResult = ref('')
const repairImages = ref([])

const openCompleteModal = (row) => {
  currentOrder.value = row
  repairResult.value = ''
  repairImages.value = []
  showCompleteModal.value = true
}

const handleReject = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '拒绝原因不能为空'
    })
    const response = await rejectOrder({ orderId: row.id, reason: reason })
    if (response.code === 200) {
      ElMessage.success('拒单成功')
      loadList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      // axios拦截器已统一处理错误提示
    }
  }
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
      orderId: currentOrder.value.id,
      repairResult: repairResult.value,
      images: repairImages.value.join(',')
    })
    if (response.code === 200) {
      ElMessage.success('维修完成')
      showCompleteModal.value = false
      loadList()
    }
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

onMounted(() => {
  loadList({ status: 3 })
})
</script>

<template>
  <div class="orders-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>进行中列表</span>
        </div>
      </template>
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180"></el-table-column>
        <el-table-column prop="faultTypeName" label="故障类型" width="120"></el-table-column>
        <el-table-column label="宿舍" width="150">
          <template #default="{ row }">
            {{ row.dormitoryName || row.buildingName + ' ' + row.roomNumber }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="故障描述" show-overflow-tooltip></el-table-column>
        <el-table-column label="故障图片" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.images && row.images.length > 0"
              :src="row.images.split(',')[0]"
              :preview-src-list="row.images.split(',')"
              fit="cover"
              style="width: 80px; height: 60px; border-radius: 4px;"
            />
            <span v-else style="color: #999;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180"></el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="$router.push(`/repairman/order/detail/${row.id}`)">查看</el-button>
            <el-button type="success" size="small" @click="openCompleteModal(row)">完成</el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        :page-num="pagination.pageNum"
        :page-size="pagination.pageSize"
        @change="handlePageChange"
      />
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
.orders-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.complete-form {
  padding: 10px;
}
</style>