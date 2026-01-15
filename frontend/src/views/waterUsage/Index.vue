<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #ee9ca7 0%, #ffdde1 100%);">
      <div class="header-info">
        <h2>用水记录</h2>
        <p>管理用户的用水记录，包括抄表数据和费用计算</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加用水记录
      </el-button>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户">
          <el-select
            v-model="searchForm.userId"
            placeholder="请选择用户"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option
              v-for="user in userList"
              :key="user.userId"
              :label="`${user.realName}(${user.username})`"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="水表">
          <el-select
            v-model="searchForm.meterId"
            placeholder="请选择水表"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option
              v-for="meter in meterList"
              :key="meter.meterId"
              :label="meter.meterNo"
              :value="meter.meterId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="抄表月份">
          <el-date-picker
            v-model="searchForm.readMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            clearable
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已缴费" :value="2" />
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
        <el-table-column prop="usageId" label="ID" width="70" align="center" />
        <el-table-column prop="readMonth" label="抄表月份" width="110" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="info">{{ row.readMonth }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="readDate" label="抄表日期" width="110" align="center" />
        <el-table-column label="水表信息" width="140">
          <template #default="{ row }">
            <div class="meter-info" v-if="getMeterInfo(row.meterId)">
              <el-icon><Monitor /></el-icon>
              <span>{{ getMeterInfo(row.meterId).meterNo }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" width="120">
          <template #default="{ row }">
            <div class="user-info" v-if="getUserInfo(row.userId)">
              <el-icon><User /></el-icon>
              <span>{{ getUserInfo(row.userId).realName }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastReading" label="上次读数" width="100" align="right">
          <template #default="{ row }">
            <span class="reading-text">{{ row.lastReading }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="currentReading" label="本次读数" width="100" align="right">
          <template #default="{ row }">
            <span class="reading-text current">{{ row.currentReading }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="usage" label="用水量" width="100" align="right">
          <template #default="{ row }">
            <span class="usage-value">{{ row.usage }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="应缴金额" width="110" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.status)"
              effect="light"
              class="status-tag"
            >
              <span class="status-dot" :class="getStatusClass(row.status)"></span>
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                v-if="row.status === 0"
                type="success"
                link
                @click="handleConfirm(row)"
              >
                <el-icon><CircleCheck /></el-icon>
                确认
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
      width="700px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="130px"
        class="dialog-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="水表" prop="meterId">
              <el-select
                v-model="formData.meterId"
                placeholder="请选择水表"
                filterable
                style="width: 100%"
                @change="handleMeterChange"
              >
                <el-option
                  v-for="meter in meterList"
                  :key="meter.meterId"
                  :label="`${meter.meterNo} - ${meter.installAddress}`"
                  :value="meter.meterId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户" prop="userId">
              <el-select
                v-model="formData.userId"
                placeholder="请选择用户"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="user in userList"
                  :key="user.userId"
                  :label="`${user.realName}(${user.username})`"
                  :value="user.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="抄表月份" prop="readMonth">
              <el-date-picker
                v-model="formData.readMonth"
                type="month"
                placeholder="选择月份"
                format="YYYY-MM"
                value-format="YYYY-MM"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抄表日期" prop="readDate">
              <el-date-picker
                v-model="formData.readDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上次读数" prop="lastReading">
              <el-input-number
                v-model="formData.lastReading"
                :precision="2"
                :min="0"
                style="width: 100%"
                @change="calculateUsage"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="本次读数" prop="currentReading">
              <el-input-number
                v-model="formData.currentReading"
                :precision="2"
                :min="0"
                style="width: 100%"
                @change="calculateUsage"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用水量(m³)">
              <el-input-number
                v-model="formData.usage"
                :precision="2"
                :min="0"
                :disabled="true"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价(元/m³)" prop="price">
              <el-input-number
                v-model="formData.price"
                :precision="2"
                :min="0"
                style="width: 100%"
                @change="calculateAmount"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="应缴金额(元)">
          <el-input-number
            v-model="formData.amount"
            :precision="2"
            :min="0"
            :disabled="true"
            style="width: 100%"
            class="amount-input"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUsagePage, addUsage, updateUsage, confirmUsage, deleteUsage, getUsageById } from '@/api/waterUsage'
import { getUserPage } from '@/api/user'
import { getMeterPage } from '@/api/waterMeter'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  Edit,
  Delete,
  Monitor,
  User,
  CircleCheck
} from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加用水记录')
const formRef = ref(null)
const userList = ref([])
const meterList = ref([])

const searchForm = reactive({
  userId: null,
  meterId: null,
  readMonth: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  usageId: null,
  meterId: null,
  userId: null,
  readerId: null,
  readMonth: '',
  readDate: '',
  lastReading: 0,
  currentReading: 0,
  usage: 0,
  price: 0,
  amount: 0,
  status: 0
})

const formRules = {
  meterId: [{ required: true, message: '请选择水表', trigger: 'change' }],
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  readMonth: [{ required: true, message: '请选择抄表月份', trigger: 'change' }],
  readDate: [{ required: true, message: '请选择抄表日期', trigger: 'change' }],
  lastReading: [{ required: true, message: '请输入上次读数', trigger: 'blur' }],
  currentReading: [{ required: true, message: '请输入本次读数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入水费单价', trigger: 'blur' }]
}

const getStatusName = (status) => {
  const names = { 0: '待确认', 1: '已确认', 2: '已缴费' }
  return names[status] || '未知'
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'info' }
  return types[status] || 'info'
}

const getStatusClass = (status) => {
  const classes = { 0: 'warning', 1: 'success', 2: 'info' }
  return classes[status] || ''
}

const getUserInfo = (userId) => {
  return userList.value.find(u => u.userId === userId)
}

const getMeterInfo = (meterId) => {
  return meterList.value.find(m => m.meterId === meterId)
}

const loadUserList = async () => {
  try {
    const res = await getUserPage({ page: 1, size: 1000 })
    if (res.code === 200) {
      userList.value = res.data.records
    }
  } catch (error) {
    console.error('加载用户列表失败', error)
  }
}

const loadMeterList = async () => {
  try {
    const res = await getMeterPage({ page: 1, size: 1000 })
    if (res.code === 200) {
      meterList.value = res.data.records
    }
  } catch (error) {
    console.error('加载水表列表失败', error)
  }
}

const calculateUsage = () => {
  if (formData.currentReading && formData.lastReading) {
    formData.usage = parseFloat((formData.currentReading - formData.lastReading).toFixed(2))
    if (formData.usage < 0) {
      ElMessage.warning('本次读数不能小于上次读数')
      formData.usage = 0
    }
    calculateAmount()
  }
}

const calculateAmount = () => {
  if (formData.usage && formData.price) {
    formData.amount = parseFloat((formData.usage * formData.price).toFixed(2))
  }
}

const handleMeterChange = (meterId) => {
  const meter = getMeterInfo(meterId)
  if (meter) {
    formData.userId = meter.userId
    formData.lastReading = meter.currentReading || 0
    calculateUsage()
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUsagePage({
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
  searchForm.userId = null
  searchForm.meterId = null
  searchForm.readMonth = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加用水记录'
  Object.assign(formData, {
    usageId: null,
    meterId: null,
    userId: null,
    readerId: null,
    readMonth: '',
    readDate: '',
    lastReading: 0,
    currentReading: 0,
    usage: 0,
    price: 2.8,
    amount: 0,
    status: 0
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑用水记录'
  try {
    const res = await getUsageById(row.usageId)
    if (res.code === 200 && res.data) {
      Object.assign(formData, {
        ...res.data,
        readDate: res.data.readDate || '',
        readMonth: res.data.readMonth || ''
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const handleConfirm = (row) => {
  ElMessageBox.confirm('确定要确认该用水记录吗？', '确认操作', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const res = await confirmUsage(row.usageId)
      if (res.code === 200) {
        ElMessage.success('确认成功')
        loadData()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用水记录吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      const res = await deleteUsage(row.usageId)
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
      if (formData.currentReading <= formData.lastReading) {
        ElMessage.error('本次读数必须大于上次读数')
        return
      }
      
      submitLoading.value = true
      try {
        let res
        if (formData.usageId) {
          res = await updateUsage(formData)
        } else {
          res = await addUsage(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.usageId ? '更新成功' : '添加成功')
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
  loadUserList()
  loadMeterList()
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
  color: #333;
  
  .header-info {
    h2 {
      font-size: 22px;
      font-weight: 600;
      margin-bottom: 6px;
    }
    
    p {
      font-size: 14px;
      opacity: 0.75;
    }
  }
  
  .el-button {
    background: rgba(255, 255, 255, 0.8);
    border-color: rgba(255, 255, 255, 0.9);
    color: #333;
    
    &:hover {
      background: white;
      border-color: white;
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

.meter-info,
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .el-icon {
    color: var(--primary-color);
  }
}

.reading-text {
  font-family: 'Monaco', 'Consolas', monospace;
  color: var(--text-secondary);
  
  &.current {
    color: var(--primary-color);
    font-weight: 500;
  }
}

.usage-value {
  font-weight: 600;
  color: #11998e;
}

.amount-value {
  font-weight: 600;
  color: #f56c6c;
  font-size: 14px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  
  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    
    &.warning {
      background-color: #e6a23c;
      box-shadow: 0 0 0 3px rgba(230, 162, 60, 0.2);
    }
    
    &.success {
      background-color: #52c41a;
      box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.2);
    }
    
    &.info {
      background-color: #909399;
      box-shadow: 0 0 0 3px rgba(144, 147, 153, 0.2);
    }
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 4px;
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
}

.amount-input {
  :deep(.el-input__inner) {
    color: #f56c6c;
    font-weight: 600;
    font-size: 16px;
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
