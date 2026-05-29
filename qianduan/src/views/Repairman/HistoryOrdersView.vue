<!--
  维修历史视图
  
  功能说明：
  1. 展示维修员已完成的报修单历史记录
  2. 支持分页查看
  
  路由：/repairman/orders/history
-->
<script setup>
import { onMounted } from 'vue'
import { getRepairList } from '../../api/repair'
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

onMounted(() => {
  loadList({ status: 4 })
})
</script>

<template>
  <div class="orders-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>维修历史</span>
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
              style="width: 80px; height: 60px; border-radius: 4px; z-index: 9999;"
              :z-index="9999"
            />
            <span v-else style="color: #999;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="repairResult" label="维修结果" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180"></el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="$router.push(`/repairman/order/detail/${row.id}`)">查看</el-button>
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
