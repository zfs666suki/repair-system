<!--
  学生端首页视图
  
  功能说明：
  1. 展示学生端首页仪表盘
  2. 显示宿舍信息和未读消息数量
  3. 提供快捷操作入口（提交报修、查看报修等）
  
  路由：/student
-->
<script setup>
// 导入Vue核心API和路由相关功能
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
// 导入用户状态管理store
import { useUserStore } from '../../stores/user'
// 导入宿舍信息和通知相关的API接口
import { getDormitoryInfo } from '../../api/repair'
import { getUnreadCountByType } from '../../api/notification'
// 导入通用操作卡片组件
import ActionCard from '../../components/Common/ActionCard.vue'

// 获取路由实例，用于页面跳转
const router = useRouter()
// 获取用户状态管理实例
const userStore = useUserStore()
// 宿舍信息数据，初始值为null
const dormitoryInfo = ref(null)
// 系统通知未读数量
const notificationUnreadCount = ref(0)
// 个人消息未读数量
const messageUnreadCount = ref(0)

/**
 * 加载未读消息数量
 * 同时获取系统通知和个人消息的未读数量
 */
const loadUnreadCounts = async () => {
  try {
    // 并行请求两种类型的未读数量：1-系统通知，2-个人消息
    const [notificationRes, messageRes] = await Promise.all([
      getUnreadCountByType(1),
      getUnreadCountByType(2)
    ])
    // 更新未读数量，如果返回空则默认为0
    // 为什么要加 .value？ 因为在 Vue 3 中，我们用 ref() 定义变量时，它像一个“盒子”。要拿到盒子里的东西，必须写 .value。
    notificationUnreadCount.value = notificationRes.data || 0
    messageUnreadCount.value = messageRes.data || 0
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

/**
 * 组件挂载时执行的生命周期钩子
 * 1. 获取当前用户的宿舍信息
 * 2. 加载未读消息数量
 */
onMounted(async () => {
  try {
    // 调用API获取宿舍信息
    const res = await getDormitoryInfo()
    dormitoryInfo.value = res.data
  } catch (error) {
    console.error('获取宿舍信息失败:', error)
  }
  // 等待未读数量加载完成
  await loadUnreadCounts()
})

/**
 * 跳转到提交报修页面
 */
const goToSubmitRepair = () => {
  router.push('/student/repair/submit')
}

/**
 * 跳转到我的报修列表页面
 */
const goToRepairList = () => {
  router.push('/student/repair/list')
}

/**
 * 跳转到系统通知页面
 */
const goToNotification = () => {
  router.push('/student/notification')
}

/**
 * 跳转到个人消息页面
 */
const goToMessages = () => {
  router.push('/student/messages')
}
</script>

<template>
  <!-- 学生首页主容器 -->
  <div class="student-home">
    <!-- 欢迎卡片区域 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="welcome-card">
          <h2>欢迎使用宿舍报修系统</h2>
          <p>您好，{{ userStore.userName }}同学</p>
          <!-- userName（Getter - 计算属性） -->
        </div>
      </el-col>
    </el-row>

    <!-- 个人信息和宿舍信息展示区域 -->
    <el-row :gutter="20" class="info-row">
      <!-- 个人信息卡片 -->
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <span>个人信息</span>
          </template>
          <div class="info-content">
            <p><strong>用户名：</strong>{{ userStore.userInfo?.username }}</p>
            <!-- userInfo?.username(原始属性) -->
            <p><strong>姓名：</strong>{{ userStore.userInfo?.realName }}</p>
            <p><strong>电话：</strong>{{ userStore.userInfo?.phone }}</p>
          </div>
        </el-card>
      </el-col>

      <!-- 宿舍信息卡片 -->
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <span>宿舍信息</span>
          </template>
          <!-- 有宿舍信息时显示 -->
          <div class="info-content" v-if="dormitoryInfo">
            <p><strong>楼栋：</strong>{{ dormitoryInfo.buildingName }}</p>
            <p><strong>房间号：</strong>{{ dormitoryInfo.roomNumber }}</p>
          </div>
          <!-- 无宿舍信息时显示提示 -->
          <div class="info-content" v-else>
            <p style="color: #999;">暂无宿舍信息</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要功能操作区域 - 第一行 -->
    <el-row :gutter="20" class="action-row">
      <!-- 提交报修功能入口 -->
      <el-col :span="12">
        <ActionCard title="提交报修" description="快速提交新的报修单" @click="goToSubmitRepair" />
      </el-col>
      <!-- 查看报修记录功能入口 -->
      <el-col :span="12">
        <ActionCard title="我的报修" description="查看报修记录及状态" @click="goToRepairList" />
      </el-col>
    </el-row>

    <!-- 消息通知功能区域 - 第二行 -->
    <el-row :gutter="20" class="action-row">
      <!-- 系统通知入口，显示未读数量角标 -->
      <el-col :span="12">
        <ActionCard title="系统通知" description="查看报修状态更新通知" 
          :badge="notificationUnreadCount" @click="goToNotification" />
      </el-col>
      <!-- 个人消息入口，显示未读数量角标 -->
      <el-col :span="12">
        <ActionCard title="个人消息" description="与维修员在线沟通" 
          :badge="messageUnreadCount" @click="goToMessages" />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
/* 学生首页主容器样式 */
.student-home {
  padding: 20px;
}

/* 欢迎卡片渐变背景样式 */
.welcome-card {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 20px;
}

/* 欢迎标题样式 */
.welcome-card h2 {
  margin: 0 0 10px 0;
}

/* 欢迎副标题样式 */
.welcome-card p {
  margin: 0;
  opacity: 0.9;
}

/* 信息行和操作行的间距 */
.info-row, .action-row {
  margin-bottom: 20px;
}

/* 信息卡片占满高度 */
.info-card {
  height: 100%;
}

/* 信息内容段落样式 */
.info-content p {
  margin: 10px 0;
  color: #333;
}
</style>