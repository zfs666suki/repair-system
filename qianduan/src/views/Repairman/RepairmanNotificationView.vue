<!--
  维修员端系统通知视图
  
  功能说明：
  1. 展示系统通知列表
  2. 支持标记已读
  3. 自动刷新未读数量
  
  路由：/repairman/notification
-->
<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getNotificationList, markAsRead } from '../../api/notification'

const notificationList = ref([])
const unreadCount = ref(0)

const loadNotifications = async () => {
  try {
    const response = await getNotificationList({ pageNum: 1, pageSize: 100 })
    if (response.code === 200) {
      const allMessages = response.data.records || []
      notificationList.value = allMessages.filter(item => item.type === 1)
      unreadCount.value = notificationList.value.filter(item => item.isRead === 0).length
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
  }
}

const handleNotificationClick = async (item) => {
  if (item.isRead === 0) {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value--
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days === 1) {
    return '昨天'
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

let interval = null

onMounted(() => {
  loadNotifications()
  interval = setInterval(loadNotifications, 30000)
})

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
        <span>系统通知</span>
        <el-badge :value="unreadCount" class="notification-badge" />
      </template>

      <el-empty v-if="notificationList.length === 0" description="暂无消息" />

      <div v-else class="notification-items">
        <div
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleNotificationClick(item)"
        >
          <div class="notification-icon">
            <span class="icon-system">通知</span>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ item.title }}</div>
            <div class="notification-desc">{{ item.content }}</div>
            <div class="notification-time">{{ formatTime(item.createTime) }}</div>
          </div>
          <div v-if="item.isRead === 0" class="unread-dot" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.notification-list {
  padding: 20px;
}

.notification-badge {
  margin-left: 10px;
}

.notification-items {
  max-height: 600px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #fafafa;
  }

  &.unread {
    background-color: #fffbe6;
  }
}

.notification-icon {
  margin-right: 12px;
  font-size: 14px;
  color: #409eff;
  background-color: #ecf5ff;
  padding: 8px 12px;
  border-radius: 4px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
}

.notification-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: 12px;
  color: #c0c4cc;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background-color: #f56c6c;
  border-radius: 50%;
  margin-top: 8px;
}
</style>
