/**
 * 用户认证相关 API 接口模块
 * 
 * 功能说明：
 * 1. 提供用户登录、登出等认证功能
 * 2. 所有接口返回统一格式：{ code, message, data }
 * 
 * 使用规范：
 * - 接口函数直接返回 Promise 对象
 * - 业务层调用使用 async/await 语法
 * - 全局错误处理由 axios 响应拦截器统一管理
 */
import request from '../utils/axios'

// 用户登录接口
/**
 * 用户登录接口
 * @param {string} username - 用户名（可以是学号、工号或管理员账号）
 * @param {string} password - 密码
 * @returns {Promise} 返回一个 Promise 对象，解析后包含后端返回的数据
 * 
 * 工作流程：
 * 1. 调用 request.post 发起 POST 请求到 /user/login
 * 2. 请求会经过 axios.js 中的请求拦截器（自动添加 Token，如果有的话）
 * 3. 后端验证用户名和密码，成功后返回 JWT Token 和用户信息
 * 4. 响应会经过 axios.js 中的响应拦截器（检查 code === 200）
 * 5. 如果成功，返回 { code: 200, message: "success", data: { token, user } }
 * 6. 如果失败，拦截器会自动弹窗提示错误，并抛出异常
 * 
 */
export function login(username, password) {
  return request.post('/user/login', { username, password })
}
/* 原始 Promise 写法  .then 和 .catch 其实就是 Promise 对象的两个方法。
handleLogin() {
  const promise = login(this.username, this.password)
  
  // 注册成功回调
  promise.then(res => {
    console.log('成功:', res)
  })
  
  // 注册失败回调
  promise.catch(error => {
    console.log('失败:', error)
  })
}
传统写法（.then / .catch）  这种写法不需要在函数前加 async，它通过链式调用来处理结果。
handleLogin() {
  // 1. 调用 login，它返回一个 Promise
  login(this.username, this.password)
    .then(res => {
      // 2. 请求成功时执行这里（对应 async/await 的 try 块）
      console.log('登录成功', res)
      const { token, user } = res.data
      sessionStorage.setItem('token', token)
    })
    .catch(error => {
      // 3. 请求失败时执行这里（对应 async/await 的 catch 块）
      console.log('登录失败', error)
    })
}
现代写法（async/await）  这是目前最主流的写法，代码看起来像同步执行，逻辑非常清晰。
// 在 Vue 组件的方法中
async handleLogin() {
  try {
    // 1. await 会“暂停”代码执行，直到 login 函数里的请求完成
    // 2. 如果成功，res 就是 axios.js 拦截器返回的那个对象 { code: 200, message: "success",data: ... }
    如果你在拦截器里返回 response：那你拿到的就是包含 status, headers 等的完整对象。
    如果你在拦截器里返回 response.data（你现在的做法）：那你拿到的就是纯粹的后端业务数据。
    const res = await login(this.username, this.password)
    
    // 3. 拿到数据后进行处理
    console.log('登录成功', res)
    const { token, user } = res.data
    sessionStorage.setItem('token', token)
    
  } catch (error) {
    // 4. 如果请求失败（比如密码错、断网），代码会直接跳到这里
    console.log('登录失败，错误信息已在拦截器弹窗显示')
  }
} */