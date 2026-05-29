<!--
  系统通知列表视图
  
  功能说明：
  1. 展示系统通知列表
  2. 支持标记已读
  3. 自动刷新未读数量
  
  路由：/student/notification
-->
<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Bell, Message } from '@element-plus/icons-vue'
import { getNotificationList, getUnreadCount, markAsRead } from '../../api/notification'

// 消息通知列表数据
const notificationList = ref([])
// 未读消息数量
const unreadCount = ref(0)

/**
 * 加载消息通知列表
 * 获取所有消息后，过滤出类型为1（系统通知）的消息
 */
const loadNotifications = async () => {
  try {
    const response = await getNotificationList({ pageNum: 1, pageSize: 100 })
    if (response.code === 200) {
      const allMessages = response.data.records || []
      // 只保留系统通知类型的消息（type === 1）
      notificationList.value = allMessages.filter(item => item.type === 1)
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
  }
}

/**
 * 加载未读消息数量
 * 统计所有未读的系统通知消息
 */
const loadUnreadCount = async () => {
  try {
    const response = await getNotificationList({ pageNum: 1, pageSize: 100 })
    if (response.code === 200) {
      const allMessages = response.data.records || []
      // 统计未读的系统通知数量（type === 1 且 isRead === 0）
      unreadCount.value = allMessages.filter(item => item.type === 1 && item.isRead === 0).length
    }
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

/**
 * 处理消息点击事件
 * 如果消息未读，则标记为已读并更新未读数量
 * @param {object} item - 被点击的消息对象
 */
const handleNotificationClick = async (item) => {
  if (item.isRead === 0) {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value--
  }
}

/**
 * 格式化时间显示
 * 根据时间与当前的差距，智能显示时间格式：
 * - 当天：显示时:分
 * - 昨天：显示"昨天"
 * - 更早：显示月/日
 * @param {string} time - 时间字符串
 * @returns {string} 格式化后的时间字符串
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    // 当天：显示时:分
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days === 1) {
    // 昨天
    return '昨天'
  } else {
    // 更早的日期：显示月/日
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

// 定时器引用，用于定期刷新未读数量
let interval = null

// 组件挂载时初始化数据并启动定时刷新
onMounted(() => {
  loadNotifications()
  loadUnreadCount()
  // 每30秒刷新一次未读数量
  interval = setInterval(loadUnreadCount, 30000)
})

// 组件卸载时清除定时器，避免内存泄漏
onUnmounted(() => {
  if (interval) {
    clearInterval(interval)
  }
})
</script>

<template>
  <div class="notification-list">
    <el-card>
      <template #header>
        <span>消息通知</span>
        <!-- 显示未读消息数量的徽章 -->
        <el-badge :value="unreadCount" class="notification-badge" />
      </template>

      <!-- 当没有消息时显示空状态 -->
      <el-empty v-if="notificationList.length === 0" description="暂无消息" />

      <!-- 消息列表 -->
      <div v-else class="notification-items">
        <div
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleNotificationClick(item)"
        >
          <!-- 消息图标：系统通知显示铃铛，聊天消息显示消息图标 -->
          <div class="notification-icon">
            <el-icon v-if="item.type === 1" class="icon-system">
              <Bell />
            </el-icon>
            <el-icon v-else class="icon-chat">
              <Message />
            </el-icon>
          </div>
          <!-- 消息内容区域 -->
          <div class="notification-content">
            <div class="notification-title">{{ item.title }}</div>
            <div class="notification-desc">{{ item.content }}</div>
            <div class="notification-time">{{ formatTime(item.createTime) }}</div>
          </div>
          <!-- 未读消息的红点标识 -->
          <div v-if="item.isRead === 0" class="unread-dot" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
/* 消息通知列表容器样式 */
.notification-list {
  padding: 20px;
}

/* 未读数量徽章样式 */
.notification-badge {
  margin-left: 10px;
}

/* 消息列表滚动容器 */
.notification-items {
  max-height: 600px;
  overflow-y: auto;
}

/* 单个消息项样式 */
.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;

  /* 鼠标悬停效果 */
  &:hover {
    background-color: #fafafa;
  }

  /* 未读消息背景色 */
  &.unread {
    background-color: #fffbe6;
  }
}

/* 消息图标容器 */
.notification-icon {
  margin-right: 12px;
  font-size: 24px;

  /* 系统通知图标颜色 */
  .icon-system {
    color: #409eff;
  }

  /* 聊天消息图标颜色 */
  .icon-chat {
    color: #67c23a;
  }
}

/* 消息内容区域 */
.notification-content {
  flex: 1;
  min-width: 0;
}

/* 消息标题样式 */
.notification-title {
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
}

/* 消息描述文本样式 */
.notification-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 消息时间样式 */
.notification-time {
  font-size: 12px;
  color: #c0c4cc;
}

/* 未读消息红点标识 */
.unread-dot {
  width: 8px;
  height: 8px;
  background-color: #f56c6c;
  border-radius: 50%;
  margin-top: 8px;
}
</style>