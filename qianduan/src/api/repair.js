/**
 * 报修系统相关 API 接口模块
 * 
 * 功能说明：
 * 1. 提供学生端报修流程的完整支持（获取宿舍信息、故障类型、提交报修等）
 * 2. 提供维修人员端的订单处理功能（接单、拒单、完成订单等）
 * 3. 提供图片上传和删除功能
 * 4. 所有接口返回统一格式：{ code, message, data }
 * 
 * 使用规范：
 * - 接口函数直接返回 Promise 对象，不在内部链式调用 .then() 或 .catch()
 * - 业务层调用优先使用 async/await 语法处理异步结果
 * - 全局错误处理由 axios 响应拦截器统一管理
 */
import request from '../utils/axios'

// 获取当前学生的宿舍信息
/**
 * 获取当前学生的宿舍信息
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到宿舍详细信息
 *                    数据结构：{ buildingName, roomNumber, ... }
 * 
 * 使用场景：
 * - 学生提交报修时自动填充宿舍信息
 * - 显示用户的基本住宿信息
 * 
 * 请求示例：
 * const result = await getDormitoryInfo()
 * console.log(result.data) // 宿舍信息对象
 * 
 * 后端接口：GET /student/dormitory
 */
export function getDormitoryInfo() {
  return request.get('/student/dormitory')
}
//获取故障类型列表
/**
 * 获取故障类型列表
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到故障类型数组
 *                    数据结构：Array<{ id, typeName, description }>
 * 
 * 使用场景：
 * - 报修表单中提供故障类型下拉选择
 * - 管理员配置和管理故障分类
 * 
 * 请求示例：
 * const result = await getFaultTypes()
 * console.log(result.data) // 故障类型数组
 * 
 * 后端接口：GET /student/fault-types
 */
export function getFaultTypes() {
  return request.get('/student/fault-types')
}
//获取楼栋列表
/**
 * 获取楼栋列表
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到楼栋数组
 *                    数据结构：Array<{ id, buildingName, buildingCode }>
 * 
 * 使用场景：
 * - 管理员进行房间管理时选择楼栋
 * - 报修表单中选择所在楼栋
 * 
 * 请求示例：
 * const result = await getBuildings()
 * console.log(result.data) // 楼栋数组
 * 
 * 后端接口：GET /building/list
 */
export function getBuildings() {
  return request.get('/building/list')
}
//根据楼栋 ID 获取房间列表
/**
 * 根据楼栋 ID 获取房间列表
 * 
 * @param {number|string} buildingId - 楼栋 ID
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到房间数组
 *                    数据结构：Array<{ id, roomNumber, floor, capacity }>
 * 
 * 工作流程：
 * 1. 前端传入楼栋 ID
 * 2. 后端查询该楼栋下的所有房间
 * 3. 返回房间列表供前端展示
 * 
 * 使用场景：
 * - 级联选择：先选楼栋，再选房间
 * - 管理员批量管理某栋楼的房间
 * 
 * 请求示例：
 * const result = await getRoomsByBuilding(buildingId)
 * console.log(result.data) // 房间数组
 * 
 * 后端接口：GET /room/list?buildingId={id}
 */
export function getRoomsByBuilding(buildingId) {
  return request.get('/room/list', { params: { buildingId } })
}
//提交报修申请
/**
 * 提交报修申请
 * 
 * @param {Object} data - 报修数据对象
 * @param {string} data.orderNo - 报修单号（可选，通常由后端生成）
 * @param {string} data.faultType - 故障类型
 * @param {string} data.description - 故障描述
 * @param {string} data.contactPhone - 联系电话
 * @param {string} [data.images] - 故障图片 URL 数组（可选）
 * @param {number} [data.buildingId] - 楼栋 ID（可选）
 * @param {number} [data.roomId] - 房间 ID（可选）
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到提交结果
 *                    数据结构：包含生成的报修单号、提交时间等信息
 * 
 * 工作流程：
 * 1. 前端收集报修表单数据
 * 2. 调用此接口提交到后端
 * 3. 后端创建报修记录并分配状态（通常为"待处理"）
 * 4. 返回报修单号供后续查询
 * 
 * 注意事项：
 * - 提交前应先上传图片获取 URL，再将 URL 放入 images 字段
 * - 提交成功后应跳转到报修列表页或详情页
 * 
 * 请求示例：
 * const result = await submitRepair({
 *   faultType: '水管漏水',
 *   description: '卫生间水管持续漏水',
 *   contactPhone: '13800138000',
 *   images: ['https://oss.example.com/image1.jpg']
 * })
 * 
 * 后端接口：POST /repair/submit
 */
