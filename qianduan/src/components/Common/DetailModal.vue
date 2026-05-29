<!--
  详情弹窗组件
  
  功能说明：
  1. 封装 Element Plus 的 Dialog 组件，提供统一的详情展示弹窗
  2. 支持自定义标题、宽度、是否可关闭等属性
  3. 通过插槽允许自定义弹窗内容
  
  使用示例：
  <DetailModal
    v-model:visible="dialogVisible"
    title="报修详情"
    width="700px"
  >
    <p>自定义内容...</p>
  </DetailModal>
-->
<script setup>
// 导入 Vue 的响应式 API 和 Element Plus 组件
import { ref, watch } from 'vue'
import { ElDialog, ElButton } from 'element-plus'

// 定义组件接收的属性
const props = defineProps({
  // 对话框是否可见
  visible: {
    type: Boolean,
    default: false
  },
  // 对话框标题
  title: {
    type: String,
    default: '详情'
  },
  // 对话框宽度
  width: {
    type: String,
    default: '600px'
  },
  // 是否显示关闭按钮
  closable: {
    type: Boolean,
    default: true
  }
})

// 定义组件事件
const emit = defineEmits(['update:visible', 'close'])

// 创建本地变量用于控制对话框的显示状态
const isVisible = ref(props.visible)

// 监听父组件传入的 visible 属性变化，同步更新本地状态
watch(() => props.visible, (newVal) => {
  isVisible.value = newVal
})

// 监听本地状态变化，当对话框关闭时触发相应事件
watch(isVisible, (newVal) => {
  if (!newVal) {
    // 更新父组件的 visible 属性为 false
    emit('update:visible', false)
    // 触发关闭事件
    emit('close')
  }
})

// 处理关闭对话框的操作
const handleClose = () => {
  isVisible.value = false
}
</script>

<template>
  <!-- Element Plus 对话框组件 -->
  <ElDialog
    :title="title"
    v-model="isVisible"
    :width="width"
    :closable="closable"
    @close="handleClose"
  >
    <!-- 默认插槽，用于插入对话框内容 -->
    <slot></slot>
    <!-- 底部插槽，自定义底部按钮 -->
    <template #footer>
      <!-- 如果父组件没有提供 footer 插槽，则显示默认的关闭按钮 -->
      <slot name="footer">
        <ElButton @click="handleClose">关闭</ElButton>
      </slot>
    </template>
  </ElDialog>
</template>