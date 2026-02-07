<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
      <div class="header-info">
        <h2>缴费管理</h2>
        <p>管理用户的缴费记录，处理水费支付业务</p>
      </div>
      <div class="header-actions">
        <el-button type="warning" @click="handleBatchRemind" :disabled="unpaidCount === 0">
          <el-icon><Bell /></el-icon>
          批量催缴提醒 ({{ unpaidCount }})
        </el-button>
      </div>
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
        <el-form-item label="缴费单号">
          <el-input
            v-model="searchForm.paymentNo"
            placeholder="请输入缴费单号"
            clearable
            :prefix-icon="Search"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="全部方式" clearable style="width: 120px">
            <el-option label="现金" :value="1" />
            <el-option label="微信" :value="2" />
            <el-option label="支付宝" :value="3" />
            <el-option label="银行卡" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已退款" :value="2" />
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
        <el-table-column prop="paymentId" label="ID" width="70" align="center" />
        <el-table-column prop="paymentNo" label="缴费单号" width="180">
          <template #default="{ row }">
            <span class="payment-no">{{ row.paymentNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" width="130">
          <template #default="{ row }">
            <div class="user-info" v-if="getUserInfo(row.userId)">
              <div class="user-avatar">
                <el-icon><User /></el-icon>
              </div>
              <span>{{ getUserInfo(row.userId).realName }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="缴费金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getPaymentMethodType(row.paymentMethod)" 
              effect="light"
              class="method-tag"
            >
              <el-icon class="tag-icon"><component :is="getPaymentMethodIcon(row.paymentMethod)" /></el-icon>
              {{ getPaymentMethodName(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="缴费时间" width="170" align="center">
          <template #default="{ row }">
            <span v-if="row.paymentTime">{{ row.paymentTime }}</span>
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="transactionNo" label="交易流水号" width="180">
          <template #default="{ row }">
            <span class="transaction-no">{{ row.transactionNo || '-' }}</span>
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
              <el-button
                v-if="row.status === 0"
                type="warning"
                link
                @click="handleRemind(row)"
              >
                <el-icon><Bell /></el-icon>
                催缴
              </el-button>
              <el-button type="primary" link @click="handleView(row)">
                <el-icon><View /></el-icon>
                详情
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
    
    <!-- 创建缴费单对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建缴费单"
      width="600px"
      destroy-on-close
      @close="handleCreateDialogClose"
    >
      <el-form
        ref="createFormRef"
        :model="createFormData"
        :rules="createFormRules"
        label-width="120px"
        class="dialog-form"
      >
        <el-form-item label="用水记录" prop="usageId">
          <el-select
            v-model="createFormData.usageId"
            placeholder="请选择用水记录"
            filterable
            style="width: 100%"
            @change="handleUsageChange"
          >
            <el-option
              v-for="usage in usageList"
              :key="usage.usageId"
              :label="`${usage.readMonth} - ${getUserInfo(usage.userId)?.realName || ''} - ¥${usage.amount}`"
              :value="usage.usageId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户">
          <el-input
            :value="getUserInfo(createFormData.userId)?.realName || ''"
            disabled
            placeholder="选择用水记录后自动填充"
          />
        </el-form-item>
        <el-form-item label="缴费金额">
          <el-input
            :value="`¥ ${createFormData.amount.toFixed(2)}`"
            disabled
            class="amount-display"
          />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="createFormData.paymentMethod" class="payment-method-group">
            <el-radio-button :value="1">
              <el-icon><Coin /></el-icon>
              现金
            </el-radio-button>
            <el-radio-button :value="2">
              <el-icon><ChatDotRound /></el-icon>
              微信
            </el-radio-button>
            <el-radio-button :value="3">
              <el-icon><Wallet /></el-icon>
              支付宝
            </el-radio-button>
            <el-radio-button :value="4">
              <el-icon><CreditCard /></el-icon>
              银行卡
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateSubmit" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="缴费详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="缴费单号" :span="2">
          <span class="payment-no">{{ currentPayment.paymentNo }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="缴费金额">
          <span class="amount-value">¥{{ currentPayment.amount?.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag :type="getPaymentMethodType(currentPayment.paymentMethod)" effect="light">
            {{ getPaymentMethodName(currentPayment.paymentMethod) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">
          {{ getUserInfo(currentPayment.userId)?.realName || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentPayment.status)" effect="light">
            {{ getStatusName(currentPayment.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="缴费时间">
          {{ currentPayment.paymentTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="交易流水号">
          {{ currentPayment.transactionNo || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, markRaw } from 'vue'
import { getPaymentPage, createPayment, getPaymentById, sendReminder, batchSendReminder } from '@/api/payment'
import { getUsagePage } from '@/api/waterUsage'
import { getUserPage } from '@/api/user'
import { getMeterPage } from '@/api/waterMeter'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  User,
  View,
  CreditCard,
  Coin,
  ChatDotRound,
  Wallet,
  Bell
} from '@element-plus/icons-vue'

// 计算待缴费数量
const unpaidCount = computed(() => {
  return tableData.value.filter(item => item.status === 0).length
})

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const createFormRef = ref(null)
const userList = ref([])
const meterList = ref([])
const usageList = ref([])

const searchForm = reactive({
  userId: null,
  paymentNo: '',
  paymentMethod: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const createFormData = reactive({
  usageId: null,
  userId: null,
  meterId: null,
  amount: 0,
  paymentMethod: 1
})

const currentPayment = reactive({
  paymentId: null,
  paymentNo: '',
  userId: null,
  meterId: null,
  amount: 0,
  paymentMethod: 1,
  status: 0,
  paymentTime: '',
  transactionNo: ''
})

const createFormRules = {
  usageId: [{ required: true, message: '请选择用水记录', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const getStatusName = (status) => {
  const names = { 0: '待支付', 1: '已支付', 2: '已退款' }
  return names[status] || '未知'
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getStatusClass = (status) => {
  const classes = { 0: 'warning', 1: 'success', 2: 'danger' }
  return classes[status] || ''
}

const getPaymentMethodName = (method) => {
  const names = { 1: '现金', 2: '微信', 3: '支付宝', 4: '银行卡' }
  return names[method] || '未知'
}

const getPaymentMethodType = (method) => {
  const types = { 1: 'info', 2: 'success', 3: 'primary', 4: 'warning' }
  return types[method] || 'info'
}

const getPaymentMethodIcon = (method) => {
  const icons = { 
    1: markRaw(Coin), 
    2: markRaw(ChatDotRound), 
    3: markRaw(Wallet), 
    4: markRaw(CreditCard) 
  }
  return icons[method] || markRaw(CreditCard)
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

const loadUsageList = async () => {
  try {
    const res = await getUsagePage({ page: 1, size: 1000, status: 1 })
    if (res.code === 200) {
      usageList.value = res.data.records.filter(usage => usage.status === 1)
    }
  } catch (error) {
    console.error('加载用水记录列表失败', error)
  }
}

const handleUsageChange = (usageId) => {
  const usage = usageList.value.find(u => u.usageId === usageId)
  if (usage) {
    createFormData.userId = usage.userId
    createFormData.meterId = usage.meterId
    createFormData.amount = usage.amount
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPaymentPage({
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
  searchForm.paymentNo = ''
  searchForm.paymentMethod = null
  searchForm.status = null
  handleSearch()
}

const handleCreate = () => {
  loadUsageList()
  Object.assign(createFormData, {
    usageId: null,
    userId: null,
    meterId: null,
    amount: 0,
    paymentMethod: 1
  })
  createDialogVisible.value = true
}

const handleCreateSubmit = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await createPayment(createFormData)
        if (res.code === 200) {
          ElMessage.success('创建缴费单成功')
          createDialogVisible.value = false
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

const handleCreateDialogClose = () => {
  createFormRef.value?.resetFields()
}

const handleView = async (row) => {
  try {
    const res = await getPaymentById(row.paymentId)
    if (res.code === 200 && res.data) {
      Object.assign(currentPayment, res.data)
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

// 发送单个催缴提醒
const handleRemind = async (row) => {
  const userInfo = getUserInfo(row.userId)
  const userName = userInfo?.realName || '用户'
  
  ElMessageBox.confirm(
    `确定要向 ${userName} 发送缴费提醒吗？欠费金额：¥${row.amount}`,
    '发送催缴提醒',
    {
      confirmButtonText: '发送提醒',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await sendReminder(row.paymentId)
      if (res.code === 200) {
        ElMessage.success(`已向 ${userName} 发送缴费提醒`)
      }
    } catch (error) {
      ElMessage.error('发送失败')
    }
  }).catch(() => {})
}

// 批量发送催缴提醒
const handleBatchRemind = async () => {
  const unpaidList = tableData.value.filter(item => item.status === 0)
  if (unpaidList.length === 0) {
    ElMessage.warning('没有待缴费的记录')
    return
  }
  
  ElMessageBox.confirm(
    `确定要向所有未缴费用户发送催缴提醒吗？共 ${unpaidList.length} 条待缴费记录`,
    '批量催缴提醒',
    {
      confirmButtonText: '全部发送',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const paymentIds = unpaidList.map(item => item.paymentId)
      const res = await batchSendReminder(paymentIds)
      if (res.code === 200) {
        ElMessage.success(`已向 ${unpaidList.length} 位用户发送缴费提醒`)
      }
    } catch (error) {
      ElMessage.error('发送失败')
    }
  }).catch(() => {})
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

.payment-no {
  font-family: 'Monaco', 'Consolas', monospace;
  color: var(--primary-color);
  font-weight: 500;
}

.transaction-no {
  font-family: 'Monaco', 'Consolas', monospace;
  color: var(--text-secondary);
  font-size: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .user-avatar {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 12px;
  }
}

.amount-value {
  font-weight: 700;
  color: #f56c6c;
  font-size: 15px;
}

.method-tag {
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
    
    &.warning {
      background-color: #e6a23c;
      box-shadow: 0 0 0 3px rgba(230, 162, 60, 0.2);
    }
    
    &.success {
      background-color: #52c41a;
      box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.2);
    }
    
    &.danger {
      background-color: #ff4d4f;
      box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.2);
    }
  }
}

.text-secondary {
  color: var(--text-secondary);
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
  
  .payment-method-group {
    :deep(.el-radio-button__inner) {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.amount-display {
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
