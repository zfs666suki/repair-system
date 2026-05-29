/**
 * 管理员相关 API 接口模块
 * 
 * 功能说明：
 * 1. 提供管理员后台的用户管理功能（维修员、学生）
 * 2. 提供报修单管理、统计分析等功能
 * 3. 提供宿舍、楼栋、故障类型等基础数据管理
 * 4. 所有接口返回统一格式：{ code, message, data }
 * 
 * 使用规范：
 * - 接口函数直接返回 Promise 对象
 * - 业务层调用使用 async/await 语法
 * - 全局错误处理由 axios 响应拦截器统一管理
 */
import request from '../utils/axios'

// ==================== 用户管理 - 维修员 ====================

/**
 * 获取维修员列表
 * @param {Object} params - 查询参数（分页、搜索等）
 * @returns {Promise} 维修员列表数据
 */
export function getRepairmanList(params) {
  return request.get('/admin/repairmen', { params })
}

/**
 * 添加维修员
 * @param {Object} data - 维修员信息
 * @returns {Promise} 操作结果
 */
export function addRepairman(data) {
  return request.post('/admin/repairmen', data)
}

/**
 * 更新维修员信息
 * @param {Number} id - 维修员ID
 * @param {Object} data - 更新的维修员信息
 * @returns {Promise} 操作结果
 */
export function updateRepairman(id, data) {
  return request.put('/admin/repairmen/' + id, data)
}

/**
 * 切换维修员状态（启用/禁用）
 * @param {Number} id - 维修员ID
 * @returns {Promise} 操作结果
 */
export function toggleRepairmanStatus(id) {
  return request.put('/admin/repairmen/' + id + '/toggle')
}

// ==================== 用户管理 - 学生 ====================

/**
 * 获取所有用户列表（不包含当前登录用户自己）
 * 用于管理员发起新聊天
 * @returns {Promise} 用户列表数据
 */
export function getUserList() {
  return request.get('/admin/users')
}

/**
 * 获取学生列表
 * @param {Object} params - 查询参数（分页、搜索等）
 * @returns {Promise} 学生列表数据
 */
export function getStudentList(params) {
  return request.get('/admin/students', { params })
}

/**
 * 添加学生
 * @param {Object} data - 学生信息
 * @returns {Promise} 操作结果
 */
export function addStudent(data) {
  return request.post('/admin/students', data)
}

/**
 * 更新学生信息
 * @param {Number} id - 学生ID
 * @param {Object} data - 更新的学生信息
 * @returns {Promise} 操作结果
 */
export function updateStudent(id, data) {
  return request.put('/admin/students/' + id, data)
}

/**
 * 切换学生状态（启用/禁用）
 * @param {Number} id - 学生ID
 * @returns {Promise} 操作结果
 */
export function toggleStudentStatus(id) {
  return request.put('/admin/students/' + id + '/toggle')
}

/**
 * 下载学生导入模板
 * @returns {Promise} Excel模板文件
 */
export function downloadStudentTemplate() {
  return request.get('/admin/students/template', { responseType: 'blob' })
}

/**
 * 批量导入学生
 * @param {File} file - Excel文件
 * @returns {Promise} 导入结果
 */
