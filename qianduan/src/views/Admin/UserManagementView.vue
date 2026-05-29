<!--
  用户管理视图
  
  功能说明：
  1. 管理维修员和学生用户
  2. 支持添加、编辑、禁用/启用用户
  3. 支持分页查看用户列表
  
  路由：/admin/users
-->
<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  getRepairmanList,
  getStudentList,
  addRepairman,
  addStudent,
  updateRepairman,
  updateStudent,
  toggleRepairmanStatus,
  toggleStudentStatus
} from '../../api/admin.js'
import Pagination from '../../components/Common/Pagination.vue'
import DetailModal from '../../components/Common/DetailModal.vue'
import { useList } from '../../composables/useList'

const router = useRouter()
const activeTab = ref('repairman')

const showAddModal = ref(false)
const showEditModal = ref(false)
const currentUserId = ref(null)

const formData = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  className: ''
})

const {
  list: userList,
  total,
  pagination,
  searchForm,
  loading,
  loadList,
  handlePageChange
} = useList((params) => {
  if (activeTab.value === 'repairman') {
    return getRepairmanList(params)
  } else {
    return getStudentList(params)
  }
})

searchForm.className = ''

watch(activeTab, () => {
  loadList()
})

const handleAdd = async () => {
  if (!formData.username) {
    ElMessage.error('请输入用户名')
    return
  }
  if (formData.username.length < 3) {
    ElMessage.error('用户名至少3个字符')
    return
  }
  if (!formData.password) {
    ElMessage.error('请输入密码')
    return
  }
  if (formData.password.length < 6) {
    ElMessage.error('密码至少6个字符')
    return
  }
  if (!formData.realName) {
    ElMessage.error('请输入姓名')
    return
  }

  try {
    if (activeTab.value === 'repairman') {
      await addRepairman(formData)
    } else {
      await addStudent(formData)
    }
    showAddModal.value = false
    formData.username = ''
    formData.password = ''
    formData.realName = ''
    formData.phone = ''
    formData.className = ''
    loadList()
    ElMessage.success('添加成功')
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

const handleEdit = async () => {
  if (!formData.realName) {
    ElMessage.error('请输入姓名')
    return
  }

  try {
    const data = { ...formData }
    delete data.password
    delete data.username
    if (activeTab.value === 'repairman') {
      await updateRepairman(currentUserId.value, data)
    } else {
      await updateStudent(currentUserId.value, data)
    }
    showEditModal.value = false
    formData.username = ''
    formData.password = ''
    formData.realName = ''
    formData.phone = ''
    formData.className = ''
    loadList()
    ElMessage.success('修改成功')
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

const handleToggleStatus = async (id) => {
  try {
    await ElMessageBox.confirm('确定要切换状态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    if (activeTab.value === 'repairman') {
      await toggleRepairmanStatus(id)
    } else {
      await toggleStudentStatus(id)
    }
    loadList()
    ElMessage.success('操作成功')
  } catch (error) {
    if (error !== 'cancel') {
      // axios拦截器已统一处理错误提示
    }
  }
}

const handleEditClick = (user) => {
  currentUserId.value = user.id
  formData.username = user.username
  formData.password = ''
  formData.realName = user.realName
  formData.phone = user.phone || ''
  formData.className = user.className || ''
  showEditModal.value = true
}

const handleImport = () => {
  router.push('/admin/users/import')
}

const getStatusText = (status) => {
  return status === 1 ? '启用' : '禁用'
}

const getStatusClass = (status) => {
  return status === 1 ? 'status-active' : 'status-inactive'
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <div class="tab-container">
      <el-tabs v-model="activeTab" @tab-change="loadList">
        <el-tab-pane label="维修员管理" name="repairman">
          <div class="table-header">
            <el-button type="primary" @click="showAddModal = true">添加维修员</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="学生管理" name="student">
          <div class="table-header">
            <el-button type="primary" @click="showAddModal = true">添加学生</el-button>
            <el-button type="success" @click="handleImport">批量导入</el-button>
            <el-input
              v-model="searchForm.className"
              placeholder="按班级搜索"
              class="search-input"
              @keyup.enter="loadList"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-card class="data-card">
      <el-table :data="userList" border class="data-table" :empty-text="'暂无数据'">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="className" label="班级" v-if="activeTab === 'student'" />

        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <span :class="getStatusClass(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="text" @click="handleEditClick(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleToggleStatus(scope.row.id)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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

    <!-- 添加弹窗 -->
    <DetailModal :title="activeTab === 'repairman' ? '添加维修员' : '添加学生'" v-model="showAddModal" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="formData.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="formData.password" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="formData.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="班级" v-if="activeTab === 'student'">
          <el-input v-model="formData.className" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </DetailModal>

    <!-- 编辑弹窗 -->
    <DetailModal :title="activeTab === 'repairman' ? '编辑维修员' : '编辑学生'" v-model="showEditModal" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="formData.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="formData.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="班级" v-if="activeTab === 'student'">
          <el-input v-model="formData.className" />
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
.user-management {
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

.tab-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.table-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.search-input {
  width: 200px;
  margin-left: auto;
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