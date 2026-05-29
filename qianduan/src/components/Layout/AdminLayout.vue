<!--
  管理员端布局组件
  
  功能说明：
  1. 基于 BaseLayout 封装管理员端专属布局
  2. 配置管理员端菜单项（用户管理、楼栋管理、报修管理、统计分析等）
  3. 提供完整的后台管理功能入口
  
  菜单分类：
  - 基础管理：用户、楼栋、房间、宿舍分配
  - 业务配置：故障类型
  - 核心业务：报修管理
  - 数据分析：统计分析、数据导出
  - 消息中心：系统消息、个人消息
  
  使用说明：
  - 在路由配置中使用此组件作为 layout
  - 所有管理员端页面都会嵌套在此布局中
-->
<script setup>
// 导入基础布局组件
// BaseLayout 提供了通用的侧边栏、顶部导航和主内容区域结构
import BaseLayout from './BaseLayout.vue'
import { useWebSocket } from '../../composables/useWebSocket'

/**
 * 管理员端菜单配置
 * 
 * 数据结构说明：
 * - index: 路由路径，点击菜单项时跳转的目标地址
 * - label: 菜单显示文本，用户在侧边栏看到的名称
 */
const menuItems = [
  { index: '/admin', label: '首页' },              // 管理员工作台/仪表盘
  { index: '/admin/users', label: '用户管理' },     // 管理学生、维修员账号
  { index: '/admin/buildings', label: '楼栋管理' }, // 维护楼栋信息（如 A栋、B栋）
  { index: '/admin/rooms', label: '房间管理' },     // 维护房间信息（如 101、102）
  { index: '/admin/dormitories', label: '宿舍分配' }, // 将学生分配到具体宿舍
  { index: '/admin/fault-types', label: '故障类型' }, // 配置报修分类（如水电气、门窗等）
  { index: '/admin/repair-orders', label: '报修管理' }, // 查看所有报修单，指派维修员
  { index: '/admin/statistics', label: '统计分析' },   // 数据可视化（报修趋势、完成率等）
  { index: '/admin/export', label: '数据导出' },       // 导出 Excel/CSV 报表
  { index: '/admin/notification', label: '系统消息' }, // 向全体用户发送通知
  { index: '/admin/messages', label: '个人消息' }      // 管理员收到的私信/回复
]

useWebSocket()
</script>

<template>
  <!-- 
    复用基础布局组件，传入管理员专属配置
    
    属性说明：
    - page-title: 显示在顶部导航栏的标题，标识当前角色身份
    - menu-items: 侧边栏菜单数组，BaseLayout 会根据此数组渲染导航菜单
    
    设计优势：
    1. 代码复用：学生端、维修员端、管理员端都使用同一个 BaseLayout
    2. 职责分离：AdminLayout 只负责定义"管理员需要哪些菜单"
    3. 易于维护：修改布局样式只需改 BaseLayout，不影响各角色配置
  -->
  <BaseLayout page-title="管理员端" :menu-items="menuItems" />
</template>