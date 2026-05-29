/**
 * 通知与消息相关 API 接口模块
 * 
 * 功能说明：
 * 1. 提供通知列表的查询、未读数量统计等功能
 * 2. 支持聊天消息的发送和获取
 * 3. 所有接口返回统一格式：{ code, message, data }
 * 
 * 使用规范：
 * - 接口函数直接返回 Promise 对象，不在内部链式调用 .then() 或 .catch()
 * - 业务层调用优先使用 async/await 语法处理异步结果
 * - 全局错误处理由 axios 响应拦截器统一管理
 */
import request from '../utils/axios'

// 获取通知列表
/**
 * 获取通知列表
 /**
 * @param {Object} params - 查询参数对象
 * @param {number} [params.page] - 页码，默认为 1
 * @param {number} [params.pageSize] - 每页条数，默认为 10
 * @param {string} [params.type] - 通知类型（可选）
 * @param {string} [params.status] - 通知状态（可选）
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到分页数据
 *                    数据结构：{ total: number, records: Array }
 * 
 * 请求示例：
 * const result = await getNotificationList({ page: 1, pageSize: 10 })
 * console.log(result.data) // 访问业务数据
 * 
 * 后端接口：GET /notification/list
 */
export function getNotificationList(params) {
  return request.get('/notification/list', { params })
}
//获取未读通知数量
/**
 * 获取未读通知数量
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到未读数量
 *                    数据结构：{ count: number }
 * 
 * 使用场景：
 * - 在页面头部显示未读消息角标
 * - 定期轮询检查是否有新消息
 * 
 * 请求示例：
 * const result = await getUnreadCount()
 * console.log(result.data.count) // 未读数量
 * 
 * 后端接口：GET /notification/unread-count
 */
export function getUnreadCount() {
  return request.get('/notification/unread-count')
}
//标记通知为已读
/**
 * 标记通知为已读
 * 
 * @param {number|string} id - 通知 ID
 * 
 * @returns {Promise} 返回 Promise 对象，操作成功后 data 通常为 null 或成功标识
 * 
 * 工作流程：
 * 1. 前端调用此接口标记单条通知为已读状态
 * 2. 后端更新数据库中该通知的 read_status 字段
 * 3. 更新成功后，前端应同步更新 UI 状态（如移除未读标识）
 * 
 * 请求示例：
 * await markAsRead(notificationId)
 * // 操作成功后更新本地状态
 * notification.readStatus = 1
 * 
 * 后端接口：PUT /notification/{id}/read
 */
export function markAsRead(id) {
  return request.put(`/notification/${id}/read`)
}
//发送聊天消息
/**
 * 发送聊天消息
 * 
 * @param {Object} data - 消息数据对象
 * @param {number|string} data.receiverId - 接收者 ID（用户ID）
 * @param {string} data.content - 消息内容
 * @param {string} [data.messageType] - 消息类型（可选），如 'text', 'image' 等
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到发送结果
 *                    数据结构：包含消息 ID、发送时间等信息
 * 
 * 使用场景：
 * - 学生与维修人员之间的即时通讯
 * - 管理员与用户之间的沟通
 * 
 * 注意事项：
 * - 发送成功后应通过 WebSocket 或事件总线通知对方
 * - 建议配合 eventBus.emit(EVENT_TYPES.CHAT_MESSAGE) 使用
 * 
 * 请求示例：
 * const result = await sendChatMessage({
 *   receiverId: targetUserId,
 *   content: '您好，请问有什么可以帮助您的？',
 *   messageType: 'text'
 * })
 * 
 * 后端接口：POST /notification/chat
 */
export function sendChatMessage(data) {
  return request.post('/notification/chat', data)
}
//获取聊天记录
/**
 * 获取聊天记录
 * 
 * @param {number|string} otherUserId - 对话另一方的用户 ID
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到聊天记录数组
 *                    数据结构：Array<{ id, senderId, receiverId, content, createTime, ... }>
 * 
 * 工作流程：
 * 1. 根据当前登录用户 ID 和对方用户 ID 查询两人的聊天记录
 * 2. 按时间顺序返回历史消息列表
 * 3. 前端渲染聊天界面时展示这些消息
 * 
 * 使用场景：
 * - 进入聊天页面时加载历史消息
 * - 刷新聊天窗口时重新获取最新消息
 * 
 * 请求示例：
 * const result = await getChatMessages(otherUserId)
 * console.log(result.data) // 聊天记录数组
 * 
 * 后端接口：GET /notification/chat?otherUserId={id}
 */
export function getChatMessages(otherUserId) {
  return request.get('/notification/chat', { params: { otherUserId } })
}
//根据通知类型获取未读数量
/**
 * 根据通知类型获取未读数量
 * 
 * @param {string} type - 通知类型（例如：'system' 系统通知, 'repair' 报修通知等）
 * @returns {Promise} 返回 Promise 对象，解析后得到该特定类型的未读数量
 *                    数据结构：{ count: number }
 * 
 * 使用场景：
 * - 在分类通知列表中，为每个分类标签页显示独立的未读角标
 * - 实现更精细化的消息提醒功能
 * 
 * 后端接口：GET /notification/unread-count/type?type={type}
 */
export function getUnreadCountByType(type) {
  return request.get('/notification/unread-count/type', { params: { type } })
}
//标记与指定用户所有聊天消息为已读
/**
 * 标记与指定用户的所有聊天消息为已读
 * 
 * @param {number|string} otherUserId - 对话另一方的用户 ID
 * @returns {Promise} 返回 Promise 对象，操作成功后 data 通常为 null
 * 
 * 工作流程：
 * 1. 用户点击进入某个聊天窗口
 * 2. 前端调用此接口，传入对方 ID
 * 3. 后端将该会话下所有针对当前用户的未读消息状态更新为“已读”
 * 4. 前端同步更新 UI，消除聊天列表中的未读红点
 * 
 * 注意事项：
 * - 通常在进入聊天详情页（onMounted）或切换聊天对象时调用
 * - 配合 WebSocket 实时消息推送，确保多端状态同步
 * 
 * 后端接口：PUT /notification/chat/read?otherUserId={id}
 */
export function markChatMessagesAsRead(otherUserId) {
  return request.put('/notification/chat/read', null, { params: { otherUserId } })
}