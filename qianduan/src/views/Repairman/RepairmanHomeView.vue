<!--
  维修员端首页视图
  
  功能说明：
  1. 展示维修员端首页仪表盘
  2. 显示待接单、进行中订单数量
  3. 显示未读消息数量
  4. 提供快捷操作入口
  
  路由：/repairman
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getRepairList } from '../../api/repair'
import { getUnreadCountByType } from '../../api/notification'
import ActionCard from '../../components/Common/ActionCard.vue'

const router = useRouter()
const userStore = useUserStore()

const statistics = ref({
  pendingCount: 0,
  processingCount: 0
})

const notificationUnreadCount = ref(0)
const messageUnreadCount = ref(0)

const goToPendingOrders = () => {
  router.push('/repairman/orders/pending')
}

const goToProcessingOrders = () => {
  router.push('/repairman/orders/processing')
}

const goToHistoryOrders = () => {
  router.push('/repairman/orders/history')
}

const goToNotification = () => {
  router.push('/repairman/notification')
}

const goToMessages = () => {
  router.push('/repairman/messages')
}

const loadStatistics = async () => {
  try {
    // 获取待接单数量
    const pendingResponse = await getRepairList({ pageNum: 1, pageSize: 1, status: 2 })
    if (pendingResponse.code === 200) {
      statistics.value.pendingCount = pendingResponse.data.total || 0
    }
    
    // 获取进行中数量
    const processingResponse = await getRepairList({ pageNum: 1, pageSize: 1, status: 3 })
    if (processingResponse.code === 200) {
      statistics.value.processingCount = processingResponse.data.total || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
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
  await loadUnreadCounts()
})
</script>

<template>
  <div class="repairman-home">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="welcome-card">
          <h2>欢迎使用宿舍报修系统</h2>
          <p>您好，{{ userStore.userName }}师傅</p>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="info-row">
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <span>个人信息</span>
          </template>
          <div class="info-content">
            <p><strong>用户名：</strong>{{ userStore.userInfo?.username }}</p>
            <p><strong>姓名：</strong>{{ userStore.userInfo?.realName }}</p>
            <p><strong>电话：</strong>{{ userStore.userInfo?.phone }}</p>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <span>工作统计</span>
          </template>
          <div class="info-content">
            <p><strong>待接单：</strong><span class="pending-count">{{ statistics.pendingCount }}</span> 单</p>
            <p><strong>进行中：</strong><span class="processing-count">{{ statistics.processingCount }}</span> 单</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="action-row">
      <el-col :span="12">
        <ActionCard 
          title="待接单" 
          description="查看并处理待接单的报修单" 
          :badge="statistics.pendingCount"
          @click="goToPendingOrders" 
        />
      </el-col>
      <el-col :span="12">
        <ActionCard 
          title="进行中" 
          description="查看正在处理中的报修单" 
          :badge="statistics.processingCount"
          @click="goToProcessingOrders" 
        />
      </el-col>
    </el-row>

    <el-row :gutter="20" class="action-row">
      <el-col :span="12">
        <ActionCard title="维修历史" description="查看已完成的维修记录" @click="goToHistoryOrders" />
      </el-col>
      <el-col :span="12">
        <ActionCard 
          title="系统通知" 
          description="查看报修状态更新通知" 
          :badge="notificationUnreadCount"
          @click="goToNotification" 
        />
      </el-col>
    </el-row>

    <el-row :gutter="20" class="action-row">
      <el-col :span="24">
        <ActionCard 
          title="个人消息" 
          description="与学生在线沟通" 
          :badge="messageUnreadCount"
          :full-width="true"
          @click="goToMessages" 
        />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.repairman-home {
  padding: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.welcome-card h2 {
  margin: 0 0 10px 0;
}

.welcome-card p {
  margin: 0;
  opacity: 0.9;
}

.info-row, .action-row {
  margin-bottom: 20px;
}

.info-card {
  height: 100%;
}

.info-content p {
  margin: 10px 0;
  color: #333;
}

.info-content .pending-count {
  color: #409eff;
  font-weight: bold;
}

.info-content .processing-count {
  color: #E6A23C;
  font-weight: bold;
}
</style>
