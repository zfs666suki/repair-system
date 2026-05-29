<!--
  应用根组件 - App.vue
  
  功能说明：
  1. 作为 Vue 应用的顶层组件，是整个应用的入口点
  2. 使用 RouterView 渲染当前路由匹配的组件
  3. 定义全局基础样式重置规则
  
  工作流程：
  1. main.js 创建应用实例并将 App 作为根组件
  2. App 组件中的 RouterView 根据当前 URL 路径渲染对应的页面组件
  3. 全局样式规则应用于所有元素，确保一致的盒模型和间距处理
  
  架构说明：
  - 采用 <script setup> 语法糖，简化组件编写
  - RouterView 是 vue-router 提供的内置组件，负责动态渲染路由匹配的视图
  - 全局样式使用非 scoped 的 <style> 标签，确保规则作用于整个应用
-->
<script setup>
// 从 vue-router 导入 RouterView 组件
// RouterView 是一个占位符组件，会根据当前路由配置动态渲染对应的页面组件
import { RouterView } from 'vue-router'
</script>

<template>
  <!-- 
    路由视图占位符
    根据当前 URL 路径和路由配置，自动渲染匹配的组件
    例如：
    - 访问 /login 时渲染 LoginView 组件
    - 访问 /student/home 时渲染 StudentHomeView 组件
    - 访问 /admin/users 时渲染 UserManagementView 组件
  -->
  <RouterView />
</template>

<style>
/* 
  全局 CSS 重置规则
  
  作用范围：
  - 应用于所有 HTML 元素（* 选择器）
  - 由于未使用 scoped 属性，这些规则会作用于整个应用
  
  规则说明：
  1. margin: 0 - 移除所有元素的默认外边距，避免浏览器默认样式差异
  2. padding: 0 - 移除所有元素的默认内边距，确保布局一致性
  3. box-sizing: border-box - 使用边框盒模型，使元素的宽度和高度包含 padding 和 border
     这样设置后，元素的实际尺寸 = width/height + padding + border
     便于精确控制布局，避免盒模型计算导致的尺寸溢出问题
  
  注意事项：
  - 这是常见的 CSS Reset 实践，确保跨浏览器的一致性
  - 更复杂的样式重置建议在 assets/styles/main.css 中统一管理
*/
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>