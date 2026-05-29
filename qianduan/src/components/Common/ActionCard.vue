<!--
  操作卡片组件
  
  功能说明：
  1. 封装常用的操作入口卡片样式
  2. 支持显示标题、描述、徽章数字
  3. 支持点击事件和自定义样式
  4. 常用于首页快捷操作入口
  
  使用示例：
  <ActionCard
    title="我的报修"
    description="查看和管理我的报修单"
    :badge="pendingCount"
    @click="goToRepairList"
  />
-->
<script setup>
// 定义组件属性
defineProps({
  // 卡片标题（必填）
  title: {
    type: String,
    required: true
  },
  // 卡片描述信息（可选）
  description: {
    type: String,
    default: ''
  },
  // 徽章数字，用于显示未读数量等（默认为0，大于0时显示）
  badge: {
    type: Number,
    default: 0
  },
  // 是否可点击（默认true）
  clickable: {
    type: Boolean,
    default: true
  },
  // 是否占满全宽（默认false）
  fullWidth: {
    type: Boolean,
    default: false
  }
})

// 定义组件事件：点击事件
defineEmits(['click'])
</script>

<template>
  <!-- 操作卡片容器，支持点击和全宽样式 -->
  <el-card 
    class="action-card" 
    :class="{ 'full-width': fullWidth }"
    @click="clickable && $emit('click')"
  >
    <!-- 徽章：当badge大于0时显示 -->
    <span v-if="badge > 0" class="badge">{{ badge }}</span>
    <!-- 卡片内容区域 -->
    <div class="action-content">
      <!-- 标题 -->
      <h3>{{ title }}</h3>
      <!-- 描述信息（可选） -->
      <p v-if="description">{{ description }}</p>
    </div>
  </el-card>
</template>

<style scoped>
/* 操作卡片基础样式 */
.action-card {
  cursor: pointer; /* 鼠标悬停显示手型 */
  transition: all 0.3s; /* 平滑过渡动画 */
  height: 150px; /* 固定高度 */
  display: flex; /* 弹性布局 */
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  position: relative; /* 相对定位，用于徽章绝对定位 */
}

/* 鼠标悬停效果：上移并增强阴影 */
.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 全宽样式 */
.action-card.full-width {
  width: 100%;
}

/* 内容区域样式 */
.action-content {
  text-align: center; /* 文字居中 */
  position: relative;
}

/* 标题样式 */
.action-content h3 {
  margin: 0 0 10px 0;
  color: #409eff; /* Element Plus 主题蓝色 */
}

/* 描述文字样式 */
.action-content p {
  margin: 0;
  color: #666; /* 灰色文字 */
}

/* 徽章样式：右上角圆形标记 */
.action-card .badge {
  position: absolute; /* 绝对定位 */
  top: 8px; /* 距离顶部8px */
  right: 8px; /* 距离右侧8px */
  background-color: #f56c6c; /* 红色背景 */
  color: white; /* 白色文字 */
  border-radius: 50%; /* 圆形 */
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  font-size: 12px;
}
</style>