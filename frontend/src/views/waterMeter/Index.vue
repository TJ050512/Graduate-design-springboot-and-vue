<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
      <div class="header-info">
        <h2>水表管理</h2>
        <p>管理系统中的水表信息，包括添加、编辑和删除水表</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加水表
      </el-button>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="水表编号">
          <el-input 
            v-model="searchForm.meterNo" 
            placeholder="请输入水表编号" 
            clearable 
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="水表类型">
          <el-select v-model="searchForm.meterType" placeholder="全部类型" clearable style="width: 140px;">
            <el-option label="家用表" :value="1" />
            <el-option label="商用表" :value="2" />
            <el-option label="工业表" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table 
        :data="tableData" 
        v-loading="loading"
        stripe
        highlight-current-row
      >
        <el-table-column prop="meterId" label="ID" width="80" align="center" />
        <el-table-column prop="meterNo" label="水表编号" min-width="140">
          <template #default="{ row }">
            <div class="meter-cell">
              <div class="meter-icon">
                <el-icon><Monitor /></el-icon>
              </div>
              <span class="meter-no">{{ row.meterNo }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="meterType" label="水表类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getMeterTypeTag(row.meterType)" 
              effect="light"
              class="type-tag"
            >
              <el-icon class="tag-icon"><component :is="getMeterTypeIcon(row.meterType)" /></el-icon>
              {{ getMeterTypeName(row.meterType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentReading" label="当前读数" width="120" align="center">
          <template #default="{ row }">
            <span class="reading-value">{{ row.currentReading || 0 }} <small>m³</small></span>
          </template>
        </el-table-column>
        <el-table-column prop="installAddress" label="安装地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 1 ? 'success' : 'danger'"
              effect="light"
              class="status-tag"
            >
              <span class="status-dot" :class="row.status === 1 ? 'active' : 'inactive'"></span>
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="dialog-form"
      >
        <el-form-item label="水表编号" prop="meterNo">
          <el-input 
            v-model="formData.meterNo" 
            placeholder="请输入水表编号"
            :prefix-icon="Monitor"
          />
        </el-form-item>
        <el-form-item label="水表类型" prop="meterType">
          <el-radio-group v-model="formData.meterType" class="type-radio-group">
            <el-radio-button :value="1">
              <el-icon><HomeFilled /></el-icon>
              家用表
            </el-radio-button>
            <el-radio-button :value="2">
              <el-icon><OfficeBuilding /></el-icon>
              商用表
            </el-radio-button>
            <el-radio-button :value="3">
              <el-icon><SetUp /></el-icon>
              工业表
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="安装地址" prop="installAddress">
          <el-input 
            v-model="formData.installAddress" 
            placeholder="请输入安装地址"
            :prefix-icon="Location"
          />
        </el-form-item>
        <el-form-item label="当前读数" prop="currentReading">
          <el-input-number
            v-model="formData.currentReading"
            :precision="2"
            :min="0"
            :step="0.01"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常"
            inactive-text="停用"
            inline-prompt
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, markRaw } from 'vue'
import { getMeterPage, addMeter, updateMeter, deleteMeter } from '@/api/waterMeter'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  Edit,
  Delete,
  Monitor,
  Location,
  HomeFilled,
  OfficeBuilding,
  SetUp
} from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加水表')
const formRef = ref(null)

const searchForm = reactive({
  meterNo: '',
  meterType: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  meterId: null,
  meterNo: '',
  meterType: 1,
  installAddress: '',
  currentReading: 0,
  status: 1
})

const formRules = {
  meterNo: [{ required: true, message: '请输入水表编号', trigger: 'blur' }],
  meterType: [{ required: true, message: '请选择水表类型', trigger: 'change' }],
  installAddress: [{ required: true, message: '请输入安装地址', trigger: 'blur' }]
}

const getMeterTypeName = (type) => {
  const names = { 1: '家用表', 2: '商用表', 3: '工业表' }
  return names[type] || '未知'
}

const getMeterTypeTag = (type) => {
  const tags = { 1: 'success', 2: 'warning', 3: '' }
  return tags[type] || 'info'
}

const getMeterTypeIcon = (type) => {
  const icons = { 
    1: markRaw(HomeFilled), 
    2: markRaw(OfficeBuilding), 
    3: markRaw(SetUp) 
  }
  return icons[type] || markRaw(Monitor)
}

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
  dialogTitle.value = '添加水表'
  Object.assign(formData, {
    meterId: null,
    meterNo: '',
    meterType: 1,
    installAddress: '',
    currentReading: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑水表'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除水表「${row.meterNo}」吗？`,
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
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

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (formData.meterId) {
          res = await updateMeter(formData)
        } else {
          res = await addMeter(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.meterId ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
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
.page-container {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 24px;
  border-radius: 12px;
  color: white;
  
  .header-info {
    h2 {
      font-size: 22px;
      font-weight: 600;
      margin-bottom: 6px;
    }
    
    p {
      font-size: 14px;
      opacity: 0.85;
    }
  }
  
  .el-button {
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.3);
    color: white;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
      border-color: rgba(255, 255, 255, 0.4);
    }
  }
}

// 搜索卡片
.search-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .el-form-item {
      margin-bottom: 0;
      margin-right: 0;
    }
  }
}

// 表格卡片
.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.meter-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .meter-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 14px;
  }
  
  .meter-no {
    font-family: 'Monaco', 'Consolas', monospace;
    font-weight: 500;
  }
}

.reading-value {
  font-weight: 600;
  color: var(--primary-color);
  
  small {
    font-weight: 400;
    color: var(--text-secondary);
    font-size: 12px;
  }
}

.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  
  .tag-icon {
    font-size: 12px;
  }
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  
  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    
    &.active {
      background-color: #52c41a;
      box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.2);
    }
    
    &.inactive {
      background-color: #ff4d4f;
      box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.2);
    }
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

// 分页
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color-light);
}

// 对话框表单
.dialog-form {
  padding: 10px 20px;
  
  .type-radio-group {
    :deep(.el-radio-button__inner) {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .search-form {
    flex-direction: column;
  }
}
</style>