export function submitRepair(data) {
  return request.post('/repair/submit', data)
}
//获取报修列表（分页）
/**
 * 获取报修列表（分页）
 * 
 * @param {Object} params - 查询参数对象
 * @param {number} [params.page] - 页码，默认为 1
 * @param {number} [params.pageSize] - 每页条数，默认为 10
 * @param {string} [params.status] - 报修状态筛选（可选）
 *                                   可选值：pending, accepted, processing, completed, cancelled
 * @param {string} [params.keyword] - 搜索关键词（可选）
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到分页数据
 *                    数据结构：{ total: number, records: Array }
 * 
 * 使用场景：
 * - 学生查看自己的报修历史
 * - 维修人员查看待处理或处理中的订单
 * - 管理员查看所有报修记录
 * 
 * 请求示例：
 * const result = await getRepairList({ page: 1, pageSize: 10, status: 'pending' })
 * console.log(result.data.total) // 总条数
 * console.log(result.data.records) // 当前页数据
 * 
 * 后端接口：GET /repair/list
 */
export function getRepairList(params) {
  return request.get('/repair/list', { params })
}
//获取报修详情
/**
 * 获取报修详情
 * 
 * @param {number|string} id - 报修单 ID 或报修单号
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到报修详细信息
 *                    数据结构：包含报修单全部字段及关联的用户、维修人员信息
 * 
 * 使用场景：
 * - 点击报修记录查看详情
 * - 维修人员查看订单详细信息以进行处理
 * 
 * 请求示例：
 * const result = await getRepairDetail(repairId)
 * console.log(result.data) // 报修详情对象
 * 
 * 后端接口：GET /repair/detail/{id}
 */
export function getRepairDetail(id) {
  return request.get('/repair/detail/' + id)
}
//取消报修申请
/**
 * 取消报修申请
 * 
 * @param {string} orderNo - 报修单号
 * 
 * @returns {Promise} 返回 Promise 对象，操作成功后 data 通常为 null
 * 
 * 工作流程：
 * 1. 学生发起取消请求
 * 2. 后端验证报修单状态（仅允许取消"待处理"状态的订单）
 * 3. 更新报修单状态为"已取消"
 * 4. 返回操作结果
 * 
 * 注意事项：
 * - 只有状态为"待处理"的报修单可以取消
 * - 取消后不可恢复，需重新提交报修
 * 
 * 请求示例：
 * await cancelRepair(orderNo)
 * // 操作成功后更新本地状态或刷新列表
 * 
 * 后端接口：POST /repair/cancel
 */
export function cancelRepair(orderNo) {
  return request.post('/repair/cancel', { orderNo })
}
//更新报修信息
/**
 * 更新报修信息
 * 
 * @param {Object} data - 更新的报修数据
 * @param {number|string} data.id - 报修单 ID
 * @param {string} [data.description] - 更新的故障描述（可选）
 * @param {string} [data.contactPhone] - 更新的联系电话（可选）
 * @param {string} [data.images] - 更新的图片 URL 数组（可选）
 * 
 * @returns {Promise} 返回 Promise 对象，操作成功后 data 通常为 null
 * 
 * 使用场景：
 * - 学生修改未处理的报修单信息
 * - 补充故障描述或图片
 * 
 * 注意事项：
 * - 仅允许修改"待处理"状态的报修单
 * - 已接单或处理中的订单不可修改
 * 
 * 请求示例：
 * await updateRepair({
 *   id: repairId,
 *   description: '补充：漏水情况严重，需要紧急处理',
 *   contactPhone: '13900139000'
 * })
 * 
 * 后端接口：POST /repair/update
 */
export function updateRepair(data) {
  return request.post('/repair/update', data)
}
//上传图片到阿里云 OSS
/**
 * 上传图片到阿里云 OSS
 * 
 * @param {File} file - 要上传的图片文件对象
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到上传结果
 *                    数据结构：{ url: string, fileName: string }
 * 
 * 工作流程：
 * 1. 前端创建 FormData 对象并附加文件
 * 2. 设置 Content-Type 为 multipart/form-data
 * 3. 后端接收文件并上传到阿里云 OSS
 * 4. 返回文件的访问 URL 和文件名
 * 
 * 注意事项：
 * - 文件大小和类型应在前端进行预校验
 * - 上传成功后应将返回的 URL 保存到报修表单的 images 字段
 * - 如果用户取消报修，应调用 deleteImage 清理已上传的图片
 * 
 * 请求示例：
 * const result = await uploadImage(file)
 * const imageUrl = result.data.url
 * 
 * 后端接口：POST /upload/image
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
//删除已上传的图片
/**
 * 删除已上传的图片
 * 
 * @param {string} fileName - 图片文件名（从 OSS URL 中提取或直接使用返回的 fileName）
 * 
 * @returns {Promise} 返回 Promise 对象，操作成功后 data 通常为 null
 * 
 * 使用场景：
 * - 用户在表单中删除已选择的图片
 * - 取消报修时清理已上传但未使用的图片
 * - 避免 OSS 存储浪费
 * 
 * 工作流程：
 * 1. 前端传入文件名
 * 2. 后端调用阿里云 OSS SDK 删除对应文件
 * 3. 返回删除结果
 * 
 * 请求示例：
 * await deleteImage(fileName)
 * // 操作成功后从本地图片列表中移除
 * 
 * 后端接口：DELETE /upload/image?fileName={name}
 */
