<!--
  状态徽章组件
  
  功能说明：
  1. 根据报修单状态码显示对应的状态标签
  2. 不同状态显示不同的颜色（成功、警告、危险等）
  3. 支持多种尺寸
  
  状态映射：
  - 0: 已取消 (info)
  - 1: 待分配 (warning)
  - 2: 待接单 (warning)
  - 3: 处理中 (primary)
  - 4: 已完成 (success)
  
  使用示例：
  <StatusBadge :status="order.status" size="small" />
-->
<script setup>
// 导入Vue的计算属性API，用于创建响应式计算值
import { computed } from 'vue'

/**
 * 定义组件接收的属性
 */
const props = defineProps({
  // 状态值，可以是数字或字符串类型，必填
  status: {
    type: [Number, String],
    required: true
  },
  // 徽章尺寸，可选值为 mini、small、default，默认为 default
  size: {
    type: String,
    default: 'default',
    // 验证器：确保传入的尺寸值在允许的范围内
    validator: (val) => ['mini', 'small', 'default'].includes(val)
  }
})

/**
 * 状态映射表
 * 将报修单的状态码映射为对应的文本和标签类型
 * 0: 已取消 - info（灰色）
 * 1: 待分配 - warning（橙色）
 * 2: 待接单 - warning（橙色）
 * 3: 处理中 - primary（蓝色）
 * 4: 已完成 - success（绿色）
 * 5: 已拒绝 - danger（红色）
 */
const statusMap = {
  0: { text: '已取消', type: 'info' },
  1: { text: '待分配', type: 'warning' },
  2: { text: '待接单', type: 'warning' },
  3: { text: '处理中', type: 'primary' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已拒绝', type: 'danger' }
}

/**
 * 计算当前状态对应的信息
 * 如果status是字符串类型，先转换为数字，然后从statusMap中获取对应的配置
 * 如果找不到匹配的状态，则返回默认的"未知"状态
 */
const statusInfo = computed(() => {
  const status = typeof props.status === 'string' ? parseInt(props.status) : props.status
  return statusMap[status] || { text: '未知', type: 'info' }
})

/**
 * 计算尺寸对应的CSS类名
 * 根据size属性返回对应的Element Plus标签尺寸类
 */
const sizeClass = computed(() => {
  // 尺寸映射表：将尺寸值映射为对应的CSS类名
  const sizeMap = {
    mini: 'is-mini',
    small: 'is-small',
    default: ''
  }
  return sizeMap[props.size]
})
</script>

<template>
  <!-- 使用Element Plus的Tag组件显示状态 -->
  <!-- type绑定状态类型（决定颜色），class绑定尺寸类 -->
  <el-tag :type="statusInfo.type" :class="sizeClass">
    {{ statusInfo.text }}
  </el-tag>
</template>