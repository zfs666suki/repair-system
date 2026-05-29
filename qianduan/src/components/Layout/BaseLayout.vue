<script setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '../../stores/user';

/**
 * 基础布局组件 (BaseLayout)
 * 
 * 职责：
 * 1. 提供统一的后台管理界面框架（侧边栏 + 顶栏 + 内容区）
 * 2. 处理全局导航逻辑（菜单高亮、路由跳转）
 * 3. 管理用户会话状态（显示用户名、退出登录）
 * 
 * 使用方式：
 * 由各角色专属布局（如 AdminLayout）传入配置项进行复用
 */

// ==================== 1. 属性定义 (Props) ====================
const props = defineProps({
  /**
   * 页面标题
   * 显示在顶部导航栏左侧，用于区分当前角色（如：管理员端、学生端）
   */
  pageTitle: {
    type: String,
    required: true
  },

  /**
   * 侧边栏菜单项数组
   * 结构示例：[{ index: '/path', label: '菜单名' }]
   * 由父组件根据角色权限动态传入
   */
  menuItems: {
    type: Array,
    required: true
  },

  /**
   * 是否启用 WebSocket 实时消息
   * 若开启，退出登录时需主动关闭连接以释放资源
   */
  enableWebSocket: {
    type: Boolean,
    default: false
  }
});

// ==================== 2. 响应式数据与工具 ====================
const router = useRouter();      // Vue Router 实例，用于编程式导航
/* console.log(router)
输出类似这样的对象：
Router {
  // --- 常用方法 ---
  push: ƒ push(to)          // 点击这里可以手动执行跳转到指定路由，不会替换掉当前的历史记录
  replace: ƒ replace(to)    //点击这里可以手动执行跳转到指定路由，但替换掉当前的历史记录
  go: ƒ go(delta)           // 点击这里可以跳转到指定历史记录位置，delta 为偏移量，正数表示向后跳转，负数表示向前跳转
  back: ƒ back()            // 点击这里可以跳转到上一个历史记录位置
  forward: ƒ forward()       // 点击这里可以跳转到下一个历史记录位置
  
  // --- 核心状态 ---
  currentRoute: RefImpl {
    __v_isRef: true,
    _value: {
      fullPath: "/admin/users",
      hash: "",
      matched: [...],       // 匹配到的路由组件层级
      meta: {...},          // 路由元信息
      name: "AdminUsers",
      params: {},
      path: "/admin/users", // 当前路径
      query: {}
    }
  },

  // --- 内部配置 ---
  options: {
    history: HashHistory {...},
    routes: [...]           // 你定义的所有路由规则数组
  },
  
  // --- 其他内部属性 ---
  installedApps: Set(0)     // 已安装该路由器的 Vue 应用实例
} */

const route = useRoute();        // 当前路由信息对象，用于获取当前路径
/* console.log(route) 
输出类似这样的对象：
{
  path: "/admin/users",      // <-- 我们只要这个字符串
  params: { id: "123" },
  query: { page: "1" },
  hash: "",
  fullpath: "/admin/users?page=1",
  // ... 其他很多属性
} */
const userStore = useUserStore(); // Pinia 用户状态管理，获取用户信息
/* onsole.log(userStore) 
输出类似这样的对象：
Store {
  // --- 你的 State (响应式数据) ---
  token: "eyJhbGciOiJIUzI1NiJ9...",
  userInfo: {
    id: 1,
    username: "admin",
    role: 3,
    realName: "管理员"
  },

  // --- 你的 Getters (计算属性) ---
  isLoggedIn: true,
  isAdmin: true,
  isStudent: false,
  userName: "管理员",

  // --- 你的 Actions (方法) ---
  login: ƒ login(username, password),
  logout: ƒ logout(),

  // --- Pinia 内部属性 ---
  $id: "user",
  $state: { token: "...", userInfo: {...} },
  $patch: ƒ $patch(partialStateOrMutator),
  $reset: ƒ $reset(),
  _hotUpdate: ƒ _hotUpdate(newStore),
  __v_skip: true 
} */

