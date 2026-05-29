<!--
  聊天视图
  
  功能说明：
  1. 与指定用户的实时聊天界面
  2. 支持发送和接收消息
  3. 自动滚动到最新消息
  4. 通过 WebSocket 接收实时消息推送
  
  路由：/student/chat
-->
<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User } from '@element-plus/icons-vue'
import { getChatMessages, sendChatMessage } from '../../api/notification'
import { eventBus, EVENT_TYPES } from '../../utils/eventBus'

// 路由实例和路由器实例
const route = useRoute()
const router = useRouter()
// 消息容器DOM引用，用于滚动到底部
const messagesContainer = ref(null)
// 聊天记录列表
const messages = ref([])
// 输入框中的消息内容
const messageContent = ref('')
// 聊天对象的ID
const otherUserId = ref(0)
// 聊天对象的名称
const otherUserName = ref('未知用户')

/**
 * 判断消息是否是当前用户发送的
 * @param {object} msg - 消息对象
 * @returns {boolean} 如果是当前用户发送的消息返回true
 */
const isSelfMessage = (msg) => {
  const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  return msg.relatedUserId === userInfo.id
}

/**
 * 获取消息发送者的名称
 * @param {object} msg - 消息对象
 * @returns {string} 发送者名称
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
 * 加载聊天记录
 * 获取与指定用户的聊天历史并滚动到底部
 */
const loadMessages = async () => {
  try {
    const response = await getChatMessages(otherUserId.value)
    if (response.code === 200) {
      messages.value = response.data
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  }
}

/**
 * 发送消息
 * 验证消息内容不为空后，调用API发送消息并刷新聊天记录
 */
const sendMessage = async () => {
  // 如果消息内容为空或只有空格，不发送
  if (!messageContent.value.trim()) return

  try {
    await sendChatMessage({
      receiverId: otherUserId.value,
      title: '聊天消息',
      content: messageContent.value
    })
    // 清空输入框
    messageContent.value = ''
    // 重新加载聊天记录以显示新消息
    loadMessages()
  } catch (error) {
    console.error('发送消息失败:', error)
  }
}

/**
 * 滚动到消息容器底部
 * 使用nextTick确保DOM更新后再滚动
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
 * 将时间格式化为"月/日 时:分"的格式
 * @param {string} time - 时间字符串
 * @returns {string} 格式化后的时间字符串
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
  router.push('/student/messages')
}

/**
 * 处理接收到新消息的事件
 * 如果新消息是与当前聊天对象相关的，则刷新聊天记录
 * @param {object} message - 接收到的消息对象
 */
const handleMessageReceived = (message) => {
  // 只处理个人聊天类型的消息（type === 2）
  if (message.type === 2) {
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    // 如果消息是当前用户发送给对方，或者对方发送给当前用户的，则刷新聊天记录
    if (message.userId === userInfo.id && message.relatedUserId === otherUserId.value) {
      loadMessages()
    }
  }
}

// 组件挂载时初始化数据并监听消息事件
onMounted(() => {
  // 从路由参数中获取聊天对象ID和名称
  otherUserId.value = parseInt(route.query.userId) || 0
  otherUserName.value = route.query.userName || '未知用户'
  // 加载聊天记录
  loadMessages()
  // 监听消息接收事件
  eventBus.on(EVENT_TYPES.MESSAGE_RECEIVED, handleMessageReceived)
})

// 组件卸载时移除事件监听器，避免内存泄漏
onUnmounted(() => {
  eventBus.off(EVENT_TYPES.MESSAGE_RECEIVED, handleMessageReceived)
})
</script>

<template>
  <div class="chat-container">
    <el-card>
      <template #header>
        <!-- 显示聊天对象名称 -->
        <span>与 {{ otherUserName }} 聊天</span>
        <!-- 返回消息列表按钮 -->
        <el-button @click="goBack" type="text" size="small">返回消息列表</el-button>
      </template>

      <!-- 消息展示区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ self: isSelfMessage(msg) }"
        >
          <!-- 消息头像：根据是否是自己发送的消息显示不同样式 -->
          <div class="message-avatar">
            <el-icon v-if="isSelfMessage(msg)" class="avatar-icon self-avatar">
              <User />
            </el-icon>
            <el-icon v-else class="avatar-icon other-avatar">
              <User />
            </el-icon>
          </div>
          <!-- 消息内容包裹层 -->
          <div class="message-content-wrap">
            <div class="message-sender">{{ getSenderName(msg) }}</div>
            <!-- 消息气泡 -->
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

/* 消息展示区域样式 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

/* 单个消息项样式 */
.message-item {
  display: flex;
  margin-bottom: 20px;

  /* 自己发送的消息样式：右对齐 */
  &.self {
    flex-direction: row-reverse;

    .message-content-wrap {
      align-items: flex-end;
    }

    .message-sender {
      text-align: right;
      color: #67c23a;
    }

    /* 自己的消息气泡：蓝色背景，白色文字 */
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

  /* 对方发送的消息样式 */
  &:not(.self) {
    /* 隐藏自己的头像 */
    .self-avatar {
      display: none;
    }
  }
}

/* 消息头像容器样式 */
.message-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

/* 头像图标样式 */
.avatar-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

/* 自己的头像样式：绿色背景 */
.self-avatar {
  background-color: #67c23a;
  color: white;
}

/* 对方的头像样式：灰色背景 */
.other-avatar {
  background-color: #909399;
  color: white;
}

/* 消息内容包裹层样式 */
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

/* 消息输入区域样式 */
.chat-input {
  display: flex;
  gap: 12px;

  /* 输入框占据剩余空间 */
  :deep(.el-input) {
    flex: 1;
  }
}
</style>