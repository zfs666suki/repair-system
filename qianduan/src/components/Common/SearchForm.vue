<!--
  搜索表单组件
  
  功能说明：
  1. 封装通用的搜索表单，支持输入框、下拉选择等多种搜索条件
  2. 支持搜索和重置功能
  3. 通过配置化方式定义搜索字段
  
  使用示例：
  <SearchForm
    v-model="searchForm"
    :fields="searchFields"
    @search="handleSearch"
    @reset="handleReset"
  />
-->
<script setup>
// 导入 Vue 的响应式 API 和 Element Plus 组件
import { ref, watch } from 'vue'
import { ElInput, ElSelect, ElButton, ElOption } from 'element-plus'

// 定义组件接收的属性
const props = defineProps({
  // 表单数据对象，支持双向绑定
  modelValue: {
    type: Object,
    required: true
  },
  // 搜索字段配置数组
  fields: {
    type: Array,
    required: true
  },
  // 搜索按钮的文本
  searchText: {
    type: String,
    default: '搜索'
  },
  // 是否显示重置按钮
  showReset: {
    type: Boolean,
    default: true
  }
})

// 定义组件事件
const emit = defineEmits(['update:modelValue', 'search', 'reset'])

// 创建本地表单数据的响应式副本
const localForm = ref({ ...props.modelValue })

// 监听父组件传入的 modelValue 变化，同步更新本地表单数据
watch(() => props.modelValue, (newVal) => {
  localForm.value = { ...newVal }
}, { deep: true })

// 更新指定字段的值并触发双向绑定事件
const updateField = (field, value) => {
  localForm.value[field] = value
  emit('update:modelValue', { ...localForm.value })
}

// 处理搜索操作，触发 search 事件
const handleSearch = () => {
  emit('search', { ...localForm.value })
}

// 处理重置操作，将所有字段恢复为默认值
const handleReset = () => {
  // 根据字段配置的 defaultValue 初始化表单数据
  const initialValues = {}
  props.fields.forEach(field => {
    initialValues[field.key] = field.defaultValue || ''
  })
  localForm.value = { ...initialValues }
  emit('update:modelValue', { ...initialValues })
  emit('reset')
}
</script>

<template>
  <!-- 搜索表单容器 -->
  <div class="search-form">
    <!-- 遍历字段配置，动态渲染输入框或下拉选择框 -->
    <template v-for="field in fields" :key="field.key">
      <!-- 当字段类型为 input 时，渲染文本输入框 -->
      <el-input
        v-if="field.type === 'input'"
        v-model="localForm[field.key]"
        :placeholder="field.placeholder"
        :class="field.class"
        @keyup.enter="handleSearch"
        @input="updateField(field.key, localForm[field.key])"
      />
      <!-- 当字段类型为 select 时，渲染下拉选择框 -->
      <el-select
        v-else-if="field.type === 'select'"
        v-model="localForm[field.key]"
        :placeholder="field.placeholder"
        :class="field.class"
        @change="(val) => updateField(field.key, val)"
      >
        <!-- 遍历选项配置，渲染下拉选项 -->
        <el-option
          v-for="option in field.options"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </template>
    <!-- 搜索按钮 -->
    <el-button type="primary" @click="handleSearch">{{ searchText }}</el-button>
    <!-- 重置按钮（可选显示） -->
    <el-button v-if="showReset" @click="handleReset">重置</el-button>
  </div>
</template>

<style scoped>
/* 搜索表单样式 */
.search-form {
  /* 使用弹性布局 */
  display: flex;
  /* 设置元素间距 */
  gap: 12px;
  /* 底部外边距 */
  margin-bottom: 16px;
  /* 垂直居中对齐 */
  align-items: center;
  /* 允许换行 */
  flex-wrap: wrap;
}
</style>