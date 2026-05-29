<!--
  待接单列表视图
  
  功能说明：
  1. 展示待接单的报修单列表
  2. 支持接单、拒单操作
  3. 支持分页查看
  
  路由：/repairman/orders/pending
-->
<script setup>
import { onMounted } from 'vue'
import { getRepairList, acceptOrder, rejectOrder } from '../../api/repair'
import { ElMessageBox, ElMessage } from 'element-plus'
import Pagination from '../../components/Common/Pagination.vue'
import { useList } from '../../composables/useList'

const {
  list: orders,
  total,
  pagination,
  loading,
  loadList,
  handlePageChange
} = useList(getRepairList)

const handleAccept = async (row) => {
  try {
    await ElMessageBox.confirm('确定要接这个报修单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await acceptOrder({ orderId: row.id })
    if (response.code === 200) {
      ElMessage.success('接单成功')
      loadList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      // axios拦截器已统一处理错误提示
    }
  }
}

const handleReject = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '拒绝原因不能为空'
    })
    const response = await rejectOrder({ orderId: row.id, reason: reason })
    if (response.code === 200) {
      ElMessage.success('拒单成功')
      loadList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      // axios拦截器已统一处理错误提示
    }
  }
}

onMounted(() => {
  loadList({ status: 2 })
})
</script>

<template>
  <div class="orders-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待接单列表</span>
        </div>
      </template>
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180"></el-table-column>
        <el-table-column prop="faultTypeName" label="故障类型" width="120"></el-table-column>
        <el-table-column label="宿舍" width="150">
          <template #default="{ row }">
            {{ row.dormitoryName || row.buildingName + ' ' + row.roomNumber }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="故障描述" show-overflow-tooltip></el-table-column>
        <el-table-column label="故障图片" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.images && row.images.length > 0"
              :src="row.images.split(',')[0]"
              :preview-src-list="row.images.split(',')"
              fit="cover"
              style="width: 80px; height: 60px; border-radius: 4px;"
            />
            <span v-else style="color: #999;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleAccept(row)">接单</el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">拒单</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        :page-num="pagination.pageNum"
        :page-size="pagination.pageSize"
        @change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<style scoped>
.orders-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
