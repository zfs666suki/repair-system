<!--
  个人消息列表视图
  
  功能说明：
  1. 展示个人聊天消息列表（按联系人分组）
  2. 显示未读消息数量
  3. 支持选择联系人发起聊天
  4. 通过 WebSocket 接收实时消息推送
  
  路由：/student/messages
-->
<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { User } from '@element-plus/icons-vue'
import { getNotificationList, getUnreadCount, getUnreadCountByType, markChatMessagesAsRead } from '../../api/notification'
import { getUserList } from '../../api/student'
import { eventBus, EVENT_TYPES } from '../../utils/eventBus'

// 路由器实例
const router = useRouter()
// 消息列表（按联系人分组后的最新聊天记录）
const messageList = ref([])
// 未读消息数量
const unreadCount = ref(0)
// 是否显示用户选择对话框
const showUserList = ref(false)
// 可聊天的用户列表
const userList = ref([])

/**
 * 加载个人消息列表
 * 获取所有类型为2（个人聊天）的消息，按联系人分组并保留每个联系人的最新消息
 */
const loadMessages = async () => {
  try {
    const response = await getNotificationList({ pageNum: 1, pageSize: 50 })
    if (response.code === 200) {
      // 从sessionStorage获取当前用户信息
      const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
      const allMessages = response.data.records || []
      // 只保留个人聊天类型的消息（type === 2）
      const personalMessages = allMessages.filter(item => item.type === 2)

      // 按联系人ID分组，每个联系人只保留最新的消息
      const grouped = {}
      personalMessages.forEach(msg => {
        // 确定聊天对象的ID：如果relatedUserId是当前用户，则对方是userId；否则对方是relatedUserId
        let chatUserId = msg.relatedUserId
        if (msg.relatedUserId === userInfo.id) {
          chatUserId = msg.userId
        }

        // 如果该联系人还没有记录，或者当前消息时间更新，则更新该联系人的最新消息
        if (!grouped[chatUserId] || new Date(msg.createTime) > new Date(grouped[chatUserId].createTime)) {
          grouped[chatUserId] = msg
        }
      })

      // 将分组后的对象转换为数组，并按时间倒序排序（最新的在前）
      messageList.value = Object.values(grouped).sort((a, b) =>
        new Date(b.createTime) - new Date(a.createTime)
      )
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
  }
}

/**
 * 加载未读消息数量
 * 统计所有未读的个人聊天消息
 */
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

/**
 * 处理聊天点击事件
 * 标记与该用户的所有消息为已读，然后跳转到聊天页面
 * @param {object} item - 被点击的消息对象
 */
const handleChat = async (item) => {
  const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  // 确定聊天对象的ID
  let chatUserId = item.relatedUserId

  if (item.relatedUserId === userInfo.id) {
    chatUserId = item.userId
  }

  try {
    // 标记与该用户的所有聊天消息为已读
    await markChatMessagesAsRead(chatUserId)
    item.isRead = 1
    // 重新加载未读数量
    loadUnreadCount()
  } catch (error) {
    console.error('标记已读失败:', error)
  }

  // 跳转到聊天页面，传递用户ID和用户名
  router.push(`/student/chat?userId=${chatUserId}&userName=${item.chatUserName || '未知用户'}`)
}

/**
 * 根据角色码获取角色名称
 * @param {number|string} role - 角色码
 * @returns {string} 角色名称
 */
const getRoleName = (role) => {
  const roleMap = {
    1: '学生',
    2: '维修员',
    3: '管理员'
  }
  // 如果role是字符串，转换为数字
  const roleKey = typeof role === 'string' ? parseInt(role) : role
  return roleMap[roleKey] || '未知'
}

/**
 * 加载可聊天的用户列表
 */
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

/**
 * 开始新聊天
 * 关闭用户选择对话框，跳转到聊天页面
 * @param {object} user - 选中的用户对象
 */
const startChat = (user) => {
  showUserList.value = false
  router.push(`/student/chat?userId=${user.id}&userName=${user.realName || user.username}`)
}

/**
 * 监听showUserList变化，当对话框打开时加载用户列表
 * @param {boolean} newVal - showUserList的新值
 */
const handleShowUserList = (newVal) => {
  if (newVal) {
    loadUserList()
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

/**
 * 处理接收到新消息的事件
 * 重新加载消息列表和未读数量
 */
const handleMessageReceived = () => {
  loadMessages()
  loadUnreadCount()
}

// 组件挂载时初始化数据并启动定时刷新
onMounted(() => {
  loadMessages()
  loadUnreadCount()
  // 每30秒刷新一次未读数量
  interval = setInterval(loadUnreadCount, 30000)
  // 监听消息更新事件
  eventBus.on(EVENT_TYPES.NOTIFICATION_UPDATED, handleMessageReceived)
})

// 监听showUserList的变化，当打开对话框时加载用户列表
watch(showUserList, handleShowUserList)

// 组件卸载时清除定时器和事件监听器，避免内存泄漏
onUnmounted(() => {
  if (interval) {
    clearInterval(interval)
  }
  // 移除事件监听器
  eventBus.off(EVENT_TYPES.NOTIFICATION_UPDATED, handleMessageReceived)
})
</script>

<template>
  <div class="messages-view">
    <el-card>
      <template #header>
        <span>个人消息</span>
        <!-- 显示未读消息数量的徽章 -->
        <el-badge :value="unreadCount" class="notification-badge" />
        <!-- 发起新聊天按钮 -->
        <el-button
          type="primary"
          size="small"
          class="new-chat-btn"
          @click="showUserList = true"
        >
          发起新聊天
        </el-button>
      </template>

      <!-- 当没有消息时显示空状态 -->
      <el-empty v-if="messageList.length === 0" description="暂无个人消息" />

      <!-- 消息列表 -->
      <div v-else class="message-list">
        <div
          v-for="item in messageList"
          :key="item.id"
          class="message-item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleChat(item)"
        >
          <!-- 联系人头像：显示用户名的第一个字符 -->
          <div class="avatar">
            <span class="avatar-text">{{ (item.chatUserName || '未知').charAt(0) }}</span>
          </div>
          <!-- 消息内容区域 -->
          <div class="message-content">
            <div class="message-title">{{ item.chatUserName || '未知用户' }}</div>
            <div class="message-desc">{{ item.content }}</div>
            <div class="message-time">{{ formatTime(item.createTime) }} · {{ getRoleName(item.chatUserRole) }}</div>
          </div>
          <!-- 未读消息的红点标识 -->
          <div v-if="item.isRead === 0" class="unread-dot" />
        </div>
      </div>
    </el-card>

    <!-- 选择联系人对话框 -->
    <el-dialog title="选择联系人" v-model="showUserList" width="400px">
      <!-- 当用户列表为空时显示空状态 -->
      <el-empty v-if="userList.length === 0" description="暂无联系人" />
      <!-- 用户列表 -->
      <div v-else class="user-list">
        <div
          v-for="user in userList"
          :key="user.id"
          class="user-item"
          @click="startChat(user)"
        >
          <!-- 用户头像图标 -->
          <div class="avatar">
            <el-icon class="avatar-icon">
              <User />
            </el-icon>
          </div>
          <!-- 用户信息 -->
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
/* 消息视图容器样式 */
.messages-view {
  padding: 20px;
}

/* 未读数量徽章样式 */
.notification-badge {
  margin-left: 10px;
}

/* 消息列表滚动容器 */
.message-list {
  max-height: 600px;
  overflow-y: auto;
}

/* 单个消息项样式 */
.message-item {
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

/* 头像容器样式 */
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

/* 头像图标样式 */
.avatar-icon {
  font-size: 24px;
  color: #409eff;
}

/* 消息内容区域 */
.message-content {
  flex: 1;
  min-width: 0;
}

/* 消息标题样式 */
.message-title {
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
}

/* 消息描述文本样式 */
.message-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 消息时间和角色样式 */
.message-time {
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

/* 发起新聊天按钮右浮动 */
.new-chat-btn {
  float: right;
}

/* 用户列表滚动容器 */
.user-list {
  max-height: 300px;
  overflow-y: auto;
}

/* 用户列表项样式 */
.user-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;

  /* 鼠标悬停效果 */
  &:hover {
    background-color: #fafafa;
  }
}

/* 用户信息区域 */
.user-info {
  flex: 1;
}

/* 用户名称样式 */
.user-name {
  font-weight: 500;
  color: #303133;
}

/* 用户角色样式 */
.user-role {
  font-size: 12px;
  color: #909399;
}

/* 在线状态文本样式 */
.online-text {
  color: #67c23a;
  font-size: 12px;
}

/* 离线状态文本样式 */
.offline-text {
  color: #909399;
  font-size: 12px;
}

/* 离线状态徽章样式 */
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