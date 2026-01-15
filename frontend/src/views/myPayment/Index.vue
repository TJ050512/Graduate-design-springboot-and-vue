<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
      <div class="header-info">
        <h2>我的缴费</h2>
        <p>查看您的缴费记录，进行在线缴费</p>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalCount }}</div>
            <div class="stat-label">缴费记录数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ paidAmount.toFixed(2) }}</div>
            <div class="stat-label">已缴金额</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ unpaidAmount.toFixed(2) }}</div>
            <div class="stat-label">待缴金额</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="缴费单号">
          <el-input
            v-model="searchForm.paymentNo"
            placeholder="请输入缴费单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
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
        <el-table-column prop="paymentNo" label="缴费单号" width="180">
          <template #default="{ row }">
            <span class="payment-no">{{ row.paymentNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="缴费金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getPaymentMethodType(row.paymentMethod)" effect="light">
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
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" effect="light">
              {{ row.status === 1 ? '已支付' : '待支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handlePay(row)"
            >
              <el-icon><CreditCard /></el-icon>
              去支付
            </el-button>
            <el-button v-else type="primary" link @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 支付对话框 -->
    <el-dialog v-model="payDialogVisible" title="确认支付" width="500px">
      <div class="pay-confirm-card">
        <div class="pay-amount">
          <span class="label">支付金额</span>
          <span class="value">¥{{ currentPayment.amount?.toFixed(2) }}</span>
        </div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="缴费单号">{{ currentPayment.paymentNo }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">
            <el-tag :type="getPaymentMethodType(currentPayment.paymentMethod)" effect="light">
              {{ getPaymentMethodName(currentPayment.paymentMethod) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handlePayConfirm" :loading="payLoading">
          <el-icon><CreditCard /></el-icon>
          确认支付
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="缴费详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="缴费单号">{{ currentPayment.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="缴费金额">
          <span class="amount-value">¥{{ currentPayment.amount?.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag :type="getPaymentMethodType(currentPayment.paymentMethod)" effect="light">
            {{ getPaymentMethodName(currentPayment.paymentMethod) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentPayment.status === 1 ? 'success' : 'warning'" effect="light">
            {{ currentPayment.status === 1 ? '已支付' : '待支付' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="缴费时间">{{ currentPayment.paymentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="交易流水号">{{ currentPayment.transactionNo || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getPaymentPage, pay, getPaymentById } from '@/api/payment'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  View,
  CreditCard,
  Tickets,
  CircleCheck,
  Warning
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const payLoading = ref(false)
const tableData = ref([])
const payDialogVisible = ref(false)
const detailDialogVisible = ref(false)

const searchForm = reactive({
  paymentNo: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const currentPayment = reactive({
  paymentId: null,
  paymentNo: '',
  amount: 0,
  paymentMethod: 1,
  status: 0,
  paymentTime: '',
  transactionNo: ''
})

// 统计数据
const totalCount = computed(() => pagination.total)

const paidAmount = computed(() => {
  return tableData.value
    .filter(item => item.status === 1)
    .reduce((sum, item) => sum + (item.amount || 0), 0)
})

const unpaidAmount = computed(() => {
  return tableData.value
    .filter(item => item.status === 0)
    .reduce((sum, item) => sum + (item.amount || 0), 0)
})

const getPaymentMethodName = (method) => {
  const names = { 1: '现金', 2: '微信', 3: '支付宝', 4: '银行卡' }
  return names[method] || '未知'
}

const getPaymentMethodType = (method) => {
  const types = { 1: 'info', 2: 'success', 3: 'primary', 4: 'warning' }
  return types[method] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPaymentPage({
      page: pagination.current,
      size: pagination.size,
      userId: userStore.userInfo?.userId,  // 只查询当前用户的数据
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
  searchForm.paymentNo = ''
  searchForm.status = null
  handleSearch()
}

const handlePay = (row) => {
  Object.assign(currentPayment, { ...row })
  payDialogVisible.value = true
}

const handlePayConfirm = async () => {
  payLoading.value = true
  try {
    const res = await pay(currentPayment.paymentId)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error(error)
  } finally {
    payLoading.value = false
  }
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
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
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
    h2 { font-size: 22px; font-weight: 600; margin-bottom: 6px; }
    p { font-size: 14px; opacity: 0.85; }
  }
}

.stat-row { margin-bottom: 20px; }

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 22px;
  }
  
  .stat-info {
    .stat-value { font-size: 24px; font-weight: 700; color: #303133; }
    .stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
  }
}

.search-card {
  margin-bottom: 20px;
  :deep(.el-card__body) { padding: 20px; }
  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    .el-form-item { margin-bottom: 0; margin-right: 0; }
  }
}

.table-card {
  :deep(.el-card__body) { padding: 0; }
}

.payment-no {
  font-family: 'Monaco', 'Consolas', monospace;
  color: var(--primary-color);
}

.amount-value { font-weight: 700; color: #f56c6c; }
.text-secondary { color: var(--text-secondary); }

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color-light);
}

.pay-confirm-card {
  .pay-amount {
    text-align: center;
    padding: 24px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
    border-radius: 12px;
    margin-bottom: 20px;
    
    .label { display: block; color: #909399; font-size: 14px; margin-bottom: 8px; }
    .value { font-size: 36px; font-weight: 700; color: #f56c6c; }
  }
}
</style>
