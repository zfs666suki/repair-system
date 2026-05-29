/**
 * 事件总线模块
 * 
 * 功能说明：
 * 1. 提供组件间解耦通信机制
 * 2. 支持事件的订阅、发布、取消订阅
 * 3. 主要用于 WebSocket 消息的分发
 * 
 * 使用规范：
 * - on(): 在组件 onMounted 中订阅事件
 * - emit(): 在需要通知其他组件时发布事件
 * - off(): 在组件 onUnmounted 中取消订阅，防止内存泄漏
 * 
 * 预定义事件类型：
 * - MESSAGE_RECEIVED: 收到新消息
 * - NOTIFICATION_UPDATED: 通知更新
 * - CHAT_MESSAGE: 聊天消息
 */
// 事件总线，用于组件间通信
import { ref } from 'vue'

// 创建一个简单的事件总线
const listeners = ref({})

export const eventBus = {
  /**
   * 订阅事件
   * @param {string} eventName - 事件名称
   * @param {function} callback - 回调函数 事件发生时自动调用
   */
  /*   第一步：函数参数
  on(eventName, callback)  //我想监听"收到消息"这个事件，当发生时执行一个函数
  eventBus.on('message_received', function(data) {
  console.log('收到了新消息:', data)
  }) */
  /*   第二步：检查事件是否已经存在
  listeners 是一个对象，用来存储所有的事件和它们的回调函数。结构是这样的：
  listeners.value = {
  'message_received': [函数1, 函数2],  // 多个组件都监听了这个事件
  'notification_updated': [函数3]
  } */
  on(eventName, callback) {
    if (!listeners.value[eventName]) {
      listeners.value[eventName] = []
    }
    // 第三步：将回调函数添加到事件数组中
    listeners.value[eventName].push(callback)
  },

  /**
   * 发布事件
   * @param {string} eventName - 事件名称
   * @param {any} data - 事件数据 要传递给订阅者的数据，比如新消息的内容
   */
  /*   第一步：函数参数
  emit(eventName, data)  // 发布一个"收到消息"的事件，并传递消息数据
  eventBus.emit('message_received', { 
  content: '你好',
  from: '张三'
  }) */
  // 第二步：检查是否有订阅者订阅了这个事件
  emit(eventName, data) {
    if (listeners.value[eventName]) {
      // 第三步：遍历所有订阅者，调用他们的回调函数
      listeners.value[eventName].forEach(callback => {
        try {
          // 第四步：调用回调函数，传递事件数据
          callback(data)
          //执行结果为 输出：收到了新消息: { content: '你好', from: '张三' }
        } catch (error) {
          console.error('事件处理失败:', error)
        }
      })
    }
  },

  /**
   * 取消订阅
   * @param {string} eventName - 要取消订阅的事件名称
   * @param {function} callback - 要取消的回调函数（可选，不传则取消所有订阅）
   */
  /*组件A在页面加载时订阅了 'message_received' 事件，当用户离开这个页面（组件被销毁）时，如果不清除订阅
  即使组件已经不存在了，事件触发时还会尝试执行它的回调函数这会导致内存泄漏和错误，所以：组件销毁时，必须调用 off() 来取消订阅。*/
  /* 第一步：函数参数
   off(eventName, callback)
   // 用法1：只取消某个特定的回调函数
  eventBus.off('message_received', myCallback)
   // 用法2：取消该事件的所有订阅
   eventBus.off('message_received') */
   //第二步：检查事件是否存在 不存在则直接返回，不执行后续操作
  off(eventName, callback) {
    if (!listeners.value[eventName]) return
    // 第三步：判断是否传入了 callback
    if (callback) {
      // 传入了 callback，只取消这个特定的回调函数
      //.filter() 是数组的方法，它会创建一个新数组，只包含满足条件的元素。
      listeners.value[eventName] = listeners.value[eventName].filter(
        // 保留所有不是目标 callback 的其他函数
        cb => cb !== callback
      )
    } else {
      // 没有传入 callback，取消该事件的所有订阅
      // 直接将事件数组设为空即可
      listeners.value[eventName] = []
    }
  }
}

// 预定义事件名称
export const EVENT_TYPES = {
  MESSAGE_RECEIVED: 'message_received', // 收到新消息
  NOTIFICATION_UPDATED: 'notification_updated', // 通知更新
  CHAT_MESSAGE: 'chat_message' // 聊天消息
}
