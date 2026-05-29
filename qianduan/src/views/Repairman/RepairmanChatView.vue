<!--
  维修员端聊天视图
  
  功能说明：
  1. 与指定用户的实时聊天界面
  2. 支持发送和接收消息
  3. 通过 WebSocket 接收实时消息推送
  
  路由：/repairman/chat
-->
<script setup>
// 导入 Vue 核心功能和路由相关依赖
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 导入聊天相关的 API 接口
import { getChatMessages, sendChatMessage } from '../../api/notification'
// 导入事件总线，用于接收实时消息通知
import { eventBus, EVENT_TYPES } from '../../utils/eventBus'

// 获取当前路由实例和路由器实例
const route = useRoute()
const router = useRouter()

// 聊天消息容器的 DOM 引用
const messagesContainer = ref(null)
// 存储聊天消息列表
const messages = ref([])
// 当前输入的消息内容
const messageContent = ref('')
// 聊天对方的用户 ID
const otherUserId = ref(0)
// 聊天对方的用户名
const otherUserName = ref('未知用户')

/**
 * 判断消息是否为当前用户发送
 * @param {Object} msg - 消息对象
 * @returns {Boolean} 是否为当前用户发送的消息
 */
const isSelfMessage = (msg) => {
  // 从 sessionStorage 中获取当前用户信息
  const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  return msg.relatedUserId === userInfo.id
}

/**
 * 获取消息发送者的显示名称
 * @param {Object} msg - 消息对象
 * @returns {String} 发送者名称
 */
const getSenderName = (msg) => {
  const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  // 如果是自己发送的消息，显示自己的名字
  if (msg.relatedUserId === userInfo.id) {
    return userInfo.realName || userInfo.username || '我'
  }
  // 否则显示对方的名字
  return msg.senderName || otherUserName.value || '未知用户'
}

/**
 * 获取当前用户的头像文字（姓名的首字符）
 * @returns {String} 头像文字
 */
const getSelfAvatar = () => {
  const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  return (userInfo.realName || userInfo.username || '我').charAt(0)
}

/**
 * 获取对方用户的头像文字（姓名的首字符）
 * @returns {String} 头像文字
 */
const getOtherAvatar = () => {
  return otherUserName.value.charAt(0)
}

/**
 * 加载聊天记录
 * 从服务器获取与当前聊天对象的完整对话历史
 */
