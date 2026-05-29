<!--
  图片上传组件
  
  功能说明：
  1. 支持多图片上传，可设置最大上传数量
  2. 支持图片预览功能
  3. 支持删除已上传图片
  4. 使用 v-model 实现双向绑定
  
  使用示例：
  <ImageUploader
    v-model="imageList"
    :maxCount="3"
    :disabled="false"
  />
-->
<script setup>
import { ref, watch } from 'vue'
import { uploadImage, deleteImage } from '../../api/repair'
import { ElMessage, ElDialog } from 'element-plus'

// 定义组件接收的属性
const props = defineProps({
  // 已上传图片列表，支持双向绑定
  modelValue: {
    type: Array,
    default: () => []
  },                  //图片未上传时，默认值 props.modelValue=[]
  // 最大上传数量限制
  maxCount: {
    type: Number,
    default: 3
  },
  // 是否禁用上传功能
  disabled: {
    type: Boolean,
    default: false
  }
})

// 定义组件事件，用于更新父组件的图片列表
const emit = defineEmits(['update:modelValue']) //事件名 update:modelValue 是 Vue 3 中用于双向绑定的事件名

// 本地图片列表状态
const imageList = ref([...props.modelValue])
// 图片预览对话框显示状态
const imagePreviewVisible = ref(false)
// 当前预览的图片URL
const imagePreviewUrl = ref('')

// 监听父组件传入的图片列表变化，同步到本地状态
watch(() => props.modelValue, (newVal) => {
  imageList.value = [...newVal]
}, { deep: true })

/**
 * 处理图片上传
 * @param {Object} file - Element Plus Upload组件传递的文件对象
 * @returns {Boolean} - 返回false阻止默认上传行为，使用自定义上传逻辑
 */
const handleImageUpload = async (file) => {
  // 检查是否超过最大上传数量
  if (imageList.value.length >= props.maxCount) {
    ElMessage.warning(`最多上传${props.maxCount}张图片`)
    return false
  }
  
  try {
    // 调用API上传图片文件
    const res = await uploadImage(file.raw)
    const imageUrl = res.data
    
    // 将上传成功的图片URL添加到列表
    imageList.value.push(imageUrl)
    
    // 通知父组件更新图片列表
    emit('update:modelValue', [...imageList.value])
    
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
  
  return false
}

/**
 * 处理删除图片
 * @param {Number} index - 要删除的图片在列表中的索引
 */
const handleRemoveImage = async (index) => {
  // 获取要删除的图片URL
  const imageUrl = imageList.value[index]
  // 从URL中提取文件名
  const fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1)
  
  try {
    // 调用API删除服务器上的图片文件
    await deleteImage(fileName)
  } catch (error) {
    // 记录删除失败的错误信息，但不中断本地删除操作
    console.error('删除图片失败:', error)
  } finally {
    // 从本地列表中移除图片
    imageList.value.splice(index, 1)
    // 通知父组件更新图片列表
    emit('update:modelValue', [...imageList.value])
  }
}

/**
 * 处理图片预览
 * @param {String} url - 要预览的图片URL
 */
const handlePreviewImage = (url) => {
  imagePreviewUrl.value = url
  imagePreviewVisible.value = true
}
</script>

<template>
  <div class="image-uploader">
    <!-- 图片上传区域 -->
    <div class="upload-area">
      <el-upload
        class="image-upload"
        action="#"
        :auto-upload="false"
        :show-file-list="false"
        accept="image/*"
        :on-change="handleImageUpload"
        :disabled="disabled || imageList.length >= maxCount"
      >
        <el-button type="primary">
          上传图片
        </el-button>
      </el-upload>
      <span class="upload-tip">支持 JPG、PNG 格式，最多{{ maxCount }}张</span>
    </div>

    <!-- 已上传图片列表展示 -->
    <div v-if="imageList.length > 0" class="image-list">
      <div
        v-for="(image, index) in imageList"
        :key="index"
        class="image-item"
      >
        <!-- 图片缩略图，点击可预览大图 -->
        <img 
          :src="image" 
          :alt="`图片${index + 1}`" 
          class="preview-image"
          @click="handlePreviewImage(image)"
        />
        <!-- 图片操作按钮区域 -->
        <div class="image-actions">
          <span class="delete-btn" @click="handleRemoveImage(index)">删除</span>
        </div>
      </div>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="imagePreviewVisible" title="图片预览" width="80%">
      <img :src="imagePreviewUrl" class="preview-dialog-image" />
    </el-dialog>
  </div>
</template>

<style scoped>
/* 图片上传组件容器 */
.image-uploader {
  width: 100%;
}

/* 上传区域布局 */
.upload-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 上传提示文字样式 */
.upload-tip {
  font-size: 12px;
  color: #999;
}

/* 图片列表容器 */
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

/* 单个图片项容器 */
.image-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
  cursor: pointer;
}

/* 预览图片样式 */
.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 图片操作按钮区域 */
.image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 4px;
  background: rgba(0, 0, 0, 0.5);
}

/* 删除按钮样式 */
.delete-btn {
  padding: 4px 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  font-size: 12px;
  color: #f56c6c;
}

/* 删除按钮悬停效果 */
.delete-btn:hover {
  background: #fff;
}

/* 预览对话框中的图片样式 */
.preview-dialog-image {
  width: 100%;
  height: auto;
}
</style>