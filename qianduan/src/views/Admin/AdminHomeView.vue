<!--
  管理员端首页视图
  
  功能说明：
  1. 展示管理员端首页仪表盘
  2. 显示报修统计信息（总数、待处理、处理中、已完成）
  3. 显示用户数量统计（维修员、学生）
  4. 显示未读消息数量
  5. 提供快捷操作入口
  
  路由：/admin
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStatisticsSummary, getRepairmanList, getStudentList } from '../../api/admin.js'
import { getUnreadCountByType } from '../../api/notification'
import ActionCard from '../../components/Common/ActionCard.vue'

const router = useRouter()

const statistics = ref({
  totalCount: 0,
  pendingCount: 0,
  processingCount: 0,
  completedCount: 0
})

const repairmanCount = ref(0)
const studentCount = ref(0)

const notificationUnreadCount = ref(0)
const messageUnreadCount = ref(0)

const currentDate = new Date().toISOString().split('T')[0]

const loadStatistics = async () => {
  try {
    const response = await getStatisticsSummary({
      startDate: '2024-01-01',
      endDate: currentDate
    })
    if (response.code === 200) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadUserCounts = async () => {
  try {
    const [repairmanRes, studentRes] = await Promise.all([
      getRepairmanList({ pageNum: 1, pageSize: 1 }),
      getStudentList({ pageNum: 1, pageSize: 1 })
    ])
    if (repairmanRes.code === 200) {
      repairmanCount.value = repairmanRes.data.total
    }
    if (studentRes.code === 200) {
      studentCount.value = studentRes.data.total
    }
  } catch (error) {
    console.error('加载用户数量失败:', error)
  }
}

const goToRepairOrder = () => {
  router.push('/admin/repair-orders')
}

const goToUserManagement = () => {
  router.push('/admin/users')
}

const goToBuildingManagement = () => {
  router.push('/admin/buildings')
}

const goToRoomManagement = () => {
  router.push('/admin/rooms')
}

const goToDormitoryAllocation = () => {
  router.push('/admin/dormitories')
}

const goToFaultTypeManagement = () => {
  router.push('/admin/fault-types')
}

const goToStatistics = () => {
  router.push('/admin/statistics')
}

const goToExport = () => {
  router.push('/admin/export')
}

const goToNotification = () => {
  router.push('/admin/notification')
}

const goToMessages = () => {
  router.push('/admin/messages')
}

const loadUnreadCounts = async () => {
  try {
    const [notificationRes, messageRes] = await Promise.all([
      getUnreadCountByType(1),
      getUnreadCountByType(2)
    ])
    notificationUnreadCount.value = notificationRes.data || 0
    messageUnreadCount.value = messageRes.data || 0
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

onMounted(async () => {
  loadStatistics()
  loadUserCounts()
  await loadUnreadCounts()
})
</script>

<template>
  <div class="admin-home">
    <h2 class="page-header">管理员首页</h2>
    
    <div class="stats-grid">
      <div class="stat-card" @click="goToRepairOrder">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalCount }}</div>
          <div class="stat-label">总报修单</div>
        </div>
      </div>
      
      <div class="stat-card" @click="goToRepairOrder">
        <div class="stat-icon">⏳</div>
        <div class="stat-info">
          <div class="stat-value pending">{{ statistics.pendingCount }}</div>
          <div class="stat-label">待分配</div>
        </div>
      </div>
      
      <div class="stat-card" @click="goToRepairOrder">
        <div class="stat-icon">🔧</div>
        <div class="stat-info">
          <div class="stat-value processing">{{ statistics.processingCount }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      
      <div class="stat-card" @click="goToRepairOrder">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <div class="stat-value completed">{{ statistics.completedCount }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      
      <div class="stat-card" @click="goToUserManagement">
        <div class="stat-icon">👷</div>
        <div class="stat-info">
          <div class="stat-value">{{ repairmanCount }}</div>
          <div class="stat-label">维修员数量</div>
        </div>
      </div>
      
      <div class="stat-card" @click="goToUserManagement">
        <div class="stat-icon">👨‍🎓</div>
        <div class="stat-info">
          <div class="stat-value">{{ studentCount }}</div>
          <div class="stat-label">学生数量</div>
        </div>
      </div>
    </div>
    
    <div class="action-section">
      <h3 class="action-title">快捷操作</h3>
      
      <el-row :gutter="20" class="action-row">
        <el-col :span="12">
          <ActionCard title="报修管理" description="查看和管理报修订单" @click="goToRepairOrder" />
        </el-col>
        <el-col :span="12">
          <ActionCard title="用户管理" description="管理学生和维修员" @click="goToUserManagement" />
        </el-col>
      </el-row>
      
      <el-row :gutter="20" class="action-row">
        <el-col :span="12">
          <ActionCard title="楼栋管理" description="管理宿舍楼栋信息" @click="goToBuildingManagement" />
        </el-col>
        <el-col :span="12">
          <ActionCard title="房间管理" description="管理宿舍房间信息" @click="goToRoomManagement" />
        </el-col>
      </el-row>
      
      <el-row :gutter="20" class="action-row">
        <el-col :span="12">
          <ActionCard title="宿舍分配" description="分配学生宿舍" @click="goToDormitoryAllocation" />
        </el-col>
        <el-col :span="12">
          <ActionCard title="故障类型" description="管理故障类型信息" @click="goToFaultTypeManagement" />
        </el-col>
      </el-row>
      
      <el-row :gutter="20" class="action-row">
        <el-col :span="12">
          <ActionCard title="统计分析" description="查看报修统计数据" @click="goToStatistics" />
        </el-col>
        <el-col :span="12">
          <ActionCard title="数据导出" description="导出报修数据" @click="goToExport" />
        </el-col>
      </el-row>
      
      <el-row :gutter="20" class="action-row">
        <el-col :span="12">
          <ActionCard 
            title="系统通知" 
            description="查看系统消息" 
            :badge="notificationUnreadCount"
            @click="goToNotification" 
          />
        </el-col>
        <el-col :span="12">
          <ActionCard 
            title="个人消息" 
            description="与用户在线沟通" 
            :badge="messageUnreadCount"
            @click="goToMessages" 
          />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.admin-home {
  padding: 20px;
}

.page-header {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 40px;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-value.pending {
  color: #f5a623;
}

.stat-value.processing {
  color: #409EFF;
}

.stat-value.completed {
  color: #67c23a;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.action-section {
  margin-top: 20px;
}

.action-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.action-row {
  margin-bottom: 20px;
}
</style>
