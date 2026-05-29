/**
 * WebSocket 通信组合式函数模块
 * 
 * 功能说明：
 * 1. 封装 WebSocket 连接的生命周期管理
 * 2. 组件挂载时自动建立连接
 * 3. 组件卸载时自动关闭连接
 * 4. 处理接收到的消息并通过事件总线分发
 * 
 * 使用规范：
 * - 在布局组件（如 StudentLayout）中使用
 * - 无需手动管理连接，自动处理
 * - 消息通过 eventBus 分发到各个组件
 * 
 * 注意事项：
 * - 不要在多个组件中重复调用，建议在顶层布局组件中使用
 * - 依赖 sessionStorage 中的 userInfo.id
 */
import { onMounted, onUnmounted } from 'vue'
import { initWebSocket, closeWebSocket } from '../utils/websocket'
import { eventBus, EVENT_TYPES } from '../utils/eventBus'

/**
 * WebSocket 通信组合式函数
 * 用于在组件中统一管理 WebSocket 连接的初始化和销毁
 * @returns {void}
 */
export function useWebSocket() {
  /**
   * 处理接收到的 WebSocket 消息
   * 当收到服务器推送的消息时，通过事件总线通知其他组件
   * @param {object} message - 服务器推送的消息对象
   */
  const handleWebSocketMessage = (message) => {
    console.log('收到服务器推送消息:', message)
    
    // 只处理系统通知(type=1)和个人聊天(type=2)类型的消息
    if (message.type === 1 || message.type === 2) {
      // 发布"收到消息"事件，通知聊天相关组件更新
      eventBus.emit(EVENT_TYPES.MESSAGE_RECEIVED, message)
      // 发布"通知更新"事件，通知消息列表组件更新未读数
      eventBus.emit(EVENT_TYPES.NOTIFICATION_UPDATED)
    }
  }

  // 组件挂载时初始化 WebSocket 连接
  onMounted(() => {
    // 从 sessionStorage 获取当前用户信息
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    
    // 如果用户已登录（有用户ID），则建立 WebSocket 连接
    if (userInfo.id) {
      // 初始化 WebSocket，传入用户ID和消息处理回调函数
      initWebSocket(userInfo.id, handleWebSocketMessage)
    }
  })

  // 组件卸载时关闭 WebSocket 连接，避免资源泄漏
  onUnmounted(() => {
    closeWebSocket()
  })
}