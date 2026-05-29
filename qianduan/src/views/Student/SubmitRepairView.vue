<!--
  提交报修视图
  
  功能说明：
  1. 学生提交报修单的表单页面
  2. 支持选择故障类型、填写故障描述、上传图片
  3. 支持编辑已提交的报修单
  
  路由：/student/repair/submit
-->
<script setup>
// 导入Vue核心API：响应式引用、计算属性、生命周期钩子、响应式对象
import { ref, computed, onMounted, reactive } from 'vue'
// 导入Vue Router的路由实例和当前路由信息
import { useRouter, useRoute } from 'vue-router'
// 导入报修相关的API接口方法
import { getFaultTypes, getDormitoryInfo, submitRepair, updateRepair, getRepairDetail } from '../../api/repair'
// 导入图片上传组件
import ImageUploader from '../../components/Common/ImageUploader.vue'
// 导入Element Plus的消息提示组件
import { ElMessage } from 'element-plus'

// 获取路由实例，用于页面跳转
const router = useRouter()
// 获取当前路由信息，用于获取URL参数
const route = useRoute()

// 存储故障类型列表的响应式引用
const faultTypes = ref([])
// 存储宿舍信息的响应式引用（包含楼栋和房间号）
const dormitoryInfo = ref(null)
// 存储编辑模式下的报修单ID，null表示新增模式
const editId = ref(null)

// 表单数据响应式对象
const formData = reactive({
  faultTypeId: '',    // 故障类型ID
  description: '',    // 故障描述
  images: []          // 故障图片URL数组
})

// 计算属性：根据宿舍信息生成完整的宿舍地址字符串
const dormitoryAddress = computed(() => {
  if (!dormitoryInfo.value) return ''
  return `${dormitoryInfo.value.buildingName} ${dormitoryInfo.value.roomNumber}`
  //模板字符串
  //反引号 ` 作用：它告诉电脑：“这里面是一段特殊的字符串，我可以往里面塞变量。”
  //        对比：普通的字符串是用单引号 ' 或双引号 "，但它们只能放纯文字，不能直接放变量。
  //占位符 ${} 作用：在模板字符串中插入变量，变量的值会被替换到占位符的位置
  //        例如：`${dormitoryInfo.value.buildingName} ${dormitoryInfo.value.roomNumber}`
})

// 计算属性：判断当前是否为编辑模式（存在editId则为编辑模式）
const isEditMode = computed(() => !!editId.value)

// 组件挂载时执行：获取故障类型、宿舍信息，如果是编辑模式则获取报修详情
onMounted(async () => {
  // 从URL查询参数中获取editId，如果存在则转换为整数
  const editParam = route.query.editId
  //.query：专门用来读取 URL 问号 ? 后面的参数。 .editId：我们要找的具体参数名。    
  if (editParam) {
    editId.value = parseInt(editParam)
  }

  try {
    // 构建需要并行执行的Promise数组
    const promises = [getFaultTypes(), getDormitoryInfo()]

    // 如果是编辑模式，额外获取报修详情
    if (editId.value) {
      promises.push(getRepairDetail(editId.value))
      //promise = [getFaultTypes(), getDormitoryInfo(), getRepairDetail(editId.value)]
    }

    // 并行执行所有API请求
    const results = await Promise.all(promises)
    // 设置故障类型列表
    faultTypes.value = results[0].data || []
    // 设置宿舍信息
    dormitoryInfo.value = results[1].data

    // 如果是编辑模式且有详情数据，填充表单
    if (editId.value && results[2]) {
      const detail = results[2].data
      formData.faultTypeId = detail.faultTypeId || ''
      formData.description = detail.description || ''
      // 将图片字符串转换为数组（以逗号分隔）
      formData.images = detail.images ? detail.images.split(',') : []
    }
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
})

// 提交表单处理函数
const handleSubmit = async () => {
  // 验证故障类型是否已选择
  if (!formData.faultTypeId) {
    ElMessage.error('请选择故障类型')
    return
  }
  // 验证故障描述是否已填写
  if (!formData.description) {
    ElMessage.error('请输入故障描述')
    return
  }
  // 验证故障描述长度不超过500字符
  if (formData.description.length > 500) {
    ElMessage.error('描述不能超过500个字符')
    return
  }

  try {
    // 将图片数组转换为逗号分隔的字符串
    const images = formData.images.join(',')

    // 根据模式调用不同的API
    if (isEditMode.value) {
      // 编辑模式：调用更新接口
      const data = {
        orderId: editId.value,
        faultTypeId: formData.faultTypeId,
        description: formData.description,
        images: images
      }
      await updateRepair(data)
      ElMessage.success('修改成功')
    } else {
      // 新增模式：调用提交接口
      const data = {
        faultTypeId: formData.faultTypeId,
        description: formData.description,
        images: images
      }
      await submitRepair(data)
      ElMessage.success('提交成功')
    }
    // 操作成功后跳转到报修列表页
    router.push('/student/repair/list')
  } catch (error) {
    // axios拦截器已统一处理错误提示
  }
}

// 取消操作，返回上一页
const handleCancel = () => {
  router.back()
}
</script>

<template>
  <div class="submit-repair">
    <el-card>
      <!-- 卡片头部：根据模式显示不同标题 -->
      <template #header>
        <span>{{ isEditMode ? '编辑报修' : '提交报修' }}</span>
      </template>

      <!-- 报修表单 -->
      <el-form
        :model="formData"
        label-width="100px"
        class="repair-form"
      >
        <!-- 故障类型选择器 -->
        <el-form-item label="故障类型">
          <el-select
            v-model="formData.faultTypeId"
            placeholder="请选择故障类型"
            style="width: 100%"
          >
            <el-option
              v-for="item in faultTypes"
              :key="item.id"
              :label="item.typeName + (item.description ? ' - ' + item.description : '')"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 故障地址显示（只读，自动从宿舍信息获取） -->
        <el-form-item label="故障地址">
          <el-input
            :value="dormitoryAddress"
            disabled
          />
        </el-form-item>

        <!-- 故障描述文本域 -->
        <el-form-item label="故障描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述故障情况"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 故障图片上传组件 -->
        <el-form-item label="故障图片">
          <ImageUploader v-model="formData.images" />
          <!--等价于
          <ImageUploader :modelValue="formData.images" 
                          @update:modelValue="formData.images = $event" />
          @ 是 v-on 的缩写，表示监听事件
          当子组件触发 update:modelValue 事件时，自动更新父组件的 formData.images-->
        </el-form-item>

        <!-- 表单操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">
            提交报修
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
/* 报修页面容器样式 */
.submit-repair {
  padding: 20px;
}

/* 表单样式 */
.repair-form {
  margin-top: 20px;
}
</style>