/**
 * 侧边栏折叠状态
 * 目前固定为 false（不折叠），预留接口以便未来扩展“收起侧边栏”功能
 */
const isCollapse = computed(() => false);

/**
 * 当前激活的菜单项
 * 自动匹配当前 URL 路径，实现菜单高亮联动
 */
//当你访问 http://localhost:5173/admin/users 时，route.path 的值就是 "/admin/users"。
const defaultActive = computed(() => route.path);
console.log('当前 route.path:', route.path);

// ==================== 3. 业务逻辑方法 ====================

/**
 * 处理下拉菜单命令
 * @param {string} command - 命令标识符（如 'logout'）
 */
const handleCommand = (command) => {
  if (command === 'logout') {
    // 步骤1: 如果启用了 WebSocket，先安全关闭连接
    if (props.enableWebSocket) {
      const { closeWebSocket } = require('../../utils/websocket');
      closeWebSocket();
    }

    // 步骤2: 清除本地 Token 和用户信息
    userStore.logout();

    // 步骤3: 强制跳转回登录页
    router.push('/login');
  }
};
</script>

<template>
  <!-- 整体容器：占满全屏高度 -->
  <el-container class="layout-container">
    
    <!-- 左侧边栏：包含 Logo 和导航菜单 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
      <div class="logo">
        <span>报修系统</span>
      </div>

      <!--Element Plus 菜单组件 
        router: 开启路由模式，点击菜单自动跳转
        :default-active: 含义：默认激活（高亮）的菜单项。
          工作原理：它需要一个字符串值，这个值必须和某个 <el-menu-item> 的 index 属性完全一致。
        :collapse 含义：是否折叠（收起）菜单。效果：
          false (展开)：显示图标 + 文字;true (折叠)：只显示图标，隐藏文字
        active-text-color="#409EFF" 含义：激活状态下文字的颜色。#409EFF 是 Element Plus 默认的主题蓝.
      -->
      <!--菜单高亮需要三个条件：
      1. 匹配机制 ：default-active === el-menu-item.index
      2. 类名添加 ：Element Plus 自动添加 is-active 类
      3. 样式定义 ：自定义 CSS 来显示高亮效果 -->
      <el-menu
        :default-active="defaultActive"
        class="sidebar-menu"
        :collapse="isCollapse"
        router
        active-text-color="#409EFF"
      >
        <!-- 循环渲染父组件传入的菜单项 -->
        <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主体区域：包含顶栏和内容区 -->
    <el-container>
      
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <!-- 显示当前模块标题 -->
          <span class="page-title">{{ pageTitle }}</span>
        </div>

        <div class="header-right">
          <!-- 用户信息下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <!-- 从 Store 获取并显示用户姓名 -->
              {{ userStore.userName }}
              <span class="arrow">▼</span>
            </span>
            
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区：动态加载路由匹配的组件 -->
      <el-main class="main-content">
        <router-view />
      </el-main>

    </el-container>
  </el-container>
</template>

<style scoped>
/* 确保布局占满整个浏览器窗口 */
.layout-container {
  height: 100vh;
}

/* 侧边栏样式 */
.aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  transition: width 0.3s; /* 平滑过渡效果 */
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #333;
  font-size: 18px;
  font-weight: bold;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e6e6e6;
}

.sidebar-menu {
  border-right: none;
  background-color: #fff;
}

/* 展开状态下的菜单宽度 */
.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.sidebar-menu .el-menu-item {
  color: #333;
}

/* 激活菜单项样式 */
.sidebar-menu .el-menu-item.is-active {
  color: #409EFF !important;
  background-color: #ecf5ff !important;
}

/* 顶部导航栏样式 */
.header {
  background-color: #fff;
  display: flex;
  justify-content: space-between; /* 左右两端对齐 */
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08); /* 底部阴影增加层次感 */
  border-bottom: 1px solid #e6e6e6;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

/* 用户信息区域 */
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #333;
}

.arrow {
  margin-left: 5px;
  font-size: 12px;
}

/* 主内容区背景与内边距 */
.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>