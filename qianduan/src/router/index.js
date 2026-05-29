/**
 * Vue Router 路由配置模块
 *
 * 功能说明：
 * 1. 定义应用的所有路由规则和页面映射关系
 * 2. 实现基于角色的访问控制（RBAC）
 * 3. 提供路由守卫进行身份验证和权限检查
 * 4. 支持路由懒加载优化性能
 *
 * 路由结构：
 * - 登录页：/login
 * - 学生端：/student/* （角色值：1）
 * - 维修员端：/repairman/* （角色值：2）
 * - 管理员端：/admin/* （角色值：3）
 *
 * 技术栈：
 * - vue-router 4.x：Vue 3 官方路由管理器
 * - createWebHistory：使用 HTML5 History 模式，URL 不带 # 号
 *
 * 使用规范：
 * - 在组件中使用：import router from '@/router'
 * - 编程式导航：router.push('/path')
 * - 获取当前路由：useRoute()
 */
import { createRouter, createWebHistory } from 'vue-router'

/**
 * 创建路由实例
 * 
 * 配置说明：
 * - history: 使用 HTML5 History 模式，需要服务器支持 URL 重写
 * - BASE_URL: 从环境变量获取基础路径，支持部署到子目录
 * - routes: 定义所有路由规则，采用懒加载方式提升性能
 */
