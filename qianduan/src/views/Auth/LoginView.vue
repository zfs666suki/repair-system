<!--
  登录页面视图
  
  功能说明：
  1. 用户登录入口页面
  2. 支持学生、维修员、管理员三种角色登录
  3. 登录成功后根据角色跳转到对应首页
  
  路由：/login
-->
<script setup>
// 导入Vue Router用于页面跳转
import { useRouter } from 'vue-router'
// 导入用户状态管理store
import { useUserStore } from '../../stores/user'
// 导入Element Plus消息提示组件
import { ElMessage } from 'element-plus'
// 导入Vue响应式API
import { reactive } from 'vue'

// 获取路由实例
const router = useRouter()
// 获取用户状态管理实例
const userStore = useUserStore()

// 定义表单数据，包含用户名和密码
const formData = reactive({
  username: '',
  password: ''
})

/**
 * 处理登录逻辑
 * 1. 验证表单数据
 * 2. 调用登录接口
 * 3. 根据用户角色跳转到对应页面
 */
const handleLogin = async () => {
  // 验证用户名是否为空
  if (!formData.username) {
    ElMessage.error('请输入用户名')
    return
  }
  // 验证用户名长度至少3个字符
  if (formData.username.length < 3) {
    ElMessage.error('用户名至少3个字符')
    return
  }
  // 验证密码是否为空
  if (!formData.password) {
    ElMessage.error('请输入密码')
    return
  }
  // 验证密码长度至少6个字符
  if (formData.password.length < 6) {
    ElMessage.error('密码至少6个字符')
    return
  }

  try {
    // 调用用户store的登录方法
    const result = await userStore.login(formData.username, formData.password)
    if (result) {
      // 根据用户角色进行页面跳转
      // role === 1: 学生用户
      if (result.role === 1) {
        router.push('/student')
      // role === 2: 维修人员
      } else if (result.role === 2) {
        router.push('/repairman')
      // role === 3: 管理员
      } else if (result.role === 3) {
        router.push('/admin')
      }
    }
  } catch (error) {
    // 登录失败时自动显示错误信息（store内部已处理）
  }
}
</script>

<template>
  <!-- 登录页面容器 -->
  <div class="login-container">
    <!-- 登录框 -->
    <div class="login-box">
      <!-- 系统标题 -->
      <h2>宿舍报修系统</h2>
      <!-- 登录表单 -->
      <el-form :model="formData" class="login-form">
        <!-- 用户名输入框 -->
        <el-form-item>
          <el-input
            v-model="formData.username"
            placeholder="用户名"
            size="large"
          />
        </el-form-item>
        <!-- 密码输入框，支持回车键提交 -->
        <el-form-item>
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="密码"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="handleLogin"
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
/* 登录页面容器样式：全屏居中显示 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-image: url('/src/assets/img/q.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* 登录框样式：白色背景，圆角，阴影效果 */
.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

/* 标题样式：居中对齐 */
h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

/* 表单样式：上边距 */
.login-form {
  margin-top: 20px;
}

/* 登录按钮样式：占满宽度 */
.login-button {
  width: 100%;
}
</style>