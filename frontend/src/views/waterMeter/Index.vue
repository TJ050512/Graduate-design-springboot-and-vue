<template>
  <div class="water-meter-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>水表管理</span>
          <el-button type="primary" @click="handleAdd">添加水表</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="水表编号">
          <el-input v-model="searchForm.meterNo" placeholder="请输入水表编号" clearable />
        </el-form-item>
        <el-form-item label="水表类型">
          <el-select v-model="searchForm.meterType" placeholder="请选择" clearable>
            <el-option label="家用表" :value="1" />
            <el-option label="商用表" :value="2" />
            <el-option label="工业表" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="meterId" label="ID" width="80" />
        <el-table-column prop="meterNo" label="水表编号" />
        <el-table-column prop="meterType" label="水表类型">
          <template #default="{ row }">
            <el-tag v-if="row.meterType === 1" type="success">家用表</el-tag>
            <el-tag v-else-if="row.meterType === 2" type="warning">商用表</el-tag>
            <el-tag v-else type="info">工业表</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentReading" label="当前读数" />
        <el-table-column prop="installAddress" label="安装地址" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMeterPage, addMeter, updateMeter, deleteMeter } from '@/api/waterMeter'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  meterNo: '',
  meterType: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMeterPage({
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.meterNo = ''
  searchForm.meterType = null
  handleSearch()
}

const handleAdd = () => {
  ElMessage.info('添加功能待实现')
}

const handleEdit = (row) => {
  ElMessage.info('编辑功能待实现')
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该水表吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteMeter(row.meterId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.water-meter-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 20px;
  }
}
</style>