const loadMessages = async () => {
  try {
    // 调用 API 获取聊天记录
    const response = await getChatMessages(otherUserId.value)
    if (response.code === 200) {
      messages.value = response.data
      // 加载完成后滚动到底部，显示最新消息
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  }
}

/**
 * 发送消息
 * 将用户输入的内容发送给指定的聊天对象
 */
const sendMessage = async () => {
  // 如果消息内容为空或只有空格，则不发送
  if (!messageContent.value.trim()) return

  try {
    // 调用 API 发送消息
    await sendChatMessage({
      receiverId: otherUserId.value,
      title: '聊天消息',
      content: messageContent.value
    })
    // 清空输入框
    messageContent.value = ''
    // 重新加载消息列表以显示刚发送的消息
    loadMessages()
  } catch (error) {
    console.error('发送消息失败:', error)
  }
}

/**
 * 滚动到消息容器底部
 * 确保最新消息始终可见
 */
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

/**
 * 格式化时间显示
 * @param {String} time - 时间字符串
 * @returns {String} 格式化后的时间（月-日 时:分）
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 返回消息列表页面
 */
const goBack = () => {
  router.push('/repairman/messages')
}

/**
 * 处理接收到的新消息事件
 * 当通过 WebSocket 收到新消息时触发
 * @param {Object} message - 接收到的消息对象
 */
const handleMessageReceived = (message) => {
  // 只处理聊天类型的消息（type === 2）
  if (message.type === 2) {
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    // 验证消息是否属于当前聊天会话
    if (message.userId === userInfo.id && message.relatedUserId === otherUserId.value) {
      // 重新加载消息列表以显示新消息
      loadMessages()
    }
  }
}

/**
 * 组件挂载时的初始化操作
 */
onMounted(() => {
  // 从路由参数中获取聊天对象的 ID 和名称
  otherUserId.value = parseInt(route.query.userId) || 0
  otherUserName.value = route.query.userName || '未知用户'
  // 加载历史聊天记录
  loadMessages()
  // 订阅消息接收事件，实现实时更新
  eventBus.on(EVENT_TYPES.MESSAGE_RECEIVED, handleMessageReceived)
})

/**
 * 组件卸载时的清理操作
 */
onUnmounted(() => {
  // 取消订阅消息接收事件，防止内存泄漏
  eventBus.off(EVENT_TYPES.MESSAGE_RECEIVED, handleMessageReceived)
})
</script>

<template>
  <div class="chat-container">
    <el-card>
      <!-- 卡片头部：显示聊天对象名称和返回按钮 -->
      <template #header>
        <span>与 {{ otherUserName }} 聊天</span>
        <el-button @click="goBack" type="text" size="small">返回消息列表</el-button>
      </template>

      <!-- 聊天消息展示区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <!-- 遍历渲染每条消息 -->
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ self: isSelfMessage(msg) }"
        >
          <!-- 消息头像 -->
          <div class="message-avatar">
            <span v-if="isSelfMessage(msg)" class="avatar-text self-avatar">{{ getSelfAvatar() }}</span>
            <span v-else class="avatar-text other-avatar">{{ getOtherAvatar() }}</span>
          </div>
          <!-- 消息内容和发送者信息 -->
          <div class="message-content-wrap">
            <div class="message-sender">{{ getSenderName(msg) }}</div>
            <div class="message-bubble">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 消息输入区域 -->
      <div class="chat-input">
        <el-input
          v-model="messageContent"
          placeholder="输入消息内容..."
          @keyup.enter="sendMessage"
        />
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
/* 聊天容器样式：设置高度和布局 */
.chat-container {
  padding: 20px;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

/* 消息列表容器：可滚动，带背景色 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

/* 单条消息项的样式 */
.message-item {
  display: flex;
  margin-bottom: 20px;

  /* 自己发送的消息：右对齐 */
  &.self {
    flex-direction: row-reverse;

    .message-content-wrap {
      align-items: flex-end;
    }

    .message-sender {
      text-align: right;
      color: #67c23a;
    }

    /* 自己的消息气泡：蓝色背景 */
    .message-bubble {
      background-color: #409eff;
      color: white;
      border-radius: 12px 12px 0 12px;

      .message-time {
        color: rgba(255, 255, 255, 0.7);
      }
    }

    /* 隐藏对方的头像 */
    .other-avatar {
      display: none;
    }
  }

  /* 对方发送的消息：左对齐 */
  &:not(.self) {
    .self-avatar {
      display: none;
    }
  }
}

/* 头像容器样式 */
.message-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

/* 头像文字样式：圆形背景 */
.avatar-text {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

/* 自己的头像：绿色背景 */
.self-avatar {
  background-color: #67c23a;
  color: white;
}

/* 对方的头像：灰色背景 */
.other-avatar {
  background-color: #909399;
  color: white;
}

/* 消息内容包装器 */
.message-content-wrap {
  display: flex;
  flex-direction: column;
  margin: 0 12px;
  max-width: 70%;
}

/* 发送者名称样式 */
.message-sender {
  font-size: 12px;
  color: #409eff;
  margin-bottom: 4px;
  font-weight: 500;
}

/* 消息气泡样式 */
.message-bubble {
  padding: 12px 16px;
  background-color: white;
  border-radius: 12px 12px 12px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 消息文本样式 */
.message-text {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
}

/* 消息时间样式 */
.message-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

/* 输入区域样式：弹性布局 */
.chat-input {
  display: flex;
  gap: 12px;

  :deep(.el-input) {
    flex: 1;
  }
}
</style>