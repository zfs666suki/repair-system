<!--
  维修员端个人消息视图
  
  功能说明：
  1. 展示个人聊天消息列表
  2. 支持选择联系人发起聊天
  3. 通过 WebSocket 接收实时消息推送
  
  路由：/repairman/messages
-->
<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getNotificationList, getUnreadCountByType, markChatMessagesAsRead } from '../../api/notification'
import { getUserList } from '../../api/repair'
import { eventBus, EVENT_TYPES } from '../../utils/eventBus'

const router = useRouter()
const messageList = ref([])
const unreadCount = ref(0)
const showUserList = ref(false)
const userList = ref([])

const loadMessages = async () => {
  try {
    const response = await getNotificationList({ pageNum: 1, pageSize: 50 })
    if (response.code === 200) {
      const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
      const allMessages = response.data.records || []
      const personalMessages = allMessages.filter(item => item.type === 2)

      const grouped = {}
      personalMessages.forEach(msg => {
        let chatUserId = msg.relatedUserId
        if (msg.relatedUserId === userInfo.id) {
          chatUserId = msg.userId
        }

        if (!grouped[chatUserId] || new Date(msg.createTime) > new Date(grouped[chatUserId].createTime)) {
          grouped[chatUserId] = msg
        }
      })

      messageList.value = Object.values(grouped).sort((a, b) =>
        new Date(b.createTime) - new Date(a.createTime)
      )
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
  }
}

const loadUnreadCount = async () => {
  try {
    const response = await getUnreadCountByType(2)
    if (response.code === 200) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

const handleChat = async (item) => {
  const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  let chatUserId = item.relatedUserId

  if (item.relatedUserId === userInfo.id) {
    chatUserId = item.userId
  }

  try {
    await markChatMessagesAsRead(chatUserId)
    item.isRead = 1
    loadUnreadCount()
  } catch (error) {
    console.error('标记已读失败:', error)
  }

  router.push(`/repairman/chat?userId=${chatUserId}&userName=${item.chatUserName || '未知用户'}`)
}

const getRoleName = (role) => {
  const roleMap = {
    1: '学生',
    2: '维修员',
    3: '管理员'
  }
  const roleKey = typeof role === 'string' ? parseInt(role) : role
  return roleMap[roleKey] || '未知'
}

const loadUserList = async () => {
  try {
    const response = await getUserList()
    if (response.code === 200) {
      userList.value = response.data || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const startChat = (user) => {
  showUserList.value = false
  router.push(`/repairman/chat?userId=${user.id}&userName=${user.realName || user.username}`)
}

const handleShowUserList = (newVal) => {
  if (newVal) {
    loadUserList()
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

const handleMessageReceived = () => {
  loadMessages()
  loadUnreadCount()
}

onMounted(() => {
  loadMessages()
  loadUnreadCount()
  interval = setInterval(loadUnreadCount, 30000)
  eventBus.on(EVENT_TYPES.NOTIFICATION_UPDATED, handleMessageReceived)
})

watch(showUserList, handleShowUserList)

onUnmounted(() => {
  if (interval) {
    clearInterval(interval)
  }
  eventBus.off(EVENT_TYPES.NOTIFICATION_UPDATED, handleMessageReceived)
})
</script>

<template>
  <div class="messages-view">
    <el-card>
      <template #header>
        <span>个人消息</span>
        <el-badge :value="unreadCount" class="notification-badge" />
        <el-button
          type="primary"
          size="small"
          class="new-chat-btn"
          @click="showUserList = true"
        >
          发起新聊天
        </el-button>
      </template>

      <el-empty v-if="messageList.length === 0" description="暂无个人消息" />

      <div v-else class="message-list">
        <div
          v-for="item in messageList"
          :key="item.id"
          class="message-item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleChat(item)"
        >
          <div class="avatar">
            <span class="avatar-text">{{ (item.chatUserName || '未知').charAt(0) }}</span>
          </div>
          <div class="message-content">
            <div class="message-title">{{ item.chatUserName || '未知用户' }}</div>
            <div class="message-desc">{{ item.content }}</div>
            <div class="message-time">{{ formatTime(item.createTime) }} · {{ getRoleName(item.chatUserRole) }}</div>
          </div>
          <div v-if="item.isRead === 0" class="unread-dot" />
        </div>
      </div>
    </el-card>

    <el-dialog title="选择联系人" v-model="showUserList" width="400px">
      <el-empty v-if="userList.length === 0" description="暂无联系人" />
      <div v-else class="user-list">
        <div
          v-for="user in userList"
          :key="user.id"
          class="user-item"
          @click="startChat(user)"
        >
          <div class="avatar">
            <span class="avatar-text">{{ (user.realName || user.username || '未知').charAt(0) }}</span>
          </div>
          <div class="user-info">
            <div class="user-name">{{ user.realName || user.username }}</div>
            <div class="user-role">{{ getRoleName(user.role) }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.messages-view {
  padding: 20px;
}

.notification-badge {
  margin-left: 10px;
}

.message-list {
  max-height: 600px;
  overflow-y: auto;
}

.message-item {
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

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #e8f4fd;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.avatar-text {
  font-size: 20px;
  color: #409eff;
  font-weight: bold;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
}

.message-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
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

.new-chat-btn {
  float: right;
}

.user-list {
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #fafafa;
  }
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.user-role {
  font-size: 12px;
  color: #909399;
}

.online-text {
  color: #67c23a;
  font-size: 12px;
}

.offline-text {
  color: #909399;
  font-size: 12px;
}

.offline-badge {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 12px;
  height: 12px;
  background-color: #909399;
  border-radius: 50%;
  border: 2px solid #fff;
}
</style>