export function deleteImage(fileName) {
  return request.delete('/upload/image', { params: { fileName } })
}
//维修人员接受报修订单
/**
 * 维修人员接受报修订单
 * 
 * @param {Object} data - 接单数据
 * @param {number|string} data.orderId - 报修单 ID
 * @param {number|string} data.repairmanId - 维修人员 ID
 * 
 * @returns {Promise} 返回 Promise 对象，操作成功后 data 通常为 null
 * 
 * 工作流程：
 * 1. 维修人员在待处理列表中点击"接单"
 * 2. 后端验证订单状态是否为"待处理"
 * 3. 将订单状态更新为"已接单"，并绑定维修人员
 * 4. 触发 WebSocket 通知学生订单已被接受
 * 
 * 注意事项：
 * - 一个订单只能被一个维修人员接受
 * - 接单后订单进入"处理中"状态
 * 
 * 请求示例：
 * await acceptOrder({ orderId, repairmanId })
 * // 操作成功后跳转到处理中订单列表
 * 
 * 后端接口：POST /repair/accept
 */
export function acceptOrder(data) {
  return request.post('/repair/accept', data)
}
//维修人员拒绝报修订单
/**
 * 维修人员拒绝报修订单
 * 
 * @param {Object} data - 拒单数据
 * @param {number|string} data.orderId - 报修单 ID
 * @param {string} data.reason - 拒绝原因
 * 
 * @returns {Promise} 返回 Promise 对象，操作成功后 data 通常为 null
 * 
 * 工作流程：
 * 1. 维修人员在待处理列表中点击"拒单"
 * 2. 填写拒绝原因（必填）
 * 3. 后端将订单状态重置为"待处理"，清除维修人员绑定
 * 4. 记录拒绝原因供管理员查看
 * 5. 触发 WebSocket 通知管理员有新的拒单
 * 
 * 使用场景：
 * - 维修人员无法处理该类型故障
 * - 维修人员工作饱和，无法及时响应
 * 
 * 请求示例：
 * await rejectOrder({
 *   orderId,
 *   reason: '该故障需要专业电工处理，我擅长水管维修'
 * })
 * 
 * 后端接口：POST /repair/reject
 */
export function rejectOrder(data) {
  return request.post('/repair/reject', data)
}
//维修人员完成报修订单
/**
 * 维修人员完成报修订单
 * 
 * @param {Object} data - 完成订单数据
 * @param {number|string} data.orderId - 报修单 ID
 * @param {string} [data.remark] - 完成备注（可选），如维修说明、更换零件等
 * @param {string} [data.images] - 维修后照片 URL 数组（可选）
 * 
 * @returns {Promise} 返回 Promise 对象，操作成功后 data 通常为 null
 * 
 * 工作流程：
 * 1. 维修人员完成维修后点击"完成订单"
 * 2. 可选填写维修备注和上传维修后照片
 * 3. 后端将订单状态更新为"已完成"
 * 4. 记录完成时间
 * 5. 触发 WebSocket 通知学生订单已完成
 * 
 * 注意事项：
 * - 只有"处理中"状态的订单可以完成
 * - 完成后学生可以进行评价（如果系统有评价功能）
 * 
 * 请求示例：
 * await completeOrder({
 *   orderId,
 *   remark: '已更换新的水龙头，测试无漏水',
 *   images: ['https://oss.example.com/after1.jpg']
 * })
 * 
 * 后端接口：POST /repair/complete
 */
export function completeOrder(data) {
  return request.post('/repair/complete', data)
}
//获取用户列表（用于聊天或分配）
/**
 * 获取用户列表（用于聊天或分配）
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到用户数组
 *                    数据结构：Array<{ id, username, realName, role, ... }>
 * 
 * 使用场景：
 * - 聊天功能中选择对话对象
 * - 管理员分配维修任务时选择维修人员
 * - 显示系统中的所有用户信息
 * 
 * 请求示例：
 * const result = await getUserList()
 * console.log(result.data) // 用户数组
 * 
 * 后端接口：GET /repair/users
 */
export function getUserList() {
  return request.get('/repair/users')
}