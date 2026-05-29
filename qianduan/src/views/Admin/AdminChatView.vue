<!--
  管理员端聊天视图
  
  功能说明：
  1. 与指定用户的实时聊天界面
  2. 支持发送和接收消息
  3. 通过 WebSocket 接收实时消息推送
  
  路由：/admin/chat
-->
<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getChatMessages, sendChatMessage } from '../../api/notification'
import { eventBus, EVENT_TYPES } from '../../utils/eventBus'

const route = useRoute()
const router = useRouter()
const messagesContainer = ref(null)
const messages = ref([])
const messageContent = ref('')
const otherUserId = ref(0)
const otherUserName = ref('未知用户')

const isSelfMessage = (msg) => {
  const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  return msg.relatedUserId === userInfo.id
}

const getSenderName = (msg) => {
  const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  if (msg.relatedUserId === userInfo.id) {
    return userInfo.realName || userInfo.username || '我'
  }
  return msg.senderName || otherUserName.value || '未知用户'
}

const getAvatarText = (msg, isSelf) => {
  if (isSelf) {
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    return (userInfo.realName || userInfo.username || '我').charAt(0)
  }
  return (msg.senderName || otherUserName.value || '未').charAt(0)
}

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

const sendMessage = async () => {
  if (!messageContent.value.trim()) return

  try {
    await sendChatMessage({
      receiverId: otherUserId.value,
      title: '聊天消息',
      content: messageContent.value
    })
    messageContent.value = ''
    loadMessages()
  } catch (error) {
    console.error('发送消息失败:', error)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

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

const goBack = () => {
  router.push('/admin/messages')
}

const handleMessageReceived = (message) => {
  if (message.type === 2) {
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    if (message.userId === userInfo.id && message.relatedUserId === otherUserId.value) {
      loadMessages()
    }
  }
}

onMounted(() => {
  otherUserId.value = parseInt(route.query.userId) || 0
  otherUserName.value = route.query.userName || '未知用户'
  loadMessages()
  eventBus.on(EVENT_TYPES.MESSAGE_RECEIVED, handleMessageReceived)
})

onUnmounted(() => {
  eventBus.off(EVENT_TYPES.MESSAGE_RECEIVED, handleMessageReceived)
})
</script>

<template>
  <div class="chat-container">
    <el-card>
      <template #header>
        <span>与 {{ otherUserName }} 聊天</span>
        <el-button @click="goBack" type="text" size="small">返回消息列表</el-button>
      </template>

      <div class="chat-messages" ref="messagesContainer">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ self: isSelfMessage(msg) }"
        >
          <div class="message-avatar">
            <span class="avatar-text self-avatar">{{ getAvatarText(msg, true) }}</span>
          </div>
          <div class="message-content-wrap">
            <div class="message-sender">{{ getSenderName(msg) }}</div>
            <div class="message-bubble">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>
        </div>
      </div>

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
.chat-container {
  padding: 20px;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;

  &.self {
    flex-direction: row-reverse;

    .message-content-wrap {
      align-items: flex-end;
    }

    .message-sender {
      text-align: right;
      color: #67c23a;
    }

    .message-bubble {
      background-color: #409eff;
      color: white;
      border-radius: 12px 12px 0 12px;

      .message-time {
        color: rgba(255, 255, 255, 0.7);
      }
    }

    .avatar-text {
      background-color: #67c23a;
      color: white;
    }
  }

  &:not(.self) {
    .avatar-text {
      background-color: #409eff;
      color: white;
    }
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.avatar-text {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
}

.message-content-wrap {
  display: flex;
  flex-direction: column;
  margin: 0 12px;
  max-width: 70%;
}

.message-sender {
  font-size: 12px;
  color: #409eff;
  margin-bottom: 4px;
  font-weight: 500;
}

.message-bubble {
  padding: 12px 16px;
  background-color: white;
  border-radius: 12px 12px 12px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
}

.message-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.chat-input {
  display: flex;
  gap: 12px;

  :deep(.el-input) {
    flex: 1;
  }
}
</style>
