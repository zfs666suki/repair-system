/**
 * Axios 请求封装模块
 * 
 * 功能说明：
 * 1. 创建 axios 实例，配置基础 URL 和超时时间
 * 2. 请求拦截器：自动添加 Token 到请求头
 * 3. 响应拦截器：统一处理错误，提取业务数据
 * 4. 401 未授权时自动跳转登录页
 * 
 * 使用规范：
 * - 所有 API 模块都从此文件导入 request 实例
 * - 不要在业务代码中直接创建新的 axios 实例
 * - 全局错误处理已在此配置，业务层无需重复处理
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例，配置基础 URL 和超时时间
const request = axios.create({
  baseURL: '/api', // 所有请求的基础路径
  timeout: 10000   // 请求超时时间为 10 秒
})
//Axios 的拦截器使用 .use() 方法注册，它接收两个函数作为参数

// 请求拦截器：在发送请求之前执行
/* 第一个参数：成功回调 
输入：config 是 Axios 的请求配置对象，包含 URL、method、headers、data 等所有即将发送的请求信息。
处理：在这里我们可以修改 config（如添加 Header、修改 URL 前缀等）。
输出：必须返回 config 对象（或包含 config 的 Promise）。如果不返回，请求将无法继续发送。 */
/* 第二个参数：失败回调 
触发场景：如果在请求发送前的配置阶段发生了错误（极少见，通常是代码逻辑错误），会进入此分支。
处理：直接返回一个被拒绝的 Promise (Promise.reject(error))，将错误传递给调用方（即发起请求的 .catch 块或 try...catch）。 */

/* 1: 前端发起请求
Axios 请求构建：request.post('/user/login', {username, password}) 会创建一个 POST 请求。
Axios 会自动将 data 参数序列化为 JSON,并设置 Content-Type: application/json。
此时,请求配置 config 的初始状态是:
//config 是对 HTTP 请求的"数据化封装，把请求的各个部分（方法、URL、头、 body 等）封装成一个 JS 对象
config = {
  method: 'post',
  url: '/user/login',
  data: { username: "admin", password: "123456" },
  headers: { 'Content-Type': 'application/json' },
  // baseURL 尚未拼接,将在拦截器之后由 Axios 内部处理
} */
/*
  2: 请求拦截器处理 
执行时机：在 Axios 发送 HTTP 请求之前。
代码执行：
request.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token') // 第一次登录，这里返回 null
    if (token) {
      config.headers.Authorization = token // 不执行
    }
    return config // 返回原始的 config 对象
  },
  error => {
    return Promise.reject(error)
  }
)
  */
