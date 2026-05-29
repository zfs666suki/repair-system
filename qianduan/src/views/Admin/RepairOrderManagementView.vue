<!--
  报修单管理视图
  
  功能说明：
  1. 管理所有报修单
  2. 支持查看详情、分配维修员
  3. 支持状态筛选、分页查看
  
  路由：/admin/repair-orders
-->
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRepairOrderDetail, getRepairmanList } from '../../api/admin.js'
import StatusBadge from '../../components/Common/StatusBadge.vue'
import SearchForm from '../../components/Common/SearchForm.vue'
import Pagination from '../../components/Common/Pagination.vue'
import DetailModal from '../../components/Common/DetailModal.vue'

const orderList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = ref({
  status: '',
  orderNo: ''
})

const showDetailModal = ref(false)
const currentOrder = ref(null)

// 分配相关
const showAssignModal = ref(false)
const assignOrderId = ref(null)
const repairmanList = ref([])
const selectedRepairmanId = ref(null)

// 搜索字段配置
const searchFields = [
  {
    key: 'orderNo',
    type: 'input',
    placeholder: '按订单编号搜索',
    class: 'search-input'
  },
  {
    key: 'status',
    type: 'select',
    placeholder: '按状态筛选',
    options: [
      { label: '全部状态', value: '' },
      { label: '已取消', value: 0 },
      { label: '待分配', value: 1 },
      { label: '待接单', value: 2 },
      { label: '处理中', value: 3 },
      { label: '已完成', value: 4 },
      { label: '已拒绝', value: 5 }
    ]
  }
]

