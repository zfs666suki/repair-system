/**
 * 学生端相关 API 接口模块
 * 
 * 功能说明：
 * 1. 提供学生用户获取其他用户列表的功能
 * 2. 主要用于聊天功能中选择对话对象
 * 3. 所有接口返回统一格式：{ code, message, data }
 * 
 * 使用规范：
 * - 接口函数直接返回 Promise 对象，不在内部链式调用 .then() 或 .catch()
 * - 业务层调用优先使用 async/await 语法处理异步结果
 * - 全局错误处理由 axios 响应拦截器统一管理
 */
import request from '../utils/axios'

// 获取所有用户列表（不包含当前登录用户自己）
/**
 * 获取所有用户列表（不包含当前登录用户自己）
 * 
 * @returns {Promise} 返回 Promise 对象，解析后得到用户数组
 *                    数据结构：Array<{ id, username, realName, role, avatar, ... }>
 * 
 * 工作流程：
 * 1. 前端调用此接口获取系统中的其他用户
 * 2. 后端根据当前登录用户的 token 识别用户身份
 * 3. 查询所有用户并过滤掉当前用户
 * 4. 返回用户列表供前端展示
 * 
 * 使用场景：
 * - 聊天功能中显示可对话的用户列表
 * - 选择联系人发起私聊
 * - 显示系统中的维修人员、管理员等
 * 
 * 注意事项：
 * - 返回的用户列表已自动排除当前登录用户
 * - 可根据用户角色（role）进行前端筛选，如只显示维修人员
 * 
 * 请求示例：
 * const result = await getUserList()
 * console.log(result.data) // 用户数组
 * 
 * // 筛选维修人员
 * const repairmen = result.data.filter(user => user.role === 'repairman')
 * 
 * 后端接口：GET /student/users
 */
export function getUserList() {
  return request.get('/student/users')
}