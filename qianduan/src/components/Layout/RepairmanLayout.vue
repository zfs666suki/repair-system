<!--
  维修员端布局组件
  
  功能说明：
  1. 基于 BaseLayout 封装维修员端专属布局
  2. 配置维修员端菜单项（首页、待接单、进行中、维修历史、系统通知、个人消息）
  3. 初始化 WebSocket 连接，接收实时订单推送和聊天消息
  
  使用说明：
  - 在路由配置中使用此组件作为 layout
  - 所有维修员端页面都会嵌套在此布局中
-->
<script setup>
import BaseLayout from './BaseLayout.vue'
import { useWebSocket } from '../../composables/useWebSocket'

const menuItems = [
  { index: '/repairman', label: '首页' },
  { index: '/repairman/orders/pending', label: '待接单' },
  { index: '/repairman/orders/processing', label: '进行中' },
  { index: '/repairman/orders/history', label: '维修历史' },
  { index: '/repairman/notification', label: '系统通知' },
  { index: '/repairman/messages', label: '个人消息' }
]

useWebSocket()
</script>

<template>
  <BaseLayout page-title="维修员端" :menu-items="menuItems" />
<!-- 模板中              page-title
     defineProps 中     pageTitle
     说明               Vue 会自动转换为 pageTitle -->
<!-- 静态属性（无冒号）    page-title
     动态绑定（有冒号 :） :page-title="pageTitle" -->
</template>