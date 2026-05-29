/**
 * WebSocket 连接管理模块
 * 
 * 功能说明：
 * 1. 提供 WebSocket 连接的创建、管理、关闭功能
 * 2. 自动处理连接断开后的重连（5秒间隔）
 * 3. 提供连接状态查询
 * 
 * 使用规范：
 * - initWebSocket(): 初始化连接，传入用户ID和消息回调
 * - closeWebSocket(): 关闭连接，释放资源
 * - getConnectionStatus(): 获取当前连接状态
 * 
 * 注意事项：
 * - 不要在组件中直接调用此模块，应通过 useWebSocket 组合式函数使用
 * - 连接断开会自动重连，无需手动处理
 */
import { ref } from 'vue'

// WebSocket 实例
let ws = null
// 连接状态
const isConnected = ref(false)
// 消息回调函数
let messageCallback = null

/**
 * 初始化 WebSocket 连接
 * @param {number} userId - 用户ID
 * @param {function} callback - 收到消息时的回调函数
 */
export function initWebSocket(userId, callback) {
  // 如果已连接，先断开
  if (ws) {
    ws.close()
    ws = null
  }

  // 保存回调函数
  messageCallback = callback

  // 构建 WebSocket 地址
  // 后端服务器地址，从环境变量获取或使用默认值
  const backendHost = import.meta.env.VITE_WS_HOST || 'localhost:8080'
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
   //如果网页是 https://，WebSocket 要用 wss://（加密）
   //如果网页是 http://，WebSocket 用 ws://（不加密）
  const url = `${protocol}//${backendHost}/ws/${userId}`
  // 构建 WebSocket 地址，包含用户ID
  // 假设用户ID为 5，那么 WebSocket 地址就是 ws://localhost:8080/ws/5

  //创建 WebSocket 连接
  //向服务器发起连接请求，创建一个 WebSocket 对象，存储在 ws 变量中
  ws = new WebSocket(url)

  // 连接成功时触发
  ws.onopen = function() {
    console.log('WebSocket 连接成功, URL:', url)
    isConnected.value = true
  }

  // 收到消息时触发
  //- WebSocket 连接建立后，会 持续监听 后端推送的消息
  //一旦后端发送消息， ws.onmessage 会 自动触发， 无需手动轮询，真正的 实时推送
  ws.onmessage = function(event) {
    //event 对象包含收到的数据。
    console.log('=== 收到 WebSocket 消息 ===')
    console.log('原始消息:', event.data)
    try {
      //服务器发送的消息是字符串格式的 JSON，需要解析成 JavaScript 对象。
      const message = JSON.parse(event.data)
      console.log('解析后的消息:', message)
      // 如果有消息回调函数，调用它处理消息
      if (messageCallback) {
        console.log('调用消息回调...')
        messageCallback(message)
      }
    } catch (error) {
      console.error('解析 WebSocket 消息失败:', error)
    }
  }

  // 连接关闭时触发
  ws.onclose = function() {
    console.log('WebSocket 连接关闭')
    isConnected.value = false
    // 尝试重连
    setTimeout(() => {
      initWebSocket(userId, callback)
    }, 5000)
  }

  // 连接错误时触发
  ws.onerror = function(error) {
    console.error('WebSocket 连接错误:', error)
    isConnected.value = false
  }
}

/**
 * 关闭 WebSocket 连接
 */
export function closeWebSocket() {
  // 如果已连接，先断开
  if (ws) {
    ws.close()
    ws = null
    isConnected.value = false
  }
}

/**
 * 获取连接状态
 */
export function getConnectionStatus() {
  return isConnected.value
}