export function importStudents(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/students/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ==================== 楼栋管理 ====================

/**
 * 获取楼栋列表
 * @returns {Promise} 楼栋列表数据
 */
export function getBuildingList() {
  return request.get('/admin/buildings')
}

/**
 * 添加楼栋
 * @param {Object} data - 楼栋信息
 * @returns {Promise} 操作结果
 */
export function addBuilding(data) {
  return request.post('/admin/buildings', data)
}

/**
 * 更新楼栋信息
 * @param {Number} id - 楼栋ID
 * @param {Object} data - 更新的楼栋信息
 * @returns {Promise} 操作结果
 */
export function updateBuilding(id, data) {
  return request.put('/admin/buildings/' + id, data)
}

/**
 * 切换楼栋状态（启用/禁用）
 * @param {Number} id - 楼栋ID
 * @returns {Promise} 操作结果
 */
export function toggleBuildingStatus(id) {
  return request.put('/admin/buildings/' + id + '/toggle')
}

/**
 * 删除楼栋
 * @param {Number} id - 楼栋ID
 * @returns {Promise} 操作结果
 */
export function deleteBuilding(id) {
  return request.delete('/admin/buildings/' + id)
}

// ==================== 房间管理 ====================

/**
 * 获取房间列表
 * @param {Object} params - 查询参数（分页、搜索等）
 * @returns {Promise} 房间列表数据
 */
export function getRoomList(params) {
  return request.get('/admin/rooms', { params })
}

/**
 * 添加房间
 * @param {Object} data - 房间信息
 * @returns {Promise} 操作结果
 */
export function addRoom(data) {
  return request.post('/admin/rooms', data)
}

/**
 * 更新房间信息
 * @param {Number} id - 房间ID
 * @param {Object} data - 更新的房间信息
 * @returns {Promise} 操作结果
 */
export function updateRoom(id, data) {
  return request.put('/admin/rooms/' + id, data)
}

/**
 * 切换房间状态（启用/禁用）
 * @param {Number} id - 房间ID
 * @returns {Promise} 操作结果
 */
export function toggleRoomStatus(id) {
  return request.put('/admin/rooms/' + id + '/toggle')
}

/**
 * 删除房间
 * @param {Number} id - 房间ID
 * @returns {Promise} 操作结果
 */
export function deleteRoom(id) {
  return request.delete('/admin/rooms/' + id)
}

// ==================== 宿舍分配管理 ====================

/**
 * 获取宿舍分配列表
 * @param {Object} params - 查询参数（分页、搜索等）
 * @returns {Promise} 宿舍分配列表数据
 */
export function getDormitoryList(params) {
  return request.get('/admin/dormitories', { params })
}

/**
 * 分配宿舍给学生
 * @param {Object} data - 宿舍分配信息（学生ID、房间ID等）
 * @returns {Promise} 操作结果
 */
export function assignDormitory(data) {
  return request.post('/admin/dormitories', data)
}

/**
 * 学生退宿
 * @param {Number} id - 宿舍分配记录ID
 * @returns {Promise} 操作结果
 */
export function checkoutDormitory(id) {
  return request.put('/admin/dormitories/' + id + '/checkout')
}

// ==================== 故障类型管理 ====================

/**
 * 获取故障类型列表
 * @returns {Promise} 故障类型列表数据
 */
export function getFaultTypeList() {
  return request.get('/admin/fault-types')
}

/**
 * 添加故障类型
 * @param {Object} data - 故障类型信息
 * @returns {Promise} 操作结果
 */
export function addFaultType(data) {
  return request.post('/admin/fault-types', data)
}

/**
 * 更新故障类型信息
 * @param {Number} id - 故障类型ID
 * @param {Object} data - 更新的故障类型信息
 * @returns {Promise} 操作结果
 */
export function updateFaultType(id, data) {
  return request.put('/admin/fault-types/' + id, data)
}

/**
 * 切换故障类型状态（启用/禁用）
 * @param {Number} id - 故障类型ID
 * @returns {Promise} 操作结果
 */
export function toggleFaultTypeStatus(id) {
  return request.put('/admin/fault-types/' + id + '/toggle')
}

// ==================== 报修单管理 ====================

/**
 * 获取报修单详情
 * @param {Number} orderId - 报修单ID
 * @returns {Promise} 报修单详细信息
 */
export function getRepairOrderDetail(orderId) {
  return request.get('/admin/repair-order/' + orderId)
}

// ==================== 统计功能 ====================

/**
 * 按日期统计报修单
 * @param {Object} params - 统计参数（开始日期、结束日期等）
 * @returns {Promise} 按日期的统计数据
 */
export function statisticsByDate(params) {
  return request.get('/admin/statistics/by-date', { params })
}

/**
 * 按楼栋统计报修单
 * @param {Object} params - 统计参数
 * @returns {Promise} 按楼栋的统计数据
 */
export function statisticsByBuilding(params) {
  return request.get('/admin/statistics/by-building', { params })
}

/**
 * 按故障类型统计报修单
 * @param {Object} params - 统计参数
 * @returns {Promise} 按故障类型的统计数据
 */
export function statisticsByFaultType(params) {
  return request.get('/admin/statistics/by-fault-type', { params })
}

/**
 * 按状态统计报修单
 * @param {Object} params - 统计参数
 * @returns {Promise} 按状态的统计数据
 */
export function statisticsByStatus(params) {
  return request.get('/admin/statistics/by-status', { params })
}

/**
 * 获取统计数据汇总
 * @param {Object} params - 统计参数
 * @returns {Promise} 统计数据汇总信息
 */
export function getStatisticsSummary(params) {
  return request.get('/admin/statistics/summary', { params })
}

/**
 * 获取所有维修员的统计数据
 * @param {Object} params - 统计参数
 * @returns {Promise} 维修员统计数据列表
 */
export function getAllRepairUserStatistics(params) {
  return request.get('/admin/statistics/repair-users', { params })
}

// ==================== 导出功能 ====================

/**
 * 导出报修单数据
 * @param {Object} params - 导出参数（筛选条件等）
 * @returns {Promise} Excel文件
 */
export function exportRepairOrders(params) {
  return request.get('/admin/export/repair-orders', { params, responseType: 'blob' })
}