const router = createRouter({
  // 使用 HTML5 History 模式，URL 更美观（如 /student/home 而非 /#/student/home）
  // import.meta.env.BASE_URL 允许应用部署在非根路径下
  history: createWebHistory(import.meta.env.BASE_URL),
  
  // 路由配置数组，按顺序匹配
  routes: [
    /**
     * 根路径重定向
     * 访问 / 时自动跳转到登录页
     */
    {
      path: '/',
      redirect: '/login'
    },
    
    /**
     * 登录页面路由
     * 无需认证即可访问
     * 使用动态导入实现懒加载，首次访问时才加载该组件
     */
    {
      path: '/login',
      name: 'Login', // 路由名称，用于编程式导航（如 router.push({ name: 'Login' })）
      component: () => import('../views/Auth/LoginView.vue') // 懒加载登录组件
    },

    /**
     * 学生端路由组
     * 
     * 架构说明：
     * - 父路由：StudentLayout 作为布局容器，包含侧边栏、头部等公共元素
     * - 子路由：具体的页面组件，渲染在 StudentLayout 的 <RouterView /> 中
     * - meta.role: 1 表示只有学生角色可以访问
     * 
     * 访问示例：
     * - /student -> StudentHomeView（首页）
     * - /student/repair/submit -> SubmitRepairView（提交报修）
     * - /student/repair/list -> StudentRepairListView（报修列表）
     */
    {
      path: '/student',
      // 学生端布局组件，包含侧边栏导航和顶部栏
      component: () => import('../components/Layout/StudentLayout.vue'),
      // 元数据：标记该路由组需要的角色权限（1=学生）
      // meta 是 Vue Router 中路由配置的一个元数据字段，用于存储与路由相关的自定义信息。
      // 在 Vue Router 的路由配置对象中，meta 是一个普通的 JavaScript 对象。
      // 你可以往里面放任何你想关联到这个路由的数据。它不会影响路由的匹配或渲染，但可以在路由守卫、组件内部或导航过程中被读取和使用。
      meta: { role: 1 },
      // 子路由配置，路径会相对于父路由拼接
      children: [
        {
          path: '', // 空路径表示默认子路由，访问 /student 时匹配
          name: 'StudentHome',
          component: () => import('../views/Student/StudentHomeView.vue')
        },
        {
          path: 'repair/submit', // 完整路径：/student/repair/submit
          name: 'SubmitRepair',
          component: () => import('../views/Student/SubmitRepairView.vue')
        },
        {
          path: 'repair/list', // 完整路径：/student/repair/list
          name: 'StudentRepairList',
          component: () => import('../views/Student/StudentRepairListView.vue')
        },
        {
          path: 'repair/detail/:id', // 动态路由参数，完整路径如：/student/repair/detail/123
          name: 'StudentRepairDetail',
          component: () => import('../views/Student/RepairDetailView.vue')
        },
        {
          path: 'notification', // 完整路径：/student/notification
          name: 'StudentNotification',
          component: () => import('../views/Student/NotificationListView.vue')
        },
        {
          path: 'messages', // 完整路径：/student/messages
          name: 'StudentMessages',
          component: () => import('../views/Student/MessagesView.vue')
        },
        {
          path: 'chat', // 完整路径：/student/chat
          name: 'StudentChat',
          component: () => import('../views/Student/ChatView.vue')
        }
      ]
    },

    /**
     * 维修员端路由组
     * 
     * 架构说明：
     * - 父路由：RepairmanLayout 作为布局容器
     * - meta.role: 2 表示只有维修员角色可以访问
     * 
     * 主要功能模块：
     * - 待处理订单、处理中订单、历史订单管理
     * - 消息通知和聊天功能
     */
    {
      path: '/repairman',
      // 维修员端布局组件
      component: () => import('../components/Layout/RepairmanLayout.vue'),
      // 元数据：标记该路由组需要的角色权限（2=维修员）
      meta: { role: 2 },
      children: [
        {
          path: '', // 完整路径：/repairman
          name: 'RepairmanHome',
          component: () => import('../views/Repairman/RepairmanHomeView.vue')
        },
        {
          path: 'orders/pending', // 完整路径：/repairman/orders/pending
          name: 'PendingOrders',
          component: () => import('../views/Repairman/PendingOrdersView.vue')
        },
        {
          path: 'orders/processing', // 完整路径：/repairman/orders/processing
          name: 'ProcessingOrders',
          component: () => import('../views/Repairman/ProcessingOrdersView.vue')
        },
        {
          path: 'orders/history', // 完整路径：/repairman/orders/history
          name: 'HistoryOrders',
          component: () => import('../views/Repairman/HistoryOrdersView.vue')
        },
        {
          path: 'order/detail/:id', // 动态路由，完整路径如：/repairman/order/detail/456
          name: 'RepairmanRepairDetail',
          component: () => import('../views/Repairman/RepairOrderDetailView.vue')
        },
        {
          path: 'messages', // 完整路径：/repairman/messages
          name: 'RepairmanMessages',
          component: () => import('../views/Repairman/RepairmanMessagesView.vue')
        },
        {
          path: 'notification', // 完整路径：/repairman/notification
          name: 'RepairmanNotification',
          component: () => import('../views/Repairman/RepairmanNotificationView.vue')
        },
        {
          path: 'chat', // 完整路径：/repairman/chat
          name: 'RepairmanChat',
          component: () => import('../views/Repairman/RepairmanChatView.vue')
        }
      ]
    },

    /**
     * 管理员端路由组
     * 
     * 架构说明：
     * - 父路由：AdminLayout 作为布局容器
     * - meta.role: 3 表示只有管理员角色可以访问
     * 
     * 主要功能模块：
     * - 用户管理、楼栋管理、房间管理、宿舍管理
     * - 故障类型管理、报修单管理
     * - 数据统计、导出功能
     * - 消息通知和聊天功能
     */
    {
      path: '/admin',
      // 管理员端布局组件
      component: () => import('../components/Layout/AdminLayout.vue'),
      // 元数据：标记该路由组需要的角色权限（3=管理员）
      meta: { role: 3 },
      children: [
        {
          path: '', // 完整路径：/admin
          name: 'AdminHome',
          component: () => import('../views/Admin/AdminHomeView.vue')
        },
        {
          path: 'users', // 完整路径：/admin/users
          name: 'UserManagement',
          component: () => import('../views/Admin/UserManagementView.vue')
        },
        {
          path: 'users/import', // 完整路径：/admin/users/import
          name: 'ImportStudents',
          component: () => import('../views/Admin/ImportStudentsView.vue')
        },
        {
          path: 'buildings', // 完整路径：/admin/buildings
          name: 'BuildingManagement',
          component: () => import('../views/Admin/BuildingManagementView.vue')
        },
        {
          path: 'rooms', // 完整路径：/admin/rooms
          name: 'RoomManagement',
          component: () => import('../views/Admin/RoomManagementView.vue')
        },
        {
          path: 'dormitories', // 完整路径：/admin/dormitories
          name: 'DormitoryManagement',
          component: () => import('../views/Admin/DormitoryManagementView.vue')
        },
        {
          path: 'fault-types', // 完整路径：/admin/fault-types
          name: 'FaultTypeManagement',
          component: () => import('../views/Admin/FaultTypeManagementView.vue')
        },
        {
          path: 'repair-orders', // 完整路径：/admin/repair-orders
          name: 'RepairOrderManagement',
          component: () => import('../views/Admin/RepairOrderManagementView.vue')
        },
        {
          path: 'statistics', // 完整路径：/admin/statistics
          name: 'Statistics',
          component: () => import('../views/Admin/StatisticsView.vue')
        },
        {
          path: 'export', // 完整路径：/admin/export
          name: 'Export',
          component: () => import('../views/Admin/ExportView.vue')
        },
        {
          path: 'notification', // 完整路径：/admin/notification
          name: 'AdminNotification',
          component: () => import('../views/Admin/AdminNotificationView.vue')
        },
        {
          path: 'messages', // 完整路径：/admin/messages
          name: 'AdminMessages',
          component: () => import('../views/Admin/AdminMessagesView.vue')
        },
        {
          path: 'chat', // 完整路径：/admin/chat
          name: 'AdminChat',
          component: () => import('../views/Admin/AdminChatView.vue')
        }
      ]
    }
  ]
})

