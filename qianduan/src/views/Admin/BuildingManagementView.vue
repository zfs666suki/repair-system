<!--
  楼栋管理视图
  
  功能说明：
  1. 管理宿舍楼栋信息
  2. 支持添加、编辑、删除楼栋
  3. 支持分配负责维修员
  
  路由：/admin/buildings
-->
<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  getBuildingList,
  addBuilding,
  updateBuilding,
  toggleBuildingStatus,
  deleteBuilding,
  getRepairmanList
} from '../../api/admin.js'
import DetailModal from '../../components/Common/DetailModal.vue'

const buildingList = ref([])
const repairmanList = ref([])
const showAddModal = ref(false)
const showEditModal = ref(false)
const currentBuildingId = ref(null)

const formData = reactive({
  buildingName: '',
  repairUserId: ''
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

const loadRepairmanList = async () => {
  try {
    const response = await getRepairmanList({ pageNum: 1, pageSize: 100 })
    if (response.code === 200) {
      repairmanList.value = response.data.records || []
    }
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

const handleAdd = async () => {
  if (!formData.buildingName) {
    ElMessage.error('请输入楼栋名称')
    return
  }

  try {
    await addBuilding(formData)
    showAddModal.value = false
    formData.buildingName = ''
    formData.repairUserId = ''
    loadBuildingList()
    ElMessage.success('添加成功')
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

const handleEdit = async () => {
  if (!formData.buildingName) {
    ElMessage.error('请输入楼栋名称')
    return
  }

  try {
    await updateBuilding(currentBuildingId.value, formData)
    showEditModal.value = false
    formData.buildingName = ''
    formData.repairUserId = ''
    loadBuildingList()
    ElMessage.success('修改成功')
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

const handleEditClick = (building) => {
  currentBuildingId.value = building.id
  formData.buildingName = building.buildingName
  formData.repairUserId = building.repairUserId || ''
  showEditModal.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该楼栋吗？删除后将无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteBuilding(id)
    loadBuildingList()
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
    await toggleBuildingStatus(id)
    loadBuildingList()
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

const formatUpdateTime = (row) => {
  if (!row.updateTime) return ''
  if (row.createTime === row.updateTime) return ''
  return row.updateTime
}

onMounted(() => {
  loadBuildingList()
  loadRepairmanList()
})
</script>

<template>
  <div class="building-management">
    <div class="page-header">
      <h2>楼栋管理</h2>
      <el-button type="primary" @click="showAddModal = true">添加楼栋</el-button>
    </div>

    <el-table :data="buildingList" border class="data-table" :empty-text="'暂无数据'">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="buildingName" label="楼栋名称" />
      <el-table-column prop="repairUserName" label="维修员" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <span :class="getStatusClass(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column prop="updateTime" label="更新时间" :formatter="formatUpdateTime" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button type="text" @click="handleEditClick(scope.row)">编辑</el-button>
          <el-button type="text" @click="handleDelete(scope.row.id)">删除</el-button>
          <el-button type="text" @click="handleToggleStatus(scope.row.id)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加弹窗 -->
    <DetailModal title="添加楼栋" v-model="showAddModal" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="楼栋名称">
          <el-input v-model="formData.buildingName" />
        </el-form-item>
        <el-form-item label="维修员">
          <el-select v-model="formData.repairUserId" placeholder="请选择维修员">
            <el-option
              v-for="repairman in repairmanList"
              :key="repairman.id"
              :label="repairman.realName"
              :value="repairman.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </DetailModal>

    <!-- 编辑弹窗 -->
    <DetailModal title="编辑楼栋" v-model="showEditModal" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="楼栋名称">
          <el-input v-model="formData.buildingName" />
        </el-form-item>
        <el-form-item label="维修员">
          <el-select v-model="formData.repairUserId" placeholder="请选择维修员">
            <el-option
              v-for="repairman in repairmanList"
              :key="repairman.id"
              :label="repairman.realName"
              :value="repairman.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="handleEdit">确定</el-button>
      </template>
    </DetailModal>

  </div>
</template>

<style scoped>
.building-management {
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

.status-active {
  color: #67c23a;
  font-weight: bold;
}

.status-inactive {
  color: #909399;
}
</style>