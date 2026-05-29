<!--
  未读消息徽章组件
  
  功能说明：
  1. 显示未读消息数量徽章
  2. 支持设置最大值，超过最大值显示为 "max+"
  3. 支持多种尺寸
  
  使用示例：
  <UnreadBadge :count="unreadCount" :max="99" size="small" />
-->
<script setup>
// 导入 Vue 的 computed 函数用于创建计算属性
import { computed } from 'vue'

// 定义组件接收的属性
const props = defineProps({
  // 消息数量
  count: {
    type: Number,
    default: 0
  },
  // 最大显示数量，超过此数值会显示为 "max+" 的形式
  max: {
    type: Number,
    default: 99
  },
  // 徽章尺寸大小
  size: {
    type: String,
    default: 'default',
    // 验证传入的尺寸值是否在允许范围内
    validator: (val) => ['small', 'default', 'large'].includes(val)
  }
})

// 计算要显示的数量文本
const displayCount = computed(() => {
  // 如果数量小于等于0，则不显示徽章
  if (props.count <= 0) return null
  // 如果数量超过最大值，则显示 "max+" 形式
  return props.count > props.max ? `${props.max}+` : props.count
})

// 根据尺寸返回对应样式
const sizeStyle = computed(() => {
  // 不同尺寸对应的宽高和字体大小
  const sizeMap = {
    small: { width: '18px', height: '18px', fontSize: '10px' },
    default: { width: '24px', height: '24px', fontSize: '12px' },
    large: { width: '32px', height: '32px', fontSize: '14px' }
  }
  return sizeMap[props.size]
})
</script>

<template>
  <!-- 当有需要显示的数量时才渲染徽章 -->
  <span 
    v-if="displayCount !== null"
    class="unread-badge"
    :style="sizeStyle"
  >
    {{ displayCount }}
  </span>
</template>

<style scoped>
/* 未读消息徽章样式 */
.unread-badge {
  /* 红色背景 */
  background-color: #f56c6c;
  /* 白色文字 */
  color: white;
  /* 圆形边框 */
  border-radius: 50%;
  /* 使用弹性布局居中内容 */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  /* 粗体文字 */
  font-weight: bold;
  /* 添加阴影效果 */
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
}
</style>