<!--
  房间管理视图
  
  功能说明：
  1. 管理宿舍房间信息
  2. 支持按楼栋筛选房间
  3. 支持添加、删除房间
  
  路由：/admin/rooms
-->
<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  getRoomList,
  getBuildingList,
  addRoom,
  toggleRoomStatus,
  deleteRoom
} from '../../api/admin.js'
import Pagination from '../../components/Common/Pagination.vue'
import DetailModal from '../../components/Common/DetailModal.vue'
import { useList } from '../../composables/useList'

const buildingList = ref([])

const {
  list: roomList,
  total,
  pagination,
  searchForm,
  loading,
  loadList
} = useList((params) => {
  return getRoomList({ buildingId: searchForm.buildingId || undefined, ...params })
})

searchForm.buildingId = ''

const showAddModal = ref(false)

const formData = reactive({
  buildingId: '',
  roomNumber: ''
})

const loadBuildingList = async () => {
  try {
    const response = await getBuildingList()
    if (response.code === 200) {
      buildingList.value = response.data
    }
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

const handleAdd = async () => {
  if (!formData.buildingId) {
    ElMessage.error('请选择楼栋')
    return
  }
  if (!formData.roomNumber) {
    ElMessage.error('请输入房间号')
    return
  }

  try {
    await addRoom(formData)
    showAddModal.value = false
    formData.buildingId = ''
    formData.roomNumber = ''
    loadList()
    ElMessage.success('添加成功')
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该房间吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteRoom(id)
    loadList()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      // axios拦截器已统一处理错误提示
    }
  }
}

const handleToggleStatus = async (id) => {
  try {
    await ElMessageBox.confirm('确定要切换状态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await toggleRoomStatus(id)
    loadList()
    ElMessage.success('操作成功')
  } catch (error) {
    if (error !== 'cancel') {
      // axios拦截器已统一处理错误提示
    }
  }
}

const getStatusText = (status) => {
  return status === 1 ? '启用' : '禁用'
}

const getStatusClass = (status) => {
  return status === 1 ? 'status-active' : 'status-inactive'
}

onMounted(() => {
  loadBuildingList()
  loadList()
})
</script>

<template>
  <div class="room-management">
    <div class="page-header">
      <h2>房间管理</h2>
      <el-button type="primary" @click="showAddModal = true">添加房间</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="searchForm.buildingId" placeholder="选择楼栋" @change="loadList">
        <el-option label="全部楼栋" value="" />
        <el-option
          v-for="building in buildingList"
          :key="building.id"
          :label="building.buildingName"
          :value="building.id"
        />
      </el-select>
    </div>

    <el-card class="data-card">
      <el-table :data="roomList" border class="data-table" :empty-text="'暂无数据'">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="buildingName" label="楼栋" />
        <el-table-column prop="roomNumber" label="房间号" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <span :class="getStatusClass(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="text" @click="handleToggleStatus(scope.row.id)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="text" style="color: #f56c6c" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        :page-num="pagination.pageNum"
        :page-size="pagination.pageSize"
        @change="loadList"
      />
    </el-card>

    <!-- 添加弹窗 -->
    <DetailModal title="添加房间" v-model="showAddModal" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="所属楼栋">
          <el-select v-model="formData.buildingId" placeholder="请选择楼栋">
            <el-option
              v-for="building in buildingList"
              :key="building.id"
              :label="building.buildingName"
              :value="building.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="formData.roomNumber" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </DetailModal>

  </div>
</template>

<style scoped>
.room-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.search-bar {
  margin-bottom: 16px;
}

.data-table {
  background: #fff;
  border-radius: 8px;
}

.status-active {
  color: #67c23a;
  font-weight: bold;
}

.status-inactive {
  color: #909399;
}
</style>