request.interceptors.request.use(
  config => {
    // 从 sessionStorage 中获取 token
    const token = sessionStorage.getItem('token')
    // 如果存在 token，则将其添加到请求头的 Authorization 字段
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  error => {
    // 请求错误处理
    return Promise.reject(error)
  }
)

/* 3: baseURL 拼接与网络请求
关键步骤：请求拦截器返回后，Axios 内部会自动执行 baseURL 拼接：
  - Axios 内部的 dispatchRequest 函数会将 baseURL 和 url 合并
  - config.url 从 '/user/login' 变为 '/api/user/login
HTTP 请求流程：
  1.Axios 调用浏览器底层 API (XMLHttpRequest 或 Fetch)：
    Axios 传入的路径: /api/user/login (相对路径)                      
    浏览器发起请求时，会自动基于当前域名补全为绝对路径:                                     
    http://localhost:5173 + /api/user/login             
  - 最终 URL: http://localhost:5173/api/user/login (Vite 开发服务器)
  2.Vite 代理转发 (Proxy):
    Vite 检测到 /api 前缀
    将请求转发到后端服务器: http://localhost:8080/api/user/login
  3.后端接收:
    Spring Boot 应用收到请求
    最终 URL: http://localhost:8080/api/user/login
  
    最终到达后端的请求信息：
    Method: POST
    URL: http://localhost:8080/api/user/login
    Headers: Content-Type: application/json，（第一次登录不会添加）Authorization: Bearer eyJhbGciOiJIUzI1NiJ9... (64 字符) 从 sessionStorage 中获取的 token 
    Body: {"username":"admin","password":"123456"}
服务器接收：
  后端 Spring Boot 应用收到请求。@PostMapping("/login") 被调用。
  服务层验证用户名和密码是否匹配。验证通过后，生成 JWT Token。
服务器响应：（这里是apifox的响应结果，后端可能返回不同的响应格式，只是这里举个例子）
  Status Code: 200 OK
  Headers:Content-Type: application/json
  Body:
  {
  "code": 200,
  "message": "success",
  "data": {
    "token": "Bearer eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "role": 3,
      "status": 1,
      "createTime": "2026-04-25T22:48:36",
      "updateTime": "2026-04-25T22:48:36"
            }
          }
  } */



// 响应拦截器：在接收到响应之后执行
//响应拦截器同样使用 .use()方法注册，接受两个回调函数作为参数：
/* 第1个参数：成功回调 (HTTP 状态码为 2xx 时触发)
  - 输入：response 是 Axios 的完整响应对象。
  - 处理：在此处进行统一业务逻辑判断（如检查 res.code）。
  - 输出：必须返回【希望传递给业务层的数据】。
    - 通常返回 response.data (即后端返回的具体内容)。
    - 如果返回其他值，组件接收到的就是该值。
    - 如果抛出错误或返回 Promise.reject，则进入下方的 error 回调或组件的 catch。*/
/* 第2个参数：失败回调 (HTTP 状态码非 2xx 或网络错误时触发)
  - 触发场景：
    1. HTTP 状态码错误 (如 401, 403, 500 等)。
    2. 网络错误 (如断网、DNS 解析失败)。
    3. 请求超时 (timeout)。
    4. 上方成功回调中主动抛出的 Promise.reject。
  - 处理：统一错误提示、登录失效跳转等。
  - 输出：通常返回 Promise.reject(error)，以便组件能捕获到错误并进行额外处理（如关闭 Loading）。*/

  /* 4: 响应拦截器处理
执行时机：在 Axios 接收到服务器响应之后。
代码执行:
request.interceptors.response.use(
  response => {

    // 这里 response 是完整的响应对象
    //response对象结构：
    {
      data: {                // <--- 拦截器只取走了这一部分
      code: 200,
      message: "success",
      data: { token: "...", user: {...} }
    },
    status: 200,           // HTTP 状态码
    statusText: "OK",      // 状态文本
    headers: { ... },      // 响应头
    config: { ... },       // 请求配置
    request: { ... }       // 原始请求对象
    }

    const res = response.data; // 提取 { code: 200, message: ..., data: { ... } }
    
    // 检查业务状态码
    if (res.code !== 200) {
      // 这里不会进入，因为 code 是 200
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    // 业务成功，返回 data 字段
    return res; // 返回 { code: 200, message: ..., data: { ... } }
  },
  error => {
    // 这里不会进入，因为服务器返回了 200
  }
) 
   5: 前端组件处理
   代码执行：const response = await request.post(...) 执行完毕，response 就是拦截器返回的 res。
   const { token, user } = result.data; 解构出 token 和 user。
   sessionStorage.setItem('token', token); 将 Token 存入浏览器内存。
   sessionStorage.setItem('userInfo', JSON.stringify(user)); 将用户信息存入浏览器内存。
   router.push('/'); 跳转页面。
  */
  request.interceptors.response.use(
  response => {
    // 如果响应类型是 blob（通常用于文件下载），直接返回响应对象
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 解析响应数据
    const res = response.data
    
    // 如果后端返回的状态码不是 200，视为业务错误
    if (res.code !== 200) {
      // 显示错误消息
      ElMessage.error(res.message || '请求失败')
      // 抛出错误，以便调用方可以捕获
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    // 返回正常的数据
    return res
  },

/* 登录失败情况：
  1. 服务器返回的响应：
  {
  "code": 500,
  "message": "用户名或密码错误",
  "data": null
  }
  2. axios 错误对象结构：
error = {
  response: {                    // ✅ 存在，因为服务器返回了响应
    status: 500,                 // HTTP 状态码
    statusText: "Internal Server Error", // HTTP 状态文本
    headers: {...},              // 响应头
    data: {                      // 后端返回的数据
      code: 500,
      message: "用户名或密码错误",
      data: null
    },
    config: {...},               // 请求配置
    request: {...}               // 原始请求对象
  },
  message: "Request failed with status code 500", // 错误消息
  config: {...}                //你发起请求时的配置
}
  3. 响应拦截器的处理逻辑
  // 在您的 axios.js 文件中
  request.interceptors.response.use(
    response => {
    // 这个分支不会执行，因为 code 是 500 不等于 200
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    // 这个分支会执行！
    if (error.response) {
      // 获取错误消息（优先级：message > msg > statusText）
      let errorMsg = ''
  
      // ✅ 第一级：获取 message 字段
      if (error.response?.data?.message) {
        errorMsg = error.response.data.message  // 得到："用户名或密码错误"
      } 
      // ❌ 第二级：msg 字段不存在
      else if (error.response?.data?.msg) {
        errorMsg = error.response.data.msg
      } 
      // ❌ 第三级：statusText 会被忽略，因为前面已经找到 message
      else if (error.response?.statusText) {
        errorMsg = error.response.statusText
      }
      
      // 根据状态码处理
      switch (error.response.status) {
        case 401:
          // 不会进入，因为是 500 而不是 401
          break
        case 403:
          // 不会进入
          break
        case 404:
          // 不会进入
          break
        case 500:
          // ✅ 会进入这个分支
          ElMessage.error(errorMsg || '服务器错误')  // 显示："用户名或密码错误"
          break
        default:
          // 不会进入
      }
    }
  }
) */

  error => {
    // 处理 HTTP 状态码错误或其他网络错误
    if (error.response) {
      
      // 尝试获取后端返回的错误消息
      // 尝试按优先级获取错误消息
      let errorMsg = ''
      // 可选链操作符 ?. 如果前面是 null 或 undefined ，就停止执行，返回 undefined
      if (error.response?.data?.message) {
        errorMsg = error.response.data.message
      /* 等价于（传统写法）
      if (error.response && error.response.data && error.response.data.message) {
          errorMsg = error.response.data.message
      } */
      } else if (error.response?.data?.msg) {
        errorMsg = error.response.data.msg
      } else if (error.response?.statusText) {
        errorMsg = error.response.statusText
      }
      
      // 根据 HTTP 状态码进行不同处理
      switch (error.response.status) {
        case 401:
          // 未授权：登录过期或无效
          ElMessage.error(errorMsg || '登录已过期，请重新登录')
          // 清除本地存储的 token 和用户信息
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('userInfo')
          // 跳转到登录页
          router.push('/login')
          break
        case 403:
          // 禁止访问：权限不足
          ElMessage.error(errorMsg || '没有权限访问')
          break
        case 404:
          // 资源不存在
          ElMessage.error(errorMsg || '请求地址不存在')
          break
        case 500:
          // 服务器内部错误
          ElMessage.error(errorMsg || '服务器错误')
          break
        default:
          // 其他错误
          ElMessage.error(errorMsg || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      // 请求超时
      ElMessage.error('请求超时，请稍后重试')
    } else {
      // 网络连接错误
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    // 抛出错误，以便调用方可以捕获
    return Promise.reject(error)
  }
)

// 导出配置好的 axios 实例
export default request
