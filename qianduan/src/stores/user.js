/**
 * 用户状态管理 Store 模块
 * 
 * 功能说明：
 * 1. 使用 Pinia 管理全局用户状态（Token、用户信息、角色权限）
 * 2. 提供用户登录、登出、信息更新等操作
 * 3. 持久化到 sessionStorage，刷新页面不丢失
 * 4. 提供计算属性判断用户角色和登录状态
 * 
 * 使用规范：
 * - 在组件中使用：const userStore = useUserStore()
 * - 访问状态：userStore.token, userStore.userInfo
 * - 调用方法：userStore.login(), userStore.logout()
 * 
 * 角色说明：
 * - role: 1 = 学生
 * - role: 2 = 维修员
 * - role: 3 = 管理员
 */
import { defineStore } from 'pinia'
import { login as loginApi } from '../api/auth'

/**
 * 用户状态管理 Store
 * 使用 Pinia 管理全局用户信息（Token、用户资料、角色权限等）
 * 
 * 为什么需要 Store？
 * - 组件之间共享数据（比如 Header 组件要显示用户名，Sidebar 要根据角色显示菜单）
 * - 避免在每个组件里重复从 sessionStorage 读取数据
 * - 提供统一的状态更新入口
 */
export const useUserStore = defineStore('user', {
  // state: 存储响应式数据，类似 Vue 组件的 data()
  // 唯一的公共数据源，所有组件都可以访问和修改
  state: () => ({
    // 从 sessionStorage 恢复 token，如果没有则为空字符串
    token: sessionStorage.getItem('token') || '',
    // 从 sessionStorage 恢复用户信息，解析 JSON 字符串为对象
    userInfo: JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  }),

  // getters: 计算属性，类似 Vue 组件的 computed
  // 用于派生出一些常用的状态判断，避免在组件里写重复的逻辑
  getters: {
    //getters计算属性的第一个参数是state，用于访问 state 中的属性
    //getters函数的返回值是计算后的结果，用于在组件中直接使用
    //这里都是箭头函数，没有 this 指向问题，直接使用 state 即可
    /**
     * 判断用户是否已登录
     * !!token 将 token 转换为布尔值：有 token 返回 true，无 token 返回 false,!!是逻辑非操作符，将 token 转换为布尔值
     */
    isLoggedIn: state => !!state.token,
    
    /**
     * 获取用户角色
     * role: 1=学生, 2=维修员, 3=管理员
     */
    role: state => state.userInfo?.role,
    
    /**
     * 判断是否是学生
     * 可选链操作符 ?. 在访问对象的属性或方法时，如果对象本身是 null 或 undefined，不会报错，而是直接返回 undefined。
     */
    isStudent: state => state.userInfo?.role === 1,
    
    /**
     * 判断是否是维修员
     */
    isRepairman: state => state.userInfo?.role === 2,
    
    /**
     * 判断是否是管理员
     */
    isAdmin: state => state.userInfo?.role === 3,
    
    /**
     * 获取用户显示名称
     * 优先使用 realName（真实姓名），如果没有则用 username（账号名）
     */
    userName: state => state.userInfo?.realName || state.userInfo?.username || ''
    //可选链 ?.   在访问对象的属性或方法时，如果对象本身是 null 或 undefined，不会报错，而是直接返回 undefined。
  },

  // actions: 方法集合，类似 Vue 组件的 methods
  // 用于执行异步操作或修改 state
  actions: {
    /**
     * 用户登录动作
     * @param {string} username - 用户名
     * @param {string} password - 密码
     * @returns {Promise<Object>} 返回用户信息对象
     * 
     * 工作流程：
     * 1. 调用 API 层的 loginApi 发起登录请求
     * 2. 等待后端返回结果（await 会暂停执行直到 Promise 完成）
     * 3. 从响应中提取 token 和 user 信息
     * 4. 更新 Store 的 state（内存中的状态）
     * 5. 同步更新 sessionStorage（持久化存储，防止刷新丢失）
     * 6. 返回用户信息供调用方使用（比如跳转页面）
     */
    async login(username, password) {
      // 调用 API 接口，等待登录结果
      // res 的结构：{ code: 200, message: "success", data: { token, user } }
      const res = await loginApi(username, password)
      
      // 解构赋值：从 res.data 中提取 token 和 user
      const { token, user } = res.data

      // 更新 Pinia Store 的状态（响应式数据）
      // 所有引用了 userStore 的组件会自动重新渲染
      this.token = token
      this.userInfo = user

      // 同步更新 sessionStorage（持久化存储）
      // 这样即使刷新页面，state 初始化时也能从 sessionStorage 恢复数据
      sessionStorage.setItem('token', token)
      sessionStorage.setItem('userInfo', JSON.stringify(user))

      // 返回用户信息，方便调用方进行后续操作（如路由跳转）
      return user
    },

    /**
     * 用户登出动作
     * 清除所有登录相关的状态和缓存
     * 
     * 使用场景：
     * - 用户点击"退出登录"按钮
     * - Token 过期被拦截器检测到
     * - 切换账号时
     */
    logout() {
      // 清空 Store 中的状态
      this.token = ''
      this.userInfo = {}
      
      // 清除 sessionStorage 中的数据
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
      
      // 注意：这里没有处理路由跳转
      // 通常在调用 logout() 后，由调用方决定跳转到哪个页面（如登录页）
    }
  }
})