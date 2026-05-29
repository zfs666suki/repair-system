<!--
  宿舍分配管理视图
  
  功能说明：
  1. 管理学生宿舍分配
  2. 支持分配宿舍、退宿操作
  3. 支持分页查看分配记录
  
  路由：/admin/dormitories
-->
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '../../components/Common/Pagination.vue'
import {
  getDormitoryList,
  getStudentList,
  getRoomList,
  getBuildingList,
  assignDormitory,
  checkoutDormitory
} from '../../api/admin.js'

const dormitoryList = ref([])
const studentList = ref([])
const roomList = ref([])
const buildingList = ref([])
const dormitoryRecords = ref([]) // 用于过滤可分配学生
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const showAddModal = ref(false)
const formData = ref({
  studentId: '',
  roomId: ''
})

// 获取可分配的学生（未分配或已退宿的学生）
const getAssignableStudents = () => {
  // 获取所有已入住学生的ID（状态为1）
  const occupiedStudentIds = dormitoryRecords.value
    .filter(d => d.status === 1)
    .map(d => d.studentId)
  
  // 返回未分配或已退宿的学生
  return studentList.value.filter(s => !occupiedStudentIds.includes(s.id))
}

const loadDormitoryList = async () => {
  try {
    const response = await getDormitoryList({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (response.code === 200) {
      dormitoryList.value = response.data.records
      total.value = response.data.total
    }
    // 加载所有宿舍分配记录用于过滤
    const allRecords = await getDormitoryList({ pageNum: 1, pageSize: 1000 })
    if (allRecords.code === 200) {
      dormitoryRecords.value = allRecords.data.records
    }
  } catch (error) {
    console.error('加载宿舍分配列表失败:', error)
  }
}

const loadStudentList = async () => {
  try {
    const response = await getStudentList({ pageNum: 1, pageSize: 100 })
    if (response.code === 200) {
      studentList.value = response.data.records
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
  }
}

const loadRoomList = async () => {
  try {
    const response = await getRoomList({ pageNum: 1, pageSize: 200 })
    if (response.code === 200) {
      roomList.value = response.data.records
    }
  } catch (error) {
    console.error('加载房间列表失败:', error)
  }
}

const loadBuildingList = async () => {
  try {
    const response = await getBuildingList()
    if (response.code === 200) {
      buildingList.value = response.data
    }
  } catch (error) {
    console.error('加载楼栋列表失败:', error)
  }
}

const handleAdd = async () => {
  try {
    await assignDormitory(formData.value)
    showAddModal.value = false
    resetForm()
    loadDormitoryList()
    ElMessage.success('分配成功')
  } catch (error) {
    console.error('分配失败:', error)
    ElMessage.error('分配失败')
  }
}

const handleCheckout = async (id) => {
  try {
    await ElMessageBox.confirm('确定要办理退宿吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await checkoutDormitory(id)
    loadDormitoryList()
    ElMessage.success('退宿成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退宿失败:', error)
      ElMessage.error('退宿失败')
    }
  }
}

const resetForm = () => {
  formData.value = {
    studentId: '',
    roomId: ''
  }
}



const getStatusText = (status) => {
  if (status === 1) return '入住中'
  if (status === 0) return '已退宿'
  if (status === 3) return '未分配'
  return '未知'
}

const getStatusClass = (status) => {
  if (status === 1) return 'status-active'
  if (status === 0) return 'status-inactive'
  if (status === 3) return 'status-pending'
  return ''
}

onMounted(() => {
  loadDormitoryList()
  loadStudentList()
  loadRoomList()
  loadBuildingList()
})
</script>

<template>
  <div class="dormitory-management">
    <div class="page-header">
      <h2>宿舍分配管理</h2>
      <el-button type="primary" @click="showAddModal = true">分配宿舍</el-button>
    </div>
    
    <el-card class="data-card">
      <el-table :data="dormitoryList" border class="data-table" :empty-text="'暂无数据'">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="studentId" label="学生ID" width="80" />
        <el-table-column prop="studentName" label="学生姓名" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="dormitoryInfo" label="宿舍" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <span :class="getStatusClass(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入住时间" />
        <el-table-column prop="endTime" label="退宿时间" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button 
              type="text" 
              @click="handleCheckout(scope.row.id)"
              :disabled="scope.row.status !== 1"
              :class="{ 'disabled': scope.row.status !== 1 }"
            >
              退宿
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <Pagination
        :total="total"
        :page-num="pageNum"
        :page-size="pageSize"
        @change="loadDormitoryList"
      />
    </el-card>
    
    <!-- 分配弹窗 -->
    <el-dialog title="分配宿舍" v-model="showAddModal" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="选择学生">
          <el-select v-model="formData.studentId" required placeholder="请选择学生">
            <el-option
              v-for="student in getAssignableStudents()"
              :key="student.id"
              :label="`${student.realName} (${student.username})`"
              :value="student.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择房间">
          <el-select v-model="formData.roomId" required placeholder="请选择房间">
            <el-option
              v-for="room in roomList"
              :key="room.id"
              :label="`${room.buildingName || '未知楼栋'}${room.roomNumber}`"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dormitory-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.data-table {
  background: #fff;
  border-radius: 8px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.status-active {
  color: #67c23a;
  font-weight: bold;
}

.status-inactive {
  color: #909399;
}

.disabled {
  color: #ccc;
  cursor: not-allowed;
}
</style>