const loadOrderList = async () => {
  try {
    const params = new URLSearchParams()
    params.append('pageNum', pageNum.value)
    params.append('pageSize', pageSize.value)
    if (searchForm.value.status !== '') {
      params.append('status', searchForm.value.status)
    }
    if (searchForm.value.orderNo !== '') {
      params.append('keyword', searchForm.value.orderNo)
    }

    const response = await fetch(`/api/repair/list?${params.toString()}`, {
      method: 'GET',
      headers: {
        'Authorization': sessionStorage.getItem('token'),
        'Content-Type': 'application/json'
      }
    })
    const result = await response.json()
    if (result.code === 200) {
      let data = result.data.records || result.data
      if (data && !Array.isArray(data)) {
        data = [data]
      }
      orderList.value = data || []
      total.value = result.data.total || orderList.value.length
    }
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

const handleDetail = async (orderId) => {
  try {
    const response = await getRepairOrderDetail(orderId)
    if (response.code === 200) {
      currentOrder.value = response.data
      showDetailModal.value = true
    }
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

const getImageUrl = (imagePath) => {
  if (!imagePath) return null
  return `http://localhost:8080${imagePath}`
}

// 解析图片数据，支持JSON数组或逗号分隔字符串
const parseImages = (imageStr) => {
  if (!imageStr || !imageStr.trim() || imageStr.trim() === 'null' || imageStr.trim() === '无') {
    return []
  }
  try {
    // 尝试解析JSON数组
    const parsed = JSON.parse(imageStr)
    if (Array.isArray(parsed)) {
      return parsed
    }
    return [parsed]
  } catch {
    // 不是JSON数组，尝试逗号分隔
    return imageStr.split(',').filter(url => url && url.trim())
  }
}

// 检查是否可以分配（待分配或已拒绝状态）
const canAssign = (status) => {
  return status === 1 || status === 5
}

// 打开分配弹窗
const openAssignModal = (order) => {
  assignOrderId.value = order.id
  selectedRepairmanId.value = null
  loadRepairmanList()
  showAssignModal.value = true
}

// 加载维修员列表
const loadRepairmanList = async () => {
  try {
    const response = await getRepairmanList({ pageNum: 1, pageSize: 50 })
    if (response.code === 200) {
      repairmanList.value = response.data.records || response.data || []
    }
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

// 执行分配
const handleAssign = async () => {
  if (!selectedRepairmanId.value) {
    ElMessage.warning('请选择维修员')
    return
  }

  try {
    const response = await fetch('/api/repair/assign', {
      method: 'POST',
      headers: {
        'Authorization': sessionStorage.getItem('token'),
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        orderId: assignOrderId.value,
        repairUserId: selectedRepairmanId.value
      })
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('分配成功')
      showAssignModal.value = false
      loadOrderList()
    }
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

// 搜索处理
const handleSearch = () => {
  pageNum.value = 1
  loadOrderList()
}

// 分页处理
const handlePageChange = ({ pageNum: newPageNum, pageSize: newPageSize }) => {
  pageNum.value = newPageNum
  pageSize.value = newPageSize
  loadOrderList()
}

onMounted(() => {
  loadOrderList()
})
</script>

<template>
  <div class="repair-order-management">
    <div class="page-header">
      <h2>报修管理</h2>
    </div>
    
    <SearchForm 
      v-model="searchForm" 
      :fields="searchFields" 
      @search="handleSearch"
    />
    
    <el-card class="data-card">
      <el-table :data="orderList" border class="data-table" :empty-text="'暂无数据'">
        <el-table-column prop="orderNo" label="订单编号" />
        <el-table-column prop="buildingName" label="楼栋" />
        <el-table-column prop="roomNumber" label="房间号" />
        <el-table-column prop="faultTypeName" label="故障类型" />
        <el-table-column label="故障描述" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.description || scope.row.faultDesc || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="报修人" />
        <el-table-column label="维修员">
          <template #default="scope">
            {{ scope.row.repairUserName || scope.row.repairmanName || '无' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <StatusBadge :status="scope.row.status" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="text" @click="handleDetail(scope.row.id)">详情</el-button>
            <el-button 
              v-if="canAssign(scope.row.status)" 
              type="text" 
              style="color: #67c23a"
              @click="openAssignModal(scope.row)">分配</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <Pagination 
        :total="total" 
        v-model:page-num="pageNum" 
        v-model:page-size="pageSize"
        @change="handlePageChange"
      />
    </el-card>
    
    <!-- 详情弹窗 -->
    <DetailModal title="报修单详情" v-model="showDetailModal">
      <div v-if="currentOrder" class="detail-content">
        <div class="detail-row">
          <span class="detail-label">订单编号:</span>
          <span class="detail-value">{{ currentOrder.orderNo }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">楼栋:</span>
          <span class="detail-value">{{ currentOrder.buildingName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">房间号:</span>
          <span class="detail-value">{{ currentOrder.roomNumber }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">故障类型:</span>
          <span class="detail-value">{{ currentOrder.faultTypeName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">故障描述:</span>
          <span class="detail-value">{{ currentOrder.description || currentOrder.faultDesc || '' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">报修人:</span>
          <span class="detail-value">{{ currentOrder.studentName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">维修员:</span>
          <span class="detail-value">{{ currentOrder.repairUserName || currentOrder.repairmanName || '未分配' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态:</span>
          <span class="detail-value">
            <StatusBadge :status="currentOrder.status" />
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">故障图片:</span>
          <div class="image-container">
            <el-image
              v-if="currentOrder.images && currentOrder.images.trim().length > 0 && currentOrder.images.trim() !== '无' && currentOrder.images.trim() !== 'null'"
              v-for="(image, index) in parseImages(currentOrder.images)"
              :key="'fault-' + index"
              :src="image"
              :preview-src-list="parseImages(currentOrder.images)"
              class="fault-image"
              fit="cover"
            />
            <span v-else class="no-image">无</span>
          </div>
        </div>
        <div v-if="currentOrder.rejectReason" class="detail-row">
          <span class="detail-label">拒单理由:</span>
          <span class="detail-value">{{ currentOrder.rejectReason }}</span>
        </div>
        <div v-if="currentOrder.repairResult" class="detail-row">
          <span class="detail-label">维修结果:</span>
          <span class="detail-value">{{ currentOrder.repairResult }}</span>
        </div>
        <div v-if="currentOrder.repairImages" class="detail-row">
          <span class="detail-label">维修图片:</span>
          <div class="image-container">
            <el-image
              v-if="currentOrder.repairImages && currentOrder.repairImages.trim().length > 0 && currentOrder.repairImages.trim() !== '无' && currentOrder.repairImages.trim() !== 'null'"
              v-for="(image, index) in parseImages(currentOrder.repairImages)"
              :key="'repair-' + index"
              :src="image"
              :preview-src-list="parseImages(currentOrder.repairImages)"
              class="repair-image"
              fit="cover"
            />
            <span v-else class="no-image">无</span>
          </div>
        </div>
        <div class="detail-row">
          <span class="detail-label">创建时间:</span>
          <span class="detail-value">{{ currentOrder.createTime }}</span>
        </div>
      </div>
    </DetailModal>
    
    <!-- 分配弹窗 -->
    <DetailModal title="分配维修员" v-model="showAssignModal" width="400px">
      <div class="assign-content">
        <div class="assign-item">
          <span class="assign-label">选择维修员:</span>
          <el-select v-model="selectedRepairmanId" placeholder="请选择维修员" class="repairman-select">
            <el-option
              v-for="repairman in repairmanList"
              :key="repairman.id"
              :label="repairman.realName || repairman.username"
              :value="repairman.id"
            />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAssignModal = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确认分配</el-button>
      </template>
    </DetailModal>
  </div>
</template>

<style scoped>
.repair-order-management {
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

.search-input {
  width: 200px;
}

.data-table {
  background: #fff;
  border-radius: 8px;
}

.detail-content {
  padding: 16px;
}

.detail-row {
  display: flex;
  margin-bottom: 12px;
}

.detail-label {
  width: 100px;
  color: #999;
  font-weight: 500;
}

.detail-value {
  flex: 1;
  color: #333;
}

.image-container {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.fault-image,
.repair-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.no-image {
  color: #999;
}

/* 分配弹窗样式 */
.assign-content {
  padding: 16px;
}

.assign-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.assign-label {
  font-weight: 500;
  color: #666;
}

.repairman-select {
  width: 100%;
}
</style>