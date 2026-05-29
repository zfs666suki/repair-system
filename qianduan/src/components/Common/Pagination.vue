<!--
  分页组件
  
  功能说明：
  1. 封装 Element Plus 的分页功能，提供统一的分页交互
  2. 支持页码切换、每页条数调整
  3. 支持自定义分页布局
  
  使用示例：
  <Pagination
    :total="total"
    v-model:pageNum="currentPage"
    v-model:pageSize="pageSize"
    @change="loadData"
  />
-->
<script setup>
// 导入Vue核心API：ref用于创建响应式引用，computed用于计算属性，watch用于监听变化
import { ref, computed, watch } from 'vue'

/**
 * 定义组件接收的属性（Props）
 */
const props = defineProps({
  // 数据总条数，用于计算总页数
  total: {
    type: Number,
    default: 0
  },
  // 当前页码
  pageNum: {
    type: Number,
    default: 1
  },
  // 每页显示条数
  pageSize: {
    type: Number,
    default: 10
  },
  // 每页条数选择器的选项数组
  pageSizes: {
    type: Array,
    // 返回默认数组，防止多个实例共享同一个引用
    default: () => [ 5,10, 20, 50]
  },
  // 分页组件布局字符串，控制显示哪些元素
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  // 是否显示每页条数选择器
  showSizes: {
    type: Boolean,
    default: true
  }
})

/**
 * 定义组件事件（Emits）
 * update:pageNum 和 update:pageSize 用于支持 v-model 双向绑定
 * change 用于通知父组件分页参数发生了变化
 */
const emit = defineEmits(['update:pageNum', 'update:pageSize', 'change'])

// 创建本地响应式变量，用于 el-pagination 组件的双向绑定
// 使用本地变量可以避免直接修改 props 导致的警告，并实现更平滑的交互
const currentPage = ref(props.pageNum)
const currentPageSize = ref(props.pageSize)

// 监听父组件传入的 pageNum 变化，同步更新本地变量
// 例如：当父组件重置搜索条件将页码设为1时，分页器也会跟着变
watch(() => props.pageNum, (newVal) => {
  currentPage.value = newVal
})

// 监听父组件传入的 pageSize 变化，同步更新本地变量
watch(() => props.pageSize, (newVal) => {
  currentPageSize.value = newVal
})

/**
 * 计算最终的布局字符串
 * 如果 showSizes 为 false，则从布局字符串中移除 'sizes' 相关部分
 */
const computedLayout = computed(() => {
  if (!props.showSizes) {
    // 通过字符串替换移除 sizes 及其前后的逗号空格
    return props.layout.replace(', sizes', '').replace('sizes, ', '')
  }
  return props.layout
})

/**
 * 处理页码改变事件
 * @param {number} page - 新的页码
 */
const handlePageChange = (page) => {
  // 更新本地页码
  currentPage.value = page
  // 触发 update 事件，支持 v-model:page-num 双向绑定
  emit('update:pageNum', page)
  // 触发 change 事件，通知父组件执行数据加载
  emit('change', { pageNum: page, pageSize: currentPageSize.value })
}

/**
 * 处理每页条数改变事件
 * @param {number} size - 新的每页条数
 */
const handleSizeChange = (size) => {
  // 更新本地每页条数
  currentPageSize.value = size
  // 切换每页条数时，通常重置回第一页
  currentPage.value = 1
  // 触发 update 事件，支持双向绑定
  emit('update:pageSize', size)
  emit('update:pageNum', 1)
  // 触发 change 事件，通知父组件以新参数加载数据
  emit('change', { pageNum: 1, pageSize: size })
}
</script>

<template>
  <!-- 分页容器 -->
  <div class="pagination-wrapper">
    <!-- Element Plus 分页组件 -->
    <!-- v-model:current-page 和 v-model:page-size 实现与本地变量的双向绑定 -->
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="currentPageSize"
      :total="total"
      :page-sizes="pageSizes"
      :layout="computedLayout"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<style scoped>
/* 分页容器样式：右对齐，顶部留白 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>