/**
 * 全局前置路由守卫
 * 
 * 功能说明：
 * 1. 身份验证：检查用户是否已登录（通过 token 判断）
 * 2. 权限控制：根据用户角色限制访问特定路由
 * 3. 自动跳转：未登录用户访问受保护路由时跳转到登录页
 * 
 * 工作流程：
 * 1. 每次路由跳转前触发此守卫
 * 2. 从 sessionStorage 获取 token 和用户信息
 * 3. 根据目标路由和当前状态决定放行、重定向或拦截
 * 
 * @param {Object} to - 即将要进入的目标路由对象
 * @param {Object} from - 当前导航正要离开的路由对象
 * @param {Function} next - 必须调用的函数，用于解析守卫
 *                         - next()：放行
 *                         - next('/path')：重定向到指定路径
 *                         - next(false)：中断导航
 */
/* to：你要去哪里？（目标路由对象）to.path、to.name、to.query 、to.meta等,，有各种属性，用于判断路由类型、参数传递等信息
例子：如果你点击了“用户管理”，to 就长这样：
{
  path: '/admin/users',
  name: 'UserManagement',
  meta: { role: 3 }, // 只有管理员能进
  // ... 其他信息
} */
/* from：你从哪里来？（当前路由对象）from.path、from.name、from.query 、from.meta等,，有各种属性，用于判断路由类型、参数传递等信息
例子：如果你从“用户管理”跳转到“学生管理”，from 就长这样：
{
  path: '/admin/users',
  name: 'UserManagement',
  meta: { role: 3 }, // 只有管理员能进
  // ... 其他信息
} */
/* next：放行指令（核心函数），用于解析守卫（next()：放行，next('/path')：重定向到指定路径，next(false)：中断导航）
例子：next() // 放行
例子：next('/login') // 重定向到登录页
例子：next(false) // 中断导航 */
  router.beforeEach((to, from, next) => {
  // 从 sessionStorage 获取认证信息
  const token = sessionStorage.getItem('token')
  const userInfoStr = sessionStorage.getItem('userInfo')
  // 解析用户信息，如果不存在则使用空对象
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : {}

  // 情况1：目标路由是登录页
  if (to.path === '/login') {
    // 如果用户已登录且有角色信息，根据角色重定向到对应首页
    if (token && userInfo.role) {
      if (userInfo.role === 1) {
        next('/student') // 学生跳转到学生首页
      } else if (userInfo.role === 2) {
        next('/repairman') // 维修员跳转到维修员首页
      } else if (userInfo.role === 3) {
        next('/admin') // 管理员跳转到管理员首页
      }
    } else {
      // 未登录或无角色信息，允许访问登录页
      next()
    }
  } 
  // 情况2：目标路由不是登录页（需要认证的路由）
  else {
    // 如果没有 token，说明未登录，强制跳转到登录页
    if (!token) {
      next('/login')
    } else {
      // 有 token，继续检查角色权限
      const requiredRole = to.meta.role // 获取目标路由要求的角色
      
      // 如果路由定义了角色要求，且当前用户角色不匹配

      /*to: 是你想去的那个页面（比如 /admin/users）。
        meta: 是我们在定义路由时贴在那里的“标签”。
        requiredRole: 就是那个标签的值。
        如果你去学生页，requiredRole 就是 1。
        如果你去管理员页，requiredRole 就是 3。
        如果你去登录页，requiredRole 就是 undefined（因为登录页没贴标签）。*/ 
    
      /*requiredRole:
        如果这个值是 0、null 或 undefined，条件直接为假。
        目的：这意味着如果目标页面没有设置权限要求（比如某些公共页面），保安就直接放行，不进行后面的角色比对。
        userInfo.role !== requiredRole:
        这是在做不等式比对。
        userInfo.role 是你真实的身份（比如你是维修员，值为 2）。
        requiredRole 是页面的要求（比如页面要求管理员，值为 3）。
        !==: 如果 2 不等于 3，说明你走错门了 */
      if (requiredRole && userInfo.role !== requiredRole) {
        // 根据用户实际角色重定向到对应的首页，防止越权访问
        if (userInfo.role === 1) {
          next('/student')
        } else if (userInfo.role === 2) {
          next('/repairman')
        } else if (userInfo.role === 3) {
          next('/admin')
        }
      } else {
        // 角色匹配或未定义角色要求，允许访问
        next()
      }
    }
  }
})

// 导出路由实例，供 main.js 注册使用
export default router