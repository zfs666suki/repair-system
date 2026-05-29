/**
 * Vue 应用主入口文件
 * 
 * 功能说明：
 * 1. 创建并配置 Vue 应用实例
 * 2. 注册全局插件和依赖（Pinia、Vue Router、Element Plus）
 * 3. 挂载应用到 DOM 节点
 * 
 * 初始化流程：
 * 1. 导入全局样式和第三方库样式
 * 2. 创建 Vue 应用实例
 * 3. 依次注册 Pinia（状态管理）、Vue Router（路由）、Element Plus（UI 组件库）
 * 4. 将应用挂载到 #app 元素上
 * 
 * 依赖说明：
 * - Vue 3: 前端框架核心
 * - Pinia: Vue 官方推荐的状态管理库
 * - Vue Router: 官方路由管理器
 * - Element Plus: 基于 Vue 3 的 UI 组件库
 */

// 导入全局自定义样式（包含基础重置、通用工具类等）
import './assets/styles/main.css'

// 导入 Vue 核心函数，用于创建应用实例
import { createApp } from 'vue'

// 导入 Pinia 状态管理库，用于创建全局状态存储
import { createPinia } from 'pinia'

// 导入 Element Plus UI 组件库及其样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 导入根组件 App.vue，作为应用的顶层组件
import App from './App.vue'

// 导入路由配置，包含所有页面路由定义和守卫逻辑
import router from './router'

// 创建 Vue 应用实例，以 App 组件作为根组件
const app = createApp(App)

// 注册 Pinia 状态管理插件，使所有组件可以通过 useUserStore() 等方式访问全局状态
app.use(createPinia())

// 注册 Vue Router 路由插件，使应用支持路由导航、路由守卫、参数传递等功能
app.use(router)

// 注册 Element Plus UI 组件库，使所有组件可以直接使用 el-button、el-table 等 Element Plus 组件，无需单独导入
app.use(ElementPlus)

// 将配置好的 Vue 应用挂载到 HTML 中 id 为 'app' 的 DOM 元素上，此时应用正式开始运行，渲染根组件并激活所有插件
app.mount('#app')