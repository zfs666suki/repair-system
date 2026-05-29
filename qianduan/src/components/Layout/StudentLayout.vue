<!--
  学生端布局组件
  
  功能说明：
  1. 基于 BaseLayout 封装学生端专属布局
  2. 配置学生端菜单项（首页、提交报修、我的报修、系统通知、个人消息）
  3. 初始化 WebSocket 连接，接收实时通知和聊天消息
  
  使用说明：
  - 在路由配置中使用此组件作为 layout
  - 所有学生端页面都会嵌套在此布局中
-->
<script setup>
import BaseLayout from './BaseLayout.vue'
import { useWebSocket } from '../../composables/useWebSocket'

const menuItems = [
  { index: '/student', label: '首页' },
  { index: '/student/repair/submit', label: '提交报修' },
  { index: '/student/repair/list', label: '我的报修' },
  { index: '/student/notification', label: '系统通知' },
  { index: '/student/messages', label: '个人消息' }
]

useWebSocket()
</script>

<template>
  <BaseLayout page-title="学生端" :menu-items="menuItems" />
</template>