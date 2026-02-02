<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
      <div class="header-info">
        <h2>报修工单管理</h2>
        <p>管理用户提交的报修工单，及时处理各类问题</p>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="工单编号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入工单编号"
            clearable
            :prefix-icon="Search"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="报修类型">
          <el-select v-model="searchForm.repairType" placeholder="全部类型" clearable style="width: 130px">
            <el-option label="水表故障" :value="1" />
            <el-option label="漏水" :value="2" />
            <el-option label="水质问题" :value="3" />
            <el-option label="水压异常" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户">
          <el-select
            v-model="searchForm.userId"
            placeholder="请选择用户"
            clearable
            filterable
            style="width: 160px"
          >
            <el-option
              v-for="user in userList"
              :key="user.userId"
              :label="`${user.realName}(${user.username})`"
              :value="user.userId"
            />
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
        <el-table-column prop="orderId" label="ID" width="70" align="center" />
        <el-table-column prop="orderNo" label="工单编号" width="180">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="报修用户" width="120">
          <template #default="{ row }">
            <div class="user-info" v-if="getUserInfo(row.userId)">
              <span>{{ getUserInfo(row.userId).realName }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="报修标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="repairType" label="报修类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRepairTypeTag(row.repairType)" effect="light">
              {{ getRepairTypeName(row.repairType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" effect="dark" size="small">
              {{ getPriorityName(row.priority) }}
            </el-tag>
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
        <el-table-column prop="createTime" label="提交时间" width="170" align="center" />
        <el-table-column label="处理人" width="100" align="center">
          <template #default="{ row }">
            <span v-if="getUserInfo(row.handlerId)">{{ getUserInfo(row.handlerId).realName }}</span>
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link @click="handleView(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button
                v-if="row.status === 0"
                type="success"
                link
                @click="handleStart(row)"
              >
                <el-icon><VideoPlay /></el-icon>
                处理
              </el-button>
              <el-button
                v-if="row.status === 1"
                type="warning"
                link
                @click="handleComplete(row)"
              >
                <el-icon><CircleCheck /></el-icon>
                完成
              </el-button>
              <el-button
                v-if="row.status === 0 || row.status === 1"
                type="danger"
                link
                @click="handleCancel(row)"
              >
                <el-icon><CircleClose /></el-icon>
                取消
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
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="工单详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单编号" :span="2">
          <span class="order-no">{{ currentOrder.orderNo }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="报修标题" :span="2">
          {{ currentOrder.title }}
        </el-descriptions-item>
        <el-descriptions-item label="报修类型">
          <el-tag :type="getRepairTypeTag(currentOrder.repairType)" effect="light">
            {{ getRepairTypeName(currentOrder.repairType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(currentOrder.priority)" effect="dark">
            {{ getPriorityName(currentOrder.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报修用户">
          {{ getUserInfo(currentOrder.userId)?.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)" effect="light">
            {{ getStatusName(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">
          {{ currentOrder.contactName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ currentOrder.contactPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="报修地址" :span="2">
          {{ currentOrder.address || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">
          <div class="description-text">{{ currentOrder.description }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ currentOrder.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="处理人">
          {{ getUserInfo(currentOrder.handlerId)?.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="开始处理时间">
          {{ currentOrder.handleTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="完成时间">
          {{ currentOrder.completeTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2" v-if="currentOrder.handleResult">
          <div class="description-text">{{ currentOrder.handleResult }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="用户评分" v-if="currentOrder.rating">
          <el-rate v-model="currentOrder.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="用户反馈" :span="2" v-if="currentOrder.feedback">
          <div class="description-text">{{ currentOrder.feedback }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 完成工单对话框 -->
    <el-dialog
      v-model="completeDialogVisible"
      title="完成工单"
      width="500px"
    >
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="处理结果" required>
          <el-input
            v-model="completeForm.handleResult"
            type="textarea"
            :rows="4"
            placeholder="请输入处理结果..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCompleteSubmit" :loading="submitLoading">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRepairOrderPage, getRepairOrderById, handleOrder, completeOrder, cancelOrder } from '@/api/repair'
import { getUserPage } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  View,
  VideoPlay,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const detailDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const userList = ref([])

const searchForm = reactive({
  orderNo: '',
  repairType: null,
  status: null,
  userId: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const currentOrder = reactive({
  orderId: null,
  orderNo: '',
  userId: null,
  meterId: null,
  repairType: null,
  title: '',
  description: '',
  contactName: '',
  contactPhone: '',
  address: '',
  status: null,
  priority: null,
  handlerId: null,
  handleTime: '',
  completeTime: '',
  handleResult: '',
  feedback: '',
  rating: null,
  createTime: ''
})

const completeForm = reactive({
  orderId: null,
  handleResult: ''
})

const getRepairTypeName = (type) => {
  const names = { 1: '水表故障', 2: '漏水', 3: '水质问题', 4: '水压异常', 5: '其他' }
  return names[type] || '未知'
}

const getRepairTypeTag = (type) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info', 4: 'primary', 5: '' }
  return types[type] || 'info'
}

const getStatusName = (status) => {
  const names = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已取消' }
  return names[status] || '未知'
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return types[status] || 'info'
}

const getStatusClass = (status) => {
  const classes = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return classes[status] || ''
}

const getPriorityName = (priority) => {
  const names = { 1: '紧急', 2: '普通', 3: '低' }
  return names[priority] || '普通'
}

const getPriorityType = (priority) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'success' }
  return types[priority] || 'warning'
}

const getUserInfo = (userId) => {
  return userList.value.find(u => u.userId === userId)
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

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRepairOrderPage({
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
  searchForm.orderNo = ''
  searchForm.repairType = null
  searchForm.status = null
  searchForm.userId = null
  handleSearch()
}

const handleView = async (row) => {
  try {
    const res = await getRepairOrderById(row.orderId)
    if (res.code === 200 && res.data) {
      Object.assign(currentOrder, res.data)
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const handleStart = async (row) => {
  try {
    await ElMessageBox.confirm('确定开始处理该工单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await handleOrder(row.orderId)
    if (res.code === 200) {
      ElMessage.success('已开始处理')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleComplete = (row) => {
  completeForm.orderId = row.orderId
  completeForm.handleResult = ''
  completeDialogVisible.value = true
}

const handleCompleteSubmit = async () => {
  if (!completeForm.handleResult.trim()) {
    ElMessage.warning('请输入处理结果')
    return
  }
  
  submitLoading.value = true
  try {
    const res = await completeOrder(completeForm.orderId, completeForm.handleResult)
    if (res.code === 200) {
      ElMessage.success('工单已完成')
      completeDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该工单吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await cancelOrder(row.orderId)
    if (res.code === 200) {
      ElMessage.success('工单已取消')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

onMounted(() => {
  loadUserList()
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
}

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

.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.order-no {
  font-family: 'Monaco', 'Consolas', monospace;
  color: #667eea;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
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
    
    &.primary {
      background-color: #409eff;
      box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
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

.text-secondary {
  color: var(--text-secondary);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color-light);
}

.description-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}

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
